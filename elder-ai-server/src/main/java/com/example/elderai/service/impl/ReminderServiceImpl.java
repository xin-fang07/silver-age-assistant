package com.example.elderai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.elderai.common.BusinessException;
import com.example.elderai.domain.ReminderSchedule;
import com.example.elderai.dto.ReminderDTO;
import com.example.elderai.entity.Reminder;
import com.example.elderai.entity.ReminderExecution;
import com.example.elderai.entity.FamilyBinding;
import com.example.elderai.entity.Notification;
import com.example.elderai.entity.Device;
import com.example.elderai.infrastructure.lock.DistributedTaskLockService;
import com.example.elderai.mapper.ReminderMapper;
import com.example.elderai.mapper.ReminderExecutionMapper;
import com.example.elderai.mapper.FamilyBindingMapper;
import com.example.elderai.mapper.NotificationMapper;
import com.example.elderai.mapper.DeviceMapper;
import com.example.elderai.service.DevicePushLogService;
import com.example.elderai.service.ReminderService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/** 生活提醒的创建、周期推进和超时处理。 */
@Service
public class ReminderServiceImpl implements ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderServiceImpl.class);
    private static final int PENDING = 0;
    private static final int COMPLETED = 1;
    private static final int EXPIRED = 2;

    @Resource
    private ReminderMapper reminderMapper;

    @Resource private ReminderExecutionMapper executionMapper;
    @Resource private FamilyBindingMapper familyBindingMapper;
    @Resource private NotificationMapper notificationMapper;
    @Resource private DeviceMapper deviceMapper;
    @Resource private DevicePushLogService devicePushLogService;

    @Resource
    private DistributedTaskLockService taskLockService;

    @Resource
    private com.example.elderai.handler.ReminderWebSocketHandler webSocketHandler;

    /** 到点后保留给用户确认完成的时间，默认24小时。 */
    @Value("${app.reminder.expiration-grace-hours:24}")
    private long expirationGraceHours;

    @Value("${app.reminder.check-ms:60000}")
    private long checkIntervalMs;

    @Value("${app.scheduler.lock.reminder-at-most-seconds:300}")
    private long reminderLockAtMostSeconds;

    @Value("${app.reminder.family-alert-consecutive-misses:2}")
    private int familyAlertConsecutiveMisses;

    private final java.util.Set<String> notifiedReminders = 
            java.util.Collections.synchronizedSet(new java.util.LinkedHashSet<>());

    @Override
    public List<Reminder> listByUser(Long userId) {
        return reminderMapper.selectList(new LambdaQueryWrapper<Reminder>()
                .eq(Reminder::getUserId, userId)
                .orderByAsc(Reminder::getStatus)
                .orderByAsc(Reminder::getRemindTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Reminder add(Long userId, ReminderDTO dto) {
        Reminder reminder = new Reminder();
        reminder.setUserId(userId);
        reminder.setFamilyUserId(userId);
        reminder.setElderInfoId(dto.getElderInfoId());
        copyEditableFields(reminder, dto);
        reminder.setStatus(PENDING);
        reminder.setCompletedCount(0);
        reminder.setMissedCount(0);
        reminder.setConsecutiveMissedCount(0);
        reminder.setCreateTime(LocalDateTime.now());
        reminder.setUpdateTime(LocalDateTime.now());
        reminderMapper.insert(reminder);
        log.info("用户 [{}] 创建了{}提醒，标题内容未记录", userId, reminder.getRepeatType());
        return reminder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Reminder update(Long id, Long userId, ReminderDTO dto) {
        Reminder reminder = requireOwned(id, userId);
        copyEditableFields(reminder, dto);
        reminder.setElderInfoId(dto.getElderInfoId());
        reminder.setStatus(PENDING);
        reminder.setUpdateTime(LocalDateTime.now());
        reminderMapper.updateById(reminder);
        log.info("用户 [{}] 修改了提醒 [{}]，标题内容未记录", userId, id);
        return reminder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        requireOwned(id, userId);
        reminderMapper.delete(new LambdaQueryWrapper<Reminder>()
                .eq(Reminder::getId, id)
                .eq(Reminder::getUserId, userId));
        log.info("用户 [{}] 删除了提醒 [{}]", userId, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPaused(Long id, Long userId, boolean paused) {
        Reminder reminder = requireOwned(id, userId);
        if (!paused && reminder.getStatus() != 3) {
            throw new BusinessException(400, "该提醒当前未暂停");
        }
        if (paused && reminder.getStatus() != PENDING) {
            throw new BusinessException(400, "只有待执行提醒可以暂停");
        }
        reminder.setStatus(paused ? 3 : PENDING);
        reminder.setUpdateTime(LocalDateTime.now());
        reminderMapper.updateById(reminder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id, Long userId) {
        Reminder reminder = requireOwned(id, userId);
        if (reminder.getStatus() != PENDING) {
            throw new BusinessException(400, reminder.getStatus() == COMPLETED
                    ? "该提醒已完成，无需重复操作" : "该提醒已超过可处理时间");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledTime = reminder.getRemindTime();
        LambdaUpdateWrapper<Reminder> update = new LambdaUpdateWrapper<>();
        update.eq(Reminder::getId, id)
                .eq(Reminder::getUserId, userId)
                .eq(Reminder::getStatus, PENDING)
                .set(Reminder::getLastCompletedAt, now)
                .set(Reminder::getCompletedCount, safeCount(reminder.getCompletedCount()) + 1)
                .set(Reminder::getConsecutiveMissedCount, 0)
                .set(Reminder::getUpdateTime, now);

        if (ReminderSchedule.recurring(reminder.getRepeatType())) {
            update.set(Reminder::getRemindTime, ReminderSchedule.nextAfter(
                            reminder.getRemindTime(), reminder.getRepeatType(), now))
                    .set(Reminder::getStatus, PENDING);
        } else {
            update.set(Reminder::getStatus, COMPLETED);
        }

        if (reminderMapper.update(null, update) != 1) {
            throw new BusinessException(409, "提醒状态已经变化，请刷新后重试");
        }
        recordExecution(reminder, scheduledTime, "COMPLETED", null, userId, "FAMILY");
        log.info("用户 [{}] 完成了提醒 [{}]", userId, id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void snooze(Long id, Long userId, int minutes) {
        if (minutes < 5 || minutes > 240) {
            throw new BusinessException(400, "稍后提醒时间须在5到240分钟之间");
        }
        Reminder reminder = requireActionable(id, userId);
        LocalDateTime oldTime = reminder.getRemindTime();
        LocalDateTime now = LocalDateTime.now();
        reminder.setRemindTime(now.plusMinutes(minutes));
        reminder.setUpdateTime(now);
        reminderMapper.updateById(reminder);
        recordExecution(reminder, oldTime, "SNOOZED", minutes, userId, "FAMILY");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void skip(Long id, Long userId) {
        Reminder reminder = requireActionable(id, userId);
        LocalDateTime scheduledTime = reminder.getRemindTime();
        LocalDateTime now = LocalDateTime.now();
        reminder.setMissedCount(safeCount(reminder.getMissedCount()) + 1);
        reminder.setConsecutiveMissedCount(safeCount(reminder.getConsecutiveMissedCount()) + 1);
        reminder.setUpdateTime(now);
        if (ReminderSchedule.recurring(reminder.getRepeatType())) {
            reminder.setRemindTime(ReminderSchedule.nextAfter(scheduledTime, reminder.getRepeatType(), now));
            reminder.setStatus(PENDING);
        } else {
            reminder.setStatus(EXPIRED);
        }
        reminderMapper.updateById(reminder);
        recordExecution(reminder, scheduledTime, "SKIPPED", null, userId, "FAMILY");
        notifyFamilyIfNeeded(reminder, "老人主动跳过了提醒");
    }

    @Override
    public Map<String, Object> statistics(Long userId, int days) {
        int safeDays = Math.max(1, Math.min(days, 365));
        LocalDateTime from = LocalDateTime.now().minusDays(safeDays);
        List<ReminderExecution> rows = executionMapper.selectList(
                new LambdaQueryWrapper<ReminderExecution>()
                        .eq(ReminderExecution::getUserId, userId)
                        .ge(ReminderExecution::getScheduledTime, from)
                        .orderByDesc(ReminderExecution::getScheduledTime));
        long completed = rows.stream().filter(e -> "COMPLETED".equals(e.getAction())).count();
        long skipped = rows.stream().filter(e -> "SKIPPED".equals(e.getAction())).count();
        long missed = rows.stream().filter(e -> "MISSED".equals(e.getAction())).count();
        long total = completed + skipped + missed;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("days", safeDays);
        result.put("total", total);
        result.put("completed", completed);
        result.put("skipped", skipped);
        result.put("missed", missed);
        result.put("snoozed", rows.stream().filter(e -> "SNOOZED".equals(e.getAction())).count());
        result.put("completionRate", total == 0 ? 0 : Math.round(completed * 1000.0 / total) / 10.0);
        result.put("medicineTotal", countType(rows, userId, "MEDICINE", false));
        result.put("medicineCompleted", countType(rows, userId, "MEDICINE", true));
        result.put("recentExecutions", rows.stream().limit(20).toList());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncomplete(Long id, Long userId) {
        Reminder reminder = requireOwned(id, userId);
        if (ReminderSchedule.recurring(reminder.getRepeatType())) {
            throw new BusinessException(400, "周期提醒完成后已自动进入下一次，不能撤销");
        }
        if (reminder.getStatus() != COMPLETED) {
            throw new BusinessException(400, "只有已完成提醒可以撤销完成");
        }
        LambdaUpdateWrapper<Reminder> update = new LambdaUpdateWrapper<>();
        update.eq(Reminder::getId, id)
                .eq(Reminder::getUserId, userId)
                .eq(Reminder::getStatus, COMPLETED)
                .set(Reminder::getStatus, PENDING)
                .set(Reminder::getLastCompletedAt, null)
                .set(Reminder::getCompletedCount,
                        Math.max(0, safeCount(reminder.getCompletedCount()) - 1))
                .set(Reminder::getUpdateTime, LocalDateTime.now());
        if (reminderMapper.update(null, update) != 1) {
            throw new BusinessException(409, "提醒状态已经变化，请刷新后重试");
        }
    }

    /** 检查到点提醒并推送，以及过期处理。 */
    @Override
    @Scheduled(fixedDelayString = "${app.reminder.check-ms:60000}")
    @Transactional(rollbackFor = Exception.class)
    public void checkAndNotify() {
        long atLeastMs = Math.max(1_000, checkIntervalMs - 5_000);
        taskLockService.runWithLock("reminder-check",
                Duration.ofSeconds(reminderLockAtMostSeconds),
                Duration.ofMillis(atLeastMs), () -> {
                    processDueReminders();
                    processOverdueReminders();
                });
    }

    private void processDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> dueReminders = reminderMapper.selectList(new LambdaQueryWrapper<Reminder>()
                .eq(Reminder::getStatus, PENDING)
                .le(Reminder::getRemindTime, now));

        for (Reminder reminder : dueReminders) {
            String key = reminder.getUserId() + "-" + reminder.getId() + "-" + reminder.getRemindTime();
            if (notifiedReminders.contains(key)) {
                continue;
            }

            try {
                webSocketHandler.sendReminderToUser(reminder.getUserId(), reminder);
                notifiedReminders.add(key);
                if (notifiedReminders.size() > 1000) {
                    java.util.Iterator<String> iter = notifiedReminders.iterator();
                    for (int i = 0; i < 200 && iter.hasNext(); i++) {
                        iter.next();
                        iter.remove();
                    }
                }
            } catch (Exception e) {
                log.error("推送提醒失败，userId={}, reminderId={}: {}",
                        reminder.getUserId(), reminder.getId(), e.getMessage());
            }
        }
    }

    private void processOverdueReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusHours(expirationGraceHours);
        List<Reminder> overdue = reminderMapper.selectList(new LambdaQueryWrapper<Reminder>()
                .eq(Reminder::getStatus, PENDING)
                .le(Reminder::getRemindTime, cutoff));

        for (Reminder reminder : overdue) {
            int dueOccurrences = ReminderSchedule.dueOccurrences(
                    reminder.getRemindTime(), reminder.getRepeatType(), now);
            LambdaUpdateWrapper<Reminder> update = new LambdaUpdateWrapper<>();
            update.eq(Reminder::getId, reminder.getId())
                    .eq(Reminder::getStatus, PENDING)
                    .eq(Reminder::getRemindTime, reminder.getRemindTime())
                    .set(Reminder::getMissedCount, safeCount(reminder.getMissedCount()) + dueOccurrences)
                    .set(Reminder::getConsecutiveMissedCount,
                            safeCount(reminder.getConsecutiveMissedCount()) + dueOccurrences)
                    .set(Reminder::getUpdateTime, now);
            if (ReminderSchedule.recurring(reminder.getRepeatType())) {
                update.set(Reminder::getRemindTime, ReminderSchedule.nextAfter(
                                reminder.getRemindTime(), reminder.getRepeatType(), now))
                        .set(Reminder::getStatus, PENDING);
            } else {
                update.set(Reminder::getStatus, EXPIRED);
            }
            if (reminderMapper.update(null, update) == 1) {
                LocalDateTime occurrence = reminder.getRemindTime();
                for (int i = 0; i < dueOccurrences; i++) {
                    recordExecution(reminder, occurrence, "MISSED", null,
                            reminder.getUserId(), "SYSTEM");
                    if (ReminderSchedule.recurring(reminder.getRepeatType())) {
                        occurrence = ReminderSchedule.nextAfter(occurrence,
                                reminder.getRepeatType(), occurrence);
                    }
                }
                reminder.setConsecutiveMissedCount(
                        safeCount(reminder.getConsecutiveMissedCount()) + dueOccurrences);
                notifyFamilyIfNeeded(reminder, "提醒连续未确认");
                log.warn("提醒 [{}] 超过 {} 小时未处理，已执行超时策略",
                        reminder.getId(), expirationGraceHours);
            }
        }
    }

    private Reminder requireOwned(Long id, Long userId) {
        Reminder reminder = reminderMapper.selectById(id);
        if (reminder == null || !reminder.getUserId().equals(userId)) {
            throw new BusinessException(404, "提醒不存在");
        }
        return reminder;
    }

    private Reminder requireActionable(Long id, Long userId) {
        Reminder reminder = requireOwned(id, userId);
        if (reminder.getStatus() != PENDING || reminder.getRemindTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException(400, "提醒尚未到点或已经处理");
        }
        return reminder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void simulatePush(Long reminderId) {
        Reminder r = reminderMapper.selectById(reminderId);
        if (r == null) {
            throw new BusinessException(404, "提醒不存在");
        }
        r.setPushStatus("PUSHED");
        r.setPushedAt(LocalDateTime.now());
        Device device = deviceMapper.selectOne(new QueryWrapper<Device>()
                .eq("elder_id", r.getElderInfoId()).last("LIMIT 1"));
        r.setTargetDeviceId(device != null ? device.getDeviceId() : "SIM-DEVICE-DEFAULT");
        r.setUpdateTime(LocalDateTime.now());
        reminderMapper.updateById(r);
        devicePushLogService.record(r.getElderInfoId(), r.getFamilyUserId(), r.getTargetDeviceId(),
                "REMINDER", reminderId, r.getTitle(), r.getContent(), "SENT", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmByElder(Long reminderId) {
        Reminder r = reminderMapper.selectById(reminderId);
        if (r == null) {
            throw new BusinessException(404, "提醒不存在");
        }
        if (!"PUSHED".equals(r.getPushStatus())) {
            // 演示宽容：未推送也允许确认，先置为已推送
            r.setPushStatus("PUSHED");
            if (r.getPushedAt() == null) {
                r.setPushedAt(LocalDateTime.now());
            }
        }
        r.setConfirmStatus("CONFIRMED");
        r.setConfirmedAt(LocalDateTime.now());
        // 闭环：老人确认收到后，提醒置为已完成，从待处理列表移除
        r.setStatus(1);
        r.setUpdateTime(LocalDateTime.now());
        reminderMapper.updateById(r);
        recordExecution(r, r.getRemindTime(), "CONFIRMED_BY_ELDER", null,
                r.getUserId(), "ELDER_DEVICE");
    }

    private void recordExecution(Reminder reminder, LocalDateTime scheduledTime, String action,
                                 Integer snoozeMinutes, Long operatorId, String operatorRole) {
        ReminderExecution e = new ReminderExecution();
        e.setReminderId(reminder.getId());
        e.setUserId(reminder.getUserId());
        e.setScheduledTime(scheduledTime);
        e.setAction(action);
        e.setActionTime(LocalDateTime.now());
        e.setSnoozeMinutes(snoozeMinutes);
        e.setOperatorUserId(operatorId);
        e.setOperatorRole(operatorRole);
        e.setCreateTime(LocalDateTime.now());
        executionMapper.insert(e);
    }

    private void notifyFamilyIfNeeded(Reminder reminder, String reason) {
        int misses = safeCount(reminder.getConsecutiveMissedCount());
        int threshold = reminder.getEscalationThreshold() == null
                ? familyAlertConsecutiveMisses : reminder.getEscalationThreshold();
        if (misses < threshold) return;
        List<FamilyBinding> bindings = familyBindingMapper.selectList(
                new LambdaQueryWrapper<FamilyBinding>()
                        .eq(FamilyBinding::getElderInfoId, reminder.getElderInfoId())
                        .eq(FamilyBinding::getStatus, 1));
        for (FamilyBinding binding : bindings) {
            Notification n = new Notification();
            n.setUserId(binding.getFamilyUserId());
            n.setType("REMINDER");
            n.setTitle("家人提醒连续未确认");
            n.setContent(reason + "：" + reminder.getTitle() + "，累计未完成 " + misses + " 次，请及时关注。");
            n.setRefId(reminder.getId());
            n.setIsRead(0);
            n.setCreateTime(LocalDateTime.now());
            notificationMapper.insert(n);
        }
    }

    private long countType(List<ReminderExecution> rows, Long userId, String type, boolean completedOnly) {
        java.util.Set<Long> ids = new java.util.HashSet<>();
        for (ReminderExecution e : rows) {
            ids.add(e.getReminderId());
        }
        if (ids.isEmpty()) return 0;
        java.util.Set<Long> matching = new java.util.HashSet<>();
        reminderMapper.selectList(new LambdaQueryWrapper<Reminder>()
                .eq(Reminder::getUserId, userId).eq(Reminder::getRemindType, type).in(Reminder::getId, ids))
                .forEach(r -> matching.add(r.getId()));
        return rows.stream().filter(e -> matching.contains(e.getReminderId()))
                .filter(e -> completedOnly ? "COMPLETED".equals(e.getAction())
                        : "COMPLETED".equals(e.getAction()) || "SKIPPED".equals(e.getAction())
                        || "MISSED".equals(e.getAction()))
                .count();
    }

    private void copyEditableFields(Reminder reminder, ReminderDTO dto) {
        reminder.setTitle(dto.getTitle().trim());
        reminder.setContent(dto.getContent() == null ? null : dto.getContent().trim());
        reminder.setEscalationThreshold(dto.getEscalationThreshold() == null
                ? familyAlertConsecutiveMisses : dto.getEscalationThreshold());
        reminder.setRemindType(dto.getRemindType());
        reminder.setRemindTime(dto.getRemindTime());
        reminder.setRepeatType(ReminderSchedule.normalize(dto.getRepeatType()));
    }

    private int safeCount(Integer value) {
        return value == null ? 0 : value;
    }
}

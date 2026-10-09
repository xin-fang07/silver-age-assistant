package com.example.elderai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.elderai.common.BusinessException;
import com.example.elderai.common.PageResult;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.HealthRecord;
import com.example.elderai.entity.HealthWarning;
import com.example.elderai.entity.User;
import com.example.elderai.entity.Notification;
import com.example.elderai.entity.Device;
import com.example.elderai.entity.ElderInfo;
import com.example.elderai.infrastructure.lock.DistributedTaskLockService;
import com.example.elderai.mapper.HealthRecordMapper;
import com.example.elderai.mapper.HealthWarningMapper;
import com.example.elderai.mapper.UserMapper;
import com.example.elderai.mapper.NotificationMapper;
import com.example.elderai.mapper.DeviceMapper;
import com.example.elderai.mapper.ElderInfoMapper;
import com.example.elderai.service.DevicePushLogService;
import com.example.elderai.service.HealthWarningService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 健康异常评估与处理闭环。 */
@Slf4j
@Service
public class HealthWarningServiceImpl implements HealthWarningService {

    private static final int ACTIVE = 0;
    private static final int ACKNOWLEDGED = 1;
    private static final int RESOLVED = 2;

    @Resource
    private HealthRecordMapper healthRecordMapper;

    @Resource
    private HealthWarningMapper healthWarningMapper;

    @Resource
    private UserMapper userMapper;
    @Resource
    private NotificationMapper notificationMapper;

    @Resource
    private DeviceMapper deviceMapper;
    @Resource
    private ElderInfoMapper elderInfoMapper;
    @Resource private DevicePushLogService devicePushLogService;

    @Resource
    private DistributedTaskLockService taskLockService;

    @Value("${app.health.warning.bp-systolic-high:140}")
    private int bpSystolicHigh;
    @Value("${app.health.warning.bp-diastolic-high:90}")
    private int bpDiastolicHigh;
    @Value("${app.health.warning.bp-systolic-low:90}")
    private int bpSystolicLow;
    @Value("${app.health.warning.bp-diastolic-low:60}")
    private int bpDiastolicLow;
    @Value("${app.health.warning.blood-sugar-high:7.0}")
    private double bloodSugarHigh;
    @Value("${app.health.warning.blood-sugar-low:3.9}")
    private double bloodSugarLow;
    @Value("${app.health.warning.heart-rate-high:100}")
    private int heartRateHigh;
    @Value("${app.health.warning.heart-rate-low:60}")
    private int heartRateLow;

    @Value("${app.scheduler.lock.health-warning-at-most-seconds:4200}")
    private long healthWarningLockAtMostSeconds;

    @Value("${app.scheduler.lock.health-warning-at-least-seconds:3300}")
    private long healthWarningLockAtLeastSeconds;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndWarn(Long elderId) {
        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthRecord::getElderInfoId, elderId)
                .orderByDesc(HealthRecord::getCreateTime)
                .last("LIMIT 1");
        HealthRecord record = healthRecordMapper.selectOne(wrapper);
        if (record != null) {
            evaluateRecord(record);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evaluateRecord(HealthRecord record) {
        if (record == null || record.getId() == null) {
            return;
        }
        Map<String, WarningEvaluation> evaluations = evaluate(record);
        List<HealthWarning> existing = healthWarningMapper.selectList(
                new LambdaQueryWrapper<HealthWarning>()
                        .eq(HealthWarning::getRecordId, record.getId()));

        Map<String, HealthWarning> existingByType = new LinkedHashMap<>();
        existing.forEach(item -> existingByType.put(item.getWarningType(), item));
        LocalDateTime now = LocalDateTime.now();

        for (String type : List.of("BLOOD_PRESSURE", "BLOOD_SUGAR", "HEART_RATE")) {
            WarningEvaluation evaluation = evaluations.get(type);
            HealthWarning warning = existingByType.get(type);
            if (evaluation == null) {
                resolveIfActive(warning, now);
                continue;
            }
            if (warning == null) {
                warning = new HealthWarning();
                warning.setUserId(record.getUserId());
                warning.setFamilyId(record.getUserId());
                warning.setElderId(record.getElderInfoId());
                warning.setRecordId(record.getId());
                warning.setWarningType(type);
                warning.setWarningLevel(evaluation.level());
                warning.setWarningContent(evaluation.content());
                warning.setIsRead(0);
                warning.setStatus(ACTIVE);
                warning.setCreateTime(now);
                warning.setUpdateTime(now);
                healthWarningMapper.insert(warning);
                devicePushLogService.record(warning.getElderId(), warning.getUserId(),
                        record.getSourceDeviceId(), "WARNING", warning.getId(),
                        "健康预警", warning.getWarningContent(), "SENT", null);
                notifyReporter(record.getUserId(), warning);
                log.warn("[健康预警] 用户{} 类型{} 等级{}", record.getUserId(),
                        type, evaluation.level());
            } else if (!Objects.equals(warning.getWarningLevel(), evaluation.level())
                    || !Objects.equals(warning.getWarningContent(), evaluation.content())) {
                warning.setWarningLevel(evaluation.level());
                warning.setWarningContent(evaluation.content());
                warning.setStatus(ACTIVE);
                warning.setIsRead(0);
                warning.setActionNote(null);
                warning.setHandledAt(null);
                warning.setUpdateTime(now);
                healthWarningMapper.updateById(warning);
                log.warn("[健康预警更新] 用户{} 类型{} 等级{}", record.getUserId(),
                        type, evaluation.level());
            }
        }
    }

    private Map<String, WarningEvaluation> evaluate(HealthRecord record) {
        Map<String, WarningEvaluation> result = new LinkedHashMap<>();
        Integer sbp = record.getBloodPressureHigh();
        Integer dbp = record.getBloodPressureLow();
        BigDecimal sugar = record.getBloodSugar();
        Integer heartRate = record.getHeartRate();

        if (sbp != null && dbp != null) {
            if (sbp >= bpSystolicHigh || dbp >= bpDiastolicHigh) {
                int level = sbp >= 180 || dbp >= 120 ? 3
                        : (sbp >= 160 || dbp >= 100 ? 2 : 1);
                result.put("BLOOD_PRESSURE", new WarningEvaluation(level,
                        String.format("血压偏高：%d/%d mmHg", sbp, dbp)));
            } else if (sbp < bpSystolicLow || dbp < bpDiastolicLow) {
                int level = sbp < 80 || dbp < 50 ? 2 : 1;
                result.put("BLOOD_PRESSURE", new WarningEvaluation(level,
                        String.format("血压偏低：%d/%d mmHg", sbp, dbp)));
            }
        }

        if (sugar != null) {
            double value = sugar.doubleValue();
            if (value > bloodSugarHigh) {
                int level = value >= 16.7 ? 3 : (value >= 11.1 ? 2 : 1);
                result.put("BLOOD_SUGAR", new WarningEvaluation(level,
                        String.format("血糖偏高：%.1f mmol/L", value)));
            } else if (value < bloodSugarLow) {
                int level = value < 3.0 ? 3 : 2;
                result.put("BLOOD_SUGAR", new WarningEvaluation(level,
                        String.format("血糖偏低：%.1f mmol/L", value)));
            }
        }

        if (heartRate != null) {
            if (heartRate > heartRateHigh) {
                int level = heartRate >= 130 ? 3 : (heartRate > 120 ? 2 : 1);
                result.put("HEART_RATE", new WarningEvaluation(level,
                        String.format("心率偏快：%d 次/分钟", heartRate)));
            } else if (heartRate < heartRateLow) {
                int level = heartRate < 45 ? 3 : (heartRate < 50 ? 2 : 1);
                result.put("HEART_RATE", new WarningEvaluation(level,
                        String.format("心率偏慢：%d 次/分钟", heartRate)));
            }
        }
        return result;
    }

    private void resolveIfActive(HealthWarning warning, LocalDateTime now) {
        if (warning == null || Objects.equals(warning.getStatus(), RESOLVED)) {
            return;
        }
        warning.setStatus(RESOLVED);
        warning.setIsRead(1);
        warning.setActionNote("健康记录调整后已恢复到预设范围");
        warning.setHandledAt(now);
        warning.setUpdateTime(now);
        healthWarningMapper.updateById(warning);
    }

    @Override
    public PageResult<HealthWarning> listByUser(Long userId, PageQueryDTO dto) {
        Page<HealthWarning> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<HealthWarning> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthWarning::getFamilyId, userId)
                .orderByAsc(HealthWarning::getStatus)
                .orderByDesc(HealthWarning::getWarningLevel)
                .orderByDesc(HealthWarning::getCreateTime);
        Page<HealthWarning> result = healthWarningMapper.selectPage(page, wrapper);
        List<HealthWarning> records = result.getRecords();
        records.forEach(w -> w.setElderName(resolveElderName(w.getElderId())));
        return PageResult.pageSuccess(records, result.getTotal(),
                result.getCurrent(), result.getSize());
    }

    @Override
    public PageResult<HealthWarning> listAll(PageQueryDTO dto) {
        Page<HealthWarning> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<HealthWarning> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(HealthWarning::getStatus)
                .orderByDesc(HealthWarning::getWarningLevel)
                .orderByDesc(HealthWarning::getCreateTime);
        Page<HealthWarning> result = healthWarningMapper.selectPage(page, wrapper);
        List<HealthWarning> records = result.getRecords();
        records.forEach(w -> w.setElderName(resolveElderName(w.getElderId())));
        return PageResult.pageSuccess(records, result.getTotal(),
                result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id, Long userId) {
        updateStatus(id, userId, ACKNOWLEDGED, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Long userId, Integer status, String actionNote) {
        if (status == null || (status != ACKNOWLEDGED && status != RESOLVED)) {
            throw new BusinessException(400, "预警状态不合法");
        }
        HealthWarning warning = healthWarningMapper.selectById(id);
        if (warning == null || !warning.getFamilyId().equals(userId)) {
            throw new BusinessException(404, "健康预警不存在");
        }
        if (Objects.equals(warning.getStatus(), RESOLVED)) {
            throw new BusinessException(400, "该预警已处理完成");
        }
        warning.setStatus(status);
        warning.setIsRead(1);
        warning.setActionNote(actionNote == null || actionNote.isBlank()
                ? (status == ACKNOWLEDGED ? "用户已知晓" : "用户确认已处理")
                : actionNote.trim());
        warning.setHandledAt(LocalDateTime.now());
        warning.setUpdateTime(LocalDateTime.now());
        healthWarningMapper.updateById(warning);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRecord(Long recordId, Long userId) {
        healthWarningMapper.delete(new LambdaQueryWrapper<HealthWarning>()
                .eq(HealthWarning::getRecordId, recordId)
                .eq(HealthWarning::getFamilyId, userId));
    }

    @Override
    public int countUnread(Long userId) {
        return healthWarningMapper.selectCount(new LambdaQueryWrapper<HealthWarning>()
                .eq(HealthWarning::getFamilyId, userId)
                .eq(HealthWarning::getStatus, ACTIVE)).intValue();
    }

    /** 每小时兜底复查，实时预警由健康记录保存流程触发。 */
    @Override
    @Scheduled(cron = "${app.health.warning.check-cron:0 0 * * * ?}")
    public void scheduledCheck() {
        taskLockService.runWithLock("health-warning-scan",
                Duration.ofSeconds(healthWarningLockAtMostSeconds),
                Duration.ofSeconds(healthWarningLockAtLeastSeconds), this::scanAllElders);
    }

    private void scanAllElders() {
        List<ElderInfo> elders = elderInfoMapper.selectList(
                new LambdaQueryWrapper<ElderInfo>().eq(ElderInfo::getStatus, 1));
        for (ElderInfo elder : elders) {
            try {
                checkAndWarn(elder.getId());
            } catch (Exception e) {
                log.error("[健康预警] 老人{}检测异常，类型={}",
                        elder.getId(), e.getClass().getSimpleName());
            }
        }
    }

    private Long resolveElderId(HealthRecord record) {
        if (record == null || record.getSourceDeviceId() == null) {
            return null;
        }
        Device device = deviceMapper.selectOne(new LambdaQueryWrapper<Device>()
                .eq(Device::getDeviceId, record.getSourceDeviceId()));
        return device != null ? device.getElderId() : null;
    }

    private String resolveElderName(Long elderId) {
        if (elderId == null) {
            return "未关联老人";
        }
        ElderInfo elderInfo = elderInfoMapper.selectById(elderId);
        if (elderInfo != null) {
            if (elderInfo.getRealName() != null && !elderInfo.getRealName().isBlank()) {
                return elderInfo.getRealName();
            }
            if (elderInfo.getNickname() != null && !elderInfo.getNickname().isBlank()) {
                return elderInfo.getNickname();
            }
            return "未命名老人";
        }
        User user = userMapper.selectById(elderId);
        if (user != null) {
            return user.getUsername();
        }
        return "未关联老人";
    }

    private void notifyReporter(Long userId, HealthWarning warning) {
        if (userId == null || warning == null) {
            return;
        }
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType("WARNING");
        n.setTitle("健康异常预警");
        n.setContent("您照护的老人出现" + warning.getWarningContent());
        n.setRefId(warning.getId());
        n.setIsRead(0);
        n.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(n);
    }

    private record WarningEvaluation(int level, String content) {
    }
}

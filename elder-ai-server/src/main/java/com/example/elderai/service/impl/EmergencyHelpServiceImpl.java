package com.example.elderai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.elderai.common.BusinessException;
import com.example.elderai.common.PageResult;
import com.example.elderai.domain.EmergencyStatus;
import com.example.elderai.dto.EmergencyHelpDTO;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.EmergencyHelp;
import com.example.elderai.infrastructure.lock.DistributedTaskLockService;
import com.example.elderai.mapper.EmergencyHelpMapper;
import com.example.elderai.service.EmergencyHelpService;
import com.example.elderai.service.EmergencyNotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 紧急求助服务实现类
 * <p>
 * 提供紧急求助的创建、查询和处理功能。
 * 老年人用户可发起求助，管理员可查看和处理所有求助请求。
 * </p>
 *
 * @author elder-ai-team
 */
@Service // 标记为 Spring Service Bean
public class EmergencyHelpServiceImpl implements EmergencyHelpService {

    private static final Logger log = LoggerFactory.getLogger(EmergencyHelpServiceImpl.class);
    private static final Pattern IDEMPOTENCY_KEY_PATTERN = Pattern.compile("[A-Za-z0-9._:-]{8,64}");

    /** 紧急求助数据访问层 */
    @Autowired
    private EmergencyHelpMapper emergencyHelpMapper;

    @Autowired
    private EmergencyNotificationService emergencyNotificationService;

    @Autowired
    private DistributedTaskLockService taskLockService;

    @Autowired
    private com.example.elderai.mapper.UserMapper userMapper;

    @Value("${app.emergency.escalation-minutes:5}")
    private long escalationMinutes;

    @Value("${app.emergency.max-escalation-level:3}")
    private int maxEscalationLevel;

    @Value("${app.emergency.escalation-check-ms:60000}")
    private long escalationCheckMs;

    @Value("${app.scheduler.lock.emergency-at-most-seconds:300}")
    private long emergencyLockAtMostSeconds;

    // ==================== 创建求助 ====================

    /**
     * 创建紧急求助
     * <p>
     * 老年人用户发起紧急求助，初始状态为"待处理"（status=0）。
     * 记录求助人、联系方式和求助内容。
     * </p>
     *
     * @param userId 当前登录用户ID
     * @param dto    紧急求助参数
     * @return 创建成功的求助记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmergencyHelp createHelp(Long userId, EmergencyHelpDTO dto, String idempotencyKey) {
        String requestId = normalizeIdempotencyKey(idempotencyKey);
        EmergencyHelp existing = findByRequestId(userId, requestId);
        if (existing != null) {
            log.info("用户 [{}] 的紧急求助幂等命中，求助ID: {}", userId, existing.getId());
            return existing;
        }

        // 构建 EmergencyHelp 实体并设置字段
        EmergencyHelp help = new EmergencyHelp();
        help.setUserId(userId);
        help.setRequestId(requestId);
        help.setContactName(dto.getContactName() == null || dto.getContactName().isBlank()
                ? "未设置" : dto.getContactName().trim());
        help.setContactPhone(dto.getContactPhone() == null || dto.getContactPhone().isBlank()
                ? "未设置" : dto.getContactPhone().trim());
        help.setContactEmail(dto.getContactEmail());
        help.setHelpContent(dto.getHelpContent());
        help.setLatitude(dto.getLatitude());
        help.setLongitude(dto.getLongitude());
        help.setLocationAccuracy(dto.getLocationAccuracy());
        help.setLocationText(dto.getLocationText());
        help.setStatus(EmergencyStatus.SUBMITTED);
        help.setNotificationStatus(0);
        help.setEscalationLevel(0);
        help.setCreateTime(LocalDateTime.now());
        help.setUpdateTime(LocalDateTime.now());

        // 插入数据库
        try {
            emergencyHelpMapper.insert(help);
        } catch (DuplicateKeyException ex) {
            EmergencyHelp concurrent = findByRequestId(userId, requestId);
            if (concurrent != null) {
                log.info("用户 [{}] 的并发紧急求助已去重，求助ID: {}", userId, concurrent.getId());
                return concurrent;
            }
            throw ex;
        }
        emergencyNotificationService.notifyCreated(help);
        log.warn("用户 [{}] 发起了紧急求助！求助ID: {}", userId, help.getId());

        return help;
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        String normalized = idempotencyKey.trim();
        if (!IDEMPOTENCY_KEY_PATTERN.matcher(normalized).matches()) {
            throw new BusinessException(400, "Idempotency-Key 格式不正确");
        }
        return normalized;
    }

    private EmergencyHelp findByRequestId(Long userId, String requestId) {
        if (requestId == null) {
            return null;
        }
        return emergencyHelpMapper.selectOne(new LambdaQueryWrapper<EmergencyHelp>()
                .eq(EmergencyHelp::getUserId, userId)
                .eq(EmergencyHelp::getRequestId, requestId)
                .last("LIMIT 1"));
    }

    // ==================== 用户查询求助记录 ====================

    /**
     * 查询当前用户的所有求助记录
     * <p>
     * 按创建时间倒序排列，最新的求助排在最前面。
     * </p>
     *
     * @param userId 当前登录用户ID
     * @return 求助记录列表
     */
    @Override
    public List<EmergencyHelp> listByUser(Long userId) {
        // 构建查询条件：限定用户，按创建时间倒序
        LambdaQueryWrapper<EmergencyHelp> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmergencyHelp::getUserId, userId);
        wrapper.orderByDesc(EmergencyHelp::getCreateTime);

        return emergencyHelpMapper.selectList(wrapper);
    }

    @Override
    public EmergencyHelp getById(Long id) {
        EmergencyHelp help = emergencyHelpMapper.selectById(id);
        if (help == null) throw new BusinessException(404, "求助记录不存在");
        return help;
    }

    // ==================== 管理员查询所有求助 ====================

    /**
     * 管理员查看所有求助记录（分页）
     * <p>
     * 不限制用户，管理员可以看到系统中所有的紧急求助记录。
     * 按创建时间倒序排列。
     * </p>
     *
     * @param dto 分页查询参数
     * @return 分页结果，包含求助记录列表和分页信息
     */
    @Override
    public PageResult<EmergencyHelp> listAll(PageQueryDTO dto, Integer status) {
        // 1. 构建分页对象
        Page<EmergencyHelp> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        // 2. 构建查询条件：不限制用户，按创建时间倒序
        LambdaQueryWrapper<EmergencyHelp> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(EmergencyHelp::getStatus, status);
        }

        // 支持关键字搜索（匹配联系人姓名或求助内容）
        if (dto.getKeyword() != null && !dto.getKeyword().trim().isEmpty()) {
            String keyword = dto.getKeyword().trim();
            wrapper.and(w -> w
                    .like(EmergencyHelp::getContactName, keyword)
                    .or()
                    .like(EmergencyHelp::getHelpContent, keyword)
            );
        }

        wrapper.orderByDesc(EmergencyHelp::getCreateTime);

        // 3. 执行分页查询
        Page<EmergencyHelp> resultPage = emergencyHelpMapper.selectPage(page, wrapper);

        // 4. 填充老人电话号码
        for (EmergencyHelp help : resultPage.getRecords()) {
            com.example.elderai.entity.User user = userMapper.selectById(help.getUserId());
            if (user != null && user.getPhone() != null && !user.getPhone().isBlank()) {
                help.setElderPhone(user.getPhone());
            }
        }

        // 5. 构建分页返回结果
        return PageResult.pageSuccess(
                resultPage.getRecords(),
                resultPage.getTotal(),
                resultPage.getCurrent(),
                resultPage.getSize()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transition(Long id, Long operatorId, Integer targetStatus, String remark) {
        EmergencyHelp help = emergencyHelpMapper.selectById(id);
        if (help == null) {
            throw new BusinessException(404, "求助记录不存在");
        }
        EmergencyStatus.requireTransition(help.getStatus(), targetStatus);
        if (targetStatus == EmergencyStatus.COMPLETED && (remark == null || remark.isBlank())) {
            throw new BusinessException(400, "完成求助时必须填写处理备注");
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<EmergencyHelp> update = new LambdaUpdateWrapper<>();
        update.eq(EmergencyHelp::getId, id)
                .eq(EmergencyHelp::getStatus, help.getStatus())
                .set(EmergencyHelp::getStatus, targetStatus)
                .set(EmergencyHelp::getUpdateTime, now);
        if (remark != null && !remark.isBlank()) {
            update.set(EmergencyHelp::getHandleRemark, remark.trim());
        }
        if (targetStatus == EmergencyStatus.ACKNOWLEDGED) {
            update.set(EmergencyHelp::getAcknowledgedBy, operatorId)
                    .set(EmergencyHelp::getAcknowledgedRole, "ADMIN")
                    .set(EmergencyHelp::getAcknowledgedName, "平台工作人员")
                    .set(EmergencyHelp::getAcknowledgedAt, now);
        } else if (targetStatus == EmergencyStatus.PROCESSING) {
            update.set(EmergencyHelp::getProcessingAt, now);
        } else if (targetStatus == EmergencyStatus.COMPLETED) {
            update.set(EmergencyHelp::getCompletedAt, now);
        }
        if (emergencyHelpMapper.update(null, update) != 1) {
            throw new BusinessException(409, "求助状态已被其他工作人员更新，请刷新后重试");
        }
        log.warn("管理员 [{}] 将求助 [{}] 状态推进为 {}", operatorId, id, targetStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acknowledgeByFamily(Long id, Long familyUserId, String familyName) {
        EmergencyHelp help = getById(id);
        EmergencyStatus.requireTransition(help.getStatus(), EmergencyStatus.ACKNOWLEDGED);
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<EmergencyHelp> update = new LambdaUpdateWrapper<>();
        update.eq(EmergencyHelp::getId, id)
                .eq(EmergencyHelp::getStatus, help.getStatus())
                .set(EmergencyHelp::getStatus, EmergencyStatus.ACKNOWLEDGED)
                .set(EmergencyHelp::getAcknowledgedBy, familyUserId)
                .set(EmergencyHelp::getAcknowledgedRole, "FAMILY")
                .set(EmergencyHelp::getAcknowledgedName,
                        familyName == null || familyName.isBlank() ? "家属" : familyName)
                .set(EmergencyHelp::getAcknowledgedAt, now)
                .set(EmergencyHelp::getHandleRemark, "家属已确认收到求助")
                .set(EmergencyHelp::getUpdateTime, now);
        if (emergencyHelpMapper.update(null, update) != 1) {
            throw new BusinessException(409, "求助已被其他人员接单，请刷新后查看");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, Long userId) {
        EmergencyHelp help = emergencyHelpMapper.selectById(id);
        if (help == null || !help.getUserId().equals(userId)) {
            throw new BusinessException(404, "求助记录不存在");
        }
        EmergencyStatus.requireTransition(help.getStatus(), EmergencyStatus.CANCELLED);
        help.setStatus(EmergencyStatus.CANCELLED);
        help.setUpdateTime(LocalDateTime.now());
        emergencyHelpMapper.updateById(help);
    }

    @Override
    @Scheduled(fixedDelayString = "${app.emergency.escalation-check-ms:60000}")
    @Transactional(rollbackFor = Exception.class)
    public void escalateOverdue() {
        long atLeastMs = Math.max(1_000, escalationCheckMs - 5_000);
        taskLockService.runWithLock("emergency-escalation",
                Duration.ofSeconds(emergencyLockAtMostSeconds),
                Duration.ofMillis(atLeastMs), this::processOverdueEmergencies);
    }

    private void processOverdueEmergencies() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(escalationMinutes);
        LambdaQueryWrapper<EmergencyHelp> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(EmergencyHelp::getStatus, EmergencyStatus.SUBMITTED, EmergencyStatus.ESCALATED)
                .lt(EmergencyHelp::getEscalationLevel, maxEscalationLevel)
                .le(EmergencyHelp::getUpdateTime, cutoff);

        for (EmergencyHelp help : emergencyHelpMapper.selectList(wrapper)) {
            int oldStatus = help.getStatus();
            int nextLevel = (help.getEscalationLevel() == null ? 0 : help.getEscalationLevel()) + 1;
            LocalDateTime now = LocalDateTime.now();
            LambdaUpdateWrapper<EmergencyHelp> update = new LambdaUpdateWrapper<>();
            update.eq(EmergencyHelp::getId, help.getId())
                    .eq(EmergencyHelp::getStatus, oldStatus)
                    .set(EmergencyHelp::getStatus, EmergencyStatus.ESCALATED)
                    .set(EmergencyHelp::getEscalationLevel, nextLevel)
                    .set(EmergencyHelp::getEscalatedAt, now)
                    .set(EmergencyHelp::getUpdateTime, now);
            if (emergencyHelpMapper.update(null, update) == 1) {
                help.setStatus(EmergencyStatus.ESCALATED);
                help.setEscalationLevel(nextLevel);
                help.setEscalatedAt(now);
                emergencyNotificationService.recordEscalation(help);
                emergencyNotificationService.notifyEscalation(help);
                log.error("紧急求助 [{}] 超时未接单，已升级至第 {} 级", help.getId(), nextLevel);
            }
        }
    }
}

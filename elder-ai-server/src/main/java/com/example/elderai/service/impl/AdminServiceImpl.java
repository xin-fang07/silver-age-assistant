package com.example.elderai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.elderai.common.BusinessException;
import com.example.elderai.common.PageResult;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.EmergencyHelp;
import com.example.elderai.entity.Reminder;
import com.example.elderai.entity.User;
import com.example.elderai.entity.ElderInfo;
import com.example.elderai.entity.Device;
import com.example.elderai.entity.HealthWarning;
import com.example.elderai.entity.HealthRecord;
import com.example.elderai.mapper.EmergencyHelpMapper;
import com.example.elderai.mapper.ReminderMapper;
import com.example.elderai.mapper.ChatRecordMapper;
import com.example.elderai.mapper.UserMapper;
import com.example.elderai.mapper.SystemLogMapper;
import com.example.elderai.mapper.ElderInfoMapper;
import com.example.elderai.mapper.DeviceMapper;
import com.example.elderai.mapper.HealthWarningMapper;
import com.example.elderai.mapper.HealthRecordMapper;
import com.example.elderai.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员服务实现类
 * <p>
 * 提供管理员专属功能：用户管理（查询、禁用/启用）和控制台数据统计。
 * 控制台统计包括总用户数、今日问答数、待处理求助数和本周提醒数。
 * </p>
 *
 * @author elder-ai-team
 */
@Service // 标记为 Spring Service Bean
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    /** 用户数据访问层 */
    @Autowired
    private UserMapper userMapper;

    /** 聊天记录数据访问层 */
    @Autowired
    private ChatRecordMapper chatRecordMapper;

    /** 紧急求助数据访问层 */
    @Autowired
    private EmergencyHelpMapper emergencyHelpMapper;

    /** 提醒事项数据访问层 */
    @Autowired
    private ReminderMapper reminderMapper;

    @Autowired
    private SystemLogMapper systemLogMapper;

    @Autowired
    private ElderInfoMapper elderInfoMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private HealthWarningMapper healthWarningMapper;

    @Autowired
    private HealthRecordMapper healthRecordMapper;

    // ==================== 分页查询用户 ====================

    /**
     * 分页查询所有用户
     * <p>
     * 管理员可查看系统中所有注册用户（包括老年人和管理员角色）。
     * 支持按用户名进行模糊搜索。
     * 返回的用户信息中不包含密码字段（由前端或序列化配置处理）。
     * </p>
     *
     * @param dto 分页查询参数（支持 keyword 搜索用户名）
     * @return 分页结果，包含用户列表和分页信息
     */
    @Override
    public PageResult<User> listUsers(PageQueryDTO dto) {
        // 1. 构建分页对象
        Page<User> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 支持按用户名模糊搜索
        if (dto.getKeyword() != null && !dto.getKeyword().trim().isEmpty()) {
            wrapper.like(User::getUsername, dto.getKeyword().trim());
        }

        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);

        // 3. 执行分页查询
        Page<User> resultPage = userMapper.selectPage(page, wrapper);

        // 4. 安全处理：清除返回结果中的密码字段
        //    防止密码被泄露到前端
        for (User user : resultPage.getRecords()) {
            user.setPassword(null);
        }

        // 5. 构建分页返回结果
        return PageResult.pageSuccess(
                resultPage.getRecords(),
                resultPage.getTotal(),
                resultPage.getCurrent(),
                resultPage.getSize()
        );
    }

    // ==================== 禁用/启用用户 ====================

    /**
     * 禁用/启用用户
     * <p>
     * 管理员可以修改用户的状态（0=禁用, 1=正常）。
     * 禁用后用户将无法登录系统。
     * </p>
     *
     * @param userId 目标用户ID
     * @param status 目标状态：0-禁用, 1-正常
     * @throws BusinessException 用户不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        // 1. 查询用户确认存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 2. 更新状态
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 3. 记录操作日志
        String statusDesc = status == 1 ? "启用" : "禁用";
        log.info("管理员{}了用户 [{}]: {}", statusDesc, userId, user.getUsername());
    }

    // ==================== 控制台统计 ====================

    /**
     * 获取控制台统计数据
     * <p>
     * 统计以下4个关键指标供管理后台首页展示：
     * <ul>
     *   <li><b>userCount</b>：系统中所有注册用户总数</li>
     *   <li><b>todayChatCount</b>：今日 AI 对话次数（今天 00:00:00 至 23:59:59）</li>
     *   <li><b>pendingEmergencyCount</b>：当前待处理的紧急求助数量（status=0）</li>
     *   <li><b>weekReminderCount</b>：本周内创建的提醒数量（周一 00:00 至周日 23:59）</li>
     * </ul>
     * </p>
     *
     * @return 统计数据 Map，键名分别为 userCount、todayChatCount、pendingEmergencyCount、weekReminderCount
     */
    @Override
    public Map<String, Object> getDashboardStats() {
        Long userCount = userMapper.selectCount(null);
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(today, LocalTime.MAX);

        LambdaQueryWrapper<com.example.elderai.entity.ChatRecord> chatWrapper = new LambdaQueryWrapper<>();
        chatWrapper.between(com.example.elderai.entity.ChatRecord::getCreateTime, todayStart, todayEnd);
        Long todayChatCount = chatRecordMapper.selectCount(chatWrapper);

        LambdaQueryWrapper<EmergencyHelp> emergencyWrapper = new LambdaQueryWrapper<>();
        emergencyWrapper.in(EmergencyHelp::getStatus, 0, 1, 2, 5);
        Long pendingEmergencyCount = emergencyHelpMapper.selectCount(emergencyWrapper);

        LocalDate monday = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate sunday = today.with(java.time.DayOfWeek.SUNDAY);
        LocalDateTime weekStart = LocalDateTime.of(monday, LocalTime.MIN);
        LocalDateTime weekEnd = LocalDateTime.of(sunday, LocalTime.MAX);

        LambdaQueryWrapper<Reminder> reminderWrapper = new LambdaQueryWrapper<>();
        reminderWrapper.between(Reminder::getCreateTime, weekStart, weekEnd);
        Long weekReminderCount = reminderMapper.selectCount(reminderWrapper);

        LocalDate trendStartDate = today.minusDays(6);
        LocalDateTime trendStart = LocalDateTime.of(trendStartDate, LocalTime.MIN);

        Map<String, Long> chatByDay = countByDay(chatRecordMapper.selectMaps(
                new QueryWrapper<com.example.elderai.entity.ChatRecord>()
                        .select("DATE(create_time) AS day", "COUNT(*) AS count")
                        .ge("create_time", trendStart)
                        .groupBy("DATE(create_time)")), "count");

        Map<String, Long> activeUsersByDay = countByDay(systemLogMapper.selectMaps(
                new QueryWrapper<com.example.elderai.entity.SystemLog>()
                        .select("DATE(create_time) AS day", "COUNT(DISTINCT user_id) AS count")
                        .ge("create_time", trendStart)
                        .isNotNull("user_id")
                        .groupBy("DATE(create_time)")), "count");

        List<Map<String, Object>> reminderRows = reminderMapper.selectMaps(
                new QueryWrapper<Reminder>()
                        .select("DATE(remind_time) AS day", "COUNT(*) AS total",
                                "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS completed")
                        .ge("remind_time", trendStart)
                        .le("remind_time", todayEnd)
                        .groupBy("DATE(remind_time)"));
        Map<String, Long> reminderTotals = countByDay(reminderRows, "total");
        Map<String, Long> reminderCompleted = countByDay(reminderRows, "completed");

        Map<String, Long> roleDistribution = new LinkedHashMap<>();
        for (Map<String, Object> row : userMapper.selectMaps(new QueryWrapper<User>()
                .select("role", "COUNT(*) AS count").groupBy("role"))) {
            roleDistribution.put(String.valueOf(row.get("role")), number(row.get("count")));
        }

        Long elderCount = elderInfoMapper.selectCount(null);
        Long familyCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, "FAMILY"));
        Long deviceTotalCount = deviceMapper.selectCount(null);
        Long deviceOnlineCount = deviceMapper.selectCount(new LambdaQueryWrapper<Device>().eq(Device::getStatus, 1));

        Map<String, Long> warningDistribution = new LinkedHashMap<>();
        warningDistribution.put("level1", 0L);
        warningDistribution.put("level2", 0L);
        warningDistribution.put("level3", 0L);
        for (Map<String, Object> row : healthWarningMapper.selectMaps(new QueryWrapper<HealthWarning>()
                .select("warning_level", "COUNT(*) AS count").groupBy("warning_level"))) {
            Integer level = (int) number(row.get("warning_level"));
            warningDistribution.put("level" + level, number(row.get("count")));
        }
        Long totalWarningCount = warningDistribution.values().stream().mapToLong(Long::longValue).sum();

        List<Map<String, Object>> healthRows = healthRecordMapper.selectMaps(
                new QueryWrapper<HealthRecord>()
                        .select("DATE(create_time) AS day",
                                "AVG(blood_pressure_high) AS bp_high",
                                "AVG(blood_pressure_low) AS bp_low",
                                "AVG(blood_sugar) AS blood_sugar",
                                "AVG(heart_rate) AS heart_rate")
                        .ge("create_time", trendStart)
                        .le("create_time", todayEnd)
                        .groupBy("DATE(create_time)"));

        Map<String, Map<String, Long>> healthTrend = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = trendStartDate.plusDays(i);
            String key = day.toString();
            Map<String, Long> dayData = new LinkedHashMap<>();
            dayData.put("bloodPressure", 0L);
            dayData.put("bloodSugar", 0L);
            dayData.put("heartRate", 0L);
            healthTrend.put(key, dayData);
        }
        for (Map<String, Object> row : healthRows) {
            String day = String.valueOf(row.get("day"));
            if (healthTrend.containsKey(day)) {
                long bpHigh = Math.round(number(row.get("bp_high")));
                long bpLow = Math.round(number(row.get("bp_low")));
                healthTrend.get(day).put("bloodPressure", bpHigh > 0 ? bpHigh : bpLow > 0 ? bpLow : 0L);
                healthTrend.get(day).put("bloodSugar", (long) Math.round(number(row.get("blood_sugar"))));
                healthTrend.get(day).put("heartRate", (long) Math.round(number(row.get("heart_rate"))));
            }
        }

        List<String> trendDates = new ArrayList<>();
        List<Long> chatTrend = new ArrayList<>();
        List<Long> activeUserTrend = new ArrayList<>();
        List<Long> reminderCompletionTrend = new ArrayList<>();
        List<Long> bloodPressureTrend = new ArrayList<>();
        List<Long> bloodSugarTrend = new ArrayList<>();
        List<Long> heartRateTrend = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate day = trendStartDate.plusDays(i);
            String key = day.toString();
            trendDates.add(day.getMonthValue() + "月" + day.getDayOfMonth() + "日");
            chatTrend.add(chatByDay.getOrDefault(key, 0L));
            activeUserTrend.add(activeUsersByDay.getOrDefault(key, 0L));
            long total = reminderTotals.getOrDefault(key, 0L);
            long completed = reminderCompleted.getOrDefault(key, 0L);
            reminderCompletionTrend.add(total == 0 ? 0L : Math.round(completed * 100.0 / total));
            Map<String, Long> h = healthTrend.getOrDefault(key, new HashMap<>());
            bloodPressureTrend.add(h.getOrDefault("bloodPressure", 0L));
            bloodSugarTrend.add(h.getOrDefault("bloodSugar", 0L));
            heartRateTrend.add(h.getOrDefault("heartRate", 0L));
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userCount);
        stats.put("todayChatCount", todayChatCount);
        stats.put("pendingEmergencyCount", pendingEmergencyCount);
        stats.put("weekReminderCount", weekReminderCount);
        stats.put("roleDistribution", roleDistribution);
        stats.put("trendDates", trendDates);
        stats.put("chatTrend", chatTrend);
        stats.put("activeUserTrend", activeUserTrend);
        stats.put("reminderCompletionTrend", reminderCompletionTrend);
        stats.put("elderCount", elderCount);
        stats.put("familyCount", familyCount);
        stats.put("deviceTotalCount", deviceTotalCount);
        stats.put("deviceOnlineCount", deviceOnlineCount);
        stats.put("warningDistribution", warningDistribution);
        stats.put("totalWarningCount", totalWarningCount);
        stats.put("bloodPressureTrend", bloodPressureTrend);
        stats.put("bloodSugarTrend", bloodSugarTrend);
        stats.put("heartRateTrend", heartRateTrend);

        log.info("控制台统计数据: {}", stats);
        return stats;
    }

    private Map<String, Long> countByDay(List<Map<String, Object>> rows, String valueKey) {
        Map<String, Long> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object day = row.get("day");
            if (day != null) {
                result.put(day.toString(), number(row.get(valueKey)));
            }
        }
        return result;
    }

    private long number(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }
}

package com.example.elderai.controller;

import com.example.elderai.common.PageResult;
import com.example.elderai.common.Result;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.EmergencyHelp;
import com.example.elderai.entity.HealthRecord;
import com.example.elderai.entity.HealthWarning;
import com.example.elderai.entity.Reminder;
import com.example.elderai.entity.ElderInfo;
import com.example.elderai.entity.Device;
import com.example.elderai.security.SecurityUtils;
import com.example.elderai.security.AuthenticatedUser;
import com.example.elderai.common.BusinessException;
import com.example.elderai.service.EmergencyHelpService;
import com.example.elderai.service.FamilyBindingService;
import com.example.elderai.service.HealthRecordService;
import com.example.elderai.service.HealthWarningService;
import com.example.elderai.service.ReminderService;
import com.example.elderai.dto.ReminderDTO;
import com.example.elderai.mapper.HealthRecordMapper;
import com.example.elderai.mapper.HealthWarningMapper;
import com.example.elderai.mapper.EmergencyHelpMapper;
import com.example.elderai.mapper.UserMapper;
import com.example.elderai.mapper.DeviceMapper;
import com.example.elderai.mapper.ReminderExecutionMapper;
import com.example.elderai.mapper.ElderInfoMapper;
import com.example.elderai.entity.ReminderExecution;
import com.example.elderai.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.validation.Valid;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/family")
public class FamilyController {

    @Resource
    private FamilyBindingService familyBindingService;
    @Resource
    private HealthRecordService healthRecordService;
    @Resource
    private EmergencyHelpService emergencyHelpService;
    @Resource
    private HealthWarningService healthWarningService;
    @Resource
    private ReminderService reminderService;
    @Resource private HealthRecordMapper healthRecordMapper;
    @Resource private ElderInfoMapper elderInfoMapper;
    @Resource private HealthWarningMapper healthWarningMapper;
    @Resource private EmergencyHelpMapper emergencyHelpMapper;
    @Resource private ReminderExecutionMapper reminderExecutionMapper;
    @Resource private UserMapper userMapper;
    @Resource private DeviceMapper deviceMapper;

    private String defaultStart() { return LocalDate.now().minusDays(30).toString(); }
    private String defaultEnd() { return LocalDate.now().toString(); }

    /** 家属首页照护总览，一次请求返回每位已绑定老人的关键状态。 */
    @GetMapping("/overview")
    public Result<List<Map<String, Object>>> overview() {
        Long familyId = SecurityUtils.currentUserId();
        List<Map<String, Object>> elders = familyBindingService.myElders(familyId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> elder : elders) {
            Long elderId = Long.valueOf(String.valueOf(elder.get("elderInfoId")));
            Map<String, Object> row = new java.util.HashMap<>(elder);
            row.put("todayReminder", reminderService.statistics(elderId, 1));

            HealthRecord latestHealth = healthRecordMapper.selectOne(new QueryWrapper<HealthRecord>()
                    .eq("elder_info_id", elderId).orderByDesc("COALESCE(measured_at, create_time)").last("LIMIT 1"));
            row.put("latestHealth", latestHealth);
            Long warningCount = healthWarningMapper.selectCount(new QueryWrapper<HealthWarning>()
                    .eq("elder_id", elderId).in("status", 0, 1));
            row.put("unhandledWarningCount", warningCount);
            long deviceTotal = deviceMapper.selectCount(new QueryWrapper<Device>().eq("elder_id", elderId));
            long deviceOnline = deviceMapper.selectCount(new QueryWrapper<Device>().eq("elder_id", elderId).eq("status", 1));
            row.put("deviceCount", deviceTotal);
            row.put("deviceOnlineCount", deviceOnline);
            EmergencyHelp latestEmergency = emergencyHelpMapper.selectOne(new QueryWrapper<EmergencyHelp>()
                    .eq("user_id", familyId).orderByDesc("create_time").last("LIMIT 1"));
            row.put("latestEmergency", latestEmergency);

            ElderInfo elderInfo = elderInfoMapper.selectById(elderId);
            ReminderExecution latestExecution = reminderExecutionMapper.selectOne(new QueryWrapper<ReminderExecution>()
                    .eq("user_id", familyId).orderByDesc("COALESCE(action_time, create_time)").last("LIMIT 1"));
            LocalDateTime active = elderInfo == null ? null : elderInfo.getUpdateTime();
            if (latestHealth != null && latestHealth.getCreateTime() != null && (active == null || latestHealth.getCreateTime().isAfter(active))) active = latestHealth.getCreateTime();
            if (latestEmergency != null && latestEmergency.getUpdateTime() != null && (active == null || latestEmergency.getUpdateTime().isAfter(active))) active = latestEmergency.getUpdateTime();
            if (latestExecution != null) {
                LocalDateTime t = latestExecution.getActionTime() != null ? latestExecution.getActionTime() : latestExecution.getCreateTime();
                if (t != null && (active == null || t.isAfter(active))) active = t;
            }
            row.put("lastActiveTime", active);
            result.add(row);
        }
        return Result.success(result);
    }

    @GetMapping("/health/list")
    public Result<List<HealthRecord>> healthList(@RequestParam Long elderId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        familyBindingService.assertBound(SecurityUtils.currentUserId(), elderId);
        return Result.success(healthRecordService.listByUser(
                elderId, startDate != null ? startDate : defaultStart(), endDate != null ? endDate : defaultEnd()));
    }

    @GetMapping("/health/chart")
    public Result<Map<String, Object>> healthChart(@RequestParam Long elderId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        familyBindingService.assertBound(SecurityUtils.currentUserId(), elderId);
        return Result.success(healthRecordService.getChartData(
                elderId, startDate != null ? startDate : defaultStart(), endDate != null ? endDate : defaultEnd()));
    }

    @GetMapping("/health/advice")
    public Result<String> healthAdvice(@RequestParam Long elderId) {
        familyBindingService.assertBound(SecurityUtils.currentUserId(), elderId);
        return Result.success(healthRecordService.generateAdvice(elderId));
    }

    @GetMapping("/emergency/list")
    public Result<List<EmergencyHelp>> emergencyList(@RequestParam Long elderId) {
        familyBindingService.assertBound(SecurityUtils.currentUserId(), elderId);
        return Result.success(emergencyHelpService.listByUser(elderId));
    }

    @GetMapping("/emergency/{id}")
    public Result<Map<String, Object>> emergencyDetail(@PathVariable Long id) {
        EmergencyHelp help = emergencyHelpService.getById(id);
        familyBindingService.assertBound(SecurityUtils.currentUserId(), help.getUserId());
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", help.getId());
        result.put("userId", help.getUserId());
        result.put("contactName", help.getContactName());
        result.put("contactPhone", help.getContactPhone());
        result.put("contactEmail", help.getContactEmail());
        result.put("helpContent", help.getHelpContent());
        result.put("status", help.getStatus());
        result.put("notificationStatus", help.getNotificationStatus());
        result.put("notificationMessage", help.getNotificationMessage());
        result.put("notifiedAt", help.getNotifiedAt());
        result.put("acknowledgedBy", help.getAcknowledgedBy());
        result.put("acknowledgedRole", help.getAcknowledgedRole());
        result.put("acknowledgedName", help.getAcknowledgedName());
        result.put("acknowledgedAt", help.getAcknowledgedAt());
        result.put("processingAt", help.getProcessingAt());
        result.put("completedAt", help.getCompletedAt());
        result.put("latitude", help.getLatitude());
        result.put("longitude", help.getLongitude());
        result.put("locationAccuracy", help.getLocationAccuracy());
        result.put("locationText", help.getLocationText());
        result.put("handleRemark", help.getHandleRemark());
        result.put("createTime", help.getCreateTime());
        result.put("updateTime", help.getUpdateTime());
        User elder = userMapper.selectById(help.getUserId());
        if (elder != null && elder.getPhone() != null && !elder.getPhone().isBlank()) {
            result.put("elderPhone", elder.getPhone());
        }
        return Result.success(result);
    }

    @PutMapping("/emergency/{id}/acknowledge")
    public Result<Void> acknowledgeEmergency(@PathVariable Long id) {
        AuthenticatedUser user = SecurityUtils.currentUser();
        if (!"FAMILY".equals(user.role())) throw new BusinessException(403, "仅家属账号可确认求助");
        EmergencyHelp help = emergencyHelpService.getById(id);
        familyBindingService.assertBound(user.userId(), help.getUserId());
        emergencyHelpService.acknowledgeByFamily(id, user.userId(), user.username());
        return Result.success("已确认收到求助");
    }

    @GetMapping("/warning/list")
    public Result<PageResult<HealthWarning>> warningList(@RequestParam Long elderId,
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        familyBindingService.assertBound(SecurityUtils.currentUserId(), elderId);
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        return Result.success(healthWarningService.listByUser(elderId, dto));
    }

    @GetMapping("/reminder/list")
    public Result<List<Reminder>> reminderList(@RequestParam Long elderId) {
        familyBindingService.assertBound(SecurityUtils.currentUserId(), elderId);
        return Result.success(reminderService.listByUser(elderId));
    }

    @PostMapping("/reminder/add")
    public Result<Reminder> addReminder(@RequestParam Long elderId,
                                        @Valid @RequestBody ReminderDTO dto) {
        familyBindingService.assertBound(SecurityUtils.currentUserId(), elderId);
        return Result.success("已为老人创建提醒", reminderService.add(elderId, dto));
    }

    @GetMapping("/reminder/statistics")
    public Result<Map<String, Object>> reminderStatistics(@RequestParam Long elderId,
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        familyBindingService.assertBound(SecurityUtils.currentUserId(), elderId);
        return Result.success(reminderService.statistics(elderId, days));
    }

    @PutMapping("/reminder/{id}")
    public Result<Reminder> updateReminder(@PathVariable Long id, @RequestParam Long elderId,
                                           @Valid @RequestBody ReminderDTO dto) {
        familyBindingService.assertBound(SecurityUtils.currentUserId(), elderId);
        return Result.success(reminderService.update(id, elderId, dto));
    }

    @DeleteMapping("/reminder/{id}")
    public Result<Void> deleteReminder(@PathVariable Long id, @RequestParam Long elderId) {
        familyBindingService.assertBound(SecurityUtils.currentUserId(), elderId);
        reminderService.delete(id, elderId);
        return Result.success("提醒已删除");
    }

    @PutMapping("/reminder/{id}/pause")
    public Result<Void> pauseReminder(@PathVariable Long id, @RequestParam Long elderId,
                                      @RequestParam boolean paused) {
        familyBindingService.assertBound(SecurityUtils.currentUserId(), elderId);
        reminderService.setPaused(id, elderId, paused);
        return Result.success(paused ? "提醒已暂停" : "提醒已恢复");
    }

    @GetMapping("/reminder/{id}/executions")
    public Result<List<ReminderExecution>> reminderExecutions(@PathVariable Long id, @RequestParam Long elderId) {
        familyBindingService.assertBound(SecurityUtils.currentUserId(), elderId);
        return Result.success(reminderExecutionMapper.selectList(new QueryWrapper<ReminderExecution>()
                .eq("reminder_id", id).eq("user_id", SecurityUtils.currentUserId()).orderByDesc("scheduled_time")));
    }
}

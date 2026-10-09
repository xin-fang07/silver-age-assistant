package com.example.elderai.controller;

import com.example.elderai.common.PageResult;
import com.example.elderai.common.Result;
import com.example.elderai.entity.*;
import com.example.elderai.mapper.*;
import com.example.elderai.security.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/warning-center")
public class AdminWarningController {

    private static final Logger logger = LoggerFactory.getLogger(AdminWarningController.class);

    @Resource
    private HealthWarningMapper healthWarningMapper;

    @Resource
    private EmergencyHelpMapper emergencyHelpMapper;

    @Resource
    private WarningHandleLogMapper warningHandleLogMapper;

    @Resource
    private ElderInfoMapper elderInfoMapper;

    @Resource
    private UserMapper userMapper;

    private Long getCurrentUserId() {
        return SecurityUtils.currentUserId();
    }

    private String getCurrentUserName() {
        User user = userMapper.selectById(getCurrentUserId());
        return user != null ? user.getNickname() : "admin";
    }

    private void saveHandleLog(String eventType, Long eventId, Long elderId, String action, String actionDesc, String remark) {
        WarningHandleLog log = new WarningHandleLog();
        log.setEventType(eventType);
        log.setEventId(eventId);
        log.setElderId(elderId);
        log.setAction(action);
        log.setActionDesc(actionDesc);
        log.setOperatorId(getCurrentUserId());
        log.setOperatorName(getCurrentUserName());
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        warningHandleLogMapper.insert(log);
    }

    @GetMapping("/events")
    public Result<Map<String, Object>> getEvents(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) Integer status) {
        try {
            logger.info("getEvents called - pageNum: {}, pageSize: {}, eventType: {}, status: {}", pageNum, pageSize, eventType, status);

            List<Map<String, Object>> events = new ArrayList<>();

            if (eventType == null || eventType.equals("HEALTH_WARNING")) {
                List<HealthWarning> warnings = healthWarningMapper.selectList(new LambdaQueryWrapper<HealthWarning>()
                    .eq(status != null, HealthWarning::getStatus, status)
                    .orderByDesc(HealthWarning::getCreateTime)
                    .last("LIMIT " + pageSize));

                for (HealthWarning warning : warnings) {
                    Map<String, Object> event = new LinkedHashMap<>();
                    event.put("id", warning.getId());
                    event.put("eventType", "HEALTH_WARNING");
                    event.put("elderId", warning.getElderId());
                    event.put("elderName", getElderName(warning.getElderId()));
                    event.put("title", getWarningTitle(warning.getWarningType()));
                    event.put("content", warning.getWarningContent());
                    event.put("warningLevel", warning.getWarningLevel());
                    event.put("warningType", warning.getWarningType());
                    event.put("status", warning.getStatus());
                    event.put("statusText", getWarningStatusText(warning.getStatus()));
                    event.put("createTime", warning.getCreateTime());
                    event.put("handledAt", warning.getHandledAt());
                    event.put("actionNote", warning.getActionNote());
                    events.add(event);
                }
            }

            if (eventType == null || eventType.equals("SOS")) {
                List<EmergencyHelp> helps = emergencyHelpMapper.selectList(new LambdaQueryWrapper<EmergencyHelp>()
                    .eq(status != null, EmergencyHelp::getStatus, status)
                    .orderByDesc(EmergencyHelp::getCreateTime)
                    .last("LIMIT " + pageSize));

                for (EmergencyHelp help : helps) {
                    Map<String, Object> event = new LinkedHashMap<>();
                    event.put("id", help.getId());
                    event.put("eventType", "SOS");
                    event.put("elderId", help.getUserId());
                    event.put("elderName", getUserRealName(help.getUserId()));
                    event.put("title", "紧急求助");
                    event.put("content", help.getHelpContent());
                    event.put("contactName", help.getContactName());
                    event.put("contactPhone", help.getContactPhone());
                    event.put("status", help.getStatus());
                    event.put("statusText", getEmergencyStatusText(help.getStatus()));
                    event.put("createTime", help.getCreateTime());
                    event.put("acknowledgedAt", help.getAcknowledgedAt());
                    event.put("completedAt", help.getCompletedAt());
                    event.put("handleRemark", help.getHandleRemark());
                    events.add(event);
                }
            }

            events.sort((a, b) -> {
                LocalDateTime ta = (LocalDateTime) a.get("createTime");
                LocalDateTime tb = (LocalDateTime) b.get("createTime");
                if (ta == null && tb == null) return 0;
                if (ta == null) return 1;
                if (tb == null) return -1;
                return tb.compareTo(ta);
            });

            int total = 0;
            if (eventType == null || eventType.equals("HEALTH_WARNING")) {
                total += healthWarningMapper.selectCount(new LambdaQueryWrapper<HealthWarning>()
                    .eq(status != null, HealthWarning::getStatus, status));
            }
            if (eventType == null || eventType.equals("SOS")) {
                total += emergencyHelpMapper.selectCount(new LambdaQueryWrapper<EmergencyHelp>()
                    .eq(status != null, EmergencyHelp::getStatus, status));
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("list", events);
            result.put("total", total);
            logger.info("getEvents success - total: {}, events size: {}", total, events.size());
            return Result.success(result);
        } catch (Exception e) {
            logger.error("getEvents failed", e);
            return Result.error("获取事件列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/events/{eventType}/{eventId}/logs")
    public Result<List<WarningHandleLog>> getHandleLogs(
            @PathVariable String eventType,
            @PathVariable Long eventId) {
        LambdaQueryWrapper<WarningHandleLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningHandleLog::getEventType, eventType)
                .eq(WarningHandleLog::getEventId, eventId)
                .orderByAsc(WarningHandleLog::getCreateTime);
        List<WarningHandleLog> logs = warningHandleLogMapper.selectList(wrapper);
        return Result.success(logs);
    }

    @PutMapping("/events/{eventType}/{eventId}/acknowledge")
    public Result<Void> acknowledge(
            @PathVariable String eventType,
            @PathVariable Long eventId,
            @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.get("remark") : null;

        if ("HEALTH_WARNING".equals(eventType)) {
            HealthWarning warning = healthWarningMapper.selectById(eventId);
            if (warning != null) {
                warning.setIsRead(1);
                warning.setStatus(1);
                warning.setHandledAt(LocalDateTime.now());
                warning.setActionNote(remark);
                healthWarningMapper.updateById(warning);
                saveHandleLog(eventType, eventId, warning.getElderId(), "ACKNOWLEDGE", "确认知晓", remark);
            }
        } else if ("SOS".equals(eventType)) {
            EmergencyHelp help = emergencyHelpMapper.selectById(eventId);
            if (help != null) {
                help.setAcknowledgedBy(getCurrentUserId());
                help.setAcknowledgedRole("ADMIN");
                help.setAcknowledgedName(getCurrentUserName());
                help.setAcknowledgedAt(LocalDateTime.now());
                help.setStatus(1);
                emergencyHelpMapper.updateById(help);
                saveHandleLog(eventType, eventId, help.getUserId(), "ACKNOWLEDGE", "确认接单", remark);
            }
        }
        return Result.success("已确认知晓");
    }

    @PutMapping("/events/{eventType}/{eventId}/contact-family")
    public Result<Void> contactFamily(
            @PathVariable String eventType,
            @PathVariable Long eventId,
            @RequestBody Map<String, String> body) {
        String remark = body.get("remark");

        Long elderId = null;
        if ("HEALTH_WARNING".equals(eventType)) {
            HealthWarning warning = healthWarningMapper.selectById(eventId);
            if (warning != null) {
                elderId = warning.getElderId();
                saveHandleLog(eventType, eventId, elderId, "CONTACT_FAMILY", "联系家属", remark);
            }
        } else if ("SOS".equals(eventType)) {
            EmergencyHelp help = emergencyHelpMapper.selectById(eventId);
            if (help != null) {
                elderId = help.getUserId();
                saveHandleLog(eventType, eventId, elderId, "CONTACT_FAMILY", "联系家属", remark);
            }
        }
        return Result.success("家属联系记录已保存");
    }

    @PutMapping("/events/{eventType}/{eventId}/handle")
    public Result<Void> handle(
            @PathVariable String eventType,
            @PathVariable Long eventId,
            @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.get("remark") : null;

        if ("HEALTH_WARNING".equals(eventType)) {
            HealthWarning warning = healthWarningMapper.selectById(eventId);
            if (warning != null) {
                warning.setStatus(2);
                warning.setHandledAt(LocalDateTime.now());
                warning.setActionNote(remark);
                healthWarningMapper.updateById(warning);
                saveHandleLog(eventType, eventId, warning.getElderId(), "RESOLVE", "已解决", remark);
            }
        } else if ("SOS".equals(eventType)) {
            EmergencyHelp help = emergencyHelpMapper.selectById(eventId);
            if (help != null) {
                help.setProcessingAt(LocalDateTime.now());
                help.setStatus(2);
                emergencyHelpMapper.updateById(help);
                saveHandleLog(eventType, eventId, help.getUserId(), "HANDLE", "处理中", remark);
            }
        }
        return Result.success("处理状态已更新");
    }

    @PutMapping("/events/{eventType}/{eventId}/close")
    public Result<Void> close(
            @PathVariable String eventType,
            @PathVariable Long eventId,
            @RequestBody Map<String, String> body) {
        String remark = body.get("remark");

        if ("HEALTH_WARNING".equals(eventType)) {
            HealthWarning warning = healthWarningMapper.selectById(eventId);
            if (warning != null) {
                warning.setStatus(2);
                warning.setHandledAt(LocalDateTime.now());
                warning.setActionNote(remark);
                healthWarningMapper.updateById(warning);
                saveHandleLog(eventType, eventId, warning.getElderId(), "CLOSE", "闭环完结", remark);
            }
        } else if ("SOS".equals(eventType)) {
            EmergencyHelp help = emergencyHelpMapper.selectById(eventId);
            if (help != null) {
                help.setCompletedAt(LocalDateTime.now());
                help.setStatus(3);
                help.setHandleRemark(remark);
                emergencyHelpMapper.updateById(help);
                saveHandleLog(eventType, eventId, help.getUserId(), "CLOSE", "闭环完结", remark);
            }
        }
        return Result.success("事件已闭环完结");
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        Long hwPending = healthWarningMapper.selectCount(new LambdaQueryWrapper<HealthWarning>().eq(HealthWarning::getStatus, 0));
        Long hwHandled = healthWarningMapper.selectCount(new LambdaQueryWrapper<HealthWarning>().eq(HealthWarning::getStatus, 2));

        Long sosPending = emergencyHelpMapper.selectCount(new LambdaQueryWrapper<EmergencyHelp>().in(EmergencyHelp::getStatus, 0, 5));
        Long sosProcessing = emergencyHelpMapper.selectCount(new LambdaQueryWrapper<EmergencyHelp>().in(EmergencyHelp::getStatus, 1, 2));
        Long sosCompleted = emergencyHelpMapper.selectCount(new LambdaQueryWrapper<EmergencyHelp>().eq(EmergencyHelp::getStatus, 3));

        stats.put("healthWarningPending", hwPending);
        stats.put("healthWarningHandled", hwHandled);
        stats.put("sosPending", sosPending);
        stats.put("sosProcessing", sosProcessing);
        stats.put("sosCompleted", sosCompleted);

        return Result.success(stats);
    }

    private String getElderName(Long elderId) {
        if (elderId == null) return "-";
        ElderInfo elder = elderInfoMapper.selectById(elderId);
        return elder != null ? elder.getRealName() : "-";
    }

    private String getUserRealName(Long userId) {
        if (userId == null) return "-";
        User user = userMapper.selectById(userId);
        return user != null ? user.getNickname() : "-";
    }

    private String getWarningTitle(String type) {
        Map<String, String> titles = Map.of(
                "BLOOD_PRESSURE", "血压异常预警",
                "BLOOD_SUGAR", "血糖异常预警",
                "HEART_RATE", "心率异常预警",
                "WEIGHT", "体重异常预警"
        );
        return titles.getOrDefault(type, type);
    }

    private String getWarningStatusText(Integer status) {
        Map<Integer, String> texts = Map.of(
                0, "待处理",
                1, "已知晓",
                2, "已处理"
        );
        return texts.getOrDefault(status, "未知");
    }

    private String getEmergencyStatusText(Integer status) {
        Map<Integer, String> texts = Map.of(
                0, "待接单",
                1, "已接单",
                2, "处理中",
                3, "已完成",
                4, "已取消",
                5, "已升级"
        );
        return texts.getOrDefault(status, "未知");
    }
}
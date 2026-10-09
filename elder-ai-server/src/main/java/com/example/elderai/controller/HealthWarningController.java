package com.example.elderai.controller;

import com.example.elderai.common.PageResult;
import com.example.elderai.common.Result;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.dto.HealthWarningActionDTO;
import com.example.elderai.entity.HealthWarning;
import com.example.elderai.service.HealthWarningService;
import com.example.elderai.security.SecurityUtils;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 健康预警控制器
 */
@RestController
@RequestMapping("/api/health-warning")
public class HealthWarningController {

    @Resource
    private HealthWarningService healthWarningService;

    private Long getCurrentUserId() {
        return SecurityUtils.currentUserId();
    }

    @GetMapping("/list")
    public PageResult<HealthWarning> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getCurrentUserId();
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        return healthWarningService.listByUser(userId, dto);
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Object>> unreadCount() {
        Long userId = getCurrentUserId();
        int count = healthWarningService.countUnread(userId);
        return Result.success(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        healthWarningService.markAsRead(id, getCurrentUserId());
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @Valid @RequestBody HealthWarningActionDTO dto) {
        healthWarningService.updateStatus(id, getCurrentUserId(), dto.getStatus(), dto.getActionNote());
        return Result.success(dto.getStatus() == 1 ? "已确认知晓" : "预警已处理", null);
    }
}

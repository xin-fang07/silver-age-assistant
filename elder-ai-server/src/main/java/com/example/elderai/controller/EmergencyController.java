package com.example.elderai.controller;

import com.example.elderai.common.Result;
import com.example.elderai.dto.EmergencyHelpDTO;
import com.example.elderai.entity.EmergencyHelp;
import com.example.elderai.security.SecurityUtils;
import com.example.elderai.service.EmergencyHelpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 老人端紧急求助接口。
 *
 * <p>求助创建、平台留痕和邮件通知由同一业务流程完成，避免客户端重复触发通知。</p>
 */
@Tag(name = "紧急求助", description = "老年人一键 SOS 求助，支持状态追踪和自动升级")
@RestController
@RequestMapping("/api/emergency")
public class EmergencyController {

    @Resource
    private EmergencyHelpService emergencyHelpService;

    /**
     * 创建求助单。联系人未填写时也允许先提交，确保 SOS 主流程不会被表单阻断。
     */
    @Operation(summary = "提交紧急求助", description = "一键 SOS 创建求助单，自动通知紧急联系人和管理员")
    @PostMapping("/help")
    public Result<EmergencyHelp> createHelp(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody EmergencyHelpDTO dto) {
        EmergencyHelp help = emergencyHelpService.createHelp(
                SecurityUtils.currentUserId(), dto, idempotencyKey);
        return Result.success("求助已提交，工作人员将尽快处理", help);
    }

    /**
     * 查询当前用户自己的全部求助记录。
     */
    @Operation(summary = "查询我的求助记录", description = "获取当前用户的所有求助单及处理状态")
    @GetMapping("/list")
    public Result<List<EmergencyHelp>> list() {
        return Result.success(emergencyHelpService.listByUser(SecurityUtils.currentUserId()));
    }

    /**
     * 用户撤销尚未进入人工处理阶段的求助。
     */
    @Operation(summary = "撤销求助", description = "用户主动取消尚未处理的求助单")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        emergencyHelpService.cancel(id, SecurityUtils.currentUserId());
        return Result.success("求助已取消", null);
    }
}

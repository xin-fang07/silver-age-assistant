package com.example.elderai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderai.common.Result;
import com.example.elderai.dto.AccountDeletionRequestDTO;
import com.example.elderai.entity.ServiceTicket;
import com.example.elderai.mapper.ServiceTicketMapper;
import com.example.elderai.security.SecurityUtils;
import com.example.elderai.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/user/deletion-request")
public class AccountDeletionController {
    @Resource private ServiceTicketMapper requestMapper;
    @Resource private UserService userService;

    @PostMapping
    public Result<ServiceTicket> create(
            @Valid @RequestBody(required = false) AccountDeletionRequestDTO dto,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        Long userId = SecurityUtils.currentUserId();
        ServiceTicket existing = requestMapper.selectOne(
                new LambdaQueryWrapper<ServiceTicket>()
                        .eq(ServiceTicket::getUserId, userId).eq(ServiceTicket::getType, "DELETION")
                        .in(ServiceTicket::getStatus, "PENDING", "PROCESSING")
                        .last("LIMIT 1"));
        ServiceTicket row = existing;
        if (row == null) {
            row = new ServiceTicket(); row.setTicketNo("TK" + System.currentTimeMillis()); row.setType("DELETION");
            row.setUserId(userId);
            row.setSubject("账户注销申请"); row.setContent(dto == null || dto.getReason() == null ? "用户申请注销账户" : dto.getReason().trim());
            row.setStatus("PENDING"); row.setPriority("HIGH"); row.setCreateTime(LocalDateTime.now()); row.setUpdateTime(LocalDateTime.now());
            requestMapper.insert(row);
        }
        if (authorization != null && authorization.startsWith("Bearer ")) {
            userService.logout(authorization.substring(7).trim());
        }
        return Result.success(existing == null ? "注销申请已提交" : "您已有待处理的注销申请", row);
    }
}

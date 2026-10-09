package com.example.elderai.controller;

import com.example.elderai.common.Result;
import com.example.elderai.entity.Notification;
import com.example.elderai.mapper.NotificationMapper;
import com.example.elderai.security.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Resource
    private NotificationMapper notificationMapper;

    @GetMapping("/list")
    public Result<List<Notification>> list(@RequestParam(required = false) String type) {
        QueryWrapper<Notification> qw = new QueryWrapper<>();
        qw.eq("user_id", SecurityUtils.currentUserId());
        if (type != null && !type.isBlank()) {
            qw.eq("type", type);
        }
        qw.orderByDesc("create_time");
        return Result.success(notificationMapper.selectList(qw));
    }

    @PostMapping("/read")
    public Result<Void> read(@RequestBody Map<String, Long> body) {
        Long id = body.get("id");
        if (id != null) {
            Notification n = notificationMapper.selectById(id);
            if (n != null && n.getUserId().equals(SecurityUtils.currentUserId())) {
                n.setIsRead(1);
                notificationMapper.updateById(n);
            }
        }
        return Result.success();
    }

    @PostMapping("/read-all")
    public Result<Void> readAll() {
        List<Notification> list = notificationMapper.selectList(new QueryWrapper<Notification>()
                .eq("user_id", SecurityUtils.currentUserId()).eq("is_read", 0));
        for (Notification n : list) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
        return Result.success();
    }
}

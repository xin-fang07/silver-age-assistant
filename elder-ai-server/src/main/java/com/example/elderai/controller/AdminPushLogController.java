package com.example.elderai.controller;

import com.example.elderai.common.Result;
import com.example.elderai.entity.DevicePushLog;
import com.example.elderai.service.DevicePushLogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/push-log")
public class AdminPushLogController {

    @Resource
    private DevicePushLogService devicePushLogService;

    @GetMapping
    public Result<List<DevicePushLog>> list() {
        return Result.success(devicePushLogService.listAll());
    }
}

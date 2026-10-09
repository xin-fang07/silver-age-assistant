package com.example.elderai.controller;

import com.example.elderai.common.Result;
import com.example.elderai.security.SecurityUtils;
import com.example.elderai.service.ExportService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Resource
    private ExportService exportService;

    /**
     * 导出健康记录为 Excel
     */
    @GetMapping("/health/excel")
    public void exportHealthRecords(HttpServletResponse response) throws IOException {
        Long userId = SecurityUtils.currentUserId();
        exportService.exportHealthRecords(userId, response);
    }

    /**
     * 导出聊天记录为 JSON
     */
    @GetMapping("/chat/json")
    public void exportChatRecords(HttpServletResponse response) throws IOException {
        Long userId = SecurityUtils.currentUserId();
        exportService.exportChatRecords(userId, response);
    }

    /**
     * 生成健康报告（用于分享）
     */
    @GetMapping("/health/report")
    public Result<String> generateHealthReport() {
        Long userId = SecurityUtils.currentUserId();
        String report = exportService.generateHealthReport(userId);
        return Result.success(report);
    }
}
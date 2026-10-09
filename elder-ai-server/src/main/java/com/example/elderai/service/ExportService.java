package com.example.elderai.service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface ExportService {

    void exportHealthRecords(Long userId, HttpServletResponse response) throws IOException;

    void exportChatRecords(Long userId, HttpServletResponse response) throws IOException;

    String generateHealthReport(Long userId);

    void exportAdminHealthRecords(Long elderInfoId, String startDate, String endDate, HttpServletResponse response) throws IOException;

    void exportLlmAnalysis(Long elderInfoId, String riskLevel, HttpServletResponse response) throws IOException;
}
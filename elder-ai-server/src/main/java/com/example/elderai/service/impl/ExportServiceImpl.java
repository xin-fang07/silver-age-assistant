package com.example.elderai.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderai.entity.ChatRecord;
import com.example.elderai.entity.ElderInfo;
import com.example.elderai.entity.HealthRecord;
import com.example.elderai.entity.FamilyBinding;
import com.example.elderai.entity.LlmAnalysisResult;
import com.example.elderai.mapper.ChatRecordMapper;
import com.example.elderai.mapper.ElderInfoMapper;
import com.example.elderai.mapper.FamilyBindingMapper;
import com.example.elderai.mapper.HealthRecordMapper;
import com.example.elderai.mapper.LlmAnalysisResultMapper;
import com.example.elderai.service.ExportService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportServiceImpl implements ExportService {

    @Resource
    private HealthRecordMapper healthRecordMapper;

    @Resource
    private ChatRecordMapper chatRecordMapper;

    @Resource
    private ElderInfoMapper elderInfoMapper;

    @Resource
    private FamilyBindingMapper familyBindingMapper;

    @Resource
    private LlmAnalysisResultMapper llmAnalysisResultMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void exportHealthRecords(Long userId, HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthRecord::getUserId, userId)
                .orderByDesc(HealthRecord::getRecordDate);
        List<HealthRecord> records = healthRecordMapper.selectList(wrapper);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("健康记录");

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        String[] headers = {"记录日期", "收缩压(mmHg)", "舒张压(mmHg)", "血糖(mmol/L)", "心率(次/分)", "体重(kg)", "备注"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 5000);
        }

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        int rowNum = 1;
        for (HealthRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, record.getRecordDate() != null ? record.getRecordDate().format(DATE_FORMATTER) : "", dataStyle);
            createCell(row, 1, record.getBloodPressureHigh(), dataStyle);
            createCell(row, 2, record.getBloodPressureLow(), dataStyle);
            createCell(row, 3, record.getBloodSugar() != null ? record.getBloodSugar().toPlainString() : "", dataStyle);
            createCell(row, 4, record.getHeartRate(), dataStyle);
            createCell(row, 5, record.getWeight() != null ? record.getWeight().toPlainString() : "", dataStyle);
            createCell(row, 6, record.getRemark() != null ? record.getRemark() : "", dataStyle);
        }

        String filename = "健康记录_" + LocalDate.now().format(DATE_FORMATTER) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));

        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        outputStream.flush();
        outputStream.close();
        workbook.close();
    }

    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
        cell.setCellStyle(style);
    }

    @Override
    public void exportChatRecords(Long userId, HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<ChatRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatRecord::getUserId, userId)
                .orderByDesc(ChatRecord::getCreateTime);
        List<ChatRecord> records = chatRecordMapper.selectList(wrapper);

        JSONArray array = new JSONArray();
        for (ChatRecord record : records) {
            JSONObject obj = new JSONObject();
            obj.put("id", record.getId());
            obj.put("question", record.getQuestion());
            obj.put("answer", record.getAnswer());
            obj.put("isFallback", record.getIsFallback());
            obj.put("createTime", record.getCreateTime() != null ? record.getCreateTime().format(DATETIME_FORMATTER) : "");
            array.add(obj);
        }

        String filename = "聊天记录_" + LocalDate.now().format(DATE_FORMATTER) + ".json";
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));

        OutputStream outputStream = response.getOutputStream();
        outputStream.write(array.toJSONString().getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        outputStream.close();
    }

    private Long resolveElderOfFamily(Long familyUserId) {
        if (familyUserId == null) {
            return null;
        }
        com.example.elderai.entity.FamilyBinding binding = familyBindingMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.elderai.entity.FamilyBinding>()
                        .eq("family_user_id", familyUserId).eq("status", 1).last("LIMIT 1"));
        return binding != null ? binding.getElderInfoId() : null;
    }

    @Override
    public String generateHealthReport(Long userId) {
        Long elderInfoId = resolveElderOfFamily(userId);
        ElderInfo elderInfo = elderInfoId != null ? elderInfoMapper.selectById(elderInfoId) : null;

        LambdaQueryWrapper<HealthRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.eq(HealthRecord::getUserId, userId)
                .orderByDesc(HealthRecord::getRecordDate)
                .last("LIMIT 30");
        List<HealthRecord> records = healthRecordMapper.selectList(recordWrapper);

        StringBuilder report = new StringBuilder();
        report.append("【银发智能生活助手 - 健康报告】\n\n");
        report.append("========== 个人信息 ==========\n");
        if (elderInfo != null) {
            report.append("姓名：").append(elderInfo.getRealName() != null ? elderInfo.getRealName() : "未填写").append("\n");
            report.append("年龄：").append(elderInfo.getAge() != null ? elderInfo.getAge() + " 岁" : "未填写").append("\n");
            report.append("性别：").append(elderInfo.getGender() != null ? (elderInfo.getGender() == 1 ? "男" : "女") : "未填写").append("\n");
            if (elderInfo.getHeight() != null) {
                double heightM = elderInfo.getHeight() / 100.0;
                double bmi = 65 / (heightM * heightM);
                report.append("身高：").append(elderInfo.getHeight()).append(" cm\n");
                report.append("BMI：").append(String.format("%.1f", bmi)).append(" (").append(getBmiStatus(bmi)).append(")\n");
            }
            report.append("既往病史：").append(elderInfo.getMedicalHistory() != null ? elderInfo.getMedicalHistory() : "无").append("\n");
        }

        report.append("\n========== 健康数据统计 ==========\n");
        if (records.isEmpty()) {
            report.append("暂无健康记录\n");
        } else {
            int bpCount = 0, sugarCount = 0, hrCount = 0;
            int bpHighSum = 0, bpLowSum = 0, hrSum = 0;
            double sugarSum = 0;

            for (HealthRecord record : records) {
                if (record.getBloodPressureHigh() != null && record.getBloodPressureLow() != null) {
                    bpCount++;
                    bpHighSum += record.getBloodPressureHigh();
                    bpLowSum += record.getBloodPressureLow();
                }
                if (record.getBloodSugar() != null) {
                    sugarCount++;
                    sugarSum += record.getBloodSugar().doubleValue();
                }
                if (record.getHeartRate() != null) {
                    hrCount++;
                    hrSum += record.getHeartRate();
                }
            }

            if (bpCount > 0) {
                report.append("血压（收缩压/舒张压）：")
                        .append(bpHighSum / bpCount).append("/").append(bpLowSum / bpCount)
                        .append(" mmHg（基于 ").append(bpCount).append(" 次记录）\n");
            } else {
                report.append("血压：暂无记录\n");
            }

            if (sugarCount > 0) {
                report.append("血糖：").append(String.format("%.1f", sugarSum / sugarCount))
                        .append(" mmol/L（基于 ").append(sugarCount).append(" 次记录）\n");
            } else {
                report.append("血糖：暂无记录\n");
            }

            if (hrCount > 0) {
                report.append("心率：").append(hrSum / hrCount)
                        .append(" 次/分钟（基于 ").append(hrCount).append(" 次记录）\n");
            } else {
                report.append("心率：暂无记录\n");
            }

            report.append("\n========== 最近记录 ==========\n");
            int showCount = Math.min(5, records.size());
            for (int i = 0; i < showCount; i++) {
                HealthRecord r = records.get(i);
                report.append(r.getRecordDate().format(DATE_FORMATTER)).append("：");
                if (r.getBloodPressureHigh() != null) {
                    report.append("血压 ").append(r.getBloodPressureHigh()).append("/").append(r.getBloodPressureLow()).append(" ");
                }
                if (r.getBloodSugar() != null) {
                    report.append("血糖 ").append(r.getBloodSugar().toPlainString()).append(" ");
                }
                if (r.getHeartRate() != null) {
                    report.append("心率 ").append(r.getHeartRate()).append(" ");
                }
                report.append("\n");
            }
        }

        report.append("\n========== 健康建议 ==========\n");
        report.append("1. 建议每天定时测量血压、血糖，保持记录习惯\n");
        report.append("2. 合理饮食，少吃高盐、高糖、高脂肪食物\n");
        report.append("3. 适当运动，如散步、太极拳等\n");
        report.append("4. 保持良好心态，保证充足睡眠\n");
        report.append("5. 如有异常，请及时就医\n");

        report.append("\n报告生成时间：").append(LocalDateTime.now().format(DATETIME_FORMATTER)).append("\n");
        report.append("生成来源：银发智能生活助手");

        return report.toString();
    }

    private String getBmiStatus(double bmi) {
        if (bmi < 18.5) return "偏瘦";
        if (bmi < 24) return "正常";
        if (bmi < 28) return "超重";
        return "肥胖";
    }

    @Override
    public void exportAdminHealthRecords(Long elderInfoId, String startDate, String endDate, HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        if (elderInfoId != null) {
            wrapper.eq(HealthRecord::getElderInfoId, elderInfoId);
        }
        if (startDate != null) {
            wrapper.ge(HealthRecord::getRecordDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(HealthRecord::getRecordDate, endDate);
        }
        wrapper.orderByDesc(HealthRecord::getCreateTime);
        List<HealthRecord> records = healthRecordMapper.selectList(wrapper);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("健康记录");

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        String[] headers = {"老人姓名", "记录日期", "收缩压(mmHg)", "舒张压(mmHg)", "心率(次/分)", "血氧(%)", "步数", "血糖(mmol/L)", "体重(kg)", "数据来源", "录入时间", "备注"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 5000);
        }

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        int rowNum = 1;
        for (HealthRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            String elderName = "";
            if (record.getElderInfoId() != null) {
                ElderInfo elder = elderInfoMapper.selectById(record.getElderInfoId());
                if (elder != null) elderName = elder.getRealName();
            }
            createCell(row, 0, elderName, dataStyle);
            createCell(row, 1, record.getRecordDate() != null ? record.getRecordDate().format(DATE_FORMATTER) : "", dataStyle);
            createCell(row, 2, record.getBloodPressureHigh(), dataStyle);
            createCell(row, 3, record.getBloodPressureLow(), dataStyle);
            createCell(row, 4, record.getHeartRate(), dataStyle);
            createCell(row, 5, record.getBloodOxygen(), dataStyle);
            createCell(row, 6, record.getSteps(), dataStyle);
            createCell(row, 7, record.getBloodSugar() != null ? record.getBloodSugar().toPlainString() : "", dataStyle);
            createCell(row, 8, record.getWeight() != null ? record.getWeight().toPlainString() : "", dataStyle);
            String sourceTypeText = switch (record.getSourceType()) {
                case "DEVICE" -> "智能设备";
                case "MANUAL" -> "手动录入";
                case "FILE_IMPORT" -> "文件导入";
                default -> record.getSourceType();
            };
            createCell(row, 9, sourceTypeText, dataStyle);
            createCell(row, 10, record.getCreateTime() != null ? record.getCreateTime().format(DATETIME_FORMATTER) : "", dataStyle);
            createCell(row, 11, record.getRemark() != null ? record.getRemark() : "", dataStyle);
        }

        String filename = "健康记录_" + LocalDate.now().format(DATE_FORMATTER) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));

        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        outputStream.flush();
        outputStream.close();
        workbook.close();
    }

    @Override
    public void exportLlmAnalysis(Long elderInfoId, String riskLevel, HttpServletResponse response) throws IOException {
        LambdaQueryWrapper<LlmAnalysisResult> wrapper = new LambdaQueryWrapper<>();
        if (elderInfoId != null) {
            wrapper.eq(LlmAnalysisResult::getElderInfoId, elderInfoId);
        }
        if (riskLevel != null) {
            wrapper.eq(LlmAnalysisResult::getRiskLevel, riskLevel);
        }
        wrapper.orderByDesc(LlmAnalysisResult::getCreateTime);
        List<LlmAnalysisResult> records = llmAnalysisResultMapper.selectList(wrapper);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("LLM分析结果");

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        String[] headers = {"老人姓名", "风险等级", "分析周期", "生成批次", "推送状态", "生成时间", "AI分析原文"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, i == 6 ? 15000 : 5000);
        }

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        int rowNum = 1;
        for (LlmAnalysisResult record : records) {
            Row row = sheet.createRow(rowNum++);
            String elderName = "";
            if (record.getElderInfoId() != null) {
                ElderInfo elder = elderInfoMapper.selectById(record.getElderInfoId());
                if (elder != null) elderName = elder.getRealName();
            }
            createCell(row, 0, elderName, dataStyle);
            String riskLevelText = switch (record.getRiskLevel()) {
                case "NORMAL" -> "正常";
                case "LOW" -> "偏低";
                case "HIGH" -> "偏高";
                case "DANGER" -> "高危";
                default -> record.getRiskLevel();
            };
            createCell(row, 1, riskLevelText, dataStyle);
            createCell(row, 2, (record.getStartDate() != null ? record.getStartDate() : "") + " ~ " + (record.getEndDate() != null ? record.getEndDate() : ""), dataStyle);
            createCell(row, 3, record.getBatchNo(), dataStyle);
            createCell(row, 4, record.getPushed() == 1 ? "已推送" : "未推送", dataStyle);
            createCell(row, 5, record.getCreateTime() != null ? record.getCreateTime().format(DATETIME_FORMATTER) : "", dataStyle);
            createCell(row, 6, record.getAnalysisText() != null ? record.getAnalysisText() : "", dataStyle);
        }

        String filename = "LLM分析记录_" + LocalDate.now().format(DATE_FORMATTER) + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));

        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        outputStream.flush();
        outputStream.close();
        workbook.close();
    }
}
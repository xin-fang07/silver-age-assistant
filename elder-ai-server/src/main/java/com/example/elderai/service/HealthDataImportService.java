package com.example.elderai.service;

import com.example.elderai.common.BusinessException;
import com.example.elderai.dto.HealthImportResultDTO;
import com.example.elderai.dto.HealthRecordDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class HealthDataImportService {
    private static final int MAX_ROWS = 1000;
    private static final List<String> COLUMNS = List.of("record_date", "blood_pressure_high", "blood_pressure_low", "blood_sugar", "heart_rate", "weight", "remark");
    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("记录日期", "record_date"), Map.entry("日期", "record_date"),
            Map.entry("收缩压", "blood_pressure_high"), Map.entry("高压", "blood_pressure_high"),
            Map.entry("舒张压", "blood_pressure_low"), Map.entry("低压", "blood_pressure_low"),
            Map.entry("血糖", "blood_sugar"), Map.entry("心率", "heart_rate"),
            Map.entry("体重", "weight"), Map.entry("备注", "remark"));

    private final HealthRecordService healthRecordService;
    private final Validator validator;

    public HealthDataImportService(HealthRecordService healthRecordService, Validator validator) {
        this.healthRecordService = healthRecordService;
        this.validator = validator;
    }

    public HealthImportResultDTO importFile(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException(400, "请选择要导入的文件");
        if (file.getSize() > 5 * 1024 * 1024) throw new BusinessException(400, "文件不能超过5MB");
        String name = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase(Locale.ROOT);
        List<Map<String, String>> rows;
        try {
            if (name.endsWith(".csv")) rows = readCsv(file);
            else if (name.endsWith(".xlsx") || name.endsWith(".xls")) rows = readExcel(file);
            else throw new BusinessException(400, "仅支持 CSV、XLS、XLSX 文件");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(400, "文件解析失败，请使用系统模板并检查文件格式");
        }
        if (rows.size() > MAX_ROWS) throw new BusinessException(400, "单次最多导入1000条数据");

        HealthImportResultDTO result = new HealthImportResultDTO();
        result.setBatchId(UUID.randomUUID().toString().replace("-", ""));
        result.setTotalRows(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            try {
                HealthRecordDTO dto = toDto(rows.get(i));
                Set<ConstraintViolation<HealthRecordDTO>> violations = validator.validate(dto);
                if (!violations.isEmpty()) {
                    String message = violations.stream().map(ConstraintViolation::getMessage).distinct().reduce((a, b) -> a + "；" + b).orElse("数据不合法");
                    throw new IllegalArgumentException(message);
                }
                healthRecordService.addImported(userId, dto, result.getBatchId());
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.getErrors().add(new HealthImportResultDTO.RowError(i + 2, userMessage(e)));
            }
        }
        result.setFailedCount(result.getErrors().size());
        return result;
    }

    private List<Map<String, String>> readExcel(MultipartFile file) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(Locale.CHINA);
            Row header = sheet.getRow(sheet.getFirstRowNum());
            if (header == null) throw new BusinessException(400, "文件缺少表头");
            List<String> headers = new ArrayList<>();
            for (Cell cell : header) headers.add(normalizeHeader(formatter.formatCellValue(cell)));
            List<Map<String, String>> rows = new ArrayList<>();
            for (int n = header.getRowNum() + 1; n <= sheet.getLastRowNum(); n++) {
                Row row = sheet.getRow(n);
                if (row == null) continue;
                Map<String, String> values = new HashMap<>();
                boolean empty = true;
                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String value;
                    if (cell != null && "record_date".equals(headers.get(c)) && DateUtil.isCellDateFormatted(cell)) {
                        value = cell.getLocalDateTimeCellValue().toLocalDate().toString();
                    } else {
                        value = cell == null ? "" : formatter.formatCellValue(cell).trim();
                    }
                    if (!value.isEmpty()) empty = false;
                    values.put(headers.get(c), value);
                }
                if (!empty) rows.add(values);
            }
            return rows;
        }
    }

    private List<Map<String, String>> readCsv(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String first = reader.readLine();
            if (first == null) throw new BusinessException(400, "CSV 文件为空");
            if (first.startsWith("\uFEFF")) first = first.substring(1);
            List<String> headers = parseCsvLine(first).stream().map(this::normalizeHeader).toList();
            List<Map<String, String>> rows = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> cells = parseCsvLine(line);
                Map<String, String> values = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) values.put(headers.get(i), i < cells.size() ? cells.get(i).trim() : "");
                rows.add(values);
            }
            return rows;
        }
    }

    private List<String> parseCsvLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') { value.append('"'); i++; }
                else quoted = !quoted;
            } else if (ch == ',' && !quoted) { cells.add(value.toString()); value.setLength(0); }
            else value.append(ch);
        }
        cells.add(value.toString());
        return cells;
    }

    private String normalizeHeader(String raw) {
        String key = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT).replace(" ", "_");
        key = ALIASES.getOrDefault(key, key);
        return COLUMNS.contains(key) ? key : "ignored_" + key;
    }

    private HealthRecordDTO toDto(Map<String, String> row) {
        HealthRecordDTO dto = new HealthRecordDTO();
        dto.setRecordDate(parseDate(required(row, "record_date", "记录日期不能为空")));
        dto.setBloodPressureHigh(integer(row.get("blood_pressure_high"), "收缩压"));
        dto.setBloodPressureLow(integer(row.get("blood_pressure_low"), "舒张压"));
        dto.setBloodSugar(decimal(row.get("blood_sugar"), "血糖"));
        dto.setHeartRate(integer(row.get("heart_rate"), "心率"));
        dto.setWeight(decimal(row.get("weight"), "体重"));
        dto.setRemark(blankToNull(row.get("remark")));
        return dto;
    }

    private LocalDate parseDate(String value) {
        for (DateTimeFormatter formatter : List.of(DateTimeFormatter.ISO_LOCAL_DATE, DateTimeFormatter.ofPattern("yyyy/M/d"), DateTimeFormatter.ofPattern("yyyy.M.d"))) {
            try { return LocalDate.parse(value, formatter); } catch (DateTimeParseException ignored) { }
        }
        throw new IllegalArgumentException("记录日期格式应为 yyyy-MM-dd");
    }
    private Integer integer(String value, String label) { try { return blankToNull(value) == null ? null : new BigDecimal(value).intValueExact(); } catch (Exception e) { throw new IllegalArgumentException(label + "必须是整数"); } }
    private BigDecimal decimal(String value, String label) { try { return blankToNull(value) == null ? null : new BigDecimal(value); } catch (Exception e) { throw new IllegalArgumentException(label + "必须是数字"); } }
    private String required(Map<String, String> row, String key, String message) { String value = blankToNull(row.get(key)); if (value == null) throw new IllegalArgumentException(message); return value; }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String userMessage(Exception e) { return e.getMessage() == null || e.getMessage().isBlank() ? "导入失败" : e.getMessage(); }
}

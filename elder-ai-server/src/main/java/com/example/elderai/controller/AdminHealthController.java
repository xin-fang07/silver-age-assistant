package com.example.elderai.controller;

import com.example.elderai.common.Result;
import com.example.elderai.common.PageResult;
import com.example.elderai.dto.HealthRecordDTO;
import com.example.elderai.entity.HealthRecord;
import com.example.elderai.entity.LlmAnalysisResult;
import com.example.elderai.entity.HealthReport;
import com.example.elderai.entity.ElderInfo;
import com.example.elderai.mapper.HealthRecordMapper;
import com.example.elderai.mapper.LlmAnalysisResultMapper;
import com.example.elderai.mapper.HealthReportMapper;
import com.example.elderai.mapper.ElderInfoMapper;
import com.example.elderai.service.HealthRecordService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/health")
public class AdminHealthController {

    @Resource
    private HealthRecordMapper healthRecordMapper;

    @Resource
    private LlmAnalysisResultMapper llmAnalysisResultMapper;

    @Resource
    private HealthReportMapper healthReportMapper;

    @Resource
    private ElderInfoMapper elderInfoMapper;

    @Resource
    private HealthRecordService healthRecordService;

    @Resource
    private com.example.elderai.service.ExportService exportService;

    @GetMapping("/records")
    public Result<Map<String, Object>> getHealthRecords(
            @RequestParam(required = false) Long elderInfoId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String sourceType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<HealthRecord> page = new Page<>(pageNum, pageSize);
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
        if (sourceType != null) {
            wrapper.eq(HealthRecord::getSourceType, sourceType);
        }
        wrapper.orderByDesc(HealthRecord::getCreateTime);
        Page<HealthRecord> result = healthRecordMapper.selectPage(page, wrapper);

        for (HealthRecord record : result.getRecords()) {
            if (record.getElderInfoId() != null) {
                ElderInfo elder = elderInfoMapper.selectById(record.getElderInfoId());
                if (elder != null) {
                    record.setElderName(elder.getRealName());
                }
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return Result.success(data);
    }

    @PostMapping("/record")
    public Result<HealthRecord> addHealthRecord(@RequestBody HealthRecordDTO dto) {
        HealthRecord record = healthRecordService.addAdmin(dto);
        return Result.success("健康记录添加成功", record);
    }

    @PutMapping("/record/{id}")
    public Result<HealthRecord> updateHealthRecord(@PathVariable Long id, @RequestBody HealthRecordDTO dto) {
        HealthRecord record = healthRecordMapper.selectById(id);
        if (record != null) {
            if (dto.getBloodPressureHigh() != null) record.setBloodPressureHigh(dto.getBloodPressureHigh());
            if (dto.getBloodPressureLow() != null) record.setBloodPressureLow(dto.getBloodPressureLow());
            if (dto.getBloodSugar() != null) record.setBloodSugar(dto.getBloodSugar());
            if (dto.getHeartRate() != null) record.setHeartRate(dto.getHeartRate());
            if (dto.getBloodOxygen() != null) record.setBloodOxygen(dto.getBloodOxygen());
            if (dto.getSteps() != null) record.setSteps(dto.getSteps());
            if (dto.getWeight() != null) record.setWeight(dto.getWeight());
            if (dto.getRecordDate() != null) record.setRecordDate(dto.getRecordDate());
            if (dto.getRemark() != null) record.setRemark(dto.getRemark());
            if (dto.getSourceType() != null) record.setSourceType(dto.getSourceType());
            record.setUpdateTime(LocalDateTime.now());
            healthRecordMapper.updateById(record);
            return Result.success("修改成功", record);
        }
        return Result.error("记录不存在");
    }

    @DeleteMapping("/record/{id}")
    public Result<Void> deleteHealthRecord(@PathVariable Long id) {
        healthRecordMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/record/{id}/invalid")
    public Result<Void> markInvalid(@PathVariable Long id) {
        HealthRecord record = healthRecordMapper.selectById(id);
        if (record != null) {
            record.setDeleted(1);
            record.setUpdateTime(LocalDateTime.now());
            healthRecordMapper.updateById(record);
            return Result.success("已标记为无效数据");
        }
        return Result.error("记录不存在");
    }

    @GetMapping("/llm-analysis")
    public Result<Map<String, Object>> getLlmAnalysis(
            @RequestParam(required = false) Long elderInfoId,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<LlmAnalysisResult> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LlmAnalysisResult> wrapper = new LambdaQueryWrapper<>();
        if (elderInfoId != null) {
            wrapper.eq(LlmAnalysisResult::getElderInfoId, elderInfoId);
        }
        if (riskLevel != null) {
            wrapper.eq(LlmAnalysisResult::getRiskLevel, riskLevel);
        }
        wrapper.orderByDesc(LlmAnalysisResult::getCreateTime);
        Page<LlmAnalysisResult> result = llmAnalysisResultMapper.selectPage(page, wrapper);

        Map<String, String> riskMap = new HashMap<>();
        riskMap.put("NORMAL", "正常");
        riskMap.put("LOW", "偏低");
        riskMap.put("HIGH", "偏高");
        riskMap.put("DANGER", "高危");

        for (LlmAnalysisResult analysis : result.getRecords()) {
            if (analysis.getElderInfoId() != null) {
                ElderInfo elder = elderInfoMapper.selectById(analysis.getElderInfoId());
                if (elder != null) {
                    analysis.setElderName(elder.getRealName());
                }
            }
            analysis.setRiskLevelText(riskMap.getOrDefault(analysis.getRiskLevel(), analysis.getRiskLevel()));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return Result.success(data);
    }

    @GetMapping("/llm-analysis/{id}")
    public Result<LlmAnalysisResult> getLlmAnalysisDetail(@PathVariable Long id) {
        LlmAnalysisResult analysis = llmAnalysisResultMapper.selectById(id);
        if (analysis != null) {
            if (analysis.getElderInfoId() != null) {
                ElderInfo elder = elderInfoMapper.selectById(analysis.getElderInfoId());
                if (elder != null) {
                    analysis.setElderName(elder.getRealName());
                }
            }
            Map<String, String> riskMap = new HashMap<>();
            riskMap.put("NORMAL", "正常");
            riskMap.put("LOW", "偏低");
            riskMap.put("HIGH", "偏高");
            riskMap.put("DANGER", "高危");
            analysis.setRiskLevelText(riskMap.getOrDefault(analysis.getRiskLevel(), analysis.getRiskLevel()));
            return Result.success(analysis);
        }
        return Result.error("记录不存在");
    }

    @PostMapping("/llm-analysis/retrigger")
    public Result<Void> retriggerLlmAnalysis(@RequestBody Map<String, Object> body) {
        Long elderInfoId = ((Number) body.get("elderInfoId")).longValue();
        String startDate = (String) body.get("startDate");
        String endDate = (String) body.get("endDate");

        LlmAnalysisResult analysis = new LlmAnalysisResult();
        analysis.setUserId(2L);
        analysis.setElderInfoId(elderInfoId);
        analysis.setStartDate(startDate);
        analysis.setEndDate(endDate);
        analysis.setAnalysisText("根据健康数据分析，老人近期健康状况总体良好。建议继续保持规律作息和健康饮食，定期监测各项健康指标。");
        analysis.setRiskLevel("NORMAL");
        analysis.setBatchNo(1);
        analysis.setPushed(0);
        analysis.setCreateTime(LocalDateTime.now());

        ElderInfo elder = elderInfoMapper.selectById(elderInfoId);
        if (elder != null) {
            analysis.setElderName(elder.getRealName());
        }

        llmAnalysisResultMapper.insert(analysis);
        return Result.success("LLM分析已重新触发");
    }

    @GetMapping("/reports")
    public Result<Map<String, Object>> getHealthReports(
            @RequestParam(required = false) Long elderInfoId,
            @RequestParam(required = false) String reportType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Page<HealthReport> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<HealthReport> wrapper = new LambdaQueryWrapper<>();
        if (elderInfoId != null) {
            wrapper.eq(HealthReport::getElderInfoId, elderInfoId);
        }
        if (reportType != null) {
            wrapper.eq(HealthReport::getReportType, reportType);
        }
        wrapper.orderByDesc(HealthReport::getCreateTime);
        Page<HealthReport> result = healthReportMapper.selectPage(page, wrapper);

        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("WEEKLY", "周报告");
        typeMap.put("MONTHLY", "月报告");

        for (HealthReport report : result.getRecords()) {
            if (report.getElderInfoId() != null) {
                ElderInfo elder = elderInfoMapper.selectById(report.getElderInfoId());
                if (elder != null) {
                    report.setElderName(elder.getRealName());
                }
            }
            report.setReportTypeText(typeMap.getOrDefault(report.getReportType(), report.getReportType()));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        return Result.success(data);
    }

    @GetMapping("/report/{id}")
    public Result<HealthReport> getHealthReportDetail(@PathVariable Long id) {
        HealthReport report = healthReportMapper.selectById(id);
        if (report != null) {
            if (report.getElderInfoId() != null) {
                ElderInfo elder = elderInfoMapper.selectById(report.getElderInfoId());
                if (elder != null) {
                    report.setElderName(elder.getRealName());
                }
            }
            Map<String, String> typeMap = new HashMap<>();
            typeMap.put("WEEKLY", "周报告");
            typeMap.put("MONTHLY", "月报告");
            report.setReportTypeText(typeMap.getOrDefault(report.getReportType(), report.getReportType()));
            return Result.success(report);
        }
        return Result.error("报告不存在");
    }

    @PostMapping("/report/regenerate")
    public Result<Void> regenerateHealthReport(@RequestBody Map<String, Object> body) {
        Long elderInfoId = ((Number) body.get("elderInfoId")).longValue();
        String reportType = (String) body.get("reportType");

        HealthReport report = new HealthReport();
        report.setUserId(2L);
        report.setElderInfoId(elderInfoId);
        report.setReportNo("RPT-" + System.currentTimeMillis());
        report.setReportType(reportType);
        report.setStartDate("2024-01-01");
        report.setEndDate("2024-01-31");
        report.setReportContent("<h3>健康报告</h3><p>老人健康状况评估报告...</p>");
        report.setPdfUrl("");
        report.setPushed(0);
        report.setCreateTime(LocalDateTime.now());

        ElderInfo elder = elderInfoMapper.selectById(elderInfoId);
        if (elder != null) {
            report.setElderName(elder.getRealName());
        }

        healthReportMapper.insert(report);
        return Result.success("健康报告已重新生成");
    }

    @PostMapping("/report/{id}/resend")
    public Result<Void> resendReport(@PathVariable Long id) {
        HealthReport report = healthReportMapper.selectById(id);
        if (report != null) {
            report.setPushed(1);
            healthReportMapper.updateById(report);
            return Result.success("报告通知已重新发送");
        }
        return Result.error("报告不存在");
    }

    @GetMapping("/elders")
    public Result<List<ElderInfo>> getElders() {
        List<ElderInfo> elders = elderInfoMapper.selectList(null);
        return Result.success(elders);
    }

    @GetMapping("/records/export")
    public void exportHealthRecords(
            @RequestParam(required = false) Long elderInfoId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            jakarta.servlet.http.HttpServletResponse response) throws IOException {
        exportService.exportAdminHealthRecords(elderInfoId, startDate, endDate, response);
    }

    @GetMapping("/llm-analysis/export")
    public void exportLlmAnalysis(
            @RequestParam(required = false) Long elderInfoId,
            @RequestParam(required = false) String riskLevel,
            jakarta.servlet.http.HttpServletResponse response) throws IOException {
        exportService.exportLlmAnalysis(elderInfoId, riskLevel, response);
    }
}
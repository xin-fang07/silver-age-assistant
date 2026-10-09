package com.example.elderai.controller;

import com.example.elderai.common.Result;
import com.example.elderai.dto.HealthRecordDTO;
import com.example.elderai.dto.DeviceHealthDataDTO;
import com.example.elderai.dto.HealthImportResultDTO;
import com.example.elderai.entity.HealthRecord;
import com.example.elderai.service.HealthDataImportService;
import com.example.elderai.service.HealthRecordService;
import com.example.elderai.security.SecurityUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;

/**
 * 健康记录控制器
 * <p>
 * 提供老年人健康指标的记录、查询、图表数据获取和删除功能。
 * 支持血压（收缩压/舒张压）、血糖、心率、体重等指标的记录，
 * 并可生成图表数据和 AI 健康建议。
 * </p>
 *
 * @author elder-ai-team
 */
@RestController
@RequestMapping("/api/health")
@Validated
public class HealthController {

    /** 注入健康记录服务，处理健康数据的增删查和分析 */
    @Resource
    private HealthRecordService healthRecordService;

    @Resource
    private HealthDataImportService healthDataImportService;

    /**
     * 从 HTTP 请求头中提取 JWT Token 并解析出当前登录用户的 ID
     *
     * @return 当前登录用户的 ID
     */
    private Long getCurrentUserId() {
        return SecurityUtils.currentUserId();
    }

    /**
     * 新增健康记录
     * <p>
     * 记录一次健康指标数据，包括血压、血糖、心率、体重及记录日期。
     * 至少需提供记录日期（recordDate），其他指标为可选。
     * </p>
     *
     * @param dto 健康记录请求参数（包含 bloodPressureHigh、bloodPressureLow、bloodSugar、heartRate、weight、recordDate、remark）
     * @return Result 对象，data 为保存成功的健康记录
     */
    @PostMapping("/record")
    public Result<HealthRecord> add(@Valid @RequestBody HealthRecordDTO dto) {
        Long userId = getCurrentUserId();
        // 调用 healthRecordService.add() 保存健康记录
        HealthRecord record = healthRecordService.add(userId, dto);
        return Result.success("健康记录添加成功", record);
    }

    /** 批量导入 CSV/XLS/XLSX，合法行入库，错误行在结果中逐行返回。 */
    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public Result<HealthImportResultDTO> importData(@RequestPart("file") MultipartFile file) {
        return Result.success("导入处理完成", healthDataImportService.importFile(getCurrentUserId(), file));
    }

    /** 智能手环、血压计、血糖仪等设备统一数据接入接口。 */
    @PostMapping("/device-data")
    public Result<HealthRecord> receiveDeviceData(@Valid @RequestBody DeviceHealthDataDTO dto) {
        return Result.success("设备数据接收成功", healthRecordService.addDeviceData(getCurrentUserId(), dto));
    }

    /**
     * 按日期范围查询当前用户的健康记录
     * <p>
     * 查询指定日期范围内的所有健康记录，按记录日期升序排列。
     * startDate 和 endDate 为可选参数：
     * 不传则查询所有记录，只传 startDate 则查询该日期之后，
     * 只传 endDate 则查询该日期之前。
     * </p>
     *
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd，如 "2024-01-01"）
     * @param endDate   结束日期（可选，格式：yyyy-MM-dd，如 "2024-01-31"）
     * @return Result 对象，data 为健康记录列表
     */
    @GetMapping("/list")
    public Result<List<HealthRecord>> list(@RequestParam(required = false)
                                           @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "日期格式必须为 yyyy-MM-dd") String startDate,
                                           @RequestParam(required = false)
                                           @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "日期格式必须为 yyyy-MM-dd") String endDate) {
        Long userId = getCurrentUserId();
        // 调用 healthRecordService.listByUser() 按日期范围查询
        List<HealthRecord> list = healthRecordService.listByUser(userId, startDate, endDate);
        return Result.success(list);
    }

    /**
     * 获取健康数据图表所需的数据
     * <p>
     * 将指定日期范围内的健康记录转换为前端 ECharts 图表所需的数组格式。
     * 返回的 Map 包含 5 个并列数组：
     * <ul>
     *   <li>dates - 日期数组（X轴）</li>
     *   <li>systolic - 收缩压数组</li>
     *   <li>diastolic - 舒张压数组</li>
     *   <li>bloodSugar - 血糖数组</li>
     *   <li>heartRate - 心率数组</li>
     * </ul>
     * </p>
     *
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate   结束日期（可选，格式：yyyy-MM-dd）
     * @return Result 对象，data 为图表数据 Map
     */
    @GetMapping("/chart")
    public Result<Map<String, Object>> getChartData(@RequestParam(required = false)
                                                     @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "日期格式必须为 yyyy-MM-dd") String startDate,
                                                     @RequestParam(required = false)
                                                     @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "日期格式必须为 yyyy-MM-dd") String endDate) {
        Long userId = getCurrentUserId();
        // 调用 healthRecordService.getChartData() 获取图表数据
        Map<String, Object> chartData = healthRecordService.getChartData(userId, startDate, endDate);
        return Result.success(chartData);
    }

    /**
     * 修改健康记录
     */
    @PutMapping("/{id}")
    public Result<HealthRecord> update(@PathVariable Long id,
                                        @Valid @RequestBody HealthRecordDTO dto) {
        Long userId = getCurrentUserId();
        HealthRecord record = healthRecordService.update(id, userId, dto);
        return Result.success("修改成功", record);
    }

    /**
     * 删除单条健康记录
     * <p>
     * 只能删除当前用户自己的记录，Service 层会校验归属权。
     * 此操作不可逆，建议前端弹出确认提示。
     * </p>
     *
     * @param id 健康记录 ID
     * @return Result 对象，操作结果提示
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        // 调用 healthRecordService.delete() 删除指定记录
        healthRecordService.delete(id, userId);
        return Result.success();
    }

    /**
     * 获取 AI 健康建议
     * <p>
     * 获取当前用户最近 7 天的健康数据，拼接成文本发送给 DeepSeek AI，
     * 由 AI 分析数据趋势并给出个性化的健康建议。
     * 若 AI 调用失败，则返回预设的降级建议文本（如"建议定期监测"）。
     * 注意：此建议不保存到聊天记录中。
     * </p>
     *
     * @return Result 对象，data 为 AI 生成的健康建议文本
     */
    @GetMapping("/advice")
    public Result<String> getAdvice() {
        Long userId = getCurrentUserId();
        // 调用 healthRecordService.generateAdvice() 获取 AI 健康建议
        String advice = healthRecordService.generateAdvice(userId);
        return Result.success(advice);
    }
}

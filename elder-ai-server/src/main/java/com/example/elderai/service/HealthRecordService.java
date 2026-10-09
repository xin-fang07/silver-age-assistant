package com.example.elderai.service;

import com.example.elderai.dto.HealthRecordDTO;
import com.example.elderai.dto.DeviceHealthDataDTO;
import com.example.elderai.entity.HealthRecord;

import java.util.List;
import java.util.Map;

/**
 * 健康记录服务接口
 * <p>
 * 提供健康指标的增删查和图表数据生成功能，
 * 以及基于最近健康数据的 AI 健康建议生成。
 * </p>
 *
 * @author elder-ai-team
 */
public interface HealthRecordService {

    /**
     * 新增健康记录
     * <p>
     * 将 DTO 中的数据转换为 HealthRecord 实体并保存到数据库。
     * </p>
     *
     * @param userId 当前登录用户ID
     * @param dto    健康记录请求参数
     * @return 保存成功的健康记录
     */
    HealthRecord add(Long userId, HealthRecordDTO dto);

    HealthRecord addDeviceData(Long userId, DeviceHealthDataDTO dto);

    HealthRecord addImported(Long userId, HealthRecordDTO dto, String batchId);

    HealthRecord addAdmin(HealthRecordDTO dto);

    /**
     * 修改健康记录
     */
    HealthRecord update(Long id, Long userId, HealthRecordDTO dto);

    /**
     * 按日期范围查询当前用户的健康记录
     * <p>
     * 查询指定日期范围内的所有健康记录，按记录日期升序排列。
     * </p>
     *
     * @param userId    当前登录用户ID
     * @param startDate 开始日期（字符串格式，如 "2024-01-01"）
     * @param endDate   结束日期（字符串格式，如 "2024-01-31"）
     * @return 健康记录列表
     */
    List<HealthRecord> listByUser(Long userId, String startDate, String endDate);

    /**
     * 获取图表所需数据
     * <p>
     * 将指定日期范围内的健康记录转换为前端 ECharts 图表所需的数组格式。
     * 返回的 Map 包含6个List字段：
     * <ul>
     *   <li>dates - 日期数组</li>
     *   <li>systolic - 收缩压数组</li>
     *   <li>diastolic - 舒张压数组</li>
     *   <li>bloodSugar - 血糖数组</li>
     *   <li>heartRate - 心率数组</li>
     *   <li>weight - 体重数组</li>
     * </ul>
     * </p>
     *
     * @param userId    当前登录用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 图表数据 Map
     */
    Map<String, Object> getChartData(Long userId, String startDate, String endDate);

    /**
     * 删除单条健康记录
     * <p>
     * 需验证该记录属于当前用户，防止越权删除。
     * </p>
     *
     * @param id     健康记录ID
     * @param userId 当前登录用户ID
     */
    void delete(Long id, Long userId);

    /**
     * 生成健康建议
     * <p>
     * 获取当前用户最近7天的健康数据，拼接成文本发送给 DeepSeek AI 获取健康建议。
     * 如果 AI 调用失败，则返回预设的降级建议文本。
     * 注意：此建议不保存到 chat_record 表（这不是聊天记录，而是健康分析）。
     * </p>
     *
     * @param userId 当前登录用户ID
     * @return AI 生成的健康建议文本
     */
    String generateAdvice(Long userId);
}

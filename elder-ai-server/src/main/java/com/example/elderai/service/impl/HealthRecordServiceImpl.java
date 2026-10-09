package com.example.elderai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.elderai.common.BusinessException;
import com.example.elderai.dto.HealthRecordDTO;
import com.example.elderai.dto.DeviceHealthDataDTO;
import com.example.elderai.entity.HealthRecord;
import com.example.elderai.entity.Device;
import com.example.elderai.mapper.DeviceMapper;
import com.example.elderai.mapper.ElderInfoMapper;
import com.example.elderai.mapper.HealthRecordMapper;
import com.example.elderai.service.HealthRecordService;
import com.example.elderai.service.HealthWarningService;
import com.example.elderai.utils.DeepSeekClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 健康记录服务实现类
 * <p>
 * 提供健康指标的增删查、图表数据生成和 AI 健康建议功能。
 * 健康数据以日期为单位记录，支持按日期范围查询和统计。
 * </p>
 *
 * @author elder-ai-team
 */
@Service // 标记为 Spring Service Bean
public class HealthRecordServiceImpl implements HealthRecordService {

    private static final Logger log = LoggerFactory.getLogger(HealthRecordServiceImpl.class);

    /** 健康记录数据访问层 */
    @Autowired
    private HealthRecordMapper healthRecordMapper;

    @Autowired
    private ElderInfoMapper elderInfoMapper;

    /** DeepSeek AI 客户端，用于生成健康建议 */
    @Autowired
    private DeepSeekClient deepSeekClient;

    @Autowired
    private HealthWarningService healthWarningService;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private Validator validator;

    // ==================== 新增健康记录 ====================

    /**
     * 新增健康记录
     * <p>
     * 将 DTO 中的健康指标数据映射为 HealthRecord 实体并保存。
     * </p>
     *
     * @param userId 当前登录用户ID
     * @param dto    健康记录参数
     * @return 保存成功的健康记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public HealthRecord add(Long userId, HealthRecordDTO dto) {
        return saveRecord(userId, dto, "MANUAL", null, null, null, null, dto.getElderInfoId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HealthRecord addImported(Long userId, HealthRecordDTO dto, String batchId) {
        return saveRecord(userId, dto, "FILE_IMPORT", null, null, batchId, null, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HealthRecord addAdmin(HealthRecordDTO dto) {
        return saveRecord(2L, dto, dto.getSourceType() != null ? dto.getSourceType() : "MANUAL", null, null, null, null, dto.getElderInfoId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HealthRecord addDeviceData(Long userId, DeviceHealthDataDTO dto) {
        // 通过设备编号查找设备，确定数据归属的家属与老人档案
        Device device = deviceMapper.selectOne(new LambdaQueryWrapper<Device>()
                .eq(Device::getDeviceId, dto.getDeviceId()));
        // 设备数据归属到绑定该设备的家属；若设备未注册则回退到当前登录用户
        Long ownerUserId = (device != null && device.getUserId() != null) ? device.getUserId() : userId;
        // 设备绑定的老人档案ID（可能为 null，前端/管理端未绑定时留空）
        Long elderInfoId = (device != null) ? device.getElderId() : null;

        Long duplicateCount = healthRecordMapper.selectCount(new LambdaQueryWrapper<HealthRecord>()
                .eq(HealthRecord::getUserId, ownerUserId)
                .eq(HealthRecord::getSourceDeviceId, dto.getDeviceId())
                .eq(HealthRecord::getExternalRecordId, dto.getExternalRecordId()));
        if (duplicateCount > 0) {
            throw new BusinessException(409, "该设备记录已接收，请勿重复提交");
        }
        HealthRecordDTO metrics = dto.getMetrics();
        metrics.setRecordDate(dto.getMeasuredAt().toLocalDate());
        Set<ConstraintViolation<HealthRecordDTO>> violations = validator.validate(metrics);
        if (!violations.isEmpty()) {
            String message = violations.stream().map(ConstraintViolation::getMessage)
                    .distinct().reduce((a, b) -> a + "；" + b).orElse("健康指标不合法");
            throw new BusinessException(400, message);
        }
        String remark = metrics.getRemark();
        metrics.setRemark((remark == null || remark.isBlank() ? "" : remark + "；") + "设备类型：" + dto.getDeviceType());
        return saveRecord(ownerUserId, metrics, "DEVICE", dto.getDeviceId(), dto.getExternalRecordId(), null, dto.getMeasuredAt(), elderInfoId);
    }

    private HealthRecord saveRecord(Long userId, HealthRecordDTO dto, String sourceType,
                                    String deviceId, String externalRecordId, String batchId,
                                    LocalDateTime measuredAt, Long elderInfoId) {
        // 构建 HealthRecord 实体
        HealthRecord record = new HealthRecord();
        record.setUserId(userId);
        record.setElderInfoId(elderInfoId);
        record.setBloodPressureHigh(dto.getBloodPressureHigh());
        record.setBloodPressureLow(dto.getBloodPressureLow());
        record.setBloodSugar(dto.getBloodSugar());
        record.setHeartRate(dto.getHeartRate());
        record.setBloodOxygen(dto.getBloodOxygen());
        record.setSteps(dto.getSteps());
        record.setWeight(dto.getWeight());
        record.setRecordDate(dto.getRecordDate());
        record.setMeasuredAt(measuredAt);
        record.setSourceType(sourceType);
        record.setSourceDeviceId(deviceId);
        record.setExternalRecordId(externalRecordId);
        record.setImportBatchId(batchId);
        record.setRemark(dto.getRemark());
        record.setCreateTime(LocalDateTime.now());

        // 插入数据库
        healthRecordMapper.insert(record);
        healthWarningService.evaluateRecord(record);
        log.info("用户 [{}] 新增了健康记录，日期: {}", userId, dto.getRecordDate());

        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HealthRecord update(Long id, Long userId, HealthRecordDTO dto) {
        HealthRecord record = healthRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(404, "健康记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权修改此记录");
        }
        record.setBloodPressureHigh(dto.getBloodPressureHigh());
        record.setBloodPressureLow(dto.getBloodPressureLow());
        record.setBloodSugar(dto.getBloodSugar());
        record.setHeartRate(dto.getHeartRate());
        record.setBloodOxygen(dto.getBloodOxygen());
        record.setSteps(dto.getSteps());
        record.setWeight(dto.getWeight());
        record.setRecordDate(dto.getRecordDate());
        record.setRemark(dto.getRemark());
        healthRecordMapper.updateById(record);
        healthWarningService.evaluateRecord(record);
        return record;
    }

    // ==================== 按日期范围查询 ====================

    /**
     * 按日期范围查询当前用户的健康记录
     * <p>
     * 查询指定日期范围内的所有记录，按记录日期升序排列。
     * startDate 和 endDate 为字符串格式（如 "2024-01-01"），转换为 LocalDate 进行查询。
     * </p>
     *
     * @param userId    当前登录用户ID
     * @param startDate 开始日期字符串
     * @param endDate   结束日期字符串
     * @return 健康记录列表
     */
    @Override
    public List<HealthRecord> listByUser(Long userId, String startDate, String endDate) {
        LocalDate parsedStart = startDate == null || startDate.isBlank() ? null : LocalDate.parse(startDate);
        LocalDate parsedEnd = endDate == null || endDate.isBlank() ? null : LocalDate.parse(endDate);
        if (parsedStart != null && parsedEnd != null && parsedStart.isAfter(parsedEnd)) {
            throw new BusinessException(400, "开始日期不能晚于结束日期");
        }
        // 构建查询条件
        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthRecord::getUserId, userId);

        // 日期范围过滤（recordDate 在 startDate 和 endDate 之间）
        if (parsedStart != null) {
            wrapper.ge(HealthRecord::getRecordDate, parsedStart);
        }
        if (parsedEnd != null) {
            wrapper.le(HealthRecord::getRecordDate, parsedEnd);
        }

        // 按记录日期升序排列
        wrapper.orderByAsc(HealthRecord::getRecordDate);

        List<HealthRecord> list = healthRecordMapper.selectList(wrapper);
        // 填充老人姓名（根据 elderInfoId 查 elder_info.real_name），便于前端展示数据归属
        fillElderNames(list);
        return list;
    }

    /**
     * 根据 elderInfoId 批量查询老人姓名并填充到记录的 elderName 字段（非持久化）
     */
    private void fillElderNames(List<HealthRecord> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> elderIds = list.stream()
                .map(HealthRecord::getElderInfoId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (elderIds.isEmpty()) {
            return;
        }
        List<com.example.elderai.entity.ElderInfo> elders = elderInfoMapper.selectBatchIds(elderIds);
        Map<Long, String> nameMap = elders.stream()
                .collect(Collectors.toMap(com.example.elderai.entity.ElderInfo::getId, e -> e.getRealName() == null ? "未命名老人" : e.getRealName(), (a, b) -> a));
        list.forEach(r -> {
            if (r.getElderInfoId() != null) {
                r.setElderName(nameMap.get(r.getElderInfoId()));
            }
        });
    }

    // ==================== 图表数据 ====================

    /**
     * 获取图表所需的数据数组
     * <p>
     * 将健康记录列表转换为前端 ECharts 图表所需的并行数组格式。
     * 每个数组中的元素按日期一一对应，便于图表绘制。
     * </p>
     *
     * @param userId    当前登录用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return Map 包含5个数组：dates, systolic, diastolic, bloodSugar, heartRate
     */
    @Override
    public Map<String, Object> getChartData(Long userId, String startDate, String endDate) {
        // 1. 获取日期范围内的健康记录
        List<HealthRecord> records = listByUser(userId, startDate, endDate);

        // 2. 构建各指标的数据数组
        List<String> dates = new ArrayList<>();         // 日期
        List<Integer> systolic = new ArrayList<>();     // 收缩压（高压）
        List<Integer> diastolic = new ArrayList<>();    // 舒张压（低压）
        List<Object> bloodSugar = new ArrayList<>();    // 血糖值
        List<Integer> heartRate = new ArrayList<>();    // 心率
        List<Object> weight = new ArrayList<>();        // 体重

        for (HealthRecord record : records) {
            // 日期：格式化为字符串 "yyyy-MM-dd"
            dates.add(record.getRecordDate().toString());
            // 收缩压：可能为 null
            systolic.add(record.getBloodPressureHigh());
            // 舒张压：可能为 null
            diastolic.add(record.getBloodPressureLow());
            // 血糖：BigDecimal 转字符串保留精度，或保留为 null
            bloodSugar.add(record.getBloodSugar());
            // 心率：可能为 null
            heartRate.add(record.getHeartRate());
            weight.add(record.getWeight());
        }

        // 3. 组装返回 Map
        Map<String, Object> chartData = new LinkedHashMap<>();
        chartData.put("dates", dates);
        chartData.put("systolic", systolic);
        chartData.put("diastolic", diastolic);
        chartData.put("bloodSugar", bloodSugar);
        chartData.put("heartRate", heartRate);
        chartData.put("weight", weight);

        return chartData;
    }

    // ==================== 删除记录 ====================

    /**
     * 删除单条健康记录
     * <p>
     * 删除前验证该记录是否属于当前用户。
     * </p>
     *
     * @param id     健康记录ID
     * @param userId 当前登录用户ID
     * @throws BusinessException 记录不存在或不属于当前用户时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        // 1. 查询记录确认存在
        HealthRecord record = healthRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(404, "健康记录不存在");
        }

        // 2. 验证记录归属
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除此健康记录");
        }

        // 3. 执行物理删除
        healthWarningService.deleteByRecord(id, userId);
        healthRecordMapper.deleteById(id);
        log.info("用户 [{}] 删除了健康记录 [{}]", userId, id);
    }

    // ==================== AI 健康建议 ====================

    /**
     * 生成 AI 健康建议
     * <p>
     * 获取用户最近7天的健康数据，拼接成文本发送给 DeepSeek AI，
     * 让 AI 根据数据给出个性化的健康建议。
     * 如果 AI 调用失败，返回预设的降级建议。
     * 注意：此建议不保存到 chat_record 表。
     * </p>
     *
     * @param userId 当前登录用户ID
     * @return AI 健康建议文本
     */
    @Override
    public String generateAdvice(Long userId) {
        // 1. 计算最近7天的日期范围
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6); // 最近7天（含今天）

        // 2. 查询最近7天的健康记录
        List<HealthRecord> records = listByUser(userId, startDate.toString(), endDate.toString());

        // 如果没有数据，返回提示信息
        if (records.isEmpty()) {
            return "您最近7天还没有记录健康数据，建议您先记录血压、血糖、心率等健康指标，以便我为您提供个性化的健康建议。";
        }

        // 3. 将健康数据拼接成易于 AI 理解的文本描述
        StringBuilder dataText = new StringBuilder();
        dataText.append("以下是用户最近7天的健康数据记录，请基于这些数据给出健康建议：\n");

        for (HealthRecord record : records) {
            dataText.append("日期：").append(record.getRecordDate());
            if (record.getBloodPressureHigh() != null && record.getBloodPressureLow() != null) {
                dataText.append("，血压：").append(record.getBloodPressureHigh())
                        .append("/").append(record.getBloodPressureLow()).append("mmHg");
            }
            if (record.getBloodSugar() != null) {
                dataText.append("，血糖：").append(record.getBloodSugar()).append("mmol/L");
            }
            if (record.getHeartRate() != null) {
                dataText.append("，心率：").append(record.getHeartRate()).append("次/分");
            }
            if (record.getWeight() != null) {
                dataText.append("，体重：").append(record.getWeight()).append("kg");
            }
            dataText.append("；\n");
        }
        dataText.append("请用简单易懂的语言，面向老年人，给出健康分析、风险提示和生活建议。"
                + "不得作出疾病诊断或要求用户自行停药、改药；明显异常时建议咨询医生，出现紧急症状时提示拨打120。"
                + "字数控制在300字以内。");

        // 4. 尝试调用 DeepSeek AI 获取健康建议
        try {
            log.info("用户 [{}] 请求生成健康建议", userId);
            String advice = deepSeekClient.chat(dataText.toString());
            log.info("DeepSeek 健康建议生成成功，用户: {}", userId);
            return advice;
        } catch (RuntimeException e) {
            // 5. AI 调用失败，返回预设的降级建议
            log.warn("DeepSeek 健康建议生成失败，返回降级建议。用户: {}，异常类型: {}",
                    userId, e.getClass().getSimpleName());

            // 分析用户的基础健康数据，生成简单的降级建议
            return buildFallbackAdvice(records);
        }
    }

    /**
     * 构建降级健康建议
     * <p>
     * 当 AI 服务不可用时，基于最近数据的平均值生成简单的健康提示。
     * </p>
     *
     * @param records 最近7天的健康记录
     * @return 降级健康建议文本
     */
    private String buildFallbackAdvice(List<HealthRecord> records) {
        StringBuilder advice = new StringBuilder();
        advice.append("【健康数据摘要】\n");

        // 计算血压平均值（过滤 null 值）
        double avgSystolic = records.stream()
                .filter(r -> r.getBloodPressureHigh() != null)
                .mapToInt(HealthRecord::getBloodPressureHigh)
                .average().orElse(0);
        double avgDiastolic = records.stream()
                .filter(r -> r.getBloodPressureLow() != null)
                .mapToInt(HealthRecord::getBloodPressureLow)
                .average().orElse(0);

        // 计算心率平均值
        double avgHeartRate = records.stream()
                .filter(r -> r.getHeartRate() != null)
                .mapToInt(HealthRecord::getHeartRate)
                .average().orElse(0);

        long bloodPressureCount = records.stream()
                .filter(r -> r.getBloodPressureHigh() != null && r.getBloodPressureLow() != null)
                .count();
        long heartRateCount = records.stream()
                .filter(r -> r.getHeartRate() != null)
                .count();

        if (bloodPressureCount > 0) {
            advice.append("最近7天平均血压：").append(String.format("%.0f", avgSystolic))
                    .append("/").append(String.format("%.0f", avgDiastolic)).append("mmHg。\n");
        }
        if (heartRateCount > 0) {
            advice.append("最近7天平均心率：").append(String.format("%.0f", avgHeartRate))
                    .append("次/分。\n");
        }
        advice.append("\n");

        // 根据平均值给出基本建议
        advice.append("【基本建议】\n");
        if (bloodPressureCount > 0 && (avgSystolic > 140 || avgDiastolic > 90)) {
            advice.append("您的血压偏高，建议低盐低脂饮食，保持心情舒畅，定期测量血压，必要时就医。\n");
        } else if (bloodPressureCount > 0 && (avgSystolic < 90 || avgDiastolic < 60)) {
            advice.append("您的血压偏低，建议适当增加营养，避免突然站立，如感头晕请及时休息。\n");
        } else if (bloodPressureCount > 0) {
            advice.append("您的血压在正常范围内，请继续保持良好的生活习惯。\n");
        }

        if (heartRateCount > 0 && avgHeartRate > 100) {
            advice.append("您的心率偏快，建议避免剧烈运动和情绪激动，保持充足睡眠。\n");
        } else if (heartRateCount > 0 && avgHeartRate < 60) {
            advice.append("您的心率偏慢，如有不适请及时就医检查。\n");
        }

        advice.append("\n温馨提示：以上建议仅供参考，如有身体不适请及时就医。");
        return advice.toString();
    }
}

package com.example.elderai.dto;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 健康记录请求DTO
 * 用于创建健康指标记录
 */
@Data
public class HealthRecordDTO {

    /** 收缩压（高压） */
    @JsonAlias("systolicPressure")
    @Min(value = 50, message = "收缩压不能低于50 mmHg")
    @Max(value = 260, message = "收缩压不能高于260 mmHg")
    private Integer bloodPressureHigh;

    /** 舒张压（低压） */
    @JsonAlias("diastolicPressure")
    @Min(value = 30, message = "舒张压不能低于30 mmHg")
    @Max(value = 160, message = "舒张压不能高于160 mmHg")
    private Integer bloodPressureLow;

    /** 血糖值 */
    @DecimalMin(value = "1.0", message = "血糖不能低于1.0 mmol/L")
    @DecimalMax(value = "40.0", message = "血糖不能高于40.0 mmol/L")
    @Digits(integer = 2, fraction = 2, message = "血糖最多保留两位小数")
    private BigDecimal bloodSugar;

    /** 心率（次/分钟） */
    @Min(value = 30, message = "心率不能低于30次/分")
    @Max(value = 220, message = "心率不能高于220次/分")
    private Integer heartRate;

    /** 血氧饱和度(%) */
    @Min(value = 50, message = "血氧饱和度不能低于50%")
    @Max(value = 100, message = "血氧饱和度不能高于100%")
    private Integer bloodOxygen;

    /** 每日步数 */
    @Min(value = 0, message = "步数不能为负数")
    @Max(value = 100000, message = "步数不能超过100000")
    private Integer steps;

    /** 体重（kg） */
    @DecimalMin(value = "20.0", message = "体重不能低于20kg")
    @DecimalMax(value = "300.0", message = "体重不能高于300kg")
    @Digits(integer = 3, fraction = 2, message = "体重最多保留两位小数")
    private BigDecimal weight;

    /** 记录日期，不能为空 */
    @NotNull(message = "记录日期不能为空")
    @PastOrPresent(message = "记录日期不能晚于今天")
    private LocalDate recordDate;

    /** 备注信息 */
    @Size(max = 500, message = "备注不能超过500个字")
    private String remark;

    /** 关联老人档案ID（补录/手动录入时由前端选择传入） */
    private Long elderInfoId;

    /** 数据来源（管理员手动录入时指定） */
    private String sourceType;

    @AssertTrue(message = "请至少填写一项健康指标")
    public boolean isAnyMetricPresent() {
        return bloodPressureHigh != null || bloodPressureLow != null || bloodSugar != null
                || heartRate != null || weight != null || bloodOxygen != null || steps != null;
    }

    @AssertTrue(message = "收缩压和舒张压必须同时填写")
    public boolean isBloodPressureComplete() {
        return (bloodPressureHigh == null) == (bloodPressureLow == null);
    }

    @AssertTrue(message = "收缩压必须高于舒张压")
    public boolean isBloodPressureOrderValid() {
        return bloodPressureHigh == null || bloodPressureLow == null
                || bloodPressureHigh > bloodPressureLow;
    }
}

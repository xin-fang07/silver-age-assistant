package com.example.elderai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 模拟智能设备上传健康数据请求体。
 * 由外部设备（或演示用模拟设备）调用 POST /api/device/upload 上报。
 */
@Data
public class DeviceUploadDTO {

    /** 关联老人档案ID（elder_info.id），标识数据归属哪位老人 */
    @NotNull(message = "老人ID不能为空")
    private Long elderId;

    /** 心率（次/分钟） */
    private Integer heartRate;

    /** 血压，格式 "收缩压/舒张压"，如 "120/80" */
    private String bloodPressure;

    /** 血氧饱和度(%) */
    private Integer bloodOxygen;

    /** 血糖(mmol/L) */
    private BigDecimal bloodSugar;

    /** 步数 */
    private Integer steps;
}

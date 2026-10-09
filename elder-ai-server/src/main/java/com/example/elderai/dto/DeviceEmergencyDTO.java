package com.example.elderai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 模拟智能设备上报 SOS 紧急求助请求体。
 * 由外部设备（或演示用模拟设备）调用 POST /api/device/emergency 上报。
 */
@Data
public class DeviceEmergencyDTO {

    /** 关联老人档案ID（elder_info.id），标识报警归属哪位老人 */
    @NotNull(message = "老人ID不能为空")
    private Long elderId;

    /** 求助内容描述 */
    private String content;

    /** 报警定位纬度 */
    private BigDecimal latitude;

    /** 报警定位经度 */
    private BigDecimal longitude;

    /** 报警位置文本描述 */
    private String locationText;

    /** 触发报警的设备编号（可选） */
    private String deviceId;
}

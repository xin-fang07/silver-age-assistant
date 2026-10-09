package com.example.elderai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/** 智能手环、血压计、血糖仪等设备的统一上报协议。 */
@Data
public class DeviceHealthDataDTO {
    @NotBlank(message = "设备类型不能为空")
    @Size(max = 40, message = "设备类型不能超过40个字符")
    private String deviceType;

    @NotBlank(message = "设备编号不能为空")
    @Size(max = 64, message = "设备编号不能超过64个字符")
    private String deviceId;

    @NotBlank(message = "外部记录编号不能为空")
    @Size(max = 100, message = "外部记录编号不能超过100个字符")
    private String externalRecordId;

    @NotNull(message = "测量时间不能为空")
    @PastOrPresent(message = "测量时间不能晚于当前时间")
    private LocalDateTime measuredAt;

    @NotNull(message = "健康指标不能为空")
    private HealthRecordDTO metrics;
}

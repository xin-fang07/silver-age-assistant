package com.example.elderai.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 紧急求助请求DTO
 * 接收老年人发起的紧急求助信息
 */
@Data
public class EmergencyHelpDTO {

    /** 紧急联系人姓名，不能为空 */
    @Size(max = 50, message = "联系人姓名不能超过50字")
    private String contactName;

    /** 紧急联系人电话，不能为空 */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "联系人电话格式不正确")
    private String contactPhone;

    @Email(message = "联系人邮箱格式不正确")
    private String contactEmail;

    /** 求助内容描述 */
    @Size(max = 500, message = "求助内容不能超过500字")
    private String helpContent;

    @DecimalMin(value = "-90.0", message = "纬度不正确")
    @DecimalMax(value = "90.0", message = "纬度不正确")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "经度不正确")
    @DecimalMax(value = "180.0", message = "经度不正确")
    private BigDecimal longitude;

    @DecimalMin(value = "0.0", message = "定位精度不正确")
    @DecimalMax(value = "100000.0", message = "定位精度不正确")
    private BigDecimal locationAccuracy;

    @Size(max = 255, message = "位置描述不能超过255字")
    private String locationText;
}

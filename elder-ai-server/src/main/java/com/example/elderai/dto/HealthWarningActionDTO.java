package com.example.elderai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 健康预警处理请求。 */
@Data
public class HealthWarningActionDTO {

    /** 1-已知晓，2-已处理。 */
    @NotNull(message = "预警状态不能为空")
    @Min(value = 1, message = "预警状态不合法")
    @Max(value = 2, message = "预警状态不合法")
    private Integer status;

    @Size(max = 500, message = "处理说明不能超过500个字")
    private String actionNote;
}

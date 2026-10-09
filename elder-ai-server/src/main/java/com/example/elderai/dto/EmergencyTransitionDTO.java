package com.example.elderai.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 管理员推进紧急求助状态。 */
@Data
public class EmergencyTransitionDTO {

    @NotNull(message = "目标状态不能为空")
    @Min(value = 1, message = "目标状态不正确")
    @Max(value = 3, message = "目标状态不正确")
    private Integer status;

    @Size(max = 500, message = "处理备注不能超过500字")
    private String remark;
}

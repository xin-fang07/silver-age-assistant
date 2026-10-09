package com.example.elderai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;

/**
 * 提醒事项请求DTO
 * 用于创建或更新提醒事项
 */
@Data
public class ReminderDTO {

    /** 提醒标题，不能为空 */
    @NotBlank(message = "提醒标题不能为空")
    @Size(max = 100, message = "提醒标题不能超过100个字")
    private String title;

    /** 提醒详细内容 */
    @Size(max = 500, message = "提醒内容不能超过500个字")
    private String content;

    /** 关联老人档案ID（elder_info.id），家属创建的提醒必须指定服务于哪位老人 */
    @NotNull(message = "请选择关联老人")
    private Long elderInfoId;

    /** 提醒类型，如：MEDICINE-用药提醒, CHECKUP-体检提醒, ACTIVITY-活动提醒，不能为空 */
    @NotBlank(message = "提醒类型不能为空")
    @Pattern(regexp = "MEDICINE|EXERCISE|CHECKUP|PAYMENT|OTHER",
            message = "提醒类型不合法")
    private String remindType;

    /** 提醒时间，不能为空 */
    @NotNull(message = "提醒时间不能为空")
    @Future(message = "提醒时间必须晚于当前时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime remindTime;

    /** 重复规则：ONCE-仅一次，DAILY-每天，WEEKLY-每周。 */
    @Pattern(regexp = "ONCE|DAILY|WEEKLY", message = "重复规则不合法")
    private String repeatType = "ONCE";

    @Min(value = 1, message = "升级提醒次数不能小于1")
    @Max(value = 10, message = "升级提醒次数不能大于10")
    private Integer escalationThreshold = 2;
}

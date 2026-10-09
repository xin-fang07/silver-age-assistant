package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 每一次提醒的执行结果，用于照护追踪和完成率统计。 */
@Data
@TableName("reminder_execution")
public class ReminderExecution {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reminderId;
    private Long userId;
    private LocalDateTime scheduledTime;
    /** COMPLETED / SNOOZED / SKIPPED / MISSED */
    private String action;
    private LocalDateTime actionTime;
    private Integer snoozeMinutes;
    private Long operatorUserId;
    private String operatorRole;
    private LocalDateTime createTime;
}

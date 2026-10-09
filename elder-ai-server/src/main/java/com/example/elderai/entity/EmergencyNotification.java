package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 紧急求助通知尝试记录。 */
@Data
@TableName("emergency_notification")
public class EmergencyNotification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long helpId;
    private String channel;
    private String recipient;
    private String status;
    private Integer attemptNo;
    private String errorMessage;
    private LocalDateTime createTime;
    private LocalDateTime sentTime;
}

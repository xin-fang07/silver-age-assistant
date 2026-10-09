package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 站内通知（家属预警等）。 */
@Data
@TableName("notification")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 接收用户ID */
    private Long userId;
    /** 类型：SOS / HEALTH / REMINDER / SYSTEM */
    private String type;
    private String title;
    private String content;
    /** 关联业务ID（如求助单ID） */
    private Long refId;
    /** 0-未读 1-已读 */
    private Integer isRead;
    private LocalDateTime createTime;
}

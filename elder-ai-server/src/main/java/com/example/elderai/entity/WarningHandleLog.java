package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("warning_handle_log")
public class WarningHandleLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String eventType;

    private Long eventId;

    private Long elderId;

    private String action;

    private String actionDesc;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private LocalDateTime createTime;
}
package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("health_report")
public class HealthReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long elderInfoId;

    private String reportNo;

    private String reportType;

    private String startDate;

    private String endDate;

    private String reportContent;

    private String pdfUrl;

    private Integer pushed;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String elderName;

    @TableField(exist = false)
    private String reportTypeText;
}
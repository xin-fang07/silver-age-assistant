package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("llm_analysis_result")
public class LlmAnalysisResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long elderInfoId;

    private String analysisText;

    private String riskLevel;

    private String startDate;

    private String endDate;

    private Integer batchNo;

    private Integer pushed;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String elderName;

    @TableField(exist = false)
    private String riskLevelText;
}
package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健康记录实体类，对应数据库 health_record 表
 * 记录老年人日常的健康指标数据，包括血压、血糖、心率等
 */
@Data
@TableName("health_record")
public class HealthRecord {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的用户ID（设备数据归属到绑定该设备的家属） */
    private Long userId;

    /** 关联老人档案ID（elder_info.id）；设备上报数据据此归属到具体老人 */
    private Long elderInfoId;

    /** 收缩压（高压） */
    private Integer bloodPressureHigh;

    /** 舒张压（低压） */
    private Integer bloodPressureLow;

    /** 血糖值，使用BigDecimal保证精度 */
    private BigDecimal bloodSugar;

    /** 心率（次/分钟） */
    private Integer heartRate;

    /** 血氧饱和度(%) */
    private Integer bloodOxygen;

    /** 每日步数 */
    private Integer steps;

    /** 体重（kg），使用BigDecimal保证精度 */
    private BigDecimal weight;

    /** 记录日期 */
    private LocalDate recordDate;

    /** 实际测量时间（设备数据可精确到分钟，手工记录允许为空） */
    private LocalDateTime measuredAt;

    /** 数据来源：MANUAL、FILE_IMPORT、DEVICE */
    private String sourceType;

    /** 设备唯一编号或厂商侧设备编号 */
    private String sourceDeviceId;

    /** 厂商侧记录编号，用于接口幂等去重 */
    private String externalRecordId;

    /** 文件导入批次号 */
    private String importBatchId;

    /** 备注信息 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除, 1-已删除 */
    private Integer deleted;

    /** 老人姓名（非持久化字段，由后端根据 elderInfoId 查询 elder_info 填充，仅用于前端展示） */
    @TableField(exist = false)
    private String elderName;
}

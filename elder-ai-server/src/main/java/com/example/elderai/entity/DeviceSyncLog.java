package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备数据同步记录实体，对应 device_sync_log 表。
 * 每次设备上报（或模拟上传）都会留一条记录，便于管理员端审计与演示。
 */
@Data
@TableName("device_sync_log")
public class DeviceSyncLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 设备唯一标识 */
    private String deviceId;

    /** 设备名称 */
    private String deviceName;

    /** 关联老人档案ID */
    private Long elderInfoId;

    /** 老人姓名 */
    private String elderName;

    /** 家属用户ID */
    private Long familyUserId;

    /** 本次同步指标摘要 */
    private String metricsSummary;

    /** 同步状态 SUCCESS / FAIL */
    private String syncStatus;

    /** 失败原因 */
    private String errorMsg;

    /** 同步时间 */
    private LocalDateTime syncTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}

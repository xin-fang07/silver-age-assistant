package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 智能设备实体类，对应数据库 device 表
 * 记录家属绑定的智能手表 / 电子血压计 / 血糖仪等健康设备。
 * 设备上报的健康数据统一进入 health_record（source_type = DEVICE）。
 */
@Data
@TableName("device")
public class Device {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 绑定该设备的家属用户ID */
    private Long userId;

    /** 设备唯一编号（硬件唯一SN码） */
    private String deviceId;

    /** 设备名称（用户自定义） */
    private String deviceName;

    /** 设备类型：WATCH / BLOOD_PRESSURE / SLEEP_MONITOR / GLUCOSE / HEART_RATE / OTHER */
    private String deviceType;

    /** 设备型号 */
    private String model;

    /** 出厂编号 */
    private String factoryCode;

    /** 固件版本 */
    private String firmwareVersion;

    /** 生产批次 */
    private String productionBatch;

    /** 设备状态：1-在线，0-离线 */
    private Integer status;

    /** 设备启用/禁用状态：1-启用，0-禁用 */
    private Integer enabled;

    /** 厂商（可选） */
    private String vendor;

    /** 设备备注 */
    private String remark;

    /** 关联的老人档案ID（elder_info.id），可空 */
    private Long elderId;

    /** 最后同步时间 */
    private LocalDateTime lastSyncTime;

    /** 绑定时间 */
    private LocalDateTime bindTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

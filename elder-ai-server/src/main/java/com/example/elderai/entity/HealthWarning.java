package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 健康预警记录实体类，对应数据库 health_warning 表
 */
@Data
@TableName("health_warning")
public class HealthWarning {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的用户ID（兼容保留字段，新查询以 family_id 为准） */
    private Long userId;

    /** 接收通知的家属用户ID（家属端预警中心按此查询） */
    private Long familyId;

    /** 预警关联老人ID：elder_info.id，历史数据可能为 ELDER 用户ID */
    private Long elderId;

    /** 关联的健康记录ID */
    private Long recordId;

    /** 预警类型：BLOOD_PRESSURE-血压异常, BLOOD_SUGAR-血糖异常, HEART_RATE-心率异常 */
    private String warningType;

    /** 预警等级：1-轻度, 2-中度, 3-重度 */
    private Integer warningLevel;

    /** 预警详情描述 */
    private String warningContent;

    /** 是否已读：0-未读, 1-已读 */
    private Integer isRead;

    /** 处理状态：0-待处理, 1-已知晓, 2-已处理 */
    private Integer status;

    /** 用户填写的处理说明 */
    private String actionNote;

    /** 知晓或处理时间 */
    private LocalDateTime handledAt;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 展示用：老人姓名（不落库，由服务层填充） */
    @TableField(exist = false)
    private String elderName;
}

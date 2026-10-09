package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 紧急求助实体类，对应数据库 emergency_help 表
 * 记录老年人发起的紧急求助请求及处理情况
 */
@Data
@TableName("emergency_help")
public class EmergencyHelp {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的用户ID */
    private Long userId;

    /** 客户端请求幂等键；同一用户重复提交同一键时只创建一条求助 */
    private String requestId;

    /** 紧急联系人姓名 */
    private String contactName;

    /** 紧急联系人电话 */
    private String contactPhone;

    /** 紧急联系人邮箱 */
    private String contactEmail;

    /** 求助内容描述 */
    private String helpContent;

    /** 状态：0-已提交, 1-已接单, 2-处理中, 3-已完成, 4-已取消, 5-已升级 */
    private Integer status;

    /** 联系人通知状态：0-未配置, 2-已发送, 3-失败 */
    private Integer notificationStatus;

    /** 通知结果说明 */
    private String notificationMessage;

    /** 最近成功通知时间 */
    private LocalDateTime notifiedAt;

    /** 接单管理员 */
    private Long acknowledgedBy;

    private String acknowledgedRole;
    private String acknowledgedName;

    private LocalDateTime acknowledgedAt;
    private LocalDateTime processingAt;
    private LocalDateTime completedAt;
    private LocalDateTime escalatedAt;
    private Integer escalationLevel;

    /** 可选的求助定位 */
    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;
    /** 浏览器报告的95%置信定位误差半径，单位：米。 */
    private java.math.BigDecimal locationAccuracy;
    private String locationText;

    /** 处理备注信息 */
    private String handleRemark;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 老人电话号码（非数据库字段，用于前端展示） */
    @TableField(exist = false)
    private String elderPhone;
}

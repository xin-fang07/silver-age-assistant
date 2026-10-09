package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提醒事项实体类，对应数据库 reminder 表
 * 用于存储老年人的各类生活提醒（用药、体检、活动等）
 */
@Data
@TableName("reminder")
public class Reminder {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建者(家属)用户ID；提醒归属家属，按此字段查询与鉴权 */
    private Long userId;

    /** 关联老人档案ID（elder_info.id），标识该提醒服务于哪位老人 */
    private Long elderInfoId;

    /** 创建者家属用户ID，冗余存储便于追溯 */
    private Long familyUserId;

    /** 提醒标题 */
    private String title;

    /** 提醒详细内容 */
    private String content;

    /** 提醒类型，如：MEDICINE-用药提醒, CHECKUP-体检提醒, ACTIVITY-活动提醒 */
    private String remindType;

    /** 设定的提醒时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime remindTime;

    /** 状态：0-待提醒, 1-已完成, 2-已过期 */
    private Integer status;

    /** 重复规则：ONCE、DAILY、WEEKLY */
    private String repeatType;

    /** 最近完成时间 */
    private LocalDateTime lastCompletedAt;

    /** 累计完成次数 */
    private Integer completedCount;

    /** 累计错过次数 */
    private Integer missedCount;

    /** 连续未确认次数，完成一次后清零。 */
    private Integer consecutiveMissedCount;

    /** 连续未确认达到该次数时通知家属。 */
    private Integer escalationThreshold;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 推送状态：PENDING 待推送 / PUSHED 已推送 / FAILED 推送失败（虚拟推送，不接真实硬件） */
    private String pushStatus;

    /** 推送时间 */
    private LocalDateTime pushedAt;

    /** 老人确认状态：UNCONFIRMED 待确认 / CONFIRMED 已确认 */
    private String confirmStatus;

    /** 确认时间 */
    private LocalDateTime confirmedAt;

    /** 推送目标设备号（device.device_id） */
    private String targetDeviceId;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 非持久化：老人真实姓名（展示用） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String elderName;
}

package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动报名实体，对应表 activity_signup。
 * 家属用户为所绑定的某位老人报名线下活动，记录联系手机与备注。
 *
 * @author elder-ai-team
 */
@Data
@TableName("activity_signup")
public class ActivitySignup {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联活动 id */
    private Long activityId;

    /** 报名家属用户 id */
    private Long familyUserId;

    /** 关联老人档案 id（为哪位老人报名） */
    private Long elderInfoId;

    /** 联系手机 */
    private String contactPhone;

    /** 备注 */
    private String remark;

    /** 状态：0-已报名, 1-已取消 */
    private Integer status;

    private LocalDateTime createTime;

    /** 非持久化：活动标题（展示用） */
    @TableField(exist = false)
    private String activityTitle;

    /** 非持久化：老人真实姓名（展示用） */
    @TableField(exist = false)
    private String elderName;
}

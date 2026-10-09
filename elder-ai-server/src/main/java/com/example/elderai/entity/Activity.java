package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 娱乐活动实体，对应表 activity。
 * <p>
 * type 区分两类内容：ONLINE-线上资讯、OFFLINE-线下活动。
 * 线下活动携带地点/时间/报名截止/名额等字段并支持家属报名（activity_signup）。
 * 线上资讯仅使用 title/coverImage/content 字段。
 * LocalDateTime 字段使用 Jackson 默认 ISO 序列化（前端展示时再做格式化）。
 * </p>
 *
 * @author elder-ai-team
 */
@Data
@TableName("activity")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 活动标题 */
    private String title;

    /** 类型：ONLINE-线上资讯, OFFLINE-线下活动 */
    private String type;

    /** 封面图 URL */
    private String coverImage;

    /** 活动介绍 / 资讯正文 */
    private String content;

    /** 活动地点（线下活动） */
    private String location;

    /** 开始时间（线下活动） */
    private LocalDateTime startTime;

    /** 结束时间（线下活动） */
    private LocalDateTime endTime;

    /** 报名截止时间（线下活动） */
    private LocalDateTime signupDeadline;

    /** 名额上限，0 表示不限 */
    private Integer capacity;

    /** 已报名人数 */
    private Integer signupCount;

    /** 发布人（管理员 id） */
    private Long publisherId;

    /** 状态：0-草稿, 1-已发布, 2-已下架 */
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 新闻资讯实体类，对应数据库 news 表
 * 存储面向老年人的新闻、健康资讯等内容
 */
@Data
@TableName("news")
public class News {

    /** 主键ID，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 新闻标题 */
    private String title;

    /** 新闻正文内容 */
    private String content;

    /** 新闻摘要 */
    private String summary;

    /** 封面图片URL */
    private String coverImage;

    private String sourceUrl;

    /** 新闻类型：如 HEALTH-健康养生, LIFE-生活百科, POLICY-政策资讯 */
    private String newsType;

    /** 发布者用户ID */
    private Long publisherId;

    /** 浏览次数 */
    private Integer viewCount;

    /** 状态：0-草稿, 1-已发布, 2-已下架 */
    private Integer status;

    private LocalDateTime scheduledPublishTime;

    private LocalDateTime publishedAt;

    private Long updatedBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

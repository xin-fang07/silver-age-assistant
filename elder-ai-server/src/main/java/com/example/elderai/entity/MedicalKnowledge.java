package com.example.elderai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医疗知识库文章实体。
 * <p>
 * 存储面向老年人与家属的医疗健康科普文章，支持分类（慢病管理/用药安全/急救常识/
 * 饮食营养/心理健康/康复护理）、富文本正文、草稿/发布状态等。
 * 正文 content 为富文本，入库前必须经 {@code HtmlSanitizer} 清洗以防护 XSS。
 * </p>
 *
 * @author elder-ai-team
 */
@Data
@TableName("medical_knowledge")
public class MedicalKnowledge {

    /** 主键 ID（自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章标题 */
    private String title;

    /** 分类枚举：CHRONIC 慢病管理 / MEDICATION 用药安全 / FIRST_AID 急救常识 / NUTRITION 饮食营养 / MENTAL 心理健康 / REHAB 康复护理 */
    private String category;

    /** 分类中文标签（非持久化字段，由后端根据 category 枚举填充，仅用于前端展示） */
    @TableField(exist = false)
    private String categoryLabel;

    /** 摘要（列表展示用） */
    private String summary;

    /** 正文（富文本，入库前经 HtmlSanitizer 清洗） */
    private String content;

    /** 封面图地址（可选） */
    private String coverImage;

    /** 作者 */
    private String author;

    /** 浏览次数 */
    private Integer viewCount;

    /** 点赞次数 */
    private Integer likeCount;

    /** 状态：0-草稿，1-已发布 */
    private Integer status;

    /** 发布者（管理员）ID */
    private Long publisherId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}

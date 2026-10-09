package com.example.elderai.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 新闻发布请求DTO
 * 用于管理员发布新闻资讯
 */
@Data
public class NewsDTO {

    /** 新闻标题，不能为空 */
    @NotBlank(message = "新闻标题不能为空")
    private String title;

    /** 新闻正文内容，不能为空 */
    @NotBlank(message = "新闻内容不能为空")
    private String content;

    /** 新闻摘要 */
    private String summary;

    /** 封面图片URL */
    private String coverImage;

    private String sourceUrl;

    /** 新闻类型，如：HEALTH-健康养生, LIFE-生活百科, POLICY-政策资讯 */
    private String newsType;

    private Integer status;

    private LocalDateTime scheduledPublishTime;
}

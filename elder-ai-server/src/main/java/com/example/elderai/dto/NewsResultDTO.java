package com.example.elderai.dto;

import lombok.Data;

import java.util.List;

@Data
public class NewsResultDTO {

    private String title;

    private String summary;

    private String source;

    private String publishTime;

    private List<NewsArticle> articles;

    @Data
    public static class NewsArticle {
        private String title;
        private String description;
        private String source;
        private String url;
        private String publishTime;
        private String imageUrl;
    }
}
package com.example.elderai.dto;

import com.example.elderai.entity.News;
import lombok.Data;

import java.util.List;

/**
 * 资讯列表缓存载体（避免直接缓存泛型 PageResult 导致的类型擦除问题）。
 */
@Data
public class NewsListCacheDTO {
    private List<News> records;
    private long total;
    private long pageNum;
    private long pageSize;
}

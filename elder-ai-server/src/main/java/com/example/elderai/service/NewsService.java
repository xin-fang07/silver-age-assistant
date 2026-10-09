package com.example.elderai.service;

import com.example.elderai.common.PageResult;
import com.example.elderai.dto.NewsDTO;
import com.example.elderai.dto.NewsResultDTO;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.News;

public interface NewsService {

    NewsResultDTO getLatestNews(String category);

    NewsResultDTO searchNews(String keyword);

    PageResult<News> listPublished(PageQueryDTO dto);

    PageResult<News> listAll(PageQueryDTO dto);

    News getDetail(Long id);

    News create(Long publisherId, NewsDTO dto);

    News update(Long id, Long updatedBy, NewsDTO dto);

    News changeStatus(Long id, Long updatedBy, Integer status);

    void delete(Long id);
}
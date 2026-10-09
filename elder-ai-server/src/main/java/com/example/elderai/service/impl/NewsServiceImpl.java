package com.example.elderai.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.example.elderai.common.BusinessException;
import com.example.elderai.common.PageResult;
import com.example.elderai.dto.NewsDTO;
import com.example.elderai.dto.NewsResultDTO;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.News;
import com.example.elderai.mapper.NewsMapper;
import com.example.elderai.service.NewsService;
import com.example.elderai.service.RedisCacheService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class NewsServiceImpl implements NewsService {

    private static final Logger log = LoggerFactory.getLogger(NewsServiceImpl.class);

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private NewsMapper newsMapper;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();

    private static final String NEWS_API_URL = "https://newsapi.org/v2/top-headlines";
    private static final String NEWS_API_KEY = "your-newsapi-key";

    private static final Map<String, String> CATEGORY_MAP = Map.of(
            "头条", "general",
            "科技", "technology",
            "健康", "health",
            "体育", "sports",
            "娱乐", "entertainment",
            "business", "business",
            "science", "science"
    );

    private static final String[] CHINESE_SOURCES = {
            "bbc-news", "cnn", "reuters", "the-huffington-post", "the-new-york-times",
            "google-news", "techcrunch", "techradar", "engadget", "mashable"
    };

    @Override
    public NewsResultDTO getLatestNews(String category) {
        String cacheKey = "news:latest:" + (category != null ? category : "default");
        NewsResultDTO cached = redisCacheService.getObject(cacheKey, NewsResultDTO.class);
        if (cached != null) {
            return cached;
        }

        NewsResultDTO result = new NewsResultDTO();

        try {
            result = fetchNewsFromApi(category);
            if (result.getArticles() == null || result.getArticles().isEmpty()) {
                result = generateMockNews(category);
            }
        } catch (Exception e) {
            log.warn("新闻API调用失败，使用模拟数据: {}", e.getMessage());
            result = generateMockNews(category);
        }

        redisCacheService.setObject(cacheKey, result, 10);
        return result;
    }

    @Override
    public NewsResultDTO searchNews(String keyword) {
        String cacheKey = "news:search:" + keyword;
        NewsResultDTO cached = redisCacheService.getObject(cacheKey, NewsResultDTO.class);
        if (cached != null) {
            return cached;
        }

        NewsResultDTO result = new NewsResultDTO();

        try {
            result = searchNewsFromApi(keyword);
            if (result.getArticles() == null || result.getArticles().isEmpty()) {
                result = generateMockSearchNews(keyword);
            }
        } catch (Exception e) {
            log.warn("新闻搜索失败，使用模拟数据: {}", e.getMessage());
            result = generateMockSearchNews(keyword);
        }

        redisCacheService.setObject(cacheKey, result, 10);
        return result;
    }

    private NewsResultDTO fetchNewsFromApi(String category) {
        String apiCategory = CATEGORY_MAP.getOrDefault(category != null ? category : "头条", "general");
        String url = NEWS_API_URL + "?category=" + apiCategory + "&language=zh&pageSize=10&apiKey=" + NEWS_API_KEY;

        try {
            String json = get(url);
            JSONObject root = JSON.parseObject(json);
            NewsResultDTO result = new NewsResultDTO();
            result.setArticles(parseArticles(root.getJSONArray("articles")));
            result.setSource("NewsAPI");
            result.setPublishTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private NewsResultDTO searchNewsFromApi(String keyword) {
        String url = NEWS_API_URL + "?q=" + keyword + "&language=zh&pageSize=10&apiKey=" + NEWS_API_KEY;

        try {
            String json = get(url);
            JSONObject root = JSON.parseObject(json);
            NewsResultDTO result = new NewsResultDTO();
            result.setArticles(parseArticles(root.getJSONArray("articles")));
            result.setSource("NewsAPI");
            result.setPublishTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<NewsResultDTO.NewsArticle> parseArticles(JSONArray articles) {
        List<NewsResultDTO.NewsArticle> list = new ArrayList<>();
        if (articles == null) return list;

        for (int i = 0; i < articles.size(); i++) {
            JSONObject item = articles.getJSONObject(i);
            NewsResultDTO.NewsArticle article = new NewsResultDTO.NewsArticle();
            article.setTitle(item.getString("title"));
            article.setDescription(item.getString("description"));
            article.setSource(item.getString("source.name"));
            article.setUrl(item.getString("url"));
            article.setPublishTime(item.getString("publishedAt"));
            article.setImageUrl(item.getString("urlToImage"));
            list.add(article);
        }
        return list;
    }

    private NewsResultDTO generateMockNews(String category) {
        NewsResultDTO result = new NewsResultDTO();
        result.setSource("模拟新闻");
        result.setPublishTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        List<NewsResultDTO.NewsArticle> articles = new ArrayList<>();

        if ("健康".equals(category) || "health".equals(category)) {
            articles.add(createArticle("老年人健康养生指南", "专家建议老年人每天保持适量运动，饮食均衡，保持良好心态。", "健康时报"));
            articles.add(createArticle("冬季预防感冒小窍门", "冬季气温变化大，老年人应注意保暖，勤洗手，多喝水。", "养生周刊"));
            articles.add(createArticle("科学补钙的正确方法", "老年人补钙应选择合适的钙片，并配合维生素D促进吸收。", "健康在线"));
        } else if ("科技".equals(category) || "technology".equals(category)) {
            articles.add(createArticle("人工智能助力养老服务", "AI技术正在改变老年人的生活方式，提供更智能的健康管理。", "科技日报"));
            articles.add(createArticle("5G技术在养老领域的应用", "5G网络为远程医疗和智能看护提供了更好的技术支持。", "数码前沿"));
            articles.add(createArticle("智能家居让生活更便捷", "智能设备可以帮助老年人更方便地控制家中电器和环境。", "科技资讯"));
        } else if ("体育".equals(category) || "sports".equals(category)) {
            articles.add(createArticle("适合老年人的运动项目", "太极拳、广场舞、健步走等运动深受老年人喜爱。", "体育新闻"));
            articles.add(createArticle("冬季室内运动推荐", "天气寒冷时，老年人可以选择室内游泳、瑜伽等运动。", "健身指南"));
            articles.add(createArticle("老年人运动会圆满举行", "各地举办老年人运动会，展示老年人的精神风貌。", "体育快报"));
        } else {
            articles.add(createArticle("全国养老服务体系建设持续推进", "各地积极完善养老服务设施，提升老年人生活质量。", "新华社"));
            articles.add(createArticle("新型养老模式受到青睐", "社区养老、居家养老等多种模式满足不同老年人需求。", "人民日报"));
            articles.add(createArticle("老年人数字化生活培训", "社区开展智能手机使用培训，帮助老年人融入数字时代。", "民生新闻"));
            articles.add(createArticle("医疗保障政策利好老年人", "医保政策不断完善，减轻老年人看病负担。", "健康报"));
            articles.add(createArticle("关爱老年人心理健康", "社会各界关注老年人心理健康，提供心理咨询服务。", "心理周刊"));
        }

        result.setArticles(articles);
        return result;
    }

    private NewsResultDTO generateMockSearchNews(String keyword) {
        NewsResultDTO result = new NewsResultDTO();
        result.setSource("模拟搜索");
        result.setPublishTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        List<NewsResultDTO.NewsArticle> articles = new ArrayList<>();

        if (keyword.contains("新闻") || keyword.contains("联播")) {
            articles.add(createArticle("今日要闻速览", "今天的重要新闻包括：国内经济持续向好，民生保障不断加强。", "新闻联播"));
            articles.add(createArticle("国际新闻简报", "国际方面，各国加强合作，共同应对全球性挑战。", "国际新闻"));
            articles.add(createArticle("社会热点聚焦", "近期社会热点事件回顾与分析。", "热点追踪"));
        } else if (keyword.contains("天气") || keyword.contains("气候")) {
            articles.add(createArticle("近期天气变化趋势", "专家分析未来一周天气变化，提醒市民做好应对准备。", "气象预报"));
            articles.add(createArticle("气候变化与健康", "气候变化对老年人健康的影响及应对建议。", "健康气象"));
        } else {
            articles.add(createArticle("关于" + keyword + "的最新消息", "近期与" + keyword + "相关的重要动态和进展。", "综合报道"));
            articles.add(createArticle("专家解读" + keyword, "业内专家对" + keyword + "的深入分析和专业见解。", "专家观点"));
        }

        result.setArticles(articles);
        return result;
    }

    private NewsResultDTO.NewsArticle createArticle(String title, String description, String source) {
        NewsResultDTO.NewsArticle article = new NewsResultDTO.NewsArticle();
        article.setTitle(title);
        article.setDescription(description);
        article.setSource(source);
        article.setUrl("#");
        article.setPublishTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        return article;
    }

    private String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new RuntimeException("新闻API请求失败，状态码: " + response.code());
            }
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException("新闻API网络异常", e);
        }
    }

    @Override
    public PageResult<News> listPublished(PageQueryDTO dto) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<News> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(dto.getPageNum(), dto.getPageSize());
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<News> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(News::getStatus, 1);
        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            wrapper.like(News::getTitle, dto.getKeyword());
        }
        if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            wrapper.eq(News::getNewsType, dto.getCategory());
        }
        wrapper.orderByDesc(News::getCreateTime);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<News> resultPage = newsMapper.selectPage(page, wrapper);
        return PageResult.pageSuccess(resultPage.getRecords(), resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    public PageResult<News> listAll(PageQueryDTO dto) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<News> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(dto.getPageNum(), dto.getPageSize());
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<News> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            wrapper.like(News::getTitle, dto.getKeyword());
        }
        wrapper.orderByDesc(News::getCreateTime);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<News> resultPage = newsMapper.selectPage(page, wrapper);
        return PageResult.pageSuccess(resultPage.getRecords(), resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    public News getDetail(Long id) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new BusinessException(404, "资讯不存在");
        }
        news.setViewCount(news.getViewCount() != null ? news.getViewCount() + 1 : 1);
        newsMapper.updateById(news);
        return news;
    }

    @Override
    public News create(Long publisherId, NewsDTO dto) {
        News news = new News();
        news.setTitle(dto.getTitle());
        news.setContent(dto.getContent());
        news.setSummary(dto.getSummary());
        news.setCoverImage(dto.getCoverImage());
        news.setSourceUrl(dto.getSourceUrl());
        news.setNewsType(dto.getNewsType());
        news.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        news.setScheduledPublishTime(dto.getScheduledPublishTime());
        news.setPublisherId(publisherId);
        news.setViewCount(0);
        news.setCreateTime(java.time.LocalDateTime.now());
        news.setUpdateTime(java.time.LocalDateTime.now());
        newsMapper.insert(news);
        return news;
    }

    @Override
    public News update(Long id, Long updatedBy, NewsDTO dto) {
        News existing = newsMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "资讯不存在");
        }
        existing.setTitle(dto.getTitle());
        existing.setContent(dto.getContent());
        existing.setSummary(dto.getSummary());
        existing.setCoverImage(dto.getCoverImage());
        existing.setSourceUrl(dto.getSourceUrl());
        existing.setNewsType(dto.getNewsType());
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }
        existing.setScheduledPublishTime(dto.getScheduledPublishTime());
        existing.setUpdatedBy(updatedBy);
        existing.setUpdateTime(java.time.LocalDateTime.now());
        newsMapper.updateById(existing);
        return existing;
    }

    @Override
    public News changeStatus(Long id, Long updatedBy, Integer status) {
        News existing = newsMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "资讯不存在");
        }
        existing.setStatus(status);
        existing.setUpdatedBy(updatedBy);
        existing.setUpdateTime(java.time.LocalDateTime.now());
        if (status == 1 && existing.getPublishedAt() == null) {
            existing.setPublishedAt(java.time.LocalDateTime.now());
        }
        newsMapper.updateById(existing);
        return existing;
    }

    @Override
    public void delete(Long id) {
        if (newsMapper.selectById(id) == null) {
            throw new BusinessException(404, "资讯不存在");
        }
        newsMapper.deleteById(id);
    }
}
package com.example.elderai.controller;

import com.example.elderai.common.PageResult;
import com.example.elderai.common.Result;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.News;
import com.example.elderai.service.NewsService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 新闻资讯控制器（用户端）
 * <p>
 * 提供资讯的浏览功能，仅展示已发布状态的资讯。
 * 支持分页查询列表和查看单条资讯详情（包含完整内容），
 * 查看详情时自动增加浏览次数。
 * </p>
 *
 * @author elder-ai-team
 */
@RestController
@RequestMapping("/api/news")
public class NewsController {

    /** 注入新闻资讯服务，处理资讯查询的业务逻辑 */
    @Resource
    private NewsService newsService;

    /**
     * 分页查询已发布的资讯
     * <p>
     * 仅查询状态为"已发布"（status=1）的资讯，按创建时间倒序排列。
     * GET 请求通过 @RequestParam 接收分页参数，组装为 PageQueryDTO 调用 Service。
     * 此接口无需 JWT 认证，游客也可浏览（根据业务需求，这里保留 JWT 兼容设计）。
     * </p>
     *
     * @param pageNum  当前页码，默认第 1 页
     * @param pageSize 每页记录数，默认 10 条
     * @param keyword  搜索关键字（可选），对标题进行模糊匹配
     * @return PageResult 对象，包含已发布资讯列表、总记录数、当前页码和每页大小
     */
    @GetMapping("/list")
    public PageResult<News> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String category) {
        // 组装分页查询参数
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        dto.setKeyword(keyword);
        dto.setCategory(normalizeCategory(category));
        // 调用 newsService.listPublished() 查询已发布资讯
        return newsService.listPublished(dto);
    }

    private String normalizeCategory(String category) {
        if (category == null || category.isBlank()) return null;
        return switch (category.trim()) {
            case "健康养生", "HEALTH" -> "HEALTH";
            case "政策解读", "POLICY" -> "POLICY";
            case "社区活动", "ACTIVITY" -> "ACTIVITY";
            default -> null;
        };
    }

    /**
     * 获取资讯详情
     * <p>
     * 查询单条资讯的完整内容（包括 title、content、summary、coverImage 等）。
     * 调用此接口后，该资讯的浏览次数（viewCount）会自动 +1。
     * </p>
     *
     * @param id 资讯 ID
     * @return Result 对象，data 为资讯完整详情（包含 content 内容）
     */
    @GetMapping("/{id}")
    public Result<News> getDetail(@PathVariable Long id) {
        // 调用 newsService.getDetail() 获取详情并增加浏览计数
        News news = newsService.getDetail(id);
        return Result.success(news);
    }
}

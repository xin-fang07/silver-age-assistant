package com.example.elderai.controller;

import com.example.elderai.common.PageResult;
import com.example.elderai.common.Result;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.MedicalKnowledge;
import com.example.elderai.security.SecurityUtils;
import com.example.elderai.service.ChatService;
import com.example.elderai.service.MedicalKnowledgeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 医疗知识库控制器（家属端 / 公开）。
 * <p>
 * 提供知识文章的浏览、详情查看，以及 AI 健康问答（LLM 预留接口）。
 * </p>
 *
 * @author elder-ai-team
 */
@RestController
@RequestMapping("/api/knowledge")
public class MedicalKnowledgeController {

    @Resource
    private MedicalKnowledgeService knowledgeService;

    @Resource
    private ChatService chatService;

    /** 分页查询已发布知识文章（支持分类/关键字筛选） */
    @GetMapping("/list")
    public PageResult<MedicalKnowledge> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        dto.setKeyword(keyword);
        dto.setCategory(category);
        return knowledgeService.pagePublished(dto);
    }

    /** 分类枚举（供前端筛选/下拉） */
    @GetMapping("/categories")
    public Result<List<Map<String, String>>> categories() {
        return Result.success(knowledgeService.categories());
    }

    /** 文章详情（浏览量 +1） */
    @GetMapping("/{id}")
    public Result<MedicalKnowledge> detail(@PathVariable Long id) {
        return Result.success(knowledgeService.getDetail(id));
    }

    /**
     * AI 健康问答（LLM 预留接口）。
     * <p>
     * 当前直接复用 ChatService（底层 DeepSeek 大模型 + FAQ 降级）生成回答。
     * 后续可在此检索 medical_knowledge 表中相关文章，拼为上下文注入，
     * 实现「知识库 + AI 健康问答」的 RAG 能力。
     * </p>
     */
    @PostMapping("/ask")
    public Result<Map<String, Object>> ask(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        if (question == null || question.isBlank()) {
            return Result.error("请输入问题");
        }
        Long userId = SecurityUtils.currentUserId();
        // TODO: 后续接入知识库检索，将相关 medical_knowledge 文章作为 RAG 上下文传入 ChatService
        Map<String, Object> answer = chatService.chat(userId, question, null, null);
        return Result.success(answer);
    }
}

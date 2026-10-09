package com.example.elderai.controller;

import com.example.elderai.common.PageResult;
import com.example.elderai.common.Result;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.MedicalKnowledge;
import com.example.elderai.security.SecurityUtils;
import com.example.elderai.service.MedicalKnowledgeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员端医疗知识库控制器。
 * <p>
 * 位于 /api/admin/** 下，受全局 SecurityConfig 的 ADMIN 角色拦截保护。
 * 提供文章的增删改查与分类枚举。
 * </p>
 *
 * @author elder-ai-team
 */
@RestController
@RequestMapping("/api/admin/knowledge")
public class AdminMedicalKnowledgeController {

    @Resource
    private MedicalKnowledgeService knowledgeService;

    /** 分页查询全部文章（含草稿） */
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
        return knowledgeService.pageAll(dto);
    }

    /** 分类枚举 */
    @GetMapping("/categories")
    public Result<List<Map<String, String>>> categories() {
        return Result.success(knowledgeService.categories());
    }

    /** 新增文章 */
    @PostMapping
    public Result<Long> create(@RequestBody MedicalKnowledge entity) {
        Long publisherId = SecurityUtils.currentUserId();
        return Result.success(knowledgeService.create(entity, publisherId));
    }

    /** 更新文章 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody MedicalKnowledge entity) {
        entity.setId(id);
        knowledgeService.update(entity);
        return Result.success(null);
    }

    /** 删除文章 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return Result.success(null);
    }
}

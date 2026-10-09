package com.example.elderai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.elderai.common.BusinessException;
import com.example.elderai.common.HtmlSanitizer;
import com.example.elderai.common.PageResult;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.MedicalKnowledge;
import com.example.elderai.mapper.MedicalKnowledgeMapper;
import com.example.elderai.service.MedicalKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 医疗知识库服务实现类。
 * <p>
 * 实现文章的发布端/管理端分页查询、详情（浏览量+1）、增删改，以及分类枚举。
 * 富文本正文在入库前统一经 {@link HtmlSanitizer} 清洗，防护 XSS。
 * </p>
 *
 * @author elder-ai-team
 */
@Service
public class MedicalKnowledgeServiceImpl implements MedicalKnowledgeService {

    /** 分类枚举：value -> 中文 label */
    private static final Map<String, String> CATEGORY_LABELS = Map.of(
            "CHRONIC", "慢病管理",
            "MEDICATION", "用药安全",
            "FIRST_AID", "急救常识",
            "NUTRITION", "饮食营养",
            "MENTAL", "心理健康",
            "REHAB", "康复护理"
    );

    @Autowired
    private MedicalKnowledgeMapper medicalKnowledgeMapper;

    @Override
    public PageResult<MedicalKnowledge> pagePublished(PageQueryDTO dto) {
        Page<MedicalKnowledge> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<MedicalKnowledge> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalKnowledge::getStatus, 1);
        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            wrapper.like(MedicalKnowledge::getTitle, dto.getKeyword());
        }
        if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            wrapper.eq(MedicalKnowledge::getCategory, dto.getCategory());
        }
        wrapper.orderByDesc(MedicalKnowledge::getCreateTime);
        Page<MedicalKnowledge> resultPage = medicalKnowledgeMapper.selectPage(page, wrapper);
        resultPage.getRecords().forEach(this::fillCategoryLabel);
        return PageResult.pageSuccess(resultPage.getRecords(), resultPage.getTotal(),
                resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    public MedicalKnowledge getDetail(Long id) {
        MedicalKnowledge entity = medicalKnowledgeMapper.selectById(id);
        if (entity == null) {
            throw new BusinessException(404, "文章不存在");
        }
        entity.setViewCount(entity.getViewCount() != null ? entity.getViewCount() + 1 : 1);
        medicalKnowledgeMapper.updateById(entity);
        fillCategoryLabel(entity);
        return entity;
    }

    @Override
    public PageResult<MedicalKnowledge> pageAll(PageQueryDTO dto) {
        Page<MedicalKnowledge> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<MedicalKnowledge> wrapper = new LambdaQueryWrapper<>();
        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            wrapper.like(MedicalKnowledge::getTitle, dto.getKeyword());
        }
        if (dto.getCategory() != null && !dto.getCategory().isBlank()) {
            wrapper.eq(MedicalKnowledge::getCategory, dto.getCategory());
        }
        wrapper.orderByDesc(MedicalKnowledge::getCreateTime);
        Page<MedicalKnowledge> resultPage = medicalKnowledgeMapper.selectPage(page, wrapper);
        resultPage.getRecords().forEach(this::fillCategoryLabel);
        return PageResult.pageSuccess(resultPage.getRecords(), resultPage.getTotal(),
                resultPage.getCurrent(), resultPage.getSize());
    }

    @Override
    public Long create(MedicalKnowledge entity, Long publisherId) {
        entity.setContent(HtmlSanitizer.sanitize(entity.getContent()));
        if (entity.getStatus() == null) {
            entity.setStatus(0);
        }
        entity.setViewCount(0);
        entity.setLikeCount(0);
        entity.setPublisherId(publisherId);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        medicalKnowledgeMapper.insert(entity);
        return entity.getId();
    }

    @Override
    public void update(MedicalKnowledge entity) {
        MedicalKnowledge existing = medicalKnowledgeMapper.selectById(entity.getId());
        if (existing == null) {
            throw new BusinessException(404, "文章不存在");
        }
        if (entity.getTitle() != null) {
            existing.setTitle(entity.getTitle());
        }
        if (entity.getCategory() != null) {
            existing.setCategory(entity.getCategory());
        }
        if (entity.getSummary() != null) {
            existing.setSummary(entity.getSummary());
        }
        if (entity.getContent() != null) {
            existing.setContent(HtmlSanitizer.sanitize(entity.getContent()));
        }
        if (entity.getCoverImage() != null) {
            existing.setCoverImage(entity.getCoverImage());
        }
        if (entity.getAuthor() != null) {
            existing.setAuthor(entity.getAuthor());
        }
        if (entity.getStatus() != null) {
            existing.setStatus(entity.getStatus());
        }
        existing.setUpdateTime(LocalDateTime.now());
        medicalKnowledgeMapper.updateById(existing);
    }

    @Override
    public void delete(Long id) {
        if (medicalKnowledgeMapper.selectById(id) == null) {
            throw new BusinessException(404, "文章不存在");
        }
        medicalKnowledgeMapper.deleteById(id);
    }

    @Override
    public List<Map<String, String>> categories() {
        List<Map<String, String>> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : CATEGORY_LABELS.entrySet()) {
            Map<String, String> item = new HashMap<>();
            item.put("value", entry.getKey());
            item.put("label", entry.getValue());
            list.add(item);
        }
        return list;
    }

    /** 根据 category 枚举填充非持久化展示字段 categoryLabel */
    private void fillCategoryLabel(MedicalKnowledge entity) {
        if (entity.getCategory() != null) {
            entity.setCategoryLabel(CATEGORY_LABELS.get(entity.getCategory()));
        }
    }
}

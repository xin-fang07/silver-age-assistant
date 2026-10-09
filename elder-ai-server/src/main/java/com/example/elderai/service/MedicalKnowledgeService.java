package com.example.elderai.service;

import com.example.elderai.common.PageResult;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.MedicalKnowledge;

import java.util.List;
import java.util.Map;

/**
 * 医疗知识库服务接口。
 * <p>
 * 区分家属端（仅已发布）与管理端（含草稿）查询，并提供文章的增删改查与分类枚举。
 * </p>
 *
 * @author elder-ai-team
 */
public interface MedicalKnowledgeService {

    /** 家属端：分页查询已发布文章（支持分类/关键字筛选） */
    PageResult<MedicalKnowledge> pagePublished(PageQueryDTO dto);

    /** 家属端：文章详情（浏览量 +1，附分类中文标签） */
    MedicalKnowledge getDetail(Long id);

    /** 管理端：分页查询全部文章（含草稿） */
    PageResult<MedicalKnowledge> pageAll(PageQueryDTO dto);

    /** 新增文章（清洗正文、初始化统计字段，记录发布者） */
    Long create(MedicalKnowledge entity, Long publisherId);

    /** 更新文章（清洗正文，保留浏览/点赞等统计字段） */
    void update(MedicalKnowledge entity);

    /** 删除文章 */
    void delete(Long id);

    /** 分类枚举（value + 中文 label），供前端下拉与展示 */
    List<Map<String, String>> categories();
}

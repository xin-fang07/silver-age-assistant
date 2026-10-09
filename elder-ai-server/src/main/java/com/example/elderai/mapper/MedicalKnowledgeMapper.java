package com.example.elderai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderai.entity.MedicalKnowledge;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医疗知识库文章 Mapper。
 *
 * @author elder-ai-team
 */
@Mapper
public interface MedicalKnowledgeMapper extends BaseMapper<MedicalKnowledge> {
}

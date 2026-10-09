package com.example.elderai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderai.entity.MedicalRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医疗就诊记录 Mapper。
 *
 * @author elder-ai-team
 */
@Mapper
public interface MedicalRecordMapper extends BaseMapper<MedicalRecord> {
}

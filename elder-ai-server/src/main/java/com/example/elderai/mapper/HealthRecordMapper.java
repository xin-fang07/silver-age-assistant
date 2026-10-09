package com.example.elderai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderai.entity.HealthRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 健康记录Mapper接口
 * 继承MyBatis Plus的BaseMapper，自动获得CRUD能力
 */
@Mapper
public interface HealthRecordMapper extends BaseMapper<HealthRecord> {
}

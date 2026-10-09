package com.example.elderai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderai.entity.EmergencyHelp;
import org.apache.ibatis.annotations.Mapper;

/**
 * 紧急求助Mapper接口
 * 继承MyBatis Plus的BaseMapper，自动获得CRUD能力
 */
@Mapper
public interface EmergencyHelpMapper extends BaseMapper<EmergencyHelp> {
}

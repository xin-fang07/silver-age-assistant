package com.example.elderai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderai.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    List<SystemConfig> selectByConfigType(String configType);
}
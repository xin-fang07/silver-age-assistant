package com.example.elderai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderai.entity.SystemLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统日志Mapper接口
 * 继承MyBatis Plus的BaseMapper，自动获得CRUD能力
 */
@Mapper
public interface SystemLogMapper extends BaseMapper<SystemLog> {
}

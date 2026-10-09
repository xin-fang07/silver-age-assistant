package com.example.elderai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderai.entity.ElderInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 老年人信息Mapper接口
 * 继承MyBatis Plus的BaseMapper，自动获得CRUD能力
 */
@Mapper
public interface ElderInfoMapper extends BaseMapper<ElderInfo> {
}

package com.example.elderai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderai.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * 继承MyBatis Plus的BaseMapper，自动获得CRUD能力
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}

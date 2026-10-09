package com.example.elderai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderai.entity.News;
import org.apache.ibatis.annotations.Mapper;

/**
 * 新闻资讯Mapper接口
 * 继承MyBatis Plus的BaseMapper，自动获得CRUD能力
 */
@Mapper
public interface NewsMapper extends BaseMapper<News> {
}

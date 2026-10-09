package com.example.elderai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.elderai.entity.Reminder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 提醒事项Mapper接口
 * 继承MyBatis Plus的BaseMapper，自动获得CRUD能力
 */
@Mapper
public interface ReminderMapper extends BaseMapper<Reminder> {
}

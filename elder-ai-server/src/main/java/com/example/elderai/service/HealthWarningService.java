package com.example.elderai.service;

import com.example.elderai.common.PageResult;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.HealthWarning;
import com.example.elderai.entity.HealthRecord;

import java.util.List;

/**
 * 健康预警服务接口
 */
public interface HealthWarningService {

    /**
     * 检测健康数据异常并生成预警（按老人档案维度）
     */
    void checkAndWarn(Long elderId);

    /** 保存或修改健康记录后立即重新评估该记录。 */
    void evaluateRecord(HealthRecord record);

    /**
     * 分页查询预警列表
     */
    PageResult<HealthWarning> listByUser(Long userId, PageQueryDTO dto);

    /**
     * 管理员分页查询所有预警
     */
    PageResult<HealthWarning> listAll(PageQueryDTO dto);

    /**
     * 标记预警为已读
     */
    void markAsRead(Long id, Long userId);

    /** 用户确认知晓或完成处理。 */
    void updateStatus(Long id, Long userId, Integer status, String actionNote);

    /** 删除健康记录时同步清理关联预警。 */
    void deleteByRecord(Long recordId, Long userId);

    /**
     * 获取用户未读预警数量
     */
    int countUnread(Long userId);

    /**
     * 定时任务：检查所有用户最近7天的健康数据
     */
    void scheduledCheck();
}

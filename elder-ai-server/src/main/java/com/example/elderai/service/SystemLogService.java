package com.example.elderai.service;

import com.example.elderai.common.PageResult;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.SystemLog;

/**
 * 系统日志服务接口
 * <p>
 * 提供系统操作日志的分页查询和保存功能，
 * 用于审计追踪和问题排查。
 * </p>
 *
 * @author elder-ai-team
 */
public interface SystemLogService {

    /**
     * 分页查询系统日志
     * <p>
     * 按创建时间倒序排列，最新的日志排在最前面。
     * </p>
     *
     * @param dto 分页查询参数（支持 keyword 搜索）
     * @return 分页结果，包含日志列表和分页信息
     */
    PageResult<SystemLog> list(PageQueryDTO dto);

    /**
     * 保存系统日志
     * <p>
     * 将日志记录插入到 system_log 表中。
     * </p>
     *
     * @param log 系统日志实体
     */
    void save(SystemLog log);
}

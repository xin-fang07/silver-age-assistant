package com.example.elderai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.elderai.common.PageResult;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.SystemLog;
import com.example.elderai.mapper.SystemLogMapper;
import com.example.elderai.service.SystemLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 系统日志服务实现类
 * <p>
 * 提供系统操作日志的查询和保存功能。
 * 日志记录用于追踪用户操作行为和系统运行状态。
 * </p>
 *
 * @author elder-ai-team
 */
@Service // 标记为 Spring Service Bean
public class SystemLogServiceImpl implements SystemLogService {

    private static final Logger log = LoggerFactory.getLogger(SystemLogServiceImpl.class);

    /** 系统日志数据访问层 */
    @Autowired
    private SystemLogMapper systemLogMapper;

    // ==================== 分页查询 ====================

    /**
     * 分页查询系统日志
     * <p>
     * 按创建时间倒序排列，最新的日志排在最前面。
     * 支持按操作描述（operation）进行模糊搜索。
     * </p>
     *
     * @param dto 分页查询参数（支持 keyword 搜索 operation 字段）
     * @return 分页结果，包含日志列表和分页信息
     */
    @Override
    public PageResult<SystemLog> list(PageQueryDTO dto) {
        // 1. 构建分页对象
        Page<SystemLog> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        // 2. 构建查询条件
        LambdaQueryWrapper<SystemLog> wrapper = new LambdaQueryWrapper<>();

        // 支持按操作描述、用户名、请求编号或错误摘要模糊搜索
        if (dto.getKeyword() != null && !dto.getKeyword().trim().isEmpty()) {
            String keyword = dto.getKeyword().trim();
            wrapper.and(w -> w
                    .like(SystemLog::getOperation, keyword)
                    .or()
                    .like(SystemLog::getUsername, keyword)
                    .or()
                    .like(SystemLog::getTraceId, keyword)
                    .or()
                    .like(SystemLog::getErrorMessage, keyword)
            );
        }

        // 按创建时间倒序
        wrapper.orderByDesc(SystemLog::getCreateTime);

        // 3. 执行分页查询
        Page<SystemLog> resultPage = systemLogMapper.selectPage(page, wrapper);

        // 4. 构建分页返回结果
        return PageResult.pageSuccess(
                resultPage.getRecords(),
                resultPage.getTotal(),
                resultPage.getCurrent(),
                resultPage.getSize()
        );
    }

    // ==================== 保存日志 ====================

    /**
     * 保存系统日志
     * <p>
     * 将日志记录插入到 system_log 表中。
     * 如果 createTime 未设置，自动填充当前时间。
     * </p>
     *
     * @param sysLog 系统日志实体
     */
    @Override
    public void save(SystemLog sysLog) {
        // 如果创建时间为空，自动设置为当前时间
        if (sysLog.getCreateTime() == null) {
            sysLog.setCreateTime(java.time.LocalDateTime.now());
        }

        // 插入数据库
        systemLogMapper.insert(sysLog);
        log.debug("保存系统日志: {}", sysLog.getOperation());
    }
}

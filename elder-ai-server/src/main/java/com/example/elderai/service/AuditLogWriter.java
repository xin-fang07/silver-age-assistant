package com.example.elderai.service;

import com.example.elderai.entity.SystemLog;
import com.example.elderai.mapper.SystemLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** 审计日志异步持久化，写入失败不影响主业务响应。 */
@Service
public class AuditLogWriter {

    private static final Logger log = LoggerFactory.getLogger(AuditLogWriter.class);
    private final SystemLogMapper systemLogMapper;

    public AuditLogWriter(SystemLogMapper systemLogMapper) {
        this.systemLogMapper = systemLogMapper;
    }

    @Async("auditExecutor")
    public void write(SystemLog systemLog) {
        try {
            systemLogMapper.insert(systemLog);
        } catch (Exception ex) {
            log.error("审计日志写入失败，traceId={}", systemLog.getTraceId(), ex);
        }
    }
}

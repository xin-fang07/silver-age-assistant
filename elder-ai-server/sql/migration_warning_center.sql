USE elder_ai_assistant;

CREATE TABLE IF NOT EXISTS warning_handle_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    event_type VARCHAR(20) NOT NULL COMMENT '事件类型：HEALTH_WARNING-健康预警, SOS-紧急求助',
    event_id BIGINT NOT NULL COMMENT '关联事件ID（health_warning.id 或 emergency_help.id）',
    elder_id BIGINT DEFAULT NULL COMMENT '关联老人ID',
    action VARCHAR(50) NOT NULL COMMENT '操作类型：CREATE-创建, ACKNOWLEDGE-确认知晓, CONTACT_FAMILY-联系家属, HANDLE-处理中, RESOLVE-已解决, CLOSE-闭环完结',
    action_desc VARCHAR(200) DEFAULT NULL COMMENT '操作描述',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(100) DEFAULT NULL COMMENT '操作人姓名',
    remark TEXT DEFAULT NULL COMMENT '处理备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_event_type (event_type),
    KEY idx_event_id (event_id),
    KEY idx_elder_id (elder_id),
    KEY idx_action (action),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预警处理日志表';
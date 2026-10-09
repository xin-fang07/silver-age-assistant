-- 健康预警模块数据库迁移脚本
-- 执行方式：Navicat 中打开 elder_ai_assistant 库 → 右键 → 运行 SQL 文件 → 选择本文件

-- 创建健康预警表
CREATE TABLE IF NOT EXISTS health_warning (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    record_id BIGINT DEFAULT NULL COMMENT '关联的健康记录ID',
    warning_type VARCHAR(30) NOT NULL COMMENT '预警类型：BLOOD_PRESSURE-血压, BLOOD_SUGAR-血糖, HEART_RATE-心率',
    warning_level TINYINT NOT NULL DEFAULT 1 COMMENT '预警等级：1-轻度, 2-中度, 3-重度',
    warning_content VARCHAR(500) DEFAULT NULL COMMENT '预警详情描述',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读, 1-已读',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理, 1-已知晓, 2-已处理',
    action_note VARCHAR(500) DEFAULT NULL COMMENT '处理说明',
    handled_at DATETIME DEFAULT NULL COMMENT '知晓或处理时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_warning_type (warning_type),
    INDEX idx_create_time (create_time),
    UNIQUE KEY uk_record_type (record_id, warning_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康预警记录表';

-- 第二阶段：紧急求助闭环升级（只执行一次）
-- 执行前请先备份 elder_ai_assistant 数据库。

-- 将旧状态转换为新状态：旧 1=已处理 -> 新 3=已完成；旧 2=已取消 -> 新 4=已取消。
UPDATE emergency_help SET status = 3 WHERE status = 1;
UPDATE emergency_help SET status = 4 WHERE status = 2;

ALTER TABLE emergency_help
    ADD COLUMN contact_email VARCHAR(120) NULL COMMENT '紧急联系人邮箱' AFTER contact_phone,
    MODIFY COLUMN status TINYINT NOT NULL DEFAULT 0 COMMENT '0-已提交,1-已接单,2-处理中,3-已完成,4-已取消,5-已升级',
    ADD COLUMN notification_status TINYINT NOT NULL DEFAULT 0 COMMENT '0-未配置,2-成功,3-失败' AFTER status,
    ADD COLUMN notification_message VARCHAR(255) NULL COMMENT '通知结果说明' AFTER notification_status,
    ADD COLUMN notified_at DATETIME NULL COMMENT '最近成功通知时间' AFTER notification_message,
    ADD COLUMN acknowledged_by BIGINT NULL COMMENT '接单管理员ID' AFTER notified_at,
    ADD COLUMN acknowledged_at DATETIME NULL COMMENT '接单时间' AFTER acknowledged_by,
    ADD COLUMN processing_at DATETIME NULL COMMENT '开始处理时间' AFTER acknowledged_at,
    ADD COLUMN completed_at DATETIME NULL COMMENT '完成时间' AFTER processing_at,
    ADD COLUMN escalated_at DATETIME NULL COMMENT '最近升级时间' AFTER completed_at,
    ADD COLUMN escalation_level TINYINT NOT NULL DEFAULT 0 COMMENT '升级级别' AFTER escalated_at,
    ADD COLUMN latitude DECIMAL(10,7) NULL COMMENT '求助纬度' AFTER escalation_level,
    ADD COLUMN longitude DECIMAL(10,7) NULL COMMENT '求助经度' AFTER latitude,
    ADD COLUMN location_text VARCHAR(255) NULL COMMENT '位置描述' AFTER longitude;

CREATE TABLE emergency_notification (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知记录ID',
    help_id BIGINT NOT NULL COMMENT '求助ID',
    channel VARCHAR(30) NOT NULL COMMENT '通知渠道',
    recipient VARCHAR(160) DEFAULT NULL COMMENT '接收对象',
    status VARCHAR(20) NOT NULL COMMENT 'SENT/FAILED/SKIPPED',
    attempt_no INT NOT NULL DEFAULT 1 COMMENT '尝试次数',
    error_message VARCHAR(500) DEFAULT NULL COMMENT '失败说明',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_time DATETIME DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_help_id (help_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='紧急求助通知记录';

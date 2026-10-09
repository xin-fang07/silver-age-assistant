-- 第三阶段：健康预警闭环与周期提醒（只执行一次）
-- 执行前请确认已完成当前数据库备份。

ALTER TABLE health_warning
    ADD COLUMN status TINYINT NOT NULL DEFAULT 0 COMMENT '0-待处理,1-已知晓,2-已处理' AFTER is_read,
    ADD COLUMN action_note VARCHAR(500) NULL COMMENT '处理说明' AFTER status,
    ADD COLUMN handled_at DATETIME NULL COMMENT '知晓或处理时间' AFTER action_note,
    ADD COLUMN update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time;

UPDATE health_warning
SET status = CASE WHEN is_read = 1 THEN 1 ELSE 0 END;

ALTER TABLE health_warning
    ADD UNIQUE KEY uk_record_type (record_id, warning_type);

ALTER TABLE reminder
    ADD COLUMN repeat_type VARCHAR(20) NOT NULL DEFAULT 'ONCE' COMMENT 'ONCE/DAILY/WEEKLY' AFTER status,
    ADD COLUMN last_completed_at DATETIME NULL COMMENT '最近完成时间' AFTER repeat_type,
    ADD COLUMN completed_count INT NOT NULL DEFAULT 0 COMMENT '累计完成次数' AFTER last_completed_at,
    ADD COLUMN missed_count INT NOT NULL DEFAULT 0 COMMENT '累计错过次数' AFTER completed_count;

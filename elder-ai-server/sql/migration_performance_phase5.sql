-- 第五阶段：性能索引与紧急求助幂等升级
-- 执行前请先备份 elder_ai_assistant 数据库。本脚本只执行一次。

ALTER TABLE `emergency_help`
    ADD COLUMN `request_id` VARCHAR(64) NULL COMMENT '客户端幂等请求号' AFTER `user_id`,
    ADD UNIQUE KEY `uk_emergency_user_request` (`user_id`, `request_id`),
    ADD INDEX `idx_emergency_user_time` (`user_id`, `create_time`),
    ADD INDEX `idx_emergency_status_time` (`status`, `create_time`),
    ADD INDEX `idx_emergency_escalation` (`status`, `update_time`, `escalation_level`);

ALTER TABLE `chat_record`
    ADD INDEX `idx_chat_user_time` (`user_id`, `create_time`);

ALTER TABLE `reminder`
    ADD INDEX `idx_reminder_user_status_time` (`user_id`, `status`, `remind_time`),
    ADD INDEX `idx_reminder_status_time` (`status`, `remind_time`),
    ADD INDEX `idx_reminder_create_time` (`create_time`);

ALTER TABLE `health_record`
    ADD INDEX `idx_health_user_create` (`user_id`, `create_time`);

ALTER TABLE `health_warning`
    ADD INDEX `idx_warning_user_state` (`user_id`, `status`, `warning_level`, `create_time`),
    ADD INDEX `idx_warning_state_time` (`status`, `warning_level`, `create_time`);

ALTER TABLE `news`
    ADD INDEX `idx_news_status_time` (`status`, `create_time`);

ALTER TABLE `system_log`
    ADD INDEX `idx_log_time_user` (`create_time`, `user_id`);

ALTER TABLE `user`
    ADD INDEX `idx_user_status_role` (`status`, `role`);

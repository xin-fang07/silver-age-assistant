ALTER TABLE `reminder`
    ADD COLUMN `consecutive_missed_count` INT NOT NULL DEFAULT 0 COMMENT '连续未确认次数' AFTER `missed_count`;

CREATE TABLE IF NOT EXISTS `reminder_execution` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `reminder_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `scheduled_time` DATETIME NOT NULL,
    `action` VARCHAR(20) NOT NULL COMMENT 'COMPLETED/SNOOZED/SKIPPED/MISSED',
    `action_time` DATETIME DEFAULT NULL,
    `snooze_minutes` INT DEFAULT NULL,
    `operator_user_id` BIGINT DEFAULT NULL,
    `operator_role` VARCHAR(20) DEFAULT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_execution_user_time` (`user_id`, `scheduled_time`),
    KEY `idx_execution_reminder_time` (`reminder_id`, `scheduled_time`),
    KEY `idx_execution_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒执行流水';

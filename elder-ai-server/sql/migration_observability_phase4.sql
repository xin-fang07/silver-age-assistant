-- 第四阶段：企业级可观测性与审计字段升级
-- 执行前请先备份 elder_ai_assistant 数据库。本脚本只执行一次。

ALTER TABLE `system_log`
    ADD COLUMN `trace_id` VARCHAR(64) NULL COMMENT '请求追踪编号' AFTER `duration`,
    ADD COLUMN `status_code` INT NULL COMMENT 'HTTP状态码' AFTER `trace_id`,
    ADD COLUMN `success` TINYINT NULL COMMENT '是否成功：0-失败，1-成功' AFTER `status_code`,
    ADD COLUMN `error_message` VARCHAR(500) NULL COMMENT '安全错误摘要' AFTER `success`;

UPDATE `system_log`
SET `status_code` = 200,
    `success` = 1
WHERE `status_code` IS NULL;

-- 兼容迁移期间仍在运行的旧后端：旧实体未提供新字段时自动记录为成功。
ALTER TABLE `system_log`
    MODIFY COLUMN `status_code` INT NOT NULL DEFAULT 200 COMMENT 'HTTP状态码',
    MODIFY COLUMN `success` TINYINT NOT NULL DEFAULT 1 COMMENT '是否成功：0-失败，1-成功';

ALTER TABLE `system_log`
    ADD INDEX `idx_trace_id` (`trace_id`),
    ADD INDEX `idx_status_time` (`status_code`, `create_time`);

-- =====================================================
-- 迁移脚本：AI 对话上下文记忆功能
-- 为 chat_record 表增加 conversation_id 字段，用于多轮对话
-- 执行时间：部署 v1.1.0 时执行
-- =====================================================

USE elder_ai_assistant;

-- 1. 为 chat_record 表增加会话ID字段
ALTER TABLE `chat_record`
    ADD COLUMN `conversation_id` BIGINT NULL DEFAULT NULL COMMENT '会话ID，同一会话共享，用于上下文记忆'
    AFTER `user_id`;

-- 2. 增加索引：按用户+会话查询历史记录
ALTER TABLE `chat_record`
    ADD INDEX `idx_conv_user_time` (`user_id`, `conversation_id`, `create_time`);

-- 3. 历史数据回填：将已有记录的 conversation_id 设置为记录ID本身
-- （每条旧记录视为独立会话，不影响现有功能）
UPDATE `chat_record` SET `conversation_id` = `id` WHERE `conversation_id` IS NULL;

SHOW TABLES;
-- 表结构
DESC `user`;
DESC `elder_info`;
DESC `chat_record`;
DESC `reminder`;
DESC `emergency_help`;
DESC `health_record`;
DESC `news`;
DESC `system_log`;
DESC `health_warning`;
DESC `scheduler_lock`;
DESC `rate_limit_bucket`;
-- 索引
SHOW INDEX FROM `user`;
SHOW INDEX FROM `elder_info`;
SHOW INDEX FROM `chat_record`;
SHOW INDEX FROM `reminder`;
SHOW INDEX FROM `emergency_help`;
SHOW INDEX FROM `health_record`;
SHOW INDEX FROM `news`;
SHOW INDEX FROM `system_log`;
SHOW INDEX FROM `health_warning`;
SHOW INDEX FROM `scheduler_lock`;
SHOW INDEX FROM `rate_limit_bucket`;
-- 外键
SELECT TABLE_NAME, COLUMN_NAME, CONSTRAINT_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE WHERE TABLE_SCHEMA='elder_ai_assistant' AND REFERENCED_TABLE_NAME IS NOT NULL;
-- 数据统计
SELECT 'user' AS tbl, COUNT(*) AS cnt FROM `user`
UNION ALL SELECT 'elder_info', COUNT(*) FROM `elder_info`
UNION ALL SELECT 'chat_record', COUNT(*) FROM `chat_record`
UNION ALL SELECT 'reminder', COUNT(*) FROM `reminder`
UNION ALL SELECT 'emergency_help', COUNT(*) FROM `emergency_help`
UNION ALL SELECT 'health_record', COUNT(*) FROM `health_record`
UNION ALL SELECT 'news', COUNT(*) FROM `news`
UNION ALL SELECT 'system_log', COUNT(*) FROM `system_log`
UNION ALL SELECT 'health_warning', COUNT(*) FROM `health_warning`
UNION ALL SELECT 'scheduler_lock', COUNT(*) FROM `scheduler_lock`
UNION ALL SELECT 'rate_limit_bucket', COUNT(*) FROM `rate_limit_bucket`;

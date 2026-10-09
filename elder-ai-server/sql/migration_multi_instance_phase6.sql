-- 第六阶段：多实例任务租约与共享限流
-- 执行前请先备份 elder_ai_assistant 数据库。本脚本只执行一次。

CREATE TABLE `scheduler_lock` (
    `lock_name`             VARCHAR(100) NOT NULL COMMENT '任务锁名称',
    `locked_until`          DATETIME(3)  NOT NULL COMMENT '最长租约截止时间',
    `lock_at_least_until`   DATETIME(3)  NOT NULL COMMENT '最短持有截止时间',
    `locked_at`             DATETIME(3)  NOT NULL COMMENT '最近获得锁时间',
    `locked_by`             VARCHAR(100) NOT NULL COMMENT '实例标识',
    PRIMARY KEY (`lock_name`),
    KEY `idx_scheduler_locked_until` (`locked_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='多实例定时任务租约';

CREATE TABLE `rate_limit_bucket` (
    `bucket_key`     CHAR(64)    NOT NULL COMMENT 'SHA-256匿名限流键',
    `window_id`      BIGINT      NOT NULL COMMENT '固定窗口编号',
    `request_count`  INT         NOT NULL DEFAULT 1 COMMENT '窗口内请求数',
    `expires_at`     DATETIME(3) NOT NULL COMMENT '窗口过期时间',
    PRIMARY KEY (`bucket_key`, `window_id`),
    KEY `idx_rate_limit_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='多实例共享限流窗口';

-- ================================================================-- 数据库优化迁移建议（已回答答辩时参考）-- 1. 所有表建议增加 updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- 2. 建议增加 deleted TINYINT(1) DEFAULT 0 实现逻辑删除 -- 3. 建议增加外键约束(生产环境)或保持应用层完整性(当前方案) -- ================================================================-- =====================================================
-- 基于LLM的银发智能生活助手系统 - 数据库初始化脚本
-- 数据库名称：elder_ai_assistant
-- MySQL版本要求：8.0+
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS elder_ai_assistant
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE elder_ai_assistant;

-- =====================================================
-- 1. 用户表（管理员 + 老年用户统一管理）
-- =====================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    `username`      VARCHAR(50)     NOT NULL                 COMMENT '用户名',
    `password`      VARCHAR(255)    NOT NULL                 COMMENT '密码（BCrypt加密）',
    `role`          VARCHAR(20)     NOT NULL DEFAULT 'ELDER' COMMENT '角色：ELDER-老年用户, ADMIN-管理员',
    `phone`         VARCHAR(20)     DEFAULT NULL             COMMENT '手机号',
    `avatar`        VARCHAR(255)    DEFAULT NULL             COMMENT '头像URL',
    `status`        TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：1-正常, 0-禁用',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_user_status_role` (`status`, `role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 注意：默认用户数据不再通过 SQL 脚本插入！
-- 管理员和测试用户的密码由 DataInitializer 组件在项目启动时
-- 通过 BCryptPasswordEncoder 动态加密后自动创建。
-- 默认账号信息：
--   管理员：admin / Admin@123456  （角色：ADMIN）
--   测试用户：test / Test@123456  （角色：ELDER，手机：13800001111）
-- 如需修改默认密码，请编辑 DataInitializer.java 中的对应硬编码密码。
-- =====================================================
-- 以下为废弃的静态 INSERT 语句（密码哈希值与实际的 BCrypt 不匹配，已注释）：
-- INSERT INTO `user` (`username`, `password`, `role`) VALUES
-- ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 'ADMIN');
-- INSERT INTO `user` (`username`, `password`, `role`, `phone`) VALUES
-- ('test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', 'ELDER', '13800001111');

-- =====================================================
-- 2. 老人信息表（老年用户的详细资料）
-- =====================================================
DROP TABLE IF EXISTS `elder_info`;
CREATE TABLE `elder_info` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '信息ID',
    `user_id`           BIGINT          NOT NULL                 COMMENT '关联用户ID',
    `real_name`         VARCHAR(50)     DEFAULT NULL             COMMENT '真实姓名',
    `gender`            TINYINT         DEFAULT NULL             COMMENT '性别：0-女, 1-男',
    `age`               INT             DEFAULT NULL             COMMENT '年龄',
    `address`           VARCHAR(255)    DEFAULT NULL             COMMENT '住址',
    `emergency_contact` VARCHAR(50)     DEFAULT NULL             COMMENT '紧急联系人姓名',
    `emergency_phone`   VARCHAR(20)     DEFAULT NULL             COMMENT '紧急联系人电话',
    `medical_history`   TEXT            DEFAULT NULL             COMMENT '既往病史',
    `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老人信息表';

-- =====================================================
-- 3. 聊天记录表（LLM问答记录持久化）
-- =====================================================
DROP TABLE IF EXISTS `chat_record`;
CREATE TABLE `chat_record` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    `user_id`       BIGINT          NOT NULL                 COMMENT '用户ID',
    `conversation_id` BIGINT        DEFAULT NULL             COMMENT '会话ID，同一会话共享，用于多轮上下文记忆',
    `question`      TEXT            NOT NULL                 COMMENT '用户提问内容',
    `answer`        TEXT            NOT NULL                 COMMENT 'AI回答内容',
    `is_fallback`   TINYINT         NOT NULL DEFAULT 0       COMMENT '是否为降级回答：0-否, 1-是',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_chat_user_time` (`user_id`, `create_time`),
    KEY `idx_conv_user_time` (`user_id`, `conversation_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天记录表';

-- =====================================================
-- 4. 提醒事项表
-- =====================================================
DROP TABLE IF EXISTS `reminder`;
CREATE TABLE `reminder` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '提醒ID',
    `user_id`       BIGINT          NOT NULL                 COMMENT '用户ID',
    `title`         VARCHAR(100)    NOT NULL                 COMMENT '提醒标题',
    `content`       VARCHAR(500)    DEFAULT NULL             COMMENT '提醒内容描述',
    `remind_type`   VARCHAR(30)     NOT NULL                 COMMENT '提醒类型：MEDICINE-吃药, EXERCISE-运动, CHECKUP-体检, PAYMENT-缴费, OTHER-其他',
    `remind_time`   DATETIME        NOT NULL                 COMMENT '提醒时间',
    `status`        TINYINT         NOT NULL DEFAULT 0       COMMENT '状态：0-待提醒, 1-已完成, 2-已过期',
    `repeat_type`   VARCHAR(20)     NOT NULL DEFAULT 'ONCE'  COMMENT '重复规则：ONCE-仅一次, DAILY-每天, WEEKLY-每周',
    `last_completed_at` DATETIME    DEFAULT NULL             COMMENT '最近完成时间',
    `completed_count` INT           NOT NULL DEFAULT 0       COMMENT '累计完成次数',
    `missed_count`  INT             NOT NULL DEFAULT 0       COMMENT '累计错过次数',
    `consecutive_missed_count` INT   NOT NULL DEFAULT 0       COMMENT '连续未确认次数',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_remind_time` (`remind_time`),
    KEY `idx_reminder_user_status_time` (`user_id`, `status`, `remind_time`),
    KEY `idx_reminder_status_time` (`status`, `remind_time`),
    KEY `idx_reminder_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提醒事项表';

DROP TABLE IF EXISTS `reminder_execution`;
CREATE TABLE `reminder_execution` (
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

-- =====================================================
-- 5. 紧急求助表
-- =====================================================
DROP TABLE IF EXISTS `emergency_help`;
CREATE TABLE `emergency_help` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '求助ID',
    `user_id`           BIGINT          NOT NULL                 COMMENT '用户ID',
    `request_id`        VARCHAR(64)     DEFAULT NULL             COMMENT '客户端幂等请求号',
    `contact_name`      VARCHAR(50)     NOT NULL                 COMMENT '紧急联系人姓名',
    `contact_phone`     VARCHAR(20)     NOT NULL                 COMMENT '紧急联系人电话',
    `contact_email`     VARCHAR(120)    DEFAULT NULL             COMMENT '紧急联系人邮箱',
    `help_content`      VARCHAR(500)    DEFAULT NULL             COMMENT '求助备注',
    `status`            TINYINT         NOT NULL DEFAULT 0       COMMENT '状态：0-已提交,1-已接单,2-处理中,3-已完成,4-已取消,5-已升级',
    `notification_status` TINYINT       NOT NULL DEFAULT 0       COMMENT '联系人通知：0-未配置,2-成功,3-失败',
    `notification_message` VARCHAR(255) DEFAULT NULL             COMMENT '通知结果说明',
    `notified_at`       DATETIME        DEFAULT NULL             COMMENT '最近成功通知时间',
    `acknowledged_by`   BIGINT          DEFAULT NULL             COMMENT '接单管理员ID',
    `acknowledged_role` VARCHAR(20)     DEFAULT NULL             COMMENT '接单身份：ADMIN/FAMILY',
    `acknowledged_name` VARCHAR(50)     DEFAULT NULL             COMMENT '接单人显示名称',
    `acknowledged_at`   DATETIME        DEFAULT NULL             COMMENT '接单时间',
    `processing_at`     DATETIME        DEFAULT NULL             COMMENT '开始处理时间',
    `completed_at`      DATETIME        DEFAULT NULL             COMMENT '完成时间',
    `escalated_at`      DATETIME        DEFAULT NULL             COMMENT '最近升级时间',
    `escalation_level`  TINYINT         NOT NULL DEFAULT 0       COMMENT '升级级别',
    `latitude`          DECIMAL(10,7)   DEFAULT NULL             COMMENT '求助纬度',
    `longitude`         DECIMAL(10,7)   DEFAULT NULL             COMMENT '求助经度',
    `location_accuracy` DECIMAL(10,2)   DEFAULT NULL             COMMENT '定位误差半径（米，95%置信）',
    `location_text`     VARCHAR(255)    DEFAULT NULL             COMMENT '位置描述',
    `handle_remark`     VARCHAR(500)    DEFAULT NULL             COMMENT '处理备注',
    `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '求助时间',
    `update_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_emergency_user_request` (`user_id`, `request_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_emergency_user_time` (`user_id`, `create_time`),
    KEY `idx_emergency_status_time` (`status`, `create_time`),
    KEY `idx_emergency_escalation` (`status`, `update_time`, `escalation_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='紧急求助表';

CREATE TABLE `emergency_notification` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '通知记录ID',
    `help_id`       BIGINT       NOT NULL COMMENT '求助ID',
    `channel`       VARCHAR(30)  NOT NULL COMMENT '通知渠道',
    `recipient`     VARCHAR(160) DEFAULT NULL COMMENT '接收对象',
    `status`        VARCHAR(20)  NOT NULL COMMENT 'SENT/FAILED/SKIPPED',
    `attempt_no`    INT          NOT NULL DEFAULT 1 COMMENT '尝试次数',
    `error_message` VARCHAR(500) DEFAULT NULL COMMENT '失败说明',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `sent_time`     DATETIME     DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_help_id` (`help_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='紧急求助通知记录';

-- =====================================================
-- 6. 健康数据记录表
-- =====================================================
DROP TABLE IF EXISTS `health_record`;
CREATE TABLE `health_record` (
    `id`                BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '记录ID',
    `user_id`           BIGINT          NOT NULL                 COMMENT '用户ID',
    `blood_pressure_high` INT           DEFAULT NULL             COMMENT '收缩压（高压）',
    `blood_pressure_low`  INT           DEFAULT NULL             COMMENT '舒张压（低压）',
    `blood_sugar`       DECIMAL(5,2)    DEFAULT NULL             COMMENT '血糖值（mmol/L）',
    `heart_rate`        INT             DEFAULT NULL             COMMENT '心率（次/分）',
    `weight`            DECIMAL(5,2)    DEFAULT NULL             COMMENT '体重（kg）',
    `record_date`       DATE            NOT NULL                 COMMENT '记录日期',
    `measured_at`       DATETIME        DEFAULT NULL             COMMENT '设备实际测量时间',
    `source_type`       VARCHAR(20)     NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL/FILE_IMPORT/DEVICE',
    `source_device_id`  VARCHAR(64)     DEFAULT NULL             COMMENT '来源设备编号',
    `external_record_id` VARCHAR(100)   DEFAULT NULL             COMMENT '厂商侧记录编号',
    `import_batch_id`   VARCHAR(32)     DEFAULT NULL             COMMENT '文件导入批次号',
    `remark`            VARCHAR(500)    DEFAULT NULL             COMMENT '备注',
    `create_time`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_record` (`user_id`, `record_date`),
    KEY `idx_health_user_create` (`user_id`, `create_time`)
    ,UNIQUE KEY `uk_health_device_record` (`user_id`, `source_device_id`, `external_record_id`)
    ,KEY `idx_health_import_batch` (`import_batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康数据记录表';

-- =====================================================
-- 6.1 健康预警表
-- =====================================================
DROP TABLE IF EXISTS `health_warning`;
CREATE TABLE `health_warning` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         BIGINT       NOT NULL COMMENT '关联用户ID',
    `record_id`       BIGINT       DEFAULT NULL COMMENT '关联健康记录ID',
    `warning_type`    VARCHAR(30)  NOT NULL COMMENT 'BLOOD_PRESSURE/BLOOD_SUGAR/HEART_RATE',
    `warning_level`   TINYINT      NOT NULL DEFAULT 1 COMMENT '预警等级：1-轻度,2-中度,3-重度',
    `warning_content` VARCHAR(500) DEFAULT NULL COMMENT '预警详情',
    `is_read`         TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读',
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '0-待处理,1-已知晓,2-已处理',
    `action_note`     VARCHAR(500) DEFAULT NULL COMMENT '处理说明',
    `handled_at`      DATETIME     DEFAULT NULL COMMENT '处理时间',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_record_type` (`record_id`, `warning_type`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_warning_type` (`warning_type`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_warning_user_state` (`user_id`, `status`, `warning_level`, `create_time`),
    KEY `idx_warning_state_time` (`status`, `warning_level`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康预警记录表';

-- =====================================================
-- 7. 养老资讯公告表
-- =====================================================
DROP TABLE IF EXISTS `news`;
CREATE TABLE `news` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '资讯ID',
    `title`         VARCHAR(200)    NOT NULL                 COMMENT '标题',
    `content`       LONGTEXT        NOT NULL                 COMMENT '内容（支持富文本）',
    `summary`       VARCHAR(500)    DEFAULT NULL             COMMENT '摘要',
    `cover_image`   VARCHAR(255)    DEFAULT NULL             COMMENT '封面图片URL',
    `news_type`     VARCHAR(30)     NOT NULL DEFAULT 'GENERAL' COMMENT '类型：GENERAL-通用, HEALTH-健康, POLICY-政策, ACTIVITY-活动',
    `publisher_id`  BIGINT          NOT NULL                 COMMENT '发布者ID（管理员）',
    `view_count`    INT             NOT NULL DEFAULT 0       COMMENT '浏览次数',
    `status`        TINYINT         NOT NULL DEFAULT 1       COMMENT '状态：1-发布, 0-草稿',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_news_type` (`news_type`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_news_status_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='养老资讯公告表';

-- 插入几条示例资讯
INSERT INTO `news` (`title`, `content`, `summary`, `news_type`, `publisher_id`) VALUES
('夏季老年人防暑降温小贴士', '<p>夏季高温天气，老年人需特别注意防暑降温。以下是一些实用建议：</p><p>1. 避免在上午10点至下午4点外出</p><p>2. 多喝温开水，少量多次</p><p>3. 穿着宽松、透气的棉质衣物</p><p>4. 室内保持通风，温度控制在26-28℃</p><p>5. 若出现头晕、恶心等症状，立即就医</p>', '夏季高温来袭，为老年朋友准备的防暑降温实用指南。', 'HEALTH', 1),
('2024年养老金调整政策解读', '<p>根据最新政策，2024年退休人员基本养老金调整方案如下：</p><p>1. 定额调整：每人每月增加30元</p><p>2. 挂钩调整：与缴费年限和养老金水平挂钩</p><p>3. 倾斜调整：高龄退休人员适当提高调整水平</p><p>具体金额以当地社保部门公布为准。</p>', '2024年养老金调整最新政策解读，了解您的养老金变化。', 'POLICY', 1),
('社区老年活动中心开放通知', '<p>各位老年朋友，社区老年活动中心将于本周六正式开放！</p><p>开放时间：每天上午9:00-11:30，下午14:00-17:00</p><p>活动内容：太极拳、书法班、棋牌室、健康讲座</p><p>欢迎各位老年朋友前来参加！</p>', '社区老年活动中心即将开放，丰富老年人的文化生活。', 'ACTIVITY', 1);

-- =====================================================
-- 8. 系统日志表
-- =====================================================
DROP TABLE IF EXISTS `system_log`;
CREATE TABLE `system_log` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '日志ID',
    `user_id`       BIGINT          DEFAULT NULL             COMMENT '操作用户ID',
    `username`      VARCHAR(50)     DEFAULT NULL             COMMENT '操作用户名',
    `operation`     VARCHAR(100)    NOT NULL                 COMMENT '操作描述',
    `method`        VARCHAR(200)    DEFAULT NULL             COMMENT '请求方法',
    `params`        TEXT            DEFAULT NULL             COMMENT '请求参数',
    `ip`            VARCHAR(50)     DEFAULT NULL             COMMENT 'IP地址',
    `duration`      BIGINT          DEFAULT NULL             COMMENT '执行耗时（毫秒）',
    `trace_id`      VARCHAR(64)     DEFAULT NULL             COMMENT '请求追踪编号',
    `status_code`   INT             NOT NULL DEFAULT 200     COMMENT 'HTTP状态码',
    `success`       TINYINT         NOT NULL DEFAULT 1       COMMENT '是否成功：0-失败，1-成功',
    `error_message` VARCHAR(500)    DEFAULT NULL             COMMENT '安全错误摘要',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_trace_id` (`trace_id`),
    KEY `idx_status_time` (`status_code`, `create_time`),
    KEY `idx_log_time_user` (`create_time`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- =====================================================
-- 9. 多实例定时任务租约
-- =====================================================
DROP TABLE IF EXISTS `scheduler_lock`;
CREATE TABLE `scheduler_lock` (
    `lock_name`             VARCHAR(100) NOT NULL COMMENT '任务锁名称',
    `locked_until`          DATETIME(3)  NOT NULL COMMENT '最长租约截止时间',
    `lock_at_least_until`   DATETIME(3)  NOT NULL COMMENT '最短持有截止时间',
    `locked_at`             DATETIME(3)  NOT NULL COMMENT '最近获得锁时间',
    `locked_by`             VARCHAR(100) NOT NULL COMMENT '实例标识',
    PRIMARY KEY (`lock_name`),
    KEY `idx_scheduler_locked_until` (`locked_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='多实例定时任务租约';

-- =====================================================
-- 10. 多实例共享限流窗口
-- =====================================================
DROP TABLE IF EXISTS `rate_limit_bucket`;
CREATE TABLE `rate_limit_bucket` (
    `bucket_key`     CHAR(64)    NOT NULL COMMENT 'SHA-256匿名限流键',
    `window_id`      BIGINT      NOT NULL COMMENT '固定窗口编号',
    `request_count`  INT         NOT NULL DEFAULT 1 COMMENT '窗口内请求数',
    `expires_at`     DATETIME(3) NOT NULL COMMENT '窗口过期时间',
    PRIMARY KEY (`bucket_key`, `window_id`),
    KEY `idx_rate_limit_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='多实例共享限流窗口';

-- =====================================================
-- 11. 家属-老人绑定关系表
-- =====================================================
DROP TABLE IF EXISTS `family_binding`;
CREATE TABLE `family_binding` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '绑定ID',
    `family_user_id` BIGINT         NOT NULL                 COMMENT '家属用户ID',
    `elder_user_id`  BIGINT         NOT NULL                 COMMENT '老人用户ID',
    `relation`      VARCHAR(20)     DEFAULT '子女'           COMMENT '亲属关系：子女/配偶/其他',
    `status`        TINYINT         NOT NULL DEFAULT 2       COMMENT '状态：0-已解除,1-有效,2-待老人确认,3-已拒绝',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_family_elder` (`family_user_id`, `elder_user_id`),
    KEY `idx_elder_user_id` (`elder_user_id`),
    KEY `idx_family_status` (`family_user_id`, `status`)
    ,KEY `idx_elder_binding_status` (`elder_user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家属-老人绑定关系表';

-- =====================================================
-- 12. 站内通知表
-- =====================================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id`            BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '通知ID',
    `user_id`       BIGINT          NOT NULL                 COMMENT '接收用户ID',
    `type`          VARCHAR(20)     NOT NULL DEFAULT 'SYS'   COMMENT '通知类型：SOS-紧急求助, HEALTH-健康预警, REMINDER-提醒未确认, SYS-系统通知',
    `title`         VARCHAR(100)    NOT NULL                 COMMENT '通知标题',
    `content`       VARCHAR(500)    DEFAULT NULL             COMMENT '通知内容',
    `ref_id`        BIGINT          DEFAULT NULL             COMMENT '关联业务ID',
    `is_read`       TINYINT         NOT NULL DEFAULT 0       COMMENT '是否已读：0-未读, 1-已读',
    `create_time`   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_user_read` (`user_id`, `is_read`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内通知表';

DROP TABLE IF EXISTS `contact_message`;
CREATE TABLE `contact_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL,
    `name` VARCHAR(50) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `email` VARCHAR(120) DEFAULT NULL,
    `subject` VARCHAR(30) NOT NULL,
    `message` VARCHAR(1000) NOT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_contact_status_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户联系留言';

DROP TABLE IF EXISTS `account_deletion_request`;
CREATE TABLE `account_deletion_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `reason` VARCHAR(500) DEFAULT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    `requested_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `processed_by` BIGINT DEFAULT NULL,
    `processed_at` DATETIME DEFAULT NULL,
    `process_remark` VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_deletion_user_status` (`user_id`, `status`),
    KEY `idx_deletion_status_time` (`status`, `requested_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账户注销申请';

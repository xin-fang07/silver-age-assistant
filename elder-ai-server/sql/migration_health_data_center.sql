USE elder_ai_assistant;

ALTER TABLE health_record
    ADD COLUMN IF NOT EXISTS blood_oxygen INT DEFAULT NULL COMMENT '血氧饱和度(%)' AFTER heart_rate,
    ADD COLUMN IF NOT EXISTS steps INT DEFAULT NULL COMMENT '每日步数' AFTER blood_oxygen,
    ADD COLUMN IF NOT EXISTS update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER create_time,
    ADD COLUMN IF NOT EXISTS deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除,1-已删除' AFTER update_time;

CREATE TABLE IF NOT EXISTS llm_analysis_result (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    elder_info_id BIGINT DEFAULT NULL COMMENT '关联老人档案ID',
    analysis_text LONGTEXT NOT NULL COMMENT 'LLM分析完整输出文本',
    risk_level VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '风险等级：NORMAL-正常,LOW-偏低,HIGH-偏高,DANGER-高危',
    start_date DATE NOT NULL COMMENT '分析开始日期',
    end_date DATE NOT NULL COMMENT '分析结束日期',
    batch_no INT NOT NULL DEFAULT 1 COMMENT '生成批次号',
    pushed TINYINT NOT NULL DEFAULT 0 COMMENT '是否推送家属：0-未推送,1-已推送',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_elder_info_id (elder_info_id),
    KEY idx_risk_level (risk_level),
    KEY idx_create_time (create_time),
    KEY idx_batch_no (batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='LLM分析结果记录表';

CREATE TABLE IF NOT EXISTS health_report (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    elder_info_id BIGINT DEFAULT NULL COMMENT '关联老人档案ID',
    report_no VARCHAR(64) NOT NULL COMMENT '报告编号',
    report_type VARCHAR(20) NOT NULL DEFAULT 'MONTHLY' COMMENT '报告类型：WEEKLY-周报告,MONTHLY-月报告',
    start_date DATE NOT NULL COMMENT '报告周期开始日期',
    end_date DATE NOT NULL COMMENT '报告周期结束日期',
    report_content LONGTEXT DEFAULT NULL COMMENT '报告内容（HTML格式）',
    pdf_url VARCHAR(500) DEFAULT NULL COMMENT 'PDF文件URL',
    pushed TINYINT NOT NULL DEFAULT 0 COMMENT '是否推送家属：0-未推送,1-已推送',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_report_no (report_no),
    KEY idx_user_id (user_id),
    KEY idx_elder_info_id (elder_info_id),
    KEY idx_report_type (report_type),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康报告表';
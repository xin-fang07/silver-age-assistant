-- 健康数据文件导入与设备接入扩展（MySQL 8.x）
-- 执行前请先选择项目数据库：USE elder_ai_assistant;

ALTER TABLE `health_record`
    ADD COLUMN `measured_at` DATETIME NULL COMMENT '设备实际测量时间' AFTER `record_date`,
    ADD COLUMN `source_type` VARCHAR(20) NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL/FILE_IMPORT/DEVICE' AFTER `measured_at`,
    ADD COLUMN `source_device_id` VARCHAR(64) NULL COMMENT '来源设备编号' AFTER `source_type`,
    ADD COLUMN `external_record_id` VARCHAR(100) NULL COMMENT '厂商侧记录编号' AFTER `source_device_id`,
    ADD COLUMN `import_batch_id` VARCHAR(32) NULL COMMENT '文件导入批次号' AFTER `external_record_id`,
    ADD UNIQUE KEY `uk_health_device_record` (`user_id`, `source_device_id`, `external_record_id`),
    ADD KEY `idx_health_import_batch` (`import_batch_id`);


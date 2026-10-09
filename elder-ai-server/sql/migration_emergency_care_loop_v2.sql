ALTER TABLE `emergency_help`
    ADD COLUMN `acknowledged_role` VARCHAR(20) DEFAULT NULL COMMENT '接单身份：ADMIN/FAMILY' AFTER `acknowledged_by`,
    ADD COLUMN `acknowledged_name` VARCHAR(50) DEFAULT NULL COMMENT '接单人显示名称' AFTER `acknowledged_role`;

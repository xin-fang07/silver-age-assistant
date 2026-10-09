ALTER TABLE `emergency_help`
    ADD COLUMN `location_accuracy` DECIMAL(10,2) DEFAULT NULL
    COMMENT '定位误差半径（米，95%置信）' AFTER `longitude`;

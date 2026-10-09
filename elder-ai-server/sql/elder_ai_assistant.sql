/*
 Navicat Premium Data Transfer

 Source Server         : bysj
 Source Server Type    : MySQL
 Source Server Version : 80012 (8.0.12)
 Source Host           : localhost:3306
 Source Schema         : elder_ai_assistant

 Target Server Type    : MySQL
 Target Server Version : 80012 (8.0.12)
 File Encoding         : 65001

 Date: 16/07/2026 18:36:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_record
-- ----------------------------
DROP TABLE IF EXISTS `chat_record`;
CREATE TABLE `chat_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户提问内容',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'AI回答内容',
  `is_fallback` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否为降级回答：0-否, 1-是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '聊天记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_record
-- ----------------------------
INSERT INTO `chat_record` VALUES (1, 1, '今天天气怎么样？', '抱歉，我现在看不到您所在地方的具体天气。不过您可以试试这些简单方法查天气：\n\n1. **看窗外最直接**：先看看外面是晴天、阴天还是下雨，出门前心里有个底。\n2. **问手机或小度音箱**：对着手机说“今天天气”，或者问问家里的智能音箱，马上就能知道温度、要不要带伞。\n3. **看电视新闻后播报**：早上地方电视台新闻结束后，通常会播本地天气，字大又清楚。\n4. **出门小提醒**：\n   - 如果感觉凉，带件薄外套，热了方便脱。\n   - 阴天最好随身带把折叠伞，免得突然下雨。\n   - 太阳大就戴帽子或太阳镜，慢慢走，多喝水。\n\n您方便告诉我您在哪个城市吗？下次我可以试着帮您查更准确的天气。', 0, '2026-07-14 15:28:00');
INSERT INTO `chat_record` VALUES (15, 2, '有什么适合老人的运动？', '您好！我给您推荐几种适合老人的运动：\n\n1. 散步。每天走20-30分钟。最好选平路，不费膝盖。\n\n2. 打太极拳。动作缓慢，能活动全身。还能练平衡，防摔倒。\n\n3. 做手指操。简单活动手指关节。看电视时就能做。\n\n4. 跟着视频做椅子操。坐在椅子上动动胳膊腿。安全又舒服。\n\n记住：运动前先热身5分钟。感觉累了就休息。最好有家人陪着练。', 0, '2026-07-15 23:57:22');
INSERT INTO `chat_record` VALUES (16, 1, '你好', '您好！很高兴见到您。  \n有什么需要帮忙的吗？  \n关于手机、电脑、电视等智能设备，  \n或者生活上的小问题，  \n都可以问我。  \n我会用简单的话告诉您怎么做。', 0, '2026-07-16 15:39:28');
INSERT INTO `chat_record` VALUES (17, 1, '今天有什么新闻', '您好！我来帮您看看今天的新闻。\n\n1. **天气方面**  \n今天大部分地区天气不错，适合出门散步。记得带伞，以防万一。\n\n2. **健康提醒**  \n秋天来了，早晚温差大。出门多穿件外套，别着凉。\n\n3. **生活小贴士**  \n多吃水果蔬菜，比如梨和苹果。对身体好，还能润肺。\n\n4. **好消息**  \n听说公园里新开了老年活动中心，可以去下棋、聊天。\n\n您想了解哪方面新闻？我可以再详细告诉您。慢慢来，不着急！', 0, '2026-07-16 15:39:45');
INSERT INTO `chat_record` VALUES (18, 1, '今天的天气', '您好！我来告诉您北京的天气情况：\n\n1. 现在是阴天，没有太阳直晒。\n2. 温度31.9度，感觉有36.4度，闷热。\n3. 湿度54%，有点潮湿，风很小。\n\n建议您：\n- 出门记得带把伞，防下雨。\n- 多喝水，防止中暑。\n- 穿浅色、透气的衣服。\n- 最好在室内活动，凉快些。\n\n天气闷热，您要注意休息哦！', 0, '2026-07-16 15:42:21');

-- ----------------------------
-- Table structure for elder_info
-- ----------------------------
DROP TABLE IF EXISTS `elder_info`;
CREATE TABLE `elder_info`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '信息ID',
  `user_id` bigint(20) NOT NULL COMMENT '关联用户ID',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `gender` tinyint(4) NULL DEFAULT NULL COMMENT '性别：0-女, 1-男',
  `age` int(11) NULL DEFAULT NULL COMMENT '年龄',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '住址',
  `emergency_contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '紧急联系人姓名',
  `emergency_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '紧急联系人电话',
  `medical_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '既往病史',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '老人信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of elder_info
-- ----------------------------
INSERT INTO `elder_info` VALUES (1, 2, NULL, NULL, NULL, NULL, '小张', '17377791921', NULL, '2026-07-14 20:19:58', '2026-07-14 20:25:00');
INSERT INTO `elder_info` VALUES (2, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-15 18:43:37', '2026-07-15 18:43:37');
INSERT INTO `elder_info` VALUES (3, 3, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-16 16:21:59', '2026-07-16 16:21:59');

-- ----------------------------
-- Table structure for emergency_help
-- ----------------------------
DROP TABLE IF EXISTS `emergency_help`;
CREATE TABLE `emergency_help`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '求助ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '紧急联系人姓名',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '紧急联系人电话',
  `contact_email` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '紧急联系人邮箱',
  `help_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '求助备注',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0-已提交,1-已接单,2-处理中,3-已完成,4-已取消,5-已升级',
  `notification_status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '0-未配置,2-成功,3-失败',
  `notification_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '通知结果说明',
  `notified_at` datetime NULL DEFAULT NULL COMMENT '最近成功通知时间',
  `acknowledged_by` bigint(20) NULL DEFAULT NULL COMMENT '接单管理员ID',
  `acknowledged_at` datetime NULL DEFAULT NULL COMMENT '接单时间',
  `processing_at` datetime NULL DEFAULT NULL COMMENT '开始处理时间',
  `completed_at` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `escalated_at` datetime NULL DEFAULT NULL COMMENT '最近升级时间',
  `escalation_level` tinyint(4) NOT NULL DEFAULT 0 COMMENT '升级级别',
  `latitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '求助纬度',
  `longitude` decimal(10, 7) NULL DEFAULT NULL COMMENT '求助经度',
  `location_text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '位置描述',
  `handle_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '求助时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '紧急求助表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of emergency_help
-- ----------------------------
INSERT INTO `emergency_help` VALUES (1, 1, '小方', '17377791921', NULL, NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-07-14 15:30:35', '2026-07-14 15:30:35');
INSERT INTO `emergency_help` VALUES (2, 2, '小张', '17377791921', NULL, '我遇到了紧急情况，请尽快联系我！', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-07-14 20:20:43', '2026-07-14 20:20:43');
INSERT INTO `emergency_help` VALUES (3, 2, '小张', '17377791921', NULL, '我遇到了紧急情况，请尽快联系我！', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-07-14 20:21:18', '2026-07-14 20:21:18');
INSERT INTO `emergency_help` VALUES (4, 2, '小张', '17377791921', NULL, '我遇到了紧急情况，请尽快联系我！', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-07-14 20:24:50', '2026-07-14 20:24:50');
INSERT INTO `emergency_help` VALUES (5, 2, '小张', '17377791921', NULL, '我遇到了紧急情况，请尽快联系我！', 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, '2026-07-14 20:25:03', '2026-07-14 20:25:03');

-- ----------------------------
-- Table structure for emergency_notification
-- ----------------------------
DROP TABLE IF EXISTS `emergency_notification`;
CREATE TABLE `emergency_notification`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知记录ID',
  `help_id` bigint(20) NOT NULL COMMENT '求助ID',
  `channel` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知渠道',
  `recipient` varchar(160) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收对象',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SENT/FAILED/SKIPPED',
  `attempt_no` int(11) NOT NULL DEFAULT 1 COMMENT '尝试次数',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '失败说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `sent_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_help_id`(`help_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '紧急求助通知记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of emergency_notification
-- ----------------------------

-- ----------------------------
-- Table structure for health_record
-- ----------------------------
DROP TABLE IF EXISTS `health_record`;
CREATE TABLE `health_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `blood_pressure_high` int(11) NULL DEFAULT NULL COMMENT '收缩压（高压）',
  `blood_pressure_low` int(11) NULL DEFAULT NULL COMMENT '舒张压（低压）',
  `blood_sugar` decimal(5, 2) NULL DEFAULT NULL COMMENT '血糖值（mmol/L）',
  `heart_rate` int(11) NULL DEFAULT NULL COMMENT '心率（次/分）',
  `weight` decimal(5, 2) NULL DEFAULT NULL COMMENT '体重（kg）',
  `record_date` date NOT NULL COMMENT '记录日期',
  `measured_at` datetime NULL DEFAULT NULL COMMENT '设备实际测量时间',
  `source_type` varchar(20) NOT NULL DEFAULT 'MANUAL' COMMENT 'MANUAL/FILE_IMPORT/DEVICE',
  `source_device_id` varchar(64) NULL DEFAULT NULL COMMENT '来源设备编号',
  `external_record_id` varchar(100) NULL DEFAULT NULL COMMENT '厂商侧记录编号',
  `import_batch_id` varchar(32) NULL DEFAULT NULL COMMENT '文件导入批次号',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_record`(`user_id` ASC, `record_date` ASC) USING BTREE,
  UNIQUE INDEX `uk_health_device_record`(`user_id` ASC, `source_device_id` ASC, `external_record_id` ASC) USING BTREE,
  INDEX `idx_health_import_batch`(`import_batch_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '健康数据记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of health_record
-- ----------------------------
INSERT INTO `health_record` (`id`, `user_id`, `blood_pressure_high`, `blood_pressure_low`, `blood_sugar`, `heart_rate`, `weight`, `record_date`, `remark`, `create_time`)
VALUES (1, 3, NULL, NULL, NULL, NULL, 40.00, '2026-07-17', '', '2026-07-16 16:25:16');

-- ----------------------------
-- Table structure for health_warning
-- ----------------------------
DROP TABLE IF EXISTS `health_warning`;
CREATE TABLE `health_warning`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '关联用户ID',
  `record_id` bigint(20) NULL DEFAULT NULL COMMENT '关联的健康记录ID',
  `warning_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预警类型：BLOOD_PRESSURE-血压, BLOOD_SUGAR-血糖, HEART_RATE-心率',
  `warning_level` tinyint(4) NOT NULL DEFAULT 1 COMMENT '预警等级：1-轻度, 2-中度, 3-重度',
  `warning_content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预警详情描述',
  `is_read` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读, 1-已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_warning_type`(`warning_type` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '健康预警记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of health_warning
-- ----------------------------

-- ----------------------------
-- Table structure for news
-- ----------------------------
DROP TABLE IF EXISTS `news`;
CREATE TABLE `news`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '资讯ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容（支持富文本）',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '摘要',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `news_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'GENERAL' COMMENT '类型：GENERAL-通用, HEALTH-健康, POLICY-政策, ACTIVITY-活动',
  `publisher_id` bigint(20) NOT NULL COMMENT '发布者ID（管理员）',
  `view_count` int(11) NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：1-发布, 0-草稿',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_news_type`(`news_type` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '养老资讯公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of news
-- ----------------------------
INSERT INTO `news` VALUES (1, '夏季老年人防暑降温小贴士', '<p>夏季高温天气，老年人需特别注意防暑降温。以下是一些实用建议：</p><p>1. 避免在上午10点至下午4点外出</p><p>2. 多喝温开水，少量多次</p><p>3. 穿着宽松、透气的棉质衣物</p><p>4. 室内保持通风，温度控制在26-28℃</p><p>5. 若出现头晕、恶心等症状，立即就医</p>', '夏季高温来袭，为老年朋友准备的防暑降温实用指南。', NULL, 'HEALTH', 1, 0, 1, '2026-07-14 15:09:28', '2026-07-14 15:09:28');
INSERT INTO `news` VALUES (2, '2024年养老金调整政策解读', '<p>根据最新政策，2024年退休人员基本养老金调整方案如下：</p><p>1. 定额调整：每人每月增加30元</p><p>2. 挂钩调整：与缴费年限和养老金水平挂钩</p><p>3. 倾斜调整：高龄退休人员适当提高调整水平</p><p>具体金额以当地社保部门公布为准。</p>', '2024年养老金调整最新政策解读，了解您的养老金变化。', NULL, 'POLICY', 1, 0, 1, '2026-07-14 15:09:28', '2026-07-14 15:09:28');
INSERT INTO `news` VALUES (3, '社区老年活动中心开放通知', '<p>各位老年朋友，社区老年活动中心将于本周六正式开放！</p><p>开放时间：每天上午9:00-11:30，下午14:00-17:00</p><p>活动内容：太极拳、书法班、棋牌室、健康讲座</p><p>欢迎各位老年朋友前来参加！</p>', '社区老年活动中心即将开放，丰富老年人的文化生活。', NULL, 'ACTIVITY', 1, 1, 1, '2026-07-14 15:09:28', '2026-07-14 15:09:28');

-- ----------------------------
-- Table structure for reminder
-- ----------------------------
DROP TABLE IF EXISTS `reminder`;
CREATE TABLE `reminder`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '提醒ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提醒标题',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提醒内容描述',
  `remind_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提醒类型：MEDICINE-吃药, EXERCISE-运动, CHECKUP-体检, PAYMENT-缴费, OTHER-其他',
  `remind_time` datetime NOT NULL COMMENT '提醒时间',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0-待提醒, 1-已完成, 2-已过期',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_remind_time`(`remind_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '提醒事项表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reminder
-- ----------------------------
INSERT INTO `reminder` VALUES (3, 2, '运动', '', 'EXERCISE', '2026-07-16 11:17:00', 1, '2026-07-16 11:16:07', '2026-07-16 11:16:08');
INSERT INTO `reminder` VALUES (4, 2, '吃药', '', 'MEDICINE', '2026-07-16 11:22:00', 2, '2026-07-16 11:16:33', '2026-07-16 11:22:00');

-- ----------------------------
-- Table structure for system_log
-- ----------------------------
DROP TABLE IF EXISTS `system_log`;
CREATE TABLE `system_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作用户名',
  `operation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作描述',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方法',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `duration` bigint(20) NULL DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1092 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_log
-- ----------------------------
INSERT INTO `system_log` VALUES (1, 4, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 135, '2026-07-14 15:25:57');
INSERT INTO `system_log` VALUES (2, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=admin, password=******)]', '0:0:0:0:0:0:0:1', 136, '2026-07-14 15:26:31');
INSERT INTO `system_log` VALUES (3, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=admin, password=******)]', '0:0:0:0:0:0:0:1', 107, '2026-07-14 15:26:54');
INSERT INTO `system_log` VALUES (4, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-14 15:26:54');
INSERT INTO `system_log` VALUES (5, 1, 'admin', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=今天天气怎么样？)]', '0:0:0:0:0:0:0:1', 6382, '2026-07-14 15:28:06');
INSERT INTO `system_log` VALUES (6, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-14 15:28:12');
INSERT INTO `system_log` VALUES (7, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 15:28:20');
INSERT INTO `system_log` VALUES (8, 1, 'admin', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-14 15:28:23');
INSERT INTO `system_log` VALUES (9, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 20, '2026-07-14 15:28:31');
INSERT INTO `system_log` VALUES (10, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-14 15:28:35');
INSERT INTO `system_log` VALUES (11, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 15:28:39');
INSERT INTO `system_log` VALUES (12, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 15:29:51');
INSERT INTO `system_log` VALUES (13, 1, 'admin', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 15:29:52');
INSERT INTO `system_log` VALUES (14, 1, 'admin', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 15:29:52');
INSERT INTO `system_log` VALUES (15, 1, 'admin', 'POST /api/emergency/help', 'com.example.elderai.controller.EmergencyController.createHelp', '[EmergencyHelpDTO(contactName=小方, contactPhone=17377791921, helpContent=null)]', '0:0:0:0:0:0:0:1', 83, '2026-07-14 15:30:35');
INSERT INTO `system_log` VALUES (16, 1, 'admin', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 15:30:35');
INSERT INTO `system_log` VALUES (17, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 101, '2026-07-14 15:31:20');
INSERT INTO `system_log` VALUES (18, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-14 15:31:20');
INSERT INTO `system_log` VALUES (19, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-14 15:31:27');
INSERT INTO `system_log` VALUES (20, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 15:31:28');
INSERT INTO `system_log` VALUES (21, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 15:31:30');
INSERT INTO `system_log` VALUES (22, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 64, '2026-07-14 15:31:57');
INSERT INTO `system_log` VALUES (23, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 70, '2026-07-14 15:32:02');
INSERT INTO `system_log` VALUES (24, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 70, '2026-07-14 15:32:02');
INSERT INTO `system_log` VALUES (25, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 15:32:08');
INSERT INTO `system_log` VALUES (26, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 21, '2026-07-14 15:32:11');
INSERT INTO `system_log` VALUES (27, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 15:32:15');
INSERT INTO `system_log` VALUES (28, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 10, '2026-07-14 15:32:15');
INSERT INTO `system_log` VALUES (29, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-14 15:32:23');
INSERT INTO `system_log` VALUES (30, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 15:32:24');
INSERT INTO `system_log` VALUES (31, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 9, '2026-07-14 15:32:24');
INSERT INTO `system_log` VALUES (32, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=admin, password=******)]', '0:0:0:0:0:0:0:1', 121, '2026-07-14 15:32:49');
INSERT INTO `system_log` VALUES (33, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 21, '2026-07-14 15:32:50');
INSERT INTO `system_log` VALUES (34, 1, 'admin', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-14 15:32:56');
INSERT INTO `system_log` VALUES (35, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-14 15:32:57');
INSERT INTO `system_log` VALUES (36, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-14 15:32:57');
INSERT INTO `system_log` VALUES (37, 1, 'admin', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-14 15:32:58');
INSERT INTO `system_log` VALUES (38, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 15:32:59');
INSERT INTO `system_log` VALUES (39, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 15:33:01');
INSERT INTO `system_log` VALUES (40, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 15:33:01');
INSERT INTO `system_log` VALUES (41, 1, 'admin', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-14 15:33:01');
INSERT INTO `system_log` VALUES (42, 1, 'admin', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 15:33:02');
INSERT INTO `system_log` VALUES (43, 1, 'admin', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 15:33:02');
INSERT INTO `system_log` VALUES (44, 1, 'admin', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 15:33:07');
INSERT INTO `system_log` VALUES (45, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-14 15:34:56');
INSERT INTO `system_log` VALUES (46, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 15:35:14');
INSERT INTO `system_log` VALUES (47, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-14 15:35:27');
INSERT INTO `system_log` VALUES (48, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 15:35:27');
INSERT INTO `system_log` VALUES (49, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-14 15:35:28');
INSERT INTO `system_log` VALUES (50, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-14 15:35:31');
INSERT INTO `system_log` VALUES (51, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 82, '2026-07-14 15:37:02');
INSERT INTO `system_log` VALUES (52, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=admin, password=******)]', '0:0:0:0:0:0:0:1', 99, '2026-07-14 15:37:24');
INSERT INTO `system_log` VALUES (53, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 24, '2026-07-14 15:37:25');
INSERT INTO `system_log` VALUES (54, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-14 15:37:31');
INSERT INTO `system_log` VALUES (55, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 15:37:32');
INSERT INTO `system_log` VALUES (56, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 15:37:33');
INSERT INTO `system_log` VALUES (57, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-14 15:37:35');
INSERT INTO `system_log` VALUES (58, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 15:37:39');
INSERT INTO `system_log` VALUES (59, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 17, '2026-07-14 15:41:33');
INSERT INTO `system_log` VALUES (60, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 15:41:39');
INSERT INTO `system_log` VALUES (61, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 15:41:40');
INSERT INTO `system_log` VALUES (62, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 15:41:48');
INSERT INTO `system_log` VALUES (63, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 83, '2026-07-14 15:42:17');
INSERT INTO `system_log` VALUES (64, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-14 15:42:19');
INSERT INTO `system_log` VALUES (65, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-14 16:01:00');
INSERT INTO `system_log` VALUES (66, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-14 16:01:02');
INSERT INTO `system_log` VALUES (67, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 16:01:02');
INSERT INTO `system_log` VALUES (68, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 16:01:02');
INSERT INTO `system_log` VALUES (69, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 16:01:14');
INSERT INTO `system_log` VALUES (70, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 16:05:02');
INSERT INTO `system_log` VALUES (71, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-14 16:05:05');
INSERT INTO `system_log` VALUES (72, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 96, '2026-07-14 16:05:31');
INSERT INTO `system_log` VALUES (73, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 16:05:35');
INSERT INTO `system_log` VALUES (74, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 13, '2026-07-14 16:09:48');
INSERT INTO `system_log` VALUES (75, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-14 16:09:51');
INSERT INTO `system_log` VALUES (76, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 12, '2026-07-14 16:12:45');
INSERT INTO `system_log` VALUES (77, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=admin, password=******)]', '0:0:0:0:0:0:0:1', 137, '2026-07-14 16:13:30');
INSERT INTO `system_log` VALUES (78, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 10, '2026-07-14 16:13:31');
INSERT INTO `system_log` VALUES (79, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-14 16:13:46');
INSERT INTO `system_log` VALUES (80, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 16:13:48');
INSERT INTO `system_log` VALUES (81, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 16:13:51');
INSERT INTO `system_log` VALUES (82, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-14 16:13:52');
INSERT INTO `system_log` VALUES (83, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 16:14:13');
INSERT INTO `system_log` VALUES (84, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 16:14:30');
INSERT INTO `system_log` VALUES (85, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 16:14:31');
INSERT INTO `system_log` VALUES (86, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 24, '2026-07-14 16:14:31');
INSERT INTO `system_log` VALUES (87, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 16:14:32');
INSERT INTO `system_log` VALUES (88, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 16:14:32');
INSERT INTO `system_log` VALUES (89, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-14 16:15:46');
INSERT INTO `system_log` VALUES (90, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 20, '2026-07-14 16:26:51');
INSERT INTO `system_log` VALUES (91, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-14 16:27:22');
INSERT INTO `system_log` VALUES (92, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 33, '2026-07-14 16:32:10');
INSERT INTO `system_log` VALUES (93, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 14, '2026-07-14 16:32:11');
INSERT INTO `system_log` VALUES (94, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 30, '2026-07-14 16:36:24');
INSERT INTO `system_log` VALUES (95, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 119, '2026-07-14 16:38:01');
INSERT INTO `system_log` VALUES (96, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 16:38:08');
INSERT INTO `system_log` VALUES (97, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 16:38:18');
INSERT INTO `system_log` VALUES (98, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 16:38:42');
INSERT INTO `system_log` VALUES (99, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 62, '2026-07-14 16:38:43');
INSERT INTO `system_log` VALUES (100, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 63, '2026-07-14 16:38:43');
INSERT INTO `system_log` VALUES (101, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 16:38:44');
INSERT INTO `system_log` VALUES (102, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 16:38:50');
INSERT INTO `system_log` VALUES (103, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 16:38:50');
INSERT INTO `system_log` VALUES (104, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 16:38:52');
INSERT INTO `system_log` VALUES (105, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 16:39:02');
INSERT INTO `system_log` VALUES (106, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-14 16:42:03');
INSERT INTO `system_log` VALUES (107, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 24, '2026-07-14 16:42:25');
INSERT INTO `system_log` VALUES (108, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 16:42:34');
INSERT INTO `system_log` VALUES (109, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 16:42:34');
INSERT INTO `system_log` VALUES (110, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 14, '2026-07-14 16:42:35');
INSERT INTO `system_log` VALUES (111, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 16:42:43');
INSERT INTO `system_log` VALUES (112, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 16:42:43');
INSERT INTO `system_log` VALUES (113, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 16:42:44');
INSERT INTO `system_log` VALUES (114, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-14 16:42:48');
INSERT INTO `system_log` VALUES (115, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 16:42:49');
INSERT INTO `system_log` VALUES (116, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 16:42:51');
INSERT INTO `system_log` VALUES (117, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 16:42:51');
INSERT INTO `system_log` VALUES (118, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 16:42:54');
INSERT INTO `system_log` VALUES (119, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 8, '2026-07-14 16:42:54');
INSERT INTO `system_log` VALUES (120, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 16:42:57');
INSERT INTO `system_log` VALUES (121, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 16:42:58');
INSERT INTO `system_log` VALUES (122, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 16:42:58');
INSERT INTO `system_log` VALUES (123, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 16:43:04');
INSERT INTO `system_log` VALUES (124, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 16:43:11');
INSERT INTO `system_log` VALUES (125, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 16:43:12');
INSERT INTO `system_log` VALUES (126, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 16:50:57');
INSERT INTO `system_log` VALUES (127, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-14 16:51:08');
INSERT INTO `system_log` VALUES (128, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 16, '2026-07-14 16:57:36');
INSERT INTO `system_log` VALUES (129, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 16:57:55');
INSERT INTO `system_log` VALUES (130, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 16:58:19');
INSERT INTO `system_log` VALUES (131, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 16:58:38');
INSERT INTO `system_log` VALUES (132, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 25, '2026-07-14 17:00:23');
INSERT INTO `system_log` VALUES (133, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 92, '2026-07-14 17:00:50');
INSERT INTO `system_log` VALUES (134, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 17:01:05');
INSERT INTO `system_log` VALUES (135, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 17:01:13');
INSERT INTO `system_log` VALUES (136, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:01:17');
INSERT INTO `system_log` VALUES (137, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:01:24');
INSERT INTO `system_log` VALUES (138, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:01:24');
INSERT INTO `system_log` VALUES (139, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:01:26');
INSERT INTO `system_log` VALUES (140, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:01:34');
INSERT INTO `system_log` VALUES (141, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:01:34');
INSERT INTO `system_log` VALUES (142, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 17:01:49');
INSERT INTO `system_log` VALUES (143, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:01:54');
INSERT INTO `system_log` VALUES (144, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:01:57');
INSERT INTO `system_log` VALUES (145, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:01:58');
INSERT INTO `system_log` VALUES (146, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:01:59');
INSERT INTO `system_log` VALUES (147, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 17:02:00');
INSERT INTO `system_log` VALUES (148, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:02:01');
INSERT INTO `system_log` VALUES (149, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:02:02');
INSERT INTO `system_log` VALUES (150, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 17:02:02');
INSERT INTO `system_log` VALUES (151, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 17:02:04');
INSERT INTO `system_log` VALUES (152, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 17:02:05');
INSERT INTO `system_log` VALUES (153, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:02:05');
INSERT INTO `system_log` VALUES (154, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:02:22');
INSERT INTO `system_log` VALUES (155, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:02:23');
INSERT INTO `system_log` VALUES (156, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:02:23');
INSERT INTO `system_log` VALUES (157, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:02:28');
INSERT INTO `system_log` VALUES (158, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 93, '2026-07-14 17:04:31');
INSERT INTO `system_log` VALUES (159, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 58, '2026-07-14 17:05:21');
INSERT INTO `system_log` VALUES (160, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 56, '2026-07-14 17:06:52');
INSERT INTO `system_log` VALUES (161, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 22, '2026-07-14 17:07:08');
INSERT INTO `system_log` VALUES (162, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 12, '2026-07-14 17:09:34');
INSERT INTO `system_log` VALUES (163, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 17:12:38');
INSERT INTO `system_log` VALUES (164, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:12:47');
INSERT INTO `system_log` VALUES (165, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 17:12:54');
INSERT INTO `system_log` VALUES (166, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:12:57');
INSERT INTO `system_log` VALUES (167, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:12:57');
INSERT INTO `system_log` VALUES (168, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-14 17:13:07');
INSERT INTO `system_log` VALUES (169, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:13:11');
INSERT INTO `system_log` VALUES (170, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:13:11');
INSERT INTO `system_log` VALUES (171, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:13:14');
INSERT INTO `system_log` VALUES (172, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:13:16');
INSERT INTO `system_log` VALUES (173, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:13:16');
INSERT INTO `system_log` VALUES (174, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:13:25');
INSERT INTO `system_log` VALUES (175, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 17:13:27');
INSERT INTO `system_log` VALUES (176, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 17:13:27');
INSERT INTO `system_log` VALUES (177, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:13:31');
INSERT INTO `system_log` VALUES (178, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:13:36');
INSERT INTO `system_log` VALUES (179, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:13:36');
INSERT INTO `system_log` VALUES (180, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:13:42');
INSERT INTO `system_log` VALUES (181, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:13:44');
INSERT INTO `system_log` VALUES (182, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:13:49');
INSERT INTO `system_log` VALUES (183, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:13:54');
INSERT INTO `system_log` VALUES (184, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:14:04');
INSERT INTO `system_log` VALUES (185, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:14:04');
INSERT INTO `system_log` VALUES (186, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:14:08');
INSERT INTO `system_log` VALUES (187, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:14:15');
INSERT INTO `system_log` VALUES (188, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:14:15');
INSERT INTO `system_log` VALUES (189, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:15:48');
INSERT INTO `system_log` VALUES (190, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:16:09');
INSERT INTO `system_log` VALUES (191, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:16:26');
INSERT INTO `system_log` VALUES (192, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:16:35');
INSERT INTO `system_log` VALUES (193, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:16:35');
INSERT INTO `system_log` VALUES (194, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-14 17:18:07');
INSERT INTO `system_log` VALUES (195, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 17:24:42');
INSERT INTO `system_log` VALUES (196, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 17:24:55');
INSERT INTO `system_log` VALUES (197, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:24:59');
INSERT INTO `system_log` VALUES (198, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:25:02');
INSERT INTO `system_log` VALUES (199, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:25:04');
INSERT INTO `system_log` VALUES (200, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:25:04');
INSERT INTO `system_log` VALUES (201, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:25:05');
INSERT INTO `system_log` VALUES (202, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:25:07');
INSERT INTO `system_log` VALUES (203, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:25:12');
INSERT INTO `system_log` VALUES (204, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 17:25:19');
INSERT INTO `system_log` VALUES (205, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:25:21');
INSERT INTO `system_log` VALUES (206, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:25:21');
INSERT INTO `system_log` VALUES (207, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:25:26');
INSERT INTO `system_log` VALUES (208, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:25:28');
INSERT INTO `system_log` VALUES (209, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:25:29');
INSERT INTO `system_log` VALUES (210, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:25:29');
INSERT INTO `system_log` VALUES (211, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:25:47');
INSERT INTO `system_log` VALUES (212, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:25:51');
INSERT INTO `system_log` VALUES (213, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 13, '2026-07-14 17:25:51');
INSERT INTO `system_log` VALUES (214, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:25:53');
INSERT INTO `system_log` VALUES (215, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:25:54');
INSERT INTO `system_log` VALUES (216, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:25:55');
INSERT INTO `system_log` VALUES (217, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:25:55');
INSERT INTO `system_log` VALUES (218, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:26:11');
INSERT INTO `system_log` VALUES (219, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:26:12');
INSERT INTO `system_log` VALUES (220, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:26:12');
INSERT INTO `system_log` VALUES (221, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:26:20');
INSERT INTO `system_log` VALUES (222, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:26:20');
INSERT INTO `system_log` VALUES (223, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 17:26:21');
INSERT INTO `system_log` VALUES (224, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 17:26:21');
INSERT INTO `system_log` VALUES (225, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 17:26:22');
INSERT INTO `system_log` VALUES (226, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 17:26:22');
INSERT INTO `system_log` VALUES (227, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 17:26:26');
INSERT INTO `system_log` VALUES (228, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:26:27');
INSERT INTO `system_log` VALUES (229, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:26:32');
INSERT INTO `system_log` VALUES (230, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:26:32');
INSERT INTO `system_log` VALUES (231, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:29:30');
INSERT INTO `system_log` VALUES (232, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-14 17:29:30');
INSERT INTO `system_log` VALUES (233, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:29:46');
INSERT INTO `system_log` VALUES (234, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:29:46');
INSERT INTO `system_log` VALUES (235, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:29:53');
INSERT INTO `system_log` VALUES (236, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 17:29:53');
INSERT INTO `system_log` VALUES (237, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:30:01');
INSERT INTO `system_log` VALUES (238, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:30:01');
INSERT INTO `system_log` VALUES (239, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:30:38');
INSERT INTO `system_log` VALUES (240, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:30:38');
INSERT INTO `system_log` VALUES (241, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 29, '2026-07-14 17:31:39');
INSERT INTO `system_log` VALUES (242, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:32:07');
INSERT INTO `system_log` VALUES (243, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 17:32:21');
INSERT INTO `system_log` VALUES (244, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:32:30');
INSERT INTO `system_log` VALUES (245, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:32:34');
INSERT INTO `system_log` VALUES (246, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:32:36');
INSERT INTO `system_log` VALUES (247, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:32:38');
INSERT INTO `system_log` VALUES (248, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:32:38');
INSERT INTO `system_log` VALUES (249, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:32:39');
INSERT INTO `system_log` VALUES (250, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:32:43');
INSERT INTO `system_log` VALUES (251, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:32:45');
INSERT INTO `system_log` VALUES (252, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:32:45');
INSERT INTO `system_log` VALUES (253, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:32:52');
INSERT INTO `system_log` VALUES (254, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:32:54');
INSERT INTO `system_log` VALUES (255, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:32:54');
INSERT INTO `system_log` VALUES (256, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:32:57');
INSERT INTO `system_log` VALUES (257, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:32:59');
INSERT INTO `system_log` VALUES (258, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 17:33:01');
INSERT INTO `system_log` VALUES (259, 2, 'test', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=你好)]', '0:0:0:0:0:0:0:1', 5669, '2026-07-14 17:34:19');
INSERT INTO `system_log` VALUES (260, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 17:35:15');
INSERT INTO `system_log` VALUES (261, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:35:18');
INSERT INTO `system_log` VALUES (262, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:35:18');
INSERT INTO `system_log` VALUES (263, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:35:20');
INSERT INTO `system_log` VALUES (264, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:40:20');
INSERT INTO `system_log` VALUES (265, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:41:41');
INSERT INTO `system_log` VALUES (266, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:42:11');
INSERT INTO `system_log` VALUES (267, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:42:12');
INSERT INTO `system_log` VALUES (268, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:42:12');
INSERT INTO `system_log` VALUES (269, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 17:42:13');
INSERT INTO `system_log` VALUES (270, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:42:15');
INSERT INTO `system_log` VALUES (271, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:42:16');
INSERT INTO `system_log` VALUES (272, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 17:42:16');
INSERT INTO `system_log` VALUES (273, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-14 17:42:20');
INSERT INTO `system_log` VALUES (274, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-14 17:42:20');
INSERT INTO `system_log` VALUES (275, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 17:42:22');
INSERT INTO `system_log` VALUES (276, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:42:24');
INSERT INTO `system_log` VALUES (277, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 17:42:26');
INSERT INTO `system_log` VALUES (278, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 17:42:26');
INSERT INTO `system_log` VALUES (279, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 17:49:03');
INSERT INTO `system_log` VALUES (280, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 8, '2026-07-14 17:49:03');
INSERT INTO `system_log` VALUES (281, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-14 17:51:12');
INSERT INTO `system_log` VALUES (282, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 19:50:53');
INSERT INTO `system_log` VALUES (283, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 19:53:27');
INSERT INTO `system_log` VALUES (284, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 95, '2026-07-14 19:54:01');
INSERT INTO `system_log` VALUES (285, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 19:54:01');
INSERT INTO `system_log` VALUES (286, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 19:54:01');
INSERT INTO `system_log` VALUES (287, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 19:54:06');
INSERT INTO `system_log` VALUES (288, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 19:54:07');
INSERT INTO `system_log` VALUES (289, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 19:54:07');
INSERT INTO `system_log` VALUES (290, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 19:54:08');
INSERT INTO `system_log` VALUES (291, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 19:54:11');
INSERT INTO `system_log` VALUES (292, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 19:54:11');
INSERT INTO `system_log` VALUES (293, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 19:55:09');
INSERT INTO `system_log` VALUES (294, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 23, '2026-07-14 19:59:48');
INSERT INTO `system_log` VALUES (295, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 126, '2026-07-14 20:00:19');
INSERT INTO `system_log` VALUES (296, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 20:00:32');
INSERT INTO `system_log` VALUES (297, 2, 'test', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=你好)]', '0:0:0:0:0:0:0:1', 3396, '2026-07-14 20:00:44');
INSERT INTO `system_log` VALUES (298, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 20:02:17');
INSERT INTO `system_log` VALUES (299, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 20:02:17');
INSERT INTO `system_log` VALUES (300, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 20:02:17');
INSERT INTO `system_log` VALUES (301, 2, 'test', 'DELETE /api/chat-record/3', 'com.example.elderai.controller.ChatRecordController.deleteById', '[3]', '0:0:0:0:0:0:0:1', 43, '2026-07-14 20:02:22');
INSERT INTO `system_log` VALUES (302, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 20:02:22');
INSERT INTO `system_log` VALUES (303, 2, 'test', 'DELETE /api/chat-record/2', 'com.example.elderai.controller.ChatRecordController.deleteById', '[2]', '0:0:0:0:0:0:0:1', 20, '2026-07-14 20:02:24');
INSERT INTO `system_log` VALUES (304, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 20:02:24');
INSERT INTO `system_log` VALUES (305, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 20:02:25');
INSERT INTO `system_log` VALUES (306, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 20:02:25');
INSERT INTO `system_log` VALUES (307, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 20:02:27');
INSERT INTO `system_log` VALUES (308, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 20:02:28');
INSERT INTO `system_log` VALUES (309, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 20:02:28');
INSERT INTO `system_log` VALUES (310, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 20:09:25');
INSERT INTO `system_log` VALUES (311, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 20:09:25');
INSERT INTO `system_log` VALUES (312, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 20:09:38');
INSERT INTO `system_log` VALUES (313, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 20:09:38');
INSERT INTO `system_log` VALUES (314, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 20:09:47');
INSERT INTO `system_log` VALUES (315, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 20:09:47');
INSERT INTO `system_log` VALUES (316, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 20:10:06');
INSERT INTO `system_log` VALUES (317, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 20:10:06');
INSERT INTO `system_log` VALUES (318, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 20:10:13');
INSERT INTO `system_log` VALUES (319, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 20:10:13');
INSERT INTO `system_log` VALUES (320, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 20:10:18');
INSERT INTO `system_log` VALUES (321, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 20:10:18');
INSERT INTO `system_log` VALUES (322, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 20:10:50');
INSERT INTO `system_log` VALUES (323, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 20:10:50');
INSERT INTO `system_log` VALUES (324, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 20:15:22');
INSERT INTO `system_log` VALUES (325, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 20:15:22');
INSERT INTO `system_log` VALUES (326, 2, 'test', 'PUT /api/user/elder-info', 'com.example.elderai.controller.UserController.updateElderInfo', '[ElderInfoDTO(realName=null, gender=null, age=null, address=null, emergencyContact=小张, emergencyPhone=17377791921, medicalHistory=null)]', '0:0:0:0:0:0:0:1', 15, '2026-07-14 20:16:39');
INSERT INTO `system_log` VALUES (327, 2, 'test', 'PUT /api/user/elder-info', 'com.example.elderai.controller.UserController.updateElderInfo', '[ElderInfoDTO(realName=null, gender=null, age=null, address=null, emergencyContact=小张, emergencyPhone=17377791921, medicalHistory=null)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 20:16:41');
INSERT INTO `system_log` VALUES (328, 2, 'test', 'PUT /api/user/elder-info', 'com.example.elderai.controller.UserController.updateElderInfo', '[ElderInfoDTO(realName=null, gender=null, age=null, address=null, emergencyContact=小张, emergencyPhone=17377791921, medicalHistory=null)]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 20:16:44');
INSERT INTO `system_log` VALUES (329, 2, 'test', 'PUT /api/user/elder-info', 'com.example.elderai.controller.UserController.updateElderInfo', '[ElderInfoDTO(realName=null, gender=null, age=null, address=null, emergencyContact=小张, emergencyPhone=17377791921, medicalHistory=null)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 20:16:51');
INSERT INTO `system_log` VALUES (330, 2, 'test', 'PUT /api/user/elder-info', 'com.example.elderai.controller.UserController.updateElderInfo', '[ElderInfoDTO(realName=null, gender=null, age=null, address=null, emergencyContact=小张, emergencyPhone=17377791921, medicalHistory=null)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 20:17:02');
INSERT INTO `system_log` VALUES (331, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 20:17:45');
INSERT INTO `system_log` VALUES (332, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 20:17:47');
INSERT INTO `system_log` VALUES (333, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-14 20:17:47');
INSERT INTO `system_log` VALUES (334, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 20:17:48');
INSERT INTO `system_log` VALUES (335, 2, 'test', 'GET /api/news/3', 'com.example.elderai.controller.NewsController.getDetail', '[3]', '0:0:0:0:0:0:0:1', 53, '2026-07-14 20:17:51');
INSERT INTO `system_log` VALUES (336, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 20:17:55');
INSERT INTO `system_log` VALUES (337, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 20:17:57');
INSERT INTO `system_log` VALUES (338, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-14 20:17:57');
INSERT INTO `system_log` VALUES (339, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 46, '2026-07-14 20:19:55');
INSERT INTO `system_log` VALUES (340, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 46, '2026-07-14 20:19:55');
INSERT INTO `system_log` VALUES (341, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 11, '2026-07-14 20:19:58');
INSERT INTO `system_log` VALUES (342, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 38, '2026-07-14 20:19:58');
INSERT INTO `system_log` VALUES (343, 2, 'test', 'PUT /api/user/elder-info', 'com.example.elderai.controller.UserController.updateElderInfo', '[ElderInfoDTO(realName=null, gender=null, age=null, address=null, emergencyContact=小张, emergencyPhone=17377791921, medicalHistory=null)]', '0:0:0:0:0:0:0:1', 21, '2026-07-14 20:20:09');
INSERT INTO `system_log` VALUES (344, 2, 'test', 'POST /api/emergency/help', 'com.example.elderai.controller.EmergencyController.createHelp', '[EmergencyHelpDTO(contactName=小张, contactPhone=17377791921, helpContent=我遇到了紧急情况，请尽快联系我！)]', '0:0:0:0:0:0:0:1', 99, '2026-07-14 20:20:43');
INSERT INTO `system_log` VALUES (345, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-14 20:20:43');
INSERT INTO `system_log` VALUES (346, 2, 'test', 'POST /api/emergency/help', 'com.example.elderai.controller.EmergencyController.createHelp', '[EmergencyHelpDTO(contactName=小张, contactPhone=17377791921, helpContent=我遇到了紧急情况，请尽快联系我！)]', '0:0:0:0:0:0:0:1', 107, '2026-07-14 20:21:18');
INSERT INTO `system_log` VALUES (347, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 11, '2026-07-14 20:21:18');
INSERT INTO `system_log` VALUES (348, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 20:23:00');
INSERT INTO `system_log` VALUES (349, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 20:23:00');
INSERT INTO `system_log` VALUES (350, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 20:23:14');
INSERT INTO `system_log` VALUES (351, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 20:23:14');
INSERT INTO `system_log` VALUES (352, 2, 'test', 'POST /api/emergency/help', 'com.example.elderai.controller.EmergencyController.createHelp', '[EmergencyHelpDTO(contactName=小张, contactPhone=17377791921, helpContent=我遇到了紧急情况，请尽快联系我！)]', '0:0:0:0:0:0:0:1', 16, '2026-07-14 20:24:50');
INSERT INTO `system_log` VALUES (353, 2, 'test', 'PUT /api/user/elder-info', 'com.example.elderai.controller.UserController.updateElderInfo', '[ElderInfoDTO(realName=null, gender=null, age=null, address=null, emergencyContact=小张, emergencyPhone=17377791921, medicalHistory=null)]', '0:0:0:0:0:0:0:1', 15, '2026-07-14 20:25:00');
INSERT INTO `system_log` VALUES (354, 2, 'test', 'POST /api/emergency/help', 'com.example.elderai.controller.EmergencyController.createHelp', '[EmergencyHelpDTO(contactName=小张, contactPhone=17377791921, helpContent=我遇到了紧急情况，请尽快联系我！)]', '0:0:0:0:0:0:0:1', 20, '2026-07-14 20:25:03');
INSERT INTO `system_log` VALUES (355, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 20:26:55');
INSERT INTO `system_log` VALUES (356, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-14 20:26:55');
INSERT INTO `system_log` VALUES (357, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 107, '2026-07-14 20:26:57');
INSERT INTO `system_log` VALUES (358, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 20:26:58');
INSERT INTO `system_log` VALUES (359, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-14 20:26:58');
INSERT INTO `system_log` VALUES (360, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-14 20:28:36');
INSERT INTO `system_log` VALUES (361, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-14 20:28:36');
INSERT INTO `system_log` VALUES (362, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-14 20:46:07');
INSERT INTO `system_log` VALUES (363, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-14 20:51:06');
INSERT INTO `system_log` VALUES (364, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 11, '2026-07-14 20:51:06');
INSERT INTO `system_log` VALUES (365, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 136, '2026-07-15 14:50:40');
INSERT INTO `system_log` VALUES (366, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 26, '2026-07-15 14:51:08');
INSERT INTO `system_log` VALUES (367, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 25, '2026-07-15 14:51:09');
INSERT INTO `system_log` VALUES (368, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 306, '2026-07-15 14:53:10');
INSERT INTO `system_log` VALUES (369, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 36, '2026-07-15 15:02:18');
INSERT INTO `system_log` VALUES (370, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 26, '2026-07-15 15:02:20');
INSERT INTO `system_log` VALUES (371, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 26, '2026-07-15 15:02:20');
INSERT INTO `system_log` VALUES (372, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 12, '2026-07-15 15:02:21');
INSERT INTO `system_log` VALUES (373, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 15:02:26');
INSERT INTO `system_log` VALUES (374, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 15:02:26');
INSERT INTO `system_log` VALUES (375, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 13, '2026-07-15 15:02:27');
INSERT INTO `system_log` VALUES (376, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=admin, password=******)]', '0:0:0:0:0:0:0:1', 174, '2026-07-15 15:02:58');
INSERT INTO `system_log` VALUES (377, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 45, '2026-07-15 15:02:59');
INSERT INTO `system_log` VALUES (378, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 14, '2026-07-15 15:03:15');
INSERT INTO `system_log` VALUES (379, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 19, '2026-07-15 15:03:16');
INSERT INTO `system_log` VALUES (380, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 18, '2026-07-15 15:03:33');
INSERT INTO `system_log` VALUES (381, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 15:03:36');
INSERT INTO `system_log` VALUES (382, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 14, '2026-07-15 15:03:38');
INSERT INTO `system_log` VALUES (383, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 15:03:42');
INSERT INTO `system_log` VALUES (384, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 12, '2026-07-15 15:03:44');
INSERT INTO `system_log` VALUES (385, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 15:03:46');
INSERT INTO `system_log` VALUES (386, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 13, '2026-07-15 15:05:14');
INSERT INTO `system_log` VALUES (387, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 15:05:14');
INSERT INTO `system_log` VALUES (388, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 15:05:15');
INSERT INTO `system_log` VALUES (389, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 15:05:17');
INSERT INTO `system_log` VALUES (390, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 15:11:58');
INSERT INTO `system_log` VALUES (391, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 15:11:59');
INSERT INTO `system_log` VALUES (392, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 14, '2026-07-15 15:12:01');
INSERT INTO `system_log` VALUES (393, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 15:12:02');
INSERT INTO `system_log` VALUES (394, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 15:12:03');
INSERT INTO `system_log` VALUES (395, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 15:12:29');
INSERT INTO `system_log` VALUES (396, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 15:12:34');
INSERT INTO `system_log` VALUES (397, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 15:12:52');
INSERT INTO `system_log` VALUES (398, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 15:12:56');
INSERT INTO `system_log` VALUES (399, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 15:13:10');
INSERT INTO `system_log` VALUES (400, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 15:13:13');
INSERT INTO `system_log` VALUES (401, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 15:13:17');
INSERT INTO `system_log` VALUES (402, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 15:14:33');
INSERT INTO `system_log` VALUES (403, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 15:14:41');
INSERT INTO `system_log` VALUES (404, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 15:21:15');
INSERT INTO `system_log` VALUES (405, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 52, '2026-07-15 15:21:15');
INSERT INTO `system_log` VALUES (406, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 14, '2026-07-15 15:24:55');
INSERT INTO `system_log` VALUES (407, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=admin, password=******)]', '0:0:0:0:0:0:0:1', 239, '2026-07-15 15:25:52');
INSERT INTO `system_log` VALUES (408, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 15:25:52');
INSERT INTO `system_log` VALUES (409, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 15:25:57');
INSERT INTO `system_log` VALUES (410, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 15:25:58');
INSERT INTO `system_log` VALUES (411, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 15:25:59');
INSERT INTO `system_log` VALUES (412, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-15 15:26:01');
INSERT INTO `system_log` VALUES (413, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 15:35:13');
INSERT INTO `system_log` VALUES (414, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 15:46:46');
INSERT INTO `system_log` VALUES (415, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 58, '2026-07-15 15:48:08');
INSERT INTO `system_log` VALUES (416, NULL, NULL, 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 15:48:27');
INSERT INTO `system_log` VALUES (417, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 288, '2026-07-15 15:49:02');
INSERT INTO `system_log` VALUES (418, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 18, '2026-07-15 15:49:11');
INSERT INTO `system_log` VALUES (419, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 22, '2026-07-15 15:49:11');
INSERT INTO `system_log` VALUES (420, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 12, '2026-07-15 15:49:19');
INSERT INTO `system_log` VALUES (421, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 15:57:17');
INSERT INTO `system_log` VALUES (422, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 15:58:03');
INSERT INTO `system_log` VALUES (423, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 21, '2026-07-15 15:58:10');
INSERT INTO `system_log` VALUES (424, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 15:58:20');
INSERT INTO `system_log` VALUES (425, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 22, '2026-07-15 15:58:21');
INSERT INTO `system_log` VALUES (426, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 42, '2026-07-15 15:58:21');
INSERT INTO `system_log` VALUES (427, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-15 15:58:25');
INSERT INTO `system_log` VALUES (428, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 15:58:28');
INSERT INTO `system_log` VALUES (429, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 15:58:28');
INSERT INTO `system_log` VALUES (430, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 15:58:30');
INSERT INTO `system_log` VALUES (431, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 53, '2026-07-15 15:58:35');
INSERT INTO `system_log` VALUES (432, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 53, '2026-07-15 15:58:35');
INSERT INTO `system_log` VALUES (433, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 3, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 15:58:37');
INSERT INTO `system_log` VALUES (434, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=admin, password=******)]', '0:0:0:0:0:0:0:1', 262, '2026-07-15 15:58:58');
INSERT INTO `system_log` VALUES (435, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 15:58:58');
INSERT INTO `system_log` VALUES (436, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 15:59:11');
INSERT INTO `system_log` VALUES (437, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 15:59:13');
INSERT INTO `system_log` VALUES (438, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 15:59:18');
INSERT INTO `system_log` VALUES (439, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 72, '2026-07-15 15:59:19');
INSERT INTO `system_log` VALUES (440, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 41, '2026-07-15 15:59:26');
INSERT INTO `system_log` VALUES (441, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 15:59:30');
INSERT INTO `system_log` VALUES (442, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 250, '2026-07-15 15:59:50');
INSERT INTO `system_log` VALUES (443, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 16:02:30');
INSERT INTO `system_log` VALUES (444, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=admin, password=******)]', '0:0:0:0:0:0:0:1', 208, '2026-07-15 16:05:25');
INSERT INTO `system_log` VALUES (445, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 16:05:26');
INSERT INTO `system_log` VALUES (446, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 16:22:24');
INSERT INTO `system_log` VALUES (447, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 16:22:24');
INSERT INTO `system_log` VALUES (448, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 12, '2026-07-15 16:29:04');
INSERT INTO `system_log` VALUES (449, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 16:29:05');
INSERT INTO `system_log` VALUES (450, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 16:44:14');
INSERT INTO `system_log` VALUES (451, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 39, '2026-07-15 16:44:20');
INSERT INTO `system_log` VALUES (452, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 32, '2026-07-15 16:44:20');
INSERT INTO `system_log` VALUES (453, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 16:44:20');
INSERT INTO `system_log` VALUES (454, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 16:46:16');
INSERT INTO `system_log` VALUES (455, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 16:46:23');
INSERT INTO `system_log` VALUES (456, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 16:46:23');
INSERT INTO `system_log` VALUES (457, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 16:49:00');
INSERT INTO `system_log` VALUES (458, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 16:49:00');
INSERT INTO `system_log` VALUES (459, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 16:49:08');
INSERT INTO `system_log` VALUES (460, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 16:49:51');
INSERT INTO `system_log` VALUES (461, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 16:50:09');
INSERT INTO `system_log` VALUES (462, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 16:56:57');
INSERT INTO `system_log` VALUES (463, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 16:57:04');
INSERT INTO `system_log` VALUES (464, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 1, '2026-07-15 16:57:07');
INSERT INTO `system_log` VALUES (465, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 13, '2026-07-15 16:58:01');
INSERT INTO `system_log` VALUES (466, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 16, '2026-07-15 16:58:14');
INSERT INTO `system_log` VALUES (467, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 16:58:15');
INSERT INTO `system_log` VALUES (468, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 16:58:17');
INSERT INTO `system_log` VALUES (469, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 16:59:28');
INSERT INTO `system_log` VALUES (470, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 21, '2026-07-15 17:02:19');
INSERT INTO `system_log` VALUES (471, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 5, (无法序列化)]', '0:0:0:0:0:0:0:1', 18, '2026-07-15 17:02:19');
INSERT INTO `system_log` VALUES (472, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 15, '2026-07-15 17:02:45');
INSERT INTO `system_log` VALUES (473, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 5, (无法序列化)]', '0:0:0:0:0:0:0:1', 18, '2026-07-15 17:02:45');
INSERT INTO `system_log` VALUES (474, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 17:02:46');
INSERT INTO `system_log` VALUES (475, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 17:02:46');
INSERT INTO `system_log` VALUES (476, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 24, '2026-07-15 17:02:46');
INSERT INTO `system_log` VALUES (477, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 109, '2026-07-15 17:03:06');
INSERT INTO `system_log` VALUES (478, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 17:03:11');
INSERT INTO `system_log` VALUES (479, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 17:03:12');
INSERT INTO `system_log` VALUES (480, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 17:03:12');
INSERT INTO `system_log` VALUES (481, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 12, '2026-07-15 17:03:14');
INSERT INTO `system_log` VALUES (482, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 17:03:16');
INSERT INTO `system_log` VALUES (483, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 17:03:16');
INSERT INTO `system_log` VALUES (484, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 17:03:17');
INSERT INTO `system_log` VALUES (485, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 17:03:24');
INSERT INTO `system_log` VALUES (486, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 17:03:24');
INSERT INTO `system_log` VALUES (487, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 17:03:24');
INSERT INTO `system_log` VALUES (488, 1, 'admin', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 18:43:37');
INSERT INTO `system_log` VALUES (489, 1, 'admin', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 39, '2026-07-15 18:43:37');
INSERT INTO `system_log` VALUES (490, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 18:43:54');
INSERT INTO `system_log` VALUES (491, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 18:43:54');
INSERT INTO `system_log` VALUES (492, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 14, '2026-07-15 18:43:54');
INSERT INTO `system_log` VALUES (493, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=admin, password=******)]', '0:0:0:0:0:0:0:1', 73, '2026-07-15 18:47:20');
INSERT INTO `system_log` VALUES (494, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 18:47:21');
INSERT INTO `system_log` VALUES (495, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 5, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 18:47:21');
INSERT INTO `system_log` VALUES (496, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 18:47:27');
INSERT INTO `system_log` VALUES (497, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 18:47:31');
INSERT INTO `system_log` VALUES (498, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 18:47:36');
INSERT INTO `system_log` VALUES (499, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 18:47:39');
INSERT INTO `system_log` VALUES (500, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 18:47:48');
INSERT INTO `system_log` VALUES (501, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 18:47:51');
INSERT INTO `system_log` VALUES (502, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 1, '2026-07-15 18:47:51');
INSERT INTO `system_log` VALUES (503, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 18:47:51');
INSERT INTO `system_log` VALUES (504, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=admin, password=******)]', '0:0:0:0:0:0:0:1', 66, '2026-07-15 18:48:13');
INSERT INTO `system_log` VALUES (505, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 18:48:13');
INSERT INTO `system_log` VALUES (506, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 5, (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 18:48:13');
INSERT INTO `system_log` VALUES (507, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 18:48:57');
INSERT INTO `system_log` VALUES (508, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 18:49:00');
INSERT INTO `system_log` VALUES (509, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 18:49:01');
INSERT INTO `system_log` VALUES (510, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 5, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 18:49:01');
INSERT INTO `system_log` VALUES (511, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-15 18:49:03');
INSERT INTO `system_log` VALUES (512, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 12, '2026-07-15 19:00:45');
INSERT INTO `system_log` VALUES (513, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 16, '2026-07-15 19:00:45');
INSERT INTO `system_log` VALUES (514, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 34, '2026-07-15 19:00:45');
INSERT INTO `system_log` VALUES (515, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 19:01:22');
INSERT INTO `system_log` VALUES (516, 1, 'admin', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 19:01:23');
INSERT INTO `system_log` VALUES (517, 1, 'admin', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 19:01:23');
INSERT INTO `system_log` VALUES (518, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:01:24');
INSERT INTO `system_log` VALUES (519, 1, 'admin', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 16, '2026-07-15 19:01:29');
INSERT INTO `system_log` VALUES (520, 1, 'admin', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:01:35');
INSERT INTO `system_log` VALUES (521, 1, 'admin', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 19:01:35');
INSERT INTO `system_log` VALUES (522, 1, 'admin', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 19:01:36');
INSERT INTO `system_log` VALUES (523, 1, 'admin', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 19:01:36');
INSERT INTO `system_log` VALUES (524, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 19:01:38');
INSERT INTO `system_log` VALUES (525, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 19:01:38');
INSERT INTO `system_log` VALUES (526, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 19:01:48');
INSERT INTO `system_log` VALUES (527, 1, 'admin', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:01:50');
INSERT INTO `system_log` VALUES (528, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 0, '2026-07-15 19:01:53');
INSERT INTO `system_log` VALUES (529, 1, 'admin', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:02:58');
INSERT INTO `system_log` VALUES (530, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 1, '2026-07-15 19:03:00');
INSERT INTO `system_log` VALUES (531, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-15 19:03:01');
INSERT INTO `system_log` VALUES (532, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-15 19:03:01');
INSERT INTO `system_log` VALUES (533, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 19:03:16');
INSERT INTO `system_log` VALUES (534, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 19:03:28');
INSERT INTO `system_log` VALUES (535, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 95, '2026-07-15 19:03:48');
INSERT INTO `system_log` VALUES (536, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:03:52');
INSERT INTO `system_log` VALUES (537, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 1, '2026-07-15 19:03:55');
INSERT INTO `system_log` VALUES (538, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 1, '2026-07-15 19:03:55');
INSERT INTO `system_log` VALUES (539, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 0, '2026-07-15 19:03:57');
INSERT INTO `system_log` VALUES (540, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 19:04:55');
INSERT INTO `system_log` VALUES (541, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 19:04:56');
INSERT INTO `system_log` VALUES (542, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:04:56');
INSERT INTO `system_log` VALUES (543, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 1, '2026-07-15 19:05:19');
INSERT INTO `system_log` VALUES (544, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 19:05:19');
INSERT INTO `system_log` VALUES (545, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-15 19:05:19');
INSERT INTO `system_log` VALUES (546, 1, 'admin', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 19:05:22');
INSERT INTO `system_log` VALUES (547, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 19:05:24');
INSERT INTO `system_log` VALUES (548, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:08:44');
INSERT INTO `system_log` VALUES (549, 1, 'admin', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 19:08:45');
INSERT INTO `system_log` VALUES (550, 1, 'admin', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 1, '2026-07-15 19:08:47');
INSERT INTO `system_log` VALUES (551, 1, 'admin', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 19:08:47');
INSERT INTO `system_log` VALUES (552, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:08:48');
INSERT INTO `system_log` VALUES (553, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:08:48');
INSERT INTO `system_log` VALUES (554, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 19:08:50');
INSERT INTO `system_log` VALUES (555, 1, 'admin', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 8, '2026-07-15 19:09:23');
INSERT INTO `system_log` VALUES (556, 1, 'admin', 'PUT /api/user/password', 'com.example.elderai.controller.UserController.updatePassword', '[PasswordUpdateDTO(oldPassword=******, newPassword=******)]', '0:0:0:0:0:0:0:1', 165, '2026-07-15 19:09:39');
INSERT INTO `system_log` VALUES (557, 1, 'admin', 'PUT /api/user/password', 'com.example.elderai.controller.UserController.updatePassword', '[PasswordUpdateDTO(oldPassword=******, newPassword=******)]', '0:0:0:0:0:0:0:1', 97, '2026-07-15 19:09:48');
INSERT INTO `system_log` VALUES (558, 1, 'admin', 'PUT /api/user/password', 'com.example.elderai.controller.UserController.updatePassword', '[PasswordUpdateDTO(oldPassword=******, newPassword=******)]', '0:0:0:0:0:0:0:1', 361, '2026-07-15 19:10:04');
INSERT INTO `system_log` VALUES (559, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 19:10:16');
INSERT INTO `system_log` VALUES (560, 1, 'admin', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 19:10:19');
INSERT INTO `system_log` VALUES (561, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 97, '2026-07-15 19:10:43');
INSERT INTO `system_log` VALUES (562, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 125, '2026-07-15 19:10:51');
INSERT INTO `system_log` VALUES (563, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:10:54');
INSERT INTO `system_log` VALUES (564, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:11:10');
INSERT INTO `system_log` VALUES (565, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 19:11:14');
INSERT INTO `system_log` VALUES (566, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:11:16');
INSERT INTO `system_log` VALUES (567, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 19:11:16');
INSERT INTO `system_log` VALUES (568, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 19:11:22');
INSERT INTO `system_log` VALUES (569, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 19:11:48');
INSERT INTO `system_log` VALUES (570, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:11:55');
INSERT INTO `system_log` VALUES (571, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 0, '2026-07-15 19:12:02');
INSERT INTO `system_log` VALUES (572, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:12:10');
INSERT INTO `system_log` VALUES (573, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 19:12:24');
INSERT INTO `system_log` VALUES (574, 2, 'test', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=今天天气怎么样？)]', '0:0:0:0:0:0:0:1', 4453, '2026-07-15 19:14:16');
INSERT INTO `system_log` VALUES (575, 2, 'test', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=我这里是杭州 请告诉我杭州的天气)]', '0:0:0:0:0:0:0:1', 6178, '2026-07-15 19:15:26');
INSERT INTO `system_log` VALUES (576, 2, 'test', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=今天新闻联播是什么)]', '0:0:0:0:0:0:0:1', 4275, '2026-07-15 19:18:01');
INSERT INTO `system_log` VALUES (577, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 12, '2026-07-15 19:26:43');
INSERT INTO `system_log` VALUES (578, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 12, '2026-07-15 19:26:43');
INSERT INTO `system_log` VALUES (579, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 19:26:51');
INSERT INTO `system_log` VALUES (580, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:26:52');
INSERT INTO `system_log` VALUES (581, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 19:26:52');
INSERT INTO `system_log` VALUES (582, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 19:26:58');
INSERT INTO `system_log` VALUES (583, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 18, '2026-07-15 19:27:57');
INSERT INTO `system_log` VALUES (584, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 19:29:00');
INSERT INTO `system_log` VALUES (585, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 19:29:02');
INSERT INTO `system_log` VALUES (586, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 19:29:02');
INSERT INTO `system_log` VALUES (587, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:29:09');
INSERT INTO `system_log` VALUES (588, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 8, '2026-07-15 19:31:13');
INSERT INTO `system_log` VALUES (589, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 19:31:26');
INSERT INTO `system_log` VALUES (590, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 19:34:39');
INSERT INTO `system_log` VALUES (591, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 19:34:39');
INSERT INTO `system_log` VALUES (592, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-15 19:34:39');
INSERT INTO `system_log` VALUES (593, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 19:36:25');
INSERT INTO `system_log` VALUES (594, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 19:36:25');
INSERT INTO `system_log` VALUES (595, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 19:36:25');
INSERT INTO `system_log` VALUES (596, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 19:36:41');
INSERT INTO `system_log` VALUES (597, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 19:36:41');
INSERT INTO `system_log` VALUES (598, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 19:36:41');
INSERT INTO `system_log` VALUES (599, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 19:36:47');
INSERT INTO `system_log` VALUES (600, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 27, '2026-07-15 19:36:48');
INSERT INTO `system_log` VALUES (601, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 19:36:48');
INSERT INTO `system_log` VALUES (602, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-15 19:38:16');
INSERT INTO `system_log` VALUES (603, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-15 19:38:16');
INSERT INTO `system_log` VALUES (604, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 46, '2026-07-15 19:41:43');
INSERT INTO `system_log` VALUES (605, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 46, '2026-07-15 19:41:43');
INSERT INTO `system_log` VALUES (606, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 19:41:45');
INSERT INTO `system_log` VALUES (607, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 19:41:45');
INSERT INTO `system_log` VALUES (608, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 19:41:54');
INSERT INTO `system_log` VALUES (609, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 19:41:54');
INSERT INTO `system_log` VALUES (610, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 77, '2026-07-15 19:41:54');
INSERT INTO `system_log` VALUES (611, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2869, '2026-07-15 19:41:57');
INSERT INTO `system_log` VALUES (612, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 700, '2026-07-15 19:42:06');
INSERT INTO `system_log` VALUES (613, 2, 'test', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=今天湖南天气, city=杭州)]', '0:0:0:0:0:0:0:1', 980, '2026-07-15 19:42:35');
INSERT INTO `system_log` VALUES (614, 2, 'test', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=今天湖南的天气怎么样, city=杭州)]', '0:0:0:0:0:0:0:1', 965, '2026-07-15 19:43:00');
INSERT INTO `system_log` VALUES (615, 2, 'test', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=今天湖南天气, city=杭州)]', '0:0:0:0:0:0:0:1', 2733, '2026-07-15 19:45:08');
INSERT INTO `system_log` VALUES (616, 2, 'test', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=今天湖南天气怎么样, city=杭州)]', '0:0:0:0:0:0:0:1', 1651, '2026-07-15 19:45:27');
INSERT INTO `system_log` VALUES (617, 2, 'test', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=今天长沙天气怎么有, city=杭州)]', '0:0:0:0:0:0:0:1', 9266, '2026-07-15 20:03:39');
INSERT INTO `system_log` VALUES (618, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 174, '2026-07-15 20:04:26');
INSERT INTO `system_log` VALUES (619, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 20:04:26');
INSERT INTO `system_log` VALUES (620, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 28, '2026-07-15 20:04:27');
INSERT INTO `system_log` VALUES (621, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 96, '2026-07-15 20:04:27');
INSERT INTO `system_log` VALUES (622, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 3293, '2026-07-15 20:04:30');
INSERT INTO `system_log` VALUES (623, 2, 'test', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=今天天气怎么样？, city=北京)]', '0:0:0:0:0:0:0:1', 2918, '2026-07-15 20:04:42');
INSERT INTO `system_log` VALUES (624, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 20:05:25');
INSERT INTO `system_log` VALUES (625, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 20:05:25');
INSERT INTO `system_log` VALUES (626, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 20:05:29');
INSERT INTO `system_log` VALUES (627, 2, 'test', 'POST /api/reminder/add', 'com.example.elderai.controller.ReminderController.add', '[ReminderDTO(title=吃药, content=, remindType=MEDICINE, remindTime=2026-07-15T20:07)]', '0:0:0:0:0:0:0:1', 101, '2026-07-15 20:05:42');
INSERT INTO `system_log` VALUES (628, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 20:05:42');
INSERT INTO `system_log` VALUES (629, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 20:06:18');
INSERT INTO `system_log` VALUES (630, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 20:06:18');
INSERT INTO `system_log` VALUES (631, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 20:06:18');
INSERT INTO `system_log` VALUES (632, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2437, '2026-07-15 20:06:20');
INSERT INTO `system_log` VALUES (633, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 25, '2026-07-15 20:20:31');
INSERT INTO `system_log` VALUES (634, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 20:30:01');
INSERT INTO `system_log` VALUES (635, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2506, '2026-07-15 20:38:21');
INSERT INTO `system_log` VALUES (636, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=长沙)]', '0:0:0:0:0:0:0:1', 688, '2026-07-15 20:38:32');
INSERT INTO `system_log` VALUES (637, 2, 'test', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=你好, city=null)]', '0:0:0:0:0:0:0:1', 1507, '2026-07-15 20:38:53');
INSERT INTO `system_log` VALUES (638, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 20:46:51');
INSERT INTO `system_log` VALUES (639, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 27, '2026-07-15 20:46:51');
INSERT INTO `system_log` VALUES (640, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 46, '2026-07-15 20:46:51');
INSERT INTO `system_log` VALUES (641, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2486, '2026-07-15 20:46:53');
INSERT INTO `system_log` VALUES (642, 2, 'test', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=你好, city=null)]', '0:0:0:0:0:0:0:1', 1385, '2026-07-15 20:47:01');
INSERT INTO `system_log` VALUES (643, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 39, '2026-07-15 20:50:57');
INSERT INTO `system_log` VALUES (644, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 20:58:30');
INSERT INTO `system_log` VALUES (645, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 23, '2026-07-15 21:18:59');
INSERT INTO `system_log` VALUES (646, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 21:19:03');
INSERT INTO `system_log` VALUES (647, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 21:19:03');
INSERT INTO `system_log` VALUES (648, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 30, '2026-07-15 21:19:03');
INSERT INTO `system_log` VALUES (649, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2488, '2026-07-15 21:19:06');
INSERT INTO `system_log` VALUES (650, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 21:19:35');
INSERT INTO `system_log` VALUES (651, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 21:19:35');
INSERT INTO `system_log` VALUES (652, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 18, '2026-07-15 21:19:35');
INSERT INTO `system_log` VALUES (653, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 1644, '2026-07-15 21:19:37');
INSERT INTO `system_log` VALUES (654, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 21:20:02');
INSERT INTO `system_log` VALUES (655, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 21:20:05');
INSERT INTO `system_log` VALUES (656, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 21:20:05');
INSERT INTO `system_log` VALUES (657, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-15 21:20:05');
INSERT INTO `system_log` VALUES (658, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 1575, '2026-07-15 21:20:06');
INSERT INTO `system_log` VALUES (659, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 21:20:08');
INSERT INTO `system_log` VALUES (660, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 21:20:27');
INSERT INTO `system_log` VALUES (661, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 21:20:27');
INSERT INTO `system_log` VALUES (662, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 21:20:27');
INSERT INTO `system_log` VALUES (663, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 1726, '2026-07-15 21:20:28');
INSERT INTO `system_log` VALUES (664, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 21:22:11');
INSERT INTO `system_log` VALUES (665, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 21:22:11');
INSERT INTO `system_log` VALUES (666, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 16, '2026-07-15 21:22:11');
INSERT INTO `system_log` VALUES (667, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2480, '2026-07-15 21:22:14');
INSERT INTO `system_log` VALUES (668, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 21:23:13');
INSERT INTO `system_log` VALUES (669, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 21:23:13');
INSERT INTO `system_log` VALUES (670, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 21:23:13');
INSERT INTO `system_log` VALUES (671, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 1569, '2026-07-15 21:23:15');
INSERT INTO `system_log` VALUES (672, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 168, '2026-07-15 21:24:13');
INSERT INTO `system_log` VALUES (673, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 21:24:17');
INSERT INTO `system_log` VALUES (674, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 21:24:17');
INSERT INTO `system_log` VALUES (675, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 22, '2026-07-15 21:24:17');
INSERT INTO `system_log` VALUES (676, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 1721, '2026-07-15 21:24:18');
INSERT INTO `system_log` VALUES (677, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 21:25:38');
INSERT INTO `system_log` VALUES (678, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 21:25:38');
INSERT INTO `system_log` VALUES (679, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 12, '2026-07-15 21:25:38');
INSERT INTO `system_log` VALUES (680, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2457, '2026-07-15 21:25:40');
INSERT INTO `system_log` VALUES (681, 2, 'test', 'PUT /api/reminder/1/complete', 'com.example.elderai.controller.ReminderController.complete', '[1]', '0:0:0:0:0:0:0:1', 20, '2026-07-15 21:28:58');
INSERT INTO `system_log` VALUES (682, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 21:29:11');
INSERT INTO `system_log` VALUES (683, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 21:29:11');
INSERT INTO `system_log` VALUES (684, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 13, '2026-07-15 21:29:11');
INSERT INTO `system_log` VALUES (685, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2484, '2026-07-15 21:29:14');
INSERT INTO `system_log` VALUES (686, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 21:29:21');
INSERT INTO `system_log` VALUES (687, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 21:29:21');
INSERT INTO `system_log` VALUES (688, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 25, '2026-07-15 21:29:21');
INSERT INTO `system_log` VALUES (689, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 716, '2026-07-15 21:29:22');
INSERT INTO `system_log` VALUES (690, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 21:29:59');
INSERT INTO `system_log` VALUES (691, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 21:29:59');
INSERT INTO `system_log` VALUES (692, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 21:30:02');
INSERT INTO `system_log` VALUES (693, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 21:30:02');
INSERT INTO `system_log` VALUES (694, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 18, '2026-07-15 21:30:02');
INSERT INTO `system_log` VALUES (695, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 699, '2026-07-15 21:30:02');
INSERT INTO `system_log` VALUES (696, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 677, '2026-07-15 21:30:18');
INSERT INTO `system_log` VALUES (697, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 21:33:50');
INSERT INTO `system_log` VALUES (698, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 21:33:50');
INSERT INTO `system_log` VALUES (699, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 16, '2026-07-15 21:33:50');
INSERT INTO `system_log` VALUES (700, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 21:33:51');
INSERT INTO `system_log` VALUES (701, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 14, '2026-07-15 21:33:51');
INSERT INTO `system_log` VALUES (702, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 18, '2026-07-15 21:33:51');
INSERT INTO `system_log` VALUES (703, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 2497, '2026-07-15 21:33:53');
INSERT INTO `system_log` VALUES (704, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 2591, '2026-07-15 21:33:53');
INSERT INTO `system_log` VALUES (705, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 21:35:38');
INSERT INTO `system_log` VALUES (706, 2, 'test', 'PUT /api/reminder/1/complete', 'com.example.elderai.controller.ReminderController.complete', '[1]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 21:35:41');
INSERT INTO `system_log` VALUES (707, 2, 'test', 'PUT /api/reminder/1/uncomplete', 'com.example.elderai.controller.ReminderController.uncomplete', '[1]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 21:35:44');
INSERT INTO `system_log` VALUES (708, 2, 'test', 'DELETE /api/reminder/1', 'com.example.elderai.controller.ReminderController.delete', '[1]', '0:0:0:0:0:0:0:1', 32, '2026-07-15 21:35:46');
INSERT INTO `system_log` VALUES (709, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 24, '2026-07-15 21:42:55');
INSERT INTO `system_log` VALUES (710, 2, 'test', 'DELETE /api/chat-record/5', 'com.example.elderai.controller.ChatRecordController.deleteById', '[5]', '0:0:0:0:0:0:0:1', 26, '2026-07-15 21:42:59');
INSERT INTO `system_log` VALUES (711, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 21:42:59');
INSERT INTO `system_log` VALUES (712, 2, 'test', 'DELETE /api/chat-record/clear', 'com.example.elderai.controller.ChatRecordController.clearAll', '[]', '0:0:0:0:0:0:0:1', 28, '2026-07-15 21:43:02');
INSERT INTO `system_log` VALUES (713, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 21:43:02');
INSERT INTO `system_log` VALUES (714, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 21:43:04');
INSERT INTO `system_log` VALUES (715, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 13, '2026-07-15 21:43:04');
INSERT INTO `system_log` VALUES (716, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 17, '2026-07-15 21:43:04');
INSERT INTO `system_log` VALUES (717, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 2486, '2026-07-15 21:43:07');
INSERT INTO `system_log` VALUES (718, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 21:43:13');
INSERT INTO `system_log` VALUES (719, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 21:43:13');
INSERT INTO `system_log` VALUES (720, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 21:43:13');
INSERT INTO `system_log` VALUES (721, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 686, '2026-07-15 21:43:14');
INSERT INTO `system_log` VALUES (722, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 21:52:34');
INSERT INTO `system_log` VALUES (723, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 21:52:34');
INSERT INTO `system_log` VALUES (724, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 17, '2026-07-15 21:52:34');
INSERT INTO `system_log` VALUES (725, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 2459, '2026-07-15 21:52:36');
INSERT INTO `system_log` VALUES (726, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 21:56:52');
INSERT INTO `system_log` VALUES (727, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 21:56:52');
INSERT INTO `system_log` VALUES (728, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 21:56:52');
INSERT INTO `system_log` VALUES (729, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 2585, '2026-07-15 21:56:54');
INSERT INTO `system_log` VALUES (730, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 21:57:36');
INSERT INTO `system_log` VALUES (731, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-15 21:57:36');
INSERT INTO `system_log` VALUES (732, 2, 'test', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 40, '2026-07-15 21:57:36');
INSERT INTO `system_log` VALUES (733, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 100, '2026-07-15 22:08:10');
INSERT INTO `system_log` VALUES (734, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 91, '2026-07-15 22:08:16');
INSERT INTO `system_log` VALUES (735, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 22:08:16');
INSERT INTO `system_log` VALUES (736, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 22:08:16');
INSERT INTO `system_log` VALUES (737, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 14, '2026-07-15 22:08:16');
INSERT INTO `system_log` VALUES (738, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2489, '2026-07-15 22:08:19');
INSERT INTO `system_log` VALUES (739, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 8, '2026-07-15 22:08:47');
INSERT INTO `system_log` VALUES (740, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 13, '2026-07-15 22:08:47');
INSERT INTO `system_log` VALUES (741, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 15, '2026-07-15 22:08:47');
INSERT INTO `system_log` VALUES (742, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 709, '2026-07-15 22:08:48');
INSERT INTO `system_log` VALUES (743, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 22:08:53');
INSERT INTO `system_log` VALUES (744, 2, 'test', 'POST /api/reminder/add', 'com.example.elderai.controller.ReminderController.add', '[ReminderDTO(title=qq, content=, remindType=EXERCISE, remindTime=2026-07-15T22:10)]', '0:0:0:0:0:0:0:1', 57, '2026-07-15 22:09:03');
INSERT INTO `system_log` VALUES (745, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 22:09:03');
INSERT INTO `system_log` VALUES (746, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 22:09:21');
INSERT INTO `system_log` VALUES (747, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 16, '2026-07-15 22:09:21');
INSERT INTO `system_log` VALUES (748, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 22:09:22');
INSERT INTO `system_log` VALUES (749, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 22:09:22');
INSERT INTO `system_log` VALUES (750, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 22:09:22');
INSERT INTO `system_log` VALUES (751, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 696, '2026-07-15 22:09:23');
INSERT INTO `system_log` VALUES (752, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 22:09:27');
INSERT INTO `system_log` VALUES (753, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 22:09:31');
INSERT INTO `system_log` VALUES (754, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 22:09:31');
INSERT INTO `system_log` VALUES (755, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 19, '2026-07-15 22:09:31');
INSERT INTO `system_log` VALUES (756, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 685, '2026-07-15 22:09:31');
INSERT INTO `system_log` VALUES (757, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 22:09:45');
INSERT INTO `system_log` VALUES (758, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 12, '2026-07-15 22:09:45');
INSERT INTO `system_log` VALUES (759, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-15 22:09:45');
INSERT INTO `system_log` VALUES (760, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 693, '2026-07-15 22:09:46');
INSERT INTO `system_log` VALUES (761, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 22:09:49');
INSERT INTO `system_log` VALUES (762, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 22:09:49');
INSERT INTO `system_log` VALUES (763, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-15 22:09:49');
INSERT INTO `system_log` VALUES (764, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 727, '2026-07-15 22:09:49');
INSERT INTO `system_log` VALUES (765, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=admin, password=******)]', '0:0:0:0:0:0:0:1', 93, '2026-07-15 22:10:59');
INSERT INTO `system_log` VALUES (766, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 23, '2026-07-15 22:11:00');
INSERT INTO `system_log` VALUES (767, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 5, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 22:11:00');
INSERT INTO `system_log` VALUES (768, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 22:14:02');
INSERT INTO `system_log` VALUES (769, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 22:14:02');
INSERT INTO `system_log` VALUES (770, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-15 22:14:02');
INSERT INTO `system_log` VALUES (771, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2467, '2026-07-15 22:14:05');
INSERT INTO `system_log` VALUES (772, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 22:14:11');
INSERT INTO `system_log` VALUES (773, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 22:21:08');
INSERT INTO `system_log` VALUES (774, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 22:21:35');
INSERT INTO `system_log` VALUES (775, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 22:21:35');
INSERT INTO `system_log` VALUES (776, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 22:21:38');
INSERT INTO `system_log` VALUES (777, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 22:21:42');
INSERT INTO `system_log` VALUES (778, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 22:21:42');
INSERT INTO `system_log` VALUES (779, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 22:21:42');
INSERT INTO `system_log` VALUES (780, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2442, '2026-07-15 22:21:45');
INSERT INTO `system_log` VALUES (781, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 22:28:28');
INSERT INTO `system_log` VALUES (782, 1, 'admin', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 12, '2026-07-15 22:28:30');
INSERT INTO `system_log` VALUES (783, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 13, '2026-07-15 22:28:34');
INSERT INTO `system_log` VALUES (784, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 22:28:39');
INSERT INTO `system_log` VALUES (785, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-15 22:28:40');
INSERT INTO `system_log` VALUES (786, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 12, '2026-07-15 22:28:40');
INSERT INTO `system_log` VALUES (787, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 22:28:41');
INSERT INTO `system_log` VALUES (788, 1, 'admin', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 8, '2026-07-15 22:28:42');
INSERT INTO `system_log` VALUES (789, 1, 'admin', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 8, '2026-07-15 22:31:15');
INSERT INTO `system_log` VALUES (790, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 147, '2026-07-15 23:07:54');
INSERT INTO `system_log` VALUES (791, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 23:07:54');
INSERT INTO `system_log` VALUES (792, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 15, '2026-07-15 23:07:54');
INSERT INTO `system_log` VALUES (793, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 21, '2026-07-15 23:07:54');
INSERT INTO `system_log` VALUES (794, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2478, '2026-07-15 23:07:57');
INSERT INTO `system_log` VALUES (795, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 23:08:07');
INSERT INTO `system_log` VALUES (796, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 23:08:07');
INSERT INTO `system_log` VALUES (797, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 18, '2026-07-15 23:08:07');
INSERT INTO `system_log` VALUES (798, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 689, '2026-07-15 23:08:07');
INSERT INTO `system_log` VALUES (799, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 23:08:24');
INSERT INTO `system_log` VALUES (800, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 23:08:24');
INSERT INTO `system_log` VALUES (801, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 23:08:24');
INSERT INTO `system_log` VALUES (802, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 678, '2026-07-15 23:08:25');
INSERT INTO `system_log` VALUES (803, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 23:08:28');
INSERT INTO `system_log` VALUES (804, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 23:08:33');
INSERT INTO `system_log` VALUES (805, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=admin, password=******)]', '0:0:0:0:0:0:0:1', 88, '2026-07-15 23:08:45');
INSERT INTO `system_log` VALUES (806, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 23:08:45');
INSERT INTO `system_log` VALUES (807, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 5, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 23:08:45');
INSERT INTO `system_log` VALUES (808, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 12, '2026-07-15 23:08:53');
INSERT INTO `system_log` VALUES (809, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 23:08:54');
INSERT INTO `system_log` VALUES (810, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 23:08:57');
INSERT INTO `system_log` VALUES (811, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 23:08:58');
INSERT INTO `system_log` VALUES (812, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 23:24:51');
INSERT INTO `system_log` VALUES (813, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 23:24:52');
INSERT INTO `system_log` VALUES (814, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 23:24:53');
INSERT INTO `system_log` VALUES (815, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 23:24:54');
INSERT INTO `system_log` VALUES (816, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 23:24:57');
INSERT INTO `system_log` VALUES (817, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 5, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 23:24:57');
INSERT INTO `system_log` VALUES (818, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 23:25:02');
INSERT INTO `system_log` VALUES (819, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 33, '2026-07-15 23:25:03');
INSERT INTO `system_log` VALUES (820, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 23:25:05');
INSERT INTO `system_log` VALUES (821, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 23:25:06');
INSERT INTO `system_log` VALUES (822, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 23:25:12');
INSERT INTO `system_log` VALUES (823, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 23:25:12');
INSERT INTO `system_log` VALUES (824, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 23:25:12');
INSERT INTO `system_log` VALUES (825, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2459, '2026-07-15 23:25:15');
INSERT INTO `system_log` VALUES (826, 1, 'admin', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 23:25:30');
INSERT INTO `system_log` VALUES (827, 1, 'admin', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 23:25:30');
INSERT INTO `system_log` VALUES (828, 1, 'admin', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-15 23:25:32');
INSERT INTO `system_log` VALUES (829, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 23:25:34');
INSERT INTO `system_log` VALUES (830, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 23:25:34');
INSERT INTO `system_log` VALUES (831, 1, 'admin', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 23:25:34');
INSERT INTO `system_log` VALUES (832, 1, 'admin', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 7, '2026-07-15 23:38:39');
INSERT INTO `system_log` VALUES (833, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-15 23:38:39');
INSERT INTO `system_log` VALUES (834, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 23:38:39');
INSERT INTO `system_log` VALUES (835, 1, 'admin', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 23:38:59');
INSERT INTO `system_log` VALUES (836, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-15 23:38:59');
INSERT INTO `system_log` VALUES (837, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 23:38:59');
INSERT INTO `system_log` VALUES (838, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 23:39:01');
INSERT INTO `system_log` VALUES (839, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 23:39:01');
INSERT INTO `system_log` VALUES (840, 1, 'admin', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 23:39:01');
INSERT INTO `system_log` VALUES (841, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 23:39:02');
INSERT INTO `system_log` VALUES (842, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 23:39:02');
INSERT INTO `system_log` VALUES (843, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 23:39:02');
INSERT INTO `system_log` VALUES (844, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 23:39:02');
INSERT INTO `system_log` VALUES (845, 1, 'admin', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 23:39:02');
INSERT INTO `system_log` VALUES (846, 1, 'admin', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-15 23:39:02');
INSERT INTO `system_log` VALUES (847, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 23:39:02');
INSERT INTO `system_log` VALUES (848, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 23:39:02');
INSERT INTO `system_log` VALUES (849, 1, 'admin', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 23:39:14');
INSERT INTO `system_log` VALUES (850, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 23:39:14');
INSERT INTO `system_log` VALUES (851, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 23:39:14');
INSERT INTO `system_log` VALUES (852, 1, 'admin', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 23:44:30');
INSERT INTO `system_log` VALUES (853, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 23:44:30');
INSERT INTO `system_log` VALUES (854, 1, 'admin', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 23:44:30');
INSERT INTO `system_log` VALUES (855, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 167, '2026-07-15 23:54:28');
INSERT INTO `system_log` VALUES (856, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 23:54:29');
INSERT INTO `system_log` VALUES (857, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-15 23:54:29');
INSERT INTO `system_log` VALUES (858, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-15 23:54:29');
INSERT INTO `system_log` VALUES (859, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2432, '2026-07-15 23:54:31');
INSERT INTO `system_log` VALUES (860, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-15 23:57:08');
INSERT INTO `system_log` VALUES (861, 2, 'test', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=有什么适合老人的运动？, city=null)]', '0:0:0:0:0:0:0:1', 2469, '2026-07-15 23:57:25');
INSERT INTO `system_log` VALUES (862, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 00:05:38');
INSERT INTO `system_log` VALUES (863, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 00:05:38');
INSERT INTO `system_log` VALUES (864, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-16 00:05:38');
INSERT INTO `system_log` VALUES (865, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2590, '2026-07-16 00:05:40');
INSERT INTO `system_log` VALUES (866, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 00:05:51');
INSERT INTO `system_log` VALUES (867, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 00:05:55');
INSERT INTO `system_log` VALUES (868, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 00:05:55');
INSERT INTO `system_log` VALUES (869, 2, 'test', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 00:05:55');
INSERT INTO `system_log` VALUES (870, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-16 00:05:58');
INSERT INTO `system_log` VALUES (871, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=伦敦)]', '0:0:0:0:0:0:0:1', 813, '2026-07-16 00:06:01');
INSERT INTO `system_log` VALUES (872, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 00:06:11');
INSERT INTO `system_log` VALUES (873, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 00:06:11');
INSERT INTO `system_log` VALUES (874, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 27, '2026-07-16 00:06:11');
INSERT INTO `system_log` VALUES (875, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 511, '2026-07-16 00:06:11');
INSERT INTO `system_log` VALUES (876, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 00:06:18');
INSERT INTO `system_log` VALUES (877, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 00:06:34');
INSERT INTO `system_log` VALUES (878, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 00:06:34');
INSERT INTO `system_log` VALUES (879, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-16 00:06:34');
INSERT INTO `system_log` VALUES (880, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 502, '2026-07-16 00:06:35');
INSERT INTO `system_log` VALUES (881, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 00:06:39');
INSERT INTO `system_log` VALUES (882, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 00:06:39');
INSERT INTO `system_log` VALUES (883, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-16 00:06:39');
INSERT INTO `system_log` VALUES (884, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 1413, '2026-07-16 00:06:40');
INSERT INTO `system_log` VALUES (885, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 00:06:42');
INSERT INTO `system_log` VALUES (886, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 00:06:44');
INSERT INTO `system_log` VALUES (887, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 00:06:44');
INSERT INTO `system_log` VALUES (888, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-16 00:06:44');
INSERT INTO `system_log` VALUES (889, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 505, '2026-07-16 00:06:44');
INSERT INTO `system_log` VALUES (890, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 00:06:46');
INSERT INTO `system_log` VALUES (891, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 00:06:46');
INSERT INTO `system_log` VALUES (892, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-16 00:06:46');
INSERT INTO `system_log` VALUES (893, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 511, '2026-07-16 00:06:46');
INSERT INTO `system_log` VALUES (894, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-16 00:06:51');
INSERT INTO `system_log` VALUES (895, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-16 00:06:53');
INSERT INTO `system_log` VALUES (896, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 00:07:02');
INSERT INTO `system_log` VALUES (897, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 00:07:06');
INSERT INTO `system_log` VALUES (898, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 00:07:06');
INSERT INTO `system_log` VALUES (899, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-16 00:07:06');
INSERT INTO `system_log` VALUES (900, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 507, '2026-07-16 00:07:07');
INSERT INTO `system_log` VALUES (901, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 00:08:48');
INSERT INTO `system_log` VALUES (902, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 00:08:53');
INSERT INTO `system_log` VALUES (903, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 00:08:53');
INSERT INTO `system_log` VALUES (904, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 00:08:53');
INSERT INTO `system_log` VALUES (905, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-16 00:08:55');
INSERT INTO `system_log` VALUES (906, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2199, '2026-07-16 00:08:56');
INSERT INTO `system_log` VALUES (907, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 00:12:12');
INSERT INTO `system_log` VALUES (908, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 1, '2026-07-16 00:13:39');
INSERT INTO `system_log` VALUES (909, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 00:13:39');
INSERT INTO `system_log` VALUES (910, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-16 00:13:39');
INSERT INTO `system_log` VALUES (911, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 3402, '2026-07-16 00:13:42');
INSERT INTO `system_log` VALUES (912, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 00:23:47');
INSERT INTO `system_log` VALUES (913, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 00:23:47');
INSERT INTO `system_log` VALUES (914, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 13, '2026-07-16 00:23:47');
INSERT INTO `system_log` VALUES (915, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 2443, '2026-07-16 00:23:49');
INSERT INTO `system_log` VALUES (916, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 00:24:06');
INSERT INTO `system_log` VALUES (917, 2, 'test', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 7, '2026-07-16 00:24:06');
INSERT INTO `system_log` VALUES (918, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 00:24:06');
INSERT INTO `system_log` VALUES (919, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 00:24:22');
INSERT INTO `system_log` VALUES (920, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 00:24:38');
INSERT INTO `system_log` VALUES (921, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 15, '2026-07-16 00:24:38');
INSERT INTO `system_log` VALUES (922, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 32, '2026-07-16 00:24:38');
INSERT INTO `system_log` VALUES (923, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 469, '2026-07-16 00:24:38');
INSERT INTO `system_log` VALUES (924, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 142, '2026-07-16 11:12:19');
INSERT INTO `system_log` VALUES (925, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=test, password=******)]', '0:0:0:0:0:0:0:1', 168, '2026-07-16 11:12:29');
INSERT INTO `system_log` VALUES (926, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 9, '2026-07-16 11:12:30');
INSERT INTO `system_log` VALUES (927, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 37, '2026-07-16 11:12:30');
INSERT INTO `system_log` VALUES (928, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 106, '2026-07-16 11:12:30');
INSERT INTO `system_log` VALUES (929, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 1918, '2026-07-16 11:12:32');
INSERT INTO `system_log` VALUES (930, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 15, '2026-07-16 11:12:56');
INSERT INTO `system_log` VALUES (931, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-16 11:15:35');
INSERT INTO `system_log` VALUES (932, 2, 'test', 'PUT /api/reminder/2/complete', 'com.example.elderai.controller.ReminderController.complete', '[2]', '0:0:0:0:0:0:0:1', 7, '2026-07-16 11:15:41');
INSERT INTO `system_log` VALUES (933, 2, 'test', 'DELETE /api/reminder/2', 'com.example.elderai.controller.ReminderController.delete', '[2]', '0:0:0:0:0:0:0:1', 26, '2026-07-16 11:15:46');
INSERT INTO `system_log` VALUES (934, 2, 'test', 'POST /api/reminder/add', 'com.example.elderai.controller.ReminderController.add', '[ReminderDTO(title=运动, content=, remindType=EXERCISE, remindTime=2026-07-16T11:17)]', '0:0:0:0:0:0:0:1', 85, '2026-07-16 11:16:07');
INSERT INTO `system_log` VALUES (935, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-16 11:16:07');
INSERT INTO `system_log` VALUES (936, 2, 'test', 'PUT /api/reminder/3/complete', 'com.example.elderai.controller.ReminderController.complete', '[3]', '0:0:0:0:0:0:0:1', 30, '2026-07-16 11:16:08');
INSERT INTO `system_log` VALUES (937, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-16 11:16:08');
INSERT INTO `system_log` VALUES (938, 2, 'test', 'POST /api/reminder/add', 'com.example.elderai.controller.ReminderController.add', '[ReminderDTO(title=吃药, content=, remindType=MEDICINE, remindTime=2026-07-16T11:22)]', '0:0:0:0:0:0:0:1', 65, '2026-07-16 11:16:33');
INSERT INTO `system_log` VALUES (939, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 11:16:33');
INSERT INTO `system_log` VALUES (940, 2, 'test', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 26, '2026-07-16 11:16:37');
INSERT INTO `system_log` VALUES (941, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 27, '2026-07-16 11:16:37');
INSERT INTO `system_log` VALUES (942, 2, 'test', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-16 11:16:52');
INSERT INTO `system_log` VALUES (943, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 11:16:55');
INSERT INTO `system_log` VALUES (944, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 11:16:55');
INSERT INTO `system_log` VALUES (945, 2, 'test', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 8, '2026-07-16 11:16:55');
INSERT INTO `system_log` VALUES (946, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 1297, '2026-07-16 11:17:00');
INSERT INTO `system_log` VALUES (947, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=纽约)]', '0:0:0:0:0:0:0:1', 389, '2026-07-16 11:17:04');
INSERT INTO `system_log` VALUES (948, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 16, '2026-07-16 11:17:09');
INSERT INTO `system_log` VALUES (949, 2, 'test', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 10, '2026-07-16 11:17:16');
INSERT INTO `system_log` VALUES (950, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 11:17:18');
INSERT INTO `system_log` VALUES (951, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-16 11:17:20');
INSERT INTO `system_log` VALUES (952, 2, 'test', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 7, '2026-07-16 11:17:20');
INSERT INTO `system_log` VALUES (953, 2, 'test', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 15, '2026-07-16 11:17:20');
INSERT INTO `system_log` VALUES (954, 2, 'test', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 383, '2026-07-16 11:17:20');
INSERT INTO `system_log` VALUES (955, 2, 'test', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 11:17:21');
INSERT INTO `system_log` VALUES (956, 2, 'test', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 11:17:21');
INSERT INTO `system_log` VALUES (957, 2, 'test', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 11:17:21');
INSERT INTO `system_log` VALUES (958, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=admin, password=******)]', '0:0:0:0:0:0:0:1', 98, '2026-07-16 11:19:46');
INSERT INTO `system_log` VALUES (959, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=admin, password=******)]', '0:0:0:0:0:0:0:1', 115, '2026-07-16 11:19:54');
INSERT INTO `system_log` VALUES (960, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 24, '2026-07-16 11:19:54');
INSERT INTO `system_log` VALUES (961, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 5, (无法序列化)]', '0:0:0:0:0:0:0:1', 13, '2026-07-16 11:19:54');
INSERT INTO `system_log` VALUES (962, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-16 11:20:06');
INSERT INTO `system_log` VALUES (963, 1, 'admin', 'GET /api/admin/logs', 'com.example.elderai.controller.AdminController.listLogs', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 36, '2026-07-16 11:20:09');
INSERT INTO `system_log` VALUES (964, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 12, '2026-07-16 11:20:25');
INSERT INTO `system_log` VALUES (965, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 5, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-16 11:20:26');
INSERT INTO `system_log` VALUES (966, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 13, '2026-07-16 11:20:28');
INSERT INTO `system_log` VALUES (967, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 8, '2026-07-16 11:20:31');
INSERT INTO `system_log` VALUES (968, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 5, (无法序列化)]', '0:0:0:0:0:0:0:1', 17, '2026-07-16 11:20:31');
INSERT INTO `system_log` VALUES (969, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-16 11:20:53');
INSERT INTO `system_log` VALUES (970, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-16 11:25:33');
INSERT INTO `system_log` VALUES (971, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 11:25:39');
INSERT INTO `system_log` VALUES (972, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 11:25:39');
INSERT INTO `system_log` VALUES (973, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 12, '2026-07-16 11:25:39');
INSERT INTO `system_log` VALUES (974, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 1482, '2026-07-16 11:25:40');
INSERT INTO `system_log` VALUES (975, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 11:25:46');
INSERT INTO `system_log` VALUES (976, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 11:25:46');
INSERT INTO `system_log` VALUES (977, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-16 11:25:46');
INSERT INTO `system_log` VALUES (978, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 395, '2026-07-16 11:25:46');
INSERT INTO `system_log` VALUES (979, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '127.0.0.1', 422, '2026-07-16 11:25:53');
INSERT INTO `system_log` VALUES (980, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 14:31:18');
INSERT INTO `system_log` VALUES (981, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 14:31:18');
INSERT INTO `system_log` VALUES (982, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 23, '2026-07-16 14:31:18');
INSERT INTO `system_log` VALUES (983, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 1439, '2026-07-16 14:31:19');
INSERT INTO `system_log` VALUES (984, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 14:36:01');
INSERT INTO `system_log` VALUES (985, 1, 'admin', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-16 14:36:15');
INSERT INTO `system_log` VALUES (986, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=巴黎)]', '0:0:0:0:0:0:0:1', 1465, '2026-07-16 14:36:20');
INSERT INTO `system_log` VALUES (987, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 14:36:28');
INSERT INTO `system_log` VALUES (988, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 14:36:28');
INSERT INTO `system_log` VALUES (989, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 14:36:28');
INSERT INTO `system_log` VALUES (990, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 432, '2026-07-16 14:36:29');
INSERT INTO `system_log` VALUES (991, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 14:38:36');
INSERT INTO `system_log` VALUES (992, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 14:38:36');
INSERT INTO `system_log` VALUES (993, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-16 14:38:36');
INSERT INTO `system_log` VALUES (994, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 1513, '2026-07-16 14:38:37');
INSERT INTO `system_log` VALUES (995, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-16 14:44:31');
INSERT INTO `system_log` VALUES (996, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 14:44:31');
INSERT INTO `system_log` VALUES (997, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 14, '2026-07-16 14:44:31');
INSERT INTO `system_log` VALUES (998, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 1920, '2026-07-16 14:44:33');
INSERT INTO `system_log` VALUES (999, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 15:04:17');
INSERT INTO `system_log` VALUES (1000, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 15:04:17');
INSERT INTO `system_log` VALUES (1001, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-16 15:04:17');
INSERT INTO `system_log` VALUES (1002, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 1516, '2026-07-16 15:04:19');
INSERT INTO `system_log` VALUES (1003, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 15:04:44');
INSERT INTO `system_log` VALUES (1004, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 15:04:44');
INSERT INTO `system_log` VALUES (1005, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 15:04:44');
INSERT INTO `system_log` VALUES (1006, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 1466, '2026-07-16 15:04:45');
INSERT INTO `system_log` VALUES (1007, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-16 15:28:22');
INSERT INTO `system_log` VALUES (1008, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-16 15:28:22');
INSERT INTO `system_log` VALUES (1009, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 31, '2026-07-16 15:28:22');
INSERT INTO `system_log` VALUES (1010, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 1411, '2026-07-16 15:28:23');
INSERT INTO `system_log` VALUES (1011, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 15:39:18');
INSERT INTO `system_log` VALUES (1012, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 15:39:18');
INSERT INTO `system_log` VALUES (1013, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-16 15:39:18');
INSERT INTO `system_log` VALUES (1014, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 1280, '2026-07-16 15:39:19');
INSERT INTO `system_log` VALUES (1015, 1, 'admin', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=你好, city=null)]', '0:0:0:0:0:0:0:1', 1670, '2026-07-16 15:39:29');
INSERT INTO `system_log` VALUES (1016, 1, 'admin', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=今天有什么新闻, city=null)]', '0:0:0:0:0:0:0:1', 2301, '2026-07-16 15:39:48');
INSERT INTO `system_log` VALUES (1017, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 15:42:01');
INSERT INTO `system_log` VALUES (1018, 1, 'admin', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 15:42:06');
INSERT INTO `system_log` VALUES (1019, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 15:42:07');
INSERT INTO `system_log` VALUES (1020, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 15:42:11');
INSERT INTO `system_log` VALUES (1021, 1, 'admin', 'POST /api/chat/ask', 'com.example.elderai.controller.ChatController.ask', '[ChatRequestDTO(question=今天的天气, city=北京)]', '0:0:0:0:0:0:0:1', 3037, '2026-07-16 15:42:24');
INSERT INTO `system_log` VALUES (1022, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 15:42:31');
INSERT INTO `system_log` VALUES (1023, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 15:42:31');
INSERT INTO `system_log` VALUES (1024, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 8, '2026-07-16 15:42:31');
INSERT INTO `system_log` VALUES (1025, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=北京)]', '0:0:0:0:0:0:0:1', 385, '2026-07-16 15:42:31');
INSERT INTO `system_log` VALUES (1026, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 446, '2026-07-16 15:42:36');
INSERT INTO `system_log` VALUES (1027, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 15:58:56');
INSERT INTO `system_log` VALUES (1028, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-16 15:58:56');
INSERT INTO `system_log` VALUES (1029, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 20, '2026-07-16 15:58:56');
INSERT INTO `system_log` VALUES (1030, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 2476, '2026-07-16 15:58:59');
INSERT INTO `system_log` VALUES (1031, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=马兰西亚)]', '0:0:0:0:0:0:0:1', 2593, '2026-07-16 16:06:30');
INSERT INTO `system_log` VALUES (1032, 1, 'admin', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 16:20:01');
INSERT INTO `system_log` VALUES (1033, 1, 'admin', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 16:20:01');
INSERT INTO `system_log` VALUES (1034, 1, 'admin', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 12, '2026-07-16 16:20:01');
INSERT INTO `system_log` VALUES (1035, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=马兰西亚)]', '0:0:0:0:0:0:0:1', 1698, '2026-07-16 16:20:03');
INSERT INTO `system_log` VALUES (1036, 1, 'admin', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 1294, '2026-07-16 16:20:14');
INSERT INTO `system_log` VALUES (1037, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=admin, password=******)]', '0:0:0:0:0:0:0:1', 216, '2026-07-16 16:21:04');
INSERT INTO `system_log` VALUES (1038, 1, 'admin', 'GET /api/admin/dashboard', 'com.example.elderai.controller.AdminController.getDashboardStats', '[]', '0:0:0:0:0:0:0:1', 26, '2026-07-16 16:21:05');
INSERT INTO `system_log` VALUES (1039, 1, 'admin', 'GET /api/admin/emergency', 'com.example.elderai.controller.AdminController.listEmergency', '[1, 5, (无法序列化)]', '0:0:0:0:0:0:0:1', 27, '2026-07-16 16:21:05');
INSERT INTO `system_log` VALUES (1040, 1, 'admin', 'GET /api/admin/users', 'com.example.elderai.controller.AdminController.listUsers', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 14, '2026-07-16 16:21:07');
INSERT INTO `system_log` VALUES (1041, NULL, NULL, 'POST /api/auth/register', 'com.example.elderai.controller.AuthController.register', '[RegisterDTO(username=张奶奶, password=******, phone=19918546666)]', '0:0:0:0:0:0:0:1', 258, '2026-07-16 16:21:59');
INSERT INTO `system_log` VALUES (1042, NULL, NULL, 'POST /api/auth/login', 'com.example.elderai.controller.AuthController.login', '[LoginDTO(username=张奶奶, password=******)]', '0:0:0:0:0:0:0:1', 116, '2026-07-16 16:22:12');
INSERT INTO `system_log` VALUES (1043, 3, '张奶奶', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 16:22:43');
INSERT INTO `system_log` VALUES (1044, 3, '张奶奶', 'GET /api/emergency/list', 'com.example.elderai.controller.EmergencyController.list', '[]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 16:22:45');
INSERT INTO `system_log` VALUES (1045, 3, '张奶奶', 'GET /api/user/info', 'com.example.elderai.controller.UserController.getUserInfo', '[]', '0:0:0:0:0:0:0:1', 7, '2026-07-16 16:22:45');
INSERT INTO `system_log` VALUES (1046, 3, '张奶奶', 'GET /api/chat-record/list', 'com.example.elderai.controller.ChatRecordController.list', '[1, 10, (无法序列化)]', '0:0:0:0:0:0:0:1', 23, '2026-07-16 16:22:47');
INSERT INTO `system_log` VALUES (1047, 3, '张奶奶', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 16:22:49');
INSERT INTO `system_log` VALUES (1048, 3, '张奶奶', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 16:22:49');
INSERT INTO `system_log` VALUES (1049, 3, '张奶奶', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 16:22:49');
INSERT INTO `system_log` VALUES (1050, 3, '张奶奶', 'POST /api/health/record', 'com.example.elderai.controller.HealthController.add', '[HealthRecordDTO(bloodPressureHigh=null, bloodPressureLow=null, bloodSugar=null, heartRate=null, weight=40, recordDate=2026-07-17, remark=)]', '0:0:0:0:0:0:0:1', 70, '2026-07-16 16:25:17');
INSERT INTO `system_log` VALUES (1051, 3, '张奶奶', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 11, '2026-07-16 16:25:17');
INSERT INTO `system_log` VALUES (1052, 3, '张奶奶', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-16 16:25:17');
INSERT INTO `system_log` VALUES (1053, 3, '张奶奶', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 16:25:19');
INSERT INTO `system_log` VALUES (1054, 3, '张奶奶', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 16:25:20');
INSERT INTO `system_log` VALUES (1055, 3, '张奶奶', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 16:25:21');
INSERT INTO `system_log` VALUES (1056, 3, '张奶奶', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 16:25:22');
INSERT INTO `system_log` VALUES (1057, 3, '张奶奶', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 16:25:24');
INSERT INTO `system_log` VALUES (1058, 3, '张奶奶', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 16:25:24');
INSERT INTO `system_log` VALUES (1059, 3, '张奶奶', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 16:25:24');
INSERT INTO `system_log` VALUES (1060, 3, '张奶奶', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 16:25:37');
INSERT INTO `system_log` VALUES (1061, 3, '张奶奶', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 2, '2026-07-16 16:25:38');
INSERT INTO `system_log` VALUES (1062, 3, '张奶奶', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 6, '2026-07-16 16:25:39');
INSERT INTO `system_log` VALUES (1063, 3, '张奶奶', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 16:25:48');
INSERT INTO `system_log` VALUES (1064, 3, '张奶奶', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 16:25:52');
INSERT INTO `system_log` VALUES (1065, 3, '张奶奶', 'GET /api/health/chart', 'com.example.elderai.controller.HealthController.getChartData', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 16:26:10');
INSERT INTO `system_log` VALUES (1066, 3, '张奶奶', 'GET /api/health/advice', 'com.example.elderai.controller.HealthController.getAdvice', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 16:26:10');
INSERT INTO `system_log` VALUES (1067, 3, '张奶奶', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-16 16:26:10');
INSERT INTO `system_log` VALUES (1068, 3, '张奶奶', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 16:26:16');
INSERT INTO `system_log` VALUES (1069, 3, '张奶奶', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 16:26:16');
INSERT INTO `system_log` VALUES (1070, 3, '张奶奶', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 22, '2026-07-16 16:26:16');
INSERT INTO `system_log` VALUES (1071, 3, '张奶奶', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 1334, '2026-07-16 16:26:17');
INSERT INTO `system_log` VALUES (1072, 3, '张奶奶', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 6, '2026-07-16 17:01:12');
INSERT INTO `system_log` VALUES (1073, 3, '张奶奶', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 14, '2026-07-16 17:01:12');
INSERT INTO `system_log` VALUES (1074, 3, '张奶奶', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 24, '2026-07-16 17:01:12');
INSERT INTO `system_log` VALUES (1075, 3, '张奶奶', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 1316, '2026-07-16 17:01:13');
INSERT INTO `system_log` VALUES (1076, 3, '张奶奶', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 9, '2026-07-16 17:05:27');
INSERT INTO `system_log` VALUES (1077, 3, '张奶奶', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 8, '2026-07-16 17:05:27');
INSERT INTO `system_log` VALUES (1078, 3, '张奶奶', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 22, '2026-07-16 17:05:27');
INSERT INTO `system_log` VALUES (1079, 3, '张奶奶', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 1391, '2026-07-16 17:05:29');
INSERT INTO `system_log` VALUES (1080, 3, '张奶奶', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 1, '2026-07-16 17:05:49');
INSERT INTO `system_log` VALUES (1081, 3, '张奶奶', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 5, '2026-07-16 17:05:49');
INSERT INTO `system_log` VALUES (1082, 3, '张奶奶', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 10, '2026-07-16 17:05:49');
INSERT INTO `system_log` VALUES (1083, 3, '张奶奶', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 1293, '2026-07-16 17:05:50');
INSERT INTO `system_log` VALUES (1084, 3, '张奶奶', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 3, '2026-07-16 17:09:35');
INSERT INTO `system_log` VALUES (1085, 3, '张奶奶', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 4, '2026-07-16 17:09:35');
INSERT INTO `system_log` VALUES (1086, 3, '张奶奶', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 12, '2026-07-16 17:09:35');
INSERT INTO `system_log` VALUES (1087, 3, '张奶奶', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 1405, '2026-07-16 17:09:36');
INSERT INTO `system_log` VALUES (1088, 3, '张奶奶', 'GET /api/health/list', 'com.example.elderai.controller.HealthController.list', '[(无法序列化), (无法序列化)]', '0:0:0:0:0:0:0:1', 7, '2026-07-16 17:13:19');
INSERT INTO `system_log` VALUES (1089, 3, '张奶奶', 'GET /api/reminder/list', 'com.example.elderai.controller.ReminderController.list', '[]', '0:0:0:0:0:0:0:1', 12, '2026-07-16 17:13:19');
INSERT INTO `system_log` VALUES (1090, 3, '张奶奶', 'GET /api/news/list', 'com.example.elderai.controller.NewsController.list', '[1, 6, (无法序列化)]', '0:0:0:0:0:0:0:1', 41, '2026-07-16 17:13:19');
INSERT INTO `system_log` VALUES (1091, 3, '张奶奶', 'GET /api/weather/current', 'com.example.elderai.controller.WeatherController.currentByCity', '[WeatherQueryDTO(city=杭州)]', '0:0:0:0:0:0:0:1', 1273, '2026-07-16 17:13:20');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ELDER' COMMENT '角色：ELDER-老年用户, ADMIN-管理员',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：1-正常, 0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$q0VyY8YUIVPeq.DVqHxcSe/nTGnQTLyfupzwSG3L0/PlzJX1gGnHK', 'ADMIN', NULL, NULL, 1, '2026-07-14 15:24:46', '2026-07-15 19:10:03');
INSERT INTO `user` VALUES (2, 'test', '$2a$10$wOPRMFA7nxK..XDbyDm7MOqT1oIGg88HX5FwkgDg0jYs1DTUTGzoS', 'ELDER', '13800001111', NULL, 1, '2026-07-14 15:24:46', '2026-07-14 15:24:46');
INSERT INTO `user` VALUES (3, '张奶奶', '$2a$10$1oK8vY3bNMlPU7mudQoKLupOc.mJv6JwznQGkBGHgs5qbhxMQinti', 'ELDER', '19918546666', NULL, 1, '2026-07-16 16:21:59', '2026-07-16 16:21:59');

SET FOREIGN_KEY_CHECKS = 1;

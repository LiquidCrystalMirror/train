/*
 Navicat MySQL Dump SQL

 Source Server         : l1
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : sqlprogram

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 25/05/2026 23:06:32
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for refund_info
-- ----------------------------
DROP TABLE IF EXISTS `refund_info`;
CREATE TABLE `refund_info`  (
  `refund_id` int NOT NULL AUTO_INCREMENT COMMENT '退票ID',
  `sale_id` int NOT NULL COMMENT '订单ID',
  `ticket_id` int NOT NULL COMMENT '车票ID',
  `train_id` int NOT NULL COMMENT '车次ID',
  `user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `refund_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '退票时间',
  `refund_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '已完成' COMMENT '退票状态',
  `refund_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '退票备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`refund_id`) USING BTREE,
  INDEX `sale_id`(`sale_id` ASC) USING BTREE,
  INDEX `ticket_id`(`ticket_id` ASC) USING BTREE,
  INDEX `train_id`(`train_id` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `refund_info_ibfk_1` FOREIGN KEY (`sale_id`) REFERENCES `sale_info` (`sale_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `refund_info_ibfk_2` FOREIGN KEY (`ticket_id`) REFERENCES `ticket_info` (`ticket_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `refund_info_ibfk_3` FOREIGN KEY (`train_id`) REFERENCES `train_info` (`train_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `refund_info_ibfk_4` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '退票信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of refund_info
-- ----------------------------

-- ----------------------------
-- Table structure for sale_info
-- ----------------------------
DROP TABLE IF EXISTS `sale_info`;
CREATE TABLE `sale_info`  (
  `sale_id` int NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `ticket_id` int NOT NULL COMMENT '车票ID',
  `train_id` int NOT NULL COMMENT '车次ID',
  `user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `start_station_seq` int NOT NULL COMMENT '上车点对应的站点序号',
  `end_station_seq` int NOT NULL COMMENT '下车点对应的站点序号',
  `sale_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '购票时间',
  `sale_status` enum('已出票','已退票') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '已出票' COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`sale_id`) USING BTREE,
  INDEX `ticket_id`(`ticket_id` ASC) USING BTREE,
  INDEX `train_id`(`train_id` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_start_end_seq`(`start_station_seq` ASC, `end_station_seq` ASC) USING BTREE,
  INDEX `idx_train_start`(`train_id` ASC, `start_station_seq` ASC) USING BTREE,
  CONSTRAINT `sale_info_ibfk_1` FOREIGN KEY (`ticket_id`) REFERENCES `ticket_info` (`ticket_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `sale_info_ibfk_2` FOREIGN KEY (`train_id`) REFERENCES `train_info` (`train_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `sale_info_ibfk_3` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sale_info
-- ----------------------------

-- ----------------------------
-- Table structure for station
-- ----------------------------
DROP TABLE IF EXISTS `station`;
CREATE TABLE `station`  (
  `station_id` int NOT NULL AUTO_INCREMENT COMMENT '站点ID',
  `station_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '站点名称',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`station_id`) USING BTREE,
  UNIQUE INDEX `uk_station_name`(`station_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '站点信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of station
-- ----------------------------
INSERT INTO `station` VALUES (1, '北京南', '2026-05-20 23:33:54');
INSERT INTO `station` VALUES (2, '天津西', '2026-05-20 23:33:54');
INSERT INTO `station` VALUES (3, '济南西', '2026-05-20 23:33:54');
INSERT INTO `station` VALUES (4, '南京南', '2026-05-20 23:33:54');
INSERT INTO `station` VALUES (5, '上海虹桥', '2026-05-20 23:33:54');
INSERT INTO `station` VALUES (6, '杭州东', '2026-05-20 23:33:54');
INSERT INTO `station` VALUES (7, '广州南', '2026-05-20 23:33:54');
INSERT INTO `station` VALUES (8, '深圳北', '2026-05-20 23:33:54');
INSERT INTO `station` VALUES (9, '武汉', '2026-05-20 23:33:54');
INSERT INTO `station` VALUES (10, '成都东', '2026-05-20 23:33:54');

-- ----------------------------
-- Table structure for ticket_info
-- ----------------------------
DROP TABLE IF EXISTS `ticket_info`;
CREATE TABLE `ticket_info`  (
  `ticket_id` int NOT NULL AUTO_INCREMENT COMMENT '车票ID(主键)',
  `train_id` int NOT NULL COMMENT '关联车次ID(外键)',
  `carriage_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车厢号(如1车、二等座01车)',
  `seat_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '座位号(如A1、05号)',
  `seat_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '座位类型(硬座/软座/二等座等)',
  `ticket_status` enum('可售','已售','锁定') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '可售' COMMENT '车票状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `price` decimal(10, 2) NOT NULL COMMENT '售价',
  PRIMARY KEY (`ticket_id`) USING BTREE,
  UNIQUE INDEX `uk_train_seat`(`train_id` ASC, `carriage_number` ASC, `seat_number` ASC) USING BTREE,
  INDEX `idx_train_id`(`train_id` ASC) USING BTREE,
  INDEX `idx_ticket_status`(`ticket_status` ASC) USING BTREE,
  CONSTRAINT `ticket_info_ibfk_1` FOREIGN KEY (`train_id`) REFERENCES `train_info` (`train_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '车票信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of ticket_info
-- ----------------------------
INSERT INTO `ticket_info` VALUES (9, 1, '02', '01A', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 553.00);
INSERT INTO `ticket_info` VALUES (10, 1, '02', '01B', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 553.00);
INSERT INTO `ticket_info` VALUES (11, 1, '02', '01C', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 553.00);
INSERT INTO `ticket_info` VALUES (12, 1, '02', '01D', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 553.00);
INSERT INTO `ticket_info` VALUES (13, 1, '02', '01F', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 553.00);
INSERT INTO `ticket_info` VALUES (14, 1, '02', '02A', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 553.00);
INSERT INTO `ticket_info` VALUES (15, 1, '02', '02B', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 553.00);
INSERT INTO `ticket_info` VALUES (16, 1, '02', '02C', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 553.00);
INSERT INTO `ticket_info` VALUES (17, 1, '02', '02D', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 553.00);
INSERT INTO `ticket_info` VALUES (18, 1, '02', '02F', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 553.00);
INSERT INTO `ticket_info` VALUES (19, 2, '03', '01A', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 450.00);
INSERT INTO `ticket_info` VALUES (20, 2, '03', '01B', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 450.00);
INSERT INTO `ticket_info` VALUES (21, 2, '03', '01C', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 450.00);
INSERT INTO `ticket_info` VALUES (22, 2, '03', '01D', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 450.00);
INSERT INTO `ticket_info` VALUES (23, 2, '03', '01F', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 450.00);
INSERT INTO `ticket_info` VALUES (24, 3, '01', '01A', '一等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 680.00);
INSERT INTO `ticket_info` VALUES (25, 3, '01', '01B', '一等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 680.00);
INSERT INTO `ticket_info` VALUES (26, 3, '01', '01C', '一等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 680.00);
INSERT INTO `ticket_info` VALUES (27, 3, '01', '02A', '一等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 680.00);
INSERT INTO `ticket_info` VALUES (28, 3, '01', '02B', '一等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 680.00);
INSERT INTO `ticket_info` VALUES (29, 4, '05', '01A', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 720.00);
INSERT INTO `ticket_info` VALUES (30, 4, '05', '01B', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 720.00);
INSERT INTO `ticket_info` VALUES (31, 4, '05', '01C', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 720.00);
INSERT INTO `ticket_info` VALUES (32, 4, '05', '01D', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 720.00);
INSERT INTO `ticket_info` VALUES (33, 4, '05', '01F', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 720.00);
INSERT INTO `ticket_info` VALUES (34, 5, '02', '01A', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 890.00);
INSERT INTO `ticket_info` VALUES (35, 5, '02', '01B', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 890.00);
INSERT INTO `ticket_info` VALUES (36, 5, '02', '01C', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 890.00);
INSERT INTO `ticket_info` VALUES (37, 5, '02', '01D', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 890.00);
INSERT INTO `ticket_info` VALUES (38, 5, '02', '01F', '二等座', '可售', '2026-05-20 23:33:54', '2026-05-20 23:33:54', 890.00);

-- ----------------------------
-- Table structure for train_info
-- ----------------------------
DROP TABLE IF EXISTS `train_info`;
CREATE TABLE `train_info`  (
  `train_id` int NOT NULL AUTO_INCREMENT COMMENT '车次ID(主键)',
  `train_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车次编号(如G123)',
  `total_stations` int NOT NULL DEFAULT 0 COMMENT '途径站点数（含起终点）',
  `departure_time` datetime NOT NULL COMMENT '开车时间',
  `arrival_time` datetime NOT NULL COMMENT '到达时间',
  `run_time` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '运行时长(可选)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`train_id`) USING BTREE,
  UNIQUE INDEX `train_number`(`train_number` ASC) USING BTREE,
  INDEX `idx_train_number`(`train_number` ASC) USING BTREE,
  INDEX `idx_departure`(`departure_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '车次信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of train_info
-- ----------------------------
INSERT INTO `train_info` VALUES (5, 'G101', 5, '2026-06-01 08:00:00', '2026-06-01 12:00:00', '4小时', '2026-05-20 23:33:54', '2026-05-20 23:33:54');
INSERT INTO `train_info` VALUES (6, 'G102', 3, '2026-06-01 09:00:00', '2026-06-01 11:30:00', '2.5小时', '2026-05-20 23:33:54', '2026-05-20 23:33:54');
INSERT INTO `train_info` VALUES (7, 'G103', 4, '2026-06-01 10:00:00', '2026-06-01 14:00:00', '4小时', '2026-05-20 23:33:54', '2026-05-20 23:33:54');
INSERT INTO `train_info` VALUES (8, 'D201', 6, '2026-06-01 07:30:00', '2026-06-01 15:30:00', '8小时', '2026-05-20 23:33:54', '2026-05-20 23:33:54');
INSERT INTO `train_info` VALUES (9, 'G205', 5, '2026-06-01 13:00:00', '2026-06-01 18:00:00', '5小时', '2026-05-20 23:33:54', '2026-05-20 23:33:54');

-- ----------------------------
-- Table structure for train_station
-- ----------------------------
DROP TABLE IF EXISTS `train_station`;
CREATE TABLE `train_station`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `train_id` int NOT NULL COMMENT '车次ID',
  `station_seq` int NOT NULL COMMENT '途径点序号（由1开始）',
  `station_id` int NOT NULL COMMENT '站点ID',
  `arrival_time` datetime NULL DEFAULT NULL COMMENT '到达该站点时间',
  `departure_time` datetime NULL DEFAULT NULL COMMENT '离开该站点时间',
  `stay_minutes` int NULL DEFAULT 0 COMMENT '停留分钟数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_train_seq`(`train_id` ASC, `station_seq` ASC) USING BTREE,
  INDEX `idx_train_id`(`train_id` ASC) USING BTREE,
  INDEX `idx_station_id`(`station_id` ASC) USING BTREE,
  INDEX `idx_station_seq`(`station_seq` ASC) USING BTREE,
  CONSTRAINT `fk_train_station_station` FOREIGN KEY (`station_id`) REFERENCES `station` (`station_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_train_station_train` FOREIGN KEY (`train_id`) REFERENCES `train_info` (`train_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '车次途径站点表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of train_station
-- ----------------------------
INSERT INTO `train_station` VALUES (1, 1, 1, 1, '2026-06-01 08:00:00', '2026-06-01 08:00:00', 0, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (2, 1, 2, 2, '2026-06-01 08:45:00', '2026-06-01 08:47:00', 2, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (3, 1, 3, 3, '2026-06-01 10:00:00', '2026-06-01 10:02:00', 2, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (4, 1, 4, 4, '2026-06-01 11:00:00', '2026-06-01 11:02:00', 2, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (5, 1, 5, 5, '2026-06-01 12:00:00', '2026-06-01 12:00:00', 0, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (6, 2, 1, 1, '2026-06-01 09:00:00', '2026-06-01 09:00:00', 0, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (7, 2, 2, 3, '2026-06-01 10:15:00', '2026-06-01 10:17:00', 2, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (8, 2, 3, 5, '2026-06-01 11:30:00', '2026-06-01 11:30:00', 0, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (9, 3, 1, 2, '2026-06-01 10:00:00', '2026-06-01 10:00:00', 0, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (10, 3, 2, 3, '2026-06-01 11:00:00', '2026-06-01 11:02:00', 2, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (11, 3, 3, 4, '2026-06-01 12:30:00', '2026-06-01 12:32:00', 2, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (12, 3, 4, 5, '2026-06-01 14:00:00', '2026-06-01 14:00:00', 0, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (13, 4, 1, 1, '2026-06-01 07:30:00', '2026-06-01 07:30:00', 0, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (14, 4, 2, 2, '2026-06-01 08:15:00', '2026-06-01 08:17:00', 2, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (15, 4, 3, 3, '2026-06-01 09:30:00', '2026-06-01 09:32:00', 2, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (16, 4, 4, 4, '2026-06-01 11:00:00', '2026-06-01 11:05:00', 5, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (17, 4, 5, 5, '2026-06-01 13:00:00', '2026-06-01 13:05:00', 5, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (18, 4, 6, 6, '2026-06-01 15:30:00', '2026-06-01 15:30:00', 0, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (19, 5, 1, 3, '2026-06-01 13:00:00', '2026-06-01 13:00:00', 0, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (20, 5, 2, 4, '2026-06-01 14:30:00', '2026-06-01 14:32:00', 2, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (21, 5, 3, 5, '2026-06-01 15:30:00', '2026-06-01 15:32:00', 2, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (22, 5, 4, 6, '2026-06-01 16:30:00', '2026-06-01 16:32:00', 2, '2026-05-20 23:33:54');
INSERT INTO `train_station` VALUES (23, 5, 5, 7, '2026-06-01 18:00:00', '2026-06-01 18:00:00', 0, '2026-05-20 23:33:54');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `username` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `real_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `role` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('U2344451243', '11', '596d3f37da4af2457e0c9f160abaa0d3', 'k1', '111111111111111111', '18000000000', 'admin', '2026-05-24 23:44:45');

SET FOREIGN_KEY_CHECKS = 1;

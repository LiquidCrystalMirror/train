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

 Date: 26/05/2026 23:53:56
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for carriage_info
-- ----------------------------
DROP TABLE IF EXISTS `carriage_info`;
CREATE TABLE `carriage_info`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `carriage_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `seat_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `seat_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for departure_schedule
-- ----------------------------
DROP TABLE IF EXISTS `departure_schedule`;
CREATE TABLE `departure_schedule`  (
  `train_id` int NOT NULL,
  `train_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `departure_time` datetime NULL DEFAULT NULL,
  `id` int NOT NULL,
  `direction` tinyint NULL DEFAULT NULL,
  PRIMARY KEY (`train_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for price_schedule
-- ----------------------------
DROP TABLE IF EXISTS `price_schedule`;
CREATE TABLE `price_schedule`  (
  `train_id` int NOT NULL,
  `station_count` int NULL DEFAULT NULL,
  `price` double NULL DEFAULT NULL,
  PRIMARY KEY (`train_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
-- Table structure for router_station
-- ----------------------------
DROP TABLE IF EXISTS `router_station`;
CREATE TABLE `router_station`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `router_id` int NOT NULL COMMENT '路线ID',
  `station_seq` int NOT NULL COMMENT '途径点序号（由1开始）',
  `station_id` int NOT NULL COMMENT '站点ID',
  `stay_minutes` int NULL DEFAULT 0 COMMENT '停留分钟数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_train_seq`(`router_id` ASC, `station_seq` ASC) USING BTREE,
  INDEX `idx_train_id`(`router_id` ASC) USING BTREE,
  INDEX `idx_station_id`(`station_id` ASC) USING BTREE,
  INDEX `idx_station_seq`(`station_seq` ASC) USING BTREE,
  CONSTRAINT `fk_train_station_station` FOREIGN KEY (`station_id`) REFERENCES `station` (`station_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_train_station_train` FOREIGN KEY (`router_id`) REFERENCES `train_info` (`train_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '车次途径站点表' ROW_FORMAT = DYNAMIC;

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
  `price` double NOT NULL,
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
-- Table structure for station_connection
-- ----------------------------
DROP TABLE IF EXISTS `station_connection`;
CREATE TABLE `station_connection`  (
  `station_a_id` int NOT NULL COMMENT '站点A ID',
  `station_b_id` int NOT NULL COMMENT '站点B ID',
  `travel_time_minutes` double NOT NULL COMMENT '通行时间（分钟）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `time_consuming` int NULL DEFAULT NULL,
  PRIMARY KEY (`station_a_id`, `station_b_id`) USING BTREE,
  INDEX `idx_station_b`(`station_b_id` ASC) USING BTREE,
  CONSTRAINT `fk_station_a` FOREIGN KEY (`station_a_id`) REFERENCES `station` (`station_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_station_b` FOREIGN KEY (`station_b_id`) REFERENCES `station` (`station_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `chk_a_lt_b` CHECK (`station_a_id` < `station_b_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '站点联通表（无向图）' ROW_FORMAT = DYNAMIC;

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
  `departure_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`ticket_id`) USING BTREE,
  UNIQUE INDEX `uk_train_seat`(`train_id` ASC, `carriage_number` ASC, `seat_number` ASC) USING BTREE,
  INDEX `idx_train_id`(`train_id` ASC) USING BTREE,
  INDEX `idx_ticket_status`(`ticket_status` ASC) USING BTREE,
  CONSTRAINT `ticket_info_ibfk_1` FOREIGN KEY (`train_id`) REFERENCES `train_info` (`train_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '车票信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for train_info
-- ----------------------------
DROP TABLE IF EXISTS `train_info`;
CREATE TABLE `train_info`  (
  `train_id` int NOT NULL AUTO_INCREMENT COMMENT '车次ID(主键)',
  `train_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '车次编号(如G123)',
  `time_consuming` int NULL DEFAULT NULL,
  `router_id` int NULL DEFAULT NULL COMMENT '路线id',
  PRIMARY KEY (`train_id`) USING BTREE,
  UNIQUE INDEX `train_number`(`train_number` ASC) USING BTREE,
  INDEX `idx_train_number`(`train_number` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '车次信息表' ROW_FORMAT = DYNAMIC;

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

SET FOREIGN_KEY_CHECKS = 1;

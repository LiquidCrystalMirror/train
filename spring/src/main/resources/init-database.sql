/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : localhost:3306
 Source Schema         : sqlprogram

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 20/05/2026 23:00:00
 
 说明：此文件包含完整的建表语句和测试数据
 使用方法：删除数据库后直接运行此文件即可
*/

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `sqlprogram` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- 使用数据库
USE `sqlprogram`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `real_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `role` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色：user=普通用户，admin=管理员',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 2. 站点表
-- ----------------------------
DROP TABLE IF EXISTS `station`;
CREATE TABLE `station` (
  `station_id` int NOT NULL AUTO_INCREMENT COMMENT '站点ID',
  `station_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '站点名称',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`station_id`) USING BTREE,
  UNIQUE INDEX `uk_station_name`(`station_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '站点信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 3. 车次信息表
-- ----------------------------
DROP TABLE IF EXISTS `train_info`;
CREATE TABLE `train_info` (
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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '车次信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 4. 车次-站点关联表
-- ----------------------------
DROP TABLE IF EXISTS `train_station`;
CREATE TABLE `train_station` (
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
  CONSTRAINT `fk_train_station_train` FOREIGN KEY (`train_id`) REFERENCES `train_info` (`train_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_train_station_station` FOREIGN KEY (`station_id`) REFERENCES `station` (`station_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '车次途径站点表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 5. 车票信息表
-- ----------------------------
DROP TABLE IF EXISTS `ticket_info`;
CREATE TABLE `ticket_info` (
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
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '车票信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 6. 订单表
-- ----------------------------
DROP TABLE IF EXISTS `sale_info`;
CREATE TABLE `sale_info` (
  `sale_id` int NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `ticket_id` int NOT NULL COMMENT '车票ID',
  `train_id` int NOT NULL COMMENT '车次ID',
  `user_id` int NOT NULL COMMENT '购票用户ID（关联user表）',
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
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 7. 退票信息表
-- ----------------------------
DROP TABLE IF EXISTS `refund_info`;
CREATE TABLE `refund_info` (
  `refund_id` int NOT NULL AUTO_INCREMENT COMMENT '退票ID',
  `sale_id` int NOT NULL COMMENT '订单ID',
  `ticket_id` int NOT NULL COMMENT '车票ID',
  `train_id` int NOT NULL COMMENT '车次ID',
  `user_id` int NOT NULL COMMENT '用户ID',
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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '退票信息表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 测试数据插入
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 批量插入站点数据（10个站点）
-- ----------------------------
INSERT INTO `station` (`station_name`) VALUES 
('北京南'), ('天津西'), ('济南西'), ('南京南'), ('上海虹桥'),
('杭州东'), ('广州南'), ('深圳北'), ('武汉'), ('成都东');

-- ----------------------------
-- 批量插入车次数据（5趟列车）
-- ----------------------------
INSERT INTO `train_info` (`train_number`, `total_stations`, `departure_time`, `arrival_time`, `run_time`) VALUES 
('G101', 5, '2026-06-01 08:00:00', '2026-06-01 12:00:00', '4小时'),
('G102', 3, '2026-06-01 09:00:00', '2026-06-01 11:30:00', '2.5小时'),
('G103', 4, '2026-06-01 10:00:00', '2026-06-01 14:00:00', '4小时'),
('D201', 6, '2026-06-01 07:30:00', '2026-06-01 15:30:00', '8小时'),
('G205', 5, '2026-06-01 13:00:00', '2026-06-01 18:00:00', '5小时');

-- ----------------------------
-- 批量插入车次站点关联数据（23条记录）
-- ----------------------------
INSERT INTO `train_station` (`train_id`, `station_seq`, `station_id`, `arrival_time`, `departure_time`, `stay_minutes`) VALUES 
-- G101：北京南→天津西→济南西→南京南→上海虹桥
(1, 1, 1, '2026-06-01 08:00:00', '2026-06-01 08:00:00', 0),
(1, 2, 2, '2026-06-01 08:45:00', '2026-06-01 08:47:00', 2),
(1, 3, 3, '2026-06-01 10:00:00', '2026-06-01 10:02:00', 2),
(1, 4, 4, '2026-06-01 11:00:00', '2026-06-01 11:02:00', 2),
(1, 5, 5, '2026-06-01 12:00:00', '2026-06-01 12:00:00', 0),
-- G102：北京南→济南西→上海虹桥
(2, 1, 1, '2026-06-01 09:00:00', '2026-06-01 09:00:00', 0),
(2, 2, 3, '2026-06-01 10:15:00', '2026-06-01 10:17:00', 2),
(2, 3, 5, '2026-06-01 11:30:00', '2026-06-01 11:30:00', 0),
-- G103：天津西→济南西→南京南→上海虹桥
(3, 1, 2, '2026-06-01 10:00:00', '2026-06-01 10:00:00', 0),
(3, 2, 3, '2026-06-01 11:00:00', '2026-06-01 11:02:00', 2),
(3, 3, 4, '2026-06-01 12:30:00', '2026-06-01 12:32:00', 2),
(3, 4, 5, '2026-06-01 14:00:00', '2026-06-01 14:00:00', 0),
-- D201：北京南→天津西→济南西→南京南→上海虹桥→杭州东
(4, 1, 1, '2026-06-01 07:30:00', '2026-06-01 07:30:00', 0),
(4, 2, 2, '2026-06-01 08:15:00', '2026-06-01 08:17:00', 2),
(4, 3, 3, '2026-06-01 09:30:00', '2026-06-01 09:32:00', 2),
(4, 4, 4, '2026-06-01 11:00:00', '2026-06-01 11:05:00', 5),
(4, 5, 5, '2026-06-01 13:00:00', '2026-06-01 13:05:00', 5),
(4, 6, 6, '2026-06-01 15:30:00', '2026-06-01 15:30:00', 0),
-- G205：济南西→南京南→上海虹桥→杭州东→广州南
(5, 1, 3, '2026-06-01 13:00:00', '2026-06-01 13:00:00', 0),
(5, 2, 4, '2026-06-01 14:30:00', '2026-06-01 14:32:00', 2),
(5, 3, 5, '2026-06-01 15:30:00', '2026-06-01 15:32:00', 2),
(5, 4, 6, '2026-06-01 16:30:00', '2026-06-01 16:32:00', 2),
(5, 5, 7, '2026-06-01 18:00:00', '2026-06-01 18:00:00', 0);

-- ----------------------------
-- 批量插入用户数据（5个用户，密码都是123456的MD5值）
-- ----------------------------
INSERT INTO `user` (`username`, `password`, `real_name`, `id_card`, `phone`, `role`) VALUES 
('admin', 'e10adc3949ba59abbe56e057f20f883e', '管理员', '11010119900307663X', '13800000000', 'admin'),
('user1', 'e10adc3949ba59abbe56e057f20f883e', '张三', '110101199003076631', '13800000001', 'user'),
('user2', 'e10adc3949ba59abbe56e057f20f883e', '李四', '110101199003076632', '13800000002', 'user'),
('user3', 'e10adc3949ba59abbe56e057f20f883e', '王五', '110101199003076633', '13800000003', 'user'),
('testuser', 'e10adc3949ba59abbe56e057f20f883e', '测试用户', '110101199003076634', '13800000004', 'user');

-- ----------------------------
-- 批量插入车票数据（共30张车票）
-- ----------------------------
INSERT INTO `ticket_info` (`train_id`, `carriage_number`, `seat_number`, `seat_type`, `ticket_status`, `price`) VALUES 
-- G101次列车车票（10张二等座，¥553）
(1, '02', '01A', '二等座', '可售', 553.00),
(1, '02', '01B', '二等座', '可售', 553.00),
(1, '02', '01C', '二等座', '可售', 553.00),
(1, '02', '01D', '二等座', '可售', 553.00),
(1, '02', '01F', '二等座', '可售', 553.00),
(1, '02', '02A', '二等座', '可售', 553.00),
(1, '02', '02B', '二等座', '可售', 553.00),
(1, '02', '02C', '二等座', '可售', 553.00),
(1, '02', '02D', '二等座', '可售', 553.00),
(1, '02', '02F', '二等座', '可售', 553.00),
-- G102次列车车票（5张二等座，¥450）
(2, '03', '01A', '二等座', '可售', 450.00),
(2, '03', '01B', '二等座', '可售', 450.00),
(2, '03', '01C', '二等座', '可售', 450.00),
(2, '03', '01D', '二等座', '可售', 450.00),
(2, '03', '01F', '二等座', '可售', 450.00),
-- G103次列车车票（5张一等座，¥680）
(3, '01', '01A', '一等座', '可售', 680.00),
(3, '01', '01B', '一等座', '可售', 680.00),
(3, '01', '01C', '一等座', '可售', 680.00),
(3, '01', '02A', '一等座', '可售', 680.00),
(3, '01', '02B', '一等座', '可售', 680.00),
-- D201次列车车票（5张二等座，¥720）
(4, '05', '01A', '二等座', '可售', 720.00),
(4, '05', '01B', '二等座', '可售', 720.00),
(4, '05', '01C', '二等座', '可售', 720.00),
(4, '05', '01D', '二等座', '可售', 720.00),
(4, '05', '01F', '二等座', '可售', 720.00),
-- G205次列车车票（5张二等座，¥890）
(5, '02', '01A', '二等座', '可售', 890.00),
(5, '02', '01B', '二等座', '可售', 890.00),
(5, '02', '01C', '二等座', '可售', 890.00),
(5, '02', '01D', '二等座', '可售', 890.00),
(5, '02', '01F', '二等座', '可售', 890.00);

SET FOREIGN_KEY_CHECKS = 1;

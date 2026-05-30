-- ========================================
-- 修复 ticket_info 表唯一约束：uk_train_seat 需包含 departure_time
-- 问题：同一列车多次发车时，车厢座位模板相同，旧唯一键 (train_id, carriage_number, seat_number) 导致冲突
-- 解决：将 departure_time 加入唯一键，允许同一列车不同发车时间拥有相同座位号
-- ========================================

-- 1. 删除旧唯一索引
ALTER TABLE `ticket_info` DROP INDEX `uk_train_seat`;

-- 2. 创建新的唯一索引（加入 departure_time）
ALTER TABLE `ticket_info` 
    ADD UNIQUE INDEX `uk_train_seat`(`train_id` ASC, `carriage_number` ASC, `seat_number` ASC, `departure_time` ASC) USING BTREE;

-- 3. 验证修改结果
SHOW INDEX FROM `ticket_info` WHERE Key_name = 'uk_train_seat';

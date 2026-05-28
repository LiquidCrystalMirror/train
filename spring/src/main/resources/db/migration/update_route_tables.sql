-- 迁移脚本：统一路线相关表的router_id字段类型为BIGINT
-- 背景：router表和router_station表已使用bigint，但train_info等表仍为int

-- 备份原表数据（可选，执行前请确认）
-- CREATE TABLE train_info_backup AS SELECT * FROM train_info;
-- CREATE TABLE train_schedule_watermark_backup AS SELECT * FROM train_schedule_watermark;
-- CREATE TABLE departure_schedule_backup AS SELECT * FROM departure_schedule;

-- 1. 修改train_info表
ALTER TABLE train_info MODIFY COLUMN router_id BIGINT COMMENT '路线ID（关联router表，最后一位表示方向：0=往程，1=返程）';
ALTER TABLE train_info MODIFY COLUMN oppsite_router_id BIGINT COMMENT '反向路线ID';

-- 2. 修改train_schedule_watermark表
ALTER TABLE train_schedule_watermark MODIFY COLUMN route_id BIGINT NOT NULL COMMENT '路线ID（关联router表）';

-- 3. 修改departure_schedule表（移除direction，添加router_id）
ALTER TABLE departure_schedule DROP COLUMN IF EXISTS direction;
ALTER TABLE departure_schedule ADD COLUMN router_id BIGINT COMMENT '路线ID（关联router表，最后一位表示方向：0=往程，1=返程）';
ALTER TABLE departure_schedule ADD INDEX idx_router_id (router_id);

-- 注意：如果表中已有数据，需要先清空或转换现有ID为新的雪花ID格式
-- 建议在执行此脚本前备份数据

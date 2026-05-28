-- ========================================
-- 最终修正脚本：统一所有路线相关字段为BIGINT
-- ========================================

-- 1. departure_schedule表：删除direction字段（已添加router_id）
ALTER TABLE departure_schedule DROP COLUMN IF EXISTS direction;

-- 2. train_schedule_watermark表：route_id改为BIGINT
ALTER TABLE train_schedule_watermark MODIFY COLUMN route_id BIGINT NOT NULL COMMENT '路线ID（关联router表，最后一位表示方向：0=往程，1=返程）';

-- 验证修改结果
SELECT 
    TABLE_NAME, 
    COLUMN_NAME, 
    DATA_TYPE, 
    COLUMN_TYPE,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'sqlprogram' 
    AND TABLE_NAME IN ('departure_schedule', 'train_schedule_watermark', 'router', 'router_station', 'train_info')
    AND COLUMN_NAME LIKE '%route%' OR COLUMN_NAME = 'direction'
ORDER BY TABLE_NAME, ORDINAL_POSITION;

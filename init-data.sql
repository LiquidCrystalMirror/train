-- ============================================
-- 火车票系统初始数据脚本
-- ============================================

-- 1. 初始化站点数据
INSERT INTO station (station_name) VALUES 
('北京站'),
('天津站'),
('济南站'),
('南京站'),
('上海站'),
('杭州站'),
('广州站'),
('深圳站');

-- 2. 初始化站点联通性（相邻站点）
INSERT INTO station_connection (station_a_id, station_b_id, travel_time_minutes) VALUES
(1, 2, 35),   -- 北京-天津 35分钟
(2, 3, 90),   -- 天津-济南 90分钟
(3, 4, 180),  -- 济南-南京 180分钟
(4, 5, 70),   -- 南京-上海 70分钟
(5, 6, 45),   -- 上海-杭州 45分钟
(7, 8, 30);   -- 广州-深圳 30分钟

-- 3. 初始化路线1：京沪高铁（北京→天津→济南→南京→上海）
INSERT INTO router_station (router_id, station_seq, station_id, stay_minutes) VALUES
(1, 1, 1, 10),   -- 北京站，停留10分钟
(1, 2, 2, 5),    -- 天津站，停留5分钟
(1, 3, 3, 8),    -- 济南站，停留8分钟
(1, 4, 4, 6),    -- 南京站，停留6分钟
(1, 5, 5, 0);    -- 上海站，终点站

-- 4. 初始化路线2：沪杭线（上海→杭州）
INSERT INTO router_station (router_id, station_seq, station_id, stay_minutes) VALUES
(2, 1, 5, 10),   -- 上海站，停留10分钟
(2, 2, 6, 0);    -- 杭州站，终点站

-- 5. 初始化路线3：广深线（广州→深圳）
INSERT INTO router_station (router_id, station_seq, station_id, stay_minutes) VALUES
(3, 1, 7, 8),    -- 广州站，停留8分钟
(3, 2, 8, 0);    -- 深圳站，终点站

-- 6. 初始化列车
INSERT INTO train_info (train_number, time_consuming, router_id) VALUES
('G1001', 389, 1),   -- G1001 京沪高铁，总耗时389分钟（含停留）
('G2001', 55, 2),    -- G2001 沪杭线，总耗时55分钟
('G3001', 38, 3);    -- G3001 广深线，总耗时38分钟

-- 7. 初始化车厢模板（二等座示例）
-- 01车，座位号A-F（去掉E）
INSERT INTO carriage_info (carriage_number, seat_number, seat_type) VALUES
('01车', '01A', '二等座'), ('01车', '01B', '二等座'), ('01车', '01C', '二等座'),
('01车', '01D', '二等座'), ('01车', '01F', '二等座'),
('01车', '02A', '二等座'), ('01车', '02B', '二等座'), ('01车', '02C', '二等座'),
('01车', '02D', '二等座'), ('01车', '02F', '二等座'),
('01车', '03A', '二等座'), ('01车', '03B', '二等座'), ('01车', '03C', '二等座'),
('01车', '03D', '二等座'), ('01车', '03F', '二等座');

-- 02车
INSERT INTO carriage_info (carriage_number, seat_number, seat_type) VALUES
('02车', '01A', '二等座'), ('02车', '01B', '二等座'), ('02车', '01C', '二等座'),
('02车', '01D', '二等座'), ('02车', '01F', '二等座'),
('02车', '02A', '二等座'), ('02车', '02B', '二等座'), ('02车', '02C', '二等座'),
('02车', '02D', '二等座'), ('02车', '02F', '二等座');

-- 8. 初始化价格策略
-- G1001 京沪高铁
INSERT INTO price_schedule (train_id, station_count, price) VALUES
(1, 2, 180.0),   -- 2站 180元
(1, 3, 330.0),   -- 3站 330元
(1, 4, 500.0),   -- 4站 500元
(1, 5, 660.0);   -- 5站 660元

-- G2001 沪杭线
INSERT INTO price_schedule (train_id, station_count, price) VALUES
(2, 2, 75.0);    -- 2站 75元

-- G3001 广深线
INSERT INTO price_schedule (train_id, station_count, price) VALUES
(3, 2, 70.0);    -- 2站 70元

-- ============================================
-- 验证数据
-- ============================================

-- 查询所有站点
SELECT * FROM station;

-- 查询路线1的所有站点
SELECT rs.*, s.station_name 
FROM router_station rs 
JOIN station s ON rs.station_id = s.station_id 
WHERE rs.router_id = 1 
ORDER BY rs.station_seq;

-- 查询列车信息
SELECT t.*, r.total_stations 
FROM train_info t 
LEFT JOIN (
    SELECT router_id, COUNT(*) as total_stations 
    FROM router_station 
    GROUP BY router_id
) r ON t.router_id = r.router_id;

-- 查询价格策略
SELECT ps.*, t.train_number 
FROM price_schedule ps 
JOIN train_info t ON ps.train_id = t.train_id;

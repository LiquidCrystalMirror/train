# 发车时间创建功能 - API文档

## 核心逻辑

### 验证流程
1. **查询水位表**：获取该列车最后一次的发车记录（通过 `train_id` 唯一索引）
2. **确定基准时间**：
   - 如果水位表有记录：基准时间 = 最后一次的**到达时间**
   - 如果水位表无记录：基准时间 = **当前时间**
3. **验证时间范围**：
   - 发车时间必须在基准时间**之后**
   - 发车时间必须在当前时间**往后24小时内**
4. **验证方向交替**：
   - 上一次是往程（偶数route_id），这次必须是返程（奇数route_id）
   - 上一次是返程，这次必须是往程
5. **计算到达时间**：到达时间 = 发车时间 + 路线总时长
6. **检查时间冲突**：确保新发车时间与已有计划不重叠（时间段重叠检测）
7. **创建发车计划**：保存到 `departure_schedule` 表（存储**发车时间**）
8. **更新水位表**：
   - 先删除该列车的旧水位记录
   - 再插入新记录（存储**到达时间**）
   - 确保每个列车只有一条最新的水位记录

---

## API接口

### 创建发车计划

**接口**: `POST /api/v1/departure/create`

**请求参数**:
```json
{
  "trainId": 1,
  "departureTime": "2026-05-29T10:00:00",
  "routerId": 100
}
```

**参数说明**:
- `trainId`: 列车ID（必填）
- `departureTime`: 发车时间（必填），ISO 8601格式
- `routerId`: 路线ID（必填），可以是往程或返程

**成功响应**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "success": true,
    "message": "创建成功",
    "scheduleId": 1,
    "arriveTime": "2026-05-29T12:30:00"
  }
}
```

**失败响应示例**:

1. **时间超出范围**:
```json
{
  "code": 400,
  "message": "发车时间必须在当前时间往后24小时内",
  "data": null
}
```

2. **方向未交替**:
```json
{
  "code": 400,
  "message": "发车方向必须与上一次交替（往→返→往）",
  "data": null
}
```

3. **时间在基准时间之前**:
```json
{
  "code": 400,
  "message": "发车时间必须在基准时间(2026-05-29T12:30:00)之后",
  "data": null
}
```

4. **时间冲突**:
```json
{
  "code": 400,
  "message": "该时间段已有车次安排",
  "data": null
}
```

---

## 测试用例

### 测试用例1：首次创建发车计划

**场景**：列车第一次创建发车计划，水位表无记录

**前置准备**：
```sql
-- 确保列车存在
SELECT * FROM train_info WHERE train_id = 1;

-- 清空该列车的水位记录（如果是测试）
DELETE FROM train_schedule_watermark WHERE train_id = 'G100';
```

**请求**:
```bash
curl -X POST http://localhost:8080/api/v1/departure/create \
  -H "Content-Type: application/json" \
  -d '{
    "trainId": 1,
    "departureTime": "2026-05-29T10:00:00",
    "routerId": 100
  }'
```

**预期结果**:
- ✅ 创建成功
- `departure_schedule` 中插入一条记录，`departure_time` = "2026-05-29T10:00:00"
- `train_schedule_watermark` 中插入一条记录，`arrive_time` = 发车时间 + 路线时长

**验证SQL**:
```sql
-- 查看发车计划
SELECT * FROM departure_schedule WHERE train_id = 1;

-- 查看水位表
SELECT * FROM train_schedule_watermark WHERE train_id = 'G100';

-- 应该看到：
-- departure_schedule: 1条记录，departure_time = 2026-05-29 10:00:00
-- train_schedule_watermark: 1条记录，arrive_time = 2026-05-29 12:30:00（假设时长150分钟）
```

---

### 测试用例2：方向交替验证（往→返）

**场景**：上一次是往程（route_id=100），这次创建返程（route_id=101）

**前置条件**:
- 水位表中最后一条记录的 `route_id` = 100（往程）
- 水位表中最后一条记录的 `arrive_time` = "2026-05-29T12:30:00"

**请求**:
```bash
curl -X POST http://localhost:8080/api/v1/departure/create \
  -H "Content-Type: application/json" \
  -d '{
    "trainId": 1,
    "departureTime": "2026-05-29T13:00:00",
    "routerId": 101
  }'
```

**预期结果**:
- ✅ 创建成功（方向交替正确）

---

### 测试用例3：方向未交替（往→往）❌

**场景**：上一次是往程，这次还是往程

**前置条件**:
- 水位表中最后一条记录的 `route_id` = 100（往程）

**请求**:
```bash
curl -X POST http://localhost:8080/api/v1/departure/create \
  -H "Content-Type: application/json" \
  -d '{
    "trainId": 1,
    "departureTime": "2026-05-29T13:00:00",
    "routerId": 100
  }'
```

**预期结果**:
- ❌ 失败，提示"发车方向必须与上一次交替（往→返→往）"

---

### 测试用例4：时间超出24小时范围 ❌

**场景**：发车时间超过当前时间24小时

**请求**:
```bash
curl -X POST http://localhost:8080/api/v1/departure/create \
  -H "Content-Type: application/json" \
  -d '{
    "trainId": 1,
    "departureTime": "2026-06-05T10:00:00",
    "routerId": 100
  }'
```

**预期结果**:
- ❌ 失败，提示"发车时间必须在当前时间往后24小时内"

---

### 测试用例5：时间在基准时间之前 ❌

**场景**：发车时间早于水位表的到达时间

**前置条件**:
- 水位表中最后一条记录的 `arrive_time` = "2026-05-29T12:30:00"

**请求**:
```bash
curl -X POST http://localhost:8080/api/v1/departure/create \
  -H "Content-Type: application/json" \
  -d '{
    "trainId": 1,
    "departureTime": "2026-05-29T11:00:00",
    "routerId": 101
  }'
```

**预期结果**:
- ❌ 失败，提示"发车时间必须在基准时间(2026-05-29T12:30:00)之后"

---

### 测试用例6：时间冲突检测 ❌

**场景**：新发车时间与已有计划重叠

**前置条件**:
- 已有发车计划：10:00 发车，12:30 到达

**请求**:
```bash
curl -X POST http://localhost:8080/api/v1/departure/create \
  -H "Content-Type: application/json" \
  -d '{
    "trainId": 1,
    "departureTime": "2026-05-29T11:00:00",
    "routerId": 101
  }'
```

**预期结果**:
- ❌ 失败，提示"该时间段已有车次安排"

---

## 数据库表说明

### departure_schedule（发车计划表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键（自增） |
| train_id | int | 列车ID |
| train_number | varchar(255) | 车次编号（冗余） |
| departure_time | datetime | **发车时间** |
| router_id | bigint | 路线ID |

**主键**: `(train_id, departure_time)` - 同一列车在同一时间只能有一个发车计划

### train_schedule_watermark（水位表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键（自增） |
| train_id | varchar(20) | 车次编号 |
| route_id | bigint | 路线ID |
| depart_time | datetime | 始发时间 |
| arrive_time | datetime | **终到时间**（用于下次基准时间） |
| updated_by | int | 创建人ID |

**唯一索引**: `uk_train(train_id)` - 每个列车只有一条最新的水位记录

---

## SQL验证示例

### 1. 查看列车的发车计划
```sql
SELECT 
    ds.id,
    ds.train_number,
    ds.departure_time AS '发车时间',
    r.router_name AS '路线名称',
    r.total_duration AS '时长(分钟)',
    DATE_ADD(ds.departure_time, INTERVAL r.total_duration MINUTE) AS '预计到达时间'
FROM departure_schedule ds
INNER JOIN router r ON ds.router_id = r.router_id
WHERE ds.train_id = 1
ORDER BY ds.departure_time ASC;
```

### 2. 查看列车的水位记录
```sql
SELECT 
    tw.train_id AS '车次号',
    tw.route_id AS '路线ID',
    tw.depart_time AS '始发时间',
    tw.arrive_time AS '终到时间',
    TIMESTAMPDIFF(MINUTE, tw.depart_time, tw.arrive_time) AS '实际时长(分钟)'
FROM train_schedule_watermark tw
WHERE tw.train_id = 'G100';
```

### 3. 检查时间段冲突
```sql
-- 检查新计划（10:00-12:30）是否与已有计划冲突
SELECT 
    ds.departure_time AS '已有发车时间',
    DATE_ADD(ds.departure_time, INTERVAL r.total_duration MINUTE) AS '已有到达时间',
    r.router_name AS '路线'
FROM departure_schedule ds
INNER JOIN router r ON ds.router_id = r.router_id
WHERE ds.train_id = 1
AND ds.departure_time < '2026-05-29 12:30:00'  -- 新计划到达时间
AND DATE_ADD(ds.departure_time, INTERVAL r.total_duration MINUTE) > '2026-05-29 10:00:00';  -- 新计划发车时间

-- 如果返回结果，说明有冲突
```

---

## 关键算法说明

### 1. 基准时间计算
```java
LocalDateTime baseTime = (watermark != null && watermark.getArriveTime() != null) 
    ? watermark.getArriveTime()  // 使用水位表的到达时间
    : LocalDateTime.now();       // 使用当前时间
```

### 2. 方向交替判断
```java
boolean lastIsForward = (lastRouterId & 1L) == 0L;  // 偶数=往程
boolean newIsForward = (newRouterId & 1L) == 0L;
return lastIsForward != newIsForward;  // 必须不同
```

### 3. 到达时间计算
```java
LocalDateTime arriveTime = departureTime.plusMinutes(router.getTotalDuration());
```

---

## 注意事项

1. **事务保证**：整个创建过程在事务中执行，任何一步失败都会回滚
2. **水位表作用**：防止同一列车在危险时间段内重复排班
3. **方向交替**：确保列车往返运行，避免单向连续发车
4. **24小时限制**：只允许创建未来24小时内的发车计划，便于管理
5. **时间精度**：所有时间比较精确到秒

# 已删除的Direction相关功能记录

## 删除原因
`direction`字段在`departure_schedule`表中已废弃，往返方向通过`router_id`的最后一位判断：
- `router_id`为偶数 = 往程
- `router_id`为奇数 = 返程

---

## 已删除/修改的内容

### 1. DepartureSchedule实体类
**文件**: `com.example.ticket.entity.DepartureSchedule`

**删除的字段**:
```java
@TableField("direction")
private Integer direction;  // 运行方向: 0-顺行(正序), 1-逆行(逆序)
```

**新增的字段**:
```java
@TableField("router_id")
private Long routerId;  // 路线ID（通过最后一位判断方向）
```

---

### 2. DepartureController
**文件**: `com.example.ticket.controller.DepartureController`

**修改的方法**: `createSchedule()`

**原功能**:
- 接收`direction`参数（0或1）表示列车运行方向
- 将direction设置到DepartureSchedule对象中

**现功能**:
- 接收`routerId`参数（Long类型）
- 通过routerId的最后一位自动判断方向
- 将routerId设置到DepartureSchedule对象中

**删除的参数**:
```java
Integer direction = (Integer) params.get("direction");
```

**新增的参数**:
```java
Long routerId = params.get("routerId") != null ? ((Number) params.get("routerId")).longValue() : null;
```

---

### 3. 数据库表 departure_schedule

**删除的列**:
```sql
ALTER TABLE departure_schedule DROP COLUMN direction;
```

**新增的列**:
```sql
ALTER TABLE departure_schedule ADD COLUMN router_id BIGINT COMMENT '路线ID';
```

---

## 影响范围

### 前端需要调整的地方
1. **车次管理页面** - 创建发车时间时不再传递`direction`参数
2. **车次列表页面** - 显示方向时需要根据`router_id & 1`计算
3. **API调用** - 所有涉及`/api/v1/departure/create`的接口调用需要改为传递`routerId`

### 后端已完成的调整
✅ DepartureSchedule实体类 - routerId改为Long类型  
✅ DepartureController - 使用routerId替代direction  
✅ TrainInfo实体类 - routerId和oppsiteRouterId改为Long类型  
✅ TrainScheduleWatermark实体类 - routeId改为Long类型  
✅ WatermarkController - routeId参数改为Long类型  
✅ TrainScheduleWatermarkService - updateWatermark方法参数改为Long类型  

---

## 方向判断规则

### Java代码示例
```java
// 判断是否为往程（偶数）
boolean isForward = (routerId & 1L) == 0;

// 获取返程路线ID
Long returnRouteId = routerId ^ 1L;

// 获取往程路线ID
Long forwardRouteId = routerId & ~1L;
```

### SQL查询示例
```sql
-- 查询往程路线
SELECT * FROM router WHERE router_id % 2 = 0;

-- 查询返程路线
SELECT * FROM router WHERE router_id % 2 = 1;

-- 根据往程ID获取返程ID
SELECT router_id + 1 AS return_route_id FROM router WHERE router_id = ?;
```

---

## 迁移建议

如果数据库中已有使用direction字段的旧数据，需要执行以下迁移：

```sql
-- 1. 备份数据
CREATE TABLE departure_schedule_backup AS SELECT * FROM departure_schedule;

-- 2. 根据train_info表的router_id更新departure_schedule
UPDATE departure_schedule ds
JOIN train_info ti ON ds.train_id = ti.train_id
SET ds.router_id = ti.router_id;

-- 3. 删除direction字段
ALTER TABLE departure_schedule DROP COLUMN direction;
```

---

## 注意事项

⚠️ **重要提醒**:
1. 所有涉及方向的逻辑都需要改为基于`router_id`的位运算
2. 前端传参时需要确保传递正确的`routerId`（往程或返程）
3. 创建路线时系统会自动生成往返两条路线，ID相差1
4. 水位表、车次表等所有关联route_id的字段都已统一为BIGINT类型

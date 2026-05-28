# 路线ID类型统一修复总结

## 修复内容

### ✅ 已完成的Integer → Long类型转换

#### 1. 实体类 (Entity)
- ✅ `Router.routerId` - Integer → Long
- ✅ `RouterStation.routerId` - Integer → Long  
- ✅ `TrainInfo.routerId` - Integer → Long
- ✅ `TrainInfo.oppsiteRouterId` - Integer → Long
- ✅ `TrainScheduleWatermark.routeId` - Integer → Long
- ✅ `DepartureSchedule.routerId` - 新增Long类型字段（替代direction）

#### 2. Controller层
- ✅ `WatermarkController.updateWatermark()` - routeId参数改为Long
- ✅ `DepartureController.createSchedule()` - 使用routerId替代direction

#### 3. Service层
- ✅ `TrainScheduleWatermarkService.updateWatermark()` - routeId参数改为Long
- ✅ `TrainScheduleWatermarkServiceImpl.updateWatermark()` - 实现改为Long

#### 4. Mapper层
- ✅ `RouterMapper` - 所有方法支持Long类型
- ✅ `RouterStationMapper` - 所有方法支持Long类型
- ✅ `StationConnectionMapper.selectByStationIds()` - 新增方法

---

## 🗑️ 已删除的Direction相关功能

### 删除的文件/方法

**无完全删除的文件**，仅修改了以下内容：

1. **DepartureSchedule实体类**
   - ❌ 删除字段：`private Integer direction`
   - ✅ 新增字段：`private Long routerId`

2. **DepartureController.createSchedule()方法**
   - ❌ 删除参数接收：`Integer direction = (Integer) params.get("direction")`
   - ❌ 删除参数校验：`direction == null`
   - ❌ 删除字段设置：`schedule.setDirection(direction)`
   - ✅ 新增参数接收：`Long routerId = ... params.get("routerId")`
   - ✅ 新增字段设置：`schedule.setRouterId(routerId)`

---

## 📋 数据库迁移脚本

执行以下SQL完成数据库结构调整：

```sql
-- ========================================
-- 最终修正脚本：统一所有路线相关字段为BIGINT
-- ========================================

-- 1. departure_schedule表：删除direction字段
ALTER TABLE departure_schedule DROP COLUMN IF EXISTS direction;

-- 2. train_schedule_watermark表：route_id改为BIGINT
ALTER TABLE train_schedule_watermark MODIFY COLUMN route_id BIGINT NOT NULL 
    COMMENT '路线ID（关联router表，最后一位表示方向：0=往程，1=返程）';

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
    AND (COLUMN_NAME LIKE '%route%' OR COLUMN_NAME = 'direction')
ORDER BY TABLE_NAME, ORDINAL_POSITION;
```

---

## 🎯 核心设计原则

### 雪花ID方案
- **往程路线ID**: 偶数（最后一位为0）
- **返程路线ID**: 奇数（最后一位为1）
- **往返关系**: 返程ID = 往程ID + 1

### 方向判断
```java
// Java代码
boolean isForward = (routerId & 1L) == 0;  // true=往程，false=返程
Long returnRouteId = routerId ^ 1L;         // 获取对应方向的ID
```

```sql
-- SQL查询
SELECT * FROM router WHERE router_id % 2 = 0;  -- 往程
SELECT * FROM router WHERE router_id % 2 = 1;  -- 返程
```

---

## 📝 详细删除记录

完整的删除记录请查看：[DIRECTION_REMOVAL_RECORD.md](./DIRECTION_REMOVAL_RECORD.md)

包含：
- 删除原因说明
- 每个修改点的原功能描述
- 影响范围分析
- 前端调整建议
- 数据迁移方案

---

## ✨ 新功能特性

### 自动创建往返路线
调用 `/api/v1/route/create` 接口时：
1. 自动生成往程路线（ID为偶数）
2. 自动生成返程路线（ID为奇数）
3. 返程站点顺序自动反转
4. 自动计算两条路线的总时长

### API测试
详见：[ROUTE_API_TEST.md](./ROUTE_API_TEST.md)

---

## ⚠️ 注意事项

1. **所有route_id相关字段已统一为BIGINT/Long类型**
2. **direction字段已完全废弃，不再使用**
3. **前端需要适配新的API参数（routerId替代direction）**
4. **数据库需要执行迁移脚本完成结构调整**
5. **方向判断统一使用位运算：`routerId & 1L`**

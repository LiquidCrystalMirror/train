# 路线管理API测试

## 前置条件

1. 执行数据库修正脚本：
```bash
mysql -u root -p sqlprogram < C:\Users\Administrator\IdeaProjects\train\spring\src\main\resources\db\migration\final_fix.sql
```

2. 确保数据库中已有站点数据（station表）和站点联通数据（station_connection表）

---

## API测试：创建路线（自动生成往返）

### 请求信息
- **URL**: `POST http://localhost:8080/api/v1/route/create`
- **Content-Type**: `application/json`

### 请求示例

```json
{
  "routerName": "北京-上海",
  "stations": [
    {
      "stationSeq": 1,
      "stationId": 1,
      "stayMinutes": 5
    },
    {
      "stationSeq": 2,
      "stationId": 2,
      "stayMinutes": 3
    },
    {
      "stationSeq": 3,
      "stationId": 3,
      "stayMinutes": 0
    }
  ]
}
```

### 预期响应

```json
{
  "code": 200,
  "message": "创建成功，已自动生成往程和返程路线",
  "data": 1234567890
}
```

**说明**：
- `data` 返回的是往程路线ID（偶数）
- 系统会自动创建返程路线，ID为 `往程ID + 1`（奇数）
- 例如：往程ID = 1234567890，返程ID = 1234567891

---

## 验证结果

### 1. 查询路线列表
```bash
GET http://localhost:8080/api/v1/route/list
```

应该能看到两条路线：
- `北京-上海（往）` - ID为偶数
- `北京-上海（返）` - ID为奇数

### 2. 查询路线详情
```bash
GET http://localhost:8080/api/v1/route/detail?routerId=1234567890
```

### 3. 查询往返路线对
```bash
GET http://localhost:8080/api/v1/route/pair?routerId=1234567890
```

---

## 雪花ID判断规则

```java
// 判断是否为往程（偶数）
boolean isForward = (routeId & 1L) == 0;

// 获取返程ID
Long returnRouteId = routeId ^ 1L;

// 获取往程ID
Long forwardRouteId = routeId & ~1L;
```

---

## 注意事项

1. **站点必须存在**：stations中的stationId必须在station表中存在
2. **站点必须联通**：相邻站点之间必须在station_connection表中有联通关系
3. **自动计算时长**：系统会根据站点联通表的travel_time_minutes自动计算total_duration
4. **自动反转站点**：返程路线的站点顺序会自动反转（A→B→C 变成 C→B→A）

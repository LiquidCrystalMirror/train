# 全部接口测试文档

> 基础URL: `http://localhost:8080`

---

## 1. TrainController

### 添加列车（自动创建往返） `❌ 单个`

```bash
curl -X POST http://localhost:8080/api/v1/train/add \
  -H "Content-Type: application/json" \
  -d '{"trainNumber":"G1234","routerId":100}'
```

### 更新列车 `❌ 单个`

```bash
curl -X POST http://localhost:8080/api/v1/train/update \
  -H "Content-Type: application/json" \
  -d '{"trainId":1,"trainNumber":"G5678","routerId":102}'
```

### 删除列车 `❌ 单个`

```bash
curl -X POST http://localhost:8080/api/v1/train/delete \
  -H "Content-Type: application/json" \
  -d '{"id":1}'
```

### 根据车次号查询 `📋 查询`

```bash
curl -X POST http://localhost:8080/api/v1/train/query/number \
  -H "Content-Type: application/json" \
  -d '{"number":"G1234"}'
```

### 分页查询列车列表 `📋 查询`

```bash
curl -X POST http://localhost:8080/api/v1/train/list \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"find":"G"}'
```

---

## 2. StationController

### 添加车站 `❌ 单个`

```bash
curl -X POST http://localhost:8080/api/v1/station/add \
  -H "Content-Type: application/json" \
  -d '{"stationName":"北京南"}'
```

### 删除车站 `❌ 单个`

```bash
curl -X DELETE http://localhost:8080/api/v1/station/1
```

### 更新车站 `❌ 单个`

```bash
curl -X PUT http://localhost:8080/api/v1/station/update \
  -H "Content-Type: application/json" \
  -d '{"stationId":1,"stationName":"北京丰台"}'
```

### 查询所有车站 `📋 查询`

```bash
curl -X GET http://localhost:8080/api/v1/station/list
```

### 根据ID查询车站 `📋 查询`

```bash
curl -X GET http://localhost:8080/api/v1/station/1
```

### 模糊搜索车站 `📋 查询`

```bash
curl -X GET "http://localhost:8080/api/v1/station/search?name=北京"
```

### 添加车站连通关系 `❌ 单个`

```bash
curl -X POST http://localhost:8080/api/v1/station/connection/add \
  -H "Content-Type: application/json" \
  -d '{"stationAId":1,"stationBId":2,"travelTimeMinutes":30.5}'
```

### 删除连通关系 `❌ 单个`

```bash
curl -X DELETE http://localhost:8080/api/v1/station/connection/1/2
```

### 查询邻接车站 `📋 查询`

```bash
curl -X GET http://localhost:8080/api/v1/station/1/neighbors
```

### 检查两站是否连通 `📋 查询`

```bash
curl -X GET http://localhost:8080/api/v1/station/check/connection/1/2
```

---

## 3. StatsController

### 系统统计 `📊 查询`

```bash
curl -X GET http://localhost:8080/api/v1/stats/system
```

### 用户统计 `📊 查询`

```bash
curl -X GET http://localhost:8080/api/v1/stats/users
```

### 车次统计 `📊 查询`

```bash
curl -X GET http://localhost:8080/api/v1/stats/trains
```

### 车票统计 `📊 查询`

```bash
curl -X GET http://localhost:8080/api/v1/stats/tickets
```

> 注：返回数据中 `availableCount` 对应状态为 `可售` 的车票数，`soldCount` 对应状态为 `已售` 的车票数。

### 订单统计 `📊 查询`

```bash
curl -X GET http://localhost:8080/api/v1/stats/orders
```

### 用户购票记录（根据用户ID） `📊 查询`

```bash
curl -X GET http://localhost:8080/api/v1/sale/user/user123
```

### 用户退票记录 `📊 查询`

```bash
curl -X GET http://localhost:8080/api/v1/refund/user/user123
```

### 分页查询用户购票记录 `📊 查询`

```bash
curl -X POST http://localhost:8080/api/v1/sale/user/page \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"pageSize":10,"userId":"user123"}'
```

### 分页查询用户退票记录 `📊 查询`

```bash
curl -X POST http://localhost:8080/api/v1/refund/user/page \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"pageSize":10,"userId":"user123"}'
```

### 管理员查询所有售票记录（分页） `📊 查询`

```bash
curl -X POST http://localhost:8080/api/v1/sale/list \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"pageSize":10}'
```

### 管理员查询已售票聚合（含车票详情、用户ID、票价） `📊 查询`

```bash
curl -X POST http://localhost:8080/api/v1/sale/sold/page \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"pageSize":10}'
```

> 返回字段：saleId, userId, price, saleTime, saleStatus, startStationSeq, endStationSeq, ticketId, trainId, trainNumber, carriageNumber, seatNumber, seatType, ticketStatus, departureTime。一次 JOIN 三表（sale_info + ticket_info + train_info）。

### 根据车次ID查询售票记录 `📊 查询`

```bash
curl -X GET http://localhost:8080/api/v1/sale/train/1
```

### 根据车票ID查询售票记录 `📊 查询`

```bash
curl -X GET http://localhost:8080/api/v1/sale/ticket/100
```

### 管理员查询所有退票记录（分页） `📊 查询`

```bash
curl -X POST http://localhost:8080/api/v1/refund/list \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"pageSize":10}'
```

### 根据售票记录ID查询退票记录 `📊 查询`

```bash
curl -X GET http://localhost:8080/api/v1/refund/sale/200
```

---

## 4. TicketBatchController

### 批量生成车票（带水位表检查） `✅ 批量`

```bash
curl -X POST http://localhost:8080/api/v1/ticket/batch/generate \
  -H "Content-Type: application/json" \
  -d '{"trainId":1,"departureTime":"2025-06-01T10:00:00","adminId":1}'
```

> 注：`adminId` 为整数类型的管理员ID。

---

## 5. TicketController — 车票管理

### 分页查询车票 `📋 查询`

```bash
curl -X POST http://localhost:8080/api/v1/ticket/list \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"find":"G"}'
```

> 注：`find` 参数用于按车次号模糊搜索。

### 根据火车ID查询车票 `📋 查询`

```bash
curl -X POST http://localhost:8080/api/v1/ticket/train \
  -H "Content-Type: application/json" \
  -d '{"trainId":1}'
```

> 注：添加、修改、删除车票接口已被注释，车票通过批量生成接口统一创建。

---

## 5b. TicketQueryController — 票务聚合查询

### 查询库存（按车次+发车时间，返回座位类型聚合） `📋 查询`

```bash
curl -X POST http://localhost:8080/api/v1/ticket/inventory \
  -H "Content-Type: application/json" \
  -d '{"trainId":1,"departureTime":"2025-06-01T10:00:00"}'
```

> 返回 `TicketInventory` 列表：trainId, departureTime, seatType, totalCount, soldCount, remainingCount。用于用户端余票展示。

### 根据车票ID查上下车站详情 `📋 查询`

```bash
curl -X POST http://localhost:8080/api/v1/ticket/station-detail \
  -H "Content-Type: application/json" \
  -d '{"ticketId":100}'
```

> 返回：ticketId, trainId, trainNumber, departureTime, seatType, seatTypeName, carriageNumber, seatNumber, startStation{stationId,stationName,seq}, endStation{stationId,stationName,seq}。
> 从 ticketId → SaleInfo → TrainInfo → RouterStation → Station 全链路查询。

### 根据车次ID查完整路线（含站点名称） `📋 查询`

```bash
curl -X POST http://localhost:8080/api/v1/ticket/train-route \
  -H "Content-Type: application/json" \
  -d '{"trainId":1}'
```

> 返回：trainId, trainNumber, routerId, routerName, isForward, stations[{stationId,stationName,seq,stayMinutes}]。

---

## 6. UserController

### 统计用户数量（按角色） `📊 查询`

```bash
curl -X GET http://localhost:8080/api/v1/g/allTotal
```

### 分页查询用户 `📋 查询`

```bash
curl -X POST http://localhost:8080/api/v1/g/user \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"find":"张"}'
```

### 查询所有用户 `📋 查询`

```bash
curl -X GET http://localhost:8080/api/v1/g/users
```

### 登录 `🔐 认证`

```bash
curl -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{"account":"zhangsan","password":"123456"}'
```

### 用户注册 `❌ 单个`

```bash
curl -X POST http://localhost:8080/api/v1/reg \
  -H "Content-Type: application/json" \
  -d '{"username":"lisi","password":"123456","realName":"李四","idCard":"11010119900307663X","phone":"13800000000"}'
```

### 管理员注册 `❌ 单个`

```bash
curl -X POST http://localhost:8080/api/v1/admin/reg \
  -H "Content-Type: application/json" \
  -d '{"username":"admin1","password":"admin123","realName":"管理员","phone":"13900000000"}'
```

### 用户修改自身信息（需登录，携带 token） `❌ 单个`

```bash
curl -X PUT http://localhost:8080/api/v1/user/edit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your_jwt_token>" \
  -d '{"realName":"李四改","phone":"13911111111"}'
```

### 管理员修改用户信息 `❌ 单个`

```bash
curl -X PUT http://localhost:8080/api/v1/admin/user/update \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{"userId":"user123","realName":"王五"}'
```

### 管理员删除用户 `❌ 单个`

```bash
curl -X DELETE http://localhost:8080/api/v1/admin/user/{userId} \
  -H "Authorization: Bearer <admin_token>"
```

> 注：路径参数 `{userId}` 为字符串类型的用户ID（如 `abc123`），不是数字。

---

## 7. WatermarkController

### 查询列车最新水位记录 `📋 查询`

```bash
curl -X POST http://localhost:8080/api/v1/watermark/latest \
  -H "Content-Type: application/json" \
  -d '{"trainId":"1"}'
```

### 检查时间段是否危险 `📋 查询`

```bash
curl -X POST http://localhost:8080/api/v1/watermark/check/danger \
  -H "Content-Type: application/json" \
  -d '{"trainId":"1","startTime":"2025-06-01T10:00:00","endTime":"2025-06-01T12:00:00"}'
```

### 获取可出票的最早时间 `📋 查询`

```bash
curl -X POST http://localhost:8080/api/v1/watermark/earliest/ticket/time \
  -H "Content-Type: application/json" \
  -d '{"trainId":"1"}'
```

### 更新水位记录（发行车票后调用） `❌ 单个`

```bash
curl -X POST http://localhost:8080/api/v1/watermark/update \
  -H "Content-Type: application/json" \
  -d '{"trainId":"1","routeId":100,"departTime":"2025-06-01T10:00:00","arriveTime":"2025-06-01T15:00:00","updatedBy":1}'
```

---

## 8. DepartureController

### 查询指定列车的所有发车时间 `📋 查询`

```bash
curl -X POST http://localhost:8080/api/v1/departure/list \
  -H "Content-Type: application/json" \
  -d '{"trainId":1}'
```

### 根据时间范围查询车次 `📋 查询`

```bash
curl -X POST http://localhost:8080/api/v1/departure/query/timeRange \
  -H "Content-Type: application/json" \
  -d '{"startTime":"2025-06-01T00:00:00","endTime":"2025-06-30T23:59:59"}'
```

### 创建发车时间表 `❌ 单个`

```bash
curl -X POST http://localhost:8080/api/v1/departure/create \
  -H "Content-Type: application/json" \
  -d '{"trainId":1,"departureTime":"2025-06-15T08:00:00","routerId":100}'
```

### 删除发车时间表 `❌ 单个`

```bash
curl -X POST http://localhost:8080/api/v1/departure/delete \
  -H "Content-Type: application/json" \
  -d '{"id":10}'
```

### 查询直达车次（起止站、时间分页） `📋 查询`

```bash
curl -X POST http://localhost:8080/api/v1/departure/queryByStations \
  -H "Content-Type: application/json" \
  -d '{"startStationId":1,"endStationId":5,"startTime":"2025-06-01T10:00:00","pageNum":1,"pageSize":10}'
```

---

## 9. RefundController

### 退票 `❌ 单个`

```bash
curl -X POST http://localhost:8080/api/v1/refund/do \
  -H "Content-Type: application/json" \
  -d '{"saleId":100}'
```

---

## 10. RouteController

### 创建路线（自动生成往返） `🟡 部分批量（stations数组）`

```bash
curl -X POST http://localhost:8080/api/v1/route/create \
  -H "Content-Type: application/json" \
  -d '{
    "routerName":"京沪线",
    "stations":[
      {"stationSeq":1,"stationId":1,"stayMinutes":5},
      {"stationSeq":2,"stationId":2,"stayMinutes":10},
      {"stationSeq":3,"stationId":5,"stayMinutes":0}
    ]
  }'
```

### 更新路线 `🟡 部分批量（stations数组）`

```bash
curl -X POST http://localhost:8080/api/v1/route/update \
  -H "Content-Type: application/json" \
  -d '{
    "routerId":100,
    "routerName":"京沪高铁",
    "stations":[
      {"stationSeq":1,"stationId":1,"stayMinutes":5},
      {"stationSeq":2,"stationId":2,"stayMinutes":8},
      {"stationSeq":3,"stationId":5,"stayMinutes":0}
    ]
  }'
```

### 删除路线 `❌ 单个`

```bash
curl -X POST http://localhost:8080/api/v1/route/delete \
  -H "Content-Type: application/json" \
  -d '{"routerId":100}'
```

### 获取所有路线列表 `📋 查询`

```bash
curl -X GET http://localhost:8080/api/v1/route/list
```

### 获取路线详情（含站点） `📋 查询`

```bash
curl -X GET "http://localhost:8080/api/v1/route/detail?routerId=100"
```

### 获取往返路线对 `📋 查询`

```bash
curl -X GET "http://localhost:8080/api/v1/route/pair?routerId=100"
```

### 查询指定路线的所有站点 `📋 查询`

```bash
curl -X GET "http://localhost:8080/api/v1/route/stations?routerId=100"
```

---

## 11. SaleController

### 售票（需登录，携带 token） `❌ 单个`

**模式一：指定 ticketId（管理员/旧流程）**

```bash
curl -X POST http://localhost:8080/api/v1/sale/do \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your_jwt_token>" \
  -d '{
    "ticketId": 100,
    "trainId": 1,
    "startStationSeq": 1,
    "endStationSeq": 3
  }'
```

**模式二：按座位类型随机选票购票（用户端）**

```bash
curl -X POST http://localhost:8080/api/v1/sale/do \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your_jwt_token>" \
  -d '{
    "trainId": 1,
    "departureTime": "2025-06-01T10:00:00",
    "seatType": 0,
    "startStationSeq": 1,
    "endStationSeq": 3
  }'
```

> 注：模式二 `seatType` 编码：0=二等座、1=一等座、2=商务座。后端自动随机分配一张该类型的可售票。`price` 由后台根据票价策略和座位类型自动计算，无需手动传入。

### 计算票价（需登录，携带 token） `📋 查询`

```bash
curl -X POST "http://localhost:8080/api/v1/sale/calculate-price?trainId=1&ticketId=100&startStationSeq=1&endStationSeq=3" \
  -H "Authorization: Bearer <your_jwt_token>"
```

---

## 12. PriceScheduleController

### 查询某车次的价格梯度列表 `📋 查询`

```bash
curl -X POST http://localhost:8080/api/v1/price/list \
  -H "Content-Type: application/json" \
  -d '{"trainId":1}'
```

### 设置单个价格 `❌ 单个`

```bash
curl -X POST http://localhost:8080/api/v1/price/set \
  -H "Content-Type: application/json" \
  -d '{"trainId":1,"stationCount":3,"price":150.0}'
```

### 批量设置价格梯度 `🟡 批量`

```bash
curl -X POST http://localhost:8080/api/v1/price/batch \
  -H "Content-Type: application/json" \
  -d '{
    "trainId": 1,
    "priceList": [
      {"stationCount":1,"price":50.0},
      {"stationCount":2,"price":100.0},
      {"stationCount":3,"price":150.0}
    ]
  }'
```

### 检查价格梯度是否完整 `📋 查询`

```bash
curl -X POST http://localhost:8080/api/v1/price/check-complete \
  -H "Content-Type: application/json" \
  -d '{"trainId":1,"totalStationCount":5}'
```
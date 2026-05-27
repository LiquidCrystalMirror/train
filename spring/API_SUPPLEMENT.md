# 后端API接口补充说明

## 新增Controller: StatsController.java

### 文件位置
`spring/src/main/java/com/example/ticket/controller/StatsController.java`

## 新增的API接口列表

### 1. 统计接口

#### 1.1 获取系统统计数据
- **接口**: `GET /api/v1/stats/system`
- **返回数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "userCount": 100,
    "trainCount": 50,
    "ticketCount": 5000,
    "orderCount": 1000
  }
}
```

#### 1.2 获取用户统计
- **接口**: `GET /api/v1/stats/users`
- **返回数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "totalCount": 100,
    "adminCount": 5,
    "userCount": 95
  }
}
```

#### 1.3 获取车次统计
- **接口**: `GET /api/v1/stats/trains`
- **返回数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "totalCount": 50,
    "withRouteCount": 45
  }
}
```

#### 1.4 获取车票统计
- **接口**: `GET /api/v1/stats/tickets`
- **返回数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "totalCount": 5000,
    "availableCount": 3000,
    "soldCount": 2000
  }
}
```

#### 1.5 获取订单统计
- **接口**: `GET /api/v1/stats/orders`
- **返回数据**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "totalCount": 1000,
    "issuedCount": 950,
    "refundedCount": 50
  }
}
```

### 2. 用户订单接口

#### 2.1 查询用户的购票记录
- **接口**: `GET /api/v1/sale/user/{userId}`
- **参数**: userId (路径参数)
- **返回**: 该用户的所有购票记录列表

#### 2.2 查询用户的退票记录
- **接口**: `GET /api/v1/refund/user/{userId}`
- **参数**: userId (路径参数)
- **返回**: 该用户的所有退票记录列表

#### 2.3 分页查询用户的购票记录
- **接口**: `POST /api/v1/sale/user/page`
- **请求体**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "userId": "U001"
}
```
- **返回**: 分页的购票记录

#### 2.4 分页查询用户的退票记录
- **接口**: `POST /api/v1/refund/user/page`
- **请求体**:
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "userId": "U001"
}
```
- **返回**: 分页的退票记录

### 3. 售票列表接口（管理员）

#### 3.1 查询所有售票记录
- **接口**: `POST /api/v1/sale/list`
- **请求体**:
```json
{
  "pageNum": 1,
  "pageSize": 10
}
```
- **返回**: 分页的所有售票记录

#### 3.2 根据车次ID查询售票记录
- **接口**: `GET /api/v1/sale/train/{trainId}`
- **参数**: trainId (路径参数)
- **返回**: 该车次的所有售票记录

#### 3.3 根据车票ID查询售票记录
- **接口**: `GET /api/v1/sale/ticket/{ticketId}`
- **参数**: ticketId (路径参数)
- **返回**: 该车票的所有售票记录

### 4. 退票列表接口（管理员）

#### 4.1 查询所有退票记录
- **接口**: `POST /api/v1/refund/list`
- **请求体**:
```json
{
  "pageNum": 1,
  "pageSize": 10
}
```
- **返回**: 分页的所有退票记录

#### 4.2 根据售票记录ID查询退票记录
- **接口**: `GET /api/v1/refund/sale/{saleId}`
- **参数**: saleId (路径参数)
- **返回**: 该售票记录对应的退票记录

## 依赖的Mapper

StatsController 使用了以下Mapper：
- `UserMapper` - 已存在
- `TrainInfoMapper` - 已存在
- `TicketInfoMapper` - 已存在
- `SaleInfoMapper` - 已存在
- `RefundInfoMapper` - 已存在

## 前端对应的API调用

前端 `ExtraApi.js` 中的以下函数现在已经可以正常工作：

1. ✅ `getSystemStats()` - 获取系统统计数据
2. ✅ `getUserStats()` - 获取用户统计
3. ✅ `getTrainStats()` - 获取车次统计
4. ✅ `getTicketStats()` - 获取车票统计
5. ✅ `getOrderStats()` - 获取订单统计
6. ✅ `getUserPurchases(userId)` - 查询用户的购票记录
7. ✅ `getUserRefunds(userId)` - 查询用户的退票记录
8. ✅ `getUserPurchasePage(params)` - 分页查询用户的购票记录
9. ✅ `getUserRefundPage(params)` - 分页查询用户的退票记录
10. ✅ `getAllSales(params)` - 查询所有售票记录
11. ✅ `getSalesByTrain(trainId)` - 根据车次ID查询售票记录
12. ✅ `getSalesByTicket(ticketId)` - 根据车票ID查询售票记录
13. ✅ `getAllRefunds(params)` - 查询所有退票记录
14. ✅ `getRefundBySale(saleId)` - 根据售票记录ID查询退票记录

## 使用说明

1. 重启Spring Boot应用
2. 前端页面现在可以正常调用这些接口
3. 所有接口都遵循统一的 `ApiResult` 响应格式
4. 统计接口使用GET方法，列表查询接口使用POST方法（支持分页参数）

## 注意事项

1. 管理员接口（如 `/sale/list`、`/refund/list`）应该添加权限验证
2. 用户订单接口应该验证当前登录用户只能查询自己的订单
3. 建议在Service层添加业务逻辑，Controller层只负责接收参数和返回结果

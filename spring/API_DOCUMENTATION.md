# API接口文档

## 基础信息
- **Base URL**: `http://localhost:8080/api/v1`
- **Content-Type**: `application/json`
- **认证方式**: JWT Token（通过请求头或HttpServletRequest获取）

## 统一返回格式
```json
{
  "code": 2000,
  "msg": "成功",
  "data": {}
}
```

---

## 1. 用户模块

### 1.1 登录
**接口**: `POST /login`  
**参数**: 
```json
{
  "username": "admin",
  "password": "e10adc3949ba59abbe56e057f20f883e",
  "role": "admin"
}
```

### 1.2 注册
**接口**: `POST /reg`  
**参数**:
```json
{
  "username": "testuser",
  "password": "e10adc3949ba59abbe56e057f20f883e",
  "realName": "测试用户",
  "phone": "13800000000",
  "role": "user"
}
```

---

## 2. 车次管理模块

### 2.1 车次列表（分页）
**接口**: `POST /train/list`  
**参数**:
```json
{
  "pageNum": 1,
  "find": "G101"
}
```

### 2.2 添加车次
**接口**: `POST /train/add`  
**参数**:
```json
{
  "trainNumber": "G999",
  "totalStations": 3,
  "departureTime": "2026-06-01T08:00:00",
  "arrivalTime": "2026-06-01T12:00:00",
  "runTime": "4小时"
}
```

### 2.3 更新车次
**接口**: `POST /train/update`  
**参数**:
```json
{
  "trainId": 1,
  "trainNumber": "G101",
  "totalStations": 5,
  "departureTime": "2026-06-01T08:00:00",
  "arrivalTime": "2026-06-01T12:00:00",
  "runTime": "4小时"
}
```

### 2.4 删除车次
**接口**: `POST /train/delete`  
**参数**:
```json
{
  "id": 1
}
```

### 2.5 按车次号查询
**接口**: `POST /train/query/number`  
**参数**:
```json
{
  "number": "G101"
}
```

### 2.6 按发车时间查询
**接口**: `POST /train/query/time`  
**参数**:
```json
{
  "time": "2026-06-01T08:00:00"
}
```

### 2.7 ⭐按起止站点查询（新增）
**接口**: `POST /train/query/stations`  
**参数**:
```json
{
  "startStationId": 1,
  "endStationId": 5
}
```
**说明**: 查询从起点站到终点站的所有车次

### 2.8 ⭐按发车时间范围查询（新增）
**接口**: `POST /train/query/timeRange`  
**参数**:
```json
{
  "startTime": "2026-06-01T00:00:00",
  "endTime": "2026-06-01T23:59:59"
}
```
**说明**: 查询指定时间范围内发车的所有车次

---

## 3. 车票管理模块

### 3.1 车票列表（分页）
**接口**: `POST /ticket/list`  
**参数**:
```json
{
  "pageNum": 1,
  "find": "01A"
}
```

### 3.2 添加车票
**接口**: `POST /ticket/add`  
**参数**:
```json
{
  "trainId": 1,
  "carriageNumber": "02",
  "seatNumber": "03A",
  "seatType": "二等座",
  "price": 553.00,
  "ticketStatus": "可售"
}
```

### 3.3 更新车票
**接口**: `POST /ticket/update`  
**参数**:
```json
{
  "ticketId": 1,
  "price": 600.00
}
```

### 3.4 删除车票
**接口**: `POST /ticket/delete`  
**参数**:
```json
{
  "id": 1
}
```

### 3.5 按车次查询车票
**接口**: `POST /ticket/train`  
**参数**:
```json
{
  "trainId": 1
}
```

---

## 4. 售票模块

### 4.1 ⭐售票（已优化）
**接口**: `POST /sale/do`  
**参数**:
```json
{
  "ticketId": 1,
  "trainId": 1,
  "startStationSeq": 1,
  "endStationSeq": 3
}
```
**说明**: 
- 需要登录（从HttpServletRequest获取用户信息）
- Service层自动验证车票状态
- 事务性操作，保证数据一致性
- 异常由全局处理器统一处理

**成功响应**:
```json
{
  "code": 2000,
  "msg": "售票成功",
  "data": 1
}
```

**失败响应**:
```json
{
  "code": 400,
  "msg": "车票状态不可售，当前状态：已售",
  "data": null
}
```

---

## 5. 退票模块

### 5.1 ⭐退票（已优化）
**接口**: `POST /refund/do`  
**参数**:
```json
{
  "saleId": 1
}
```
**说明**:
- Service层自动验证售票记录状态
- 防止重复退票
- 事务性操作，自动恢复车票状态
- 异常由全局处理器统一处理

**成功响应**:
```json
{
  "code": 2000,
  "msg": "退票成功",
  "data": {
    "refundId": 1,
    "saleId": 1,
    "ticketId": 1,
    "trainId": 1,
    "userId": 2,
    "refundTime": "2026-05-22T10:30:00",
    "refundStatus": "已完成",
    "refundRemark": "用户申请退票"
  }
}
```

---

## 6. 存储过程调用示例

### 6.1 售票存储过程
```sql
CALL proc_sell_ticket(
  1,    -- p_ticket_id
  1,    -- p_train_id
  2,    -- p_user_id
  1,    -- p_start_station_seq
  3,    -- p_end_station_seq
  @sale_id,   -- OUT p_sale_id
  @code,      -- OUT p_result_code
  @msg        -- OUT p_result_msg
);

SELECT @sale_id AS sale_id, @code AS code, @msg AS msg;
```

### 6.2 退票存储过程
```sql
CALL proc_refund_ticket(
  1,            -- p_sale_id
  @refund_id,   -- OUT p_refund_id
  @code,        -- OUT p_result_code
  @msg          -- OUT p_result_msg
);

SELECT @refund_id AS refund_id, @code AS code, @msg AS msg;
```

### 6.3 余票查询存储过程
```sql
-- 查询指定车次余票
CALL proc_query_available_tickets(1);

-- 查询所有车次余票统计
CALL proc_query_available_tickets(NULL);
```

### 6.4 售票统计存储过程
```sql
CALL proc_query_sale_statistics(
  '2026-06-01 00:00:00',
  '2026-06-30 23:59:59'
);
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 2000 | 成功 |
| 400 | 请求参数错误/业务异常 |
| 401 | 未登录或Token过期 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | e10adc3949ba59abbe56e057f20f883e | admin | 管理员 |
| user1 | e10adc3949ba59abbe56e057f20f883e | user | 普通用户 |
| user2 | e10adc3949ba59abbe56e057f20f883e | user | 普通用户 |

> 密码为 "123456" 的MD5值

---

## Postman/Apifox 测试建议

### 环境变量设置
```
base_url: http://localhost:8080
token: {{从登录接口获取}}
```

### 请求头设置
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

### 测试流程
1. 登录获取Token
2. 查询车次列表
3. 按站点查询车次
4. 查询车票
5. 售票
6. 查询售票记录
7. 退票
8. 验证车票状态恢复

---

## 更新日志

### v1.1 (2026-05-22)
- ✅ 重构SaleController和RefundController使用RespEntity
- ✅ 新增按站点查询车次接口
- ✅ 新增按时间范围查询车次接口
- ✅ 完善Service层事务管理
- ✅ 添加全局异常处理

### v1.0 (2026-05-20)
- ✅ 基础CRUD功能
- ✅ 用户认证
- ✅ 基本查询功能

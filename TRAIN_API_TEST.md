# 列车往返功能测试用例

## 核心概念

**一趟列车 = 一条 train_info 记录**
- 包含 `router_id`（往程路线）和 `oppsite_router_id`（返程路线）
- **不需要创建两趟列车**
- `departure_schedule` 表中的发车计划选择使用哪条路线（往程或返程）

---
1. 确保数据库中已有路线数据（往程和返程路线对）
2. 往程路线ID为偶数，返程路线ID = 往程ID + 1
3. 路线必须已配置站点并计算了总时长

---

## 测试用例 1：新建列车（自动创建往返）

### 请求
```http
POST http://localhost:8080/api/v1/train/add
Content-Type: application/json
```

```json
{
  "trainNumber": "G100",
  "routerId": 100
}
```

**说明**：
- `trainNumber`: 往程列车编号（返程会自动添加"返"后缀，变成"G100返"）
- `routerId`: 可以传入往程ID（100）或返程ID（101），系统会自动识别

### 预期结果
```json
{
  "code": 200,
  "message": "添加成功，往返列车已创建",
  "data": 1
}
```

### 验证SQL
```sql
-- 查询刚创建的列车（只有一条记录）
SELECT * FROM train_info WHERE train_number = 'G100';

-- 应该返回一条记录：
-- train_number='G100', router_id=100, oppsite_router_id=101
```

---

## 测试用例 2：修改列车编号

### 请求
```http
POST http://localhost:8080/api/v1/train/update
Content-Type: application/json
```

```json
{
  "trainId": 1,
  "trainNumber": "G200"
}
```

**说明**：
- 只修改编号，不修改路线

### 预期结果
```json
{
  "code": 200,
  "message": "更新成功，往返列车已同步更新",
  "data": null
}
```

### 验证SQL
```sql
SELECT * FROM train_info WHERE train_id = 1;

-- 应该看到列车的编号已更新为 G200
```

---

## 测试用例 3：修改列车路线

### 请求
```http
POST http://localhost:8080/api/v1/train/update
Content-Type: application/json
```

```json
{
  "trainId": 1,
  "routerId": 200
}
```

**说明**：
- 将列车从原路线切换到新路线（200/201）
- 列车的 `router_id` 和 `oppsite_router_id` 都会更新

### 预期结果
```json
{
  "code": 200,
  "message": "更新成功，往返列车已同步更新",
  "data": null
}
```

### 验证SQL
```sql
SELECT train_id, train_number, router_id, oppsite_router_id, time_consuming
FROM train_info 
WHERE train_id = 1;

-- 应该看到列车的 router_id 和 oppsite_router_id 都已更新为 200/201
```

---

## 测试用例 4：同时修改编号和路线

### 请求
```http
POST http://localhost:8080/api/v1/train/update
Content-Type: application/json
```

```json
{
  "trainId": 1,
  "trainNumber": "G300",
  "routerId": 300
}
```

### 预期结果
```json
{
  "code": 200,
  "message": "更新成功，往返列车已同步更新",
  "data": null
}
```

---

## 测试用例 5：删除列车（同步删除往返）

### 请求
```http
POST http://localhost:8080/api/v1/train/delete
Content-Type: application/json
```

```json
{
  "id": 1
}
```

### 预期结果
```json
{
  "code": 200,
  "message": "删除成功，往返列车已同步删除",
  "data": null
}
```

### 验证SQL
```sql
-- 应该查不到任何记录
SELECT * FROM train_info WHERE train_id = 1;
SELECT * FROM train_info WHERE train_number = 'G300';
```

---

## 测试用例 6：错误场景 - 列车编号重复

### 请求
```http
POST http://localhost:8080/api/v1/train/add
Content-Type: application/json
```

```json
{
  "trainNumber": "G100",
  "routerId": 102
}
```

**说明**：假设 G100 已经存在

### 预期结果
```json
{
  "code": 400,
  "message": "列车编号已存在：G100",
  "data": null
}
```

---

## 测试用例 7：错误场景 - 路线不存在

### 请求
```http
POST http://localhost:8080/api/v1/train/add
Content-Type: application/json
```

```json
{
  "trainNumber": "G999",
  "routerId": 9999
}
```

### 预期结果
```json
{
  "code": 400,
  "message": "选择的路线不存在",
  "data": null
}
```

---

## 快速测试脚本（使用 curl）

```bash
# 1. 新建列车
curl -X POST http://localhost:8080/api/v1/train/add \
  -H "Content-Type: application/json" \
  -d '{"trainNumber":"G100","routerId":100}'

# 2. 查询列表确认
curl -X POST http://localhost:8080/api/v1/train/list \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"find":"G100"}'

# 3. 修改编号
curl -X POST http://localhost:8080/api/v1/train/update \
  -H "Content-Type: application/json" \
  -d '{"trainId":1,"trainNumber":"G200"}'

# 4. 删除列车
curl -X POST http://localhost:8080/api/v1/train/delete \
  -H "Content-Type: application/json" \
  -d '{"id":1}'
```

---

## 注意事项

### 1. 唯一索引约束
- `train_number` 字段有唯一索引
- **一趟列车只有一条记录**，不存在往返列车编号冲突问题

### 2. 路线ID规则
- 往程路线ID：偶数（如 100, 200, 300）
- 返程路线ID：往程ID + 1（如 101, 201, 301）
- 传入任意方向的ID都可以，系统会自动识别

### 3. 事务保证
- 所有操作都有 `@Transactional` 注解
- 新建/修改/删除失败时会自动回滚
- 列车的往返路线信息保持一致性

### 4. 时间字段
- `train_info` 表没有 `create_time` 字段
- 无需担心时间字段的填充问题

### 5. 发车计划
- `departure_schedule` 表中创建发车计划时
- 需要指定使用列车的哪条路线（往程或返程）
- 通过 `router_id` 字段选择

---

## 常见问题排查

### Q1: 提示“列车编号已存在”
**原因**：数据库中存在相同编号的列车  
**解决**：更换编号或删除已有列车

### Q2: 提示“返程路线不存在”
**原因**：只创建了往程路线，没有创建对应的返程路线  
**解决**：确保路线成对存在（100 和 101）

### Q3: 提示“所选路线尚未计算总耗时”
**原因**：路线的 `total_duration` 字段为 NULL  
**解决**：先维护路线的站点信息，系统会自动计算总时长

### Q4: 如何创建发车计划？
**答**：在 `departure_schedule` 表中创建记录时：
- `train_id`: 列车ID
- `train_number`: 列车编号（从 train_info 获取）
- `router_id`: 选择往程路线ID或返程路线ID
- `departure_time`: 发车时间

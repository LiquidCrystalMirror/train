# 用户ID改造说明

## 改造背景

原有的 `user_id` 使用 `INT AUTO_INCREMENT` 自增整数，存在以下问题：
1. **无意义**：自增ID对用户没有实际意义
2. **不可共享**：用户无法记住或分享自己的ID
3. **安全性低**：容易被猜测和遍历

## 改造方案

### 1. 数据库层面

**修改前：**
```sql
`user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
PRIMARY KEY (`user_id`)
```

**修改后：**
```sql
`user_id` varchar(20) NOT NULL COMMENT '用户ID（格式：U + 时间戳后6位 + 4位随机数）',
PRIMARY KEY (`user_id`)
```

### 2. 用户ID生成规则

**格式：** `U` + 时间戳后6位 + 4位随机数

**示例：**
- `U2605241234` - U + 260524(日期) + 1234(随机数)
- `U2605249876` - U + 260524(日期) + 9876(随机数)

**特点：**
- 长度固定（11位）
- 易于记忆和分享
- 包含时间信息
- 具有随机性，防止被猜测

### 3. 登录方式

支持两种方式登录：
1. **用户名登录**：使用 `username`（唯一）
2. **用户ID登录**：使用 `userId`（如 `U2605241234`）

前端输入框提示："用户名或用户ID"

### 4. 注册流程

1. 用户填写注册信息（username, password, realName等）
2. 后端自动生成唯一的 `userId`
3. 返回成功消息时包含 userId，提示用户牢记
4. 示例消息："注册成功，请牢记您的用户ID：U2605241234"

### 5. 用户名唯一性

数据库已设置唯一索引：
```sql
UNIQUE INDEX `username`(`username` ASC)
```

后端注册接口会检查用户名是否已存在，确保唯一性。

## 执行步骤

### 1. 执行数据库升级脚本

```bash
# 在 MySQL 中执行
source upgrade-user-table.sql
```

或者手动执行 `spring/src/main/resources/upgrade-user-table.sql`

### 2. 重启后端服务

```bash
cd spring
mvn spring-boot:run
```

### 3. 测试功能

#### 测试注册
```bash
curl -X POST http://localhost:8080/api/v1/reg \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "123456",
    "realName": "新用户",
    "idCard": "110101199001011234",
    "phone": "13800000004"
  }'
```

预期响应：
```json
{
  "code": 2000,
  "msg": "注册成功，请牢记您的用户ID：U2605241234",
  "data": {
    "userId": "U2605241234",
    "username": "newuser",
    "realName": "新用户",
    ...
  }
}
```

#### 测试登录（使用用户名）
```bash
curl -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "newuser",
    "password": "123456"
  }'
```

#### 测试登录（使用用户ID）
```bash
curl -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{
    "account": "U2605241234",
    "password": "123456"
  }'
```

## 注意事项

1. **数据迁移**：如果已有用户数据，需要为现有用户生成新的 userId
2. **外键关联**：sale_info 和 refund_info 表的 user_id 字段类型已同步修改
3. **前端显示**：建议在用户个人中心显示 userId，方便用户查看和复制
4. **密码安全**：所有用户的密码都已使用 MD5+Salt 加密存储

## 优势总结

✅ **有意义**：用户可以记住和分享自己的ID  
✅ **安全性高**：包含随机数，不易被猜测  
✅ **灵活性强**：支持用户名和用户ID两种登录方式  
✅ **唯一性保证**：用户名有数据库唯一索引保护  
✅ **易于扩展**：字符串类型便于未来调整格式  

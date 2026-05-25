# JWT安全修复说明

## 问题发现

在检查JWT解码和用户信息存储时，发现了两个安全问题：

### 1. 后端返回完整User对象（包含password字段）
**位置**: `UserController.java` 第97行

**原代码**:
```java
Map<String, Object> map = new HashMap<>();
map.put("user", user);  // ⚠️ 包含password字段
map.put("token", token);
```

**问题**: 
- 虽然数据库中的密码是加密的（MD5+Salt），但返回给前端仍然不安全
- 增加了网络传输的数据量
- 违反了最小权限原则

### 2. 前端用户名获取逻辑错误
**位置**: `Dashboard.vue` 第163行

**原代码**:
```javascript
const userName = computed(() => {
  const userData = user.value
  return userData ? (userData.username || userData.name || '管理员') : '管理员'
})
```

**问题**:
- `userData.name` 字段在新系统中不存在
- 应该优先显示 `realName`（真实姓名）
- fallback值应该是"用户"而不是"管理员"

---

## 修复方案

### ✅ 后端修复 - 创建安全的User对象

**文件**: `spring/src/main/java/com/example/ticket/controller/UserController.java`

**修复后的代码**:
```java
// 生成 token
String token = generateJwtToken(user);

// 安全处理：移除密码字段后再返回
User safeUser = new User();
safeUser.setUserId(user.getUserId());
safeUser.setUsername(user.getUsername());
safeUser.setRealName(user.getRealName());
safeUser.setIdCard(user.getIdCard());
safeUser.setPhone(user.getPhone());
safeUser.setRole(user.getRole());
safeUser.setCreateTime(user.getCreateTime());

Map<String, Object> map = new HashMap<>();
map.put("user", safeUser);  // ✅ 不包含password
map.put("token", token);

return new RespEntity(2000, "登录成功", map);
```

**优点**:
- ✅ 不返回敏感信息（password）
- ✅ 只返回必要的用户信息
- ✅ 符合安全最佳实践
- ✅ 减少网络传输数据量

---

### ✅ 前端修复 - 修正用户名显示逻辑

**文件**: `vue/src/views/Dashboard.vue`

**修复后的代码**:
```javascript
// 获取用户信息
const user = computed(() => authService.getUser())
const userName = computed(() => {
  const userData = user.value
  return userData ? (userData.realName || userData.username || '用户') : '用户'
})
```

**优先级**:
1. `realName` - 真实姓名（优先显示）
2. `username` - 用户名（如果没有真实姓名）
3. `'用户'` - 默认值（如果都没有）

**优点**:
- ✅ 符合新系统的字段结构
- ✅ 更友好的显示方式
- ✅ 合理的fallback机制

---

## JWT Token内容

### Token中包含的Claims（后端第130-145行）:
```java
claims.put("userId", user.getUserId());      // 用户ID
claims.put("username", user.getUsername());   // 用户名
claims.put("realName", user.getRealName());   // 真实姓名
claims.put("phone", user.getPhone());         // 手机号
claims.put("role", user.getRole());           // 角色
```

### Token中**不包含**:
- ❌ password（密码）
- ❌ idCard（身份证号）
- ❌ createTime（创建时间）

**原因**: 
- Token应该尽量小巧
- 敏感信息不应该放在JWT中
- 这些信息可以从localStorage的user对象中获取

---

## 前端存储的用户信息

### localStorage中存储的内容:
```javascript
// Login.vue 第120行
localStorage.setItem('user', JSON.stringify(response.data.user))
```

**包含字段**（修复后）:
```json
{
  "userId": 1,
  "username": "admin",
  "realName": "管理员",
  "idCard": "110101199001011234",
  "phone": "13800138000",
  "role": "admin",
  "createTime": "2024-01-01T00:00:00"
}
```

**不包含**:
- ❌ password（已修复）

---

## 安全建议

### 1. JWT最佳实践
- ✅ JWT中只存放必要的身份标识
- ✅ 不要存放敏感信息（密码、身份证号等）
- ✅ 设置合理的过期时间（当前是24小时）
- ✅ 使用强密钥签名（通过`${my.jwt_pwd}`配置）

### 2. 用户信息安全
- ✅ 不在响应中返回密码字段
- ✅ 前端不存储密码
- ✅ 使用HTTPS传输（生产环境）
- ✅ 敏感操作需要二次验证

### 3. 前端存储
- ✅ 使用localStorage存储非敏感用户信息
- ✅ Token存储在localStorage（可以改用httpOnly cookie更安全）
- ✅ 退出登录时清除所有存储

---

## 测试验证

### 1. 登录测试
```bash
# 使用Apifox或Postman测试
POST http://localhost:8080/api/v1/login
Content-Type: application/x-www-form-urlencoded

username=admin&password=123456&role=admin
```

**期望响应**:
```json
{
  "code": 2000,
  "msg": "登录成功",
  "data": {
    "user": {
      "userId": 1,
      "username": "admin",
      "realName": "管理员",
      "idCard": "...",
      "phone": "...",
      "role": "admin",
      "createTime": "..."
      // ✅ 没有password字段
    },
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

### 2. 前端显示测试
1. 登录系统
2. 查看右上角用户名显示
3. 应该显示真实姓名（如果有）或用户名
4. 不应该显示"管理员"作为默认值（除非真的是管理员且没有realName）

### 3. JWT解码测试
可以使用 https://jwt.io/ 解码Token，验证包含的claims是否正确。

---

## 相关文件清单

### 修改的文件:
1. `spring/src/main/java/com/example/ticket/controller/UserController.java`
   - 修复登录接口返回的用户对象

2. `vue/src/views/Dashboard.vue`
   - 修复用户名显示逻辑

### 相关的文件（无需修改）:
1. `vue/src/views/Login.vue` - 登录页面（正确）
2. `vue/src/views/DashboardHome.vue` - 首页（已正确使用realName）
3. `vue/src/service/AuthService.js` - 认证服务（正确）
4. `spring/src/main/java/com/example/ticket/interceptor/JwtInterceptor.java` - JWT拦截器（正确）

---

## 总结

通过这次修复：
- ✅ 提高了系统安全性（不返回密码字段）
- ✅ 修正了前端显示逻辑（使用正确的字段）
- ✅ 符合现代Web应用的安全最佳实践
- ✅ 保持了前后端数据一致性

**修复时间**: 2026年5月24日  
**影响范围**: 登录功能和用户信息显示  
**风险等级**: 低（向后兼容）

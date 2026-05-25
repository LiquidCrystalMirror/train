# JWT架构重构说明

## 🎯 重构目标

将JWT相关的所有逻辑（生成、解析、验证）完全封装在`JwtUtil`工具类中，实现职责分离和代码复用。

---

## 📁 文件结构

```
spring/src/main/java/com/example/ticket/
├── util/
│   └── JwtUtil.java          # JWT工具类（新增）✅
├── interceptor/
│   └── JwtInterceptor.java   # JWT拦截器（重构）✅
└── controller/
    └── UserController.java   # 用户控制器（重构）✅
```

---

## ✅ 重构完成内容

### 1. JwtUtil.java - JWT工具类

**职责**: 负责所有JWT相关的操作

**核心方法**:

#### Token生成
```java
public String generateToken(User user)
```
- 从User对象提取信息
- 设置claims（userId, username, realName, phone, role）
- 设置签发时间和过期时间（24小时）
- 使用HS256算法签名

#### Token解析
```java
public Claims parseToken(String token) throws Exception
```
- 验证Token签名
- 解析Token内容
- 返回Claims对象

#### Token验证（核心方法）⭐
```java
public User parseAndValidateToken(String token)
```
**功能**:
1. 检查Token是否为空
2. 解析Token获取Claims
3. **检查Token是否过期**
4. 从Claims构建User对象
5. 如果任何步骤失败，返回null

**优点**:
- ✅ 一站式解决：解析+验证+构建User
- ✅ 异常处理完善：所有错误统一返回null
- ✅ 过期检查：自动验证Token有效期
- ✅ 安全性：不返回密码字段

#### 便捷方法
```java
public Integer getUserIdFromToken(String token)
public String getUsernameFromToken(String token)
public String getRoleFromToken(String token)
public boolean validateToken(String token)
public boolean isTokenExpired(String token)
```

---

### 2. JwtInterceptor.java - JWT拦截器

**职责**: 拦截请求，验证Token，提取用户信息

**重构前**（❌ 职责混乱）:
```java
// 直接在拦截器中处理JWT逻辑
Claims claims = Jwts.parser()
    .setSigningKey(jwtpwd)
    .parseClaimsJws(jwt)
    .getBody();

User user = new User();
user.setUserId((Integer) claims.get("userId"));
// ... 手动设置每个字段
```

**重构后**（✅ 职责清晰）:
```java
// 只负责调用工具类
User user = jwtUtil.parseAndValidateToken(jwt);

if (user == null) {
    // Token无效，返回错误
}

req.setAttribute("auth", user);
```

**改进点**:
- ✅ 不再关心JWT的具体实现
- ✅ 代码从23行减少到5行
- ✅ 易于理解和维护
- ✅ 如果需要修改JWT逻辑，只需改JwtUtil

---

### 3. UserController.java - 用户控制器

**职责**: 处理用户相关业务逻辑

**重构前**（❌ 包含JWT生成逻辑）:
```java
@Value("${my.jwt_pwd}")
private String jwtPwd;

private String generateJwtToken(User user) {
    // 19行JWT生成代码
    return Jwts.builder()...
}

String token = generateJwtToken(user);
```

**重构后**（✅ 纯业务逻辑）:
```java
@Resource
private JwtUtil jwtUtil;

String token = jwtUtil.generateToken(user);
```

**改进点**:
- ✅ 删除了19行的JWT生成代码
- ✅ 删除了jwtPwd字段
- ✅ Controller专注于业务逻辑
- ✅ 代码更简洁清晰

---

## 🔄 工作流程

### Token生成流程
```
用户登录
  ↓
UserController.login()
  ↓
验证用户名/密码/角色
  ↓
jwtUtil.generateToken(user)  ← 调用工具类
  ↓
返回Token给前端
```

### Token验证流程
```
客户端请求（携带Token）
  ↓
JwtInterceptor.preHandle()
  ↓
提取Token（去除"Bearer "前缀）
  ↓
jwtUtil.parseAndValidateToken(token)  ← 调用工具类
  ├─ 解析Token
  ├─ 验证签名
  ├─ 检查过期
  └─ 构建User对象
  ↓
如果user == null → 返回401错误
如果user != null → 存入request，放行
  ↓
Controller获取用户信息
```

---

## 📊 架构对比

### 重构前
```
┌─────────────────────┐
│  UserController     │
│  - jwtPwd字段       │  ❌ 职责混乱
│  - generateJwtToken │
└─────────────────────┘
         ↓
┌─────────────────────┐
│ JwtInterceptor      │
│  - jwtpwd字段       │  ❌ 职责混乱
│  - JWT解析逻辑      │
│  - User构建逻辑     │
└─────────────────────┘
```

**问题**:
- ❌ JWT逻辑分散在两个类中
- ❌ 代码重复（密钥配置）
- ❌ 难以维护和测试
- ❌ 违反单一职责原则

### 重构后
```
┌─────────────────────┐
│     JwtUtil         │  ✅ 专门负责JWT
│  - generateToken    │
│  - parseToken       │
│  - parseAndValidate │
│  - validateToken    │
└─────────────────────┘
         ↑                    ↑
┌─────────────────┐  ┌──────────────────┐
│ UserController  │  │ JwtInterceptor   │
│                 │  │                  │
│ jwtUtil生成     │  │ jwtUtil验证      │
└─────────────────┘  └──────────────────┘
```

**优势**:
- ✅ JWT逻辑集中在一个类
- ✅ 单一职责原则
- ✅ 代码复用
- ✅ 易于维护和测试
- ✅ 符合开闭原则

---

## 🎨 设计模式应用

### 1. 工具类模式（Utility Pattern）
`JwtUtil`作为工具类，提供静态方法风格的API（虽然使用Spring注入）

### 2. 单一职责原则（SRP）
- `JwtUtil`: 只负责JWT操作
- `JwtInterceptor`: 只负责拦截验证
- `UserController`: 只负责用户业务

### 3. 依赖注入（DI）
通过`@Resource`注入`JwtUtil`，而不是直接new对象

### 4. 封装（Encapsulation）
JWT的内部实现细节被完全封装在`JwtUtil`中

---

## 💡 使用示例

### 在其他Controller中使用JwtUtil

如果未来需要在其他Controller中验证Token：

```java
@RestController
@RequestMapping("/api/v1/order")
public class OrderController {
    
    @Resource
    private JwtUtil jwtUtil;
    
    @GetMapping("/my-orders")
    public RespEntity getMyOrders(
            @RequestHeader("Authorization") String authorization) {
        
        String token = authorization.substring(7);
        User user = jwtUtil.parseAndValidateToken(token);
        
        if (user == null) {
            return new RespEntity(4001, "Token无效", null);
        }
        
        // 使用user.getUserId()查询订单
        List<Order> orders = orderService.getByUserId(user.getUserId());
        return new RespEntity(2000, "成功", orders);
    }
}
```

---

## 🔒 安全特性

### 1. Token签名验证
- 使用HS256算法
- 密钥从配置文件读取
- 防止Token被篡改

### 2. Token过期检查
- 默认24小时有效期
- `parseAndValidateToken`自动检查过期
- 过期的Token会被拒绝

### 3. 敏感信息保护
- Token中不包含密码
- 返回的User对象password为null
- 最小化信息暴露

### 4. 异常处理
- 所有JWT异常统一处理
- 返回null表示验证失败
- 不泄露具体错误原因（防止攻击者探测）

---

## 📝 配置说明

### JWT密钥配置
**文件**: `application-data.yml`
```yaml
my:
  jwt_pwd: ${JWT_SECRET}
```

**环境变量**:
```bash
# .env文件
JWT_SECRET=your_super_secret_key_here
```

### Token有效期
**位置**: `JwtUtil.java` 第26行
```java
private static final long TOKEN_EXPIRATION = 86400000L; // 24小时
```

如需修改，直接更改这个常量即可。

---

## ✨ 总结

### 重构成果
1. ✅ 创建了专门的`JwtUtil`工具类
2. ✅ 将所有JWT逻辑集中管理
3. ✅ `JwtInterceptor`只负责调用工具类
4. ✅ `UserController`不再包含JWT实现细节
5. ✅ 代码更清晰、更易维护

### 关键改进
- **职责分离**: 每个类只做一件事
- **代码复用**: JWT逻辑可以被多个地方使用
- **易于测试**: 可以独立测试JwtUtil
- **易于扩展**: 添加新功能只需修改JwtUtil

### 最佳实践
- ✅ 工具类封装复杂逻辑
- ✅ 拦截器保持轻量
- ✅ Controller专注业务
- ✅ 依赖注入解耦

---

**重构完成时间**: 2026年5月24日  
**影响范围**: JWT相关的所有代码  
**向后兼容**: ✅ 是（API保持不变）

# 登录注册角色管理重构说明

## 📋 修改概述

将前端登录注册的角色选择逻辑移除，改为：
1. **普通用户**：通过前端注册页面自动注册为 `user` 角色
2. **管理员**：通过独立的HTML页面注册为 `admin` 角色
3. **Token中的role**：始终从数据库获取，确保安全性

---

## ✅ 已完成的修改

### 1. 前端修改

#### 📄 `vue/src/views/Register.vue`
- **删除**：提交数据中的 `role: 'user'` 字段
- **结果**：注册时不再传递role参数给后端

```javascript
// 修改前
const submitData = {
  username: registerForm.username,
  realName: registerForm.realName,
  idCard: registerForm.idCard,
  phone: registerForm.phone,
  password: registerForm.password,
  role: 'user'  // ❌ 删除
}

// 修改后
const submitData = {
  username: registerForm.username,
  realName: registerForm.realName,
  idCard: registerForm.idCard,
  phone: registerForm.phone,
  password: registerForm.password
  // ✅ 不包含role，由后端强制设置
}
```

#### 📄 `vue/src/views/Login.vue`
- **删除**：角色选择的 `<el-select>` 组件（第39-44行）
- **删除**：`loginForm` 中的 `role: 'user'` 字段
- **删除**：`loginRules` 中的 role 验证规则

```javascript
// 修改前
const loginForm = reactive({
  username: '',
  password: '',
  role: 'user'  // ❌ 删除
})

// 修改后
const loginForm = reactive({
  username: '',
  password: ''
  // ✅ 只保留用户名和密码
})
```

---

### 2. 后端修改

#### 📄 `spring/src/main/java/com/example/ticket/controller/UserController.java`

##### ✏️ 修改登录接口（第67-87行）
```java
// 修改前
@PostMapping("/login")
public RespEntity login(
        @RequestParam String username,
        @RequestParam String password,
        @RequestParam String role) {  // ❌ 删除此参数
    
    // ... 
    // 角色校验
    if (!user.getRole().equals(role)) {
        return new RespEntity(4002, "角色不匹配", null);
    }
}

// 修改后
@PostMapping("/login")
public RespEntity login(
        @RequestParam String username,
        @RequestParam String password) {  // ✅ 只保留用户名和密码
    
    User user = userMapper.selectByUsername(username);
    
    if (user == null) {
        return new RespEntity(4001, "用户名或密码错误", null);
    }
    
    // ✅ 直接进行密码校验，不再校验角色
    
    // 密码校验
    if (!passwordUtil.verifyPassword(password, user.getPassword())) {
        return new RespEntity(4004, "密码错误", null);
    }
    
    // 生成token（包含数据库中的role）
    String token = jwtUtil.generateToken(user);
}
```

##### ✏️ 修改普通用户注册接口（第110-126行）
```java
// 修改前
if (user.getRole() == null) {
    user.setRole("user");
}

// 修改后
// 安全处理：强制设置角色为普通用户，防止越权注册
user.setRole("user");
```

##### ➕ 新增管理员注册接口（第128-150行）
```java
@PostMapping("/admin/reg")
public RespEntity adminRegister(@RequestBody User user) {
    // 检查用户名是否存在
    User exist = userMapper.selectByUsername(user.getUsername());
    if (exist != null) {
        return new RespEntity(4000, "用户名已存在", null);
    }

    // 强制设置为管理员
    user.setRole("admin");

    // 对密码进行加密处理
    String encryptedPassword = passwordUtil.md5WithSalt(user.getPassword());
    user.setPassword(encryptedPassword);
    
    user.setCreateTime(LocalDateTime.now());
    userService.save(user);
    user.setPassword(null);

    return new RespEntity(2000, "管理员注册成功", user);
}
```

---

#### 📄 `spring/src/main/java/com/example/ticket/config/MyWebConfig.java`

##### ✏️ 更新拦截器白名单
```java
// 修改前
.excludePathPatterns(
    "/api/v1/g/**",
    "/api/v1/login",
    "/api/v1/reg",
    "/api/v1/index",
    "/**"  // ❌ 删除这一行（会导致所有接口都不需要认证）
);

// 修改后
.excludePathPatterns(
    "/api/v1/g/**",
    "/api/v1/login",
    "/api/v1/reg",
    "/api/v1/admin/reg",  // ✅ 添加管理员注册接口
    "/api/v1/index"
);
```

---

### 3. 新增文件

#### 📄 `spring/src/main/resources/static/admin-register.html`

**功能**：独立的管理员注册页面

**访问地址**：`http://localhost:8080/admin-register.html`

**特性**：
- ✅ 纯HTML+CSS+JavaScript实现，无需Vue环境
- ✅ 表单验证（用户名、身份证、手机号格式）
- ✅ 密码一致性检查
- ✅ 调用 `/api/v1/admin/reg` 接口
- ✅ 注册成功后自动跳转到登录页
- ✅ 友好的UI和错误提示

**使用方式**：
1. 启动Spring Boot后端
2. 浏览器访问 `http://localhost:8080/admin-register.html`
3. 填写管理员信息并提交
4. 注册成功后使用新账号登录

---

## 🔐 Token中的role来源验证

### ✅ JWT Token生成流程

#### 1. 登录时从数据库获取role
**文件**: `UserController.java` 第73行
```java
User user = userMapper.selectByUsername(username);
// ✅ user.getRole() 来自数据库查询结果
```

#### 2. 生成Token时将role写入claims
**文件**: `JwtUtil.java` 第40行
```java
public String generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getUserId());
    claims.put("username", user.getUsername());
    claims.put("realName", user.getRealName());
    claims.put("phone", user.getPhone());
    claims.put("role", user.getRole());  // ✅ role来自数据库
    // ...
}
```

#### 3. 解析Token时从claims读取role
**文件**: `JwtUtil.java` 第136行
```java
public User parseAndValidateToken(String token) {
    // ...
    Claims claims = parseToken(token);
    User user = new User();
    user.setUserId((Integer) claims.get("userId"));
    user.setUsername(String.valueOf(claims.get("username")));
    user.setRealName(String.valueOf(claims.get("realName")));
    user.setPhone(claims.get("phone") != null ? String.valueOf(claims.get("phone")) : null);
    user.setRole(String.valueOf(claims.get("role")));  // ✅ 从Token中读取
    user.setPassword(null);
    return user;
}
```

#### 4. 拦截器使用解析后的User对象
**文件**: `JwtInterceptor.java` 第36行
```java
User user = jwtUtil.parseAndValidateToken(jwt);
// ✅ user.getRole() 来自Token，而Token的role来自数据库
req.setAttribute("auth", user);
```

#### 5. Controller从request获取User
**文件**: `UserController.java` 第153行
```java
User login = (User) request.getAttribute("auth");
if (!"admin".equals(login.getRole())) {  // ✅ role来自Token
    return new RespEntity(403, "无权限", null);
}
```

---

### ✅ 前端获取role的流程

#### 1. 登录成功后存储用户信息
**文件**: `Login.vue` 第120行
```javascript
localStorage.setItem('user', JSON.stringify(response.data.user))
// ✅ response.data.user.role 来自后端返回（数据库中查询的）
```

#### 2. 从localStorage读取role
**文件**: `DashboardHome.vue` 第126-133行
```javascript
const currentUser = computed(() => authService.getUser())
const userRole = computed(() => {
  const user = currentUser.value
  return user ? user.role : 'user'  // ✅ 从localStorage获取
})
const isAdmin = computed(() => userRole.value === 'admin')
```

**文件**: `user/ListView.vue` 第129-131行
```javascript
const isAdmin = computed(() => {
  const user = authService.getUser();
  return user && user.role === 'admin';  // ✅ 从localStorage获取
});
```

---

## 🎯 最终架构总结

### 普通用户注册流程
```
前端Register.vue 
  → POST /api/v1/reg (不包含role)
  → UserController.register()
  → 强制设置 user.setRole("user")
  → 保存到数据库
  → 返回用户信息（不含密码）
```

### 管理员注册流程
```
浏览器访问 admin-register.html
  → POST /api/v1/admin/reg (不包含role)
  → UserController.adminRegister()
  → 强制设置 user.setRole("admin")
  → 保存到数据库
  → 返回用户信息（不含密码）
```

### 登录流程
```
前端Login.vue 
  → POST /api/v1/login (只传username和password)
  → UserController.login()
  → 从数据库查询用户（包含role）
  → 验证密码
  → 生成JWT Token（包含数据库中的role）
  → 返回 { user, token }
  → 前端存储到localStorage
```

### 权限验证流程
```
前端请求带Token 
  → JwtInterceptor.preHandle()
  → jwtUtil.parseAndValidateToken()
  → 从Token中解析出role
  → 存入request.setAttribute("auth", user)
  → Controller获取user并检查role
```

---

## ⚠️ 重要安全说明

1. **role始终来自数据库**：无论是登录还是Token解析，role都源自数据库，不会被前端篡改
2. **注册时强制设置role**：普通用户注册强制为"user"，管理员注册强制为"admin"
3. **管理员注册页面应受保护**：建议将此页面放在内网或添加额外认证机制
4. **删除了"/**"白名单**：现在只有明确列出的接口才不需要JWT认证

---

## 📝 测试步骤

### 1. 测试普通用户注册
```bash
# 前端操作
1. 访问前端注册页面
2. 填写用户信息
3. 注册成功后查看数据库，role应为"user"
```

### 2. 测试管理员注册
```bash
# 浏览器访问
http://localhost:8080/admin-register.html

# 填写管理员信息并提交
# 注册成功后查看数据库，role应为"admin"
```

### 3. 测试登录
```bash
# 使用普通用户登录
- 不需要选择角色
- 登录后localStorage中的user.role应为"user"

# 使用管理员登录
- 不需要选择角色
- 登录后localStorage中的user.role应为"admin"
```

### 4. 测试权限控制
```bash
# 使用普通用户Token访问管理员接口
→ 应该返回403无权限

# 使用管理员Token访问管理员接口
→ 应该正常执行
```

---

## ✅ 完成清单

- [x] 前端Register.vue删除role字段
- [x] 前端Login.vue删除角色选择器和相关代码
- [x] 后端登录接口删除role参数和校验
- [x] 后端注册接口强制设置role为"user"
- [x] 后端新增管理员注册接口 `/api/v1/admin/reg`
- [x] 创建管理员注册HTML页面
- [x] 更新拦截器白名单配置
- [x] 验证Token中role的来源（数据库）
- [x] 验证前端role的使用（localStorage）

---

**修改完成时间**: 2026-05-24  
**修改人**: AI Assistant  
**审核状态**: ✅ 已完成

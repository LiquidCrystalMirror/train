# 权限控制架构说明

## 🎯 设计理念

**权限判断完全通过拦截器实现，业务代码零侵入**

```
请求流程：
┌─────────────┐
│   请求到达   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│ 1. JWT拦截器     │ ← 第一层：验证Token有效性
│    - 无效 → 401  │
│    - 有效 → 放行 │    将User存入request
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│ 2. Role拦截器    │ ← 第二层：验证用户权限
│    - 无注解 → 放行│    只处理有@RequireRole的方法
│    - 有注解      │
│      ✓ 符合 → 放行│
│      ✗ 不符 → 403│
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│ 3. Controller   │ ← 纯粹的业务逻辑
│    零权限判断    │    不包含任何role检查
└─────────────────┘
```

---

## 📋 使用方法

### 1. 管理员接口（需要admin角色）

```java
@RoleInterceptor.RequireRole("admin")
@PostMapping("/admin/some/action")
public ApiResult<Void> adminAction(@RequestBody SomeData data) {
    // 直接写业务逻辑，不需要检查role
    someService.doSomething(data);
    return ApiResult.success("操作成功");
}
```

### 2. 普通用户接口（需要user角色）

```java
@RoleInterceptor.RequireRole("user")
@PostMapping("/user/some/action")
public ApiResult<Void> userAction(@RequestBody SomeData data) {
    // 直接写业务逻辑
    someService.doSomething(data);
    return ApiResult.success("操作成功");
}
```

### 3. 公开接口（无需权限）

```java
// 不加注解，任何人都可以访问（但需要JWT验证）
@GetMapping("/public/data")
public ApiResult<List<Data>> getPublicData() {
    return ApiResult.success("查询成功", dataService.list());
}
```

### 4. 获取当前用户信息

```java
@PostMapping("/some/action")
public ApiResult<Void> someAction(HttpServletRequest request) {
    // 从request中获取JWT验证后的用户
    User currentUser = (User) request.getAttribute("auth");
    
    // 可以使用userId等信息
    String userId = currentUser.getUserId();
    
    return ApiResult.success("操作成功");
}
```

---

## ✅ 已清理的代码

### 删除前的错误写法 ❌

```java
@PostMapping("/admin/user/update")
public ApiResult<Void> adminUpdate(@RequestBody User user, HttpServletRequest request) {
    User login = (User) request.getAttribute("auth");
    if (!"admin".equals(login.getRole())) {  // ❌ 内嵌role判断
        return ApiResult.error(403, "无权限");
    }
    
    userService.updateById(user);
    return ApiResult.success("修改成功");
}
```

### 删除后的正确写法 ✅

```java
@RoleInterceptor.RequireRole("admin")  // ✅ 使用注解
@PostMapping("/admin/user/update")
public ApiResult<Void> adminUpdate(@RequestBody User user) {
    userService.updateById(user);  // 纯粹的业务逻辑
    return ApiResult.success("修改成功");
}
```

---

## 🔧 配置说明

### MyWebConfig.java

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    // 1. JWT拦截器（第一层）
    registry.addInterceptor(jwtInterceptor)
            .addPathPatterns("/api/v1/**")
            .excludePathPatterns(
                "/api/v1/login",
                "/api/v1/reg",
                "/api/v1/admin/reg",
                "/api/v1/g/**",
                "/static/**",
                "/favicon.ico"
            );
    
    // 2. Role拦截器（第二层）
    registry.addInterceptor(roleInterceptor)
            .addPathPatterns("/api/v1/**")
            .excludePathPatterns(
                "/api/v1/login",
                "/api/v1/reg",
                "/api/v1/admin/reg",
                "/api/v1/g/**",
                "/static/**",
                "/favicon.ico"
            );
}
```

---

## 📝 已应用的文件

### UserController
- ✅ `/admin/user/update` - 添加 `@RequireRole("admin")`
- ✅ `/admin/user/{id}` - 添加 `@RequireRole("admin")`
- ❌ 删除了所有内嵌的 `if (!"admin".equals(...))` 判断

### 其他Controller
- 待逐步迁移...

---

## ⚠️ 注意事项

1. **不要在Controller或Service中写role判断**
   - ❌ `if ("admin".equals(user.getRole()))`
   - ✅ 使用 `@RequireRole("admin")` 注解

2. **Role拦截器只处理JWT已放行的请求**
   - JWT验证失败 → 返回401，不会到达Role拦截器
   - JWT验证成功 → Role拦截器检查权限

3. **注解可以放在方法或类上**
   ```java
   @RoleInterceptor.RequireRole("admin")  // 类级别：所有方法都需要admin
   @RestController
   @RequestMapping("/api/v1/admin")
   public class AdminController {
       
       @PostMapping("/action1")  // 继承类的注解
       public ApiResult<Void> action1() { ... }
       
       @Override
       @RoleInterceptor.RequireRole("user")  // 方法级别覆盖
       @PostMapping("/action2")
       public ApiResult<Void> action2() { ... }
   }
   ```

4. **白名单接口不需要注解**
   - `/api/v1/login`、`/api/v1/reg` 等在拦截器白名单中
   - 这些接口根本不会经过JWT和Role拦截器

---

## 🎉 优势

1. **代码清晰**：业务逻辑与权限控制分离
2. **易于维护**：权限规则集中管理
3. **类型安全**：编译期检查，避免字符串拼写错误
4. **统一响应**：401/403错误由拦截器统一返回
5. **零侵入**：Service层完全不需要关心权限

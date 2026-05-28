# JWT拦截器配置说明

## 🔧 已修复的问题

### 问题描述
JWT拦截器虽然已经编写完成（`JwtInterceptor.java`），但在 `MyWebConfig.java` 中被注释掉了，导致：
- ❌ **后端完全没有JWT验证**
- ❌ **任何人都可以访问所有接口**（只要前端发送请求）
- ❌ **无法配置白名单**
- ⚠️ **严重的安全漏洞**

### 修复方案
启用并正确配置JWT拦截器，添加合理的白名单。

---

## 📋 当前配置

### 拦截规则
```java
registry.addInterceptor(jwtInterceptor)
    .addPathPatterns("/api/v1/**")          // 拦截所有/api/v1/下的接口
    .excludePathPatterns(                    // 排除以下路径（白名单）
        "/api/v1/login",           // 用户登录
        "/api/v1/reg",             // 用户注册
        "/api/v1/admin/reg",       // 管理员注册
        "/api/v1/g/**",            // 公开查询接口
        "/static/**",              // 静态资源
        "/favicon.ico"             // 网站图标
    );
```

### 工作原理
1. **所有 `/api/v1/**` 的请求都会被拦截**
2. **白名单中的接口不需要Token**
3. **其他接口必须在Header中携带 `Authorization: Bearer <token>`**
4. **Token无效或过期会返回401错误**

---

## 🎯 白名单说明

### 1. 登录注册接口（无需Token）
| 路径 | 方法 | 说明 |
|------|------|------|
| `/api/v1/login` | POST | 用户登录，获取Token |
| `/api/v1/reg` | POST | 普通用户注册 |
| `/api/v1/admin/reg` | POST | 管理员注册 |

### 2. 公开查询接口（以 `/g/` 开头）
这些接口通常用于：
- 首页数据展示
- 公开信息查询
- 统计数据获取

示例：
- `/api/v1/g/allTotal` - 统计用户数量
- `/api/v1/g/user` - 分页查询用户
- `/api/v1/g/users` - 查询所有用户

### 3. 静态资源
- `/static/**` - 图片、CSS、JS等
- `/favicon.ico` - 网站图标

---

## 🔐 需要Token的接口示例

以下接口**必须携带有效的Token**才能访问：

### 路线管理
- ✅ `POST /api/v1/route/create` - 创建路线
- ✅ `POST /api/v1/route/update` - 更新路线
- ✅ `POST /api/v1/route/delete` - 删除路线
- ✅ `GET /api/v1/route/list` - 查询路线列表

### 车次管理
- ✅ `POST /api/v1/train/add` - 添加列车
- ✅ `POST /api/v1/train/update` - 更新列车
- ✅ `POST /api/v1/train/delete` - 删除列车

### 发车时间管理
- ✅ `POST /api/v1/departure/create` - 创建发车时间
- ✅ `POST /api/v1/departure/delete` - 删除发车时间

### 用户信息管理
- ✅ `PUT /api/v1/user/edit` - 修改个人信息
- ✅ `PUT /api/v1/admin/user/update` - 管理员修改用户

---

## 💡 如何添加新的白名单

如果需要让某个接口不需要Token验证，在 `MyWebConfig.java` 中添加：

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(jwtInterceptor)
            .addPathPatterns("/api/v1/**")
            .excludePathPatterns(
                    // 现有的白名单...
                    "/api/v1/login",
                    "/api/v1/reg",
                    "/api/v1/admin/reg",
                    "/api/v1/g/**",
                    "/static/**",
                    "/favicon.ico",
                    
                    // 👇 在这里添加新的白名单
                    "/api/v1/your/new/path"  // 新增的公开接口
            );
}
```

---

## ⚠️ 重要提醒

### 1. 安全性
- ✅ **现在后端有完整的JWT验证**
- ✅ **未授权的请求会被拦截**
- ✅ **Token过期会自动返回401**

### 2. 前端适配
前端已经在 `vue/src/request/request.js` 中配置了：
```javascript
// 自动在请求头添加Token
config.headers['Authorization'] = `Bearer ${token}`

// Token失效时自动跳转登录
if (!authService.isTokenValid()) {
    authService.redirectToLogin()
}
```

### 3. 测试建议
测试受保护的接口时：
```bash
# 1. 先登录获取Token
curl -X POST http://localhost:8080/api/v1/login \
  -H "Content-Type: application/json" \
  -d '{"account":"admin","password":"123456"}'

# 2. 使用Token访问受保护接口
curl -X GET http://localhost:8080/api/v1/route/list \
  -H "Authorization: Bearer <your_token_here>"
```

---

## 📝 相关文件

- **拦截器实现**: `com.example.ticket.interceptor.JwtInterceptor`
- **拦截器配置**: `com.example.ticket.config.MyWebConfig`
- **Token工具类**: `com.example.ticket.util.JwtUtil`
- **前端请求配置**: `vue/src/request/request.js`
- **前端认证服务**: `vue/src/service/AuthService.js`

---

## 🔄 历史对比

### 修复前（❌ 危险）
```java
// MyWebConfig.java - 第27-38行被注释
// @Override
// public void addInterceptors(InterceptorRegistry registry) {
//     ...
// }
```
**结果**: 所有接口都无需Token，任何人都可以访问！

### 修复后（✅ 安全）
```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(jwtInterceptor)
            .addPathPatterns("/api/v1/**")
            .excludePathPatterns(...);  // 明确的白名单
}
```
**结果**: 只有白名单接口可公开访问，其他都需要Token！

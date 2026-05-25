# 火车售票系统 - 前端项目结构

## 📁 项目目录结构

```
vue/
├── public/                          # 静态资源
│   ├── favicon.svg
│   └── icons.svg
├── src/
│   ├── api/                         # API接口定义
│   │   ├── LoginApi.js             # 登录API
│   │   ├── RegisterApi.js          # 注册API
│   │   ├── UserApi.js              # 用户管理API
│   │   ├── TrainApi.js             # 车次管理API
│   │   ├── TicketApi.js            # 车票管理API
│   │   ├── SaleApi.js              # 售票API
│   │   ├── RefundApi.js            # 退票API
│   │   ├── OrdersApi.js            # 订单查询API
│   │   └── ExtraApi.js             # 扩展API（预留）
│   │
│   ├── assets/                      # 资源文件
│   │   ├── hero.png
│   │   ├── vite.svg
│   │   └── vue.svg
│   │
│   ├── components/                  # 公共组件
│   │   └── HelloWorld.vue
│   │
│   ├── request/                     # HTTP请求封装
│   │   └── request.js              # Axios配置和拦截器
│   │
│   ├── router/                      # 路由配置
│   │   └── index.js                # Vue Router配置
│   │
│   ├── service/                     # 服务层
│   │   └── AuthService.js          # 认证服务
│   │
│   ├── views/                       # 页面视图
│   │   ├── Dashboard.vue           # 主布局框架
│   │   ├── DashboardHome.vue       # 首页/仪表盘 ⭐新增
│   │   ├── Login.vue               # 登录页
│   │   ├── Register.vue            # 注册页
│   │   │
│   │   ├── user/                   # 用户管理
│   │   │   └── ListView.vue        # 用户列表
│   │   │
│   │   ├── train/                  # 车次管理
│   │   │   ├── ListView.vue        # 车次列表
│   │   │   └── SearchView.vue      # 车次查询 ⭐新增
│   │   │
│   │   ├── ticket/                 # 车票管理
│   │   │   └── ListView.vue        # 车票列表
│   │   │
│   │   ├── sale/                   # 售票管理
│   │   │   └── SellView.vue        # 售票页面
│   │   │
│   │   ├── refund/                 # 退票管理
│   │   │   └── RefundView.vue      # 退票页面
│   │   │
│   │   └── orders/                 # 订单管理
│   │       ├── ListView.vue        # 订单记录（管理员）
│   │       └── MyOrdersView.vue    # 我的订单 ⭐新增
│   │
│   ├── App.vue                      # 根组件
│   ├── main.js                      # 入口文件
│   └── style.css                    # 全局样式
│
├── node_modules/                    # 依赖包
├── index.html                       # HTML模板
├── package.json                     # 项目配置
├── vite.config.js                   # Vite配置
├── FRONTEND_ADAPTATION.md          # 前端适配说明
├── NEW_FEATURES.md                 # 新增功能说明
└── PROJECT_STRUCTURE.md            # 项目结构说明（本文件）
```

---

## 🎯 核心模块说明

### 1. API层 (`src/api/`)
所有与后端的交互都通过API层进行，采用统一的调用方式：

```javascript
// GET请求
import { get } from "@/request/request.js"
export function getData() {
    return get('/api/v1/endpoint')
}

// POST请求
import { post } from "@/request/request.js"
export function postData(params) {
    return post('/api/v1/endpoint', params)
}
```

**API文件分类**:
- **基础API**: LoginApi, RegisterApi, UserApi
- **业务API**: TrainApi, TicketApi, SaleApi, RefundApi, OrdersApi
- **扩展API**: ExtraApi (预留未来功能)

---

### 2. 路由层 (`src/router/`)
使用Vue Router进行路由管理，包含路由守卫：

**路由结构**:
```
/ → /login (重定向)
/login → 登录页
/register → 注册页
/dashboard → 主框架 (需要认证)
  ├─ /dashboard/home → 首页
  ├─ /dashboard/user → 用户管理
  ├─ /dashboard/train → 车次列表
  ├─ /dashboard/train-search → 车次查询
  ├─ /dashboard/ticket → 车票管理
  ├─ /dashboard/sale → 售票
  ├─ /dashboard/refund → 退票
  ├─ /dashboard/orders → 订单记录
  └─ /dashboard/my-orders → 我的订单
```

**路由守卫**:
- 检查登录状态
- 白名单机制（/login, /register不需要登录）
- Token验证

---

### 3. 视图层 (`src/views/`)

#### 页面分类：

**认证相关**:
- `Login.vue` - 登录页面（用户名/密码/角色）
- `Register.vue` - 注册页面（完整用户信息）

**主框架**:
- `Dashboard.vue` - 主布局（侧边栏+顶部导航+内容区）
- `DashboardHome.vue` - 首页仪表盘（统计+快捷操作）⭐新增

**管理功能** (主要面向管理员):
- `user/ListView.vue` - 用户管理
- `train/ListView.vue` - 车次管理
- `ticket/ListView.vue` - 车票管理
- `orders/ListView.vue` - 订单记录查询

**业务功能** (面向所有用户):
- `sale/SellView.vue` - 售票
- `refund/RefundView.vue` - 退票
- `train/SearchView.vue` - 车次查询 ⭐新增
- `orders/MyOrdersView.vue` - 我的订单 ⭐新增

---

### 4. 服务层 (`src/service/`)

**AuthService.js** - 认证服务:
```javascript
- saveAuth(token, user)     // 保存登录信息
- getToken()                // 获取Token
- getUser()                 // 获取用户信息
- isLoggedIn()              // 检查是否登录
- isTokenValid()            // 检查Token有效性
- checkAuth()               // 检查认证状态
- redirectToLogin()         // 跳转登录页
- clearAuth()               // 清除认证信息
- logout()                  // 退出登录
```

---

### 5. 请求层 (`src/request/`)

**request.js** - Axios封装:
- 创建Axios实例
- 请求拦截器（自动添加Token）
- 响应拦截器（统一处理错误）
- Token白名单机制
- 401自动跳转登录

---

## 🔧 技术栈

### 核心框架
- **Vue 3** - 渐进式JavaScript框架
- **Vue Router 4** - 官方路由管理器
- **Element Plus** - Vue 3组件库

### 工具库
- **Axios** - HTTP客户端
- **@element-plus/icons-vue** - Element Plus图标库

### 构建工具
- **Vite** - 下一代前端构建工具

---

## 🚀 快速开始

### 1. 安装依赖
```bash
cd vue
npm install
```

### 2. 启动开发服务器
```bash
npm run dev
```

### 3. 构建生产版本
```bash
npm run build
```

### 4. 预览生产构建
```bash
npm run preview
```

---

## 📝 开发规范

### 1. 命名规范
- **文件**: PascalCase (如: `ListView.vue`)
- **变量**: camelCase (如: `userData`)
- **常量**: UPPER_SNAKE_CASE (如: `API_BASE_URL`)
- **组件名**: PascalCase (如: `<UserList />`)

### 2. 代码组织
```vue
<template>
  <!-- 模板 -->
</template>

<script setup>
// 导入
import { ref } from 'vue'

// 响应式数据
const data = ref(null)

// 方法
const handleClick = () => {}

// 生命周期
onMounted(() => {})
</script>

<style scoped>
/* 样式 */
</style>
```

### 3. API调用规范
```javascript
// ✅ 推荐
import * as UserApi from '@/api/UserApi.js'
UserApi.getUserPage(params).then(resp => {
  if (resp.code === 2000) {
    // 处理成功
  }
})

// ❌ 避免
import axios from 'axios'
axios.get('/api/v1/user')
```

---

## 🎨 页面功能详解

### DashboardHome.vue (首页)
**功能**:
- 系统统计卡片（用户、车次、车票、订单数量）
- 欢迎信息和用户角色显示
- 快捷操作按钮（根据权限动态显示）

**待完善**:
- 需要从后端获取真实统计数据
- 可以添加图表展示趋势

### SearchView.vue (车次查询)
**功能**:
- 按起止站点查询
- 按时间范围查询
- 按车次号查询
- 查看车票详情
- 直接购票入口

**特点**:
- 多种查询方式灵活切换
- 实时显示查询结果
- 支持查看和购买车票

### MyOrdersView.vue (我的订单)
**功能**:
- 查看个人购票记录
- 查看个人退票记录
- 在线退票功能
- 订单状态显示

**待完善**:
- 需要后端提供用户订单查询接口
- 可以添加订单筛选功能

---

## 🔐 权限控制

### 角色类型
- **admin** - 管理员（全部权限）
- **user** - 普通用户（部分权限）

### 权限分布
| 功能 | 管理员 | 普通用户 |
|------|--------|----------|
| 用户管理 | ✅ | ❌ |
| 车次管理 | ✅ | ❌ |
| 车票管理 | ✅ | ❌ |
| 车次查询 | ✅ | ✅ |
| 售票 | ✅ | ✅ |
| 退票 | ✅ | ✅ |
| 订单查询 | ✅ | ✅ (仅自己的) |

---

## 📊 数据流向

```
用户操作 
  ↓
Vue组件 (views/)
  ↓
API调用 (api/)
  ↓
HTTP请求 (request/request.js)
  ↓
后端接口 (/api/v1/*)
  ↓
响应处理
  ↓
更新UI
```

---

## 🔄 状态管理

目前使用组件级状态管理（`ref`, `reactive`），对于跨组件共享的状态：
- 用户信息：localStorage + AuthService
- Token：localStorage + 请求拦截器

如需更复杂的状态管理，可以考虑引入Pinia。

---

## 📦 依赖说明

### 生产依赖
```json
{
  "vue": "^3.5.30",              // Vue 3核心
  "vue-router": "^5.0.4",        // 路由管理
  "element-plus": "^2.13.6",     // UI组件库
  "@element-plus/icons-vue": "^2.3.2",  // 图标库
  "axios": "^1.14.0",            // HTTP客户端
  "qs": "^6.15.0",               // 查询字符串处理
  "vant": "^4.9.24"              // 移动端UI（可选）
}
```

### 开发依赖
```json
{
  "@vitejs/plugin-vue": "^6.0.5",  // Vite Vue插件
  "vite": "^8.0.1"                 // 构建工具
}
```

---

## 🐛 常见问题

### 1. 路由跳转但页面不刷新
**原因**: 使用了相同的组件  
**解决**: 使用 `key` 属性或监听路由变化

### 2. Token过期但未跳转
**原因**: 拦截器配置问题  
**解决**: 检查 `request.js` 中的响应拦截器

### 3. 跨域问题
**原因**: 前后端端口不同  
**解决**: 在 `vite.config.js` 中配置代理

---

## 📞 技术支持

如有问题，请参考：
1. [Vue 3 官方文档](https://cn.vuejs.org/)
2. [Element Plus 文档](https://element-plus.org/zh-CN/)
3. [Vue Router 文档](https://router.vuejs.org/zh/)
4. [Vite 文档](https://cn.vitejs.dev/)

---

## ✨ 未来规划

### 短期目标
- [ ] 完善首页统计数据接口
- [ ] 实现用户订单查询接口
- [ ] 添加更多筛选条件
- [ ] 优化移动端适配

### 长期目标
- [ ] 引入图表库（ECharts）
- [ ] 添加数据导出功能
- [ ] 实现消息通知系统
- [ ] 添加操作日志
- [ ] 国际化支持

---

**最后更新**: 2026年5月24日  
**版本**: v1.0.0

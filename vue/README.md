# 火车售票系统 - 前端

> 基于 Vue 3 + Element Plus 构建的现代化火车售票管理系统

## 🎯 项目简介

这是一个完整的火车售票系统前端应用，提供车次管理、车票管理、售票、退票、订单查询等核心功能。采用现代化的技术栈和优雅的用户界面设计。

## ✨ 主要特性

- 🔐 **用户认证** - 支持管理员和普通用户两种角色
- 🚂 **车次管理** - 车次的增删改查和多种查询方式
- 🎫 **车票管理** - 车票的完整生命周期管理
- 💰 **售票功能** - 便捷的售票流程
- ↩️ **退票功能** - 灵活的退票处理
- 📊 **订单查询** - 全面的订单记录查询
- 🏠 **数据统计** - 直观的首页仪表盘
- 📱 **响应式设计** - 适配各种屏幕尺寸

## 🛠️ 技术栈

- **框架**: Vue 3 (Composition API)
- **路由**: Vue Router 4
- **UI库**: Element Plus
- **HTTP**: Axios
- **构建**: Vite
- **图标**: Element Plus Icons

## 📦 快速开始

### 前置要求

- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装

```bash
# 进入项目目录
cd vue

# 安装依赖
npm install
```

### 开发

```bash
# 启动开发服务器
npm run dev

# 访问 http://localhost:5173
```

### 构建

```bash
# 生产环境构建
npm run build

# 预览构建结果
npm run preview
```

## 📁 项目结构

```
vue/
├── src/
│   ├── api/              # API接口定义
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   ├── request/          # HTTP请求封装
│   ├── router/           # 路由配置
│   ├── service/          # 服务层
│   ├── views/            # 页面视图
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html
├── package.json
└── vite.config.js
```

详细的项目结构请查看 [PROJECT_STRUCTURE.md](./PROJECT_STRUCTURE.md)

## 🚀 核心功能模块

### 1. 用户管理
- 用户登录/注册
- 用户信息管理
- 角色权限控制

### 2. 车次管理
- 车次列表（分页、搜索）
- 车次增删改查
- 按站点/时间/车次号查询 ⭐新增

### 3. 车票管理
- 车票列表（分页、搜索）
- 车票增删改查
- 按车次查询车票

### 4. 售票管理
- 在线售票
- 选择车次和座位
- 自动关联用户

### 5. 退票管理
- 在线退票
- 退票记录查询
- 退票状态跟踪

### 6. 订单查询
- 全部订单记录（管理员）
- 我的订单（普通用户）⭐新增
- 购票/退票记录分离展示

### 7. 首页仪表盘 ⭐新增
- 系统统计数据
- 快捷操作入口
- 用户信息展示

## 📖 文档

- [前端适配说明](./FRONTEND_ADAPTATION.md) - 前后端适配详情
- [新增功能说明](./NEW_FEATURES.md) - 最新添加的功能
- [项目结构说明](./PROJECT_STRUCTURE.md) - 详细的目录结构和开发规范

## 🔌 API接口

所有API接口都位于 `src/api/` 目录下：

- `LoginApi.js` - 登录接口
- `RegisterApi.js` - 注册接口
- `UserApi.js` - 用户管理接口
- `TrainApi.js` - 车次管理接口
- `TicketApi.js` - 车票管理接口
- `SaleApi.js` - 售票接口
- `RefundApi.js` - 退票接口
- `OrdersApi.js` - 订单查询接口
- `ExtraApi.js` - 扩展接口（预留）

## 🎨 界面预览

### 登录页面
- 用户名/密码/角色选择
- 优雅的渐变背景
- 表单验证

### 首页仪表盘
- 统计卡片展示
- 欢迎信息
- 快捷操作按钮

### 车次查询
- 多种查询条件
- 实时查询结果
- 车票查看和购买

### 我的订单
- 购票记录
- 退票记录
- 在线退票功能

## 🔐 权限说明

| 功能 | 管理员 | 普通用户 |
|------|--------|----------|
| 用户管理 | ✅ | ❌ |
| 车次管理 | ✅ | ❌ |
| 车票管理 | ✅ | ❌ |
| 车次查询 | ✅ | ✅ |
| 售票 | ✅ | ✅ |
| 退票 | ✅ | ✅ |
| 订单查询 | 全部订单 | 仅自己的订单 |

## 📝 开发规范

### 代码风格
- 使用 Composition API (`<script setup>`)
- 统一的命名规范
- 清晰的注释

### API调用
```javascript
import * as TrainApi from '@/api/TrainApi.js'

// 查询车次
TrainApi.getTrainPage(params).then(resp => {
  if (resp.code === 2000) {
    // 处理成功响应
  }
})
```

### 路由跳转
```javascript
import { useRouter } from 'vue-router'

const router = useRouter()
router.push('/dashboard/train')
```

## 🐛 常见问题

### 1. 如何修改后端地址？
在 `src/request/request.js` 中修改 Axios 实例的 `baseURL`。

### 2. Token存储在哪里？
Token存储在 `localStorage` 中，键名为 `token`。

### 3. 如何添加新页面？
1. 在 `src/views/` 下创建页面组件
2. 在 `src/router/index.js` 中添加路由
3. 在 `Dashboard.vue` 中添加菜单项

### 4. 如何实现权限控制？
使用 `authService.getUser()` 获取用户信息，根据 `role` 字段判断权限。

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目仅供学习和参考使用。

## 👥 联系方式

如有问题或建议，请通过以下方式联系：
- 提交 Issue
- 发送邮件

## 🙏 致谢

感谢以下开源项目：
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Vite](https://vitejs.dev/)
- [Axios](https://axios-http.com/)

---

**Happy Coding! 🎉**

# 火车售票系统前端适配说明

## 项目概述
已将原仓库管理项目的前端适配为火车售票系统，包含以下功能模块：

## 功能模块

### 1. 用户管理
- 用户登录（支持管理员和普通用户）
- 用户注册
- 用户信息管理（管理员可编辑和删除）

### 2. 车次管理
- 车次列表查询（分页、搜索）
- 新增车次
- 编辑车次
- 删除车次
- 按车次号、起止站点、时间范围查询

### 3. 车票管理
- 车票列表查询（分页、搜索）
- 新增车票
- 编辑车票
- 删除车票
- 按车次ID查询车票

### 4. 售票管理
- 售票功能（选择车次、车票、站点）

### 5. 退票管理
- 退票功能（输入售票记录ID）

### 6. 订单查询
- 售票记录查询
- 退票记录查询

## 技术栈
- Vue 3
- Element Plus
- Vue Router
- Axios

## 主要修改内容

### 前端修改
1. **删除不需要的文件**
   - 删除了部门管理（dept）
   - 删除了库存管理（stock）
   - 删除了API测试工具（ApiTester）

2. **新增页面**
   - `views/train/ListView.vue` - 车次管理
   - `views/ticket/ListView.vue` - 车票管理
   - `views/sale/SellView.vue` - 售票
   - `views/refund/RefundView.vue` - 退票

3. **更新页面**
   - `views/user/ListView.vue` - 适配后端用户实体
   - `views/orders/ListView.vue` - 订单查询
   - `views/Login.vue` - 适配后端登录接口
   - `views/Register.vue` - 适配后端注册接口
   - `views/Dashboard.vue` - 更新侧边栏菜单

4. **API文件**
   - `api/UserApi.js` - 用户相关API
   - `api/TrainApi.js` - 车次相关API
   - `api/TicketApi.js` - 车票相关API
   - `api/SaleApi.js` - 售票相关API
   - `api/RefundApi.js` - 退票相关API
   - `api/OrdersApi.js` - 订单相关API
   - `api/LoginApi.js` - 登录API
   - `api/RegisterApi.js` - 注册API

5. **服务文件**
   - `service/AuthService.js` - 认证服务（新建）

6. **路由配置**
   - 更新了所有路由，映射到新的页面

### 后端修改
为了统一返回JSON格式，修改了以下Controller方法：

1. **TrainController**
   - `add()` - 返回RespEntity
   - `update()` - 返回RespEntity
   - `delete()` - 返回RespEntity
   - `queryByNumber()` - 返回RespEntity
   - `queryByTime()` - 返回RespEntity

2. **TicketController**
   - `add()` - 返回RespEntity
   - `update()` - 返回RespEntity
   - `delete()` - 返回RespEntity
   - `getByTrain()` - 返回RespEntity

## 启动说明

### 后端启动
```bash
cd spring
mvn spring-boot:run
```

### 前端启动
```bash
cd vue
npm install
npm run dev
```

## API接口规范

所有接口统一返回格式：
```json
{
  "code": 2000,
  "msg": "成功",
  "data": {}
}
```

### 主要接口路径
- 登录：`POST /api/v1/login`
- 注册：`POST /api/v1/reg`
- 用户管理：`/api/v1/g/user`, `/api/v1/admin/user/*`
- 车次管理：`/api/v1/train/*`
- 车票管理：`/api/v1/ticket/*`
- 售票：`POST /api/v1/sale/do`
- 退票：`POST /api/v1/refund/do`

## 注意事项

1. 前端使用Bearer Token进行身份验证
2. 所有需要认证的请求都会在请求头中携带Token
3. Token过期或无效时会自动跳转到登录页
4. 管理员和普通用户有不同的权限控制

## 待完善功能

1. 订单查询页面目前只显示空表格，需要后端提供对应的列表接口
2. 可以添加更完善的表单验证
3. 可以添加更多的查询条件（如按日期范围查询车次等）
4. 可以添加图表统计功能

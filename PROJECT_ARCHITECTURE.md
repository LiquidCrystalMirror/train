# 火车票调度系统 — 项目架构文档

## 技术栈
| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.5.15 + MyBatis-Plus 3.5.10.1 |
| 前端框架 | Vue 3 (Composition API) + Vite |
| 数据库 | MySQL 8.0 |
| 认证 | JWT (jjwt 0.11.5) |
| 构建 | Maven (后端) / npm (前端) |

## 数据库表（11张）
```
carriage_info          — 车厢模板（车厢号+座位号+座位类型），作为批量生成车票的模板
departure_schedule     — 发车计划（train_id+departure_time+router_id）
price_schedule         — 价格梯度（train_id+station_count→price）
refund_info            — 退票记录
router                 — 路线基本信息（router_id, name, total_duration）
router_station         — 路线途经站点（router_id→station_seq→station_id, stay_minutes）
sale_info              — 订单信息（购票记录）
station                — 站点字典
station_connection     — 站点联通表（无向图，存通行时间）
ticket_info            — 车票信息（train_id+carriage+seat+status+departure_time）
ticket_inventory       — 座位库存（train_id+departure_time+seat_type→total/sold/remaining）
train_info             — 车次信息（train_number+router_id+oppsite_router_id）
train_schedule_watermark — 水位表（每车次一条，存最新发车/到达时间）
user                   — 用户表
```

## 核心业务流程

### 1. 发车计划创建流程 (createScheduleWithValidation)
```
前端 → /api/v1/departure/create
  → DepartureController
  → DepartureScheduleServiceImpl.createScheduleWithValidation()
     ├─ 验证列车存在
     ├─ 验证路线ID有效
     ├─ 获取路线站点列表，校验价格梯度完整性
     ├─ 查询水位表，校验24小时内规则
     ├─ 校验方向交替（往程/返程交替）
     ├─ 计算到达时间，检测时间冲突
     ├─ 保存 departure_schedule 记录
     └─ 更新 train_schedule_watermark 水位表

⚠️ 缺失：此处未调用车票自动生成 → 需修复
```

### 2. 车票自动生成流程 (generateTicketsWithoutWatermarkCheck)
```
TicketServiceImpl.generateTicketsWithoutWatermarkCheck(trainId, departureTime)
  ├─ 验证列车存在
  ├─ 检查是否重复生成（trainId+departureTime唯一）
  ├─ 从 carriage_info 获取所有车厢模板
  ├─ 遍历模板批量创建 ticket_info（状态=可售）
  ├─ 批量保存车票（saveBatch）
  └─ 按 seat_type 分组统计，初始化 ticket_inventory 库存
```

### 3. 购票流程 (sellTicket)
```
SaleServiceImpl.sellTicket()
  ├─ 校验车票状态（必须"可售"）
  ├─ 乐观锁扣减库存（remainingCount为条件）
  ├─ 根据起止站点序号计算站数→查价格→乘座位类型系数
  ├─ 保存 sale_info 订单
  └─ 更新 ticket_info 状态为"已售"
```

## 后端包结构
```
com.example.ticket
├── config/          — MyBatisPlusConfig, MyWebConfig(JWT拦截器), EnvConfig
├── controller/      — 14个REST Controller
├── entity/          — 14个实体类（对应数据库表）
├── enums/           — 枚举（SeatTypeEnum等）
├── exception/       — BusinessException, GlobalExceptionHandler
├── interceptor/     — JWT认证拦截器
├── mapper/          — MyBatis Mapper接口 + resources/mapper/*.xml
├── service/         — 服务接口 + impl实现类（12个Service）
├── util/            — ApiResult, DepartureTimeValidator等工具类
├── vo/              — 视图对象 TrainScheduleQueryVO
└── TicketApplication.java — 启动类
```

## 前端路由与页面
```
/ (/)                    → 首页重定向
/login                   → Login.vue
/register                → Register.vue
/user/dashboard          → user/UserDashboard.vue
/user/home               → user/UserHome.vue
/user/orders             → user/orders/OrdersView.vue
/user/refund             → user/refund/RefundView.vue
/user/train              → user/train/TrainView.vue
/admin/dashboard         → admin/AdminDashboard.vue
/admin/home              → admin/AdminHome.vue
/admin/train/list        → admin/train/ListView.vue
/admin/train/search      → admin/train/SearchView.vue
/admin/station/list      → admin/station/...
/admin/route/list        → admin/route/...
/admin/departure/list    → admin/departure/...
/admin/sale/list         → admin/sale/...
/admin/orders/list       → admin/orders/...
/admin/ticket/list       → admin/ticket/...
/admin/refund/list       → admin/refund/...
/admin/user/list         → admin/user/...
```

## 前端 API 模块（14个）
```
DepartureApi.js  — 发车计划CRUD
ExtraApi.js      — 额外功能
LoginApi.js      — 登录
OrdersApi.js     — 订单查询
RefundApi.js     — 退票
RegisterApi.js   — 注册
RouteApi.js      — 路线管理
SaleApi.js       — 售票
StationApi.js    — 站点管理
StockApi.js      — 库存查询
TicketApi.js     — 车票查询
TrainApi.js      — 车次管理
UserApi.js       — 用户管理
```

## 关键设计决策
- **水位表**：每车次仅存最新一条记录，控制发车时间无需拉太远
- **方向交替**：通过 route_id 末位判断（偶数=往程，奇数=返程）
- **价格梯度**：按站点数阶梯定价 × 座位类型系数
- **乐观锁库存**：用 remainingCount 做条件扣减，防止超卖
- **危险时间**：已存在车次的运行时间段内禁止生成新车票

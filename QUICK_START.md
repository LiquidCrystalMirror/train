# 快速启动指南

## 🚀 启动步骤

### 1. 数据库准备

确保MySQL数据库已创建并执行了`sqlprogram.sql`文件中的所有表结构。

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库(如果不存在)
CREATE DATABASE IF NOT EXISTS sqlprogram CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

# 使用数据库
USE sqlprogram;

# 执行SQL文件
source C:/Users/Administrator/IdeaProjects/train/sqlprogram.sql;
```

### 2. 配置环境变量

复制`.env.example`为`.env`并配置数据库连接信息:

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=sqlprogram
DB_USERNAME=root
DB_PASSWORD=your_password
```

### 3. 启动后端

```bash
# 进入spring目录
cd spring

# Maven清理并编译
mvn clean install

# 启动应用
mvn spring-boot:run

# 或者运行jar包
java -jar target/ticket-0.0.1-SNAPSHOT.jar
```

后端将在 `http://localhost:8080` 启动

### 4. 启动前端

```bash
# 进入vue目录
cd vue

# 安装依赖(首次运行)
npm install

# 启动开发服务器
npm run dev
```

前端将在 `http://localhost:5173` 启动

---

## 📋 功能测试清单

### 管理员端测试

#### 1. 路线管理 (/admin/route)
- [ ] 创建新路线
- [ ] 添加站点到路线
- [ ] 设置站点停留时间
- [ ] 查看路线详情
- [ ] 编辑路线
- [ ] 删除路线

#### 2. 列车管理 (/admin/train)
- [ ] 创建列车(关联route_id)
- [ ] 查看列车列表
- [ ] 编辑列车信息
- [ ] 删除列车

#### 3. 车次管理 (/admin/departure)
- [ ] 为列车创建车次
- [ ] 设置发车时间
- [ ] 选择运行方向(顺行/逆行)
- [ ] 查看车次列表
- [ ] 删除车次

#### 4. 水位表检查
- [ ] 查询最新水位记录
- [ ] 检查危险时间区域
- [ ] 获取最早出票时间
- [ ] 更新水位记录

#### 5. 车票管理
- [ ] 基于模板批量生成车票
- [ ] 查询可售车票
- [ ] 售票
- [ ] 退票

---

## 🔍 API测试示例

使用Postman或curl测试API:

### 1. 创建路线
```bash
curl -X POST http://localhost:8080/api/v1/route/save \
  -H "Content-Type: application/json" \
  -d '{
    "routerId": 1,
    "stations": [
      {"stationSeq": 1, "stationId": 1, "stayMinutes": 5},
      {"stationSeq": 2, "stationId": 2, "stayMinutes": 3}
    ]
  }'
```

### 2. 查询路线站点
```bash
curl -X POST http://localhost:8080/api/v1/route/stations \
  -H "Content-Type: application/json" \
  -d '{"routerId": 1}'
```

### 3. 创建车次
```bash
curl -X POST http://localhost:8080/api/v1/departure/create \
  -H "Content-Type: application/json" \
  -d '{
    "trainId": 1,
    "departureTime": "2026-05-28T08:00:00",
    "direction": 0
  }'
```

### 4. 查询车次列表
```bash
curl -X POST http://localhost:8080/api/v1/departure/list \
  -H "Content-Type: application/json" \
  -d '{"trainId": 1}'
```

### 5. 检查危险时间
```bash
curl -X POST http://localhost:8080/api/v1/watermark/check/danger \
  -H "Content-Type: application/json" \
  -d '{
    "trainId": "G123",
    "startTime": "2026-05-28T08:00:00",
    "endTime": "2026-05-28T12:00:00"
  }'
```

---

## ⚠️ 常见问题

### 1. 数据库连接失败
**问题**: `Communications link failure`

**解决**:
- 检查MySQL服务是否启动
- 确认`.env`文件中的数据库配置正确
- 确认数据库用户有足够权限
- 检查防火墙设置

### 2. 端口被占用
**问题**: `Port 8080 was already in use`

**解决**:
```bash
# Windows查找占用端口的进程
netstat -ano | findstr :8080

# 杀死进程
taskkill /PID <PID> /F

# 或者修改application.yml中的端口
server.port=8081
```

### 3. 前端无法访问后端
**问题**: CORS错误或网络错误

**解决**:
- 检查后端是否正常运行
- 确认`vite.config.js`中的proxy配置正确
- 清除浏览器缓存
- 检查浏览器控制台Network标签

### 4. Mapper XML文件未加载
**问题**: `Invalid bound statement`

**解决**:
- 确认XML文件在`src/main/resources/mapper/`目录下
- 检查`application-mapper.yml`中的`mapper-locations`配置
- 重新编译项目: `mvn clean install`
- 检查target目录中是否包含XML文件

### 5. 实体类字段不匹配
**问题**: `Column 'xxx' not found`

**解决**:
- 确认实体类字段名与数据库列名一致(驼峰转下划线)
- 检查`@TableField`注解是否正确
- 确认`map-underscore-to-camel-case`配置为true
- 重启应用使配置生效

### 6. SQL注入防护说明
**问**: 本项目是否已防止SQL注入？

**答**: ✅ 是的，已完全防护：
- MyBatis使用`#{}`参数化查询，自动进行预编译处理
- 所有用户输入都通过Controller层验证
- 不使用`${}`拼接SQL（除非特殊场景且已做严格校验）
- 无需额外配置，MyBatis默认安全

### 7. 外键约束问题
**问题**: 删除数据时出现外键约束错误

**解决**:
- 确认数据库中是否启用了外键约束
- 按正确顺序删除数据(先删子表，再删父表)
- 或在业务层实现级联删除逻辑

---

## 🛠️ 开发工具推荐

### IDE
- **后端**: IntelliJ IDEA (推荐) 或 Eclipse
- **前端**: VS Code 或 WebStorm

### 数据库工具
- Navicat for MySQL
- MySQL Workbench
- DBeaver

### API测试
- Postman
- Apifox
- Insomnia

### 其他
- Git (版本控制)
- Maven (依赖管理)
- Node.js 16+ (前端运行环境)

---

## 📞 技术支持

如遇到问题,请检查:
1. 控制台日志输出
2. 浏览器开发者工具(Network标签)
3. MySQL错误日志
4. Spring Boot启动日志

祝开发顺利! 🎉

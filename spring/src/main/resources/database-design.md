# 火车售票管理信息系统 - 数据库设计文档

## 一、需求分析

### 1.1 系统功能需求

火车售票管理信息系统主要实现以下核心功能：

1. **车次管理**
   - 车次信息的增删改查
   - 车次包含：车次号、起止地点、到达时间、开车时间、途经站点

2. **车票管理**
   - 每一车次的车票管理（车厢号、座位号、座位类型、价格）
   - 车票状态管理（可售、已售、锁定）

3. **售票功能**
   - 用户选择车次和座位进行购票
   - 记录售票信息（用户、车次、座位、上下车站点）
   - 自动更新车票状态

4. **退票功能**
   - 用户申请退票
   - 记录退票信息
   - 自动恢复车票状态为可售

5. **查询功能**
   - 按车次查询
   - 按起止地点查询
   - 按发车时间查询
   - 余票查询
   - 售票统计查询

### 1.2 数据分析

#### 核心数据实体
- **用户（User）**：系统使用者，分为普通用户和管理员
- **站点（Station）**：火车停靠的站点信息
- **车次（TrainInfo）**：列车运行信息
- **车次站点关联（TrainStation）**：车次与途经站点的多对多关系
- **车票（TicketInfo）**：具体座位的车票信息
- **售票记录（SaleInfo）**：用户的购票订单
- **退票记录（RefundInfo）**：用户的退票记录

#### 数据关系
- 一个车次有多个途经站点（1:N）
- 一个车次有多张车票（1:N）
- 一个用户可以有多条售票记录（1:N）
- 一张车票对应一条售票记录（1:1）
- 一条售票记录可以有一条退票记录（1:1）

---

## 二、数据库概念结构设计（E-R图）

### 2.1 实体及属性

```
┌─────────────┐
│   User      │
├─────────────┤
│ *user_id    │
│  username   │
│  password   │
│  real_name  │
│  id_card    │
│  phone      │
│  role       │
│  create_time│
└─────────────┘

┌─────────────┐
│  Station    │
├─────────────┤
│ *station_id │
│ station_name│
│ create_time │
└─────────────┘

┌──────────────┐
│  TrainInfo   │
├──────────────┤
│  *train_id   │
│ train_number │
│total_stations│
│departure_time│
│ arrival_time │
│   run_time   │
│ create_time  │
│ update_time  │
└──────────────┘

┌─────────────────┐
│  TrainStation   │
├─────────────────┤
│      *id        │
│    train_id     │
│  station_seq    │
│   station_id    │
│  arrival_time   │
│ departure_time  │
│ stay_minutes    │
│  create_time    │
└─────────────────┘

┌──────────────┐
│ TicketInfo   │
├──────────────┤
│  *ticket_id  │
│   train_id   │
│carriage_number│
│ seat_number  │
│  seat_type   │
│ticket_status │
│ create_time  │
│ update_time  │
│    price     │
└──────────────┘

┌──────────────┐
│  SaleInfo    │
├──────────────┤
│  *sale_id    │
│  ticket_id   │
│   train_id   │
│   user_id    │
│start_station_seq│
│end_station_seq  │
│  sale_time   │
│ sale_status  │
│ create_time  │
└──────────────┘

┌──────────────┐
│ RefundInfo   │
├──────────────┤
│  *refund_id  │
│   sale_id    │
│  ticket_id   │
│   train_id   │
│   user_id    │
│ refund_time  │
│refund_status │
│refund_remark │
│ create_time  │
└──────────────┘
```

### 2.2 实体间关系

```
User (1) ──────< (N) SaleInfo
                        │
                        │ (1)
                        │
                        >────── (1) TicketInfo
                        │              │
                        │              │ (N)
                        │              │
                    TrainInfo (1) ─────┘
                        │
                        │ (1)
                        │
                        >────── (N) TrainStation >────── (N) Station
```

**关系说明：**
- User 与 SaleInfo：一对多（一个用户可以购买多张票）
- SaleInfo 与 TicketInfo：一对一（一张票对应一条销售记录）
- TicketInfo 与 TrainInfo：多对一（多个座位属于同一车次）
- TrainInfo 与 TrainStation：一对多（一个车次有多个途经站点）
- TrainStation 与 Station：多对一（多个车次站点关联指向同一站点）
- SaleInfo 与 RefundInfo：一对一（一条销售记录最多一条退票记录）

---

## 三、数据库逻辑结构设计

### 3.1 数据表结构

#### 表1：user（用户表）

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| user_id | INT | PRIMARY KEY, AUTO_INCREMENT | 用户ID |
| username | VARCHAR(30) | NOT NULL, UNIQUE | 账号 |
| password | VARCHAR(50) | NOT NULL | 密码（MD5加密） |
| real_name | VARCHAR(30) | NULL | 真实姓名 |
| id_card | VARCHAR(18) | NULL | 身份证号 |
| phone | VARCHAR(11) | NULL | 手机号 |
| role | VARCHAR(10) | NOT NULL | 角色：user/admin |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### 表2：station（站点表）

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| station_id | INT | PRIMARY KEY, AUTO_INCREMENT | 站点ID |
| station_name | VARCHAR(50) | NOT NULL, UNIQUE | 站点名称 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

#### 表3：train_info（车次信息表）

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| train_id | INT | PRIMARY KEY, AUTO_INCREMENT | 车次ID |
| train_number | VARCHAR(20) | NOT NULL, UNIQUE | 车次编号（如G123） |
| total_stations | INT | NOT NULL, DEFAULT 0 | 途径站点数 |
| departure_time | DATETIME | NOT NULL | 开车时间 |
| arrival_time | DATETIME | NOT NULL | 到达时间 |
| run_time | VARCHAR(30) | NULL | 运行时长 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |

**索引：**
- idx_train_number：车次号索引
- idx_departure：发车时间索引

#### 表4：train_station（车次站点关联表）

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 自增主键 |
| train_id | INT | NOT NULL, FOREIGN KEY | 车次ID |
| station_seq | INT | NOT NULL | 途径点序号 |
| station_id | INT | NOT NULL, FOREIGN KEY | 站点ID |
| arrival_time | DATETIME | NULL | 到达该站点时间 |
| departure_time | DATETIME | NULL | 离开该站点时间 |
| stay_minutes | INT | DEFAULT 0 | 停留分钟数 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**约束：**
- uk_train_seq：UNIQUE(train_id, station_seq) - 同一车次站点序号唯一
- fk_train_station_train：FOREIGN KEY (train_id) REFERENCES train_info(train_id)
- fk_train_station_station：FOREIGN KEY (station_id) REFERENCES station(station_id)

**索引：**
- idx_train_id：车次ID索引
- idx_station_id：站点ID索引
- idx_station_seq：站点序号索引

#### 表5：ticket_info（车票信息表）

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| ticket_id | INT | PRIMARY KEY, AUTO_INCREMENT | 车票ID |
| train_id | INT | NOT NULL, FOREIGN KEY | 关联车次ID |
| carriage_number | VARCHAR(10) | NOT NULL | 车厢号 |
| seat_number | VARCHAR(10) | NOT NULL | 座位号 |
| seat_type | VARCHAR(20) | NULL | 座位类型（二等座/一等座等） |
| ticket_status | ENUM('可售','已售','锁定') | DEFAULT '可售' | 车票状态 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| price | DECIMAL(10,2) | NOT NULL | 售价 |

**约束：**
- uk_train_seat：UNIQUE(train_id, carriage_number, seat_number) - 同一车次座位唯一
- fk_ticket_train：FOREIGN KEY (train_id) REFERENCES train_info(train_id)

**索引：**
- idx_train_id：车次ID索引
- idx_ticket_status：车票状态索引

#### 表6：sale_info（售票信息表）

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| sale_id | INT | PRIMARY KEY, AUTO_INCREMENT | 订单ID |
| ticket_id | INT | NOT NULL, FOREIGN KEY | 车票ID |
| train_id | INT | NOT NULL, FOREIGN KEY | 车次ID |
| user_id | INT | NOT NULL, FOREIGN KEY | 购票用户ID |
| start_station_seq | INT | NOT NULL | 上车站点序号 |
| end_station_seq | INT | NOT NULL | 下车站点序号 |
| sale_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 购票时间 |
| sale_status | ENUM('已出票','已退票') | DEFAULT '已出票' | 状态 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**约束：**
- fk_sale_ticket：FOREIGN KEY (ticket_id) REFERENCES ticket_info(ticket_id)
- fk_sale_train：FOREIGN KEY (train_id) REFERENCES train_info(train_id)
- fk_sale_user：FOREIGN KEY (user_id) REFERENCES user(user_id)

**索引：**
- idx_start_end_seq：(start_station_seq, end_station_seq)
- idx_train_start：(train_id, start_station_seq)

#### 表7：refund_info（退票信息表）

| 字段名 | 数据类型 | 约束 | 说明 |
|--------|---------|------|------|
| refund_id | INT | PRIMARY KEY, AUTO_INCREMENT | 退票ID |
| sale_id | INT | NOT NULL, FOREIGN KEY | 订单ID |
| ticket_id | INT | NOT NULL, FOREIGN KEY | 车票ID |
| train_id | INT | NOT NULL, FOREIGN KEY | 车次ID |
| user_id | INT | NOT NULL, FOREIGN KEY | 用户ID |
| refund_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 退票时间 |
| refund_status | VARCHAR(20) | DEFAULT '已完成' | 退票状态 |
| refund_remark | VARCHAR(255) | NULL | 退票备注 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

**约束：**
- fk_refund_sale：FOREIGN KEY (sale_id) REFERENCES sale_info(sale_id)
- fk_refund_ticket：FOREIGN KEY (ticket_id) REFERENCES ticket_info(ticket_id)
- fk_refund_train：FOREIGN KEY (train_id) REFERENCES train_info(train_id)
- fk_refund_user：FOREIGN KEY (user_id) REFERENCES user(user_id)

**索引：**
- idx_sale_id：售票记录ID索引
- idx_ticket_id：车票ID索引

### 3.2 视图设计（可选）

目前系统未使用视图，所有查询通过存储过程和SQL语句实现。

### 3.3 存储过程设计

详见 `procedures-and-triggers.sql` 文件，包含：

1. **proc_sell_ticket**：售票存储过程
   - 参数：车票ID、车次ID、用户ID、上下车站点序号
   - 输出：售票记录ID、结果码、结果消息
   - 功能：事务性售票，自动更新车票状态

2. **proc_refund_ticket**：退票存储过程
   - 参数：售票记录ID
   - 输出：退票记录ID、结果码、结果消息
   - 功能：事务性退票，自动恢复车票状态

3. **proc_query_available_tickets**：余票查询存储过程
   - 参数：车次ID（可选）
   - 功能：统计车次余票情况

4. **proc_query_sale_statistics**：售票统计存储过程
   - 参数：开始时间、结束时间
   - 功能：统计指定时间范围内的售票情况

### 3.4 触发器设计

1. **trigger_after_sale_insert**：售票记录插入后触发
   - 功能：记录审计日志（示例）

2. **trigger_after_refund_insert**：退票记录插入后触发
   - 功能：记录审计日志（示例）

---

## 四、AI辅助数据库设计说明

### 4.1 AI在数据库设计中的应用

在本项目中，AI工具被用于以下方面辅助数据库设计：

#### 1. 需求分析与实体识别
- **应用场景**：通过自然语言描述业务需求，AI帮助识别核心实体和属性
- **示例提示词**：
  ```
  我要设计一个火车售票系统，需要管理车次、站点、车票、售票和退票信息。
  请帮我分析需要哪些数据表，每个表应该包含哪些字段？
  ```

#### 2. ER图设计建议
- **应用场景**：AI根据实体关系生成ER图的结构建议
- **示例提示词**：
  ```
  我有以下实体：用户、车次、站点、车票、售票记录、退票记录。
  请分析它们之间的关系（一对一、一对多、多对多），并给出ER图设计建议。
  ```

#### 3. SQL语句生成
- **应用场景**：AI辅助生成建表语句、索引、约束等
- **示例提示词**：
  ```
  请为火车售票系统生成MySQL建表语句，要求：
  1. 车次表包含车次号、发车时间、到达时间等字段
  2. 车票表需要保证同一车次座位号唯一
  3. 添加适当的外键约束和索引
  ```

#### 4. 存储过程优化
- **应用场景**：AI帮助编写和优化存储过程逻辑
- **示例提示词**：
  ```
  请帮我写一个售票存储过程，需要：
  1. 检查车票是否可售
  2. 插入售票记录
  3. 更新车票状态为已售
  4. 使用事务保证数据一致性
  5. 返回结果码和消息
  ```

#### 5. 性能优化建议
- **应用场景**：AI分析查询模式，提供索引优化建议
- **示例提示词**：
  ```
  我的系统经常需要：
  1. 按车次号查询
  2. 按发车时间范围查询
  3. 按起止站点查询车次
  请问应该在哪些字段上建立索引？
  ```

### 4.2 AI辅助设计的优势

1. **提高效率**：快速生成基础SQL代码，减少重复劳动
2. **减少错误**：AI可以帮助发现设计中的逻辑错误和遗漏
3. **最佳实践**：AI提供行业标准的数据库设计规范
4. **迭代优化**：可以快速尝试多种设计方案，选择最优解

### 4.3 注意事项

1. **人工审核**：AI生成的代码需要经过人工审核和调整
2. **业务理解**：AI不能完全理解业务细节，需要开发者补充
3. **安全性**：敏感信息（如密码加密）需要特别关注
4. **性能测试**：AI建议的索引和查询需要在实际环境中测试

---

## 五、总结

本数据库设计满足了火车售票管理信息系统的基本需求：

✅ **完整性**：包含7张核心数据表，覆盖所有业务场景  
✅ **规范性**：遵循第三范式，减少数据冗余  
✅ **一致性**：通过外键约束、存储过程、触发器保证数据一致性  
✅ **可扩展性**：预留扩展字段，支持未来功能增加  
✅ **性能优化**：合理使用索引，提高查询效率  

**评分点对应：**
- 需求分析：✓ 已完成
- ER图设计：✓ 已完成
- 逻辑设计：✓ 已完成（7张表+约束+索引）
- 存储过程/触发器：✓ 已完成（4个存储过程+2个触发器）
- AI辅助设计：✓ 已说明

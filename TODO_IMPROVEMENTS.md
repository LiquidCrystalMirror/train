# 待完善功能清单

## ✅ 已完成的核心功能

- ✅ 数据库表结构与代码完全匹配
- ✅ 路线管理（创建、编辑、删除、查询）
- ✅ 车次管理（创建、删除、查询）
- ✅ 水位表机制（危险时间检查）
- ✅ 车票批量生成（基于车厢模板）
- ✅ SQL注入防护（MyBatis #{ }参数化查询）
- ✅ 前后端完整链路打通

---

## ⚠️ 需要完善的功能

### 1. 列车管理页面优化 (优先级: 高)

**文件**: `vue/src/views/admin/train/ListView.vue`

**需要添加**:
- [ ] 显示列车的`router_id`字段
- [ ] 创建/编辑列车时可选择路线
- [ ] 路线下拉框联动显示

**实现示例**:
```vue
<el-form-item label="所属路线">
  <el-select v-model="trainForm.routerId" placeholder="选择路线">
    <el-option
      v-for="route in routeList"
      :key="route.routerId"
      :label="`路线${route.routerId}`"
      :value="route.routerId"
    />
  </el-select>
</el-form-item>
```

---

### 2. 票价计算逻辑 (优先级: 高)

**当前问题**: `price_schedule`表存在但未使用

**需要实现**:
- [ ] 根据起点站和终点站的站点数量计算票价
- [ ] 在购票时自动查询价格
- [ ] 管理员可配置不同列车的价格策略

**实现思路**:
```java
// 在SaleService中
public Double calculatePrice(Integer trainId, Integer startSeq, Integer endSeq) {
    int stationCount = Math.abs(endSeq - startSeq) + 1;
    return priceScheduleService.getPriceByTrainAndStations(trainId, stationCount);
}
```

---

### 3. 车票生成时集成水位表检查 (优先级: 高)

**当前问题**: 生成车票时未检查水位表

**需要添加**:
- [ ] 生成车票前检查是否在危险时间内
- [ ] 生成车票后自动更新水位表
- [ ] 返回提示信息给管理员

**实现示例**:
```java
@Transactional
public List<TicketInfo> generateTicketsWithWatermarkCheck(
    Integer trainId, 
    LocalDateTime departureTime,
    Integer adminId
) {
    // 1. 获取列车信息
    TrainInfo train = trainService.getById(trainId);
    
    // 2. 检查危险时间
    if (watermarkService.isInDangerZone(
        train.getTrainNumber(), 
        departureTime, 
        departureTime.plusHours(2)
    )) {
        throw new BusinessException("该时间段在危险区域内，无法生成车票");
    }
    
    // 3. 生成车票
    List<TicketInfo> tickets = generateTicketsFromTemplate(trainId, departureTime);
    
    // 4. 更新水位表
    LocalDateTime arriveTime = departureTime.plusMinutes(train.getTimeConsuming());
    watermarkService.updateWatermark(
        train.getTrainNumber(),
        train.getRouterId(),
        departureTime,
        arriveTime,
        adminId
    );
    
    return tickets;
}
```

---

### 4. 车厢模板管理界面 (优先级: 中)

**当前问题**: `carriage_info`表需要初始数据

**需要实现**:
- [ ] 车厢模板列表展示
- [ ] 添加/编辑车厢模板
- [ ] 批量导入模板(Excel)
- [ ] 删除模板

**建议位置**: `/admin/carriage-template`

**初始数据SQL示例**:
```sql
-- 二等座车厢模板
INSERT INTO carriage_info (carriage_number, seat_number, seat_type) VALUES
('01车', '01A', '二等座'),
('01车', '01B', '二等座'),
('01车', '01C', '二等座'),
('01车', '01D', '二等座'),
('01车', '01F', '二等座');
-- ... 更多座位
```

---

### 5. 路线列表查询API (优先级: 中)

**当前问题**: RouteController缺少列表查询接口

**需要添加**:
```java
// RouteController.java
@PostMapping("/list")
public ApiResult<List<Map<String, Object>>> listRoutes() {
    // 查询所有路线及其站点信息
    List<Map<String, Object>> routes = routerStationService.listAllRoutes();
    return ApiResult.success("查询成功", routes);
}
```

**前端调用**:
```javascript
// RouteApi.js
export function listRoutes() {
    return request({
        url: '/api/v1/route/list',
        method: 'post'
    })
}
```

---

### 6. 车次冲突检测优化 (优先级: 中)

**当前问题**: 仅检查同一列车的简单时间冲突

**需要优化**:
- [ ] 考虑站点停留时间
- [ ] 考虑缓冲时间(如30分钟)
- [ ] 检查路线交叉冲突(如果多列车共用部分路线)

**实现思路**:
```java
public boolean hasConflict(Integer trainId, LocalDateTime newDepartureTime) {
    TrainInfo train = trainService.getById(trainId);
    int totalTime = train.getTimeConsuming() + 30; // 包含缓冲时间
    
    LocalDateTime newEndTime = newDepartureTime.plusMinutes(totalTime);
    
    return departureScheduleService.hasConflict(
        trainId, 
        newDepartureTime.minusMinutes(30), // 向前扩展缓冲
        newEndTime.plusMinutes(30)         // 向后扩展缓冲
    );
}
```

---

### 7. 操作日志记录 (优先级: 低)

**需要记录的操作**:
- [ ] 路线创建/修改/删除
- [ ] 车次创建/删除
- [ ] 车票批量生成
- [ ] 水位表更新

**简单实现**:
```java
@Aspect
@Component
public class OperationLogAspect {
    
    @Autowired
    private OperationLogMapper logMapper;
    
    @AfterReturning("@annotation(operation)")
    public void logOperation(JoinPoint joinPoint, OperationLog operation) {
        OperationLogEntity log = new OperationLogEntity();
        log.setOperator(getCurrentUserId());
        log.setOperation(operation.value());
        log.setCreateTime(LocalDateTime.now());
        logMapper.insert(log);
    }
}
```

---

### 8. 前端体验优化 (优先级: 中)

**需要改进**:
- [ ] 添加加载状态(loading)
- [ ] 优化错误提示(message)
- [ ] 表单验证规则
- [ ] 确认对话框二次确认
- [ ] 操作成功后的自动刷新

**示例**:
```vue
<template>
  <el-button 
    type="primary" 
    :loading="saving"
    @click="handleSave"
  >
    保存
  </el-button>
</template>

<script setup>
const saving = ref(false)

const handleSave = async () => {
  saving.value = true
  try {
    await saveRouteStations(data)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败: ' + error.message)
  } finally {
    saving.value = false
  }
}
</script>
```

---

### 9. 数据初始化脚本 (优先级: 高)

**需要创建**: `init-data.sql`

**内容**:
```sql
-- 1. 初始化站点
INSERT INTO station (station_name) VALUES 
('北京站'), ('上海站'), ('广州站'), ('深圳站');

-- 2. 初始化站点联通性
INSERT INTO station_connection (station_a_id, station_b_id, travel_time_minutes) VALUES
(1, 2, 300), -- 北京-上海 5小时
(2, 3, 240), -- 上海-广州 4小时
(3, 4, 60);  -- 广州-深圳 1小时

-- 3. 初始化路线
INSERT INTO router_station (router_id, station_seq, station_id, stay_minutes) VALUES
(1, 1, 1, 10),  -- 路线1: 北京
(1, 2, 2, 15),  -- 路线1: 上海
(1, 3, 3, 10),  -- 路线1: 广州
(1, 4, 4, 0);   -- 路线1: 深圳

-- 4. 初始化列车
INSERT INTO train_info (train_number, time_consuming, router_id) VALUES
('G1001', 610, 1); -- G1001, 总耗时610分钟, 路线1

-- 5. 初始化车厢模板
INSERT INTO carriage_info (carriage_number, seat_number, seat_type) VALUES
('01车', '01A', '二等座'),
('01车', '01B', '二等座'),
('01车', '01C', '二等座'),
('01车', '01D', '二等座'),
('01车', '01F', '二等座');

-- 6. 初始化价格策略
INSERT INTO price_schedule (train_id, station_count, price) VALUES
(1, 2, 500.0),  -- 2站 500元
(1, 3, 800.0),  -- 3站 800元
(1, 4, 1000.0); -- 4站 1000元
```

---

### 10. 全局异常处理优化 (优先级: 低)

**当前**: 已有`GlobalExceptionHandler`

**可优化**:
- [ ] 统一错误码定义
- [ ] 更友好的错误消息
- [ ] 记录异常日志

---

## 📊 优先级说明

- **高优先级**: 影响核心功能使用，建议立即完成
- **中优先级**: 提升用户体验，建议近期完成
- **低优先级**: 锦上添花功能，可后续迭代

---

## 🎯 推荐完成顺序

1. **第一步**: 数据初始化脚本 (快速测试基础功能)
2. **第二步**: 列车管理页面优化 (完善管理流程)
3. **第三步**: 票价计算逻辑 (完善售票功能)
4. **第四步**: 车票生成集成水位表 (完善安全机制)
5. **第五步**: 车厢模板管理 (完善数据准备)
6. **第六步**: 前端体验优化 (提升用户满意度)
7. **第七步**: 其他优化项 (按需实施)

---

## 💡 开发建议

1. **每完成一个功能就测试一次**，确保不影响现有功能
2. **保持代码风格一致**，遵循已有的命名规范
3. **及时提交Git**，方便回滚和协作
4. **编写简单的测试用例**，保证核心逻辑正确
5. **文档同步更新**，记录重要的设计决策

---

## 🔍 测试检查清单

完成每个功能后，请检查:
- [ ] 后端API能正常响应
- [ ] 前端页面能正常显示
- [ ] 数据能正确保存到数据库
- [ ] 边界情况有正确处理(空值、异常输入等)
- [ ] 控制台无错误日志
- [ ] 浏览器Network请求正常

祝开发顺利！🚀

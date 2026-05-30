<template>
  <div class="train-search">
    <el-card>
      <template #header>
        <h3>车次查询</h3>
      </template>

      <!-- 查询条件 -->
      <el-form :model="searchForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="出发站">
              <el-select
                  v-model="searchForm.startStationId"
                  placeholder="请选择出发站"
                  filterable
                  clearable
                  style="width: 100%"
                  @change="onStationChange"
              >
                <el-option
                    v-for="station in stationList"
                    :key="station.stationId"
                    :label="station.stationName"
                    :value="station.stationId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="到达站">
              <el-select
                  v-model="searchForm.endStationId"
                  placeholder="请选择到达站"
                  filterable
                  clearable
                  style="width: 100%"
                  @change="onStationChange"
              >
                <el-option
                    v-for="station in stationList"
                    :key="station.stationId"
                    :label="station.stationName"
                    :value="station.stationId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item>
              <el-button type="primary" @click="handleSearch" :loading="searching">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="发车时间">
              <div class="time-range-wrapper">
                <div class="time-range-display">
                  <el-tag type="info" size="small">
                    查询范围：{{ formatDateTimeRange() }}
                  </el-tag>
                  <el-button
                      link
                      size="small"
                      @click="useTimeRange = !useTimeRange"
                      style="margin-left: 12px"
                  >
                    {{ useTimeRange ? '使用默认范围' : '自定义时间' }}
                  </el-button>
                </div>
                <div v-if="useTimeRange" class="time-range-picker">
                  <el-date-picker
                      v-model="searchForm.startTime"
                      type="datetime"
                      placeholder="开始时间"
                      style="width: 45%; margin-right: 10px"
                      value-format="YYYY-MM-DDTHH:mm:ss"
                  />
                  <span>至</span>
                  <el-date-picker
                      v-model="searchForm.endTime"
                      type="datetime"
                      placeholder="结束时间"
                      style="width: 45%; margin-left: 10px"
                      value-format="YYYY-MM-DDTHH:mm:ss"
                  />
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 查询结果 -->
    <el-card class="result-card">
      <template #header>
        <div class="result-header">
          <h3>查询结果</h3>
          <span v-if="scheduleList.length > 0" class="result-count">共找到 {{ scheduleList.length }} 个车次</span>
        </div>
      </template>

      <div v-if="scheduleList.length > 0" class="train-list">
        <div
            v-for="schedule in scheduleList"
            :key="schedule.trainId"
            class="train-card"
        >
          <div class="train-card-header">
            <div class="train-number">{{ schedule.trainNumber }}</div>
            <div class="train-time">
              <div class="departure">
                <span class="time">{{ schedule.departureTimeStr }}</span>
                <span class="station">{{ getStartStationDisplay() }}</span>
              </div>
              <div class="arrow">→</div>
              <div class="arrival">
                <span class="time">{{ schedule.arrivalTimeStr }}</span>
                <span class="station">{{ getEndStationDisplay() }}</span>
              </div>
            </div>
            <div class="train-info">
              <span class="duration">{{ schedule.duration }}</span>
            </div>
            <el-button
                type="primary"
                size="small"
                @click="viewTickets(schedule)"
            >
              查看车票
            </el-button>
          </div>
        </div>
      </div>
      <div v-else-if="searched" class="empty-result">
        <el-empty description="暂无符合条件的车次" />
      </div>
      <div v-else class="empty-result">
        <el-empty description="请选择出发站和到达站后查询" />
      </div>
    </el-card>

    <!-- 余票库存对话框 -->
    <el-dialog :title="`${currentTrainNumber} - 余票信息`" v-model="ticketDialogVisible" width="750px" @close="resetInventory">
      <div style="margin-bottom: 16px; color: #606266; font-size: 14px; padding: 8px 12px; background: #f5f7fa; border-radius: 6px;">
        📅 发车时间：{{ currentDepartureTimeStr }}
        &nbsp;&nbsp;|&nbsp;&nbsp;
        🚉 {{ getStartStationDisplay() }} → {{ getEndStationDisplay() }}
      </div>

      <el-table
          :data="inventoryList"
          stripe
          border
          style="width: 100%"
          v-loading="ticketLoading"
      >
        <el-table-column prop="seatType" label="座位类型" align="center">
          <template #default="scope">
            {{ getSeatTypeText(scope.row.seatType) }}
          </template>
        </el-table-column>
        <el-table-column prop="price" label="票价" align="center" width="120">
          <template #default="scope">
            <span style="color: #e6a23c; font-weight: bold; font-size: 16px;">
              ¥{{ formatPrice(scope.row.price) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="总票数" align="center" width="100" />
        <el-table-column prop="soldCount" label="已售" align="center" width="100" />
        <el-table-column prop="remainingCount" label="剩余" align="center" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.remainingCount > 0 ? 'success' : 'danger'" size="small">
              {{ scope.row.remainingCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="100">
          <template #default="scope">
            <el-button
                size="small"
                type="primary"
                :disabled="scope.row.remainingCount <= 0 || priceCalculating"
                @click="handleBuyClick(scope.row)"
            >
              购买
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!ticketLoading && inventoryList.length === 0" style="text-align:center;padding:30px;color:#909399;">
        暂无库存信息
      </div>
    </el-dialog>

    <!-- 购票确认对话框 -->
    <el-dialog title="确认购票" v-model="buyDialogVisible" width="480px" @close="resetBuyDialog">
      <div v-if="buyingItem" v-loading="priceCalculating">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="车次">
            <span style="font-weight: bold; color: #409eff;">{{ currentTrainNumber }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="发车时间">{{ currentDepartureTimeStr }}</el-descriptions-item>
          <el-descriptions-item label="座位类型">
            <el-tag size="small" type="warning">{{ getSeatTypeText(buyingItem.seatType) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="出发站">{{ getStartStationDisplay() }}（第{{ startStationSeq }}站）</el-descriptions-item>
          <el-descriptions-item label="到达站">{{ getEndStationDisplay() }}（第{{ endStationSeq }}站）</el-descriptions-item>
          <el-descriptions-item label="票价">
            <span v-if="calculatedPrice !== null" style="color: #e6a23c; font-weight: bold; font-size: 18px;">
              ¥{{ formatPrice(calculatedPrice) }}
            </span>
            <span v-else style="color: #909399;">计算中...</span>
          </el-descriptions-item>
          <el-descriptions-item label="剩余票数">
            <el-tag :type="buyingItem.remainingCount > 0 ? 'success' : 'danger'" size="small">
              {{ buyingItem.remainingCount }} 张
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <el-alert
            v-if="buyingItem.remainingCount > 0 && buyingItem.remainingCount < 5"
            title="温馨提示"
            type="warning"
            description="该座位类型余票紧张，请尽快购买！"
            show-icon
            :closable="false"
            style="margin-top: 16px;"
        />
      </div>
      <template #footer>
        <el-button @click="buyDialogVisible = false">取消</el-button>
        <el-button
            type="primary"
            :loading="buyLoading"
            @click="handleConfirmBuy"
            :disabled="calculatedPrice === null"
        >
          确认支付 ¥{{ formatPrice(calculatedPrice) }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { querySchedulesByStations } from '@/api/DepartureApi.js'
import { getTicketInventory } from '@/api/TicketApi.js'
import { sellTicket } from '@/api/SaleApi.js'
import { calculatePrice } from '@/api/ExtraApi.js'  // 从 ExtraApi 导入
import { getRouteStations } from '@/api/RouteApi.js'
import { getAllStations } from '@/api/StationApi.js'


const searchForm = ref({
  startStationId: null,
  endStationId: null,
  startTime: '',
  endTime: ''
})

const stationList = ref([])
const scheduleList = ref([])
const inventoryList = ref([])
const ticketDialogVisible = ref(false)
const ticketLoading = ref(false)
const searching = ref(false)
const searched = ref(false)
const currentTrainNumber = ref('')
const currentDepartureTimeStr = ref('')
const currentSchedule = ref(null)
const useTimeRange = ref(false)

// 购票相关
const buyDialogVisible = ref(false)
const buyLoading = ref(false)
const buyingItem = ref(null)
const startStationSeq = ref(0)
const endStationSeq = ref(0)
const calculatedPrice = ref(null)
const priceCalculating = ref(false)

// 获取出发站显示名称
const getStartStationDisplay = () => {
  const station = stationList.value.find(s => s.stationId === searchForm.value.startStationId)
  return station ? station.stationName : '出发站'
}

// 获取到达站显示名称
const getEndStationDisplay = () => {
  const station = stationList.value.find(s => s.stationId === searchForm.value.endStationId)
  return station ? station.stationName : '到达站'
}

// 获取当前时间（格式：YYYY-MM-DDTHH:mm:ss）
const getCurrentDateTime = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

// 获取一周后的时间
const getOneWeekLaterDateTime = () => {
  const date = new Date()
  date.setDate(date.getDate() + 7)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`
}

// 设置默认时间范围
const setDefaultTimeRange = () => {
  searchForm.value.startTime = getCurrentDateTime()
  searchForm.value.endTime = getOneWeekLaterDateTime()
}

// 格式化显示时间范围
const formatDateTimeRange = () => {
  if (useTimeRange.value) {
    return `${formatDateTime(searchForm.value.startTime)} 至 ${formatDateTime(searchForm.value.endTime)}`
  } else {
    return `${formatDateTime(getCurrentDateTime())} 至 ${formatDateTime(getOneWeekLaterDateTime())}`
  }
}

// 格式化日期时间显示
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  let str = dateTime
  if (str.includes('T')) {
    str = str.replace('T', ' ')
  }
  return str.substring(0, 16)
}

// 计算运行时长
const calculateDuration = (departureTime, arrivalTime) => {
  if (!departureTime || !arrivalTime) return '--'
  try {
    const start = new Date(departureTime)
    const end = new Date(arrivalTime)
    const minutes = Math.round((end - start) / (60 * 1000))
    if (minutes < 0) return '--'
    if (minutes < 60) return `${minutes}分钟`
    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60
    return mins === 0 ? `${hours}小时` : `${hours}小时${mins}分钟`
  } catch (e) {
    return '--'
  }
}

// 获取座位类型文本
const getSeatTypeText = (type) => {
  const map = {
    3: '商务座',
    2: '一等座',
    1: '二等座'
  }
  return map[type] || '类型' + type
}

// 格式化价格
const formatPrice = (price) => {
  if (price === null || price === undefined) return '0.00'
  return Number(price).toFixed(2)
}

// 站点选择变化
const onStationChange = () => {
  scheduleList.value = []
  searched.value = false
}

// 加载站点列表
const loadStations = async () => {
  try {
    const res = await getAllStations()
    if (res.code === 200) {
      stationList.value = res.data || []
      console.log('站点列表:', stationList.value)
    }
  } catch (error) {
    console.error('加载站点列表失败:', error)
  }
}

// 查询车次
const handleSearch = async () => {
  if (!searchForm.value.startStationId) {
    ElMessage.warning('请选择出发站')
    return
  }
  if (!searchForm.value.endStationId) {
    ElMessage.warning('请选择到达站')
    return
  }
  if (searchForm.value.startStationId === searchForm.value.endStationId) {
    ElMessage.warning('出发站和到达站不能相同')
    return
  }

  searching.value = true

  try {
    let startTime = searchForm.value.startTime
    if (!useTimeRange.value) {
      startTime = getCurrentDateTime()
    }

    const resp = await querySchedulesByStations(
        searchForm.value.startStationId,
        searchForm.value.endStationId,
        startTime,
        1,
        100
    )

    if (resp.code === 200) {
      let records = []
      if (resp.data) {
        if (Array.isArray(resp.data)) {
          records = resp.data
        } else if (resp.data.records && Array.isArray(resp.data.records)) {
          records = resp.data.records
        } else if (resp.data.list && Array.isArray(resp.data.list)) {
          records = resp.data.list
        }
      }

      if (records.length === 0) {
        scheduleList.value = []
        searched.value = true
        ElMessage.info('未找到符合条件的车次')
        return
      }

      scheduleList.value = records.map(item => ({
        trainId: item.trainId,
        trainNumber: item.trainNumber,
        departureTime: item.departureTime,
        departureTimeStr: formatDateTime(item.departureTime),
        arrivalTime: item.endArrivalTime,
        arrivalTimeStr: formatDateTime(item.endArrivalTime),
        duration: calculateDuration(item.departureTime, item.endArrivalTime),
        routerId: item.routerId,
        routerName: item.routerName
      }))

      searched.value = true
      ElMessage.success(`找到 ${scheduleList.value.length} 个车次`)
    } else {
      ElMessage.error(resp.message || '查询失败')
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败，请稍后重试')
  } finally {
    searching.value = false
  }
}

// 重置
const handleReset = () => {
  searchForm.value = {
    startStationId: null,
    endStationId: null,
    startTime: '',
    endTime: ''
  }
  useTimeRange.value = false
  setDefaultTimeRange()
  scheduleList.value = []
  searched.value = false
}

// 查看余票（库存聚合）
const viewTickets = async (schedule) => {
  currentTrainNumber.value = schedule.trainNumber
  currentDepartureTimeStr.value = schedule.departureTimeStr
  currentSchedule.value = schedule
  ticketDialogVisible.value = true
  ticketLoading.value = true

  try {
    const resp = await getTicketInventory(schedule.trainId, schedule.departureTime)
    if (resp.code === 200) {
      inventoryList.value = resp.data || []
    } else {
      ElMessage.error(resp.message || '查询库存失败')
    }

    if (schedule.routerId) {
      const routeResp = await getRouteStations(schedule.routerId)
      if (routeResp.code === 200) {
        const stations = routeResp.data || []
        const startSt = stations.find(s => s.stationId === searchForm.value.startStationId)
        const endSt = stations.find(s => s.stationId === searchForm.value.endStationId)
        startStationSeq.value = startSt ? startSt.stationSeq : 0
        endStationSeq.value = endSt ? endSt.stationSeq : 0
      }
    }
  } catch (error) {
    console.error('查询库存失败:', error)
    ElMessage.error('查询库存失败')
  } finally {
    ticketLoading.value = false
  }
}

// 重置库存对话框
const resetInventory = () => {
  inventoryList.value = []
}

// 重置购票对话框
const resetBuyDialog = () => {
  buyingItem.value = null
  calculatedPrice.value = null
  priceCalculating.value = false
}

// 点击购买按钮 - 先计算价格
// 修改 handleBuyClick 函数中的票价计算部分
const handleBuyClick = async (item) => {
  if (startStationSeq.value <= 0 || endStationSeq.value <= 0) {
    ElMessage.warning('无法确定站点序号，请重新查询')
    return
  }
  if (startStationSeq.value >= endStationSeq.value) {
    ElMessage.warning('出发站序号必须小于到达站序号')
    return
  }

  buyingItem.value = item
  buyDialogVisible.value = true
  priceCalculating.value = true
  calculatedPrice.value = null

  try {
    // 调用票价计算接口 - 传入 seatType 而非 ticketId（库存是聚合数据，没有单个 ticketId）
    const resp = await calculatePrice(
        currentSchedule.value.trainId,  // trainId
        item.seatType,                   // seatType（座位类型编码 0/1/2）
        startStationSeq.value,          // startStationSeq
        endStationSeq.value             // endStationSeq
    )

    console.log('票价计算结果:', resp)

    if (resp.code === 200) {
      // 根据实际返回结构调整
      calculatedPrice.value = resp.data || resp.price || 0
    } else {
      ElMessage.error(resp.message || '票价计算失败')
      buyDialogVisible.value = false
    }
  } catch (error) {
    console.error('票价计算失败:', error)
    ElMessage.error('票价计算失败：' + (error.message || '未知错误'))
    buyDialogVisible.value = false
  } finally {
    priceCalculating.value = false
  }
}

// 确认购买
const handleConfirmBuy = async () => {
  if (!buyingItem.value || !currentSchedule.value) return
  if (calculatedPrice.value === null) {
    ElMessage.warning('票价尚未计算完成，请稍候')
    return
  }

  buyLoading.value = true
  try {
    const params = {
      trainId: currentSchedule.value.trainId,
      departureTime: currentSchedule.value.departureTime,
      seatType: buyingItem.value.seatType,
      startStationSeq: startStationSeq.value,
      endStationSeq: endStationSeq.value
    }
    const resp = await sellTicket(params)
    if (resp.code === 200) {
      ElMessage.success(resp.message || '购票成功')
      buyDialogVisible.value = false
      // 刷新库存
      if (currentSchedule.value) {
        await viewTickets(currentSchedule.value)
      }
    } else {
      ElMessage.error(resp.message || '购票失败')
    }
  } catch (error) {
    console.error('购票失败:', error)
    ElMessage.error('购票失败：' + (error.message || '未知错误'))
  } finally {
    buyLoading.value = false
  }
}

onMounted(async () => {
  await loadStations()
  setDefaultTimeRange()
})
</script>

<style scoped>
.train-search {
  padding: 20px;
}

.result-card {
  margin-top: 20px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-count {
  color: #909399;
  font-size: 14px;
}

.train-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.train-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
}

.train-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.train-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.train-number {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
  min-width: 100px;
}

.train-time {
  display: flex;
  align-items: center;
  gap: 20px;
  flex: 1;
}

.departure, .arrival {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.departure .time, .arrival .time {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.departure .station, .arrival .station {
  font-size: 12px;
  color: #909399;
}

.arrow {
  font-size: 20px;
  color: #c0ccd5;
}

.train-info {
  display: flex;
  gap: 16px;
}

.duration {
  font-size: 14px;
  color: #606266;
}

.empty-result {
  padding: 40px 0;
}

.time-range-wrapper {
  width: 100%;
}

.time-range-display {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.time-range-picker {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
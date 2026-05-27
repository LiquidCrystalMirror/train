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
            <el-form-item label="出发站ID">
              <el-input-number v-model="searchForm.startStationId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="到达站ID">
              <el-input-number v-model="searchForm.endStationId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="开始时间">
              <el-date-picker
                v-model="searchForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                style="width: 100%"
                value-format="YYYY-MM-DDTHH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="结束时间">
              <el-date-picker
                v-model="searchForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                style="width: 100%"
                value-format="YYYY-MM-DDTHH:mm:ss"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="车次号">
              <el-input v-model="searchForm.trainNumber" placeholder="输入车次号" clearable />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 查询结果 -->
    <el-card class="result-card">
      <template #header>
        <h3>查询结果</h3>
      </template>
      
      <el-table :data="trainList" stripe border style="width: 100%">
        <el-table-column prop="trainId" label="车次ID" align="center" />
        <el-table-column prop="trainNumber" label="车次号" align="center" />
        <el-table-column prop="departureTime" label="发车时间" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.departureTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="arrivalTime" label="到达时间" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.arrivalTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="runTime" label="运行时长" align="center" />
        <el-table-column prop="totalStations" label="站点数" align="center" />
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewTickets(scope.row)">
              查看车票
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 车票列表对话框 -->
    <el-dialog title="车票列表" v-model="ticketDialogVisible" width="800px">
      <el-table :data="ticketList" stripe border style="width: 100%">
        <el-table-column prop="ticketId" label="车票ID" align="center" />
        <el-table-column prop="carriageNumber" label="车厢号" align="center" />
        <el-table-column prop="seatNumber" label="座位号" align="center" />
        <el-table-column prop="seatType" label="座位类型" align="center">
          <template #default="scope">
            {{ getSeatTypeText(scope.row.seatType) }}
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" align="center">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="ticketStatus" label="状态" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.ticketStatus === 'available' ? 'success' : 'info'">
              {{ scope.row.ticketStatus === 'available' ? '可售' : '已售' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button 
              size="small" 
              type="primary" 
              :disabled="scope.row.ticketStatus !== 'available'"
              @click="handleBuyTicket(scope.row)"
            >
              购买
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import * as TrainApi from '@/api/TrainApi.js'
import * as TicketApi from '@/api/TicketApi.js'

const searchForm = ref({
  startStationId: null,
  endStationId: null,
  startTime: '',
  endTime: '',
  trainNumber: ''
})

const trainList = ref([])
const ticketList = ref([])
const ticketDialogVisible = ref(false)
const currentTrainId = ref(null)

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 19)
}

// 获取座位类型文本
const getSeatTypeText = (type) => {
  const map = {
    'business': '商务座',
    'first': '一等座',
    'second': '二等座',
    'hard': '硬座',
    'soft': '软座',
    'hard_sleeper': '硬卧',
    'soft_sleeper': '软卧'
  }
  return map[type] || type
}

// 查询车次
const handleSearch = () => {
  // 如果有起止站点，使用站点查询
  if (searchForm.value.startStationId && searchForm.value.endStationId) {
    TrainApi.queryByStations(searchForm.value.startStationId, searchForm.value.endStationId)
      .then((resp) => {
        if (resp.code === 200) {
          trainList.value = resp.data || []
          ElMessage.success(`查询到 ${trainList.value.length} 个车次`)
        }
      })
      .catch(err => {
        ElMessage.error('查询失败')
      })
  } 
  // 如果有时间范围，使用时间范围查询
  else if (searchForm.value.startTime && searchForm.value.endTime) {
    TrainApi.queryByTimeRange(searchForm.value.startTime, searchForm.value.endTime)
      .then((resp) => {
        if (resp.code === 200) {
          trainList.value = resp.data || []
          ElMessage.success(`查询到 ${trainList.value.length} 个车次`)
        }
      })
      .catch(err => {
        ElMessage.error('查询失败')
      })
  }
  // 如果有车次号，使用车次号查询
  else if (searchForm.value.trainNumber) {
    TrainApi.queryByNumber(searchForm.value.trainNumber)
      .then((resp) => {
        if (resp.code === 200) {
          trainList.value = resp.data || []
          ElMessage.success(`查询到 ${trainList.value.length} 个车次`)
        }
      })
      .catch(err => {
        ElMessage.error('查询失败')
      })
  }
  else {
    ElMessage.warning('请输入查询条件')
  }
}

// 重置
const handleReset = () => {
  searchForm.value = {
    startStationId: null,
    endStationId: null,
    startTime: '',
    endTime: '',
    trainNumber: ''
  }
  trainList.value = []
}

// 查看车票
const viewTickets = (train) => {
  currentTrainId.value = train.trainId
  TicketApi.getTicketsByTrain(train.trainId)
    .then((resp) => {
      if (resp.code === 200) {
        ticketList.value = resp.data || []
        ticketDialogVisible.value = true
      }
    })
    .catch(err => {
      ElMessage.error('查询车票失败')
    })
}

// 购买车票
const handleBuyTicket = (ticket) => {
  ElMessage.info(`准备购买车票：${ticket.carriageNumber}车厢 ${ticket.seatNumber}座`)
  // 这里可以跳转到售票页面或打开购票对话框
}
</script>

<style scoped>
.train-search {
  padding: 20px;
}

.result-card {
  margin-top: 20px;
}
</style>

<template>
  <div class="ticket-management">
    <!-- 搜索表单 -->
    <div class="search-bar">
      <el-form :model="searchForm" class="search-form">
        <el-row :gutter="10">
          <el-col :span="6">
            <el-form-item label="车次ID">
              <el-input
                  v-model="searchForm.trainId"
                  placeholder="请输入车次ID"
                  clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item>
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
              <el-button @click="handleReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
          :data="tableData"
          stripe
          border
          style="width: 100%"
          v-loading="loading"
      >
        <el-table-column prop="ticketId" label="ID" align="center" />
        <el-table-column prop="trainId" label="车次ID" align="center" />
        <el-table-column prop="carriageNumber" label="车厢号" align="center" />
        <el-table-column prop="seatNumber" label="座位号" align="center" />
        <el-table-column prop="seatType" label="座位类型" align="center">
          <template #default="scope">
            {{ getSeatTypeName(scope.row.seatType) }}
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格(元)" align="center">
          <template #default="scope">
            ¥{{ formatPrice(scope.row.price) }}
          </template>
        </el-table-column>
        <el-table-column prop="ticketStatus" label="状态" align="center">
          <template #default="scope">
            <el-tag :type="getTicketStatusType(scope.row.ticketStatus)" size="small">
              {{ scope.row.ticketStatus }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <el-pagination
        v-if="!isSearchByTrainId"
        class="mgt-4"
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        layout="total, prev, pager, next, jumper"
        :total="total"
        @current-change="loadAllTickets"
    />

    <!-- 按车次查询时的提示 -->
    <div v-else class="search-tip">
      <el-alert type="info" :closable="false" show-icon>
        当前显示车次 ID 为 {{ searchForm.trainId }} 的车票，共 {{ tableData.length }} 张
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import * as TicketApi from '@/api/TicketApi.js'

const searchForm = ref({
  trainId: ''
})

const pagination = ref({
  pageNum: 1,
  pageSize: 10  // 固定每页10条
})

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const isSearchByTrainId = ref(false)

// 座位类型映射
const seatTypeMap = {
  1: '商务座',
  2: '一等座',
  3: '二等座',
  4: '硬座',
  5: '软座',
  6: '硬卧',
  7: '软卧'
}

// 格式化价格
const formatPrice = (price) => {
  if (price === null || price === undefined) return '0.00'
  return Number(price).toFixed(2)
}

// 状态标签类型
const getTicketStatusType = (status) => {
  if (status === '可售') return 'success'
  if (status === '已售') return 'danger'
  if (status === '锁定') return 'warning'
  return 'info'
}

const getSeatTypeName = (type) => {
  return seatTypeMap[type] || type || '未知'
}

// 加载所有车票（分页）
const loadAllTickets = () => {
  loading.value = true
  const params = {
    pageNum: pagination.value.pageNum,
    pageSize: pagination.value.pageSize
  }

  TicketApi.getTicketPage(params).then((resp) => {
    loading.value = false
    if (resp.code === 200 && resp.data) {
      tableData.value = resp.data.records || []
      total.value = resp.data.total || 0
    } else {
      ElMessage.error(resp.message || '加载数据失败')
    }
  }).catch(err => {
    loading.value = false
    console.error('加载数据失败:', err)
    ElMessage.error('加载数据失败')
  })
}

// 根据车次ID查询
const loadTicketsByTrainId = (trainId) => {
  loading.value = true
  TicketApi.getTicketsByTrain(trainId).then((resp) => {
    loading.value = false
    if (resp.code === 200 && resp.data) {
      tableData.value = resp.data || []
      total.value = tableData.value.length
    } else {
      ElMessage.error(resp.message || '查询失败')
      tableData.value = []
      total.value = 0
    }
  }).catch(err => {
    loading.value = false
    console.error('查询失败:', err)
    ElMessage.error('查询失败')
    tableData.value = []
    total.value = 0
  })
}

// 查询（根据是否有车次ID决定调用哪个接口）
const handleSearch = () => {
  if (searchForm.value.trainId) {
    isSearchByTrainId.value = true
    loadTicketsByTrainId(searchForm.value.trainId)
  } else {
    isSearchByTrainId.value = false
    pagination.value.pageNum = 1
    loadAllTickets()
  }
}

// 重置搜索
const handleReset = () => {
  searchForm.value.trainId = ''
  isSearchByTrainId.value = false
  pagination.value.pageNum = 1
  loadAllTickets()
}

onMounted(() => {
  loadAllTickets()
})
</script>

<style scoped>
.ticket-management {
  padding: 20px;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.search-form {
  flex: 1;
}

.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: auto;
}

.mgt-4 {
  margin-top: 16px;
}

.el-pagination {
  padding: 16px;
  text-align: right;
}

.search-tip {
  margin-top: 16px;
}
</style>
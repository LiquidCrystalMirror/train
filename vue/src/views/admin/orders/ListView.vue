<template>
  <div class="order-management">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="订单记录" name="orders">
        <div class="table-container">
          <div class="search-bar">
            <el-input
                v-model="searchForm.trainNumber"
                placeholder="车次号"
                clearable
                style="width: 180px; margin-right: 10px;"
            />
            <el-input
                v-model="searchForm.userId"
                placeholder="用户ID"
                clearable
                style="width: 180px; margin-right: 10px;"
            />
            <el-button type="primary" @click="handleSearchSales">查询</el-button>
            <el-button @click="resetSalesSearch">重置</el-button>
          </div>

          <el-table :data="salesList" stripe border style="width: 100%" v-loading="salesLoading">
            <el-table-column prop="trainNumber" label="车次号" align="center" min-width="100" />
            <el-table-column prop="userId" label="用户ID" align="center" width="140" />
            <el-table-column prop="carriageNumber" label="车厢号" align="center" width="80" />
            <el-table-column prop="seatNumber" label="座位号" align="center" width="80" />
            <el-table-column prop="startStationName" label="出发站" align="center" min-width="120" />
            <el-table-column prop="startArrivalTime" label="上车时间" align="center" min-width="160">
              <template #default="scope">
                <span style="color: #409EFF; font-weight: 500;">{{ formatDateTime(scope.row.startArrivalTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="endStationName" label="到达站" align="center" min-width="120" />
            <el-table-column prop="endArrivalTime" label="预计到达时间" align="center" min-width="160">
              <template #default="scope">
                <span style="color: #67c23a; font-weight: 500;">{{ formatDateTime(scope.row.endArrivalTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="seatType" label="座位类型" align="center" min-width="100">
              <template #default="scope">
                <el-tag size="small" type="info">{{ getSeatTypeText(scope.row.seatType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="票价" align="center" min-width="100">
              <template #default="scope">
                <span style="color: #e6a23c; font-weight: bold;">¥{{ formatPrice(scope.row.price) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="saleTime" label="购票时间" align="center" min-width="160">
              <template #default="scope">
                {{ formatDateTime(scope.row.saleTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="saleStatus" label="状态" align="center" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.saleStatus === '已出票' ? 'success' : 'info'">
                  {{ scope.row.saleStatus || '已出票' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
              v-if="salesTotal > 0"
              class="pagination"
              v-model:current-page="salesPage.pageNum"
              v-model:page-size="salesPage.pageSize"
              layout="total, prev, pager, next, jumper"
              :total="salesTotal"
              @current-change="loadSalesList"
          />
          <div v-else-if="!salesLoading && salesList.length === 0" class="empty-tip">
            <el-empty description="暂无售票记录" />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSoldTickets } from '@/api/ExtraApi.js'

const activeTab = ref('orders')

// 售票列表
const salesList = ref([])
const salesLoading = ref(false)
const salesTotal = ref(0)
const salesPage = ref({
  pageNum: 1,
  pageSize: 10
})
const searchForm = ref({
  trainNumber: '',
  userId: ''
})

// 座位类型映射
const getSeatTypeText = (type) => {
  const map = {
    3: '商务座',
    2: '一等座',
    1: '二等座'
  }
  return map[type] || '未知'
}

// 格式化价格
const formatPrice = (price) => {
  if (price === null || price === undefined) return '0.00'
  return Number(price).toFixed(2)
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 19)
}

// 处理售票记录数据
const processSalesRecord = (record) => {
  return {
    ...record,
    userId: record.userId || record.userid || '-',
    startStationName: record.startStationName || `站点${record.startStationSeq}`,
    endStationName: record.endStationName || `站点${record.endStationSeq}`,
    startArrivalTime: record.startArrivalTime || record.departureTime,
    endArrivalTime: record.endArrivalTime || record.departureTime
  }
}

// 加载所有售票记录
const loadSalesList = async () => {
  salesLoading.value = true
  try {
    const params = {
      pageNum: salesPage.value.pageNum,
      pageSize: salesPage.value.pageSize
    }
    const resp = await getSoldTickets(params)
    console.log('售票记录响应:', resp)

    if (resp.code === 200 && resp.data) {
      const records = resp.data.records || []
      salesList.value = records.map(processSalesRecord)
      salesTotal.value = resp.data.total || 0
    } else {
      ElMessage.error(resp.message || '加载售票记录失败')
    }
  } catch (error) {
    console.error('加载售票记录失败:', error)
    ElMessage.error('加载售票记录失败')
  } finally {
    salesLoading.value = false
  }
}

// 搜索售票记录
const handleSearchSales = async () => {
  salesLoading.value = true
  try {
    const params = {
      pageNum: 1,
      pageSize: 1000
    }
    const resp = await getSoldTickets(params)
    if (resp.code === 200 && resp.data) {
      let records = resp.data.records || []

      if (searchForm.value.trainNumber) {
        records = records.filter(item =>
            item.trainNumber && item.trainNumber.includes(searchForm.value.trainNumber)
        )
      }
      if (searchForm.value.userId) {
        records = records.filter(item =>
            (item.userId || item.userid) && (item.userId || item.userid).includes(searchForm.value.userId)
        )
      }

      const processedRecords = records.map(processSalesRecord)
      const start = (salesPage.value.pageNum - 1) * salesPage.value.pageSize
      const end = start + salesPage.value.pageSize
      salesList.value = processedRecords.slice(start, end)
      salesTotal.value = records.length
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
  } finally {
    salesLoading.value = false
  }
}

// 重置售票搜索
const resetSalesSearch = () => {
  searchForm.value = {
    trainNumber: '',
    userId: ''
  }
  salesPage.value.pageNum = 1
  loadSalesList()
}

onMounted(() => {
  loadSalesList()
})
</script>

<style scoped>
.order-management {
  background: white;
  padding: 20px;
  border-radius: 8px;
  min-height: calc(100vh - 120px);
}

.table-container {
  margin-top: 16px;
}

.search-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

.empty-tip {
  padding: 40px 0;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}
</style>
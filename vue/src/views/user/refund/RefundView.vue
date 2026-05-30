<template>
  <div class="refund-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>退票</h3>
          <span class="subtitle">可退票的订单（发车前可退票）</span>
        </div>
      </template>

      <!-- 订单列表 -->
      <div class="order-list-section">
        <el-table
            :data="orderList"
            stripe
            border
            style="width: 100%"
            v-loading="loading"
            empty-text="暂无可退票的订单"
        >
          <el-table-column prop="saleId" label="订单ID" align="center" width="100" />
          <el-table-column prop="trainNumber" label="车次号" align="center" min-width="100" />
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
          <el-table-column prop="seatType" label="座位类型" align="center" width="100">
            <template #default="scope">
              <el-tag size="small" type="info">{{ getSeatTypeText(scope.row.seatType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="票价" align="center" width="100">
            <template #default="scope">
              <span style="color: #e6a23c; font-weight: bold;">¥{{ formatPrice(scope.row.price) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="saleTime" label="购票时间" align="center" min-width="160">
            <template #default="scope">
              {{ formatDateTime(scope.row.saleTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="100" fixed="right">
            <template #default="scope">
              <el-button
                  size="small"
                  type="danger"
                  :loading="refundingId === scope.row.saleId"
                  @click="handleRefund(scope.row)"
              >
                退票
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <el-pagination
            v-if="total > 0"
            class="pagination"
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            layout="total, prev, pager, next, jumper"
            :total="total"
            @current-change="loadOrderList"
        />
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && orderList.length === 0" class="empty-tip">
        <el-empty description="暂无订单记录" />
      </div>
    </el-card>

    <!-- 退票确认对话框 -->
    <el-dialog v-model="confirmDialogVisible" title="退票确认" width="420px">
      <div class="confirm-content">
        <el-icon class="warning-icon" color="#e6a23c" :size="50"><Warning /></el-icon>
        <p>确认退票后，将无法恢复！</p>
        <div class="refund-info" v-if="currentRefundOrder">
          <div class="refund-info-item">
            <span class="label">车次：</span>
            <span class="value">{{ currentRefundOrder.trainNumber }}</span>
          </div>
          <div class="refund-info-item">
            <span class="label">座位：</span>
            <span class="value">{{ currentRefundOrder.carriageNumber }}车厢 {{ currentRefundOrder.seatNumber }}座</span>
          </div>
          <div class="refund-info-item">
            <span class="label">出发站：</span>
            <span class="value">{{ currentRefundOrder.startStationName }}</span>
          </div>
          <div class="refund-info-item">
            <span class="label">到达站：</span>
            <span class="value">{{ currentRefundOrder.endStationName }}</span>
          </div>
          <div class="refund-info-item">
            <span class="label">发车时间：</span>
            <span class="value">{{ formatDateTime(currentRefundOrder.startArrivalTime) }}</span>
          </div>
          <div class="refund-info-item">
            <span class="label">票价：</span>
            <span class="value" style="color: #e6a23c; font-weight: bold;">¥{{ formatPrice(currentRefundOrder.price) }}</span>
          </div>
        </div>
        <el-alert
            title="温馨提示"
            type="warning"
            description="退票后票款将原路返回，请确认操作。"
            show-icon
            :closable="false"
            style="margin-top: 16px;"
        />
      </div>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="refunding" @click="executeRefund">确认退票</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning, Close } from '@element-plus/icons-vue'
import { refundTicket } from '@/api/RefundApi.js'
import { getUserPurchasePage } from '@/api/ExtraApi.js'
import authService from '@/service/AuthService.js'

// 获取当前用户
const currentUser = computed(() => authService.getUser())
const userId = computed(() => currentUser.value?.userId)

const orderList = ref([])
const loading = ref(false)
const refunding = ref(false)
const refundingId = ref(null)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const confirmDialogVisible = ref(false)
const currentRefundOrder = ref(null)

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

// 处理订单记录
const processOrderRecord = (record) => {
  return {
    ...record,
    startStationName: record.startStationName || `站点${record.startStationSeq}`,
    endStationName: record.endStationName || `站点${record.endStationSeq}`,
    startArrivalTime: record.startArrivalTime || record.departureTime,
    endArrivalTime: record.endArrivalTime || record.departureTime
  }
}

// 加载用户订单列表
const loadOrderList = async () => {
  if (!userId.value) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    const params = {
      userId: userId.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    const resp = await getUserPurchasePage(params)
    console.log('订单列表响应:', resp)

    if (resp.code === 200 && resp.data) {
      const records = resp.data.records || []
      // 只显示已出票的订单（可退票）
      const availableRecords = records.filter(item => item.saleStatus === '已出票')
      orderList.value = availableRecords.map(processOrderRecord)
      total.value = resp.data.total || 0
    } else {
      ElMessage.error(resp.message || '加载订单失败')
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

// 退票
const handleRefund = (order) => {
  currentRefundOrder.value = order
  confirmDialogVisible.value = true
}

// 执行退票
const executeRefund = async () => {
  if (!currentRefundOrder.value) return

  refunding.value = true
  refundingId.value = currentRefundOrder.value.saleId

  try {
    const resp = await refundTicket(currentRefundOrder.value.saleId)
    if (resp.code === 200) {
      ElMessage.success(resp.message || '退票成功')
      confirmDialogVisible.value = false
      // 刷新列表
      await loadOrderList()
    } else {
      ElMessage.error(resp.message || '退票失败')
    }
  } catch (error) {
    console.error('退票失败:', error)
    ElMessage.error('退票失败：' + (error.message || '未知错误'))
  } finally {
    refunding.value = false
    refundingId.value = null
    currentRefundOrder.value = null
  }
}

onMounted(() => {
  if (userId.value) {
    loadOrderList()
  } else {
    ElMessage.warning('请先登录')
  }
})
</script>

<style scoped>
.refund-management {
  padding: 20px;
  background: white;
  border-radius: 8px;
  min-height: calc(100vh - 120px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.subtitle {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.order-list-section {
  margin-top: 16px;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

.empty-tip {
  padding: 40px 0;
}

.confirm-content {
  text-align: center;
  padding: 10px;
}

.warning-icon {
  margin-bottom: 16px;
}

.refund-info {
  margin-top: 20px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  text-align: left;
}

.refund-info-item {
  margin: 8px 0;
  font-size: 14px;
  display: flex;
}

.refund-info-item .label {
  width: 80px;
  color: #606266;
}

.refund-info-item .value {
  flex: 1;
  color: #303133;
  font-weight: 500;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}
</style>
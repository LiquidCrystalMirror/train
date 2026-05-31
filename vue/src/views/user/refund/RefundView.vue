<template>
  <div class="refund-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>退票</h3>
          <span class="subtitle">可退票的订单（发车前30分钟以上可退票）</span>
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
            :fit="true"
        >
          <el-table-column prop="saleId" label="订单ID" align="center" min-width="100" />
          <el-table-column prop="trainNumber" label="车次号" align="center" min-width="100" />
          <el-table-column prop="carriageNumber" label="车厢号" align="center" min-width="80" />
          <el-table-column prop="seatNumber" label="座位号" align="center" min-width="80" />
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
          <el-table-column label="操作" align="center" min-width="100" fixed="right">
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
import { Warning } from '@element-plus/icons-vue'
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
  // 统一替换 T 为空格，并截取到秒
  return dateTime.replace('T', ' ').substring(0, 19)
}

// 处理订单记录，补充站点名称和发车时间
const processOrderRecord = (record) => {
  return {
    ...record,
    startStationName: record.startStationName || `站点${record.startStationSeq}`,
    endStationName: record.endStationName || `站点${record.endStationSeq}`,
    startArrivalTime: record.startArrivalTime || record.departureTime,
    endArrivalTime: record.endArrivalTime || record.departureTime
  }
}

/**
 * 检查订单是否可退票（发车前30分钟以上）
 * @param {Object} order 订单对象
 * @returns {boolean} true=可退票，false=不可退票
 */
const isRefundable = (order) => {
  if (!order.startArrivalTime) return false

  try {
    const now = new Date()
    const departureTime = new Date(order.startArrivalTime)

    // 如果发车时间已过，不可退票
    if (departureTime <= now) return false

    // 计算时间差（毫秒）
    const timeDiff = departureTime - now
    // 30分钟对应的毫秒数
    const thirtyMinutes = 30 * 60 * 1000

    // 如果距离发车时间不足30分钟，不可退票
    return timeDiff >= thirtyMinutes
  } catch (error) {
    console.error('检查退票时间失败:', error)
    return false
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
      // 1. 先筛选出状态为“已出票”的订单
      let availableRecords = records.filter(item => item.saleStatus === '已出票')
      // 2. 处理订单记录，补充必要字段
      let processedRecords = availableRecords.map(processOrderRecord)
      // 3. 再根据发车时间筛选可退票的订单（距离发车时间 >= 30分钟）
      const refundableRecords = processedRecords.filter(order => isRefundable(order))

      orderList.value = refundableRecords
      total.value = refundableRecords.length  // 注意：这里显示的是过滤后的数量，如需显示原始总数可保留 resp.data.total

      // 可选：如果过滤后数量为0但原始有订单，可给出提示
      if (availableRecords.length > 0 && refundableRecords.length === 0) {
        ElMessage.info('当前没有距离发车时间超过30分钟的订单，无法退票')
      }
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
  // 二次确认时间限制，防止前端时间差异
  if (!isRefundable(order)) {
    ElMessage.warning('该订单距离发车时间不足30分钟，无法退票')
    return
  }
  currentRefundOrder.value = order
  confirmDialogVisible.value = true
}

// 执行退票
const executeRefund = async () => {
  if (!currentRefundOrder.value) return

  // 再次校验时间限制
  if (!isRefundable(currentRefundOrder.value)) {
    ElMessage.warning('该订单距离发车时间不足30分钟，无法退票')
    confirmDialogVisible.value = false
    currentRefundOrder.value = null
    return
  }

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

/* 隐藏表格滚动条 */
.order-list-section {
  overflow-x: auto;
}

.order-list-section::-webkit-scrollbar {
  display: none;
}

.order-list-section {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

:deep(.el-table__body-wrapper) {
  overflow-x: hidden !important;
}

:deep(.el-table) {
  overflow-x: auto;
}
</style>
<template>
  <div class="my-orders">
    <el-tabs v-model="activeTab">
      <!-- 我的购票记录 -->
      <el-tab-pane label="我的购票" name="purchases">
        <div class="table-container">
          <el-table
              :data="purchaseList"
              stripe
              border
              style="width: 100%"
              v-loading="purchaseLoading"
              :fit="true"
          >
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
            <el-table-column prop="seatTypeName" label="座位类型" align="center" min-width="100">
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

          <!-- 购票分页 -->
          <el-pagination
              v-if="purchaseTotal > 0"
              class="pagination"
              v-model:current-page="purchasePage.pageNum"
              v-model:page-size="purchasePage.pageSize"
              layout="total, prev, pager, next, jumper"
              :total="purchaseTotal"
              @current-change="loadPurchaseList"
          />
          <div v-else-if="!purchaseLoading && purchaseList.length === 0" class="empty-tip">
            <el-empty description="暂无购票记录" />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserPurchasePage } from '@/api/ExtraApi.js'
import authService from '@/service/AuthService.js'

const activeTab = ref('purchases')

// 购票列表
const purchaseList = ref([])
const purchaseLoading = ref(false)
const purchaseTotal = ref(0)
const purchasePage = ref({
  pageNum: 1,
  pageSize: 10
})

// 获取当前用户
const currentUser = computed(() => authService.getUser())
const userId = computed(() => currentUser.value?.userId)

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

// 处理购票记录数据
const processPurchaseRecord = (record) => {
  return {
    ...record,
    seatTypeName: getSeatTypeText(record.seatType)
  }
}

// 加载购票记录
const loadPurchaseList = async () => {
  if (!userId.value) {
    console.warn('用户未登录')
    return
  }

  purchaseLoading.value = true
  try {
    const params = {
      userId: userId.value,
      pageNum: purchasePage.value.pageNum,
      pageSize: purchasePage.value.pageSize
    }
    const resp = await getUserPurchasePage(params)
    console.log('购票记录响应:', resp)

    if (resp.code === 200 && resp.data) {
      const records = resp.data.records || []
      purchaseList.value = records.map(processPurchaseRecord)
      purchaseTotal.value = resp.data.total || 0
    } else {
      ElMessage.error(resp.message || '加载购票记录失败')
    }
  } catch (error) {
    console.error('加载购票记录失败:', error)
    ElMessage.error('加载购票记录失败')
  } finally {
    purchaseLoading.value = false
  }
}

onMounted(() => {
  if (userId.value) {
    loadPurchaseList()
  } else {
    ElMessage.warning('请先登录')
  }
})
</script>

<style scoped>
.my-orders {
  background: white;
  padding: 20px;
  border-radius: 8px;
  min-height: calc(100vh - 120px);
}

.table-container {
  margin-top: 16px;
  overflow-x: auto;
}

/* 隐藏滚动条但保留功能（可选，如果需要完全禁止滚动条可以注释掉 overflow-x 属性） */
.table-container::-webkit-scrollbar {
  display: none;
}

.table-container {
  -ms-overflow-style: none;
  scrollbar-width: none;
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

/* 确保表格不出现滚动条 */
:deep(.el-table__body-wrapper) {
  overflow-x: hidden;
}

:deep(.el-table) {
  overflow-x: auto;
}
</style>
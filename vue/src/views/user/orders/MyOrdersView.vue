<template>
  <div class="my-orders">
    <el-tabs v-model="activeTab">
      <!-- 我的购票记录 -->
      <el-tab-pane label="我的购票" name="purchases">
        <el-table :data="purchaseList" stripe border style="width: 100%">
          <el-table-column prop="saleId" label="订单ID" width="100" />
          <el-table-column prop="ticketId" label="车票ID" width="100" />
          <el-table-column prop="trainId" label="车次ID" width="100" />
          <el-table-column prop="startStationSeq" label="上车站序号" width="120" />
          <el-table-column prop="endStationSeq" label="下车站序号" width="120" />
          <el-table-column prop="saleTime" label="购票时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.saleTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="saleStatus" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.saleStatus === '已出票' ? 'success' : 'info'">
                {{ scope.row.saleStatus }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button 
                size="small" 
                type="danger"
                :disabled="scope.row.saleStatus !== '已出票'"
                @click="handleRefund(scope.row)"
              >
                退票
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 我的退票记录 -->
      <el-tab-pane label="我的退票" name="refunds">
        <el-table :data="refundList" stripe border style="width: 100%">
          <el-table-column prop="refundId" label="退票ID" width="100" />
          <el-table-column prop="saleId" label="原订单ID" width="120" />
          <el-table-column prop="ticketId" label="车票ID" width="100" />
          <el-table-column prop="trainId" label="车次ID" width="100" />
          <el-table-column prop="refundTime" label="退票时间" width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.refundTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="refundStatus" label="退票状态" width="120" />
          <el-table-column prop="refundRemark" label="备注" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as RefundApi from '@/api/RefundApi.js'
import authService from '@/service/AuthService.js'

const activeTab = ref('purchases')
const purchaseList = ref([])
const refundList = ref([])

// 获取当前用户
const currentUser = computed(() => authService.getUser())

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 19)
}

// 加载数据
const loadData = () => {
  // TODO: 需要后端提供查询用户订单的接口
  // 目前这里只是示例，实际应该调用后端API
  console.log('当前用户:', currentUser.value)
}

// 退票
const handleRefund = (order) => {
  ElMessageBox.confirm('确定要退票吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    RefundApi.refundTicket(order.saleId)
      .then((resp) => {
        if (resp.code === 2000) {
          ElMessage.success('退票成功')
          loadData()
        } else {
          ElMessage.error(resp.msg || '退票失败')
        }
      })
      .catch(err => {
        ElMessage.error('退票失败：' + (err.message || '未知错误'))
      })
  }).catch(() => {
    // 取消操作
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-orders {
  background: white;
  padding: 20px;
  border-radius: 8px;
}
</style>

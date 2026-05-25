<template>
  <div class="order-management">
    <el-tabs v-model="activeTab">
      <!-- 售票记录 -->
      <el-tab-pane label="售票记录" name="sales">
        <el-table :data="salesData" stripe border style="width: 100%">
          <el-table-column prop="saleId" label="记录ID" width="100" />
          <el-table-column prop="ticketId" label="车票ID" width="100" />
          <el-table-column prop="trainId" label="车次ID" width="100" />
          <el-table-column prop="userId" label="用户ID" width="100" />
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
        </el-table>
      </el-tab-pane>

      <!-- 退票记录 -->
      <el-tab-pane label="退票记录" name="refunds">
        <el-table :data="refundsData" stripe border style="width: 100%">
          <el-table-column prop="refundId" label="退票ID" width="100" />
          <el-table-column prop="saleId" label="售票记录ID" width="120" />
          <el-table-column prop="ticketId" label="车票ID" width="100" />
          <el-table-column prop="trainId" label="车次ID" width="100" />
          <el-table-column prop="userId" label="用户ID" width="100" />
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
import { ref, onMounted } from 'vue'
import * as OrdersApi from '@/api/OrdersApi.js'

const activeTab = ref('sales')
const salesData = ref([])
const refundsData = ref([])

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 19)
}

onMounted(() => {
  // 这里可以添加加载数据的逻辑
  // 由于后端可能没有提供列表接口，暂时使用空数据
})
</script>

<style scoped>
.order-management {
  background: white;
  padding: 20px;
  border-radius: 8px;
}
</style>

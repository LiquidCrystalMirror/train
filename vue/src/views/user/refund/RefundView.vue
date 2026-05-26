<template>
  <div class="refund-management">
    <el-card>
      <template #header>
        <h3>退票</h3>
      </template>
      
      <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
        <el-form-item label="售票记录ID" prop="saleId">
          <el-input-number v-model="form.saleId" :min="1" style="width: 100%" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="danger" @click="handleRefund" :loading="loading">
            <el-icon><Close /></el-icon>
            确认退票
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import * as RefundApi from '@/api/RefundApi.js'

const form = ref({
  saleId: 1
})

const formRef = ref(null)
const loading = ref(false)

const rules = {
  saleId: [{ required: true, message: '请输入售票记录ID', trigger: 'blur' }]
}

const handleRefund = () => {
  formRef.value.validate().then(() => {
    loading.value = true
    RefundApi.refundTicket(form.value.saleId).then((resp) => {
      if (resp.code === 200) {
        ElMessage.success(resp.message || '退票成功')
        handleReset()
      } else {
        ElMessage.error(resp.message || '退票失败')
      }
    }).catch(err => {
      ElMessage.error('退票失败：' + (err.message || '未知错误'))
    }).finally(() => {
      loading.value = false
    })
  })
}

const handleReset = () => {
  form.value = {
    saleId: 1
  }
}
</script>

<style scoped>
.refund-management {
  max-width: 600px;
}
</style>

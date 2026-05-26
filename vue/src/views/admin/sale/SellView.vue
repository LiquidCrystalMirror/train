<template>
  <div class="sale-management">
    <el-card>
      <template #header>
        <h3>售票</h3>
      </template>
      
      <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
        <el-form-item label="车次ID" prop="trainId">
          <el-input-number v-model="form.trainId" :min="1" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="车票ID" prop="ticketId">
          <el-input-number v-model="form.ticketId" :min="1" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="上车站点序号" prop="startStationSeq">
          <el-input-number v-model="form.startStationSeq" :min="1" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="下车站点序号" prop="endStationSeq">
          <el-input-number v-model="form.endStationSeq" :min="1" style="width: 100%" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSell" :loading="loading">
            <el-icon><ShoppingCart /></el-icon>
            确认售票
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import * as SaleApi from '@/api/SaleApi.js'

const form = ref({
  trainId: 1,
  ticketId: 1,
  startStationSeq: 1,
  endStationSeq: 2
})

const formRef = ref(null)
const loading = ref(false)

const rules = {
  trainId: [{ required: true, message: '请输入车次ID', trigger: 'blur' }],
  ticketId: [{ required: true, message: '请输入车票ID', trigger: 'blur' }],
  startStationSeq: [{ required: true, message: '请输入上车站点序号', trigger: 'blur' }],
  endStationSeq: [{ required: true, message: '请输入下车站点序号', trigger: 'blur' }]
}

const handleSell = () => {
  formRef.value.validate().then(() => {
    loading.value = true
    SaleApi.sellTicket(form.value).then((resp) => {
      if (resp.code === 200) {
        ElMessage.success(resp.message || '售票成功')
        handleReset()
      } else {
        ElMessage.error(resp.message || '售票失败')
      }
    }).catch(err => {
      ElMessage.error('售票失败：' + (err.message || '未知错误'))
    }).finally(() => {
      loading.value = false
    })
  })
}

const handleReset = () => {
  form.value = {
    trainId: 1,
    ticketId: 1,
    startStationSeq: 1,
    endStationSeq: 2
  }
}
</script>

<style scoped>
.sale-management {
  max-width: 600px;
}
</style>

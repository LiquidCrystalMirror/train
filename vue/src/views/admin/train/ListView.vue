<template>
  <div class="train-management">
    <!-- 搜索表单 -->
    <el-form :model="searchForm" class="search-form">
      <el-row :gutter="10">
        <el-col :span="6">
          <el-form-item label="车次号">
            <el-input v-model="searchForm.find" placeholder="请输入车次号" clearable />
          </el-form-item>
        </el-col>
        <el-col :span="6">
          <el-form-item>
            <el-button type="primary" @click="loadData">
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

    <!-- 操作按钮 -->
    <el-button type="primary" @click="handleAdd" class="mgb-4">
      <el-icon><Plus /></el-icon>
      新增车次
    </el-button>

    <!-- 数据表格 -->
    <el-table :data="tableData" stripe border style="width: 100%">
      <el-table-column prop="trainId" label="ID" width="80" />
      <el-table-column prop="trainNumber" label="车次号" width="120" />
      <el-table-column prop="departureTime" label="发车时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.departureTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="arrivalTime" label="到达时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.arrivalTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="runTime" label="运行时长" width="120" />
      <el-table-column prop="totalStations" label="站点数" width="100" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-popconfirm title="确定要删除吗？" @confirm="handleDelete(scope.row.trainId)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      class="mgt-4"
      v-model:current-page="searchForm.pageNum"
      v-model:page-size="searchForm.pageSize"
      :page-sizes="[5, 10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      @size-change="loadData"
      @current-change="loadData"
    />

    <!-- 编辑对话框 -->
    <el-dialog :title="form.trainId ? '编辑车次' : '新增车次'" v-model="dialogVisible" width="600px">
      <el-form label-width="100px" :model="form" :rules="rules" ref="formRef">
        <el-form-item label="车次号" prop="trainNumber">
          <el-input v-model="form.trainNumber" placeholder="如：G1001" />
        </el-form-item>
        <el-form-item label="发车时间" prop="departureTime">
          <el-date-picker
            v-model="form.departureTime"
            type="datetime"
            placeholder="选择发车时间"
            style="width: 100%"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="到达时间" prop="arrivalTime">
          <el-date-picker
            v-model="form.arrivalTime"
            type="datetime"
            placeholder="选择到达时间"
            style="width: 100%"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="运行时长" prop="runTime">
          <el-input v-model="form.runTime" placeholder="如：4小时30分" />
        </el-form-item>
        <el-form-item label="站点总数" prop="totalStations">
          <el-input-number v-model="form.totalStations" :min="1" :max="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import * as TrainApi from '@/api/TrainApi.js'

const searchForm = ref({
  find: '',
  pageNum: 1,
  pageSize: 10
})

let total = ref(0)
const tableData = ref([])
let dialogVisible = ref(false)
let form = ref({
  trainNumber: '',
  departureTime: '',
  arrivalTime: '',
  runTime: '',
  totalStations: 2
})
let formRef = ref(null)

const rules = {
  trainNumber: [{ required: true, message: '请输入车次号', trigger: 'blur' }],
  departureTime: [{ required: true, message: '请选择发车时间', trigger: 'change' }],
  arrivalTime: [{ required: true, message: '请选择到达时间', trigger: 'change' }]
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 19)
}

// 加载数据
const loadData = () => {
  TrainApi.getTrainPage(searchForm.value).then((resp) => {
    if (resp.code === 2000 && resp.data) {
      tableData.value = resp.data.records || []
      total.value = resp.data.total || 0
    }
  }).catch(err => {
    ElMessage.error('加载数据失败')
  })
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    find: '',
    pageNum: 1,
    pageSize: 10
  }
  loadData()
}

// 新增
const handleAdd = () => {
  dialogVisible.value = true
  form.value = {
    trainNumber: '',
    departureTime: '',
    arrivalTime: '',
    runTime: '',
    totalStations: 2
  }
}

// 编辑
const handleEdit = (row) => {
  dialogVisible.value = true
  form.value = JSON.parse(JSON.stringify(row))
}

// 保存
const handleSave = () => {
  formRef.value.validate().then(() => {
    const apiCall = form.value.trainId ? TrainApi.updateTrain : TrainApi.addTrain
    apiCall(form.value).then(() => {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    }).catch(err => {
      ElMessage.error('保存失败')
    })
  })
}

// 删除
const handleDelete = (id) => {
  TrainApi.deleteTrain(id).then(() => {
    ElMessage.success('删除成功')
    loadData()
  }).catch(err => {
    ElMessage.error('删除失败')
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.search-form {
  margin-bottom: 20px;
}
.mgb-4 {
  margin-bottom: 16px;
}
.mgt-4 {
  margin-top: 16px;
}
</style>

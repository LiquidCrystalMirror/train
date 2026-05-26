<template>
  <div class="station-management">
    <el-form :model="searchForm" class="search-form">
      <el-row :gutter="10">
        <el-col :span="6">
          <el-form-item label="站点名称">
            <el-input v-model="searchForm.name" placeholder="请输入站点名称" clearable />
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

    <el-button type="primary" @click="handleAdd" class="mgb-4">
      <el-icon><Plus /></el-icon>
      新增站点
    </el-button>

    <el-table :data="tableData" stripe border style="width: 100%">
      <el-table-column prop="stationId" label="ID" width="80" />
      <el-table-column prop="stationName" label="站点名称" width="200" />
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="scope">
          {{ formatDateTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="success" @click="handleViewConnections(scope.row)">
            查看连通
          </el-button>
          <el-popconfirm title="确定要删除吗？" @confirm="handleDelete(scope.row.stationId)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="form.stationId ? '编辑站点' : '新增站点'" v-model="dialogVisible" width="500px">
      <el-form label-width="100px" :model="form" :rules="rules" ref="formRef">
        <el-form-item label="站点名称" prop="stationName">
          <el-input v-model="form.stationName" placeholder="请输入站点名称" />
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import StationApi from '@/api/StationApi.js'

const router = useRouter()

const searchForm = ref({
  name: ''
})

const tableData = ref([])
let dialogVisible = ref(false)
let form = ref({
  stationName: ''
})
let formRef = ref(null)

const rules = {
  stationName: [{ required: true, message: '请输入站点名称', trigger: 'blur' }]
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 19)
}

const loadData = () => {
  if (searchForm.value.name) {
    StationApi.queryByName(searchForm.value.name).then((resp) => {
      if (resp.code === 2000 && resp.data) {
        tableData.value = resp.data
      }
    }).catch(err => {
      ElMessage.error('加载数据失败')
    })
  } else {
    StationApi.listStations().then((resp) => {
      if (resp.code === 2000 && resp.data) {
        tableData.value = resp.data
      }
    }).catch(err => {
      ElMessage.error('加载数据失败')
    })
  }
}

const handleReset = () => {
  searchForm.value = {
    name: ''
  }
  loadData()
}

const handleAdd = () => {
  dialogVisible.value = true
  form.value = {
    stationName: ''
  }
}

const handleEdit = (row) => {
  dialogVisible.value = true
  form.value = JSON.parse(JSON.stringify(row))
}

const handleSave = () => {
  formRef.value.validate().then(() => {
    const apiCall = form.value.stationId ? StationApi.updateStation : StationApi.addStation
    apiCall(form.value).then((resp) => {
      ElMessage.success(resp.msg || '保存成功')
      dialogVisible.value = false
      loadData()
    }).catch(err => {
      ElMessage.error('保存失败')
    })
  })
}

const handleDelete = (id) => {
  StationApi.deleteStation(id).then((resp) => {
    ElMessage.success(resp.msg || '删除成功')
    loadData()
  }).catch(err => {
    ElMessage.error('删除失败')
  })
}

const handleViewConnections = (row) => {
  router.push({
    path: '/admin/station/detail',
    query: { id: row.stationId, name: row.stationName }
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
</style>

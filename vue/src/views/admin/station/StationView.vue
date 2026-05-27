<template>
  <div class="station-detail">
    <el-card class="header-card">
      <div class="header-content">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <h2>站点详情：{{ stationName }}</h2>
      </div>
    </el-card>

    <el-card class="mgt-4">
      <template #header>
        <div class="card-header">
          <span>连通站点管理</span>
          <el-button type="primary" @click="handleAddConnection">
            <el-icon><Plus /></el-icon>
            添加连通关系
          </el-button>
        </div>
      </template>

      <el-table :data="connectionData" stripe border style="width: 100%">
        <el-table-column prop="neighborStationId" label="邻站ID" width="120" align="center" />
        <el-table-column prop="neighborStationName" label="邻站名称" min-width="150" align="center" />
        <el-table-column prop="travelTimeMinutes" label="通行时间（分钟）" width="180" align="center" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleEditConnection(scope.row)">
              编辑
            </el-button>
            <el-popconfirm
                title="确定要删除此连通关系吗？"
                @confirm="handleDeleteConnection(scope.row.neighborStationId)"
            >
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="connectionData.length === 0" description="暂无连通站点" />
    </el-card>

    <el-dialog :title="isEditMode ? '编辑通行时间' : '添加连通关系'" v-model="dialogVisible" width="500px">
      <el-form label-width="120px" :model="connectionForm" :rules="connectionRules" ref="connectionFormRef">
        <el-form-item v-if="!isEditMode" label="选择邻站" prop="neighborStationId">
          <el-select v-model="connectionForm.neighborStationId" placeholder="请选择站点" style="width: 100%" filterable>
            <el-option
                v-for="station in availableStations"
                :key="station.stationId"
                :label="station.stationName"
                :value="station.stationId"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-else label="邻站名称">
          <el-input :value="connectionForm.neighborStationName" disabled />
        </el-form-item>
        <el-form-item label="通行时间(分钟)" prop="travelTimeMinutes">
          <el-input-number
              v-model="connectionForm.travelTimeMinutes"
              :min="1"
              :max="10000"
              :step="1"
              style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveConnection">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import StationApi from '@/api/StationApi.js'

const route = useRoute()
const router = useRouter()

const stationId = ref(parseInt(route.query.id))
const stationName = ref(route.query.name || '')
const connectionData = ref([])
const allStations = ref([])
const availableStations = ref([])
let dialogVisible = ref(false)
let isEditMode = ref(false)
let connectionForm = ref({
  neighborStationId: null,
  neighborStationName: '',
  travelTimeMinutes: 30
})
let connectionFormRef = ref(null)

const connectionRules = {
  neighborStationId: [{ required: true, message: '请选择邻站', trigger: 'change' }],
  travelTimeMinutes: [{ required: true, message: '请输入通行时间', trigger: 'blur' }]
}

const goBack = () => {
  router.back()
}

const loadConnections = () => {
  StationApi.getNeighbors(stationId.value).then((resp) => {
    if (resp.code === 200 && resp.data) {
      console.log('后端返回的原始数据:', resp.data)
      connectionData.value = resp.data.map(item => ({
        neighborStationId: item.neighborStationId,
        neighborStationName: item.neighborStationName,
        travelTimeMinutes: item.travelTimeMinutes || 0
      }))
      console.log('处理后的数据:', connectionData.value)
    }
  }).catch(err => {
    console.error('加载连通关系失败:', err)
    ElMessage.error('加载连通关系失败')
  })
}

const loadAllStations = () => {
  StationApi.listStations().then((resp) => {
    if (resp.code === 200 && resp.data) {
      allStations.value = resp.data
      filterAvailableStations()
    }
  }).catch(err => {
    ElMessage.error('加载站点列表失败')
  })
}

const filterAvailableStations = () => {
  const connectedIds = connectionData.value.map(item => item.neighborStationId)
  availableStations.value = allStations.value.filter(station =>
      station.stationId !== stationId.value && !connectedIds.includes(station.stationId)
  )
}

const handleAddConnection = () => {
  filterAvailableStations()
  if (availableStations.value.length === 0) {
    ElMessage.warning('所有站点都已连通或无其他站点可选')
    return
  }
  isEditMode.value = false
  dialogVisible.value = true
  connectionForm.value = {
    neighborStationId: null,
    neighborStationName: '',
    travelTimeMinutes: 30.0  // 使用浮点数，避免类型转换问题
  }
}

const handleEditConnection = (row) => {
  console.log('编辑行数据:', row)
  isEditMode.value = true
  dialogVisible.value = true
  connectionForm.value = {
    neighborStationId: row.neighborStationId,
    neighborStationName: row.neighborStationName,
    travelTimeMinutes: row.travelTimeMinutes
  }
}

const handleSaveConnection = () => {
  connectionFormRef.value.validate().then(() => {
    if (isEditMode.value) {
      StationApi.addConnection(
          stationId.value,
          connectionForm.value.neighborStationId,
          connectionForm.value.travelTimeMinutes
      ).then((resp) => {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        loadConnections()
      }).catch(err => {
        ElMessage.error('更新失败')
      })
    } else {
      StationApi.addConnection(
          stationId.value,
          connectionForm.value.neighborStationId,
          connectionForm.value.travelTimeMinutes
      ).then((resp) => {
        ElMessage.success(resp.message || '添加成功')
        dialogVisible.value = false
        loadConnections()
        loadAllStations()
      }).catch(err => {
        ElMessage.error('添加失败')
      })
    }
  })
}

const handleDeleteConnection = (neighborId) => {
  StationApi.removeConnection(stationId.value, neighborId).then((resp) => {
    ElMessage.success(resp.message || '删除成功')
    loadConnections()
    loadAllStations()
  }).catch(err => {
    ElMessage.error('删除失败')
  })
}

onMounted(() => {
  if (!stationId.value) {
    ElMessage.error('缺少站点ID参数')
    router.back()
    return
  }
  loadConnections()
  loadAllStations()
})
</script>

<style scoped>
.station-detail {
  padding: 20px;
}
.header-card {
  margin-bottom: 20px;
}
.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
}
.header-content h2 {
  margin: 0;
  font-size: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.mgt-4 {
  margin-top: 16px;
}
</style>

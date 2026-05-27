<template>
  <div class="route-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>路线管理</span>
          <el-button type="primary" @click="showCreateDialog">新建路线</el-button>
        </div>
      </template>

      <!-- 路线列表 -->
      <el-table :data="routeList" border stripe>
        <el-table-column prop="routerId" label="路线ID" width="100" align="center" />
        <el-table-column label="站点序列" min-width="300" align="center">
          <template #default="{ row }">
            <el-tag v-for="station in row.stations" :key="station.stationSeq" size="small" style="margin-right: 5px;">
              {{ station.stationSeq }}.{{ station.stationName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" @click="viewRouteDetail(row.routerId)">查看</el-button>
            <el-button size="small" type="primary" @click="editRoute(row.routerId)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteRouteHandler(row.routerId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑路线对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑路线' : '新建路线'"
      width="800px"
    >
      <el-form :model="routeForm" label-width="120px">
        <el-form-item label="路线ID">
          <el-input-number v-model="routeForm.routerId" :min="1" :disabled="isEdit" />
        </el-form-item>

        <el-form-item label="添加站点">
          <div class="station-selector">
            <el-select v-model="selectedStation" placeholder="选择站点" style="width: 200px; margin-right: 10px;">
              <el-option
                v-for="station in allStations"
                :key="station.stationId"
                :label="station.stationName"
                :value="station.stationId"
              />
            </el-select>
            <el-button type="primary" @click="addStation" :disabled="!selectedStation">添加</el-button>
          </div>
        </el-form-item>

        <el-form-item label="站点序列">
          <el-table :data="routeForm.stations" border max-height="300">
            <el-table-column prop="stationSeq" label="序号" width="80" align="center" />
            <el-table-column prop="stationName" label="站点名称" align="center" />
            <el-table-column prop="stayMinutes" label="停留(分钟)" width="120" align="center">
              <template #default="{ row }">
                <el-input-number v-model="row.stayMinutes" :min="0" size="small" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ $index }">
                <el-button size="small" type="danger" @click="removeStation($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRoute">保存</el-button>
      </template>
    </el-dialog>

    <!-- 路线详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="路线详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="路线ID">{{ currentRouteId }}</el-descriptions-item>
        <el-descriptions-item label="站点序列">
          <div v-for="station in routeDetail" :key="station.id" style="margin-bottom: 10px;">
            <el-tag>{{ station.stationSeq }}. {{ station.stationName }}</el-tag>
            <span style="margin-left: 10px; color: #999;">停留 {{ station.stayMinutes }} 分钟</span>
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRouteStations, saveRouteStations, deleteRoute } from '@/api/RouteApi.js'
import { getAllStations } from '@/api/StationApi.js'

const routeList = ref([])
const allStations = ref([])
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const selectedStation = ref(null)
const currentRouteId = ref(null)
const routeDetail = ref([])

const routeForm = ref({
  routerId: 1,
  stations: []
})

// 加载所有站点
const loadAllStations = async () => {
  try {
    const res = await getAllStations()
    if (res.code === 200) {
      allStations.value = res.data || []
    }
  } catch (error) {
    console.error('加载站点失败:', error)
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  routeForm.value = {
    routerId: Math.floor(Math.random() * 1000) + 1,
    stations: []
  }
  dialogVisible.value = true
}

// 添加站点到路线
const addStation = () => {
  if (!selectedStation.value) return
  
  const station = allStations.value.find(s => s.stationId === selectedStation.value)
  if (!station) return

  // 检查是否已添加
  if (routeForm.value.stations.some(s => s.stationId === selectedStation.value)) {
    ElMessage.warning('该站点已在路线中')
    return
  }

  routeForm.value.stations.push({
    stationId: station.stationId,
    stationName: station.stationName,
    stationSeq: routeForm.value.stations.length + 1,
    stayMinutes: 0
  })

  selectedStation.value = null
}

// 移除站点
const removeStation = (index) => {
  routeForm.value.stations.splice(index, 1)
  // 重新编号
  routeForm.value.stations.forEach((s, i) => {
    s.stationSeq = i + 1
  })
}

// 保存路线
const saveRoute = async () => {
  if (routeForm.value.stations.length === 0) {
    ElMessage.warning('请至少添加一个站点')
    return
  }

  try {
    const data = {
      routerId: routeForm.value.routerId,
      stations: routeForm.value.stations.map(s => ({
        stationSeq: s.stationSeq,
        stationId: s.stationId,
        stayMinutes: s.stayMinutes
      }))
    }

    const res = await saveRouteStations(data)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadRouteList()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

// 查看路线详情
const viewRouteDetail = async (routerId) => {
  try {
    const res = await getRouteStations(routerId)
    if (res.code === 200) {
      currentRouteId.value = routerId
      routeDetail.value = res.data || []
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询失败:', error)
  }
}

// 编辑路线
const editRoute = async (routerId) => {
  isEdit.value = true
  routeForm.value.routerId = routerId
  
  try {
    const res = await getRouteStations(routerId)
    if (res.code === 200) {
      routeForm.value.stations = (res.data || []).map(s => ({
        ...s,
        stationName: allStations.value.find(st => st.stationId === s.stationId)?.stationName || ''
      }))
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('加载路线失败:', error)
  }
}

// 删除路线
const deleteRouteHandler = async (routerId) => {
  try {
    await ElMessageBox.confirm('确定要删除该路线吗?', '提示', {
      type: 'warning'
    })
    
    const res = await deleteRoute(routerId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadRouteList()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 加载路线列表(简化版,实际应该从后端获取)
const loadRouteList = async () => {
  // TODO: 实现路线列表查询API
  routeList.value = []
}

onMounted(() => {
  loadAllStations()
  loadRouteList()
})
</script>

<style scoped>
.route-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.station-selector {
  display: flex;
  align-items: center;
}
</style>

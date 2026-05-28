<template>
  <div class="route-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>路线管理</span>
          <el-button type="primary" @click="showCreateDialog">新建路线</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
            v-model="searchKeyword"
            placeholder="输入路线ID或名称搜索"
            clearable
            style="width: 300px;"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>

      <!-- 路线列表（新增总时长列） -->
      <el-table :data="filteredRouteList" border stripe>
        <el-table-column prop="routerId" label="路线ID" width="100" align="center" />
        <el-table-column prop="routerName" label="路线名称" min-width="150" align="center" />
        <!-- 新增总时长列 -->
        <el-table-column label="总时长" width="150" align="center">
          <template #default="{ row }">
            {{ formatDuration(row.totalDuration) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
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
        <el-form-item label="路线名称" required>
          <el-input v-model="routeForm.routerName" placeholder="请输入路线名称" />
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

    <!-- 路线详情对话框（新增总时长显示） -->
    <el-dialog v-model="detailDialogVisible" title="路线详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="路线名称">{{ currentRouteName }}</el-descriptions-item>
        <el-descriptions-item label="路线ID">{{ currentRouteId }}</el-descriptions-item>
        <!-- 新增总时长 -->
        <el-descriptions-item label="总时长">
          {{ formatDuration(currentTotalDuration) }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(currentCreateTime) }}</el-descriptions-item>
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
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getRouteList,
  getRouteDetail,
  createRoute,
  updateRoute,
  deleteRoute
} from '@/api/RouteApi.js'
import { getAllStations } from '@/api/StationApi.js'

// ---------- 数据 ----------
const routeList = ref([])               // 所有路线
const searchKeyword = ref('')           // 搜索关键字
const allStations = ref([])             // 所有站点
const dialogVisible = ref(false)        // 新建/编辑弹窗
const detailDialogVisible = ref(false)  // 详情弹窗
const isEdit = ref(false)               // 是否为编辑模式
const selectedStation = ref(null)       // 当前选中的待添加站点ID

// 当前详情数据
const currentRouteId = ref(null)
const currentRouteName = ref('')
const currentCreateTime = ref('')
const currentTotalDuration = ref(null)  // 新增：存储当前路线的总时长
const routeDetail = ref([])

// 表单数据
const routeForm = ref({
  routerName: '',
  stations: []
})

// ---------- 计算属性 ----------
// 过滤后的路线列表（按ID或名称模糊匹配）
const filteredRouteList = computed(() => {
  if (!searchKeyword.value) return routeList.value
  const kw = searchKeyword.value.toLowerCase()
  return routeList.value.filter(route => {
    return route.routerId.toString().includes(kw) ||
        route.routerName.toLowerCase().includes(kw)
  })
})

// ---------- 辅助函数 ----------
// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 新增：格式化总时长（分钟 → 小时/分钟）
const formatDuration = (minutes) => {
  if (minutes === null || minutes === undefined) return '--'
  const totalMins = Math.round(minutes) // 四舍五入取整
  if (totalMins < 60) return `${totalMins}分钟`
  const hours = Math.floor(totalMins / 60)
  const remainMins = totalMins % 60
  return remainMins === 0 ? `${hours}小时` : `${hours}小时${remainMins}分钟`
}

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

// 加载路线列表
const loadRouteList = async () => {
  try {
    const res = await getRouteList()
    if (res.code === 200) {
      routeList.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载路线列表失败')
    }
  } catch (error) {
    console.error('加载路线列表失败:', error)
    ElMessage.error('加载路线列表失败')
  }
}

// 搜索处理
const handleSearch = () => {
  // 计算属性会自动更新
}

// 显示新建对话框
const showCreateDialog = () => {
  isEdit.value = false
  routeForm.value = {
    routerName: '',
    stations: []
  }
  dialogVisible.value = true
}

// 添加站点到路线
const addStation = () => {
  if (!selectedStation.value) return

  const station = allStations.value.find(s => s.stationId === selectedStation.value)
  if (!station) return

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
  routeForm.value.stations.forEach((s, i) => {
    s.stationSeq = i + 1
  })
}

// 保存路线（新建或编辑）
const saveRoute = async () => {
  if (!routeForm.value.routerName.trim()) {
    ElMessage.warning('请输入路线名称')
    return
  }
  if (routeForm.value.stations.length === 0) {
    ElMessage.warning('请至少添加一个站点')
    return
  }

  const stationsPayload = routeForm.value.stations.map(s => ({
    stationSeq: s.stationSeq,
    stationId: s.stationId,
    stayMinutes: s.stayMinutes
  }))

  try {
    let res
    if (isEdit.value) {
      res = await updateRoute({
        routerId: currentRouteId.value,
        routerName: routeForm.value.routerName,
        stations: stationsPayload
      })
    } else {
      res = await createRoute({
        routerName: routeForm.value.routerName,
        stations: stationsPayload
      })
    }

    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      await loadRouteList()
    } else {
      ElMessage.error(res.message || (isEdit.value ? '更新失败' : '创建失败'))
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

// 查看路线详情（增加总时长赋值）
const viewRouteDetail = async (routerId) => {
  try {
    const res = await getRouteDetail(routerId)
    if (res.code === 200) {
      const data = res.data
      currentRouteId.value = data.routerId
      currentRouteName.value = data.routerName
      currentCreateTime.value = data.createTime
      currentTotalDuration.value = data.totalDuration ?? null  // 新增：获取总时长
      const stationsWithName = (data.stations || []).map(station => {
        const found = allStations.value.find(s => s.stationId === station.stationId)
        return {
          ...station,
          stationName: found ? found.stationName : `站点${station.stationId}`
        }
      })
      routeDetail.value = stationsWithName
      detailDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取详情失败')
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询路线详情失败')
  }
}

// 编辑路线（无需处理总时长，但保留原逻辑）
const editRoute = async (routerId) => {
  isEdit.value = true
  currentRouteId.value = routerId
  try {
    const res = await getRouteDetail(routerId)
    if (res.code === 200) {
      const data = res.data
      routeForm.value.routerName = data.routerName
      const stationsWithName = (data.stations || []).map(station => {
        const found = allStations.value.find(s => s.stationId === station.stationId)
        return {
          stationId: station.stationId,
          stationName: found ? found.stationName : `站点${station.stationId}`,
          stationSeq: station.stationSeq,
          stayMinutes: station.stayMinutes
        }
      })
      routeForm.value.stations = stationsWithName
      dialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取路线信息失败')
    }
  } catch (error) {
    console.error('加载路线失败:', error)
    ElMessage.error('加载路线失败')
  }
}

// 删除路线
const deleteRouteHandler = async (routerId) => {
  try {
    await ElMessageBox.confirm('确定要删除该路线吗？删除后不可恢复！', '提示', {
      type: 'warning'
    })
    const res = await deleteRoute(routerId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadRouteList()
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

// ---------- 生命周期 ----------
onMounted(() => {
  loadAllStations()
  loadRouteList()
})
</script>

<style scoped>
/* 原有样式不变 */
.route-management {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}
.station-selector {
  display: flex;
  align-items: center;
}
</style>
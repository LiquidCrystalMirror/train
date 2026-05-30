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

      <!-- 路线列表 -->
      <el-table :data="filteredRouteList" border stripe v-loading="loading">
        <el-table-column prop="routerId" label="路线ID" width="100" align="center" />
        <el-table-column prop="routerName" label="路线名称" min-width="150" align="center" />
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
        @closed="resetDialog"
    >
      <el-form :model="routeForm" label-width="120px">
        <el-form-item label="路线名称" required>
          <el-input v-model="routeForm.routerName" placeholder="请输入路线名称" />
        </el-form-item>

        <!-- 初始站点选择（仅在新建时显示） -->
        <el-form-item label="起始站点" v-if="!isEdit && routeForm.stations.length === 0">
          <el-select
              v-model="initialStationId"
              placeholder="请选择起始站点"
              style="width: 100%"
              @change="addInitialStation"
          >
            <el-option
                v-for="station in allStations"
                :key="station.stationId"
                :label="station.stationName"
                :value="station.stationId"
            />
          </el-select>
          <div class="form-tip">请先选择起始站点，后续将只显示与当前站点连通的站点</div>
        </el-form-item>

        <!-- 站点添加（基于连通性） -->
        <el-form-item label="添加站点" v-if="routeForm.stations.length > 0">
          <div class="station-selector">
            <el-select
                v-model="selectedStation"
                placeholder="请选择与上一站点连通的站点"
                style="width: 250px; margin-right: 10px;"
                :disabled="!connectedStations.length"
                clearable
            >
              <el-option
                  v-for="station in connectedStations"
                  :key="station.stationId"
                  :label="`${station.stationName} (行程: ${formatDuration(station.travelTimeMinutes)})`"
                  :value="station.stationId"
              />
            </el-select>
            <el-button
                type="primary"
                @click="addStation"
                :disabled="!selectedStation"
            >
              添加
            </el-button>
          </div>
          <div class="form-tip" v-if="!connectedStations.length && routeForm.stations.length > 0">
            <el-alert
                title="当前站点没有可连通的站点，路线到此结束"
                type="info"
                :closable="false"
                show-icon
            />
          </div>
        </el-form-item>

        <el-form-item label="站点序列">
          <el-table :data="routeForm.stations" border max-height="300">
            <el-table-column prop="stationSeq" label="序号" width="80" align="center" />
            <el-table-column prop="stationName" label="站点名称" align="center" />
            <el-table-column label="行程时间" width="120" align="center">
              <template #default="{ row, $index }">
                <span v-if="$index > 0">
                  {{ formatDuration(row.travelTimeFromPrev) }}
                </span>
                <span v-else>起始站</span>
              </template>
            </el-table-column>
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
        <el-button type="primary" @click="saveRoute" :disabled="routeForm.stations.length < 2">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 路线详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="路线详情" width="600px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="路线名称">{{ currentRouteName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="路线ID">{{ currentRouteId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="总时长">
          {{ formatDuration(currentTotalDuration) }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(currentCreateTime) }}</el-descriptions-item>
        <el-descriptions-item label="站点序列">
          <div v-for="(station, index) in routeDetail" :key="index" style="margin-bottom: 10px;">
            <el-tag>{{ station.stationSeq }}. {{ station.stationName }}</el-tag>
            <span style="margin-left: 10px; color: #999;">
              <span v-if="index > 0">行程: {{ formatDuration(station.travelTimeFromPrev) }}, </span>
              停留: {{ station.stayMinutes }} 分钟
            </span>
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getRouteList,
  getRouteDetail,
  createRoute,
  updateRoute,
  deleteRoute
} from '@/api/RouteApi.js'
import stationApi from '@/api/StationApi.js'

// ---------- 数据 ----------
const loading = ref(false)
const routeList = ref([])
const searchKeyword = ref('')
const allStations = ref([])
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const isEdit = ref(false)
const selectedStation = ref(null)
const initialStationId = ref(null)

// 当前详情数据
const currentRouteId = ref(null)
const currentRouteName = ref('')
const currentCreateTime = ref('')
const currentTotalDuration = ref(null)
const routeDetail = ref([])

// 表单数据
const routeForm = ref({
  routerName: '',
  stations: []
})

// 连通站点列表
const connectedStations = ref([])

// ---------- 计算属性 ----------
const filteredRouteList = computed(() => {
  if (!searchKeyword.value) return routeList.value
  const kw = searchKeyword.value.toLowerCase()
  return routeList.value.filter(route => {
    return route.routerId?.toString().includes(kw) ||
        route.routerName?.toLowerCase().includes(kw)
  })
})

const lastStation = computed(() => {
  const stations = routeForm.value.stations
  if (stations.length === 0) return null
  return stations[stations.length - 1]
})

// ---------- 辅助函数 ----------
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const formatDuration = (minutes) => {
  if (minutes === null || minutes === undefined) return '--'
  const totalMins = Math.round(minutes)
  if (totalMins < 60) return `${totalMins}分钟`
  const hours = Math.floor(totalMins / 60)
  const remainMins = totalMins % 60
  return remainMins === 0 ? `${hours}小时` : `${hours}小时${remainMins}分钟`
}

// 检查两个站点是否连通
const checkStationsConnected = async (stationAId, stationBId) => {
  if (!stationAId || !stationBId) return false
  try {
    const res = await stationApi.checkConnection(stationAId, stationBId)
    return res?.code === 200 && res.data === true
  } catch (error) {
    console.error('检查连通性失败:', error)
    return false
  }
}

// 加载所有站点
const loadAllStations = async () => {
  try {
    const res = await stationApi.listStations()
    if (res?.code === 200) {
      allStations.value = res.data || []
      console.log('站点加载成功:', allStations.value.length)
    }
  } catch (error) {
    console.error('加载站点失败:', error)
    ElMessage.error('加载站点失败')
  }
}

// 加载路线列表
const loadRouteList = async () => {
  loading.value = true
  try {
    const res = await getRouteList()
    if (res?.code === 200) {
      routeList.value = res.data || []
    } else {
      ElMessage.error(res?.message || '加载路线列表失败')
    }
  } catch (error) {
    console.error('加载路线列表失败:', error)
    ElMessage.error('加载路线列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {}

const resetDialog = () => {
  initialStationId.value = null
  selectedStation.value = null
  connectedStations.value = []
  routeForm.value = {
    routerName: '',
    stations: []
  }
}

const showCreateDialog = () => {
  isEdit.value = false
  resetDialog()
  dialogVisible.value = true
}

const addInitialStation = async () => {
  if (!initialStationId.value) return

  const station = allStations.value.find(s => s.stationId === initialStationId.value)
  if (!station) {
    ElMessage.error('站点不存在')
    return
  }

  routeForm.value.stations.push({
    stationId: station.stationId,
    stationName: station.stationName,
    stationSeq: 1,
    stayMinutes: 0,
    travelTimeFromPrev: 0
  })

  initialStationId.value = null
  await nextTick()
  await loadConnectedStations()
}

// 【关键修复】加载连通站点 - 适配正确的字段名
const loadConnectedStations = async () => {
  console.log('=== loadConnectedStations 开始 ===')

  if (!lastStation.value) {
    console.log('没有最后站点')
    connectedStations.value = []
    return
  }

  const stationId = lastStation.value.stationId
  console.log(`获取站点 ${stationId} 的连通站点`)

  try {
    const res = await stationApi.getNeighbors(stationId)
    console.log('API 原始响应:', res)

    if (res?.code === 200) {
      const neighbors = res.data || []
      console.log('原始邻居数据:', neighbors)

      // 【关键】适配字段名：可能是 neighborStationId 或 stationId
      const enriched = []
      for (const neighbor of neighbors) {
        // 尝试多种可能的字段名
        let neighborId = neighbor.neighborStationId || neighbor.stationId || neighbor.neighbor_station_id
        let travelTime = neighbor.travelTimeMinutes || neighbor.travel_time_minutes || 0

        console.log(`解析邻居: neighborId=${neighborId}, travelTime=${travelTime}`)

        if (neighborId) {
          const fullStation = allStations.value.find(s => s.stationId === neighborId)
          enriched.push({
            stationId: neighborId,
            stationName: fullStation?.stationName || `站点${neighborId}`,
            travelTimeMinutes: travelTime
          })
        }
      }

      console.log('补充名称后的邻居:', enriched)

      // 过滤掉已经在路线中的站点
      const existingIds = routeForm.value.stations.map(s => s.stationId)
      const filtered = enriched.filter(n => !existingIds.includes(n.stationId))

      console.log('已存在的站点ID:', existingIds)
      console.log('过滤后的连通站点:', filtered)

      connectedStations.value = filtered

      if (filtered.length === 0) {
        console.warn(`站点 ${lastStation.value.stationName}(${stationId}) 没有可添加的连通站点`)
      } else {
        console.log(`✅ 找到 ${filtered.length} 个连通站点`)
      }
    }
  } catch (error) {
    console.error('获取连通站点失败:', error)
    connectedStations.value = []
  }
}

const addStation = async () => {
  if (!selectedStation.value) return

  // 验证连通性
  const isConnected = await checkStationsConnected(
      lastStation.value.stationId,
      selectedStation.value
  )

  if (!isConnected) {
    ElMessage.error('站点不连通，无法添加')
    return
  }

  const neighbor = connectedStations.value.find(s => s.stationId === selectedStation.value)
  if (!neighbor) {
    ElMessage.error('未找到站点信息')
    return
  }

  const station = allStations.value.find(s => s.stationId === selectedStation.value)
  if (!station) {
    ElMessage.error('站点不存在')
    return
  }

  routeForm.value.stations.push({
    stationId: station.stationId,
    stationName: station.stationName,
    stationSeq: routeForm.value.stations.length + 1,
    stayMinutes: 0,
    travelTimeFromPrev: neighbor.travelTimeMinutes || 0
  })

  selectedStation.value = null
  await loadConnectedStations()
}

const removeStation = async (index) => {
  routeForm.value.stations.splice(index, 1)
  routeForm.value.stations.forEach((s, i) => {
    s.stationSeq = i + 1
  })
  await loadConnectedStations()
}

const saveRoute = async () => {
  if (!routeForm.value.routerName?.trim()) {
    ElMessage.warning('请输入路线名称')
    return
  }
  if (routeForm.value.stations.length < 2) {
    ElMessage.warning('路线至少需要2个站点')
    return
  }

  // 验证所有站点的连通性
  for (let i = 1; i < routeForm.value.stations.length; i++) {
    const prevStation = routeForm.value.stations[i - 1]
    const currStation = routeForm.value.stations[i]

    const isConnected = await checkStationsConnected(
        prevStation.stationId,
        currStation.stationId
    )

    if (!isConnected) {
      ElMessage.error(`站点 "${prevStation.stationName}" 和 "${currStation.stationName}" 之间不连通，无法保存路线`)
      return
    }
  }

  const stationsPayload = routeForm.value.stations.map(s => ({
    stationSeq: s.stationSeq,
    stationId: s.stationId,
    stayMinutes: s.stayMinutes || 0
  }))

  try {
    let res
    if (isEdit.value) {
      res = await updateRoute({
        routerId: currentRouteId.value,
        routerName: routeForm.value.routerName.trim(),
        stations: stationsPayload
      })
    } else {
      res = await createRoute({
        routerName: routeForm.value.routerName.trim(),
        stations: stationsPayload
      })
    }

    if (res?.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      await loadRouteList()
    } else {
      ElMessage.error(res?.message || (isEdit.value ? '更新失败' : '创建失败'))
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

const viewRouteDetail = async (routerId) => {
  try {
    const res = await getRouteDetail(routerId)
    if (res?.code === 200) {
      const data = res.data
      currentRouteId.value = data.routerId
      currentRouteName.value = data.routerName
      currentCreateTime.value = data.createTime
      currentTotalDuration.value = data.totalDuration ?? null

      const stationsWithTravelTime = []
      for (let i = 0; i < (data.stations || []).length; i++) {
        const station = data.stations[i]
        const found = allStations.value.find(s => s.stationId === station.stationId)
        let travelTimeFromPrev = 0

        if (i > 0) {
          const prevStation = data.stations[i - 1]
          const travelTime = await getTravelTimeBetweenStations(
              prevStation.stationId,
              station.stationId
          )
          travelTimeFromPrev = travelTime
        }

        stationsWithTravelTime.push({
          ...station,
          stationName: found ? found.stationName : `站点${station.stationId}`,
          travelTimeFromPrev
        })
      }

      routeDetail.value = stationsWithTravelTime
      detailDialogVisible.value = true
    } else {
      ElMessage.error(res?.message || '获取详情失败')
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询路线详情失败')
  }
}

const editRoute = async (routerId) => {
  isEdit.value = true
  currentRouteId.value = routerId
  try {
    const res = await getRouteDetail(routerId)
    if (res?.code === 200) {
      const data = res.data
      routeForm.value.routerName = data.routerName

      const stationsWithName = (data.stations || []).map((station) => {
        const found = allStations.value.find(s => s.stationId === station.stationId)
        return {
          stationId: station.stationId,
          stationName: found ? found.stationName : `站点${station.stationId}`,
          stationSeq: station.stationSeq,
          stayMinutes: station.stayMinutes || 0,
          travelTimeFromPrev: 0
        }
      })

      routeForm.value.stations = stationsWithName
      dialogVisible.value = true
    } else {
      ElMessage.error(res?.message || '获取路线信息失败')
    }
  } catch (error) {
    console.error('加载路线失败:', error)
    ElMessage.error('加载路线失败')
  }
}

const deleteRouteHandler = async (routerId) => {
  try {
    await ElMessageBox.confirm('确定要删除该路线吗？删除后不可恢复！', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    const res = await deleteRoute(routerId)

    if (res?.code === 200) {
      ElMessage.success('删除成功')
      await loadRouteList()
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 获取两个站点之间的行程时间（简化版）
const getTravelTimeBetweenStations = async (stationAId, stationBId) => {
  try {
    const res = await stationApi.getNeighbors(stationAId)
    if (res?.code === 200) {
      const neighbor = res.data.find(n =>
          (n.neighborStationId === stationBId || n.stationId === stationBId)
      )
      return neighbor?.travelTimeMinutes || 0
    }
    return 0
  } catch (error) {
    return 0
  }
}

// ---------- 生命周期 ----------
onMounted(async () => {
  console.log('组件挂载，开始加载数据...')
  await loadAllStations()
  await loadRouteList()
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
.search-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}
.station-selector {
  display: flex;
  align-items: center;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
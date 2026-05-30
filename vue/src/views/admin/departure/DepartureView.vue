<template>
  <div class="departure-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>车次管理</span>
          <el-button type="primary" @click="showCreateDialog">新建车次</el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="列车">
          <el-select v-model="searchForm.trainId" placeholder="选择列车" clearable style="width: 200px;" @change="onSearchTrainChange">
            <el-option
                v-for="train in trainList"
                :key="train.trainId"
                :label="train.trainNumber"
                :value="train.trainId"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadSchedules">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 车次列表 -->
      <div v-if="showTable">
        <el-table :data="scheduleList" border stripe style="width: 100%;">
          <el-table-column prop="trainNumber" label="车次编号" min-width="120" align="center" />
          <el-table-column label="路线" min-width="200" align="center">
            <template #default="{ row }">
              {{ getRouterNameByRouterId(row.routerId) }}
            </template>
          </el-table-column>
          <el-table-column label="发车时间" min-width="180" align="center">
            <template #default="{ row }">
              {{ formatDateTime(row.departureTime) }}
            </template>
          </el-table-column>
          <el-table-column label="运行方向" min-width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getDirectionType(row.routerId)" size="small">
                {{ getDirectionName(row.routerId) }}
              </el-tag>
            </template>
          </el-table-column>
          <!-- 操作列已移除，车次一旦创建不可修改或删除 -->
        </el-table>

        <!-- 提示信息 -->
        <div class="table-footer-tip">
          <el-alert
              title="提示"
              type="info"
              :closable="false"
              show-icon
          >
            车次一旦创建即开始售票，不可修改或删除。请确认发车时间和方向无误后再创建。
          </el-alert>
        </div>
      </div>
      <div v-else class="empty-placeholder">
        <el-empty description="请选择列车并点击查询" :image-size="120" />
      </div>
    </el-card>

    <!-- 创建车次对话框 -->
    <el-dialog v-model="dialogVisible" title="新建车次" width="500px" @close="resetDialog">
      <el-form :model="scheduleForm" label-width="100px">
        <el-form-item label="选择列车" required>
          <el-select v-model="scheduleForm.trainId" placeholder="选择列车" style="width: 100%;" @change="onTrainChange">
            <el-option
                v-for="train in trainList"
                :key="train.trainId"
                :label="train.trainNumber"
                :value="train.trainId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="运行方向" required v-if="availableRouters.length > 0">
          <el-radio-group v-model="scheduleForm.selectedRouterId">
            <el-radio
                v-for="router in availableRouters"
                :key="router.routerId"
                :value="router.routerId"
            >
              {{ router.routerName }}（{{ router.direction }}）
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="发车时间" required>
          <el-date-picker
              v-model="scheduleForm.departureTime"
              type="datetime"
              placeholder="选择发车时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="width: 100%;"
              :disabled-date="disabledPastDate"
          />
        </el-form-item>

        <!-- 运行信息预览 -->
        <el-alert
            v-if="scheduleForm.selectedRouterId && selectedRouteDuration && scheduleForm.departureTime"
            title="运行信息"
            type="success"
            :closable="false"
            show-icon
        >
          <div>运行方向: {{ getDirectionName(scheduleForm.selectedRouterId) }}</div>
          <div>总耗时: {{ formatDuration(selectedRouteDuration) }}</div>
          <div>预计到达时间: {{ calculateArrivalTime() }}</div>
        </el-alert>

        <!-- 重要提示 -->
        <el-alert
            title="重要提示"
            type="warning"
            :closable="false"
            show-icon
            style="margin-top: 16px;"
        >
          <div style="font-size: 13px; line-height: 1.5;">
            车次创建后将立即开始售票，<strong>不可修改或删除</strong>。请仔细核对发车时间和运行方向。
          </div>
        </el-alert>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSchedule" :loading="creating">
          确认创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSchedules, createSchedule, deleteSchedule } from '@/api/DepartureApi.js'
import { getTrainList } from '@/api/TrainApi.js'
import { getRouteList, getRouteDetail } from '@/api/RouteApi.js'

const scheduleList = ref([])
const trainList = ref([])
const routeList = ref([])
const routeDetailMap = ref(new Map())
const dialogVisible = ref(false)
const showTable = ref(false)
const creating = ref(false)

const searchForm = ref({
  trainId: null
})

const scheduleForm = ref({
  trainId: null,
  departureTime: '',
  selectedRouterId: null
})

// 当前选中列车可用的路线列表（往程和返程）
const availableRouters = ref([])

// 获取选中路线的耗时
const selectedRouteDuration = computed(() => {
  if (!scheduleForm.value.selectedRouterId) return null
  const route = routeList.value.find(r => r.routerId === scheduleForm.value.selectedRouterId)
  return route?.totalDuration || null
})

// 禁用过去的日期
const disabledPastDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

// 格式化时长
const formatDuration = (minutes) => {
  if (!minutes && minutes !== 0) return '--'
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins === 0 ? `${hours}小时` : `${hours}小时${mins}分钟`
}

// 计算预计到达时间
const calculateArrivalTime = () => {
  if (!scheduleForm.value.departureTime || !selectedRouteDuration.value) return '请先选择发车时间'
  try {
    const departureDate = new Date(scheduleForm.value.departureTime)
    const arrivalDate = new Date(departureDate.getTime() + selectedRouteDuration.value * 60 * 1000)
    return arrivalDate.toLocaleString('zh-CN')
  } catch (e) {
    return '计算失败'
  }
}

// 获取基础路线名称（去掉（往）/（返）后缀）
const getBaseRouteName = (routerName) => {
  if (!routerName) return '-'
  return routerName.replace(/[（(]往[）)]|[（(]返[）)]/g, '').trim()
}

// 格式化日期时间（用于显示）
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  try {
    let dateStr = dateTimeStr
    if (dateTimeStr.includes('T')) {
      dateStr = dateTimeStr.replace('T', ' ')
    }
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN')
  } catch (e) {
    return dateTimeStr
  }
}

// 根据 routerId 获取路线名称
const getRouterNameByRouterId = (routerId) => {
  if (!routerId) return '-'
  const route = routeList.value.find(r => r.routerId === routerId)
  if (route) {
    return getBaseRouteName(route.routerName)
  }
  return `路线${routerId}`
}

// 根据 routerId 获取运行方向名称
const getDirectionName = (routerId) => {
  if (!routerId) return '未知'
  const route = routeList.value.find(r => r.routerId === routerId)
  if (route) {
    if (route.routerName.includes('（往）') || route.routerName.includes('(往)')) {
      return '往程'
    }
    if (route.routerName.includes('（返）') || route.routerName.includes('(返)')) {
      return '返程'
    }
  }
  return routerId % 2 === 0 ? '往程' : '返程'
}

// 根据 routerId 获取标签类型
const getDirectionType = (routerId) => {
  const direction = getDirectionName(routerId)
  return direction === '往程' ? 'primary' : 'success'
}

// 重置对话框
const resetDialog = () => {
  scheduleForm.value = {
    trainId: null,
    departureTime: '',
    selectedRouterId: null
  }
  availableRouters.value = []
}

// 列车选择变化时，设置可用的路线列表
const onTrainChange = (trainId) => {
  const train = trainList.value.find(t => t.trainId === trainId)
  if (train) {
    // 构建可用的路线列表
    const routers = []

    // 添加往程路线
    if (train.routerId) {
      const forwardRoute = routeList.value.find(r => r.routerId === train.routerId)
      if (forwardRoute) {
        routers.push({
          routerId: train.routerId,
          routerName: getBaseRouteName(forwardRoute.routerName),
          direction: '往程'
        })
      }
    }

    // 添加返程路线
    if (train.oppsiteRouterId) {
      const returnRoute = routeList.value.find(r => r.routerId === train.oppsiteRouterId)
      if (returnRoute) {
        routers.push({
          routerId: train.oppsiteRouterId,
          routerName: getBaseRouteName(returnRoute.routerName),
          direction: '返程'
        })
      }
    }

    availableRouters.value = routers

    // 默认选中第一个
    if (routers.length > 0) {
      scheduleForm.value.selectedRouterId = routers[0].routerId
    } else {
      scheduleForm.value.selectedRouterId = null
    }
  } else {
    availableRouters.value = []
    scheduleForm.value.selectedRouterId = null
  }
}

// 搜索时列车选择变化
const onSearchTrainChange = (trainId) => {
  if (!trainId) {
    showTable.value = false
    scheduleList.value = []
  }
}

// 加载所有路线
const loadRouteList = async () => {
  try {
    const res = await getRouteList()
    if (res.code === 200) {
      routeList.value = res.data || []
      console.log('路线列表:', routeList.value)
    }
  } catch (error) {
    console.error('加载路线列表失败:', error)
  }
}

// 加载路线详情（获取站点数）
const loadRouteDetail = async (routerId) => {
  if (!routerId) return null
  if (routeDetailMap.value.has(routerId)) {
    return routeDetailMap.value.get(routerId)
  }
  try {
    const res = await getRouteDetail(routerId)
    if (res.code === 200 && res.data) {
      routeDetailMap.value.set(routerId, res.data)
      return res.data
    }
  } catch (error) {
    console.error('加载路线详情失败:', error)
  }
  return null
}

// 加载列车列表
const loadTrainList = async () => {
  try {
    const res = await getTrainList({ pageNum: 1, pageSize: 100, find: '' })
    if (res.code === 200) {
      trainList.value = res.data?.records || []
      console.log('列车列表:', trainList.value)
    }
  } catch (error) {
    console.error('加载列车列表失败:', error)
  }
}

// 加载车次列表
const loadSchedules = async () => {
  if (!searchForm.value.trainId) {
    ElMessage.warning('请选择列车')
    showTable.value = false
    scheduleList.value = []
    return
  }

  try {
    const res = await getSchedules(searchForm.value.trainId)
    console.log('车次列表响应:', res)

    if (res.code === 200) {
      const data = res.data || []
      // 为每条数据添加车次编号
      scheduleList.value = data.map(item => {
        const train = trainList.value.find(t => t.trainId === item.trainId)
        return {
          ...item,
          trainNumber: train ? train.trainNumber : `车次${item.trainId}`
        }
      })
      showTable.value = true

      if (scheduleList.value.length === 0) {
        ElMessage.info('该列车暂无车次数据')
      }
    } else {
      ElMessage.error(res.message || '查询失败')
      showTable.value = false
      scheduleList.value = []
    }
  } catch (error) {
    console.error('查询失败:', error)
    ElMessage.error('查询失败')
    showTable.value = false
    scheduleList.value = []
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  resetDialog()
  dialogVisible.value = true
}

// 创建车次
const handleCreateSchedule = async () => {
  if (!scheduleForm.value.trainId) {
    ElMessage.warning('请选择列车')
    return
  }
  if (!scheduleForm.value.selectedRouterId) {
    ElMessage.warning('请选择运行方向')
    return
  }
  if (!scheduleForm.value.departureTime) {
    ElMessage.warning('请选择发车时间')
    return
  }

  // 二次确认
  try {
    await ElMessageBox.confirm(
        `确认创建车次吗？\n\n车次将立即开始售票，创建后不可修改或删除！`,
        '重要确认',
        {
          confirmButtonText: '确认创建',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
  } catch {
    return
  }

  creating.value = true
  try {
    // 确保日期格式为 ISO-8601 格式（YYYY-MM-DDTHH:mm:ss）
    let departureTime = scheduleForm.value.departureTime
    if (departureTime && departureTime.includes(' ')) {
      departureTime = departureTime.replace(' ', 'T')
    }

    const data = {
      trainId: scheduleForm.value.trainId,
      departureTime: departureTime,
      routerId: scheduleForm.value.selectedRouterId
    }

    console.log('创建车次参数:', data)
    const res = await createSchedule(data)
    if (res.code === 200) {
      ElMessage.success('创建成功，车次已开始售票')
      dialogVisible.value = false
      if (searchForm.value.trainId === scheduleForm.value.trainId) {
        await loadSchedules()
      } else if (searchForm.value.trainId === null) {
        // 如果之前没有选中列车，自动选中刚创建的列车
        searchForm.value.trainId = scheduleForm.value.trainId
        await loadSchedules()
      }
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    console.error('创建失败:', error)
    if (error.message && error.message.includes('Duplicate entry')) {
      ElMessage.error('创建失败：该车次在此发车时间已存在')
    } else {
      ElMessage.error('创建失败：' + (error.message || '未知错误'))
    }
  } finally {
    creating.value = false
  }
}

onMounted(() => {
  loadRouteList()
  loadTrainList()
})
</script>

<style scoped>
.departure-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.empty-placeholder {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.table-footer-tip {
  margin-top: 16px;
}
</style>
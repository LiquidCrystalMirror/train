<template>
  <div class="train-management">
    <!-- 搜索表单和操作按钮 -->
    <div class="search-bar">
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
      <div class="action-buttons">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增车次
        </el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table :data="displayData" stripe border style="width: 100%;" v-loading="loading" row-key="id">
        <el-table-column prop="trainNumber" label="车次号" align="center" min-width="120" />
        <el-table-column label="路线名称" align="center" min-width="200">
          <template #default="scope">
            {{ getBaseRouteName(scope.row.routerName) }}
          </template>
        </el-table-column>
        <el-table-column label="总耗时(分钟)" align="center" width="120">
          <template #default="scope">
            {{ formatDuration(scope.row.timeConsuming) }}
          </template>
        </el-table-column>
        <el-table-column label="方向" align="center" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.direction === 'return' ? 'success' : 'primary'" size="small">
              {{ scope.row.direction === 'return' ? '返程' : '往程' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" align="center" width="260">
          <template #default="scope">
            <div class="action-buttons-cell">
              <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="warning" @click="handlePriceConfig(scope.row)">价格配置</el-button>
              <el-popconfirm title="确定要删除吗？" @confirm="handleDelete(scope.row.trainId)">
                <template #reference>
                  <el-button size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <el-pagination
        class="mgt-4"
        v-model:current-page="searchForm.pageNum"
        v-model:page-size="searchForm.pageSize"
        layout="total, prev, pager, next, jumper"
        :total="total"
        @current-change="loadData"
    />

    <!-- 编辑/新增对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" @close="resetForm">
      <el-form label-width="100px" :model="form" :rules="rules" ref="formRef">
        <el-form-item label="车次号" prop="trainNumber">
          <el-input v-model="form.trainNumber" placeholder="如：G1001" />
        </el-form-item>
        <el-form-item label="所属路线" prop="routerId">
          <el-select v-model="form.routerId" placeholder="选择路线" style="width: 100%" clearable @change="onRouteChange">
            <el-option
                v-for="route in allRouteList"
                :key="route.routerId"
                :label="getBaseRouteName(route.routerName)"
                :value="route.routerId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="总耗时" v-if="form.routerId">
          <el-input :value="getSelectedRouteDuration()" disabled placeholder="选择路线后自动计算" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 价格配置对话框 -->
    <el-dialog :title="priceDialogTitle" v-model="priceDialogVisible" width="700px" @close="resetPriceForm">
      <div class="price-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="车次号">{{ currentTrain.trainNumber }}</el-descriptions-item>
          <el-descriptions-item label="路线名称">{{ getBaseRouteName(currentTrain.routerName) }}</el-descriptions-item>
          <el-descriptions-item label="方向">
            <el-tag :type="currentTrain.direction === 'return' ? 'success' : 'primary'" size="small">
              {{ currentTrain.direction === 'return' ? '返程' : '往程' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总耗时">{{ formatDuration(currentTrain.timeConsuming) }}</el-descriptions-item>
          <el-descriptions-item label="总站点数" :span="2">
            <el-tag type="info">{{ totalStationsCount }} 站</el-tag>
            <span class="tip-text" style="margin-left: 12px; color: #909399; font-size: 12px">
              提示：区间站数包含起始站
            </span>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="price-table-container">
        <el-table :data="displayPriceList" stripe border style="width: 100%; margin-top: 20px">
          <el-table-column prop="stationCount" label="区间站数" align="center" width="120">
            <template #default="scope">
              {{ scope.row.stationCount }} 站
            </template>
          </el-table-column>
          <el-table-column prop="price" label="价格(元)" align="center" min-width="180">
            <template #default="scope">
              <el-input-number
                  v-model="scope.row.price"
                  :min="0"
                  :precision="2"
                  :step="10"
                  controls-position="right"
                  style="width: 160px"
                  placeholder="请输入价格"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="100">
            <template #default="scope">
              <el-button
                  type="primary"
                  size="small"
                  @click="saveSinglePrice(scope.row)"
                  :loading="scope.row.saving"
              >
                保存
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="price-batch-actions" style="margin-top: 20px; text-align: right">
        <el-button type="primary" @click="batchSavePrices" :loading="batchSaving">
          批量保存所有价格
        </el-button>
      </div>

      <template #footer>
        <el-button @click="priceDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import * as TrainApi from '@/api/TrainApi.js'
import { getRouteList, getRouteDetail } from '@/api/RouteApi.js'
import { getPriceList, setPrice, batchSetPrices } from '@/api/ExtraApi.js'

// 基础价格（第1站固定价格）
const BASE_PRICE = 50

const searchForm = ref({
  find: '',
  pageNum: 1,
  pageSize: 10
})

const rawData = ref([])
const routeList = ref([])
const routeDetailMap = ref(new Map())
const loading = ref(false)
let dialogVisible = ref(false)
let dialogTitle = ref('新增车次')
let form = ref({
  trainId: null,
  trainNumber: '',
  routerId: null
})
let formRef = ref(null)

// 价格配置相关
const priceDialogVisible = ref(false)
const priceDialogTitle = ref('价格梯度配置')
const currentTrain = ref({})
const priceList = ref([]) // 存储所有价格（包括第1站）
const displayPriceList = ref([]) // 显示给用户的价格列表（从2站开始到总站数）
const batchSaving = ref(false)

// 表单验证规则
const rules = {
  trainNumber: [{ required: true, message: '请输入车次号', trigger: 'blur' }],
  routerId: [{ required: true, message: '请选择路线', trigger: 'change' }]
}

// 格式化时长
const formatDuration = (minutes) => {
  if (!minutes && minutes !== 0) return '--'
  if (minutes < 60) return `${minutes}分钟`
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  return mins === 0 ? `${hours}小时` : `${hours}小时${mins}分钟`
}

// 获取基础路线名称
const getBaseRouteName = (routerName) => {
  if (!routerName) return '-'
  return routerName.replace(/[（(]往[）)]|[（(]返[）)]/g, '').trim()
}

// 根据路线ID获取路线名称
const getRouterName = (routerId) => {
  if (!routerId) return '-'
  const route = routeList.value.find(r => r.routerId === routerId)
  return route ? route.routerName : `路线${routerId}`
}

// 根据路线ID获取路线耗时
const getRouterDuration = (routerId) => {
  if (!routerId) return null
  const route = routeList.value.find(r => r.routerId === routerId)
  return route ? route.totalDuration : null
}

// 所有路线
const allRouteList = computed(() => routeList.value)

// 获取当前选中路线的总耗时
const getSelectedRouteDuration = () => {
  if (!form.value.routerId) return '请先选择路线'
  const duration = getRouterDuration(form.value.routerId)
  if (duration) {
    return `${formatDuration(duration)}`
  }
  return '该路线尚未配置耗时'
}

// 将原始数据拆分为往程和返程两行
const displayData = computed(() => {
  const result = []
  rawData.value.forEach(item => {
    const forwardDuration = getRouterDuration(item.routerId)
    result.push({
      id: `${item.trainId}-forward`,
      trainId: item.trainId,
      trainNumber: item.trainNumber,
      routerId: item.routerId,
      routerName: getRouterName(item.routerId),
      timeConsuming: forwardDuration,
      direction: 'forward'
    })

    if (item.oppsiteRouterId) {
      const returnDuration = getRouterDuration(item.oppsiteRouterId)
      result.push({
        id: `${item.trainId}-return`,
        trainId: item.trainId,
        trainNumber: item.trainNumber,
        routerId: item.oppsiteRouterId,
        routerName: getRouterName(item.oppsiteRouterId),
        timeConsuming: returnDuration,
        direction: 'return'
      })
    }
  })

  if (searchForm.value.find) {
    const keyword = searchForm.value.find.toLowerCase()
    return result.filter(item =>
        item.trainNumber?.toLowerCase().includes(keyword) ||
        item.routerName?.toLowerCase().includes(keyword)
    )
  }
  return result
})

const total = computed(() => displayData.value.length)
const totalStationsCount = computed(() => {
  const routeDetail = routeDetailMap.value.get(currentTrain.value.routerId)
  return routeDetail?.stations?.length || 0
})

// 加载所有路线
const loadRouteList = async () => {
  try {
    const res = await getRouteList()
    if (res.code === 200) {
      routeList.value = res.data || []
    } else {
      ElMessage.error('加载路线列表失败')
    }
  } catch (error) {
    console.error('加载路线列表失败:', error)
    ElMessage.error('加载路线列表失败')
  }
}

// 加载路线详情
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

// 加载价格列表
const loadPriceList = async (trainId, routerId) => {
  try {
    const res = await getPriceList(trainId)
    const routeDetail = await loadRouteDetail(routerId)
    const totalStations = routeDetail?.stations?.length || 0

    const priceMap = new Map()
    if (res.code === 200 && res.data) {
      res.data.forEach(item => {
        priceMap.set(item.stationCount, item.price)
      })
    }

    // 生成完整的价格列表（从1站到总站数）
    const fullPriceList = []
    for (let i = 1; i <= totalStations; i++) {
      fullPriceList.push({
        stationCount: i,
        price: priceMap.has(i) ? priceMap.get(i) : (i === 1 ? BASE_PRICE : null),
        saving: false
      })
    }

    // 显示给用户的列表（从2站开始到总站数，不包含第1站）
    const displayList = fullPriceList.slice(1).map(item => ({
      ...item,
      saving: false
    }))

    return { fullPriceList, displayList }
  } catch (error) {
    console.error('加载价格列表失败:', error)
    return { fullPriceList: [], displayList: [] }
  }
}

// 打开价格配置对话框
const handlePriceConfig = async (row) => {
  currentTrain.value = row
  priceDialogTitle.value = `价格梯度配置 - ${row.trainNumber} (${getBaseRouteName(row.routerName)})`
  priceDialogVisible.value = true

  const { fullPriceList, displayList } = await loadPriceList(row.trainId, row.routerId)
  priceList.value = fullPriceList
  displayPriceList.value = displayList

  if (displayList.length === 0) {
    ElMessage.warning('请先配置路线站点信息')
  }
}

// 保存单个价格梯度
const saveSinglePrice = async (priceItem) => {
  if (priceItem.price === null || priceItem.price === undefined) {
    ElMessage.warning('请输入价格')
    return
  }

  priceItem.saving = true
  try {
    // 准备要保存的所有价格数据（包括第1站）
    const allPriceData = []

    // 1. 添加第1站基础价格
    allPriceData.push({
      stationCount: 1,
      price: BASE_PRICE
    })

    // 2. 添加所有显示的价格（从2站到总站数）
    displayPriceList.value.forEach(item => {
      allPriceData.push({
        stationCount: item.stationCount,
        price: item.price
      })
    })

    console.log('保存单个价格时的所有数据:', allPriceData)

    const res = await batchSetPrices(currentTrain.value.trainId, allPriceData)

    if (res.code === 200) {
      // 更新本地价格列表
      priceList.value = allPriceData

      // 更新显示列表
      const displayItem = displayPriceList.value.find(p => p.stationCount === priceItem.stationCount)
      if (displayItem) {
        displayItem.price = priceItem.price
      }

      ElMessage.success(`保存成功：${priceItem.stationCount}站价格 ${priceItem.price}元`)
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存价格失败:', error)
    ElMessage.error('保存价格失败')
  } finally {
    priceItem.saving = false
  }
}

// 批量保存所有价格
const batchSavePrices = async () => {
  const invalidItems = displayPriceList.value.filter(item => item.price === null || item.price === undefined)
  if (invalidItems.length > 0) {
    ElMessage.warning(`请先填写所有价格梯度（共${invalidItems.length}项未填写）`)
    return
  }

  // 准备所有价格数据（包括第1站）
  const allPriceData = []

  // 1. 第1站基础价格
  allPriceData.push({
    stationCount: 1,
    price: BASE_PRICE
  })

  // 2. 所有显示的价格（从2站到总站数）
  displayPriceList.value.forEach(item => {
    allPriceData.push({
      stationCount: item.stationCount,
      price: item.price
    })
  })

  console.log('批量保存的所有价格数据:', allPriceData)

  batchSaving.value = true
  try {
    const res = await batchSetPrices(currentTrain.value.trainId, allPriceData)
    if (res.code === 200) {
      priceList.value = allPriceData
      ElMessage.success(`批量保存成功！共保存 ${allPriceData.length} 个价格梯度`)
    } else {
      ElMessage.error(res.message || '批量保存失败')
    }
  } catch (error) {
    console.error('批量保存失败:', error)
    ElMessage.error('批量保存失败')
  } finally {
    batchSaving.value = false
  }
}

// 重置价格表单
const resetPriceForm = () => {
  currentTrain.value = {}
  priceList.value = []
  displayPriceList.value = []
}

// 重置新增/编辑表单
const resetForm = () => {}

// 加载车次数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await TrainApi.getTrainPage({
      pageNum: searchForm.value.pageNum,
      pageSize: searchForm.value.pageSize,
      find: searchForm.value.find
    })
    if (res.code === 200 && res.data) {
      rawData.value = res.data.records || []
    }
  } catch (err) {
    console.error('加载数据失败:', err)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
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

// 新增车次
const handleAdd = () => {
  dialogTitle.value = '新增车次'
  dialogVisible.value = true
  form.value = {
    trainId: null,
    trainNumber: '',
    routerId: null
  }
}

// 路线选择变化
const onRouteChange = async (routerId) => {
  if (routerId) {
    await loadRouteDetail(routerId)
  }
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑车次'
  dialogVisible.value = true
  form.value = {
    trainId: row.trainId,
    trainNumber: row.trainNumber,
    routerId: row.routerId
  }
  if (row.routerId) {
    loadRouteDetail(row.routerId)
  }
}

// 保存（新增或更新）
const handleSave = () => {
  formRef.value.validate().then(async () => {
    try {
      let res
      if (form.value.trainId) {
        res = await TrainApi.updateTrain({
          trainId: form.value.trainId,
          trainNumber: form.value.trainNumber,
          routerId: form.value.routerId
        })
        if (res.code === 200) {
          ElMessage.success('保存成功')
          dialogVisible.value = false
          await loadData()
          await loadRouteList()
        } else {
          ElMessage.error(res.message || '保存失败')
        }
      } else {
        res = await TrainApi.addTrain({
          trainNumber: form.value.trainNumber,
          routerId: form.value.routerId
        })

        if (res.code === 200) {
          const newTrainId = res.data?.trainId || res.data
          const routeDetail = await loadRouteDetail(form.value.routerId)
          const totalStations = routeDetail?.stations?.length || 0

          if (totalStations > 1) {
            const allPriceData = []

            // 第1站基础价格
            allPriceData.push({
              stationCount: 1,
              price: BASE_PRICE
            })

            // 生成区间价格（2站到总站数）
            for (let i = 2; i <= totalStations; i++) {
              const defaultPrice = i * 30 + 20
              allPriceData.push({
                stationCount: i,
                price: defaultPrice
              })
            }

            try {
              await batchSetPrices(newTrainId, allPriceData)
              ElMessage.success(`车次创建成功，已初始化 ${allPriceData.length} 个价格梯度`)
            } catch (priceError) {
              console.error('价格梯度初始化失败:', priceError)
              ElMessage.warning('车次创建成功，但价格梯度初始化失败，请手动配置')
            }
          } else {
            ElMessage.success('车次创建成功')
          }

          dialogVisible.value = false
          await loadData()
          await loadRouteList()
        } else {
          ElMessage.error(res.message || '保存失败')
        }
      }
    } catch (error) {
      console.error('保存失败:', error)
      ElMessage.error('保存失败')
    }
  }).catch(() => {})
}

// 删除
const handleDelete = (trainId) => {
  TrainApi.deleteTrain(trainId).then(() => {
    ElMessage.success('删除成功')
    loadData()
  }).catch(err => {
    console.error('删除失败:', err)
    ElMessage.error('删除失败')
  })
}

onMounted(() => {
  loadData()
  loadRouteList()
})
</script>

<style scoped>
.train-management {
  padding: 20px;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.search-form {
  flex: 1;
}

.action-buttons {
  margin-left: 20px;
}

.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.mgt-4 {
  margin-top: 16px;
}

.el-pagination {
  padding: 16px;
  text-align: right;
}

.price-info {
  margin-bottom: 20px;
}

.price-table-container {
  max-height: 500px;
  overflow-y: auto;
}

.action-buttons-cell {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
  flex-wrap: nowrap;
}

.action-buttons-cell .el-button {
  margin: 0;
  padding: 5px 10px;
}

:deep(.el-input-number .el-input__inner) {
  text-align: center;
}

.tip-text {
  font-size: 12px;
}
</style>
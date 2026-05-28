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
          <el-select v-model="searchForm.trainId" placeholder="选择列车" clearable style="width: 200px;">
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

      <!-- 车次列表 - 仅在有有效查询时显示 -->
      <div v-if="showTable">
        <el-table :data="scheduleList" border stripe>
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="trainName" label="车次编号" width="120" align="center" />
          <el-table-column prop="departureTime" label="发车时间" width="180" align="center" />
          <el-table-column label="运行方向" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="row.direction === 0 ? 'success' : 'warning'">
                {{ row.direction === 0 ? '顺行' : '逆行' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right" align="center">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="deleteScheduleHandler(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div v-else class="empty-placeholder">
        <el-empty description="请选择列车并点击查询" :image-size="120" />
      </div>
    </el-card>

    <!-- 创建车次对话框 -->
    <el-dialog v-model="dialogVisible" title="新建车次" width="500px">
      <el-form :model="scheduleForm" label-width="100px">
        <el-form-item label="选择列车" required>
          <el-select v-model="scheduleForm.trainId" placeholder="选择列车" style="width: 100%;">
            <el-option
                v-for="train in trainList"
                :key="train.trainId"
                :label="`${train.trainNumber} (路线${train.routerId})`"
                :value="train.trainId"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="发车时间" required>
          <el-date-picker
              v-model="scheduleForm.departureTime"
              type="datetime"
              placeholder="选择发车时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="width: 100%;"
          />
        </el-form-item>

        <el-form-item label="运行方向" required>
          <el-radio-group v-model="scheduleForm.direction">
            <el-radio :label="0">顺行(正序)</el-radio>
            <el-radio :label="1">逆行(逆序)</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-alert
            v-if="selectedTrainInfo"
            title="列车信息"
            type="info"
            :closable="false"
            show-icon
        >
          <div>车次编号: {{ selectedTrainInfo.trainNumber }}</div>
          <div>路线ID: {{ selectedTrainInfo.routerId }}</div>
          <div>总耗时: {{ selectedTrainInfo.timeConsuming }} 分钟</div>
        </el-alert>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSchedule">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSchedules, createSchedule, deleteSchedule } from '@/api/DepartureApi.js'
import { getTrainList } from '@/api/TrainApi.js'

const scheduleList = ref([])
const trainList = ref([])
const dialogVisible = ref(false)
const showTable = ref(false) // 控制表格显示/隐藏

const searchForm = ref({
  trainId: null
})

const scheduleForm = ref({
  trainId: null,
  departureTime: '',
  direction: 0
})

const selectedTrainInfo = computed(() => {
  if (!scheduleForm.value.trainId) return null
  return trainList.value.find(t => t.trainId === scheduleForm.value.trainId)
})

// 监听列车选择变化，当未选择列车时隐藏表格并清空数据
watch(() => searchForm.value.trainId, (newVal) => {
  if (!newVal) {
    showTable.value = false
    scheduleList.value = []
  } else {
    // 切换列车时，隐藏之前的表格数据，需要重新点击查询
    showTable.value = false
    scheduleList.value = []
  }
})

// 加载列车列表
const loadTrainList = async () => {
  try {
    const res = await getTrainList({ pageNum: 1, find: '' })
    if (res.code === 200) {
      trainList.value = res.data?.records || []
    }
  } catch (error) {
    console.error('加载列车列表失败:', error)
  }
}

// 加载车次列表
const loadSchedules = async () => {
  // 未选择列车时，不发送请求，隐藏表格并提示
  if (!searchForm.value.trainId) {
    ElMessage.warning('请选择列车')
    showTable.value = false
    scheduleList.value = []
    return
  }

  try {
    const res = await getSchedules(searchForm.value.trainId)
    if (res.code === 200) {
      scheduleList.value = res.data || []
      showTable.value = true // 查询成功，显示表格
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
  scheduleForm.value = {
    trainId: null,
    departureTime: '',
    direction: 0
  }
  dialogVisible.value = true
}

// 创建车次
const handleCreateSchedule = async () => {
  if (!scheduleForm.value.trainId || !scheduleForm.value.departureTime) {
    ElMessage.warning('请填写完整信息')
    return
  }

  try {
    const data = {
      trainId: scheduleForm.value.trainId,
      departureTime: scheduleForm.value.departureTime,
      direction: scheduleForm.value.direction
    }

    const res = await createSchedule(data)
    if (res.code === 200) {
      ElMessage.success('创建成功')
      dialogVisible.value = false
      // 如果当前正在查看该车次的列表，刷新列表
      if (searchForm.value.trainId === scheduleForm.value.trainId) {
        await loadSchedules()
      }
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    console.error('创建失败:', error)
    ElMessage.error('创建失败')
  }
}

// 删除车次
const deleteScheduleHandler = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该车次吗?', '提示', {
      type: 'warning'
    })

    const res = await deleteSchedule(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      // 删除后重新加载列表，保持表格显示状态
      await loadSchedules()
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

onMounted(() => {
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
</style>
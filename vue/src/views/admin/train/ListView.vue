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
      <el-table :data="tableData" stripe border style="width: 100%;">
        <el-table-column prop="trainId" label="ID" align="center" />
        <el-table-column prop="trainNumber" label="列车号" align="center" />
        <el-table-column prop="routerId" label="路线ID" align="center" />
        <el-table-column label="路线名称" align="center">
          <template #default="scope">
            {{ getRouterName(scope.row.routerId) }}
          </template>
        </el-table-column>
        <el-table-column prop="timeConsuming" label="总耗时(分钟)" align="center" />
        <el-table-column label="操作" fixed="right" align="center">
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
    </div>

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

    <!-- 编辑/新增对话框 -->
    <el-dialog :title="form.trainId ? '编辑列车' : '新增列车'" v-model="dialogVisible" width="600px">
      <el-form label-width="100px" :model="form" :rules="rules" ref="formRef">
        <el-form-item label="车次号" prop="trainNumber">
          <el-input v-model="form.trainNumber" placeholder="如：G1001" />
        </el-form-item>
        <el-form-item label="所属路线" prop="routerId">
          <el-select v-model="form.routerId" placeholder="选择路线" style="width: 100%" clearable>
            <el-option
                v-for="route in routeList"
                :key="route.routerId"
                :label="`${route.routerId}-${route.routerName}`"
                :value="route.routerId"
            />
          </el-select>
        </el-form-item>
        <!-- 总耗时不再允许编辑，仅作展示（可选） -->
        <el-form-item label="总耗时" v-if="form.routerId">
          <el-input :value="getSelectedRouteDuration()" disabled placeholder="选择路线后自动计算" />
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
import {ref, onMounted} from 'vue'
import {ElMessage} from 'element-plus'
import {Search, Refresh, Plus} from '@element-plus/icons-vue'
import * as TrainApi from '@/api/TrainApi.js'
import {getRouteList} from '@/api/RouteApi.js'

const searchForm = ref({
  find: '',
  pageNum: 1,
  pageSize: 10
})

let total = ref(0)
const tableData = ref([])
const routeList = ref([])      // 存储所有路线 { routerId, routerName, totalDuration }
let dialogVisible = ref(false)
let form = ref({
  trainNumber: '',
  routerId: null
  // 注意：不包含 timeConsuming，后端会根据 routerId 自动计算
})
let formRef = ref(null)

// 表单验证规则
const rules = {
  trainNumber: [{required: true, message: '请输入车次号', trigger: 'blur'}],
  routerId: [{required: true, message: '请选择路线', trigger: 'change'}]
}

// 根据路线ID获取路线名称
const getRouterName = (routerId) => {
  if (!routerId) return '-'
  const route = routeList.value.find(r => r.routerId === routerId)
  return route ? route.routerName : `路线${routerId}`
}

// 获取当前选中路线的总耗时（用于对话框展示）
const getSelectedRouteDuration = () => {
  if (!form.value.routerId) return '请先选择路线'
  const route = routeList.value.find(r => r.routerId === form.value.routerId)
  if (route && route.totalDuration) {
    return `${route.totalDuration} 分钟`
  }
  return '该路线尚未配置耗时'
}

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

// 加载车次数据
const loadData = () => {
  TrainApi.getTrainPage(searchForm.value).then((resp) => {
    if (resp.code === 200 && resp.data) {
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
    routerId: null
  }
}

// 编辑
const handleEdit = (row) => {
  dialogVisible.value = true
  // 只回填车次号和路线ID，总耗时由后端维护
  form.value = {
    trainId: row.trainId,
    trainNumber: row.trainNumber,
    routerId: row.routerId
  }
}

// 保存（新增或更新）
const handleSave = () => {
  formRef.value.validate().then(() => {
    const apiCall = form.value.trainId ? TrainApi.updateTrain : TrainApi.addTrain
    // 注意：不传递 timeConsuming，后端会根据 routerId 自动计算
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
</style>
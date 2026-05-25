<template>
  <div class="ticket-management">
    <!-- 搜索表单 -->
    <el-form :model="searchForm" class="search-form">
      <el-row :gutter="10">
        <el-col :span="6">
          <el-form-item label="车次ID">
            <el-input v-model="searchForm.find" placeholder="请输入车次ID" clearable />
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
      新增车票
    </el-button>

    <!-- 数据表格 -->
    <el-table :data="tableData" stripe border style="width: 100%">
      <el-table-column prop="ticketId" label="ID" width="80" />
      <el-table-column prop="trainId" label="车次ID" width="100" />
      <el-table-column prop="carriageNumber" label="车厢号" width="100" />
      <el-table-column prop="seatNumber" label="座位号" width="100" />
      <el-table-column prop="seatType" label="座位类型" width="120" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="scope">
          ¥{{ scope.row.price }}
        </template>
      </el-table-column>
      <el-table-column prop="ticketStatus" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.ticketStatus === 'available' ? 'success' : 'info'">
            {{ scope.row.ticketStatus === 'available' ? '可售' : '已售' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-popconfirm title="确定要删除吗？" @confirm="handleDelete(scope.row.ticketId)">
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
    <el-dialog :title="form.ticketId ? '编辑车票' : '新增车票'" v-model="dialogVisible" width="600px">
      <el-form label-width="100px" :model="form" :rules="rules" ref="formRef">
        <el-form-item label="车次ID" prop="trainId">
          <el-input-number v-model="form.trainId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="车厢号" prop="carriageNumber">
          <el-input v-model="form.carriageNumber" placeholder="如：01" />
        </el-form-item>
        <el-form-item label="座位号" prop="seatNumber">
          <el-input v-model="form.seatNumber" placeholder="如：01A" />
        </el-form-item>
        <el-form-item label="座位类型" prop="seatType">
          <el-select v-model="form.seatType" placeholder="请选择座位类型" style="width: 100%">
            <el-option label="商务座" value="business" />
            <el-option label="一等座" value="first" />
            <el-option label="二等座" value="second" />
            <el-option label="硬座" value="hard" />
            <el-option label="软座" value="soft" />
            <el-option label="硬卧" value="hard_sleeper" />
            <el-option label="软卧" value="soft_sleeper" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="ticketStatus">
          <el-select v-model="form.ticketStatus" placeholder="请选择状态" style="width: 100%">
            <el-option label="可售" value="available" />
            <el-option label="已售" value="sold" />
          </el-select>
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
import * as TicketApi from '@/api/TicketApi.js'

const searchForm = ref({
  find: '',
  pageNum: 1,
  pageSize: 10
})

let total = ref(0)
const tableData = ref([])
let dialogVisible = ref(false)
let form = ref({
  trainId: 1,
  carriageNumber: '',
  seatNumber: '',
  seatType: 'second',
  price: 0,
  ticketStatus: 'available'
})
let formRef = ref(null)

const rules = {
  trainId: [{ required: true, message: '请输入车次ID', trigger: 'blur' }],
  carriageNumber: [{ required: true, message: '请输入车厢号', trigger: 'blur' }],
  seatNumber: [{ required: true, message: '请输入座位号', trigger: 'blur' }],
  seatType: [{ required: true, message: '请选择座位类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

// 加载数据
const loadData = () => {
  TicketApi.getTicketPage(searchForm.value).then((resp) => {
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
    trainId: 1,
    carriageNumber: '',
    seatNumber: '',
    seatType: 'second',
    price: 0,
    ticketStatus: 'available'
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
    const apiCall = form.value.ticketId ? TicketApi.updateTicket : TicketApi.addTicket
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
  TicketApi.deleteTicket(id).then(() => {
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

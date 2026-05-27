<template>
  <div class="user-management">
    <div class="search-bar">
      <el-form :model="searchForm" class="search-form">
        <el-row :gutter="10">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="搜索">
              <el-input v-model="searchForm.find" placeholder="请输入用户名或姓名" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item>
              <el-button type="primary" @click="findUser">
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
      
      <div class="action-buttons">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>
    </div>

    <div class="table-container">
    <el-table :data="tableData" stripe border style="width: 100%;">
      <el-table-column prop="userId" label="ID" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="真实姓名" />
      <el-table-column prop="idCard" label="身份证号" show-overflow-tooltip />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="role" label="角色">
        <template #default="scope">
          <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'primary'">
            {{ scope.row.role === 'admin' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" show-overflow-tooltip>
        <template #default="scope">
          {{ formatDateTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" v-if="isAdmin">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-popconfirm title="确定要删除吗？" @confirm="handleDelete(scope.row.userId)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </div>


  <el-pagination
      class="mgt-4"
      v-model:current-page="searchForm.pageNum"
      v-model:page-size="searchForm.pageSize"
      :page-sizes="[1, 2, 10, 200]"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      @size-change="findUser"
      @current-change="findUser"
  />

  <!--对话框-->
  <el-dialog :title="userForm.userId?'编辑用户':'新增用户'" v-model="dialogVisible">
    <el-form label-width="100px" :model="userForm" :rules="userFormRules" ref="userFormRef">
      <el-form-item label="用户名" prop="username">
        <el-input placeholder="请输入用户名" v-model="userForm.username" :readonly="!!userForm.userId"/>
      </el-form-item>
      <el-form-item label="真实姓名" prop="realName">
        <el-input placeholder="请输入真实姓名" v-model="userForm.realName"/>
      </el-form-item>
      <el-form-item label="身份证号" prop="idCard">
        <el-input placeholder="请输入身份证号" v-model="userForm.idCard"/>
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input placeholder="请输入手机号" v-model="userForm.phone"/>
      </el-form-item>
      <el-form-item label="角色" prop="role">
        <el-select v-model="userForm.role" placeholder="请选择角色">
          <el-option label="管理员" value="admin" />
          <el-option label="普通用户" value="user" />
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

import {ref, computed} from "vue";
import * as userAPI from "@/api/UserApi.js";
import {ElMessage} from "element-plus";
import { Search, Refresh, Plus } from '@element-plus/icons-vue';
import authService from "@/service/AuthService.js";

const searchForm = ref({
  find: '',
  pageNum: 1,
  pageSize: 10,
});

let total = ref(0);
const tableData = ref([]);

let dialogVisible = ref(false);
let userForm = ref({
  username: '',
  realName: '',
  idCard: '',
  phone: '',
  role: 'user'
});
let userFormRef = ref(null);
let userFormRules = ref({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
})

// 判断当前用户是否为管理员
const isAdmin = computed(() => {
  const user = authService.getUser();
  return user && user.role === 'admin';
});

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return dateTime.replace('T', ' ').substring(0, 19)
}

//**************************************方法开始****************************************

const handleReset=()=>{
  searchForm.value = {
    find: '',
    pageNum: 1,
    pageSize: 10
  };
  findUser();
};

const findUser=()=>{
  userAPI.getUserPage(searchForm.value).then((resp)=>{
    if (resp.code === 200 && resp.data) {
      tableData.value = resp.data.records || [];
      total.value = resp.data.total || 0;
    }
  })
}

const handleAdd=()=>{
  dialogVisible.value = true;
  userForm.value = {
    username: '',
    realName: '',
    idCard: '',
    phone: '',
    role: 'user'
  };
}

const handleEdit=(row)=>{
  dialogVisible.value = true;
  userForm.value = JSON.parse(JSON.stringify(row));
}

const handleSave=()=>{
  userFormRef.value.validate().then(()=>{
    if(userForm.value.userId){
      // 管理员更新用户
      userAPI.updateUser(userForm.value.userId, userForm.value).then((resp)=>{
        ElMessage.success('修改成功');
        findUser();
        dialogVisible.value = false;
      })
    }else{
      ElMessage.warning('请使用注册功能添加用户');
    }
  })
}

const handleDelete=(id)=>{
  userAPI.deleteUser(id).then((resp)=>{
    if (resp.code === 200) {
      ElMessage.success(resp.message || '删除成功');
      findUser();
    }
  })
}

findUser();
//**************************************方法结束****************************************
</script>


<style scoped>
.user-management {
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

.el-pagination {
  padding: 16px;
  text-align: right;
}
</style>
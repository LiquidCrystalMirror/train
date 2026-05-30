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
        <el-table-column prop="userId" label="ID" align="center" width="80" />
        <el-table-column prop="username" label="用户名" align="center" />
        <el-table-column prop="realName" label="真实姓名" align="center" />
        <el-table-column prop="idCard" label="身份证号" show-overflow-tooltip align="center" />
        <el-table-column prop="phone" label="手机号" align="center" />
        <el-table-column prop="role" label="角色" align="center" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'primary'">
              {{ scope.row.role === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" show-overflow-tooltip align="center" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" v-if="isAdmin" align="center" width="150" fixed="right">
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

    <!-- 分页 -->
    <el-pagination
        class="mgt-4"
        v-model:current-page="searchForm.pageNum"
        v-model:page-size="searchForm.pageSize"
        layout="total, prev, pager, next, jumper"
        :total="total"
        @current-change="findUser"
    />

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" @close="resetForm">
      <el-form label-width="100px" :model="userForm" :rules="userFormRules" ref="userFormRef">
        <el-form-item label="用户名" prop="username">
          <el-input placeholder="请输入用户名" v-model="userForm.username" :readonly="isEdit" />
        </el-form-item>

        <!-- 新增时显示密码字段 -->
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input
              placeholder="请输入密码"
              v-model="userForm.password"
              type="password"
              show-password
          />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input placeholder="请输入真实姓名" v-model="userForm.realName" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input placeholder="请输入身份证号" v-model="userForm.idCard" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input placeholder="请输入手机号" v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="用户类型" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择用户类型">
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import * as userAPI from "@/api/UserApi.js";
import { registerApi } from "@/api/RegisterApi.js";
import { ElMessage } from "element-plus";
import { Search, Refresh, Plus } from '@element-plus/icons-vue';
import authService from "@/service/AuthService.js";

const searchForm = ref({
  find: '',
  pageNum: 1,
  pageSize: 10,
});

let total = ref(0);
const tableData = ref([]);
const saving = ref(false);

let dialogVisible = ref(false);
let dialogTitle = ref('新增用户');
let isEdit = ref(false);
let userForm = ref({
  username: '',
  password: '',
  realName: '',
  idCard: '',
  phone: '',
  role: 'user'
});
let userFormRef = ref(null);

// 身份证号正则
const idCardPattern = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
// 手机号正则
const phonePattern = /^1[3-9]\d{9}$/

let userFormRules = ref({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: idCardPattern, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: phonePattern, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ]
});

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

// 重置表单
const resetForm = () => {
  userForm.value = {
    username: '',
    password: '',
    realName: '',
    idCard: '',
    phone: '',
    role: 'user'
  };
  isEdit.value = false;
  dialogTitle.value = '新增用户';
}

// 查询用户
const findUser = () => {
  userAPI.getUserPage(searchForm.value).then((resp) => {
    if (resp.code === 200 && resp.data) {
      tableData.value = resp.data.records || [];
      total.value = resp.data.total || 0;
    }
  })
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    find: '',
    pageNum: 1,
    pageSize: 10
  };
  findUser();
};

// 新增用户
const handleAdd = () => {
  resetForm();
  dialogVisible.value = true;
}

// 编辑用户
const handleEdit = (row) => {
  isEdit.value = true;
  dialogTitle.value = '编辑用户';
  dialogVisible.value = true;
  // 编辑时不包含密码字段
  const { password, ...rest } = row;
  userForm.value = JSON.parse(JSON.stringify(rest));
}

// 保存用户
const handleSave = () => {
  userFormRef.value.validate().then(async () => {
    saving.value = true;
    try {
      if (isEdit.value) {
        // 编辑用户（不修改密码）
        const updateData = {
          realName: userForm.value.realName,
          idCard: userForm.value.idCard,
          phone: userForm.value.phone,
          role: userForm.value.role
        };
        const resp = await userAPI.updateUser(userForm.value.userId, updateData);
        if (resp.code === 200) {
          ElMessage.success('修改成功');
          dialogVisible.value = false;
          findUser();
        } else {
          ElMessage.error(resp.message || '修改失败');
        }
      } else {
        // 新增用户 - 调用注册接口
        const registerData = {
          username: userForm.value.username,
          password: userForm.value.password,
          realName: userForm.value.realName,
          idCard: userForm.value.idCard,
          phone: userForm.value.phone,
          role: userForm.value.role
        };
        const resp = await registerApi(registerData);
        if (resp.code === 200) {
          ElMessage.success('新增用户成功');
          dialogVisible.value = false;
          findUser();
        } else {
          ElMessage.error(resp.message || '新增用户失败');
        }
      }
    } catch (error) {
      console.error('保存失败:', error);
      ElMessage.error('保存失败');
    } finally {
      saving.value = false;
    }
  }).catch(() => {
    saving.value = false;
  });
}

// 删除用户
const handleDelete = (id) => {
  userAPI.deleteUser(id).then((resp) => {
    if (resp.code === 200) {
      ElMessage.success(resp.message || '删除成功');
      findUser();
    } else {
      ElMessage.error(resp.message || '删除失败');
    }
  }).catch(err => {
    console.error('删除失败:', err);
    ElMessage.error('删除失败');
  });
}

// 初始化加载数据
findUser();
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
  overflow-x: auto;
}

.el-pagination {
  padding: 16px;
  text-align: right;
}

.mgt-4 {
  margin-top: 16px;
}
</style>
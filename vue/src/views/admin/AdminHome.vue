<template>
  <div class="dashboard-home">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="40"><List /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.trainCount }}</div>
              <div class="stat-label">车次总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="40"><Ticket /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.ticketCount }}</div>
              <div class="stat-label">车票总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="40"><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.orderCount }}</div>
              <div class="stat-label">订单总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 欢迎信息 -->
    <el-card class="welcome-card">
      <template #header>
        <h3>欢迎使用火车售票系统</h3>
      </template>
      <div class="welcome-content">
        <p>您好，{{ userName }}！</p>
        <p>当前角色：<el-tag :type="userRole === 'admin' ? 'danger' : 'primary'">{{ userRoleText }}</el-tag></p>
        <p>登录时间：{{ loginTime }}</p>
      </div>
    </el-card>

    <!-- 快捷操作 -->
    <el-card class="quick-actions-card">
      <template #header>
        <h3>快捷操作</h3>
      </template>
      <el-row :gutter="20">
        <el-col :span="6" v-if="isAdmin">
          <el-button type="primary" style="width: 100%; height: 80px" @click="goToPage('/admin/train')">
            <el-icon :size="30"><List /></el-icon>
            <div>车次管理</div>
          </el-button>
        </el-col>
        <el-col :span="6" v-if="isAdmin">
          <el-button type="success" style="width: 100%; height: 80px" @click="goToPage('/admin/ticket')">
            <el-icon :size="30"><Ticket /></el-icon>
            <div>车票管理</div>
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="warning" style="width: 100%; height: 80px" @click="goToPage('/admin/sale')">
            <el-icon :size="30"><ShoppingCart /></el-icon>
            <div>售票</div>
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="info" style="width: 100%; height: 80px" @click="goToPage('/admin/orders')">
            <el-icon :size="30"><Document /></el-icon>
            <div>订单查询</div>
          </el-button>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, List, Ticket, ShoppingCart, Document } from '@element-plus/icons-vue'
import authService from '@/service/AuthService.js'

const router = useRouter()

// 统计数据（实际应该从后端获取）
const stats = ref({
  userCount: 0,
  trainCount: 0,
  ticketCount: 0,
  orderCount: 0
})

// 用户信息
const currentUser = computed(() => authService.getUser())
const userName = computed(() => {
  const user = currentUser.value
  return user ? (user.realName || user.username || '用户') : '用户'
})
const userRole = computed(() => {
  const user = currentUser.value
  return user ? user.role : 'user'
})
const userRoleText = computed(() => {
  return userRole.value === 'admin' ? '管理员' : '普通用户'
})
const isAdmin = computed(() => userRole.value === 'admin')

// 登录时间
const loginTime = ref(new Date().toLocaleString('zh-CN'))

// 页面跳转
const goToPage = (path) => {
  router.push(path)
}

onMounted(() => {
  // TODO: 从后端获取统计数据
  // 这里只是示例数据
  stats.value = {
    userCount: 0,
    trainCount: 0,
    ticketCount: 0,
    orderCount: 0
  }
})
</script>

<style scoped>
.dashboard-home {
  padding: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.welcome-card {
  margin-bottom: 20px;
}

.welcome-content p {
  margin: 10px 0;
  font-size: 16px;
  color: #666;
}

.quick-actions-card {
  margin-bottom: 20px;
}

.el-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
</style>

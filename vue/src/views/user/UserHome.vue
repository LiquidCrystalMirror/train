<template>
  <div class="user-home">
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <h2>欢迎使用火车票预订系统</h2>
        </div>
      </template>
      <div class="welcome-content">
        <p>您好，{{ userName }}！</p>
        <p>您可以在这里查询和预订火车票。</p>
      </div>
    </el-card>

    <el-row :gutter="20" class="feature-cards">
      <el-col :span="8">
        <el-card class="feature-card" shadow="hover" @click="$router.push('/user/train-search')">
          <el-icon :size="50" color="#409EFF"><Search /></el-icon>
          <h3>车票查询</h3>
          <p>查询可用列车和余票信息</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="feature-card" shadow="hover" @click="$router.push('/user/my-orders')">
          <el-icon :size="50" color="#67C23A"><Tickets /></el-icon>
          <h3>我的订单</h3>
          <p>查看和管理您的订单</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="feature-card" shadow="hover" @click="$router.push('/user/refund')">
          <el-icon :size="50" color="#F56C6C"><Close /></el-icon>
          <h3>退票服务</h3>
          <p>申请退票和查看退票记录</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import authService from "@/service/AuthService.js";
import { Search, Tickets, Close } from '@element-plus/icons-vue';

const user = computed(() => authService.getUser())
const userName = computed(() => {
  const userData = user.value
  return userData ? (userData.realName || userData.username || '用户') : '用户'
})
</script>

<style scoped>
.user-home {
  padding: 20px;
}

.welcome-card {
  margin-bottom: 30px;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.welcome-content {
  padding: 20px 0;
}

.welcome-content p {
  margin: 10px 0;
  font-size: 16px;
  color: #606266;
}

.feature-cards {
  margin-top: 20px;
}

.feature-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.feature-card h3 {
  margin: 15px 0 10px 0;
  font-size: 18px;
  color: #303133;
}

.feature-card p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}
</style>

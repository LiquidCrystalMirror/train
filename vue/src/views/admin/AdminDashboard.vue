<template>
  <el-container class="layout-container" style="height: 100%">
    <el-header height="40px" class="custom-header">
      <div class="header-left">
        <el-icon class="collapse-icon" @click="toggleMenu">
          <Fold v-if="isCollapse" />
          <Expand v-else />
        </el-icon>
        <span class="system-title">管理系统</span>
      </div>

      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="30" icon="UserFilled" style="margin-right: 8px; background-color: #ffd04b;" />
            <span>欢迎，{{ userName }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-container class="main-container">
      <el-aside :style="{ width: isCollapse ? '64px' : '200px', transition: 'width 0.3s' }" class="custom-aside">
        <div class="sidebar-toggle-btn" :class="{ 'is-collapsed': isCollapse }" @click="toggleMenu">
          <el-icon :size="20">
            <Operation />
          </el-icon>
          <span v-if="!isCollapse" class="toggle-text">收起菜单</span>
          <span v-else class="toggle-text tooltip-text">展开</span>
        </div>

        <el-menu
            :collapse="isCollapse"
            active-text-color="#409EFF"
            background-color="#304156"
            :default-active="activeMenu"
            text-color="#bfcbd9"
            :router="true"
            style="border: none;"
            :collapse-transition="false">
          <el-menu-item index="/admin/home">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          
          <el-sub-menu index="1">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/admin/user">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="2">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>基础数据</span>
            </template>
            <el-menu-item index="/admin/station">
              <el-icon><Location /></el-icon>
              <span>站点管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/train">
              <el-icon><List /></el-icon>
              <span>车次列表</span>
            </el-menu-item>
            <el-menu-item index="/admin/train-search">
              <el-icon><Search /></el-icon>
              <span>车次查询</span>
            </el-menu-item>
            <el-menu-item index="/admin/ticket">
              <el-icon><Ticket /></el-icon>
              <span>车票管理</span>
            </el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="3">
            <template #title>
              <el-icon><ShoppingCart /></el-icon>
              <span>售票管理</span>
            </template>
            <el-menu-item index="/admin/sale">
              <el-icon><ShoppingCart /></el-icon>
              <span>售票</span>
            </el-menu-item>
            <el-menu-item index="/admin/refund">
              <el-icon><Close /></el-icon>
              <span>退票</span>
            </el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="4">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>订单查询</span>
            </template>
            <el-menu-item index="/admin/orders">
              <el-icon><Document /></el-icon>
              <span>订单记录</span>
            </el-menu-item>
            <el-menu-item index="/admin/my-orders">
              <el-icon><Tickets /></el-icon>
              <span>我的订单</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <el-main class="main-content">
        <router-view></router-view>
      </el-main>
    </el-container>
  </el-container>
</template>


<script setup>
import {computed, onMounted, ref} from "vue";
import { useRoute } from 'vue-router';
import { ElMessage } from "element-plus";
import router from "@/router/index.js";
import authService from "@/service/AuthService.js";
import {
  Fold,
  Expand,
  Operation,
  Setting,
  User,
  ArrowDown,
  SwitchButton,
  Document,
  List,
  Ticket,
  ShoppingCart,
  Close,
  HomeFilled,
  Search,
  Tickets,
  Location
} from '@element-plus/icons-vue';

const route = useRoute();

const isCollapse = ref(false)

const activeMenu = computed(() => {
  return route.path
})

const user = computed(() => authService.getUser())
const userName = computed(() => {
  const userData = user.value
  return userData ? (userData.realName || userData.username || '用户') : '用户'
})

onMounted(() => {
  if (!authService.checkAuth()) {
    return
  }
})


const toggleMenu = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('user')
    localStorage.removeItem('token')
    sessionStorage.clear()

    ElMessage.success('退出登录成功')

    router.replace('/login')
  }
}

</script>




<style scoped>
.layout-container {
  height: 100vh;
}

.main-container {
  height: calc(100vh - 40px);
}

.custom-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  color: #ffffff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-icon {
  font-size: 22px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: rgba(255, 255, 255, 0.9);
  padding: 6px;
  border-radius: 6px;
}

.collapse-icon:hover {
  color: #ffffff;
  background-color: rgba(255, 255, 255, 0.15);
  transform: scale(1.1);
}

.system-title {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  color: #ffffff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.95);
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background-color: rgba(255, 255, 255, 0.1);
}

.user-info:hover {
  background-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.custom-aside {
  background-color: #304156;
  overflow-x: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
}

.sidebar-toggle-btn {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  margin: 20px 16px 24px 16px;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.08) 0%, rgba(255, 255, 255, 0.02) 100%);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #bfcbd9;
  border: 1px solid rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(4px);
}

.sidebar-toggle-btn:hover {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.15) 0%, rgba(255, 255, 255, 0.05) 100%);
  color: #ffffff;
  transform: translateY(-2px);
  border-color: rgba(255, 255, 255, 0.15);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.sidebar-toggle-btn:active {
  transform: translateY(0px);
}

.sidebar-toggle-btn.is-collapsed {
  justify-content: center;
  padding: 12px 0;
  margin: 20px 12px 24px 12px;
}

.sidebar-toggle-btn .toggle-text {
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
  letter-spacing: 0.5px;
}

.sidebar-toggle-btn.is-collapsed {
  position: relative;
}

.sidebar-toggle-btn.is-collapsed .tooltip-text {
  display: none;
  position: absolute;
  left: 100%;
  top: 50%;
  transform: translateY(-50%);
  margin-left: 12px;
  background: rgba(0, 0, 0, 0.85);
  color: #fff;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  white-space: nowrap;
  z-index: 1000;
  backdrop-filter: blur(8px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.sidebar-toggle-btn.is-collapsed:hover .tooltip-text {
  display: block;
}

.sidebar-toggle-btn .el-icon {
  transition: transform 0.3s ease;
}

.sidebar-toggle-btn:hover .el-icon {
  transform: scale(1.1);
}

.el-menu {
  flex: 1;
  border-right: none !important;
}

.el-menu--collapse {
  width: 100%;
}

.main-content {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f2 100%);
  padding: 20px;
  overflow-y: auto;
}

.main-content::-webkit-scrollbar {
  width: 8px;
}

.main-content::-webkit-scrollbar-track {
  background: #e4e9f2;
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
}

.main-content::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}
</style>
import { createWebHashHistory, createRouter } from 'vue-router'
import authService from "@/service/AuthService.js";
import { ElMessage } from 'element-plus'

let routes=[
  {
    path:'/',
    redirect:'/login'
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
  },
  {
    path:'/register',
    name:'register',
    component:()=>import('@/views/Register.vue')
  },
  {
    path:'/admin',
    name:'admin',
    component:()=>import('@/views/admin/AdminDashboard.vue'),
    meta: { requiresAuth: true, role: 'admin' },
    redirect: '/admin/home',
    children:[
      {
        path:'/admin/home',
        name:'adminHome',
        component:()=>import('@/views/admin/AdminHome.vue'),
        meta: { requiresAuth: true, role: 'admin' }
      },
      {
        path:'/admin/user',
        name:'adminUser',
        component:()=>import('@/views/admin/user/ListView.vue'),
        meta: { requiresAuth: true, role: 'admin' }
      },
      {
        path:'/admin/station',
        name:'adminStation',
        component:()=>import('@/views/admin/station/ListView.vue'),
        meta: { requiresAuth: true, role: 'admin' }
      },
      {
        path:'/admin/station/detail',
        name:'adminStationDetail',
        component:()=>import('@/views/admin/station/StationView.vue'),
        meta: { requiresAuth: true, role: 'admin' }
      },
      {
        path:'/admin/train',
        name:'adminTrain',
        component:()=>import('@/views/admin/train/ListView.vue'),
        meta: { requiresAuth: true, role: 'admin' }
      },
      {
        path:'/admin/train-search',
        name:'adminTrainSearch',
        component:()=>import('@/views/admin/train/SearchView.vue'),
        meta: { requiresAuth: true, role: 'admin' }
      },
      {
        path:'/admin/ticket',
        name:'adminTicket',
        component:()=>import('@/views/admin/ticket/ListView.vue'),
        meta: { requiresAuth: true, role: 'admin' }
      },
      {
        path:'/admin/sale',
        name:'adminSale',
        component:()=>import('@/views/admin/sale/SellView.vue'),
        meta: { requiresAuth: true, role: 'admin' }
      },
      {
        path:'/admin/refund',
        name:'adminRefund',
        component:()=>import('@/views/admin/refund/RefundView.vue'),
        meta: { requiresAuth: true, role: 'admin' }
      },
      {
        path:'/admin/orders',
        name:'adminOrders',
        component:()=>import('@/views/admin/orders/ListView.vue'),
        meta: { requiresAuth: true, role: 'admin' }
      },
      {
        path:'/admin/my-orders',
        name:'adminMyOrders',
        component:()=>import('@/views/admin/orders/MyOrdersView.vue'),
        meta: { requiresAuth: true, role: 'admin' }
      }
    ]
  },
  {
    path:'/user',
    name:'user',
    component:()=>import('@/views/user/UserDashboard.vue'),
    meta: { requiresAuth: true, role: 'user' },
    redirect: '/user/home',
    children:[
      {
        path:'/user/home',
        name:'userHome',
        component:()=>import('@/views/user/UserHome.vue'),
        meta: { requiresAuth: true, role: 'user' }
      },
      {
        path:'/user/train-search',
        name:'userTrainSearch',
        component:()=>import('@/views/user/train/SearchView.vue'),
        meta: { requiresAuth: true, role: 'user' }
      },
      {
        path:'/user/my-orders',
        name:'userMyOrders',
        component:()=>import('@/views/user/orders/MyOrdersView.vue'),
        meta: { requiresAuth: true, role: 'user' }
      },
      {
        path:'/user/refund',
        name:'userRefund',
        component:()=>import('@/views/user/refund/RefundView.vue'),
        meta: { requiresAuth: true, role: 'user' }
      }
    ]
  }
]
const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach((to, from) => {
  console.log('路由守卫触发:', {
    from: from.path,
    to: to.path,
    isLoggedIn: authService.isLoggedIn()
  })

  const whiteList = ['/login', '/register']

  if (whiteList.includes(to.path)) {
    if (to.path === '/login' && authService.isLoggedIn()) {
      const user = authService.getUser()
      console.log('已登录用户信息:', user)
      if (!user || !user.role) {
        console.warn('用户信息不完整，清除登录状态')
        authService.clearAuth()
        return true
      }
      if (user.role === 'admin') {
        return '/admin'
      } else {
        return '/user'
      }
    }
    return true
  }

  if (!authService.isLoggedIn()) {
    console.log('未登录，重定向到登录页')
    authService.redirectToLogin()
    return false
  }

  const user = authService.getUser()
  console.log('当前用户角色:', user?.role, '路由要求角色:', to.meta.role)

  if (!user || !user.role) {
    console.warn('用户信息不完整，清除登录状态')
    authService.clearAuth()
    return '/login'
  }

  if (to.meta.role) {
    if (user.role !== to.meta.role) {
      console.warn('角色不匹配，重定向到对应首页')
      ElMessage.warning('您没有权限访问该页面')
      if (user.role === 'admin') {
        return '/admin'
      } else {
        return '/user'
      }
    }
  }

  return true
})

export default router
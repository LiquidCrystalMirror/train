// 认证服务 - 管理用户登录状态和token
import router from '@/router'

const TOKEN_KEY = 'token'
const USER_KEY = 'user'

class AuthService {
  // 保存登录信息
  saveAuth(token, user) {
    localStorage.setItem(TOKEN_KEY, token)
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  }

  // 获取token
  getToken() {
    return localStorage.getItem(TOKEN_KEY)
  }

  // 获取用户信息
  getUser() {
    const userStr = localStorage.getItem(USER_KEY)
    return userStr ? JSON.parse(userStr) : null
  }

  // 检查是否已登录
  isLoggedIn() {
    return !!this.getToken()
  }

  // 检查token是否有效
  isTokenValid() {
    const token = this.getToken()
    if (!token) return false
    
    // 可以在这里添加token过期检查逻辑
    return true
  }

  // 检查认证状态
  checkAuth() {
    if (!this.isLoggedIn()) {
      this.redirectToLogin()
      return false
    }
    return true
  }

  // 跳转到登录页
  redirectToLogin() {
    this.clearAuth()
    router.replace('/login')
  }

  // 清除认证信息
  clearAuth() {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  // 退出登录
  logout() {
    this.clearAuth()
    router.replace('/login')
  }
}

export default new AuthService()

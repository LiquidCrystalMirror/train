<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>用户注册</h2>
        </div>
      </template>

      <!-- 注册表单 -->
      <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              clearable
          />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input
              v-model="registerForm.realName"
              placeholder="请输入真实姓名"
              prefix-icon="UserFilled"
              clearable
          />
        </el-form-item>

        <el-form-item label="身份证号" prop="idCard">
          <el-input
              v-model="registerForm.idCard"
              placeholder="请输入身份证号"
              prefix-icon="Postcard"
              clearable
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              prefix-icon="Phone"
              clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
              clearable
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              prefix-icon="Lock"
              show-password
              clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button
              type="primary"
              :loading="loading"
              class="register-btn"
              @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 底部按钮区域 -->
      <div class="footer-buttons">
        <el-divider>
          <span class="divider-text">已有账号？</span>
        </el-divider>
        <el-button
            type="info"
            plain
            class="login-btn"
            @click="handleLogin"
        >
          返回登录
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerApi} from "@/api/RegisterApi.js";

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

// 注册表单数据
const registerForm = reactive({
  username: '',
  realName: '',
  idCard: '',
  phone: '',
  password: '',
  confirmPassword: '',
})

// 修改：使用函数形式的验证规则
const checkConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: checkConfirmPassword, trigger: 'blur' }
  ],
}

// 注册方法
const handleRegister = async () => {
  // 表单验证
  try {
    await registerFormRef.value.validate()
  } catch (error) {
    console.log('表单验证失败:', error)
    return
  }

  loading.value = true

  try {
    // 准备提交数据（不包含role，后端会强制设置为user）
    const submitData = {
      username: registerForm.username,
      realName: registerForm.realName,
      idCard: registerForm.idCard,
      phone: registerForm.phone,
      password: registerForm.password
    }

    console.log('发送注册数据:', submitData)

    const response = await registerApi(submitData)

    console.log('注册响应:', response)

    if (response.code === 2000) {
      ElMessage.success(response.msg || '注册成功')
      // 延迟跳转，让用户看到成功消息和userId
      setTimeout(() => {
        router.push('/login')
      }, 3000)
    } else {
      ElMessage.error(response.msg || '注册失败')
    }
  } catch (error) {
    console.error('注册错误:', error)

    if (error.response) {
      // 服务器返回错误
      console.log('错误状态码:', error.response.status)
      console.log('错误数据:', error.response.data)
      ElMessage.error(error.response?.data?.msg || `请求失败: ${error.response.status}`)
    } else if (error.request) {
      // 请求已发送但无响应
      console.log('请求对象:', error.request)
      ElMessage.error('服务器无响应，请检查后端服务')
    } else {
      // 其他错误
      ElMessage.error('注册失败: ' + error.message)
    }
  } finally {
    loading.value = false
  }
}

// 返回登录
const handleLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 100%;
  max-width: 500px;
  border-radius: 12px;
  box-shadow: 0 20px 35px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.register-btn {
  width: 100%;
  margin-top: 10px;
}

.footer-buttons {
  margin-top: 20px;
  text-align: center;
}

.divider-text {
  color: #999;
  font-size: 14px;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .register-card {
    width: 90%;
    max-width: 400px;
  }
}
</style>
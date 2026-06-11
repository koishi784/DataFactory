<template>
  <div class="login-page">
    <!-- 左侧：动画角色区 -->
    <div class="login-left">
      <CharacterScene
        :is-password-focused="passwordFocused"
        :is-login-error="loginError"
        :account-empty-count="accountEmptyCount"
      />
    </div>

    <!-- 右侧：表单区 -->
    <div class="login-right">
      <LoginForm
        v-if="isLoginMode"
        :loading="loginLoading"
        @login="handleLogin"
        @password-focus="passwordFocused = $event"
        @account-empty="accountEmptyCount++"
        @switch-to-register="isLoginMode = false"
      />
      <RegisterForm
        v-else
        :loading="registerLoading"
        @register="handleRegister"
        @password-focus="passwordFocused = $event"
        @switch-to-login="isLoginMode = true"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { register as registerApi } from '@/api/auth'
import type { LoginRequest, RegisterRequest } from '@/types'
import CharacterScene from './CharacterScene.vue'
import LoginForm from './LoginForm.vue'
import RegisterForm from './RegisterForm.vue'

const router = useRouter()
const authStore = useAuthStore()

// 登录/注册模式切换
const isLoginMode = ref(true)

// 交互状态
const passwordFocused = ref(false)
const loginError = ref(false)
const accountEmptyCount = ref(0)

// 加载状态
const loginLoading = ref(false)
const registerLoading = ref(false)

// 登录
async function handleLogin(account: string, password: string, rememberMe: boolean) {
  loginLoading.value = true
  loginError.value = false
  try {
    await authStore.login({ account, password, rememberMe } as LoginRequest)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {
    loginError.value = true
  } finally {
    loginLoading.value = false
  }
}

// 注册
async function handleRegister(data: RegisterRequest) {
  registerLoading.value = true
  try {
    const result = await registerApi(data) as any
    // 注册成功后端自动返回 token，直接设置登录状态
    authStore.setToken(result.accessToken, result.refreshToken)
    await authStore.fetchUserInfo()
    ElMessage.success('注册成功')
    router.push('/dashboard')
  } catch {
    // 错误已在 Axios 拦截器中处理
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.login-left {
  flex: 0 0 55%;
  height: 100vh;
  overflow: hidden;
}

.login-right {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #ffffff;
  padding: 40px;
}
</style>

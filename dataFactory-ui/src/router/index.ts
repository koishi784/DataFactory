import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()
  const token = authStore.accessToken

  if (to.path === '/login') {
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  // 如果已登录但 userInfo 为空，尝试获取用户信息
  if (!authStore.userInfo) {
    const ok = await authStore.fetchUserInfo()
    if (!ok) {
      // 获取失败，检查 token 是否已被清除（刷新 token 失败导致）
      if (!authStore.accessToken) {
        next('/login')
        return
      }
      // 其他网络错误，token 有效，允许访问
      next()
      return
    }
  }

  // 权限校验（仅在用户信息加载成功后才做）
  const permission = to.meta.permission as string | undefined
  if (permission && authStore.userInfo && !authStore.hasPermission(permission)) {
    next('/dashboard')
    return
  }

  next()
})

export default router

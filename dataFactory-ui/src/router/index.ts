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

  // 如果已登录但 userInfo 为空，获取用户信息
  if (!authStore.userInfo) {
    try {
      await authStore.fetchUserInfo()
    } catch {
      authStore.logout()
      next('/login')
      return
    }
  }

  // 权限校验
  const permission = to.meta.permission as string | undefined
  if (permission && !authStore.hasPermission(permission)) {
    next('/dashboard')
    return
  }

  next()
})

export default router

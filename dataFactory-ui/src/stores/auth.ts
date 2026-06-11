import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getCurrentUser, refreshToken as refreshTokenApi } from '@/api/auth'
import type { LoginRequest, CurrentUserInfo } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || '')
  const refreshTokenStr = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref<CurrentUserInfo | null>(null)

  const isLoggedIn = computed(() => !!accessToken.value)
  const permissions = computed(() => userInfo.value?.permissions || [])
  const roles = computed(() => userInfo.value?.roles || [])

  function hasPermission(perm: string): boolean {
    if (!perm) return true
    return permissions.value.some((p) => p === perm || p === '*')
  }

  function hasRole(role: string): boolean {
    return roles.value.includes(role)
  }

  /** 登录成功或注册成功后设置 token */
  function setToken(access: string, refresh: string) {
    accessToken.value = access
    refreshTokenStr.value = refresh
    localStorage.setItem('accessToken', access)
    localStorage.setItem('refreshToken', refresh)
  }

  async function loginAction(data: LoginRequest) {
    const result = await loginApi(data) as any
    setToken(result.accessToken, result.refreshToken)
    await fetchUserInfo()
  }

  async function fetchUserInfo(): Promise<boolean> {
    try {
      userInfo.value = await getCurrentUser() as CurrentUserInfo
      return true
    } catch {
      userInfo.value = null
      return false
    }
  }

  async function refreshTokenAction() {
    const currentRefreshToken = refreshTokenStr.value
    if (!currentRefreshToken) {
      throw new Error('No refresh token available')
    }
    try {
      const result = await refreshTokenApi(currentRefreshToken) as any
      setToken(result.accessToken, result.refreshToken)
      return result.accessToken
    } catch {
      // 不要在这里调用 logout() — 让 axios 拦截器统一处理
      throw new Error('Token refresh failed')
    }
  }

  function logout() {
    logoutApi().catch(() => {})
    accessToken.value = ''
    refreshTokenStr.value = ''
    userInfo.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  return {
    accessToken,
    refreshToken: refreshTokenStr,
    userInfo,
    isLoggedIn,
    permissions,
    roles,
    hasPermission,
    hasRole,
    setToken,
    login: loginAction,
    fetchUserInfo,
    doRefreshToken: refreshTokenAction,
    logout,
  }
})

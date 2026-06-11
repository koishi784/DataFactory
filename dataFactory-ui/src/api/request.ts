import axios, { AxiosError } from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import type { Result } from '@/types'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
})

// 是否正在刷新 token
let isRefreshing = false
// 等待刷新 token 的请求队列
let pendingRequests: Array<(token: string) => void> = []

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    if (authStore.accessToken) {
      config.headers.Authorization = `Bearer ${authStore.accessToken}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

request.interceptors.response.use(
  (response) => {
    const res = response.data as Result
    if (res.code !== 100200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res.data !== undefined ? res.data : response.data
  },
  async (error: AxiosError) => {
    const { response, config } = error
    if (!response || !config) {
      ElMessage.error('网络连接失败，请检查网络')
      return Promise.reject(error)
    }

    // token 过期，尝试刷新
    if (response.status === 401) {
      const authStore = useAuthStore()

      if (!isRefreshing) {
        isRefreshing = true
        try {
          const newToken = await authStore.doRefreshToken()
          isRefreshing = false
          // 重放等待队列中的请求
          pendingRequests.forEach((cb) => cb(newToken))
          pendingRequests = []
          // 重放当前请求
          config.headers.Authorization = `Bearer ${newToken}`
          return request(config)
        } catch {
          isRefreshing = false
          pendingRequests = []
          authStore.logout()
          window.location.href = '/login'
          return Promise.reject(error)
        }
      } else {
        // 正在刷新，将请求加入队列等待
        return new Promise((resolve) => {
          pendingRequests.push((token: string) => {
            config.headers.Authorization = `Bearer ${token}`
            resolve(request(config))
          })
        })
      }
    }

    // 其他错误
    const res = response.data as Result
    ElMessage.error(res?.message || '请求失败')
    return Promise.reject(error)
  }
)

export default request

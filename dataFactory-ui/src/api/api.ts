import request from './request'
import type { PageParams, PageResult } from '@/types'
import type { ApiCategory, ApiInfo } from '@/types/api'

// ===== 接口分类 =====
export function getApiCategoryTree() {
  return request.get<ApiCategory[]>('/api-categories/tree')
}

export function createApiCategory(data: { name: string; parentId: number; sortOrder?: number }) {
  return request.post('/api-categories', data)
}

export function updateApiCategory(id: number, data: { name?: string; sortOrder?: number }) {
  return request.put(`/api-categories/${id}`, data)
}

export function deleteApiCategory(id: number) {
  return request.delete(`/api-categories/${id}`)
}

// ===== 接口管理 =====
export function getApiList(params: PageParams & { keyword?: string; status?: number; categoryId?: number; source?: string }) {
  return request.get<PageResult<ApiInfo>>('/apis', { params })
}

export function getApiDetail(id: number) {
  return request.get<any>(`/apis/${id}`)
}

export function createApi(data: Record<string, any>) {
  return request.post('/apis', data)
}

export function updateApi(id: number, data: Record<string, any>) {
  return request.put(`/apis/${id}`, data)
}

export function publishApi(id: number) {
  return request.put(`/apis/${id}/publish`)
}

export function disableApi(id: number) {
  return request.put(`/apis/${id}/disable`)
}

export function deleteApi(id: number) {
  return request.delete(`/apis/${id}`)
}

export function batchPublishApi(ids: number[]) {
  return request.put('/apis/batch/publish', { ids })
}

export function batchDisableApi(ids: number[]) {
  return request.put('/apis/batch/disable', { ids })
}

export function batchCategoryApi(ids: number[], categoryId: number) {
  return request.put('/apis/batch/category', { ids, categoryId })
}

export function testApi(id: number, data: Record<string, any>) {
  return request.post(`/apis/${id}/test`, data)
}

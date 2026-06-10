import request from './request'
import type { PageParams, PageResult } from '@/types'
import type { Script, ScriptCategory } from '@/types/script'

// ===== 脚本分类 =====
export function getScriptCategoryTree() {
  return request.get<ScriptCategory[]>('/script-categories/tree')
}

export function createScriptCategory(data: { name: string; parentId: number; sortOrder?: number }) {
  return request.post('/script-categories', data)
}

export function updateScriptCategory(id: number, data: { name?: string; sortOrder?: number }) {
  return request.put(`/script-categories/${id}`, data)
}

export function deleteScriptCategory(id: number) {
  return request.delete(`/script-categories/${id}`)
}

// ===== 脚本管理 =====
export function getScriptList(params: PageParams & { scriptName?: string; scriptType?: string; status?: number }) {
  return request.get<PageResult<Script>>('/scripts', { params })
}

export function getScriptDetail(id: number) {
  return request.get<Script>(`/scripts/${id}`)
}

export function createScript(data: Partial<Script>) {
  return request.post('/scripts', data)
}

export function updateScript(id: number, data: Partial<Script>) {
  return request.put(`/scripts/${id}`, data)
}

export function debugScript(id: number, params: Record<string, any>) {
  return request.post(`/scripts/${id}/debug`, params)
}

export function publishScript(id: number) {
  return request.put(`/scripts/${id}/publish`)
}

export function disableScript(id: number) {
  return request.put(`/scripts/${id}/disable`)
}

export function deleteScript(id: number) {
  return request.delete(`/scripts/${id}`)
}

export function batchPublishScript(ids: number[]) {
  return request.put('/scripts/batch/publish', { ids })
}

export function batchDisableScript(ids: number[]) {
  return request.put('/scripts/batch/disable', { ids })
}

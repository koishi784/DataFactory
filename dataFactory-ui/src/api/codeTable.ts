import request from './request'
import type { PageParams, PageResult } from '@/types'
import type { CodeTable, CodeItem } from '@/types/codeTable'

// ===== 码表管理 =====
export function getCodeTableList(params: PageParams & { tableName?: string; tableCode?: string; status?: number }) {
  return request.get<PageResult<CodeTable>>('/code-tables', { params })
}

export function getCodeTableDetail(id: number) {
  return request.get<CodeTable>(`/code-tables/${id}`)
}

export function createCodeTable(data: Partial<CodeTable>) {
  return request.post('/code-tables', data)
}

export function updateCodeTable(id: number, data: Partial<CodeTable>) {
  return request.put(`/code-tables/${id}`, data)
}

export function publishCodeTable(id: number) {
  return request.put(`/code-tables/${id}/publish`)
}

export function disableCodeTable(id: number) {
  return request.put(`/code-tables/${id}/disable`)
}

export function deleteCodeTable(id: number) {
  return request.delete(`/code-tables/${id}`)
}

export function batchPublishCodeTable(ids: number[]) {
  return request.put('/code-tables/batch/publish', { ids })
}

export function batchDisableCodeTable(ids: number[]) {
  return request.put('/code-tables/batch/disable', { ids })
}

// ===== 码值管理 =====
export function getCodeItemList(tableId: number) {
  return request.get<CodeItem[]>(`/code-tables/${tableId}/items`)
}

export function createCodeItem(tableId: number, data: Partial<CodeItem>) {
  return request.post(`/code-tables/${tableId}/items`, data)
}

export function updateCodeItem(tableId: number, itemId: number, data: Partial<CodeItem>) {
  return request.put(`/code-tables/${tableId}/items/${itemId}`, data)
}

export function deleteCodeItem(tableId: number, itemId: number) {
  return request.delete(`/code-tables/${tableId}/items/${itemId}`)
}

import request from './request'
import type { PageParams, PageResult } from '@/types'
import type { DatabaseConnection } from '@/types/database'

export function getDatabaseList(params: PageParams & { connectionName?: string; dbType?: string; status?: number }) {
  return request.get<PageResult<DatabaseConnection>>('/databases', { params })
}

export function getDatabaseDetail(id: number) {
  return request.get<DatabaseConnection>(`/databases/${id}`)
}

export function createDatabase(data: Partial<DatabaseConnection>) {
  return request.post('/databases', data)
}

export function updateDatabase(id: number, data: Partial<DatabaseConnection>) {
  return request.put(`/databases/${id}`, data)
}

export function testDatabase(id: number) {
  return request.post(`/databases/${id}/test`)
}

export function publishDatabase(id: number) {
  return request.put(`/databases/${id}/publish`)
}

export function disableDatabase(id: number) {
  return request.put(`/databases/${id}/disable`)
}

export function deleteDatabase(id: number) {
  return request.delete(`/databases/${id}`)
}

export function batchPublishDatabase(ids: number[]) {
  return request.put('/databases/batch/publish', { ids })
}

export function batchDisableDatabase(ids: number[]) {
  return request.put('/databases/batch/disable', { ids })
}

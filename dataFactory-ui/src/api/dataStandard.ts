import request from './request'
import type { PageParams, PageResult } from '@/types'
import type { DataStandard } from '@/types/dataStandard'

export function getDataStandardList(params: PageParams & { standardName?: string; standardCode?: string; status?: number }) {
  return request.get<PageResult<DataStandard>>('/data-standards', { params })
}

export function getDataStandardDetail(id: number) {
  return request.get<DataStandard>(`/data-standards/${id}`)
}

export function createDataStandard(data: Partial<DataStandard>) {
  return request.post('/data-standards', data)
}

export function updateDataStandard(id: number, data: Partial<DataStandard>) {
  return request.put(`/data-standards/${id}`, data)
}

export function publishDataStandard(id: number) {
  return request.put(`/data-standards/${id}/publish`)
}

export function disableDataStandard(id: number) {
  return request.put(`/data-standards/${id}/disable`)
}

export function deleteDataStandard(id: number) {
  return request.delete(`/data-standards/${id}`)
}

export function batchPublishDataStandard(ids: number[]) {
  return request.put('/data-standards/batch/publish', { ids })
}

export function batchDisableDataStandard(ids: number[]) {
  return request.put('/data-standards/batch/disable', { ids })
}

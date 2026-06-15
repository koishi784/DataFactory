import request from './request'
import type { PageParams, PageResult } from '@/types'
import type { DataStandard } from '@/types/dataStandard'

export function getDataStandardList(params: PageParams & { keyword?: string; status?: number; dataType?: string }) {
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

/** §7.9 导入模板下载 */
export function downloadTemplate() {
  return request.get('/data-standards/template', { responseType: 'blob' })
}

/** §7.10 标准导入 */
export function importDataStandard(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/data-standards/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

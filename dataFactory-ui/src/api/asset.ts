import request from './request'
import type { PageParams, PageResult } from '@/types'
import type { Asset, AssetDirectory } from '@/types/asset'

// ===== 资产目录 =====
export function getAssetDirectoryTree() {
  return request.get<AssetDirectory[]>('/assets/directories/tree')
}

export function createAssetDirectory(data: { name: string; parentId: number; sortOrder?: number }) {
  return request.post('/assets/directories', data)
}

export function updateAssetDirectory(id: number, data: { name?: string; sortOrder?: number }) {
  return request.put(`/assets/directories/${id}`, data)
}

export function deleteAssetDirectory(id: number) {
  return request.delete(`/assets/directories/${id}`)
}

// ===== 数据资产管理 =====
export function getAssetList(params: PageParams & { keyword?: string; status?: number; directoryId?: number }) {
  return request.get<PageResult<Asset>>('/assets', { params })
}

export function getAssetDetail(id: number) {
  return request.get<Asset>(`/assets/${id}`)
}

export function createAsset(data: Partial<Asset>) {
  return request.post('/assets', data)
}

export function updateAsset(id: number, data: Partial<Asset>) {
  return request.put(`/assets/${id}`, data)
}

export function publishAsset(id: number) {
  return request.put(`/assets/${id}/publish`)
}

export function disableAsset(id: number) {
  return request.put(`/assets/${id}/disable`)
}

export function deleteAsset(id: number) {
  return request.delete(`/assets/${id}`)
}

export function batchPublishAsset(ids: number[]) {
  return request.put('/assets/batch/publish', { ids })
}

export function batchDisableAsset(ids: number[]) {
  return request.put('/assets/batch/disable', { ids })
}

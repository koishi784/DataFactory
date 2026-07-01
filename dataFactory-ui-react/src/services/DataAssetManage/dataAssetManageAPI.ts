import { request } from '@umijs/max';

export interface AssetDirectoryTreeNode {
  id: number;
  name: string;
  parentId: number;
  level: number;
  sortOrder: number;
  createTime: string;
  children: AssetDirectoryTreeNode[];
}

export interface AssetItem {
  id: number;
  assetName: string;
  englishName: string;
  description: string;
  status: number;
  directoryIds: number[];
  createTime: string;
  updateTime: string;
}

export interface AssetField {
  id: number;
  englishFieldName: string;
  chineseFieldName: string;
  description: string;
  standardId: number | null;
  sortOrder: number;
}

export interface AssetDirectoryRef {
  id: number;
  name: string;
}

export interface AssetDetail extends AssetItem {
  fields: AssetField[];
  directories: AssetDirectoryRef[];
}

export interface AssetListParams {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  status?: string;
  directoryId?: number;
}

export interface CreateAssetData {
  assetName: string;
  englishName: string;
  description?: string;
  directoryIds: number[];
  fields?: CreateAssetFieldData[];
}

export interface CreateAssetFieldData {
  englishFieldName: string;
  chineseFieldName: string;
  description?: string;
  standardId?: number;
  sortOrder?: number;
}

export interface CreateDirectoryData {
  name: string;
  parentId: number;
  sortOrder?: number;
}

export interface UpdateDirectoryData {
  name: string;
  sortOrder?: number;
}

export async function getAssetDirectoryTree() {
  return request('/assets/directories/tree', {
    method: 'GET',
  });
}

export async function getAssetList(params?: AssetListParams) {
  return request('/assets', {
    method: 'GET',
    params,
  });
}

export async function getAssetDetail(id: number) {
  return request(`/assets/${id}`, {
    method: 'GET',
  });
}

export async function createAsset(data: CreateAssetData) {
  return request('/assets', {
    method: 'POST',
    data,
  });
}

export async function updateAsset(id: number, data: CreateAssetData) {
  return request(`/assets/${id}`, {
    method: 'PUT',
    data,
  });
}

export async function deleteAsset(id: number) {
  return request(`/assets/${id}`, {
    method: 'DELETE',
  });
}

export async function publishAsset(id: number) {
  return request(`/assets/${id}/publish`, {
    method: 'PUT',
  });
}

export async function disableAsset(id: number) {
  return request(`/assets/${id}/disable`, {
    method: 'PUT',
  });
}

export async function batchPublishAssets(data: { ids: number[] }) {
  return request('/assets/batch/publish', {
    method: 'PUT',
    data,
  });
}

export async function batchDisableAssets(data: { ids: number[] }) {
  return request('/assets/batch/disable', {
    method: 'PUT',
    data,
  });
}

export async function createDirectory(data: CreateDirectoryData) {
  return request('/assets/directories', {
    method: 'POST',
    data,
  });
}

export async function updateDirectory(id: number, data: UpdateDirectoryData) {
  return request(`/assets/directories/${id}`, {
    method: 'PUT',
    data,
  });
}

export async function deleteDirectory(id: number) {
  return request(`/assets/directories/${id}`, {
    method: 'DELETE',
  });
}
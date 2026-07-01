import { request } from '@umijs/max';

export interface DataStandardItem {
  id: number;
  name: string;
  englishName: string;
  standardCode: string;
  dataType: 'String' | 'Int' | 'Float' | 'Enum';
  length: number | null;
  precision: number | null;
  defaultValue: string | null;
  rangeMin: string | null;
  rangeMax: string | null;
  enumRange: string | null;
  sourceOrganization: string;
  nullable: number;
  description: string;
  status: number;
  createTime: string;
  updateTime: string;
}

export interface DataStandardListParams {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  status?: string;
  dataType?: string;
}

export interface CreateDataStandardData {
  name: string;
  englishName: string;
  dataType: 'String' | 'Int' | 'Float' | 'Enum';
  sourceOrganization: string;
  length?: number;
  precision?: number;
  defaultValue?: string;
  rangeMin?: string;
  rangeMax?: string;
  enumRange?: string;
  nullable?: number;
  description?: string;
}

export async function getDataStandardList(params?: DataStandardListParams) {
  return request('/data-standards', {
    method: 'GET',
    params,
  });
}

export async function getDataStandardDetail(id: number) {
  return request(`/data-standards/${id}`, {
    method: 'GET',
  });
}

export async function createDataStandard(data: CreateDataStandardData) {
  return request('/data-standards', {
    method: 'POST',
    data,
  });
}

export async function updateDataStandard(id: number, data: CreateDataStandardData) {
  return request(`/data-standards/${id}`, {
    method: 'PUT',
    data,
  });
}

export async function deleteDataStandard(id: number) {
  return request(`/data-standards/${id}`, {
    method: 'DELETE',
  });
}

export async function publishDataStandard(id: number) {
  return request(`/data-standards/${id}/publish`, {
    method: 'PUT',
  });
}

export async function disableDataStandard(id: number) {
  return request(`/data-standards/${id}/disable`, {
    method: 'PUT',
  });
}

export async function batchPublishDataStandards(data: { ids: number[] }) {
  return request('/data-standards/batch/publish', {
    method: 'PUT',
    data,
  });
}

export async function batchDisableDataStandards(data: { ids: number[] }) {
  return request('/data-standards/batch/disable', {
    method: 'PUT',
    data,
  });
}

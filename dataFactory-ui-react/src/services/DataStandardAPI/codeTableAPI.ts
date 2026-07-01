import { request } from '@umijs/max';

export interface CodeTableItem {
  id: number;
  tableName: string;
  tableCode: string;
  description: string;
  status: number;
  codeItemCount: number;
  createTime: string;
  updateTime: string;
}

export interface CodeTableDetail {
  id: number;
  tableName: string;
  tableCode: string;
  description: string;
  status: number;
  codeItemCount: number;
  createTime: string;
  updateTime: string;
}

export interface CodeTableInfo {
  id: number;
  tableName: string;
  tableCode: string;
  description: string;
}

export interface CodeItem {
  id: number;
  code: string;
  name: string;
  value: string;
  sortOrder: number;
  parentCode: string | null;
  status: number;
  description: string;
}

export interface CodeTableItemsResponse {
  tableInfo: CodeTableInfo;
  items: CodeItem[];
}

export interface CodeTableListParams {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  status?: string;
}

export interface CreateCodeTableData {
  tableName: string;
  description?: string;
  items?: CreateCodeItemData[];
}

export interface CreateCodeItemData {
  code: string;
  name: string;
  value: string;
  sortOrder?: number;
  parentCode?: string;
  description?: string;
}

export async function getCodeTableList(params?: CodeTableListParams) {
  return request('/code-tables', {
    method: 'GET',
    params,
  });
}

export async function getCodeTableDetail(id: number) {
  return request(`/code-tables/${id}`, {
    method: 'GET',
  });
}

export async function getCodeTableItems(id: number) {
  return request(`/code-tables/${id}/items`, {
    method: 'GET',
  });
}

export async function createCodeTable(data: CreateCodeTableData) {
  return request('/code-tables', {
    method: 'POST',
    data,
  });
}

export async function updateCodeTable(id: number, data: CreateCodeTableData) {
  return request(`/code-tables/${id}`, {
    method: 'PUT',
    data,
  });
}

export async function createCodeItem(tableId: number, data: CreateCodeItemData) {
  return request(`/code-tables/${tableId}/items`, {
    method: 'POST',
    data,
  });
}

export async function updateCodeItem(tableId: number, itemId: number, data: CreateCodeItemData) {
  return request(`/code-tables/${tableId}/items/${itemId}`, {
    method: 'PUT',
    data,
  });
}

export async function deleteCodeItem(tableId: number, itemId: number) {
  return request(`/code-tables/${tableId}/items/${itemId}`, {
    method: 'DELETE',
  });
}

export async function publishCodeTable(id: number) {
  return request(`/code-tables/${id}/publish`, {
    method: 'PUT',
  });
}

export async function disableCodeTable(id: number) {
  return request(`/code-tables/${id}/disable`, {
    method: 'PUT',
  });
}

export async function deleteCodeTable(id: number) {
  return request(`/code-tables/${id}`, {
    method: 'DELETE',
  });
}

export async function batchPublishCodeTables(data: { ids: number[] }) {
  return request('/code-tables/batch/publish', {
    method: 'PUT',
    data,
  });
}

export async function batchDisableCodeTables(data: { ids: number[] }) {
  return request('/code-tables/batch/disable', {
    method: 'PUT',
    data,
  });
}

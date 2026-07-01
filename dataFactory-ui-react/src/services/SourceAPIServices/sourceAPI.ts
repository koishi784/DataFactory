import { request } from '@umijs/max';

// 5.1 接口分类管理

// 5.1.1 查询分类树
export async function getCategoryTree() {
  return request('/api-categories/tree', {
    method: 'GET',
  });
}

// 5.1.2 新增分类
export async function createCategory(data: {
  name: string;
  parentId: number;
  sortOrder?: number;
}) {
  return request('/api-categories', {
    method: 'POST',
    data,
  });
}

// 5.1.3 编辑分类
export async function updateCategory(id: number, data: {
  name: string;
  sortOrder?: number;
}) {
  return request(`/api-categories/${id}`, {
    method: 'PUT',
    data,
  });
}

// 5.1.4 删除分类
export async function deleteCategory(id: number) {
  return request(`/api-categories/${id}`, {
    method: 'DELETE',
  });
}

// 5.2 接口注册管理

// 5.2.1 查询接口列表
export async function getApiList(params: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  status?: string;
  categoryId?: number;
  source?: string;
  sortOrder?: string;
}) {
  return request('/apis', {
    method: 'GET',
    params,
  });
}

// 5.2.2 查询接口详情
export async function getApiDetail(id: number) {
  return request(`/apis/${id}`, {
    method: 'GET',
  });
}

// 5.2.3 新增注册接口
export async function createApi(data: {
  apiName: string;
  apiDescription?: string;
  categoryId: number;
  source: string;
  protocol: string;
  method: string;
  url: string;
  timeout?: number;
  retryCount?: number;
  headers?: Array<{
    key: string;
    value: string;
    required: boolean;
    description?: string;
  }>;
  requestParams?: Array<{
    paramName: string;
    paramType: string;
    dataType: string;
    required: boolean;
    description?: string;
    defaultValue?: string;
    exampleValue?: string;
    sortOrder?: number;
    validationRule?: string;
    minValue?: string;
    maxValue?: string;
  }>;
  responseExample?: string;
  remark?: string;
}) {
  return request('/apis', {
    method: 'POST',
    data,
  });
}

// 5.2.4 编辑接口
export async function updateApi(id: number, data: {
  apiName: string;
  apiDescription?: string;
  categoryId: number;
  source: string;
  protocol: string;
  method: string;
  url: string;
  timeout?: number;
  retryCount?: number;
  headers?: Array<{
    key: string;
    value: string;
    required: boolean;
    description?: string;
  }>;
  requestParams?: Array<{
    paramName: string;
    paramType: string;
    dataType: string;
    required: boolean;
    description?: string;
    defaultValue?: string;
    exampleValue?: string;
    sortOrder?: number;
    validationRule?: string;
    minValue?: string;
    maxValue?: string;
  }>;
  responseExample?: string;
  remark?: string;
}) {
  return request(`/apis/${id}`, {
    method: 'PUT',
    data,
  });
}

// 5.2.5 发布接口
export async function publishApi(id: number) {
  return request(`/apis/${id}/publish`, {
    method: 'PUT',
  });
}

// 5.2.6 停用接口
export async function disableApi(id: number) {
  return request(`/apis/${id}/disable`, {
    method: 'PUT',
  });
}

// 5.2.7 删除接口
export async function deleteApi(id: number) {
  return request(`/apis/${id}`, {
    method: 'DELETE',
  });
}

// 5.2.8 批量发布接口
export async function batchPublishApis(data: { ids: number[] }) {
  return request('/apis/batch/publish', {
    method: 'PUT',
    data,
  });
}

// 5.2.9 批量停用接口
export async function batchDisableApis(data: { ids: number[] }) {
  return request('/apis/batch/disable', {
    method: 'PUT',
    data,
  });
}

// 5.2.10 批量分类
export async function batchUpdateCategory(data: { ids: number[]; categoryId: number }) {
  return request('/apis/batch/category', {
    method: 'PUT',
    data,
  });
}

// 5.2.11 接口测试调用
export async function testApi(id: number, data: { paramValues?: Record<string, any> }) {
  return request(`/apis/${id}/test`, {
    method: 'POST',
    data,
  });
}
import { request } from '@umijs/max';

export async function getDatabaseList(params?: {
  pageNum?: number;
  pageSize?: number;
  keyword?: string;
  dbType?: string;
  status?: string;
}) {
  return request('/databases', {
    method: 'GET',
    params,
  });
}

export async function getDatabaseDetail(id: number) {
  return request(`/databases/${id}`, {
    method: 'GET',
  });
}

export async function createDatabase(data: {
  connectionName: string;
  dbType: string;
  host: string;
  port: number;
  databaseName: string;
  username: string;
  password: string;
  jdbcParams?: string;
  description?: string;
}) {
  return request('/databases', {
    method: 'POST',
    data,
  });
}

export async function updateDatabase(id: number, data: {
  connectionName: string;
  dbType: string;
  host: string;
  port: number;
  databaseName: string;
  username: string;
  password?: string;
  jdbcParams?: string;
  description?: string;
}) {
  return request(`/databases/${id}`, {
    method: 'PUT',
    data,
  });
}

export async function testDatabaseConnection(id: number) {
  return request(`/databases/${id}/test`, {
    method: 'POST',
  });
}

export async function publishDatabase(id: number) {
  return request(`/databases/${id}/publish`, {
    method: 'PUT',
  });
}

export async function disableDatabase(id: number) {
  return request(`/databases/${id}/disable`, {
    method: 'PUT',
  });
}

export async function deleteDatabase(id: number) {
  return request(`/databases/${id}`, {
    method: 'DELETE',
  });
}

export async function batchPublishDatabases(data: { ids: number[] }) {
  return request('/databases/batch/publish', {
    method: 'PUT',
    data,
  });
}

export async function batchDisableDatabases(data: { ids: number[] }) {
  return request('/databases/batch/disable', {
    method: 'PUT',
    data,
  });
}

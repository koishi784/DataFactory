import request from './request'
import type { PageParams, PageResult } from '@/types'
import type { Task, TaskCategory, TaskExecution } from '@/types/task'

// ===== 任务分类 =====
export function getTaskCategoryTree() {
  return request.get<TaskCategory[]>('/task-categories/tree')
}

export function createTaskCategory(data: { name: string; parentId: number; sortOrder?: number }) {
  return request.post('/task-categories', data)
}

export function updateTaskCategory(id: number, data: { name?: string; sortOrder?: number }) {
  return request.put(`/task-categories/${id}`, data)
}

export function deleteTaskCategory(id: number) {
  return request.delete(`/task-categories/${id}`)
}

// ===== 任务管理 =====
export function getTaskList(params: PageParams & { keyword?: string; status?: number; categoryId?: number; scheduleType?: string }) {
  return request.get<PageResult<Task>>('/tasks', { params })
}

export function getTaskDetail(id: number) {
  return request.get<Task>(`/tasks/${id}`)
}

export function createTask(data: Partial<Task>) {
  return request.post('/tasks', data)
}

export function updateTask(id: number, data: Partial<Task>) {
  return request.put(`/tasks/${id}`, data)
}

export function updateTaskDagConfig(id: number, data: { nodes: any[]; edges: any[] }) {
  return request.put(`/tasks/${id}/config`, data)
}

export function setTaskTriggerConfig(id: number, data: { scheduleType: string; cronExpression?: string }) {
  return request.put(`/tasks/${id}/trigger-config`, data)
}

export function testRunTask(id: number) {
  return request.post(`/tasks/${id}/test-run`)
}

export function executeTask(id: number) {
  return request.post(`/tasks/${id}/execute`)
}

export function cancelExecution(taskId: number, executionId: number) {
  return request.post(`/tasks/${taskId}/executions/${executionId}/cancel`)
}

export function getTaskExecutions(taskId: number, params: PageParams) {
  return request.get<PageResult<TaskExecution>>(`/tasks/${taskId}/executions`, { params })
}

export function publishTask(id: number) {
  return request.put(`/tasks/${id}/publish`)
}

export function disableTask(id: number) {
  return request.put(`/tasks/${id}/disable`)
}

export function deleteTask(id: number) {
  return request.delete(`/tasks/${id}`)
}

export function batchPublishTask(ids: number[]) {
  return request.put('/tasks/batch/publish', { ids })
}

export function batchDisableTask(ids: number[]) {
  return request.put('/tasks/batch/disable', { ids })
}

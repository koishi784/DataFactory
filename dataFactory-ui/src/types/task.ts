import type { BaseEntity } from './index'

export interface Task extends BaseEntity {
  taskName: string
  taskDescription?: string
  description?: string
  categoryId?: number
  status: number
  scheduleType?: string
  cronExpression?: string
  executeStatus?: number
  lastExecuteTime?: string
  nextExecuteTime?: string
}

export interface TaskNode {
  id: string
  type: 'START' | 'END' | 'API' | 'SCRIPT' | 'MAPPING' | 'OUTPUT'
  label: string
  x: number
  y: number
  config?: Record<string, any>
}

export interface TaskEdge {
  id: string
  sourceNodeId: string
  targetNodeId: string
}

export interface TaskExecution extends BaseEntity {
  taskId: number
  status: string
  startTime?: string
  endTime?: string
  duration?: number
  errorMessage?: string
}

export interface TaskCategory {
  id: number
  name: string
  parentId: number
  level: number
  sortOrder: number
  children?: TaskCategory[]
}

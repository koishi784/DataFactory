import type { BaseEntity } from './index'

export interface Script extends BaseEntity {
  scriptName: string
  scriptType: string
  description?: string
  content?: string
  version: string
  categoryId?: number
  status: number
  params?: ScriptParam[]
}

export interface ScriptParam {
  id?: number
  scriptId?: number
  paramName: string
  paramType: 'INPUT' | 'OUTPUT'
  dataType: string
  required: boolean
  defaultValue?: string
  description?: string
  sortOrder: number
}

export interface ScriptCategory {
  id: number
  name: string
  parentId: number
  level: number
  sortOrder: number
  children?: ScriptCategory[]
}

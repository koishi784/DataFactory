import type { BaseEntity } from './index'

export interface Script extends BaseEntity {
  scriptName: string
  scriptType: string
  description?: string
  categoryId: number
  fileId?: number
  fileName?: string
  scriptContent?: string
  status: number
  inputParams?: ScriptParam[]
  outputParams?: ScriptParam[]
}

export interface ScriptParam {
  id?: number
  paramName: string
  paramType: string
  description?: string
}

export interface ScriptCategory {
  id: number
  name: string
  parentId: number
  level: number
  sortOrder: number
  children?: ScriptCategory[]
}

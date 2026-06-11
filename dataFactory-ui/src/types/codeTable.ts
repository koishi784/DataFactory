import type { BaseEntity } from './index'

export interface CodeTable extends BaseEntity {
  tableName: string
  tableCode: string
  description?: string
  status: number
}

export interface CodeItem {
  id?: number
  tableId?: number
  itemKey: string
  itemValue: string
  sortOrder: number
  parentId?: number
  enabled: boolean
  description?: string
}

import type { BaseEntity } from './index'

export interface Asset extends BaseEntity {
  assetName: string
  englishName: string
  description?: string
  status: number
  directoryId?: number
}

export interface AssetDirectory {
  id: number
  name: string
  parentId: number
  level: number
  sortOrder: number
  children?: AssetDirectory[]
}

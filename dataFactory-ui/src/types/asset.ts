import type { BaseEntity } from './index'

export interface AssetField {
  id?: number
  englishFieldName: string
  chineseFieldName: string
  description?: string
  standardId?: number
  sortOrder?: number
}

export interface Asset extends BaseEntity {
  assetName: string
  englishName: string
  description?: string
  status: number
  directoryIds?: number[]
  directories?: { id: number; name: string }[]
  fields?: AssetField[]
}

export interface AssetDirectory {
  id: number
  name: string
  parentId: number
  level: number
  sortOrder: number
  children?: AssetDirectory[]
}

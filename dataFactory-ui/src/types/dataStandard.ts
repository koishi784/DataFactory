import type { BaseEntity } from './index'

export interface DataStandard extends BaseEntity {
  standardName: string
  standardCode: string
  standardType: string
  dataType: string
  length?: number
  precision?: number
  validationRule?: string
  description?: string
  status: number
}

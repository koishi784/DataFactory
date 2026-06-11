import type { BaseEntity } from './index'

export interface DataStandard extends BaseEntity {
  name: string
  englishName: string
  standardCode: string
  dataType: string
  length?: number
  precision?: number
  sourceOrganization: string
  nullable: number
  defaultValue?: string
  rangeMin?: string
  rangeMax?: string
  enumRange?: string
  validationRule?: string
  description?: string
  status: number
  createTime: string
  updateTime: string
}

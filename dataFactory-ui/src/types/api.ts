import type { BaseEntity } from './index'

/** 接口分类 */
export interface ApiCategory {
  id: number
  name: string
  parentId: number
  level: number
  sortOrder: number
  children?: ApiCategory[]
}

/** 接口请求头 */
export interface ApiHeader {
  id?: number
  apiId?: number
  headerKey: string
  headerValue?: string
  required: boolean
  description?: string
  sortOrder: number
}

/** 接口请求参数 */
export interface ApiParam {
  id?: number
  apiId?: number
  paramName: string
  paramType: 'QUERY' | 'PATH' | 'HEADER' | 'BODY'
  dataType: string
  required: boolean
  description?: string
  defaultValue?: string
  exampleValue?: string
  sortOrder: number
  validationRule?: string
  minValue?: string
  maxValue?: string
}

/** 接口信息 */
export interface ApiInfo extends BaseEntity {
  apiName: string
  apiDescription?: string
  categoryId: number
  categoryName?: string
  source: string
  protocol: string
  method: string
  url: string
  timeout: number
  retryCount: number
  status: number
  version: string
  responseExample?: string
  remark?: string
  headers?: ApiHeader[]
  params?: ApiParam[]
}

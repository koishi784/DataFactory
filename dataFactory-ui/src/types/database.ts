import type { BaseEntity } from './index'

export interface DatabaseConnection extends BaseEntity {
  connectionName: string
  dbType: string
  host: string
  port: number
  databaseName: string
  username: string
  password?: string
  status: number
  description?: string
  jdbcParams?: string
  lastTestTime?: string
  lastTestResult?: number
}

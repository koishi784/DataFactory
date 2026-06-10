/** 统一响应格式 */
export interface Result<T = any> {
  code: number
  message: string
  data?: T
  timestamp: number
}

/** 分页响应格式 */
export interface PageResult<T = any> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

/** 分页请求参数 */
export interface PageParams {
  pageNum?: number
  pageSize?: number
}

/** 资源状态枚举 */
export enum StatusCode {
  DRAFT = 0,
  PUBLISHED = 1,
  DISABLED = 2,
}

/** 资源状态映射 */
export const StatusMap: Record<number, { label: string; type: 'info' | 'success' | 'danger' }> = {
  [StatusCode.DRAFT]: { label: '未发布', type: 'info' },
  [StatusCode.PUBLISHED]: { label: '已发布', type: 'success' },
  [StatusCode.DISABLED]: { label: '已停用', type: 'danger' },
}

/** 基础实体字段 */
export interface BaseEntity {
  id: number
  createTime: string
  updateTime: string
  createBy?: string
  updateBy?: string
}

/** 登录请求（对齐后端 LoginRequest.java） */
export interface LoginRequest {
  account: string
  password: string
  rememberMe?: boolean
}

/** 注册请求（对齐后端 RegisterRequest.java） */
export interface RegisterRequest {
  username: string
  password: string
  confirmPassword: string
  nickname?: string
  email?: string
  mobile?: string
  remark?: string
}

/** 登录响应 */
export interface LoginResult {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  userInfo: any
}

/** 当前用户信息 */
export interface CurrentUserInfo {
  id: number
  username: string
  nickname: string
  email: string
  mobile?: string
  avatar?: string
  status?: number
  roles: string[]
  permissions: string[]
}

/** 刷新 token 响应 */
export interface RefreshTokenResult {
  accessToken: string
  refreshToken: string
}

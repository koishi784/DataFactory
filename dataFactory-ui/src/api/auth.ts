import request from './request'
import type { LoginRequest, RegisterRequest, LoginResult, CurrentUserInfo, RefreshTokenResult } from '@/types'

export function login(data: LoginRequest) {
  return request.post<LoginResult>('/auth/login', data)
}

export function register(data: RegisterRequest) {
  return request.post<LoginResult>('/auth/register', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function getCurrentUser() {
  return request.get<CurrentUserInfo>('/auth/user-info')
}

export function refreshToken(refreshToken: string) {
  return request.post<RefreshTokenResult>('/auth/refresh-token', { refreshToken })
}

export function changePassword(data: { oldPassword: string; newPassword: string; confirmPassword: string }) {
  return request.put('/auth/password', data)
}

export function updateProfile(data: { nickname?: string; email?: string }) {
  return request.put('/auth/profile', data)
}

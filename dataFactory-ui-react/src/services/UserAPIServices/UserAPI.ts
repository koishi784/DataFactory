import { request } from '@umijs/max';

// 4.1 用户注册
export async function register(data: {
  username: string;
  password: string;
  confirmPassword: string;
  nickname?: string;
  email?: string;
  mobile?: string;
  remark?: string;
}) {
  return request('/auth/register', {
    method: 'POST',
    data,
    skipToken: true,
  });
}

// 4.2 用户登录
export async function login(data: {
  account: string;
  password: string;
  rememberMe?: boolean;
}) {
  return request('/auth/login', {
    method: 'POST',
    data,
    skipToken: true,
  });
}

// 4.3 用户登出
export async function logout() {
  return request('/auth/logout', {
    method: 'POST',
  });
}

// 4.4 获取当前用户信息
export async function getUserInfo() {
  return request('/auth/user-info', {
    method: 'GET',
  });
}

// 4.5 刷新令牌
export async function refreshToken(data: { refreshToken: string }) {
  return request('/auth/refresh-token', {
    method: 'POST',
    data,
    skipToken: true,
  });
}

// 4.6 修改密码
export async function changePassword(data: {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}) {
  return request('/auth/password', {
    method: 'PUT',
    data,
  });
}

// 4.7 修改个人信息
export async function updateProfile(data: {
  nickname?: string;
  email?: string;
  mobile?: string;
  remark?: string;
}) {
  return request('/auth/profile', {
    method: 'PUT',
    data,
  });
}

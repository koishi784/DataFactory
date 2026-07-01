export default {
  'POST /api/v1/auth/register': {
    code: 100200,
    message: '注册成功',
    data: {
      accessToken: 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ6aGFuZ3NhbiIsInVzZXJJZCI6MTIzNDU2LCJpYXQiOjE3MTczMDU2MDAsImV4cCI6MTcxNzMxMjgwMH0.xxx',
      refreshToken: 'dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4gZXhhbXBsZQ',
      tokenType: 'Bearer',
      expiresIn: 7200,
      userInfo: {
        id: 123456,
        username: 'zhangsan',
        nickname: '张三',
        email: 'zhangsan@example.com',
        mobile: '13800138000',
        status: 1,
        createTime: '2026-06-02 10:00:00',
      },
    },
    timestamp: 1717305600000,
  },

  'POST /api/v1/auth/login': {
    code: 100200,
    message: '登录成功',
    data: {
      accessToken: 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ6aGFuZ3NhbiIsInVzZXJJZCI6MTIzNDU2LCJpYXQiOjE3MTczMDU2MDAsImV4cCI6MTcxNzMxMjgwMH0.xxx',
      refreshToken: 'dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4gZXhhbXBsZQ',
      tokenType: 'Bearer',
      expiresIn: 7200,
      userInfo: {
        id: 123456,
        username: 'zhangsan',
        nickname: '张三',
        email: 'zhangsan@example.com',
        mobile: '13800138000',
        status: 1,
        lastLoginTime: '2026-06-02 10:00:00',
        createTime: '2026-05-01 09:00:00',
      },
    },
    timestamp: 1717305600000,
  },

  'POST /api/v1/auth/logout': {
    code: 100200,
    message: '登出成功',
    timestamp: 1717305600000,
  },

  'GET /api/v1/auth/user-info': {
    code: 100200,
    message: '操作成功',
    data: {
      id: 123456,
      username: 'zhangsan',
      nickname: '张三',
      email: 'zhangsan@example.com',
      mobile: '13800138000',
      status: 1,
      roles: ['DATA_ENGINEER'],
      permissions: ['api:read', 'api:write', 'database:read', 'task:read', 'task:write'],
      lastLoginTime: '2026-06-02 10:00:00',
      createTime: '2026-05-01 09:00:00',
    },
    timestamp: 1717305600000,
  },

  'POST /api/v1/auth/refresh-token': {
    code: 100200,
    message: '令牌刷新成功',
    data: {
      accessToken: 'eyJhbGciOiJIUzI1NiJ9.xxx_NEW_TOKEN',
      refreshToken: 'NEW_REFRESH_TOKEN_STRING',
      tokenType: 'Bearer',
      expiresIn: 7200,
    },
    timestamp: 1717305600000,
  },

  'PUT /api/v1/auth/password': {
    code: 100200,
    message: '密码修改成功，请重新登录',
    timestamp: 1717305600000,
  },

  'PUT /api/v1/auth/profile': {
    code: 100200,
    message: '个人信息更新成功',
    data: {
      id: 123456,
      username: 'zhangsan',
      nickname: '张三',
      email: 'zhangsan@example.com',
      mobile: '13900139000',
      status: 1,
      remark: '数据工程师',
      lastLoginTime: '2026-06-03 10:00:00',
      createTime: '2026-05-01 09:00:00',
    },
    timestamp: 1717305600000,
  },
}
import { register } from '@/services/UserAPIServices/UserAPI';
import { history } from '@umijs/max';
import { Button, Card, Form, Input, message } from 'antd';
import React, { useState } from 'react';

const RegisterPage: React.FC = () => {
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (values: {
    username: string;
    password: string;
    confirmPassword: string;
    nickname?: string;
    email?: string;
    mobile?: string;
    remark?: string;
  }) => {
    if (values.password !== values.confirmPassword) {
      message.error('两次输入的密码不一致');
      return;
    }

    setLoading(true);
    try {
      const response = await register({
        username: values.username,
        password: values.password,
        confirmPassword: values.confirmPassword,
        nickname: values.nickname,
        email: values.email,
        mobile: values.mobile,
        remark: values.remark,
      });

      if (response.data) {
        const { accessToken, refreshToken, userInfo } = response.data;

        localStorage.setItem('token', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('userInfo', JSON.stringify(userInfo));

        message.success('注册成功');
        history.push('/data');
      }
    } catch (error) {
      console.error('注册失败:', error);
      message.error('注册失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = () => {
    history.push('/login');
  };

  const passwordValidator = (_: any, value: string) => {
    if (!value) {
      return Promise.resolve();
    }
    const hasUpperCase = /[A-Z]/.test(value);
    const hasLowerCase = /[a-z]/.test(value);
    const hasNumber = /\d/.test(value);
    const hasSpecial = /[@$!%*?&]/.test(value);
    const count =
      (hasUpperCase ? 1 : 0) +
      (hasLowerCase ? 1 : 0) +
      (hasNumber ? 1 : 0) +
      (hasSpecial ? 1 : 0);
    if (count < 3) {
      return Promise.reject(
        new Error('密码需包含大小写字母、数字、特殊字符中的至少三种'),
      );
    }
    return Promise.resolve();
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      }}
    >
      <Card
        style={{ width: 400, boxShadow: '0 10px 40px rgba(0, 0, 0, 0.2)' }}
        title={
          <div
            style={{
              textAlign: 'center',
              fontSize: '24px',
              fontWeight: 'bold',
              color: '#1890ff',
            }}
          >
            数据工厂 - 注册
          </div>
        }
      >
        <Form name="register" onFinish={handleSubmit} layout="vertical">
          <Form.Item
            name="username"
            label="用户名"
            rules={[
              { required: true, message: '请输入用户名' },
              { min: 4, max: 20, message: '用户名长度为4-20字符' },
              {
                pattern: /^[a-zA-Z][a-zA-Z0-9_]{3,19}$/,
                message: '以字母开头，仅支持字母、数字、下划线',
              },
            ]}
          >
            <Input placeholder="请输入用户名" style={{ borderRadius: 8 }} />
          </Form.Item>

          <Form.Item
            name="password"
            label="密码"
            rules={[
              { required: true, message: '请输入密码' },
              { min: 8, max: 32, message: '密码长度为8-32字符' },
              { validator: passwordValidator },
            ]}
          >
            <Input.Password placeholder="请输入密码" style={{ borderRadius: 8 }} />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            label="确认密码"
            rules={[
              { required: true, message: '请确认密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('两次输入的密码不一致'));
                },
              }),
            ]}
          >
            <Input.Password placeholder="请确认密码" style={{ borderRadius: 8 }} />
          </Form.Item>

          <Form.Item
            name="nickname"
            label="昵称"
            rules={[{ min: 2, max: 20, message: '昵称长度为2-20字符' }]}
          >
            <Input placeholder="请输入昵称" style={{ borderRadius: 8 }} />
          </Form.Item>

          <Form.Item
            name="email"
            label="邮箱"
            rules={[
              { type: 'email', message: '请输入有效的邮箱地址' },
              { max: 64, message: '邮箱地址不超过64字符' },
            ]}
          >
            <Input placeholder="请输入邮箱" style={{ borderRadius: 8 }} />
          </Form.Item>

          <Form.Item
            name="mobile"
            label="手机号"
            rules={[
              {
                pattern: /^1[3-9]\d{9}$/,
                message: '请输入有效的11位手机号',
              },
            ]}
          >
            <Input placeholder="请输入手机号" style={{ borderRadius: 8 }} />
          </Form.Item>

          <Form.Item
            name="remark"
            label="备注"
            rules={[{ max: 200, message: '备注不超过200字符' }]}
          >
            <Input.TextArea
              placeholder="请输入备注（最多200字符）"
              rows={3}
              maxLength={200}
              style={{ borderRadius: 8 }}
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              style={{
                width: '100%',
                height: 40,
                borderRadius: 8,
                fontSize: 16,
              }}
            >
              注册
            </Button>
          </Form.Item>

          <div style={{ textAlign: 'center' }}>
            <Button type="link" onClick={handleLogin} style={{ padding: 0 }}>
              已有账号？立即登录
            </Button>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default RegisterPage;

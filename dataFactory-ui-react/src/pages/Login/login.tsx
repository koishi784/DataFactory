import { login } from '@/services/UserAPIServices/UserAPI';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { history } from '@umijs/max';
import { Button, Card, Checkbox, Form, Input, message } from 'antd';
import React, { useState } from 'react';

const LoginPage: React.FC = () => {
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (values: {
    account: string;
    password: string;
    rememberMe: boolean;
  }) => {
    setLoading(true);
    try {
      const response = await login({
        account: values.account, 
        password: values.password,
        rememberMe: values.rememberMe,
      });

      if (response.data) {
        const { accessToken, refreshToken, userInfo } = response.data;
        localStorage.setItem('token', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('userInfo', JSON.stringify(userInfo));

        message.success('登录成功');
        history.push('/data');
      }
    } catch (error) {
      console.error('登录失败:', error);
      message.error('登录失败，请检查账号密码');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = () => {
    history.push('/register');
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
            数据工厂 - 登录
          </div>
        }
      >
        <Form name="login" onFinish={handleSubmit} layout="vertical">
          <Form.Item
            name="account"
            label="账号"
            rules={[{ required: true, message: '请输入账号' }]}
          >
            <Input
              prefix={<UserOutlined style={{ color: 'rgba(0,0,0,.45)' }} />}
              placeholder="用户名/邮箱/手机号"
              style={{ borderRadius: 8 }}
            />
          </Form.Item>

          <Form.Item
            name="password"
            label="密码"
            rules={[
              { required: true, message: '请输入密码' },
              { min: 8, message: '密码至少8位' },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined style={{ color: 'rgba(0,0,0,.45)' }} />}
              placeholder="请输入密码"
              style={{ borderRadius: 8 }}
            />
          </Form.Item>

          <Form.Item
            name="rememberMe"
            valuePropName="checked"
            initialValue={false}
          >
            <Checkbox>记住我</Checkbox>
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
              登录
            </Button>
          </Form.Item>

          <div style={{ textAlign: 'center' }}>
            <Button type="link" onClick={handleRegister} style={{ padding: 0 }}>
              还没有账号？立即注册
            </Button>
          </div>
        </Form>
      </Card>
    </div>
  );
};

export default LoginPage;

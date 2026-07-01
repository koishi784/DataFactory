import { changePassword, updateProfile } from '@/services/UserAPIServices/UserAPI';
import { history } from '@umijs/max';
import {
  Button,
  Card,
  Form,
  Input,
  message,
  Avatar,
  Space,
} from 'antd';
import React, { useEffect, useState } from 'react';
import {
  UserOutlined,
  MailOutlined,
  PhoneOutlined,
  EditOutlined,
  LockOutlined,
  SaveOutlined,
} from '@ant-design/icons';
import logo from '@/assets/logo.jpg';

interface UserInfo {
  userId?: number;
  username?: string;
  nickname?: string;
  email?: string;
  mobile?: string;
  remark?: string;
  avatar?: string;
  [key: string]: any;
}

const UserManagePage: React.FC = () => {
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const [passwordLoading, setPasswordLoading] = useState(false);
  const [profileLoading, setProfileLoading] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [form] = Form.useForm();
  const [passwordForm] = Form.useForm();

  useEffect(() => {
    const storedUserInfo = localStorage.getItem('userInfo');
    if (storedUserInfo) {
      try {
        const parsed = JSON.parse(storedUserInfo);
        setUserInfo(parsed);
        form.setFieldsValue({
          nickname: parsed.nickname,
          email: parsed.email,
          mobile: parsed.mobile,
          remark: parsed.remark,
        });
      } catch (error) {
        console.error('解析用户信息失败:', error);
      }
    }
  }, [form]);

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

  const handleChangePassword = async (values: {
    oldPassword: string;
    newPassword: string;
    confirmPassword: string;
  }) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error('两次输入的密码不一致');
      return;
    }

    setPasswordLoading(true);
    try {
      await changePassword({
        oldPassword: values.oldPassword,
        newPassword: values.newPassword,
        confirmPassword: values.confirmPassword,
      });
      message.success('密码修改成功');
      passwordForm.resetFields();
    } catch (error) {
      console.error('修改密码失败:', error);
    } finally {
      setPasswordLoading(false);
    }
  };

  const handleUpdateProfile = async (values: {
    nickname?: string;
    email?: string;
    mobile?: string;
    remark?: string;
  }) => {
    setProfileLoading(true);
    try {
      const response = await updateProfile(values);
      if (response.data) {
        localStorage.setItem('userInfo', JSON.stringify(response.data));
        setUserInfo(response.data);
        setIsEditing(false);
        message.success('个人信息修改成功');
      }
    } catch (error) {
      console.error('修改个人信息失败:', error);
    } finally {
      setProfileLoading(false);
    }
  };

  const handleCancelEdit = () => {
    form.setFieldsValue({
      nickname: userInfo?.nickname,
      email: userInfo?.email,
      mobile: userInfo?.mobile,
      remark: userInfo?.remark,
    });
    setIsEditing(false);
  };

  const handleBack = () => {
    history.push('/data');
  };

  return (
    <div style={{ minHeight: '100vh', background: '#f5f5f5', padding: '16px 24px' }}>
      <Button
        type="text"
        onClick={handleBack}
        style={{ marginBottom: 16, color: '#666' }}
      >
        返回
      </Button>

      <div style={{ display: 'grid', gridTemplateColumns: '240px 1fr', gap: 16 }}>
        <Card
          style={{
            borderRadius: 12,
            textAlign: 'center',
            padding: '40px 24px',
            height: 'fit-content',
          }}
          styles={{ body: { padding: 0 } }}
        >
          <Avatar
            size={96}
            src={userInfo?.avatar || logo}
            icon={<UserOutlined />}
            style={{ marginBottom: 16 }}
          />
          <h3 style={{ margin: '0 0 6px', fontSize: 20, fontWeight: 600 }}>
            {userInfo?.nickname || userInfo?.username || '用户'}
          </h3>
          <p style={{ margin: 0, color: '#999', fontSize: 14 }}>
            @{userInfo?.username || 'username'}
          </p>

          <div style={{ marginTop: 32, textAlign: 'left', borderTop: '1px solid #f0f0f0', paddingTop: 24 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 16 }}>
              <MailOutlined style={{ color: '#1890ff' }} />
              <span style={{ color: '#666', fontSize: 14 }}>
                {userInfo?.email || '未设置邮箱'}
              </span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <PhoneOutlined style={{ color: '#1890ff' }} />
              <span style={{ color: '#666', fontSize: 14 }}>
                {userInfo?.mobile || '未设置手机号'}
              </span>
            </div>
          </div>
        </Card>

        <Card style={{ borderRadius: 12 }} styles={{ body: { padding: '24px 28px' } }}>
          <div style={{ marginBottom: 24 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
              <div>
                <h3 style={{ margin: 0, fontSize: 16, fontWeight: 600 }}>个人信息</h3>
                <p style={{ margin: '4px 0 0', color: '#999', fontSize: 13 }}>管理您的账户信息</p>
              </div>
              {!isEditing ? (
                <Button
                  icon={<EditOutlined />}
                  onClick={() => setIsEditing(true)}
                  style={{ borderRadius: 6 }}
                >
                  编辑
                </Button>
              ) : (
                <Space>
                  <Button onClick={handleCancelEdit} style={{ borderRadius: 6 }}>取消</Button>
                  <Button
                    type="primary"
                    icon={<SaveOutlined />}
                    loading={profileLoading}
                    onClick={() => form.submit()}
                    style={{ borderRadius: 6 }}
                  >
                    保存
                  </Button>
                </Space>
              )}
            </div>

            <Form
              form={form}
              name="updateProfile"
              onFinish={handleUpdateProfile}
              layout="vertical"
              disabled={!isEditing}
            >
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                <Form.Item
                  name="nickname"
                  label="昵称"
                  rules={[{ min: 2, max: 20, message: '昵称长度为2-20字符' }]}
                >
                  <Input placeholder="请输入昵称" style={{ borderRadius: 6, height: 36 }} />
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
                  <Input placeholder="请输入手机号" style={{ borderRadius: 6, height: 36 }} />
                </Form.Item>
              </div>
              <Form.Item
                name="email"
                label="邮箱"
                rules={[
                  { type: 'email', message: '请输入有效的邮箱地址' },
                  { max: 64, message: '邮箱地址不超过64字符' },
                ]}
              >
                <Input placeholder="请输入邮箱" style={{ borderRadius: 6, height: 36 }} />
              </Form.Item>
              <Form.Item
                name="remark"
                label="个人简介"
                rules={[{ max: 200, message: '简介不超过200字符' }]}
              >
                <Input.TextArea
                  placeholder="介绍一下自己..."
                  rows={2}
                  maxLength={200}
                  style={{ borderRadius: 6 }}
                />
              </Form.Item>
            </Form>
          </div>

          <div style={{ borderTop: '1px solid #f0f0f0', margin: '0 -28px', padding: '0 28px' }}>
            <div style={{ marginTop: 24, marginBottom: 20 }}>
              <h3 style={{ margin: 0, fontSize: 16, fontWeight: 600 }}>
                <LockOutlined style={{ marginRight: 8, color: '#1890ff' }} />
                修改密码
              </h3>
              <p style={{ margin: '4px 0 0', color: '#999', fontSize: 13 }}>定期更换密码保护账户安全</p>
            </div>

            <Form
              form={passwordForm}
              name="changePassword"
              onFinish={handleChangePassword}
              layout="vertical"
              style={{ maxWidth: 360 }}
            >
              <Form.Item
                name="oldPassword"
                label="原密码"
                rules={[{ required: true, message: '请输入原密码' }]}
              >
                <Input.Password placeholder="请输入原密码" style={{ borderRadius: 6, height: 36 }} />
              </Form.Item>
              <Form.Item
                name="newPassword"
                label="新密码"
                rules={[
                  { required: true, message: '请输入新密码' },
                  { min: 8, max: 32, message: '密码长度为8-32字符' },
                  { validator: passwordValidator },
                ]}
              >
                <Input.Password placeholder="请输入新密码" style={{ borderRadius: 6, height: 36 }} />
              </Form.Item>
              <Form.Item
                name="confirmPassword"
                label="确认新密码"
                rules={[
                  { required: true, message: '请确认新密码' },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      if (!value || getFieldValue('newPassword') === value) {
                        return Promise.resolve();
                      }
                      return Promise.reject(new Error('两次输入的密码不一致'));
                    },
                  }),
                ]}
              >
                <Input.Password placeholder="请确认新密码" style={{ borderRadius: 6, height: 36 }} />
              </Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                loading={passwordLoading}
                style={{ borderRadius: 6, marginTop: 4, height: 36, minWidth: 100 }}
              >
                修改密码
              </Button>
            </Form>
          </div>
        </Card>
      </div>
    </div>
  );
};

export default UserManagePage;

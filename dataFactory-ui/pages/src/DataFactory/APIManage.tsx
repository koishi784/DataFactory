import { PlusOutlined } from '@ant-design/icons';
import { Button, Card, Input, Space, Table, Tag } from 'antd';
import React from 'react';

const APIManage: React.FC = () => {
  const columns = [
    {
      title: 'API名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'API路径',
      dataIndex: 'path',
      key: 'path',
    },
    {
      title: '请求方法',
      dataIndex: 'method',
      key: 'method',
      render: (method: string) => {
        const colorMap: Record<string, string> = {
          GET: 'green',
          POST: 'blue',
          PUT: 'orange',
          DELETE: 'red',
        };
        return <Tag color={colorMap[method]}>{method}</Tag>;
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => (
        <Tag color={status === 1 ? 'success' : 'default'}>
          {status === 1 ? '启用' : '禁用'}
        </Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
    },
    {
      title: '操作',
      key: 'action',
      render: () => (
        <Space size="small">
          <Button type="link" size="small">
            编辑
          </Button>
          <Button type="link" size="small" danger>
            删除
          </Button>
        </Space>
      ),
    },
  ];

  const data = [
    {
      key: '1',
      name: '获取用户列表',
      path: '/api/v1/users',
      method: 'GET',
      status: 1,
      createTime: '2026-01-01 10:00:00',
    },
    {
      key: '2',
      name: '创建用户',
      path: '/api/v1/users',
      method: 'POST',
      status: 1,
      createTime: '2026-01-02 10:00:00',
    },
  ];

  return (
    <div>
      <Card
        title="接口管理"
        extra={
          <Space>
            <Input placeholder="搜索接口" style={{ width: 200 }} />
            <Button type="primary" icon={<PlusOutlined />}>
              新建接口
            </Button>
          </Space>
        }
      >
        <Table columns={columns} dataSource={data} />
      </Card>
    </div>
  );
};

export default APIManage;

import { PlusOutlined } from '@ant-design/icons';
import { Button, Card, Space, Table, Tag } from 'antd';
import React from 'react';

const CodeTable: React.FC = () => {
  const columns = [
    {
      title: '码表编码',
      dataIndex: 'code',
      key: 'code',
    },
    {
      title: '码表名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '码表类型',
      dataIndex: 'type',
      key: 'type',
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
      title: '操作',
      key: 'action',
      render: () => (
        <Space size="small">
          <Button type="link" size="small">
            编辑
          </Button>
          <Button type="link" size="small">
            查看详情
          </Button>
        </Space>
      ),
    },
  ];

  const data = [
    {
      key: '1',
      code: 'GENDER',
      name: '性别码表',
      type: '系统码表',
      status: 1,
    },
  ];

  return (
    <div>
      <Card
        title="码表管理"
        extra={
          <Button type="primary" icon={<PlusOutlined />}>
            新建码表
          </Button>
        }
      >
        <Table columns={columns} dataSource={data} />
      </Card>
    </div>
  );
};

export default CodeTable;

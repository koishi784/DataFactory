import { PlusOutlined } from '@ant-design/icons';
import { Button, Card, Space, Table, Tag } from 'antd';
import React from 'react';

const DataAssetManage: React.FC = () => {
  const columns = [
    {
      title: '资产名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '资产类型',
      dataIndex: 'type',
      key: 'type',
    },
    {
      title: '所属部门',
      dataIndex: 'department',
      key: 'department',
    },
    {
      title: '数据量',
      dataIndex: 'size',
      key: 'size',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => (
        <Tag color={status === 1 ? 'success' : 'default'}>
          {status === 1 ? '已发布' : '草稿'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: () => (
        <Space size="small">
          <Button type="link" size="small">
            查看
          </Button>
          <Button type="link" size="small">
            编辑
          </Button>
        </Space>
      ),
    },
  ];

  const data = [
    {
      key: '1',
      name: '客户信息表',
      type: '数据表',
      department: '市场部',
      size: '1.2GB',
      status: 1,
    },
  ];

  return (
    <div>
      <Card
        title="数据资产管理"
        extra={
          <Button type="primary" icon={<PlusOutlined />}>
            新建资产
          </Button>
        }
      >
        <Table columns={columns} dataSource={data} />
      </Card>
    </div>
  );
};

export default DataAssetManage;

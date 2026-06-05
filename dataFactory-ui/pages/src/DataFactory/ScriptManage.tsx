import { PlusOutlined } from '@ant-design/icons';
import { Button, Card, Space, Table, Tag } from 'antd';
import React from 'react';

const ScriptManage: React.FC = () => {
  const columns = [
    {
      title: '脚本名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '脚本类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => <Tag color="purple">{type}</Tag>,
    },
    {
      title: '语言',
      dataIndex: 'language',
      key: 'language',
    },
    {
      title: '创建人',
      dataIndex: 'creator',
      key: 'creator',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
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
      name: '数据清洗脚本',
      type: 'ETL',
      language: 'Python',
      creator: 'admin',
      createTime: '2026-01-01 10:00:00',
      status: 1,
    },
  ];

  return (
    <div>
      <Card
        title="脚本管理"
        extra={
          <Button type="primary" icon={<PlusOutlined />}>
            新建脚本
          </Button>
        }
      >
        <Table columns={columns} dataSource={data} />
      </Card>
    </div>
  );
};

export default ScriptManage;

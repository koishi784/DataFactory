import { PlusOutlined } from '@ant-design/icons';
import { Button, Card, Space, Table, Tag } from 'antd';
import React from 'react';

const DataManage: React.FC = () => {
  const columns = [
    {
      title: '数据库名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '数据库类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => <Tag color="blue">{type}</Tag>,
    },
    {
      title: '主机地址',
      dataIndex: 'host',
      key: 'host',
    },
    {
      title: '端口',
      dataIndex: 'port',
      key: 'port',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => (
        <Tag color={status === 1 ? 'success' : 'default'}>
          {status === 1 ? '正常' : '异常'}
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
      name: '主数据库',
      type: 'MySQL',
      host: 'localhost',
      port: 3306,
      status: 1,
    },
  ];

  return (
    <div>
      <Card
        title="数据库管理"
        extra={
          <Button type="primary" icon={<PlusOutlined />}>
            新建数据库
          </Button>
        }
      >
        <Table columns={columns} dataSource={data} />
      </Card>
    </div>
  );
};

export default DataManage;

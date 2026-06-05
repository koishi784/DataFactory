import { PlusOutlined } from '@ant-design/icons';
import { Badge, Button, Card, Space, Table } from 'antd';
import React from 'react';

const TaskManage: React.FC = () => {
  const columns = [
    {
      title: '任务名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '任务类型',
      dataIndex: 'type',
      key: 'type',
    },
    {
      title: '执行周期',
      dataIndex: 'cycle',
      key: 'cycle',
    },
    {
      title: '最近执行时间',
      dataIndex: 'lastRunTime',
      key: 'lastRunTime',
    },
    {
      title: '执行状态',
      dataIndex: 'runStatus',
      key: 'runStatus',
      render: (status: number) => {
        const statusMap = [
          { color: 'default', text: '未执行' },
          { color: 'processing', text: '执行中' },
          { color: 'success', text: '成功' },
          { color: 'error', text: '失败' },
        ];
        return (
          <Badge
            status={statusMap[status].color as any}
            text={statusMap[status].text}
          />
        );
      },
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
      name: '每日数据同步',
      type: '数据同步',
      cycle: '每天 00:00',
      lastRunTime: '2026-01-01 00:00:00',
      runStatus: 2,
    },
  ];

  return (
    <div>
      <Card
        title="任务管理"
        extra={
          <Button type="primary" icon={<PlusOutlined />}>
            新建任务
          </Button>
        }
      >
        <Table columns={columns} dataSource={data} />
      </Card>
    </div>
  );
};

export default TaskManage;

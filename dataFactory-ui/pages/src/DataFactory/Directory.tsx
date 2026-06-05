import { PlusOutlined } from '@ant-design/icons';
import { Button, Card, Space, Tree } from 'antd';
import React from 'react';

const Directory: React.FC = () => {
  const treeData = [
    {
      title: '数据标准目录',
      key: '0',
      children: [
        {
          title: '基础数据标准',
          key: '0-0',
          children: [
            { title: '人员信息标准', key: '0-0-0' },
            { title: '组织机构标准', key: '0-0-1' },
          ],
        },
        {
          title: '业务数据标准',
          key: '0-1',
          children: [{ title: '交易数据标准', key: '0-1-0' }],
        },
      ],
    },
  ];

  return (
    <div>
      <Card
        title="数据标准目录"
        extra={
          <Space>
            <Button type="primary" icon={<PlusOutlined />}>
              新建目录
            </Button>
          </Space>
        }
      >
        <Tree showLine defaultExpandAll treeData={treeData} />
      </Card>
    </div>
  );
};

export default Directory;

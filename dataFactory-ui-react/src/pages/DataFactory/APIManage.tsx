import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Space, Input, Select, Tag, Tree, Modal, Form, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, PlayCircleOutlined, PoweroffOutlined, MoreOutlined } from '@ant-design/icons';
import { Dropdown, Menu } from 'antd';
import { getCategoryTree, getApiList, publishApi, disableApi, deleteApi, testApi } from '@/services/SourceAPIServices/sourceAPI';

interface CategoryNode {
  id: number;
  name: string;
  parentId: number;
  level: number;
  sortOrder: number;
  children?: CategoryNode[];
  createTime: string;
}

interface ApiItem {
  id: number;
  apiName: string;
  apiDescription: string;
  apiCategory: string;
  categoryId: number;
  source: string;
  protocol: string;
  method: string;
  url: string;
  status: number;
  timeout: number;
  retryCount: number;
  version: string;
  createTime: string;
  updateTime: string;
  createBy: string;
  updateBy: string;
}

const APIManage: React.FC = () => {
  const [categoryTree, setCategoryTree] = useState<CategoryNode[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [apiList, setApiList] = useState<ApiItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<number[]>([]);
  const [searchParams, setSearchParams] = useState({
    keyword: '',
    status: '',
    source: '',
  });
  const [testModalVisible, setTestModalVisible] = useState(false);
  const [currentApiId, setCurrentApiId] = useState<number | null>(null);
  const [testResult, setTestResult] = useState<{ success: boolean; statusCode: number; responseTime: number; responseBody: string; errorMessage?: string } | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    loadCategories();
    loadApiList();
  }, []);

  const loadCategories = async () => {
    try {
      const res = await getCategoryTree();
      setCategoryTree(res.data);
    } catch (error) {
      console.error('Failed to load categories:', error);
    }
  };

  const loadApiList = async (params?: { categoryId?: number; keyword?: string; status?: string; source?: string }) => {
    setLoading(true);
    try {
      const res = await getApiList({
        pageNum: 1,
        pageSize: 100,
        categoryId: params?.categoryId ?? selectedCategory ?? undefined,
        keyword: params?.keyword ?? searchParams.keyword,
        status: params?.status ?? searchParams.status,
        source: params?.source ?? searchParams.source,
      });
      setApiList(res.data.records);
    } catch (error) {
      console.error('Failed to load API list:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCategorySelect = (selectedKeys: React.Key[]) => {
    const id = selectedKeys.length > 0 ? Number(selectedKeys[0]) : undefined;
    setSelectedCategory(id ?? null);
    loadApiList({ categoryId: id });
  };

  const handleSearch = () => {
    loadApiList();
  };

  const handleReset = () => {
    setSearchParams({ keyword: '', status: '', source: '' });
    form.resetFields();
    loadApiList();
  };

  const handlePublish = async (id: number) => {
    try {
      await publishApi(id);
      message.success('发布成功');
      loadApiList();
    } catch (error) {
      message.error('发布失败');
    }
  };

  const handleDisable = async (id: number) => {
    try {
      await disableApi(id);
      message.success('停用成功');
      loadApiList();
    } catch (error) {
      message.error('停用失败');
    }
  };

  const handleDelete = async (id: number) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该接口吗？',
      onOk: async () => {
        try {
          await deleteApi(id);
          message.success('删除成功');
          loadApiList();
        } catch (error) {
          message.error('删除失败');
        }
      },
    });
  };

  const handleTest = (id: number) => {
    setCurrentApiId(id);
    setTestModalVisible(true);
    setTestResult(null);
  };

  const handleExecuteTest = async () => {
    if (!currentApiId) return;
    try {
      const res = await testApi(currentApiId, {});
      setTestResult(res.data);
    } catch (error) {
      console.error('Test failed:', error);
    }
  };

  const handleBatchPublish = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要发布的接口');
      return;
    }
    Modal.confirm({
      title: '批量发布',
      content: `确定要发布选中的 ${selectedRowKeys.length} 个接口吗？`,
      onOk: async () => {
        try {
          await publishApi(selectedRowKeys[0]);
          message.success('批量发布成功');
          loadApiList();
          setSelectedRowKeys([]);
        } catch (error) {
          message.error('批量发布失败');
        }
      },
    });
  };

  const handleBatchDisable = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要停用的接口');
      return;
    }
    Modal.confirm({
      title: '批量停用',
      content: `确定要停用选中的 ${selectedRowKeys.length} 个接口吗？`,
      onOk: async () => {
        try {
          await disableApi(selectedRowKeys[0]);
          message.success('批量停用成功');
          loadApiList();
          setSelectedRowKeys([]);
        } catch (error) {
          message.error('批量停用失败');
        }
      },
    });
  };

  const handleBatchCategory = () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要修改分类的接口');
      return;
    }
    Modal.info({
      title: '批量分类',
      content: '批量修改分类功能开发中',
    });
  };

  const treeData = categoryTree.map(node => ({
    title: node.name,
    key: String(node.id),
    children: node.children?.map(child => ({
      title: child.name,
      key: String(child.id),
      children: child.children?.map(grandchild => ({
        title: grandchild.name,
        key: String(grandchild.id),
      })),
    })),
  }));

  const columns = [
    {
      title: '接口名称',
      dataIndex: 'apiName',
      key: 'apiName',
    },
    {
      title: '接口描述',
      dataIndex: 'apiDescription',
      key: 'apiDescription',
      ellipsis: true,
    },
    {
      title: '接口分类',
      dataIndex: 'apiCategory',
      key: 'apiCategory',
    },
    {
      title: '接口来源',
      dataIndex: 'source',
      key: 'source',
    },
    {
      title: 'API状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: number) => {
        const statusMap: Record<number, { label: string; color: string }> = {
          0: { label: '未发布', color: 'default' },
          1: { label: '已发布', color: 'success' },
          2: { label: '已停用', color: 'warning' },
        };
        const item = statusMap[status];
        return <Tag color={item.color}>{item.label}</Tag>;
      },
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      align: 'center',
      render: (_: any, record: ApiItem) => {
        const menu = (
          <Menu>
            {record.status === 0 && (
              <Menu.Item key="publish" onClick={() => handlePublish(record.id)}>
                发布
              </Menu.Item>
            )}
            {record.status === 1 && (
              <Menu.Item key="disable" onClick={() => handleDisable(record.id)}>
                停用
              </Menu.Item>
            )}
            {record.status === 2 && (
              <Menu.Item key="publish" onClick={() => handlePublish(record.id)}>
                发布
              </Menu.Item>
            )}
            <Menu.Item key="test" onClick={() => handleTest(record.id)}>
              接口测试
            </Menu.Item>
            {record.status !== 1 && (
              <Menu.Item key="edit">
                编辑
              </Menu.Item>
            )}
            {record.status === 0 && (
              <Menu.Item key="delete" danger onClick={() => handleDelete(record.id)}>
                删除
              </Menu.Item>
            )}
          </Menu>
        );
        return (
          <Dropdown overlay={menu} trigger={['click']}>
            <Button type="link" size="small" icon={<MoreOutlined />}>
              更多
            </Button>
          </Dropdown>
        );
      },
    },
  ] as any;

  const rowSelection = {
    selectedRowKeys,
    onChange: (keys: React.Key[]) => setSelectedRowKeys(keys as number[]),
  };

  return (
    <div style={{ display: 'flex', height: 'calc(100vh - 64px)' }}>
      <Card style={{ width: 240, margin: 16, borderRadius: 12 }} bodyStyle={{ padding: 0 }}>
        <div style={{ padding: 16, borderBottom: '1px solid #f0f0f0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ fontWeight: 600 }}>接口分类</span>
          <Button type="link" size="small" icon={<PlusOutlined />} />
        </div>
        <Tree
          showLine
          defaultExpandAll
          onSelect={handleCategorySelect}
          selectedKeys={selectedCategory ? [String(selectedCategory)] : []}
          treeData={treeData}
          style={{ padding: 8 }}
        />
      </Card>

      <div style={{ flex: 1, margin: 16, display: 'flex', flexDirection: 'column' }}>
        <Card style={{ borderRadius: 12, marginBottom: 16 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <div style={{ display: 'flex', gap: 16 }}>
              <Select
                placeholder="接口来源"
                style={{ width: 150 }}
                value={searchParams.source}
                onChange={(value) => setSearchParams({ ...searchParams, source: value })}
              >
                <Select.Option value="数据服务">数据服务</Select.Option>
                <Select.Option value="用户中心">用户中心</Select.Option>
                <Select.Option value="订单系统">订单系统</Select.Option>
                <Select.Option value="支付平台">支付平台</Select.Option>
              </Select>
              <Select
                placeholder="API状态"
                style={{ width: 120 }}
                value={searchParams.status}
                onChange={(value) => setSearchParams({ ...searchParams, status: value })}
              >
                <Select.Option value="0">未发布</Select.Option>
                <Select.Option value="1">已发布</Select.Option>
                <Select.Option value="2">已停用</Select.Option>
              </Select>
              <Input
                placeholder="接口名称"
                style={{ width: 200 }}
                value={searchParams.keyword}
                onChange={(e) => setSearchParams({ ...searchParams, keyword: e.target.value })}
              />
              <Button onClick={handleReset}>重置</Button>
              <Button type="primary" onClick={handleSearch}>查询</Button>
            </div>
            <Button type="primary" icon={<PlusOutlined />} href="/data/source/APIManualRegistration">
              人工注册
            </Button>
          </div>

          <div style={{ display: 'flex', gap: 12, marginBottom: 16 }}>
            <Button icon={<PlayCircleOutlined />} onClick={handleBatchPublish} disabled={selectedRowKeys.length === 0}>
              批量发布
            </Button>
            <Button icon={<PoweroffOutlined />} onClick={handleBatchDisable} disabled={selectedRowKeys.length === 0}>
              批量停用
            </Button>
            <Button onClick={handleBatchCategory} disabled={selectedRowKeys.length === 0}>
              批量分类
            </Button>
          </div>

          <Table
            rowSelection={rowSelection}
            columns={columns}
            dataSource={apiList}
            loading={loading}
            pagination={{ pageSize: 20 }}
            rowKey="id"
          />
        </Card>

      </div>

      <Modal
        title="接口测试"
        visible={testModalVisible}
        onCancel={() => setTestModalVisible(false)}
        footer={[
          <Button key="execute" type="primary" onClick={handleExecuteTest}>
            执行测试
          </Button>,
          <Button key="close" onClick={() => setTestModalVisible(false)}>
            关闭
          </Button>,
        ]}
        width={700}
      >
        <Form form={form} layout="vertical">
          <Form.Item label="测试参数">
            <Input.TextArea rows={4} placeholder="输入测试参数（JSON格式）" />
          </Form.Item>
          {testResult && (
            <div style={{ marginTop: 16 }}>
              <div style={{ marginBottom: 8 }}>
                <span style={{ fontWeight: 600 }}>测试结果：</span>
                <Tag color={testResult.success ? 'success' : 'error'}>
                  {testResult.success ? '成功' : '失败'}
                </Tag>
              </div>
              {testResult.success ? (
                <div>
                  <p>状态码：{testResult.statusCode}</p>
                  <p>响应耗时：{testResult.responseTime}ms</p>
                  <p>响应内容：</p>
                  <pre style={{ backgroundColor: '#f5f5f5', padding: 12, borderRadius: 8, maxHeight: 200, overflow: 'auto' }}>
                    {testResult.responseBody}
                  </pre>
                </div>
              ) : (
                <p style={{ color: '#ff4d4f' }}>错误信息：{testResult.errorMessage}</p>
              )}
            </div>
          )}
        </Form>
      </Modal>
    </div>
  );
};

export default APIManage;
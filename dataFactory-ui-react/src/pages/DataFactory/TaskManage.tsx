import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate } from 'umi';
import {
  Card,
  Table,
  Button,
  Space,
  Input,
  Select,
  Tag,
  Tree,
  Modal,
  Form,
  message,
  Dropdown,
  Menu,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  PoweroffOutlined,
  MoreOutlined,
  SearchOutlined,
  ExperimentOutlined,
} from '@ant-design/icons';
import {
  getTaskCategoryTree,
  getTaskList,
  publishTask,
  disableTask,
  deleteTask,
  batchPublishTasks,
  batchDisableTasks,
  testRunTask,
  createTaskCategory,
  updateTaskCategory,
  deleteTaskCategory,
  type TaskItem,
  type TaskListParams,
  type TaskCategoryTreeNode,
  type TestRunTaskResult,
} from '@/services/TaskManage/taskManageAPI';

const STATUS_MAP: Record<number, { label: string; color: string }> = {
  0: { label: '未发布', color: 'default' },
  1: { label: '已发布', color: 'success' },
  2: { label: '已停用', color: 'warning' },
};

const EXECUTE_STATUS_MAP: Record<number, { label: string; color: string }> = {
  0: { label: '等待', color: 'default' },
  1: { label: '执行中', color: 'processing' },
  2: { label: '成功', color: 'success' },
  3: { label: '失败', color: 'error' },
  4: { label: '已取消', color: 'warning' },
};

const TASK_NAME_REGEX = /^[\u4e00-\u9fa5a-zA-Z]+$/;

interface CategoryFormValues {
  name: string;
  parentId?: number;
  sortOrder?: number;
}

const TaskManage: React.FC = () => {
  const navigate = useNavigate();
  const [categoryTree, setCategoryTree] = useState<TaskCategoryTreeNode[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [taskList, setTaskList] = useState<TaskItem[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<number[]>([]);
  const [searchParams, setSearchParams] = useState<TaskListParams>({
    pageNum: 1,
    pageSize: 20,
    keyword: '',
    status: '',
  });
  const [categorySearchText, setCategorySearchText] = useState('');

  const [testModalVisible, setTestModalVisible] = useState(false);
  const [testTaskId, setTestTaskId] = useState<number | null>(null);
  const [testResult, setTestResult] = useState<TestRunTaskResult | null>(null);
  const [testLoading, setTestLoading] = useState(false);

  const [categoryModalVisible, setCategoryModalVisible] = useState(false);
  const [categoryModalMode, setCategoryModalMode] = useState<'add' | 'edit'>('add');
  const [editingCategory, setEditingCategory] = useState<TaskCategoryTreeNode | null>(null);
  const [categoryParentId, setCategoryParentId] = useState<number | undefined>(undefined);
  const [categoryForm] = Form.useForm<CategoryFormValues>();

  const fetchCategoryTree = useCallback(async () => {
    try {
      const res = await getTaskCategoryTree();
      setCategoryTree(res.data);
    } catch (error) {
      console.error('查询分类树失败:', error);
      message.error('查询分类树失败');
    }
  }, []);

  const fetchTaskList = useCallback(async (params?: TaskListParams) => {
    setLoading(true);
    try {
      const res = await getTaskList(params);
      const records = res.data.records || [];
      const sortedRecords = [...records].sort((a, b) => {
        if (a.status !== b.status) {
          return a.status - b.status;
        }
        return new Date(b.updateTime).getTime() - new Date(a.updateTime).getTime();
      });
      setTaskList(sortedRecords);
      setTotal(res.data.total || 0);
    } catch (error) {
      console.error('查询任务列表失败:', error);
      message.error('查询任务列表失败');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCategoryTree();
    fetchTaskList();
  }, [fetchCategoryTree, fetchTaskList]);

  const refreshList = useCallback(() => {
    fetchTaskList({
      pageNum: searchParams.pageNum,
      pageSize: searchParams.pageSize,
      categoryId: selectedCategory ?? undefined,
      keyword: searchParams.keyword || undefined,
      status: searchParams.status || undefined,
    });
  }, [fetchTaskList, searchParams, selectedCategory]);

  const handleCategorySelect = (selectedKeys: React.Key[]) => {
    const id = selectedKeys.length > 0 ? Number(selectedKeys[0]) : null;
    setSelectedCategory(id);
    fetchTaskList({
      pageNum: 1,
      pageSize: searchParams.pageSize,
      categoryId: id ?? undefined,
      keyword: searchParams.keyword || undefined,
      status: searchParams.status || undefined,
    });
  };

  const handleSearch = () => {
    fetchTaskList({
      pageNum: 1,
      pageSize: searchParams.pageSize,
      categoryId: selectedCategory ?? undefined,
      keyword: searchParams.keyword || undefined,
      status: searchParams.status || undefined,
    });
  };

  const handleReset = () => {
    setSearchParams({
      pageNum: 1,
      pageSize: 20,
      keyword: '',
      status: '',
    });
    setSelectedCategory(null);
    fetchTaskList({
      pageNum: 1,
      pageSize: 20,
    });
  };

  const handleTableChange = (pagination: { current?: number; pageSize?: number }) => {
    setSearchParams((prev) => ({
      ...prev,
      pageNum: pagination.current || 1,
      pageSize: pagination.pageSize || 20,
    }));
    fetchTaskList({
      pageNum: pagination.current || 1,
      pageSize: pagination.pageSize || 20,
      categoryId: selectedCategory ?? undefined,
      keyword: searchParams.keyword || undefined,
      status: searchParams.status || undefined,
    });
  };

  const getCategoryPath = (categoryId: number): string => {
    const findPath = (
      nodes: TaskCategoryTreeNode[],
      targetId: number,
      path: string[],
    ): string[] | null => {
      for (const node of nodes) {
        const currentPath = [...path, node.name];
        if (node.id === targetId) return currentPath;
        if (node.children && node.children.length > 0) {
          const found = findPath(node.children, targetId, currentPath);
          if (found) return found;
        }
      }
      return null;
    };
    const path = findPath(categoryTree, categoryId, []);
    return path ? path.join('-') : '-';
  };

  const handlePublish = (id: number) => {
    Modal.confirm({
      title: '确认发布',
      content: '确定要发布该任务吗？',
      onOk: async () => {
        try {
          await publishTask(id);
          message.success('发布成功');
          refreshList();
        } catch (error) {
          message.error('发布失败');
        }
      },
    });
  };

  const handleDisable = (id: number) => {
    Modal.confirm({
      title: '确认停用',
      content: '确定要停用该任务吗？',
      onOk: async () => {
        try {
          await disableTask(id);
          message.success('停用成功');
          refreshList();
        } catch (error) {
          message.error('停用失败');
        }
      },
    });
  };

  const handleDelete = (id: number) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该任务吗？',
      onOk: async () => {
        try {
          await deleteTask(id);
          message.success('删除成功');
          refreshList();
        } catch (error) {
          message.error('删除失败');
        }
      },
    });
  };

  const handleEdit = (record: TaskItem) => {
    message.info('任务编辑功能开发中');
  };

  const handleTestRun = (id: number) => {
    setTestTaskId(id);
    setTestResult(null);
    setTestModalVisible(true);
  };

  const handleExecuteTest = async () => {
    if (!testTaskId) return;
    setTestLoading(true);
    try {
      const res = await testRunTask(testTaskId, {});
      setTestResult(res.data);
    } catch (error) {
      console.error('测试运行失败:', error);
      message.error('测试运行失败');
    } finally {
      setTestLoading(false);
    }
  };

  const handleBatchPublish = () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要发布的任务');
      return;
    }
    const hasPublished = taskList
      .filter((item) => selectedRowKeys.includes(item.id))
      .some((item) => item.status === 1);
    if (hasPublished) {
      message.error('所选任务中包含已发布状态的数据，操作不合法');
      return;
    }
    Modal.confirm({
      title: '批量发布',
      content: `确定要发布选中的 ${selectedRowKeys.length} 个任务吗？`,
      onOk: async () => {
        try {
          await batchPublishTasks({ ids: selectedRowKeys });
          message.success('批量发布成功');
          setSelectedRowKeys([]);
          refreshList();
        } catch (error) {
          message.error('批量发布失败');
        }
      },
    });
  };

  const handleBatchDisable = () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要停用的任务');
      return;
    }
    const hasInvalid = taskList
      .filter((item) => selectedRowKeys.includes(item.id))
      .some((item) => item.status !== 1);
    if (hasInvalid) {
      message.error('所选任务中包含未发布或已停用状态的数据，操作不合法');
      return;
    }
    Modal.confirm({
      title: '批量停用',
      content: `确定要停用选中的 ${selectedRowKeys.length} 个任务吗？`,
      onOk: async () => {
        try {
          await batchDisableTasks({ ids: selectedRowKeys });
          message.success('批量停用成功');
          setSelectedRowKeys([]);
          refreshList();
        } catch (error) {
          message.error('批量停用失败');
        }
      },
    });
  };

  const buildTreeData = (nodes: TaskCategoryTreeNode[]): any[] => {
    return nodes.map((node) => {
      const hasChildren = node.children && node.children.length > 0;
      return {
        title: <span>{node.name}</span>,
        key: String(node.id),
        children: hasChildren ? buildTreeData(node.children) : undefined,
        isLeaf: !hasChildren,
      };
    });
  };

  const filterTreeData = (nodes: TaskCategoryTreeNode[], searchText: string): any[] => {
    if (!searchText.trim()) {
      return buildTreeData(nodes);
    }
    const filtered: any[] = [];
    nodes.forEach((node) => {
      const hasChildren = node.children && node.children.length > 0;
      const childResults = hasChildren ? filterTreeData(node.children, searchText) : [];
      const matchesSearch = node.name.toLowerCase().includes(searchText.toLowerCase());
      if (matchesSearch || childResults.length > 0) {
        filtered.push({
          title: <span>{node.name}</span>,
          key: String(node.id),
          children: childResults.length > 0 ? childResults : undefined,
          isLeaf: !hasChildren,
        });
      }
    });
    return filtered;
  };

  const handleAddCategory = (parentId?: number) => {
    setCategoryModalMode('add');
    setCategoryParentId(parentId);
    setEditingCategory(null);
    categoryForm.resetFields();
    if (parentId !== undefined && parentId !== 0) {
      categoryForm.setFieldsValue({ parentId });
    }
    setCategoryModalVisible(true);
  };

  const handleEditCategory = (node: TaskCategoryTreeNode) => {
    setCategoryModalMode('edit');
    setEditingCategory(node);
    setCategoryParentId(node.parentId);
    categoryForm.setFieldsValue({
      name: node.name,
      sortOrder: node.sortOrder,
    });
    setCategoryModalVisible(true);
  };

  const handleDeleteCategory = (node: TaskCategoryTreeNode) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该分类吗？仅能删除无子分类且无关联任务的分类。',
      onOk: async () => {
        try {
          await deleteTaskCategory(node.id);
          message.success('删除成功');
          fetchCategoryTree();
        } catch (error) {
          message.error('删除失败');
        }
      },
    });
  };

  const handleCategoryModalSubmit = async () => {
    try {
      const values = await categoryForm.validateFields();
      if (categoryModalMode === 'add') {
        await createTaskCategory({
          name: values.name,
          parentId: categoryParentId ?? 0,
          sortOrder: values.sortOrder,
        });
        message.success('新增分类成功');
        setCategoryModalVisible(false);
        fetchCategoryTree();
      } else if (editingCategory) {
        await updateTaskCategory(editingCategory.id, {
          name: values.name,
          sortOrder: values.sortOrder,
        });
        message.success('编辑分类成功');
        setCategoryModalVisible(false);
        fetchCategoryTree();
      }
    } catch (error) {
      console.error('分类操作失败:', error);
    }
  };

  const findCategoryNode = (nodes: TaskCategoryTreeNode[], key: string): TaskCategoryTreeNode | null => {
    for (const n of nodes) {
      if (String(n.id) === key) return n;
      if (n.children && n.children.length > 0) {
        const found = findCategoryNode(n.children, key);
        if (found) return found;
      }
    }
    return null;
  };

  const titleRender = (node: any) => {
    const treeNode = findCategoryNode(categoryTree, node.key);
    if (!treeNode) return <span>{node.title}</span>;

    const menu = (
      <Menu>
        <Menu.Item key="add" onClick={() => handleAddCategory(treeNode.id)}>
          新增子分类
        </Menu.Item>
        <Menu.Item key="edit" onClick={() => handleEditCategory(treeNode)}>
          编辑
        </Menu.Item>
        <Menu.Item key="delete" danger onClick={() => handleDeleteCategory(treeNode)}>
          删除
        </Menu.Item>
      </Menu>
    );

    return (
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
        <span>{treeNode.name}</span>
        <Dropdown overlay={menu} trigger={['contextMenu']}>
          <span style={{ padding: '0 4px' }}>
            <MoreOutlined style={{ fontSize: 12, color: '#999' }} />
          </span>
        </Dropdown>
      </div>
    );
  };

  const tableColumns = [
    {
      title: '任务名称',
      dataIndex: 'taskName',
      key: 'taskName',
      width: 200,
      render: (text: string) => <a>{text}</a>,
    },
    {
      title: '任务描述',
      dataIndex: 'taskDescription',
      key: 'taskDescription',
      ellipsis: true,
    },
    {
      title: '任务分类',
      dataIndex: 'categoryId',
      key: 'categoryId',
      width: 200,
      render: (categoryId: number) => getCategoryPath(categoryId),
    },
    {
      title: '发布状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: number) => {
        const item = STATUS_MAP[status];
        return <Tag color={item.color}>{item.label}</Tag>;
      },
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 170,
    },
    {
      title: '操作',
      key: 'action',
      width: 320,
      align: 'center' as const,
      fixed: 'right' as const,
      render: (_: any, record: TaskItem) => {
        const actions: React.ReactNode[] = [];
        actions.push(
          <Button key="test" type="link" size="small" onClick={() => handleTestRun(record.id)}>
            任务测试
          </Button>,
        );
        if (record.status === 0 || record.status === 2) {
          actions.push(
            <Button key="publish" type="link" size="small" onClick={() => handlePublish(record.id)}>
              发布
            </Button>,
          );
        }
        if (record.status === 1) {
          actions.push(
            <Button key="disable" type="link" size="small" onClick={() => handleDisable(record.id)}>
              停用
            </Button>,
          );
        }
        if (record.status !== 1) {
          actions.push(
            <Button key="edit" type="link" size="small" onClick={() => handleEdit(record)}>
              编辑
            </Button>,
          );
        }
        if (record.status === 0) {
          actions.push(
            <Button key="delete" type="link" size="small" danger onClick={() => handleDelete(record.id)}>
              删除
            </Button>,
          );
        }
        return <Space size={0}>{actions}</Space>;
      },
    },
  ];

  const rowSelection = {
    selectedRowKeys,
    onChange: (keys: React.Key[]) => setSelectedRowKeys(keys as number[]),
  };

  return (
    <div style={{ display: 'flex', height: 'calc(100vh - 64px)' }}>
      <Card style={{ width: 280, margin: 16, borderRadius: 12 }} bodyStyle={{ padding: 0 }}>
        <div
          style={{
            padding: 16,
            borderBottom: '1px solid #f0f0f0',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <span style={{ fontWeight: 600 }}>任务分类</span>
          <Button type="link" size="small" icon={<PlusOutlined />} onClick={() => handleAddCategory(0)} />
        </div>
        <div style={{ padding: 8, borderBottom: '1px solid #f0f0f0' }}>
          <Input
            placeholder="按分类名称查询"
            prefix={<SearchOutlined />}
            value={categorySearchText}
            onChange={(e) => setCategorySearchText(e.target.value)}
            style={{ width: '100%' }}
          />
        </div>
        <Tree
          showLine
          defaultExpandAll
          onSelect={handleCategorySelect}
          selectedKeys={selectedCategory ? [String(selectedCategory)] : []}
          treeData={filterTreeData(categoryTree, categorySearchText)}
          titleRender={titleRender}
          style={{ padding: 8 }}
        />
      </Card>

      <div style={{ flex: 1, margin: 16, display: 'flex', flexDirection: 'column' }}>
        <Card style={{ borderRadius: 12 }}>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: 16,
            }}
          >
            <div
              style={{
                display: 'flex',
                gap: 16,
                alignItems: 'center',
                flexWrap: 'wrap',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <span
                  style={{
                    marginRight: 8,
                    whiteSpace: 'nowrap',
                    minWidth: 80,
                    textAlign: 'right',
                  }}
                >
                  发布状态：
                </span>
                <Select
                  placeholder="请选择状态"
                  allowClear
                  style={{ width: 150 }}
                  value={searchParams.status || undefined}
                  onChange={(value) => setSearchParams({ ...searchParams, status: value || '' })}
                >
                  <Select.Option value="0">未发布</Select.Option>
                  <Select.Option value="1">已发布</Select.Option>
                  <Select.Option value="2">已停用</Select.Option>
                </Select>
              </div>
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <span
                  style={{
                    marginRight: 8,
                    whiteSpace: 'nowrap',
                    minWidth: 80,
                    textAlign: 'right',
                  }}
                >
                  任务名称：
                </span>
                <Input
                  placeholder="请输入任务名称"
                  style={{ width: 200 }}
                  value={searchParams.keyword || ''}
                  onChange={(e) => setSearchParams({ ...searchParams, keyword: e.target.value })}
                />
              </div>
              <Button onClick={handleReset}>重置</Button>
              <Button type="primary" onClick={handleSearch}>
                检索
              </Button>
            </div>
            <div style={{ marginLeft: 16 }}>
              <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/data/addTask')}>
                新增任务
              </Button>
            </div>
          </div>

          <div style={{ display: 'flex', gap: 12, marginBottom: 16 }}>
            <Button
              icon={<PlayCircleOutlined />}
              onClick={handleBatchPublish}
              disabled={selectedRowKeys.length === 0}
            >
              批量发布
            </Button>
            <Button
              icon={<PoweroffOutlined />}
              onClick={handleBatchDisable}
              disabled={selectedRowKeys.length === 0}
            >
              批量停用
            </Button>
            <Button disabled={selectedRowKeys.length === 0}>批量分类</Button>
          </div>

          <Table
            rowSelection={rowSelection}
            columns={tableColumns}
            dataSource={taskList}
            loading={loading}
            pagination={{
              current: searchParams.pageNum,
              pageSize: searchParams.pageSize,
              total,
              showSizeChanger: true,
              pageSizeOptions: ['10', '20', '50', '100'],
              showTotal: (t) => `共 ${t} 条`,
            }}
            onChange={handleTableChange}
            rowKey="id"
            scroll={{ x: 1000 }}
          />
        </Card>
      </div>

      <Modal
        title="任务测试运行"
        visible={testModalVisible}
        onCancel={() => setTestModalVisible(false)}
        width={800}
        footer={[
          <Button key="close" onClick={() => setTestModalVisible(false)}>
            关闭
          </Button>,
          <Button
            key="execute"
            type="primary"
            icon={<ExperimentOutlined />}
            onClick={handleExecuteTest}
            loading={testLoading}
          >
            开始测试
          </Button>,
        ]}
      >
        {testResult && (
          <div>
            <div style={{ marginBottom: 16, display: 'flex', alignItems: 'center', gap: 16 }}>
              <span>执行状态：</span>
              <Tag color={EXECUTE_STATUS_MAP[testResult.status]?.color || 'default'}>
                {EXECUTE_STATUS_MAP[testResult.status]?.label || '未知'}
              </Tag>
              <span style={{ color: '#666' }}>总耗时：{testResult.totalDuration}ms</span>
            </div>
            <div style={{ marginBottom: 8, fontWeight: 600 }}>节点执行详情：</div>
            <Table
              size="small"
              dataSource={testResult.nodeResults}
              rowKey="nodeId"
              pagination={false}
              bordered
              columns={[
                { title: '节点名称', dataIndex: 'nodeName', key: 'nodeName', width: 120 },
                { title: '节点类型', dataIndex: 'nodeType', key: 'nodeType', width: 100 },
                {
                  title: '状态',
                  dataIndex: 'status',
                  key: 'status',
                  width: 80,
                  render: (status: number) => {
                    const map: Record<number, { label: string; color: string }> = {
                      2: { label: '成功', color: 'success' },
                      3: { label: '失败', color: 'error' },
                      5: { label: '跳过', color: 'default' },
                    };
                    const item = map[status] || { label: '未知', color: 'default' };
                    return <Tag color={item.color}>{item.label}</Tag>;
                  },
                },
                { title: '耗时(ms)', dataIndex: 'duration', key: 'duration', width: 90 },
                {
                  title: '日志',
                  dataIndex: 'logs',
                  key: 'logs',
                  ellipsis: true,
                },
              ]}
            />
          </div>
        )}
        {!testResult && !testLoading && (
          <div style={{ textAlign: 'center', padding: '40px 0', color: '#999' }}>
            点击"开始测试"按钮运行任务
          </div>
        )}
      </Modal>

      <Modal
        title={categoryModalMode === 'add' ? '新增分类' : '编辑分类'}
        visible={categoryModalVisible}
        onCancel={() => setCategoryModalVisible(false)}
        footer={[
          <Button key="cancel" onClick={() => setCategoryModalVisible(false)}>
            取消
          </Button>,
          <Button key="confirm" type="primary" onClick={handleCategoryModalSubmit}>
            确定
          </Button>,
        ]}
      >
        <Form form={categoryForm} layout="vertical">
          <Form.Item
            label={
              <span>
                <span style={{ color: '#ff4d4f' }}>*</span> 分类名称
              </span>
            }
            name="name"
            rules={[
              { required: true, message: '请输入分类名称' },
              { max: 50, message: '分类名称最大50字符' },
            ]}
          >
            <Input placeholder="请输入分类名称" />
          </Form.Item>
          <Form.Item label="排序号" name="sortOrder">
            <Input type="number" placeholder="请输入排序号" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default TaskManage;

import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Space, Tag, Modal, Form, Input, Select, InputNumber, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, CheckCircleOutlined, CloseCircleOutlined, ReloadOutlined, MoreOutlined } from '@ant-design/icons';
import { Dropdown, Menu } from 'antd';
import { getDatabaseList, createDatabase, updateDatabase, deleteDatabase, publishDatabase, disableDatabase, testDatabaseConnection } from '../../services/SourceAPIServices/dataBaseAPI';

interface DatabaseItem {
  id: number;
  connectionName: string;
  dbType: string;
  host: string;
  port: number;
  databaseName: string;
  username: string;
  status: number;
  description: string;
  lastTestTime: string;
  lastTestResult: number;
  createTime: string;
  updateTime: string;
}

const DataBaseManage: React.FC = () => {
  const [dataSource, setDataSource] = useState<DatabaseItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useState({
    status: '',
    keyword: '',
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [isEdit, setIsEdit] = useState(false);
  const [editingItem, setEditingItem] = useState<DatabaseItem | null>(null);
  const [form] = Form.useForm();
  const [testModalVisible, setTestModalVisible] = useState(false);
  const [testingId, setTestingId] = useState<number>(0);
  const [testResult, setTestResult] = useState<{ success: boolean; message: string; responseTime: number } | null>(null);
  const [currentStep, setCurrentStep] = useState(1);
  const [selectedDbType, setSelectedDbType] = useState('MYSQL');
  const [sortOrder, setSortOrder] = useState<'ascend' | 'descend' | undefined>('descend');

  const statusMap: Record<number, { label: string; color: string }> = {
    0: { label: '未发布', color: 'default' },
    1: { label: '已发布', color: 'success' },
    2: { label: '已停用', color: 'warning' },
  };

  const dbTypes = [
    { value: 'MYSQL', label: 'MySQL' },
    { value: 'POSTGRESQL', label: 'PostgreSQL' },
    { value: 'ORACLE', label: 'Oracle' },
    { value: 'SQLSERVER', label: 'SQL Server' },
    { value: 'HIVE', label: 'Hive' },
    { value: 'CLICKHOUSE', label: 'ClickHouse' },
  ];

  const loadData = async () => {
    setLoading(true);
    try {
      const params: Record<string, string | number | undefined> = {};
      if (searchParams.status) params.status = searchParams.status;
      if (searchParams.keyword) params.keyword = searchParams.keyword;
      const res = await getDatabaseList(params);
      const sortedData = [...res.data.records].sort((a: DatabaseItem, b: DatabaseItem) => {
        if (a.status !== b.status) {
          return a.status - b.status;
        }
        const timeCompare = new Date(a.updateTime).getTime() - new Date(b.updateTime).getTime();
        return sortOrder === 'ascend' ? timeCompare : -timeCompare;
      });
      setDataSource(sortedData);
    } catch (error) {
      message.error('加载数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [sortOrder]);

  const handleSearch = () => {
    loadData();
  };

  const handleReset = () => {
    setSearchParams({ status: '', keyword: '' });
    loadData();
  };

  const handleAdd = () => {
    setIsEdit(false);
    setEditingItem(null);
    setCurrentStep(1);
    setSelectedDbType('MYSQL');
    form.resetFields();
    setModalVisible(true);
  };

  const handleSelectDbType = () => {
    setCurrentStep(2);
    form.setFieldsValue({
      dbType: selectedDbType,
      port: selectedDbType === 'MYSQL' ? 3306 : 5432,
    });
  };

  const handleEdit = (record: DatabaseItem) => {
    setIsEdit(true);
    setEditingItem(record);
    form.setFieldsValue({
      connectionName: record.connectionName,
      dbType: record.dbType,
      host: record.host,
      port: record.port,
      databaseName: record.databaseName,
      username: record.username,
      description: record.description,
    });
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    Modal.confirm({
      title: '确认删除',
      content: '删除后该数据库将不能被系统其他模块使用，确定删除？',
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteDatabase(id);
          message.success('删除成功');
          loadData();
        } catch (error) {
          message.error('删除失败');
        }
      },
    });
  };

  const handlePublish = async (id: number) => {
    try {
      await publishDatabase(id);
      message.success('发布成功');
      loadData();
    } catch (error) {
      message.error('发布失败');
    }
  };

  const handleDisable = async (id: number) => {
    try {
      await disableDatabase(id);
      message.success('停用成功');
      loadData();
    } catch (error) {
      message.error('停用失败');
    }
  };

  const handleTest = (id: number) => {
    setTestingId(id);
    setTestResult(null);
    setTestModalVisible(true);
  };

  const doTestConnection = async () => {
    try {
      const res = await testDatabaseConnection(testingId);
      setTestResult({
        success: res.data.success,
        message: res.data.success ? '连接成功' : res.data.errorMessage || '连接失败',
        responseTime: res.data.responseTime || 0,
      });
    } catch (error) {
      setTestResult({
        success: false,
        message: '连接测试失败',
        responseTime: 0,
      });
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();

      let data;

      if (isEdit && editingItem) {
        data = {
          connectionName: values.connectionName,
          dbType: values.dbType,
          host: values.host,
          port: values.port,
          databaseName: values.databaseName,
          username: values.username,
          password: values.password,
          description: values.description,
        };
        await updateDatabase(editingItem.id, data);
        message.success('编辑成功');
      } else {
        const jdbcUrl = values.jdbcUrl;
        let host = '';
        let port = 3306;
        let databaseName = '';

        if (jdbcUrl) {
          const match = jdbcUrl.match(/jdbc:mysql:\/\/([^:]+):?(\d+)?\/?(\w*)/);
          if (match) {
            host = match[1];
            port = match[2] ? parseInt(match[2], 10) : 3306;
            databaseName = match[3] || '';
          }
        }

        data = {
          connectionName: values.connectionName,
          dbType: 'MYSQL',
          host,
          port,
          databaseName,
          username: values.username,
          password: values.password,
          description: values.description,
        };
        await createDatabase(data);
        message.success('新增成功');
      }

      setModalVisible(false);
      setCurrentStep(1);
      loadData();
    } catch (error) {
      message.error('保存失败');
    }
  };

  const columns = [
    {
      title: '数据源名称',
      dataIndex: 'connectionName',
      key: 'connectionName',
      width: 120,
      ellipsis: true,
    },
    {
      title: '数据库类型',
      dataIndex: 'dbType',
      key: 'dbType',
      width: 110,
      render: (type: string) => <Tag color="blue">{type}</Tag>,
    },
    {
      title: '数据源描述',
      dataIndex: 'description',
      key: 'description',
      width: 150,
      ellipsis: true,
    },
    {
      title: '连接信息',
      key: 'connection',
      width: 250,
      ellipsis: true,
      render: (_: any, record: DatabaseItem) => (
        <span>jdbc:mysql://{record.host}:{record.port}/{record.databaseName}</span>
      ),
    },
    {
      title: '应用状态',
      dataIndex: 'status',
      key: 'status',
      width: 90,
      render: (status: number) => (
        <Tag color={statusMap[status]?.color || 'default'}>
          {statusMap[status]?.label || '未知'}
        </Tag>
      ),
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 160,
      sorter: true,
      sortOrder,
      onHeaderCell: () => ({
        onClick: () => {
          setSortOrder(prev => prev === 'ascend' ? 'descend' : 'ascend');
        },
      }),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      align: 'center',
      fixed: 'right',
      render: (_: any, record: DatabaseItem) => {
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
            {record.status !== 1 && (
              <Menu.Item key="edit" onClick={() => handleEdit(record)}>
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
          <Space size="small">
            <Button type="link" size="small" onClick={() => handleTest(record.id)}>
              连通测试
            </Button>
            <Dropdown overlay={menu} trigger={['click']}>
              <Button type="link" size="small" icon={<MoreOutlined />}>
                更多
              </Button>
            </Dropdown>
          </Space>
        );
      },
    },
  ] as any;

  return (
    <div>
      <Card
        title="数据库管理"
        extra={
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增数据源
          </Button>
        }
      >
        <div style={{ display: 'flex', gap: 16, marginBottom: 16, alignItems: 'center' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span>应用状态：</span>
            <Select
              placeholder="全部"
              style={{ width: 120 }}
              value={searchParams.status || undefined}
              onChange={(value) => setSearchParams({ ...searchParams, status: value || '' })}
            >
              <Select.Option value="0">未发布</Select.Option>
              <Select.Option value="1">已发布</Select.Option>
              <Select.Option value="2">已停用</Select.Option>
            </Select>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span>数据源名称：</span>
            <Input
              placeholder="请输入数据源名称"
              style={{ width: 200 }}
              value={searchParams.keyword}
              onChange={(e) => setSearchParams({ ...searchParams, keyword: e.target.value })}
              onPressEnter={handleSearch}
            />
          </div>
          <Button onClick={handleReset}>重置</Button>
          <Button type="primary" onClick={handleSearch}>查询</Button>
        </div>
        <Table
          columns={columns}
          dataSource={dataSource}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
          scroll={{ x: 1030 }}
        />
      </Card>

      <Modal
        title={isEdit ? '编辑数据源' : (currentStep === 1 ? '数据库类型' : '添加数据库')}
        visible={modalVisible}
        onCancel={() => {
          setModalVisible(false);
          setCurrentStep(1);
        }}
        footer={null}
        width={600}
      >
        {isEdit ? (
          <Form form={form} layout="vertical">
            <Form.Item
              label="*数据源名称"
              name="connectionName"
              rules={[
                { required: true, message: '请输入数据源名称' },
                { pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_]+$/, message: '仅支持中英文、数字、下划线' },
                { max: 50, message: '最大50字符' },
              ]}
            >
              <Input placeholder="请输入数据源名称" />
            </Form.Item>
            <Form.Item
              label="*数据库类型"
              name="dbType"
              rules={[{ required: true, message: '请选择数据库类型' }]}
            >
              <Select placeholder="请选择数据库类型">
                {dbTypes.map((type) => (
                  <Select.Option key={type.value} value={type.value}>
                    {type.label}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item
              label="*主机地址"
              name="host"
              rules={[{ required: true, message: '请输入主机地址' }]}
            >
              <Input placeholder="请输入主机地址" />
            </Form.Item>
            <Form.Item
              label="*端口"
              name="port"
              rules={[{ required: true, message: '请输入端口号' }]}
            >
              <InputNumber min={1} max={65535} placeholder="请输入端口号" />
            </Form.Item>
            <Form.Item
              label="*数据库名称"
              name="databaseName"
              rules={[{ required: true, message: '请输入数据库名称' }]}
            >
              <Input placeholder="请输入数据库名称" />
            </Form.Item>
            <Form.Item
              label="*用户名"
              name="username"
              rules={[{ required: true, message: '请输入用户名' }]}
            >
              <Input placeholder="请输入用户名" />
            </Form.Item>
            <Form.Item
              label="密码（不填则不修改）"
              name="password"
            >
              <Input.Password placeholder="请输入密码" />
            </Form.Item>
            <Form.Item label="描述" name="description">
              <Input.TextArea rows={3} placeholder="请输入描述" maxLength={200} />
            </Form.Item>
            <div style={{ display: 'flex', gap: 16, justifyContent: 'flex-end', marginTop: 24 }}>
              <Button onClick={() => setModalVisible(false)}>取消</Button>
              <Button type="primary" onClick={handleSubmit}>确定</Button>
            </div>
          </Form>
        ) : (
          <>
            {currentStep === 1 ? (
              <div>
                <h3 style={{ textAlign: 'center', marginBottom: 24 }}>数据库类型</h3>
                <div style={{ display: 'flex', justifyContent: 'center' }}>
                  <div
                    style={{
                      border: selectedDbType === 'MYSQL' ? '2px solid #1890ff' : '1px solid #d9d9d9',
                      borderRadius: 8,
                      padding: 20,
                      cursor: 'pointer',
                      textAlign: 'center',
                    }}
                    onClick={() => setSelectedDbType('MYSQL')}
                  >
                    <div style={{ fontSize: 48, marginBottom: 8 }}>🗄️</div>
                    <div style={{ color: selectedDbType === 'MYSQL' ? '#1890ff' : '#666' }}>MySQL</div>
                  </div>
                </div>
                <div style={{ display: 'flex', gap: 16, justifyContent: 'flex-end', marginTop: 24 }}>
                  <Button onClick={() => setModalVisible(false)}>取消</Button>
                  <Button type="primary" onClick={handleSelectDbType}>确定</Button>
                </div>
              </div>
            ) : (
              <Form form={form} layout="vertical">
                <Form.Item
                  label="*数据库类型"
                  name="dbType"
                  initialValue="MYSQL"
                >
                  <Input disabled value="MySQL" />
                </Form.Item>
                <Form.Item
                  label="*数据源名称"
                  name="connectionName"
                  rules={[
                    { required: true, message: '请输入数据源名称' },
                    { pattern: /^[\u4e00-\u9fa5a-zA-Z0-9_]+$/, message: '仅支持中英文、数字、下划线' },
                    { max: 50, message: '最大50字符' },
                  ]}
                >
                  <Input placeholder="请输入数据源名称" />
                </Form.Item>
                <Form.Item label="数据源描述" name="description">
                  <Input placeholder="请输入数据源描述" />
                </Form.Item>
                <Form.Item
                  label="*JDBC URL"
                  name="jdbcUrl"
                  rules={[{ required: true, message: '请输入JDBC URL' }]}
                >
                  <Input placeholder="jdbc:mysql://host:port/database" />
                </Form.Item>
                <Form.Item
                  label="*用户名"
                  name="username"
                  rules={[{ required: true, message: '请输入用户名' }]}
                >
                  <Input placeholder="请输入用户名" />
                </Form.Item>
                <Form.Item
                  label="密码"
                  name="password"
                >
                  <Input.Password placeholder="请输入密码" />
                </Form.Item>
                <div style={{ display: 'flex', gap: 16, justifyContent: 'space-between', marginTop: 24 }}>
                  <Button onClick={() => setCurrentStep(1)}>上一步</Button>
                  <div style={{ display: 'flex', gap: 16 }}>
                    <Button onClick={() => setModalVisible(false)}>取消</Button>
                    <Button type="primary" onClick={handleSubmit}>确定</Button>
                  </div>
                </div>
              </Form>
            )}
          </>
        )}
      </Modal>

      <Modal
        title="数据库连接测试"
        visible={testModalVisible}
        onCancel={() => setTestModalVisible(false)}
        footer={[
          <Button key="back" onClick={() => setTestModalVisible(false)}>关闭</Button>,
          <Button key="test" type="primary" onClick={doTestConnection} icon={<ReloadOutlined />}>
            测试连接
          </Button>,
        ]}
        width={400}
      >
        {testResult ? (
          <div style={{ textAlign: 'center', padding: '20px 0' }}>
            {testResult.success ? (
              <CheckCircleOutlined style={{ fontSize: 48, color: '#52c41a', marginBottom: 16 }} />
            ) : (
              <CloseCircleOutlined style={{ fontSize: 48, color: '#ff4d4f', marginBottom: 16 }} />
            )}
            <p style={{ fontSize: 16, fontWeight: 'bold', marginBottom: 8 }}>
              {testResult.message}
            </p>
            <p style={{ color: '#666' }}>响应时间：{testResult.responseTime}ms</p>
          </div>
        ) : (
          <p style={{ textAlign: 'center', padding: '20px 0' }}>点击下方按钮测试数据库连接</p>
        )}
      </Modal>
    </div>
  );
};

export default DataBaseManage;

import React, { useState, useEffect } from 'react';
import { Card, Table, Button, Form, Input, Select, Modal, message, Popconfirm, Upload, Space } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, MoreOutlined, DownloadOutlined, UploadOutlined } from '@ant-design/icons';
import {
  getDataStandardList,
  createDataStandard,
  updateDataStandard,
  deleteDataStandard,
  publishDataStandard,
  disableDataStandard,
  batchPublishDataStandards,
  batchDisableDataStandards,
  type DataStandardItem,
} from '../../services/DataStandardAPI/directoryAPI';

const { TextArea } = Input;

const statusMap: Record<number, string> = {
  0: '未发布',
  1: '已发布',
  2: '已停用',
};

const dataTypeMap: Record<string, string> = {
  String: 'String',
  Int: 'Int',
  Float: 'Float',
  Enum: 'Enum',
};

const nullableMap: Record<number, string> = {
  0: '可为空',
  1: '不可为空',
};

const sourceOrganizations = ['数宜信', '数据中心', '研发部', '业务部', '其他'];

const Directory: React.FC = () => {
  const [dataSource, setDataSource] = useState<DataStandardItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [searchParams, setSearchParams] = useState({
    sourceOrganization: '',
    status: '',
    standardCode: '',
    name: '',
    englishName: '',
  });
  const [modalVisible, setModalVisible] = useState(false);
  const [importModalVisible, setImportModalVisible] = useState(false);
  const [isEdit, setIsEdit] = useState(false);
  const [editingItem, setEditingItem] = useState<DataStandardItem | null>(null);
  const [form] = Form.useForm();
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [sortOrder, setSortOrder] = useState<'ascend' | 'descend' | undefined>('descend');

  const loadData = async () => {
    setLoading(true);
    try {
      const params: Record<string, string | number | undefined> = {};
      if (searchParams.sourceOrganization) params.sourceOrganization = searchParams.sourceOrganization;
      if (searchParams.status) params.status = searchParams.status;
      if (searchParams.standardCode) params.keyword = searchParams.standardCode;
      if (searchParams.name) params.keyword = searchParams.name;
      if (searchParams.englishName) params.keyword = searchParams.englishName;
      const res = await getDataStandardList(params);
      const sortedData = [...res.data.records].sort((a: DataStandardItem, b: DataStandardItem) => {
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
    setSearchParams({
      sourceOrganization: '',
      status: '',
      standardCode: '',
      name: '',
      englishName: '',
    });
    loadData();
  };

  const handleAdd = () => {
    setIsEdit(false);
    setEditingItem(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record: DataStandardItem) => {
    setIsEdit(true);
    setEditingItem(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteDataStandard(id);
      message.success('删除成功');
      loadData();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handlePublish = async (id: number) => {
    try {
      await publishDataStandard(id);
      message.success('发布成功');
      loadData();
    } catch (error) {
      message.error('发布失败');
    }
  };

  const handleDisable = async (id: number) => {
    try {
      await disableDataStandard(id);
      message.success('停用成功');
      loadData();
    } catch (error) {
      message.error('停用失败');
    }
  };

  const handleBatchPublish = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要发布的标准');
      return;
    }
    try {
      await batchPublishDataStandards({ ids: selectedRowKeys.map(key => Number(key)) });
      message.success('批量发布成功');
      setSelectedRowKeys([]);
      loadData();
    } catch (error) {
      message.error('批量发布失败');
    }
  };

  const handleBatchDisable = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要停用的标准');
      return;
    }
    try {
      await batchDisableDataStandards({ ids: selectedRowKeys.map(key => Number(key)) });
      message.success('批量停用成功');
      setSelectedRowKeys([]);
      loadData();
    } catch (error) {
      message.error('批量停用失败');
    }
  };

  const handleSubmit = async () => {
    try {
      await form.validateFields();
      const values = form.getFieldsValue();
      if (isEdit && editingItem) {
        await updateDataStandard(editingItem.id, values);
        message.success('编辑成功');
      } else {
        await createDataStandard(values);
        message.success('新增成功');
      }
      setModalVisible(false);
      loadData();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const handleImport = () => {
    setImportModalVisible(true);
  };

  const handleImportConfirm = () => {
    message.info('导入功能开发中');
    setImportModalVisible(false);
  };

  const columns = [
    {
      title: '',
      dataIndex: 'id',
      key: 'id',
      width: 40,
      render: (_: number, __: DataStandardItem, index: number) => (
        <span>{index + 1}</span>
      ),
    },
    {
      title: '标准编号',
      dataIndex: 'standardCode',
      key: 'standardCode',
      width: 100,
    },
    {
      title: '中文名称',
      dataIndex: 'name',
      key: 'name',
      width: 120,
    },
    {
      title: '英文名称',
      dataIndex: 'englishName',
      key: 'englishName',
      width: 120,
    },
    {
      title: '标准说明',
      dataIndex: 'description',
      key: 'description',
      width: 150,
      ellipsis: true,
    },
    {
      title: '来源机构',
      dataIndex: 'sourceOrganization',
      key: 'sourceOrganization',
      width: 100,
    },
    {
      title: '数据类型',
      dataIndex: 'dataType',
      key: 'dataType',
      width: 80,
      render: (text: string) => <span>{dataTypeMap[text] || text}</span>,
    },
    {
      title: '数据长度',
      dataIndex: 'length',
      key: 'length',
      width: 80,
    },
    {
      title: '数据精度',
      dataIndex: 'precision',
      key: 'precision',
      width: 80,
    },
    {
      title: '默认值',
      dataIndex: 'defaultValue',
      key: 'defaultValue',
      width: 80,
    },
    {
      title: '取值范围',
      key: 'range',
      width: 120,
      render: (_: any, record: DataStandardItem) => {
        if (record.rangeMin || record.rangeMax) {
          return `${record.rangeMin || ''}-${record.rangeMax || ''}`;
        }
        return <span>-</span>;
      },
    },
    {
      title: '枚举范围',
      dataIndex: 'enumRange',
      key: 'enumRange',
      width: 120,
    },
    {
      title: '是否可为空',
      dataIndex: 'nullable',
      key: 'nullable',
      width: 90,
      render: (text: number) => <span>{nullableMap[text] || '-'}</span>,
    },
    {
      title: '标准状态',
      dataIndex: 'status',
      key: 'status',
      width: 80,
      render: (text: number) => <span>{statusMap[text] || '-'}</span>,
    },
    {
      title: '更新日期',
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
      width: 140,
      fixed: 'right' as const,
      align: 'center' as const,
      render: (_: unknown, record: DataStandardItem) => {
        const menuItems = [];
        if (record.status === 0) {
          menuItems.push(
            { key: 'publish', label: <Button type="link" onClick={() => handlePublish(record.id)}>发布</Button> },
            { key: 'edit', label: <Button type="link" onClick={() => handleEdit(record)}>编辑</Button> },
            {
              key: 'delete', label: (
                <Popconfirm
                  title="确定删除该标准吗？"
                  onConfirm={() => handleDelete(record.id)}
                  okText="确定"
                  cancelText="取消"
                >
                  <Button type="link" danger>删除</Button>
                </Popconfirm>
              )
            }
          );
        } else if (record.status === 1) {
          menuItems.push(
            { key: 'disable', label: <Button type="link" onClick={() => handleDisable(record.id)}>停用</Button> }
          );
        } else if (record.status === 2) {
          menuItems.push(
            { key: 'publish', label: <Button type="link" onClick={() => handlePublish(record.id)}>发布</Button> },
            { key: 'edit', label: <Button type="link" onClick={() => handleEdit(record)}>编辑</Button> }
          );
        }

        if (menuItems.length === 0) return <span>-</span>;
        if (menuItems.length === 1) {
          return <>{menuItems[0].label}</>;
        }

        return (
          <Space>
            <Button
              type="link"
              icon={<MoreOutlined />}
              onClick={() => { }}
            >
              更多
            </Button>
          </Space>
        );
      },
    },
  ];

  const rowSelection = {
    selectedRowKeys,
    onChange: (keys: React.Key[]) => {
      setSelectedRowKeys(keys);
    },
  };

  return (
    <div style={{ padding: '24px' }}>
      <Card style={{ borderRadius: 12, marginBottom: 16 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 16, flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span>来源机构：</span>
            <Select
              placeholder="请选择"
              style={{ width: 150 }}
              value={searchParams.sourceOrganization}
              onChange={(value) => setSearchParams(prev => ({ ...prev, sourceOrganization: value }))}
              options={sourceOrganizations.map(org => ({ label: org, value: org }))}
            />
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span>标准状态：</span>
            <Select
              placeholder="请选择"
              style={{ width: 150 }}
              value={searchParams.status}
              onChange={(value) => setSearchParams(prev => ({ ...prev, status: value }))}
              options={[
                { label: '未发布', value: '0' },
                { label: '已发布', value: '1' },
                { label: '已停用', value: '2' },
              ]}
            />
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span>标准编号：</span>
            <Input
              placeholder="请输入"
              style={{ width: 150 }}
              value={searchParams.standardCode}
              onChange={(e) => setSearchParams(prev => ({ ...prev, standardCode: e.target.value }))}
            />
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span>中文名称：</span>
            <Input
              placeholder="请输入"
              style={{ width: 150 }}
              value={searchParams.name}
              onChange={(e) => setSearchParams(prev => ({ ...prev, name: e.target.value }))}
            />
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span>英文名称：</span>
            <Input
              placeholder="请输入"
              style={{ width: 150 }}
              value={searchParams.englishName}
              onChange={(e) => setSearchParams(prev => ({ ...prev, englishName: e.target.value }))}
            />
          </div>
          <Button onClick={handleReset}>重置</Button>
          <Button type="primary" onClick={handleSearch}>查询</Button>
        </div>
      </Card>

      <Card style={{ borderRadius: 12 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <Space>
            <Button onClick={handleBatchPublish}>批量发布</Button>
            <Button onClick={handleBatchDisable}>批量停用</Button>
          </Space>
          <Space>
            <Button icon={<DownloadOutlined />}>导入模板下载</Button>
            <Button type="primary" icon={<UploadOutlined />} onClick={handleImport}>标准导入</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>新增标准</Button>
          </Space>
        </div>

        <Table
          rowSelection={rowSelection}
          columns={columns}
          dataSource={dataSource}
          loading={loading}
          rowKey="id"
          scroll={{ x: 1600 }}
          pagination={{
            pageSize: 20,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </Card>

      <Modal
        title={isEdit ? '编辑标准' : '新增标准'}
        visible={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={[
          <Button key="cancel" onClick={() => setModalVisible(false)}>取消</Button>,
          <Button key="confirm" type="primary" onClick={handleSubmit}>确定</Button>,
        ]}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            label="*中文名称"
            name="name"
            rules={[
              { required: true, message: '请输入中文名称' },
              { pattern: /^[\u4e00-\u9fa5a-zA-Z]+$/, message: '中文名称只支持中文及英文大小写' },
              { max: 50, message: '中文名称最大50字符' },
            ]}
          >
            <Input placeholder="请输入标准中文名称" />
          </Form.Item>

          <Form.Item
            label="*英文名称"
            name="englishName"
            rules={[
              { required: true, message: '请输入英文名称' },
              { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '英文名称只支持英文大小写、数字及下划线，且只能英文开头' },
              { max: 100, message: '英文名称最大100字符' },
            ]}
          >
            <Input placeholder="请输入标准英文名称" />
          </Form.Item>

          <Form.Item
            label="标准说明"
            name="description"
            rules={[
              { max: 500, message: '标准说明最大500字符' },
            ]}
          >
            <TextArea rows={3} placeholder="请输入标准说明" />
          </Form.Item>

          <Form.Item
            label="*来源机构"
            name="sourceOrganization"
            rules={[
              { required: true, message: '请选择来源机构' },
            ]}
          >
            <Select placeholder="请选择来源机构" style={{ width: '100%' }}>
              {sourceOrganizations.map(org => (
                <Select.Option key={org} value={org}>{org}</Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="*数据类型"
            name="dataType"
            rules={[
              { required: true, message: '请选择数据类型' },
            ]}
          >
            <Select placeholder="请选择数据类型" style={{ width: '100%' }}>
              <Select.Option value="String">String</Select.Option>
              <Select.Option value="Int">Int</Select.Option>
              <Select.Option value="Float">Float</Select.Option>
              <Select.Option value="Enum">Enum</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            label="数据长度"
            name="length"
            rules={[
              { pattern: /^[1-9]\d*$/, message: '数据长度只能是正整数' },
            ]}
          >
            <Input type="number" placeholder="数据长度" />
          </Form.Item>

          <Form.Item
            label="数据精度"
            name="precision"
            rules={[
              { pattern: /^\d*$/, message: '数据精度是非负整数' },
            ]}
          >
            <Input type="number" placeholder="数据精度" />
          </Form.Item>

          <Form.Item
            label="默认值"
            name="defaultValue"
          >
            <Input placeholder="默认值" />
          </Form.Item>

          <Form.Item
            label="取值范围最小值"
            name="rangeMin"
          >
            <Input placeholder="取值范围最小值" />
          </Form.Item>

          <Form.Item
            label="取值范围最大值"
            name="rangeMax"
          >
            <Input placeholder="取值范围最大值" />
          </Form.Item>

          <Form.Item
            label="枚举范围"
            name="enumRange"
          >
            <Input placeholder="引用码表编号" />
          </Form.Item>

          <Form.Item
            label="是否可为空"
            name="nullable"
          >
            <Select placeholder="请选择" style={{ width: '100%' }}>
              <Select.Option value={0}>可为空</Select.Option>
              <Select.Option value={1}>不可为空</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="标准文件导入"
        visible={importModalVisible}
        onCancel={() => setImportModalVisible(false)}
        footer={[
          <Button key="cancel" onClick={() => setImportModalVisible(false)}>取消</Button>,
          <Button key="confirm" type="primary" onClick={handleImportConfirm}>确定</Button>,
        ]}
      >
        <div style={{ marginBottom: 16 }}>
          <span style={{ color: '#ff4d4f' }}>*</span>
          <span>选择文件：</span>
          <Button type="primary">本地文件</Button>
        </div>
        <p style={{ color: '#666', fontSize: 12 }}>
          提示：请下载最新模板，按照模板标准准备导入文件后导入标准文档，导入后系统将满足正确条件的标准导入到标准目录，错误的标准将被过滤。
        </p>
      </Modal>
    </div>
  );
};

export default Directory;

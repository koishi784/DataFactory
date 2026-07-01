import React, { useState, useEffect, useCallback } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  Select,
  Row,
  Col,
  message,
  Popconfirm,
} from 'antd';
import { PlusOutlined, DownloadOutlined, UploadOutlined } from '@ant-design/icons';
import {
  getCodeTableList,
  createCodeTable,
  updateCodeTable,
  deleteCodeTable,
  publishCodeTable,
  disableCodeTable,
  batchPublishCodeTables,
  batchDisableCodeTables,
  getCodeTableItems,
  type CodeTableItem,
  type CodeTableListParams,
} from '@/services/DataStandardAPI/codeTableAPI';

interface CodeItemForm {
  code: string;
  name: string;
  value: string;
  description?: string;
}

const STATUS_MAP: Record<number, { label: string; color: string }> = {
  0: { label: '未发布', color: 'default' },
  1: { label: '已发布', color: 'success' },
  2: { label: '已停用', color: 'warning' },
};

const CodeTable: React.FC = () => {
  const [form] = Form.useForm();
  const [data, setData] = useState<CodeTableItem[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalTitle, setModalTitle] = useState('新增码表');
  const [editingId, setEditingId] = useState<number | null>(null);
  const [selectedRows, setSelectedRows] = useState<number[]>([]);
  const [searchParams, setSearchParams] = useState<CodeTableListParams>({
    pageNum: 1,
    pageSize: 20,
  });

  const [codeItems, setCodeItems] = useState<CodeItemForm[]>([
    { code: '', name: '', value: '' },
  ]);

  const fetchData = useCallback(async (params?: CodeTableListParams) => {
    setLoading(true);
    try {
      const response = await getCodeTableList(params);
      if (response && response.data) {
        setData(response.data.records || []);
        setTotal(response.data.total || 0);
      }
    } catch (error) {
      console.error('查询码表列表失败:', error);
      message.error('查询码表列表失败');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData(searchParams);
  }, [fetchData, searchParams]);

  const handleSearch = () => {
    setSearchParams((prev) => ({ ...prev, pageNum: 1 }));
  };

  const handleReset = () => {
    setSearchParams({ pageNum: 1, pageSize: 20 });
  };

  const handleTableChange = (pagination: { current?: number; pageSize?: number }) => {
    setSearchParams((prev) => ({
      ...prev,
      pageNum: pagination.current || 1,
      pageSize: pagination.pageSize || 20,
    }));
  };

  const handleAddCodeItem = () => {
    setCodeItems((prev) => [...prev, { code: '', name: '', value: '' }]);
  };

  const handleRemoveCodeItem = (index: number) => {
    setCodeItems((prev) => prev.filter((_, i) => i !== index));
  };

  const handleCodeItemChange = (index: number, field: keyof CodeItemForm, value: string) => {
    setCodeItems((prev) =>
      prev.map((item, i) => (i === index ? { ...item, [field]: value } : item)),
    );
  };

  const validateName = (rule: unknown, value: string) => {
    const regex = /^[\u4e00-\u9fa5a-zA-Z]+$/;
    if (!value || value.trim() === '') {
      return Promise.reject('请输入名称');
    }
    if (!regex.test(value.trim())) {
      return Promise.reject('名称只能包含中文及大小写英文');
    }
    return Promise.resolve();
  };

  const handleOpenModal = (id?: number) => {
    if (id) {
      setModalTitle('编辑码表');
      setEditingId(id);
      const item = data.find((d) => d.id === id);
      if (item) {
        form.setFieldsValue({
          tableName: item.tableName,
          description: item.description,
        });
        getCodeTableItems(id).then((res) => {
          if (res && res.data && res.data.items) {
            setCodeItems(
              res.data.items.map((item: any) => ({
                code: item.code,
                name: item.name,
                value: item.value,
                description: item.description,
              })),
            );
          }
        });
      }
    } else {
      setModalTitle('新增码表');
      setEditingId(null);
      form.resetFields();
      setCodeItems([{ code: '', name: '', value: '' }]);
    }
    setModalVisible(true);
  };

  const handleCloseModal = () => {
    setModalVisible(false);
    setEditingId(null);
    form.resetFields();
    setCodeItems([{ code: '', name: '', value: '' }]);
  };

  const handleSubmit = () => {
    form.validateFields().then((values) => {
      const codeRegex = /^[\u4e00-\u9fa5a-zA-Z]+$/;
      
      // 校验码值配置
      const codes: string[] = [];
      const names: string[] = [];
      for (let i = 0; i < codeItems.length; i++) {
        const item = codeItems[i];
        const trimmedCode = item.code.trim();
        const trimmedName = item.name.trim();

        if (trimmedCode === '') {
          message.error(`第 ${i + 1} 行编码取值为空`);
          return;
        }
        if (trimmedName === '') {
          message.error(`第 ${i + 1} 行编码中文名称为空`);
          return;
        }
        if (!codeRegex.test(trimmedName)) {
          message.error(`第 ${i + 1} 行编码中文名称只能包含中文及大小写英文`);
          return;
        }
        if (codes.includes(trimmedCode)) {
          message.error('编码取值不能重复');
          return;
        }
        if (names.includes(trimmedName)) {
          message.error('编码中文名称不能重复');
          return;
        }
        codes.push(trimmedCode);
        names.push(trimmedName);
      }

      const validItems = codeItems.filter(
        (item) => item.code.trim() !== '' && item.name.trim() !== '',
      );

      const requestData = {
        tableName: values.tableName,
        description: values.description,
        items: validItems.map((item, index) => ({
          code: item.code.trim(),
          name: item.name.trim(),
          value: item.value.trim() || item.name.trim(),
          sortOrder: index + 1,
          description: item.description,
        })),
      };

      const request = editingId
        ? updateCodeTable(editingId, requestData)
        : createCodeTable(requestData);

      request
        .then(() => {
          message.success(editingId ? '编辑成功' : '新增成功');
          handleCloseModal();
          fetchData(searchParams);
        })
        .catch((error) => {
          console.error('操作失败:', error);
          message.error('操作失败');
        });
    });
  };

  const handlePublish = (id: number) => {
    publishCodeTable(id)
      .then(() => {
        message.success('发布成功');
        fetchData(searchParams);
      })
      .catch((error) => {
        console.error('发布失败:', error);
        message.error('发布失败');
      });
  };

  const handleDisable = (id: number) => {
    disableCodeTable(id)
      .then(() => {
        message.success('停用成功');
        fetchData(searchParams);
      })
      .catch((error) => {
        console.error('停用失败:', error);
        message.error('停用失败');
      });
  };

  const handleDelete = (id: number) => {
    deleteCodeTable(id)
      .then(() => {
        message.success('删除成功');
        fetchData(searchParams);
      })
      .catch((error) => {
        console.error('删除失败:', error);
        message.error('删除失败');
      });
  };

  const handleBatchPublish = () => {
    if (selectedRows.length === 0) {
      message.warning('请选择要发布的码表');
      return;
    }
    batchPublishCodeTables({ ids: selectedRows })
      .then(() => {
        message.success('批量发布成功');
        setSelectedRows([]);
        fetchData(searchParams);
      })
      .catch((error) => {
        console.error('批量发布失败:', error);
        message.error('批量发布失败');
      });
  };

  const handleBatchDisable = () => {
    if (selectedRows.length === 0) {
      message.warning('请选择要停用的码表');
      return;
    }
    batchDisableCodeTables({ ids: selectedRows })
      .then(() => {
        message.success('批量停用成功');
        setSelectedRows([]);
        fetchData(searchParams);
      })
      .catch((error) => {
        console.error('批量停用失败:', error);
        message.error('批量停用失败');
      });
  };

  const columns = [
    {
      title: '码表编号',
      dataIndex: 'tableCode',
      key: 'tableCode',
      width: 120,
    },
    {
      title: '码表名称',
      dataIndex: 'tableName',
      key: 'tableName',
    },
    {
      title: '码表说明',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '码表状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: number) => (
        <Tag color={STATUS_MAP[status]?.color}>
          {STATUS_MAP[status]?.label}
        </Tag>
      ),
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
      width: 200,
      render: (_: any, record: CodeTableItem) => (
        <Space size="small">
          {record.status !== 1 && (
            <Button type="link" size="small" onClick={() => handleOpenModal(record.id)}>
              编辑
            </Button>
          )}
          {record.status !== 1 && (
            <Popconfirm
              title="确定发布该码表？"
              onConfirm={() => handlePublish(record.id)}
              okText="确定"
              cancelText="取消"
            >
              <Button type="link" size="small">
                发布
              </Button>
            </Popconfirm>
          )}
          {record.status === 1 && (
            <Popconfirm
              title="确定停用该码表？"
              onConfirm={() => handleDisable(record.id)}
              okText="确定"
              cancelText="取消"
            >
              <Button type="link" size="small">
                停用
              </Button>
            </Popconfirm>
          )}
          {record.status === 0 && (
            <Popconfirm
              title="确定删除该码表？"
              onConfirm={() => handleDelete(record.id)}
              okText="确定"
              cancelText="取消"
            >
              <Button type="link" size="small" danger>
                删除
              </Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ];

  const rowSelection = {
    selectedRowKeys: selectedRows,
    onChange: (keys: React.Key[]) => {
      setSelectedRows(keys as number[]);
    },
  };

  return (
    <div>
      <Card
        title="码表管理"
        extra={
          <Space>
            <Button icon={<DownloadOutlined />} onClick={() => message.info('码表模板下载功能暂未开放')}>
              码表模板下载
            </Button>
            <Button icon={<UploadOutlined />} onClick={() => message.info('码表导入功能暂未开放')}>
              码表导入
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => handleOpenModal()}>
              新增码表
            </Button>
          </Space>
        }
      >
        <div style={{ marginBottom: 16 }}>
          <Row gutter={16} align="middle">
            <Col span={6}>
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <span style={{ marginRight: 8, whiteSpace: 'nowrap', minWidth: 80, textAlign: 'right' }}>码表状态：</span>
                <Select
                  placeholder="请选择状态"
                  allowClear
                  style={{ flex: 1 }}
                  value={searchParams.status || undefined}
                  onChange={(value) =>
                    setSearchParams((prev) => ({ ...prev, status: value || undefined }))
                  }
                  options={[
                    { value: '0', label: '未发布' },
                    { value: '1', label: '已发布' },
                    { value: '2', label: '已停用' },
                  ]}
                />
              </div>
            </Col>
            <Col span={6}>
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <span style={{ marginRight: 8, whiteSpace: 'nowrap', minWidth: 80, textAlign: 'right' }}>码表名称：</span>
                <Input
                  placeholder="请输入码表名称"
                  style={{ flex: 1 }}
                  value={searchParams.keyword || ''}
                  onChange={(e) =>
                    setSearchParams((prev) => ({ ...prev, keyword: e.target.value }))
                  }
                  onPressEnter={handleSearch}
                />
              </div>
            </Col>
            <Col span={4}>
              <Button onClick={handleReset}>重置</Button>
              <Button type="primary" onClick={handleSearch} style={{ marginLeft: 8 }}>
                查询
              </Button>
            </Col>
          </Row>
        </div>

        <div style={{ marginBottom: 16 }}>
          <Button
            type="default"
            onClick={handleBatchPublish}
            disabled={selectedRows.length === 0}
          >
            批量发布
          </Button>
          <Button
            type="default"
            onClick={handleBatchDisable}
            disabled={selectedRows.length === 0}
            style={{ marginLeft: 8 }}
          >
            批量停用
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          loading={loading}
          pagination={{
            current: searchParams.pageNum,
            pageSize: searchParams.pageSize,
            total,
            showSizeChanger: true,
            pageSizeOptions: ['10', '20', '50', '100'],
            showTotal: (total) => `共 ${total} 条`,
          }}
          onChange={handleTableChange}
          rowSelection={rowSelection}
        />
      </Card>

      <Modal
        title={modalTitle}
        open={modalVisible}
        onCancel={handleCloseModal}
        onOk={handleSubmit}
        width={700}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            label="码表名称"
            name="tableName"
            rules={[
              { required: true, message: '请输入码表名称' },
              { validator: validateName },
            ]}
          >
            <Input placeholder="请输入标准中文名称" />
          </Form.Item>

          <Form.Item label="码表说明" name="description">
            <Input.TextArea placeholder="请输入标准说明" rows={3} />
          </Form.Item>

          <Form.Item label="编码配置">
            <div>
              <Table
                dataSource={codeItems}
                rowKey={(record, index) => String(index)}
                pagination={false}
                size="small"
                columns={[
                  {
                    title: '编码取值',
                    dataIndex: 'code',
                    width: 100,
                    render: (_, record, index) => (
                      <Input
                        value={record.code}
                        onChange={(e) =>
                          handleCodeItemChange(index, 'code', e.target.value)
                        }
                        placeholder="请输入编码"
                      />
                    ),
                  },
                  {
                    title: '编码名称',
                    dataIndex: 'name',
                    width: 120,
                    render: (_, record, index) => (
                      <Input
                        value={record.name}
                        onChange={(e) =>
                          handleCodeItemChange(index, 'name', e.target.value)
                        }
                        placeholder="请输入编码名称"
                      />
                    ),
                  },
                  {
                    title: '码值含义',
                    dataIndex: 'value',
                    render: (_, record, index) => (
                      <Input
                        value={record.value}
                        onChange={(e) =>
                          handleCodeItemChange(index, 'value', e.target.value)
                        }
                        placeholder="请输入码值含义"
                      />
                    ),
                  },
                  {
                    title: '操作',
                    width: 80,
                    render: (_, __, index) => (
                      <Button
                        type="link"
                        size="small"
                        danger
                        onClick={() => handleRemoveCodeItem(index)}
                        disabled={codeItems.length === 1}
                      >
                        删除
                      </Button>
                    ),
                  },
                ]}
              />
              <Button
                type="link"
                size="small"
                onClick={handleAddCodeItem}
                style={{ marginTop: 8, float: 'right' }}
              >
                添加
              </Button>
            </div>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default CodeTable;

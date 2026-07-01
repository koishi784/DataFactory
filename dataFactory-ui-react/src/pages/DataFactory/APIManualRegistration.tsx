import React, { useState, useEffect } from 'react';
import { Card, Form, Input, InputNumber, Select, Button, Modal, message, Table } from 'antd';
import { ArrowLeftOutlined, PlusOutlined, EditOutlined, DeleteOutlined, CodeOutlined } from '@ant-design/icons';
import { getCategoryTree, createApi } from '@/services/SourceAPIServices/sourceAPI';

interface CategoryNode {
  id: number;
  name: string;
  parentId: number;
  level: number;
  children?: CategoryNode[];
}

interface ParamItem {
  key: string;
  paramName: string;
  paramPosition: string;
  dataType: string;
  required: boolean;
  defaultValue: string;
  description: string;
}

interface BodyParamItem {
  key: string;
  paramName: string;
  dataType: string;
  required: boolean;
  defaultValue: string;
  description: string;
}

interface ResponseParamItem {
  key: string;
  paramName: string;
  dataType: string;
  description: string;
}

const APIManualRegistration: React.FC = () => {
  const [currentStep, setCurrentStep] = useState(1);
  const [categoryList, setCategoryList] = useState<{ value: number; label: string }[]>([]);
  const [form] = Form.useForm();
  const [errors, setErrors] = useState<string[]>([]);

  const [inputParams, setInputParams] = useState<ParamItem[]>([
    { key: '1', paramName: 'id', paramPosition: 'query', dataType: 'Int', required: true, defaultValue: '3412443243', description: '企业唯一id' },
    { key: '2', paramName: 'id', paramPosition: 'query', dataType: 'Int', required: true, defaultValue: '3412443243', description: '企业唯一id' },
  ]);

  const [bodyParams, setBodyParams] = useState<BodyParamItem[]>([
    { key: '1', paramName: 'name', dataType: 'String', required: true, defaultValue: '', description: '' },
    { key: '2', paramName: 'sex', dataType: 'Int', required: false, defaultValue: '', description: '' },
  ]);

  const [responseParams, setResponseParams] = useState<ResponseParamItem[]>([
    { key: '1', paramName: 'name', dataType: 'String', description: '' },
    { key: '2', paramName: 'sex', dataType: 'Int', description: '' },
  ]);

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    try {
      const res = await getCategoryTree();
      const options: { value: number; label: string }[] = [];
      const flattenCategories = (nodes: CategoryNode[], prefix = '') => {
        nodes.forEach(node => {
          const label = prefix ? `${prefix} - ${node.name}` : node.name;
          options.push({ value: node.id, label });
          if (node.children && node.children.length > 0) {
            flattenCategories(node.children, label);
          }
        });
      };
      flattenCategories(res.data);
      setCategoryList(options);
    } catch (error) {
      console.error('Failed to load categories:', error);
    }
  };

  const validateBasicInfo = (): boolean => {
    const errorsList: string[] = [];
    const values = form.getFieldsValue();

    if (!values.apiName || values.apiName.trim() === '') {
      errorsList.push('接口名称不能为空');
    }

    if (!values.categoryId) {
      errorsList.push('所属分类不能为空');
    }

    if (!values.protocol) {
      errorsList.push('协议不能为空');
    }

    if (!values.method) {
      errorsList.push('请求方式不能为空');
    }

    if (!values.url || values.url.trim() === '') {
      errorsList.push('接口路径不能为空');
    }

    if (!values.source || values.source.trim() === '') {
      errorsList.push('接口来源不能为空');
    }

    setErrors(errorsList);
    return errorsList.length === 0;
  };

  const handleNextStep = () => {
    if (validateBasicInfo()) {
      setErrors([]);
      setCurrentStep(2);
    }
  };

  const handlePrevStep = () => {
    setCurrentStep(1);
  };

  const handleCancel = () => {
    Modal.confirm({
      title: '确认取消',
      content: '确定要取消当前的接口注册吗？所有输入的信息将会丢失。',
      onOk: () => {
        window.history.back();
      },
    });
  };

  const handleSave = async () => {
    if (!validateBasicInfo()) return;

    try {
      const values = form.getFieldsValue();
      const apiData = {
        apiName: values.apiName,
        apiDescription: values.apiDescription || '',
        categoryId: values.categoryId,
        source: values.source,
        protocol: values.protocol,
        method: values.method,
        url: values.url,
        timeout: values.timeout ? Number(values.timeout) * 1000 : 30000,
        retryCount: values.retryCount || 0,
        requestParams: inputParams.map(p => ({
          paramName: p.paramName,
          paramType: p.paramPosition.toUpperCase(),
          dataType: p.dataType.toUpperCase(),
          required: p.required,
          defaultValue: p.defaultValue,
          description: p.description,
          sortOrder: 1,
        })),
        responseExample: JSON.stringify({ data: responseParams.reduce((acc, p) => ({ ...acc, [p.paramName]: null }), {}) }),
        remark: values.remark || '',
      };

      await createApi(apiData);
      message.success('接口注册成功');
      window.history.back();
    } catch (error) {
      message.error('接口注册失败');
    }
  };

  const handleAddInputParam = () => {
    const newKey = String(Date.now());
    setInputParams([...inputParams, {
      key: newKey,
      paramName: '',
      paramPosition: 'query',
      dataType: 'String',
      required: false,
      defaultValue: '',
      description: '',
    }]);
  };

  const handleEditInputParam = (key: string, field: keyof ParamItem, value: string | boolean) => {
    setInputParams(inputParams.map(p =>
      p.key === key ? { ...p, [field]: value } : p
    ));
  };

  const handleDeleteInputParam = (key: string) => {
    setInputParams(inputParams.filter(p => p.key !== key));
  };

  const handleAddBodyParam = () => {
    const newKey = String(Date.now());
    setBodyParams([...bodyParams, {
      key: newKey,
      paramName: '',
      dataType: 'String',
      required: false,
      defaultValue: '',
      description: '',
    }]);
  };

  const handleEditBodyParam = (key: string, field: keyof BodyParamItem, value: string | boolean) => {
    setBodyParams(bodyParams.map(p =>
      p.key === key ? { ...p, [field]: value } : p
    ));
  };

  const handleDeleteBodyParam = (key: string) => {
    setBodyParams(bodyParams.filter(p => p.key !== key));
  };

  const handleAddResponseParam = () => {
    const newKey = String(Date.now());
    setResponseParams([...responseParams, {
      key: newKey,
      paramName: '',
      dataType: 'String',
      description: '',
    }]);
  };

  const handleEditResponseParam = (key: string, field: keyof ResponseParamItem, value: string) => {
    setResponseParams(responseParams.map(p =>
      p.key === key ? { ...p, [field]: value } : p
    ));
  };

  const handleDeleteResponseParam = (key: string) => {
    setResponseParams(responseParams.filter(p => p.key !== key));
  };

  const handleJsonImport = (type: 'input' | 'body' | 'response') => {
    Modal.info({
      title: 'JSON数据导入',
      content: (
        <div>
          <p>请输入JSON数据:</p>
          <textarea style={{ width: '100%', height: 150 }} />
        </div>
      ),
      footer: [
        <Button key="ok" type="primary">确定</Button>,
        <Button key="cancel">取消</Button>,
      ],
    });
  };

  const inputParamColumns = [
    { title: '*参数名称', dataIndex: 'paramName', key: 'paramName', width: 120 },
    {
      title: '*参数位置',
      dataIndex: 'paramPosition',
      key: 'paramPosition',
      width: 100,
      render: (text: string, record: ParamItem) => (
        <Select
          value={record.paramPosition}
          onChange={(value) => handleEditInputParam(record.key, 'paramPosition', value)}
          style={{ width: '100%' }}
        >
          <Select.Option value="query">query</Select.Option>
          <Select.Option value="path">path</Select.Option>
          <Select.Option value="header">header</Select.Option>
        </Select>
      ),
    },
    {
      title: '*数据类型',
      dataIndex: 'dataType',
      key: 'dataType',
      width: 100,
      render: (text: string, record: ParamItem) => (
        <Select
          value={record.dataType}
          onChange={(value) => handleEditInputParam(record.key, 'dataType', value)}
          style={{ width: '100%' }}
        >
          <Select.Option value="String">String</Select.Option>
          <Select.Option value="Int">Int</Select.Option>
          <Select.Option value="Float">Float</Select.Option>
          <Select.Option value="Object">Object</Select.Option>
          <Select.Option value="Array">Array</Select.Option>
        </Select>
      ),
    },
    {
      title: '*是否必填',
      dataIndex: 'required',
      key: 'required',
      width: 80,
      render: (text: boolean, record: ParamItem) => (
        <Select
          value={record.required}
          onChange={(value) => handleEditInputParam(record.key, 'required', value)}
          style={{ width: '100%' }}
        >
          <Select.Option value={true}>是</Select.Option>
          <Select.Option value={false}>否</Select.Option>
        </Select>
      ),
    },
    {
      title: '默认值',
      dataIndex: 'defaultValue',
      key: 'defaultValue',
      width: 120,
      render: (text: string, record: ParamItem) => (
        <Input
          value={record.defaultValue}
          onChange={(e) => handleEditInputParam(record.key, 'defaultValue', e.target.value)}
        />
      ),
    },
    {
      title: '参数描述',
      dataIndex: 'description',
      key: 'description',
      render: (text: string, record: ParamItem) => (
        <Input
          value={record.description}
          onChange={(e) => handleEditInputParam(record.key, 'description', e.target.value)}
        />
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_: any, record: ParamItem) => (
        <div style={{ display: 'flex', gap: 8 }}>
          <Button type="link" size="small" icon={<EditOutlined />}>编辑</Button>
          <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteInputParam(record.key)}>删除</Button>
        </div>
      ),
    },
  ];

  const bodyParamColumns = [
    { title: '*参数名称', dataIndex: 'paramName', key: 'paramName', width: 150 },
    {
      title: '*数据类型',
      dataIndex: 'dataType',
      key: 'dataType',
      width: 120,
      render: (text: string, record: BodyParamItem) => (
        <Select
          value={record.dataType}
          onChange={(value) => handleEditBodyParam(record.key, 'dataType', value)}
          style={{ width: '100%' }}
        >
          <Select.Option value="String">String</Select.Option>
          <Select.Option value="Int">Int</Select.Option>
          <Select.Option value="Float">Float</Select.Option>
          <Select.Option value="Object">Object</Select.Option>
          <Select.Option value="Array">Array</Select.Option>
        </Select>
      ),
    },
    {
      title: '*是否必填',
      dataIndex: 'required',
      key: 'required',
      width: 100,
      render: (text: boolean, record: BodyParamItem) => (
        <Select
          value={record.required}
          onChange={(value) => handleEditBodyParam(record.key, 'required', value)}
          style={{ width: '100%' }}
        >
          <Select.Option value={true}>是</Select.Option>
          <Select.Option value={false}>否</Select.Option>
        </Select>
      ),
    },
    {
      title: '默认值',
      dataIndex: 'defaultValue',
      key: 'defaultValue',
      width: 120,
      render: (text: string, record: BodyParamItem) => (
        <Input
          value={record.defaultValue}
          onChange={(e) => handleEditBodyParam(record.key, 'defaultValue', e.target.value)}
        />
      ),
    },
    {
      title: '参数说明',
      dataIndex: 'description',
      key: 'description',
      render: (text: string, record: BodyParamItem) => (
        <Input
          value={record.description}
          onChange={(e) => handleEditBodyParam(record.key, 'description', e.target.value)}
        />
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_: any, record: BodyParamItem) => (
        <div style={{ display: 'flex', gap: 8 }}>
          <Button type="link" size="small" icon={<EditOutlined />}>编辑</Button>
          <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteBodyParam(record.key)}>删除</Button>
          <Button type="link" size="small">添加下级</Button>
        </div>
      ),
    },
  ];

  const responseParamColumns = [
    { title: '*参数名称', dataIndex: 'paramName', key: 'paramName', width: 150 },
    {
      title: '*数据类型',
      dataIndex: 'dataType',
      key: 'dataType',
      width: 120,
      render: (text: string, record: ResponseParamItem) => (
        <Select
          value={record.dataType}
          onChange={(value) => handleEditResponseParam(record.key, 'dataType', value)}
          style={{ width: '100%' }}
        >
          <Select.Option value="String">String</Select.Option>
          <Select.Option value="Int">Int</Select.Option>
          <Select.Option value="Float">Float</Select.Option>
          <Select.Option value="Object">Object</Select.Option>
          <Select.Option value="Array">Array</Select.Option>
        </Select>
      ),
    },
    {
      title: '参数说明',
      dataIndex: 'description',
      key: 'description',
      render: (text: string, record: ResponseParamItem) => (
        <Input
          value={record.description}
          onChange={(e) => handleEditResponseParam(record.key, 'description', e.target.value)}
        />
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      render: (_: any, record: ResponseParamItem) => (
        <div style={{ display: 'flex', gap: 8 }}>
          <Button type="link" size="small" icon={<EditOutlined />}>编辑</Button>
          <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteResponseParam(record.key)}>删除</Button>
          <Button type="link" size="small">添加下级</Button>
        </div>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Card style={{ borderRadius: 12 }}>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 32 }}>
          <Button type="link" icon={<ArrowLeftOutlined />} onClick={() => window.history.back()}>
            返回
          </Button>
          <div style={{ flex: 1, display: 'flex', justifyContent: 'center', gap: 48 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <div style={{ width: 32, height: 32, borderRadius: 16, backgroundColor: currentStep >= 1 ? '#1890ff' : '#e8e8e8', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontWeight: 600 }}>
                1
              </div>
              <span style={{ fontWeight: currentStep >= 1 ? 600 : 400 }}>基本信息</span>
            </div>
            <div style={{ width: 48, height: 2, backgroundColor: currentStep >= 2 ? '#1890ff' : '#e8e8e8' }} />
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <div style={{ width: 32, height: 32, borderRadius: 16, backgroundColor: currentStep >= 2 ? '#1890ff' : '#e8e8e8', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontWeight: 600 }}>
                2
              </div>
              <span style={{ fontWeight: currentStep >= 2 ? 600 : 400 }}>参数配置</span>
            </div>
          </div>
          <div style={{ width: 100 }} />
        </div>

        {errors.length > 0 && (
          <div style={{ marginBottom: 16, padding: 12, backgroundColor: '#fff2f0', borderRadius: 8 }}>
            {errors.map((error, index) => (
              <p key={index} style={{ color: '#ff4d4f', fontSize: 12, margin: 0, display: 'flex', alignItems: 'center', gap: 8 }}>
                <span>!</span>{error}
              </p>
            ))}
          </div>
        )}

        <Form form={form} layout="vertical">
          {currentStep === 1 && (
            <div>
              <div style={{ marginBottom: 24 }}>
                <h3 style={{ margin: 0, fontSize: 16, fontWeight: 600, marginBottom: 16 }}>基本信息</h3>
                <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
                  <Form.Item label="*接口分类" name="categoryId" style={{ width: 'calc(50% - 8px)' }}>
                    <Select placeholder="请选择接口分类" style={{ width: '100%' }} options={categoryList} />
                  </Form.Item>
                  <Form.Item label="*接口名称" name="apiName" style={{ width: 'calc(50% - 8px)' }}>
                    <Input placeholder="请输入接口名称" maxLength={30} />
                  </Form.Item>
                  <Form.Item label="*接口来源" name="source" style={{ width: 'calc(50% - 8px)' }}>
                    <Select placeholder="请选择接口来源" style={{ width: '100%' }}>
                      <Select.Option value="数据服务">数据服务</Select.Option>
                      <Select.Option value="用户中心">用户中心</Select.Option>
                      <Select.Option value="订单系统">订单系统</Select.Option>
                      <Select.Option value="支付平台">支付平台</Select.Option>
                      <Select.Option value="物流系统">物流系统</Select.Option>
                    </Select>
                  </Form.Item>
                  <Form.Item label="接口描述" name="apiDescription" style={{ width: 'calc(50% - 8px)' }}>
                    <Input.TextArea rows={3} placeholder="请输入接口描述" maxLength={200} />
                  </Form.Item>
                </div>
              </div>

              <div style={{ marginBottom: 24 }}>
                <h3 style={{ margin: 0, fontSize: 16, fontWeight: 600, marginBottom: 16 }}>API参数</h3>
                <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
                  <Form.Item label="*协议" name="protocol" style={{ width: 'calc(25% - 12px)' }}>
                    <Select placeholder="请选择协议" style={{ width: '100%' }}>
                      <Select.Option value="HTTP">HTTP</Select.Option>
                      <Select.Option value="HTTPS">HTTPS</Select.Option>
                    </Select>
                  </Form.Item>
                  <Form.Item label="*请求方式" name="method" style={{ width: 'calc(25% - 12px)' }}>
                    <Select placeholder="请选择请求方式" style={{ width: '100%' }}>
                      <Select.Option value="GET">GET</Select.Option>
                      <Select.Option value="POST">POST</Select.Option>
                      <Select.Option value="PUT">PUT</Select.Option>
                      <Select.Option value="DELETE">DELETE</Select.Option>
                    </Select>
                  </Form.Item>
                  <Form.Item label="*接口路径" name="url" style={{ width: 'calc(50% - 12px)' }}>
                    <Input placeholder="请输入接口路径" maxLength={500} />
                  </Form.Item>
                  <Form.Item label="超时时间（秒）" name="timeout" style={{ width: 'calc(25% - 12px)' }}>
                    <InputNumber placeholder="默认30" min={1} max={1800} style={{ width: '100%' }} />
                  </Form.Item>
                  <Form.Item label="重试次数" name="retryCount" style={{ width: 'calc(25% - 12px)' }}>
                    <InputNumber placeholder="默认0" min={0} max={3} style={{ width: '100%' }} />
                  </Form.Item>
                </div>
              </div>
            </div>
          )}

          {currentStep === 2 && (
            <div>
              <div style={{ marginBottom: 24 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                  <h3 style={{ margin: 0, fontSize: 16, fontWeight: 600 }}>输入参数</h3>
                  <div style={{ display: 'flex', gap: 8 }}>
                    <Button type="link" icon={<PlusOutlined />} onClick={handleAddInputParam}>新增参数</Button>
                    <Button type="link" icon={<CodeOutlined />} onClick={() => handleJsonImport('input')}>Json数据导入</Button>
                  </div>
                </div>
                <Table columns={inputParamColumns} dataSource={inputParams} pagination={false} bordered />
              </div>

              <div style={{ marginBottom: 24 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                  <h3 style={{ margin: 0, fontSize: 16, fontWeight: 600 }}>请求Body</h3>
                  <div style={{ display: 'flex', gap: 8 }}>
                    <Button type="link" icon={<PlusOutlined />} onClick={handleAddBodyParam}>新增参数</Button>
                    <Button type="link" icon={<CodeOutlined />} onClick={() => handleJsonImport('body')}>Json数据导入</Button>
                  </div>
                </div>
                <Table columns={bodyParamColumns} dataSource={bodyParams} pagination={false} bordered />
              </div>

              <div style={{ marginBottom: 24 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                  <h3 style={{ margin: 0, fontSize: 16, fontWeight: 600 }}>*返回参数</h3>
                  <div style={{ display: 'flex', gap: 8 }}>
                    <Button type="link" icon={<PlusOutlined />} onClick={handleAddResponseParam}>新增参数</Button>
                    <Button type="link" icon={<CodeOutlined />} onClick={() => handleJsonImport('response')}>Json数据导入</Button>
                  </div>
                </div>
                <Table columns={responseParamColumns} dataSource={responseParams} pagination={false} bordered />
              </div>
            </div>
          )}
        </Form>

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 32 }}>
          {currentStep === 2 && (
            <Button type="primary" style={{ backgroundColor: '#52c41a', borderColor: '#52c41a' }}>测试</Button>
          )}
          <div style={{ display: 'flex', gap: 12 }}>
            <Button onClick={handleCancel}>取消</Button>
            {currentStep === 2 && <Button onClick={handlePrevStep}>上一步</Button>}
            {currentStep === 1 && <Button type="primary" onClick={handleNextStep}>下一步</Button>}
            <Button type="primary" onClick={handleSave}>保存并退出</Button>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default APIManualRegistration;
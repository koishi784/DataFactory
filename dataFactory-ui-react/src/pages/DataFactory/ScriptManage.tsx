import React, { useState, useEffect, useCallback, useMemo } from 'react';
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
  Upload,
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
  UploadOutlined,
  SearchOutlined,
  ExperimentOutlined,
} from '@ant-design/icons';
import type { UploadFile, UploadProps } from 'antd/es/upload/interface';
import {
  getScriptCategoryTree,
  getScriptList,
  createScript,
  updateScript,
  deleteScript,
  publishScript,
  disableScript,
  batchPublishScripts,
  batchDisableScripts,
  debugScript,
  createScriptCategory,
  updateScriptCategory,
  deleteScriptCategory,
  type ScriptItem,
  type ScriptListParams,
  type ScriptCategoryTreeNode,
  type DebugScriptResult,
} from '@/services/ScriptManage/scriptManageAPI';

const { TextArea } = Input;

const STATUS_MAP: Record<number, { label: string; color: string }> = {
  0: { label: '未发布', color: 'default' },
  1: { label: '已发布', color: 'success' },
  2: { label: '已停用', color: 'warning' },
};

const DATA_TYPES = ['String', 'Int', 'Float', 'Boolean', 'Object', 'Array'];

const SCRIPT_NAME_REGEX = /^[\u4e00-\u9fa5a-zA-Z]+$/;
const IDENTIFIER_REGEX = /^[a-zA-Z][a-zA-Z0-9_]*$/;

interface ParamRow {
  key: string;
  paramName: string;
  paramType: string;
  description: string;
  required?: boolean;
  editing?: boolean;
  isNew?: boolean;
}

interface CategoryFormValues {
  name: string;
  parentId?: number;
  sortOrder?: number;
}

const ScriptManage: React.FC = () => {
  const [categoryTree, setCategoryTree] = useState<ScriptCategoryTreeNode[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [scriptList, setScriptList] = useState<ScriptItem[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<number[]>([]);
  const [searchParams, setSearchParams] = useState<ScriptListParams>({
    pageNum: 1,
    pageSize: 20,
    keyword: '',
    status: '',
  });
  const [categorySearchText, setCategorySearchText] = useState('');

  const [modalVisible, setModalVisible] = useState(false);
  const [isEdit, setIsEdit] = useState(false);
  const [editingScript, setEditingScript] = useState<ScriptItem | null>(null);
  const [form] = Form.useForm();
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [inputParams, setInputParams] = useState<ParamRow[]>([]);
  const [outputParams, setOutputParams] = useState<ParamRow[]>([]);

  const [debugModalVisible, setDebugModalVisible] = useState(false);
  const [debugScriptId, setDebugScriptId] = useState<number | null>(null);
  const [debugResult, setDebugResult] = useState<DebugScriptResult | null>(null);
  const [debugLoading, setDebugLoading] = useState(false);
  const [debugParams, setDebugParams] = useState('');

  const [categoryModalVisible, setCategoryModalVisible] = useState(false);
  const [categoryModalMode, setCategoryModalMode] = useState<'add' | 'edit'>('add');
  const [editingCategory, setEditingCategory] = useState<ScriptCategoryTreeNode | null>(null);
  const [categoryParentId, setCategoryParentId] = useState<number | undefined>(undefined);
  const [categoryForm] = Form.useForm<CategoryFormValues>();

  const fetchCategoryTree = useCallback(async () => {
    try {
      const res = await getScriptCategoryTree();
      setCategoryTree(res.data);
    } catch (error) {
      console.error('查询分类树失败:', error);
      message.error('查询分类树失败');
    }
  }, []);

  const fetchScriptList = useCallback(async (params?: ScriptListParams) => {
    setLoading(true);
    try {
      const res = await getScriptList(params);
      const records = res.data.records || [];
      const sortedRecords = [...records].sort((a, b) => {
        if (a.status !== b.status) {
          return a.status - b.status;
        }
        return new Date(b.updateTime).getTime() - new Date(a.updateTime).getTime();
      });
      setScriptList(sortedRecords);
      setTotal(res.data.total || 0);
    } catch (error) {
      console.error('查询脚本列表失败:', error);
      message.error('查询脚本列表失败');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCategoryTree();
    fetchScriptList();
  }, [fetchCategoryTree, fetchScriptList]);

  const refreshList = useCallback(() => {
    fetchScriptList({
      pageNum: searchParams.pageNum,
      pageSize: searchParams.pageSize,
      categoryId: selectedCategory ?? undefined,
      keyword: searchParams.keyword || undefined,
      status: searchParams.status || undefined,
    });
  }, [fetchScriptList, searchParams, selectedCategory]);

  const handleCategorySelect = (selectedKeys: React.Key[]) => {
    const id = selectedKeys.length > 0 ? Number(selectedKeys[0]) : null;
    setSelectedCategory(id);
    fetchScriptList({
      pageNum: 1,
      pageSize: searchParams.pageSize,
      categoryId: id ?? undefined,
      keyword: searchParams.keyword || undefined,
      status: searchParams.status || undefined,
    });
  };

  const handleSearch = () => {
    fetchScriptList({
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
    fetchScriptList({
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
    fetchScriptList({
      pageNum: pagination.current || 1,
      pageSize: pagination.pageSize || 20,
      categoryId: selectedCategory ?? undefined,
      keyword: searchParams.keyword || undefined,
      status: searchParams.status || undefined,
    });
  };

  const getCategoryName = (categoryId: number): string => {
    const findNode = (nodes: ScriptCategoryTreeNode[]): string | null => {
      for (const node of nodes) {
        if (node.id === categoryId) return node.name;
        if (node.children && node.children.length > 0) {
          const found = findNode(node.children);
          if (found) return found;
        }
      }
      return null;
    };
    return findNode(categoryTree) || '-';
  };

  const handleAddScript = () => {
    setIsEdit(false);
    setEditingScript(null);
    form.resetFields();
    setFileList([]);
    setInputParams([]);
    setOutputParams([]);
    if (selectedCategory) {
      form.setFieldsValue({ categoryId: selectedCategory });
    }
    form.setFieldsValue({ scriptType: 'PYTHON' });
    setModalVisible(true);
  };

  const handleEditScript = (record: ScriptItem) => {
    setIsEdit(true);
    setEditingScript(record);
    form.resetFields();
    setFileList([
      {
        uid: '-1',
        name: record.fileName,
        status: 'done',
      },
    ]);
    form.setFieldsValue({
      scriptName: record.scriptName,
      scriptType: record.scriptType,
      categoryId: record.categoryId,
      className: '',
      functionName: '',
      description: record.description,
    });
    setInputParams([]);
    setOutputParams([]);
    setModalVisible(true);
  };

  const handlePublish = (id: number) => {
    Modal.confirm({
      title: '确认发布',
      content: '确定要发布该脚本吗？',
      onOk: async () => {
        try {
          await publishScript(id);
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
      content: '确定要停用该脚本吗？',
      onOk: async () => {
        try {
          await disableScript(id);
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
      content: '确定要删除该脚本吗？',
      onOk: async () => {
        try {
          await deleteScript(id);
          message.success('删除成功');
          refreshList();
        } catch (error) {
          message.error('删除失败');
        }
      },
    });
  };

  const handleBatchPublish = () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要发布的脚本');
      return;
    }
    const hasPublished = scriptList
      .filter((item) => selectedRowKeys.includes(item.id))
      .some((item) => item.status === 1);
    if (hasPublished) {
      message.error('所选脚本中包含已发布状态的数据，操作不合法');
      return;
    }
    Modal.confirm({
      title: '批量发布',
      content: `确定要发布选中的 ${selectedRowKeys.length} 个脚本吗？`,
      onOk: async () => {
        try {
          await batchPublishScripts({ ids: selectedRowKeys });
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
      message.warning('请选择要停用的脚本');
      return;
    }
    const hasInvalid = scriptList
      .filter((item) => selectedRowKeys.includes(item.id))
      .some((item) => item.status !== 1);
    if (hasInvalid) {
      message.error('所选脚本中包含未发布或已停用状态的数据，操作不合法');
      return;
    }
    Modal.confirm({
      title: '批量停用',
      content: `确定要停用选中的 ${selectedRowKeys.length} 个脚本吗？`,
      onOk: async () => {
        try {
          await batchDisableScripts({ ids: selectedRowKeys });
          message.success('批量停用成功');
          setSelectedRowKeys([]);
          refreshList();
        } catch (error) {
          message.error('批量停用失败');
        }
      },
    });
  };

  const validateScriptNameUnique = async (_: any, value: string) => {
    if (!value) return Promise.resolve();
    if (!SCRIPT_NAME_REGEX.test(value)) {
      return Promise.reject(new Error('脚本名称只支持中文和英文大小写'));
    }
    if (value.length > 50) {
      return Promise.reject(new Error('脚本名称最大50字符'));
    }
    try {
      const res = await getScriptList({ keyword: value, pageSize: 100 });
      const exists = (res.data.records || []).some(
        (item: ScriptItem) =>
          item.scriptName === value && (!isEdit || item.id !== editingScript?.id),
      );
      if (exists) {
        return Promise.reject(new Error('脚本名称已存在'));
      }
    } catch (e) {}
    return Promise.resolve();
  };

  const beforeUpload: UploadProps['beforeUpload'] = (file) => {
    const isPythonFile = file.name.toLowerCase().endsWith('.py');
    if (!isPythonFile) {
      message.error('只能上传 Python 脚本文件（.py）');
      return Upload.LIST_IGNORE;
    }
    const isLt10M = file.size / 1024 / 1024 < 10;
    if (!isLt10M) {
      message.error('脚本文件不能超过 10MB');
      return Upload.LIST_IGNORE;
    }
    return false;
  };

  const handleFileChange: UploadProps['onChange'] = (info) => {
    let newFileList = [...info.fileList];
    newFileList = newFileList.slice(-1);
    setFileList(newFileList);
  };

  const handleModalSubmit = async () => {
    try {
      const values = await form.validateFields();

      if (!isEdit && fileList.length === 0) {
        message.error('信息填写不完整，无法保存');
        return;
      }
      if (fileList.length > 0) {
        const fileName = fileList[0].name;
        if (!fileName.toLowerCase().endsWith('.py')) {
          message.error('文件类型错误，无法保存');
          return;
        }
      }

      const inputParamsData = inputParams
        .filter((p) => p.paramName && p.paramType && !p.isNew)
        .map((p) => ({
          paramName: p.paramName,
          paramType: p.paramType,
          description: p.description,
        }));
      const outputParamsData = outputParams
        .filter((p) => p.paramName && p.paramType && !p.isNew)
        .map((p) => ({
          paramName: p.paramName,
          paramType: p.paramType,
          description: p.description,
        }));

      const submitData = {
        scriptName: values.scriptName,
        categoryId: values.categoryId,
        fileId: 1001,
        description: values.description || '',
        inputParams: inputParamsData,
        outputParams: outputParamsData,
      };

      if (isEdit && editingScript) {
        await updateScript(editingScript.id, submitData);
        message.success('编辑成功');
        setModalVisible(false);
        refreshList();
      } else {
        await createScript(submitData);
        message.success('新增成功');
        setModalVisible(false);
        refreshList();
      }
    } catch (error) {
      console.error('保存失败:', error);
    }
  };

  const handleAddInputParam = () => {
    const newKey = String(Date.now());
    setInputParams([
      ...inputParams,
      { key: newKey, paramName: '', paramType: 'String', description: '', required: false, editing: true, isNew: true },
    ]);
  };

  const handleEditInputParam = (key: string) => {
    setInputParams(inputParams.map((p) => (p.key === key ? { ...p, editing: true } : p)));
  };

  const handleSaveInputParam = (key: string) => {
    const param = inputParams.find((p) => p.key === key);
    if (!param) return;
    if (!param.paramName) {
      message.error('参数名称不能为空');
      return;
    }
    if (!IDENTIFIER_REGEX.test(param.paramName)) {
      message.error('参数名称只支持英文大小写、数字及下划线，且只能英文开头');
      return;
    }
    setInputParams(inputParams.map((p) => (p.key === key ? { ...p, editing: false, isNew: false } : p)));
  };

  const handleCancelInputParam = (key: string, isNew: boolean) => {
    if (isNew) {
      setInputParams(inputParams.filter((p) => p.key !== key));
    } else {
      setInputParams(inputParams.map((p) => (p.key === key ? { ...p, editing: false } : p)));
    }
  };

  const handleDeleteInputParam = (key: string) => {
    setInputParams(inputParams.filter((p) => p.key !== key));
  };

  const handleInputParamChange = (key: string, field: keyof ParamRow, value: string | boolean) => {
    setInputParams(inputParams.map((p) => (p.key === key ? { ...p, [field]: value } : p)));
  };

  const handleAddOutputParam = () => {
    const newKey = String(Date.now());
    setOutputParams([
      ...outputParams,
      { key: newKey, paramName: '', paramType: 'String', description: '', editing: true, isNew: true },
    ]);
  };

  const handleEditOutputParam = (key: string) => {
    setOutputParams(outputParams.map((p) => (p.key === key ? { ...p, editing: true } : p)));
  };

  const handleSaveOutputParam = (key: string) => {
    const param = outputParams.find((p) => p.key === key);
    if (!param) return;
    if (!param.paramName) {
      message.error('参数名称不能为空');
      return;
    }
    if (!IDENTIFIER_REGEX.test(param.paramName)) {
      message.error('参数名称只支持英文大小写、数字及下划线，且只能英文开头');
      return;
    }
    setOutputParams(outputParams.map((p) => (p.key === key ? { ...p, editing: false, isNew: false } : p)));
  };

  const handleCancelOutputParam = (key: string, isNew: boolean) => {
    if (isNew) {
      setOutputParams(outputParams.filter((p) => p.key !== key));
    } else {
      setOutputParams(outputParams.map((p) => (p.key === key ? { ...p, editing: false } : p)));
    }
  };

  const handleDeleteOutputParam = (key: string) => {
    setOutputParams(outputParams.filter((p) => p.key !== key));
  };

  const handleOutputParamChange = (key: string, field: keyof ParamRow, value: string | boolean) => {
    setOutputParams(outputParams.map((p) => (p.key === key ? { ...p, [field]: value } : p)));
  };

  const handleDebug = (id: number) => {
    setDebugScriptId(id);
    setDebugResult(null);
    setDebugParams('{}');
    setDebugModalVisible(true);
  };

  const handleExecuteDebug = async () => {
    if (!debugScriptId) return;
    setDebugLoading(true);
    try {
      let params: Record<string, any> = {};
      if (debugParams && debugParams.trim()) {
        try {
          params = JSON.parse(debugParams);
        } catch (e) {
          message.error('参数格式错误，请输入合法的JSON');
          return;
        }
      }
      const res = await debugScript(debugScriptId, { params });
      setDebugResult(res.data);
    } catch (error) {
      console.error('调试失败:', error);
      message.error('调试失败');
    } finally {
      setDebugLoading(false);
    }
  };

  const buildCategoryOptions = (nodes: ScriptCategoryTreeNode[], prefix = ''): { value: number; label: string }[] => {
    const options: { value: number; label: string }[] = [];
    nodes.forEach((node) => {
      const label = prefix ? `${prefix} - ${node.name}` : node.name;
      options.push({ value: node.id, label });
      if (node.children && node.children.length > 0) {
        options.push(...buildCategoryOptions(node.children, label));
      }
    });
    return options;
  };

  const categoryOptions = useMemo(() => buildCategoryOptions(categoryTree), [categoryTree]);

  const buildTreeData = (nodes: ScriptCategoryTreeNode[]): any[] => {
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

  const filterTreeData = (nodes: ScriptCategoryTreeNode[], searchText: string): any[] => {
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

  const handleEditCategory = (node: ScriptCategoryTreeNode) => {
    setCategoryModalMode('edit');
    setEditingCategory(node);
    setCategoryParentId(node.parentId);
    categoryForm.setFieldsValue({
      name: node.name,
      sortOrder: node.sortOrder,
    });
    setCategoryModalVisible(true);
  };

  const handleDeleteCategory = (node: ScriptCategoryTreeNode) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该分类吗？仅能删除无子分类且无关联脚本的分类。',
      onOk: async () => {
        try {
          await deleteScriptCategory(node.id);
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
        await createScriptCategory({
          name: values.name,
          parentId: categoryParentId ?? 0,
          sortOrder: values.sortOrder,
        });
        message.success('新增分类成功');
        setCategoryModalVisible(false);
        fetchCategoryTree();
      } else if (editingCategory) {
        await updateScriptCategory(editingCategory.id, {
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

  const findCategoryNode = (nodes: ScriptCategoryTreeNode[], key: string): ScriptCategoryTreeNode | null => {
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

  const inputParamColumns = [
    {
      title: '*参数名称',
      dataIndex: 'paramName',
      key: 'paramName',
      width: 150,
      render: (text: string, record: ParamRow) =>
        record.editing ? (
          <Input
            value={record.paramName}
            onChange={(e) => handleInputParamChange(record.key, 'paramName', e.target.value)}
            placeholder="请输入参数名称"
          />
        ) : (
          text
        ),
    },
    {
      title: '*数据类型',
      dataIndex: 'paramType',
      key: 'paramType',
      width: 120,
      render: (text: string, record: ParamRow) =>
        record.editing ? (
          <Select
            value={record.paramType}
            onChange={(value) => handleInputParamChange(record.key, 'paramType', value)}
            style={{ width: '100%' }}
          >
            {DATA_TYPES.map((type) => (
              <Select.Option key={type} value={type}>
                {type}
              </Select.Option>
            ))}
          </Select>
        ) : (
          text
        ),
    },
    {
      title: '*是否必填',
      dataIndex: 'required',
      key: 'required',
      width: 100,
      render: (text: boolean, record: ParamRow) =>
        record.editing ? (
          <Select
            value={record.required ?? false}
            onChange={(value) => handleInputParamChange(record.key, 'required', value)}
            style={{ width: '100%' }}
          >
            <Select.Option value={true}>是</Select.Option>
            <Select.Option value={false}>否</Select.Option>
          </Select>
        ) : text ? (
          '是'
        ) : (
          '否'
        ),
    },
    {
      title: '参数描述',
      dataIndex: 'description',
      key: 'description',
      render: (text: string, record: ParamRow) =>
        record.editing ? (
          <Input
            value={record.description}
            onChange={(e) => handleInputParamChange(record.key, 'description', e.target.value)}
            placeholder="请输入参数描述"
          />
        ) : (
          text
        ),
    },
    {
      title: '操作',
      key: 'action',
      width: 160,
      fixed: 'right' as const,
      render: (_: any, record: ParamRow) =>
        record.editing ? (
          <Space>
            <Button type="link" size="small" onClick={() => handleSaveInputParam(record.key)}>
              保存
            </Button>
            <Button type="link" size="small" onClick={() => handleCancelInputParam(record.key, !!record.isNew)}>
              取消
            </Button>
          </Space>
        ) : (
          <Space>
            <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEditInputParam(record.key)}>
              编辑
            </Button>
            <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteInputParam(record.key)}>
              删除
            </Button>
          </Space>
        ),
    },
  ];

  const outputParamColumns = [
    {
      title: '*参数名称',
      dataIndex: 'paramName',
      key: 'paramName',
      width: 150,
      render: (text: string, record: ParamRow) =>
        record.editing ? (
          <Input
            value={record.paramName}
            onChange={(e) => handleOutputParamChange(record.key, 'paramName', e.target.value)}
            placeholder="请输入参数名称"
          />
        ) : (
          text
        ),
    },
    {
      title: '*数据类型',
      dataIndex: 'paramType',
      key: 'paramType',
      width: 120,
      render: (text: string, record: ParamRow) =>
        record.editing ? (
          <Select
            value={record.paramType}
            onChange={(value) => handleOutputParamChange(record.key, 'paramType', value)}
            style={{ width: '100%' }}
          >
            {DATA_TYPES.map((type) => (
              <Select.Option key={type} value={type}>
                {type}
              </Select.Option>
            ))}
          </Select>
        ) : (
          text
        ),
    },
    {
      title: '参数描述',
      dataIndex: 'description',
      key: 'description',
      render: (text: string, record: ParamRow) =>
        record.editing ? (
          <Input
            value={record.description}
            onChange={(e) => handleOutputParamChange(record.key, 'description', e.target.value)}
            placeholder="请输入参数描述"
          />
        ) : (
          text
        ),
    },
    {
      title: '操作',
      key: 'action',
      width: 160,
      fixed: 'right' as const,
      render: (_: any, record: ParamRow) =>
        record.editing ? (
          <Space>
            <Button type="link" size="small" onClick={() => handleSaveOutputParam(record.key)}>
              保存
            </Button>
            <Button type="link" size="small" onClick={() => handleCancelOutputParam(record.key, !!record.isNew)}>
              取消
            </Button>
          </Space>
        ) : (
          <Space>
            <Button type="link" size="small" icon={<EditOutlined />} onClick={() => handleEditOutputParam(record.key)}>
              编辑
            </Button>
            <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteOutputParam(record.key)}>
              删除
            </Button>
          </Space>
        ),
    },
  ];

  const tableColumns = [
    {
      title: '脚本名称',
      dataIndex: 'scriptName',
      key: 'scriptName',
      width: 200,
    },
    {
      title: '脚本描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '脚本分类',
      dataIndex: 'categoryId',
      key: 'categoryId',
      width: 150,
      render: (categoryId: number) => getCategoryName(categoryId),
    },
    {
      title: '脚本状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: number) => {
        const item = STATUS_MAP[status];
        return <Tag color={item.color}>{item.label}</Tag>;
      },
    },
    {
      title: '更新日期',
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
      render: (_: any, record: ScriptItem) => {
        const actions: React.ReactNode[] = [];
        actions.push(
          <Button key="test" type="link" size="small" onClick={() => handleDebug(record.id)}>
            测试
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
            <Button key="edit" type="link" size="small" onClick={() => handleEditScript(record)}>
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

  const isFileUploadDisabled = isEdit && editingScript?.status === 2;

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
          <span style={{ fontWeight: 600 }}>脚本分类</span>
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
                  脚本状态：
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
                  脚本名称：
                </span>
                <Input
                  placeholder="请输入脚本名称"
                  style={{ width: 200 }}
                  value={searchParams.keyword || ''}
                  onChange={(e) => setSearchParams({ ...searchParams, keyword: e.target.value })}
                />
              </div>
              <Button onClick={handleReset}>重置</Button>
              <Button type="primary" onClick={handleSearch}>
                查询
              </Button>
            </div>
            <div style={{ marginLeft: 16 }}>
              <Button type="primary" icon={<PlusOutlined />} onClick={handleAddScript}>
                新增脚本
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
            dataSource={scriptList}
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
        title={isEdit ? '编辑脚本' : '新增脚本'}
        visible={modalVisible}
        onCancel={() => setModalVisible(false)}
        width={800}
        footer={[
          <Button key="cancel" onClick={() => setModalVisible(false)}>
            取消
          </Button>,
          <Button key="confirm" type="primary" onClick={handleModalSubmit}>
            确定
          </Button>,
        ]}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            label={
              <span>
                <span style={{ color: '#ff4d4f' }}>*</span> 文件上传
              </span>
            }
            name="fileId"
          >
            <Upload
              fileList={fileList}
              beforeUpload={beforeUpload}
              onChange={handleFileChange}
              disabled={isFileUploadDisabled}
              maxCount={1}
            >
              <Button icon={<UploadOutlined />} disabled={isFileUploadDisabled}>
                上传文件
              </Button>
            </Upload>
            {isFileUploadDisabled && (
              <div style={{ color: '#999', fontSize: 12, marginTop: 4 }}>
                已停用状态的脚本不能更换文件
              </div>
            )}
          </Form.Item>

          <Form.Item
            label={
              <span>
                <span style={{ color: '#ff4d4f' }}>*</span> 脚本名称
              </span>
            }
            name="scriptName"
            rules={[{ validator: validateScriptNameUnique }]}
          >
            <Input placeholder="请输入脚本名称" maxLength={50} />
          </Form.Item>

          <Form.Item
            label={
              <span>
                <span style={{ color: '#ff4d4f' }}>*</span> 脚本分类
              </span>
            }
            name="categoryId"
            rules={[{ required: true, message: '请选择脚本分类' }]}
          >
            <Select placeholder="请选择脚本分类" options={categoryOptions} />
          </Form.Item>

          <Form.Item
            label={
              <span>
                <span style={{ color: '#ff4d4f' }}>*</span> 脚本类型
              </span>
            }
            name="scriptType"
          >
            <Select disabled>
              <Select.Option value="PYTHON">Python</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            label={
              <span>
                <span style={{ color: '#ff4d4f' }}>*</span> 类名
              </span>
            }
            name="className"
            rules={[
              { required: true, message: '请输入类名' },
              {
                pattern: IDENTIFIER_REGEX,
                message: '类名只支持英文大小写、数字及下划线，且只能英文开头',
              },
            ]}
          >
            <Input placeholder="请输入类名" />
          </Form.Item>

          <Form.Item
            label={
              <span>
                <span style={{ color: '#ff4d4f' }}>*</span> 函数名
              </span>
            }
            name="functionName"
            rules={[
              { required: true, message: '请输入函数名' },
              {
                pattern: IDENTIFIER_REGEX,
                message: '函数名只支持英文大小写、数字及下划线，且只能英文开头',
              },
            ]}
          >
            <Input placeholder="请输入函数名" />
          </Form.Item>

          <Form.Item label="描述" name="description">
            <TextArea rows={3} placeholder="请输入描述" maxLength={500} />
          </Form.Item>
        </Form>

        <div style={{ marginTop: 24 }}>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: 8,
            }}
          >
            <span style={{ fontWeight: 600 }}>输入参数</span>
            <Button type="link" icon={<PlusOutlined />} onClick={handleAddInputParam}>
              新增参数
            </Button>
          </div>
          <Table
            columns={inputParamColumns}
            dataSource={inputParams}
            pagination={false}
            bordered
            size="small"
            rowKey="key"
            scroll={{ x: 800 }}
          />
        </div>

        <div style={{ marginTop: 24 }}>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: 8,
            }}
          >
            <span style={{ fontWeight: 600 }}>输出参数</span>
            <Button type="link" icon={<PlusOutlined />} onClick={handleAddOutputParam}>
              新增参数
            </Button>
          </div>
          <Table
            columns={outputParamColumns}
            dataSource={outputParams}
            pagination={false}
            bordered
            size="small"
            rowKey="key"
            scroll={{ x: 800 }}
          />
        </div>
      </Modal>

      <Modal
        title="在线调试"
        visible={debugModalVisible}
        onCancel={() => setDebugModalVisible(false)}
        width={700}
        footer={[
          <Button key="close" onClick={() => setDebugModalVisible(false)}>
            关闭
          </Button>,
          <Button
            key="execute"
            type="primary"
            icon={<ExperimentOutlined />}
            onClick={handleExecuteDebug}
            loading={debugLoading}
          >
            执行调试
          </Button>,
        ]}
      >
        <div style={{ marginBottom: 16 }}>
          <div style={{ fontWeight: 600, marginBottom: 8 }}>调试参数（JSON格式）：</div>
          <Input.TextArea
            rows={6}
            value={debugParams}
            onChange={(e) => setDebugParams(e.target.value)}
            placeholder='{"paramName": "testValue"}'
            style={{ fontFamily: 'monospace' }}
          />
        </div>
        {debugResult && (
          <div style={{ marginTop: 16 }}>
            <div style={{ marginBottom: 8 }}>
              <span style={{ fontWeight: 600 }}>调试结果：</span>
              <Tag color={debugResult.success ? 'success' : 'error'}>
                {debugResult.success ? '成功' : '失败'}
              </Tag>
              <span style={{ marginLeft: 16, color: '#666' }}>
                执行耗时：{debugResult.executeTime}ms
              </span>
            </div>
            {debugResult.success ? (
              <div>
                <p style={{ fontWeight: 600 }}>执行结果：</p>
                <pre
                  style={{
                    backgroundColor: '#f5f5f5',
                    padding: 12,
                    borderRadius: 8,
                    maxHeight: 200,
                    overflow: 'auto',
                  }}
                >
                  {debugResult.result}
                </pre>
              </div>
            ) : (
              <p style={{ color: '#ff4d4f' }}>错误信息：{debugResult.errorMessage}</p>
            )}
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

export default ScriptManage;

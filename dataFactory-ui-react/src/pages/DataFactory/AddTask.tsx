import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import {
  Card,
  Steps,
  Form,
  Input,
  Select,
  Button,
  Modal,
  message,
  Table,
  Tabs,
  Tag,
  Space,
  InputNumber,
} from 'antd';
import {
  PlayCircleOutlined,
  SaveOutlined,
  CloseOutlined,
  PlusOutlined,
  DeleteOutlined,
  ApiOutlined,
  CodeOutlined,
  ReloadOutlined,
  DatabaseOutlined,
  CloudServerOutlined,
  ExperimentOutlined,
} from '@ant-design/icons';
import { Graph } from '@antv/x6';
import {
  getTaskCategoryTree,
  getTaskList,
  createTask,
  updateTaskConfig,
  setTaskTriggerConfig,
  testRunTask,
  type TaskCategoryTreeNode,
  type TestRunTaskResult,
} from '@/services/TaskManage/taskManageAPI';

const { Step } = Steps;
const { TextArea } = Input;
const { TabPane } = Tabs;

const TASK_NAME_REGEX = /^[\u4e00-\u9fa5a-zA-Z]+$/;

const EXECUTE_STATUS_MAP: Record<number, { label: string; color: string }> = {
  0: { label: '等待', color: 'default' },
  1: { label: '执行中', color: 'processing' },
  2: { label: '成功', color: 'success' },
  3: { label: '失败', color: 'error' },
  4: { label: '已取消', color: 'warning' },
};

interface NodeItem {
  nodeId: string;
  nodeName: string;
  nodeType: 'START' | 'API' | 'SCRIPT' | 'MAPPING' | 'OUTPUT' | 'END';
  positionX: number;
  positionY: number;
  config: any;
}

interface EdgeItem {
  edgeId: string;
  sourceNodeId: string;
  targetNodeId: string;
  condition?: string | null;
}

// 节点类型配置
const NODE_TYPE_CONFIG: Record<string, { label: string; color: string; icon: React.ReactNode }> = {
  START: { label: 'START', color: '#1890ff', icon: null },
  END: { label: 'END', color: '#1890ff', icon: null },
  API: { label: 'API', color: '#52c41a', icon: <ApiOutlined /> },
  SCRIPT: { label: '脚本', color: '#722ed1', icon: <CodeOutlined /> },
  MAPPING: { label: '映射', color: '#fa8c16', icon: <ReloadOutlined /> },
  OUTPUT: { label: '输出', color: '#eb2f96', icon: <DatabaseOutlined /> },
};

// 节点类型面板
const NodeTypePanel: React.FC<{ onDragStart: (type: string) => void }> = ({ onDragStart }) => {
  const nodeTypes = [
    { group: '数据源节点', items: [{ type: 'API', label: 'API', icon: <ApiOutlined />, disabled: false }] },
    {
      group: '数据处理节点',
      items: [
        { type: 'SCRIPT', label: '脚本组件', icon: <CodeOutlined />, disabled: false },
        { type: 'MAPPING', label: '映射组件', icon: <ReloadOutlined />, disabled: false },
        { type: 'OUTPUT', label: '输出组件', icon: <DatabaseOutlined />, disabled: false },
        { type: 'CLEAN', label: '清洗组件', icon: <CloudServerOutlined />, disabled: true },
      ],
    },
  ];

  return (
    <div style={{ padding: 12, borderRight: '1px solid #e8e8e8', height: '100%', overflow: 'auto' }}>
      {nodeTypes.map((group) => (
        <div key={group.group} style={{ marginBottom: 16 }}>
          <div style={{ fontSize: 12, color: '#666', marginBottom: 8 }}>{group.group}</div>
          {group.items.map((item) => (
            <div
              key={item.type}
              draggable={!item.disabled}
              onDragStart={(e) => {
                if (!item.disabled) {
                  e.dataTransfer.setData('nodeType', item.type);
                  onDragStart(item.type);
                }
              }}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 8,
                padding: '8px 12px',
                marginBottom: 6,
                backgroundColor: item.disabled ? '#f5f5f5' : '#fff',
                border: '1px solid #e8e8e8',
                borderRadius: 4,
                cursor: item.disabled ? 'not-allowed' : 'move',
                opacity: item.disabled ? 0.5 : 1,
              }}
            >
              <span style={{ color: item.disabled ? '#bfbfbf' : '#1890ff' }}>{item.icon}</span>
              <span style={{ fontSize: 13, color: item.disabled ? '#bfbfbf' : '#333' }}>{item.label}</span>
            </div>
          ))}
        </div>
      ))}
    </div>
  );
};

// DAG 画布组件
const DAGCanvas: React.FC<{
  initialNodes: NodeItem[];
  initialEdges: EdgeItem[];
  onNodesChange: (nodes: NodeItem[]) => void;
  onEdgesChange: (edges: EdgeItem[]) => void;
  onNodeSelect: (nodeId: string | null) => void;
  selectedNodeId: string | null;
}> = ({ initialNodes, initialEdges, onNodesChange, onEdgesChange, onNodeSelect, selectedNodeId }) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const graphRef = useRef<Graph | null>(null);
  const syncTimerRef = useRef<number | null>(null);

  // 同步数据到父组件
  const syncData = useCallback(() => {
    const graph = graphRef.current;
    if (!graph) return;

    if (syncTimerRef.current) {
      clearTimeout(syncTimerRef.current);
    }
    syncTimerRef.current = window.setTimeout(() => {
      const x6Nodes = graph.getNodes();
      const x6Edges = graph.getEdges();

      const nodeList: NodeItem[] = x6Nodes.map((node) => {
        const data = node.getData() as NodeItem;
        const pos = node.getPosition();
        return {
          nodeId: node.id,
          nodeName: data?.nodeName || (node.attr('label/text') as string) || '',
          nodeType: data?.nodeType || 'API',
          positionX: pos.x,
          positionY: pos.y,
          config: data?.config || {},
        };
      });

      const edgeList: EdgeItem[] = x6Edges.map((edge) => ({
        edgeId: edge.id,
        sourceNodeId: typeof edge.source === 'string' ? edge.source : (edge.source as any).cell || '',
        targetNodeId: typeof edge.target === 'string' ? edge.target : (edge.target as any).cell || '',
        condition: null,
      }));

      onNodesChange(nodeList);
      onEdgesChange(edgeList);
    }, 50);
  }, [onNodesChange, onEdgesChange]);

  // 初始化图形
  useEffect(() => {
    if (!containerRef.current) return;

    // 注册自定义节点
    Graph.registerNode('dag-node', {
      inherit: 'rect',
      width: 120,
      height: 40,
      attrs: {
        body: {
          fill: '#fff',
          stroke: '#1890ff',
          strokeWidth: 2,
          rx: 4,
          ry: 4,
        },
        label: {
          text: '',
          fill: '#333',
          fontSize: 12,
          refX: 0.5,
          refY: 0.5,
          textAnchor: 'middle',
          textVerticalAnchor: 'middle',
        },
      },
      ports: {
        groups: {
          left: { position: 'left', attrs: { circle: { r: 4, magnet: true, stroke: '#1890ff', fill: '#fff' } } },
          right: { position: 'right', attrs: { circle: { r: 4, magnet: true, stroke: '#1890ff', fill: '#fff' } } },
        },
      },
    });

    const graph: Graph = new Graph({
      container: containerRef.current,
      panning: {
        enabled: true,
        modifiers: ['ctrl', 'meta'],
      },
      mousewheel: {
        enabled: true,
        modifiers: ['ctrl', 'meta'],
      },
      connecting: {
        snap: true,
        allowBlank: false,
        allowLoop: false,
        highlight: true,
        connector: { name: 'rounded' },
        router: { name: 'manhattan' },
        validateConnection({ sourceCell, targetCell }: any) {
          if (!sourceCell || !targetCell) return false;
          if (sourceCell.id === targetCell.id) return false;
          const targetData = targetCell.getData?.() as NodeItem;
          if (targetData?.nodeType === 'START') return false;
          const sourceData = sourceCell.getData?.() as NodeItem;
          if (sourceData?.nodeType === 'END') return false;
          return true;
        },
      },
    });

    graphRef.current = graph;

    // 初始节点
    initialNodes.forEach((node) => {
      const config = NODE_TYPE_CONFIG[node.nodeType] || { color: '#1890ff', label: node.nodeType };
      graph.addNode({
        id: node.nodeId,
        x: node.positionX,
        y: node.positionY,
        shape: 'dag-node',
        attrs: {
          body: { stroke: config.color },
          label: { text: node.nodeName },
        },
        data: node,
        ports: {
          items: [
            { id: 'port-left', group: 'left' },
            { id: 'port-right', group: 'right' },
          ],
        },
      });
    });

    // 初始边
    initialEdges.forEach((edge) => {
      graph.addEdge({
        id: edge.edgeId,
        source: edge.sourceNodeId,
        target: edge.targetNodeId,
      });
    });

    graph.centerContent();

    // 节点选择事件
    graph.on('node:click', ({ node }: any) => {
      onNodeSelect(node.id);
    });

    // 画布点击事件
    graph.on('blank:click', () => {
      onNodeSelect(null);
    });

    // 连接完成事件
    graph.on('edge:connected', () => {
      syncData();
    });

    // 边点击事件 - 删除连线
    graph.on('edge:click', ({ edge }: any) => {
      Modal.confirm({
        title: '删除连线',
        content: '确定要删除这条连线吗？',
        onOk: () => {
          graph.removeCell(edge);
          syncData();
        },
      });
    });

    // 节点移动事件
    graph.on('node:moved', () => {
      syncData();
    });

    // 边删除事件
    graph.on('edge:removed', () => {
      syncData();
    });

    // 节点删除事件
    graph.on('node:removed', () => {
      syncData();
    });

    return () => {
      if (syncTimerRef.current) {
        clearTimeout(syncTimerRef.current);
      }
      graph.dispose();
    };
  }, []);

  // 选中节点同步
  useEffect(() => {
    const graph = graphRef.current;
    if (!graph) return;

    const nodes = graph.getNodes();
    nodes.forEach((node) => {
      if (node.id === selectedNodeId) {
        node.attr('body/strokeWidth', 3);
      } else {
        const data = node.getData() as NodeItem;
        const config = NODE_TYPE_CONFIG[data?.nodeType] || { color: '#1890ff' };
        node.attr('body/strokeWidth', 2);
      }
    });
  }, [selectedNodeId]);

  // 处理节点删除
  const handleNodeDelete = useCallback((nodeId: string) => {
    const graph = graphRef.current;
    if (!graph) return;

    const node = graph.getCellById(nodeId);
    if (node) {
      graph.removeCell(node);
      onNodeSelect(null);
      syncData();
    }
  }, [onNodeSelect, syncData]);

  // 更新节点属性
  const updateNodeData = useCallback((nodeId: string, updates: Partial<NodeItem>) => {
    const graph = graphRef.current;
    if (!graph) return;

    const node = graph.getCellById(nodeId) as any;
    if (node) {
      if (updates.nodeName !== undefined) {
        node.attr('label/text', updates.nodeName);
      }
      const data = node.getData() || {};
      node.setData({ ...data, ...updates });
      syncData();
    }
  }, [syncData]);

  // 暴露方法
  useEffect(() => {
    (window as any).__dagCanvas = {
      deleteNode: handleNodeDelete,
      updateNodeData,
      addNode: (nodeType: string, x: number, y: number) => {
        const graph = graphRef.current;
        if (!graph) return;

        const config = NODE_TYPE_CONFIG[nodeType] || { color: '#1890ff', label: nodeType };
        const newNode = {
          nodeId: `node_${Date.now()}`,
          nodeName: config.label + '节点',
          nodeType: nodeType as any,
          positionX: x,
          positionY: y,
          config: {},
        };

        graph.addNode({
          id: newNode.nodeId,
          x: newNode.positionX,
          y: newNode.positionY,
          shape: 'dag-node',
          attrs: {
            body: { stroke: config.color },
            label: { text: newNode.nodeName },
          },
          data: newNode,
          ports: {
            items: [
              { id: 'port-left', group: 'left' },
              { id: 'port-right', group: 'right' },
            ],
          },
        });

        syncData();
      },
      getSelectedNode: (): NodeItem | null => {
        const graph = graphRef.current;
        if (!graph || !selectedNodeId) return null;
        const node = graph.getCellById(selectedNodeId) as any;
        return node?.getData() || null;
      },
    };
  }, [handleNodeDelete, updateNodeData, selectedNodeId, syncData]);

  // 拖放处理
  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    const nodeType = e.dataTransfer.getData('nodeType');
    if (!nodeType || !graphRef.current || !containerRef.current) return;

    const graph = graphRef.current;
    const bbox = containerRef.current.getBoundingClientRect();

    // 计算画布内坐标（相对容器坐标）
    const x = e.clientX - bbox.left - 60;
    const y = e.clientY - bbox.top - 20;

    if ((window as any).__dagCanvas?.addNode) {
      (window as any).__dagCanvas.addNode(nodeType, x, y);
    }
  }, []);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
  }, []);

  return (
    <div
      ref={containerRef}
      onDrop={handleDrop}
      onDragOver={handleDragOver}
      style={{
        flex: 1,
        backgroundColor: '#fafafa',
        backgroundImage:
          'linear-gradient(#e8e8e8 1px, transparent 1px), linear-gradient(90deg, #e8e8e8 1px, transparent 1px)',
        backgroundSize: '20px 20px',
      }}
    />
  );
};

const AddTask: React.FC = () => {
  const [currentStep, setCurrentStep] = useState(1);
  const [form] = Form.useForm();
  const [categoryTree, setCategoryTree] = useState<TaskCategoryTreeNode[]>([]);
  const [errors, setErrors] = useState<string[]>([]);
  const [testPassed, setTestPassed] = useState(false);

  const [taskId, setTaskId] = useState<number | null>(null);

  const [nodes, setNodes] = useState<NodeItem[]>([
    { nodeId: 'node_start', nodeName: 'START', nodeType: 'START', positionX: 100, positionY: 200, config: { description: '' } },
    { nodeId: 'node_end', nodeName: 'END', nodeType: 'END', positionX: 500, positionY: 200, config: { description: '' } },
  ]);
  const [edges, setEdges] = useState<EdgeItem[]>([]);
  const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);
  const [activePropTab, setActivePropTab] = useState<'attr' | 'config'>('attr');

  const [testModalVisible, setTestModalVisible] = useState(false);
  const [testLoading, setTestLoading] = useState(false);
  const [testResult, setTestResult] = useState<TestRunTaskResult | null>(null);

  const [triggerMode, setTriggerMode] = useState<'API' | 'CRON'>('API');
  const [apiForm] = Form.useForm();
  const [cronExpression, setCronExpression] = useState('0 0 2 * * ?');
  const [inputParams, setInputParams] = useState<any[]>([
    { key: '1', paramName: 'id', dataType: 'int', required: true, defaultValue: '' },
    { key: '2', paramName: 'name', dataType: 'string', required: true, defaultValue: '' },
  ]);

  const fetchCategoryTree = useCallback(async () => {
    try {
      const res = await getTaskCategoryTree();
      setCategoryTree(res.data);
    } catch (error) {
      console.error('查询分类树失败:', error);
    }
  }, []);

  useEffect(() => {
    fetchCategoryTree();
  }, [fetchCategoryTree]);

  const buildCategoryOptions = (nodes: TaskCategoryTreeNode[], prefix = ''): { value: number; label: string }[] => {
    const options: { value: number; label: string }[] = [];
    nodes.forEach((node) => {
      const label = prefix ? `${prefix}-${node.name}` : node.name;
      options.push({ value: node.id, label });
      if (node.children && node.children.length > 0) {
        options.push(...buildCategoryOptions(node.children, label));
      }
    });
    return options;
  };

  const categoryOptions = useMemo(() => buildCategoryOptions(categoryTree), [categoryTree]);

  const validateTaskNameUnique = async (_: any, value: string) => {
    if (!value) return Promise.resolve();
    if (!TASK_NAME_REGEX.test(value)) {
      return Promise.reject(new Error('任务名称只支持中文和英文大小写'));
    }
    if (value.length > 50) {
      return Promise.reject(new Error('任务名称最大50字符'));
    }
    try {
      const res = await getTaskList({ keyword: value, pageSize: 100 });
      const exists = (res.data.records || []).some((item: any) => item.taskName === value);
      if (exists) {
        return Promise.reject(new Error('任务名称已存在'));
      }
    } catch (e) { }
    return Promise.resolve();
  };

  const validateStep1 = async (): Promise<boolean> => {
    try {
      await form.validateFields();
      setErrors([]);
      return true;
    } catch (err: any) {
      const errorList: string[] = [];
      if (err.errorFields) {
        err.errorFields.forEach((field: any) => {
          field.errors.forEach((msg: string) => errorList.push(msg));
        });
      }
      setErrors(errorList);
      return false;
    }
  };

  const handleNextStep = async () => {
    if (currentStep === 1) {
      const valid = await validateStep1();
      if (!valid) {
        message.error('信息填写不完整，无法跳转下一步或保存');
        return;
      }
      if (!taskId) {
        const values = form.getFieldsValue();
        try {
          const res = await createTask({
            taskName: values.taskName,
            categoryId: values.categoryId,
            taskDescription: values.taskDescription,
          });
          setTaskId(res.data.id);
          setCurrentStep(2);
        } catch (error) {
          message.error('创建任务失败');
        }
      } else {
        setCurrentStep(2);
      }
    } else if (currentStep === 2) {
      // 暂时取消测试验证，直接进入下一步
      setCurrentStep(3);
    }
  };

  const handlePrevStep = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const handleCancel = () => {
    Modal.confirm({
      title: '确认取消',
      content: '确定要取消当前操作吗？所有输入的信息将会丢失。',
      onOk: () => {
        window.history.back();
      },
    });
  };

  const handleSaveAndExit = async () => {
    if (currentStep === 1) {
      const valid = await validateStep1();
      if (!valid) {
        message.error('信息填写不完整，无法保存');
        return;
      }
      if (!taskId) {
        const values = form.getFieldsValue();
        try {
          await createTask({
            taskName: values.taskName,
            categoryId: values.categoryId,
            taskDescription: values.taskDescription,
          });
          message.success('保存成功');
          window.history.back();
        } catch (error) {
          message.error('保存失败');
        }
      } else {
        message.success('保存成功');
        window.history.back();
      }
    } else if (currentStep === 2) {
      if (taskId) {
        try {
          await updateTaskConfig(taskId, { nodes: nodes as any, edges: edges as any });
          message.success('保存成功');
          window.history.back();
        } catch (error) {
          message.error('保存失败');
        }
      }
    } else if (currentStep === 3) {
      if (taskId) {
        try {
          const cronExpr = triggerMode === 'CRON' ? cronExpression : undefined;
          await setTaskTriggerConfig(taskId, {
            scheduleType: triggerMode,
            cronExpression: cronExpr,
          });
          message.success('保存成功');
          window.history.back();
        } catch (error) {
          message.error('保存失败');
        }
      }
    }
  };

  const handleDragStart = () => { };

  const handleTestRun = async () => {
    if (!taskId) {
      message.error('请先保存基本信息');
      return;
    }
    setTestModalVisible(true);
    setTestResult(null);
    setTestLoading(true);
    try {
      // 先保存当前配置
      await updateTaskConfig(taskId, { nodes: nodes as any, edges: edges as any });
      // 再运行测试
      const res = await testRunTask(taskId, {});
      setTestResult(res.data);
      if (res.data.status === 2) {
        setTestPassed(true);
      }
    } catch (error) {
      console.error('测试运行失败:', error);
      message.error('测试运行失败');
    } finally {
      setTestLoading(false);
    }
  };

  const handleTriggerModeChange = (mode: 'API' | 'CRON') => {
    setTriggerMode(mode);
  };

  const handleAddParam = () => {
    const newKey = String(Date.now());
    setInputParams([
      ...inputParams,
      { key: newKey, paramName: '', dataType: 'string', required: false, defaultValue: '' },
    ]);
  };

  const handleDeleteParam = (key: string) => {
    setInputParams(inputParams.filter((p) => p.key !== key));
  };

  const handleParamChange = (key: string, field: string, value: any) => {
    setInputParams(inputParams.map((p) => (p.key === key ? { ...p, [field]: value } : p)));
  };

  const paramColumns = [
    {
      title: '参数名称',
      dataIndex: 'paramName',
      key: 'paramName',
      width: 150,
      render: (text: string, record: any) => (
        <Input
          value={text}
          onChange={(e) => handleParamChange(record.key, 'paramName', e.target.value)}
          size="small"
        />
      ),
    },
    {
      title: '数据类型',
      dataIndex: 'dataType',
      key: 'dataType',
      width: 120,
      render: (text: string, record: any) => (
        <Select
          value={text}
          onChange={(v) => handleParamChange(record.key, 'dataType', v)}
          size="small"
          style={{ width: '100%' }}
        >
          <Select.Option value="string">string</Select.Option>
          <Select.Option value="int">int</Select.Option>
          <Select.Option value="float">float</Select.Option>
          <Select.Option value="boolean">boolean</Select.Option>
          <Select.Option value="object">object</Select.Option>
          <Select.Option value="array">array</Select.Option>
        </Select>
      ),
    },
    {
      title: '是否必填',
      dataIndex: 'required',
      key: 'required',
      width: 100,
      render: (text: boolean, record: any) => (
        <Select
          value={text}
          onChange={(v) => handleParamChange(record.key, 'required', v)}
          size="small"
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
      render: (text: string, record: any) => (
        <Input
          value={text}
          onChange={(e) => handleParamChange(record.key, 'defaultValue', e.target.value)}
          size="small"
        />
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 80,
      render: (_: any, record: any) => (
        <Button type="link" size="small" danger icon={<DeleteOutlined />} onClick={() => handleDeleteParam(record.key)} />
      ),
    },
  ];

  const selectedNode = nodes.find((n) => n.nodeId === selectedNodeId);

  const handleNodeNameChange = (value: string) => {
    if (!selectedNodeId) return;
    // 通过 X6 API 更新节点
    if ((window as any).__dagCanvas?.updateNodeData) {
      (window as any).__dagCanvas.updateNodeData(selectedNodeId, { nodeName: value });
    }
  };

  const handleDeleteNode = () => {
    if (!selectedNodeId) return;
    if (selectedNode?.nodeType === 'START' || selectedNode?.nodeType === 'END') {
      message.warning('START 和 END 节点不能删除');
      return;
    }
    // 通过 X6 API 删除节点
    if ((window as any).__dagCanvas?.deleteNode) {
      (window as any).__dagCanvas.deleteNode(selectedNodeId);
    }
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#f5f5f5' }}>
      <div
        style={{
          padding: '8px 24px',
          backgroundColor: '#fff',
          borderBottom: '1px solid #e8e8e8',
          display: 'flex',
          alignItems: 'center',
          fontSize: 13,
        }}
      >
        <span style={{ color: '#666' }}>数据工厂</span>
        <span style={{ margin: '0 8px', color: '#bfbfbf' }}>›</span>
        <span style={{ color: '#333' }}>数据集接口管理</span>
      </div>

      <Card style={{ margin: 16, borderRadius: 8 }} bodyStyle={{ padding: 24 }}>
        <div style={{ maxWidth: 800, margin: '0 auto 32px' }}>
          <Steps current={currentStep - 1}>
            <Step title="基本信息" />
            <Step title="任务配置" />
            <Step title="任务触发设置" />
          </Steps>
        </div>

        {errors.length > 0 && (
          <div
            style={{
              position: 'fixed',
              right: 24,
              top: 100,
              width: 280,
              zIndex: 1000,
            }}
          >
            <Card
              size="small"
              title={
                <span style={{ color: '#fa8c16' }}>
                  <span style={{ marginRight: 8 }}>!</span>
                  任务名称重复，无法提交。
                </span>
              }
              extra={
                <Button type="text" size="small" onClick={() => setErrors([])}>
                  <CloseOutlined />
                </Button>
              }
              style={{ marginBottom: 12, borderColor: '#ffd591' }}
            >
              <div style={{ color: '#666' }}>
                {errors.map((err, i) => (
                  <div key={i}>{err}</div>
                ))}
              </div>
            </Card>
            <Card
              size="small"
              title={
                <span style={{ color: '#fa8c16' }}>
                  <span style={{ marginRight: 8 }}>!</span>
                  信息填写不完整，无法跳转下一步或保存。
                </span>
              }
              extra={
                <Button type="text" size="small" onClick={() => setErrors([])}>
                  <CloseOutlined />
                </Button>
              }
              style={{ borderColor: '#ffd591' }}
            />
          </div>
        )}

        {currentStep === 1 && (
          <div style={{ display: 'flex', justifyContent: 'center' }}>
            <div style={{ width: 600 }}>
              <div style={{ fontSize: 14, fontWeight: 600, marginBottom: 16 }}>基本信息</div>
              <Form form={form} layout="vertical">
                <Form.Item
                  label={
                    <span>
                      <span style={{ color: '#ff4d4f' }}></span> 任务分类
                    </span>
                  }
                  name="categoryId"
                  rules={[{ required: true, message: '请选择任务分类' }]}
                >
                  <Select placeholder="请选择任务分类" options={categoryOptions} />
                </Form.Item>
                <Form.Item
                  label={
                    <span>
                      <span style={{ color: '#ff4d4f' }}>*</span> 任务名称
                    </span>
                  }
                  name="taskName"
                  rules={[{ validator: validateTaskNameUnique }]}
                >
                  <Input placeholder="请输入任务名称" maxLength={50} />
                </Form.Item>
                <Form.Item label="任务描述" name="taskDescription">
                  <TextArea rows={4} placeholder="请输入任务描述" maxLength={500} />
                </Form.Item>
              </Form>
            </div>
          </div>
        )}

        {currentStep === 2 && (
          <div style={{ display: 'flex', border: '1px solid #e8e8e8', borderRadius: 4, height: 560 }}>
            <div style={{ width: 180, flexShrink: 0 }}>
              <div
                style={{
                  padding: '8px 12px',
                  borderBottom: '1px solid #e8e8e8',
                  fontWeight: 600,
                  fontSize: 13,
                }}
              >
                数据处理节点类型
              </div>
              <NodeTypePanel onDragStart={handleDragStart} />
            </div>

            <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
              <div
                style={{
                  padding: '8px 16px',
                  borderBottom: '1px solid #e8e8e8',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                }}
              >
                <Button type="link" icon={<PlayCircleOutlined />} onClick={handleTestRun}>
                  运行
                </Button>
                <span style={{ fontSize: 13, fontWeight: 500 }}>
                  {form.getFieldValue('taskName') || '未命名任务'}
                </span>
                <span style={{ width: 80 }} />
              </div>
              <DAGCanvas
                initialNodes={nodes}
                initialEdges={edges}
                onNodesChange={setNodes}
                onEdgesChange={setEdges}
                onNodeSelect={setSelectedNodeId}
                selectedNodeId={selectedNodeId}
              />
            </div>

            <div style={{ width: 280, flexShrink: 0, borderLeft: '1px solid #e8e8e8' }}>
              <Tabs
                activeKey={activePropTab}
                onChange={(k) => setActivePropTab(k as any)}
                tabPosition="right"
                size="small"
              >
                <TabPane tab="节点属性" key="attr">
                  <div style={{ padding: 12 }}>
                    {selectedNode ? (
                      <div>
                        <Form layout="vertical">
                          <Form.Item label="节点ID">
                            <Input value={selectedNode.nodeId} size="small" disabled />
                          </Form.Item>
                          <Form.Item label="节点类型">
                            <Input value={selectedNode.nodeType} size="small" disabled />
                          </Form.Item>
                          <Form.Item
                            label={
                              <span>
                                <span style={{ color: '#ff4d4f' }}>*</span> 节点名称
                              </span>
                            }
                          >
                            <Input
                              value={selectedNode.nodeName}
                              onChange={(e) => handleNodeNameChange(e.target.value)}
                              size="small"
                              disabled={selectedNode.nodeType === 'START' || selectedNode.nodeType === 'END'}
                            />
                          </Form.Item>
                          {(selectedNode.nodeType === 'START' || selectedNode.nodeType === 'END') && (
                            <Form.Item label="节点说明">
                              <TextArea
                                rows={3}
                                value={selectedNode.config?.description || ''}
                                onChange={(e) => {
                                  setNodes(
                                    nodes.map((n) =>
                                      n.nodeId === selectedNodeId
                                        ? { ...n, config: { ...n.config, description: e.target.value } }
                                        : n,
                                    ),
                                  );
                                }}
                                size="small"
                              />
                            </Form.Item>
                          )}
                          {selectedNode.nodeType !== 'START' && selectedNode.nodeType !== 'END' && (
                            <Button
                              type="primary"
                              danger
                              block
                              size="small"
                              icon={<DeleteOutlined />}
                              onClick={handleDeleteNode}
                            >
                              删除节点
                            </Button>
                          )}
                        </Form>
                      </div>
                    ) : (
                      <div style={{ color: '#999', textAlign: 'center', padding: '40px 0' }}>
                        请选择画布中的节点
                      </div>
                    )}
                  </div>
                </TabPane>
                <TabPane tab="节点配置" key="config">
                  <div style={{ padding: 12 }}>
                    {selectedNode ? (
                      <div>
                        {selectedNode.nodeType === 'API' && (
                          <Form layout="vertical" size="small">
                            <Form.Item label="关联接口ID">
                              <InputNumber style={{ width: '100%' }} size="small" />
                            </Form.Item>
                            <Form.Item label="超时时间(ms)">
                              <InputNumber style={{ width: '100%' }} defaultValue={60000} size="small" />
                            </Form.Item>
                            <Form.Item label="重试次数">
                              <InputNumber style={{ width: '100%' }} defaultValue={0} min={0} max={3} size="small" />
                            </Form.Item>
                          </Form>
                        )}
                        {selectedNode.nodeType === 'SCRIPT' && (
                          <Form layout="vertical" size="small">
                            <Form.Item label="关联脚本ID">
                              <InputNumber style={{ width: '100%' }} size="small" />
                            </Form.Item>
                            <Form.Item label="脚本版本">
                              <Input placeholder="最新版本" size="small" />
                            </Form.Item>
                            <Form.Item label="数据源ID">
                              <InputNumber style={{ width: '100%' }} size="small" />
                            </Form.Item>
                          </Form>
                        )}
                        {selectedNode.nodeType === 'MAPPING' && (
                          <div>
                            <div style={{ fontSize: 12, color: '#666', marginBottom: 8 }}>字段映射</div>
                            <div style={{ maxHeight: 300, overflow: 'auto' }}>
                              {[1, 2, 3].map((i) => (
                                <div
                                  key={i}
                                  style={{
                                    display: 'flex',
                                    gap: 4,
                                    marginBottom: 6,
                                    alignItems: 'center',
                                  }}
                                >
                                  <Input size="small" placeholder="源字段" style={{ flex: 1 }} />
                                  <span>→</span>
                                  <Input size="small" placeholder="目标字段" style={{ flex: 1 }} />
                                </div>
                              ))}
                            </div>
                            <Button type="link" size="small" icon={<PlusOutlined />} style={{ padding: 0 }}>
                              添加映射
                            </Button>
                          </div>
                        )}
                        {selectedNode.nodeType === 'OUTPUT' && (
                          <Form layout="vertical" size="small">
                            <Form.Item label="输出类型">
                              <Select size="small">
                                <Select.Option value="DATABASE">数据库</Select.Option>
                                <Select.Option value="FILE">文件</Select.Option>
                                <Select.Option value="API_PUSH">API推送</Select.Option>
                              </Select>
                            </Form.Item>
                            <Form.Item label="目标数据源ID">
                              <InputNumber style={{ width: '100%' }} size="small" />
                            </Form.Item>
                            <Form.Item label="目标表名">
                              <Input size="small" placeholder="请输入目标表名" />
                            </Form.Item>
                            <Form.Item label="写入模式">
                              <Select size="small">
                                <Select.Option value="INSERT">INSERT</Select.Option>
                                <Select.Option value="UPSERT">UPSERT</Select.Option>
                                <Select.Option value="OVERWRITE">OVERWRITE</Select.Option>
                                <Select.Option value="APPEND">APPEND</Select.Option>
                              </Select>
                            </Form.Item>
                          </Form>
                        )}
                        {(selectedNode.nodeType === 'START' || selectedNode.nodeType === 'END') && (
                          <div style={{ color: '#999', textAlign: 'center', fontSize: 12 }}>
                            该节点无需配置参数
                          </div>
                        )}
                      </div>
                    ) : (
                      <div style={{ color: '#999', textAlign: 'center', padding: '40px 0' }}>
                        请选择画布中的节点
                      </div>
                    )}
                  </div>
                </TabPane>
              </Tabs>
            </div>
          </div>
        )}

        {currentStep === 3 && (
          <div>
            <div style={{ marginBottom: 16 }}>
              <span style={{ marginRight: 8, fontWeight: 500 }}>触发方式配置：</span>
              <Space>
                <Button
                  type={triggerMode === 'API' ? 'primary' : 'default'}
                  onClick={() => handleTriggerModeChange('API')}
                >
                  生成API
                </Button>
                <Button
                  type={triggerMode === 'CRON' ? 'primary' : 'default'}
                  onClick={() => handleTriggerModeChange('CRON')}
                >
                  配置定时任务
                </Button>
              </Space>
            </div>

            {triggerMode === 'API' && (
              <Card size="small" title="API信息配置" style={{ marginBottom: 16 }}>
                <Form form={apiForm} layout="vertical">
                  <Form.Item label="接口名称" name="apiName">
                    <Input placeholder="请输入接口名称" />
                  </Form.Item>
                  <Form.Item label="Path" name="path">
                    <Input placeholder="请输入接口Path" />
                  </Form.Item>
                  <Form.Item label="接口描述" name="apiDescription">
                    <TextArea rows={3} placeholder="请输入接口描述" />
                  </Form.Item>
                </Form>
              </Card>
            )}

            {triggerMode === 'CRON' && (
              <Card size="small" title="定时任务配置" style={{ marginBottom: 16 }}>
                <div style={{ marginBottom: 16 }}>
                  <div style={{ fontWeight: 500, marginBottom: 8 }}>输入参数</div>
                  <div style={{ marginBottom: 8, textAlign: 'right' }}>
                    <Button type="link" size="small" icon={<PlusOutlined />} onClick={handleAddParam}>
                      新增参数
                    </Button>
                  </div>
                  <Table
                    columns={paramColumns}
                    dataSource={inputParams}
                    pagination={false}
                    size="small"
                    bordered
                    rowKey="key"
                  />
                </div>
                <div style={{ marginBottom: 8, fontWeight: 500 }}>Cron表达式：{cronExpression}</div>
              </Card>
            )}
          </div>
        )}

        <div style={{ display: 'flex', justifyContent: 'center', gap: 12, marginTop: 32 }}>
          {currentStep > 1 && <Button onClick={handlePrevStep}>上一步</Button>}
          <Button onClick={handleCancel}>取消</Button>
          {currentStep < 3 && (
            <Button type="primary" onClick={handleNextStep}>
              下一步
            </Button>
          )}
          <Button type="primary" icon={<SaveOutlined />} onClick={handleSaveAndExit}>
            保存并退出
          </Button>
        </div>
      </Card>

      <Modal
        title="测试运行"
        visible={testModalVisible}
        onCancel={() => setTestModalVisible(false)}
        width={800}
        footer={[
          <Button key="close" onClick={() => setTestModalVisible(false)}>
            关闭
          </Button>,
          <Button
            key="rerun"
            type="primary"
            icon={<ExperimentOutlined />}
            onClick={handleTestRun}
            loading={testLoading}
          >
            重新运行
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
            {testResult.status === 3 && testResult.nodeResults.some((n: any) => n.status === 3) && (
              <div style={{ marginTop: 16, padding: 12, backgroundColor: '#fff2f0', borderRadius: 4 }}>
                <div style={{ color: '#ff4d4f', fontWeight: 500, marginBottom: 4 }}>错误信息：</div>
                {testResult.nodeResults
                  .filter((n: any) => n.status === 3 && n.errorMessage)
                  .map((n: any, i: number) => (
                    <div key={i} style={{ color: '#ff4d4f', fontSize: 12 }}>
                      {n.nodeName}：{n.errorMessage}
                    </div>
                  ))}
              </div>
            )}
          </div>
        )}
        {!testResult && testLoading && (
          <div style={{ textAlign: 'center', padding: '40px 0', color: '#666' }}>测试运行中...</div>
        )}
        {!testResult && !testLoading && (
          <div style={{ textAlign: 'center', padding: '40px 0', color: '#999' }}>
            点击"重新运行"按钮运行任务
          </div>
        )}
      </Modal>
    </div>
  );
};

export default AddTask;

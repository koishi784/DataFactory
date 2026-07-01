import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'umi';
import { Card, Table, Button, Space, Input, Select, Tag, Tree, Modal, message } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, PlayCircleOutlined, PoweroffOutlined, MoreOutlined, SearchOutlined } from '@ant-design/icons';
import { Dropdown, Menu } from 'antd';
import {
  getAssetDirectoryTree,
  getAssetList,
  publishAsset,
  disableAsset,
  deleteAsset,
  batchPublishAssets,
  batchDisableAssets,
  type AssetItem,
  type AssetListParams,
  type AssetDirectoryTreeNode,
} from '@/services/DataAssetManage/dataAssetManageAPI';

const STATUS_MAP: Record<number, { label: string; color: string }> = {
  0: { label: '未发布', color: 'default' },
  1: { label: '已发布', color: 'success' },
  2: { label: '已停用', color: 'warning' },
};

const DataAssetManage: React.FC = () => {
  const navigate = useNavigate();
  const [treeData, setTreeData] = useState<AssetDirectoryTreeNode[]>([]);
  const [selectedDirectory, setSelectedDirectory] = useState<number | null>(null);
  const [assetList, setAssetList] = useState<AssetItem[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState<number[]>([]);
  const [searchParams, setSearchParams] = useState<AssetListParams>({
    pageNum: 1,
    pageSize: 20,
    keyword: '',
    status: '',
  });
  const [englishKeyword, setEnglishKeyword] = useState('');
  const [directorySearchText, setDirectorySearchText] = useState('');

  const fetchDirectoryTree = useCallback(async () => {
    try {
      const response = await getAssetDirectoryTree();
      if (response && response.data) {
        setTreeData(response.data);
      }
    } catch (error) {
      console.error('查询目录树失败:', error);
      message.error('查询目录树失败');
    }
  }, []);

  const fetchAssetList = useCallback(async (params?: AssetListParams) => {
    setLoading(true);
    try {
      const response = await getAssetList(params);
      if (response && response.data) {
        const records = response.data.records || [];
        const sortedRecords = [...records].sort((a, b) => {
          if (a.status !== b.status) {
            return a.status - b.status;
          }
          return new Date(b.updateTime).getTime() - new Date(a.updateTime).getTime();
        });
        setAssetList(sortedRecords);
        setTotal(response.data.total || 0);
      }
    } catch (error) {
      console.error('查询资产列表失败:', error);
      message.error('查询资产列表失败');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDirectoryTree();
    fetchAssetList();
  }, [fetchDirectoryTree, fetchAssetList]);

  const handleDirectorySelect = (selectedKeys: React.Key[]) => {
    const id = selectedKeys.length > 0 ? Number(selectedKeys[0]) : undefined;
    setSelectedDirectory(id ?? null);
    fetchAssetList({
      pageNum: 1,
      pageSize: searchParams.pageSize,
      directoryId: id,
      keyword: searchParams.keyword,
      status: searchParams.status,
    });
  };

  const handleSearch = () => {
    const combinedKeyword = `${searchParams.keyword}${englishKeyword}`;
    fetchAssetList({
      pageNum: 1,
      pageSize: searchParams.pageSize,
      directoryId: selectedDirectory ?? undefined,
      keyword: combinedKeyword || undefined,
      status: searchParams.status,
    });
  };

  const handleReset = () => {
    setSearchParams({
      pageNum: 1,
      pageSize: 20,
      keyword: '',
      status: '',
    });
    setEnglishKeyword('');
    setSelectedDirectory(null);
    fetchAssetList({
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
    fetchAssetList({
      pageNum: pagination.current || 1,
      pageSize: pagination.pageSize || 20,
      directoryId: selectedDirectory ?? undefined,
      keyword: searchParams.keyword,
      status: searchParams.status,
    });
  };

  const handlePublish = async (id: number) => {
    Modal.confirm({
      title: '确认发布',
      content: '确定要发布该数据资产表吗？',
      onOk: async () => {
        try {
          await publishAsset(id);
          message.success('发布成功');
          fetchAssetList({
            pageNum: searchParams.pageNum,
            pageSize: searchParams.pageSize,
            directoryId: selectedDirectory ?? undefined,
            keyword: searchParams.keyword,
            status: searchParams.status,
          });
        } catch (error) {
          message.error('发布失败');
        }
      },
    });
  };

  const handleDisable = async (id: number) => {
    Modal.confirm({
      title: '确认停用',
      content: '确定要停用该数据资产表吗？',
      onOk: async () => {
        try {
          await disableAsset(id);
          message.success('停用成功');
          fetchAssetList({
            pageNum: searchParams.pageNum,
            pageSize: searchParams.pageSize,
            directoryId: selectedDirectory ?? undefined,
            keyword: searchParams.keyword,
            status: searchParams.status,
          });
        } catch (error) {
          message.error('停用失败');
        }
      },
    });
  };

  const handleDelete = async (id: number) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该数据资产表吗？',
      onOk: async () => {
        try {
          await deleteAsset(id);
          message.success('删除成功');
          fetchAssetList({
            pageNum: searchParams.pageNum,
            pageSize: searchParams.pageSize,
            directoryId: selectedDirectory ?? undefined,
            keyword: searchParams.keyword,
            status: searchParams.status,
          });
        } catch (error) {
          message.error('删除失败');
        }
      },
    });
  };

  const handleBatchPublish = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要发布的数据资产表');
      return;
    }
    Modal.confirm({
      title: '批量发布',
      content: `确定要发布选中的 ${selectedRowKeys.length} 个数据资产表吗？`,
      onOk: async () => {
        try {
          await batchPublishAssets({ ids: selectedRowKeys });
          message.success('批量发布成功');
          setSelectedRowKeys([]);
          fetchAssetList({
            pageNum: searchParams.pageNum,
            pageSize: searchParams.pageSize,
            directoryId: selectedDirectory ?? undefined,
            keyword: searchParams.keyword,
            status: searchParams.status,
          });
        } catch (error) {
          message.error('批量发布失败');
        }
      },
    });
  };

  const handleBatchDisable = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要停用的数据资产表');
      return;
    }
    Modal.confirm({
      title: '批量停用',
      content: `确定要停用选中的 ${selectedRowKeys.length} 个数据资产表吗？`,
      onOk: async () => {
        try {
          await batchDisableAssets({ ids: selectedRowKeys });
          message.success('批量停用成功');
          setSelectedRowKeys([]);
          fetchAssetList({
            pageNum: searchParams.pageNum,
            pageSize: searchParams.pageSize,
            directoryId: selectedDirectory ?? undefined,
            keyword: searchParams.keyword,
            status: searchParams.status,
          });
        } catch (error) {
          message.error('批量停用失败');
        }
      },
    });
  };

  const buildTreeData = (nodes: AssetDirectoryTreeNode[]): any[] => {
    return nodes.map((node) => {
      const hasChildren = node.children && node.children.length > 0;
      return {
        title: (
          <span>{node.name}</span>
        ),
        key: String(node.id),
        children: hasChildren ? buildTreeData(node.children) : undefined,
        isLeaf: !hasChildren,
      };
    });
  };

  const filterTreeData = (nodes: AssetDirectoryTreeNode[], searchText: string): any[] => {
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
          title: (
            <span>{node.name}</span>
          ),
          key: String(node.id),
          children: childResults.length > 0 ? childResults : undefined,
          isLeaf: !hasChildren,
        });
      }
    });
    return filtered;
  };

  const columns = [
    {
      title: '数据资产表中文名称',
      dataIndex: 'assetName',
      key: 'assetName',
      width: 200,
    },
    {
      title: '数据资产表英文名称',
      dataIndex: 'englishName',
      key: 'englishName',
      width: 200,
    },
    {
      title: '数据资产表描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
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
      width: 150,
      align: 'center',
      render: (_: any, record: AssetItem) => {
        const menu = (
          <Menu>
            {record.status === 0 && (
              <Menu.Item key="publish" onClick={() => handlePublish(record.id)}>
                <span>发布</span>
              </Menu.Item>
            )}
            {record.status === 1 && (
              <Menu.Item key="disable" onClick={() => handleDisable(record.id)}>
                <span>停用</span>
              </Menu.Item>
            )}
            {record.status === 2 && (
              <Menu.Item key="publish" onClick={() => handlePublish(record.id)}>
                <span>发布</span>
              </Menu.Item>
            )}
            {record.status !== 1 && (
              <Menu.Item key="edit">
                <span>编辑</span>
              </Menu.Item>
            )}
            {record.status === 0 && (
              <Menu.Item key="delete" danger onClick={() => handleDelete(record.id)}>
                <span>删除</span>
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
      <Card style={{ width: 280, margin: 16, borderRadius: 12 }} bodyStyle={{ padding: 0 }}>
        <div style={{ padding: 16, borderBottom: '1px solid #f0f0f0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span style={{ fontWeight: 600 }}>数据资产表目录</span>
          <Button type="link" size="small" icon={<PlusOutlined />} onClick={() => message.info('新增目录功能开发中')} />
        </div>
        <div style={{ padding: 8, borderBottom: '1px solid #f0f0f0' }}>
          <Input
            placeholder="按数据资产表名称或目录名称查询"
            prefix={<SearchOutlined />}
            value={directorySearchText}
            onChange={(e) => setDirectorySearchText(e.target.value)}
            style={{ width: '100%' }}
          />
        </div>
        <Tree
          showLine
          defaultExpandAll
          onSelect={handleDirectorySelect}
          selectedKeys={selectedDirectory ? [String(selectedDirectory)] : []}
          treeData={filterTreeData(treeData, directorySearchText)}
          style={{ padding: 8 }}
        />
      </Card>

      <div style={{ flex: 1, margin: 16, display: 'flex', flexDirection: 'column' }}>
        <Card style={{ borderRadius: 12 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <div style={{ display: 'flex', gap: 16, alignItems: 'center', flexWrap: 'wrap' }}>
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <span style={{ marginRight: 8, whiteSpace: 'nowrap', minWidth: 100, textAlign: 'right' }}>数据资产表状态：</span>
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
                <span style={{ marginRight: 8, whiteSpace: 'nowrap', minWidth: 80, textAlign: 'right' }}>中文名称：</span>
                <Input
                  placeholder="请输入中文名称"
                  style={{ width: 200 }}
                  value={searchParams.keyword || ''}
                  onChange={(e) => setSearchParams({ ...searchParams, keyword: e.target.value })}
                />
              </div>
              <div style={{ display: 'flex', alignItems: 'center' }}>
                <span style={{ marginRight: 8, whiteSpace: 'nowrap', minWidth: 80, textAlign: 'right' }}>英文名称：</span>
                <Input
                  placeholder="请输入英文名称"
                  style={{ width: 200 }}
                  value={englishKeyword}
                  onChange={(e) => setEnglishKeyword(e.target.value)}
                />
              </div>
              <Button onClick={handleReset}>重置</Button>
              <Button type="primary" onClick={handleSearch}>查询</Button>
            </div>
            <div style={{ marginLeft: 16 }}>
              <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/data/addDataAsset')}>
                新增资产表
              </Button>
            </div>
          </div>

          <div style={{ display: 'flex', gap: 12, marginBottom: 16 }}>
            <Button icon={<PlayCircleOutlined />} onClick={handleBatchPublish} disabled={selectedRowKeys.length === 0}>
              批量发布
            </Button>
            <Button icon={<PoweroffOutlined />} onClick={handleBatchDisable} disabled={selectedRowKeys.length === 0}>
              批量停用
            </Button>
          </div>

          <Table
            rowSelection={rowSelection}
            columns={columns}
            dataSource={assetList}
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
            rowKey="id"
          />
        </Card>
      </div>
    </div>
  );
};

export default DataAssetManage;

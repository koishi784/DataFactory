import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Card, Form, Input, Select, Button, Table, message, Spin, Row, Col, Divider } from 'antd';
import { PlusOutlined, DeleteOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate } from 'umi';
import {
    getAssetDirectoryTree,
    createAsset,
    type AssetDirectoryTreeNode,
    type CreateAssetData,
    type CreateAssetFieldData,
} from '@/services/DataAssetManage/dataAssetManageAPI';
import {
    getDataStandardList,
    type DataStandardItem,
    type DataStandardListParams,
} from '@/services/DataStandardAPI/directoryAPI';

interface DirectoryItem {
    id: number;
    name: string;
    isPlaceholder?: boolean;
}

interface AssetFieldForm {
    id?: number;
    englishFieldName: string;
    chineseFieldName: string;
    description?: string;
    standardId?: number;
    sortOrder?: number;
}

const AddDataAsset: React.FC = () => {
    const navigate = useNavigate();
    const [form] = Form.useForm();
    const [treeData, setTreeData] = useState<AssetDirectoryTreeNode[]>([]);
    const [directoryItems, setDirectoryItems] = useState<DirectoryItem[]>([]);
    const [assetFields, setAssetFields] = useState<AssetFieldForm[]>([
        { englishFieldName: '', chineseFieldName: '' },
    ]);

    const [standardOptions, setStandardOptions] = useState<{ value: number; label: string }[]>([]);
    const [standardLoading, setStandardLoading] = useState(false);
    const [standardPageNum, setStandardPageNum] = useState(1);
    const [standardHasMore, setStandardHasMore] = useState(true);
    const [standardSearchKeyword, setStandardSearchKeyword] = useState('');
    const standardDropdownRef = useRef<HTMLDivElement>(null);

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

    useEffect(() => {
        fetchDirectoryTree();
    }, [fetchDirectoryTree]);

    const leafNodeOptions: { value: number; label: string }[] = [];
    const collectLeafNodes = (nodes: AssetDirectoryTreeNode[], path: string = '') => {
        nodes.forEach((node) => {
            const nodePath = path ? `${path} / ${node.name}` : node.name;
            if (!node.children || node.children.length === 0) {
                leafNodeOptions.push({ value: node.id, label: nodePath });
            } else {
                collectLeafNodes(node.children, nodePath);
            }
        });
    };
    collectLeafNodes(treeData);

    const handleAddDirectory = () => {
        setDirectoryItems((prev) => [...prev, { id: 0, name: '' }]);
    };

    const handleRemoveDirectory = (index: number) => {
        setDirectoryItems((prev) => prev.filter((_, i) => i !== index));
    };

    const handleDirectoryChange = (index: number, value: number) => {
        const selected = leafNodeOptions.find((opt) => opt.value === value);
        setDirectoryItems((prev) =>
            prev.map((item, i) =>
                i === index ? { id: value, name: selected?.label || '' } : item,
            ),
        );
    };

    const handleAddField = () => {
        setAssetFields((prev) => [...prev, { englishFieldName: '', chineseFieldName: '' }]);
    };

    const handleRemoveField = (index: number) => {
        setAssetFields((prev) => prev.filter((_, i) => i !== index));
    };

    const handleFieldChange = (index: number, field: keyof AssetFieldForm, value: string | number | null) => {
        setAssetFields((prev) =>
            prev.map((item, i) => (i === index ? { ...item, [field]: value } : item)),
        );
    };

    const fetchStandards = useCallback(async (keyword?: string, pageNum = 1) => {
        setStandardLoading(true);
        try {
            const params: DataStandardListParams = {
                pageNum,
                pageSize: 20,
                keyword,
            };
            const response = await getDataStandardList(params);
            if (response && response.data) {
                const records = response.data.records || [];
                const newOptions = records.map((item: DataStandardItem) => ({
                    value: item.id,
                    label: `${item.standardCode} · ${item.name} · ${item.englishName}`,
                }));

                if (pageNum === 1) {
                    setStandardOptions(newOptions);
                } else {
                    setStandardOptions((prev) => [...prev, ...newOptions]);
                }

                setStandardHasMore(records.length >= 20);
                setStandardPageNum(pageNum);
            }
        } catch (error) {
            console.error('查询数据标准失败:', error);
        } finally {
            setStandardLoading(false);
        }
    }, []);

    const handleStandardSearch = (value: string) => {
        setStandardSearchKeyword(value);
        setStandardPageNum(1);
        fetchStandards(value, 1);
    };

    const handleStandardDropdownVisibleChange = (visible: boolean) => {
        if (visible) {
            setStandardOptions([]);
            setStandardPageNum(1);
            setStandardHasMore(true);
            fetchStandards(standardSearchKeyword, 1);
        }
    };

    const handleStandardDropdownScroll = (e: React.UIEvent<HTMLDivElement>) => {
        const target = e.target as HTMLDivElement;
        if (!standardLoading && standardHasMore) {
            const { scrollTop, scrollHeight, clientHeight } = target;
            if (scrollHeight - scrollTop - clientHeight < 50) {
                fetchStandards(standardSearchKeyword, standardPageNum + 1);
            }
        }
    };

    const handleSubmit = () => {
        form.validateFields().then((values) => {
            const englishNameRegex = /^[a-zA-Z][a-zA-Z0-9_]*$/;
            const englishFieldRegex = /^[a-zA-Z][a-zA-Z0-9_]*$/;
            const chineseFieldRegex = /^[\u4e00-\u9fa5a-zA-Z]+$/;

            if (!englishNameRegex.test(values.englishName.trim())) {
                message.error('英文名称只能包含英文大小写、数字及下划线，且必须英文开头');
                return;
            }

            const selectedDirectoryIds = directoryItems.map((item) => item.id);
            if (selectedDirectoryIds.length === 0 || selectedDirectoryIds.includes(0)) {
                message.error('请选择所属目录');
                return;
            }

            const englishFieldNames: string[] = [];
            const chineseFieldNames: string[] = [];
            for (let i = 0; i < assetFields.length; i++) {
                const field = assetFields[i];
                const trimmedEnglish = field.englishFieldName.trim();
                const trimmedChinese = field.chineseFieldName.trim();

                if (trimmedEnglish === '') {
                    message.error(`第 ${i + 1} 行字段英文名称为空`);
                    return;
                }
                if (trimmedChinese === '') {
                    message.error(`第 ${i + 1} 行字段中文名称为空`);
                    return;
                }
                if (!englishFieldRegex.test(trimmedEnglish)) {
                    message.error(`第 ${i + 1} 行字段英文名称格式不正确`);
                    return;
                }
                if (!chineseFieldRegex.test(trimmedChinese)) {
                    message.error(`第 ${i + 1} 行字段中文名称格式不正确`);
                    return;
                }
                if (englishFieldNames.includes(trimmedEnglish)) {
                    message.error('字段英文名称不能重复');
                    return;
                }
                if (chineseFieldNames.includes(trimmedChinese)) {
                    message.error('字段中文名称不能重复');
                    return;
                }
                englishFieldNames.push(trimmedEnglish);
                chineseFieldNames.push(trimmedChinese);
            }

            const fieldsData: CreateAssetFieldData[] = assetFields.map((field, index) => ({
                englishFieldName: field.englishFieldName.trim(),
                chineseFieldName: field.chineseFieldName.trim(),
                description: field.description,
                standardId: field.standardId,
                sortOrder: field.sortOrder || index + 1,
            }));

            const requestData: CreateAssetData = {
                assetName: values.assetName,
                englishName: values.englishName,
                description: values.description,
                directoryIds: selectedDirectoryIds,
                fields: fieldsData,
            };

            createAsset(requestData)
                .then(() => {
                    message.success('新增成功');
                    navigate('/data/dataAssetManage');
                })
                .catch((error) => {
                    console.error('新增失败:', error);
                    message.error('新增失败');
                });
        });
    };

    const handleCancel = () => {
        navigate('/data/dataAssetManage');
    };

    return (
        <div style={{ minHeight: 'calc(100vh - 64px)', backgroundColor: '#f5f7fa', padding: '24px' }}>
            <Card
                style={{
                    borderRadius: '12px',
                    boxShadow: '0 2px 12px rgba(0, 0, 0, 0.08)',
                    border: 'none',
                }}
                styles={{ body: { padding: '0' } }}
            >
                <div
                    style={{
                        padding: '20px 24px',
                        borderBottom: '1px solid #f0f0f0',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                    }}
                >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <Button
                            type="link"
                            icon={<ArrowLeftOutlined />}
                            onClick={handleCancel}
                            style={{ color: '#666', fontWeight: '500' }}
                        >
                            返回
                        </Button>
                        <div>
                            <h2 style={{ margin: '0', fontSize: '18px', fontWeight: '600', color: '#1a1a1a' }}>
                                新增数据资产表
                            </h2>
                            <p style={{ margin: '4px 0 0', fontSize: '12px', color: '#999' }}>
                                创建数据资产表基础信息、所属目录及字段定义
                            </p>
                        </div>
                    </div>
                    <div style={{ display: 'flex', gap: '12px' }}>
                        <Button onClick={handleCancel}>取消</Button>
                        <Button type="primary" onClick={handleSubmit} size="large">
                            保存并提交
                        </Button>
                    </div>
                </div>

                <div style={{ padding: '32px' }}>
                    <Form form={form} layout="vertical">
                        <Card
                            style={{
                                marginBottom: '24px',
                                borderRadius: '8px',
                                border: '1px solid #e8e8e8',
                                boxShadow: 'none',
                            }}
                            title={
                                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                    <span style={{ color: '#ff4d4f', fontWeight: 'bold' }}>*</span>
                                    <span style={{ fontWeight: '600', fontSize: '14px' }}>数据资产表基础信息</span>
                                </div>
                            }
                        >
                            <div style={{ padding: '20px' }}>
                                <Row gutter={24}>
                                    <Col span={12}>
                                        <Form.Item
                                            label={
                                                <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                                                    <span style={{ color: '#ff4d4f' }}>*</span>
                                                    <span>中文名称</span>
                                                </span>
                                            }
                                            name="assetName"
                                            rules={[
                                                { required: true, message: '请输入中文名称' },
                                                { pattern: /^[\u4e00-\u9fa5a-zA-Z]+$/, message: '中文名称只能包含中文及英文大小写' },
                                            ]}
                                        >
                                            <Input
                                                placeholder="请输入数据资产表中文名称"
                                                size="large"
                                                style={{ borderRadius: '6px' }}
                                            />
                                        </Form.Item>
                                    </Col>
                                    <Col span={12}>
                                        <Form.Item
                                            label={
                                                <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                                                    <span style={{ color: '#ff4d4f' }}>*</span>
                                                    <span>英文名称</span>
                                                </span>
                                            }
                                            name="englishName"
                                            rules={[
                                                { required: true, message: '请输入英文名称' },
                                                { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '英文名称只能包含英文大小写、数字及下划线，且必须英文开头' },
                                            ]}
                                        >
                                            <Input
                                                placeholder="请输入数据资产表英文名称"
                                                size="large"
                                                style={{ borderRadius: '6px' }}
                                            />
                                        </Form.Item>
                                    </Col>
                                    <Col span={24}>
                                        <Form.Item label="数据资产表描述">
                                            <Input.TextArea
                                                placeholder="请输入数据资产表描述（可选）"
                                                rows={3}
                                                style={{ borderRadius: '6px' }}
                                            />
                                        </Form.Item>
                                    </Col>
                                </Row>
                            </div>
                        </Card>

                        <Card
                            style={{
                                marginBottom: '24px',
                                borderRadius: '8px',
                                border: '1px solid #e8e8e8',
                                boxShadow: 'none',
                            }}
                            title={
                                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                    <span style={{ color: '#ff4d4f', fontWeight: 'bold' }}>*</span>
                                    <span style={{ fontWeight: '600', fontSize: '14px' }}>所属目录</span>
                                </div>
                            }
                        >
                            <div style={{ padding: '20px' }}>
                                <div style={{ marginBottom: '16px' }}>
                                    <Table
                                        dataSource={directoryItems.length > 0 ? directoryItems : [{ id: -1, name: '', isPlaceholder: true }]}
                                        rowKey={(record) => record.id}
                                        pagination={false}
                                        size="middle"
                                        bordered
                                        style={{ borderRadius: '6px', overflow: 'hidden' }}
                                        columns={[
                                            {
                                                title: '所属目录',
                                                width: '85%',
                                                render: (_, record: DirectoryItem, index: number) => {
                                                    if (record.isPlaceholder) {
                                                        return (
                                                            <div style={{ textAlign: 'center', color: '#999', padding: '20px' }}>
                                                                暂无所属目录，请点击下方按钮添加
                                                            </div>
                                                        );
                                                    }
                                                    return (
                                                        <Select
                                                            placeholder="请选择叶子节点目录"
                                                            style={{ width: '100%' }}
                                                            value={directoryItems[index]?.id || undefined}
                                                            onChange={(value) => handleDirectoryChange(index, value)}
                                                            options={leafNodeOptions}
                                                            showSearch
                                                            filterOption={(input, option) =>
                                                                !!(option?.label?.toLowerCase().includes(input.toLowerCase()))
                                                            }
                                                        />
                                                    );
                                                },
                                            },
                                            {
                                                title: '操作',
                                                width: '15%',
                                                align: 'center' as const,
                                                render: (_, record: DirectoryItem, index: number) => {
                                                    if (record.isPlaceholder) return null;
                                                    return (
                                                        <Button
                                                            type="link"
                                                            danger
                                                            onClick={() => handleRemoveDirectory(index)}
                                                            size="small"
                                                        >
                                                            删除
                                                        </Button>
                                                    );
                                                },
                                            },
                                        ]}
                                    />
                                </div>
                                <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                                    <Button
                                        type="dashed"
                                        icon={<PlusOutlined />}
                                        onClick={handleAddDirectory}
                                        size="middle"
                                    >
                                        添加所属目录
                                    </Button>
                                </div>
                            </div>
                        </Card>

                        <Card
                            style={{
                                marginBottom: '24px',
                                borderRadius: '8px',
                                border: '1px solid #e8e8e8',
                                boxShadow: 'none',
                            }}
                            title={
                                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                    <span style={{ color: '#ff4d4f', fontWeight: 'bold' }}>*</span>
                                    <span style={{ fontWeight: '600', fontSize: '14px' }}>数据资产表字段定义</span>
                                </div>
                            }
                        >
                            <div style={{ padding: '20px' }}>
                                <div style={{ marginBottom: '16px' }}>
                                    <Table
                                        dataSource={assetFields}
                                        rowKey={(record, index) => String(index)}
                                        pagination={false}
                                        size="middle"
                                        bordered
                                        style={{ borderRadius: '6px', overflow: 'hidden' }}
                                        columns={[
                                            {
                                                title: (
                                                    <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                                                        <span style={{ color: '#ff4d4f' }}>*</span>
                                                        <span>字段英文名称</span>
                                                    </span>
                                                ),
                                                width: 180,
                                                render: (_, record: AssetFieldForm, index: number) => (
                                                    <Input
                                                        value={record.englishFieldName}
                                                        onChange={(e) => handleFieldChange(index, 'englishFieldName', e.target.value)}
                                                        placeholder="英文开头，含英文、数字、下划线"
                                                        size="small"
                                                        style={{ borderRadius: '4px' }}
                                                    />
                                                ),
                                            },
                                            {
                                                title: (
                                                    <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                                                        <span style={{ color: '#ff4d4f' }}>*</span>
                                                        <span>字段中文名称</span>
                                                    </span>
                                                ),
                                                width: 160,
                                                render: (_, record: AssetFieldForm, index: number) => (
                                                    <Input
                                                        value={record.chineseFieldName}
                                                        onChange={(e) => handleFieldChange(index, 'chineseFieldName', e.target.value)}
                                                        placeholder="中文及英文大小写"
                                                        size="small"
                                                        style={{ borderRadius: '4px' }}
                                                    />
                                                ),
                                            },
                                            {
                                                title: '字段说明',
                                                render: (_, record: AssetFieldForm, index: number) => (
                                                    <Input
                                                        value={record.description}
                                                        onChange={(e) => handleFieldChange(index, 'description', e.target.value)}
                                                        placeholder="请输入字段说明（可选）"
                                                        size="small"
                                                        style={{ borderRadius: '4px' }}
                                                    />
                                                ),
                                            },
                                            {
                                                title: '标准映射',
                                                width: 300,
                                                render: (_, record: AssetFieldForm, index: number) => (
                                                    <Select
                                                        showSearch
                                                        placeholder="请搜索数据标准"
                                                        allowClear
                                                        value={record.standardId}
                                                        onChange={(value) => handleFieldChange(index, 'standardId', value ?? undefined)}
                                                        onSearch={handleStandardSearch}
                                                        onDropdownVisibleChange={handleStandardDropdownVisibleChange}
                                                        dropdownRender={(menu) => (
                                                            <div
                                                                ref={standardDropdownRef}
                                                                style={{ maxHeight: 350, overflowY: 'auto', borderRadius: '6px' }}
                                                                onScroll={handleStandardDropdownScroll}
                                                            >
                                                                {menu}
                                                                {standardLoading && (
                                                                    <div style={{ textAlign: 'center', padding: '8px' }}>
                                                                        <Spin size="small" />
                                                                    </div>
                                                                )}
                                                            </div>
                                                        )}
                                                        options={standardOptions}
                                                        size="small"
                                                        style={{ width: '100%', borderRadius: '4px' }}
                                                    />
                                                ),
                                            },
                                            {
                                                title: '操作',
                                                width: 100,
                                                align: 'center' as const,
                                                render: (_, __: AssetFieldForm, index: number) => (
                                                    <div style={{ display: 'flex', gap: '4px', justifyContent: 'center' }}>
                                                        <Button
                                                            type="link"
                                                            size="small"
                                                            onClick={() => { }}
                                                        >
                                                            编辑
                                                        </Button>
                                                        <Button
                                                            type="link"
                                                            danger
                                                            size="small"
                                                            onClick={() => handleRemoveField(index)}
                                                            disabled={assetFields.length === 1}
                                                        >
                                                            删除
                                                        </Button>
                                                    </div>
                                                ),
                                            },
                                        ]}
                                    />
                                </div>
                                <div style={{ display: 'flex', justifyContent: 'flex-start' }}>
                                    <Button
                                        type="dashed"
                                        icon={<PlusOutlined />}
                                        onClick={handleAddField}
                                        size="middle"
                                    >
                                        添加字段
                                    </Button>
                                </div>
                            </div>
                        </Card>

                        <Divider style={{ margin: '0' }} />

                        <div style={{ padding: '16px 0', display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                            <Button onClick={handleCancel} size="large">
                                取消
                            </Button>
                            <Button type="primary" onClick={handleSubmit} size="large">
                                保存并提交
                            </Button>
                        </div>
                    </Form>
                </div>
            </Card>
        </div>
    );
};

export default AddDataAsset;

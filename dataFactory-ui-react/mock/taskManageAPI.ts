export default {
    // §11.1 查询任务分类树
    'GET /api/v1/task-categories/tree': {
        code: 100200,
        message: '操作成功',
        data: [
            {
                id: 1,
                name: '数据同步任务',
                parentId: 0,
                level: 1,
                sortOrder: 1,
                createTime: '2026-06-01 10:00:00',
                children: [
                    {
                        id: 2,
                        name: '全量同步',
                        parentId: 1,
                        level: 2,
                        sortOrder: 1,
                        createTime: '2026-06-01 10:00:00',
                        children: [],
                    },
                    {
                        id: 3,
                        name: '增量同步',
                        parentId: 1,
                        level: 2,
                        sortOrder: 2,
                        createTime: '2026-06-01 10:00:00',
                        children: [],
                    },
                ],
            },
            {
                id: 4,
                name: '数据处理任务',
                parentId: 0,
                level: 1,
                sortOrder: 2,
                createTime: '2026-06-02 10:00:00',
                children: [
                    {
                        id: 5,
                        name: '数据清洗',
                        parentId: 4,
                        level: 2,
                        sortOrder: 1,
                        createTime: '2026-06-02 10:00:00',
                        children: [],
                    },
                    {
                        id: 6,
                        name: '数据转换',
                        parentId: 4,
                        level: 2,
                        sortOrder: 2,
                        createTime: '2026-06-02 10:00:00',
                        children: [],
                    },
                ],
            },
            {
                id: 7,
                name: '报表生成任务',
                parentId: 0,
                level: 1,
                sortOrder: 3,
                createTime: '2026-06-03 10:00:00',
                children: [],
            },
        ],
        timestamp: 1718049600000,
    },

    // §11.2 查询任务列表
    'GET /api/v1/tasks': {
        code: 100200,
        message: '操作成功',
        data: {
            total: 6,
            pageNum: 1,
            pageSize: 20,
            records: [
                {
                    id: 1,
                    taskName: '企业信息每日同步',
                    taskDescription: '每日凌晨同步企业工商信息数据',
                    categoryId: 2,
                    status: 1,
                    executeStatus: 2,
                    scheduleType: 'CRON',
                    lastExecuteTime: '2026-06-02 02:00:00',
                    nextExecuteTime: '2026-06-03 02:00:00',
                    createTime: '2026-06-01 10:00:00',
                    updateTime: '2026-06-01 10:00:00',
                },
                {
                    id: 2,
                    taskName: '财务数据清洗任务',
                    taskDescription: '清洗并标准化财务数据',
                    categoryId: 5,
                    status: 0,
                    executeStatus: 0,
                    scheduleType: 'API',
                    lastExecuteTime: null,
                    nextExecuteTime: null,
                    createTime: '2026-06-02 10:00:00',
                    updateTime: '2026-06-02 10:00:00',
                },
                {
                    id: 3,
                    taskName: '订单数据增量同步',
                    taskDescription: '每小时增量同步订单数据',
                    categoryId: 3,
                    status: 1,
                    executeStatus: 1,
                    scheduleType: 'CRON',
                    lastExecuteTime: '2026-06-02 14:00:00',
                    nextExecuteTime: '2026-06-02 15:00:00',
                    createTime: '2026-06-02 11:00:00',
                    updateTime: '2026-06-02 11:00:00',
                },
                {
                    id: 4,
                    taskName: '月度报表生成',
                    taskDescription: '每月1号生成月度数据报表',
                    categoryId: 7,
                    status: 2,
                    executeStatus: 3,
                    scheduleType: 'CRON',
                    lastExecuteTime: '2026-06-01 00:00:00',
                    nextExecuteTime: null,
                    createTime: '2026-05-25 10:00:00',
                    updateTime: '2026-06-01 10:00:00',
                },
                {
                    id: 5,
                    taskName: '客户数据转换任务',
                    taskDescription: '转换客户数据格式',
                    categoryId: 6,
                    status: 1,
                    executeStatus: 2,
                    scheduleType: 'API',
                    lastExecuteTime: '2026-06-02 09:30:00',
                    nextExecuteTime: null,
                    createTime: '2026-06-01 14:00:00',
                    updateTime: '2026-06-01 14:00:00',
                },
                {
                    id: 6,
                    taskName: '库存数据同步',
                    taskDescription: '实时同步库存数据',
                    categoryId: 2,
                    status: 0,
                    executeStatus: 0,
                    scheduleType: 'CRON',
                    lastExecuteTime: null,
                    nextExecuteTime: null,
                    createTime: '2026-06-02 15:00:00',
                    updateTime: '2026-06-02 15:00:00',
                },
            ],
        },
        timestamp: 1718049600000,
    },

    // §11.3 查询任务详情
    'GET /api/v1/tasks/1': {
        code: 100200,
        message: '操作成功',
        data: {
            id: 1,
            taskName: '企业信息每日同步',
            taskDescription: '每日凌晨同步企业工商信息数据',
            categoryId: 2,
            status: 1,
            executeStatus: 2,
            scheduleType: 'CRON',
            lastExecuteTime: '2026-06-02 02:00:00',
            nextExecuteTime: '2026-06-03 02:00:00',
            createTime: '2026-06-01 10:00:00',
            updateTime: '2026-06-01 10:00:00',
            nodes: [
                {
                    nodeId: 'node_1',
                    nodeName: '开始',
                    nodeType: 'START',
                    positionX: 100,
                    positionY: 200,
                    config: {
                        description: '任务开始节点',
                    },
                },
                {
                    nodeId: 'node_2',
                    nodeName: '获取企业数据',
                    nodeType: 'API',
                    positionX: 300,
                    positionY: 200,
                    config: {
                        apiId: 10,
                        paramMapping: {
                            orderId: '${task_param.order_id}',
                        },
                        timeout: 60000,
                        retryCount: 2,
                    },
                },
                {
                    nodeId: 'node_3',
                    nodeName: '数据清洗',
                    nodeType: 'SCRIPT',
                    positionX: 500,
                    positionY: 200,
                    config: {
                        scriptId: 5,
                        scriptVersion: '1.2.0',
                        params: {
                            start_date: '${task_param.start_date}',
                            end_date: '${task_param.end_date}',
                        },
                        dataSourceId: 1,
                    },
                },
                {
                    nodeId: 'node_4',
                    nodeName: '字段映射',
                    nodeType: 'MAPPING',
                    positionX: 700,
                    positionY: 200,
                    config: {
                        mappings: [
                            {
                                sourceField: '${node_3.output.data.order_id}',
                                targetField: 'order_id',
                                transformRule: null,
                                defaultValue: null,
                            },
                            {
                                sourceField: '${node_3.output.data.order_amount}',
                                targetField: 'total_amount',
                                transformRule: 'amount * 100',
                                defaultValue: '0',
                            },
                        ],
                    },
                },
                {
                    nodeId: 'node_5',
                    nodeName: '写入目标库',
                    nodeType: 'OUTPUT',
                    positionX: 900,
                    positionY: 200,
                    config: {
                        outputType: 'DATABASE',
                        targetDataSourceId: 2,
                        targetTable: 'dwd_order_summary',
                        writeMode: 'INSERT',
                        fieldMappings: [
                            {
                                sourceField: '${node_4.output.order_id}',
                                targetColumn: 'order_id',
                            },
                        ],
                    },
                },
                {
                    nodeId: 'node_6',
                    nodeName: '结束',
                    nodeType: 'END',
                    positionX: 1100,
                    positionY: 200,
                    config: {
                        description: '任务结束节点',
                    },
                },
            ],
            edges: [
                {
                    edgeId: 'edge_1',
                    sourceNodeId: 'node_1',
                    targetNodeId: 'node_2',
                    condition: null,
                },
                {
                    edgeId: 'edge_2',
                    sourceNodeId: 'node_2',
                    targetNodeId: 'node_3',
                    condition: null,
                },
                {
                    edgeId: 'edge_3',
                    sourceNodeId: 'node_3',
                    targetNodeId: 'node_4',
                    condition: null,
                },
                {
                    edgeId: 'edge_4',
                    sourceNodeId: 'node_4',
                    targetNodeId: 'node_5',
                    condition: null,
                },
                {
                    edgeId: 'edge_5',
                    sourceNodeId: 'node_5',
                    targetNodeId: 'node_6',
                    condition: null,
                },
            ],
            triggerConfig: {
                scheduleType: 'CRON',
                cronExpression: '0 0 2 * * ?',
                effectiveDate: '2026-06-15 00:00:00',
                expireDate: '2027-06-15 00:00:00',
            },
        },
        timestamp: 1718049600000,
    },

    // §11.4 新增任务
    'POST /api/v1/tasks': {
        code: 100200,
        message: '新增成功',
        data: {
            id: 7,
            taskName: '新增同步任务',
            taskDescription: '新增的测试同步任务',
            categoryId: 2,
            status: 0,
            createTime: '2026-06-09 10:00:00',
            updateTime: '2026-06-09 10:00:00',
        },
        timestamp: 1718049600000,
    },

    // §11.5 更新任务DAG配置
    'PUT /api/v1/tasks/1/config': {
        code: 100200,
        message: '配置更新成功',
        data: {
            id: 1,
            taskName: '企业信息每日同步',
            nodes: [
                {
                    nodeId: 'node_1',
                    nodeName: '开始',
                    nodeType: 'START',
                    positionX: 100,
                    positionY: 200,
                    config: {
                        description: '任务开始节点',
                    },
                },
                {
                    nodeId: 'node_2',
                    nodeName: '获取企业数据',
                    nodeType: 'API',
                    positionX: 300,
                    positionY: 200,
                    config: {
                        apiId: 10,
                        paramMapping: {
                            orderId: '${task_param.order_id}',
                        },
                        timeout: 60000,
                        retryCount: 2,
                    },
                },
            ],
            edges: [
                {
                    edgeId: 'edge_1',
                    sourceNodeId: 'node_1',
                    targetNodeId: 'node_2',
                    condition: null,
                },
            ],
        },
        timestamp: 1718049600000,
    },

    // §11.9 设置任务触发方式
    'PUT /api/v1/tasks/1/trigger-config': {
        code: 100200,
        message: '触发配置成功',
        data: {
            id: 1,
            scheduleType: 'CRON',
            cronExpression: '0 0 2 * * ?',
            effectiveDate: '2026-06-15 00:00:00',
            expireDate: '2027-06-15 00:00:00',
        },
        timestamp: 1718049600000,
    },

    // §11.10 测试运行任务
    'POST /api/v1/tasks/1/test-run': {
        code: 100200,
        message: '任务测试运行完成',
        data: {
            executionId: 5678,
            status: 2,
            startTime: '2026-06-02 14:30:00',
            endTime: '2026-06-02 14:30:45',
            totalDuration: 45230,
            nodeResults: [
                {
                    nodeId: 'node_1',
                    nodeName: '开始',
                    nodeType: 'START',
                    status: 2,
                    startTime: '2026-06-02 14:30:00',
                    endTime: '2026-06-02 14:30:00',
                    duration: 10,
                    inputData: null,
                    outputData: null,
                    errorMessage: null,
                    logs: 'Start node executed.',
                },
                {
                    nodeId: 'node_2',
                    nodeName: '获取订单数据',
                    nodeType: 'API',
                    status: 2,
                    startTime: '2026-06-02 14:30:00',
                    endTime: '2026-06-02 14:30:12',
                    duration: 12000,
                    inputData: '{"orderId": "ORD20260602001"}',
                    outputData: '{"status":200,"data":{"orderId":"ORD20260602001","amount":299.00}}',
                    errorMessage: null,
                    logs: '[2026-06-02 14:30:00] Requesting API...\n[2026-06-02 14:30:12] Response received.',
                },
                {
                    nodeId: 'node_3',
                    nodeName: '数据清洗',
                    nodeType: 'SCRIPT',
                    status: 2,
                    startTime: '2026-06-02 14:30:12',
                    endTime: '2026-06-02 14:30:25',
                    duration: 13000,
                    inputData: '{"data": {"orderId":"ORD20260602001","amount":299.00}}',
                    outputData: '{"orderId":"ORD20260602001","cleanedAmount":299.00}',
                    errorMessage: null,
                    logs: '[2026-06-02 14:30:12] Script started...\n[2026-06-02 14:30:25] Script completed.',
                },
                {
                    nodeId: 'node_4',
                    nodeName: '字段映射',
                    nodeType: 'MAPPING',
                    status: 2,
                    startTime: '2026-06-02 14:30:25',
                    endTime: '2026-06-02 14:30:30',
                    duration: 5000,
                    inputData: '{"orderId":"ORD20260602001","cleanedAmount":299.00}',
                    outputData: '{"order_id":"ORD20260602001","total_amount":29900}',
                    errorMessage: null,
                    logs: 'Field mapping completed.',
                },
                {
                    nodeId: 'node_5',
                    nodeName: '写入目标库',
                    nodeType: 'OUTPUT',
                    status: 2,
                    startTime: '2026-06-02 14:30:30',
                    endTime: '2026-06-02 14:30:40',
                    duration: 10000,
                    inputData: '{"order_id":"ORD20260602001","total_amount":29900}',
                    outputData: '{"affectedRows":1}',
                    errorMessage: null,
                    logs: '[2026-06-02 14:30:30] Writing to database...\n[2026-06-02 14:30:40] Write completed.',
                },
                {
                    nodeId: 'node_6',
                    nodeName: '结束',
                    nodeType: 'END',
                    status: 2,
                    startTime: '2026-06-02 14:30:40',
                    endTime: '2026-06-02 14:30:45',
                    duration: 5220,
                    inputData: null,
                    outputData: null,
                    errorMessage: null,
                    logs: 'Task execution completed.',
                },
            ],
        },
        timestamp: 1718049600000,
    },

    // §11.11 查询任务执行历史
    'GET /api/v1/tasks/1/executions': {
        code: 100200,
        message: '操作成功',
        data: {
            total: 5,
            pageNum: 1,
            pageSize: 20,
            records: [
                {
                    executionId: 1001,
                    status: 2,
                    startTime: '2026-06-02 02:00:00',
                    endTime: '2026-06-02 02:00:45',
                    duration: 45000,
                    triggerType: 'CRON',
                    triggerBy: '系统定时',
                },
                {
                    executionId: 1002,
                    status: 2,
                    startTime: '2026-06-01 02:00:00',
                    endTime: '2026-06-01 02:00:42',
                    duration: 42000,
                    triggerType: 'CRON',
                    triggerBy: '系统定时',
                },
                {
                    executionId: 1003,
                    status: 3,
                    startTime: '2026-05-31 02:00:00',
                    endTime: '2026-05-31 02:00:30',
                    duration: 30000,
                    triggerType: 'CRON',
                    triggerBy: '系统定时',
                },
                {
                    executionId: 1004,
                    status: 2,
                    startTime: '2026-05-30 02:00:00',
                    endTime: '2026-05-30 02:00:38',
                    duration: 38000,
                    triggerType: 'API',
                    triggerBy: 'admin',
                },
                {
                    executionId: 1005,
                    status: 2,
                    startTime: '2026-05-29 02:00:00',
                    endTime: '2026-05-29 02:00:40',
                    duration: 40000,
                    triggerType: 'CRON',
                    triggerBy: '系统定时',
                },
            ],
        },
        timestamp: 1718049600000,
    },

    // §11.12 手动执行任务
    'POST /api/v1/tasks/1/execute': {
        code: 100200,
        message: '任务执行已触发',
        data: {
            executionId: 1006,
        },
        timestamp: 1718049600000,
    },

    // §11.13 停止正在执行的任务
    'POST /api/v1/tasks/1/executions/1006/cancel': {
        code: 100200,
        message: '任务已停止',
        data: {
            executionId: 1006,
            status: 4,
        },
        timestamp: 1718049600000,
    },

    // §11.14 发布任务
    'PUT /api/v1/tasks/2/publish': {
        code: 100200,
        message: '发布成功',
        data: {
            id: 2,
            status: 1,
        },
        timestamp: 1718049600000,
    },

    // §11.14 停用任务
    'PUT /api/v1/tasks/1/disable': {
        code: 100200,
        message: '停用成功',
        data: {
            id: 1,
            status: 2,
        },
        timestamp: 1718049600000,
    },

    // §11.14 删除任务
    'DELETE /api/v1/tasks/6': {
        code: 100200,
        message: '删除成功',
        timestamp: 1718049600000,
    },

    // §11.15 批量发布任务
    'PUT /api/v1/tasks/batch/publish': {
        code: 100200,
        message: '批量发布成功',
        data: {
            successCount: 2,
            failCount: 0,
        },
        timestamp: 1718049600000,
    },

    // §11.16 批量停用任务
    'PUT /api/v1/tasks/batch/disable': {
        code: 100200,
        message: '批量停用成功',
        data: {
            successCount: 2,
            failCount: 0,
        },
        timestamp: 1718049600000,
    },

    // §11.18 新增分类
    'POST /api/v1/task-categories': {
        code: 100200,
        message: '新增成功',
        data: {
            id: 10,
            name: '新增任务分类',
            parentId: 1,
            level: 2,
            sortOrder: 3,
            createTime: '2026-06-09 10:00:00',
        },
        timestamp: 1718049600000,
    },

    // §11.18 编辑分类
    'PUT /api/v1/task-categories/7': {
        code: 100200,
        message: '编辑成功',
        data: {
            id: 7,
            name: '报表生成任务（已更新）',
            parentId: 0,
            level: 1,
            sortOrder: 3,
            createTime: '2026-06-03 10:00:00',
            updateTime: '2026-06-09 10:00:00',
        },
        timestamp: 1718049600000,
    },

    // §11.18 删除分类
    'DELETE /api/v1/task-categories/7': {
        code: 100200,
        message: '删除成功',
        timestamp: 1718049600000,
    },
}
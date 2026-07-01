export default {
    // §10.1 查询脚本分类树
    'GET /api/v1/script-categories/tree': {
        code: 100200,
        message: '操作成功',
        data: [
            {
                id: 1,
                name: '数据清洗脚本',
                parentId: 0,
                level: 1,
                sortOrder: 1,
                createTime: '2026-06-01 10:00:00',
                children: [
                    {
                        id: 2,
                        name: '工商数据清洗',
                        parentId: 1,
                        level: 2,
                        sortOrder: 1,
                        createTime: '2026-06-01 10:00:00',
                        children: [],
                    },
                    {
                        id: 3,
                        name: '财务数据清洗',
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
                name: '数据转换脚本',
                parentId: 0,
                level: 1,
                sortOrder: 2,
                createTime: '2026-06-02 10:00:00',
                children: [
                    {
                        id: 5,
                        name: '格式转换',
                        parentId: 4,
                        level: 2,
                        sortOrder: 1,
                        createTime: '2026-06-02 10:00:00',
                        children: [],
                    },
                ],
            },
            {
                id: 6,
                name: '数据校验脚本',
                parentId: 0,
                level: 1,
                sortOrder: 3,
                createTime: '2026-06-03 10:00:00',
                children: [],
            },
        ],
        timestamp: 1718049600000,
    },

    // §10.2 查询脚本列表
    'GET /api/v1/scripts': {
        code: 100200,
        message: '操作成功',
        data: {
            total: 6,
            pageNum: 1,
            pageSize: 20,
            records: [
                {
                    id: 1,
                    scriptName: '工商信息清洗脚本',
                    scriptType: 'PYTHON',
                    categoryId: 2,
                    fileName: 'business_data_clean.py',
                    description: '清洗企业工商信息数据',
                    status: 1,
                    createTime: '2026-06-01 10:00:00',
                    updateTime: '2026-06-01 10:00:00',
                },
                {
                    id: 2,
                    scriptName: '财务数据标准化脚本',
                    scriptType: 'PYTHON',
                    categoryId: 3,
                    fileName: 'finance_data_standard.py',
                    description: '标准化企业财务数据',
                    status: 0,
                    createTime: '2026-06-02 10:00:00',
                    updateTime: '2026-06-02 10:00:00',
                },
                {
                    id: 3,
                    scriptName: 'JSON转Excel脚本',
                    scriptType: 'PYTHON',
                    categoryId: 5,
                    fileName: 'json_to_excel.py',
                    description: '将JSON格式转换为Excel',
                    status: 1,
                    createTime: '2026-06-03 10:00:00',
                    updateTime: '2026-06-03 10:00:00',
                },
                {
                    id: 4,
                    scriptName: '数据完整性校验脚本',
                    scriptType: 'PYTHON',
                    categoryId: 6,
                    fileName: 'data_integrity_check.py',
                    description: '校验数据完整性和一致性',
                    status: 2,
                    createTime: '2026-06-04 10:00:00',
                    updateTime: '2026-06-05 10:00:00',
                },
                {
                    id: 5,
                    scriptName: '注册信息清洗脚本',
                    scriptType: 'PYTHON',
                    categoryId: 2,
                    fileName: 'register_info_clean.py',
                    description: '清洗企业注册信息',
                    status: 0,
                    createTime: '2026-06-05 10:00:00',
                    updateTime: '2026-06-05 10:00:00',
                },
                {
                    id: 6,
                    scriptName: 'CSV转JSON脚本',
                    scriptType: 'PYTHON',
                    categoryId: 5,
                    fileName: 'csv_to_json.py',
                    description: '将CSV格式转换为JSON',
                    status: 1,
                    createTime: '2026-06-06 10:00:00',
                    updateTime: '2026-06-06 10:00:00',
                },
            ],
        },
        timestamp: 1718049600000,
    },

    // §10.3 查询脚本详情
    'GET /api/v1/scripts/1': {
        code: 100200,
        message: '操作成功',
        data: {
            id: 1,
            scriptName: '工商信息清洗脚本',
            scriptType: 'PYTHON',
            categoryId: 2,
            fileName: 'business_data_clean.py',
            description: '清洗企业工商信息数据',
            status: 1,
            createTime: '2026-06-01 10:00:00',
            updateTime: '2026-06-01 10:00:00',
            inputParams: [
                {
                    id: 1,
                    paramName: '企业唯一id',
                    paramType: 'Int',
                    description: '企业唯一标识',
                },
                {
                    id: 2,
                    paramName: '数据源路径',
                    paramType: 'String',
                    description: '原始数据文件路径',
                },
            ],
            outputParams: [
                {
                    id: 1,
                    paramName: 'id',
                    paramType: 'Int',
                    description: '企业唯一id',
                },
                {
                    id: 2,
                    paramName: '企业名称',
                    paramType: 'String',
                    description: '清洗后的企业名称',
                },
            ],
        },
        timestamp: 1718049600000,
    },

    // §10.4 新增脚本
    'POST /api/v1/scripts': {
        code: 100200,
        message: '新增成功',
        data: {
            id: 7,
            scriptName: '新增脚本',
            scriptType: 'PYTHON',
            categoryId: 2,
            fileName: 'new_script.py',
            description: '新增测试脚本',
            status: 0,
            createTime: '2026-06-09 10:00:00',
            updateTime: '2026-06-09 10:00:00',
            inputParams: [],
            outputParams: [],
        },
        timestamp: 1718049600000,
    },

    // §10.5 编辑脚本
    'PUT /api/v1/scripts/2': {
        code: 100200,
        message: '编辑成功',
        data: {
            id: 2,
            scriptName: '财务数据标准化脚本',
            scriptType: 'PYTHON',
            categoryId: 3,
            fileName: 'finance_data_standard.py',
            description: '标准化企业财务数据（已更新）',
            status: 0,
            createTime: '2026-06-02 10:00:00',
            updateTime: '2026-06-09 10:00:00',
            inputParams: [],
            outputParams: [],
        },
        timestamp: 1718049600000,
    },

    // §10.6 在线调试脚本
    'POST /api/v1/scripts/1/debug': {
        code: 100200,
        message: '执行成功',
        data: {
            success: true,
            executeTime: 1234,
            result: '{"id": 1001, "企业名称": "示例企业"}',
            errorMessage: null,
        },
        timestamp: 1718049600000,
    },

    // §10.7 发布脚本
    'PUT /api/v1/scripts/2/publish': {
        code: 100200,
        message: '发布成功',
        data: {
            id: 2,
            status: 1,
        },
        timestamp: 1718049600000,
    },

    // §10.7 停用脚本
    'PUT /api/v1/scripts/1/disable': {
        code: 100200,
        message: '停用成功',
        data: {
            id: 1,
            status: 2,
        },
        timestamp: 1718049600000,
    },

    // §10.7 删除脚本
    'DELETE /api/v1/scripts/5': {
        code: 100200,
        message: '删除成功',
        timestamp: 1718049600000,
    },

    // §10.8 批量发布脚本
    'PUT /api/v1/scripts/batch/publish': {
        code: 100200,
        message: '批量发布成功',
        data: {
            successCount: 2,
            failCount: 0,
        },
        timestamp: 1718049600000,
    },

    // §10.9 批量停用脚本
    'PUT /api/v1/scripts/batch/disable': {
        code: 100200,
        message: '批量停用成功',
        data: {
            successCount: 2,
            failCount: 0,
        },
        timestamp: 1718049600000,
    },

    // §10.10 新增分类
    'POST /api/v1/script-categories': {
        code: 100200,
        message: '新增成功',
        data: {
            id: 10,
            name: '新增脚本分类',
            parentId: 1,
            level: 2,
            sortOrder: 3,
            createTime: '2026-06-09 10:00:00',
        },
        timestamp: 1718049600000,
    },

    // §10.10 编辑分类
    'PUT /api/v1/script-categories/6': {
        code: 100200,
        message: '编辑成功',
        data: {
            id: 6,
            name: '数据校验脚本（已更新）',
            parentId: 0,
            level: 1,
            sortOrder: 3,
            createTime: '2026-06-03 10:00:00',
            updateTime: '2026-06-09 10:00:00',
        },
        timestamp: 1718049600000,
    },

    // §10.10 删除分类
    'DELETE /api/v1/script-categories/6': {
        code: 100200,
        message: '删除成功',
        timestamp: 1718049600000,
    },
}
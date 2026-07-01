export default {
  'GET /api/v1/code-tables': {
    code: 100200,
    message: '操作成功',
    data: {
      total: 6,
      pageNum: 1,
      pageSize: 20,
      records: [
        {
          id: 1,
          tableName: '企业类型',
          tableCode: 'MZB00001',
          description: '企业类型枚举码表',
          status: 1,
          codeItemCount: 3,
          createTime: '2026-06-01 10:00:00',
          updateTime: '2026-06-01 10:00:00',
        },
        {
          id: 2,
          tableName: '企业经营状态',
          tableCode: 'MZB00002',
          description: '企业经营状态码表',
          status: 0,
          codeItemCount: 4,
          createTime: '2026-06-02 10:00:00',
          updateTime: '2026-06-02 10:00:00',
        },
        {
          id: 3,
          tableName: '企业行业',
          tableCode: 'MZB00003',
          description: '企业所属行业分类',
          status: 2,
          codeItemCount: 5,
          createTime: '2026-06-03 10:00:00',
          updateTime: '2026-06-03 10:00:00',
        },
        {
          id: 4,
          tableName: '员工学历',
          tableCode: 'MZB00004',
          description: '员工学历类型',
          status: 1,
          codeItemCount: 6,
          createTime: '2026-06-04 10:00:00',
          updateTime: '2026-06-04 10:00:00',
        },
        {
          id: 5,
          tableName: '合同类型',
          tableCode: 'MZB00005',
          description: '合同分类码表',
          status: 0,
          codeItemCount: 2,
          createTime: '2026-06-05 10:00:00',
          updateTime: '2026-06-05 10:00:00',
        },
        {
          id: 6,
          tableName: '审批状态',
          tableCode: 'MZB00006',
          description: '审批流程状态码表',
          status: 2,
          codeItemCount: 3,
          createTime: '2026-06-06 10:00:00',
          updateTime: '2026-06-06 10:00:00',
        },
      ],
    },
    timestamp: 1718049600000,
  },

  'GET /api/v1/code-tables/1': {
    code: 100200,
    message: '操作成功',
    data: {
      id: 1,
      tableName: '企业类型',
      tableCode: 'MZB00001',
      description: '企业类型枚举码表',
      status: 1,
      codeItemCount: 3,
      createTime: '2026-06-01 10:00:00',
      updateTime: '2026-06-01 10:00:00',
    },
    timestamp: 1718049600000,
  },

  'GET /api/v1/code-tables/1/items': {
    code: 100200,
    message: '操作成功',
    data: {
      tableInfo: {
        id: 1,
        tableName: '企业类型',
        tableCode: 'MZB00001',
        description: '企业类型枚举码表',
      },
      items: [
        {
          id: 1,
          code: '01',
          name: '国有企业',
          value: 'STATED_OWNED',
          sortOrder: 1,
          parentCode: null,
          status: 1,
          description: '国有控股企业',
        },
        {
          id: 2,
          code: '02',
          name: '民营企业',
          value: 'PRIVATE',
          sortOrder: 2,
          parentCode: null,
          status: 1,
          description: '私人控股企业',
        },
        {
          id: 3,
          code: '03',
          name: '外资企业',
          value: 'FOREIGN',
          sortOrder: 3,
          parentCode: null,
          status: 0,
          description: '外商投资企业',
        },
      ],
    },
    timestamp: 1718049600000,
  },

  'POST /api/v1/code-tables': {
    code: 100200,
    message: '新增成功',
    data: {
      id: 7,
      tableName: '新增码表',
      tableCode: 'MZB00007',
      description: '新增测试码表',
      status: 0,
      codeItemCount: 2,
      createTime: '2026-06-09 10:00:00',
      updateTime: '2026-06-09 10:00:00',
    },
    timestamp: 1718049600000,
  },

  'PUT /api/v1/code-tables/2': {
    code: 100200,
    message: '编辑成功',
    data: {
      id: 2,
      tableName: '企业经营状态',
      tableCode: 'MZB00002',
      description: '企业经营状态码表（已更新）',
      status: 0,
      codeItemCount: 4,
      createTime: '2026-06-02 10:00:00',
      updateTime: '2026-06-09 10:00:00',
    },
    timestamp: 1718049600000,
  },

  'POST /api/v1/code-tables/1/items': {
    code: 100200,
    message: '新增码值成功',
    data: {
      id: 4,
      code: '04',
      name: '合资企业',
      value: 'JOINT_VENTURE',
      sortOrder: 4,
      parentCode: null,
      status: 1,
      description: '中外合资企业',
    },
    timestamp: 1718049600000,
  },

  'PUT /api/v1/code-tables/1/items/1': {
    code: 100200,
    message: '更新码值成功',
    data: {
      id: 1,
      code: '01',
      name: '国有企业',
      value: 'STATED_OWNED',
      sortOrder: 1,
      parentCode: null,
      status: 1,
      description: '国有控股企业（已更新）',
    },
    timestamp: 1718049600000,
  },

  'DELETE /api/v1/code-tables/1/items/3': {
    code: 100200,
    message: '删除码值成功',
    timestamp: 1718049600000,
  },

  'PUT /api/v1/code-tables/2/publish': {
    code: 100200,
    message: '发布成功',
    data: {
      id: 2,
      status: 1,
    },
    timestamp: 1718049600000,
  },

  'PUT /api/v1/code-tables/1/disable': {
    code: 100200,
    message: '停用成功',
    data: {
      id: 1,
      status: 2,
    },
    timestamp: 1718049600000,
  },

  'DELETE /api/v1/code-tables/5': {
    code: 100200,
    message: '删除成功',
    timestamp: 1718049600000,
  },

  'PUT /api/v1/code-tables/batch/publish': {
    code: 100200,
    message: '批量发布成功',
    data: {
      successCount: 2,
      failCount: 0,
    },
    timestamp: 1718049600000,
  },

  'PUT /api/v1/code-tables/batch/disable': {
    code: 100200,
    message: '批量停用成功',
    data: {
      successCount: 2,
      failCount: 0,
    },
    timestamp: 1718049600000,
  },
}

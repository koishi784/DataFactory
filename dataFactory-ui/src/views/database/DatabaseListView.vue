<template>
  <PageContainer title="数据库连接管理">
    <SearchForm :model="searchForm" :fields="searchFields" @search="handleSearch" @reset="handleReset" />
    <BatchActions :has-selection="selectedIds.length > 0" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable" />
    <DataTable
      :data="list" :loading="loading" :total="total"
      :current-page="pagination.pageNum" :page-size="pagination.pageSize"
      selectable show-pagination
      @selection-change="handleSelectionChange"
      @page-change="handlePageChange" @size-change="handleSizeChange"
    >
      <el-table-column prop="connectionName" label="连接名称" show-overflow-tooltip />
      <el-table-column prop="dbType" label="数据库类型" width="120" />
      <el-table-column prop="host" label="主机地址" width="140" />
      <el-table-column prop="databaseName" label="数据库名称" width="140" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }"><StatusTag :status="row.status" /></template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link size="small" @click="router.push(`/database/${row.id}/detail`)">详情</el-button>
          <el-button link size="small" @click="handleTest(row.id)">测试</el-button>
          <StatusAction :status="row.status" @edit="router.push(`/database/${row.id}/edit`)" @publish="handlePublish(row.id, row.connectionName)" @disable="handleDisable(row.id, row.connectionName)" @delete="handleDelete(row.id, row.connectionName)" />
        </template>
      </el-table-column>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'
import DataTable from '@/components/DataTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import StatusAction from '@/components/StatusAction.vue'
import BatchActions from '@/components/BatchActions.vue'
import { useCrud } from '@/composables/useCrud'
import { useStatusActions } from '@/composables/useStatusActions'
import { getDatabaseList, publishDatabase, disableDatabase, deleteDatabase, batchPublishDatabase, batchDisableDatabase, testDatabase } from '@/api/database'
import type { DatabaseConnection } from '@/types/database'

const router = useRouter()
const selectedIds = ref<number[]>([])

const searchFields = [
  { prop: 'connectionName', label: '连接名称' },
  { prop: 'dbType', label: '数据库类型', type: 'select' as const, options: [
    { label: 'MySQL', value: 'MySQL' }, { label: 'PostgreSQL', value: 'PostgreSQL' },
    { label: 'Oracle', value: 'Oracle' }, { label: 'SQLServer', value: 'SQLServer' },
    { label: 'Hive', value: 'Hive' }, { label: 'ClickHouse', value: 'ClickHouse' },
  ]},
]

const { list, loading, total, pagination, searchForm, handleSearch, handleReset, handlePageChange, handleSizeChange, fetchData } = useCrud<DatabaseConnection>({ apiGetList: getDatabaseList })
const { handlePublish, handleDisable, handleDelete, handleBatchPublish, handleBatchDisable } = useStatusActions({
  apiPublish: publishDatabase, apiDisable: disableDatabase, apiDelete: deleteDatabase,
  batchPublish: batchPublishDatabase, batchDisable: batchDisableDatabase,
  fetchData, getSelectedIds: () => selectedIds.value, getSelectedStatuses: () => [],
})

async function handleTest(id: number) {
  try {
    await testDatabase(id)
    ElMessage.success('数据库连接测试成功')
  } catch { /* handled by interceptor */ }
}

function handleSelectionChange(selection: any[]) {
  selectedIds.value = selection.map((s: any) => s.id)
}

fetchData()
</script>

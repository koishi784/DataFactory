<template>
  <PageContainer title="脚本管理">
    <SearchForm :model="searchForm" :fields="searchFields" @search="handleSearch" @reset="handleReset" />
    <BatchActions :has-selection="selectedIds.length > 0" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable" />
    <DataTable
      :data="list" :loading="loading" :total="total"
      :current-page="pagination.pageNum" :page-size="pagination.pageSize"
      selectable show-pagination
      @selection-change="handleSelectionChange"
      @page-change="handlePageChange" @size-change="handleSizeChange"
    >
      <el-table-column prop="scriptName" label="脚本名称" show-overflow-tooltip />
      <el-table-column prop="scriptType" label="脚本类型" width="120" />
      <el-table-column prop="version" label="版本号" width="90" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }"><StatusTag :status="row.status" /></template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <StatusAction :status="row.status" @edit="router.push(`/script/${row.id}/edit`)" @publish="handlePublish(row.id, row.scriptName)" @disable="handleDisable(row.id, row.scriptName)" @delete="handleDelete(row.id, row.scriptName)" />
          <el-button link size="small">调试</el-button>
        </template>
      </el-table-column>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'
import DataTable from '@/components/DataTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import StatusAction from '@/components/StatusAction.vue'
import BatchActions from '@/components/BatchActions.vue'
import { useCrud } from '@/composables/useCrud'
import { useStatusActions } from '@/composables/useStatusActions'
import { getScriptList, publishScript, disableScript, deleteScript, batchPublishScript, batchDisableScript } from '@/api/script'
import type { Script } from '@/types/script'

const router = useRouter()
const selectedIds = ref<number[]>([])

const searchFields = [
  { prop: 'scriptName', label: '脚本名称' },
  { prop: 'scriptType', label: '脚本类型', type: 'select' as const, options: [{ label: 'Python', value: 'Python' }] },
]

const { list, loading, total, pagination, searchForm, handleSearch, handleReset, handlePageChange, handleSizeChange, fetchData } = useCrud<Script>({ apiGetList: getScriptList })
const { handlePublish, handleDisable, handleDelete, handleBatchPublish, handleBatchDisable } = useStatusActions({
  apiPublish: publishScript, apiDisable: disableScript, apiDelete: deleteScript,
  batchPublish: batchPublishScript, batchDisable: batchDisableScript,
  fetchData, getSelectedIds: () => selectedIds.value, getSelectedStatuses: () => [],
})

function handleSelectionChange(selection: any[]) { selectedIds.value = selection.map((s: any) => s.id) }
fetchData()
</script>

<template>
  <PageContainer title="接口列表">
    <SearchForm :model="searchForm" :fields="searchFields" @search="handleSearch" @reset="handleReset" />
    <BatchActions :has-selection="selectedIds.length > 0" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable" />
    <DataTable
      :data="list" :loading="loading" :total="total"
      :current-page="pagination.pageNum" :page-size="pagination.pageSize"
      selectable show-pagination
      @selection-change="handleSelectionChange"
      @page-change="handlePageChange" @size-change="handleSizeChange"
    >
      <el-table-column prop="apiName" label="接口名称" show-overflow-tooltip />
      <el-table-column prop="method" label="请求方法" width="90" />
      <el-table-column prop="url" label="接口地址" show-overflow-tooltip />
      <el-table-column prop="source" label="接口来源" width="120" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }"><StatusTag :status="row.status" /></template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <StatusAction :status="row.status" @edit="router.push(`/api/${row.id}/edit`)" @publish="handlePublish(row.id, row.apiName)" @disable="handleDisable(row.id, row.apiName)" @delete="handleDelete(row.id, row.apiName)" />
          <el-button link size="small" @click="router.push(`/api/${row.id}/test`)">测试</el-button>
        </template>
      </el-table-column>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
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
import { getApiList, publishApi, disableApi, deleteApi, batchPublishApi, batchDisableApi } from '@/api/api'
import type { ApiInfo } from '@/types/api'

const router = useRouter()
const selectedIds = ref<number[]>([])

const searchFields = [
  { prop: 'apiName', label: '接口名称' },
  { prop: 'method', label: '请求方法', type: 'select' as const, options: [{ label: 'GET', value: 'GET' }, { label: 'POST', value: 'POST' }, { label: 'PUT', value: 'PUT' }, { label: 'DELETE', value: 'DELETE' }] },
]

const { list, loading, total, pagination, searchForm, handleSearch, handleReset, handlePageChange, handleSizeChange, fetchData } = useCrud<ApiInfo>({ apiGetList: getApiList })
const { handlePublish, handleDisable, handleDelete, handleBatchPublish, handleBatchDisable } = useStatusActions({
  apiPublish: publishApi, apiDisable: disableApi, apiDelete: deleteApi,
  batchPublish: batchPublishApi, batchDisable: batchDisableApi,
  fetchData, getSelectedIds: () => selectedIds.value, getSelectedStatuses: () => [],
})

function handleSelectionChange(selection: any[]) {
  selectedIds.value = selection.map((s: any) => s.id)
}

fetchData()
</script>

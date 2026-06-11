<template>
  <PageContainer title="数据标准目录">
    <SearchForm :model="searchForm" :fields="searchFields" @search="handleSearch" @reset="handleReset" />
    <BatchActions :has-selection="selectedIds.length > 0" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable" />
    <DataTable
      :data="list" :loading="loading" :total="total"
      :current-page="pagination.pageNum" :page-size="pagination.pageSize"
      selectable show-pagination
      @selection-change="handleSelectionChange"
      @page-change="handlePageChange" @size-change="handleSizeChange"
    >
      <el-table-column prop="standardName" label="标准名称" show-overflow-tooltip />
      <el-table-column prop="standardCode" label="标准编码" width="140" />
      <el-table-column prop="dataType" label="数据类型" width="100" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }"><StatusTag :status="row.status" /></template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <StatusAction :status="row.status" @edit="router.push(`/standard/${row.id}/edit`)" @publish="handlePublish(row.id, row.standardName)" @disable="handleDisable(row.id, row.standardName)" @delete="handleDelete(row.id, row.standardName)" />
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
import { getDataStandardList, publishDataStandard, disableDataStandard, deleteDataStandard, batchPublishDataStandard, batchDisableDataStandard } from '@/api/dataStandard'
import type { DataStandard } from '@/types/dataStandard'

const router = useRouter()
const selectedIds = ref<number[]>([])

const searchFields = [
  { prop: 'standardName', label: '标准名称' },
  { prop: 'standardCode', label: '标准编码' },
]

const { list, loading, total, pagination, searchForm, handleSearch, handleReset, handlePageChange, handleSizeChange, fetchData } = useCrud<DataStandard>({ apiGetList: getDataStandardList })
const { handlePublish, handleDisable, handleDelete, handleBatchPublish, handleBatchDisable } = useStatusActions({
  apiPublish: publishDataStandard, apiDisable: disableDataStandard, apiDelete: deleteDataStandard,
  batchPublish: batchPublishDataStandard, batchDisable: batchDisableDataStandard,
  fetchData, getSelectedIds: () => selectedIds.value, getSelectedStatuses: () => [],
})

function handleSelectionChange(selection: any[]) {
  selectedIds.value = selection.map((s: any) => s.id)
}

fetchData()
</script>

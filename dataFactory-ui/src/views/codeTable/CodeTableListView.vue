<template>
  <PageContainer title="码表管理">
    <SearchForm :model="searchForm" :fields="searchFields" @search="handleSearch" @reset="handleReset" />
    <div class="toolbar">
      <BatchActions :has-selection="selectedIds.length > 0" :show-category="false" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable">
        <template #extra>
          <el-button type="primary" @click="router.push('/code-table/create')">新增码表</el-button>
        </template>
      </BatchActions>
    </div>
    <DataTable
      :data="list" :loading="loading" :total="total"
      :current-page="pagination.pageNum" :page-size="pagination.pageSize"
      selectable show-pagination
      @selection-change="handleSelectionChange"
      @page-change="handlePageChange" @size-change="handleSizeChange"
    >
      <el-table-column prop="tableName" label="码表名称" show-overflow-tooltip width="300" />
      <el-table-column prop="tableCode" label="码表编码" width="300" />
      <el-table-column prop="description" label="码表说明" show-overflow-tooltip min-width="160" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }"><StatusTag :status="row.status" /></template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <StatusAction :status="row.status" @edit="router.push(`/code-table/${row.id}/edit`)" @publish="handlePublish(row.id, row.tableName)" @disable="handleDisable(row.id, row.tableName)" @delete="handleDelete(row.id, row.tableName)" />
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
import { getCodeTableList, publishCodeTable, disableCodeTable, deleteCodeTable, batchPublishCodeTable, batchDisableCodeTable } from '@/api/codeTable'

const router = useRouter()
const selectedIds = ref<number[]>([])

const searchFields = [
  { prop: 'tableName', label: '码表名称' },
  { prop: 'tableCode', label: '码表编码' },
]

const { list, loading, total, pagination, searchForm, handleSearch, handleReset, handlePageChange, handleSizeChange, fetchData } = useCrud<CodeTable>({ apiGetList: getCodeTableList })
const { handlePublish, handleDisable, handleDelete, handleBatchPublish, handleBatchDisable } = useStatusActions({
  apiPublish: publishCodeTable, apiDisable: disableCodeTable, apiDelete: deleteCodeTable,
  batchPublish: batchPublishCodeTable, batchDisable: batchDisableCodeTable,
  fetchData, getSelectedIds: () => selectedIds.value, getSelectedStatuses: () => [],
})

function handleSelectionChange(selection: any[]) {
  selectedIds.value = selection.map((s: any) => s.id)
}

fetchData()
</script>

<style scoped lang="scss">
.toolbar {
  display: flex;
  align-items: center;
  padding-bottom: 12px;
}
</style>

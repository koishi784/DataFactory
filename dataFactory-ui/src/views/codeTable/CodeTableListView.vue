<template>
  <PageContainer title="码表管理">
    <SearchForm :model="searchForm" :fields="searchFields" @search="handleSearch" @reset="handleReset" />
    <BatchActions :has-selection="selectedIds.length > 0" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable" />
    <DataTable
      :data="list" :loading="loading" :total="total"
      :current-page="pagination.pageNum" :page-size="pagination.pageSize"
      selectable show-pagination
      @selection-change="handleSelectionChange"
      @page-change="handlePageChange" @size-change="handleSizeChange"
    >
      <el-table-column prop="tableName" label="码表名称" show-overflow-tooltip />
      <el-table-column prop="tableCode" label="码表编码" width="140" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }"><StatusTag :status="row.status" /></template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link size="small" @click="openCodeItems(row)">码值管理</el-button>
          <StatusAction :status="row.status" @edit="router.push(`/code-table/${row.id}/edit`)" @publish="handlePublish(row.id, row.tableName)" @disable="handleDisable(row.id, row.tableName)" @delete="handleDelete(row.id, row.tableName)" />
        </template>
      </el-table-column>
    </DataTable>

    <!-- 码值管理弹窗 -->
    <el-dialog v-model="codeItemVisible" title="码值管理" width="600px" destroy-on-close>
      <p>码值列表（待实现）</p>
    </el-dialog>
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
import type { CodeTable } from '@/types/codeTable'

const router = useRouter()
const selectedIds = ref<number[]>([])
const codeItemVisible = ref(false)
const currentCodeTable = ref<CodeTable | null>(null)

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

function openCodeItems(row: CodeTable) {
  currentCodeTable.value = row
  codeItemVisible.value = true
}

function handleSelectionChange(selection: any[]) {
  selectedIds.value = selection.map((s: any) => s.id)
}

fetchData()
</script>

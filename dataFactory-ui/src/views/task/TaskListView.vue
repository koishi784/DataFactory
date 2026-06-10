<template>
  <PageContainer title="任务管理">
    <SearchForm :model="searchForm" :fields="searchFields" @search="handleSearch" @reset="handleReset" />
    <BatchActions :has-selection="selectedIds.length > 0" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable" />
    <DataTable
      :data="list" :loading="loading" :total="total"
      :current-page="pagination.pageNum" :page-size="pagination.pageSize"
      selectable show-pagination
      @selection-change="handleSelectionChange"
      @page-change="handlePageChange" @size-change="handleSizeChange"
    >
      <el-table-column prop="taskName" label="任务名称" show-overflow-tooltip />
      <el-table-column prop="scheduleType" label="调度类型" width="120" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }"><StatusTag :status="row.status" /></template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button link size="small" @click="router.push(`/task/${row.id}/dag`)">DAG编排</el-button>
          <el-button link size="small" @click="router.push(`/task/${row.id}/executions`)">执行历史</el-button>
          <StatusAction :status="row.status" @edit="router.push(`/task/${row.id}/edit`)" @publish="handlePublish(row.id, row.taskName)" @disable="handleDisable(row.id, row.taskName)" @delete="handleDelete(row.id, row.taskName)" />
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
import { getTaskList, publishTask, disableTask, deleteTask, batchPublishTask, batchDisableTask } from '@/api/task'
import type { Task } from '@/types/task'

const router = useRouter()
const selectedIds = ref<number[]>([])

const searchFields = [
  { prop: 'taskName', label: '任务名称' },
  { prop: 'scheduleType', label: '调度类型', type: 'select' as const, options: [{ label: '手动', value: 'MANUAL' }, { label: 'CRON', value: 'CRON' }, { label: 'API', value: 'API' }] },
]

const { list, loading, total, pagination, searchForm, handleSearch, handleReset, handlePageChange, handleSizeChange, fetchData } = useCrud<Task>({ apiGetList: getTaskList })
const { handlePublish, handleDisable, handleDelete, handleBatchPublish, handleBatchDisable } = useStatusActions({
  apiPublish: publishTask, apiDisable: disableTask, apiDelete: deleteTask,
  batchPublish: batchPublishTask, batchDisable: batchDisableTask,
  fetchData, getSelectedIds: () => selectedIds.value, getSelectedStatuses: () => [],
})

function handleSelectionChange(selection: any[]) { selectedIds.value = selection.map((s: any) => s.id) }
fetchData()
</script>

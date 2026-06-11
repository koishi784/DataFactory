<template>
  <PageContainer title="数据标准目录">
    <template #actions>
      <el-button type="primary" @click="router.push('/standard/create')">新增数据标准</el-button>
    </template>
    <SearchForm :model="searchForm" :fields="searchFields" @search="handleSearch" @reset="handleReset" />
    <BatchActions :has-selection="selectedIds.length > 0" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable" />
    <DataTable
      :data="list" :loading="loading" :total="total"
      :current-page="pagination.pageNum" :page-size="pagination.pageSize"
      selectable show-pagination
      @selection-change="handleSelectionChange"
      @page-change="handlePageChange" @size-change="handleSizeChange"
    >
      <el-table-column prop="standardCode" label="编号" width="130" />
      <el-table-column prop="name" label="中文名" show-overflow-tooltip />
      <el-table-column prop="englishName" label="英文名" show-overflow-tooltip />
      <el-table-column prop="sourceOrganization" label="来源" width="100" show-overflow-tooltip />
      <el-table-column prop="dataType" label="数据类型" width="90" />
      <el-table-column prop="length" label="长度" width="80" />
      <el-table-column prop="precision" label="数据精度" width="70" />
      <el-table-column prop="defaultValue" label="默认值" width="80" />
      <el-table-column label="取值范围" width="130" show-overflow-tooltip>
        <template #default="{ row }">{{ [row.rangeMin, row.rangeMax].filter(Boolean).join(' ~ ') || '-' }}</template>
      </el-table-column>
      <el-table-column prop="enumRange" label="枚举范围" width="100" show-overflow-tooltip />
      <el-table-column label="是否可为空" width="75">
        <template #default="{ row }">{{ row.nullable === 1 ? '否' : '是' }}</template>
      </el-table-column>
      <el-table-column label="标准状态" width="80">
        <template #default="{ row }"><StatusTag :status="row.status" /></template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新日期" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <StatusAction :status="row.status" @edit="router.push(`/standard/${row.id}/edit`)" @publish="handlePublish(row.id, row.name)" @disable="handleDisable(row.id, row.name)" @delete="handleDelete(row.id, row.name)" />
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
  { prop: 'keyword', label: '标准名称 / 英文名 / 编码' },
  { prop: 'status', label: '状态', type: 'select', options: [
    { label: '未发布', value: 0 },
    { label: '已发布', value: 1 },
    { label: '已停用', value: 2 },
  ]},
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

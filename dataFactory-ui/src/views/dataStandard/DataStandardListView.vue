<template>
  <PageContainer title="数据标准目录">
    <SearchForm :model="searchForm" :fields="searchFields" @search="handleSearch" @reset="handleReset" />
    <div class="toolbar">
      <BatchActions :has-selection="selectedIds.length > 0" :show-category="false" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable">
        <template #extra>
          <el-button type="primary" @click="router.push('/standard/create')">新增数据标准</el-button>
        </template>
        <template #append>
          <el-button type="primary" @click="handleDownloadTemplate">下载模板</el-button>
          <el-button type="primary" @click="triggerImport">导入模板</el-button>
        </template>
      </BatchActions>
      <input ref="fileInputRef" type="file" accept=".xlsx" style="display:none" @change="handleFileChange" />
    </div>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'
import DataTable from '@/components/DataTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import StatusAction from '@/components/StatusAction.vue'
import BatchActions from '@/components/BatchActions.vue'
import { useCrud } from '@/composables/useCrud'
import { useStatusActions } from '@/composables/useStatusActions'
import { getDataStandardList, publishDataStandard, disableDataStandard, deleteDataStandard, batchPublishDataStandard, batchDisableDataStandard, downloadTemplate, importDataStandard } from '@/api/dataStandard'
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
  { prop: 'dataType', label: '数据类型', type: 'select', options: [
    { label: '字符串(String)', value: 'String' },
    { label: '整型(Int)', value: 'Int' },
    { label: '浮点型(Float)', value: 'Float' },
    { label: '枚举(Enum)', value: 'Enum' },
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

// 导入 / 下载模板
const fileInputRef = ref<HTMLInputElement | null>(null)

function triggerImport() {
  fileInputRef.value?.click()
}

async function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  try {
    const res = await importDataStandard(file) as any
    const { totalCount, successCount, failCount, failDetails } = res
    ElMessage.success(`导入完成：共 ${totalCount} 条，成功 ${successCount} 条，失败 ${failCount} 条`)
    if (failDetails?.length > 0) {
      ElMessageBox.alert(
        failDetails.map((d: any) => `第 ${d.rowIndex} 行：${d.reason}`).join('\n'),
        '导入失败详情',
        { type: 'warning', confirmButtonText: '确定' }
      )
    }
    fetchData()
  } catch { /* handled */ }
  target.value = ''
}

async function handleDownloadTemplate() {
  try {
    const blob = await downloadTemplate() as Blob
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '数据标准导入模板.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
  } catch { /* handled */ }
}

fetchData()
</script>

<style scoped lang="scss">
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
}
</style>

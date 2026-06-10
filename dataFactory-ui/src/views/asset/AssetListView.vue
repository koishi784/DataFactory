<template>
  <PageContainer title="数据资产管理">
    <el-row :gutter="16">
      <el-col :span="6">
        <CategoryTree :data="[]" title="资产目录" @node-click="handleNodeClick" />
      </el-col>
      <el-col :span="18">
        <SearchForm :model="searchForm" :fields="searchFields" @search="handleSearch" @reset="handleReset" />
        <BatchActions :has-selection="selectedIds.length > 0" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable" />
        <DataTable
          :data="list" :loading="loading" :total="total"
          :current-page="pagination.pageNum" :page-size="pagination.pageSize"
          selectable show-pagination
          @selection-change="handleSelectionChange"
          @page-change="handlePageChange" @size-change="handleSizeChange"
        >
          <el-table-column prop="assetName" label="资产名称" show-overflow-tooltip />
          <el-table-column prop="englishName" label="英文名称" width="160" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }"><StatusTag :status="row.status" /></template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" width="170" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <StatusAction :status="row.status" @edit="router.push(`/asset/${row.id}/edit`)" @publish="handlePublish(row.id, row.assetName)" @disable="handleDisable(row.id, row.assetName)" @delete="handleDelete(row.id, row.assetName)" />
            </template>
          </el-table-column>
        </DataTable>
      </el-col>
    </el-row>
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
import CategoryTree from '@/components/CategoryTree.vue'
import { useCrud } from '@/composables/useCrud'
import { useStatusActions } from '@/composables/useStatusActions'
import { getAssetList, publishAsset, disableAsset, deleteAsset, batchPublishAsset, batchDisableAsset } from '@/api/asset'
import type { Asset } from '@/types/asset'

const router = useRouter()
const selectedIds = ref<number[]>([])

const searchFields = [
  { prop: 'assetName', label: '资产名称' },
]

const { list, loading, total, pagination, searchForm, handleSearch, handleReset, handlePageChange, handleSizeChange, fetchData } = useCrud<Asset>({ apiGetList: getAssetList })
const { handlePublish, handleDisable, handleDelete, handleBatchPublish, handleBatchDisable } = useStatusActions({
  apiPublish: publishAsset, apiDisable: disableAsset, apiDelete: deleteAsset,
  batchPublish: batchPublishAsset, batchDisable: batchDisableAsset,
  fetchData, getSelectedIds: () => selectedIds.value, getSelectedStatuses: () => [],
})

function handleNodeClick(node: any) { console.log('选中目录:', node) }
function handleSelectionChange(selection: any[]) { selectedIds.value = selection.map((s: any) => s.id) }

fetchData()
</script>

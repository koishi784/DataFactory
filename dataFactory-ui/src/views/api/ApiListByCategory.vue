<template>
  <div class="api-list-by-category">
    <div class="header">
      <h3 class="title">{{ categoryName }} — 接口列表</h3>
      <el-button type="primary" size="small" @click="router.push('/api/create')">
        新增接口
      </el-button>
    </div>
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

    <!-- 批量移动分类弹窗 -->
    <el-dialog v-model="categoryDialogVisible" title="批量修改分类" width="380px">
      <el-tree-select
        v-model="moveCategoryId"
        :data="allCategories"
        :props="{ label: 'name', children: 'children', value: 'id' }"
        placeholder="请选择目标分类"
        style="width: 100%"
      />
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchCategory">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import DataTable from '@/components/DataTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import StatusAction from '@/components/StatusAction.vue'
import { useCrud } from '@/composables/useCrud'
import { useStatusActions } from '@/composables/useStatusActions'
import { getApiList, publishApi, disableApi, deleteApi, batchPublishApi, batchDisableApi, batchCategoryApi, getApiCategoryTree } from '@/api/api'
import type { ApiInfo, ApiCategory } from '@/types/api'

const props = defineProps<{
  categoryId: number
  categoryName: string
}>()

const router = useRouter()
const selectedIds = ref<number[]>([])

const { list, loading, total, pagination, searchForm, handleSearch, handleReset, handlePageChange, handleSizeChange, fetchData } = useCrud<ApiInfo>({
  apiGetList: (params) => getApiList({ ...params, categoryId: props.categoryId }),
})
const { handlePublish, handleDisable, handleDelete, handleBatchPublish, handleBatchDisable } = useStatusActions({
  apiPublish: publishApi, apiDisable: disableApi, apiDelete: deleteApi,
  batchPublish: batchPublishApi, batchDisable: batchDisableApi,
  fetchData, getSelectedIds: () => selectedIds.value, getSelectedStatuses: () => [],
})

// 批量移动分类
const categoryDialogVisible = ref(false)
const moveCategoryId = ref<number | null>(null)
const allCategories = ref<ApiCategory[]>([])

function handleSelectionChange(selection: any[]) {
  selectedIds.value = selection.map((s: any) => s.id)
}

async function handleBatchCategory() {
  if (!moveCategoryId.value) {
    ElMessage.warning('请选择目标分类')
    return
  }
  try {
    await batchCategoryApi(selectedIds.value, moveCategoryId.value)
    ElMessage.success('批量分类成功')
    categoryDialogVisible.value = false
    fetchData()
  } catch {
    // handled by interceptor
  }
}

async function fetchAllCategories() {
  try {
    allCategories.value = await getApiCategoryTree() as any
  } catch {
    allCategories.value = []
  }
}

watch(() => props.categoryId, () => {
  fetchData()
})

onMounted(() => {
  fetchAllCategories()
})
</script>

<style scoped lang="scss">
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  .title {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
  }
}
</style>

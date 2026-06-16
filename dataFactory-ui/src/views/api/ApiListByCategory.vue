<template>
  <div class="api-list-by-category">
    <div class="header">
      <h3 class="title">{{ categoryName ? categoryName + ' — 接口列表' : '全部接口' }}</h3>
    </div>
    <BatchActions :has-selection="selectedIds.length > 0" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable" @batch-category="openBatchCategoryDialog">
      <template #extra>
        <el-button type="primary" @click="router.push('/api/create')">新增接口</el-button>
      </template>
    </BatchActions>
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
          <el-button link size="small" @click="openTestDialog(row)">测试</el-button>
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

    <!-- 接口测试弹框 -->
    <el-dialog v-model="testDialogVisible" :title="`接口测试 — ${testingApi?.apiName || ''}`" width="720px" top="5vh" @close="resetTestDialog">
      <div v-if="testLoading" style="text-align: center; padding: 40px">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p style="margin-top: 12px">正在测试...</p>
      </div>
      <template v-else>
        <el-descriptions :column="2" size="small" border style="margin-bottom: 16px">
          <el-descriptions-item label="请求方法">{{ testingApi?.method }}</el-descriptions-item>
          <el-descriptions-item label="接口地址"><code>{{ testingApi?.url }}</code></el-descriptions-item>
          <el-descriptions-item label="接口来源">{{ testingApi?.source }}</el-descriptions-item>
          <el-descriptions-item label="超时时间">{{ testingApi?.timeout }}ms</el-descriptions-item>
        </el-descriptions>

        <template v-if="testResult">
          <el-divider content-position="left">测试结果</el-divider>
          <el-descriptions :column="2" size="small" border style="margin-bottom: 12px">
            <el-descriptions-item label="调用结果">
              <el-tag :type="testResult.success ? 'success' : 'danger'" size="small">
                {{ testResult.success ? '成功' : '失败' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="响应状态码">{{ testResult.statusCode ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="响应耗时">{{ testResult.responseTime ? `${testResult.responseTime}ms` : '-' }}</el-descriptions-item>
          </el-descriptions>
          <div v-if="testResult.errorMessage" style="margin-bottom: 12px">
            <p style="font-weight: 600; color: var(--el-color-danger); margin-bottom: 4px">错误信息：</p>
            <pre style="background: #fdf0ef; padding: 8px; border-radius: 4px; white-space: pre-wrap; word-break: break-all;">{{ testResult.errorMessage }}</pre>
          </div>
          <div v-if="testResult.responseBody">
            <p style="font-weight: 600; margin-bottom: 4px">响应 Body：</p>
            <pre style="background: #f5f7fa; padding: 8px; border-radius: 4px; white-space: pre-wrap; word-break: break-all; max-height: 300px; overflow-y: auto;">{{ formatJson(testResult.responseBody) }}</pre>
          </div>
        </template>

        <div v-if="!testResult && !testLoading" style="text-align: center; color: var(--el-text-color-secondary); padding: 40px 0">
          点击下方"执行测试"按钮开始测试
        </div>
      </template>
      <template #footer>
        <el-button @click="testDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="testLoading" @click="execTest">执行测试</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import DataTable from '@/components/DataTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import StatusAction from '@/components/StatusAction.vue'
import BatchActions from '@/components/BatchActions.vue'
import { useCrud } from '@/composables/useCrud'
import { useStatusActions } from '@/composables/useStatusActions'
import { getApiList, publishApi, disableApi, deleteApi, batchPublishApi, batchDisableApi, batchCategoryApi, getApiCategoryTree, testApi } from '@/api/api'
import type { ApiInfo, ApiCategory } from '@/types/api'

const props = defineProps<{
  categoryId?: number
  categoryName?: string
}>()

const router = useRouter()
const selectedIds = ref<number[]>([])
const selectedRows = ref<any[]>([])

const { list, loading, total, pagination, searchForm, handleSearch, handleReset, handlePageChange, handleSizeChange, fetchData } = useCrud<ApiInfo>({
  apiGetList: (params) => getApiList({ ...params, categoryId: props.categoryId }),
})
const { handlePublish, handleDisable, handleDelete, handleBatchPublish, handleBatchDisable } = useStatusActions({
  apiPublish: publishApi, apiDisable: disableApi, apiDelete: deleteApi,
  batchPublish: batchPublishApi, batchDisable: batchDisableApi,
  fetchData, getSelectedIds: () => selectedIds.value, getSelectedStatuses: () => selectedRows.value.map(r => r.status),
})

// 批量移动分类
const categoryDialogVisible = ref(false)
const moveCategoryId = ref<number | null>(null)
const allCategories = ref<ApiCategory[]>([])

function handleSelectionChange(selection: any[]) {
  selectedIds.value = selection.map((s: any) => s.id)
  selectedRows.value = selection
}

// 接口测试弹框
const testDialogVisible = ref(false)
const testLoading = ref(false)
const testingApi = ref<ApiInfo | null>(null)
const testResult = ref<{
  success: boolean; statusCode?: number; responseTime?: number;
  responseBody?: string; responseHeaders?: Record<string, string>; errorMessage?: string
} | null>(null)

function openTestDialog(row: ApiInfo) {
  testingApi.value = row
  testResult.value = null
  testDialogVisible.value = true
}

function resetTestDialog() {
  testLoading.value = false
  testingApi.value = null
  testResult.value = null
}

function formatJson(str: string) {
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

async function execTest() {
  if (!testingApi.value) return
  testLoading.value = true
  testResult.value = null
  try {
    const res = await testApi(testingApi.value.id, {})
    testResult.value = res as any
  } catch {
    testResult.value = { success: false, errorMessage: '测试调用失败' }
  } finally {
    testLoading.value = false
  }
}

function openBatchCategoryDialog() {
  moveCategoryId.value = null
  categoryDialogVisible.value = true
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
}, { immediate: true })

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

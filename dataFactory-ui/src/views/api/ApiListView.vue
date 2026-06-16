<template>
  <PageContainer title="接口列表">
    <el-form :model="searchForm" inline class="search-bar" @keyup.enter="handleSearch">
      <el-form-item label="接口名称">
        <el-input v-model="searchForm.keyword" placeholder="请输入接口名称" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="请求方法">
        <el-select v-model="searchForm.method" placeholder="请求方法" clearable style="width: 110px">
          <el-option label="GET" value="GET" />
          <el-option label="POST" value="POST" />
          <el-option label="PUT" value="PUT" />
          <el-option label="DELETE" value="DELETE" />
        </el-select>
      </el-form-item>
      <el-form-item label="接口来源">
        <el-input v-model="searchForm.source" placeholder="请输入" clearable style="width: 140px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 110px">
          <el-option label="未发布" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="已停用" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="接口分类">
        <el-tree-select
          v-model="searchForm.categoryId"
          :data="categories"
          :props="{ label: 'name', children: 'children', value: 'id' }"
          placeholder="请选择分类"
          clearable
          style="width: 180px"
          @change="handleSearch"
        />
      </el-form-item>
      <el-form-item label="排序">
        <el-select v-model="searchForm.sortOrder" placeholder="排序方式" clearable style="width: 110px" @change="handleSearch">
          <el-option label="升序" value="asc" />
          <el-option label="降序" value="desc" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    <div class="toolbar">
      <BatchActions :has-selection="selectedIds.length > 0" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable" @batch-category="openBatchCategoryDialog">
        <template #extra>
          <el-button type="primary" @click="router.push('/api/create')">新增接口</el-button>
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
      <el-table-column prop="apiName" label="接口名称" show-overflow-tooltip min-width="140" />
      <el-table-column prop="apiDescription" label="接口描述" show-overflow-tooltip min-width="160" />
      <el-table-column prop="apiCategory" label="接口分类" show-overflow-tooltip width="130" />
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

    <!-- 批量分类弹框 -->
    <el-dialog v-model="batchCategoryVisible" title="批量分类" width="420px" top="30vh">
      <el-form label-width="80px">
        <el-form-item label="目标分类">
          <el-tree-select
            v-model="batchCategoryId"
            :data="categories"
            :props="{ label: 'name', children: 'children', value: 'id' }"
            placeholder="请选择分类"
            clearable
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchCategoryVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchCategoryLoading" @click="handleBatchCategory">确定</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer.vue'
import DataTable from '@/components/DataTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import StatusAction from '@/components/StatusAction.vue'
import BatchActions from '@/components/BatchActions.vue'
import { useCrud } from '@/composables/useCrud'
import { useStatusActions } from '@/composables/useStatusActions'
import { getApiList, publishApi, disableApi, deleteApi, batchPublishApi, batchDisableApi, batchCategoryApi, testApi, getApiCategoryTree } from '@/api/api'
import type { ApiInfo, ApiCategory } from '@/types/api'

const router = useRouter()
const selectedIds = ref<number[]>([])

const categories = ref<ApiCategory[]>([])

async function fetchCategories() {
  try {
    categories.value = (await getApiCategoryTree()) as any
  } catch {
    categories.value = []
  }
}

const { list, loading, total, pagination, searchForm, handleSearch, handleReset, handlePageChange, handleSizeChange, fetchData } = useCrud<ApiInfo>({
  apiGetList: getApiList,
  defaultForm: { categoryId: null as number | null, sortOrder: '' },
})
const { handlePublish, handleDisable, handleDelete, handleBatchPublish, handleBatchDisable } = useStatusActions({
  apiPublish: publishApi, apiDisable: disableApi, apiDelete: deleteApi,
  batchPublish: batchPublishApi, batchDisable: batchDisableApi,
  fetchData, getSelectedIds: () => selectedIds.value, getSelectedStatuses: () => [],
})

function handleSelectionChange(selection: any[]) {
  selectedIds.value = selection.map((s: any) => s.id)
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

// 批量分类
const batchCategoryVisible = ref(false)
const batchCategoryId = ref<number | null>(null)
const batchCategoryLoading = ref(false)

function openBatchCategoryDialog() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择需要分类的接口')
    return
  }
  batchCategoryId.value = null
  batchCategoryVisible.value = true
}

async function handleBatchCategory() {
  if (!batchCategoryId.value) {
    ElMessage.warning('请选择目标分类')
    return
  }
  batchCategoryLoading.value = true
  try {
    await batchCategoryApi(selectedIds.value, batchCategoryId.value)
    ElMessage.success('批量分类成功')
    batchCategoryVisible.value = false
    fetchData()
  } catch {
    // 错误由全局拦截处理
  } finally {
    batchCategoryLoading.value = false
  }
}

fetchData()
onMounted(() => { fetchCategories() })
</script>

<style scoped lang="scss">
.search-bar {
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}
.toolbar {
  display: flex;
  align-items: center;
  padding-bottom: 12px;
}
</style>

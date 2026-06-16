<template>
  <div class="script-list-by-category">
    <div class="header">
      <h3 class="title">{{ categoryName ? categoryName + ' — 脚本列表' : '全部脚本' }}</h3>
    </div>
    <BatchActions :has-selection="selectedIds.length > 0" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable" @batch-category="openBatchCategoryDialog">
      <template #extra>
        <el-button type="primary" @click="router.push('/script/create')">新增脚本</el-button>
      </template>
    </BatchActions>
    <DataTable
      :data="list" :loading="loading" :total="total"
      :current-page="pagination.pageNum" :page-size="pagination.pageSize"
      selectable show-pagination
      @selection-change="handleSelectionChange"
      @page-change="handlePageChange" @size-change="handleSizeChange"
    >
      <el-table-column prop="scriptName" label="脚本名称" show-overflow-tooltip min-width="140" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip min-width="160" />
      <el-table-column prop="scriptType" label="脚本类型" width="100" />
      <el-table-column prop="fileName" label="文件名" show-overflow-tooltip width="140" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }"><StatusTag :status="row.status" /></template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link size="small" @click="openDebugDialog(row)">调试</el-button>
          <StatusAction :status="row.status" @edit="router.push(`/script/${row.id}/edit`)" @publish="handlePublish(row.id, row.scriptName)" @disable="handleDisable(row.id, row.scriptName)" @delete="handleDelete(row.id, row.scriptName)" />
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

    <!-- 调试弹框 -->
    <el-dialog v-model="debugDialogVisible" :title="`调试 — ${debuggingScript?.scriptName || ''}`" width="620px" top="20vh">
      <div v-if="debugLoading" style="text-align: center; padding: 40px">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p style="margin-top: 12px">正在执行调试...</p>
      </div>
      <template v-else-if="debugResult">
        <el-descriptions :column="1" size="small" border>
          <el-descriptions-item label="执行结果">
            <el-tag :type="debugResult.success ? 'success' : 'danger'" size="small">
              {{ debugResult.success ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="执行耗时">{{ debugResult.executeTime ? `${debugResult.executeTime}ms` : '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="debugResult.result" label="输出结果">
            <pre style="background: #f5f7fa; padding: 8px; border-radius: 4px; white-space: pre-wrap; max-height: 200px; overflow-y: auto;">{{ debugResult.result }}</pre>
          </el-descriptions-item>
          <el-descriptions-item v-if="debugResult.errorMessage" label="错误信息">
            <pre style="color: var(--el-color-danger); white-space: pre-wrap;">{{ debugResult.errorMessage }}</pre>
          </el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="debugDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="debugLoading" @click="execDebug">执行调试</el-button>
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
import { getScriptList, publishScript, disableScript, deleteScript, batchPublishScript, batchDisableScript, batchCategoryScript, debugScript, getScriptCategoryTree } from '@/api/script'
import type { Script, ScriptCategory } from '@/types/script'

const props = defineProps<{
  categoryId?: number
  categoryName?: string
}>()

const router = useRouter()
const selectedIds = ref<number[]>([])
const selectedRows = ref<any[]>([])

const { list, loading, total, pagination, searchForm, handleSearch, handleReset, handlePageChange, handleSizeChange, fetchData } = useCrud<Script>({
  apiGetList: (params) => getScriptList({ ...params, categoryId: props.categoryId }),
})
const { handlePublish, handleDisable, handleDelete, handleBatchPublish, handleBatchDisable } = useStatusActions({
  apiPublish: publishScript, apiDisable: disableScript, apiDelete: deleteScript,
  batchPublish: batchPublishScript, batchDisable: batchDisableScript,
  fetchData, getSelectedIds: () => selectedIds.value, getSelectedStatuses: () => selectedRows.value.map(r => r.status),
})

// 批量移动分类
const categoryDialogVisible = ref(false)
const moveCategoryId = ref<number | null>(null)
const allCategories = ref<ScriptCategory[]>([])

function handleSelectionChange(selection: any[]) {
  selectedIds.value = selection.map((s: any) => s.id)
  selectedRows.value = selection
}

async function handleBatchCategory() {
  if (!moveCategoryId.value) {
    ElMessage.warning('请选择目标分类')
    return
  }
  try {
    await batchCategoryScript(selectedIds.value, moveCategoryId.value)
    ElMessage.success('批量分类成功')
    categoryDialogVisible.value = false
    fetchData()
  } catch {
    // handled by interceptor
  }
}

function openBatchCategoryDialog() {
  moveCategoryId.value = null
  categoryDialogVisible.value = true
}

async function fetchAllCategories() {
  try {
    allCategories.value = await getScriptCategoryTree() as any
  } catch {
    allCategories.value = []
  }
}

// 调试弹框
const debugDialogVisible = ref(false)
const debugLoading = ref(false)
const debuggingScript = ref<Script | null>(null)
const debugResult = ref<{ success: boolean; executeTime?: number; result?: string; errorMessage?: string } | null>(null)

function openDebugDialog(row: Script) {
  debuggingScript.value = row
  debugResult.value = null
  debugDialogVisible.value = true
  execDebug()
}

async function execDebug() {
  if (!debuggingScript.value) return
  debugLoading.value = true
  debugResult.value = null
  try {
    const res = await debugScript(debuggingScript.value.id, {}) as any
    debugResult.value = res
  } catch {
    debugResult.value = { success: false, errorMessage: '调试调用失败' }
  } finally {
    debugLoading.value = false
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

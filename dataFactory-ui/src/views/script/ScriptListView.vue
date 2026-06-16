<template>
  <PageContainer title="脚本列表">
    <div class="toolbar">
      <BatchActions :has-selection="selectedIds.length > 0" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable" @batch-category="openBatchCategoryDialog">
        <template #extra>
          <el-button type="primary" @click="router.push('/script/create')">新增脚本</el-button>
        </template>
      </BatchActions>
    </div>
    <el-form :model="searchForm" inline class="search-bar" @keyup.enter="handleSearch">
      <el-form-item label="关键字">
        <el-input v-model="searchForm.keyword" placeholder="脚本名称/说明" clearable style="width: 160px" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 110px">
          <el-option label="未发布" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="已停用" :value="2" />
        </el-select>
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
      <el-table-column prop="categoryName" label="脚本分类" show-overflow-tooltip width="130" />
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
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
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
import { getScriptList, publishScript, disableScript, deleteScript, batchPublishScript, batchDisableScript, batchCategoryScript, debugScript, getScriptCategoryTree } from '@/api/script'
import type { Script, ScriptCategory } from '@/types/script'

const router = useRouter()
const selectedIds = ref<number[]>([])

const categories = ref<ScriptCategory[]>([])

async function fetchCategories() {
  try {
    categories.value = await getScriptCategoryTree() as any
  } catch {
    categories.value = []
  }
}

const { list, loading, total, pagination, searchForm, handleSearch, handleReset, handlePageChange, handleSizeChange, fetchData } = useCrud<Script>({
  apiGetList: getScriptList,
  defaultForm: { keyword: '', status: null as number | null, sortOrder: '' },
})
const { handlePublish, handleDisable, handleDelete, handleBatchPublish, handleBatchDisable } = useStatusActions({
  apiPublish: publishScript, apiDisable: disableScript, apiDelete: deleteScript,
  batchPublish: batchPublishScript, batchDisable: batchDisableScript,
  fetchData, getSelectedIds: () => selectedIds.value, getSelectedStatuses: () => [],
})

function handleSelectionChange(selection: any[]) {
  selectedIds.value = selection.map((s: any) => s.id)
}

// 批量分类
const batchCategoryVisible = ref(false)
const batchCategoryId = ref<number | null>(null)
const batchCategoryLoading = ref(false)

function openBatchCategoryDialog() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择需要分类的脚本')
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
    await batchCategoryScript(selectedIds.value, batchCategoryId.value)
    ElMessage.success('批量分类成功')
    batchCategoryVisible.value = false
    fetchData()
  } catch {
    // 错误由全局拦截处理
  } finally {
    batchCategoryLoading.value = false
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

fetchData()
onMounted(() => { fetchCategories() })
</script>

<style scoped lang="scss">
.toolbar {
  display: flex;
  align-items: center;
  padding-bottom: 12px;
}
.search-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 12px;
}
</style>

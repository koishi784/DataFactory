<template>
  <PageContainer title="任务管理">
    <el-row :gutter="16">
      <el-col :span="5">
        <CategoryTree
          :data="treeData"
          title="任务分类"
          show-add
          @node-click="handleNodeClick"
          @add="handleAddCategory"
          @edit="handleEditCategory"
          @delete="handleDeleteCategory"
        />
      </el-col>
      <el-col :span="19">
        <el-form :model="searchForm" inline class="search-bar" @keyup.enter="handleSearch">
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" placeholder="任务名称/说明" clearable style="width: 160px" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 110px">
              <el-option label="未发布" :value="0" />
              <el-option label="已发布" :value="1" />
              <el-option label="已停用" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="调度类型">
            <el-select v-model="searchForm.scheduleType" placeholder="调度类型" clearable style="width: 120px">
              <el-option label="手动" value="MANUAL" />
              <el-option label="CRON" value="CRON" />
              <el-option label="API" value="API" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="toolbar">
          <BatchActions :has-selection="selectedIds.length > 0" :show-category="false" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable">
            <template #extra>
              <el-button type="primary" @click="router.push('/task/create')">新增任务</el-button>
            </template>
          </BatchActions>
          <div v-if="selectedNode" style="font-size: 13px; color: var(--el-text-color-secondary); margin-left: auto">
            当前分类：<strong>{{ selectedNode.name }}</strong>
          </div>
        </div>
        <DataTable
          :data="list" :loading="loading" :total="total"
          :current-page="pagination.pageNum" :page-size="pagination.pageSize"
          selectable show-pagination
          @selection-change="handleSelectionChange"
          @page-change="handlePageChange" @size-change="handleSizeChange"
        >
          <el-table-column prop="taskName" label="任务名称" show-overflow-tooltip min-width="140" />
          <el-table-column prop="scheduleType" label="调度类型" width="100" />
          <el-table-column prop="executeStatus" label="最近执行状态" width="110">
            <template #default="{ row }">
              <el-tag v-if="row.executeStatus === 0" type="info" size="small">等待</el-tag>
              <el-tag v-else-if="row.executeStatus === 1" type="warning" size="small">执行中</el-tag>
              <el-tag v-else-if="row.executeStatus === 2" type="success" size="small">成功</el-tag>
              <el-tag v-else-if="row.executeStatus === 3" type="danger" size="small">失败</el-tag>
              <el-tag v-else-if="row.executeStatus === 4" type="info" size="small">已取消</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="lastExecuteTime" label="最近执行时间" width="170" />
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }"><StatusTag :status="row.status" /></template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" width="170" />
          <el-table-column label="操作" width="320" fixed="right">
            <template #default="{ row }">
              <el-button link size="small" @click="router.push(`/task/${row.id}/dag`)">DAG编排</el-button>
              <el-button link size="small" @click="router.push(`/task/${row.id}/executions`)">执行历史</el-button>
              <StatusAction :status="row.status" @edit="router.push(`/task/${row.id}/edit`)" @publish="handlePublish(row.id, row.taskName)" @disable="handleDisable(row.id, row.taskName)" @delete="handleDelete(row.id, row.taskName)" />
            </template>
          </el-table-column>
        </DataTable>
      </el-col>
    </el-row>

    <!-- 分类新增/编辑弹框 -->
    <el-dialog v-model="categoryDialogVisible" :title="categoryIsEdit ? '编辑分类' : '新增分类'" width="400px">
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="分类名称" required>
          <el-input v-model="categoryForm.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="categoryForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="categorySubmitting" @click="handleCategorySubmit">确定</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import CategoryTree from '@/components/CategoryTree.vue'
import DataTable from '@/components/DataTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import StatusAction from '@/components/StatusAction.vue'
import BatchActions from '@/components/BatchActions.vue'
import { useCrud } from '@/composables/useCrud'
import { useStatusActions } from '@/composables/useStatusActions'
import {
  getTaskList, publishTask, disableTask, deleteTask,
  batchPublishTask, batchDisableTask,
  getTaskCategoryTree, createTaskCategory, updateTaskCategory, deleteTaskCategory,
} from '@/api/task'
import type { Task, TaskCategory } from '@/types/task'

const router = useRouter()
const selectedIds = ref<number[]>([])

// ===== 分类树 =====
const treeData = ref<TaskCategory[]>([])
const selectedNode = ref<TaskCategory | null>(null)

async function fetchTree() {
  try { treeData.value = await getTaskCategoryTree() as any } catch { treeData.value = [] }
}

function handleNodeClick(node: TaskCategory) {
  selectedNode.value = node
  pagination.pageNum = 1
  fetchData()
}

// 分类 CRUD
const categoryDialogVisible = ref(false)
const categoryIsEdit = ref(false)
const categoryEditId = ref<number | null>(null)
const categorySubmitting = ref(false)
const categoryForm = ref({ name: '', sortOrder: 0 })

function handleAddCategory() {
  categoryIsEdit.value = false
  categoryEditId.value = null
  categoryForm.value = { name: '', sortOrder: 0 }
  categoryDialogVisible.value = true
}

function handleEditCategory(data: TaskCategory) {
  categoryIsEdit.value = true
  categoryEditId.value = data.id
  categoryForm.value = { name: data.name, sortOrder: data.sortOrder ?? 0 }
  categoryDialogVisible.value = true
}

async function handleDeleteCategory(data: TaskCategory) {
  try {
    await ElMessageBox.confirm(`确定删除分类"${data.name}"吗？仅能删除无子分类且无关联任务的分类。`, '删除确认', { type: 'warning' })
    await deleteTaskCategory(data.id)
    ElMessage.success('删除成功')
    if (selectedNode.value?.id === data.id) selectedNode.value = null
    await fetchTree()
  } catch { /* cancel */ }
}

async function handleCategorySubmit() {
  if (!categoryForm.value.name.trim()) { ElMessage.warning('请输入分类名称'); return }
  categorySubmitting.value = true
  try {
    if (categoryIsEdit.value && categoryEditId.value) {
      await updateTaskCategory(categoryEditId.value, categoryForm.value)
    } else {
      await createTaskCategory({ ...categoryForm.value, parentId: selectedNode.value?.id ?? 0 })
    }
    ElMessage.success(categoryIsEdit.value ? '编辑成功' : '新增成功')
    categoryDialogVisible.value = false
    await fetchTree()
  } catch { /* handled */ } finally { categorySubmitting.value = false }
}

// ===== 任务列表 =====
const { list, loading, total, pagination, searchForm, handleSearch, handleReset, handlePageChange, handleSizeChange, fetchData } = useCrud<Task>({
  apiGetList: (params) => {
    const p: any = { ...params }
    if (selectedNode.value) p.categoryId = selectedNode.value.id
    return getTaskList(p)
  },
  defaultForm: { keyword: '', status: null as number | null, scheduleType: '' },
})
const { handlePublish, handleDisable, handleDelete, handleBatchPublish, handleBatchDisable } = useStatusActions({
  apiPublish: publishTask, apiDisable: disableTask, apiDelete: deleteTask,
  batchPublish: batchPublishTask, batchDisable: batchDisableTask,
  fetchData, getSelectedIds: () => selectedIds.value, getSelectedStatuses: () => [],
})

function handleSelectionChange(selection: any[]) { selectedIds.value = selection.map((s: any) => s.id) }

onMounted(() => { fetchTree(); fetchData() })
</script>

<style scoped lang="scss">
.search-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 12px;
}
.toolbar {
  display: flex;
  align-items: center;
  padding-bottom: 12px;
}
</style>

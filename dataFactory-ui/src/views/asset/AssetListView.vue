<template>
  <PageContainer title="数据资产管理">
    <el-row :gutter="16">
      <el-col :span="6">
        <CategoryTree
          :data="treeData"
          title="资产目录"
          show-add
          @node-click="handleNodeClick"
          @add="handleAddCategory"
          @edit="handleEditCategory"
          @delete="handleDeleteCategory"
        />
      </el-col>
      <el-col :span="18">
        <div class="toolbar">
          <BatchActions :has-selection="selectedIds.length > 0" :show-category="false" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable">
            <template #extra>
              <el-button type="primary" @click="router.push('/asset/create')">新增资产</el-button>
            </template>
          </BatchActions>
        </div>
        <el-form :model="searchForm" inline class="search-bar" @keyup.enter="handleSearch">
          <el-form-item label="关键字">
            <el-input v-model="searchForm.keyword" placeholder="名称/英文名" clearable style="width: 160px" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 110px">
              <el-option label="未发布" :value="0" />
              <el-option label="已发布" :value="1" />
              <el-option label="已停用" :value="2" />
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
          <el-table-column prop="assetName" label="资产名称" show-overflow-tooltip min-width="130" />
          <el-table-column prop="englishName" label="英文名称" width="160" show-overflow-tooltip />
          <el-table-column prop="description" label="描述" show-overflow-tooltip min-width="150" />
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

    <!-- 分类新增/编辑弹框 -->
    <el-dialog v-model="categoryDialogVisible" :title="categoryIsEdit ? '编辑目录' : '新增目录'" width="400px">
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="目录名称" required>
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
import DataTable from '@/components/DataTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import StatusAction from '@/components/StatusAction.vue'
import BatchActions from '@/components/BatchActions.vue'
import CategoryTree from '@/components/CategoryTree.vue'
import { useCrud } from '@/composables/useCrud'
import { useStatusActions } from '@/composables/useStatusActions'
import { getAssetList, publishAsset, disableAsset, deleteAsset, batchPublishAsset, batchDisableAsset, getAssetDirectoryTree, createAssetDirectory, updateAssetDirectory, deleteAssetDirectory } from '@/api/asset'
import type { Asset, AssetDirectory } from '@/types/asset'

const router = useRouter()
const selectedIds = ref<number[]>([])

// ===== 分类树 =====
const treeData = ref<AssetDirectory[]>([])
const selectedNode = ref<AssetDirectory | null>(null)

async function fetchTree() {
  try { treeData.value = await getAssetDirectoryTree() as any } catch { treeData.value = [] }
}

function handleNodeClick(node: AssetDirectory) {
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

function handleEditCategory(data: AssetDirectory) {
  categoryIsEdit.value = true
  categoryEditId.value = data.id
  categoryForm.value = { name: data.name, sortOrder: data.sortOrder ?? 0 }
  categoryDialogVisible.value = true
}

async function handleDeleteCategory(data: AssetDirectory) {
  try {
    await ElMessageBox.confirm(`确定删除目录"${data.name}"吗？有子目录或关联资产的目录不可删除。`, '删除确认', { type: 'warning' })
    await deleteAssetDirectory(data.id)
    ElMessage.success('删除成功')
    if (selectedNode.value?.id === data.id) selectedNode.value = null
    await fetchTree()
  } catch { /* cancel */ }
}

async function handleCategorySubmit() {
  if (!categoryForm.value.name.trim()) { ElMessage.warning('请输入目录名称'); return }
  categorySubmitting.value = true
  try {
    if (categoryIsEdit.value && categoryEditId.value) {
      await updateAssetDirectory(categoryEditId.value, categoryForm.value)
    } else {
      await createAssetDirectory({ ...categoryForm.value, parentId: selectedNode.value?.id ?? 0 })
    }
    ElMessage.success(categoryIsEdit.value ? '编辑成功' : '新增成功')
    categoryDialogVisible.value = false
    await fetchTree()
  } catch { /* handled */ } finally { categorySubmitting.value = false }
}

// ===== 资产列表 =====
const { list, loading, total, pagination, searchForm, handleSearch, handleReset, handlePageChange, handleSizeChange, fetchData } = useCrud<Asset>({
  apiGetList: (params) => {
    const p: any = { ...params }
    if (selectedNode.value) p.directoryId = selectedNode.value.id
    return getAssetList(p)
  },
  defaultForm: { keyword: '', status: null as number | null },
})
const { handlePublish, handleDisable, handleDelete, handleBatchPublish, handleBatchDisable } = useStatusActions({
  apiPublish: publishAsset, apiDisable: disableAsset, apiDelete: deleteAsset,
  batchPublish: batchPublishAsset, batchDisable: batchDisableAsset,
  fetchData, getSelectedIds: () => selectedIds.value, getSelectedStatuses: () => [],
})

function handleSelectionChange(selection: any[]) { selectedIds.value = selection.map((s: any) => s.id) }

onMounted(() => { fetchTree(); fetchData() })
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

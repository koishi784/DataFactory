<template>
  <PageContainer title="脚本分类管理">
    <el-row :gutter="16">
      <el-col :span="8">
        <CategoryTree
          :data="treeData" title="脚本分类"
          show-add
          @node-click="handleNodeClick"
          @add="handleAdd"
          @edit="handleEdit"
          @delete="handleDelete"
        />
      </el-col>
      <el-col :span="16">
        <template v-if="selectedNode">
          <ScriptListByCategory :category-id="selectedNode.id" :category-name="selectedNode.name" />
        </template>
        <el-alert v-else title="请选择左侧分类进行管理" type="info" show-icon :closable="false" />
      </el-col>
    </el-row>

    <!-- 新增/编辑分类弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑分类' : '新增分类'"
      width="400px"
      @close="resetDialog"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称" required>
          <el-input v-model="form.name" maxlength="50" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import CategoryTree from '@/components/CategoryTree.vue'
import ScriptListByCategory from './ScriptListByCategory.vue'
import { getScriptCategoryTree, createScriptCategory, updateScriptCategory, deleteScriptCategory } from '@/api/script'
import type { ScriptCategory } from '@/types/script'

const treeData = ref<ScriptCategory[]>([])
const selectedNode = ref<ScriptCategory | null>(null)

// 弹窗状态
const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const submitting = ref(false)
const form = ref({ name: '', sortOrder: 0 })

async function fetchTree() {
  try {
    treeData.value = await getScriptCategoryTree() as any
  } catch {
    treeData.value = []
  }
}

function handleNodeClick(node: ScriptCategory) {
  selectedNode.value = node
}

function handleAdd() {
  isEditing.value = false
  editingId.value = null
  form.value = { name: '', sortOrder: 0 }
  dialogVisible.value = true
}

function handleEdit(data: ScriptCategory) {
  isEditing.value = true
  editingId.value = data.id
  form.value = { name: data.name, sortOrder: data.sortOrder ?? 0 }
  dialogVisible.value = true
}

async function handleDelete(data: ScriptCategory) {
  try {
    await ElMessageBox.confirm(
      `确定删除分类"${data.name}"吗？仅能删除无子分类且无关联脚本的分类。`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteScriptCategory(data.id)
    ElMessage.success('删除成功')
    if (selectedNode.value?.id === data.id) {
      selectedNode.value = null
    }
    await fetchTree()
  } catch {
    // 取消操作
  }
}

function resetDialog() {
  form.value = { name: '', sortOrder: 0 }
  editingId.value = null
  isEditing.value = false
}

async function handleSubmit() {
  if (!form.value.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  submitting.value = true
  try {
    if (isEditing.value && editingId.value) {
      await updateScriptCategory(editingId.value, { name: form.value.name, sortOrder: form.value.sortOrder })
      ElMessage.success('编辑成功')
    } else {
      await createScriptCategory({
        name: form.value.name,
        parentId: selectedNode.value?.id ?? 0,
        sortOrder: form.value.sortOrder,
      })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await fetchTree()
  } catch {
    // 错误已在拦截器中处理
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchTree()
})
</script>

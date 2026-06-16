<template>
  <PageContainer :title="isEdit ? '编辑任务' : '新增任务'">
    <!-- 步骤指示器 -->
    <el-steps :active="1" align-center style="margin-bottom: 32px">
      <el-step title="基本信息" description="任务名称与分类" />
      <el-step title="任务配置" description="DAG 流程编排" />
      <el-step title="触发设置" description="调度方式配置" />
    </el-steps>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="task-form">
      <!-- 基本信息卡片 -->
      <el-card shadow="never" class="form-card">
        <template #header>
          <div class="card-header"><el-icon><InfoFilled /></el-icon> 基本信息</div>
        </template>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="任务名称" prop="taskName">
              <el-input v-model="form.taskName" maxlength="50" placeholder="全局唯一，仅支持中文和英文大小写" clearable />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="任务分类" prop="categoryId">
          <el-tree-select
            v-model="form.categoryId"
            :data="categories"
            :props="{ label: 'name', children: 'children', value: 'id' }"
            placeholder="请选择任务分类"
            clearable
          />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="form.taskDescription" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="描述说明（选填）" />
        </el-form-item>
      </el-card>

      <!-- 提交按钮 -->
      <div class="form-actions">
        <el-button type="primary" size="large" @click="handleSave">
          <el-icon><Check /></el-icon> 保存
        </el-button>
        <el-button size="large" @click="router.back()">取消</el-button>
      </div>
    </el-form>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { InfoFilled, Check } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer.vue'
import { createTask, updateTask, getTaskDetail, getTaskCategoryTree } from '@/api/task'
import type { TaskCategory } from '@/types/task'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const formRef = ref()
const categories = ref<TaskCategory[]>([])

const form = reactive({
  taskName: '',
  taskDescription: '',
  categoryId: null as number | null,
})

const rules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { max: 50, message: '任务名称不能超过50个字符', trigger: 'blur' },
  ],
  categoryId: [{ required: true, message: '请选择任务分类', trigger: 'change' }],
}

onMounted(async () => {
  try {
    categories.value = (await getTaskCategoryTree()) as any
  } catch { categories.value = [] }
  if (isEdit) {
    const detail = await getTaskDetail(Number(route.params.id)) as any
    if (detail) {
      form.taskName = detail.taskName || ''
      form.taskDescription = detail.taskDescription || ''
      form.categoryId = detail.categoryId ?? null
    }
  }
})

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    let id: number
    if (isEdit) {
      await updateTask(Number(route.params.id), form)
      id = Number(route.params.id)
      ElMessage.success('更新成功')
    } else {
      const res = await createTask(form) as any
      id = res.id || res.taskId
      ElMessage.success('新增成功')
    }
    router.push(`/task/${id}/dag`)
  } catch { /* handled */ }
}
</script>

<style scoped lang="scss">
.task-form {
  max-width: 900px;
  margin: 0 auto;
}

.form-card {
  margin-bottom: 20px;
  border-radius: 8px;

  .card-header {
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 600;
    font-size: 15px;
  }
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 8px 0 24px;
}
</style>

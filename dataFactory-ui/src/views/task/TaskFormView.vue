<template>
  <PageContainer :title="isEdit ? '编辑任务' : '新增任务'">
    <el-form :model="form" label-width="120px" style="max-width: 600px">
      <el-form-item label="任务名称" required>
        <el-input v-model="form.taskName" maxlength="50" />
      </el-form-item>
      <el-form-item label="任务描述">
        <el-input v-model="form.description" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="调度类型">
        <el-select v-model="form.scheduleType" style="width: 100%">
          <el-option label="手动触发" value="MANUAL" />
          <el-option label="定时调度(CRON)" value="CRON" />
          <el-option label="API触发" value="API" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.scheduleType === 'CRON'" label="CRON表达式">
        <el-input v-model="form.cronExpression" placeholder="如：0 0 2 * * ?" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSave">保存</el-button>
        <el-button @click="router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { createTask, getTaskDetail } from '@/api/task'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const form = reactive({ taskName: '', description: '', scheduleType: 'MANUAL', cronExpression: '' })

onMounted(async () => {
  if (isEdit) {
    const detail = await getTaskDetail(Number(route.params.id)) as any
    if (detail) Object.assign(form, detail)
  }
})

async function handleSave() {
  try {
    if (isEdit) {
      const { updateTask } = await import('@/api/task')
      await updateTask(Number(route.params.id), form)
    } else {
      await createTask(form)
    }
    ElMessage.success(isEdit ? '更新成功' : '新增成功')
    router.push('/task/list')
  } catch { /* handled */ }
}
</script>

<template>
  <PageContainer title="触发设置">
    <el-steps :active="3" align-center style="margin-bottom: 32px">
      <el-step title="基本信息" description="任务名称与分类" />
      <el-step title="任务配置" description="DAG 流程编排" />
      <el-step title="触发设置" description="调度方式配置" />
    </el-steps>

    <el-form :model="form" label-width="140px" style="max-width: 520px">
      <el-form-item label="调度类型" required>
        <el-select v-model="form.scheduleType" style="width: 100%">
          <el-option label="手动触发（仅API触发）" value="API" />
          <el-option label="定时调度（CRON）" value="CRON" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="form.scheduleType === 'CRON'" label="CRON 表达式" required>
        <el-input v-model="form.cronExpression" placeholder="如：0 0 2 * * ?" />
        <div style="font-size: 12px; color: var(--el-text-color-secondary); margin-top: 4px">
          每天凌晨2:00：0 0 2 * * ? &nbsp;|&nbsp; 每30分钟：0 */30 * * * ? &nbsp;|&nbsp; 工作日8点：0 0 8 * * MON-FRI
        </div>
      </el-form-item>
      <el-form-item label="生效日期">
        <el-date-picker v-model="form.effectiveDate" type="datetime" placeholder="生效日期" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
      </el-form-item>
      <el-form-item label="失效日期">
        <el-date-picker v-model="form.expireDate" type="datetime" placeholder="失效日期" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSave">保存</el-button>
        <el-button @click="router.back()">返回</el-button>
      </el-form-item>
    </el-form>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { setTaskTriggerConfig, getTaskDetail } from '@/api/task'

const route = useRoute()
const router = useRouter()
const taskId = Number(route.params.id)

const form = reactive({
  scheduleType: 'CRON',
  cronExpression: '',
  effectiveDate: '',
  expireDate: '',
})

onMounted(async () => {
  try {
    const detail = await getTaskDetail(taskId) as any
    if (detail?.scheduleType) form.scheduleType = detail.scheduleType
    if (detail?.cronExpression) form.cronExpression = detail.cronExpression
  } catch { /* ignore */ }
})

async function handleSave() {
  if (form.scheduleType === 'CRON' && !form.cronExpression.trim()) {
    ElMessage.warning('定时调度必须填写 CRON 表达式')
    return
  }
  try {
    await setTaskTriggerConfig(taskId, {
      scheduleType: form.scheduleType,
      cronExpression: form.scheduleType === 'CRON' ? form.cronExpression : undefined,
    })
    ElMessage.success('触发设置保存成功')
    router.push('/task/list')
  } catch { /* handled */ }
}
</script>

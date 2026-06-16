<template>
  <PageContainer title="触发设置">
    <el-steps :active="3" align-center style="margin-bottom: 32px">
      <el-step title="基本信息" description="任务名称与分类" />
      <el-step title="任务配置" description="DAG 流程编排" />
      <el-step title="触发设置" description="调度方式配置" />
    </el-steps>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="trigger-form">
      <!-- 触发设置卡片 -->
      <el-card shadow="never" class="form-card">
        <template #header>
          <div class="card-header"><el-icon><Clock /></el-icon> 触发设置</div>
        </template>

        <el-form-item label="调度类型" prop="scheduleType">
          <el-radio-group v-model="form.scheduleType" class="schedule-type-group">
            <el-radio-button value="API">
              <el-icon><Link /></el-icon> API 触发
            </el-radio-button>
            <el-radio-button value="CRON">
              <el-icon><Timer /></el-icon> 定时调度
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- ===== API 触发模式 ===== -->
        <template v-if="form.scheduleType === 'API'">
          <el-form-item label="API 名称" prop="apiName">
            <el-input v-model="form.apiName" maxlength="100" placeholder="如：工商信息查询任务API" />
          </el-form-item>
          <el-form-item label="访问路径" prop="apiPath">
            <el-input v-model="form.apiPath" maxlength="500" placeholder="如：/api/task/commercial-info" />
          </el-form-item>
          <el-form-item label="API 描述">
            <el-input v-model="form.apiDescription" type="textarea" :rows="2" maxlength="1000" placeholder="描述说明（选填）" show-word-limit />
          </el-form-item>
        </template>

        <!-- ===== 定时调度 (CRON) 模式 ===== -->
        <template v-if="form.scheduleType === 'CRON'">
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="时间单位" prop="timeUnit">
                <el-select v-model="form.timeUnit" style="width: 100%">
                  <el-option label="秒" value="SECOND" />
                  <el-option label="分" value="MINUTE" />
                  <el-option label="时" value="HOUR" />
                  <el-option label="天" value="DAY" />
                  <el-option label="月" value="MONTH" />
                  <el-option label="周" value="WEEK" />
                  <el-option label="年" value="YEAR" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="配置模式" prop="configType">
                <el-select v-model="form.configType" style="width: 100%">
                  <el-option label="范围" value="RANGE" />
                  <el-option label="间隔" value="INTERVAL" />
                  <el-option label="指定" value="SPECIFY" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 范围模式 -->
          <el-row v-if="form.configType === 'RANGE'" :gutter="24">
            <el-col :span="12">
              <el-form-item label="起始值" prop="fromValue">
                <el-input-number v-model="form.fromValue" :min="0" :max="59" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束值" prop="toValue">
                <el-input-number v-model="form.toValue" :min="0" :max="59" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 间隔模式 -->
          <el-row v-if="form.configType === 'INTERVAL'" :gutter="24">
            <el-col :span="12">
              <el-form-item label="起始值" prop="fromValue">
                <el-input-number v-model="form.fromValue" :min="0" :max="59" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="间隔值" prop="intervalValue">
                <el-input-number v-model="form.intervalValue" :min="1" :max="59" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 指定模式 -->
          <el-form-item v-if="form.configType === 'SPECIFY'" label="指定值">
            <div style="display: flex; flex-wrap: wrap; gap: 8px; width: 100%">
              <el-tag
                v-for="(v, i) in form.specifiedValues"
                :key="i"
                closable
                @close="form.specifiedValues.splice(i, 1)"
              >{{ v }}</el-tag>
              <el-input
                v-if="specifyInputVisible"
                ref="specifyInputRef"
                v-model="specifyInputValue"
                size="small"
                style="width: 90px"
                @keyup.enter="addSpecifyValue"
                @blur="addSpecifyValue"
              />
              <el-button v-else size="small" @click="showSpecifyInput">+ 添加</el-button>
            </div>
          </el-form-item>

          <el-form-item label="CRON 表达式">
            <el-input v-model="cronPreview" readonly>
              <template #append>
                <el-button @click="copyCron">复制</el-button>
              </template>
            </el-input>
          </el-form-item>

          <!-- 任务参数 -->
          <el-form-item label="任务参数">
            <el-table :data="form.taskParams" size="small" stripe border style="width: 100%">
              <el-table-column type="index" label="序号" width="55" />
              <el-table-column label="参数名" min-width="160">
                <template #default="{ row }">
                  <el-input v-model="row.paramName" size="small" placeholder="参数名" />
                </template>
              </el-table-column>
              <el-table-column label="参数值" min-width="180">
                <template #default="{ row }">
                  <el-input v-model="row.paramValue" size="small" placeholder="参数值" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="65" fixed="right">
                <template #default="{ $index }">
                  <el-button link type="danger" size="small" @click="form.taskParams.splice($index, 1)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button type="primary" plain size="small" style="margin-top: 10px" @click="form.taskParams.push({ paramName: '', paramValue: '' })">
              <el-icon><Plus /></el-icon> 新增参数
            </el-button>
          </el-form-item>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="生效日期">
                <el-date-picker v-model="form.effectiveDate" type="datetime" placeholder="生效日期" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="失效日期">
                <el-date-picker v-model="form.expireDate" type="datetime" placeholder="失效日期" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>
      </el-card>

      <!-- 提交按钮 -->
      <div class="form-actions">
        <el-button type="primary" size="large" @click="handleSave">
          <el-icon><Check /></el-icon> 保存
        </el-button>
        <el-button size="large" @click="router.back()">返回</el-button>
      </div>
    </el-form>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref, computed, watch, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, Check, Link, Timer, Plus } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer.vue'
import { setTaskTriggerConfig, getTaskDetail } from '@/api/task'

const route = useRoute()
const router = useRouter()
const taskId = Number(route.params.id)
const formRef = ref()

const form = reactive({
  scheduleType: 'CRON',
  // API 模式
  apiName: '',
  apiPath: '',
  apiDescription: '',
  // CRON 模式
  timeUnit: 'SECOND',
  configType: 'INTERVAL',
  fromValue: 0,
  toValue: 59,
  intervalValue: 10,
  specifiedValues: [] as number[],
  cronExpression: '',
  taskParams: [] as { paramName: string; paramValue: string }[],
  effectiveDate: '',
  expireDate: '',
})

const rules: Record<string, any> = {
  scheduleType: [{ required: true, message: '请选择调度类型', trigger: 'change' }],
  apiName: [
    {
      required: true,
      message: '请输入 API 名称',
      trigger: 'blur',
      validator: (_rule: any, value: string, callback: any) => {
        if (form.scheduleType === 'API' && !value?.trim()) {
          callback(new Error('请输入 API 名称'))
        } else {
          callback()
        }
      },
    },
  ],
  apiPath: [
    {
      required: true,
      message: '请输入 API 访问路径',
      trigger: 'blur',
      validator: (_rule: any, value: string, callback: any) => {
        if (form.scheduleType === 'API' && !value?.trim()) {
          callback(new Error('请输入 API 访问路径'))
        } else {
          callback()
        }
      },
    },
  ],
}

// ===== CRON 预览 =====
const cronPreview = computed(() => {
  if (form.scheduleType !== 'CRON') return ''
  const unit = form.timeUnit
  const type = form.configType

  if (unit === 'SECOND') {
    if (type === 'INTERVAL' && form.intervalValue != null) {
      return `${form.fromValue ?? 0}/${form.intervalValue} * * * * ?`
    } else if (type === 'SPECIFY' && form.specifiedValues.length > 0) {
      return `${form.specifiedValues.join(',')} * * * * ?`
    } else if (type === 'RANGE' && form.fromValue != null && form.toValue != null) {
      return `${form.fromValue}-${form.toValue} * * * * ?`
    }
  } else if (unit === 'MINUTE') {
    const min = form.intervalValue ? `*/${form.intervalValue}` : `${form.fromValue ?? 0}`
    if (type === 'INTERVAL') {
      return `0 ${min} * * * ?`
    } else if (type === 'SPECIFY' && form.specifiedValues.length > 0) {
      return `0 ${form.specifiedValues.join(',')} * * * ?`
    } else if (type === 'RANGE' && form.fromValue != null && form.toValue != null) {
      return `0 ${form.fromValue}-${form.toValue} * * * ?`
    }
  } else if (unit === 'HOUR') {
    const hour = form.intervalValue ? `*/${form.intervalValue}` : `${form.fromValue ?? 0}`
    if (type === 'INTERVAL') {
      return `0 0 ${hour} * * ?`
    } else if (type === 'SPECIFY' && form.specifiedValues.length > 0) {
      return `0 0 ${form.specifiedValues.join(',')} * * ?`
    } else if (type === 'RANGE' && form.fromValue != null && form.toValue != null) {
      return `0 0 ${form.fromValue}-${form.toValue} * * ?`
    }
  } else if (unit === 'DAY') {
    return `0 0 ${form.fromValue ?? 2} * * ?`
  }
  return '0 0 2 * * ?'
})

// 同步 cronPreview 到 form.cronExpression
watch(cronPreview, (val) => {
  form.cronExpression = val
})

// ===== 指定值输入 =====
const specifyInputVisible = ref(false)
const specifyInputRef = ref<any>(null)
const specifyInputValue = ref('')

function showSpecifyInput() {
  specifyInputVisible.value = true
  nextTick(() => specifyInputRef.value?.focus())
}

function addSpecifyValue() {
  const val = parseInt(specifyInputValue.value, 10)
  if (!isNaN(val) && !form.specifiedValues.includes(val)) {
    form.specifiedValues.push(val)
  }
  specifyInputValue.value = ''
  specifyInputVisible.value = false
}

// ===== 复制 CRON =====
function copyCron() {
  navigator.clipboard.writeText(cronPreview.value)
  ElMessage.success('已复制')
}

onMounted(async () => {
  try {
    const detail = await getTaskDetail(taskId) as any
    if (detail?.scheduleType) form.scheduleType = detail.scheduleType
    if (detail?.triggerConfig) {
      const tc = detail.triggerConfig
      if (tc.apiName) form.apiName = tc.apiName
      if (tc.apiPath) form.apiPath = tc.apiPath
    }
  } catch { /* ignore */ }
})

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()

    const payload: Record<string, any> = {
      scheduleType: form.scheduleType,
    }

    if (form.scheduleType === 'API') {
      payload.apiConfig = {
        apiName: form.apiName,
        apiPath: form.apiPath,
        apiDescription: form.apiDescription || undefined,
      }
    } else if (form.scheduleType === 'CRON') {
      payload.scheduleConfig = {
        timeUnit: form.timeUnit,
        configType: form.configType,
        fromValue: form.configType !== 'SPECIFY' ? form.fromValue : undefined,
        toValue: form.configType === 'RANGE' ? form.toValue : undefined,
        intervalValue: form.configType === 'INTERVAL' ? form.intervalValue : undefined,
        specifiedValues: form.configType === 'SPECIFY' ? form.specifiedValues : undefined,
      }
      payload.effectiveDate = form.effectiveDate || undefined
      payload.expireDate = form.expireDate || undefined
      if (form.taskParams.length > 0) {
        payload.taskParams = Object.fromEntries(
          form.taskParams.filter(p => p.paramName).map(p => [p.paramName, p.paramValue])
        )
      }
    }

    await setTaskTriggerConfig(taskId, payload)
    ElMessage.success('触发设置保存成功')
    router.push('/task/list')
  } catch { /* handled */ }
}
</script>

<style scoped lang="scss">
.trigger-form {
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

.schedule-type-group {
  display: flex;
  gap: 0;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 8px 0 24px;
}
</style>

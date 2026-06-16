<template>
  <PageContainer :title="isEdit ? '编辑数据库连接' : '新增数据库连接'">
    <el-form :model="form" label-width="110px" class="styled-form">
      <!-- 连接信息卡片 -->
      <el-card shadow="never" class="form-card">
        <template #header>
          <div class="card-header"><el-icon><InfoFilled /></el-icon> 连接信息</div>
        </template>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="连接名称" required>
              <el-input v-model="form.connectionName" maxlength="50" placeholder="全局唯一" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据库类型" required>
              <el-select v-model="form.dbType" style="width: 100%" disabled>
                <el-option v-for="db in dbOptions" :key="db.value" :label="db.label" :value="db.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="主机地址" required>
              <el-input v-model="form.host" placeholder="如：192.168.1.100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="端口" required>
              <el-input-number v-model="form.port" :min="1" :max="65535" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="数据库名称" required>
              <el-input v-model="form.databaseName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户名" required>
              <el-input v-model="form.username" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? '不填则不修改密码' : '请输入密码'" />
        </el-form-item>
      </el-card>

      <!-- 高级配置卡片 -->
      <el-card shadow="never" class="form-card">
        <template #header>
          <div class="card-header"><el-icon><Setting /></el-icon> 高级配置</div>
        </template>
        <el-form-item label="JDBC 参数">
          <el-input v-model="form.jdbcParams" type="textarea" :rows="2" placeholder="useSSL=true&serverTimezone=Asia/Shanghai（可选）" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="描述说明（选填）" />
        </el-form-item>
      </el-card>

      <!-- 提交按钮 -->
      <div class="form-actions">
        <el-button type="primary" size="large" @click="handleSave"><el-icon><Check /></el-icon> 保存</el-button>
        <el-button v-if="isEdit" size="large" :loading="testLoading" @click="handleTest">测试连接</el-button>
        <el-button size="large" @click="router.back()">取消</el-button>
      </div>
    </el-form>

    <!-- 测试连接弹框 -->
    <el-dialog v-model="testDialogVisible" title="测试连接" width="560px" top="30vh">
      <div v-if="testLoading" style="text-align: center; padding: 40px">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p style="margin-top: 12px">正在测试连接...</p>
      </div>
      <template v-else-if="testResult">
        <el-descriptions :column="1" size="small" border>
          <el-descriptions-item label="测试结果">
            <el-tag :type="testResult.success ? 'success' : 'danger'" size="small">{{ testResult.success ? '连接成功' : '连接失败' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="响应耗时">{{ testResult.responseTime ? `${testResult.responseTime}ms` : '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="testResult.errorMessage" label="错误信息"><span style="color: var(--el-color-danger)">{{ testResult.errorMessage }}</span></el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer><el-button @click="testDialogVisible = false">关闭</el-button></template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, Check, InfoFilled, Setting } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer.vue'
import { createDatabase, updateDatabase, getDatabaseDetail, testDatabase } from '@/api/database'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id

const dbOptions = [
  { label: 'MySQL', value: 'MySQL' }, { label: 'PostgreSQL', value: 'PostgreSQL' },
  { label: 'Oracle', value: 'Oracle' }, { label: 'SQLServer', value: 'SQLServer' },
  { label: 'Hive', value: 'Hive' }, { label: 'ClickHouse', value: 'ClickHouse' },
]

const form = reactive({
  connectionName: '', dbType: '', host: '', port: 3306,
  databaseName: '', username: '', password: '', jdbcParams: '', description: '',
})

const testLoading = ref(false)
const testDialogVisible = ref(false)
const testResult = ref<{ success: boolean; responseTime?: number; errorMessage?: string } | null>(null)

async function handleTest() {
  if (!route.params.id) return
  testLoading.value = true; testDialogVisible.value = true; testResult.value = null
  try {
    const res = await testDatabase(Number(route.params.id))
    testResult.value = res as any
  } catch (e: any) { testResult.value = { success: false, errorMessage: e?.message || '连接测试失败' } }
  finally { testLoading.value = false }
}

onMounted(async () => {
  form.dbType = (route.query.dbType as string) || 'MySQL'
  if (isEdit) { const d = await getDatabaseDetail(Number(route.params.id)) as any; if (d) Object.assign(form, d) }
})

async function handleSave() {
  try {
    if (isEdit) await updateDatabase(Number(route.params.id), form)
    else await createDatabase(form)
    ElMessage.success(isEdit ? '更新成功' : '新增成功'); router.push('/database/list')
  } catch { /* handled */ }
}
</script>

<style scoped lang="scss">
.styled-form { max-width: 800px; margin: 0 auto; }
.form-card { margin-bottom: 20px; border-radius: 8px; }
.card-header { display: flex; align-items: center; gap: 6px; font-weight: 600; font-size: 15px; }
.form-actions { display: flex; justify-content: center; gap: 16px; padding: 8px 0 24px; }
</style>

<template>
  <PageContainer :title="isEdit ? '编辑码表' : '新增码表'">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="styled-form">
      <!-- 基本信息卡片 -->
      <el-card shadow="never" class="form-card">
        <template #header>
          <div class="card-header"><el-icon><InfoFilled /></el-icon> 基本信息</div>
        </template>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="码表名称" prop="tableName">
              <el-input v-model="form.tableName" maxlength="50" placeholder="全局唯一" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="isEdit" label="码表编码">
              <el-input v-model="form.tableCode" disabled placeholder="系统自动生成，不可更改" />
            </el-form-item>
            <el-form-item v-else label="码表编码">
              <el-input value="系统自动生成" disabled placeholder="新建后自动生成" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="描述说明（选填）" />
        </el-form-item>
      </el-card>

      <!-- 提交按钮 -->
      <div class="form-actions">
        <el-button type="primary" size="large" @click="handleSave"><el-icon><Check /></el-icon> 保存</el-button>
        <el-button size="large" @click="router.back()">取消</el-button>
      </div>
    </el-form>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, InfoFilled } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer.vue'
import { createCodeTable, updateCodeTable, getCodeTableDetail } from '@/api/codeTable'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const formRef = ref()

const form = reactive({ tableName: '', tableCode: '', description: '' })

const rules = {
  tableName: [{ required: true, message: '请输入码表名称', trigger: 'blur' }],
}

onMounted(async () => {
  if (isEdit) {
    const detail = await getCodeTableDetail(Number(route.params.id)) as any
    if (detail) Object.assign(form, detail)
  }
})

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (isEdit) await updateCodeTable(Number(route.params.id), form)
    else await createCodeTable(form)
    ElMessage.success(isEdit ? '更新成功' : '新增成功')
    router.push('/code-table/list')
  } catch { /* handled */ }
}
</script>

<style scoped lang="scss">
.styled-form { max-width: 600px; margin: 0 auto; }
.form-card { margin-bottom: 20px; border-radius: 8px; }
.card-header { display: flex; align-items: center; gap: 6px; font-weight: 600; font-size: 15px; }
.form-actions { display: flex; justify-content: center; gap: 16px; padding: 8px 0 24px; }
</style>

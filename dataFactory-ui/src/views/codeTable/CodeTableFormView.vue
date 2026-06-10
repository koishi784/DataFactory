<template>
  <PageContainer :title="isEdit ? '编辑码表' : '新增码表'">
    <el-form :model="form" label-width="120px" style="max-width: 600px">
      <el-form-item label="码表名称" required>
        <el-input v-model="form.tableName" maxlength="50" />
      </el-form-item>
      <el-form-item label="码表编码" required>
        <el-input v-model="form.tableCode" maxlength="50" placeholder="全局唯一" />
      </el-form-item>
      <el-form-item label="描述">
        <el-input v-model="form.description" type="textarea" :rows="3" />
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
import { createCodeTable, updateCodeTable, getCodeTableDetail } from '@/api/codeTable'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id

const form = reactive({ tableName: '', tableCode: '', description: '' })

onMounted(async () => {
  if (isEdit) {
    const detail = await getCodeTableDetail(Number(route.params.id)) as any
    if (detail) Object.assign(form, detail)
  }
})

async function handleSave() {
  try {
    if (isEdit) await updateCodeTable(Number(route.params.id), form)
    else await createCodeTable(form)
    ElMessage.success(isEdit ? '更新成功' : '新增成功')
    router.push('/code-table/list')
  } catch { /* handled */ }
}
</script>

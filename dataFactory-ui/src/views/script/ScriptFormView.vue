<template>
  <PageContainer :title="isEdit ? '编辑脚本' : '新增脚本'">
    <el-form :model="form" label-width="120px" style="max-width: 800px">
      <el-form-item label="脚本名称" required>
        <el-input v-model="form.scriptName" maxlength="50" />
      </el-form-item>
      <el-form-item label="脚本类型" required>
        <el-select v-model="form.scriptType" style="width: 100%">
          <el-option label="Python 3" value="Python" />
        </el-select>
      </el-form-item>
      <el-form-item label="脚本内容">
        <el-input v-model="form.content" type="textarea" :rows="12" placeholder="在此编写 Python 脚本代码" />
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
import { createScript, updateScript, getScriptDetail } from '@/api/script'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const form = reactive({ scriptName: '', scriptType: 'Python', content: '', description: '' })

onMounted(async () => {
  if (isEdit) {
    const detail = await getScriptDetail(Number(route.params.id)) as any
    if (detail) Object.assign(form, detail)
  }
})

async function handleSave() {
  try {
    if (isEdit) await updateScript(Number(route.params.id), form)
    else await createScript(form)
    ElMessage.success(isEdit ? '更新成功' : '新增成功')
    router.push('/script/list')
  } catch { /* handled */ }
}
</script>

<template>
  <PageContainer :title="isEdit ? '编辑数据资产' : '新增数据资产'">
    <el-form :model="form" label-width="120px" style="max-width: 600px">
      <el-form-item label="中文名称" required>
        <el-input v-model="form.assetName" maxlength="50" />
      </el-form-item>
      <el-form-item label="英文名称" required>
        <el-input v-model="form.englishName" maxlength="100" placeholder="仅支持英文、数字和下划线" />
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
import { createAsset, updateAsset, getAssetDetail } from '@/api/asset'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const form = reactive({ assetName: '', englishName: '', description: '' })

onMounted(async () => {
  if (isEdit) {
    const detail = await getAssetDetail(Number(route.params.id)) as any
    if (detail) Object.assign(form, detail)
  }
})

async function handleSave() {
  try {
    if (isEdit) await updateAsset(Number(route.params.id), form)
    else await createAsset(form)
    ElMessage.success(isEdit ? '更新成功' : '新增成功')
    router.push('/asset/list')
  } catch { /* handled */ }
}
</script>

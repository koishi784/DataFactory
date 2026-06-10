<template>
  <PageContainer :title="isEdit ? '编辑数据标准' : '新增数据标准'">
    <el-form :model="form" label-width="120px" style="max-width: 600px">
      <el-form-item label="标准名称" required>
        <el-input v-model="form.standardName" maxlength="50" />
      </el-form-item>
      <el-form-item label="标准编码" required>
        <el-input v-model="form.standardCode" maxlength="50" placeholder="全局唯一" />
      </el-form-item>
      <el-form-item label="数据类型" required>
        <el-select v-model="form.dataType" style="width: 100%">
          <el-option label="字符串(String)" value="String" />
          <el-option label="整型(Integer)" value="Integer" />
          <el-option label="浮点型(Float)" value="Float" />
          <el-option label="枚举(Enum)" value="Enum" />
        </el-select>
      </el-form-item>
      <el-form-item label="长度">
        <el-input-number v-model="form.length" :min="0" />
      </el-form-item>
      <el-form-item label="精度">
        <el-input-number v-model="form.precision" :min="0" />
      </el-form-item>
      <el-form-item label="校验规则">
        <el-input v-model="form.validationRule" placeholder="正则表达式" />
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
import { createDataStandard, updateDataStandard, getDataStandardDetail } from '@/api/dataStandard'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id

const form = reactive({ standardName: '', standardCode: '', dataType: 'String', length: 0, precision: 0, validationRule: '', description: '' })

onMounted(async () => {
  if (isEdit) {
    const detail = await getDataStandardDetail(Number(route.params.id)) as any
    if (detail) Object.assign(form, detail)
  }
})

async function handleSave() {
  try {
    if (isEdit) await updateDataStandard(Number(route.params.id), form)
    else await createDataStandard(form)
    ElMessage.success(isEdit ? '更新成功' : '新增成功')
    router.push('/standard')
  } catch { /* handled */ }
}
</script>

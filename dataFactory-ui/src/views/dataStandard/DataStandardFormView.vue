<template>
  <PageContainer :title="isEdit ? '编辑数据标准' : '新增数据标准'">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="max-width: 600px">
      <el-form-item label="标准中文名" prop="name">
        <el-input v-model="form.name" maxlength="50" />
      </el-form-item>
      <el-form-item label="标准英文名" prop="englishName">
        <el-input v-model="form.englishName" maxlength="100" placeholder="仅支持英文、数字和下划线，须以字母开头" />
      </el-form-item>
      <el-form-item label="标准编码" prop="standardCode">
        <el-input v-model="form.standardCode" maxlength="50" placeholder="全局唯一" />
      </el-form-item>
      <el-form-item label="数据类型" prop="dataType">
        <el-select v-model="form.dataType" style="width: 100%">
          <el-option label="字符串(String)" value="String" />
          <el-option label="整型(Integer)" value="Integer" />
          <el-option label="浮点型(Float)" value="Float" />
          <el-option label="枚举(Enum)" value="Enum" />
        </el-select>
      </el-form-item>

      <!-- 数据类型相关字段 -->
      <el-form-item label="长度" prop="length">
        <el-input-number v-model="form.length" :min="0" :disabled="isLengthDisabled" style="width: 100%" />
        <div v-if="form.dataType === 'String'" class="form-tip">当前数据类型为 String，可编辑</div>
        <div v-else class="form-tip form-tip--muted">当前数据类型不允许编辑长度</div>
      </el-form-item>
      <el-form-item label="数据精度" prop="precision">
        <el-input-number v-model="form.precision" :min="0" :disabled="isPrecisionDisabled" style="width: 100%" />
        <div v-if="form.dataType === 'Float'" class="form-tip">当前数据类型为 Float，可编辑</div>
        <div v-else class="form-tip form-tip--muted">当前数据类型不允许编辑数据精度</div>
      </el-form-item>
      <el-form-item label="取值范围最小值" prop="rangeMin">
        <el-input v-model="form.rangeMin" :disabled="isRangeDisabled" placeholder="请输入取值范围最小值" />
        <div v-if="['Integer', 'Float'].includes(form.dataType)" class="form-tip">当前数据类型可编辑</div>
        <div v-else class="form-tip form-tip--muted">当前数据类型不允许编辑取值范围</div>
      </el-form-item>
      <el-form-item label="取值范围最大值" prop="rangeMax">
        <el-input v-model="form.rangeMax" :disabled="isRangeDisabled" placeholder="请输入取值范围最大值" />
      </el-form-item>
      <el-form-item label="枚举范围" prop="enumRange">
        <el-input v-model="form.enumRange" :disabled="isEnumRangeDisabled" placeholder="多个枚举值请用逗号分隔" />
        <div v-if="form.dataType === 'Enum'" class="form-tip">当前数据类型为 Enum，可编辑</div>
        <div v-else class="form-tip form-tip--muted">当前数据类型不允许编辑枚举范围</div>
      </el-form-item>

      <el-form-item label="默认值" prop="defaultValue">
        <el-input v-model="form.defaultValue" />
      </el-form-item>
      <el-form-item label="校验规则" prop="validationRule">
        <el-input v-model="form.validationRule" placeholder="正则表达式" />
      </el-form-item>
      <el-form-item label="来源机构" prop="sourceOrganization">
        <el-input v-model="form.sourceOrganization" maxlength="100" />
      </el-form-item>
      <el-form-item label="是否必填">
        <el-switch v-model="form.nullable" :active-value="1" :inactive-value="0" active-text="不可为空" inactive-text="可为空" />
      </el-form-item>
      <el-form-item label="描述" prop="description">
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
import { reactive, ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { createDataStandard, updateDataStandard, getDataStandardDetail } from '@/api/dataStandard'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const formRef = ref()

const form = reactive({
  name: '',
  englishName: '',
  standardCode: '',
  dataType: 'String' as string,
  length: 0,
  precision: 0,
  rangeMin: '',
  rangeMax: '',
  enumRange: '',
  defaultValue: '',
  sourceOrganization: '',
  nullable: 0,
  validationRule: '',
  description: '',
})

const rules = {
  name: [{ required: true, message: '请输入标准中文名', trigger: 'blur' }],
  englishName: [
    { required: true, message: '请输入标准英文名', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '仅支持英文、数字和下划线，须以字母开头', trigger: 'blur' },
  ],
  standardCode: [{ required: true, message: '请输入标准编码', trigger: 'blur' }],
  dataType: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
  sourceOrganization: [{ required: true, message: '请输入来源机构', trigger: 'blur' }],
}

// 根据数据类型控制字段禁用状态
const isLengthDisabled = computed(() => form.dataType !== 'String')
const isPrecisionDisabled = computed(() => form.dataType !== 'Float')
const isRangeDisabled = computed(() => !['Integer', 'Float'].includes(form.dataType))
const isEnumRangeDisabled = computed(() => form.dataType !== 'Enum')

/** 根据数据类型清空不适用字段 */
function clearFieldsByDataType(type: string) {
  switch (type) {
    case 'String':
      form.precision = 0
      form.rangeMin = ''
      form.rangeMax = ''
      form.enumRange = ''
      break
    case 'Integer':
      form.length = 0
      form.precision = 0
      form.enumRange = ''
      break
    case 'Float':
      form.length = 0
      form.enumRange = ''
      break
    case 'Enum':
      form.length = 0
      form.precision = 0
      form.rangeMin = ''
      form.rangeMax = ''
      break
  }
}

// 监听数据类型变化，自动清空不适用字段
watch(() => form.dataType, (newType) => {
  clearFieldsByDataType(newType)
})

onMounted(async () => {
  if (isEdit) {
    try {
      const detail = await getDataStandardDetail(Number(route.params.id)) as any
      if (detail) {
        Object.assign(form, detail)
        // 确保字段状态与数据类型一致（应对历史数据中存在不合规字段值的情况）
        clearFieldsByDataType(form.dataType)
      }
    } catch { /* handled */ }
  }
})

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (isEdit) {
      await updateDataStandard(Number(route.params.id), form)
    } else {
      await createDataStandard(form)
    }
    ElMessage.success(isEdit ? '更新成功' : '新增成功')
    router.push('/standard')
  } catch { /* handled */ }
}
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  margin-top: 4px;
  color: #409eff;
}
.form-tip--muted {
  color: #c0c4cc;
}
</style>

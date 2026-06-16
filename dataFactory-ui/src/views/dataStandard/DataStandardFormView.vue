<template>
  <PageContainer :title="isEdit ? '编辑数据标准' : '新增数据标准'">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="styled-form">
      <!-- 基本信息卡片 -->
      <el-card shadow="never" class="form-card">
        <template #header>
          <div class="card-header"><el-icon><InfoFilled /></el-icon> 基本信息</div>
        </template>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="标准中文名" prop="name">
              <el-input v-model="form.name" maxlength="50" placeholder="仅支持中文及英文大小写" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标准英文名" prop="englishName">
              <el-input v-model="form.englishName" maxlength="100" placeholder="仅支持英文、数字和下划线" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item v-if="isEdit" label="标准编码">
              <el-input v-model="form.standardCode" disabled placeholder="系统自动生成，不可更改" />
            </el-form-item>
            <el-form-item v-else label="标准编码">
              <el-input value="系统自动生成" disabled placeholder="新建后自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源机构" prop="sourceOrganization">
              <el-input v-model="form.sourceOrganization" maxlength="100" placeholder="如：数宜信" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
      </el-card>

      <!-- 数据类型与约束卡片 -->
      <el-card shadow="never" class="form-card">
        <template #header>
          <div class="card-header"><el-icon><Setting /></el-icon> 数据类型与约束</div>
        </template>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="数据类型" prop="dataType">
              <el-select v-model="form.dataType" style="width: 100%">
                <el-option label="字符串(String)" value="String" />
                <el-option label="整型(Int)" value="Int" />
                <el-option label="浮点型(Float)" value="Float" />
                <el-option label="枚举(Enum)" value="Enum" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否必填">
              <el-switch v-model="form.nullable" :active-value="1" :inactive-value="0" active-text="不可为空" inactive-text="可为空" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24">
          <el-col :span="12" v-if="form.dataType === 'String'">
            <el-form-item label="长度" prop="length">
              <el-input-number v-model="form.length" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.dataType === 'Float'">
            <el-form-item label="数据精度" prop="precision">
              <el-input-number v-model="form.precision" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24" v-if="['String', 'Int', 'Float'].includes(form.dataType)">
          <el-col :span="12">
            <el-form-item label="默认值">
              <el-input v-model="form.defaultValue" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="24" v-if="['Int', 'Float'].includes(form.dataType)">
          <el-col :span="12">
            <el-form-item label="取值范围最小值">
              <el-input v-model="form.rangeMin" placeholder="最小值" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="取值范围最大值">
              <el-input v-model="form.rangeMax" placeholder="最大值" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item v-if="form.dataType === 'Enum'" label="枚举范围">
          <el-select
            v-model="form.enumRange"
            filterable
            remote
            :remote-method="searchCodeTables"
            :loading="searchingCodeTable"
            placeholder="搜索已发布的码表名称"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="item in codeTableOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
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
import { reactive, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, InfoFilled, Setting } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer.vue'
import { createDataStandard, updateDataStandard, getDataStandardDetail } from '@/api/dataStandard'
import { getCodeTableList } from '@/api/codeTable'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const formRef = ref()
const codeTableOptions = ref<{ value: string; label: string }[]>([])
const searchingCodeTable = ref(false)
let searchTimer: ReturnType<typeof setTimeout> | null = null

const form = reactive({
  name: '', englishName: '', standardCode: '',
  dataType: 'String' as string, length: undefined, precision: undefined,
  rangeMin: '', rangeMax: '', enumRange: '', defaultValue: '',
  sourceOrganization: '', nullable: 0, description: '',
})

const rules = {
  name: [{ required: true, message: '请输入标准中文名', trigger: 'blur' }],
  englishName: [{ required: true, message: '请输入标准英文名', trigger: 'blur' }, { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '仅支持英文、数字和下划线，须以字母开头', trigger: 'blur' }],
  dataType: [{ required: true, message: '请选择数据类型', trigger: 'change' }],
  sourceOrganization: [{ required: true, message: '请输入来源机构', trigger: 'blur' }],
}

function clearFieldsByDataType(type: string) {
  switch (type) {
    case 'String': form.precision = undefined; form.rangeMin = ''; form.rangeMax = ''; form.enumRange = ''; break
    case 'Int': form.length = undefined; form.precision = undefined; form.enumRange = ''; break
    case 'Float': form.length = undefined; form.enumRange = ''; break
    case 'Enum': form.length = undefined; form.precision = undefined; form.rangeMin = ''; form.rangeMax = ''; break
  }
}

watch(() => form.dataType, clearFieldsByDataType)

onMounted(async () => {
  if (isEdit) {
    try {
      const detail = await getDataStandardDetail(Number(route.params.id)) as any
      if (detail) { Object.assign(form, detail); clearFieldsByDataType(form.dataType) }
    } catch { /* handled */ }
  }
})

function searchCodeTables(keyword: string) {
  if (searchTimer) clearTimeout(searchTimer)
  if (!keyword || !keyword.trim()) {
    codeTableOptions.value = []
    return
  }
  searchingCodeTable.value = true
  searchTimer = setTimeout(async () => {
    try {
      const res: any = await getCodeTableList({ keyword: keyword.trim(), status: '1', pageNum: 1, pageSize: 20 })
      const records = res?.records || res?.data?.records || []
      codeTableOptions.value = records.map((item: any) => ({
        value: item.tableCode,
        label: `${item.tableName}（${item.tableCode}）`,
      }))
    } catch { codeTableOptions.value = [] }
    finally { searchingCodeTable.value = false }
  }, 300)
}

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (isEdit) await updateDataStandard(Number(route.params.id), form)
    else await createDataStandard(form)
    ElMessage.success(isEdit ? '更新成功' : '新增成功'); router.push('/standard')
  } catch { /* handled */ }
}
</script>

<style scoped lang="scss">
.styled-form { max-width: 800px; margin: 0 auto; }
.form-card { margin-bottom: 20px; border-radius: 8px; }
.card-header { display: flex; align-items: center; gap: 6px; font-weight: 600; font-size: 15px; }
.form-actions { display: flex; justify-content: center; gap: 16px; padding: 8px 0 24px; }
</style>

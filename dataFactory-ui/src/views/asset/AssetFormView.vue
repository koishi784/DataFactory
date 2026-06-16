<template>
  <PageContainer :title="isEdit ? '编辑数据资产' : '新增数据资产'">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" class="styled-form">
      <!-- 基本信息卡片 -->
      <el-card shadow="never" class="form-card">
        <template #header>
          <div class="card-header"><el-icon><InfoFilled /></el-icon> 基本信息</div>
        </template>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="中文名称" prop="assetName">
              <el-input v-model="form.assetName" maxlength="50" placeholder="全局唯一，仅支持中文及英文大小写" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="英文名称" prop="englishName">
              <el-input v-model="form.englishName" maxlength="100" placeholder="仅支持英文、数字和下划线，以字母开头" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="所属目录" prop="directoryIds">
          <el-tree-select
            v-model="form.directoryIds"
            :data="directories"
            :props="{ label: 'name', children: 'children', value: 'id' }"
            placeholder="请选择所属目录（可多选）"
            multiple clearable
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" show-word-limit />
        </el-form-item>
      </el-card>

      <!-- 字段定义卡片 -->
      <el-card shadow="never" class="form-card">
        <template #header>
          <div class="card-header"><el-icon><Setting /></el-icon> 字段定义</div>
        </template>
        <el-table :data="form.fields" size="small" stripe border style="width: 100%">
          <el-table-column type="index" label="序号" width="55" />
          <el-table-column label="字段英文名" min-width="150">
            <template #default="{ row }"><el-input v-model="row.englishFieldName" size="small" placeholder="如：firmName" /></template>
          </el-table-column>
          <el-table-column label="字段中文名" min-width="150">
            <template #default="{ row }"><el-input v-model="row.chineseFieldName" size="small" placeholder="如：企业名称" /></template>
          </el-table-column>
          <el-table-column label="说明" min-width="140">
            <template #default="{ row }"><el-input v-model="row.description" size="small" placeholder="选填" /></template>
          </el-table-column>
          <el-table-column label="关联标准ID" width="100">
            <template #default="{ row }"><el-input-number v-model="row.standardId" :min="0" size="small" controls-position="right" style="width:80px" /></template>
          </el-table-column>
          <el-table-column label="排序" width="70">
            <template #default="{ row }"><el-input-number v-model="row.sortOrder" :min="0" size="small" controls-position="right" style="width:60px" /></template>
          </el-table-column>
          <el-table-column label="操作" width="60" fixed="right">
            <template #default="{ $index }"><el-button link type="danger" size="small" @click="form.fields.splice($index, 1)">删除</el-button></template>
          </el-table-column>
        </el-table>
        <el-button type="primary" plain size="small" style="margin-top: 10px" @click="addField"><el-icon><Plus /></el-icon> 新增字段</el-button>
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
import { Check, Plus, InfoFilled, Setting } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer.vue'
import { createAsset, updateAsset, getAssetDetail, getAssetDirectoryTree } from '@/api/asset'
import type { AssetDirectory, AssetField } from '@/types/asset'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const formRef = ref()
const directories = ref<AssetDirectory[]>([])

function newField(): AssetField { return { englishFieldName: '', chineseFieldName: '', description: '', sortOrder: 0 } }

const form = reactive({
  assetName: '', englishName: '', description: '', directoryIds: [] as number[], fields: [] as AssetField[],
})

const rules = {
  assetName: [{ required: true, message: '请输入中文名称', trigger: 'blur' }],
  englishName: [{ required: true, message: '请输入英文名称', trigger: 'blur' }, { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '仅支持英文、数字和下划线，以字母开头', trigger: 'blur' }],
  directoryIds: [{ required: true, message: '请选择所属目录', trigger: 'change' }],
}

function addField() { form.fields.push(newField()) }

onMounted(async () => {
  try { directories.value = await getAssetDirectoryTree() as any } catch { directories.value = [] }
  if (isEdit) {
    const detail = await getAssetDetail(Number(route.params.id)) as any
    if (detail) { form.assetName = detail.assetName || ''; form.englishName = detail.englishName || ''; form.description = detail.description || ''; form.directoryIds = detail.directoryIds || []; form.fields = detail.fields || [] }
  }
})

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (isEdit) await updateAsset(Number(route.params.id), form)
    else await createAsset(form)
    ElMessage.success(isEdit ? '更新成功' : '新增成功'); router.push('/asset/list')
  } catch { /* handled */ }
}
</script>

<style scoped lang="scss">
.styled-form { max-width: 800px; margin: 0 auto; }
.form-card { margin-bottom: 20px; border-radius: 8px; }
.card-header { display: flex; align-items: center; gap: 6px; font-weight: 600; font-size: 15px; }
.form-actions { display: flex; justify-content: center; gap: 16px; padding: 8px 0 24px; }
</style>

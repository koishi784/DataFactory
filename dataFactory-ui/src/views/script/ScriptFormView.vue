<template>
  <PageContainer :title="isEdit ? '编辑脚本' : '新增脚本'">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="script-form">
      <!-- 基本信息卡片 -->
      <el-card shadow="never" class="form-card">
        <template #header>
          <div class="card-header"><el-icon><InfoFilled /></el-icon> 基本信息</div>
        </template>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="脚本名称" prop="scriptName">
              <el-input v-model="form.scriptName" maxlength="50" placeholder="全局唯一，仅支持中文和英文大小写" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="脚本类型" prop="scriptType">
              <el-select v-model="form.scriptType" style="width: 100%" disabled>
                <el-option label="Python" value="PYTHON" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="所属分类" prop="categoryId">
          <el-tree-select
            v-model="form.categoryId"
            :data="categories"
            :props="{ label: 'name', children: 'children', value: 'id' }"
            placeholder="请选择脚本分类"
            clearable
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="描述说明（选填）" />
        </el-form-item>
      </el-card>

      <!-- 脚本源码卡片 -->
      <el-card shadow="never" class="form-card">
        <template #header>
          <div class="card-header"><el-icon><Document /></el-icon> 脚本源码</div>
        </template>
        <el-radio-group v-model="inputMode" class="mode-switch">
          <el-radio-button value="upload">
            <el-icon><Upload /></el-icon> 文件上传
          </el-radio-button>
          <el-radio-button value="edit">
            <el-icon><EditPen /></el-icon> 在线编辑
          </el-radio-button>
        </el-radio-group>

        <div class="mode-content">
          <!-- 文件上传模式 -->
          <template v-if="inputMode === 'upload'">
            <div class="upload-area" @click="fileInputRef?.click()">
              <input ref="fileInputRef" type="file" style="display:none" @change="handleFileChange" />
              <template v-if="!form.fileId">
                <el-icon :size="40" color="#409eff"><Upload /></el-icon>
                <p class="upload-text">点击选择脚本文件（.py）</p>
                <p class="upload-hint">支持 Python 脚本文件上传</p>
              </template>
              <template v-else>
                <el-icon :size="40" color="#67c23a"><Document /></el-icon>
                <p class="upload-text">{{ uploadFileName }}</p>
                <p class="upload-hint">文件已上传</p>
                <el-button size="small" @click.stop="clearFile">重新选择</el-button>
              </template>
            </div>
          </template>

          <!-- 在线编辑模式 -->
          <template v-if="inputMode === 'edit'">
            <el-input
              v-model="form.scriptContent"
              type="textarea"
              :rows="16"
              placeholder="在此编辑脚本代码"
              class="code-editor"
            />
          </template>
        </div>
      </el-card>

      <!-- 参数配置卡片 -->
      <el-card shadow="never" class="form-card">
        <template #header>
          <div class="card-header"><el-icon><Setting /></el-icon> 参数配置</div>
        </template>
        <el-tabs type="border-card">
          <el-tab-pane label="输入参数">
            <el-table :data="form.inputParams" size="small" stripe border style="width: 100%">
              <el-table-column type="index" label="序号" width="55" />
              <el-table-column label="参数名称" min-width="160">
                <template #default="{ row }">
                  <el-input v-model="row.paramName" size="small" placeholder="如：企业唯一id" />
                </template>
              </el-table-column>
              <el-table-column label="数据类型" width="130">
                <template #default="{ row }">
                  <el-select v-model="row.paramType" size="small" style="width: 100%">
                    <el-option label="字符串" value="String" />
                    <el-option label="整型" value="Int" />
                    <el-option label="浮点型" value="Float" />
                    <el-option label="布尔型" value="Boolean" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="描述" min-width="180">
                <template #default="{ row }">
                  <el-input v-model="row.description" size="small" placeholder="选填" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="65" fixed="right">
                <template #default="{ $index }">
                  <el-button link type="danger" size="small" @click="form.inputParams.splice($index, 1)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button type="primary" plain size="small" style="margin-top: 10px" @click="form.inputParams.push({ paramName: '', paramType: 'String', description: '' })">
              <el-icon><Plus /></el-icon> 新增输入参数
            </el-button>
          </el-tab-pane>
          <el-tab-pane label="输出参数">
            <el-table :data="form.outputParams" size="small" stripe border style="width: 100%">
              <el-table-column type="index" label="序号" width="55" />
              <el-table-column label="参数名称" min-width="160">
                <template #default="{ row }">
                  <el-input v-model="row.paramName" size="small" placeholder="如：id" />
                </template>
              </el-table-column>
              <el-table-column label="数据类型" width="130">
                <template #default="{ row }">
                  <el-select v-model="row.paramType" size="small" style="width: 100%">
                    <el-option label="字符串" value="String" />
                    <el-option label="整型" value="Int" />
                    <el-option label="浮点型" value="Float" />
                    <el-option label="布尔型" value="Boolean" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="描述" min-width="180">
                <template #default="{ row }">
                  <el-input v-model="row.description" size="small" placeholder="选填" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="65" fixed="right">
                <template #default="{ $index }">
                  <el-button link type="danger" size="small" @click="form.outputParams.splice($index, 1)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button type="primary" plain size="small" style="margin-top: 10px" @click="form.outputParams.push({ paramName: '', paramType: 'String', description: '' })">
              <el-icon><Plus /></el-icon> 新增输出参数
            </el-button>
          </el-tab-pane>
        </el-tabs>
      </el-card>

      <!-- 提交按钮 -->
      <div class="form-actions">
        <el-button type="primary" size="large" @click="handleSave">
          <el-icon><Check /></el-icon> 保存
        </el-button>
        <el-button size="large" @click="router.back()">取消</el-button>
      </div>
    </el-form>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Upload, EditPen, InfoFilled, Document, Setting, Check, Plus } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer.vue'
import { createScript, updateScript, getScriptDetail, getScriptCategoryTree } from '@/api/script'
import { uploadFile } from '@/api/common'
import type { ScriptCategory, ScriptParam } from '@/types/script'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const formRef = ref()
const categories = ref<ScriptCategory[]>([])
const fileInputRef = ref<HTMLInputElement | null>(null)
const uploadFileName = ref('')
const inputMode = ref<'upload' | 'edit'>('upload')

const form = reactive({
  scriptName: '',
  scriptType: 'PYTHON',
  categoryId: null as number | null,
  fileId: null as number | null,
  scriptContent: '',
  description: '',
  inputParams: [] as ScriptParam[],
  outputParams: [] as ScriptParam[],
})

const rules = {
  scriptName: [
    { required: true, message: '请输入脚本名称', trigger: 'blur' },
    { pattern: /^[a-zA-Z一-龥]+$/, message: '仅支持中文和英文大小写', trigger: 'blur' },
  ],
  categoryId: [{ required: true, message: '请选择所属分类', trigger: 'change' }],
}

async function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  try {
    const res = await uploadFile(file, 'SCRIPT') as any
    form.fileId = res.fileId ?? res.id
    uploadFileName.value = file.name
    ElMessage.success('文件上传成功')
  } catch { /* handled */ }
  target.value = ''
}

function clearFile() {
  form.fileId = null
  uploadFileName.value = ''
  if (fileInputRef.value) fileInputRef.value.value = ''
}

onMounted(async () => {
  try { categories.value = await getScriptCategoryTree() as any } catch { categories.value = [] }
  if (isEdit) {
    const detail = await getScriptDetail(Number(route.params.id)) as any
    if (detail) {
      form.scriptName = detail.scriptName || ''
      form.scriptType = detail.scriptType || 'PYTHON'
      form.categoryId = detail.categoryId ?? null
      form.fileId = detail.fileId ?? null
      form.scriptContent = detail.scriptContent || ''
      form.description = detail.description || ''
      form.inputParams = detail.inputParams || []
      form.outputParams = detail.outputParams || []
      if (detail.fileName) uploadFileName.value = detail.fileName
      if (detail.scriptContent) inputMode.value = 'edit'
    }
  }
})

function buildPayload() {
  const payload: Record<string, any> = {
    scriptName: form.scriptName,
    scriptType: form.scriptType,
    categoryId: form.categoryId,
    description: form.description || undefined,
  }
  if (inputMode.value === 'upload' && form.fileId) {
    payload.fileId = form.fileId
  } else if (inputMode.value === 'edit' && form.scriptContent) {
    payload.scriptContent = form.scriptContent
  }
  if (form.inputParams.length > 0) payload.inputParams = form.inputParams
  if (form.outputParams.length > 0) payload.outputParams = form.outputParams
  return payload
}

async function handleSave() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (!isEdit && !form.fileId && !form.scriptContent) {
      ElMessage.warning('请上传脚本文件或填写脚本代码')
      return
    }
    const payload = buildPayload()
    if (isEdit) {
      await updateScript(Number(route.params.id), payload)
    } else {
      await createScript(payload)
    }
    ElMessage.success(isEdit ? '更新成功' : '新增成功')
    router.push('/script/list')
  } catch { /* handled */ }
}
</script>

<style scoped lang="scss">
.script-form {
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

.mode-switch {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.mode-content {
  min-height: 100px;
}

.upload-area {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  background: #fafafa;

  &:hover {
    border-color: #409eff;
    background: #ecf5ff;
  }

  .upload-text {
    margin: 12px 0 4px;
    font-size: 15px;
    color: var(--el-text-color-primary);
    font-weight: 500;
  }

  .upload-hint {
    margin: 0 0 8px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}

.code-editor {
  :deep(.el-textarea__inner) {
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.6;
    background: #1e1e1e;
    color: #d4d4d4;
    border-color: #333;
    border-radius: 6px;

    &:focus {
      border-color: #409eff;
    }
  }
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 8px 0 24px;
}
</style>

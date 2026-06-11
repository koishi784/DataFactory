<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑' : '新增'"
    width="600px"
    @update:model-value="$emit('update:visible', $event)"
    @close="handleClose"
    destroy-on-close
  >
    <el-form ref="formRef" :model="model" :rules="rules" label-width="100px" label-position="right">
      <el-form-item v-for="field in fields" :key="field.prop" :label="field.label" :prop="field.prop">
        <el-input
          v-if="!field.type || field.type === 'input'"
          v-model="model[field.prop]"
          :placeholder="`请输入${field.label}`"
        />
        <el-select
          v-else-if="field.type === 'select'"
          v-model="model[field.prop]"
          :placeholder="`请选择${field.label}`"
          :style="{ width: '100%' }"
        >
          <el-option
            v-for="opt in field.options || []"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <el-input
          v-else-if="field.type === 'textarea'"
          v-model="model[field.prop]"
          type="textarea"
          :rows="3"
        />
      </el-form-item>
      <slot />
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleConfirm" :loading="submitting">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, type ComponentPublicInstance } from 'vue'
import type { FormInstance, FormItemRule } from 'element-plus'

export interface DialogField {
  prop: string
  label: string
  type?: 'input' | 'select' | 'textarea'
  options?: { label: string; value: any }[]
  rules?: FormItemRule[]
}

const props = defineProps<{
  visible: boolean
  isEdit: boolean
  model: Record<string, any>
  fields: DialogField[]
  rules?: Record<string, FormItemRule[]>
  submitting?: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  submit: []
}>()

const formRef = ref<ComponentPublicInstance<FormInstance>>()

function handleClose() {
  emit('update:visible', false)
}

async function handleConfirm() {
  if (!formRef.value) {
    emit('submit')
    return
  }
  try {
    await (formRef.value as unknown as FormInstance).validate()
    emit('submit')
  } catch {
    // 校验不通过
  }
}
</script>

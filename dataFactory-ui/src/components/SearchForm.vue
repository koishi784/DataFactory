<template>
  <el-form :model="model" inline class="search-form" @keyup.enter="$emit('search')">
    <el-form-item v-for="field in fields" :key="field.prop" :label="field.label">
      <!-- 文本输入 -->
      <el-input
        v-if="!field.type || field.type === 'input'"
        v-model="model[field.prop]"
        :placeholder="field.placeholder || `请输入${field.label}`"
        clearable
        :style="{ width: field.width || '180px' }"
      />
      <!-- 下拉选择 -->
      <el-select
        v-else-if="field.type === 'select'"
        v-model="model[field.prop]"
        :placeholder="field.placeholder || `请选择${field.label}`"
        clearable
        :style="{ width: field.width || '180px' }"
      >
        <el-option
          v-for="opt in field.options"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="$emit('search')">查询</el-button>
      <el-button @click="$emit('reset')">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
interface SearchField {
  prop: string
  label: string
  type?: 'input' | 'select'
  placeholder?: string
  width?: string
  options?: { label: string; value: any }[]
}

defineProps<{
  model: Record<string, any>
  fields: SearchField[]
}>()

defineEmits<{
  search: []
  reset: []
}>()
</script>

<style scoped lang="scss">
.search-form {
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}
</style>

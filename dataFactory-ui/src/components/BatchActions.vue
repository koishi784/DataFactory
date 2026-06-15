<template>
  <div class="batch-actions" v-if="show">
    <slot name="extra" />
    <el-button
      type="success"
      :disabled="!hasSelection"
      @click="$emit('batch-publish')"
    >
      批量发布
    </el-button>
    <el-button
      type="warning"
      :disabled="!hasSelection"
      @click="$emit('batch-disable')"
    >
      批量停用
    </el-button>
    <el-button
      v-if="showCategory"
      type="primary"
      :disabled="!hasSelection"
      @click="$emit('batch-category')"
    >
      批量分类
    </el-button>
    <slot name="append" />
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  hasSelection: boolean
  show?: boolean
  showCategory?: boolean
}>(), {
  show: true,
  showCategory: true,
})

defineEmits<{
  'batch-publish': []
  'batch-disable': []
  'batch-category': []
}>()
</script>

<style scoped lang="scss">
.batch-actions {
  display: flex;
  gap: 8px;
  padding-bottom: 16px;
}
</style>

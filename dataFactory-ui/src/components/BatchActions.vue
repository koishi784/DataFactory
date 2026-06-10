<template>
  <div class="batch-actions" v-if="show">
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
    <slot name="extra" />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  hasSelection: boolean
  show?: boolean
}>()

defineEmits<{
  'batch-publish': []
  'batch-disable': []
}>()
</script>

<style scoped lang="scss">
.batch-actions {
  display: flex;
  gap: 8px;
  padding-bottom: 16px;
}
</style>

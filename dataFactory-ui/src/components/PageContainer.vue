<template>
  <div class="page-container">
    <div v-if="title" class="page-header">
      <h2>{{ title }}</h2>
      <div class="page-actions">
        <slot name="actions" />
      </div>
    </div>
    <div class="page-body">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title?: string }>()
</script>

<style scoped lang="scss">
.page-container {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h2 {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }

    .page-actions {
      display: flex;
      gap: 8px;
    }
  }

  .page-body {
    background: #fff;
    border-radius: 4px;
    padding: 16px;
  }
}
</style>

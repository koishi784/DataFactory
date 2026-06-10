<template>
  <div class="status-actions">
    <el-button
      v-if="status === 0"
      type="primary"
      link
      size="small"
      @click="$emit('edit')"
    >
      编辑
    </el-button>
    <el-button
      v-if="status === 0"
      type="success"
      link
      size="small"
      @click="$emit('publish')"
    >
      发布
    </el-button>
    <el-button
      v-if="status === 1"
      type="warning"
      link
      size="small"
      @click="$emit('disable')"
    >
      停用
    </el-button>
    <el-button
      v-if="status === 2"
      type="primary"
      link
      size="small"
      @click="$emit('edit')"
    >
      编辑
    </el-button>
    <el-button
      v-if="status === 2"
      type="success"
      link
      size="small"
      @click="$emit('publish')"
    >
      重新发布
    </el-button>
    <el-button
      v-if="status === 0"
      type="danger"
      link
      size="small"
      @click="$emit('delete')"
    >
      删除
    </el-button>
  </div>
</template>

<script setup lang="ts">
defineProps<{ status: number }>()

defineEmits<{
  edit: []
  publish: []
  disable: []
  delete: []
}>()
</script>

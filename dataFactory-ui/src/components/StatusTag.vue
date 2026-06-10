<template>
  <el-tag :type="statusMap.type" size="small">
    {{ statusMap.label }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { StatusMap } from '@/types'

const props = defineProps<{ status: number }>()

const statusMap = computed(() => StatusMap[props.status] || { label: '未知', type: 'info' })
</script>

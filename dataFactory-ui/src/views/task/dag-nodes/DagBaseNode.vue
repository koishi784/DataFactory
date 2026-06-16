<template>
  <div class="dag-node" :class="'node-' + type" :style="{ background: bgColor, borderColor: borderColor }">
    <div class="node-header">{{ label }}</div>
    <Handle type="target" :position="Position.Top" />
    <Handle type="source" :position="Position.Bottom" />
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'

defineProps<{
  label?: string
  type?: string
  bgColor?: string
  borderColor?: string
}>()
</script>

<style scoped>
.dag-node {
  padding: 8px 16px;
  border-radius: 6px;
  border: 2px solid;
  min-width: 120px;
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
}
.node-header {
  white-space: nowrap;
}
.node-start { background: #67c23a; border-color: #529b2e; }
.node-end { background: #f56c6c; border-color: #c45656; }
.node-api { background: #409eff; border-color: #337ecc; }
.node-script { background: #e6a23c; border-color: #b88230; }
.node-mapping { background: #909399; border-color: #73767a; }
.node-output { background: #9b59b6; border-color: #7d3c98; }
</style>

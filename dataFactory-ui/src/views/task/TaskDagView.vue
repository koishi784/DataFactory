<template>
  <PageContainer title="DAG 任务编排">
    <el-alert title="DAG 画布编排功能" type="info" show-icon :closable="false" description="此页面需要使用 vue-flow 等拖拽库实现可视化 DAG 编排，支持 Start/API/Script/Mapping/Output/End 节点类型的拖拽、连线和参数配置。" />
  </PageContainer>
</template>

<script setup lang="ts">
import PageContainer from '@/components/PageContainer.vue'
</script>

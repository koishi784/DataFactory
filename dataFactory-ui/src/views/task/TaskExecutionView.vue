<template>
  <PageContainer title="任务执行历史">
    <el-alert title="任务执行历史" type="info" show-icon :closable="false">
      <p>展示任务每次执行的记录，包括执行状态、开始/结束时间、耗时和错误信息。</p>
    </el-alert>
  </PageContainer>
</template>

<script setup lang="ts">
import PageContainer from '@/components/PageContainer.vue'
</script>

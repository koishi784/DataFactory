<template>
  <PageContainer title="接口分类管理">
    <el-row :gutter="16">
      <el-col :span="8">
        <CategoryTree :data="[]" title="接口分类" @node-click="handleNodeClick" />
      </el-col>
      <el-col :span="16">
        <el-alert title="请选择左侧分类进行管理" type="info" show-icon :closable="false" />
      </el-col>
    </el-row>
  </PageContainer>
</template>

<script setup lang="ts">
import PageContainer from '@/components/PageContainer.vue'
import CategoryTree from '@/components/CategoryTree.vue'

function handleNodeClick(node: any) {
  console.log('选中分类:', node)
}
</script>

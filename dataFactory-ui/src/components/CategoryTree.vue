<template>
  <div class="category-tree">
    <div class="tree-header">
      <span class="tree-title">{{ title }}</span>
      <el-button v-if="showAdd" type="primary" link size="small" @click="$emit('add')">
        新增分类
      </el-button>
    </div>
    <el-tree
      :data="data"
      :props="defaultProps"
      node-key="id"
      default-expand-all
      highlight-current
      @node-click="(node) => $emit('node-click', node)"
    >
      <template #default="{ node, data }">
        <span class="tree-node">
          <span>{{ node.label }}</span>
          <span class="tree-node-actions">
            <el-button link type="primary" size="small" @click.stop="$emit('edit', data)">编辑</el-button>
            <el-button link type="danger" size="small" @click.stop="$emit('delete', data)">删除</el-button>
          </span>
        </span>
      </template>
    </el-tree>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  data: any[]
  title?: string
  showAdd?: boolean
}>()

defineEmits<{
  'node-click': [node: any]
  add: []
  edit: [data: any]
  delete: [data: any]
}>()

const defaultProps = {
  children: 'children',
  label: 'name',
}
</script>

<style scoped lang="scss">
.category-tree {
  .tree-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 8px;
    margin-bottom: 8px;
    border-bottom: 1px solid #ebeef5;

    .tree-title {
      font-size: 14px;
      font-weight: 600;
    }
  }

  .tree-node {
    flex: 1;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 14px;

    .tree-node-actions {
      display: none;
    }
  }

  .el-tree-node__content:hover .tree-node-actions {
    display: inline-flex;
  }
}
</style>

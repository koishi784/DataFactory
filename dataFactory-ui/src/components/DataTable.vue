<template>
  <div class="data-table">
    <el-table
      :data="data"
      v-loading="loading"
      stripe
      border
      style="width: 100%"
      @selection-change="$emit('selection-change', $event)"
    >
      <el-table-column v-if="selectable" type="selection" width="50" />
      <el-table-column type="index" label="序号" width="60" />
      <slot />
      <template #empty>
        <el-empty description="暂无数据" />
      </template>
    </el-table>

    <div v-if="showPagination" class="pagination-wrapper">
      <el-pagination
        :current-page="currentPage || 1"
        :page-size="pageSize || 20"
        :page-sizes="[10, 20, 50, 100]"
        :total="total || 0"
        layout="total, sizes, prev, pager, next, jumper"
        @update:current-page="$emit('page-change', $event)"
        @update:page-size="$emit('size-change', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  data: any[]
  loading?: boolean
  total?: number
  currentPage?: number
  pageSize?: number
  selectable?: boolean
  showPagination?: boolean
}>()

defineEmits<{
  'selection-change': [selection: any[]]
  'page-change': [page: number]
  'size-change': [size: number]
}>()
</script>

<style scoped lang="scss">
.data-table {
  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    padding-top: 16px;
  }
}
</style>

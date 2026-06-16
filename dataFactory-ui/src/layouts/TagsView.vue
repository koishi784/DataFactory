<template>
  <div class="tags-view" v-if="appStore.visitedViews.length > 0">
    <el-tabs
      :model-value="route.path"
      type="card"
      closable
      @tab-remove="removeTag"
      @tab-click="switchTag"
    >
      <el-tab-pane
        v-for="view in appStore.visitedViews"
        :key="view"
        :label="getLabel(view)"
        :name="view"
      />
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import router from '@/router'

const route = useRoute()
const appStore = useAppStore()

function getLabel(path: string): string {
  const resolved = router.resolve(path)
  return (resolved.meta?.title as string) || path
}

function removeTag(path: string) {
  if (path === '/dashboard') return
  appStore.removeVisitedView(path)
  if (route.path === path) {
    const views = appStore.visitedViews
    router.push(views[views.length - 1] || '/dashboard')
  }
}

function switchTag(pane: { paneName: string }) {
  router.push(pane.paneName)
}
</script>

<style scoped lang="scss">
.tags-view {
  position: relative;
  z-index: 1;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 8px;

  .el-tabs {
    :deep(.el-tabs__header) {
      margin: 0;
      border-bottom: none;
    }
  }
}
</style>

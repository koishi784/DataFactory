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
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { routes } from '@/router/routes'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

function getLabel(path: string): string {
  for (const r of routes) {
    if (r.children) {
      for (const c of r.children) {
        if (c.children) {
          for (const cc of c.children) {
            if (cc.path && `${r.path}/${c.path}/${cc.path}`.replace(/\/\//g, '/') === path) {
              return (cc.meta?.title as string) || path
            }
          }
        } else {
          const fullPath = (r.path + '/' + c.path).replace(/\/\//g, '/')
          if (fullPath === path) {
            return (c.meta?.title as string) || path
          }
        }
      }
    }
  }
  return path
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

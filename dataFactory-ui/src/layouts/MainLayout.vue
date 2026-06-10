<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <Sidebar :collapsed="appStore.sidebarCollapsed" />

    <!-- 右侧内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <Navbar @toggle-sidebar="appStore.toggleSidebar()" />

      <!-- 标签页 -->
      <TagsView />

      <!-- 主内容 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'
import TagsView from './TagsView.vue'

const router = useRouter()
const appStore = useAppStore()

// 路由变化时添加标签
router.afterEach((to) => {
  if (to.meta?.title && !to.meta?.hidden) {
    appStore.addVisitedView(to.path)
  }
})
</script>

<style scoped lang="scss">
.main-layout {
  height: 100vh;
}

.main-content {
  background: #f0f2f5;
  padding: 16px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

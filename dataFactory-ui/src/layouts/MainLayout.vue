<template>
  <el-container class="main-layout">
    <!-- 背景网格装饰 -->
    <div class="bg-grid"></div>

    <!-- 侧边栏 -->
    <Sidebar :collapsed="appStore.sidebarCollapsed" />

    <!-- 右侧内容区 -->
    <el-container class="right-container">
      <!-- 顶部导航 -->
      <Navbar @toggle-sidebar="appStore.toggleSidebar()" />

      <!-- 标签页 -->
      <!-- <TagsView /> -->

      <!-- 主内容 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <!-- AI 助手 -->
    <AiAssistant />
  </el-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import Sidebar from './Sidebar.vue'
import Navbar from './Navbar.vue'
import AiAssistant from '@/components/AiAssistant.vue'
// import TagsView from './TagsView.vue'

const router = useRouter()
const appStore = useAppStore()

// 路由变化时添加标签
// router.afterEach((to) => {
//   if (to.meta?.title && !to.meta?.hidden) {
//     appStore.addVisitedView(to.path)
//   }
// })
</script>

<style scoped lang="scss">
.main-layout {
  position: relative;
  height: 100vh;
  background: #f5f7fa;
  overflow: hidden;
}

// 背景深紫色网格
.bg-grid {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(rgba(49, 46, 129, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(49, 46, 129, 0.06) 1px, transparent 1px);
  background-size: 60px 60px;
}

.right-container {
  flex: 1;
  flex-direction: column;
  overflow: hidden;
}

.main-content {
  position: relative;
  z-index: 1;
  background: transparent;
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

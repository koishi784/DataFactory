<template>
  <el-aside :width="collapsed ? '64px' : '220px'" class="sidebar">
    <div class="logo" @click="router.push('/dashboard')">
      <el-icon :size="24"><DataAnalysis /></el-icon>
      <span v-show="!collapsed" class="logo-text">数据工厂</span>
    </div>

    <el-menu
      :default-active="route.path"
      :collapse="collapsed"
      :router="true"
      background-color="transparent"
      text-color="#ffffffb3"
      active-text-color="#fff"
    >
      <template v-for="menu in flatMenu" :key="menu.path">
        <!-- 有子菜单 -->
        <el-sub-menu v-if="menu.children?.length && showMenu(menu)" :index="menu.path">
          <template #title>
            <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
            <span>{{ menu.title }}</span>
          </template>
          <el-menu-item
            v-for="child in menu.children"
            :key="child.path"
            :index="`${menu.path}/${child.path}`"
            v-show="showMenu(child)"
          >
            <span>{{ child.title }}</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 单菜单项 -->
        <el-menu-item v-else-if="showMenu(menu)" :index="menu.path">
          <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
          <template #title>{{ menu.title }}</template>
        </el-menu-item>
      </template>
    </el-menu>
  </el-aside>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { businessRoutes, type MenuItem } from '@/router/routes'

const props = defineProps<{ collapsed: boolean }>()
const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 扁平菜单列表，去掉分组包裹
const flatMenu = computed<MenuItem[]>(() => {
  return businessRoutes
    .filter((r) => !r.meta?.hidden)
    .map((r) => ({
      path: r.path || '',
      title: (r.meta?.title as string) || '',
      icon: r.meta?.icon as string,
      permission: r.meta?.permission as string,
      children: (r.children || [])
        .filter((c) => !c.meta?.hidden)
        .map((c) => ({
          path: c.path?.replace(/^\//, '') || '',
          title: (c.meta?.title as string) || '',
          permission: c.meta?.permission as string,
        })),
    }))
})

function showMenu(menu: MenuItem): boolean {
  if (!menu.permission) return true
  return authStore.hasPermission(menu.permission)
}
</script>

<style scoped lang="scss">
.sidebar {
  position: relative;
  background: linear-gradient(180deg, #1e1b4b, #312e81);
  transition: width 0.3s;
  overflow: hidden;

  // 网格线覆盖层
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    pointer-events: none;
    background-image:
      linear-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px);
    background-size: 60px 60px;
  }

  .logo {
    position: relative;
    z-index: 1;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    cursor: pointer;
    gap: 8px;

    .logo-text {
      font-size: 18px;
      font-weight: bold;
      white-space: nowrap;
    }
  }

  .el-menu {
    position: relative;
    z-index: 1;
    border-right: none;
  }
}
</style>

<!-- 侧边栏折叠时 el-menu 弹出框的背景修复（弹出框渲染在 body 层，需全局样式） -->
<style lang="scss">
// 子菜单弹出框的外层 el-popper — 透传，让内部 .el-menu--popup 显示
body .el-popper:has(> .el-menu--popup) {
  --el-bg-color-overlay: transparent !important;
  background: transparent !important;
  background-color: transparent !important;
  border: none !important;
  box-shadow: none !important;
  padding: 0 !important;
}

// 侧边栏折叠时 el-menu-item 的 tooltip — 匹配 is-dark 的纯文本 tooltip
body .el-popper.is-dark[role="tooltip"]:not(:has(> .el-menu--popup)) {
  background: #1e1b4b !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 8px !important;
  padding: 6px 14px !important;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4) !important;
  color: #fff !important;
  font-size: 13px !important;

  .el-popper__arrow::before {
    background: #1e1b4b !important;
  }
}

// 子菜单弹出框 — 卡片+三角形箭头
.el-menu--popup {
  background: #1e1b4b !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  border-radius: 8px !important;
  padding: 4px 0 !important;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.4) !important;
  min-width: 120px !important;

  // 左侧三角形箭头
  &::before {
    content: '';
    position: absolute;
    top: 16px;
    left: -6px;
    width: 10px;
    height: 10px;
    background: #1e1b4b;
    border-left: 1px solid rgba(255, 255, 255, 0.1);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    transform: rotate(45deg);
  }

  .el-menu-item {
    height: 28px !important;
    line-height: 28px !important;
    color: #ffffffb3 !important;
    background: transparent !important;

    &:hover {
      background: rgba(255, 255, 255, 0.08) !important;
      color: #fff !important;
    }

    &.is-active {
      color: #fff !important;
      background: rgba(255, 255, 255, 0.12) !important;
    }
  }
}
</style>

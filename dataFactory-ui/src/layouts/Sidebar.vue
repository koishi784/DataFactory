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
      background-color="#001529"
      text-color="#ffffffb3"
      active-text-color="#fff"
    >
      <template v-for="menu in menuList" :key="menu.path">
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

// 从路由表生成菜单列表
const menuList = computed<MenuItem[]>(() => {
  return businessRoutes
    .filter((r) => !r.meta?.hidden)
    .map((r) => ({
      path: r.path?.replace(/^\//, '') || '',
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
  background: #001529;
  transition: width 0.3s;
  overflow: hidden;

  .logo {
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
    border-right: none;
  }
}
</style>

<template>
  <el-header class="navbar">
    <div class="left">
      <el-icon class="collapse-btn" :size="20" @click="$emit('toggle-sidebar')">
        <Fold v-if="!appStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
      <span class="page-title">{{ route.meta?.title || '首页' }}</span>
    </div>

    <div class="right">
      <el-dropdown trigger="click" @command="handleCommand">
        <span class="user-info">
          <el-avatar :size="30" icon="UserFilled" />
          <span class="username">{{ authStore.userInfo?.nickname || authStore.userInfo?.username }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人信息</el-dropdown-item>
            <el-dropdown-item command="password">修改密码</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'

defineEmits<{ 'toggle-sidebar': [] }>()

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()

function handleCommand(command: string) {
  switch (command) {
    case 'logout':
      authStore.logout()
      router.push('/login')
      break
    case 'profile':
      break
    case 'password':
      break
  }
}
</script>

<style scoped lang="scss">
.navbar {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 50px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 16px;

  .left {
    display: flex;
    align-items: center;
    gap: 12px;

    .collapse-btn {
      cursor: pointer;
      color: #606266;
    }

    .page-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;

      .username {
        font-size: 14px;
        color: #303133;
      }
    }
  }
}
</style>

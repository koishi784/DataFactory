<template>
  <PageContainer>
    <el-row :gutter="16">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="welcome-card" style="margin-top: 16px">
      <h3>欢迎使用数据工厂大数据平台</h3>
      <p>通过左侧菜单导航进入各功能模块进行数据管理。</p>
    </el-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import PageContainer from '@/components/PageContainer.vue'

const authStore = useAuthStore()

const stats = [
  { label: '接口数量', value: '-' },
  { label: '数据库连接', value: '-' },
  { label: '数据标准', value: '-' },
  { label: '任务数量', value: '-' },
]
</script>

<style scoped lang="scss">
.stat-card {
  text-align: center;

  .stat-value {
    font-size: 32px;
    font-weight: bold;
    color: #409eff;
  }

  .stat-label {
    font-size: 14px;
    color: #909399;
    margin-top: 8px;
  }
}

.welcome-card {
  h3 {
    margin-bottom: 8px;
    font-size: 16px;
  }

  p {
    color: #606266;
    font-size: 14px;
  }
}
</style>

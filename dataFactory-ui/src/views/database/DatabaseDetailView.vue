<template>
  <PageContainer title="数据库连接详情">
    <el-descriptions v-if="detail" :column="2" border>
      <el-descriptions-item label="连接名称">{{ detail.connectionName }}</el-descriptions-item>
      <el-descriptions-item label="数据库类型">{{ detail.dbType }}</el-descriptions-item>
      <el-descriptions-item label="主机地址">{{ detail.host }}</el-descriptions-item>
      <el-descriptions-item label="端口">{{ detail.port }}</el-descriptions-item>
      <el-descriptions-item label="数据库名称">{{ detail.databaseName }}</el-descriptions-item>
      <el-descriptions-item label="用户名">{{ detail.username }}</el-descriptions-item>
      <el-descriptions-item label="状态"><StatusTag :status="detail.status" /></el-descriptions-item>
      <el-descriptions-item label="最近测试时间">{{ detail.lastTestTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="最近测试结果">
        <template v-if="detail.lastTestResult !== undefined">
          <el-tag :type="detail.lastTestResult === 1 ? 'success' : 'danger'" size="small">
            {{ detail.lastTestResult === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item label="JDBC 参数" :span="2">
        <code>{{ detail.jdbcParams || '-' }}</code>
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
      <el-descriptions-item label="更新时间">{{ detail.updateTime }}</el-descriptions-item>
      <el-descriptions-item label="描述" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
    </el-descriptions>
    <el-button style="margin-top: 16px" @click="router.back()">返回</el-button>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '@/components/PageContainer.vue'
import StatusTag from '@/components/StatusTag.vue'
import { getDatabaseDetail } from '@/api/database'
import type { DatabaseConnection } from '@/types/database'

const route = useRoute()
const router = useRouter()
const detail = ref<DatabaseConnection | null>(null)

onMounted(async () => {
  detail.value = await getDatabaseDetail(Number(route.params.id)) as DatabaseConnection || null
})
</script>

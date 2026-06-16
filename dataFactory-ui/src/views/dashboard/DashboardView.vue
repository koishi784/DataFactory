<template>
  <PageContainer>
    <!-- 统计卡片 -->
    <div class="stats-grid">
      <el-card v-for="stat in stats" :key="stat.label" shadow="hover" class="stat-card" :class="{ 'stat-card--large': stat.label === '任务数量' }">
        <div class="stat-value">{{ stat.value }}</div>
        <div class="stat-label">{{ stat.label }}</div>
        <div v-if="stat.sub" class="stat-sub">{{ stat.sub }}</div>
      </el-card>
    </div>

    <!-- 任务状态分布 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>
            <div class="card-title">最近任务</div>
          </template>
          <el-table :data="recentTasks" stripe style="width: 100%" v-loading="tasksLoading" :header-cell-style="{ fontSize: '15px' }" :cell-style="{ fontSize: '14px' }">
            <el-table-column prop="taskName" label="任务名称" min-width="150" show-overflow-tooltip />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <StatusTag :status="row.status" />
              </template>
            </el-table-column>
            <el-table-column label="调度" width="90">
              <template #default="{ row }">
                <el-tag v-if="row.scheduleType === 'CRON'" size="default">定时</el-tag>
                <el-tag v-else-if="row.scheduleType === 'API'" size="default" type="primary">API</el-tag>
                <el-tag v-else size="default" type="info">手动</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="最近执行" min-width="150">
              <template #default="{ row }">
                <span v-if="row.lastExecuteTime" style="font-size: 14px">{{ row.lastExecuteTime }}</span>
                <span v-else style="color: var(--el-text-color-placeholder)">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link size="default" @click="router.push(`/task/${row.id}/edit`)">编辑</el-button>
                <el-button link size="default" @click="router.push(`/task/${row.id}/dag`)">DAG</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!tasksLoading && recentTasks.length === 0" style="text-align: center; padding: 24px; color: var(--el-text-color-secondary)">
            暂无任务数据
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card shadow="never">
          <template #header>
            <div class="card-title">快速入口</div>
          </template>
          <div class="quick-actions">
            <el-button color="#ecf5ff" style="color: #409eff; border-color: #d9ecff" @click="router.push('/api/create')">新增接口</el-button>
            <el-button color="#ecf5ff" style="color: #409eff; border-color: #d9ecff" @click="router.push('/script/create')">新增脚本</el-button>
            <el-button color="#ecf5ff" style="color: #409eff; border-color: #d9ecff" @click="router.push('/task/create')">新增任务</el-button>
            <el-button color="#ecf5ff" style="color: #409eff; border-color: #d9ecff" @click="router.push('/database/create')">新增数据库</el-button>
            <el-button color="#ecf5ff" style="color: #409eff; border-color: #d9ecff" @click="router.push('/standard/create')">新增数据标准</el-button>
            <el-button color="#ecf5ff" style="color: #409eff; border-color: #d9ecff" @click="router.push('/code-table/create')">新增码表</el-button>
          </div>
        </el-card>

        <el-card shadow="never" style="margin-top: 16px">
          <template #header>
            <div class="card-title">系统信息</div>
          </template>
          <el-descriptions :column="1" size="default" border>
            <el-descriptions-item label="当前用户" label-class-name="desc-label">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '-' }}</el-descriptions-item>
            <el-descriptions-item label="角色" label-class-name="desc-label">{{ (userStore.roles || []).join('、') || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '@/components/PageContainer.vue'
import StatusTag from '@/components/StatusTag.vue'
import { useAuthStore } from '@/stores/auth'
import { getApiList } from '@/api/api'
import { getDatabaseList } from '@/api/database'
import { getDataStandardList } from '@/api/dataStandard'
import { getTaskList } from '@/api/task'
import { getScriptList } from '@/api/script'
import { getCodeTableList } from '@/api/codeTable'
import { getAssetList } from '@/api/asset'

const router = useRouter()
const userStore = useAuthStore()

const stats = reactive([
  { label: '接口数量', value: '-', sub: '' },
  { label: '数据库连接', value: '-', sub: '' },
  { label: '数据标准', value: '-', sub: '' },
  { label: '任务数量', value: '-', sub: '' },
  { label: '脚本数量', value: '-', sub: '' },
  { label: '码表数量', value: '-', sub: '' },
  { label: '资产数量', value: '-', sub: '' },
])

const recentTasks = ref<any[]>([])
const tasksLoading = ref(false)

async function fetchCounts() {
  // 并行获取所有计数
  const results = await Promise.allSettled([
    getApiList({ pageNum: 1, pageSize: 1 }),
    getDatabaseList({ pageNum: 1, pageSize: 1 }),
    getDataStandardList({ pageNum: 1, pageSize: 1 }),
    getTaskList({ pageNum: 1, pageSize: 1 }),
    getScriptList({ pageNum: 1, pageSize: 1 }),
    getCodeTableList({ pageNum: 1, pageSize: 1 }),
    getAssetList({ pageNum: 1, pageSize: 1 }),
  ])

  const totals = results.map(r => (r.status === 'fulfilled' ? (r.value as any)?.total : null))

  // 更新卡片数值
  const labels = ['接口数量', '数据库连接', '数据标准', '任务数量', '脚本数量', '码表数量', '资产数量']
  labels.forEach((label, i) => {
    const s = stats.find(st => st.label === label)
    if (s) s.value = totals[i] != null ? totals[i] : '-'
  })

  // 获取任务状态分布
  try {
    const [draftRes, pubRes, disabledRes] = await Promise.all([
      getTaskList({ pageNum: 1, pageSize: 1, status: 0 }),
      getTaskList({ pageNum: 1, pageSize: 1, status: 1 }),
      getTaskList({ pageNum: 1, pageSize: 1, status: 2 }),
    ])
    const draft = (draftRes as any)?.total ?? 0
    const pub = (pubRes as any)?.total ?? 0
    const disabled = (disabledRes as any)?.total ?? 0
    const taskStat = stats.find(s => s.label === '任务数量')
    if (taskStat) {
      taskStat.sub = `未发布 ${draft} · 已发布 ${pub} · 已停用 ${disabled}`
    }
  } catch { /* ignore */ }
}

async function fetchRecentTasks() {
  tasksLoading.value = true
  try {
    const res = await getTaskList({ pageNum: 1, pageSize: 5, sortOrder: 'desc' }) as any
    recentTasks.value = res?.records || []
  } catch {
    recentTasks.value = []
  } finally {
    tasksLoading.value = false
  }
}

onMounted(() => {
  fetchCounts()
  fetchRecentTasks()
})
</script>

<style scoped lang="scss">
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  text-align: center;
  min-height: 130px;
  display: flex;
  align-items: center;
  justify-content: center;

  :deep(.el-card__body) {
    width: 100%;
  }

  .stat-value {
    font-size: 38px;
    font-weight: bold;
    color: #409eff;
  }

  .stat-label {
    font-size: 16px;
    color: #909399;
    margin-top: 10px;
  }

  .stat-sub {
    font-size: 13px;
    color: #c0c4cc;
    margin-top: 6px;
  }
}

.stat-card--large {
  grid-row: span 2;
  min-height: 276px;

  :deep(.el-card__body) {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }
}

.card-title {
  font-weight: 600;
  font-size: 17px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

:deep(.desc-label) {
  font-size: 14px;
  font-weight: 500;
}
</style>

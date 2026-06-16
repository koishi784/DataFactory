<template>
  <PageContainer title="数据库连接管理">
    <el-form :model="searchForm" inline class="search-bar" @keyup.enter="handleSearch">
      <el-form-item label="关键字">
        <el-input v-model="searchForm.keyword" placeholder="连接名称/数据库名" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="数据库类型">
        <el-select v-model="searchForm.dbType" placeholder="数据库类型" clearable style="width: 140px">
          <el-option label="MySQL" value="MySQL" />
          <el-option label="PostgreSQL" value="PostgreSQL" />
          <el-option label="Oracle" value="Oracle" />
          <el-option label="SQLServer" value="SQLServer" />
          <el-option label="Hive" value="Hive" />
          <el-option label="ClickHouse" value="ClickHouse" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 110px">
          <el-option label="未发布" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="已停用" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    <div class="toolbar">
      <BatchActions :has-selection="selectedIds.length > 0" :show-category="false" @batch-publish="handleBatchPublish" @batch-disable="handleBatchDisable">
        <template #extra>
          <el-button type="primary" @click="openTypeDialog">
            <el-icon><Plus /></el-icon>新增数据库
          </el-button>
        </template>
      </BatchActions>
    </div>
    <DataTable
      :data="list" :loading="loading" :total="total"
      :current-page="pagination.pageNum" :page-size="pagination.pageSize"
      selectable show-pagination
      @selection-change="handleSelectionChange"
      @page-change="handlePageChange" @size-change="handleSizeChange"
    >
      <el-table-column prop="connectionName" label="连接名称" show-overflow-tooltip width="200" />
      <el-table-column prop="dbType" label="数据库类型" width="120" />
      <el-table-column prop="host" label="主机地址" show-overflow-tooltip />
      <el-table-column prop="databaseName" label="数据库名称" width="140" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }"><StatusTag :status="row.status" /></template>
      </el-table-column>
      <el-table-column prop="updateTime" label="更新时间" width="170" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link size="small" @click="router.push(`/database/${row.id}/detail`)">详情</el-button>
          <el-button link size="small" @click="openTestDialog(row)">测试</el-button>
          <StatusAction :status="row.status" @edit="router.push(`/database/${row.id}/edit`)" @publish="handlePublish(row.id, row.connectionName)" @disable="handleDisable(row.id, row.connectionName)" @delete="handleDelete(row.id, row.connectionName)" />
        </template>
      </el-table-column>
    </DataTable>

    <!-- 测试连接弹框 -->
    <el-dialog v-model="testDialogVisible" :title="`测试连接 — ${testingConn?.connectionName || ''}`" width="560px" top="30vh" @close="resetTestDialog">
      <div v-if="testLoading" style="text-align: center; padding: 40px">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p style="margin-top: 12px">正在测试连接...</p>
      </div>
      <template v-else-if="testResult">
        <el-descriptions :column="1" size="small" border>
          <el-descriptions-item label="测试结果">
            <el-tag :type="testResult.success ? 'success' : 'danger'" size="small">
              {{ testResult.success ? '连接成功' : '连接失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="响应耗时">{{ testResult.responseTime ? `${testResult.responseTime}ms` : '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="testResult.errorMessage" label="错误信息">
            <span style="color: var(--el-color-danger)">{{ testResult.errorMessage }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="testDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 选择数据库类型弹框 -->
    <el-dialog v-model="typeDialogVisible" title="选择数据库类型" width="640px" top="20vh">
      <el-row :gutter="16">
        <el-col v-for="db in dbTypes" :key="db.value" :span="6" style="margin-bottom: 16px">
          <el-card shadow="hover" :class="['db-type-card', { 'is-disabled': !db.available }]" @click="selectDbType(db)">
            <div style="text-align: center; padding: 12px 0">
              <div style="font-size: 14px; font-weight: 600; margin-top: 4px">{{ db.label }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, Plus } from '@element-plus/icons-vue'
import PageContainer from '@/components/PageContainer.vue'
import DataTable from '@/components/DataTable.vue'
import StatusTag from '@/components/StatusTag.vue'
import StatusAction from '@/components/StatusAction.vue'
import BatchActions from '@/components/BatchActions.vue'
import { useCrud } from '@/composables/useCrud'
import { useStatusActions } from '@/composables/useStatusActions'
import { getDatabaseList, publishDatabase, disableDatabase, deleteDatabase, batchPublishDatabase, batchDisableDatabase, testDatabase } from '@/api/database'
import type { DatabaseConnection } from '@/types/database'

const router = useRouter()
const selectedIds = ref<number[]>([])

// 数据库类型选择
const typeDialogVisible = ref(false)
const dbTypes = [
  { label: 'MySQL', value: 'MySQL', available: true },
  { label: 'Oracle', value: 'Oracle', available: false },
  { label: 'Sql Server', value: 'SqlServer', available: false },
  { label: 'DB2', value: 'DB2', available: false },
  { label: 'DM DBMS', value: 'DM_DBMS', available: false },
  { label: 'Essbase', value: 'Essbase', available: false },
  { label: 'GBase', value: 'GBase', available: false },
  { label: 'GreenPlum', value: 'GreenPlum', available: false },
  { label: 'KingBaseES', value: 'KingBaseES', available: false },
  { label: 'Netezza', value: 'Netezza', available: false },
  { label: 'Sybase', value: 'Sybase', available: false },
  { label: 'PetaBase', value: 'PetaBase', available: false },
  { label: 'TeraData', value: 'TeraData', available: false },
  { label: 'Hive', value: 'Hive', available: false },
  { label: '其他', value: 'Other', available: false },
]

function openTypeDialog() {
  typeDialogVisible.value = true
}

function selectDbType(db: { label: string; value: string; available: boolean }) {
  if (db.available) {
    typeDialogVisible.value = false
    router.push({ path: '/database/create', query: { dbType: db.value } })
  } else {
    ElMessage.info('该功能尚在开发中')
  }
}

const { list, loading, total, pagination, searchForm, handleSearch, handleReset, handlePageChange, handleSizeChange, fetchData } = useCrud<DatabaseConnection>({
  apiGetList: (params) => getDatabaseList({ ...params }),
  defaultForm: { keyword: '', dbType: '', status: null as number | null },
})
const { handlePublish, handleDisable, handleDelete, handleBatchPublish, handleBatchDisable } = useStatusActions({
  apiPublish: publishDatabase, apiDisable: disableDatabase, apiDelete: deleteDatabase,
  batchPublish: batchPublishDatabase, batchDisable: batchDisableDatabase,
  fetchData, getSelectedIds: () => selectedIds.value, getSelectedStatuses: () => [],
})

// 测试连接弹框
const testDialogVisible = ref(false)
const testLoading = ref(false)
const testingConn = ref<DatabaseConnection | null>(null)
const testResult = ref<{ success: boolean; responseTime?: number; errorMessage?: string } | null>(null)

function openTestDialog(row: DatabaseConnection) {
  testingConn.value = row
  testResult.value = null
  testDialogVisible.value = true
  execTest(row.id)
}

function resetTestDialog() {
  testLoading.value = false
  testingConn.value = null
  testResult.value = null
}

async function execTest(id: number) {
  testLoading.value = true
  testResult.value = null
  try {
    const res = await testDatabase(id)
    testResult.value = res as any
  } catch (e: any) {
    testResult.value = { success: false, errorMessage: e?.message || '连接测试失败' }
  } finally {
    testLoading.value = false
  }
}

function handleSelectionChange(selection: any[]) {
  selectedIds.value = selection.map((s: any) => s.id)
}

fetchData()
</script>

<style scoped lang="scss">
.search-bar {
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
}
.db-type-card {
  cursor: pointer;
  transition: all 0.2s;
  &.is-disabled {
    cursor: pointer;
    &:hover {
      border-color: var(--el-color-primary);
      box-shadow: var(--el-box-shadow-light);
    }
  }
}
</style>

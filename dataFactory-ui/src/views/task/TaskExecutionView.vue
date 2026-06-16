<template>
  <PageContainer :title="`任务执行历史`">
    <template #actions>
      <el-button type="primary" @click="handleExecute">手动执行</el-button>
    </template>
    <el-form :model="searchForm" inline class="search-bar" @keyup.enter="handleSearch">
      <el-form-item label="执行状态">
        <el-select v-model="searchForm.status" placeholder="执行状态" clearable style="width: 120px">
          <el-option label="等待" :value="0" />
          <el-option label="执行中" :value="1" />
          <el-option label="成功" :value="2" />
          <el-option label="失败" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始日期">
        <el-date-picker v-model="searchForm.startDate" type="date" placeholder="开始日期" value-format="YYYY-MM-DD" style="width: 140px" />
      </el-form-item>
      <el-form-item label="结束日期">
        <el-date-picker v-model="searchForm.endDate" type="date" placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 140px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
    <DataTable
      :data="list" :loading="loading" :total="total"
      :current-page="pagination.pageNum" :page-size="pagination.pageSize"
      show-pagination
      @page-change="handlePageChange" @size-change="handleSizeChange"
    >
      <el-table-column prop="status" label="执行状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="info" size="small">等待</el-tag>
          <el-tag v-else-if="row.status === 1" type="warning" size="small">执行中</el-tag>
          <el-tag v-else-if="row.status === 2" type="success" size="small">成功</el-tag>
          <el-tag v-else-if="row.status === 3" type="danger" size="small">失败</el-tag>
          <el-tag v-else-if="row.status === 4" type="info" size="small">已取消</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="endTime" label="结束时间" width="170" />
      <el-table-column prop="duration" label="耗时" width="100">
        <template #default="{ row }">{{ row.duration ? `${row.duration}ms` : '-' }}</template>
      </el-table-column>
      <el-table-column prop="triggerType" label="触发方式" width="100" />
      <el-table-column prop="triggerBy" label="触发人" width="120" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" link type="danger" size="small" @click="handleCancel(row.executionId)">
            停止
          </el-button>
        </template>
      </el-table-column>
    </DataTable>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import DataTable from '@/components/DataTable.vue'
import { useCrud } from '@/composables/useCrud'
import { getTaskExecutions, executeTask, cancelExecution } from '@/api/task'
import type { TaskExecution } from '@/types/task'

const route = useRoute()
const router = useRouter()
const taskId = Number(route.params.id)

const searchForm = ref({ status: null as number | null, startDate: '', endDate: '' })
const searchFields = []

const { list, loading, total, pagination, handleSearch, handleReset, handlePageChange, handleSizeChange, fetchData } = useCrud<TaskExecution>({
  apiGetList: (params) => getTaskExecutions(taskId, { ...params, ...searchForm.value }),
})

async function handleExecute() {
  try {
    await ElMessageBox.confirm('确定手动执行该任务吗？', '执行确认', { type: 'info' })
    await executeTask(taskId)
    ElMessage.success('任务已触发执行')
    fetchData()
  } catch { /* cancel */ }
}

async function handleCancel(executionId: number) {
  try {
    await ElMessageBox.confirm('确定停止该执行吗？', '停止确认', { type: 'warning' })
    await cancelExecution(taskId, executionId)
    ElMessage.success('已停止')
    fetchData()
  } catch { /* cancel */ }
}

onMounted(() => { fetchData() })
</script>

<style scoped lang="scss">
.search-bar {
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}
</style>

<template>
  <PageContainer title="DAG 任务编排">
    <el-steps :active="2" align-center style="margin-bottom: 16px">
      <el-step title="基本信息" description="任务名称与分类" />
      <el-step title="任务配置" description="DAG 流程编排" />
      <el-step title="触发设置" description="调度方式配置" />
    </el-steps>

    <div class="dag-layout">
      <!-- 左侧节点面板 -->
      <div class="dag-palette">
        <h4 class="palette-title">节点类型</h4>
        <div
          v-for="nt in nodeTypes"
          :key="nt.type"
          class="palette-item"
          :class="'palette-' + nt.type"
          draggable="true"
          @dragstart="onDragStart($event, nt.type)"
        >
          <div class="palette-color" :style="{ background: nt.color }" />
          <span>{{ nt.label }}</span>
        </div>
      </div>

      <!-- 中间画布区域 -->
      <div class="dag-canvas-wrapper" ref="canvasWrapperRef">
        <div class="dag-toolbar">
          <el-button size="small" type="primary" @click="handleSave">保存 DAG</el-button>
          <el-button size="small" @click="handleTestRun">测试运行</el-button>
          <el-button size="small" @click="handleLayout">自动排列</el-button>
          <el-button size="small" @click="handleTriggerConfig">触发设置</el-button>
        </div>
        <div class="dag-canvas" v-loading="loadingDag">
          <VueFlow
            id="dag-flow"
            v-model:nodes="nodes"
            v-model:edges="edges"
            :node-types="customNodeTypes"
            :default-viewport="{ x: 0, y: 0, zoom: 1 }"
            :min-zoom="0.2"
            :max-zoom="3"
            fit-view-on-init
            @drop="onDrop"
            @dragover.prevent
            @dragenter.prevent
            @node-double-click="onNodeDoubleClick"
          >
            <Background :gap="20" />
            <Controls />
          </VueFlow>
        </div>
      </div>
    </div>

    <!-- 节点配置弹框 -->
    <el-dialog
      v-model="configDialogVisible"
      :title="configNodeTitle"
      width="600px"
      top="5vh"
      :close-on-click-modal="false"
    >
      <template v-if="configNodeType === 'API'">
        <el-form :model="apiConfig" label-width="120px">
          <el-form-item label="注册接口" required>
            <el-select v-model="apiConfig.apiId" filterable placeholder="请选择注册接口" style="width: 100%">
              <el-option v-for="a in apiList" :key="a.id" :label="a.apiName" :value="a.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="超时(ms)">
            <el-input-number v-model="apiConfig.timeout" :min="1" :max="1800000" style="width: 100%" />
          </el-form-item>
          <el-form-item label="重试次数">
            <el-input-number v-model="apiConfig.retryCount" :min="0" :max="5" style="width: 100%" />
          </el-form-item>
        </el-form>
      </template>
      <template v-else-if="configNodeType === 'SCRIPT'">
        <el-form :model="scriptConfig" label-width="120px">
          <el-form-item label="关联脚本" required>
            <el-select v-model="scriptConfig.scriptId" filterable placeholder="请选择脚本" style="width: 100%">
              <el-option v-for="s in scriptList" :key="s.id" :label="s.scriptName" :value="s.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="脚本版本">
            <el-input v-model="scriptConfig.scriptVersion" placeholder="不传则使用最新版本" />
          </el-form-item>
          <el-form-item label="数据源">
            <el-select v-model="scriptConfig.dataSourceId" filterable clearable placeholder="选填" style="width: 100%">
              <el-option v-for="d in dbList" :key="d.id" :label="d.connectionName" :value="d.id" />
            </el-select>
          </el-form-item>
        </el-form>
      </template>
      <template v-else-if="configNodeType === 'MAPPING'">
        <p style="margin-bottom: 8px">字段映射列表：</p>
        <div v-for="(m, i) in mappingConfig.mappings" :key="i" style="display: flex; gap: 8px; margin-bottom: 8px; align-items: center">
          <el-input v-model="m.sourceField" placeholder="源字段" size="small" style="width: 140px" />
          <span>→</span>
          <el-input v-model="m.targetField" placeholder="目标字段" size="small" style="width: 140px" />
          <el-input v-model="m.transformRule" placeholder="转换规则" size="small" style="width: 120px" />
          <el-button size="small" type="danger" link @click="mappingConfig.mappings.splice(i, 1)">删除</el-button>
        </div>
        <el-button size="small" @click="mappingConfig.mappings.push({ sourceField: '', targetField: '', transformRule: '' })">+ 新增映射</el-button>
      </template>
      <template v-else-if="configNodeType === 'OUTPUT'">
        <el-form :model="outputConfig" label-width="140px">
          <el-form-item label="输出类型" required>
            <el-select v-model="outputConfig.outputType" style="width: 100%">
              <el-option label="数据库" value="DATABASE" />
              <el-option label="文件" value="FILE" />
              <el-option label="API推送" value="API_PUSH" />
            </el-select>
          </el-form-item>
          <el-form-item label="目标数据源" required>
            <el-select v-model="outputConfig.targetDataSourceId" filterable placeholder="选择数据源" style="width: 100%">
              <el-option v-for="d in dbList" :key="d.id" :label="d.connectionName" :value="d.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="目标表名" required>
            <el-input v-model="outputConfig.targetTable" />
          </el-form-item>
          <el-form-item label="写入模式" required>
            <el-select v-model="outputConfig.writeMode" style="width: 100%">
              <el-option label="追加" value="INSERT" />
              <el-option label="存在更新" value="UPSERT" />
              <el-option label="覆盖" value="OVERWRITE" />
              <el-option label="追加(APPEND)" value="APPEND" />
            </el-select>
          </el-form-item>
        </el-form>
      </template>
      <template v-else>
        <el-alert title="该节点无需额外配置" type="info" show-icon :closable="false" />
      </template>
      <template #footer>
        <el-button @click="configDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="saveNodeConfig">确定</el-button>
      </template>
    </el-dialog>

    <!-- 测试运行结果弹框 -->
    <el-dialog v-model="testDialogVisible" title="测试运行结果" width="700px" top="5vh">
      <div v-if="testLoading" style="text-align: center; padding: 40px">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <p style="margin-top: 12px">正在测试运行...</p>
      </div>
      <template v-else-if="testResult">
        <el-descriptions :column="2" size="small" border style="margin-bottom: 12px">
          <el-descriptions-item label="执行状态">
            <el-tag v-if="testResult.status === 2" type="success" size="small">成功</el-tag>
            <el-tag v-else-if="testResult.status === 3" type="danger" size="small">失败</el-tag>
            <el-tag v-else type="info" size="small">{{ testResult.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总耗时">{{ testResult.totalDuration ? `${testResult.totalDuration}ms` : '-' }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="testResult.nodeResults?.length">
          <p style="font-weight: 600; margin-bottom: 8px">节点执行详情：</p>
          <el-table :data="testResult.nodeResults" size="small" border>
            <el-table-column prop="nodeName" label="节点" width="120" />
            <el-table-column prop="nodeType" label="类型" width="80" />
            <el-table-column prop="status" label="状态" width="70">
              <template #default="{ row }">
                <el-tag v-if="row.status === 2" type="success" size="small">成功</el-tag>
                <el-tag v-else-if="row.status === 3" type="danger" size="small">失败</el-tag>
                <el-tag v-else size="small">跳过</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="duration" label="耗时" width="80">
              <template #default="{ row }">{{ row.duration }}ms</template>
            </el-table-column>
            <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip />
          </el-table>
        </div>
      </template>
      <template #footer>
        <el-button @click="testDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, markRaw, onMounted, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import { updateTaskDagConfig, testRunTask, getTaskDetail } from '@/api/task'
import { getApiList } from '@/api/api'
import { getScriptList } from '@/api/script'
import { getDatabaseList } from '@/api/database'
import DagBaseNode from './dag-nodes/DagBaseNode.vue'
import type { TaskNode } from '@/types/task'

const route = useRoute()
const router = useRouter()
const taskId = Number(route.params.id)

// ===== 节点类型定义 =====
const nodeTypes = [
  { type: 'START', label: 'Start', color: '#67c23a' },
  { type: 'API', label: 'API', color: '#409eff' },
  { type: 'SCRIPT', label: 'Script', color: '#e6a23c' },
  { type: 'MAPPING', label: 'Mapping', color: '#909399' },
  { type: 'OUTPUT', label: 'Output', color: '#9b59b6' },
  { type: 'END', label: 'End', color: '#f56c6c' },
]

// 注册自定义节点类型
const customNodeTypes = {
  START: markRaw(DagBaseNode),
  API: markRaw(DagBaseNode),
  SCRIPT: markRaw(DagBaseNode),
  MAPPING: markRaw(DagBaseNode),
  OUTPUT: markRaw(DagBaseNode),
  END: markRaw(DagBaseNode),
}

// ===== Vue Flow 状态 =====
const nodes = ref<any[]>([])
const edges = ref<any[]>([])
const loadingDag = ref(false)

const { screenToFlowCoordinate, addNodes, addEdges, fitView } = useVueFlow({ id: 'dag-flow' })

// ===== 拖拽创建节点 =====
const canvasWrapperRef = ref<HTMLDivElement | null>(null)

function onDragStart(event: DragEvent, nodeType: string) {
  event.dataTransfer?.setData('application/dagnode', nodeType)
  event.dataTransfer!.effectAllowed = 'move'
}

function onDrop(event: DragEvent) {
  const type = event.dataTransfer?.getData('application/dagnode')
  if (!type) return

  const position = screenToFlowCoordinate({ x: event.clientX, y: event.clientY })

  const nt = nodeTypes.find(n => n.type === type)
  const label = nt?.label || type
  const colorMap: Record<string, string> = {
    START: '#67c23a', END: '#f56c6c', API: '#409eff', SCRIPT: '#e6a23c', MAPPING: '#909399', OUTPUT: '#9b59b6',
  }

  addNodes({
    id: `node_${Math.random().toString(36).substring(2, 8)}`,
    type,
    position,
    label,
    data: { label, nodeType: type, config: {} },
    style: { background: colorMap[type] || '#409eff', color: '#fff', border: '2px solid ' + (colorMap[type] || '#409eff'), borderRadius: '6px', padding: '8px 16px', fontSize: '13px', fontWeight: 600 },
  })
}

// ===== 节点配置 =====
const configDialogVisible = ref(false)
const configNodeId = ref('')
const configNodeType = ref('')
const configNodeTitle = ref('')

const apiConfig = reactive({ apiId: null as number | null, timeout: 30000, retryCount: 0 })
const scriptConfig = reactive({ scriptId: null as number | null, scriptVersion: '', dataSourceId: null as number | null })
const mappingConfig = reactive({ mappings: [] as { sourceField: string; targetField: string; transformRule: string }[] })
const outputConfig = reactive({ outputType: 'DATABASE', targetDataSourceId: null as number | null, targetTable: '', writeMode: 'INSERT' })

const apiList = ref<any[]>([])
const scriptList = ref<any[]>([])
const dbList = ref<any[]>([])

async function loadLists() {
  try { apiList.value = (await getApiList({ pageNum: 1, pageSize: 100 })) as any } catch {}
  try { scriptList.value = (await getScriptList({ pageNum: 1, pageSize: 100 })) as any } catch {}
  try { dbList.value = (await getDatabaseList({ pageNum: 1, pageSize: 100 })) as any } catch {}
}

function onNodeDoubleClick(event: any) {
  const node = event?.node || event
  if (!node?.id) return
  configNodeId.value = node.id
  configNodeType.value = node.type || node.data?.nodeType
  configNodeTitle.value = `配置 - ${node.data?.label || node.label || node.type}`
  configDialogVisible.value = true
}

function saveNodeConfig() {
  const n = nodes.value.find((n: any) => n.id === configNodeId.value)
  if (!n) { configDialogVisible.value = false; return }
  let cfg: any = {}
  if (configNodeType.value === 'API') {
    cfg = { ...apiConfig }
  } else if (configNodeType.value === 'SCRIPT') {
    cfg = { ...scriptConfig }
  } else if (configNodeType.value === 'MAPPING') {
    cfg = { mappings: [...mappingConfig.mappings] }
  } else if (configNodeType.value === 'OUTPUT') {
    cfg = { ...outputConfig }
  }
  n.data = { ...n.data, config: cfg }
  configDialogVisible.value = false
  ElMessage.success('节点配置已保存')
}

// ===== 保存 DAG =====
async function handleSave() {
  const dagNodes = nodes.value.map((n: any) => ({
    nodeId: n.id,
    nodeName: n.data?.label || n.label || n.type,
    nodeType: n.type,
    positionX: n.position?.x || 0,
    positionY: n.position?.y || 0,
    config: n.data?.config || {},
  }))
  const dagEdges = edges.value.map((e: any) => ({
    edgeId: e.id,
    sourceNodeId: e.source,
    targetNodeId: e.target,
  }))
  try {
    await updateTaskDagConfig(taskId, { nodes: dagNodes, edges: dagEdges })
    ElMessage.success('DAG 配置保存成功')
  } catch { /* handled */ }
}

// ===== 测试运行 =====
const testDialogVisible = ref(false)
const testLoading = ref(false)
const testResult = ref<any>(null)

async function handleTestRun() {
  testDialogVisible.value = true
  testLoading.value = true
  testResult.value = null
  try {
    const res = await testRunTask(taskId) as any
    testResult.value = res
  } catch {
    testResult.value = { status: 3, totalDuration: 0, nodeResults: [] }
  } finally {
    testLoading.value = false
  }
}

// ===== 自动布局 =====
function handleLayout() {
  const cols = 3
  const spacingX = 250
  const spacingY = 180
  nodes.value.forEach((n: any, i: number) => {
    n.position = { x: (i % cols) * spacingX + 50, y: Math.floor(i / cols) * spacingY + 50 }
  })
  fitView()
}

// ===== 跳转触发设置 =====
function handleTriggerConfig() {
  router.push(`/task/${taskId}/trigger-config`)
}

// ===== 加载已有 DAG 配置 =====
onMounted(async () => {
  loadingDag.value = true
  try {
    const detail = await getTaskDetail(taskId) as any
    if (detail?.nodes?.length) {
      const colorMap: Record<string, string> = {
        START: '#67c23a', END: '#f56c6c', API: '#409eff', SCRIPT: '#e6a23c', MAPPING: '#909399', OUTPUT: '#9b59b6',
      }
      nodes.value = detail.nodes.map((n: any) => ({
        id: n.nodeId,
        type: n.nodeType,
        position: { x: n.positionX || 0, y: n.positionY || 0 },
        label: n.nodeName,
        data: { label: n.nodeName, nodeType: n.nodeType, config: n.config || {} },
        style: { background: colorMap[n.nodeType] || '#409eff', color: '#fff', border: '2px solid ' + (colorMap[n.nodeType] || '#409eff'), borderRadius: '6px', padding: '8px 16px', fontSize: '13px', fontWeight: 600 },
      }))
      edges.value = (detail.edges || []).map((e: any) => ({
        id: e.edgeId,
        source: e.sourceNodeId,
        target: e.targetNodeId,
      }))
    }
  } catch { /* ignore */ } finally { loadingDag.value = false }
  loadLists()
})
</script>

<style scoped lang="scss">
.dag-layout {
  display: flex;
  height: calc(100vh - 220px);
  gap: 12px;
}
.dag-palette {
  width: 130px;
  flex-shrink: 0;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #fafafa;
  .palette-title {
    margin: 0 0 12px 0;
    font-size: 14px;
    font-weight: 600;
  }
  .palette-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px;
    margin-bottom: 6px;
    border-radius: 4px;
    cursor: grab;
    font-size: 13px;
    transition: background 0.15s;
    &:hover { background: #ecf5ff; }
    .palette-color {
      width: 12px;
      height: 12px;
      border-radius: 3px;
      flex-shrink: 0;
    }
  }
}
.dag-canvas-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.dag-toolbar {
  display: flex;
  gap: 8px;
  padding-bottom: 8px;
}
.dag-canvas {
  flex: 1;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
}
</style>

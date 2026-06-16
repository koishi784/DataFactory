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
            @connect="onConnect"
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
      width="720px"
      top="5vh"
      :close-on-click-modal="false"
    >
      <!-- 通用：节点属性 -->
      <div style="margin-bottom: 16px">
        <el-form :model="nodeForm" label-width="80px" inline>
          <el-form-item label="节点名称">
            <el-input v-model="nodeForm.name" maxlength="50" style="width: 260px" />
          </el-form-item>
          <el-form-item v-if="configNodeType !== 'MAPPING'" label="描述">
            <el-input v-model="nodeForm.description" maxlength="200" style="width: 260px" placeholder="选填" />
          </el-form-item>
        </el-form>
      </div>

      <template v-if="configNodeType === 'API'">
        <el-tabs type="border-card">
          <!-- ===== 节点配置 ===== -->
          <el-tab-pane label="节点配置">
            <el-form label-width="140px">
              <el-form-item label="API 选择" required>
                <el-select v-model="apiConfig.apiId" filterable placeholder="请选择注册接口" style="width: 100%">
                  <el-option v-for="a in apiList" :key="a.id" :label="a.apiName" :value="a.id" />
                </el-select>
              </el-form-item>

              <!-- API 参数展示（只读） -->
              <el-form-item v-if="apiConfig.apiId" label="API 参数">
                <el-table :data="apiConfig.apiParams" size="small" border style="width: 100%">
                  <el-table-column prop="paramName" label="参数名称" />
                  <el-table-column prop="required" label="是否必传" width="80">
                    <template #default="{ row }">{{ row.required ? '是' : '否' }}</template>
                  </el-table-column>
                  <el-table-column prop="dataType" label="数据类型" width="100" />
                  <el-table-column prop="description" label="参数描述" show-overflow-tooltip />
                </el-table>
              </el-form-item>

              <el-divider>输入参数</el-divider>
              <el-table :data="apiConfig.inputParams" size="small" stripe border style="width: 100%">
                <el-table-column type="index" label="序号" width="55" />
                <el-table-column label="参数名称" min-width="120">
                  <template #default="{ row }"><el-input v-model="row.paramName" size="small" placeholder="参数名" /></template>
                </el-table-column>
                <el-table-column label="参数类型" width="120">
                  <template #default="{ row }">
                    <el-select v-model="row.paramType" size="small" style="width: 100%">
                      <el-option label="字符串" value="String" />
                      <el-option label="整型" value="Int" />
                      <el-option label="浮点型" value="Float" />
                      <el-option label="布尔型" value="Boolean" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="描述" min-width="120">
                  <template #default="{ row }"><el-input v-model="row.description" size="small" placeholder="选填" /></template>
                </el-table-column>
                <el-table-column label="操作" width="65" fixed="right">
                  <template #default="{ $index }">
                    <el-button link type="danger" size="small" @click="apiConfig.inputParams.splice($index, 1)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button type="primary" plain size="small" style="margin-top: 8px" @click="apiConfig.inputParams.push({ paramName: '', paramType: 'String', description: '' })">
                <el-icon><Plus /></el-icon> 新增输入参数
              </el-button>

              <el-divider>输出参数</el-divider>
              <el-table :data="apiConfig.outputParams" size="small" stripe border style="width: 100%">
                <el-table-column type="index" label="序号" width="55" />
                <el-table-column label="参数名称" min-width="120">
                  <template #default="{ row }"><el-input v-model="row.paramName" size="small" placeholder="参数名" /></template>
                </el-table-column>
                <el-table-column label="参数类型" width="120">
                  <template #default="{ row }">
                    <el-select v-model="row.paramType" size="small" style="width: 100%">
                      <el-option label="字符串" value="String" />
                      <el-option label="整型" value="Int" />
                      <el-option label="浮点型" value="Float" />
                      <el-option label="布尔型" value="Boolean" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="描述" min-width="120">
                  <template #default="{ row }"><el-input v-model="row.description" size="small" placeholder="选填" /></template>
                </el-table-column>
                <el-table-column label="操作" width="65" fixed="right">
                  <template #default="{ $index }">
                    <el-button link type="danger" size="small" @click="apiConfig.outputParams.splice($index, 1)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button type="primary" plain size="small" style="margin-top: 8px" @click="apiConfig.outputParams.push({ paramName: '', paramType: 'String', description: '' })">
                <el-icon><Plus /></el-icon> 新增输出参数
              </el-button>

              <el-divider>节点输入参数配置</el-divider>
              <el-table :data="apiConfig.paramMappings" size="small" stripe border style="width: 100%">
                <el-table-column type="index" label="序号" width="55" />
                <el-table-column label="参数名称" min-width="120">
                  <template #default="{ row }"><el-input v-model="row.paramName" size="small" placeholder="参数名" /></template>
                </el-table-column>
                <el-table-column label="对应上游节点" min-width="140">
                  <template #default="{ row }">
                    <el-select v-model="row.upstreamNodeId" size="small" filterable style="width: 100%">
                      <el-option v-for="n in upstreamNodes" :key="n.id" :label="n.data?.label || n.label" :value="n.id" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="上游节点参数" min-width="120">
                  <template #default="{ row }">
                    <el-select v-model="row.upstreamParam" size="small" filterable style="width: 100%">
                      <el-option v-for="p in getUpstreamParams(row.upstreamNodeId)" :key="p" :label="p" :value="p" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="测试值" min-width="100">
                  <template #default="{ row }"><el-input v-model="row.testValue" size="small" placeholder="选填" /></template>
                </el-table-column>
                <el-table-column label="操作" width="65" fixed="right">
                  <template #default="{ $index }">
                    <el-button link type="danger" size="small" @click="apiConfig.paramMappings.splice($index, 1)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button type="primary" plain size="small" style="margin-top: 8px" @click="apiConfig.paramMappings.push({ paramName: '', upstreamNodeId: '', upstreamParam: '', testValue: '' })">
                <el-icon><Plus /></el-icon> 新增映射
              </el-button>
            </el-form>
          </el-tab-pane>

          <!-- ===== 存储配置 ===== -->
          <el-tab-pane label="存储配置">
            <el-form label-width="140px">
              <el-form-item label="缓存取数规则" required>
                <el-radio-group v-model="apiConfig.cacheRule">
                  <el-radio value="CACHE_FIRST">缓存优先</el-radio>
                  <el-radio value="API_FIRST">接口优先</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="缓存有效期">
                <el-input-number v-model="apiConfig.cacheDuration" :min="1" style="width: 140px" />
                <el-select v-model="apiConfig.cacheUnit" style="width: 100px; margin-left: 8px">
                  <el-option label="天" value="DAY" />
                  <el-option label="小时" value="HOUR" />
                  <el-option label="分钟" value="MINUTE" />
                </el-select>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- ===== 异常处理 ===== -->
          <el-tab-pane label="异常处理">
            <el-form label-width="140px">
              <el-form-item label="选择错误码参数">
                <el-input v-model="apiConfig.errorCodeParam" placeholder="如：errorCode" style="width: 100%" />
              </el-form-item>
              <el-form-item label="错误码映射表">
                <el-table :data="apiConfig.errorCodeMappings" size="small" stripe border style="width: 100%">
                  <el-table-column label="编码取值" min-width="100">
                    <template #default="{ row }"><el-input v-model="row.code" size="small" placeholder="200" /></template>
                  </el-table-column>
                  <el-table-column label="编码名称" min-width="120">
                    <template #default="{ row }"><el-input v-model="row.name" size="small" placeholder="调用成功" /></template>
                  </el-table-column>
                  <el-table-column label="编码含义" min-width="160">
                    <template #default="{ row }"><el-input v-model="row.description" size="small" placeholder="接口调用成功" /></template>
                  </el-table-column>
                  <el-table-column label="操作" width="65" fixed="right">
                    <template #default="{ $index }">
                      <el-button link type="danger" size="small" @click="apiConfig.errorCodeMappings.splice($index, 1)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-button type="primary" plain size="small" style="margin-top: 8px" @click="apiConfig.errorCodeMappings.push({ code: '', name: '', description: '' })">
                  <el-icon><Plus /></el-icon> 新增错误码
                </el-button>
              </el-form-item>
              <el-divider>重试机制</el-divider>
              <el-form-item>
                <el-checkbox v-model="apiConfig.retryEnabled">启用重试</el-checkbox>
              </el-form-item>
              <template v-if="apiConfig.retryEnabled">
                <el-form-item label="重试次数">
                  <el-input-number v-model="apiConfig.retryCount" :min="0" :max="10" style="width: 100%" />
                </el-form-item>
                <el-form-item label="间隔时长">
                  <el-input-number v-model="apiConfig.retryInterval" :min="1" :max="300" style="width: 140px" />
                  <span style="margin-left: 8px">秒</span>
                </el-form-item>
              </template>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </template>

      <template v-else-if="configNodeType === 'SCRIPT'">
        <el-tabs type="border-card">
          <el-tab-pane label="节点配置">
            <el-form label-width="140px">
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

              <el-divider>脚本参数（只读）</el-divider>
              <el-table :data="scriptConfig.scriptParams" size="small" border style="width: 100%">
                <el-table-column prop="paramName" label="参数名称" />
                <el-table-column prop="required" label="是否必传" width="80">
                  <template #default="{ row }">{{ row.required ? '是' : '否' }}</template>
                </el-table-column>
                <el-table-column prop="paramType" label="数据类型" width="100" />
                <el-table-column prop="description" label="描述" show-overflow-tooltip />
              </el-table>

              <el-divider>输入参数</el-divider>
              <el-table :data="scriptConfig.inputParams" size="small" stripe border style="width: 100%">
                <el-table-column type="index" label="序号" width="55" />
                <el-table-column label="参数名称" min-width="120">
                  <template #default="{ row }"><el-input v-model="row.paramName" size="small" placeholder="参数名" /></template>
                </el-table-column>
                <el-table-column label="参数类型" width="120">
                  <template #default="{ row }">
                    <el-select v-model="row.paramType" size="small" style="width: 100%">
                      <el-option label="字符串" value="String" />
                      <el-option label="整型" value="Int" />
                      <el-option label="浮点型" value="Float" />
                      <el-option label="布尔型" value="Boolean" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="描述" min-width="120">
                  <template #default="{ row }"><el-input v-model="row.description" size="small" placeholder="选填" /></template>
                </el-table-column>
                <el-table-column label="操作" width="65" fixed="right">
                  <template #default="{ $index }">
                    <el-button link type="danger" size="small" @click="scriptConfig.inputParams.splice($index, 1)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button type="primary" plain size="small" style="margin-top: 8px" @click="scriptConfig.inputParams.push({ paramName: '', paramType: 'String', description: '' })">
                <el-icon><Plus /></el-icon> 新增输入参数
              </el-button>

              <el-divider>输出参数</el-divider>
              <el-table :data="scriptConfig.outputParams" size="small" stripe border style="width: 100%">
                <el-table-column type="index" label="序号" width="55" />
                <el-table-column label="参数名称" min-width="120">
                  <template #default="{ row }"><el-input v-model="row.paramName" size="small" placeholder="参数名" /></template>
                </el-table-column>
                <el-table-column label="参数类型" width="120">
                  <template #default="{ row }">
                    <el-select v-model="row.paramType" size="small" style="width: 100%">
                      <el-option label="字符串" value="String" />
                      <el-option label="整型" value="Int" />
                      <el-option label="浮点型" value="Float" />
                      <el-option label="布尔型" value="Boolean" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="描述" min-width="120">
                  <template #default="{ row }"><el-input v-model="row.description" size="small" placeholder="选填" /></template>
                </el-table-column>
                <el-table-column label="操作" width="65" fixed="right">
                  <template #default="{ $index }">
                    <el-button link type="danger" size="small" @click="scriptConfig.outputParams.splice($index, 1)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button type="primary" plain size="small" style="margin-top: 8px" @click="scriptConfig.outputParams.push({ paramName: '', paramType: 'String', description: '' })">
                <el-icon><Plus /></el-icon> 新增输出参数
              </el-button>

              <el-divider>节点输入参数配置</el-divider>
              <el-table :data="scriptConfig.paramMappings" size="small" stripe border style="width: 100%">
                <el-table-column type="index" label="序号" width="55" />
                <el-table-column label="参数名称" min-width="120">
                  <template #default="{ row }"><el-input v-model="row.paramName" size="small" placeholder="参数名" /></template>
                </el-table-column>
                <el-table-column label="对应上游节点" min-width="140">
                  <template #default="{ row }">
                    <el-select v-model="row.upstreamNodeId" size="small" filterable style="width: 100%">
                      <el-option v-for="n in upstreamNodes" :key="n.id" :label="n.data?.label || n.label" :value="n.id" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="上游节点参数" min-width="120">
                  <template #default="{ row }">
                    <el-select v-model="row.upstreamParam" size="small" filterable style="width: 100%">
                      <el-option v-for="p in getUpstreamParams(row.upstreamNodeId)" :key="p" :label="p" :value="p" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="65" fixed="right">
                  <template #default="{ $index }">
                    <el-button link type="danger" size="small" @click="scriptConfig.paramMappings.splice($index, 1)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button type="primary" plain size="small" style="margin-top: 8px" @click="scriptConfig.paramMappings.push({ paramName: '', upstreamNodeId: '', upstreamParam: '' })">
                <el-icon><Plus /></el-icon> 新增映射
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </template>

      <template v-else-if="configNodeType === 'MAPPING'">
        <el-tabs type="border-card">
          <el-tab-pane label="字段映射">
            <el-table :data="mappingConfig.mappings" size="small" stripe border style="width: 100%">
              <el-table-column type="index" label="序号" width="55" />
              <el-table-column label="数据表字段" min-width="120">
                <template #default="{ row }"><el-input v-model="row.sourceField" size="small" placeholder="字段名" /></template>
              </el-table-column>
              <el-table-column label="对应上游节点" min-width="140">
                <template #default="{ row }">
                  <el-select v-model="row.upstreamNodeId" size="small" filterable style="width: 100%">
                    <el-option v-for="n in upstreamNodes" :key="n.id" :label="n.data?.label || n.label" :value="n.id" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="上游节点参数" min-width="120">
                <template #default="{ row }">
                  <el-select v-model="row.upstreamParam" size="small" filterable style="width: 100%">
                    <el-option v-for="p in getUpstreamParams(row.upstreamNodeId)" :key="p" :label="p" :value="p" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="65" fixed="right">
                <template #default="{ $index }">
                  <el-button link type="danger" size="small" @click="mappingConfig.mappings.splice($index, 1)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-button type="primary" plain size="small" style="margin-top: 8px" @click="mappingConfig.mappings.push({ sourceField: '', upstreamNodeId: '', upstreamParam: '' })">
              <el-icon><Plus /></el-icon> 新增映射
            </el-button>
          </el-tab-pane>
        </el-tabs>
      </template>

      <template v-else-if="configNodeType === 'OUTPUT'">
        <el-tabs type="border-card">
          <el-tab-pane label="节点配置">
            <el-form label-width="140px">
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
                <el-input v-model="outputConfig.targetTable" placeholder="选择数据源后自动加载" />
              </el-form-item>
              <el-form-item label="写入模式" required>
                <el-select v-model="outputConfig.writeMode" style="width: 100%">
                  <el-option label="追加" value="INSERT" />
                  <el-option label="存在更新" value="UPSERT" />
                  <el-option label="覆盖" value="OVERWRITE" />
                </el-select>
              </el-form-item>

              <el-divider>字段映射</el-divider>
              <el-table :data="outputConfig.mappings" size="small" stripe border style="width: 100%">
                <el-table-column type="index" label="序号" width="55" />
                <el-table-column label="数据表字段" min-width="120">
                  <template #default="{ row }"><el-input v-model="row.sourceField" size="small" placeholder="字段名" /></template>
                </el-table-column>
                <el-table-column label="对应上游节点" min-width="140">
                  <template #default="{ row }">
                    <el-select v-model="row.upstreamNodeId" size="small" filterable style="width: 100%">
                      <el-option v-for="n in upstreamNodes" :key="n.id" :label="n.data?.label || n.label" :value="n.id" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="上游节点参数" min-width="120">
                  <template #default="{ row }">
                    <el-select v-model="row.upstreamParam" size="small" filterable style="width: 100%">
                      <el-option v-for="p in getUpstreamParams(row.upstreamNodeId)" :key="p" :label="p" :value="p" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="65" fixed="right">
                  <template #default="{ $index }">
                    <el-button link type="danger" size="small" @click="outputConfig.mappings.splice($index, 1)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button type="primary" plain size="small" style="margin-top: 8px" @click="outputConfig.mappings.push({ sourceField: '', upstreamNodeId: '', upstreamParam: '' })">
                <el-icon><Plus /></el-icon> 新增字段映射
              </el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
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
import { ref, reactive, computed, markRaw, onMounted, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, Plus } from '@element-plus/icons-vue'
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
  { type: 'START', label: '开始', color: '#67c23a' },
  { type: 'API', label: 'API 接口', color: '#409eff' },
  { type: 'SCRIPT', label: '脚本', color: '#e6a23c' },
  { type: 'MAPPING', label: '映射', color: '#909399' },
  { type: 'OUTPUT', label: '输出', color: '#9b59b6' },
  { type: 'END', label: '结束', color: '#f56c6c' },
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

// ===== 连线处理 =====
function onConnect(connection: any) {
  edges.value.push({
    id: `edge_${Math.random().toString(36).substring(2, 8)}`,
    source: connection.source,
    target: connection.target,
  })
}
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

const nodeForm = reactive({
  name: '',
  description: '',
})

const apiConfig = reactive({
  apiId: null as number | null,
  apiParams: [] as any[],
  inputParams: [] as { paramName: string; paramType: string; description: string }[],
  outputParams: [] as { paramName: string; paramType: string; description: string }[],
  paramMappings: [] as { paramName: string; upstreamNodeId: string; upstreamParam: string; testValue: string }[],
  cacheRule: 'CACHE_FIRST',
  cacheDuration: 30,
  cacheUnit: 'MINUTE',
  errorCodeParam: '',
  errorCodeMappings: [] as { code: string; name: string; description: string }[],
  retryEnabled: false,
  retryCount: 3,
  retryInterval: 5,
})

const scriptConfig = reactive({
  scriptId: null as number | null,
  scriptVersion: '',
  dataSourceId: null as number | null,
  scriptParams: [] as any[],
  inputParams: [] as { paramName: string; paramType: string; description: string }[],
  outputParams: [] as { paramName: string; paramType: string; description: string }[],
  paramMappings: [] as { paramName: string; upstreamNodeId: string; upstreamParam: string }[],
})

const mappingConfig = reactive({
  mappings: [] as { sourceField: string; upstreamNodeId: string; upstreamParam: string }[],
})

const outputConfig = reactive({
  outputType: 'DATABASE',
  targetDataSourceId: null as number | null,
  targetTable: '',
  writeMode: 'INSERT',
  mappings: [] as { sourceField: string; upstreamNodeId: string; upstreamParam: string }[],
})

const apiList = ref<any[]>([])
const scriptList = ref<any[]>([])
const dbList = ref<any[]>([])

async function loadLists() {
  try {
    const res = await getScriptList({ pageNum: 1, pageSize: 100, status: 1 }) as any
    scriptList.value = res?.records || res || []
  } catch { scriptList.value = [] }
  try {
    const res = await getApiList({ pageNum: 1, pageSize: 100, status: 1 }) as any
    apiList.value = res?.records || res || []
  } catch { apiList.value = [] }
  try {
    const res = await getDatabaseList({ pageNum: 1, pageSize: 100, status: 1 }) as any
    dbList.value = res?.records || res || []
  } catch { dbList.value = [] }
}

// ===== 上游节点列表（用于参数映射下拉） =====
const upstreamNodes = computed(() => {
  return nodes.value.filter((n: any) => n.id !== configNodeId.value)
})

function getUpstreamParams(nodeId: string) {
  if (!nodeId) return []
  const n = nodes.value.find((n: any) => n.id === nodeId)
  if (!n) return []
  // 从节点 config 中获取输出参数
  const cfg = n.data?.config
  if (cfg?.outputParams?.length) return cfg.outputParams.map((p: any) => p.paramName)
  return []
}

function onNodeDoubleClick(event: any) {
  const node = event?.node || event
  if (!node?.id) return
  configNodeId.value = node.id
  configNodeType.value = node.type || node.data?.nodeType
  configNodeTitle.value = `配置 - ${node.data?.label || node.label || node.type}`
  nodeForm.name = node.data?.label || node.label || ''
  nodeForm.description = node.data?.description || ''

  // 加载已有配置
  const cfg = node.data?.config || {}
  if (configNodeType.value === 'API') {
    Object.assign(apiConfig, {
      apiId: cfg.apiId ?? null,
      apiParams: [],
      inputParams: cfg.inputParams || [],
      outputParams: cfg.outputParams || [],
      paramMappings: cfg.paramMappings || [],
      cacheRule: cfg.cacheRule || 'CACHE_FIRST',
      cacheDuration: cfg.cacheDuration ?? 30,
      cacheUnit: cfg.cacheUnit || 'MINUTE',
      errorCodeParam: cfg.errorCodeParam || '',
      errorCodeMappings: cfg.errorCodeMappings || [],
      retryEnabled: cfg.retryEnabled ?? false,
      retryCount: cfg.retryCount ?? 3,
      retryInterval: cfg.retryInterval ?? 5,
    })
  } else if (configNodeType.value === 'SCRIPT') {
    Object.assign(scriptConfig, {
      scriptId: cfg.scriptId ?? null,
      scriptVersion: cfg.scriptVersion || '',
      dataSourceId: cfg.dataSourceId ?? null,
      scriptParams: [],
      inputParams: cfg.inputParams || [],
      outputParams: cfg.outputParams || [],
      paramMappings: cfg.paramMappings || [],
    })
  } else if (configNodeType.value === 'MAPPING') {
    mappingConfig.mappings = cfg.mappings || []
  } else if (configNodeType.value === 'OUTPUT') {
    Object.assign(outputConfig, {
      outputType: cfg.outputType || 'DATABASE',
      targetDataSourceId: cfg.targetDataSourceId ?? null,
      targetTable: cfg.targetTable || '',
      writeMode: cfg.writeMode || 'INSERT',
      mappings: cfg.mappings || [],
    })
  }
  configDialogVisible.value = true
}

function saveNodeConfig() {
  const n = nodes.value.find((n: any) => n.id === configNodeId.value)
  if (!n) { configDialogVisible.value = false; return }
  let cfg: any = {}
  if (configNodeType.value === 'API') {
    cfg = { ...apiConfig }
    delete cfg.apiParams  // apiParams 是只读展示字段，不保存
  } else if (configNodeType.value === 'SCRIPT') {
    cfg = { ...scriptConfig }
    delete cfg.scriptParams  // scriptParams 是只读展示字段，不保存
  } else if (configNodeType.value === 'MAPPING') {
    cfg = { mappings: [...mappingConfig.mappings] }
  } else if (configNodeType.value === 'OUTPUT') {
    cfg = { ...outputConfig }
  }
  n.data = {
    ...n.data,
    label: nodeForm.name || n.data?.label,
    description: nodeForm.description || '',
    config: cfg,
  }
  n.label = nodeForm.name || n.label
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
  const spacingY = 120
  const startX = 100
  nodes.value.forEach((n: any, i: number) => {
    n.position = { x: startX, y: i * spacingY + 50 }
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

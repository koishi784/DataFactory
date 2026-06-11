<template>
  <PageContainer :title="isEdit ? '编辑接口' : '新增接口'">
    <el-form :model="form" label-width="120px" class="api-form">
      <el-tabs>
        <el-tab-pane label="基本信息">
          <el-form-item label="接口名称" required>
            <el-input v-model="form.apiName" placeholder="全局唯一，不允许空格" />
          </el-form-item>
          <el-form-item label="接口分类" required>
            <el-tree-select v-model="form.categoryId" :data="categories" :props="{ label: 'name', value: 'id', children: 'children' }" placeholder="请选择接口分类" style="width: 100%" />
          </el-form-item>
          <el-form-item label="请求方法" required>
            <el-select v-model="form.method" style="width: 100%">
              <el-option label="GET" value="GET" /><el-option label="POST" value="POST" />
            </el-select>
          </el-form-item>
          <el-form-item label="接口地址" required>
            <el-input v-model="form.url" placeholder="https://example.com/api/v1/..." />
          </el-form-item>
          <el-form-item label="接口来源" required>
            <el-input v-model="form.source" placeholder="如：订单系统" />
          </el-form-item>
          <el-form-item label="协议">
            <el-radio-group v-model="form.protocol">
              <el-radio value="HTTP">HTTP</el-radio>
              <el-radio value="HTTPS">HTTPS</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="超时时间(ms)">
            <el-input-number v-model="form.timeout" :min="1" :max="1800000" />
          </el-form-item>
          <el-form-item label="重试次数">
            <el-input-number v-model="form.retryCount" :min="0" :max="5" />
          </el-form-item>
          <el-form-item label="接口说明">
            <el-input v-model="form.apiDescription" type="textarea" :rows="3" maxlength="1000" show-word-limit />
          </el-form-item>
        </el-tab-pane>
        <el-tab-pane label="参数配置">
          <el-tabs type="border-card">
            <el-tab-pane label="请求头">
              <el-table :data="form.headers" size="small" stripe border style="width: 100%">
                <el-table-column type="index" label="序号" width="55" />
                <el-table-column label="名称" min-width="180">
                  <template #default="{ row }">
                    <el-input v-model="row.key" size="small" placeholder="如：Content-Type" />
                  </template>
                </el-table-column>
                <el-table-column label="值" min-width="200">
                  <template #default="{ row }">
                    <el-input v-model="row.value" size="small" placeholder="如：application/json" />
                  </template>
                </el-table-column>
                <el-table-column label="必填" width="70">
                  <template #default="{ row }">
                    <el-checkbox v-model="row.required" :true-value="true" :false-value="false" />
                  </template>
                </el-table-column>
                <el-table-column label="说明" min-width="160">
                  <template #default="{ row }">
                    <el-input v-model="row.description" size="small" placeholder="说明" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="60" fixed="right">
                  <template #default="{ $index }">
                    <el-button link type="danger" size="small" @click="removeHeader($index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button type="primary" link size="small" style="margin-top: 8px" @click="addHeader">+ 新增请求头</el-button>
            </el-tab-pane>

            <el-tab-pane label="输入参数">
              <p class="text-secondary" style="margin-bottom: 8px">配置接口的查询参数（QUERY）和路径参数（PATH）</p>
              <el-table :data="inputParams" size="small" stripe border style="width: 100%">
                <el-table-column type="index" label="序号" width="55" />
                <el-table-column label="参数名称" min-width="140">
                  <template #default="{ row }">
                    <el-input v-model="row.paramName" size="small" placeholder="如：orderId" />
                  </template>
                </el-table-column>
                <el-table-column label="参数类型" width="90">
                  <template #default="{ row }">
                    <el-select v-model="row.paramType" size="small" style="width: 100%">
                      <el-option label="QUERY" value="QUERY" />
                      <el-option label="PATH" value="PATH" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="数据类型" width="110">
                  <template #default="{ row }">
                    <el-select v-model="row.dataType" size="small" style="width: 100%">
                      <el-option label="STRING" value="STRING" />
                      <el-option label="INTEGER" value="INTEGER" />
                      <el-option label="LONG" value="LONG" />
                      <el-option label="DOUBLE" value="DOUBLE" />
                      <el-option label="BOOLEAN" value="BOOLEAN" />
                      <el-option label="DATE" value="DATE" />
                      <el-option label="DATETIME" value="DATETIME" />
                      <el-option label="OBJECT" value="OBJECT" />
                      <el-option label="ARRAY" value="ARRAY" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="必填" width="60">
                  <template #default="{ row }">
                    <el-checkbox v-model="row.required" :true-value="true" :false-value="false" />
                  </template>
                </el-table-column>
                <el-table-column label="默认值" width="100">
                  <template #default="{ row }">
                    <el-input v-model="row.defaultValue" size="small" placeholder="默认值" />
                  </template>
                </el-table-column>
                <el-table-column label="示例值" width="120">
                  <template #default="{ row }">
                    <el-input v-model="row.exampleValue" size="small" placeholder="示例值" />
                  </template>
                </el-table-column>
                <el-table-column label="排序" width="65">
                  <template #default="{ row }">
                    <el-input-number v-model="row.sortOrder" :min="0" size="small" controls-position="right" style="width: 55px" />
                  </template>
                </el-table-column>
                <el-table-column label="说明" min-width="140">
                  <template #default="{ row }">
                    <el-input v-model="row.description" size="small" placeholder="说明" />
                  </template>
                </el-table-column>
                <el-table-column label="校验规则" width="140">
                  <template #default="{ row }">
                    <el-input v-model="row.validationRule" size="small" placeholder="正则表达式" />
                  </template>
                </el-table-column>
                <el-table-column label="最小值" width="90">
                  <template #default="{ row }">
                    <el-input v-model="row.minValue" size="small" placeholder="最小值" />
                  </template>
                </el-table-column>
                <el-table-column label="最大值" width="90">
                  <template #default="{ row }">
                    <el-input v-model="row.maxValue" size="small" placeholder="最大值" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="60" fixed="right">
                  <template #default="{ $index }">
                    <el-button link type="danger" size="small" @click="removeInputParam($index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button type="primary" link size="small" style="margin-top: 8px" @click="addInputParam">+ 新增输入参数</el-button>
            </el-tab-pane>

            <el-tab-pane label="请求Body">
              <p class="text-secondary" style="margin-bottom: 8px">配置 POST 请求的请求体参数</p>
              <el-table :data="bodyParams" size="small" stripe border style="width: 100%">
                <el-table-column type="index" label="序号" width="55" />
                <el-table-column label="参数名称" min-width="140">
                  <template #default="{ row }">
                    <el-input v-model="row.paramName" size="small" placeholder="如：userId" />
                  </template>
                </el-table-column>
                <el-table-column label="数据类型" width="110">
                  <template #default="{ row }">
                    <el-select v-model="row.dataType" size="small" style="width: 100%">
                      <el-option label="STRING" value="STRING" />
                      <el-option label="INTEGER" value="INTEGER" />
                      <el-option label="LONG" value="LONG" />
                      <el-option label="DOUBLE" value="DOUBLE" />
                      <el-option label="BOOLEAN" value="BOOLEAN" />
                      <el-option label="DATE" value="DATE" />
                      <el-option label="DATETIME" value="DATETIME" />
                      <el-option label="OBJECT" value="OBJECT" />
                      <el-option label="ARRAY" value="ARRAY" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="必填" width="60">
                  <template #default="{ row }">
                    <el-checkbox v-model="row.required" :true-value="true" :false-value="false" />
                  </template>
                </el-table-column>
                <el-table-column label="默认值" width="100">
                  <template #default="{ row }">
                    <el-input v-model="row.defaultValue" size="small" placeholder="默认值" />
                  </template>
                </el-table-column>
                <el-table-column label="示例值" width="120">
                  <template #default="{ row }">
                    <el-input v-model="row.exampleValue" size="small" placeholder="示例值" />
                  </template>
                </el-table-column>
                <el-table-column label="排序" width="65">
                  <template #default="{ row }">
                    <el-input-number v-model="row.sortOrder" :min="0" size="small" controls-position="right" style="width: 55px" />
                  </template>
                </el-table-column>
                <el-table-column label="说明" min-width="140">
                  <template #default="{ row }">
                    <el-input v-model="row.description" size="small" placeholder="说明" />
                  </template>
                </el-table-column>
                <el-table-column label="校验规则" width="140">
                  <template #default="{ row }">
                    <el-input v-model="row.validationRule" size="small" placeholder="正则表达式" />
                  </template>
                </el-table-column>
                <el-table-column label="最小值" width="90">
                  <template #default="{ row }">
                    <el-input v-model="row.minValue" size="small" placeholder="最小值" />
                  </template>
                </el-table-column>
                <el-table-column label="最大值" width="90">
                  <template #default="{ row }">
                    <el-input v-model="row.maxValue" size="small" placeholder="最大值" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="60" fixed="right">
                  <template #default="{ $index }">
                    <el-button link type="danger" size="small" @click="removeBodyParam($index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button type="primary" link size="small" style="margin-top: 8px" @click="addBodyParam">+ 新增请求Body参数</el-button>
            </el-tab-pane>

            <el-tab-pane label="返回参数">
              <p class="text-secondary" style="margin-bottom: 8px">配置接口的响应示例（JSON 格式）</p>
              <el-input v-model="form.responseExample" type="textarea" :rows="10" placeholder='如：{"code": 200, "data": {...}}' />
            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>
      </el-tabs>
      <el-form-item style="margin-top: 16px">
        <el-button type="primary" @click="handleSave">保存</el-button>
        <el-button @click="router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { getApiCategoryTree, createApi, updateApi, getApiDetail } from '@/api/api'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const categories = ref<any[]>([])

interface ParamItem {
  paramName: string; paramType: string; dataType: string;
  required: boolean; description: string; defaultValue: string;
  exampleValue: string; sortOrder: number; validationRule: string;
  minValue: string; maxValue: string;
}

const form = reactive({
  apiName: '', apiDescription: '', categoryId: null as number | null,
  source: '', protocol: 'HTTPS', method: 'GET', url: '',
  timeout: 30000, retryCount: 0, responseExample: '',
  headers: [] as { key: string; value: string; required: boolean; description: string }[],
  requestParams: [] as ParamItem[],
})

// 计算属性：按参数类型拆分为三个视图（共用同一个 requestParams 数组）
const inputParams = computed(() =>
  form.requestParams.filter(p => p.paramType === 'QUERY' || p.paramType === 'PATH')
)
const bodyParams = computed(() =>
  form.requestParams.filter(p => p.paramType === 'BODY')
)

function createParamItem(paramType: string): ParamItem {
  return { paramName: '', paramType, dataType: 'STRING', required: false,
    description: '', defaultValue: '', exampleValue: '', sortOrder: 0,
    validationRule: '', minValue: '', maxValue: '' }
}

function addHeader() {
  form.headers.push({ key: '', value: '', required: false, description: '' })
}

function removeHeader(index: number) {
  form.headers.splice(index, 1)
}

function addInputParam() {
  form.requestParams.push(createParamItem('QUERY'))
}

function removeInputParam(index: number) {
  const item = inputParams.value[index]
  const i = form.requestParams.findIndex(p => p === item)
  if (i > -1) form.requestParams.splice(i, 1)
}

function addBodyParam() {
  form.requestParams.push(createParamItem('BODY'))
}

function removeBodyParam(index: number) {
  const item = bodyParams.value[index]
  const i = form.requestParams.findIndex(p => p === item)
  if (i > -1) form.requestParams.splice(i, 1)
}

onMounted(async () => {
  categories.value = (await getApiCategoryTree()) as any[] || []
  if (isEdit) {
    const detail = await getApiDetail(Number(route.params.id)) as any
    if (detail) Object.assign(form, detail)
  }
})

async function handleSave() {
  try {
    if (isEdit) {
      await updateApi(Number(route.params.id), form)
    } else {
      await createApi(form)
    }
    ElMessage.success(isEdit ? '更新成功' : '新增成功')
    router.push('/api/list')
  } catch { /* handled by interceptor */ }
}
</script>

<style scoped lang="scss">
.api-form {
  width: 100%;
}
</style>

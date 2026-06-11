<template>
  <PageContainer :title="isEdit ? '编辑接口' : '新增接口'">
    <el-form :model="form" label-width="120px" style="max-width: 800px">
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
              <el-option label="PUT" value="PUT" /><el-option label="DELETE" value="DELETE" />
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
          <el-form-item label="响应示例">
            <el-input v-model="form.responseExample" type="textarea" :rows="5" />
          </el-form-item>
        </el-tab-pane>
        <el-tab-pane label="参数配置">
          <p class="text-secondary">请求头和请求参数配置（待实现详细表单）</p>
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
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { getApiCategoryTree, createApi, updateApi, getApiDetail } from '@/api/api'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id
const categories = ref<any[]>([])

const form = reactive({
  apiName: '', apiDescription: '', categoryId: null as number | null,
  source: '', protocol: 'HTTPS', method: 'GET', url: '',
  timeout: 30000, retryCount: 0, responseExample: '',
})

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

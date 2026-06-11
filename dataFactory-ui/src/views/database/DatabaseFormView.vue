<template>
  <PageContainer :title="isEdit ? '编辑数据库连接' : '新增数据库连接'">
    <el-form :model="form" label-width="120px" style="max-width: 600px">
      <el-form-item label="连接名称" required>
        <el-input v-model="form.connectionName" maxlength="50" />
      </el-form-item>
      <el-form-item label="数据库类型" required>
        <el-select v-model="form.dbType" style="width: 100%">
          <el-option label="MySQL" value="MySQL" />
          <el-option label="PostgreSQL" value="PostgreSQL" />
          <el-option label="Oracle" value="Oracle" />
          <el-option label="SQLServer" value="SQLServer" />
          <el-option label="Hive" value="Hive" />
          <el-option label="ClickHouse" value="ClickHouse" />
        </el-select>
      </el-form-item>
      <el-form-item label="主机地址" required>
        <el-input v-model="form.host" placeholder="如：192.168.1.100" />
      </el-form-item>
      <el-form-item label="端口" required>
        <el-input-number v-model="form.port" :min="1" :max="65535" />
      </el-form-item>
      <el-form-item label="数据库名称" required>
        <el-input v-model="form.databaseName" />
      </el-form-item>
      <el-form-item label="用户名" required>
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" show-password />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSave">保存</el-button>
        <el-button @click="router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageContainer from '@/components/PageContainer.vue'
import { createDatabase, updateDatabase, getDatabaseDetail } from '@/api/database'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id

const form = reactive({
  connectionName: '', dbType: 'MySQL', host: '', port: 3306,
  databaseName: '', username: '', password: '', remark: '',
})

onMounted(async () => {
  if (isEdit) {
    const detail = await getDatabaseDetail(Number(route.params.id)) as any
    if (detail) Object.assign(form, detail)
  }
})

async function handleSave() {
  try {
    if (isEdit) {
      await updateDatabase(Number(route.params.id), form)
    } else {
      await createDatabase(form)
    }
    ElMessage.success(isEdit ? '更新成功' : '新增成功')
    router.push('/database/list')
  } catch { /* handled */ }
}
</script>

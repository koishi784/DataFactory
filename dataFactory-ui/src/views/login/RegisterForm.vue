<template>
  <div class="register-form-wrapper">
    <h2 class="form-title">注册新账号</h2>
    <p class="form-subtitle">加入数据工厂大数据平台</p>

    <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleSubmit">
      <el-form-item prop="username">
        <el-input v-model="form.username" placeholder="用户名（4-20位，字母开头）" :prefix-icon="User" />
      </el-form-item>
      <el-form-item prop="nickname">
        <el-input v-model="form.nickname" placeholder="昵称" :prefix-icon="User" />
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="密码（8-32位，含大小写字母+数字）"
          :prefix-icon="Lock"
          show-password
          @focus="$emit('password-focus', true)"
          @blur="$emit('password-focus', false)"
        />
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input
          v-model="form.confirmPassword"
          type="password"
          placeholder="确认密码"
          :prefix-icon="Lock"
          show-password
          @focus="$emit('password-focus', true)"
          @blur="$emit('password-focus', false)"
        />
      </el-form-item>
      <el-form-item prop="email">
        <el-input v-model="form.email" placeholder="邮箱" />
      </el-form-item>
      <el-form-item prop="mobile">
        <el-input v-model="form.mobile" placeholder="手机号" />
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          class="submit-btn"
          :loading="loading"
          @click="handleSubmit"
        >
          {{ loading ? '注册中...' : '注 册' }}
        </el-button>
      </el-form-item>
    </el-form>

    <div class="form-footer">
      <span>已有账号？</span>
      <el-link type="primary" :underline="false" @click="$emit('switch-to-login')">
        立即登录
      </el-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormItemRule } from 'element-plus'
import type { RegisterRequest } from '@/types'

defineProps<{ loading?: boolean }>()
const emit = defineEmits<{
  register: [data: RegisterRequest]
  'password-focus': [focused: boolean]
  'switch-to-login': []
}>()

const formRef = ref<any>()
const form = reactive<RegisterRequest>({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  mobile: '',
  remark: '',
})

const validateConfirmPass = (_rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: Record<string, FormItemRule[]> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '用户名长度需为4-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]{3,19}$/, message: '字母开头，仅支持字母、数字、下划线', trigger: 'blur' },
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 32, message: '密码长度需为8-32个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,32}$/, message: '密码须包含大写字母、小写字母和数字', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPass, trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '邮箱格式不正确', trigger: 'blur' },
  ],
  mobile: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
}

function handleSubmit() {
  if (!formRef.value) return
  formRef.value.validate((valid: boolean) => {
    if (valid) {
      emit('register', { ...form })
    }
  })
}
</script>

<style scoped lang="scss">
.register-form-wrapper {
  width: 100%;
  max-width: 400px;
  padding: 40px 32px;
}

.form-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 6px;
}

.form-subtitle {
  font-size: 14px;
  color: #909399;
  margin-bottom: 32px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
}

.form-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
  margin-top: 24px;
  font-size: 14px;
  color: #909399;

  .el-link {
    font-size: 14px;
  }
}
</style>

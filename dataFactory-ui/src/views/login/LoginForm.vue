<template>
  <div class="login-form-wrapper">
    <h2 class="form-title">登录到数据工厂</h2>
    <p class="form-subtitle">大数据平台管理控制台</p>

    <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
      <el-form-item prop="account">
        <el-input
          v-model="form.account"
          placeholder="用户名 / 邮箱 / 手机号"
          :prefix-icon="User"
        />
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="密码"
          :prefix-icon="Lock"
          show-password
          @focus="$emit('password-focus', true)"
          @blur="$emit('password-focus', false)"
        />
      </el-form-item>
      <el-form-item>
        <div class="form-options">
          <el-checkbox v-model="form.rememberMe">记住我</el-checkbox>
          <el-link type="primary" :underline="false">忘记密码？</el-link>
        </div>
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          class="submit-btn"
          :loading="loading"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </el-button>
      </el-form-item>
    </el-form>

    <div class="form-footer">
      <span>还没有账号？</span>
      <el-link type="primary" :underline="false" @click="$emit('switch-to-register')">
        立即注册
      </el-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormItemRule } from 'element-plus'

defineProps<{ loading?: boolean }>()
const emit = defineEmits<{
  login: [account: string, password: string, rememberMe: boolean]
  'password-focus': [focused: boolean]
  'account-empty': [empty: boolean]
  'switch-to-register': []
}>()

const formRef = ref<any>()
const form = reactive({ account: '', password: '', rememberMe: false })

const rules: Record<string, FormItemRule[]> = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

function handleLogin() {
  if (!formRef.value) return

  // 若账号为空，立即触发角色问号提示
  if (!form.account) {
    emit('account-empty', true)
  }

  formRef.value.validate((valid: boolean) => {
    if (valid) {
      emit('login', form.account, form.password, form.rememberMe)
    }
  })
}
</script>

<style scoped lang="scss">
.login-form-wrapper {
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

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
}

.form-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #909399;

  .el-link {
    font-size: 14px;
  }
}
</style>

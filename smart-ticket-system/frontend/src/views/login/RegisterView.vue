<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <el-icon size="40" color="#409eff"><Service /></el-icon>
        <h2>注册账号</h2>
        <p>创建普通用户账号</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleRegister">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="可选" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="可选" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleRegister">
            注册并登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="footer">
        <span>已有账号？</span>
        <router-link to="/login">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { requestRegister } from '@/api/user'
import { useUserInfoStore } from '@/stores/userInfo'

const router = useRouter()
const stores = useUserInfoStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await requestRegister(form)
    if (res.code === 200) {
      stores.setUser({
        userId: res.data.userId,
        username: res.data.username,
        nickname: res.data.nickname,
        token: res.data.token,
        roles: res.data.roles,
        permissions: res.data.permissions,
      })
      ElMessage.success('注册成功')
      router.push('/home')
    } else {
      ElMessage.error(res.msg || '注册失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f3ff 0%, #f7fafc 50%, #eef6ff 100%);
}
.register-card {
  width: 420px;
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.12);
}
.register-header {
  text-align: center;
  margin-bottom: 20px;
}
.register-header h2 {
  margin: 8px 0 4px;
}
.register-header p {
  color: #909399;
  margin: 0;
}
.submit-btn {
  width: 100%;
}
.footer {
  text-align: center;
  color: #606266;
}
</style>

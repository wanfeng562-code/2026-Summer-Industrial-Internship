<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <el-icon size="40" color="#409eff"><Service /></el-icon>
        <h2>工单管理系统</h2>
        <p>AI智能客服平台</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" prefix-icon="User" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" prefix-icon="Lock" placeholder="请输入密码" type="password" show-password size="large" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" size="large" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register">立即注册</router-link>
      </div>
      <div class="demo-accounts">
        <el-divider>演示账号</el-divider>
        <el-tag @click="fillDemo('admin', '123456')" class="demo-tag">管理员 admin</el-tag>
        <el-tag type="warning" @click="fillDemo('agent_zhang', '123456')" class="demo-tag">客服 agent_zhang</el-tag>
        <el-tag type="success" @click="fillDemo('user_wang', '123456')" class="demo-tag">用户 user_wang</el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import axios from "axios"  //导入axios
import { useUserInfoStore } from '@/stores/userInfo';

import { requestLogin } from '@/api/user'   // @/   src目录下  
import type {R, UserInfo, LoginRequest} from '@/api/user/type'

const stores = useUserInfoStore()


const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const fillDemo = (username: string, password: string) => {
  form.username = username
  form.password = password
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true

  // await axios.post('/api/user/login', {
  //       username: form.username,
  //       password: form.password
  //     }, {
  //       headers: {
  //         'Content-Type': 'application/x-www-form-urlencoded'
  //       }
  //   }).then(function (response) {
  //     console.log(response);
  //     ElMessage.success("登录成功")
  //   })
  //   .catch(function (error) {
  //     console.log(error);
  //     ElMessage.error("登录失败")
  //   });

  try {
    const res = await requestLogin(form)
    if(res.code == 200){
      stores.user.userId = res.data.userId
      stores.user.username = res.data.username
      stores.user.nickname = res.data.nickname
      stores.user.token = res.data.token
      stores.user.roles = res.data.roles
      stores.user.permissions = res.data.permissions
      router.push('/home')
      ElMessage.success("登录成功")
    }else{
      ElMessage.error(res.msg)
    }
    
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}
.login-header {
  text-align: center;
  margin-bottom: 30px;
}
.login-header h2 {
  margin: 12px 0 4px;
  color: #333;
}
.login-header p {
  color: #999;
  font-size: 14px;
}
.login-btn {
  width: 100%;
}
.login-footer {
  text-align: center;
  color: #999;
  font-size: 14px;
}
.login-footer a {
  color: #409eff;
  text-decoration: none;
  margin-left: 4px;
}
.demo-accounts {
  margin-top: 20px;
}
.demo-tag {
  cursor: pointer;
  margin: 4px;
}
</style>

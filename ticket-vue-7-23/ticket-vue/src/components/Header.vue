<script setup lang="ts">
  import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserInfoStore } from '@/stores/userInfo';

import { requestLogout } from '@/api/user'   // @/   src目录下  
import type {R, UserInfo, LoginRequest} from '@/api/user/type'

const stores = useUserInfoStore()
const route = useRoute()
const router = useRouter()

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    const res = await requestLogout()
    if(res.code == 200){
      stores.user.userId = 0
      stores.user.username = ''
      stores.user.nickname = ''
      stores.user.token = ''
      stores.user.roles = []
      stores.user.permissions = []
      router.push('/login')
    }
    
  }
}
</script>

<template>
  <!-- 顶栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{stores.user.nickname}}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
</template>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}
.header-left {
  display: flex;
  align-items: center;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.username {
  font-size: 14px;
  color: #333;
}
</style>
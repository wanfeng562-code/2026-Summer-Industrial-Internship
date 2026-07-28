<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserInfoStore } from '@/stores/userInfo'
import { requestLogout } from '@/api/user'

const stores = useUserInfoStore()
const route = useRoute()
const router = useRouter()

const handleCommand = async (command: string) => {
  if (command !== 'logout') return
  try {
    await requestLogout()
  } catch {
    // 即使服务端退出失败，也清理本地登录态。
  }
  stores.clearUser()
  ElMessage.success('已退出登录')
  await router.push('/login')
}
</script>

<template>
  <el-header class="header">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
    </el-breadcrumb>
    <div class="header-right">
      <el-tag v-if="stores.user.roles.length" size="small" type="info">
        {{ stores.user.roles.join(',') }}
      </el-tag>
      <el-dropdown @command="handleCommand">
        <span class="user-info">
          <el-avatar :size="32" icon="UserFilled" />
          <span>{{ stores.user.nickname || stores.user.username }}</span>
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

.header-right,
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info {
  cursor: pointer;
  color: #333;
}
</style>

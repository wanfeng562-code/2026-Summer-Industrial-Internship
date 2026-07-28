<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserInfoStore } from '@/stores/userInfo'

const route = useRoute()
const stores = useUserInfoStore()

const menus = computed(() => {
  const roles = stores.user.roles
  const all = [
    { path: '/home', title: '工作台', icon: 'Odometer', roles: ['USER', 'AGENT', 'ADMIN'] },
    { path: '/home/tickets', title: '工单管理', icon: 'Document', roles: ['USER', 'AGENT', 'ADMIN'] },
    { path: '/home/create', title: '创建工单', icon: 'Plus', roles: ['USER'] },
    { path: '/home/orders', title: '订单管理', icon: 'ShoppingCart', roles: ['USER', 'ADMIN'] },
    { path: '/home/policies', title: '售后策略', icon: 'Setting', roles: ['ADMIN'] },
    { path: '/home/chat', title: 'AI客服', icon: 'ChatDotRound', roles: ['USER', 'AGENT', 'ADMIN'] },
  ]
  return all.filter((menu) => menu.roles.some((role) => roles.includes(role)))
})
</script>

<template>
  <el-aside class="aside">
    <div class="logo">
      <el-icon size="24"><Service /></el-icon>
      <span class="logo-text">工单管理系统</span>
    </div>
    <el-menu
      :default-active="route.path"
      router
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      :collapse-transition="false"
    >
      <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
        <el-icon><component :is="item.icon" /></el-icon>
        <template #title>{{ item.title }}</template>
      </el-menu-item>
    </el-menu>
  </el-aside>
</template>

<style scoped>
.aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
  min-height: 100vh;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #3a4a5c;
}

.logo-text {
  white-space: nowrap;
}

.el-menu {
  border-right: none;
}
</style>

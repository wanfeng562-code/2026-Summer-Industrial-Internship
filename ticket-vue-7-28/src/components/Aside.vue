<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

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
        class="el-menu-vertical-demo"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        :collapse-transition="false"
      >
        <el-menu-item index="/home">
          <el-icon><Odometer /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>
        <el-menu-item index="/home/tickets">
          <el-icon><Document /></el-icon>
          <template #title>工单管理</template>
        </el-menu-item>
        <el-menu-item index="/home/create">
          <el-icon><Plus /></el-icon>
          <template #title>创建工单</template>
        </el-menu-item>
        <el-menu-item index="/home/orders">
          <el-icon><ShoppingCart /></el-icon>
          <template #title>订单管理</template>
        </el-menu-item>
        <el-menu-item index="/home/policies">
          <el-icon><Setting /></el-icon>
          <template #title>售后策略</template>
        </el-menu-item>
        <el-menu-item index="/home/chat">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>AI客服</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
</template>

<style scoped>
.aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
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
  cursor: pointer;
  border-bottom: 1px solid #3a4a5c;
}
.logo-text {
  white-space: nowrap;
}
.el-menu {
  border-right: none;
}
</style>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => (route.meta.title as string) || '功能开发中')
const hint = computed(
  () => (route.meta.placeholder as string) || '该页面由其他成员接入，当前仅为路由占位。',
)
</script>

<template>
  <el-card>
    <el-empty :description="title">
      <p class="hint">{{ hint }}</p>
    </el-empty>
  </el-card>
</template>

<style scoped>
.hint {
  color: #909399;
  margin-top: 8px;
}
</style>

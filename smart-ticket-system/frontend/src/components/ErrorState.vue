<script setup lang="ts">
defineProps<{
  message?: string
  title?: string
}>()

defineEmits<{
  retry: []
}>()
</script>

<template>
  <div class="error-state">
    <el-result icon="error" :title="title || '加载失败'" :sub-title="message || '请稍后重试'">
      <template #extra>
        <el-button type="primary" @click="$emit('retry')">重试</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.error-state {
  padding: 24px 0;
}
</style>

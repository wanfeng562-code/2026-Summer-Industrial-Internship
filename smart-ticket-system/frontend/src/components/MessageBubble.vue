<script setup lang="ts">
import type { TicketMessageVo } from '@/api/ticket/type'

defineProps<{
  message: TicketMessageVo
}>()
</script>

<template>
  <div class="message-item" :class="`sender-${message.senderType?.toLowerCase()}`">
    <div class="message-avatar">
      <el-avatar v-if="message.senderType === 'USER'" icon="UserFilled" :size="36" />
      <el-avatar
        v-else-if="message.senderType === 'AI'"
        :size="36"
        style="background: linear-gradient(135deg, #409eff, #764ba2)"
      >
        AI
      </el-avatar>
      <el-avatar v-else icon="Service" :size="36" style="background: #67c23a" />
    </div>
    <div class="message-body">
      <div class="message-header">
        <span class="sender-name">
          {{ message.senderName || (message.senderType === 'AI' ? 'AI客服' : '系统') }}
        </span>
        <el-tag
          v-if="message.senderType === 'AI'"
          type="success"
          size="small"
          effect="plain"
          style="margin-left: 4px"
        >
          Agent
        </el-tag>
        <span class="message-time">{{ message.createTime }}</span>
      </div>
      <div class="message-content">{{ message.content }}</div>
    </div>
  </div>
</template>

<style scoped>
.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.message-body {
  flex: 1;
  min-width: 0;
}
.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.sender-name {
  font-weight: 600;
  color: #303133;
}
.message-time {
  margin-left: auto;
  color: #909399;
  font-size: 12px;
}
.message-content {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 10px 12px;
  white-space: pre-wrap;
  line-height: 1.5;
}
.sender-ai .message-content {
  background: #ecf5ff;
}
.sender-agent .message-content,
.sender-admin .message-content {
  background: #f0f9eb;
}
</style>

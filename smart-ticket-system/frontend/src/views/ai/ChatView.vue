<script setup lang="ts">
</script>

<template>
  <el-card>
    <template #header>
      <span>AI 客服</span>
    </template>
    <el-alert
      type="info"
      :closable="false"
      title="页面容器已就绪"
      description="流式对话、停止生成等能力由成员 C 接入。当前仅提供 Layout 内路由入口，便于联调时替换业务逻辑。"
      show-icon
    />
    <div class="chat-shell">
      <el-empty description="暂未接入 AI 对话接口" />
    </div>
  </el-card>
</template>

<style scoped>
.chat-shell {
  min-height: 360px;
  margin-top: 16px;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>

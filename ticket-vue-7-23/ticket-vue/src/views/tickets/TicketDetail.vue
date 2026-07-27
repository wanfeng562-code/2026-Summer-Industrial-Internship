<template>
  <div class="ticket-detail" v-loading="loading">
    <el-row :gutter="20">
      <!-- 工单信息 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>工单信息</span>
              <el-tag >{{ 0}}</el-tag>
            </div>
          </template>0
          <el-descriptions :column="1" border>
            <el-descriptions-item label="工单编号">{{ 0 }}</el-descriptions-item>
            <el-descriptions-item label="标题">{{ 0 }}</el-descriptions-item>
            <el-descriptions-item label="分类">
              <el-tag size="small">{{ 0 }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="优先级">
              <el-tag 0effect="dark" size="small">
                {{ 0 }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="关联订单">{{ '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建用户">{{ 0 }}</el-descriptions-item>
            <el-descriptions-item label="处理客服">{{ '未分配' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ 0 }}</el-descriptions-item>
            <el-descriptions-item label="SLA截止">{{ 0 }}</el-descriptions-item>
            <el-descriptions-item label="工单描述">
              <div style="white-space: pre-wrap;">{{ 0 }}</div>
            </el-descriptions-item>
          </el-descriptions>

          <div class="action-buttons">
            <el-button  type="success" @click="handleResolve">
              标记已解决
            </el-button>
            <el-button type="primary" @click="handleClose">
              关闭工单
            </el-button>
            <el-button type="warning" @click="handleAssign">
              接单处理
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 沟通记录 -->
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>沟通记录</span>
          </template>

          <!-- 消息列表 -->
          <div class="message-list" ref="messageListRef">
            <div class="message-item" >
              <div class="message-avatar">
                <el-avatar v-if="'USER' === 'USER'" icon="UserFilled" :size="36" />
                <el-avatar v-else-if="'AI'=== 'AI'" :size="36" style="background: linear-gradient(135deg, #409eff, #764ba2);">AI</el-avatar>
                <el-avatar v-else icon="Service" :size="36" style="background: #67c23a;" />
              </div>
              <div class="message-body">
                <div class="message-header">
                  <span class="sender-name">{{ true ? 'AI客服(小智)' : '系统' }}</span>
                  <el-tag  type="success" size="small" effect="plain" style="margin-left: 4px;">Agent</el-tag>
                  <span class="message-time">{{ 0 }}</span>
                </div>
                <div class="message-content">{{ 0 }}</div>
                <!-- AI反馈按钮（适用于AI_SUGGESTION类型消息） -->
                <div  class="feedback-actions">
                  <el-button-group>
                    <el-button size="small" type="success" >采纳</el-button>
                    <el-button size="small" type="danger" >驳回</el-button>
                    <el-button size="small" >修改</el-button>
                  </el-button-group>
                  <el-tag  size="small" type="info" style="margin-left: 8px;">
                    
                  </el-tag>
                </div>
                <!-- AI自动处理完成提示（适用于AI_REPLY类型消息） -->
                <div  class="ai-auto-badge">
                  <el-tag type="success" size="small" effect="plain">AI已自动处理</el-tag>
                </div>
              </div>
            </div>
            <el-empty description="暂无沟通记录" />
          </div>

          <!-- 发送消息 -->
          <div class="message-input">
            <el-input
              v-model="newMessage"
              type="textarea"
              :rows="3"
              placeholder="输入回复内容..."
              @keyup.enter.ctrl="sendMessage"
            />
            <div class="input-footer">
              <span class="tip">Ctrl + Enter 发送</span>
              <el-button type="primary" :loading="sending" @click="sendMessage">发送</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'


const route = useRoute()
const router = useRouter()

const loading = ref(false)
const sending = ref(false)
const newMessage = ref('')
const messageListRef = ref<HTMLElement>()

const statusTagType = (status?: string) => {
  const map: Record<string, string> = {
    PENDING: 'info', AI_PROCESSING: 'warning', MANUAL_REVIEW: '',
    RESOLVED: 'success', CLOSED: 'danger'
  }
  return map[status || ''] || 'info'
}

const priorityTagType = (priority?: string) => {
  const map: Record<string, string> = { LOW: 'info', MEDIUM: '', HIGH: 'warning', URGENT: 'danger' }
  return map[priority || ''] || ''
}

const priorityName = (priority?: string) => {
  const map: Record<string, string> = { LOW: '低', MEDIUM: '中', HIGH: '高', URGENT: '紧急' }
  return map[priority || ''] || ''
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const fetchTicket = async () => {
  loading.value = true
  try {
   
  } finally {
    loading.value = false
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim()) return
  sending.value = true
  try {
    
    ElMessage.success('发送成功')
  } finally {
    sending.value = false
  }
}

const handleFeedback = async (messageId: number, type: string) => {
  try {
    if (type === 'MODIFY') {
      
    } else {
     
    }
    ElMessage.success('反馈成功')
    await fetchTicket()
  } catch {}
}

const handleResolve = async () => {
  await ElMessageBox.confirm('确定将此工单标记为已解决？', '确认')
  
  ElMessage.success('操作成功')
  await fetchTicket()
}

const handleClose = async () => {
  await ElMessageBox.confirm('确定关闭此工单？', '确认')
  
  ElMessage.success('操作成功')
  await fetchTicket()
}

const handleAssign = async () => {
  await ElMessageBox.confirm('确定接单处理此工单？', '确认')
  
  ElMessage.success('接单成功')
  await fetchTicket()
}

onMounted(() => {
  fetchTicket()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 8px;
}
.message-list {
  max-height: 500px;
  overflow-y: auto;
  padding: 10px 0;
}
.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 8px;
  border-radius: 8px;
}
.message-item.user {
  flex-direction: row-reverse;
}
.message-item.user .message-body {
  align-items: flex-end;
}
.message-body {
  flex: 1;
}
.message-header {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 4px;
}
.message-item.user .message-header {
  flex-direction: row-reverse;
}
.sender-name {
  font-size: 13px;
  font-weight: 500;
  color: #333;
}
.message-time {
  font-size: 12px;
  color: #999;
}
.message-content {
  padding: 10px 14px;
  border-radius: 12px;
  max-width: 80%;
  line-height: 1.6;
  word-break: break-word;
}
.message-item.user .message-content {
  background: #409eff;
  color: #fff;
}
.message-item.agent .message-content {
  background: #f0f9eb;
  color: #333;
}
.message-item.ai .message-content {
  background: #ecf5ff;
  color: #333;
}
.message-item.system .message-content {
  background: #f4f4f5;
  color: #666;
  font-size: 13px;
}
.feedback-actions {
  margin-top: 8px;
}
.ai-auto-badge {
  margin-top: 6px;
}
.message-input {
  border-top: 1px solid #eee;
  padding-top: 16px;
  margin-top: 16px;
}
.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.tip {
  font-size: 12px;
  color: #999;
}
</style>

<template>
  <div class="ticket-detail" v-loading="loading">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>工单信息</span>
              <el-tag :type="statusTagType(ticket?.status)">{{ ticket?.statusName }}</el-tag>
            </div>
          </template>

          <el-descriptions :column="1" border>
            <el-descriptions-item label="工单编号">{{ ticket?.ticketNo }}</el-descriptions-item>
            <el-descriptions-item label="标题">{{ ticket?.title }}</el-descriptions-item>
            <el-descriptions-item label="分类">
              <el-tag size="small">{{ ticket?.categoryName }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="优先级">
              <el-tag :type="priorityTagType(ticket?.priority)" effect="dark" size="small">
                {{ priorityName(ticket?.priority) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="关联订单">{{ ticket?.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="创建用户">{{ ticket?.username }}</el-descriptions-item>
            <el-descriptions-item label="处理客服">{{ ticket?.agentName || '未分配' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ ticket?.createTime }}</el-descriptions-item>
            <el-descriptions-item label="SLA截止">
              <el-tag v-if="ticket?.slaEscalated" type="danger">已超时升级</el-tag>
              <el-tag v-else-if="ticket?.slaWarning" type="warning">即将超时</el-tag>
              <span v-else>{{ ticket?.slaDeadline }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="工单描述">
              <div class="description">{{ ticket?.description }}</div>
            </el-descriptions-item>
          </el-descriptions>

          <div class="action-buttons">
            <el-button v-if="canResolve" type="success" @click="handleResolve">标记已解决</el-button>
            <el-button v-if="canClose" type="primary" @click="handleClose">关闭工单</el-button>
            <el-button v-if="canClaim" type="warning" @click="handleClaim">接单处理</el-button>
            <el-button v-if="canTransferManual" type="warning" @click="handleTransferManual">
              转人工客服
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <template #header>
            <span>沟通记录</span>
          </template>

          <div ref="messageListRef" class="message-list">
            <div
              v-for="message in ticket?.messages"
              :key="message.id"
              class="message-item"
              :class="message.senderType.toLowerCase()"
            >
              <el-avatar :size="36">{{ avatarText(message.senderType) }}</el-avatar>
              <div class="message-body">
                <div class="message-header">
                  <span class="sender-name">{{ message.senderName }}</span>
                  <el-tag v-if="message.senderType === 'AI'" type="success" size="small">AI</el-tag>
                  <span class="message-time">{{ message.createTime }}</span>
                </div>
                <div class="message-content">{{ message.content }}</div>
              </div>
            </div>
            <el-empty v-if="!ticket?.messages?.length" description="暂无沟通记录" />
          </div>

          <div class="message-input">
            <el-input
              v-model="newMessage"
              type="textarea"
              :rows="3"
              :disabled="!canSendMessage"
              :placeholder="canSendMessage ? '输入回复内容...' : '当前状态或角色不能发送消息'"
              @keyup.ctrl.enter="sendMessage"
            />
            <div class="input-footer">
              <span class="tip">Ctrl + Enter 发送</span>
              <el-button
                type="primary"
                :loading="sending"
                :disabled="!canSendMessage"
                @click="sendMessage"
              >
                发送
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { TagProps } from 'element-plus'
import { useUserInfoStore } from '@/stores/userInfo'
import {
  requestAddTicketMsg,
  requestClaimTicket,
  requestCloseTicket,
  requestResolveTicket,
  requestTicketDetail,
  requestTransferManual,
} from '@/api/ticket'
import type { TicketVo } from '@/api/ticket/type'

const router = useRouter()
const userStore = useUserInfoStore()
const loading = ref(false)
const sending = ref(false)
const newMessage = ref('')
const messageListRef = ref<HTMLElement>()
const ticket = ref<TicketVo | null>(null)

const ticketId = computed(() => Number(router.currentRoute.value.params.id))
const currentUserId = computed(() => userStore.getUserId)
const currentRole = computed(() => userStore.getRoles[0] || '')

const canClaim = computed(() =>
  currentRole.value === 'AGENT'
  && ticket.value?.status === 'MANUAL_REVIEW'
  && !ticket.value.agentId
)

const canResolve = computed(() =>
  ticket.value?.status === 'MANUAL_REVIEW'
  && (currentRole.value === 'ADMIN'
    || (currentRole.value === 'AGENT' && ticket.value.agentId === currentUserId.value))
)

const canClose = computed(() =>
  ticket.value?.status === 'RESOLVED'
  && (currentRole.value === 'ADMIN'
    || (currentRole.value === 'AGENT' && ticket.value.agentId === currentUserId.value))
)

const canTransferManual = computed(() =>
  currentRole.value === 'USER' && ticket.value?.status === 'AI_PROCESSING'
)

const canSendMessage = computed(() => {
  if (!ticket.value || ticket.value.status === 'CLOSED') return false
  if (currentRole.value === 'USER') return ticket.value.userId === currentUserId.value
  if (currentRole.value === 'ADMIN') return true
  return currentRole.value === 'AGENT'
    && ticket.value.status === 'MANUAL_REVIEW'
    && ticket.value.agentId === currentUserId.value
})

const statusTagType = (status?: string): TagProps['type'] => {
  const map: Record<string, TagProps['type']> = {
    AI_PROCESSING: 'warning',
    MANUAL_REVIEW: 'primary',
    RESOLVED: 'success',
    CLOSED: 'info',
  }
  return map[status || ''] || 'info'
}

const priorityTagType = (priority?: string): TagProps['type'] => {
  const map: Record<string, TagProps['type']> = {
    LOW: 'info',
    MEDIUM: 'primary',
    HIGH: 'warning',
    URGENT: 'danger',
  }
  return map[priority || ''] || 'info'
}

const priorityName = (priority?: string) => {
  const map: Record<string, string> = { LOW: '低', MEDIUM: '中', HIGH: '高', URGENT: '紧急' }
  return map[priority || ''] || priority
}

const avatarText = (senderType: string) => {
  if (senderType === 'AI') return 'AI'
  if (senderType === 'AGENT') return '客'
  if (senderType === 'SYSTEM') return '系'
  return '用'
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const fetchTicket = async () => {
  if (!Number.isFinite(ticketId.value)) {
    ElMessage.error('工单ID不正确')
    return
  }
  loading.value = true
  try {
    const response = await requestTicketDetail(ticketId.value)
    ticket.value = response.data
    scrollToBottom()
  } finally {
    loading.value = false
  }
}

const sendMessage = async () => {
  if (!ticket.value || !newMessage.value.trim()) return
  sending.value = true
  try {
    await requestAddTicketMsg(ticket.value.id, { content: newMessage.value.trim() })
    newMessage.value = ''
    await fetchTicket()
    ElMessage.success('发送成功')
  } finally {
    sending.value = false
  }
}

const handleResolve = async () => {
  const { value } = await ElMessageBox.prompt('请输入解决说明', '标记已解决', {
    inputValidator: (text) => Boolean(text?.trim()) || '解决说明不能为空',
  })
  await requestResolveTicket(ticketId.value, { content: value.trim() })
  ElMessage.success('工单已标记为解决')
  await fetchTicket()
}

const handleClose = async () => {
  const { value } = await ElMessageBox.prompt('请输入关闭说明', '关闭工单', {
    inputValidator: (text) => Boolean(text?.trim()) || '关闭说明不能为空',
  })
  await requestCloseTicket(ticketId.value, { reason: value.trim() })
  ElMessage.success('工单已关闭')
  await fetchTicket()
}

const handleClaim = async () => {
  await ElMessageBox.confirm('确定接单处理此工单？', '确认')
  await requestClaimTicket(ticketId.value)
  ElMessage.success('接单成功')
  await fetchTicket()
}

const handleTransferManual = async () => {
  await ElMessageBox.confirm('确定将此工单转交人工客服？', '确认')
  await requestTransferManual(ticketId.value)
  ElMessage.success('已转人工处理')
  await fetchTicket()
}

onMounted(fetchTicket)
</script>

<style scoped>
.card-header,
.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.description {
  white-space: pre-wrap;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  flex-wrap: wrap;
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

.message-body {
  flex: 1;
}

.message-header {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 4px;
}

.sender-name {
  font-size: 13px;
  font-weight: 500;
}

.message-time,
.tip {
  font-size: 12px;
  color: #999;
}

.message-content {
  padding: 10px 14px;
  border-radius: 12px;
  line-height: 1.6;
  word-break: break-word;
  background: #f4f4f5;
}

.message-item.user .message-content {
  background: #ecf5ff;
}

.message-item.agent .message-content {
  background: #f0f9eb;
}

.message-item.ai .message-content {
  background: #fdf6ec;
}

.message-input {
  border-top: 1px solid #eee;
  padding-top: 16px;
  margin-top: 16px;
}

.input-footer {
  margin-top: 8px;
}
</style>

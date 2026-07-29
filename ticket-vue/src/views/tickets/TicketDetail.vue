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
            <el-descriptions-item label="负责坐席组">{{ ticket?.groupName || '未分组' }}</el-descriptions-item>
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
            <el-button v-if="canAssign" type="warning" @click="assignmentDialog = true">
              {{ ticket?.agentId ? '改派客服' : '分配客服' }}
            </el-button>
            <el-button v-if="canTransferManual" type="warning" @click="handleTransferManual">
              转人工客服
            </el-button>
            <el-button v-if="canFollowUp" type="primary" plain @click="handleFollowUp">跟进</el-button>
            <el-button v-if="canReject" type="danger" plain @click="handleReject">驳回</el-button>
            <el-button v-if="currentRole === 'ADMIN' && !ticket?.archived" plain @click="priorityDialog = true">调整优先级</el-button>
            <el-button v-if="canArchive" type="info" @click="handleArchive">归档</el-button>
            <el-button v-if="canSatisfy" type="success" plain @click="handleSatisfaction">满意度评价</el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <template #header>
            <span>沟通记录</span>
          </template>

          <div ref="messageListRef" class="message-list">
            <MessageBubble
              v-for="message in ticket?.messages"
              :key="message.id"
              :message="message"
            />
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

  <el-dialog v-model="assignmentDialog" title="分配处理客服" width="440px">
    <el-select v-model="selectedAgentId" filterable placeholder="请选择客服" class="agent-select">
      <el-option
        v-for="agent in availableAgents"
        :key="agent.id"
        :label="`${agent.nickname}（${agent.username}）`"
        :value="agent.id"
      />
    </el-select>
    <template #footer>
      <el-button @click="assignmentDialog = false">取消</el-button>
      <el-button type="primary" :disabled="!selectedAgentId" @click="handleAssign">
        确认分配
      </el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="priorityDialog" title="调整优先级" width="400px">
    <el-select v-model="selectedPriority" class="agent-select">
      <el-option label="低" value="LOW" /><el-option label="中" value="MEDIUM" />
      <el-option label="高" value="HIGH" /><el-option label="紧急" value="URGENT" />
    </el-select>
    <template #footer><el-button @click="priorityDialog = false">取消</el-button><el-button type="primary" @click="handlePriority">保存</el-button></template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { TagProps } from 'element-plus'
import { useUserInfoStore } from '@/stores/userInfo'
import MessageBubble from '@/components/MessageBubble.vue'
import {
  requestAddTicketMsg,
  requestAssignTicket,
  requestClaimTicket,
  requestCloseTicket,
  requestResolveTicket,
  requestTicketDetail,
  requestTransferManual,
  requestFollowUpTicket,
  requestRejectTicket,
  requestArchiveTicket,
  requestAdjustPriority,
  requestSatisfaction,
} from '@/api/ticket'
import { requestUserPage } from '@/api/user'
import type { TicketVo } from '@/api/ticket/type'
import type { UserProfile } from '@/api/user/type'

const router = useRouter()
const userStore = useUserInfoStore()
const loading = ref(false)
const sending = ref(false)
const newMessage = ref('')
const messageListRef = ref<HTMLElement>()
const ticket = ref<TicketVo | null>(null)
const agents = ref<UserProfile[]>([])
const assignmentDialog = ref(false)
const selectedAgentId = ref<number>()
const priorityDialog = ref(false)
const selectedPriority = ref('MEDIUM')

const ticketId = computed(() => Number(router.currentRoute.value.params.id))
const currentUserId = computed(() => userStore.getUserId)
const currentRole = computed(() => userStore.getRoles[0] || '')
const availableAgents = computed(() => {
  if (!ticket.value?.groupId) return agents.value
  return agents.value.filter((agent) => agent.agentGroupId === ticket.value?.groupId)
})

const canClaim = computed(() =>
  currentRole.value === 'AGENT'
  && ticket.value?.status === 'MANUAL_REVIEW'
  && !ticket.value.agentId
)

const canAssign = computed(() =>
  currentRole.value === 'ADMIN' && ticket.value?.status === 'MANUAL_REVIEW'
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

const canFollowUp = computed(() => Boolean(ticket.value) && !ticket.value?.archived
  && ticket.value?.status !== 'CLOSED'
  && (currentRole.value === 'USER'
    || (currentRole.value === 'AGENT' && ticket.value?.agentId === currentUserId.value)
    || currentRole.value === 'ADMIN'))
const canReject = computed(() => ticket.value?.status === 'MANUAL_REVIEW'
  && (currentRole.value === 'ADMIN'
    || (currentRole.value === 'AGENT' && ticket.value?.agentId === currentUserId.value)))
const canArchive = computed(() => currentRole.value === 'ADMIN' && !ticket.value?.archived
  && ['CLOSED', 'REJECTED'].includes(ticket.value?.status || ''))
const canSatisfy = computed(() => currentRole.value === 'USER' && ticket.value?.status === 'CLOSED')

const canSendMessage = computed(() => {
  if (!ticket.value || ticket.value.archived
    || ['CLOSED', 'REJECTED'].includes(ticket.value.status)) return false
  if (currentRole.value === 'USER') return ticket.value.userId === currentUserId.value
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
    REJECTED: 'danger',
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
    selectedAgentId.value = response.data.agentId ?? undefined
    selectedPriority.value = response.data.priority
    scrollToBottom()
  } finally {
    loading.value = false
  }
}

const loadAgents = async () => {
  if (currentRole.value !== 'ADMIN') return
  const response = await requestUserPage(1, 100, 'AGENT')
  agents.value = response.data.records
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

const handleAssign = async () => {
  if (!selectedAgentId.value) return
  await requestAssignTicket(ticketId.value, selectedAgentId.value)
  assignmentDialog.value = false
  ElMessage.success('处理客服已更新')
  await fetchTicket()
}

const handleTransferManual = async () => {
  await ElMessageBox.confirm('确定将此工单转交人工客服？', '确认')
  await requestTransferManual(ticketId.value)
  ElMessage.success('已转人工处理')
  await fetchTicket()
}

const handleFollowUp = async () => {
  const { value } = await ElMessageBox.prompt('请输入跟进内容', '工单跟进', {
    inputValidator: (text) => Boolean(text?.trim()) || '跟进内容不能为空',
  })
  await requestFollowUpTicket(ticketId.value, value.trim()); ElMessage.success('跟进已保存'); await fetchTicket()
}

const handleReject = async () => {
  const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回工单', {
    inputValidator: (text) => Boolean(text?.trim()) || '驳回原因不能为空',
  })
  await requestRejectTicket(ticketId.value, value.trim()); ElMessage.success('工单已驳回'); await fetchTicket()
}

const handleArchive = async () => {
  await ElMessageBox.confirm('归档后工单将进入归档列表，历史记录仍保留。确定继续？', '归档工单')
  await requestArchiveTicket(ticketId.value); ElMessage.success('工单已归档'); await fetchTicket()
}

const handlePriority = async () => {
  await requestAdjustPriority(ticketId.value, selectedPriority.value)
  priorityDialog.value = false; ElMessage.success('优先级已更新'); await fetchTicket()
}

const handleSatisfaction = async () => {
  const { value: score } = await ElMessageBox.prompt('请输入 1-5 分', '满意度评价', {
    inputPattern: /^[1-5]$/, inputErrorMessage: '评分必须是 1-5 的整数',
  })
  const { value: comment } = await ElMessageBox.prompt('请输入评价说明（可简短填写）', '评价说明')
  await requestSatisfaction(ticketId.value, Number(score), comment || '')
  ElMessage.success('感谢你的评价')
}

onMounted(() => {
  void Promise.all([fetchTicket(), loadAgents()])
})
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

.agent-select {
  width: 100%;
}

.input-footer {
  margin-top: 8px;
}
</style>

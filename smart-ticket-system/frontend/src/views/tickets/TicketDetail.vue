<template>
  <div class="ticket-detail" v-loading="loading">
    <ErrorState v-if="errorMsg" :message="errorMsg" @retry="fetchTicket" />

    <el-row v-else-if="ticket" :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>工单信息</span>
              <el-tag :type="statusTagType(ticket.status) as any">{{ ticket.statusName }}</el-tag>
            </div>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="工单编号">{{ ticket.ticketNo }}</el-descriptions-item>
            <el-descriptions-item label="标题">{{ ticket.title }}</el-descriptions-item>
            <el-descriptions-item label="分类">
              <el-tag size="small">{{ ticket.categoryName || ticket.category }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="优先级">
              <el-tag :type="priorityTagType(ticket.priority) as any" effect="dark" size="small">
                {{ priorityName(ticket.priority) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="关联订单">{{ ticket.orderNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建用户">{{ ticket.username }}</el-descriptions-item>
            <el-descriptions-item label="处理客服">{{ ticket.agentName || '未分配' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ ticket.createTime }}</el-descriptions-item>
            <el-descriptions-item label="SLA截止">{{ ticket.slaDeadline }}</el-descriptions-item>
            <el-descriptions-item label="工单描述">
              <div style="white-space: pre-wrap">{{ ticket.description }}</div>
            </el-descriptions-item>
          </el-descriptions>

          <div class="action-buttons">
            <el-button v-if="ticket.status === 'MANUAL_REVIEW'" type="success" @click="handleResolve">
              标记已解决
            </el-button>
            <el-button v-if="ticket.status === 'RESOLVED'" type="primary" @click="handleClose">
              关闭工单
            </el-button>
            <el-button v-if="!ticket.agentId" type="warning" @click="handleAssign">
              接单处理
            </el-button>
            <el-text type="info" size="small">状态动作由成员 C 完善业务校验</el-text>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <template #header>
            <span>沟通记录</span>
          </template>

          <div class="message-list" ref="messageListRef">
            <MessageBubble v-for="msg in ticket.messages" :key="msg.id" :message="msg" />
            <el-empty v-if="!ticket.messages?.length" description="暂无沟通记录" />
          </div>

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
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { requestTicketDetail, requestAddTicketMsg } from '@/api/ticket'
import type { TicketVo } from '@/api/ticket/type'
import MessageBubble from '@/components/MessageBubble.vue'
import ErrorState from '@/components/ErrorState.vue'

const route = useRoute()
const loading = ref(false)
const sending = ref(false)
const errorMsg = ref('')
const newMessage = ref('')
const messageListRef = ref<HTMLElement>()
const ticket = ref<TicketVo | null>(null)

const statusTagType = (status?: string) => {
  const map: Record<string, string> = {
    PENDING: 'info',
    AI_PROCESSING: 'warning',
    MANUAL_REVIEW: '',
    RESOLVED: 'success',
    CLOSED: 'danger',
  }
  return map[status || ''] || 'info'
}

const priorityTagType = (priority?: string) => {
  const map: Record<string, string> = { LOW: 'info', MEDIUM: '', HIGH: 'warning', URGENT: 'danger' }
  return map[priority || ''] || ''
}

const priorityName = (priority?: string) => {
  const map: Record<string, string> = { LOW: '低', MEDIUM: '中', HIGH: '高', URGENT: '紧急' }
  return map[priority || ''] || priority || '-'
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const resolveTicketId = () => {
  const raw = route.params.id
  const id = Number(Array.isArray(raw) ? raw[0] : raw)
  return Number.isFinite(id) ? id : null
}

const fetchTicket = async () => {
  const ticketId = resolveTicketId()
  if (ticketId == null) {
    errorMsg.value = '无效的工单 ID'
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await requestTicketDetail(ticketId)
    if (res.code === 200) {
      ticket.value = res.data
      scrollToBottom()
    } else {
      errorMsg.value = res.msg || '加载工单详情失败'
    }
  } catch {
    errorMsg.value = '网络异常，工单详情加载失败'
  } finally {
    loading.value = false
  }
}

const sendMessage = async () => {
  if (!newMessage.value.trim() || !ticket.value?.id) return
  sending.value = true
  try {
    const res = await requestAddTicketMsg(ticket.value.id, {
      ticketId: ticket.value.id,
      content: newMessage.value.trim(),
    })
    if (res.code === 200) {
      newMessage.value = ''
      await fetchTicket()
      ElMessage.success('发送成功')
    } else {
      ElMessage.error(res.msg || '发送失败')
    }
  } finally {
    sending.value = false
  }
}

const handleResolve = async () => {
  await ElMessageBox.confirm('确定将此工单标记为已解决？', '确认')
  ElMessage.info('状态动作待成员 C 接入真实接口')
}

const handleClose = async () => {
  await ElMessageBox.confirm('确定关闭此工单？', '确认')
  ElMessage.info('状态动作待成员 C 接入真实接口')
}

const handleAssign = async () => {
  await ElMessageBox.confirm('确定接单处理此工单？', '确认')
  ElMessage.info('接单动作待成员 C 接入真实接口')
}

onMounted(fetchTicket)
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
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
}
.message-list {
  max-height: 500px;
  overflow-y: auto;
  padding: 10px 0;
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

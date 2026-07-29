<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { requestAiSessionMessages, requestAiSessions, streamAiChat, type AiChatSession } from '@/api/ai'
import { useUserInfoStore } from '@/stores/userInfo'

interface ChatMessage {
  id: number
  role: 'USER' | 'AI'
  content: string
  error?: boolean
}

const router = useRouter()
const userStore = useUserInfoStore()
const input = ref('')
const sessions = ref<AiChatSession[]>([])
const currentSessionNo = ref<string>()
const generating = ref(false)
const messages = ref<ChatMessage[]>([
  {
    id: Date.now(),
    role: 'AI',
    content: '你好，我是小智。你可以咨询退款、物流、商品破损和发票等售后问题。',
  },
])
const messageList = ref<HTMLElement>()
const LAST_SESSION_KEY = 'ticket-ai-current-session'
let controller: AbortController | null = null
let streamTimer: number | null = null
let streamTimedOut = false

const scrollToBottom = async () => {
  await nextTick()
  if (messageList.value) messageList.value.scrollTop = messageList.value.scrollHeight
}

const loadSessions = async () => {
  const response = await requestAiSessions()
  sessions.value = response.data
}

const openSession = async (sessionNo: string) => {
  if (generating.value) return
  const response = await requestAiSessionMessages(sessionNo)
  currentSessionNo.value = sessionNo
  sessionStorage.setItem(LAST_SESSION_KEY, sessionNo)
  messages.value = response.data.map((item) => ({ id: item.id, role: item.senderType, content: item.content }))
  await scrollToBottom()
}

const newSession = () => {
  if (generating.value) return
  currentSessionNo.value = undefined
  sessionStorage.removeItem(LAST_SESSION_KEY)
  messages.value = [{ id: Date.now(), role: 'AI', content: '你好，我是小智。你可以咨询退款、物流、商品破损和发票等售后问题。' }]
}

const initializeChat = async () => {
  try {
    await loadSessions()
    if (!sessions.value.length) return
    const rememberedSessionNo = sessionStorage.getItem(LAST_SESSION_KEY)
    const target = sessions.value.find((session) => session.sessionNo === rememberedSessionNo)
      ?? sessions.value[0]
    if (target) await openSession(target.sessionNo)
  } catch {
    ElMessage.error('AI 会话历史加载失败')
  }
}

const send = async () => {
  const content = input.value.trim()
  if (!content || generating.value) return
  if (content.length > 2000) {
    ElMessage.warning('消息不能超过 2000 个字符')
    return
  }

  messages.value.push({ id: Date.now(), role: 'USER', content })
  const aiMessage: ChatMessage = { id: Date.now() + 1, role: 'AI', content: '' }
  messages.value.push(aiMessage)
  input.value = ''
  generating.value = true
  controller = new AbortController()
  streamTimedOut = false
  streamTimer = window.setTimeout(() => {
    streamTimedOut = true
    controller?.abort()
  }, 70000)
  await scrollToBottom()

  try {
    await streamAiChat(content, currentSessionNo.value, controller.signal, {
      onSession: (sessionNo) => {
        currentSessionNo.value = sessionNo
        sessionStorage.setItem(LAST_SESSION_KEY, sessionNo)
      },
      onMessage: (chunk) => {
        aiMessage.content += chunk
        void scrollToBottom()
      },
      onError: (message) => {
        aiMessage.error = true
        aiMessage.content ||= message
      },
    })
    if (!aiMessage.content) aiMessage.content = '暂时没有生成有效回复，请稍后再试。'
    await loadSessions()
  } catch (error) {
    if ((error as Error).name === 'AbortError') {
      aiMessage.error = streamTimedOut
      aiMessage.content ||= streamTimedOut ? 'AI 响应超时，请稍后重试。' : '本次生成已停止。'
    } else {
      aiMessage.error = true
      aiMessage.content ||= (error as Error).message || 'AI 服务暂时不可用。'
    }
  } finally {
    if (streamTimer !== null) window.clearTimeout(streamTimer)
    streamTimer = null
    generating.value = false
    controller = null
    await scrollToBottom()
  }
}

const stop = () => controller?.abort()

const createTicket = () => {
  const lastQuestion = [...messages.value].reverse().find((item) => item.role === 'USER')?.content
  router.push({ path: '/home/create', query: lastQuestion ? { description: lastQuestion } : {} })
  ElMessage.info('请补充订单和问题信息后提交，复杂诉求将进入受控工单流程')
}

onMounted(() => {
  void initializeChat()
})
</script>

<template>
  <section class="chat-page">
    <el-card class="history" shadow="never">
      <template #header><div class="history-header"><strong>会话历史</strong><el-button link type="primary" @click="newSession">新建</el-button></div></template>
      <el-empty v-if="!sessions.length" description="暂无历史" :image-size="60" />
      <button v-for="session in sessions" :key="session.id" class="history-item" :class="{ active: currentSessionNo === session.sessionNo }" @click="openSession(session.sessionNo)">
        <span>{{ session.title }}</span><small>{{ session.updateTime }}</small>
      </button>
    </el-card>
    <el-card class="chat-card" shadow="never">
      <template #header>
        <div class="header">
          <div>
            <h2>AI 智能客服</h2>
            <p>AI 只提供建议，不会直接执行退款、关闭工单或修改业务数据。</p>
          </div>
          <el-button v-if="userStore.isUser" type="warning" plain @click="createTicket">
            转人工 / 创建工单
          </el-button>
        </div>
      </template>

      <div ref="messageList" class="messages" aria-live="polite">
        <div
          v-for="message in messages"
          :key="message.id"
          class="message-row"
          :class="{ user: message.role === 'USER' }"
        >
          <div class="bubble" :class="{ error: message.error }">
            <strong>{{ message.role === 'USER' ? '我' : '小智' }}</strong>
            <div class="content">{{ message.content || '正在思考…' }}</div>
          </div>
        </div>
      </div>

      <div class="composer">
        <el-input
          v-model="input"
          type="textarea"
          :rows="3"
          maxlength="2000"
          show-word-limit
          resize="none"
          placeholder="请输入售后问题，Enter 发送，Shift+Enter 换行"
          :disabled="generating"
          @keydown.enter.exact.prevent="send"
        />
        <div class="actions">
          <el-button v-if="generating" type="danger" plain @click="stop">停止生成</el-button>
          <el-button v-else type="primary" :disabled="!input.trim()" @click="send">发送</el-button>
        </div>
      </div>
    </el-card>
  </section>
</template>

<style scoped>
.chat-page {
  height: calc(100vh - 120px);
  min-height: 560px;
  display: grid;
  grid-template-columns: 250px 1fr;
  gap: 16px;
}

.history{height:100%;overflow:hidden}.history :deep(.el-card__body){height:calc(100% - 58px);overflow-y:auto;padding:10px}.history-header{display:flex;justify-content:space-between}.history-item{width:100%;border:0;background:transparent;text-align:left;padding:10px;border-radius:6px;cursor:pointer;color:#606266}.history-item:hover,.history-item.active{background:#ecf5ff;color:#409eff}.history-item span,.history-item small{display:block;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.history-item small{margin-top:4px;color:#909399;font-size:11px}

.chat-card {
  height: 100%;
}

.chat-card :deep(.el-card__body) {
  height: calc(100% - 92px);
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

h2 {
  margin: 0 0 6px;
}

.header p {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px 8px 24px;
}

.message-row {
  display: flex;
  margin-bottom: 16px;
}

.message-row.user {
  justify-content: flex-end;
}

.bubble {
  max-width: 72%;
  padding: 12px 16px;
  border-radius: 12px;
  background: #f4f4f5;
  color: #303133;
}

.user .bubble {
  background: #409eff;
  color: #fff;
}

.bubble.error {
  background: #fef0f0;
  color: #f56c6c;
}

.content {
  margin-top: 6px;
  line-height: 1.65;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.composer {
  border-top: 1px solid #ebeef5;
  padding-top: 16px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

@media (max-width: 900px){.chat-page{grid-template-columns:1fr}.history{display:none}}
</style>

import request from '@/utils/request'
import router from '@/router'
import { useUserInfoStore } from '@/stores/userInfo'
import type { R } from '@/api/ticket/type'

export interface AiChatResponse {
  content: string
  sessionNo: string
}

export interface AiChatSession {
  id: number
  sessionNo: string
  title: string
  updateTime: string
}

export interface AiChatHistoryMessage {
  id: number
  senderType: 'USER' | 'AI'
  content: string
  createTime: string
}

export const requestAiChat = (message: string, sessionNo?: string) => {
  return request.post<any, R<AiChatResponse>>('/ai/chat', { message, sessionNo })
}

export const requestAiSessions = () =>
  request.get<any, R<AiChatSession[]>>('/ai/chat/sessions')

export const requestAiSessionMessages = (sessionNo: string) =>
  request.get<any, R<AiChatHistoryMessage[]>>(
    `/ai/chat/sessions/${encodeURIComponent(sessionNo)}/messages`,
  )

export interface AiStreamHandlers {
  onMessage: (content: string) => void
  onDone?: () => void
  onError?: (message: string) => void
  onSession?: (sessionNo: string) => void
}

/**
 * 使用 fetch 消费 POST SSE。调用方持有 AbortController，可显式停止本次生成。
 */
export const streamAiChat = async (
  message: string,
  sessionNo: string | undefined,
  signal: AbortSignal,
  handlers: AiStreamHandlers,
) => {
  const userStore = useUserInfoStore()
  const response = await fetch('/api/ai/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json;charset=utf-8',
      ...(userStore.getToken ? { Authorization: `Bearer ${userStore.getToken}` } : {}),
    },
    body: JSON.stringify({ message, sessionNo }),
    signal,
  })
  if (!response.ok || !response.body) {
    let message = response.status === 401 ? '登录已失效，请重新登录' : 'AI 服务连接失败'
    try {
      const errorBody = await response.clone().json() as { msg?: string }
      message = errorBody.msg || message
    } catch {
      // 非 JSON 错误响应沿用按状态码生成的安全提示。
    }
    if (response.status === 401) {
      userStore.clearUser()
      if (router.currentRoute.value.path !== '/login') {
        void router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
      }
    }
    throw new Error(message)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let eventName = 'message'

  while (true) {
    const { done, value } = await reader.read()
    buffer += decoder.decode(value, { stream: !done }).replace(/\r\n/g, '\n')
    const frames = buffer.split('\n\n')
    buffer = frames.pop() ?? ''

    for (const frame of frames) {
      const dataLines: string[] = []
      for (const line of frame.split('\n')) {
        if (line.startsWith('event:')) eventName = line.slice(6).trim()
        if (line.startsWith('data:')) dataLines.push(line.slice(5).replace(/^ /, ''))
      }
      const data = dataLines.join('\n')
      if (eventName === 'session') handlers.onSession?.(data)
      else if (eventName === 'done') handlers.onDone?.()
      else if (eventName === 'error') handlers.onError?.(data)
      else if (data) handlers.onMessage(data)
      eventName = 'message'
    }
    if (done) break
  }
}

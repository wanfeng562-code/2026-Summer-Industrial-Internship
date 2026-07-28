import request from '@/utils/request'
import { useUserInfoStore } from '@/stores/userInfo'
import type { R } from '@/api/ticket/type'

export interface AiChatResponse {
  content: string
}

export const requestAiChat = (message: string) => {
  return request.post<any, R<AiChatResponse>>('/ai/chat', { message })
}

export interface AiStreamHandlers {
  onMessage: (content: string) => void
  onDone?: () => void
  onError?: (message: string) => void
}

/**
 * 使用 fetch 消费 POST SSE。调用方持有 AbortController，可显式停止本次生成。
 */
export const streamAiChat = async (
  message: string,
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
    body: JSON.stringify({ message }),
    signal,
  })
  if (!response.ok || !response.body) {
    throw new Error(response.status === 401 ? '登录已失效，请重新登录' : 'AI 服务连接失败')
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
      let data = ''
      for (const line of frame.split('\n')) {
        if (line.startsWith('event:')) eventName = line.slice(6).trim()
        if (line.startsWith('data:')) data += line.slice(5).trimStart()
      }
      if (eventName === 'done') handlers.onDone?.()
      else if (eventName === 'error') handlers.onError?.(data)
      else if (data) handlers.onMessage(data)
      eventName = 'message'
    }
    if (done) break
  }
}

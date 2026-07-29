import request from '@/utils/request'
import type { Page, R } from '@/api/ticket/type'

export type TicketCategory = string

export interface AfterSalePolicy {
  id: number
  policyName: string
  category: TicketCategory
  conditionType: 'ALWAYS' | 'AMOUNT' | 'REPUTATION' | 'AMOUNT_REPUTATION'
  minAmount: number | null
  maxAmount: number | null
  minReputation: number | null
  action: 'AUTO_APPROVE' | 'AUTO_REPLY' | 'MANUAL'
  replyTemplate: string | null
  priority: number
  enabled: number
  slaHours: number | null
}

export type PolicyRequest = Omit<AfterSalePolicy, 'id'>

export interface Faq {
  id: number
  category: TicketCategory
  question: string
  answer: string
  keywords: string | null
  enabled: number
}

export type FaqRequest = Omit<Faq, 'id'>

export const requestPolicyPage = (current: number, size = 10) =>
  request.get<any, R<Page<AfterSalePolicy>>>(`/policies?current=${current}&size=${size}`)

export const requestCreatePolicy = (data: PolicyRequest) =>
  request.post<any, R<AfterSalePolicy>>('/policies', data)

export const requestUpdatePolicy = (id: number, data: PolicyRequest) =>
  request.put<any, R<AfterSalePolicy>>(`/policies/${id}`, data)

export const requestPolicyEnabled = (id: number, enabled: number) =>
  request.patch<any, R<null>>(`/policies/${id}/enabled`, { enabled })

export const requestDeletePolicy = (id: number) =>
  request.delete<any, R<null>>(`/policies/${id}`)

export const requestFaqPage = (current: number, size = 10, keyword = '') =>
  request.get<any, R<Page<Faq>>>('/faqs', {
    params: { current, size, keyword: keyword || undefined },
  })

export const requestCreateFaq = (data: FaqRequest) =>
  request.post<any, R<Faq>>('/faqs', data)

export const requestUpdateFaq = (id: number, data: FaqRequest) =>
  request.put<any, R<Faq>>(`/faqs/${id}`, data)

export const requestDeleteFaq = (id: number) =>
  request.delete<any, R<null>>(`/faqs/${id}`)

export interface FaqSemanticConfig {
  id: number
  enabled: number
  similarityThreshold: number
  maxCandidates: number
  maxResults: number
  updateTime: string
}

export interface SemanticFaqResult {
  faq: Faq
  similarity: number
}

export const requestFaqSemanticConfig = () =>
  request.get<any, R<FaqSemanticConfig>>('/faqs/semantic-config')

export const requestUpdateFaqSemanticConfig = (data: Omit<FaqSemanticConfig, 'id' | 'updateTime'>) =>
  request.put<any, R<FaqSemanticConfig>>('/faqs/semantic-config', data)

export const requestFaqSemanticSearch = (question: string, category?: string) =>
  request.get<any, R<{ mode: string; results: SemanticFaqResult[] }>>('/faqs/semantic-search', {
    params: { question, category },
  })

export const requestImportFaqCsv = (file: File) => {
  const form = new FormData()
  form.append('file', file)
  return request.post<any, R<number>>('/faqs/import', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export const downloadFaqCsv = () =>
  request.get<any, Blob>('/faqs/export', { responseType: 'blob' })

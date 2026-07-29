import request from '@/utils/request'
import type { R } from '@/api/ticket/type'

export interface AgentGroup {
  id: number
  groupName: string
  leaderId: number | null
  description: string | null
  enabled: number
}

export interface AgentGroupRequest {
  groupName: string
  leaderId: number | null
  description: string
  enabled: number
  agentIds: number[]
}

export interface TicketCategoryConfig {
  id: number
  categoryCode: string
  categoryName: string
  groupId: number | null
  enabled: number
}

export const requestAgentGroups = (includeDisabled = false) =>
  request.get<any, R<AgentGroup[]>>('/agent-groups', { params: { includeDisabled } })

export const requestCreateAgentGroup = (data: AgentGroupRequest) =>
  request.post<any, R<AgentGroup>>('/agent-groups', data)

export const requestUpdateAgentGroup = (id: number, data: AgentGroupRequest) =>
  request.put<any, R<AgentGroup>>(`/agent-groups/${id}`, data)

export const requestDeleteAgentGroup = (id: number) =>
  request.delete<any, R<null>>(`/agent-groups/${id}`)

export const requestTicketCategories = (includeDisabled = false) =>
  request.get<any, R<TicketCategoryConfig[]>>('/ticket-categories', { params: { includeDisabled } })

export const requestCreateTicketCategory = (data: Omit<TicketCategoryConfig, 'id'>) =>
  request.post<any, R<TicketCategoryConfig>>('/ticket-categories', data)

export const requestUpdateTicketCategory = (id: number, data: Omit<TicketCategoryConfig, 'id'>) =>
  request.put<any, R<TicketCategoryConfig>>(`/ticket-categories/${id}`, data)

export const requestDeleteTicketCategory = (id: number) =>
  request.delete<any, R<null>>(`/ticket-categories/${id}`)

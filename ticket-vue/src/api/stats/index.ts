import request from '@/utils/request'
import type { R } from '@/api/ticket/type'

export interface DashboardStats {
  total: number
  aiProcessing: number
  manualReview: number
  resolved: number
  rejected: number
  closed: number
  slaWarning: number
  slaEscalated: number
  categoryCounts: Record<string, number>
}

export const requestDashboardStats = () =>
  request.get<any, R<DashboardStats>>('/stats/tickets')

export interface ServiceReport {
  ticketCount: number
  receptionCount: number
  aiReplyCount: number
  transferToHumanCount: number
  completedCount: number
  aiReplyRate: number
  transferToHumanRate: number
  completionRate: number
  averageSatisfaction: number
  satisfactionCount: number
  agentReceptionCounts: Record<string, number>
}

export const requestServiceReport = (year?: number, month?: number) =>
  request.get<any, R<ServiceReport>>('/stats/service', { params: { year, month } })

export const downloadServiceReport = (year?: number, month?: number) =>
  request.get<any, Blob>('/stats/service/export', { params: { year, month }, responseType: 'blob' })

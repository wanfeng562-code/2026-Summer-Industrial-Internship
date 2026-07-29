import request from '@/utils/request'
import type { R } from '@/api/ticket/type'

export interface DashboardStats {
  total: number
  aiProcessing: number
  manualReview: number
  resolved: number
  closed: number
  slaWarning: number
  slaEscalated: number
  categoryCounts: Record<string, number>
}

export const requestDashboardStats = () =>
  request.get<any, R<DashboardStats>>('/stats/tickets')

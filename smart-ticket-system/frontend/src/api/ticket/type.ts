export interface R<T> {
  code: number
  msg: string
  data: T
}

export interface Page<T> {
  total: number
  size: number
  current: number
  pages?: number
  records: T[]
}

export interface TicketVo {
  id?: number
  ticketNo: string
  userId: number
  username: string
  userNickname: string
  agentId: number
  agentName: string
  orderId: number
  orderNo: string
  title: string
  description: string
  category: string
  categoryName: string
  status: string
  statusName: string
  priority: string
  slaWarning: number
  slaEscalated: number
  slaDeadline: string
  createTime: string
  updateTime: string
  messages: TicketMessageVo[]
}

export interface TicketMessageVo {
  id?: number
  ticketId: number
  userId: number
  senderName: string
  senderType: string
  messageType: string
  content: string
  aiProcessResult: string
  humanFeedback: string
  createTime: string
}

export interface Orders {
  id?: number
  orderNo: string
  userId: number
  productName: string
  quantity: number
  unitPrice: number
  totalAmount: number
  orderStatus: string
  paymentStatus: string
  logisticsStatus: string
  logisticsNo: string
  orderTime: string
  payTime: string
  deliverTime: string
  receiveTime: string
  deleted?: number
  createTime?: string
  updateTime?: string
}

export interface OrderVo extends Orders {
  username?: string
}

export interface TicketCreateRequest {
  ordersId: number
  title: string
  description: string
  category?: string
}

export interface MessageRequest {
  ticketId: number
  content: string
}

export interface TicketUpdateRequest {
  status?: string
  category?: string
  priority?: string
  agentId?: number
}

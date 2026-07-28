import request from '@/utils/request'
import type {
  R,
  Page,
  TicketVo,
  Orders,
  OrderVo,
  TicketCreateRequest,
  MessageRequest,
  TicketUpdateRequest,
} from './type'

enum API {
  TICKET_PAGE = '/tickets',
  TICKET_DETAIL = '/tickets',
  ORDER_PAGE = '/orders',
  ORDER_DETAIL = '/orders/detail',
  CREATE_TICKET = '/tickets',
}

export const requestTicketPage = (current: number, size = 10) => {
  return request.get<any, R<Page<TicketVo>>>(`${API.TICKET_PAGE}?current=${current}&size=${size}`)
}

export const requestTicketDetail = (ticketId: number) => {
  return request.get<any, R<TicketVo>>(`${API.TICKET_DETAIL}/${ticketId}`)
}

export const requestOrdersList = (page = 1, pageSize = 10) => {
  return request.get<any, R<Page<Orders>>>(`${API.ORDER_PAGE}/${page}/${pageSize}`)
}

export const requestOrderDetail = (id: number) => {
  return request.get<any, R<OrderVo>>(`${API.ORDER_DETAIL}/${id}`)
}

export const requestCreateTicket = (data: TicketCreateRequest) => {
  return request.post<any, R<TicketVo>>(API.CREATE_TICKET, data)
}

export const requestAddTicketMsg = (ticketId: number, data: MessageRequest) => {
  return request.post<any, R<null>>(`${API.CREATE_TICKET}/${ticketId}/messages`, data)
}

export const requestUpdateTicket = (ticketId: number, data: TicketUpdateRequest) => {
  return request.put<any, R<null>>(`${API.CREATE_TICKET}/${ticketId}`, data)
}

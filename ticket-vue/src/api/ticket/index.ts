import request from "@/utils/request";
import type {
    R, Page, TicketVo, Orders, OrderVo, TicketCreateRequest, MessageRequest,
    TicketResolveRequest, TicketCloseRequest
} from './type'

//定义登录的URL
enum API{
    TICKET_PAGE = "/tickets",
    TICKET_DETAIL = "/tickets",
    ORDER_LIST = "/orders",
    CREATE_TICKET = "/tickets",
}

//发送工单分页列表请求接口
export const requestTicketPage = (current:number, size = 10)=>{
    return request.get<any, R<Page<TicketVo>>>(`${API.TICKET_PAGE}?current=${current}&size=${size}`)
}

//发送工单详情请求接口
export const requestTicketDetail = (ticketId:number)=>{
    return request.get<any, R<TicketVo>>(`${API.TICKET_DETAIL}/${ticketId}`)
}

//发送订单列表请求接口
export const requestOrdersList = (current = 1, size = 10)=>{
    return request.get<any, R<Page<Orders>>>(`${API.ORDER_LIST}?current=${current}&size=${size}`)
}

export const requestOrderDetail = (id:number)=>{
    return request.get<any, R<OrderVo>>(`${API.ORDER_LIST}/${id}`)
}

//创建工单请求接口
export const requestCreateTicket = (data : TicketCreateRequest)=>{
    return request.post<any, R<TicketVo>>(API.CREATE_TICKET, data)
}

//创建发送工单消息请求接口
export const requestAddTicketMsg =(ticketId:number, data:MessageRequest)=>{
    return request.post<any, R<null>>(`${API.CREATE_TICKET}/${ticketId}/messages`, data)
}

export const requestClaimTicket = (ticketId:number)=>{
    return request.post<any, R<null>>(`${API.TICKET_DETAIL}/${ticketId}/claim`)
}

export const requestAssignTicket = (ticketId:number, agentId:number)=>{
    return request.put<any, R<null>>(`${API.TICKET_DETAIL}/${ticketId}/assignee`, { agentId })
}

export const requestResolveTicket = (ticketId:number, data:TicketResolveRequest)=>{
    return request.post<any, R<null>>(`${API.TICKET_DETAIL}/${ticketId}/resolve`, data)
}

export const requestCloseTicket = (ticketId:number, data:TicketCloseRequest)=>{
    return request.post<any, R<null>>(`${API.TICKET_DETAIL}/${ticketId}/close`, data)
}

export const requestTransferManual = (ticketId:number)=>{
    return request.post<any, R<null>>(`${API.TICKET_DETAIL}/${ticketId}/transfer-manual`)
}

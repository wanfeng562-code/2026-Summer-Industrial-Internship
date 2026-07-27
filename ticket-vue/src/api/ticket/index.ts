import request from "@/utils/request";
import type {R, Page, TicketVo, TicketMessageVo, Orders, TicketCreateRequest} from './type'

//定义登录的URL
enum API{
    TICKET_PAGE = "/tickets",
    TICKET_DETAIL = "/tickets",
    ORDER_LIST = "/orders/1/6",
    CREATE_TICKET = "/tickets",
}

//发送工单分页列表请求接口
export const requestTicketPage = (current:number)=>{
    return request.get<any, R<Page<TicketVo>>>(`${API.TICKET_PAGE}?current=${current}&size=3`)
}

//发送工单详情请求接口
export const requestTicketDetail = (ticketId:number)=>{
    return request.get<any, R<TicketVo>>(`${API.TICKET_DETAIL}/${ticketId}`)
}

//发送订单列表请求接口
export const requestOrdersList = ()=>{
    return request.get<any, R<Orders>>(API.ORDER_LIST)
}

//创建工单请求接口
export const requestCreateTicket = (data : TicketCreateRequest)=>{
    return request.post<any, R<TicketVo>>(API.CREATE_TICKET, data)
}
import request from "@/utils/request";
import type {R, Page, TicketVo, TicketMessageVo} from './type'

//定义登录的URL
enum API{
    TICKET_PAGE = "/tickets",
}

//发送用户登录请求接口
export const requestTicketPage = ()=>{
    return request.get<any, R<Page<TicketVo>>>(API.TICKET_PAGE)
}
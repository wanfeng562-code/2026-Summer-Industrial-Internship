
//定义R类接口
export interface R<T>{
    code:number
    msg:string
    data:T
}

//定义Page类的接口
export interface Page<T>{
    total:number
    size:number
    current:number
    pages?:number
    records:Array<T>
}


//定义TicketVo类的接口
export interface TicketVo{
    id: number
    ticketNo: string
    userId: number
    username: string
    userNickname: string
    agentId:number | null
    agentName:string | null
    groupId:number | null
    groupName:string | null
    orderId:number
    orderNo:string
    title: string
    description: string
    category: string
    categoryName: string
    status:string
    statusName:string
    priority:string
    slaWarning:number
    slaEscalated:number
    slaDeadline:string
    resolveTime:string | null
    closeTime:string | null
    archived:number
    archiveTime:string | null
    createTime:string
    updateTime:string
    messages:Array<TicketMessageVo>
}

//定义工单消息
export interface TicketMessageVo {
  id?: number
  ticketId:number
  userId:number
  senderName:string
  senderType:string
  messageType:string
  content:string
  aiProcessResult:string
  humanFeedback:string
  createTime:string
}

//定义订单信息
export interface Orders {
  id:number
  orderNo:string
  userId:number
  productName:string
  quantity:number
  unitPrice:number
  totalAmount:number
  orderStatus:string
  paymentStatus:string
  logisticsStatus:string
  logisticsNo:string | null
  orderTime:string
  payTime:string | null
  deliverTime:string | null
  receiveTime:string | null
  deleted?:number
  createTime?:string
  updateTime?:string
}

export interface OrderVo extends Orders {
  username?:string
}

//定义创建工单表单
export interface TicketCreateRequest{
    orderId:number
    title:string
    description:string
    category?:string
    priority?:string
}

export interface MessageRequest{
    content:string
}

export interface TicketResolveRequest {
    content:string
}

export interface TicketCloseRequest {
    reason:string
}

export interface TicketQuery {
  keyword?: string
  status?: string
  category?: string
  priority?: string
  archived?: boolean
}


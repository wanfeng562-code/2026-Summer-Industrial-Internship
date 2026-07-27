
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
    records:Array<T>
}


//定义TicketVo类的接口
export interface TicketVo{
    id?: number
    ticketNo: string
    userId: number
    username: string
    userNickname: string
    agentId:number
    agentName:string
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
  id?: number
  orderNo:string
  userId:number
  productName:string
  quantity:number
  unitPrice:number
  totalAmount:number
  orderStatus:string
  paymentStatus:string
  logisticsStatus:string
  logisticsNo:string
  orderTime:string
  payTime:string
  deliverTime:string
  receiveTime:string
  deleted:number
  createTime:string
  updateTime:string
}

//定义创建工单表单
export interface TicketCreateRequest{
    ordersId:number
    title:string
    description:string
    category:string
}


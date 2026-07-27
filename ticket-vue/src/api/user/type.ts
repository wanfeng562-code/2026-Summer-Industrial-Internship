
//定义R类接口
export interface R<T>{
    code:number
    msg:string
    data:T
}

//定义UserInfo接口
export interface UserInfo{
    userId:number
    username:string
    nickname:string
    token:string
    roles:Array<string>
    permissions:Array<string>
}

//定义提交LoginRequest接口
export interface LoginRequest{
    username:string
    password:string
}
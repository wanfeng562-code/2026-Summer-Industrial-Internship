import request from "@/utils/request";
import type {R, UserInfo, LoginRequest} from './type'

//定义登录的URL
enum API{
    LOGIN_URL = "/user/login",
    LOGOUT_URL = "/user/logout"
}

//发送用户登录请求接口
export const requestLogin = (data : LoginRequest)=>{
    return request.post<any, R<UserInfo>>(API.LOGIN_URL, data)
}

//发送用户退出请求接口
export const requestLogout = ()=>{
    return request.get<any, R<null>>(API.LOGOUT_URL)
}
import request from '@/utils/request'
import type { R, UserInfo, LoginRequest, RegisterRequest, ProfileUpdateRequest } from './type'

enum API {
  LOGIN_URL = '/user/login',
  LOGOUT_URL = '/user/logout',
  REGISTER_URL = '/user/register',
  PROFILE_URL = '/user/profile',
}

export const requestLogin = (data: LoginRequest) => {
  return request.post<any, R<UserInfo>>(API.LOGIN_URL, data)
}

export const requestLogout = () => {
  return request.get<any, R<null>>(API.LOGOUT_URL)
}

export const requestRegister = (data: RegisterRequest) => {
  return request.post<any, R<UserInfo>>(API.REGISTER_URL, data)
}

export const requestProfile = () => {
  return request.get<any, R<UserInfo>>(API.PROFILE_URL)
}

export const requestUpdateProfile = (data: ProfileUpdateRequest) => {
  return request.put<any, R<UserInfo>>(API.PROFILE_URL, data)
}

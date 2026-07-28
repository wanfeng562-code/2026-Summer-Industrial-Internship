import request from '@/utils/request'
import type {
  R,
  UserInfo,
  UserProfile,
  LoginRequest,
  RegisterRequest,
  ProfileUpdateRequest,
} from './type'

enum API {
  LOGIN_URL = '/user/login',
  LOGOUT_URL = '/user/logout',
  REGISTER_URL = '/user/register',
  PROFILE_URL = '/user/profile',
}

export const requestLogin = (data: LoginRequest) =>
  request.post<any, R<UserInfo>>(API.LOGIN_URL, data)

export const requestLogout = () =>
  request.post<any, R<null>>(API.LOGOUT_URL)

export const requestRegister = (data: RegisterRequest) =>
  request.post<any, R<UserProfile>>(API.REGISTER_URL, data)

export const requestProfile = () =>
  request.get<any, R<UserProfile>>(API.PROFILE_URL)

export const requestUpdateProfile = (data: ProfileUpdateRequest) =>
  request.put<any, R<UserProfile>>(API.PROFILE_URL, data)

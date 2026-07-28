// 统一响应
export interface R<T> {
  code: number
  msg: string
  data: T
}

export interface UserInfo {
  userId: number
  username: string
  nickname: string
  token: string
  roles: string[]
  permissions: string[]
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  nickname: string
  email?: string
  phone?: string
}

export interface ProfileUpdateRequest {
  nickname?: string
  email?: string
  phone?: string
  password?: string
}

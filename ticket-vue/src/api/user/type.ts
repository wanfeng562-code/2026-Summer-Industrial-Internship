export interface R<T> {
  code: number
  msg: string
  data: T
}

export interface UserInfo {
  id?: number
  userId: number
  username: string
  nickname: string
  token: string
  role?: string
  roles: string[]
  permissions: string[]
}

export interface UserProfile {
  id: number
  username: string
  nickname: string
  email: string | null
  phone: string | null
  avatar: string | null
  role: string
  reputationScore: number
  createTime: string
  updateTime: string
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
  nickname: string
  email?: string
  phone?: string
  avatar?: string
}

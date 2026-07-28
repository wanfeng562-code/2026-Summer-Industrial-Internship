import { defineStore } from 'pinia'

export interface UserState {
  userId: number
  username: string
  nickname: string
  token: string
  roles: string[]
  permissions: string[]
}

export const useUserInfoStore = defineStore('userInfo', {
  state: () => ({
    user: {
      userId: 0,
      username: '',
      nickname: '',
      token: '',
      roles: [] as string[],
      permissions: [] as string[],
    } as UserState,
  }),
  getters: {
    isLogin: (state) => !!state.user.token,
    getUserId: (state) => state.user.userId,
    getUsername: (state) => state.user.username,
    getNickname: (state) => state.user.nickname,
    getToken: (state) => state.user.token,
    getRoles: (state) => state.user.roles,
    getPermissions: (state) => state.user.permissions,
    isAdmin: (state) => state.user.roles.includes('ADMIN'),
    isAgent: (state) => state.user.roles.includes('AGENT'),
    isUser: (state) => state.user.roles.includes('USER'),
  },
  actions: {
    setUser(payload: Partial<UserState>) {
      this.user = { ...this.user, ...payload }
    },
    clearUser() {
      this.user = {
        userId: 0,
        username: '',
        nickname: '',
        token: '',
        roles: [],
        permissions: [],
      }
    },
  },
  persist: {
    key: 'user',
    pick: ['user'],
  },
})

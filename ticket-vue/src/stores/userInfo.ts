import { defineStore } from 'pinia'

export interface UserState {
  userId: number
  username: string
  nickname: string
  token: string
  roles: string[]
  permissions: string[]
}

const emptyUser = (): UserState => ({
  userId: 0,
  username: '',
  nickname: '',
  token: '',
  roles: [],
  permissions: [],
})

export const useUserInfoStore = defineStore('userInfo', {
  state: () => ({
    user: emptyUser(),
  }),
  getters: {
    isLogin: (state) => Boolean(state.user.token),
    getUserId: (state) => state.user.userId,
    getUsername: (state) => state.user.username,
    getNickname: (state) => state.user.nickname,
    getRealName: (state) => state.user.nickname,
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
      this.user = emptyUser()
    },
  },
  persist: {
    key: 'user',
    pick: ['user'],
  },
})

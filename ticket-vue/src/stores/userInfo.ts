import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useUserInfoStore = defineStore('userInfo', {

  state:()=>{
    return {
      user:{
        userId:0,
        username:"",
        realName:"",
        token:"",
        roles:Array<string>(),
        permissions:Array<string>()
      }
    }
  },
  getters: {
    getUserId: (state) => state.user.userId,
    getUsername: (state) => state.user.username,
    getRealName: (state) => state.user.realName,
    getToken: (state) => state.user.token,
    getRoles: (state) => state.user.roles,
    getPermissions: (state) => state.user.permissions,
  },
  actions: {
    setUserId(userId : number){
        this.user.userId = userId
      },
    setUsername(username : string){
      this.user.username = username
    },
    setRealName(realName : string){
      this.user.realName = realName
    },
    setToken(token : string){
      this.user.token = token
    },
    setRoles(roles : Array<string>){
      this.user.roles = roles
    },
    setPermissions(permissions : Array<string>){
      this.user.permissions = permissions
    },
  },
  persist:{
    enabled:true, // 需要开启才可以持久化
    strategies:[
      {
        key:"user", // 自定义缓存的key
        paths:["user"], // 默认会缓存所有state数据，可以指定名称进行缓存
        storage:window.localStorage // 缓存地址
      }
    ]
  }

})
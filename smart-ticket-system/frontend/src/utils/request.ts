import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserInfoStore } from '@/stores/userInfo'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json;charset=utf-8' },
})

request.interceptors.request.use((config) => {
  const stores = useUserInfoStore()
  const token = stores.user.token
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

const clearLoginAndRedirect = () => {
  const stores = useUserInfoStore()
  stores.clearUser()
  if (router.currentRoute.value.path !== '/login') {
    router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
  }
}

request.interceptors.response.use(
  (response) => {
    const data = response.data
    // 业务码 401：清理登录态
    if (data && data.code === 401) {
      clearLoginAndRedirect()
      ElMessage.error(data.msg || '登录已失效')
      return Promise.reject(data)
    }
    return data
  },
  (error) => {
    const status = error.response?.status
    const msgFromBody = error.response?.data?.msg
    let message = msgFromBody || '网络出现问题'
    switch (status) {
      case 401:
        message = msgFromBody || '未登录或登录已失效'
        clearLoginAndRedirect()
        break
      case 403:
        message = msgFromBody || '无权限访问'
        break
      case 404:
        message = msgFromBody || '请求地址错误'
        break
      case 400:
        message = msgFromBody || '请求参数错误'
        break
      case 500:
        message = msgFromBody || '服务器出现问题'
        break
      default:
        if (!error.response) {
          message = '网络连接失败'
        }
    }
    ElMessage({ type: 'error', message })
    return Promise.reject(error)
  },
)

export default request

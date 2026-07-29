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
  if (stores.user.token) {
    config.headers.Authorization = `Bearer ${stores.user.token}`
  }
  return config
})

const clearLoginAndRedirect = () => {
  const stores = useUserInfoStore()
  stores.clearUser()
  if (router.currentRoute.value.path !== '/login') {
    void router.push({
      path: '/login',
      query: { redirect: router.currentRoute.value.fullPath },
    })
  }
}

request.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data?.code === 401) {
      clearLoginAndRedirect()
      ElMessage.error(data.msg || '登录已失效')
      return Promise.reject(data)
    }
    return data
  },
  async (error) => {
    const status = error.response?.status
    let msgFromBody = error.response?.data?.msg as string | undefined
    if (!msgFromBody && error.response?.data instanceof Blob) {
      try {
        const body = JSON.parse(await error.response.data.text()) as { msg?: string }
        msgFromBody = body.msg
      } catch {
        // 下载接口返回的内容不是 JSON 时，使用通用 HTTP 错误提示。
      }
    }
    let message = msgFromBody || '网络出现问题'
    switch (status) {
      case 400:
        message = msgFromBody || '请求参数错误'
        break
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
      case 409:
        message = msgFromBody || '当前数据已发生变化，请刷新后重试'
        break
      case 500:
        message = msgFromBody || '服务器出现问题'
        break
      case 503:
        message = msgFromBody || '服务暂时不可用'
        break
      default:
        if (!error.response) message = '网络连接失败'
    }
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export default request

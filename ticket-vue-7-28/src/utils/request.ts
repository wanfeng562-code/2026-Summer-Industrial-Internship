import axios from "axios"  //导入axios
import { ElMessage } from 'element-plus'
import { useUserInfoStore } from '@/stores/userInfo';
const stores = useUserInfoStore();

  //第一步:创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {'Content-Type': 'application/json;charset=utf-8'}
});

//第二步:request实例添加请求拦截器
request.interceptors.request.use((config:any) => {
  //获取用户相关的小仓库:获取仓库内部token,登录成功以后携带给服务器
  const token = stores.user.token
  if (token) {
    config.headers.Authorization = "Bearer " + token
  }
  //config配置对象,headers属性请求头,经常给服务器端携带公共参数
  //返回配置对象
  return config
})


//第三步:响应拦截器
request.interceptors.response.use(
  (response : any) => {
    //成功回调
    //简化数据
    return response.data
  },
  (error : any) => {
    //失败回调:处理http网络错误的
    //定义一个变量:存储网络错误信息
    let message = ''
    //http状态码
    const status = error.response.status
    switch (status) {
      case 401:
        message = 'TOKEN过期'
        break
      case 403:
        message = '无权限访问'
        break
      case 404:
        message = '请求地址错误'
        break
      case 500:
        message = '服务器出现问题'
        break
      default:
        message = '网络出现问题'
        break
    }
    //提示错误信息
    ElMessage({
      type: 'error',
      message,
    })
    return Promise.reject(error)
  },
)
  export default request;
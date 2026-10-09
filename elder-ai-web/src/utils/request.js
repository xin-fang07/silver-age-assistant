// ============================================================
// request.js - Axios 请求封装
// 银发智能生活助手 - 统一 HTTP 请求模块
// 功能：请求拦截（添加Token）、响应拦截（错误处理）
// ============================================================

import axios from 'axios'
import { toast } from '@/composables/useToast'
import { getToken, removeToken, removeUser } from './auth'
import router from '@/router'

// ========== 工厂：创建带统一拦截器的 Axios 实例 ==========
function createRequest(baseURL, timeout) {
  const instance = axios.create({
    baseURL,
    timeout,
    headers: { 'Content-Type': 'application/json' }
  })

  // 请求拦截器：自动附加 Bearer Token
  instance.interceptors.request.use(
    (config) => {
      const token = getToken()
      if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
      }
      return config
    },
    (error) => {
      console.error('请求发送失败：', error)
      return Promise.reject(error)
    }
  )

  // 响应拦截器：统一解包 { code, message, data } 并处理错误
  instance.interceptors.response.use(
    (response) => {
      window.dispatchEvent(new CustomEvent('app-network-restored'))
      const res = response.data
      if (res.code && res.code !== 200 && res.code !== 0) {
        const requestId = res.requestId || response.headers?.['x-request-id']
        if (!response.config.skipErrorToast) {
          const suffix = res.code >= 500 && requestId ? `（请求编号：${requestId}）` : ''
          toast.error((res.message || '请求失败，请稍后重试') + suffix)
        }
        const businessError = new Error(res.message || '请求失败')
        businessError.requestId = requestId
        return Promise.reject(businessError)
      }
      return res // 直接返回 data 层
    },
    (error) => {
      if (!error.response) window.dispatchEvent(new CustomEvent('app-network-error'))
      const status = error.response ? error.response.status : 0
      const silent = Boolean(error.config?.skipErrorToast)
      const requestId = error.response?.data?.requestId || error.response?.headers?.['x-request-id']
      error.requestId = requestId
      switch (status) {
        case 401:
          toast.error('登录状态已失效，请重新登录')
          removeToken()
          removeUser()
          router.push('/login')
          break
        case 403:
          if (!silent) toast.error(error.response?.data?.message || '您没有权限执行此操作')
          break
        case 404:
          if (!silent) toast.error(error.response?.data?.message || '请求的资源不存在')
          break
        case 413:
          if (!silent) toast.error(error.response?.data?.message || '上传文件过大')
          break
        case 429:
          if (!silent) toast.error(error.response?.data?.message || '操作过于频繁，请稍后再试')
          break
        case 500:
          if (!silent) {
            const suffix = requestId ? `（请求编号：${requestId}）` : ''
            toast.error(`服务器繁忙，请稍后重试${suffix}`)
          }
          break
        default:
          if (!silent) toast.error(error.response?.data?.message || '网络连接失败，请检查网络')
          break
      }
      return Promise.reject(error)
    }
  )

  return instance
}

// ========== 请求实例 ==========
// request：业务主后端（Java），Vite 代理 /api -> http://localhost:8080
const request = createRequest('/api', 30000)

export { request }
export default request

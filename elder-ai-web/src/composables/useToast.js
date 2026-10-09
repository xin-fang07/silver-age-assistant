// ============================================================
// useToast.js - 全局 Toast / 通知 统一封装
// 银发智能生活助手 - 统一消息提示风格，避免散落的 ElMessage 调用
// ============================================================
import { ElMessage, ElNotification } from 'element-plus'

/**
 * 统一的消息提示
 * 用法：const toast = useToast(); toast.success('保存成功')
 */
export function useToast() {
  const base = (type, message, opts = {}) => {
    return ElMessage({ type, message, duration: 2200, showClose: true, ...opts })
  }
  return {
    success: (m, o) => base('success', m, o),
    error:   (m, o) => base('error', m, { duration: 3000, ...o }),
    warning: (m, o) => base('warning', m, { duration: 2600, ...o }),
    info:    (m, o) => base('info', m, o),
    // 顶部右侧通知（用于较重要的系统事件）
    notify: (title, message, type = 'info', o = {}) =>
      ElNotification({ title, message, type, duration: 3000, position: 'top-right', ...o })
  }
}

// 同时导出单例，方便非 setup 上下文（如 request 拦截器）调用
export const toast = useToast()

// ============================================================
// auth.js - 认证工具模块
// 银发智能生活助手 - 用户认证相关工具函数
// 功能：Token 管理、用户信息管理、登录状态判断
// ============================================================

// localStorage 中的存储键名常量
const TOKEN_KEY = 'elder_ai_token'       // 认证 Token
const USER_KEY = 'elder_ai_user'         // 用户信息对象

// ============================================================
// Token 管理
// ============================================================

/**
 * 从 localStorage 获取认证 Token
 * @returns {string|null} 保存的 Token，若不存在则返回 null
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 保存认证 Token 到 localStorage
 * @param {string} token - 后端返回的 JWT Token
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 从 localStorage 删除认证 Token
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

// ============================================================
// 用户信息管理
// ============================================================

/**
 * 从 localStorage 获取用户信息
 * @returns {object|null} 用户信息对象，含 username、role 等字段；不存在返回 null
 */
export function getUser() {
  const userStr = localStorage.getItem(USER_KEY)
  if (userStr) {
    try {
      return JSON.parse(userStr)
    } catch (e) {
      // 解析失败，删除无效数据
      removeUser()
      return null
    }
  }
  return null
}

/**
 * 保存用户信息到 localStorage
 * @param {object} user - 用户信息对象（通常包含 username、role、nickname 等）
 */
export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

/**
 * 从 localStorage 删除用户信息
 */
export function removeUser() {
  localStorage.removeItem(USER_KEY)
}

// ============================================================
// 便捷查询函数
// ============================================================

/**
 * 获取当前用户角色
 * @returns {string} 用户角色，如 'ADMIN'、'USER'；未登录返回空字符串
 */
export function getRole() {
  // 优先从 JWT token 解码 role（后端签发必带，最可靠）
  const t = getToken()
  if (t) {
    const p = decodeTokenPayload(t)
    if (p && p.role) return p.role
  }
  // 兜底：从 localStorage 用户对象读取
  const user = getUser()
  return user ? user.role : ''
}

/**
 * 判断用户是否已登录
 * 判断依据：localStorage 中存在 Token 且 Token 未过期
 * @returns {boolean} true-已登录，false-未登录
 */
export function isLoggedIn() {
  return !!getToken() && !isTokenExpired()
}

/**
 * 解码 JWT Token 的 payload（不验证签名）
 * @param {string} token - JWT Token
 * @returns {object|null} payload 对象，解析失败返回 null
 */
export function decodeTokenPayload(token) {
  if (!token || token.split('.').length !== 3) return null
  try {
    const payload = token.split('.')[1]
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const json = atob(base64)
    return JSON.parse(json)
  } catch (e) {
    return null
  }
}

/**
 * 判断 Token 是否已过期
 * @returns {boolean} true-已过期或无效，false-有效
 */
export function isTokenExpired() {
  const token = getToken()
  if (!token) return true
  const payload = decodeTokenPayload(token)
  if (!payload || !payload.exp) return false
  return payload.exp * 1000 < Date.now()
}

/**
 * 获取当前用户 ID
 * @returns {number|null} 用户 ID，未登录返回 null
 */
export function getCurrentUserId() {
  const user = getUser()
  return user ? (user.id || user.userId) : null
}

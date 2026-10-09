// ============================================================
// index.js - API 接口封装
// 银发智能生活助手 - 所有后端 API 接口定义
// 功能：分类管理所有后端接口，统一使用 request.js 请求模块
// ============================================================

import request from '@/utils/request'

// ============================================================
// 1. 用户认证相关 API（登录、注册）
// ============================================================
export const authApi = {
  /**
   * 用户登录
   * @param {object} data - { username: 用户名, password: 密码 }
   * @returns {Promise} 返回 token 和用户信息
   */
  login(data) {
    return request({
      url: '/auth/login',
      method: 'post',
      data
    })
  },

  /**
   * 用户注册
   * @param {object} data - { username, password, nickname, phone 等 }
   * @returns {Promise}
   */
  register(data) {
    return request({
      url: '/auth/register',
      method: 'post',
      data
    })
  },

  /**
   * 忘记密码 - 发送验证码
   * @param {object} data - { email: 邮箱 }
   * @returns {Promise}
   */
  forgotPassword(data) {
    return request({
      url: '/auth/forgot-password',
      method: 'post',
      data
    })
  },

  /**
   * 重置密码
   * @param {object} data - { email: 邮箱, code: 验证码, newPassword: 新密码 }
   * @returns {Promise}
   */
  resetPassword(data) {
    return request({
      url: '/auth/reset-password',
      method: 'post',
      data
    })
  }
}

// ============================================================
// 2. 用户信息相关 API
// ============================================================
export const userApi = {
  /**
   * 获取当前登录用户的个人信息
   * @returns {Promise}
   */
  getUserInfo() {
    return request({
      url: '/user/info',
      method: 'get',
      skipErrorToast: true
    })
  },

  /**
   * 更新老年人信息（健康状况、紧急联系人等）
   * @param {object} data - 老年人相关信息
   * @returns {Promise}
   */
  updateElderInfo(data) {
    return request({
      url: '/user/elder-info',
      method: 'put',
      data
    })
  },

  /**
   * 更新用户基本信息（头像、昵称、电话）
   * @param {object} data - 用户基本信息
   * @returns {Promise}
   */
  updateProfile(data) {
    return request({
      url: '/user/profile',
      method: 'put',
      data
    })
  },

  /**
   * 上传头像
   * @param {File} file - 头像文件
   * @returns {Promise}
   */
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/common/upload-avatar',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  requestDeletion(data = {}) {
    return request({ url: '/user/deletion-request', method: 'post', data })
  }
}

export const notificationApi = {
  /**
   * 获取通知列表
   * @param {string} type - 通知类型（可选）
   * @returns {Promise}
   */
  list(type, options = {}) {
    return request({
      url: '/notification/list',
      method: 'get',
      params: { type },
      ...options
    })
  },

  /**
   * 标记单个通知为已读
   * @param {number} id - 通知ID
   * @returns {Promise}
   */
  read(id) {
    return request({
      url: '/notification/read',
      method: 'post',
      data: { id }
    })
  },

  /**
   * 标记所有通知为已读
   * @returns {Promise}
   */
  readAll() {
    return request({
      url: '/notification/read-all',
      method: 'post'
    })
  }
}

export const exportApi = {
  /**
   * 导出健康记录为 Excel
   */
  exportHealthExcel() {
    return request({
      url: '/export/health/excel',
      method: 'get',
      responseType: 'blob'
    })
  },

  /**
   * 导出聊天记录为 JSON
   */
  exportChatJson() {
    return request({
      url: '/export/chat/json',
      method: 'get',
      responseType: 'blob'
    })
  },

  /**
   * 生成健康报告
   */
  generateHealthReport() {
    return request({
      url: '/export/health/report',
      method: 'get'
    })
  }
}

// ============================================================
// 3. 智能问答（聊天）相关 API
// ============================================================
export const chatApi = {
  /**
   * 发送问题，获取 AI 回答（转发到 Java 后端 /api/chat/ask）
   * @param {object} data - { question: 问题内容, city: 当前城市(可选) }
   * @returns {Promise} 返回 AI 回复内容（结构：{question, answer, isFallback, createTime}）
   */
  ask(data) {
    return request({
      url: '/chat/ask',
      method: 'post',
      data
    })
  }
}

// ============================================================
// 4. 对话记录相关 API
// ============================================================
export const chatRecordApi = {
  /**
   * 获取对话记录列表（分页）
   * @param {object} params - { page, pageSize }
   * @returns {Promise}
   */
  list(params) {
    return request({
      url: '/chat-record/list',
      method: 'get',
      params
    })
  },

  /**
   * 搜索对话记录
   * @param {object} params - { keyword, page, pageSize }
   * @returns {Promise}
   */
  search(params) {
    return request({
      url: '/chat-record/search',
      method: 'get',
      params
    })
  },

  /**
   * 删除指定的对话记录
   * @param {number|string} id - 记录 ID
   * @returns {Promise}
   */
  deleteById(id) {
    return request({
      url: `/chat-record/${id}`,
      method: 'delete'
    })
  },

  /**
   * 清空当前用户的所有对话记录
   * @returns {Promise}
   */
  clearAll() {
    return request({
      url: '/chat-record/clear',
      method: 'delete'
    })
  }
}

// ============================================================
// 5. 生活提醒相关 API
// ============================================================
export const reminderApi = {
  /**
   * 获取提醒列表
   * @returns {Promise}
   */
  list() {
    return request({
      url: '/reminder/list',
      method: 'get'
    })
  },

  listSilently() {
    return request({
      url: '/reminder/list',
      method: 'get',
      skipErrorToast: true
    })
  },

  /**
   * 添加生活提醒
   * @param {object} data - { title, time, type, repeat 等 }
   * @returns {Promise}
   */
  add(data) {
    return request({
      url: '/reminder/add',
      method: 'post',
      data
    })
  },

  /**
   * 更新提醒信息
   * @param {number|string} id - 提醒 ID
   * @param {object} data - 要更新的字段
   * @returns {Promise}
   */
  update(id, data) {
    return request({
      url: `/reminder/${id}`,
      method: 'put',
      data
    })
  },

  /**
   * 删除提醒
   * @param {number|string} id - 提醒 ID
   * @returns {Promise}
   */
  delete(id) {
    return request({
      url: `/reminder/${id}`,
      method: 'delete'
    })
  },

  /**
   * 标记提醒为已完成
   * @param {number|string} id - 提醒 ID
   * @returns {Promise}
   */
  complete(id) {
    return request({
      url: `/reminder/${id}/complete`,
      method: 'put'
    })
  },

  /**
   * 取消提醒的已完成状态
   * @param {number|string} id - 提醒 ID
   * @returns {Promise}
   */
  uncomplete(id) {
    return request({
      url: `/reminder/${id}/uncomplete`,
      method: 'put'
    })
  },
  snooze(id, minutes = 10) {
    return request({ url: `/reminder/${id}/snooze`, method: 'put', data: { minutes } })
  },
  skip(id) {
    return request({ url: `/reminder/${id}/skip`, method: 'put' })
  },
  statistics(days = 30) {
    return request({ url: '/reminder/statistics', method: 'get', params: { days } })
  }
}

// ============================================================
// 6. 紧急求助相关 API
// ============================================================
export const emergencyApi = {
  /**
   * 发起紧急求助
   * @param {object} data - { type, message, location 等 }
   * @returns {Promise}
   */
  createHelp(data, idempotencyKey) {
    return request({
      url: '/emergency/help',
      method: 'post',
      data,
      headers: idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : undefined
    })
  },

  /**
   * 获取当前用户的求助记录列表
   * @returns {Promise}
   */
  list() {
    return request({
      url: '/emergency/list',
      method: 'get'
    })
  },

  cancel(id) {
    return request({
      url: `/emergency/${id}/cancel`,
      method: 'put'
    })
  }
}

// ============================================================
// 7. 健康管理相关 API
// ============================================================
export const healthApi = {
  /**
   * 添加健康记录（血压、血糖、心率等）
   * @param {object} data - { type, value, date, remark 等 }
   * @returns {Promise}
   */
  addRecord(data) {
    return request({
      url: '/health/record',
      method: 'post',
      data
    })
  },

  /**
   * 获取健康记录列表
   * @param {object} params - { type, startDate, endDate, page, pageSize }
   * @returns {Promise}
   */
  list(params) {
    return request({
      url: '/health/list',
      method: 'get',
      params
    })
  },

  /**
   * 获取健康数据图表数据（用于 ECharts 展示）
   * @param {object} params - { type, period }
   * @returns {Promise} 返回图表数据
   */
  chartData(params) {
    return request({
      url: '/health/chart',
      method: 'get',
      params
    })
  },

  /**
   * 删除健康记录
   * @param {number|string} id - 记录 ID
   * @returns {Promise}
   */
  delete(id) {
    return request({
      url: `/health/${id}`,
      method: 'delete'
    })
  },

  /**
   * 获取 AI 健康建议
   * @returns {Promise} 返回 AI 生成的健康建议
   */
  getAdvice() {
    return request({
      url: '/health/advice',
      method: 'get'
    })
  },

  /**
   * 批量导入健康数据
   * @param {File} file - 上传的 CSV/XLS/XLSX 文件
   * @returns {Promise}
   */
  importRecords(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/health/import',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  /**
   * 模拟智能设备上报数据（血压/心率等异常）
   * @param {object} data - { deviceId, measuredAt, metrics }
   * @returns {Promise}
   */
  submitDeviceData(data) {
    return request({
      url: '/health/device-data',
      method: 'post',
      data
    })
  }
}

export const healthWarningApi = {
  list(params = {}) {
    return request({
      url: '/health-warning/list',
      method: 'get',
      params
    })
  },

  unreadCount() {
    return request({
      url: '/health-warning/unread-count',
      method: 'get'
    })
  },

  updateStatus(id, status, actionNote = '') {
    return request({
      url: `/health-warning/${id}/status`,
      method: 'put',
      data: { status, actionNote }
    })
  }
}

// ============================================================
// 8. 养老资讯相关 API
// ============================================================

export const familyApi = {
  bind(data) {
    return request({ url: '/family-binding/bind', method: 'post', data })
  },
  myElders() {
    return request({ url: '/family-binding/elders', method: 'get' })
  },
  elderDetail(id) {
    return request({ url: `/family-binding/elder/${id}`, method: 'get' })
  },
  overview() {
    return request({ url: '/family/overview', method: 'get', skipErrorToast: true })
  },
  myFamilies() {
    return request({ url: '/family-binding/families', method: 'get' })
  },
  myBindingRequests() {
    return request({ url: '/family-binding/binding-requests/mine', method: 'get' })
  },
  unbind(data) {
    return request({ url: '/family-binding/unbind', method: 'post', data })
  },
  availableElders() {
    return request({ url: '/family-binding/available-elders', method: 'get' })
  },
  healthList(elderId, params = {}) {
    return request({ url: '/family/health/list', method: 'get', params: { elderId, ...params } })
  },
  healthChart(elderId, params = {}) {
    return request({ url: '/family/health/chart', method: 'get', params: { elderId, ...params } })
  },
  healthAdvice(elderId) {
    return request({ url: '/family/health/advice', method: 'get', params: { elderId } })
  },
  emergencyList(elderId) {
    return request({ url: '/family/emergency/list', method: 'get', params: { elderId } })
  },

  importRecords(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/health/import',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  submitDeviceData(data) {
    return request({ url: '/health/device-data', method: 'post', data })
  },
  emergencyDetail(id) {
    return request({ url: `/family/emergency/${id}`, method: 'get' })
  },
  acknowledgeEmergency(id) {
    return request({ url: `/family/emergency/${id}/acknowledge`, method: 'put' })
  },
  warningList(elderId, params = {}) {
    return request({ url: '/family/warning/list', method: 'get', params: { elderId, ...params } })
  },
  reminderList(elderId) {
    return request({ url: '/family/reminder/list', method: 'get', params: { elderId } })
  },
  addReminder(elderId, data) {
    return request({ url: '/family/reminder/add', method: 'post', params: { elderId }, data })
  },
  updateReminder(elderId, id, data) {
    return request({ url: `/family/reminder/${id}`, method: 'put', params: { elderId }, data })
  },
  deleteReminder(elderId, id) {
    return request({ url: `/family/reminder/${id}`, method: 'delete', params: { elderId } })
  },
  pauseReminder(elderId, id, paused) {
    return request({ url: `/family/reminder/${id}/pause`, method: 'put', params: { elderId, paused } })
  },
  reminderExecutions(elderId, id) {
    return request({ url: `/family/reminder/${id}/executions`, method: 'get', params: { elderId } })
  },
  reminderStatistics(elderId, days = 30) {
    return request({ url: '/family/reminder/statistics', method: 'get', params: { elderId, days } })
  }
}

export const contactApi = {
  submit(data) {
    return request({ url: '/contact/message', method: 'post', data })
  }
}

export const newsApi = {
  /**
   * 获取养老资讯列表（分页）
   * @param {object} params - { page, pageSize, category }
   * @returns {Promise}
   */
  list(params) {
    return request({
      url: '/news/list',
      method: 'get',
      params
    })
  },

  /**
   * 获取资讯详情
   * @param {number|string} id - 资讯 ID
   * @returns {Promise}
   */
  getDetail(id) {
    return request({
      url: `/news/${id}`,
      method: 'get'
    })
  }
}

// ============================================================
// 10. 天气查询相关 API
// ============================================================
export const weatherApi = {
  /**
   * 根据城市名查询当前天气与7日预报
   * @param {string} city - 城市名称，如 "北京"、"New York"
   * @returns {Promise}
   */
  currentByCity(city) {
    return request({
      url: '/weather/current',
      method: 'get',
      params: { city }
    })
  },

  /**
   * 根据经纬度查询当前天气与7日预报
   * @param {number} latitude - 纬度
   * @param {number} longitude - 经度
   * @returns {Promise}
   */
  currentByLocation(latitude, longitude) {
    return request({
      url: '/weather/location',
      method: 'get',
      params: { latitude, longitude }
    })
  }
}
export const adminApi = {
  tickets(params) { return request({ url:'/admin/tickets', method:'get', params }) },
  transitionTicket(id, data) { return request({ url:`/admin/tickets/${id}/status`, method:'put', data }) },
  contactMessages(params) {
    return request({ url: '/admin/contact-messages', method: 'get', params })
  },
  updateContactStatus(id, status) {
    return request({ url: `/admin/contact-messages/${id}/status`, method: 'put', data: { status } })
  },
  deletionRequests(params) {
    return request({ url: '/admin/deletion-requests', method: 'get', params })
  },
  reviewDeletion(id, data) {
    return request({ url: `/admin/deletion-requests/${id}/review`, method: 'put', data })
  },
  /**
   * 获取管理后台仪表盘数据（统计概览）
   * @returns {Promise} 返回统计数据
   */
  dashboard() {
    return request({
      url: '/admin/dashboard',
      method: 'get'
    })
  },

  /**
   * 获取用户列表（管理员功能）
   * @param {object} params - { page, pageSize, keyword, role }
   * @returns {Promise}
   */
  listUsers(params) {
    return request({
      url: '/admin/users',
      method: 'get',
      params
    })
  },

  /**
   * 更新用户状态（启用/禁用）
   * @param {number|string} id - 用户 ID
   * @param {string} status - 新状态（ACTIVE / DISABLED）
   * @returns {Promise}
   */
  updateUserStatus(id, status, reason = '') {
    return request({
      url: `/admin/users/${id}/status`,
      method: 'put',
      params: { status, reason }
    })
  },
  analytics() { return request({ url:'/admin/analytics', method:'get' }) },
  userDetail(id) { return request({ url:`/admin/users/${id}/detail`, method:'get' }) },

  /**
   * 获取紧急求助列表（管理员查看所有求助）
   * @param {object} params - { status, page, pageSize }
   * @returns {Promise}
   */
  listEmergency(params) {
    return request({
      url: '/admin/emergency',
      method: 'get',
      params
    })
  },

  transitionEmergency(id, status, remark = '') {
    return request({
      url: `/admin/emergency/${id}/status`,
      method: 'put',
      data: { status, remark }
    })
  },

  /**
   * 创建养老资讯（管理员发布资讯）
   * @param {object} data - { title, content, category, coverImage 等 }
   * @returns {Promise}
   */
  createNews(data) {
    return request({
      url: '/admin/news',
      method: 'post',
      data
    })
  },

  /**
   * 更新资讯
   * @param {number|string} id - 资讯 ID
   * @param {object} data - 要更新的字段
   * @returns {Promise}
   */
  updateNews(id, data) {
    return request({
      url: `/admin/news/${id}`,
      method: 'put',
      data
    })
  },

  /**
   * 删除资讯
   * @param {number|string} id - 资讯 ID
   * @returns {Promise}
   */
  deleteNews(id) {
    return request({
      url: `/admin/news/${id}`,
      method: 'delete'
    })
  },

  /**
   * 获取系统操作日志列表
   * @param {object} params - { page, pageSize, type, keyword }
   * @returns {Promise}
   */
  listLogs(params) {
    return request({
      url: '/admin/logs',
      method: 'get',
      params
    })
  },

  listNews(params) {
    return request({ url: '/admin/news', method: 'get', params })
  },

  changeNewsStatus(id, status) {
    return request({ url: `/admin/news/${id}/status`, method: 'put', data: { status } })
  },

  newsRevisions(id) {
    return request({ url: `/admin/news/${id}/revisions`, method: 'get' })
  },

  uploadNewsImage(file) {
    const data = new FormData()
    data.append('file', file)
    return request({ url: '/common/upload', method: 'post', data, headers: { 'Content-Type': 'multipart/form-data' } })
  },

  exportLogs(params) {
    return request({
      url: '/admin/logs/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },

  deviceList(params) {
    return request({ url: '/admin/device/list', method: 'get', params })
  },
  deviceCreate(data) {
    return request({ url: '/admin/device/create', method: 'post', data })
  },
  deviceUpdate(id, data) {
    return request({ url: `/admin/device/${id}`, method: 'put', data })
  },
  deviceEnable(id, enabled) {
    return request({ url: `/admin/device/${id}/enable`, method: 'put', data: { enabled } })
  },
  deviceDelete(id) {
    return request({ url: `/admin/device/${id}`, method: 'delete' })
  },
  deviceBatchImport(data) {
    return request({ url: '/admin/device/batch-import', method: 'post', data })
  },
  deviceBindElder(id, elderId) {
    return request({ url: `/admin/device/${id}/bind-elder`, method: 'put', data: { elderId } })
  },
  deviceUnbindElder(id) {
    return request({ url: `/admin/device/${id}/unbind-elder`, method: 'put' })
  },
  deviceRebind(id, data) {
    return request({ url: `/admin/device/${id}/rebind`, method: 'put', data })
  },
  deviceBindRecords(params) {
    return request({ url: '/admin/device/bind-records', method: 'get', params })
  },

  elderOptions() {
    return request({ url: '/admin/device/elders', method: 'get' })
  },
  familyOptions() {
    return request({ url: '/admin/device/families', method: 'get' })
  },
  deviceSyncLogs() {
    return request({ url: '/admin/device/sync-logs', method: 'get' })
  },
  deviceHealthData(params) {
    return request({ url: '/admin/device/health-data', method: 'get', params })
  },
  deviceHealthDataStatistics() {
    return request({ url: '/admin/device/health-data/statistics', method: 'get' })
  },
  deviceApiLogs(params) {
    return request({ url: '/admin/device/api-logs', method: 'get', params })
  },
  deviceApiLogsStatistics() {
    return request({ url: '/admin/device/api-logs/statistics', method: 'get' })
  },

  getWarningEvents(params) {
    return request({ url: '/admin/warning-center/events', method: 'get', params })
  },

  getWarningHandleLogs(eventType, eventId) {
    return request({ url: `/admin/warning-center/events/${eventType}/${eventId}/logs`, method: 'get' })
  },

  handleWarningEvent(eventType, eventId, action, data = {}) {
    return request({ url: `/admin/warning-center/events/${eventType}/${eventId}/${action}`, method: 'put', data })
  },

  getWarningStats() {
    return request({ url: '/admin/warning-center/stats', method: 'get' })
  },

  listReminders(params) {
    return request({ url: '/admin/reminders', method: 'get', params })
  },

  createReminder(data) {
    return request({ url: '/admin/reminders', method: 'post', data })
  },

  updateReminder(id, data) {
    return request({ url: `/admin/reminders/${id}`, method: 'put', data })
  },

  deleteReminder(id) {
    return request({ url: `/admin/reminders/${id}`, method: 'delete' })
  },

  getReminderExecutions(reminderId) {
    return request({ url: `/admin/reminders/${reminderId}/executions`, method: 'get' })
  },

  listAdminUsers(params) {
    return request({ url: '/admin/config/admin-users', method: 'get', params })
  },

  createAdminUser(data) {
    return request({ url: '/admin/config/admin-users', method: 'post', data })
  },

  updateAdminStatus(id, status) {
    return request({ url: `/admin/config/admin-users/${id}/status`, method: 'put', data: { status } })
  },

  resetAdminPassword(id, password) {
    return request({ url: `/admin/config/admin-users/${id}/password`, method: 'put', data: { password } })
  },

  updateAdminRole(id, data) {
    return request({ url: `/admin/config/admin-users/${id}/role`, method: 'put', data })
  },

  deleteAdminUser(id) {
    return request({ url: `/admin/config/admin-users/${id}`, method: 'delete' })
  },

  getSystemConfig() {
    return request({ url: '/admin/config', method: 'get' })
  },

  saveSystemConfig(data) {
    return request({ url: '/admin/config', method: 'post', data })
  },

  getElders() {
    return request({ url: '/admin/health/elders', method: 'get' })
  },
}

export const adminHealthApi = {
  getHealthRecords(params) {
    return request({ url: '/admin/health/records', method: 'get', params })
  },
  addHealthRecord(data) {
    return request({ url: '/admin/health/record', method: 'post', data })
  },
  updateHealthRecord(id, data) {
    return request({ url: `/admin/health/record/${id}`, method: 'put', data })
  },
  deleteHealthRecord(id) {
    return request({ url: `/admin/health/record/${id}`, method: 'delete' })
  },
  markInvalid(id) {
    return request({ url: `/admin/health/record/${id}/invalid`, method: 'put' })
  },
  exportHealthRecords(params) {
    return request({ url: '/admin/health/records/export', method: 'get', params, responseType: 'blob' })
  },
  getLlmAnalysis(params) {
    return request({ url: '/admin/health/llm-analysis', method: 'get', params })
  },
  getLlmAnalysisDetail(id) {
    return request({ url: `/admin/health/llm-analysis/${id}`, method: 'get' })
  },
  retriggerLlmAnalysis(data) {
    return request({ url: '/admin/health/llm-analysis/retrigger', method: 'post', data })
  },
  exportLlmAnalysis(params) {
    return request({ url: '/admin/health/llm-analysis/export', method: 'get', params, responseType: 'blob' })
  },
  getHealthReports(params) {
    return request({ url: '/admin/health/reports', method: 'get', params })
  },
  getHealthReportDetail(id) {
    return request({ url: `/admin/health/report/${id}`, method: 'get' })
  },
  regenerateHealthReport(data) {
    return request({ url: '/admin/health/report/regenerate', method: 'post', data })
  },
  resendReport(id) {
    return request({ url: `/admin/health/report/${id}/resend`, method: 'post' })
  },
  getElders() {
    return request({ url: '/admin/health/elders', method: 'get' })
  }
}

export const deviceApi = {
  getElderDevices() {
    return request({ url: '/device/elder/devices', method: 'get' })
  },
  bindDevice(data) {
    return request({ url: '/device/bind', method: 'post', data })
  },
  unbindDevice(deviceId) {
    return request({ url: `/device/unbind/${deviceId}`, method: 'delete' })
  },
  getDeviceData(deviceId) {
    return request({ url: `/device/data/${deviceId}`, method: 'get' })
  },
  uploadDeviceData(data) {
    return request({ url: '/device/upload', method: 'post', data })
  },
  pushReminderToDevice(reminderId) {
    return request({ url: '/device/push-reminder', method: 'post', data: { reminderId } })
  },
  confirmReminderByElder(reminderId) {
    return request({ url: '/device/confirm-reminder', method: 'post', data: { reminderId } })
  }
}

export const dietApi = {
  /**
   * 智能餐饮推荐（静态食谱，可按老人档案ID智能标注适合程度）
   * @param {number|string|null} elderInfoId - 老人档案ID，可选
   */
  recommend(elderInfoId) {
    return request({
      url: '/diet/recommend',
      method: 'get',
      params: elderInfoId ? { elderInfoId } : {}
    })
  }
}

export const knowledgeApi = {
  /** 家属端：分页查询已发布知识文章（支持分类/关键字筛选） */
  list(params) {
    return request({ url: '/knowledge/list', method: 'get', params })
  },
  /** 分类枚举（value + 中文 label） */
  categories() {
    return request({ url: '/knowledge/categories', method: 'get' })
  },
  /** 文章详情（浏览量 +1） */
  detail(id) {
    return request({ url: `/knowledge/${id}`, method: 'get' })
  },
  /** AI 健康问答（LLM 预留接口） */
  ask(data) {
    return request({ url: '/knowledge/ask', method: 'post', data })
  }
}

export const knowledgeAdminApi = {
  /** 管理端：分页查询全部文章（含草稿） */
  list(params) {
    return request({ url: '/admin/knowledge/list', method: 'get', params })
  },
  /** 分类枚举 */
  categories() {
    return request({ url: '/admin/knowledge/categories', method: 'get' })
  },
  /** 新增文章 */
  create(data) {
    return request({ url: '/admin/knowledge', method: 'post', data })
  },
  /** 更新文章 */
  update(id, data) {
    return request({ url: `/admin/knowledge/${id}`, method: 'put', data })
  },
  /** 删除文章 */
  remove(id) {
    return request({ url: `/admin/knowledge/${id}`, method: 'delete' })
  }
}


// ============ 医疗就诊记录（就医信息管理） ============
export const medicalRecordApi = {
  /** 家属端：分页查询本人绑定老人的就诊记录 */
  list(params) {
    return request({ url: '/medical-record/list', method: 'get', params })
  },
  /** 记录详情 */
  detail(id) {
    return request({ url: `/medical-record/${id}`, method: 'get' })
  },
  /** 新增记录 */
  create(data) {
    return request({ url: '/medical-record', method: 'post', data })
  },
  /** 修改记录 */
  update(data) {
    return request({ url: '/medical-record', method: 'put', data })
  },
  /** 删除记录 */
  remove(id) {
    return request({ url: `/medical-record/${id}`, method: 'delete' })
  },
  /** 上传图片（病历 / 检查报告），复用通用上传接口，返回 /uploads/... URL */
  uploadImage(file) {
    const fd = new FormData()
    fd.append('file', file)
    return request({ url: '/common/upload', method: 'post', data: fd, headers: { 'Content-Type': 'multipart/form-data' } })
  }
}

export const medicalRecordAdminApi = {
  /** 管理端：全量分页查询 */
  list(params) {
    return request({ url: '/admin/medical-record/list', method: 'get', params })
  },
  /** 详情 */
  detail(id) {
    return request({ url: `/admin/medical-record/${id}`, method: 'get' })
  },
  /** 修改 */
  update(id, data) {
    return request({ url: `/admin/medical-record/${id}`, method: 'put', data })
  },
  /** 删除 */
  remove(id) {
    return request({ url: `/admin/medical-record/${id}`, method: 'delete' })
  }
}

// ============ 娱乐活动（线上资讯 + 线下活动报名） ============
export const activityApi = {
  /** 家属端：已发布活动分页列表 */
  list(params) {
    return request({ url: '/activity/list', method: 'get', params })
  },
  /** 活动详情 */
  detail(id) {
    return request({ url: `/activity/${id}`, method: 'get' })
  },
  /** 报名线下活动 */
  signup(data) {
    return request({ url: '/activity/signup', method: 'post', data })
  },
  /** 我的报名 */
  mySignups(params) {
    return request({ url: '/activity/my-signups', method: 'get', params })
  },
  /** 取消报名 */
  cancelSignup(signupId) {
    return request({ url: `/activity/signup/${signupId}`, method: 'delete' })
  },
  /** 上传图片（封面），复用通用上传接口，返回 /uploads/... URL */
  uploadImage(file) {
    const fd = new FormData()
    fd.append('file', file)
    return request({ url: '/common/upload', method: 'post', data: fd, headers: { 'Content-Type': 'multipart/form-data' } })
  }
}

export const activityAdminApi = {
  /** 管理端：全量分页查询 */
  list(params) {
    return request({ url: '/admin/activity/list', method: 'get', params })
  },
  /** 详情 */
  detail(id) {
    return request({ url: `/admin/activity/${id}`, method: 'get' })
  },
  /** 新增活动 */
  create(data) {
    return request({ url: '/admin/activity', method: 'post', data })
  },
  /** 修改活动 */
  update(data) {
    return request({ url: '/admin/activity', method: 'put', data })
  },
  /** 删除活动 */
  remove(id) {
    return request({ url: `/admin/activity/${id}`, method: 'delete' })
  },
  /** 某活动的报名记录 */
  signups(activityId, params) {
    return request({ url: `/admin/activity/${activityId}/signups`, method: 'get', params })
  },
  /** 调整报名状态 */
  updateSignup(signupId, status) {
    return request({ url: `/admin/activity/signups/${signupId}`, method: 'put', params: { status } })
  }
}


// 管理员端老人档案管理
export const elderAdminApi = {
  /** 全量老人档案列表（关键字+状态筛选） */
  list(params) {
    return request({ url: '/admin/elders', method: 'get', params })
  },
  /** 老人档案详情 */
  detail(id) {
    return request({ url: `/admin/elders/${id}`, method: 'get' })
  },
  /** 编辑老人档案 */
  update(id, data) {
    return request({ url: `/admin/elders/${id}`, method: 'put', data })
  },
  /** 删除老人档案 */
  remove(id) {
    return request({ url: `/admin/elders/${id}`, method: 'delete' })
  },
  /** 某老人的绑定记录 */
  bindings(id) {
    return request({ url: `/admin/elders/${id}/bindings`, method: 'get' })
  },
  /** 管理员绑定家属到老人 */
  addBinding(data) {
    return request({ url: '/admin/elders/bindings', method: 'post', data })
  },
  /** 解除绑定 */
  removeBinding(bindingId) {
    return request({ url: `/admin/elders/bindings/${bindingId}`, method: 'delete' })
  },
  /** 家属账号选项 */
  familyOptions() {
    return request({ url: '/admin/elders/family-options', method: 'get' })
  }
}

export const pushLogApi = {
  /** 推送日志全量列表（管理端） */
  list(params) {
    return request({ url: '/admin/push-log', method: 'get', params })
  }
}

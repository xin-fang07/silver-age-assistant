// ============================================================
// useReminderNotify.js - 提醒推送通知管理
// 银发智能生活助手 - 统一管理浏览器通知、声音提醒、推送开关
// 功能：
//   1. 浏览器通知权限申请与状态检测
//   2. 定时轮询待提醒事项，到点推送
//   3. 健康预警未读推送
//   4. 声音提醒（可选）
//   5. 已通知记录去重
// ============================================================

import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import { reminderApi, healthWarningApi } from '@/api/index'
import { isLoggedIn, getToken } from '@/utils/auth'

// 本地存储 key
const BROWSER_NOTIFY_KEY = 'elder_ai_browser_notification'
const SOUND_NOTIFY_KEY = 'elder_ai_sound_notification'
const REMINDER_PUSH_KEY = 'elder_ai_reminder_push'
const WARNING_PUSH_KEY = 'elder_ai_warning_push'
const NOTIFIED_REMINDER_KEY = 'elder_ai_notified_reminders'
const NOTIFIED_WARNING_KEY = 'elder_ai_notified_warnings'

// 轮询间隔（毫秒）- WebSocket 连接失败时降级使用
const CHECK_INTERVAL = 60 * 1000 // 每分钟检查一次

// WebSocket 重连延迟（毫秒）
const RECONNECT_DELAY = 5000

// 提醒类型图标映射
const REMINDER_ICONS = {
  MEDICINE: '💊',
  EXERCISE: '🏃',
  CHECKUP: '🏥',
  PAYMENT: '💰',
  OTHER: '⏰'
}

// 预警等级对应颜色
const WARNING_COLORS = {
  1: '#f59e0b', // 轻度 - 橙
  2: '#f97316', // 中度 - 深橙
  3: '#dc2626'  // 重度 - 红
}

export function useReminderNotify() {
  const router = useRouter()

  // 通知开关状态
  // 每个页面都会创建自己的 composable 实例，因此必须在创建时读取持久化值，
  // 不能只依赖 AppLayout 调用 init() 后加载另一份实例的状态。
  const browserNotifyEnabled = ref(localStorage.getItem(BROWSER_NOTIFY_KEY) === 'true')
  const soundNotifyEnabled = ref(localStorage.getItem(SOUND_NOTIFY_KEY) === 'true')
  const reminderPushEnabled = ref(localStorage.getItem(REMINDER_PUSH_KEY) !== 'false')
  const warningPushEnabled = ref(localStorage.getItem(WARNING_PUSH_KEY) !== 'false')

  // 到点待处理数量（用于侧边栏角标）
  const dueReminderCount = ref(0)
  const unreadWarningCount = ref(0)

  // 定时器
  let checkTimer = null
  let audioCtx = null

  // WebSocket 相关
  let ws = null
  let wsReconnectTimer = null
  const wsConnected = ref(false)

  // ============== 初始化与销毁 ==============

  const init = () => {
    loadSettings()
    if (isLoggedIn()) {
      startWebSocket()
      startCheckLoop()
      checkAll()
    }
  }

  const destroy = () => {
    stopWebSocket()
    stopCheckLoop()
  }

  // ============== 设置持久化 ==============

  const loadSettings = () => {
    browserNotifyEnabled.value = localStorage.getItem(BROWSER_NOTIFY_KEY) === 'true'
    soundNotifyEnabled.value = localStorage.getItem(SOUND_NOTIFY_KEY) === 'true'
    reminderPushEnabled.value = localStorage.getItem(REMINDER_PUSH_KEY) !== 'false'
    warningPushEnabled.value = localStorage.getItem(WARNING_PUSH_KEY) !== 'false'
  }

  const saveSettings = () => {
    localStorage.setItem(BROWSER_NOTIFY_KEY, browserNotifyEnabled.value.toString())
    localStorage.setItem(SOUND_NOTIFY_KEY, soundNotifyEnabled.value.toString())
    localStorage.setItem(REMINDER_PUSH_KEY, reminderPushEnabled.value.toString())
    localStorage.setItem(WARNING_PUSH_KEY, warningPushEnabled.value.toString())
  }

  // ============== 浏览器通知权限 ==============

  /**
   * 检测浏览器是否支持通知
   */
  const isNotificationSupported = () => {
    return typeof Notification !== 'undefined'
  }

  /**
   * 获取当前通知权限状态
   * @returns {'default'|'granted'|'denied'}
   */
  const getNotificationPermission = () => {
    if (!isNotificationSupported()) return 'denied'
    return Notification.permission
  }

  /**
   * 申请浏览器通知权限
   * @returns {Promise<boolean>} 是否授权成功
   */
  const requestNotificationPermission = async () => {
    if (!isNotificationSupported()) {
      ElNotification.warning('您的浏览器不支持桌面通知功能')
      return false
    }

    if (Notification.permission === 'granted') {
      browserNotifyEnabled.value = true
      saveSettings()
      return true
    }

    if (Notification.permission === 'denied') {
      ElNotification({
        title: '通知权限已被拒绝',
        message: '请在浏览器地址栏左侧的设置中手动开启通知权限',
        type: 'warning',
        duration: 5000
      })
      return false
    }

    try {
      const permission = await Notification.requestPermission()
      const granted = permission === 'granted'
      browserNotifyEnabled.value = granted
      saveSettings()

      if (granted) {
        // 授权成功后发一条测试通知
        showBrowserNotification('通知已开启', '以后有提醒事项会及时通知您', {
          tag: 'test-notification',
          silent: true
        })
        ElNotification.success('桌面通知已开启')
      } else {
        ElNotification.warning('您拒绝了通知权限，将无法收到桌面提醒')
      }
      return granted
    } catch (e) {
      console.error('申请通知权限失败:', e)
      return false
    }
  }

  /**
   * 切换浏览器通知开关
   */
  const toggleBrowserNotify = async (enabled) => {
    if (enabled) {
      const granted = await requestNotificationPermission()
      return granted
    } else {
      browserNotifyEnabled.value = false
      saveSettings()
      return true
    }
  }

  // ============== 声音提醒 ==============

  /**
   * 播放提示音（使用 Web Audio API，无需音频文件）
   */
  const playNotifySound = () => {
    if (!soundNotifyEnabled.value) return

    try {
      if (!audioCtx) {
        audioCtx = new (window.AudioContext || window.webkitAudioContext)()
      }

      // 简单的"叮"声：两个短促的正弦波
      const playBeep = (freq, startTime, duration) => {
        const osc = audioCtx.createOscillator()
        const gain = audioCtx.createGain()
        osc.connect(gain)
        gain.connect(audioCtx.destination)
        osc.frequency.value = freq
        osc.type = 'sine'
        gain.gain.setValueAtTime(0.3, startTime)
        gain.gain.exponentialRampToValueAtTime(0.01, startTime + duration)
        osc.start(startTime)
        osc.stop(startTime + duration)
      }

      const now = audioCtx.currentTime
      playBeep(880, now, 0.15)
      playBeep(1100, now + 0.2, 0.2)
    } catch (e) {
      console.warn('播放提示音失败:', e)
    }
  }

  const toggleSoundNotify = (enabled) => {
    soundNotifyEnabled.value = enabled
    saveSettings()
    if (enabled) playNotifySound() // 开启时试听一下
  }

  // ============== 推送开关 ==============

  const setReminderPush = (enabled) => {
    reminderPushEnabled.value = enabled
    saveSettings()
  }

  const setWarningPush = (enabled) => {
    warningPushEnabled.value = enabled
    saveSettings()
  }

  // ============== WebSocket 实时推送 ==============

  const getWsUrl = () => {
    const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws'
    return `${protocol}://${window.location.host}/ws/reminder`
  }

  const startWebSocket = () => {
    if (!isLoggedIn()) return

    const token = getToken()
    if (!token) return

    stopWebSocket()

    try {
      const wsUrl = `${getWsUrl()}?token=${encodeURIComponent(token)}`
      ws = new WebSocket(wsUrl)

      ws.onopen = () => {
        wsConnected.value = true
        console.log('WebSocket 连接成功')
        // 连接成功后立即检查一次
        checkAll()
      }

      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          if (data.type === 'REMINDER_DUE') {
            handleWsReminder(data.reminder)
          }
        } catch (e) {
          console.error('解析 WebSocket 消息失败:', e)
        }
      }

      ws.onerror = () => {
        wsConnected.value = false
        console.warn('WebSocket 连接错误，将在', RECONNECT_DELAY / 1000, '秒后重连')
        scheduleReconnect()
      }

      ws.onclose = () => {
        wsConnected.value = false
        console.log('WebSocket 连接关闭')
        scheduleReconnect()
      }
    } catch (e) {
      console.error('创建 WebSocket 连接失败:', e)
      scheduleReconnect()
    }
  }

  const stopWebSocket = () => {
    if (ws) {
      try {
        ws.close()
      } catch (e) {
        console.warn('关闭 WebSocket 连接时出错:', e)
      }
      ws = null
    }
    if (wsReconnectTimer) {
      clearTimeout(wsReconnectTimer)
      wsReconnectTimer = null
    }
    wsConnected.value = false
  }

  const scheduleReconnect = () => {
    stopWebSocket()
    wsReconnectTimer = setTimeout(() => {
      if (isLoggedIn()) {
        startWebSocket()
      }
    }, RECONNECT_DELAY)
  }

  const handleWsReminder = (reminder) => {
    if (!reminderPushEnabled.value) return

    const key = `${reminder.id}-${reminder.remindTime}`
    if (getNotifiedReminders().has(key)) return

    markReminderNotified(key)
    dueReminderCount.value++

    ElNotification({
      title: '⏰ 生活提醒到时间了',
      message: `${reminder.title}\n${reminder.content || ''}`,
      type: 'warning',
      duration: 0,
      position: 'top-right',
      onClick: () => router.push('/care/reminder')
    })

    if (browserNotifyEnabled.value && document.visibilityState !== 'visible') {
      const icon = REMINDER_ICONS[reminder.remindType] || '⏰'
      showBrowserNotification(
        `${icon} 生活提醒`,
        `${reminder.title}\n${reminder.content || ''}`,
        {
          tag: 'reminder-due',
          data: { path: '/care/reminder' },
          requireInteraction: true
        }
      )
    }

    playNotifySound()
  }

  // ============== 定时检查 ==============

  const startCheckLoop = () => {
    stopCheckLoop()
    checkTimer = setInterval(checkAll, CHECK_INTERVAL)
  }

  const stopCheckLoop = () => {
    if (checkTimer) {
      clearInterval(checkTimer)
      checkTimer = null
    }
  }

  const checkAll = () => {
    if (!isLoggedIn()) return
    if (reminderPushEnabled.value) checkDueReminders()
    if (warningPushEnabled.value) checkUnreadWarnings()
  }

  // ============== 提醒检查 ==============

  const parseTime = (value) => new Date(String(value || '').replace(' ', 'T'))

  /**
   * 获取已通知的提醒ID集合
   */
  const getNotifiedReminders = () => {
    try {
      const raw = localStorage.getItem(NOTIFIED_REMINDER_KEY)
      return new Set(raw ? JSON.parse(raw) : [])
    } catch { return new Set() }
  }

  const markReminderNotified = (key) => {
    const set = getNotifiedReminders()
    set.add(key)
    // 只保留最近100条，防止无限增长
    const arr = Array.from(set).slice(-100)
    localStorage.setItem(NOTIFIED_REMINDER_KEY, JSON.stringify(arr))
  }

  const checkDueReminders = async () => {
    try {
      const res = await reminderApi.listSilently()
      const list = Array.isArray(res.data) ? res.data : (res.data?.records || [])
      const now = new Date()

      // 筛选到点且待处理的
      const dueList = list.filter(item => {
        const time = parseTime(item.remindTime)
        return Number(item.status) === 0 && !isNaN(time.getTime()) && time <= now
      })

      dueReminderCount.value = dueList.length

      // 筛选还没通知过的
      const notified = getNotifiedReminders()
      const toNotify = dueList.filter(item => {
        const key = `${item.id}-${item.remindTime}`
        return !notified.has(key)
      })

      if (!toNotify.length) return

      // 标记为已通知
      toNotify.forEach(item => {
        markReminderNotified(`${item.id}-${item.remindTime}`)
      })

      // 页面内通知（聚合显示）
      const titles = toNotify.slice(0, 3).map(item => item.title).join('、')
      const extra = toNotify.length > 3 ? `等${toNotify.length}项` : ''

      ElNotification({
        title: '⏰ 生活提醒到时间了',
        message: `${titles}${extra}，点击查看并确认完成。`,
        type: 'warning',
        duration: 0,
        position: 'top-right',
        onClick: () => router.push('/care/reminder')
      })

      // 浏览器桌面通知（页面不可见时才发，避免重复打扰）
      if (browserNotifyEnabled.value && document.visibilityState !== 'visible') {
        const first = toNotify[0]
        const icon = REMINDER_ICONS[first.remindType] || '⏰'
        showBrowserNotification(
          `${icon} 生活提醒`,
          toNotify.length === 1
            ? `${first.title}\n${first.content || ''}`
            : `${first.title} 等${toNotify.length}项提醒到时间了`,
          {
            tag: 'reminder-due',
            data: { path: '/care/reminder' },
            requireInteraction: true
          }
        )
      }

      // 声音提醒
      playNotifySound()

    } catch (error) {
      console.error('检查到期提醒失败：', error)
    }
  }

  // ============== 健康预警检查 ==============

  const getNotifiedWarnings = () => {
    try {
      const raw = localStorage.getItem(NOTIFIED_WARNING_KEY)
      return new Set(raw ? JSON.parse(raw) : [])
    } catch { return new Set() }
  }

  const markWarningNotified = (id) => {
    const set = getNotifiedWarnings()
    set.add(String(id))
    const arr = Array.from(set).slice(-100)
    localStorage.setItem(NOTIFIED_WARNING_KEY, JSON.stringify(arr))
  }

  const checkUnreadWarnings = async () => {
    try {
      const res = await healthWarningApi.unreadCount()
      const count = Number(res.data?.count ?? res.data ?? 0)
      unreadWarningCount.value = count

      if (count <= 0) return

      // 查询具体的未读预警（取最新的）
      const listRes = await healthWarningApi.list({ page: 1, pageSize: 5, status: 0 })
      const list = listRes.data?.records || listRes.data || []

      const notified = getNotifiedWarnings()
      const toNotify = list.filter(w => !notified.has(String(w.id)))

      if (!toNotify.length) return

      toNotify.forEach(w => markWarningNotified(w.id))

      const first = toNotify[0]
      const levelText = ['', '轻度', '中度', '重度'][first.warningLevel] || '异常'
      const typeText = {
        BLOOD_PRESSURE: '血压',
        BLOOD_SUGAR: '血糖',
        HEART_RATE: '心率'
      }[first.warningType] || '健康指标'

      ElNotification({
        title: `⚠️ 健康${levelText}预警`,
        message: `${typeText}异常：${first.warningContent || '请及时关注'}`,
        type: 'warning',
        duration: 0,
        position: 'top-right',
        onClick: () => router.push('/care/health')
      })

      if (browserNotifyEnabled.value && document.visibilityState !== 'visible') {
        showBrowserNotification(
          `⚠️ 健康${levelText}预警`,
          `${typeText}异常，请及时关注身体状况`,
          {
            tag: 'health-warning',
            data: { path: '/care/health' },
            requireInteraction: true
          }
        )
      }

      playNotifySound()

    } catch (error) {
      console.error('检查健康预警失败：', error)
    }
  }

  // ============== 浏览器通知封装 ==============

  /**
   * 显示浏览器桌面通知
   * @param {string} title - 标题
   * @param {string} body - 内容
   * @param {object} options - 其他选项
   */
  const showBrowserNotification = (title, body, options = {}) => {
    if (!isNotificationSupported() || Notification.permission !== 'granted') return
    if (!browserNotifyEnabled.value) return

    try {
      const notification = new Notification(title, {
        body,
        icon: '/favicon.ico',
        badge: '/favicon.ico',
        silent: options.silent || false,
        requireInteraction: options.requireInteraction || false,
        tag: options.tag || undefined,
        data: options.data || {}
      })

      // 点击通知跳转
      notification.onclick = (event) => {
        event.preventDefault()
        window.focus()
        const path = notification.data?.path
        if (path) router.push(path)
        notification.close()
      }

      // 10秒后自动关闭（如果不是常驻的）
      if (!options.requireInteraction) {
        setTimeout(() => notification.close(), 10000)
      }

      return notification
    } catch (e) {
      console.warn('显示桌面通知失败:', e)
      return null
    }
  }

  // ============== 手动刷新 ==============

  const refresh = () => {
    checkAll()
  }

  return {
    // 状态
    browserNotifyEnabled,
    soundNotifyEnabled,
    reminderPushEnabled,
    warningPushEnabled,
    dueReminderCount,
    unreadWarningCount,

    // 权限相关
    isNotificationSupported,
    getNotificationPermission,
    requestNotificationPermission,
    toggleBrowserNotify,

    // 声音
    toggleSoundNotify,
    playNotifySound,

    // 推送开关
    setReminderPush,
    setWarningPush,

    // 生命周期
    init,
    destroy,
    refresh,

    // 主动发通知
    showBrowserNotification
  }
}

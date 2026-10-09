// ============================================================
// index.js - Vue Router 路由配置
// 银发智能生活助手 - 前端路由定义与导航守卫
// ============================================================

import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { isLoggedIn, getRole } from '@/utils/auth'
import { useSpeech } from '@/composables/useSpeech'

const speech = useSpeech()

const routes = [
  // ---------- 公开官网首页（访客第一页，无需登录） ----------
  {
    path: '/landing',
    name: 'Landing',
    component: () => import('@/views/landing/index.vue'),
    meta: { title: '银发智能生活助手 - 让科技温暖晚年', noAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/home/login.vue'),
    meta: { title: '登录 - 银发智能生活助手', noAuth: true }
  },
  {
    path: '/privacy',
    name: 'Privacy',
    component: () => import('@/views/privacy/index.vue'),
    meta: { title: '隐私政策与用户协议 - 银发智能生活助手', noAuth: true }
  },
  {
    path: '/contact',
    name: 'Contact',
    component: () => import('@/views/contact/index.vue'),
    meta: { title: '联系我们 - 银发智能生活助手', noAuth: true }
  },
  {
    path: '/faq',
    name: 'Faq',
    component: () => import('@/views/faq/index.vue'),
    meta: { title: '常见问题 - 银发智能生活助手', noAuth: true }
  },

  // ---------- 老人家属智能照护端功能页面（AppLayout 侧边栏布局） ----------
  {
    path: '/care',
    component: () => import('@/components/AppLayout.vue'),
    meta: { requiresAuth: true, requiresFamily: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页 - 银发智能生活助手' }
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('@/views/chat/index.vue'),
        meta: { title: '智能助手 - 银发智能生活助手' }
      },
      {
        path: 'reminder',
        name: 'Reminder',
        component: () => import('@/views/reminder/index.vue'),
        meta: { title: '生活提醒 - 银发智能生活助手' }
      },
      {
        path: 'emergency',
        name: 'Emergency',
        component: () => import('@/views/emergency/index.vue'),
        meta: { title: '紧急求助 - 银发智能生活助手' }
      },
      {
        path: 'health',
        name: 'Health',
        component: () => import('@/views/health/index.vue'),
        meta: { title: '健康管理 - 银发智能生活助手' }
      },
      {
        path: 'device',
        name: 'Device',
        component: () => import('@/views/device/index.vue'),
        meta: { title: '设备管理 - 银发智能生活助手' }
      },
      {
        path: 'elders',
        name: 'Elders',
        component: () => import('@/views/elder/index.vue'),
        meta: { title: '我的老人 - 银发智能生活助手' }
      },
      {
        path: 'elder/:id',
        name: 'ElderDetail',
        component: () => import('@/views/elder/detail.vue'),
        meta: { title: '老人详情 - 银发智能生活助手' }
      },
      {
        path: 'alerts',
        name: 'Alerts',
        component: () => import('@/views/alerts/index.vue'),
        meta: { title: '预警中心 - 银发智能生活助手' }
      },
      {
        path: 'weather',
        name: 'Weather',
        component: () => import('@/views/weather/index.vue'),
        meta: { title: '天气查询 - 银发智能生活助手' }
      },
      {
        path: 'news',
        name: 'NewsList',
        component: () => import('@/views/news/index.vue'),
        meta: { title: '养老资讯 - 银发智能生活助手' }
      },
      {
        path: 'news/:id',
        name: 'NewsDetail',
        component: () => import('@/views/news/detail.vue'),
        meta: { title: '资讯详情 - 银发智能生活助手' }
      },
      {
        path: 'mine',
        name: 'Mine',
        component: () => import('@/views/mine/index.vue'),
        meta: { title: '我的 - 银发智能生活助手' }
      },
      {
        path: 'notification',
        name: 'Notification',
        component: () => import('@/views/notification/index.vue'),
        meta: { title: '通知中心 - 银发智能生活助手' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/index.vue'),
        meta: { title: '系统设置 - 银发智能生活助手' }
      },
      {
        path: 'family-bind',
        name: 'FamilyBind',
        component: () => import('@/views/family/bind.vue'),
        meta: { title: '老人绑定管理 - 银发智能生活助手' }
      },
      {
        path: 'diet',
        name: 'Diet',
        component: () => import('@/views/diet/index.vue'),
        meta: { title: '智能餐饮 - 银发智能生活助手' }
      },
      {
        path: 'medical-knowledge',
        name: 'MedicalKnowledge',
        component: () => import('@/views/medical-knowledge/index.vue'),
        meta: { title: '健康知识 - 银发智能生活助手' }
      },
      {
        path: 'medical-record',
        name: 'MedicalRecord',
        component: () => import('@/views/medical-record/index.vue'),
        meta: { title: '医疗档案 - 银发智能生活助手' }
      },
      {
        path: 'activity',
        name: 'Activity',
        component: () => import('@/views/activity/index.vue'),
        meta: { title: '娱乐活动 - 银发智能生活助手' }
      },
      {
        path: 'medical-knowledge/:id',
        name: 'MedicalKnowledgeDetail',
        component: () => import('@/views/medical-knowledge/detail.vue'),
        meta: { title: '知识详情 - 银发智能生活助手' }
      },
      {
        path: 'health-overview',
        name: 'HealthOverview',
        component: () => import('@/views/health/overview.vue'),
        meta: { title: '健康概况 - 银发智能生活助手' }
      },
      {
        path: 'health-report',
        name: 'HealthReport',
        component: () => import('@/views/health/report.vue'),
        meta: { title: 'LLM智能健康报告 - 银发智能生活助手' }
      },
      {
        path: 'location',
        name: 'Location',
        component: () => import('@/views/location/index.vue'),
        meta: { title: '位置信息查看 - 银发智能生活助手' }
      },
      {
        path: 'operation-log',
        name: 'OperationLog',
        component: () => import('@/views/operation/index.vue'),
        meta: { title: '操作记录 - 银发智能生活助手' }
      }
    ]
  },

  // ---------- 管理员功能页面（AdminLayout 侧边栏布局） ----------
  {
    path: '/admin',
    component: () => import('@/components/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('@/views/admin/dashboard.vue'), meta: { title: '管理控制台 - 银发智能生活助手' } },
      { path: 'analytics', name: 'AdminAnalytics', component: () => import('@/views/admin/analytics.vue'), meta: { title: '运营分析 - 银发智能生活助手' } },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/users.vue'), meta: { title: '用户管理 - 银发智能生活助手' } },
      { path: 'emergency', name: 'AdminEmergency', component: () => import('@/views/admin/emergency.vue'), meta: { title: '求助管理 - 银发智能生活助手' } },
      { path: 'news', name: 'AdminNews', component: () => import('@/views/admin/news.vue'), meta: { title: '资讯管理 - 银发智能生活助手' } },
      { path: 'logs', name: 'AdminLogs', component: () => import('@/views/admin/logs.vue'), meta: { title: '系统日志 - 银发智能生活助手' } },
      { path: 'service', name: 'AdminService', component: () => import('@/views/admin/service/reminder-task.vue'), meta: { title: '提醒任务管理 - 银发智能生活助手' } },
      { path: 'service/medicine', name: 'AdminMedicine', component: () => import('@/views/admin/service/medicine-service.vue'), meta: { title: '用药服务管理 - 银发智能生活助手' } },
      { path: 'health-warning', name: 'AdminHealthWarning', component: () => import('@/views/admin/health-warning.vue'), meta: { title: '健康预警 - 银发智能生活助手' } },
      { path: 'devices', name: 'AdminDeviceList', component: () => import('@/views/admin/device/list.vue'), meta: { title: '设备列表 - 银发智能生活助手' } },
      { path: 'device-binding', name: 'AdminDeviceBinding', component: () => import('@/views/admin/device/binding.vue'), meta: { title: '设备绑定管理 - 银发智能生活助手' } },
      { path: 'device-data', name: 'AdminDeviceData', component: () => import('@/views/admin/device/data.vue'), meta: { title: '健康数据接收 - 银发智能生活助手' } },
      { path: 'device-api-log', name: 'AdminDeviceApiLog', component: () => import('@/views/admin/device/api-log.vue'), meta: { title: '接口调用日志 - 银发智能生活助手' } },
      { path: 'medical-knowledge', name: 'AdminMedicalKnowledge', component: () => import('@/views/admin/medical-knowledge.vue'), meta: { title: '医疗知识库管理 - 银发智能生活助手' } },
      { path: 'medical-record', name: 'AdminMedicalRecord', component: () => import('@/views/admin/medical-record.vue'), meta: { title: '医疗记录管理 - 银发智能生活助手' } },
      { path: 'activity', name: 'AdminActivity', component: () => import('@/views/admin/activity.vue'), meta: { title: '娱乐活动管理 - 银发智能生活助手' } },
      { path: 'elders', name: 'AdminElders', component: () => import('@/views/admin/elder.vue'), meta: { title: '老人管理 - 银发智能生活助手' } },
      { path: 'push-log', name: 'AdminPushLog', component: () => import('@/views/admin/push-log.vue'), meta: { title: '推送日志 - 银发智能生活助手' } },
      { path: 'health-record', name: 'AdminHealthRecord', component: () => import('@/views/admin/health/health-record.vue'), meta: { title: '健康记录 - 银发智能生活助手' } },
      { path: 'llm-analysis', name: 'AdminLlmAnalysis', component: () => import('@/views/admin/health/llm-analysis.vue'), meta: { title: 'LLM分析结果 - 银发智能生活助手' } },
      { path: 'health-report', name: 'AdminHealthReport', component: () => import('@/views/admin/health/health-report.vue'), meta: { title: '健康报告管理 - 银发智能生活助手' } },
      { path: 'warning-center', name: 'AdminWarningCenter', component: () => import('@/views/admin/warning-center.vue'), meta: { title: '预警中心 - 银发智能生活助手' } },
      { path: 'admin-permission', name: 'AdminPermission', component: () => import('@/views/admin/admin-permission.vue'), meta: { title: '用户权限管理 - 银发智能生活助手' } },
      { path: 'admin-config', name: 'AdminConfig', component: () => import('@/views/admin/admin-config.vue'), meta: { title: '系统配置 - 银发智能生活助手' } }
    ]
  },

  // ---------- 500 错误页面 ----------
  {
    path: '/500',
    name: 'ServerError',
    component: () => import('@/views/error/500.vue'),
    meta: { title: '服务器错误 - 银发智能生活助手' }
  },

  // ---------- 404 页面 ----------
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/notfound/index.vue'),
    meta: { title: '页面未找到 - 银发智能生活助手' }
  }
]

const router = createRouter({ history: createWebHistory(), routes })

// ============================================================
// 模块语音简介表：按路由 name 映射「一句话介绍这个模块是干什么的」
// 进入/点击某模块时自动播报，帮助老人快速了解当前页面用途（受语音提示总开关控制）
// ============================================================
const routeVoiceMap = {
  // ---------- 公开页 ----------
  // 注：登录页(Login)的自动播报由 login.vue 的 onMounted 负责，此处不重复配置
  Landing: '银发智能生活助手首页，为老年人提供健康监测、智能助手和贴心陪伴服务。',
  // ---------- 家属照护端 ----------
  Home: '首页，汇总老人的健康状况、待办提醒和重要通知。',
  Chat: '智能助手，您可以用语音或文字向人工智能提问，获得健康和生活方面的贴心解答。',
  Reminder: '生活提醒，可以为老人设置吃药、复诊、喝水等定时提醒。',
  Emergency: '紧急求助，遇到突发情况可以一键发出求助，通知家属和管理人员。',
  Health: '健康管理，查看老人的血压、心率、血糖等健康数据和趋势。',
  Device: '设备管理，绑定和查看智能手表、血压计等健康监测设备。',
  Elders: '我的老人，管理您所照护的老人档案信息。',
  ElderDetail: '老人详情，查看这位老人的完整档案和健康记录。',
  Alerts: '预警中心，集中查看老人的健康异常预警信息。',
  Weather: '天气查询，查看今天和未来几天的天气，帮助安排老人的出行和穿衣。',
  NewsList: '养老资讯，为您提供养老政策、健康科普等实用信息。',
  NewsDetail: '资讯详情，您可以点击朗读按钮听取全文。',
  Mine: '我的，管理您的个人信息和账号设置。',
  Notification: '通知中心，查看系统推送的各类消息提醒。',
  Settings: '系统设置，调整语音、字体等适老化选项。',
  FamilyBind: '老人绑定管理，添加或解除与老人的绑定关系。',
  Diet: '智能餐饮，为老人推荐科学合理的膳食搭配。',
  MedicalKnowledge: '健康知识，提供常见疾病和养生保健的科普知识。',
  MedicalKnowledgeDetail: '知识详情，您可以点击朗读按钮听取全文。',
  MedicalRecord: '医疗档案，记录老人的就诊、诊断和用药信息。',
  Activity: '娱乐活动，浏览和报名适合老人参与的社区活动。',
  HealthOverview: '健康概况，一目了然地掌握老人的整体健康状态。',
  HealthReport: '智能健康报告，由人工智能分析生成，帮助您了解老人健康趋势。',
  Location: '位置信息，查看老人当前所在的位置。',
  OperationLog: '操作记录，查看账号的历史操作日志。',
  // ---------- 管理端 ----------
  AdminDashboard: '管理控制台，总览系统的整体运营数据。',
  AdminAnalytics: '运营分析，查看用户和业务的统计图表。',
  AdminUsers: '用户管理，管理系统中的所有用户账号。',
  AdminEmergency: '求助管理，处理老人发起的紧急求助。',
  AdminNews: '资讯管理，发布和维护养老资讯内容。',
  AdminLogs: '系统日志，查看系统运行和操作记录。',
  AdminHealthWarning: '健康预警，管理老人的健康异常预警。',
  AdminMedicalKnowledge: '医疗知识库管理，维护健康科普知识内容。',
  AdminMedicalRecord: '医疗记录管理，维护老人的就诊档案。',
  AdminActivity: '娱乐活动管理，发布和管理社区活动。',
  AdminElders: '老人管理，管理系统中的老人档案。',
  AdminHealthReport: '健康报告管理，管理人工智能生成的健康报告。'
}

let announceTimer = null
router.afterEach((to) => {
  if (announceTimer) clearTimeout(announceTimer)
  announceTimer = setTimeout(() => {
    // 仅在明确配置了「一句话简介」的模块才自动播报。
    // 不再退回朗读 meta.title：登录页(Login)的播报由 login.vue 的 onMounted
    // 负责，此处若读 title 会造成重复/意外播报；其它未配置 intro 的页面也不朗读标题。
    const intro = routeVoiceMap[to.name]
    if (intro) {
      speech.speak(intro)
    }
  }, 200)
})

router.beforeEach((to, from, next) => {
  speech.stop()
  if (to.meta.title) document.title = to.meta.title
  
  if (to.path === '/') {
    if (isLoggedIn()) {
      const role = getRole()
      if (role === 'ADMIN') return next('/admin')
      return next('/care')
    }
    return next('/landing')
  }
  
  if (to.meta.requiresAuth) {
    if (!isLoggedIn()) { next({ path: '/login', query: { redirect: to.fullPath } }); return }
    if (to.meta.requiresAdmin && getRole() !== 'ADMIN') { next('/landing'); return }
    if (to.meta.requiresFamily && getRole() !== 'FAMILY') { next('/landing'); return }
  }
  if (to.path === '/login' && isLoggedIn()) {
      const role = getRole()
      next(role === 'ADMIN' ? '/admin' : '/care')
      return
    }
  next()
})

export default router

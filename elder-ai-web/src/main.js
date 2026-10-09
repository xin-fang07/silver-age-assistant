// ============================================================
// main.js - Vue 应用入口文件
// 银发智能生活助手 - 初始化 Vue 应用、注册插件、挂载根组件
// ============================================================

import { createApp } from 'vue'

// ========== 引入根组件 ==========
import App from './App.vue'

// ========== 引入第三方插件 ==========
// Element Plus 样式文件
import 'element-plus/dist/index.css'
// 只注册项目实际使用的图标，避免把整套图标打进首屏。
import {
  AlarmClock, ArrowLeft, ArrowRight, Avatar, Bell, ChatDotRound, Check,
  Clock, CopyDocument, DataAnalysis, DataBoard, Delete, Document, Download, Edit,
  EditPen, FirstAidKit, Headset, HomeFilled, InfoFilled, Key, Link, Loading,
  Location, Lock, MagicStick, Microphone, Monitor, Notebook, Operation, Opportunity, Phone,
  Plus, Pointer, Reading, Refresh, Remove, Search, Service, Setting, Star, Sunny,
  SwitchButton, Tickets, TrendCharts, User, UserFilled, View, Warning, Food, Promotion
} from '@element-plus/icons-vue'

// Pinia 状态管理
import { createPinia } from 'pinia'

// Vue Router 路由
import router from './router'

// ========== 引入全局样式 ==========
// 适老化全局样式（大字体、高对比度等）
import './assets/style.css'
// 银发模式额外样式覆盖（切换时生效）
import './assets/elder-mode.css'
import { useElderMode } from './composables/useElderMode'

// 尽早应用适老偏好，避免页面加载后闪烁；首次使用默认开启银发模式。
useElderMode()

// ========== 创建 Vue 应用实例 ==========
const app = createApp(App)

// ========== 注册页面实际使用的 Element Plus 图标组件 ==========
const icons = {
  AlarmClock, ArrowLeft, ArrowRight, Avatar, Bell, ChatDotRound, Check,
  Clock, CopyDocument, DataAnalysis, DataBoard, Delete, Document, Download, Edit,
  EditPen, FirstAidKit, Headset, HomeFilled, InfoFilled, Key, Link, Loading,
  Location, Lock, MagicStick, Microphone, Monitor, Notebook, Operation, Opportunity, Phone,
  Plus, Pointer, Reading, Refresh, Remove, Search, Service, Setting, Star, Sunny,
  SwitchButton, Tickets, TrendCharts, User, UserFilled, View, Warning, Food, Promotion
}
for (const [key, component] of Object.entries(icons)) {
  app.component(key, component)
}

// ========== 注册插件 ==========
// Pinia 状态管理
app.use(createPinia())
// Vue Router 路由
app.use(router)

// ========== 全局错误捕获 ==========
app.config.errorHandler = (err, instance, info) => {
  console.error('[全局错误捕获]', err, info)
  if (import.meta.env.PROD && router.currentRoute.value.name !== 'ServerError') {
    router.push('/500')
  }
}

// ========== 挂载应用 ==========
// 将 Vue 应用挂载到 index.html 中的 #app 元素上
app.mount('#app')

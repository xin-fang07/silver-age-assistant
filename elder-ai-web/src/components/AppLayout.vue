<!--
  AppLayout.vue - 老人家属智能照护端布局组件（顶部横向导航栏）
  银发智能生活助手 - 现代卡片式 UI
  布局：顶部一条横向导航栏（含品牌 + 分组菜单 + 右侧操作），下方主内容区，无左侧菜单。
-->
<template>
  <el-container class="app-layout">
    <!-- ========== 顶部横向导航栏 ========== -->
    <el-header class="app-topbar">
      <!-- 横向导航菜单 -->
      <el-menu
        :default-active="activeMenu"
        mode="horizontal"
        router
        class="top-nav"
        :ellipsis="false"
        @select="handleMenuSelect"
      >
        <el-menu-item index="/care"><el-icon><HomeFilled /></el-icon>首页</el-menu-item>

        <el-sub-menu index="/care/my-elders">
          <template #title><el-icon><UserFilled /></el-icon>我的老人</template>
          <el-menu-item index="/care/elders"><el-icon><UserFilled /></el-icon>老人信息</el-menu-item>
          <el-menu-item index="/care/health-overview"><el-icon><DataAnalysis /></el-icon>健康概况</el-menu-item>
          <el-menu-item index="/care/family-bind"><el-icon><Link /></el-icon>绑定管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/care/health-group">
          <template #title><el-icon><TrendCharts /></el-icon>智能健康监测</template>
          <el-menu-item index="/care/health"><el-icon><TrendCharts /></el-icon>健康数据监测</el-menu-item>
          <el-menu-item index="/care/health-report"><el-icon><Document /></el-icon>LLM智能健康报告</el-menu-item>
          <el-menu-item index="/care/alerts"><el-icon><Bell /></el-icon>异常健康预警</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/care/medical-group">
          <template #title><el-icon><FirstAidKit /></el-icon>医疗服务</template>
          <el-menu-item index="/care/reminder">
            <el-icon><AlarmClock /></el-icon>
            <span>用药提醒</span>
            <span v-if="dueReminderCount" class="reminder-badge">{{ dueReminderCount > 99 ? '99+' : dueReminderCount }}</span>
          </el-menu-item>
          <el-menu-item index="/care/medical-record"><el-icon><Document /></el-icon>就医信息管理</el-menu-item>
          <el-menu-item index="/care/diet"><el-icon><Food /></el-icon>饮食健康管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/care/ai-group">
          <template #title><el-icon><ChatDotRound /></el-icon>AI智能助手</template>
          <el-menu-item index="/care/chat"><el-icon><Microphone /></el-icon>智能问答</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/care/emergency-group">
          <template #title><el-icon><Warning /></el-icon>紧急事件管理</template>
          <el-menu-item index="/care/emergency" class="menu-emergency"><el-icon><Phone /></el-icon>SOS报警记录</el-menu-item>
          <el-menu-item index="/care/location"><el-icon><Location /></el-icon>位置信息查看</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/care/life-group">
          <template #title><el-icon><Sunny /></el-icon>养老生活</template>
          <el-menu-item index="/care/news"><el-icon><Reading /></el-icon>养老资讯</el-menu-item>
          <el-menu-item index="/care/activity"><el-icon><Star /></el-icon>娱乐活动</el-menu-item>
          <el-sub-menu index="/care/life-assist">
            <template #title><el-icon><Sunny /></el-icon>生活辅助</template>
            <el-menu-item index="/care/weather"><el-icon><Sunny /></el-icon>天气查询</el-menu-item>
          </el-sub-menu>
        </el-sub-menu>

      </el-menu>

      <!-- 右侧操作区：头像下拉（含个人中心菜单 + 退出） -->
      <div class="topbar-right">
        <el-dropdown class="topbar-user-dropdown" trigger="click" placement="bottom-end" @command="handleUserCommand">
          <div class="user-avatar-name">
            <span class="user-avatar-icon">👤</span>
            <span class="user-name-text">{{ username }}</span>
            <el-icon class="user-caret"><CaretBottom /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
            <el-dropdown-item command="/care/mine"><el-icon><User /></el-icon>个人资料</el-dropdown-item>
            <el-dropdown-item command="/care/notification"><el-icon><Bell /></el-icon>消息通知</el-dropdown-item>
            <el-dropdown-item command="/care/operation-log"><el-icon><Notebook /></el-icon>操作记录</el-dropdown-item>
            <el-dropdown-item command="/care/settings"><el-icon><Setting /></el-icon>系统设置</el-dropdown-item>
            <el-dropdown-item divided command="logout"><el-icon><SwitchButton /></el-icon>退出</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- ========== 主内容区域 ========== -->
    <el-main id="main-content" class="app-main" tabindex="-1">
      <router-view v-slot="{ Component }">
        <transition name="slide-up" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </el-main>
    <el-backtop target=".app-main" :visibility-height="200" :right="36" :bottom="90" />
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Bell, Location, CaretBottom } from '@element-plus/icons-vue'
import { getUser, removeToken, removeUser } from '@/utils/auth'
import { useElderMode } from '@/composables/useElderMode'
import { useReminderNotify } from '@/composables/useReminderNotify'
import { notificationApi } from '@/api/index'

const router = useRouter()
const route = useRoute()
const user = getUser()
const username = ref(user ? (user.nickname || user.username) : '家属用户')
const {
  isElderMode, toggleElderMode,
  isHighContrast, toggleHighContrast,
  isReduceMotion, toggleReduceMotion
} = useElderMode()

const notificationCount = ref(0)

const activeMenu = computed(() => route.path)
const handleMenuSelect = () => {}

// 使用统一的提醒通知管理
const { dueReminderCount, init: initNotify, destroy: destroyNotify, refresh: refreshNotify } = useReminderNotify()

const loadNotificationCount = async () => {
  try {
    const res = await notificationApi.list()
    const notifications = res.data || []
    notificationCount.value = notifications.filter(n => n.isRead === 0).length
  } catch (error) {
    console.error('获取通知数量失败：', error)
  }
}

const handleUserCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  } else if (command) {
    router.push(command)
  }
}

const handleVisibilityChange = () => {
  if (document.visibilityState === 'visible') refreshNotify()
}

onMounted(() => {
  initNotify()
  loadNotificationCount()
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  destroyNotify()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
    confirmButtonText: '确定退出', cancelButtonText: '取消', type: 'warning'
  }).then(() => { removeToken(); removeUser(); router.push('/login') }).catch(() => {})
}
</script>

<style scoped>
.app-layout { height: 100vh; min-height: 100vh; }

/* ============ 顶部导航栏 ============ */
.app-topbar {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 52px;
  padding: 0 12px;
  background: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.06);
  position: relative;
  z-index: 100;
  overflow: visible;
}

.topbar-user-dropdown {
  flex-shrink: 0;
}

/* 横向菜单 */
.top-nav {
  flex: 1;
  min-width: 0;
  border-bottom: none;
  background: transparent;
  height: 50px;
  white-space: nowrap;
}
.top-nav :deep(.el-menu--horizontal) {
  display: flex !important;
  flex-wrap: nowrap !important;
  border-bottom: none !important;
}
.top-nav.el-menu--horizontal > .el-menu-item,
.top-nav.el-menu--horizontal > .el-sub-menu .el-sub-menu__title {
  height: 50px;
  line-height: 50px;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  border-bottom: none;
  padding: 0 8px;
  flex-shrink: 0;
  letter-spacing: 0.2px;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  transition: color 0.2s ease, background 0.2s ease;
}
.top-nav.el-menu--horizontal > .el-menu-item .el-icon,
.top-nav.el-menu--horizontal > .el-sub-menu .el-sub-menu__title .el-icon {
  margin-right: 3px;
  vertical-align: -2px;
  font-size: 15px;
  color: #7c8db5;
}
.top-nav.el-menu--horizontal > .el-menu-item:not(.is-active):hover,
.top-nav.el-menu--horizontal > .el-sub-menu .el-sub-menu__title:hover {
  color: #4f6aed;
  background: linear-gradient(180deg, rgba(102,126,234,0.06) 0%, rgba(118,75,162,0.06) 100%);
}
.top-nav.el-menu--horizontal > .el-menu-item:not(.is-active):hover .el-icon,
.top-nav.el-menu--horizontal > .el-sub-menu .el-sub-menu__title:hover .el-icon {
  color: #667eea;
}
.top-nav.el-menu--horizontal > .el-menu-item.is-active {
  color: #667eea;
  font-weight: 600;
  border-bottom: 2px solid #667eea;
  background: linear-gradient(180deg, rgba(102,126,234,0.08) 0%, rgba(118,75,162,0.08) 100%);
}
.top-nav.el-menu--horizontal > .el-menu-item.is-active .el-icon {
  color: #667eea;
}
.top-nav .reminder-badge {
  margin-left: 6px;
  min-width: 18px; height: 18px; padding: 0 5px;
  display: inline-flex; align-items: center; justify-content: center;
  background: #dc2626; color: #fff; font-size: 11px; font-weight: 800;
  border-radius: 999px;
}
.menu-emergency { color: #e85d75 !important; }
.menu-emergency:hover { background-color: #fef2f2 !important; }
.menu-emergency.is-active { background: #fee2e2 !important; color: #dc2626 !important; }

/* 右侧操作区 */
.topbar-right {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  padding-left: 8px;
}

/* 用户头像 + 名字 */
.user-avatar-name {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px 4px 6px;
  background: #f1f5f9;
  border: 1.5px solid #e2e8f0;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.user-avatar-name:hover {
  border-color: #667eea;
  background: #eef2ff;
}
.user-caret {
  font-size: 12px;
  color: #94a3b8;
  margin-left: 2px;
}
.user-avatar-icon {
  width: 30px; height: 30px;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  flex-shrink: 0;
}
.user-name-text {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  white-space: nowrap;
}

/* 主内容 */
.app-main {
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 24px;
  overflow-y: auto;
}

/* 通知按钮 */
.notification-btn {
  position: relative;
  min-height: 38px;
  min-width: 38px;
  padding: 0 12px;
  border: 1.5px solid #e2e8f0;
  border-radius: 10px;
  background: #fff;
  color: #334155;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}
.notification-btn:hover { border-color: #4a90d9; color: #4a90d9; }
.notification-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  min-width: 20px;
  height: 20px;
  padding: 0 5px;
  background: #dc2626;
  color: #fff;
  font-size: 12px;
  font-weight: 800;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #fff;
}

/* 修复子菜单弹出面板对齐：紧贴触发元素正下方 */
.top-nav :deep(.el-menu--horizontal > .el-sub-menu > .el-menu) {
  position: absolute !important;
  top: 100% !important;
  left: 0 !important;
}

/* ============ 响应式 ============ */
@media (max-width: 1100px) {
  .user-name-text { display: none; }
  .user-caret { display: none; }
}
@media (max-width: 768px) {
  .app-topbar { padding: 0 8px; gap: 4px; }
  .brand-text { display: none; }
}
</style>

<!--
  ============================================================
  AdminLayout.vue - 管理员端布局组件（浅色 App 风格）
  银发智能生活助手 - 后台管理系统
  ============================================================
-->
<template>
  <el-container class="admin-layout">
    <a class="skip-link" href="#admin-main-content">跳到主要内容</a>
    <!-- 左侧管理导航侧边栏（浅色风格，与用户端统一） -->
    <el-aside width="200px" class="admin-aside">
      <!-- 管理员信息区域 -->
      <div class="aside-user-area">
        <div class="admin-avatar">
          <svg viewBox="0 0 56 56" width="56" height="56">
            <defs>
              <linearGradient id="adminAvatarGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" style="stop-color:#9b59b6"/>
                <stop offset="100%" style="stop-color:#6c3483"/>
              </linearGradient>
            </defs>
            <circle cx="28" cy="28" r="26" fill="url(#adminAvatarGrad)"/>
            <circle cx="28" cy="22" r="9" fill="#fff" opacity="0.95"/>
            <ellipse cx="28" cy="44" rx="16" ry="10" fill="#fff" opacity="0.95"/>
          </svg>
        </div>
        <div class="admin-name">{{ username }}</div>
        <div class="admin-role">管理员</div>
      </div>

      <!-- 管理员导航菜单 -->
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#ffffff"
        text-color="#475569"
        active-text-color="#7c3aed"
        class="aside-menu"
        aria-label="管理功能导航"
      >
        <!-- 数据驾驶舱 -->
        <el-menu-item index="/admin"><el-icon><DataBoard /></el-icon>数据驾驶舱</el-menu-item>

        <!-- 用户与老人管理 -->
        <el-sub-menu index="/admin/user-group">
          <template #title><el-icon><UserFilled /></el-icon>用户与老人管理</template>
          <el-menu-item index="/admin/elders"><el-icon><UserFilled /></el-icon>老人档案管理</el-menu-item>
          <el-menu-item index="/admin/users"><el-icon><User /></el-icon>家属账号管理</el-menu-item>
          <el-menu-item index="/admin/elders?tab=binding"><el-icon><Link /></el-icon>家属关系绑定</el-menu-item>
        </el-sub-menu>

        <!-- 设备接入管理 -->
        <el-sub-menu index="/admin/device-group">
          <template #title><el-icon><Monitor /></el-icon>设备接入管理</template>
          <el-menu-item index="/admin/devices"><el-icon><Monitor /></el-icon>设备列表</el-menu-item>
          <el-menu-item index="/admin/device-binding"><el-icon><Link /></el-icon>设备绑定管理</el-menu-item>
          <el-menu-item index="/admin/device-data"><el-icon><Download /></el-icon>健康数据接收</el-menu-item>
          <el-menu-item index="/admin/device-api-log"><el-icon><Document /></el-icon>接口调用日志</el-menu-item>
        </el-sub-menu>

        <!-- 健康数据中心 -->
        <el-sub-menu index="/admin/health-group">
          <template #title><el-icon><TrendCharts /></el-icon>健康数据中心</template>
          <el-menu-item index="/admin/health-record"><el-icon><Document /></el-icon>健康记录</el-menu-item>
          <el-menu-item index="/admin/llm-analysis"><el-icon><MagicStick /></el-icon>LLM分析结果</el-menu-item>
          <el-menu-item index="/admin/health-report"><el-icon><Notebook /></el-icon>健康报告管理</el-menu-item>
        </el-sub-menu>

        <!-- 预警中心 -->
        <el-sub-menu index="/admin/alert-group">
          <template #title><el-icon><Warning /></el-icon>预警中心</template>
          <el-menu-item index="/admin/health-warning"><el-icon><Warning /></el-icon>健康异常预警</el-menu-item>
          <el-menu-item index="/admin/emergency" class="menu-emergency-admin"><el-icon><Bell /></el-icon>SOS紧急事件</el-menu-item>
          <el-menu-item index="/admin/warning-center"><el-icon><Operation /></el-icon>事件处理流程</el-menu-item>
          <el-menu-item index="/admin/push-log"><el-icon><Promotion /></el-icon>通知记录</el-menu-item>
        </el-sub-menu>

        <!-- 服务管理 -->
        <el-sub-menu index="/admin/service-group">
          <template #title><el-icon><Service /></el-icon>服务管理</template>
          <el-menu-item index="/admin/service"><el-icon><AlarmClock /></el-icon>提醒任务管理</el-menu-item>
          <el-menu-item index="/admin/service/medicine"><el-icon><FirstAidKit /></el-icon>用药服务管理</el-menu-item>
          <el-menu-item index="/admin/medical-record"><el-icon><CopyDocument /></el-icon>就医信息管理</el-menu-item>
        </el-sub-menu>

        <!-- 内容运营 -->
        <el-sub-menu index="/admin/content-group">
          <template #title><el-icon><EditPen /></el-icon>内容运营</template>
          <el-menu-item index="/admin/news"><el-icon><EditPen /></el-icon>养老资讯管理</el-menu-item>
          <el-menu-item index="/admin/activity"><el-icon><Tickets /></el-icon>娱乐活动管理</el-menu-item>
        </el-sub-menu>

        <!-- 系统管理 -->
        <el-sub-menu index="/admin/system-group">
          <template #title><el-icon><Setting /></el-icon>系统管理</template>
          <el-menu-item index="/admin/admin-permission"><el-icon><Key /></el-icon>用户权限管理</el-menu-item>
          <el-menu-item index="/admin/logs"><el-icon><Document /></el-icon>操作日志</el-menu-item>
          <el-menu-item index="/admin/admin-config"><el-icon><Setting /></el-icon>系统配置</el-menu-item>
        </el-sub-menu>
      </el-menu>

      <!-- 底部操作 -->
      <div class="aside-footer">
        <el-button @click="goToUser" class="footer-btn">
          <el-icon><HomeFilled /></el-icon>
          <span>返回用户端</span>
        </el-button>
        <el-button @click="handleLogout" class="footer-btn footer-btn-exit">
          <el-icon><SwitchButton /></el-icon>
          <span>退出</span>
        </el-button>
      </div>
    </el-aside>

    <!-- 右侧主体区域 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="admin-header">
        <div class="header-left">
          <span class="header-greeting">管理控制台</span>
          <el-breadcrumb separator="/" class="app-breadcrumb">
            <el-breadcrumb-item :to="{ path: rootPath }">{{ rootLabel }}</el-breadcrumb-item>
            <el-breadcrumb-item v-if="!isRoot">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tag size="large" round class="admin-tag">
            <el-icon><Avatar /></el-icon>
            管理员
          </el-tag>
        </div>
      </el-header>

      <!-- 主内容区域 + 页面切换动画 -->
      <el-main id="admin-main-content" class="admin-main" tabindex="-1">
        <router-view v-slot="{ Component }">
          <transition name="slide-up" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
      <el-backtop target=".admin-main" :visibility-height="200" :right="36" :bottom="90" />
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { getUser, removeToken, removeUser } from '@/utils/auth'

const router = useRouter()
const route = useRoute()

const user = getUser()
const username = ref(user ? (user.nickname || user.username) : '管理员')

const rootPath = computed(() => '/admin')
const rootLabel = computed(() => '管理控制台')
const isRoot = computed(() => ['AdminDashboard','NotFound','ServerError'].includes(route.name))
const currentTitle = computed(() => {
  const t = route.meta.title || ''
  return t.replace(/ - 银发智能生活助手$/, '') || '当前页面'
})
const activeMenu = computed(() => route.path)

const goToUser = () => {
  router.push('/admin')
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出管理后台吗？', '退出确认', {
    confirmButtonText: '确定退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    removeToken()
    removeUser()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
/* ========== 整体布局 ========== */
.admin-layout {
  height: 100vh;
  min-height: 100vh;
}

/* ========== 左侧导航侧边栏（浅色） ========== */
.admin-aside {
  background-color: #ffffff;
  border-right: 1px solid var(--color-border, #e8ecf1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 2px 0 16px rgba(0, 0, 0, 0.04);
}

/* 管理员信息区域（紫色渐变，区分用户端的蓝色） */
.aside-user-area {
  padding: 18px 14px 16px;
  text-align: center;
  border-bottom: 1px solid var(--color-border, #e8ecf1);
  background: linear-gradient(180deg, #f3e8ff 0%, #ffffff 100%);
}

.admin-avatar {
  margin-bottom: 10px;
  display: flex;
  justify-content: center;
}

.admin-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-primary, #1e293b);
  line-height: 1.4;
}

.admin-role {
  font-size: 14px;
  display: inline-block;
  padding: 2px 14px;
  border-radius: 12px;
  font-weight: 500;
  background: #f3e8ff;
  color: #7c3aed;
  margin-top: 4px;
}

/* 导航菜单 */
.aside-menu {
  flex: 1;
  border-right: none;
  padding: 8px 0;
  overflow-y: auto;
}

.aside-menu .el-menu-item {
  height: 54px;
  line-height: 54px;
  font-size: 16px;
  font-weight: 600;
  margin: 3px 10px;
  border-radius: 12px;
  padding: 0 18px !important;
  transition: all 0.2s ease;
}

.aside-menu .el-menu-item:hover {
  background-color: #f6f9fc;
}

.aside-menu .el-menu-item.is-active {
  background-color: #f3e8ff;
  color: #7c3aed;
  font-weight: 600;
}

.aside-menu .el-menu-item .el-icon {
  font-size: 18px;
  margin-right: 10px;
}

/* 求助管理 - 红色高亮 */
.menu-emergency-admin {
  color: #e85d75 !important;
}
.menu-emergency-admin:hover {
  background-color: #fef2f2 !important;
}
.menu-emergency-admin.is-active {
  background-color: #fef2f2 !important;
  color: #e85d75 !important;
  font-weight: 600 !important;
}

/* 底部操作按钮组 */
.aside-footer {
  padding: 10px 12px;
  border-top: 1px solid var(--color-border, #e8ecf1);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.aside-footer .footer-btn {
  width: 100%;
  min-height: 44px;
  font-size: 14px;
  padding: 0 18px !important;
  background-color: #f1f5f9 !important;
  border: 1px solid var(--color-border, #e2e8f0) !important;
  color: var(--color-text-secondary, #64748b) !important;
  border-radius: 10px;
  letter-spacing: normal !important;
}

/* 关键：两个按钮文字长度不同，若内容整体居中，图标 X 位置会不一致，
   看起来就是"没对齐"。改为左对齐 + 统一左内边距，让两个按钮的
   图标处于同一垂直线、文字也从同一处起排，实现真正的对齐。
   选择器加 .aside-footer 前缀提高特异度，压过全局 .el-button 的 padding。 */
.aside-footer .footer-btn,
.aside-footer .footer-btn :deep(> span) {
  display: flex !important;
  align-items: center !important;
  justify-content: flex-start !important;
  gap: 10px;
  line-height: 1;
}
.aside-footer .footer-btn :deep(.el-icon) {
  font-size: 18px;
  width: 18px;
  margin: 0 !important;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.aside-footer .footer-btn :deep(.el-icon svg) {
  display: block;
}
/* Element Plus 给相邻按钮默认加 margin-left:12px，纵向排列时会把
   第二个按钮整体右移，导致图标 X 与第一个按钮错位。强制归零对齐。 */
.aside-footer .footer-btn + .footer-btn {
  margin-left: 0 !important;
}

.footer-btn:hover {
  background-color: #e3f0ff !important;
  color: var(--color-primary, #4a90d9) !important;
  border-color: var(--color-primary-lighter, #a8d4ff) !important;
}

.footer-btn-exit:hover {
  background-color: #fee2e2 !important;
  border-color: #fecaca !important;
  color: #e85d75 !important;
}

/* ========== 右侧顶部栏 ========== */
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #ffffff;
  border-bottom: 1px solid var(--color-border, #e8ecf1);
  padding: 0 24px;
  height: 60px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.header-greeting {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
}

.admin-tag {
  font-size: 14px !important;
  padding: 5px 14px !important;
  min-height: 34px;
  background: #f3e8ff !important;
  border-color: #d8b4fe !important;
  color: #7c3aed !important;
  font-weight: 600 !important;
}

/* ========== 主内容区域 ========== */
.admin-main {
  background: linear-gradient(180deg, #f8fafc 0%, var(--color-bg-page, #f0f4f8) 100%);
  padding: 20px;
  overflow-y: auto;
}

/* 管理端字号与家属端保持同一档，避免银发模式再次放大 Element 菜单 */
:global(.elder-mode) .admin-layout .aside-menu .el-menu-item {
  min-height: 54px !important;
  height: 54px !important;
  line-height: 54px !important;
  font-size: 16px !important;
}
:global(.elder-mode) .admin-layout .aside-menu .el-menu-item .el-icon {
  font-size: 18px !important;
}
:global(.elder-mode) .admin-layout .footer-btn {
  min-height: 44px !important;
  padding: 9px 12px !important;
  font-size: 14px !important;
}
:global(.elder-mode) .admin-main h2,
:global(.elder-mode) .admin-main .page-title {
  font-size: 22px !important;
}
:global(.elder-mode) .admin-main .page-subtitle,
:global(.elder-mode) .admin-main .page-header p {
  font-size: 16px !important;
}
</style>

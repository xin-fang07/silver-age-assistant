<!--
  ============================================================
  emergency/index.vue - SOS 报警记录页（家属端 / 接收视角）
  银发智能生活助手
  功能：选择绑定老人 → 查看其 SOS 报警记录 → 查看位置 / 拨号 / 确认收到
  ============================================================
-->
<template>
  <div class="emergency-page">
    <!-- ========== 页面标题区域 ========== -->
    <div class="emergency-header">
      <h1 class="emergency-title">
        <svg viewBox="0 0 48 48" width="40" height="40" style="vertical-align: middle; margin-right: 8px;">
          <defs>
            <linearGradient id="sosTitleGrad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#e74c3c"/>
              <stop offset="100%" style="stop-color:#c0392b"/>
            </linearGradient>
          </defs>
          <circle cx="24" cy="24" r="22" fill="url(#sosTitleGrad)"/>
          <text x="24" y="31" text-anchor="middle" fill="#fff" font-size="16" font-weight="900" font-family="Arial">SOS</text>
        </svg>
        SOS 报警记录
      </h1>
      <p class="emergency-subtitle">绑定的老人触发 SOS 紧急求助时，报警记录会显示在这里，您可查看位置并第一时间响应</p>
    </div>

    <!-- ========== 选择查看的老人 ========== -->
    <div class="elder-select-section" v-if="elderOptions.length">
      <span class="elder-select-label">查看老人：</span>
      <el-select
        v-model="selectedElderId"
        placeholder="请选择老人"
        size="large"
        style="flex: 1; max-width: 360px;"
        @change="onElderChange"
      >
        <el-option
          v-for="e in elderOptions"
          :key="e.elderInfoId"
          :label="e.realName || e.nickname || ('老人' + e.elderInfoId)"
          :value="e.elderInfoId"
        />
      </el-select>
    </div>
    <el-alert
      v-else
      class="no-elder-alert"
      title="您还没有绑定任何老人"
      description="请先在「我的老人」中绑定老人，绑定后老人触发的 SOS 报警会显示在这里。"
      type="info"
      show-icon
      :closable="false"
    />

    <!-- ========== 接收报警说明 ========== -->
    <div class="receive-banner">
      <el-icon class="receive-banner-icon"><Bell /></el-icon>
      <span class="receive-banner-text">SOS 由绑定的老人通过智能设备或一键呼叫触发，平台会即时向您推送报警通知。您可在此查看每一条报警记录、老人实时位置与处理状态，并第一时间响应。</span>
    </div>

    <!-- ========== 快捷操作区（120拨号 + 模拟设备上报） ========== -->
    <div class="quick-call-section">
      <el-button type="danger" size="large" round @click="call120" style="font-size: 18px; font-weight: 600;">
        <svg viewBox="0 0 24 24" width="22" height="22" style="vertical-align: middle; margin-right: 6px;" fill="currentColor">
          <path d="M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z"/>
        </svg>
        拨打120急救电话
      </el-button>
      <el-button
        v-if="selectedElderId"
        type="warning"
        size="large"
        round
        :loading="simulatingSOS"
        @click="simulateDeviceSOS"
        style="font-size: 16px; font-weight: 600;"
      >
        <svg viewBox="0 0 24 24" width="20" height="20" style="vertical-align: middle; margin-right: 6px;" fill="currentColor">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
        </svg>
        模拟设备上报 SOS
      </el-button>
    </div>

    <!-- ========== SOS 报警记录 ========== -->
    <div class="history-section">
      <h3 class="history-title">
        <svg viewBox="0 0 24 24" width="24" height="24" style="vertical-align: middle; margin-right: 6px;" fill="#2c3e50">
          <path d="M13 3a9 9 0 0 0-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42A8.954 8.954 0 0 0 13 21a9 9 0 0 0 0-18zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"/>
        </svg>
        SOS 报警记录
      </h3>
      <!-- 加载中 -->
      <SkeletonLoader v-if="historyLoading" type="detail" />
      <!-- 无记录 -->
      <div v-else-if="historyList.length === 0" class="history-empty">
        <span>{{ selectedElderId ? '该老人还没有 SOS 报警记录' : '请选择要查看的老人' }}</span>
      </div>
      <!-- 记录列表（时间倒序） -->
      <div v-else class="history-list">
        <div
          v-for="item in historyList"
          :key="item.id"
          class="history-item"
        >
          <div class="history-item-left">
            <span class="history-time">
              <el-icon><Clock /></el-icon>
              {{ formatDateTime(item.createTime || item.createdAt) }}
            </span>
            <span class="history-contact">报警老人：{{ elderName || '绑定老人' }}</span>
            <span v-if="item.acknowledgedAt" class="history-contact">已由 {{ item.acknowledgedName || '家属' }} 于 {{ formatDateTime(item.acknowledgedAt) }} 确认</span>
            <span v-if="item.handleRemark" class="history-contact">处理进展：{{ item.handleRemark }}</span>
            <span v-if="item.locationAccuracy != null" class="history-contact">{{ formatLocationAccuracy(item.locationAccuracy) }}</span>
          </div>
          <div class="history-item-right">
            <!-- 状态标签 -->
            <el-tag
              :type="getStatusMeta(item.status).type"
              size="large"
              effect="dark"
            >
              {{ getStatusMeta(item.status).label }}
            </el-tag>
            <span class="notification-result">{{ getNotificationText(item) }}</span>
            <el-button v-if="item.contactPhone && item.contactPhone !== '未设置'" type="primary" plain size="large" @click="callPhone(item.contactPhone)">一键拨号</el-button>
            <el-button v-if="hasLocation(item)" type="success" plain size="large" @click="openMap(item)">查看位置</el-button>
            <el-button
              v-if="item.status === 0 || item.status === 5"
              type="warning"
              plain
              size="large"
              @click="acknowledge(item)"
            >确认收到</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// ============================================================
// SOS 报警记录页（家属端 / 接收视角）
// 逻辑：选择绑定老人 → 加载其 SOS 报警记录 → 查看位置 / 拨号 / 确认收到
// ============================================================

import { ref, onMounted, onUnmounted } from 'vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Clock } from '@element-plus/icons-vue'
import { familyApi } from '@/api/index'
import { amapMarkerUrl, formatLocationAccuracy } from '@/utils/coordinate'
import { formatFriendlyTime } from '@/utils/friendlyTime'
import axios from 'axios'

// ========== 家属端状态 ==========
const elderOptions = ref([])        // 我绑定的老人列表
const selectedElderId = ref(null)   // 当前查看的老人
const elderName = ref('')           // 当前查看的老人姓名
const historyList = ref([])         // 报警记录列表
const historyLoading = ref(false)   // 加载状态
let refreshTimer = null             // 10秒轮询刷新

// ========== 页面挂载 / 卸载 ==========
onMounted(() => {
  loadElders()
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})

// ========== 加载我绑定的老人 ==========
const loadElders = async () => {
  try {
    const res = await familyApi.myElders()
    const list = (res && res.data) ? res.data : []
    elderOptions.value = list
    if (list.length) {
      const first = list[0]
      selectedElderId.value = first.elderInfoId
      elderName.value = first.realName || first.nickname || '老人'
      fetchHistory()
      refreshTimer = setInterval(fetchHistory, 10000)
    } else {
      historyList.value = []
    }
  } catch (error) {
    console.error('加载绑定老人失败：', error)
    elderOptions.value = []
  }
}

// ========== 切换查看的老人 ==========
const onElderChange = () => {
  const e = elderOptions.value.find(x => x.elderInfoId === selectedElderId.value)
  elderName.value = e ? (e.realName || e.nickname || '老人') : ''
  fetchHistory()
}

// ========== 加载选中老人的 SOS 报警记录 ==========
const fetchHistory = async () => {
  if (!selectedElderId.value) return
  historyLoading.value = true
  try {
    const res = await familyApi.emergencyList(selectedElderId.value)
    if (res && res.data) {
      historyList.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
    } else {
      historyList.value = []
    }
  } catch (error) {
    console.error('加载 SOS 报警记录失败：', error)
    historyList.value = []
  } finally {
    historyLoading.value = false
  }
}

// ========== 家属确认收到报警 ==========
const acknowledge = async (item) => {
  try {
    await ElMessageBox.confirm('确认已收到该 SOS 报警并将立即响应？', '确认接收报警', {
      confirmButtonText: '确认收到',
      cancelButtonText: '稍后处理',
      type: 'warning'
    })
    await familyApi.acknowledgeEmergency(item.id)
    ElMessage.success('已确认收到报警')
    await fetchHistory()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') console.error('确认报警失败：', error)
  }
}

// ========== 状态展示（家属视角）==========
const STATUS_META = {
  0: { label: '待处理', type: 'danger' },
  1: { label: '家属已确认', type: 'warning' },
  2: { label: '处理中', type: 'primary' },
  3: { label: '已完成', type: 'success' },
  4: { label: '已取消', type: 'info' },
  5: { label: '已升级', type: 'danger' }
}

const getStatusMeta = (status) => STATUS_META[Number(status)] || STATUS_META[0]
const getNotificationText = (item) => item.notificationMessage || '平台已收到'

const hasLocation = (item) => item.latitude != null && item.longitude != null
const openMap = (item) => window.open(amapMarkerUrl(item), '_blank')

// ========== 拨号 ==========
const call120 = () => {
  window.location.href = 'tel:120'
}

// ========== 模拟设备上报 SOS（演示用，对齐外接智能设备接口）==========
const simulatingSOS = ref(false)
const simulateDeviceSOS = async () => {
  if (!selectedElderId.value) return
  try {
    await ElMessageBox.confirm(
      `即将模拟设备为「${elderName.value || '该老人'}」上报一条 SOS 报警，确认执行？`,
      '模拟设备上报',
      { confirmButtonText: '确认上报', cancelButtonText: '取消', type: 'warning' }
    )
  } catch { return }
  simulatingSOS.value = true
  try {
    await axios.post('http://localhost:8080/api/device/emergency', {
      elderId: selectedElderId.value,
      content: `【演示】模拟设备上报 — 老人触发 SOS 紧急求助，请家属立即响应`,
      latitude: 29.1234567,
      longitude: 110.2345678,
      locationText: '张家界市永定区XX街道XX小区'
    })
    ElMessage.success('✅ 设备 SOS 已上报！报警记录将在 10 秒内自动刷新显示')
    // 手动触发一次即时刷新
    fetchHistory()
  } catch (e) {
    const msg = e.response?.data?.message || e.message
    if (msg.includes('404') || msg.includes('Not Found')) {
      ElMessage.error('❌ 接口未找到——请确保后端已 Rebuild + 重启（端口 8080）')
    } else {
      ElMessage.error(`上报失败：${msg}`)
    }
  } finally {
    simulatingSOS.value = false
  }
}

const callPhone = async (phone) => {
  if (!phone) return
  try {
    if (/Android|iPhone|iPad|iPod/i.test(navigator.userAgent)) {
      window.location.href = `tel:${phone}`
    } else {
      await ElMessageBox.alert(`请拨打紧急联系人电话：${phone}`, '紧急联系人', { confirmButtonText: '知道了' })
    }
  } catch (e) {
    window.location.href = `tel:${phone}`
  }
}

const formatDateTime = formatFriendlyTime
</script>

<style scoped>
/*
 * 紧急求助页样式 - 适老化设计（红色紧急主题）
 * 原则：超大SOS按钮、红色主题、脉冲动画、二次确认保护
 * ============================================================
 */

/* ========== 整体页面容器 ========== */
.emergency-page {
  min-height: 100%;
  max-width: 700px;
  margin: 0 auto;
  /* 浅红色背景，营造紧急氛围 */
}

/* ========== 页面标题区域 ========== */
.emergency-header {
  text-align: center;
  padding: 16px 0 24px;
}

.emergency-title {
  font-size: 32px;
  font-weight: 800;
  color: #c0392b;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.emergency-subtitle {
  font-size: 20px;
  color: #e74c3c;
  margin: 0;
  line-height: 1.6;
}

/* ========== 选择查看的老人 ========== */
.elder-select-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.elder-select-label {
  font-size: 19px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
  white-space: nowrap;
}

.no-elder-alert {
  margin-bottom: 24px;
}

.no-elder-alert :deep(.el-alert__title) {
  font-size: 18px !important;
}

.no-elder-alert :deep(.el-alert__description) {
  font-size: 16px !important;
}

/* ========== 接收报警说明横幅 ========== */
.receive-banner {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background-color: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 14px;
  padding: 18px 20px;
  margin-bottom: 28px;
}

.receive-banner-icon {
  font-size: 26px;
  color: #f59e0b;
  flex-shrink: 0;
  margin-top: 2px;
}

.receive-banner-text {
  font-size: 17px;
  color: #9a3412;
  line-height: 1.7;
}

/* ========== 紧急联系人信息卡片 ========== */
.contact-info {
  margin-bottom: 24px;
}

.contact-card {
  border-radius: 12px;
  border-color: #fde2e2;
}

.contact-card-title {
  font-size: 20px;
  font-weight: 700;
  color: #c0392b;
  display: flex;
  align-items: center;
  gap: 8px;
}

.contact-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.contact-label {
  font-size: 19px;
  font-weight: 600;
  color: var(--color-text-secondary, #64748b);
}

.contact-value {
  font-size: 19px;
  font-weight: 700;
  color: var(--color-text-primary, #1e293b);
}

/* 无紧急联系人提示 */
.contact-info-empty {
  margin-bottom: 24px;
}

.contact-info-empty :deep(.el-alert__title) {
  font-size: 18px !important;
}

.contact-info-empty :deep(.el-alert__description) {
  font-size: 16px !important;
}

/* ========== 中央SOS按钮区域 ========== */
.sos-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0 32px;
}

/* SOS按钮外层容器 */
.sos-button-wrapper {
  cursor: pointer;
  padding: 0;
  border: 0;
  background: transparent;
  border-radius: 50%;
  /* 点击动画反馈 */
  transition: transform 0.2s;
}

.sos-button-wrapper:active {
  transform: scale(0.95);
}

/* SOS圆形按钮（200px直径） */
.sos-button {
  width: 220px;
  height: 220px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e74c3c, #c0392b);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  /* 立体阴影效果 */
  box-shadow:
    0 8px 32px rgba(231, 76, 60, 0.4),
    inset 0 -4px 8px rgba(0, 0, 0, 0.2),
    inset 0 4px 8px rgba(255, 255, 255, 0.1);
  /* 过渡动画 */
  transition: all 0.3s ease;
  /* 文字不可选中 */
  user-select: none;
}

/* SOS大文字 */
.sos-text {
  font-size: 52px;
  font-weight: 900;
  color: #fff;
  letter-spacing: 6px;
  line-height: 1.2;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

/* SOS副文字 */
.sos-sub-text {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 2px;
}

/* SOS按钮鼠标悬停效果 */
.sos-button:hover {
  box-shadow:
    0 12px 40px rgba(231, 76, 60, 0.5),
    inset 0 -4px 8px rgba(0, 0, 0, 0.2),
    inset 0 4px 8px rgba(255, 255, 255, 0.1);
}

/* SOS脉冲动画 */
.sos-pulsing {
  animation: sosPulse 2s ease-in-out infinite;
}

@keyframes sosPulse {
  0%, 100% {
    box-shadow:
      0 8px 32px rgba(231, 76, 60, 0.4),
      0 0 0 0 rgba(231, 76, 60, 0.6);
  }
  50% {
    box-shadow:
      0 8px 32px rgba(231, 76, 60, 0.4),
      0 0 0 24px rgba(231, 76, 60, 0);
  }
}

/* SOS按钮下方提示 */
.sos-hint {
  margin-top: 20px;
  font-size: 18px;
  color: #e74c3c;
  font-weight: 500;
}

/* ========== SOS进行中状态 ========== */
.sos-active-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 20px;
  background-color: #fef0f0;
  border-radius: 16px;
  border: 2px solid #fde2e2;
  width: 100%;
  max-width: 400px;
}

.sos-active-icon {
  margin-bottom: 16px;
}

.sos-active-text {
  font-size: 20px;
  font-weight: 600;
  color: #c0392b;
  text-align: center;
  margin: 0 0 12px 0;
  line-height: 1.6;
}

.sos-countdown {
  font-size: 48px;
  font-weight: 900;
  color: #e74c3c;
  line-height: 1;
  margin-bottom: 8px;
}

/* ========== 120快捷拨号区域 ========== */
.quick-call-section {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

/* ========== 求助信息表单区域 ========== */
.help-form-section {
  margin-bottom: 32px;
}

.help-form-card {
  border-radius: 12px;
  border-color: #fde2e2;
}

.form-card-title {
  font-size: 20px;
  font-weight: 700;
  color: #c0392b;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ========== 求助历史记录 ========== */
.history-section {
  margin-bottom: 40px;
}

.history-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary, #1e293b);
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #ebeef5;
}

/* 加载和空状态 */
.history-loading,
.history-empty {
  text-align: center;
  padding: 24px 0;
  font-size: 18px;
  color: var(--color-text-secondary, #64748b);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* 历史记录列表 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 单条历史记录 */
.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  gap: 16px;
  flex-wrap: wrap;
}

/* 左侧信息 */
.history-item-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* 求助时间 */
.history-time {
  font-size: 17px;
  color: var(--color-text-secondary, #64748b);
  display: flex;
  align-items: center;
  gap: 4px;
}

.history-time .el-icon {
  font-size: 18px;
}

/* 联系对象 */
.history-contact {
  font-size: 18px;
  font-weight: 500;
  color: var(--color-text-primary, #1e293b);
}

/* 右侧状态标签 */
.history-item-right {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.notification-result {
  max-width: 260px;
  font-size: 15px;
  color: var(--color-text-secondary, #64748b);
  text-align: right;
}

.history-item-right .el-tag {
  font-size: 16px !important;
  padding: 6px 18px !important;
  min-height: 36px;
  line-height: 36px;
}

/* ========== 全局 Element Plus 组件覆盖（适老化） ========== */
:deep(.el-card__header) {
  padding: 16px 24px;
  border-bottom: 2px solid #fde2e2;
}

:deep(.el-card__body) {
  padding: 20px 24px;
}

.countdown-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 16px;
}
</style>

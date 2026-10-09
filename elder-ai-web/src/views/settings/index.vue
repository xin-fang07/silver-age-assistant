<!--
  settings/index.vue - 系统设置页面
  银发智能生活助手 - 适老化显示、通知、隐私与账号、数据管理
-->
<template>
  <div class="settings-page">
    <div class="page-header">
      <h2 class="page-title">
        <svg viewBox="0 0 24 24" width="32" height="32" fill="#4a90d9" style="vertical-align:middle;margin-right:10px">
          <path d="M19.14 12.94c.04-.31.06-.63.06-.94 0-.31-.02-.63-.06-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.04.31-.06.63-.06.94s.02.63.06.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/>
        </svg>
        系统设置
      </h2>
      <p class="page-subtitle">个性化您的使用体验，管理系统设置</p>
    </div>

    <!-- 适老与显示 -->
    <div class="section-card">
      <div class="sc-title"><el-icon><Sunny /></el-icon> 适老与显示</div>
      <div class="sc-body">
        <div class="setting-item">
          <span class="si-label">长辈模式</span>
          <span class="si-hint">放大按钮、加粗文字、提升可读性</span>
          <el-switch :model-value="isElderMode" @change="setElderMode" />
        </div>
        <div class="setting-item column">
          <div class="si-label-row">
            <span class="si-label">字体大小</span>
            <span class="si-hint">调整全站字号（小 / 标准 / 大 / 特大）</span>
          </div>
          <el-radio-group class="font-radio" :model-value="fontSize" @change="setFontSize">
            <el-radio-button value="small">小</el-radio-button>
            <el-radio-button value="medium">标准</el-radio-button>
            <el-radio-button value="large">大</el-radio-button>
            <el-radio-button value="xlarge">特大</el-radio-button>
          </el-radio-group>
        </div>
        <div class="setting-item">
          <span class="si-label">高对比度</span>
          <span class="si-hint">加深文字与边框，更清晰</span>
          <el-switch :model-value="isHighContrast" @change="setHighContrast" />
        </div>
        <div class="setting-item">
          <span class="si-label">减弱动画</span>
          <span class="si-hint">降低页面过渡与动效</span>
          <el-switch :model-value="isReduceMotion" @change="setReduceMotion" />
        </div>
      </div>
    </div>

    <!-- 语音与朗读 -->
    <div class="section-card">
      <div class="sc-title"><el-icon><Microphone /></el-icon> 语音与朗读</div>
      <div class="sc-body">
        <div class="setting-item column">
          <div class="si-label-row">
            <span class="si-label">语音朗读</span>
            <span class="si-hint">控制家属端全部语音播报</span>
          </div>
          <div class="notify-desc">开启后，进入模块的自动播报、聊天与资讯朗读、语音王悬浮朗读等均可使用；关闭后家属端不再发出任何语音。（该开关与登录页的登录语音开关相互独立）</div>
          <el-switch :model-value="familyVoiceEnabled" @change="onFamilyVoiceChange" />
        </div>
      </div>
    </div>

    <!-- 通知设置 -->
    <div class="section-card">
      <div class="sc-title"><el-icon><Bell /></el-icon> 通知设置</div>
      <div class="sc-body">
        <div class="setting-item column">
          <div class="si-label-row">
            <span class="si-label">桌面通知</span>
            <span class="si-hint" :class="{ warn: notifyPermissionWarn }">当前状态：{{ notifyPermissionText }}</span>
          </div>
          <div class="notify-desc">开启后，页面后台运行时也能收到提醒弹窗，点击可直接跳转</div>
          <el-switch
            :model-value="browserNotifyEnabled"
            @change="onBrowserNotifyChange"
            :disabled="notifyPermissionWarn"
          />
        </div>
        <div class="setting-item column">
          <div class="si-label-row">
            <span class="si-label">声音提醒</span>
            <span class="si-hint">收到通知时播放提示音</span>
          </div>
          <el-switch :model-value="soundNotifyEnabled" @change="onSoundNotifyChange" />
        </div>
        <div class="setting-item">
          <span class="si-label">生活提醒推送</span>
          <span class="si-hint">吃药、运动等到点提醒</span>
          <el-switch :model-value="reminderPushEnabled" @change="onReminderPushChange" />
        </div>
        <div class="setting-item">
          <span class="si-label">健康预警推送</span>
          <span class="si-hint">血压、血糖异常时提醒</span>
          <el-switch :model-value="warningPushEnabled" @change="onWarningPushChange" />
        </div>
      </div>
    </div>

    <!-- 隐私与账号 -->
    <div class="section-card">
      <div class="sc-title"><el-icon><Lock /></el-icon> 隐私与账号</div>
      <div class="sc-body">
        <div class="setting-entry" @click="goPrivacy">
          <span class="se-label"><el-icon><Document /></el-icon> 隐私政策与用户协议</span>
          <el-icon class="se-arrow"><ArrowRight /></el-icon>
        </div>
        <div class="setting-item">
          <span class="si-label">个性化推荐授权</span>
          <span class="si-hint">关闭后停止基于数据的个性化推荐</span>
          <el-switch v-model="personalizedAuth" @change="onPersonalizedChange" />
        </div>
        <div class="setting-item">
          <span class="si-label">注销账号</span>
          <span class="si-hint">清除本设备登录并申请注销</span>
          <el-button type="danger" plain size="small" @click="handleDeleteAccount">注销</el-button>
        </div>
      </div>
    </div>

    <!-- 数据管理 -->
    <div class="section-card">
      <div class="sc-title"><el-icon><Delete /></el-icon> 数据管理</div>
      <div class="sc-body">
        <div class="setting-item">
          <span class="si-label">清除缓存</span>
          <el-button type="primary" plain @click="clearCache">清除</el-button>
        </div>
        <div class="setting-item">
          <span class="si-label">清除聊天记录</span>
          <el-button type="danger" plain @click="clearChatHistory">清除</el-button>
        </div>
      </div>
    </div>

    <!-- 关于 -->
    <div class="section-card">
      <div class="sc-title"><el-icon><InfoFilled /></el-icon> 关于系统</div>
      <div class="sc-body about-info">
        <p><strong>银发智能生活助手</strong></p>
        <p>基于 LLM 的智能养老服务平台</p>
        <p>版本 1.0.0</p>
        <p class="about-slogan">让科技温暖您的每一天</p>
      </div>
    </div>

    <!-- 保存按钮 -->
    <div class="save-area">
      <el-button type="primary" size="large" :loading="saving" @click="saveSettings">
        <el-icon><Check /></el-icon> 保存设置
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useElderMode } from '@/composables/useElderMode'
import { useReminderNotify } from '@/composables/useReminderNotify'
import { useSpeech } from '@/composables/useSpeech'
import { removeToken, removeUser } from '@/utils/auth'
import { userApi, chatRecordApi } from '@/api/index'

const router = useRouter()
const {
  isElderMode, setElderMode,
  isHighContrast, setHighContrast,
  isReduceMotion, setReduceMotion,
  fontSize, setFontSize
} = useElderMode()

// 家属端语音开关B：控制家属端全部播报（与登录页开关A相互独立）
const { familyVoiceEnabled, toggleFamilyVoice } = useSpeech()
const onFamilyVoiceChange = () => {
  // toggle 内部已负责翻转、持久化、关闭时停止当前朗读
  toggleFamilyVoice()
}

const PERSONALIZED_KEY = 'elder_ai_personalized'

// 使用统一的通知管理
const {
  browserNotifyEnabled,
  soundNotifyEnabled,
  reminderPushEnabled,
  warningPushEnabled,
  isNotificationSupported,
  getNotificationPermission,
  toggleBrowserNotify,
  toggleSoundNotify,
  setReminderPush,
  setWarningPush
} = useReminderNotify()

const personalizedAuth = ref(true)
const saving = ref(false)

// 通知权限状态文本
const notifyPermissionText = computed(() => {
  if (!isNotificationSupported()) return '不支持'
  const p = getNotificationPermission()
  return { default: '未授权', granted: '已授权', denied: '已拒绝' }[p] || p
})

const notifyPermissionWarn = computed(() => {
  if (!isNotificationSupported()) return true
  return getNotificationPermission() === 'denied'
})

const loadSettings = () => {
  personalizedAuth.value = localStorage.getItem(PERSONALIZED_KEY) !== 'false'
}

// 浏览器通知开关变化
const onBrowserNotifyChange = async (val) => {
  await toggleBrowserNotify(val)
}

// 声音提醒开关变化
const onSoundNotifyChange = (val) => {
  toggleSoundNotify(val)
}

// 提醒推送开关
const onReminderPushChange = (val) => {
  setReminderPush(val)
}

// 预警推送开关
const onWarningPushChange = (val) => {
  setWarningPush(val)
}

const onPersonalizedChange = (val) => {
  localStorage.setItem(PERSONALIZED_KEY, val ? 'true' : 'false')
}

const saveSettings = async () => {
  saving.value = true
  try {
    // 通知设置已实时保存，这里只保存个性化设置
    localStorage.setItem(PERSONALIZED_KEY, personalizedAuth.value ? 'true' : 'false')
    await new Promise(resolve => setTimeout(resolve, 400))
    ElMessage.success('设置已保存')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const goPrivacy = () => router.push('/privacy')

const handleDeleteAccount = async () => {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '提交后当前登录会立即失效，管理员审核前账户数据仍会保留。您可以填写注销原因（选填）。',
      '注销账号',
      {
        confirmButtonText: '提交注销申请',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger',
        inputType: 'textarea',
        inputPlaceholder: '请填写注销原因（选填）',
        inputValidator: value => !value || value.length <= 500 || '注销原因不能超过500字'
      }
    )
    await userApi.requestDeletion({ reason: reason?.trim() || '' })
    removeToken()
    removeUser()
    ElMessage.success('注销申请已保存，当前登录已失效')
    router.push('/login')
  } catch (e) { /* 取消 */ }
}

const clearCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清除所有缓存吗？这不会影响您的个人数据。', '清除缓存', {
      confirmButtonText: '确定清除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    localStorage.removeItem('elder_ai_weather_cache')
    localStorage.removeItem('elder_ai_health_cache')
    sessionStorage.clear()
    ElMessage.success('缓存已清除')
  } catch (e) { }
}

const clearChatHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清除所有聊天记录吗？此操作不可恢复。', '清除聊天记录', {
      confirmButtonText: '确定清除',
      cancelButtonText: '取消',
      type: 'error',
      confirmButtonClass: 'el-button--danger',
      buttonSize: 'large',
      autofocus: false
    })
    await chatRecordApi.clearAll()
    localStorage.removeItem('elder_ai_chat_history2')
    localStorage.removeItem('elder_ai_chat_history')
    ElMessage.success('聊天记录已清除')
  } catch (e) { }
}

onMounted(() => {
  loadSettings()
})
</script>

<style scoped>
.settings-page { padding: 0 0 24px; animation: fadeIn .4s ease; }
@keyframes fadeIn { from{opacity:0;transform:translateY(8px)} to{opacity:1;transform:translateY(0)} }

.page-header { padding: 24px 0 16px; text-align: center; }
.page-title { font-size: 28px; font-weight: 700; color: #1e293b; margin: 0; }
.page-subtitle { font-size: 15px; color: #64748b; margin: 8px 0 0; }

.section-card { background: #fff; border-radius: 16px; padding: 18px; margin: 0 auto 16px; max-width: 560px; box-shadow: 0 2px 10px rgba(0,0,0,.05); }
.sc-title { font-size: 17px; font-weight: 600; color: #1e293b; display: flex; align-items: center; gap: 8px; margin-bottom: 16px; }
.sc-body { padding-top: 8px; }

.setting-item { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 14px 0; border-bottom: 1px solid #f1f5f9; }
.setting-item:last-child { border-bottom: none; }
.setting-item.column { flex-direction: column; align-items: stretch; gap: 12px; }
.si-label { font-size: 16px; color: #334155; flex-shrink: 0; }
.si-label-row { display: flex; align-items: baseline; justify-content: space-between; gap: 12px; flex-wrap: wrap; }
.si-hint { font-size: 12px; color: #94a3b8; margin-left: auto; text-align: right; }
.setting-item.column .si-hint { margin-left: 0; text-align: left; }

.font-radio { width: 100%; }
.font-radio :deep(.el-radio-button__inner) { width: 100%; font-size: 16px; font-weight: 600; padding: 12px 0; }

.setting-entry { display: flex; align-items: center; justify-content: space-between; padding: 14px 0; border-bottom: 1px solid #f1f5f9; cursor: pointer; transition: background .15s; }
.setting-entry:last-child { border-bottom: none; }
.setting-entry:hover { background: #f8fafc; }
.se-label { display: flex; align-items: center; gap: 8px; font-size: 16px; color: #334155; }
.se-arrow { color: #94a3b8; }

.about-info { text-align: center; line-height: 2; }
.about-info p { margin: 4px 0; color: #334155; }
.about-slogan { color: #94a3b8; font-style: italic; }

.save-area { text-align: center; padding: 16px 0 32px; }
.save-area .el-button { min-width: 200px; }

.notify-desc {
  font-size: 13px;
  color: #94a3b8;
  line-height: 1.5;
  margin: 4px 0 8px;
}

.si-hint.warn {
  color: #dc2626;
  font-weight: 500;
}
</style>

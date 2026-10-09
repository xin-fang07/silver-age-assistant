<!--
  home/index.vue - 家属端「首页 / 智能照护驾驶舱」
  银发智能生活助手 · 家属端
  企业级 SaaS Dashboard 风格（7 区域）：
  ① 顶部欢迎栏 ② 我的老人核心卡 ③ 健康四宫格
  ④ LLM 智能报告渐变卡 ⑤ 风险预警 + 今日照护双列
  ⑥ 快捷入口 4 按钮 ⑦ 养老资讯卡
  仅优化前端 UI，不改后端接口，不改路由。
-->
<template>
  <div class="dash">
    <!-- ========== ① 顶部欢迎栏 ========== -->
    <header class="dash-top">
      <div class="top-greet">
        <div class="greet-hi">
          {{ greetText }}，{{ userName }}
          <span class="guard-badge"><i class="guard-dot"></i>守护中</span>
        </div>
        <div class="greet-sub">
          <span>{{ todayDate }}</span>
          <span class="sep">·</span>
          <span>正在守护 <b>{{ elders.length }}</b> 位老人</span>
          <span class="sep">·</span>
          <span class="tip">今日贴士：{{ dailyTip }}</span>
        </div>
      </div>
      <div class="top-right">
        <div class="weather-chip" :class="{loading:weatherLoading}" @click="changeWeatherCity" title="点击切换城市">
          <svg v-if="!weatherLoading" viewBox="0 0 48 48" width="22" height="22" v-html="weatherIconSvg"></svg>
          <el-icon v-else class="is-loading"><Loading /></el-icon>
          <span class="w-city">{{ weatherCity }}</span>
          <span class="w-temp" v-if="!weatherLoading">{{ weather.temp }}° {{ weather.text }}</span>
        </div>
      </div>
    </header>

    <!-- ========== ② 我的老人核心卡 ========== -->
    <section v-if="elders.length > 0" class="elder-card">
        <div class="elder-head">
          <el-avatar :size="58" class="elder-avatar">{{ elderInitial }}</el-avatar>
          <div class="elder-meta">
            <div class="elder-name">
              {{ selectedElder.realName || '未命名老人' }}
              <el-tag v-if="selectedElder.relation" size="small" effect="plain" class="rel-tag">{{ selectedElder.relation }}</el-tag>
            </div>
            <div class="elder-sub">
              {{ genderText(selectedElder.gender) }}
              <template v-if="selectedElder.age != null"> · {{ selectedElder.age }} 岁</template>
              <template v-if="selectedElder.birthDate"> · 生日 {{ selectedElder.birthDate }}</template>
            </div>
          </div>
          <el-button class="detail-btn" round @click="go('/care/elder/' + selectedElder.elderInfoId)">
            查看详情<el-icon class="ml4"><ArrowRight /></el-icon>
          </el-button>
        </div>

        <!-- 核心指标 -->
        <div class="elder-stats">
          <div class="stat" @click="go('/care/reminder')">
            <div class="stat-v" :class="{zero: todayPendingCare.length === 0}">{{ todayPendingCare.length }}</div>
            <div class="stat-l">待办提醒</div>
          </div>
          <div class="stat" @click="go('/care/alerts')">
            <div class="stat-v" :class="{danger: pendingWarnings.length > 0}">{{ pendingWarnings.length }}</div>
            <div class="stat-l">风险预警</div>
          </div>
          <div class="stat" @click="go('/care/health')">
            <div class="stat-v">{{ normalMetricCount }}<small>/{{ healthItems.length }}</small></div>
            <div class="stat-l">指标正常</div>
          </div>
        </div>

        <!-- 多老人切换 -->
        <div class="elder-tabs" v-if="elders.length > 1">
          <span
            v-for="e in elders"
            :key="e.elderInfoId"
            class="elder-tab"
            :class="{active: e.elderInfoId === selectedId}"
            @click="selectElder(e)"
          >{{ e.realName || '老人' }}</span>
        </div>
      </section>

      <!-- 未绑定老人：内嵌提示（不阻断其余区域渲染） -->
      <section v-else-if="!eldersLoading" class="empty-elder">
        <div class="empty-ico">👵</div>
        <div class="empty-title">还没有绑定要照护的老人</div>
        <div class="empty-desc">绑定老人后，这里将展示健康、预警与照护全景。</div>
        <el-button type="primary" round @click="go('/care/family-bind')">去绑定老人</el-button>
      </section>

      <!-- ========== ③ 健康四宫格 ========== -->
      <section class="block">
        <div class="block-head">
          <span class="block-title"><el-icon class="bt-ic"><Odometer /></el-icon>健康概览</span>
          <span class="block-more" @click="go('/care/health')">查看全部 ›</span>
        </div>
        <div v-if="elderLoading" class="mini-loading"><el-icon class="is-loading"><Loading /></el-icon>健康数据加载中…</div>
        <div v-else class="health-grid4">
          <div v-for="h in healthItems" :key="h.key" class="h-tile" :class="h.status" @click="go('/care/health')">
            <div class="h-ic"><el-icon :size="20"><component :is="h.icon" /></el-icon></div>
            <div class="h-v">{{ h.value }}<small>{{ h.unit }}</small></div>
            <div class="h-l">{{ h.label }}</div>
            <div class="h-flag" :class="h.status">{{ statusText(h.status) }}</div>
          </div>
        </div>
      </section>

      <!-- ========== ④ LLM 智能报告渐变卡 ========== -->
      <section class="llm-card" @click="go('/care/health-report')">
        <div class="llm-top">
          <span class="llm-badge"><el-icon><MagicStick /></el-icon>AI 智能分析</span>
          <span class="llm-more">查看完整报告 ›</span>
        </div>
        <div class="llm-title">健康智能报告</div>
        <div class="llm-body" :title="adviceText">
          {{ adviceText || '正在为 ' + (selectedElder.realName || '老人') + ' 生成健康建议…' }}
        </div>
      </section>

      <!-- ========== ⑤ 风险预警 + 今日照护 双列 ========== -->
      <div class="two-col">
        <section class="block warn-col">
          <div class="block-head">
            <span class="block-title"><el-icon class="bt-ic warn"><WarningFilled /></el-icon>风险预警</span>
            <span class="block-more" @click="go('/care/alerts')">全部 ›</span>
          </div>
          <div v-if="elderLoading" class="mini-loading"><el-icon class="is-loading"><Loading /></el-icon>加载中…</div>
          <template v-else>
            <div v-if="warnings.length" class="warn-list">
              <div v-for="(w, i) in warnings.slice(0,4)" :key="i" class="warn-row" :class="w.sev" @click="go('/care/alerts')">
                <span class="warn-dot"></span>
                <div class="warn-main">
                  <div class="warn-title">{{ w.title }}</div>
                  <div class="warn-time">{{ w.time }}</div>
                </div>
                <span class="warn-tag" :class="w.sev">{{ w.statusText }}</span>
              </div>
            </div>
            <div v-else class="empty-tip">暂无风险预警 🎉</div>
          </template>
        </section>

        <section class="block care-col">
          <div class="block-head">
            <span class="block-title"><el-icon class="bt-ic"><List /></el-icon>今日照护</span>
            <span class="block-more" @click="go('/care/reminder')">全部 ›</span>
          </div>
          <div v-if="elderLoading" class="mini-loading"><el-icon class="is-loading"><Loading /></el-icon>加载中…</div>
          <template v-else>
            <div v-if="todayCare.length" class="care-list">
              <div v-for="(r, i) in todayCare.slice(0,4)" :key="i" class="care-row" :class="{done:r.done}" @click="go('/care/reminder')">
                <span class="care-dot" :style="{background: r.color}"></span>
                <div class="care-main">
                  <div class="care-title">{{ r.title }}</div>
                  <div class="care-time">{{ r.time }}</div>
                </div>
                <span class="care-type" :style="{color: r.color, borderColor: r.color + '55', background: r.color + '12'}">{{ r.typeLabel }}</span>
              </div>
            </div>
            <div v-else class="empty-tip">今日照护事项已安排妥当 ✓</div>
          </template>
        </section>
      </div>

      <!-- ========== ⑥ 快捷入口 4 按钮 ========== -->
      <section class="quick-row">
        <div v-for="q in quickEntries" :key="q.path" class="quick" @click="go(q.path)">
          <div class="quick-ic" :style="{background: q.bg, color: q.color}">
            <el-icon :size="24"><component :is="q.icon" /></el-icon>
          </div>
          <div class="quick-label">{{ q.label }}</div>
        </div>
      </section>

      <!-- ========== ⑦ 养老资讯卡 ========== -->
      <section class="block">
        <div class="block-head">
          <span class="block-title"><el-icon class="bt-ic"><Reading /></el-icon>养老资讯</span>
          <span class="block-more" @click="go('/care/news')">更多 ›</span>
        </div>
        <div v-if="newsLoading" class="mini-loading"><el-icon class="is-loading"><Loading /></el-icon>资讯加载中…</div>
        <div v-else-if="newsList.length" class="news-grid">
          <div v-for="n in newsList" :key="n.id" class="news-item" @click="go('/care/news/' + n.id)">
            <div class="news-cover" :style="coverStyle(n)">
              <span class="news-cat">{{ n.category }}</span>
            </div>
            <div class="news-title">{{ n.title }}</div>
          </div>
        </div>
        <div v-else class="empty-tip">暂无最新资讯</div>
      </section>

    <div class="dash-bottom-space"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUser } from '@/utils/auth'
import { familyApi, reminderApi, newsApi, weatherApi } from '@/api/index'

const router = useRouter()
const user = getUser()
const userName = ref(user ? (user.nickname || user.username) : '家属')

const todayDate = (() => {
  const d = new Date()
  const w = ['日', '一', '二', '三', '四', '五', '六']
  return d.getFullYear() + '年' + (d.getMonth() + 1) + '月' + d.getDate() + '日 星期' + w[d.getDay()]
})()

const greetText = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

// 每日健康小贴士
const healthTips = [
  '老年人每天应保证 8 小时睡眠，午睡建议控制在 30 分钟以内',
  '夏季高温天气，建议早晨 6-8 点和傍晚后户外活动',
  '每天饮水量建议 1500-2000 毫升，少量多次更健康',
  '低盐饮食有助于血压稳定，每日盐摄入不超过 5 克',
  '适当进行手指操、太极拳，有助于保持大脑活力',
  '定期监测血压、血糖，及时了解身体状况变化',
  '保持心情舒畅，多与家人朋友沟通交流',
  '饮食均衡，多吃新鲜蔬菜水果补充维生素',
  '走路注意安全，穿防滑鞋，避免摔倒',
  '定期体检，及时发现潜在健康问题'
]
const dailyTip = computed(() => healthTips[new Date().getDate() % healthTips.length])

// ========== 我的老人 ==========
const elders = ref([])
const eldersLoading = ref(true)
const selectedId = ref(null)
const elderLoading = ref(false)

const selectedElder = computed(() => elders.value.find(e => e.elderInfoId === selectedId.value) || elders.value[0] || {})
const elderInitial = computed(() => {
  const n = selectedElder.value.realName || selectedElder.value.nickname || '老'
  return n ? n.charAt(0) : '老'
})

// ========== 健康四宫格 ==========
const healthItems = ref([
  { key: 'bloodPressure', label: '血压', unit: 'mmHg', value: '--', status: 'none', icon: 'Monitor' },
  { key: 'bloodSugar', label: '血糖', unit: 'mmol/L', value: '--', status: 'none', icon: 'Coffee' },
  { key: 'heartRate', label: '心率', unit: '次/分', value: '--', status: 'none', icon: 'Heart' },
  { key: 'weight', label: '体重', unit: 'kg', value: '--', status: 'none', icon: 'Odometer' }
])
const normalMetricCount = computed(() => healthItems.value.filter(h => h.status === 'normal').length)
const statusText = (s) => ({ normal: '正常', warn: '偏高/异常', none: '暂无' }[s] || '暂无')

// ========== LLM 报告 ==========
const adviceText = ref('')

// ========== 风险预警 ==========
const warnings = ref([])
const pendingWarnings = computed(() => warnings.value.filter(w => w.status === 'PENDING'))

// ========== 今日照护 ==========
const todayCare = ref([])
const todayPendingCare = computed(() => todayCare.value.filter(r => !r.done))

// ========== 养老资讯 ==========
const newsList = ref([])
const newsLoading = ref(true)

// ========== 快捷入口 ==========
const quickEntries = [
  { label: '健康数据', path: '/care/health', icon: 'DataLine', bg: '#eff6ff', color: '#2563eb' },
  { label: 'AI 助手', path: '/care/chat', icon: 'ChatDotRound', bg: '#ecfdf5', color: '#059669' },
  { label: '紧急求助', path: '/care/emergency', icon: 'FirstAidKit', bg: '#fef2f2', color: '#dc2626' },
  { label: '娱乐活动', path: '/care/activity', icon: 'Basketball', bg: '#faf5ff', color: '#7c3aed' }
]

const go = (p) => router.push(p)

// ========== 天气 ==========
const WEATHER_CITY_KEY = 'elder_ai_weather_city'
const weatherCity = ref(localStorage.getItem(WEATHER_CITY_KEY) || '北京')
const weatherLoading = ref(false)
const weather = ref({ text: '晴', temp: 26, iconCode: 'sunny', humidity: 45, windSpeed: 3 })
const fetchWeather = async () => {
  weatherLoading.value = true
  try {
    const res = await weatherApi.currentByCity(weatherCity.value)
    if (res.data) {
      weather.value = {
        text: res.data.weatherText || '晴',
        temp: res.data.temperature ?? 26,
        iconCode: res.data.iconCode || 'sunny',
        humidity: res.data.humidity ?? 45,
        windSpeed: res.data.windSpeed ?? 3
      }
    }
  } catch (e) {
    console.error('天气加载失败：', e)
  } finally {
    weatherLoading.value = false
  }
}
const changeWeatherCity = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入城市名称', '切换城市', {
      confirmButtonText: '确定', cancelButtonText: '取消', inputValue: weatherCity.value,
      inputValidator: (v) => v ? true : '城市名称不能为空'
    })
    if (value && value.trim()) {
      weatherCity.value = value.trim()
      localStorage.setItem(WEATHER_CITY_KEY, weatherCity.value)
      await fetchWeather()
    }
  } catch (e) {}
}
const weatherIconSvg = computed(() => {
  const code = weather.value.iconCode || 'sunny'
  const map = {
    sunny: `<circle cx="24" cy="24" r="10" fill="#fbbf24"/><g stroke="#fbbf24" stroke-width="3" stroke-linecap="round"><line x1="24" y1="4" x2="24" y2="10"/><line x1="24" y1="38" x2="24" y2="44"/><line x1="4" y1="24" x2="10" y2="24"/><line x1="38" y1="24" x2="44" y2="24"/><line x1="9" y1="9" x2="13" y2="13"/><line x1="35" y1="35" x2="39" y2="39"/><line x1="39" y1="9" x2="35" y2="13"/><line x1="13" y1="35" x2="9" y2="39"/></g>`,
    cloudy: `<circle cx="18" cy="22" r="8" fill="#fbbf24"/><path d="M16 34 Q20 26 30 28 Q40 24 40 34 Q40 42 30 42 L22 42 Q12 42 16 34" fill="#e2e8f0" stroke="#cbd5e1" stroke-width="2"/>`,
    overcast: `<path d="M12 38 Q16 26 30 28 Q46 22 46 36 Q46 46 34 46 L20 46 Q8 46 12 38" fill="#cbd5e1" stroke="#94a3b8" stroke-width="2"/>`,
    rain: `<path d="M14 32 Q18 24 30 26 Q42 20 42 32 Q42 42 30 42 L20 42 Q10 42 14 32" fill="#e2e8f0" stroke="#cbd5e1" stroke-width="2"/><g stroke="#3b82f6" stroke-width="2.5" stroke-linecap="round"><line x1="20" y1="44" x2="18" y2="50"/><line x1="28" y1="44" x2="26" y2="50"/><line x1="36" y1="44" x2="34" y2="50"/></g>`,
    thunderstorm: `<path d="M14 30 Q18 22 30 24 Q42 18 42 30 Q42 40 30 40 L20 40 Q10 40 14 30" fill="#e2e8f0" stroke="#cbd5e1" stroke-width="2"/><path d="M28 38 L22 48 L28 48 L24 58 L34 46 L28 46 Z" fill="#fbbf24" stroke="#f59e0b" stroke-width="1.5"/>`,
    snow: `<path d="M14 32 Q18 24 30 26 Q42 20 42 32 Q42 42 30 42 L20 42 Q10 42 14 32" fill="#e2e8f0" stroke="#cbd5e1" stroke-width="2"/><g fill="#fff" stroke="#60a5fa" stroke-width="1.5"><circle cx="20" cy="48" r="2"/><circle cx="28" cy="46" r="2"/><circle cx="36" cy="49" r="2"/></g>`,
    fog: `<path d="M10 30 Q16 24 30 26 Q44 22 48 30" fill="none" stroke="#cbd5e1" stroke-width="3" stroke-linecap="round"/><path d="M8 38 Q16 32 32 34 Q44 30 48 38" fill="none" stroke="#94a3b8" stroke-width="3" stroke-linecap="round"/>`,
    unknown: `<circle cx="24" cy="24" r="10" fill="#fbbf24"/><g stroke="#fbbf24" stroke-width="3" stroke-linecap="round"><line x1="24" y1="4" x2="24" y2="10"/><line x1="24" y1="38" x2="24" y2="44"/><line x1="4" y1="24" x2="10" y2="24"/><line x1="38" y1="24" x2="44" y2="24"/></g>`
  }
  return map[code] || map.sunny
})

// ========== 工具函数 ==========
const genderText = (g) => {
  if (!g) return '—'
  if (['男', 'MALE', 'M'].includes(g)) return '男'
  if (['女', 'FEMALE', 'F'].includes(g)) return '女'
  return String(g)
}
const isToday = (value) => {
  if (!value) return false
  const date = new Date(String(value).replace(' ', 'T'))
  if (isNaN(date.getTime())) return false
  const now = new Date()
  return date.getFullYear() === now.getFullYear() && date.getMonth() === now.getMonth() && date.getDate() === now.getDate()
}
const formatTime = (t) => {
  if (!t) return ''
  if (typeof t === 'string' && /^\d{1,2}:\d{2}/.test(t)) return t.slice(0, 5)
  const d = new Date(String(t).replace(' ', 'T'))
  if (isNaN(d.getTime())) return String(t)
  return String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0')
}
const formatDateTime = (t) => {
  if (!t) return ''
  const d = new Date(String(t).replace(' ', 'T'))
  if (isNaN(d.getTime())) return String(t)
  return (d.getMonth() + 1) + '月' + d.getDate() + '日 ' + String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0')
}
const evalStatus = (k, v) => {
  if (!v || v === '--') return 'none'
  if (k === 'bloodPressure') { const m = String(v).match(/(\d+)/); const s = m ? Number(m[1]) : 0; return s > 140 || s < 90 ? 'warn' : 'normal' }
  if (k === 'heartRate') { const n = Number(v); return n > 100 || n < 60 ? 'warn' : 'normal' }
  if (k === 'bloodSugar') { const n = Number(v); return n > 7 || n < 3.9 ? 'warn' : 'normal' }
  return 'normal'
}

// 提醒类型配色
const REMINDER_TYPES = {
  MEDICINE: { label: '用药', color: '#2563eb' },
  MEDICATION: { label: '用药', color: '#2563eb' },
  WATER: { label: '饮水', color: '#059669' },
  EXERCISE: { label: '运动', color: '#7c3aed' },
  MEAL: { label: '用餐', color: '#d97706' },
  CHECKUP: { label: '体检', color: '#dc2626' },
  SLEEP: { label: '睡眠', color: '#0ea5e9' },
  BIRTHDAY: { label: '生日', color: '#db2777' }
}
const mapReminder = (r) => {
  const type = r.remindType || r.type || ''
  const t = REMINDER_TYPES[String(type).toUpperCase()] || { label: type || '提醒', color: '#64748b' }
  const done = r.status === 1 || r.status === '1'
  return { ...r, type, typeLabel: t.label, color: t.color, done, time: formatTime(r.remindTime || r.time) }
}

// 预警状态映射
const WARNING_STATUS = { PENDING: '待处理', HANDLED: '已处理', CLOSED: '已关闭' }
const mapWarning = (w) => {
  let status = w.status
  if (status === 0) status = 'PENDING'
  else if (status === 1) status = 'HANDLED'
  else if (status === 2) status = 'CLOSED'
  if (!['PENDING', 'HANDLED', 'CLOSED'].includes(status)) status = 'PENDING'
  const sev = status === 'PENDING' ? 'danger' : status === 'HANDLED' ? 'success' : 'muted'
  const title = w.title || (w.warningType ? String(w.warningType) : '') || '健康预警提醒'
  return { ...w, status, statusText: WARNING_STATUS[status], sev, title, time: formatDateTime(w.createTime || w.createdAt) }
}

// ========== 数据加载 ==========
const loadElders = async () => {
  eldersLoading.value = true
  try {
    const res = await familyApi.myElders()
    const list = res.data ? (Array.isArray(res.data) ? res.data : []) : []
    elders.value = list
    if (list.length && !selectedId.value) selectedId.value = list[0].elderInfoId
  } catch (e) {
    console.error('加载老人列表失败', e)
    elders.value = []
  } finally {
    eldersLoading.value = false
  }
}

const loadElderData = async (elderId) => {
  if (!elderId) return
  elderLoading.value = true
  try {
    await Promise.all([
      loadHealth(elderId),
      loadAdvice(elderId),
      loadWarnings(elderId),
      loadReminders(elderId)
    ])
  } finally {
    elderLoading.value = false
  }
}

const loadHealth = async (elderId) => {
  try {
    const res = await familyApi.healthList(elderId, { page: 1, pageSize: 50 })
    const records = res.data?.records || (Array.isArray(res.data) ? res.data : [])
    const sorted = [...records].sort((a, b) => new Date(b.recordDate || b.createTime || 0) - new Date(a.recordDate || a.createTime || 0))
    const latest = (p) => sorted.find(p)
    const vals = {
      bloodPressure: (() => { const r = latest(x => x.bloodPressureHigh != null && x.bloodPressureLow != null); return r ? `${r.bloodPressureHigh}/${r.bloodPressureLow}` : '--' })(),
      bloodSugar: latest(x => x.bloodSugar != null)?.bloodSugar ?? '--',
      heartRate: latest(x => x.heartRate != null)?.heartRate ?? '--',
      weight: latest(x => x.weight != null)?.weight ?? '--'
    }
    for (const h of healthItems.value) {
      h.value = vals[h.key]
      h.status = evalStatus(h.key, h.value)
    }
  } catch (e) { console.error('健康数据加载失败', e) }
}

const loadAdvice = async (elderId) => {
  try {
    const res = await familyApi.healthAdvice(elderId)
    const raw = res.data
    adviceText.value = (raw && (raw.advice ?? raw.content)) ? (raw.advice ?? raw.content) : (typeof raw === 'string' ? raw : '')
  } catch (e) { console.error('健康建议加载失败', e) }
}

const loadWarnings = async (elderId) => {
  try {
    const res = await familyApi.warningList(elderId, { page: 1, pageSize: 10 })
    const records = res.data?.records || (Array.isArray(res.data) ? res.data : [])
    warnings.value = records.map(mapWarning)
  } catch (e) { console.error('预警加载失败', e); warnings.value = [] }
}

const loadReminders = async (elderId) => {
  try {
    const res = await familyApi.reminderList(elderId)
    const records = res.data?.records || (Array.isArray(res.data) ? res.data : [])
    const mapped = records.map(mapReminder)
    const todayPending = mapped.filter(r => isToday(r.remindTime || r.time) && !r.done)
    todayCare.value = todayPending.length ? todayPending : mapped.filter(r => isToday(r.remindTime || r.time)).slice(0, 4)
  } catch (e) { console.error('提醒加载失败', e); todayCare.value = [] }
}

const loadNews = async () => {
  newsLoading.value = true
  try {
    const res = await newsApi.list({ page: 1, pageSize: 6 })
    const records = res.data?.records || (Array.isArray(res.data) ? res.data : [])
    const types = { HEALTH: '健康养生', POLICY: '政策解读', LIFE: '生活百科', ACTIVITY: '社区活动' }
    newsList.value = records.map(n => ({ ...n, category: n.category || types[n.newsType] || n.newsType || '资讯' }))
  } catch (e) { console.error('资讯加载失败', e) }
  finally { newsLoading.value = false }
}

const selectElder = (e) => {
  if (selectedId.value === e.elderInfoId) return
  selectedId.value = e.elderInfoId
}

watch(selectedId, (id) => { if (id) loadElderData(id) })

const coverStyle = (n) => {
  const covers = ['/images/news/elderly-exercise.jpg', '/images/news/elderly-park-walk.jpg', '/images/news/elderly-community-fitness.jpg']
  return { backgroundImage: `url(${n.coverImage || covers[Math.abs(Number(n.id) || 0) % covers.length]})` }
}

onMounted(() => {
  fetchWeather()
  loadNews()
  loadElders().then(() => { if (selectedId.value) loadElderData(selectedId.value) })
})
</script>

<style scoped>
.dash { max-width: 1180px; margin: 0 auto; padding: 16px 20px 32px; }

/* ① 顶部欢迎栏 */
.dash-top { display: flex; align-items: flex-start; justify-content: space-between; gap: 16px; padding: 8px 4px 18px; flex-wrap: wrap; }
.greet-hi { font-size: 24px; font-weight: 800; color: #0f172a; display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.guard-badge { display: inline-flex; align-items: center; gap: 5px; font-size: 13px; font-weight: 600; color: #059669; background: #ecfdf5; padding: 3px 10px; border-radius: 999px; }
.guard-dot { width: 7px; height: 7px; border-radius: 50%; background: #10b981; box-shadow: 0 0 0 0 rgba(16,185,129,.5); animation: pulse 1.8s infinite; }
@keyframes pulse { 0%{box-shadow:0 0 0 0 rgba(16,185,129,.5)} 70%{box-shadow:0 0 0 7px rgba(16,185,129,0)} 100%{box-shadow:0 0 0 0 rgba(16,185,129,0)} }
.greet-sub { margin-top: 6px; font-size: 14px; color: #64748b; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.greet-sub .sep { opacity: .4; }
.greet-sub b { color: #2563eb; font-size: 16px; }
.greet-sub .tip { color: #94a3b8; }
.weather-chip { display: inline-flex; align-items: center; gap: 6px; background: #fff; border: 1px solid #e2e8f0; padding: 8px 14px; border-radius: 999px; cursor: pointer; font-size: 14px; color: #334155; box-shadow: 0 1px 3px rgba(15,23,42,.06); transition: transform .15s, box-shadow .15s; }
.weather-chip:hover { transform: translateY(-1px); box-shadow: 0 6px 16px rgba(37,99,235,.15); }
.weather-chip .w-city { font-weight: 600; }
.weather-chip .w-temp { color: #64748b; }
.weather-chip.loading { cursor: default; }

/* 未绑定空态 */
.empty-elder { background: #fff; border-radius: 20px; padding: 48px 20px; text-align: center; box-shadow: 0 4px 16px rgba(15,23,42,.06); margin-bottom: 18px; }
.empty-ico { font-size: 48px; }
.empty-title { font-size: 18px; font-weight: 700; color: #0f172a; margin: 12px 0 6px; }
.empty-desc { font-size: 14px; color: #94a3b8; margin-bottom: 18px; }

/* ② 我的老人核心卡 */
.elder-card { background: linear-gradient(120deg, #2563eb 0%, #1d4ed8 100%); border-radius: 22px; padding: 22px; color: #fff; box-shadow: 0 12px 30px rgba(37,99,235,.32); margin-bottom: 18px; }
.elder-head { display: flex; align-items: center; gap: 14px; }
.elder-avatar { background: rgba(255,255,255,.22) !important; color: #fff !important; font-size: 22px !important; font-weight: 700; flex-shrink: 0; }
.elder-meta { flex: 1; min-width: 0; }
.elder-name { font-size: 22px; font-weight: 800; display: flex; align-items: center; gap: 8px; }
.rel-tag { background: rgba(255,255,255,.2) !important; border-color: transparent !important; color: #fff !important; font-weight: 600; }
.elder-sub { font-size: 14px; opacity: .9; margin-top: 4px; }
.detail-btn { background: rgba(255,255,255,.95) !important; color: #1d4ed8 !important; border: none !important; font-weight: 700 !important; font-size: 14px !important; padding: 8px 16px !important; }
.detail-btn .ml4 { margin-left: 4px; }
.elder-stats { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 20px; }
.stat { background: rgba(255,255,255,.14); border-radius: 14px; padding: 14px; text-align: center; cursor: pointer; transition: background .15s, transform .15s; }
.stat:hover { background: rgba(255,255,255,.24); transform: translateY(-2px); }
.stat-v { font-size: 28px; font-weight: 800; line-height: 1; }
.stat-v small { font-size: 15px; font-weight: 600; opacity: .8; }
.stat-v.zero { color: #6ee7b7; }
.stat-v.danger { color: #fca5a5; }
.stat-l { font-size: 13px; opacity: .9; margin-top: 6px; }
.elder-tabs { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 16px; }
.elder-tab { font-size: 13px; padding: 6px 14px; border-radius: 999px; background: rgba(255,255,255,.16); color: #fff; cursor: pointer; transition: background .15s; }
.elder-tab:hover { background: rgba(255,255,255,.3); }
.elder-tab.active { background: #fff; color: #1d4ed8; font-weight: 700; }

/* 通用区块 */
.block { background: #fff; border-radius: 20px; padding: 18px; margin-bottom: 18px; box-shadow: 0 4px 16px rgba(15,23,42,.05); }
.block-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.block-title { font-size: 18px; font-weight: 800; color: #0f172a; display: flex; align-items: center; gap: 8px; }
.block-title .bt-ic { color: #2563eb; }
.block-title .bt-ic.warn { color: #dc2626; }
.block-more { font-size: 14px; color: #2563eb; cursor: pointer; font-weight: 600; }
.block-more:hover { text-decoration: underline; }
.mini-loading { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #94a3b8; padding: 18px 0; }
.empty-tip { font-size: 14px; color: #94a3b8; text-align: center; padding: 18px 0; }

/* ③ 健康四宫格 */
.health-grid4 { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.h-tile { background: #f8fafc; border: 1px solid #f1f5f9; border-radius: 16px; padding: 16px; cursor: pointer; transition: transform .18s, box-shadow .18s, border-color .18s; }
.h-tile:hover { transform: translateY(-3px); box-shadow: 0 10px 22px rgba(37,99,235,.12); border-color: #dbeafe; }
.h-tile.warn { background: #fff7ed; border-color: #fed7aa; }
.h-tile.warn .h-ic { color: #ea580c; }
.h-tile.normal .h-ic { color: #059669; }
.h-ic { color: #2563eb; display: inline-flex; }
.h-v { font-size: 24px; font-weight: 800; color: #0f172a; margin: 8px 0 2px; }
.h-v small { font-size: 12px; font-weight: 500; color: #94a3b8; margin-left: 3px; }
.h-l { font-size: 14px; color: #64748b; }
.h-flag { display: inline-block; margin-top: 8px; font-size: 12px; font-weight: 600; padding: 2px 10px; border-radius: 999px; background: #e2e8f0; color: #64748b; }
.h-flag.normal { background: #dcfce7; color: #059669; }
.h-flag.warn { background: #ffedd5; color: #ea580c; }

/* ④ LLM 渐变卡 */
.llm-card { position: relative; border-radius: 20px; padding: 22px; margin-bottom: 18px; cursor: pointer; color: #fff; overflow: hidden; background: linear-gradient(120deg, #6366f1 0%, #2563eb 100%); box-shadow: 0 14px 32px rgba(99,102,241,.32); transition: transform .18s, box-shadow .18s; }
.llm-card:hover { transform: translateY(-3px); box-shadow: 0 20px 40px rgba(99,102,241,.4); }
.llm-card::after { content: ''; position: absolute; right: -40px; top: -40px; width: 160px; height: 160px; background: radial-gradient(circle, rgba(255,255,255,.22), transparent 70%); border-radius: 50%; }
.llm-top { display: flex; align-items: center; justify-content: space-between; }
.llm-badge { display: inline-flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 700; background: rgba(255,255,255,.2); padding: 5px 12px; border-radius: 999px; }
.llm-more { font-size: 13px; opacity: .9; font-weight: 600; }
.llm-title { font-size: 20px; font-weight: 800; margin: 14px 0 10px; }
.llm-body { font-size: 14px; line-height: 1.7; opacity: .95; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; max-height: 4.8em; }

/* ⑤ 双列 */
.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; margin-bottom: 18px; }
.warn-list, .care-list { display: flex; flex-direction: column; gap: 10px; }
.warn-row, .care-row { display: flex; align-items: center; gap: 12px; padding: 12px 14px; background: #f8fafc; border-radius: 14px; cursor: pointer; transition: background .15s; }
.warn-row:hover, .care-row:hover { background: #eef2f7; }
.warn-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; background: #cbd5e1; }
.warn-row.danger .warn-dot { background: #ef4444; box-shadow: 0 0 0 4px rgba(239,68,68,.15); }
.warn-row.success .warn-dot { background: #10b981; }
.warn-row.muted .warn-dot { background: #94a3b8; }
.warn-main { flex: 1; min-width: 0; }
.warn-title { font-size: 15px; font-weight: 600; color: #0f172a; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.warn-time { font-size: 12px; color: #94a3b8; margin-top: 2px; }
.warn-tag { font-size: 12px; font-weight: 700; padding: 3px 10px; border-radius: 999px; flex-shrink: 0; }
.warn-tag.danger { background: #fee2e2; color: #dc2626; }
.warn-tag.success { background: #dcfce7; color: #059669; }
.warn-tag.muted { background: #e2e8f0; color: #64748b; }
.care-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.care-main { flex: 1; min-width: 0; }
.care-title { font-size: 15px; font-weight: 600; color: #0f172a; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.care-time { font-size: 12px; color: #94a3b8; margin-top: 2px; }
.care-row.done { opacity: .5; }
.care-row.done .care-title { text-decoration: line-through; }
.care-type { font-size: 12px; font-weight: 600; padding: 3px 10px; border-radius: 999px; border: 1px solid; flex-shrink: 0; }

/* ⑥ 快捷入口 */
.quick-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 18px; }
.quick { background: #fff; border-radius: 18px; padding: 20px 12px; text-align: center; cursor: pointer; box-shadow: 0 4px 16px rgba(15,23,42,.05); transition: transform .18s, box-shadow .18s; }
.quick:hover { transform: translateY(-4px); box-shadow: 0 14px 28px rgba(15,23,42,.12); }
.quick-ic { width: 52px; height: 52px; border-radius: 16px; display: inline-flex; align-items: center; justify-content: center; margin-bottom: 10px; }
.quick-label { font-size: 15px; font-weight: 700; color: #334155; }

/* ⑦ 资讯 */
.news-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
.news-item { cursor: pointer; }
.news-cover { height: 120px; border-radius: 14px; background-size: cover; background-position: center; background-color: #e2e8f0; position: relative; transition: transform .18s; }
.news-item:hover .news-cover { transform: scale(1.03); }
.news-cat { position: absolute; left: 8px; top: 8px; background: rgba(15,23,42,.55); color: #fff; font-size: 12px; padding: 2px 10px; border-radius: 999px; }
.news-title { font-size: 15px; font-weight: 600; color: #0f172a; line-height: 1.5; margin-top: 8px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; min-height: 2.8em; }

.dash-bottom-space { height: 12px; }

/* 响应式 */
@media (max-width: 900px) {
  .health-grid4 { grid-template-columns: repeat(2, 1fr); }
  .two-col { grid-template-columns: 1fr; }
  .news-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 560px) {
  .news-grid, .quick-row { grid-template-columns: repeat(2, 1fr); }
  .elder-stats { grid-template-columns: repeat(3, 1fr); }
}
</style>

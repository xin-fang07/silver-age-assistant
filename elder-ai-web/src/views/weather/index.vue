<!--
  ============================================================
  weather/index.vue - 天气查询页
  银发智能生活助手 - 实时查询全国各地及全球城市天气
  适老化设计：大字体、卡片布局、城市快捷选择、7日预报
  功能：城市搜索、当前天气大卡片、7日预报、语音朗读
  ============================================================
-->
<template>
  <div class="weather-page">
    <!-- ========================================================== -->
    <!-- 1. 页面标题 -->
    <!-- ========================================================== -->
    <div class="page-header">
      <h2 class="page-title">
        <svg viewBox="0 0 24 24" width="32" height="32" fill="url(#weatherGrad)" style="vertical-align:middle;margin-right:10px">
          <defs>
            <linearGradient id="weatherGrad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#4a90d9" />
              <stop offset="100%" style="stop-color:#357abd" />
            </linearGradient>
          </defs>
          <path d="M6 19a4 4 0 0 1 0-8 5 5 0 0 1 9.6-1.5A3.5 3.5 0 0 1 18 19H6z"/>
        </svg>
        天气查询
      </h2>
      <p class="page-subtitle">实时查询全国各地及全球城市的天气情况</p>
    </div>

    <!-- ========================================================== -->
    <!-- 2. 城市搜索 + 常用城市快捷选择 -->
    <!-- ========================================================== -->
    <div class="search-section">
      <div class="search-box">
        <el-input
          v-model="searchCity"
          size="large"
          placeholder="请输入城市名称，如：北京、上海、纽约、东京"
          class="search-input"
          :disabled="loading"
          clearable
          @keyup.enter="doSearch"
        >
          <template #prefix>
            <el-icon :size="20"><Search /></el-icon>
          </template>
        </el-input>
        <el-button
          type="primary"
          size="large"
          class="search-btn"
          :loading="loading"
          @click="doSearch"
        >
          <el-icon><Search /></el-icon>
          查询天气
        </el-button>
      </div>

      <!-- 常用城市快捷标签 -->
      <div class="hot-cities">
        <span class="hot-label">常用城市：</span>
        <el-tag
          v-for="c in hotCities"
          :key="c"
          class="hot-tag"
          :class="{ active: currentCity === c }"
          size="large"
          @click="quickSearch(c)"
        >{{ c }}</el-tag>
      </div>
    </div>

    <!-- ========================================================== -->
    <!-- 3. 加载中 -->
    <!-- ========================================================== -->
    <SkeletonLoader v-if="loading" type="card" :rows="4" />

    <!-- ========================================================== -->
    <!-- 4. 查询失败 -->
    <!-- ========================================================== -->
    <div v-else-if="errorMsg" class="state-box state-error">
      <svg viewBox="0 0 24 24" width="48" height="48" fill="#e85d75">
        <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
      </svg>
      <p class="error-text">{{ errorMsg }}</p>
      <el-button size="large" @click="doSearch">重新查询</el-button>
    </div>

    <!-- ========================================================== -->
    <!-- 5. 天气结果 -->
    <!-- ========================================================== -->
    <template v-else-if="weatherData">
      <!-- 当前天气大卡片 -->
      <div class="current-card">
        <div class="current-left">
          <div class="current-icon" v-html="currentIconSvg"></div>
          <div class="current-temp">{{ formatTemp(weatherData.temperature) }}<span class="temp-unit">°C</span></div>
        </div>
        <div class="current-right">
          <div class="current-city">
            {{ weatherData.city }}
            <span v-if="weatherData.country" class="current-country">{{ weatherData.country }}</span>
          </div>
          <div class="current-text">{{ weatherData.weatherText }}</div>
          <div class="current-meta">
            <div class="meta-item" v-if="weatherData.apparentTemperature != null">
              <span class="meta-label">体感温度</span>
              <span class="meta-value">{{ formatTemp(weatherData.apparentTemperature) }}°C</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">相对湿度</span>
              <span class="meta-value">{{ weatherData.humidity ?? '--' }}%</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">风速</span>
              <span class="meta-value">{{ formatWind(weatherData.windSpeed) }} km/h</span>
            </div>
          </div>
          <div class="current-update">
            数据更新于 {{ weatherData.updateTime }} · 来源 {{ weatherData.dataSource || 'Open-Meteo' }}
          </div>
        </div>
        <!-- 朗读按钮 -->
        <button class="read-btn" @click="readCurrent" title="朗读天气">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z"/>
          </svg>
          朗读
        </button>
      </div>

      <!-- 7 日预报 -->
      <div class="forecast-section">
        <h3 class="section-title">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="#4a90d9" style="vertical-align:middle;margin-right:6px"><path d="M3 3v18h18v-2H5V3H3zm4 10l3-4 3 6 3-8 4 10h2v-2h-1.2L18 10l-3 8-3-6-3 4H7z"/></svg>
          未来 7 天预报
        </h3>

        <div v-if="forecastList.length" class="forecast-list">
          <div
            v-for="(day, i) in forecastList"
            :key="day.date"
            class="forecast-item"
            :class="{ today: i === 0 }"
          >
            <div class="fc-weekday">{{ i === 0 ? '今天' : (day.weekday || '') }}</div>
            <div class="fc-date">{{ formatShortDate(day.date) }}</div>
            <div class="fc-icon" v-html="iconSvg(day.iconCode)"></div>
            <div class="fc-text">{{ day.weatherText }}</div>
            <div class="fc-temp">
              <span class="fc-max">{{ formatTemp(day.maxTemp) }}°</span>
              <span class="fc-min">{{ formatTemp(day.minTemp) }}°</span>
            </div>
          </div>
        </div>

        <div v-else class="state-box">
          <span>暂无预报数据</span>
        </div>
      </div>
    </template>

    <!-- ========================================================== -->
    <!-- 6. 初始空状态（未查询） -->
    <!-- ========================================================== -->
    <div v-else class="state-box state-empty">
      <svg viewBox="0 0 24 24" width="56" height="56" fill="#cbd5e1">
        <path d="M6 19a4 4 0 0 1 0-8 5 5 0 0 1 9.6-1.5A3.5 3.5 0 0 1 18 19H6z"/>
      </svg>
      <p>请输入城市名称，查询实时天气与 7 天预报</p>
    </div>
  </div>
</template>

<script setup>
// ============================================================
// 天气查询页逻辑 - Composition API <script setup>
// ============================================================
import { ref, computed } from 'vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import { ElMessage } from 'element-plus'
import { weatherApi } from '@/api/index'
import { useVoiceKing } from '@/composables/useVoiceKing'

const { speak } = useVoiceKing()

// 搜索框
const searchCity = ref('')
const currentCity = ref('')
const pendingCity = ref('')
const loading = ref(false)
const errorMsg = ref('')
const weatherData = ref(null)

// 常用城市
const hotCities = ['北京', '上海', '广州', '深圳', '成都', '杭州', '武汉', '西安', '哈尔滨', '香港', '纽约', '东京', '伦敦', '巴黎']

// 7 日预报
const forecastList = computed(() => (weatherData.value && weatherData.value.dailyForecast) || [])

// ========== 天气图标 SVG ==========
const ICON_MAP = {
  sunny: `<circle cx="24" cy="24" r="10" fill="#ffd43b"/><g stroke="#ffd43b" stroke-width="3" stroke-linecap="round"><line x1="24" y1="4" x2="24" y2="10"/><line x1="24" y1="38" x2="24" y2="44"/><line x1="4" y1="24" x2="10" y2="24"/><line x1="38" y1="24" x2="44" y2="24"/><line x1="9" y1="9" x2="13" y2="13"/><line x1="35" y1="35" x2="39" y2="39"/><line x1="39" y1="9" x2="35" y2="13"/><line x1="13" y1="35" x2="9" y2="39"/></g>`,
  cloudy: `<circle cx="18" cy="22" r="8" fill="#ffd43b"/><path d="M16 34 Q20 26 30 28 Q40 24 40 34 Q40 42 30 42 L22 42 Q12 42 16 34" fill="#e9ecef" stroke="#ced4da" stroke-width="2"/>`,
  overcast: `<path d="M12 38 Q16 26 30 28 Q46 22 46 36 Q46 46 34 46 L20 46 Q8 46 12 38" fill="#dee2e6" stroke="#adb5bd" stroke-width="2"/>`,
  rain: `<path d="M14 32 Q18 24 30 26 Q42 20 42 32 Q42 42 30 42 L20 42 Q10 42 14 32" fill="#e9ecef" stroke="#ced4da" stroke-width="2"/><g stroke="#4dabf7" stroke-width="2.5" stroke-linecap="round"><line x1="20" y1="44" x2="18" y2="50"/><line x1="28" y1="44" x2="26" y2="50"/><line x1="36" y1="44" x2="34" y2="50"/></g>`,
  'rain-shower': `<path d="M14 30 Q18 22 30 24 Q42 18 42 30 Q42 40 30 40 L20 40 Q10 40 14 30" fill="#e9ecef" stroke="#ced4da" stroke-width="2"/><g stroke="#4dabf7" stroke-width="2.5" stroke-linecap="round"><line x1="20" y1="42" x2="17" y2="50"/><line x1="30" y1="42" x2="27" y2="50"/><line x1="40" y1="42" x2="37" y2="50"/></g>`,
  drizzle: `<path d="M14 32 Q18 24 30 26 Q42 20 42 32 Q42 42 30 42 L20 42 Q10 42 14 32" fill="#e9ecef" stroke="#ced4da" stroke-width="2"/><g stroke="#74c0fc" stroke-width="2" stroke-linecap="round"><line x1="20" y1="44" x2="19" y2="49"/><line x1="28" y1="44" x2="27" y2="49"/><line x1="36" y1="44" x2="35" y2="49"/></g>`,
  snow: `<path d="M14 32 Q18 24 30 26 Q42 20 42 32 Q42 42 30 42 L20 42 Q10 42 14 32" fill="#e9ecef" stroke="#ced4da" stroke-width="2"/><g fill="#fff" stroke="#74c0fc" stroke-width="1.5"><circle cx="20" cy="48" r="2"/><circle cx="28" cy="46" r="2"/><circle cx="36" cy="49" r="2"/></g>`,
  thunderstorm: `<path d="M14 30 Q18 22 30 24 Q42 18 42 30 Q42 40 30 40 L20 40 Q10 40 14 30" fill="#e9ecef" stroke="#ced4da" stroke-width="2"/><path d="M28 38 L22 48 L28 48 L24 58 L34 46 L28 46 Z" fill="#ffd43b" stroke="#f59f00" stroke-width="1.5"/>`,
  fog: `<path d="M10 30 Q16 24 30 26 Q44 22 48 30" fill="none" stroke="#ced4da" stroke-width="3" stroke-linecap="round"/><path d="M8 38 Q16 32 32 34 Q44 30 48 38" fill="none" stroke="#adb5bd" stroke-width="3" stroke-linecap="round"/>`,
  'freezing-rain': `<path d="M14 32 Q18 24 30 26 Q42 20 42 32 Q42 42 30 42 L20 42 Q10 42 14 32" fill="#e9ecef" stroke="#ced4da" stroke-width="2"/><g stroke="#74c0fc" stroke-width="2.5" stroke-linecap="round"><line x1="20" y1="44" x2="18" y2="50"/><line x1="28" y1="44" x2="26" y2="50"/><line x1="36" y1="44" x2="34" y2="50"/></g><g fill="#fff" stroke="#74c0fc" stroke-width="1.5"><circle cx="20" cy="52" r="1.5"/><circle cx="28" cy="52" r="1.5"/><circle cx="36" cy="52" r="1.5"/></g>`,
  'snow-shower': `<path d="M14 30 Q18 22 30 24 Q42 18 42 30 Q42 40 30 40 L20 40 Q10 40 14 30" fill="#e9ecef" stroke="#ced4da" stroke-width="2"/><g fill="#fff" stroke="#74c0fc" stroke-width="1.5"><circle cx="20" cy="48" r="2"/><circle cx="28" cy="46" r="2"/><circle cx="36" cy="49" r="2"/><circle cx="24" cy="52" r="2"/><circle cx="32" cy="52" r="2"/></g>`,
  unknown: `<circle cx="24" cy="24" r="10" fill="#ffd43b"/><g stroke="#ffd43b" stroke-width="3" stroke-linecap="round"><line x1="24" y1="4" x2="24" y2="10"/><line x1="24" y1="38" x2="24" y2="44"/><line x1="4" y1="24" x2="10" y2="24"/><line x1="38" y1="24" x2="44" y2="24"/></g>`
}

function iconSvg(code) {
  return ICON_MAP[code] || ICON_MAP.sunny
}

const currentIconSvg = computed(() => iconSvg(weatherData.value && weatherData.value.iconCode))

// ========== 格式化辅助 ==========
function formatTemp(v) {
  if (v == null) return '--'
  // 保留整数，更接近老年人阅读习惯
  return Math.round(Number(v))
}
function formatWind(v) {
  if (v == null) return '--'
  return Math.round(Number(v))
}
function formatShortDate(dateStr) {
  if (!dateStr) return ''
  const parts = String(dateStr).split('-')
  if (parts.length === 3) return `${parts[1]}/${parts[2]}`
  return dateStr
}

// ========== 查询逻辑 ==========
async function doSearch() {
  const city = (searchCity.value || '').trim()
  if (!city) {
    ElMessage.warning('请输入要查询的城市名称')
    return
  }
  await queryWeather(city)
}

async function quickSearch(city) {
  searchCity.value = city
  await queryWeather(city)
}

async function queryWeather(city) {
  loading.value = true
  errorMsg.value = ''
  pendingCity.value = city
  try {
    const res = await weatherApi.currentByCity(city)
    if (res && res.data) {
      // 兜底字段，避免 null 导致页面异常
      weatherData.value = {
        city: res.data.city || city,
        country: res.data.country || '',
        temperature: res.data.temperature,
        apparentTemperature: res.data.apparentTemperature,
        humidity: res.data.humidity,
        windSpeed: res.data.windSpeed,
        weatherText: res.data.weatherText || '未知',
        iconCode: res.data.iconCode || 'unknown',
        updateTime: res.data.updateTime || '',
        dataSource: res.data.dataSource || 'Open-Meteo',
        dailyForecast: res.data.dailyForecast || []
      }
      currentCity.value = city
    } else {
      errorMsg.value = '未获取到天气数据，请稍后重试'
    }
  } catch (e) {
    console.error('天气查询失败：', e)
    const msg = e && e.response && e.response.data && e.response.data.message
    errorMsg.value = msg || '天气查询失败，请检查城市名称或网络连接后重试'
  } finally {
    loading.value = false
  }
}

// ========== 朗读当前天气 ==========
function readCurrent() {
  const d = weatherData.value
  if (!d) return
  const lines = []
  lines.push(`${d.city}当前天气${d.weatherText}`)
  lines.push(`温度 ${formatTemp(d.temperature)} 摄氏度`)
  if (d.apparentTemperature != null) lines.push(`体感温度 ${formatTemp(d.apparentTemperature)} 摄氏度`)
  lines.push(`湿度 ${d.humidity ?? '--'}%，风速 ${formatWind(d.windSpeed)} 公里每小时`)
  speak(lines.join('，') + '。', '天气播报')
}
</script>

<style scoped>
/* ============================================================
 * 天气页样式 - 适老化设计
 * ============================================================ */
.weather-page {
  min-height: 100%;
  padding: 8px 4px 32px 4px;
}

/* ========== 页面标题 ========== */
.page-header {
  margin-bottom: 24px;
}
.page-title {
  font-size: 30px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 6px 0;
  display: flex;
  align-items: center;
}
.page-subtitle {
  font-size: 17px;
  color: #64748b;
  margin: 0 0 0 42px;
}

/* ========== 搜索区 ========== */
.search-section {
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.05);
  padding: 22px 26px;
  margin-bottom: 24px;
}
.search-box {
  display: flex;
  gap: 12px;
  align-items: center;
}
.search-input {
  flex: 1;
}
.search-input :deep(.el-input__inner) {
  font-size: 18px !important;
  height: 52px !important;
}
.search-btn {
  min-height: 52px !important;
  font-size: 18px !important;
  font-weight: 700 !important;
  padding: 0 28px !important;
  border-radius: 12px !important;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

/* 常用城市 */
.hot-cities {
  margin-top: 18px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}
.hot-label {
  font-size: 16px;
  color: #64748b;
  font-weight: 600;
}
.hot-tag {
  font-size: 16px !important;
  padding: 8px 16px !important;
  cursor: pointer;
  border-radius: 20px !important;
  background: #e3f0ff !important;
  border-color: #a8d4ff !important;
  color: #357abd !important;
  font-weight: 500 !important;
  transition: all 0.2s !important;
}
.hot-tag:hover {
  background: #a8d4ff !important;
  transform: translateY(-2px);
}
.hot-tag.active {
  background: linear-gradient(135deg, #4a90d9, #357abd) !important;
  border-color: #357abd !important;
  color: #fff !important;
}

/* ========== 状态盒（加载/错误/空） ========== */
.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 60px 20px;
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.05);
  font-size: 18px;
  color: #64748b;
}
.state-error .error-text {
  color: #e85d75;
  font-weight: 600;
  margin: 0;
  text-align: center;
  max-width: 480px;
  line-height: 1.6;
}
.state-empty {
  color: #94a3b8;
}

/* ========== 当前天气大卡片 ========== */
.current-card {
  display: flex;
  align-items: center;
  gap: 28px;
  background: linear-gradient(135deg, #4a90d9 0%, #357abd 100%);
  border-radius: 20px;
  padding: 32px 36px;
  margin-bottom: 24px;
  color: #fff;
  box-shadow: 0 8px 28px rgba(53, 122, 189, 0.35);
  position: relative;
}
.current-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 160px;
}
.current-icon {
  width: 96px;
  height: 96px;
}
.current-icon :deep(svg) {
  width: 96px;
  height: 96px;
}
.current-temp {
  font-size: 64px;
  font-weight: 800;
  line-height: 1;
  margin-top: 8px;
}
.temp-unit {
  font-size: 28px;
  font-weight: 600;
  margin-left: 4px;
}
.current-right {
  flex: 1;
  min-width: 0;
}
.current-city {
  font-size: 30px;
  font-weight: 700;
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.current-country {
  font-size: 16px;
  font-weight: 500;
  opacity: 0.85;
}
.current-text {
  font-size: 22px;
  font-weight: 600;
  margin: 6px 0 16px;
  opacity: 0.95;
}
.current-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 28px;
}
.meta-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.meta-label {
  font-size: 14px;
  opacity: 0.8;
}
.meta-value {
  font-size: 20px;
  font-weight: 700;
}
.current-update {
  margin-top: 16px;
  font-size: 13px;
  opacity: 0.75;
}
.read-btn {
  position: absolute;
  top: 18px;
  right: 18px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 8px 16px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}
.read-btn:hover {
  background: rgba(255, 255, 255, 0.35);
  transform: scale(1.04);
}

/* ========== 7 日预报 ========== */
.forecast-section {
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.05);
  padding: 22px 26px;
}
.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 18px 0;
}
.forecast-list {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12px;
}
.forecast-item {
  background: #f8fafc;
  border: 2px solid transparent;
  border-radius: 14px;
  padding: 16px 10px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}
.forecast-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
  border-color: #a8d4ff;
}
.forecast-item.today {
  background: linear-gradient(135deg, #e3f0ff, #dbeafe);
  border-color: #4a90d9;
}
.fc-weekday {
  font-size: 17px;
  font-weight: 700;
  color: #1e293b;
}
.fc-date {
  font-size: 13px;
  color: #94a3b8;
}
.fc-icon :deep(svg) {
  width: 44px;
  height: 44px;
}
.fc-text {
  font-size: 14px;
  color: #475569;
  min-height: 20px;
}
.fc-temp {
  display: flex;
  gap: 8px;
  align-items: baseline;
  font-size: 15px;
}
.fc-max {
  font-weight: 700;
  color: #e85d75;
}
.fc-min {
  color: #4a90d9;
}

/* ========== 响应式 ========== */
@media (max-width: 1100px) {
  .forecast-list {
    grid-template-columns: repeat(4, 1fr);
  }
}
@media (max-width: 900px) {
  .current-card {
    flex-direction: column;
    text-align: center;
  }
  .current-right {
    text-align: center;
  }
  .current-city {
    justify-content: center;
  }
  .current-meta {
    justify-content: center;
  }
  .search-box {
    flex-direction: column;
    align-items: stretch;
  }
  .forecast-list {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 560px) {
  .forecast-list {
    grid-template-columns: repeat(2, 1fr);
  }
  .page-title {
    font-size: 24px;
  }
}
</style>

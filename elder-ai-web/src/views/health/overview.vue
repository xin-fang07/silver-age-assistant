<template>
  <div class="overview-page">
    <div class="page-head">
      <h2 class="page-title">健康概况</h2>
      <p class="page-sub">已绑定老人的近期健康指标与异常汇总，点击卡片查看详情或生成 AI 报告。</p>
    </div>

    <!-- 顶部 KPI -->
    <div class="kpi-row" v-loading="loading">
      <el-card class="kpi-card" shadow="never">
        <div class="kpi-num">{{ stats.elderCount }}</div>
        <div class="kpi-label">绑定老人</div>
      </el-card>
      <el-card class="kpi-card warn" shadow="never">
        <div class="kpi-num">{{ stats.warningCount }}</div>
        <div class="kpi-label">待处理预警</div>
      </el-card>
      <el-card class="kpi-card ok" shadow="never">
        <div class="kpi-num">{{ stats.onlineDevices }}</div>
        <div class="kpi-label">在线设备</div>
      </el-card>
      <el-card class="kpi-card" shadow="never">
        <div class="kpi-num">{{ elders.length ? formatTime(stats.lastActive) : '—' }}</div>
        <div class="kpi-label">最近活跃</div>
      </el-card>
    </div>

    <el-empty v-if="!loading && elders.length === 0" description="暂无绑定的老人" />

    <div v-else class="elder-list">
      <el-card
        v-for="e in elders"
        :key="e.elderInfoId"
        class="elder-card"
        shadow="hover"
      >
        <div class="elder-card-body">
          <el-avatar :size="48" class="elder-avatar">{{ (e.realName || '老').slice(0, 1) }}</el-avatar>
          <div class="elder-card-info">
            <div class="elder-name">{{ e.realName || '未命名' }}</div>
            <div class="elder-rel">关系：{{ e.relation || '—' }}</div>
            <div class="elder-metrics" v-if="e.latestHealth">
              <span :class="{ abnormal: isAbnormalBP(e.latestHealth) }">
                血压 <b>{{ bp(e.latestHealth) }}</b>
              </span>
              <span :class="{ abnormal: isAbnormalHR(e.latestHealth) }">
                心率 <b>{{ e.latestHealth.heartRate != null ? e.latestHealth.heartRate : '—' }}</b>
              </span>
              <span v-if="e.latestHealth.bloodSugar != null" :class="{ abnormal: isAbnormalBS(e.latestHealth) }">
                血糖 <b>{{ e.latestHealth.bloodSugar }}</b>
              </span>
            </div>
            <div class="elder-metrics muted" v-else>暂无健康数据</div>
          </div>
          <div class="elder-badges">
            <el-badge :value="e.unhandledWarningCount" :hidden="!e.unhandledWarningCount" type="danger">
              <el-tag size="small" effect="plain">预警</el-tag>
            </el-badge>
            <el-tag size="small" :type="e.deviceOnlineCount > 0 ? 'success' : 'info'" effect="plain">
              设备 {{ e.deviceCount || 0 }}·在线 {{ e.deviceOnlineCount || 0 }}
            </el-tag>
          </div>
        </div>
        <div class="elder-foot" v-if="e.lastActiveTime">最近活跃：{{ formatTime(e.lastActiveTime) }}</div>
        <div class="elder-actions">
          <el-button size="small" @click="goDetail(e.elderInfoId)">查看详情</el-button>
          <el-button size="small" type="primary" @click="goReport(e.elderInfoId)">生成 AI 报告</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { familyApi } from '@/api'

const router = useRouter()
const elders = ref([])
const loading = ref(false)
const stats = reactive({ elderCount: 0, warningCount: 0, onlineDevices: 0, lastActive: null })

function bp(h) {
  return h.bloodPressureHigh != null ? h.bloodPressureHigh + '/' + h.bloodPressureLow : '—'
}
function isAbnormalBP(h) {
  return h.bloodPressureHigh != null && (h.bloodPressureHigh > 140 || (h.bloodPressureLow != null && h.bloodPressureLow > 90))
}
function isAbnormalHR(h) {
  return h.heartRate != null && (h.heartRate > 100 || h.heartRate < 60)
}
function isAbnormalBS(h) {
  return h.bloodSugar != null && h.bloodSugar > 7.0
}
function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 19)
}

async function load() {
  loading.value = true
  try {
    const r = await familyApi.overview()
    const list = r.data || r
    elders.value = Array.isArray(list) ? list : []
    let warnings = 0, devices = 0, last = null
    elders.value.forEach(e => {
      warnings += Number(e.unhandledWarningCount || 0)
      devices += Number(e.deviceOnlineCount || 0)
      if (e.lastActiveTime && (!last || e.lastActiveTime > last)) last = e.lastActiveTime
    })
    stats.elderCount = elders.value.length
    stats.warningCount = warnings
    stats.onlineDevices = devices
    stats.lastActive = last
  } catch (e) {
    ElMessage.error('加载健康概况失败')
  } finally {
    loading.value = false
  }
}

function goDetail(id) { router.push(`/care/elder/${id}`) }
function goReport(id) { router.push(`/care/health-report?elderId=${id}`) }

onMounted(load)
</script>

<style scoped>
.overview-page { padding: 8px 4px; }
.page-head { margin-bottom: 16px; }
.page-title { font-size: 22px; font-weight: 600; margin: 0 0 4px; }
.page-sub { color: #909399; font-size: 13px; margin: 0; }
.kpi-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 14px; margin-bottom: 18px; }
.kpi-card { border-radius: 12px; text-align: center; }
.kpi-num { font-size: 26px; font-weight: 700; color: #303133; }
.kpi-label { font-size: 13px; color: #909399; margin-top: 4px; }
.kpi-card.warn .kpi-num { color: #f56c6c; }
.kpi-card.ok .kpi-num { color: #67c23a; }
.elder-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px; }
.elder-card { transition: transform .15s; }
.elder-card:hover { transform: translateY(-2px); }
.elder-card-body { display: flex; align-items: center; gap: 12px; }
.elder-avatar { background: #409eff; color: #fff; font-size: 18px; }
.elder-card-info { flex: 1; }
.elder-name { font-size: 16px; font-weight: 600; }
.elder-rel { font-size: 13px; color: #909399; margin-top: 2px; }
.elder-metrics { font-size: 12px; color: #606266; margin-top: 4px; display: flex; gap: 10px; flex-wrap: wrap; }
.elder-metrics.muted { color: #c0c4cc; }
.elder-metrics b { color: #303133; font-weight: 600; }
.elder-metrics .abnormal b { color: #f56c6c; }
.elder-badges { display: flex; flex-direction: column; gap: 6px; align-items: flex-end; margin-left: 4px; }
.elder-foot { font-size: 12px; color: #c0c4cc; margin-top: 10px; padding-top: 8px; border-top: 1px solid #f0f0f0; }
.elder-actions { display: flex; gap: 8px; margin-top: 10px; }
</style>

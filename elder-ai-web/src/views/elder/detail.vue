<template>
  <div class="detail-page" v-loading="loading">
    <div class="page-head">
      <el-button text :icon="ArrowLeft" @click="goBack">返回</el-button>
      <h2 class="page-title">{{ detail.realName || '老人详情' }}</h2>
      <el-tag v-if="detail.relation" size="small" type="info">{{ detail.relation }}</el-tag>
      <el-tag v-if="detail.healthStatus != null" size="small" :type="healthStatusTag(detail.healthStatus)" effect="dark">
        {{ healthStatusText(detail.healthStatus) }}
      </el-tag>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :md="9">
        <el-card class="block" shadow="never">
          <h4 class="block-title">基本信息</h4>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="姓名">{{ detail.realName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ genderText(detail.gender) }}</el-descriptions-item>
            <el-descriptions-item label="年龄">{{ detail.age != null ? detail.age + ' 岁' : '—' }}</el-descriptions-item>
            <el-descriptions-item label="生日">{{ detail.birthDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="血型">{{ detail.bloodType || '—' }}</el-descriptions-item>
            <el-descriptions-item label="紧急联系人">{{ detail.emergencyContact || '—' }}</el-descriptions-item>
            <el-descriptions-item label="紧急电话">{{ detail.emergencyPhone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="住址">{{ detail.address || '—' }}</el-descriptions-item>
            <el-descriptions-item label="既往病史">{{ detail.medicalHistory || '无' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="block" shadow="never">
          <h4 class="block-title">关联设备</h4>
          <el-empty v-if="!detail.devices || !detail.devices.length" description="未绑定设备" :image-size="50" />
          <el-table v-else :data="detail.devices" size="small" border>
            <el-table-column prop="deviceName" label="名称" />
            <el-table-column prop="deviceType" label="类型" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '在线' : '离线' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="15">
        <el-card class="block" shadow="never">
          <h4 class="block-title">最新健康指标</h4>
          <div v-if="latest" class="metric-grid">
            <div class="metric" :class="{ ab: bpAbnormal }">
              <div class="m-label">血压</div>
              <div class="m-value">{{ latest.bloodPressureHigh }}/{{ latest.bloodPressureLow }}<span>mmHg</span></div>
            </div>
            <div class="metric" :class="{ ab: hrAbnormal }">
              <div class="m-label">心率</div>
              <div class="m-value">{{ latest.heartRate }}<span>次/分</span></div>
            </div>
            <div class="metric" :class="{ ab: bsAbnormal }">
              <div class="m-label">血糖</div>
              <div class="m-value">{{ latest.bloodSugar }}<span>mmol/L</span></div>
            </div>
            <div class="metric">
              <div class="m-label">血氧</div>
              <div class="m-value">{{ latest.bloodOxygen != null ? latest.bloodOxygen : '—' }}<span>%</span></div>
            </div>
          </div>
          <el-empty v-else description="暂无健康数据" :image-size="60" />
        </el-card>

        <el-card class="block" shadow="never">
          <h4 class="block-title">健康趋势</h4>
          <div ref="chartRef" class="trend-chart"></div>
          <el-empty v-if="!healthRows.length" description="暂无趋势数据" :image-size="50" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card class="block" shadow="never">
          <h4 class="block-title">近期预警（{{ warningRows.length }}）</h4>
          <el-empty v-if="!warningRows.length" description="无预警" :image-size="50" />
          <el-table v-else :data="warningRows" size="small" border>
            <el-table-column label="类型" width="100">
              <template #default="{ row }">{{ warningTypeText(row.warningType) }}</template>
            </el-table-column>
            <el-table-column label="等级" width="70">
              <template #default="{ row }">
                <el-tag :type="row.warningLevel >= 3 ? 'danger' : row.warningLevel === 2 ? 'warning' : 'info'" size="small">
                  {{ row.warningLevel }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="warningContent" label="描述" show-overflow-tooltip />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">{{ warningStatusText(row.status) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="12">
        <el-card class="block" shadow="never">
          <h4 class="block-title">提醒事项（{{ reminderRows.length }}）</h4>
          <el-empty v-if="!reminderRows.length" description="无提醒" :image-size="50" />
          <el-table v-else :data="reminderRows" size="small" border>
            <el-table-column prop="title" label="标题" show-overflow-tooltip />
            <el-table-column prop="content" label="内容" show-overflow-tooltip />
            <el-table-column label="下次时间" width="160">
              <template #default="{ row }">{{ formatTime(row.nextTime || row.remindTime || row.createTime) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { familyApi } from '@/api'

const route = useRoute()
const router = useRouter()
const elderId = route.params.id

const loading = ref(false)
const detail = ref({})
const healthRows = ref([])
const warningRows = ref([])
const reminderRows = ref([])
const chartRef = ref(null)
let chartInstance = null

const latest = computed(() => detail.value && detail.value.healthRecord ? detail.value.healthRecord : null)
const bpAbnormal = computed(() => {
  const v = latest.value
  return v && (v.bloodPressureHigh >= 140 || v.bloodPressureHigh <= 90 || v.bloodPressureLow >= 90 || v.bloodPressureLow <= 60)
})
const hrAbnormal = computed(() => {
  const v = latest.value
  return v && (v.heartRate >= 100 || v.heartRate <= 60)
})
const bsAbnormal = computed(() => {
  const v = latest.value
  return v && v.bloodSugar != null && (v.bloodSugar >= 7.0 || v.bloodSugar <= 3.9)
})

function genderText(g) {
  if (g === 1) return '男'
  if (g === 0) return '女'
  return '未填'
}
function healthStatusText(s) {
  return ['健康', '慢性病', '需照护', '其他疾病'][s] || '未知'
}
function healthStatusTag(s) {
  return ['success', 'warning', 'danger', 'info'][s] || 'info'
}
function warningTypeText(t) {
  return { BLOOD_PRESSURE: '血压异常', BLOOD_SUGAR: '血糖异常', HEART_RATE: '心率异常' }[t] || t || '—'
}
function warningStatusText(s) {
  return ['待处理', '已知晓', '已处理'][s] || '—'
}
function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 19)
}
function goBack() {
  router.push('/care/elders')
}

function renderChart() {
  if (!chartRef.value) return
  if (!chartInstance) chartInstance = echarts.init(chartRef.value)
  const rows = [...healthRows.value].sort((a, b) =>
    new Date(a.measuredAt || a.createTime || 0) - new Date(b.measuredAt || b.createTime || 0))
  const x = rows.map(r => String(r.measuredAt || r.createTime || '').slice(0, 16))
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['收缩压', '舒张压', '心率', '血糖'] },
    grid: { left: 44, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: x },
    yAxis: { type: 'value' },
    series: [
      { name: '收缩压', type: 'line', data: rows.map(r => r.bloodPressureHigh) },
      { name: '舒张压', type: 'line', data: rows.map(r => r.bloodPressureLow) },
      { name: '心率', type: 'line', data: rows.map(r => r.heartRate) },
      { name: '血糖', type: 'line', data: rows.map(r => r.bloodSugar) }
    ]
  })
}

async function load() {
  loading.value = true
  try {
    const [d, h, w, r] = await Promise.all([
      familyApi.elderDetail(elderId),
      familyApi.healthList(elderId),
      familyApi.warningList(elderId),
      familyApi.reminderList(elderId)
    ])
    detail.value = d.data || d
    const hl = h.data
    healthRows.value = Array.isArray(hl) ? hl : (hl && hl.records ? hl.records : [])
    const wl = w.data
    warningRows.value = Array.isArray(wl) ? wl : (wl && wl.records ? wl.records : [])
    const rl = r.data
    reminderRows.value = Array.isArray(rl) ? rl : (rl && rl.records ? rl.records : [])
    await nextTick()
    renderChart()
  } catch (e) {
    ElMessage.error('加载老人详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
onBeforeUnmount(() => { if (chartInstance) chartInstance.dispose() })
</script>

<style scoped>
.detail-page { padding: 8px 4px; }
.page-head { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.page-title { font-size: 22px; font-weight: 600; margin: 0; }
.block { margin-bottom: 16px; border-radius: 12px; }
.block-title { margin: 0 0 12px; font-size: 15px; font-weight: 600; color: #303133; border-left: 3px solid #409eff; padding-left: 8px; }
.metric-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.metric { background: #f7f9fc; border-radius: 10px; padding: 12px; }
.metric.ab { background: #fef0f0; }
.m-label { font-size: 13px; color: #909399; }
.m-value { font-size: 20px; font-weight: 700; color: #303133; margin-top: 4px; }
.metric.ab .m-value { color: #f56c6c; }
.m-value span { font-size: 12px; font-weight: 400; color: #909399; margin-left: 4px; }
.trend-chart { width: 100%; height: 260px; }
</style>

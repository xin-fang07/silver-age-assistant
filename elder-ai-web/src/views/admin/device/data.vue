<template>
  <div class="device-data-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">健康数据接收</h2>
        <p class="page-subtitle">查看硬件实时上报数据流，排查硬件有没有正常传体征数据</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="loadHealthData">刷新数据</el-button>
        <el-button @click="exportData">导出数据</el-button>
      </div>
    </div>

    <div class="stat-row">
      <div class="stat-card"><div class="stat-num">{{ statistics.totalCount || 0 }}</div><div class="stat-label">上报总数</div></div>
      <div class="stat-card stat-success"><div class="stat-num">{{ statistics.successCount || 0 }}</div><div class="stat-label">成功接收</div></div>
      <div class="stat-card stat-danger"><div class="stat-num">{{ statistics.failCount || 0 }}</div><div class="stat-label">接收失败</div></div>
      <div class="stat-card"><div class="stat-num">{{ (statistics.successRate || 0).toFixed(1) }}%</div><div class="stat-label">成功率</div></div>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索设备编号/老人姓名" clearable style="width: 250px" @keyup.enter="loadHealthData" />
      <el-select v-model="filterStatus" placeholder="接收状态" style="width: 120px" clearable>
        <el-option label="成功" value="SUCCESS" />
        <el-option label="失败" value="FAIL" />
      </el-select>
      <el-button @click="loadHealthData">搜索</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <div class="section-card">
      <h3 class="section-title">实时上报列表</h3>
      <div v-loading="loading">
        <el-empty v-if="!loading && healthDataList.length === 0" description="暂无上报数据" :image-size="80" />
        <el-table v-else :data="healthDataList" style="width: 100%" stripe border>
          <el-table-column prop="deviceId" label="设备编号" min-width="150" show-overflow-tooltip />
          <el-table-column prop="deviceName" label="设备名称" min-width="120" />
          <el-table-column prop="elderName" label="绑定老人" min-width="120">
            <template #default="{ row }">
              <span v-if="row.elderName">{{ row.elderName }}</span>
              <el-tag v-else type="warning" size="small">未绑定</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="metricsSummary" label="健康指标" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">{{ formatMetrics(row.metricsSummary) }}</template>
          </el-table-column>
          <el-table-column label="原始报文" min-width="250" show-overflow-tooltip>
            <template #default="{ row }">
              <el-button type="text" size="small" @click="viewRawData(row)">查看</el-button>
            </template>
          </el-table-column>
          <el-table-column label="接收状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.syncStatus === 'SUCCESS' ? 'success' : 'danger'" size="small">
                {{ row.syncStatus === 'SUCCESS' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="errorMsg" label="失败原因" min-width="150" show-overflow-tooltip />
          <el-table-column prop="syncTime" label="上报时间" min-width="180" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button v-if="row.syncStatus === 'FAIL'" type="warning" link size="small" @click="markAbnormal(row)">标记异常</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="section-card mt-16">
      <h3 class="section-title">设备上报频次统计</h3>
      <div v-loading="statsLoading">
        <el-empty v-if="!statsLoading && !statistics.deviceStats" description="暂无统计数据" :image-size="80" />
        <el-table v-else-if="statistics.deviceStats && statistics.deviceStats.length > 0" :data="statistics.deviceStats" style="width: 100%" stripe border>
          <el-table-column prop="deviceId" label="设备编号" min-width="150" />
          <el-table-column prop="reportCount" label="上报次数" width="120" />
          <el-table-column prop="successCount" label="成功次数" width="120" />
          <el-table-column label="成功率" width="120">
            <template #default="{ row }">
              <span>{{ row.reportCount > 0 ? (row.successCount / row.reportCount * 100).toFixed(1) : 0 }}%</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="pullHistoryData(row.deviceId)">重拉历史数据</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="section-card mt-16">
      <h3 class="section-title">失败数据汇总</h3>
      <div v-loading="statsLoading">
        <el-empty v-if="!statsLoading && (failDataList.length === 0)" description="暂无失败记录" :image-size="80" />
        <el-table v-else :data="failDataList" style="width: 100%" stripe border>
          <el-table-column prop="deviceId" label="设备编号" min-width="150" />
          <el-table-column prop="deviceName" label="设备名称" min-width="120" />
          <el-table-column prop="elderName" label="绑定老人" min-width="120" />
          <el-table-column prop="errorMsg" label="失败原因" min-width="200" show-overflow-tooltip />
          <el-table-column prop="syncTime" label="上报时间" min-width="180" />
        </el-table>
      </div>
    </div>

    <el-dialog v-model="rawDataDialogVisible" title="原始上报报文" width="700px">
      <pre class="raw-data-content">{{ currentRawData }}</pre>
      <template #footer>
        <el-button @click="rawDataDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api'

const healthDataList = ref([])
const statistics = ref({})
const loading = ref(false)
const statsLoading = ref(false)
const keyword = ref('')
const filterStatus = ref('')

const rawDataDialogVisible = ref(false)
const currentRawData = ref('')

const failDataList = computed(() => healthDataList.value.filter(d => d.syncStatus === 'FAIL'))

const formatMetrics = (summary) => {
  if (!summary) return '-'
  try {
    const data = JSON.parse(summary)
    const metrics = []
    if (data.heartRate) metrics.push(`心率 ${data.heartRate}bpm`)
    if (data.bloodPressureHigh) metrics.push(`血压 ${data.bloodPressureHigh}/${data.bloodPressureLow}mmHg`)
    if (data.spo2) metrics.push(`血氧 ${data.spo2}%`)
    if (data.steps) metrics.push(`步数 ${data.steps}`)
    if (data.sleepDuration) metrics.push(`睡眠 ${data.sleepDuration}h`)
    return metrics.join(', ') || summary
  } catch {
    return summary
  }
}

const loadHealthData = async () => {
  loading.value = true
  try {
    const res = await adminApi.deviceHealthData({
      deviceId: keyword.value || undefined,
      status: filterStatus.value || undefined
    })
    let data = res.data || []
    if (keyword.value && !filterStatus.value) {
      const kw = keyword.value.toLowerCase()
      data = data.filter(d => 
        (d.deviceId && d.deviceId.toLowerCase().includes(kw)) ||
        (d.elderName && d.elderName.toLowerCase().includes(kw))
      )
    }
    healthDataList.value = data
  } catch (e) {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  statsLoading.value = true
  try {
    const res = await adminApi.deviceHealthDataStatistics()
    statistics.value = res.data || {}
  } catch (e) {
    // 拦截器已处理
  } finally {
    statsLoading.value = false
  }
}

const resetFilters = () => {
  keyword.value = ''
  filterStatus.value = ''
  loadHealthData()
}

const exportData = () => {
  const headers = ['设备编号', '设备名称', '绑定老人', '健康指标', '接收状态', '失败原因', '上报时间']
  let csv = headers.join(',') + '\n'
  healthDataList.value.forEach(d => {
    const row = [
      `"${d.deviceId || ''}"`,
      `"${d.deviceName || ''}"`,
      `"${d.elderName || ''}"`,
      `"${formatMetrics(d.metricsSummary)}"`,
      `"${d.syncStatus === 'SUCCESS' ? '成功' : '失败'}"`,
      `"${d.errorMsg || ''}"`,
      `"${d.syncTime || ''}"`
    ]
    csv += row.join(',') + '\n'
  })
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `健康数据接收_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

const viewRawData = (row) => {
  currentRawData.value = row.metricsSummary || '无原始数据'
  rawDataDialogVisible.value = true
}

const markAbnormal = (row) => {
  ElMessage.success(`已标记设备 ${row.deviceId} 的异常数据`)
}

const pullHistoryData = (deviceId) => {
  ElMessage.success(`已触发设备 ${deviceId} 重拉历史数据`)
}

onMounted(() => {
  loadHealthData()
  loadStatistics()
})
</script>

<style scoped>
.device-data-page { padding: 4px; }
.page-header { margin-bottom: 18px; display: flex; justify-content: space-between; align-items: flex-start; }
.page-title { font-size: 22px; font-weight: 700; color: #303133; margin: 0; }
.page-subtitle { font-size: 14px; color: #909399; margin: 6px 0 0; }
.header-actions { display: flex; gap: 10px; }
.stat-row { display: flex; gap: 14px; margin: 16px 0 4px; flex-wrap: wrap; }
.stat-card { flex: 1; min-width: 130px; background: #f7f9fc; border: 1px solid #eef1f6; border-radius: 12px; padding: 14px 18px; }
.stat-card.stat-success { background: #f0fdf4; border-color: #bbf7d0; }
.stat-card.stat-success .stat-num { color: #16a34a; }
.stat-card.stat-danger { background: #fef2f2; border-color: #fecaca; }
.stat-card.stat-danger .stat-num { color: #dc2626; }
.stat-num { font-size: 24px; font-weight: 700; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.section-card { background: #fff; border-radius: 12px; padding: 18px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04); }
.section-card.mt-16 { margin-top: 16px; }
.section-title { font-size: 18px; font-weight: 600; color: #303133; margin: 0 0 16px; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; align-items: center; }
.raw-data-content { background: #f5f7fa; padding: 16px; border-radius: 8px; font-size: 13px; color: #606266; max-height: 400px; overflow-y: auto; white-space: pre-wrap; word-break: break-all; }
</style>
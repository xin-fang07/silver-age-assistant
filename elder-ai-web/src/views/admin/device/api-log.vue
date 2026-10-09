<template>
  <div class="device-api-log-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">接口调用日志</h2>
        <p class="page-subtitle">记录硬件和后端服务之间所有 API 请求，排查接口报错、网络问题</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="loadApiLogs">刷新日志</el-button>
        <el-button @click="exportLogs">导出日志</el-button>
      </div>
    </div>

    <div class="stat-row">
      <div class="stat-card"><div class="stat-num">{{ statistics.totalCount || 0 }}</div><div class="stat-label">调用总数</div></div>
      <div class="stat-card stat-success"><div class="stat-num">{{ statistics.successCount || 0 }}</div><div class="stat-label">成功调用</div></div>
      <div class="stat-card stat-danger"><div class="stat-num">{{ statistics.failCount || 0 }}</div><div class="stat-label">调用失败</div></div>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索设备编号/接口类型" clearable style="width: 250px" @keyup.enter="loadApiLogs" />
      <el-select v-model="filterStatus" placeholder="调用状态" style="width: 120px" clearable>
        <el-option label="成功" value="SUCCESS" />
        <el-option label="失败" value="FAIL" />
      </el-select>
      <el-select v-model="filterType" placeholder="接口类型" style="width: 150px" clearable>
        <el-option label="数据上报" value="DATA_UPLOAD" />
        <el-option label="心跳检测" value="HEARTBEAT" />
        <el-option label="指令下发" value="COMMAND" />
        <el-option label="设备注册" value="REGISTER" />
        <el-option label="其他" value="OTHER" />
      </el-select>
      <el-button @click="loadApiLogs">搜索</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <div class="section-card">
      <h3 class="section-title">接口调用记录</h3>
      <div v-loading="loading">
        <el-empty v-if="!loading && apiLogList.length === 0" description="暂无接口调用记录" :image-size="80" />
        <el-table v-else :data="apiLogList" style="width: 100%" stripe border>
          <el-table-column prop="id" label="日志ID" width="100" />
          <el-table-column prop="deviceId" label="设备SN" min-width="150" show-overflow-tooltip />
          <el-table-column prop="pushType" label="接口类型" width="130">
            <template #default="{ row }">{{ typeText(row.pushType) }}</template>
          </el-table-column>
          <el-table-column prop="title" label="调用标题" min-width="150" show-overflow-tooltip />
          <el-table-column prop="content" label="入参内容" min-width="250" show-overflow-tooltip>
            <template #default="{ row }">
              <el-button type="text" size="small" @click="viewContent(row)">查看详情</el-button>
            </template>
          </el-table-column>
          <el-table-column label="调用结果" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
                {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="failReason" label="失败原因" min-width="150" show-overflow-tooltip />
          <el-table-column prop="createTime" label="调用时间" min-width="180" />
        </el-table>
      </div>
    </div>

    <div class="section-card mt-16">
      <h3 class="section-title">接口调用统计</h3>
      <div v-loading="statsLoading">
        <div v-if="!statsLoading && statistics.typeStats" class="stats-grid">
          <div class="stats-item" v-for="(count, type) in statistics.typeStats" :key="type">
            <div class="stats-value">{{ count }}</div>
            <div class="stats-label">{{ typeText(type) }}</div>
          </div>
        </div>
        <el-empty v-if="!statsLoading && !statistics.typeStats" description="暂无统计数据" :image-size="80" />
      </div>
    </div>

    <div class="section-card mt-16">
      <h3 class="section-title">失败原因分析</h3>
      <div v-loading="statsLoading">
        <el-empty v-if="!statsLoading && (!statistics.failReasons || Object.keys(statistics.failReasons).length === 0)" description="暂无失败记录" :image-size="80" />
        <el-table v-else :data="failReasonList" style="width: 100%" stripe border>
          <el-table-column prop="reason" label="失败原因" min-width="200" />
          <el-table-column prop="count" label="失败次数" width="120" />
          <el-table-column label="占比" width="120">
            <template #default="{ row }">
              <span>{{ (row.count / (statistics.failCount || 1) * 100).toFixed(1) }}%</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="contentDialogVisible" title="请求报文详情" width="700px">
      <pre class="content-content">{{ currentContent }}</pre>
      <template #footer>
        <el-button @click="contentDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api'

const apiLogList = ref([])
const statistics = ref({})
const loading = ref(false)
const statsLoading = ref(false)
const keyword = ref('')
const filterStatus = ref('')
const filterType = ref('')

const contentDialogVisible = ref(false)
const currentContent = ref('')

const failReasonList = computed(() => {
  if (!statistics.value.failReasons) return []
  return Object.entries(statistics.value.failReasons)
    .map(([reason, count]) => ({ reason, count }))
    .sort((a, b) => b.count - a.count)
})

const typeText = (t) => {
  const map = {
    DATA_UPLOAD: '数据上报',
    HEARTBEAT: '心跳检测',
    COMMAND: '指令下发',
    REGISTER: '设备注册',
    OTHER: '其他',
    UNKNOWN: '未知'
  }
  return map[t] || t || '未知'
}

const loadApiLogs = async () => {
  loading.value = true
  try {
    const res = await adminApi.deviceApiLogs({
      deviceId: keyword.value || undefined,
      status: filterStatus.value || undefined
    })
    let data = res.data || []
    if (keyword.value && !filterStatus.value) {
      const kw = keyword.value.toLowerCase()
      data = data.filter(d => 
        (d.deviceId && d.deviceId.toLowerCase().includes(kw)) ||
        (d.pushType && d.pushType.toLowerCase().includes(kw))
      )
    }
    if (filterType.value) {
      data = data.filter(d => d.pushType === filterType.value)
    }
    apiLogList.value = data
  } catch (e) {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  statsLoading.value = true
  try {
    const res = await adminApi.deviceApiLogsStatistics()
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
  filterType.value = ''
  loadApiLogs()
}

const exportLogs = () => {
  const headers = ['日志ID', '设备SN', '接口类型', '调用标题', '调用结果', '失败原因', '调用时间']
  let csv = headers.join(',') + '\n'
  apiLogList.value.forEach(d => {
    const row = [
      `"${d.id || ''}"`,
      `"${d.deviceId || ''}"`,
      `"${typeText(d.pushType)}"`,
      `"${d.title || ''}"`,
      `"${d.status === 'SUCCESS' ? '成功' : '失败'}"`,
      `"${d.failReason || ''}"`,
      `"${d.createTime || ''}"`
    ]
    csv += row.join(',') + '\n'
  })
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `接口调用日志_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

const viewContent = (row) => {
  currentContent.value = row.content || '无详细内容'
  contentDialogVisible.value = true
}

onMounted(() => {
  loadApiLogs()
  loadStatistics()
})
</script>

<style scoped>
.device-api-log-page { padding: 4px; }
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
.stats-grid { display: flex; gap: 16px; flex-wrap: wrap; }
.stats-item { flex: 1; min-width: 120px; background: #f7f9fc; border: 1px solid #eef1f6; border-radius: 12px; padding: 14px 18px; text-align: center; }
.stats-value { font-size: 24px; font-weight: 700; color: #303133; }
.stats-label { font-size: 13px; color: #909399; margin-top: 4px; }
.content-content { background: #f5f7fa; padding: 16px; border-radius: 8px; font-size: 13px; color: #606266; max-height: 400px; overflow-y: auto; white-space: pre-wrap; word-break: break-all; }
</style>
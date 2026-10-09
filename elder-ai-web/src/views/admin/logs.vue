<template>
  <div class="logs-page">
    <div class="page-header">
      <h2>系统操作日志</h2>
      <p>按操作、角色、状态、时间、请求 ID 和 IP 查询审计记录</p>
    </div>

    <div class="filter-card">
      <el-input v-model="filters.keyword" clearable size="large" placeholder="用户、接口或错误信息" @keyup.enter="search" />
      <el-input v-model="filters.operation" clearable size="large" placeholder="操作类型，如 GET /api" />
      <el-select v-model="filters.userRole" clearable size="large" placeholder="用户角色">
        <el-option label="家属" value="FAMILY" />
        <el-option label="管理员" value="ADMIN" />
      </el-select>
      <el-input v-model="filters.statusCode" clearable size="large" placeholder="HTTP 状态码" />
      <el-input v-model="filters.traceId" clearable size="large" placeholder="请求 ID" />
      <el-input v-model="filters.ip" clearable size="large" placeholder="IP 地址" />
      <el-date-picker v-model="filters.timeRange" type="datetimerange" value-format="YYYY-MM-DDTHH:mm:ss"
        start-placeholder="开始时间" end-placeholder="结束时间" size="large" />
      <el-checkbox v-model="filters.exceptionOnly" size="large">仅看异常请求</el-checkbox>
      <div class="actions">
        <el-button type="primary" size="large" @click="search">查询日志</el-button>
        <el-button size="large" @click="reset">重置</el-button>
        <el-button type="success" size="large" :loading="exporting" @click="exportExcel">导出 Excel</el-button>
      </div>
    </div>

    <div class="table-card">
      <AsyncState :state="pageState" :error-message="loadError" empty-title="暂无符合条件的日志" @retry="load">
      <template #default>
        <el-table :data="rows" stripe border>
          <el-table-column prop="id" label="ID" width="75" align="center" />
          <el-table-column prop="username" label="操作用户" width="130">
            <template #default="{ row }">{{ row.username || '匿名' }}</template>
          </el-table-column>
          <el-table-column prop="userRole" label="角色" width="105" align="center">
            <template #default="{ row }">{{ roleLabel(row.userRole) }}</template>
          </el-table-column>
          <el-table-column prop="operation" label="操作类型" min-width="260" show-overflow-tooltip />
          <el-table-column label="状态" width="95" align="center">
            <template #default="{ row }"><el-tag :type="statusType(row.statusCode)">{{ row.statusCode || '-' }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="traceId" label="请求 ID" min-width="220" show-overflow-tooltip />
          <el-table-column prop="ip" label="IP 地址" width="145" />
          <el-table-column prop="duration" label="耗时(ms)" width="100" align="center" />
          <el-table-column prop="errorMessage" label="错误摘要" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ row.errorMessage || (row.success === 1 ? '成功' : '-') }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="操作时间" width="180">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :page-sizes="[10,20,50]"
            :total="total" layout="total, sizes, prev, pager, next, jumper" background
            @current-change="load" @size-change="sizeChanged" />
        </div>
      </template>
      </AsyncState>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AsyncState from '@/components/AsyncState.vue'
import { adminApi } from '@/api/index'
import { formatFriendlyTime } from '@/utils/friendlyTime'

const filters = reactive({ keyword: '', operation: '', userRole: '', statusCode: '', traceId: '', ip: '', exceptionOnly: false, timeRange: [] })
const rows = ref([])
const loading = ref(false)
const pageState = ref('loading')
const loadError = ref('')
const exporting = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const params = (paged = false) => {
  const result = {
    keyword: filters.keyword.trim() || undefined,
    operation: filters.operation.trim() || undefined,
    userRole: filters.userRole || undefined,
    statusCode: filters.statusCode ? Number(filters.statusCode) : undefined,
    traceId: filters.traceId.trim() || undefined,
    ip: filters.ip.trim() || undefined,
    exceptionOnly: filters.exceptionOnly || undefined,
    startTime: filters.timeRange?.[0], endTime: filters.timeRange?.[1]
  }
  if (paged) Object.assign(result, { pageNum: page.value, pageSize: pageSize.value })
  return result
}

const load = async () => {
  loading.value = true
  pageState.value = 'loading'; loadError.value = ''
  try {
    const res = await adminApi.listLogs(params(true))
    rows.value = Array.isArray(res.data) ? res.data : []
    total.value = Number(res.total || 0)
    pageState.value = rows.value.length ? 'success' : 'empty'
  } catch (e) {
    pageState.value = 'error'; loadError.value = e?.message || '加载系统日志失败'
  } finally { loading.value = false }
}
const search = () => { page.value = 1; load() }
const reset = () => { Object.assign(filters, { keyword: '', operation: '', userRole: '', statusCode: '', traceId: '', ip: '', exceptionOnly: false, timeRange: [] }); search() }
const sizeChanged = () => { page.value = 1; load() }

const exportExcel = async () => {
  exporting.value = true
  try {
    const blob = await adminApi.exportLogs(params())
    // 后端异常时返回 JSON（而非 xlsx），需拦截避免下载到损坏文件
    if (blob && blob.type && blob.type.includes('application/json')) {
      let msg = '导出失败，请稍后重试'
      try { msg = (JSON.parse(await blob.text()).message) || msg } catch {}
      ElMessage.error(msg); exporting.value = false; return
    }
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url; a.download = `系统日志-${new Date().toISOString().slice(0, 10)}.xlsx`
    document.body.appendChild(a); a.click(); a.remove(); URL.revokeObjectURL(url)
    ElMessage.success('已导出当前筛选结果（最多 5000 条）')
  } catch (e) { ElMessage.error('导出失败，请稍后重试') }
  finally { exporting.value = false }
}

const roleLabel = role => ({ FAMILY: '家属', ADMIN: '管理员' }[role] || '匿名')
const statusType = status => !status ? 'info' : status >= 500 ? 'danger' : status >= 400 ? 'warning' : 'success'
const formatTime = formatFriendlyTime
onMounted(load)
</script>

<style scoped>
.logs-page { min-height: 100%; }
.page-header { margin-bottom: 22px; }
.page-header h2 { margin: 0 0 8px; font-size: 22px; color: #1e293b; }
.page-header p { margin: 0; font-size: 17px; color: #64748b; }
.filter-card { display: grid; grid-template-columns: repeat(3, minmax(190px, 1fr)); gap: 14px; padding: 20px; margin-bottom: 18px; background: #fff; border-radius: 14px; }
.filter-card :deep(.el-date-editor) { width: 100%; }
.actions { grid-column: 1 / -1; display: flex; gap: 12px; }
.table-card { padding: 20px; background: #fff; border-radius: 14px; }
.table-card :deep(.el-table) { font-size: 14px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 20px; }
@media (max-width: 900px) { .filter-card { grid-template-columns: 1fr; } }
</style>

<template>
  <div class="warning-center-page">
    <div class="page-header">
      <h2 class="page-title"><el-icon><Warning /></el-icon>预警中心</h2>
      <p class="page-desc">汇总所有健康预警、SOS事件的完整处置全过程，记录操作痕迹，方便追溯复盘</p>
    </div>

    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon warning-icon"><el-icon><Warning /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.healthWarningPending }}</div>
          <div class="stat-label">健康预警待处理</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon success-icon"><el-icon><Bell /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.healthWarningHandled }}</div>
          <div class="stat-label">健康预警已处理</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon danger-icon"><el-icon><Bell /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.sosPending }}</div>
          <div class="stat-label">SOS待接单</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon primary-icon"><el-icon><Clock /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.sosProcessing }}</div>
          <div class="stat-label">SOS处理中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon success-icon"><el-icon><Bell /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.sosCompleted }}</div>
          <div class="stat-label">SOS已完成</div>
        </div>
      </div>
    </div>

    <div class="filter-bar">
      <el-select v-model="filterEventType" placeholder="事件类型" size="large" style="width: 180px">
        <el-option label="全部" value="" />
        <el-option label="健康预警" value="HEALTH_WARNING" />
        <el-option label="紧急求助" value="SOS" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="状态" size="large" style="width: 150px">
        <el-option label="全部" :value="null" />
        <el-option label="待处理" :value="0" />
        <el-option label="已知晓/已接单" :value="1" />
        <el-option label="处理中" :value="2" />
        <el-option label="已完成" :value="3" />
      </el-select>
      <el-button type="primary" size="large" @click="loadEvents">搜索</el-button>
      <el-button size="large" @click="resetFilter">重置</el-button>
    </div>

    <div class="event-list">
      <div v-for="event in events" :key="event.id" class="event-card" :class="getEventCardClass(event)">
        <div class="event-header">
          <div class="event-type-tag">
            <el-tag :type="event.eventType === 'SOS' ? 'danger' : 'warning'" size="large">
              {{ event.eventType === 'SOS' ? '🆘 紧急求助' : '⚠️ 健康预警' }}
            </el-tag>
          </div>
          <div class="event-status">
            <el-tag :type="getStatusTagType(event)" size="large">{{ event.statusText }}</el-tag>
          </div>
        </div>

        <div class="event-body">
          <div class="event-title">{{ event.title }}</div>
          <div class="event-content">{{ event.content }}</div>
          <div class="event-meta">
            <div class="meta-item">
              <el-icon><User /></el-icon>
              <span>{{ event.elderName }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDateTime(event.createTime) }}</span>
            </div>
            <div v-if="event.contactName" class="meta-item">
              <el-icon><Phone /></el-icon>
              <span>{{ event.contactName }} {{ event.contactPhone }}</span>
            </div>
          </div>
        </div>

        <div class="event-actions">
          <el-button v-if="event.status === 0 || (event.eventType === 'SOS' && event.status === 5)" type="primary" size="large" @click="handleAcknowledge(event)">
            {{ event.eventType === 'SOS' ? '接单' : '确认知晓' }}
          </el-button>
          <el-button v-if="event.status === 1 || event.status === 2" type="success" size="large" @click="handleContactFamily(event)">
            联系家属
          </el-button>
          <el-button v-if="event.status === 1 || event.status === 2" type="primary" size="large" @click="handleProcess(event)">
            {{ event.eventType === 'SOS' ? '开始处理' : '处理' }}
          </el-button>
          <el-button v-if="event.status !== 3" type="success" size="large" @click="handleClose(event)">
            闭环完结
          </el-button>
          <el-button type="info" size="large" @click="viewDetail(event)">
            查看详情
          </el-button>
        </div>
      </div>

      <div v-if="events.length === 0" class="empty-state">
        <el-empty description="暂无预警事件" :image-size="80" />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="事件详情与处理记录" width="800px" :close-on-click-modal="false">
      <div class="detail-content">
        <div class="detail-header">
          <el-tag :type="currentEvent?.eventType === 'SOS' ? 'danger' : 'warning'" size="large">
            {{ currentEvent?.eventType === 'SOS' ? '🆘 紧急求助' : '⚠️ 健康预警' }}
          </el-tag>
          <el-tag :type="getStatusTagType(currentEvent)" size="large">{{ currentEvent?.statusText }}</el-tag>
        </div>

        <el-descriptions :column="1" border size="large" style="margin-top: 16px">
          <el-descriptions-item label="事件ID">{{ currentEvent?.id }}</el-descriptions-item>
          <el-descriptions-item label="老人姓名">{{ currentEvent?.elderName }}</el-descriptions-item>
          <el-descriptions-item label="事件标题">{{ currentEvent?.title }}</el-descriptions-item>
          <el-descriptions-item label="事件内容">{{ currentEvent?.content }}</el-descriptions-item>
          <el-descriptions-item label="紧急联系人" v-if="currentEvent?.contactName">
            {{ currentEvent?.contactName }} {{ currentEvent?.contactPhone }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentEvent?.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="处理备注">{{ currentEvent?.actionNote || currentEvent?.handleRemark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="logs-section">
          <h3 class="logs-title"><el-icon><Document /></el-icon>处理流程记录</h3>
          <div v-if="handleLogs.length === 0" class="logs-empty">
            <el-empty description="暂无处理记录" :image-size="60" />
          </div>
          <div v-else class="logs-list">
            <div v-for="(log, index) in handleLogs" :key="log.id" class="log-item">
              <div class="log-step">{{ index + 1 }}</div>
              <div class="log-content">
                <div class="log-header">
                  <span class="log-action">{{ getActionText(log.action) }}</span>
                  <span class="log-time">{{ formatDateTime(log.createTime) }}</span>
                </div>
                <div class="log-operator">操作人：{{ log.operatorName }}</div>
                <div class="log-remark" v-if="log.remark">{{ log.remark }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="actionVisible" :title="actionTitle" width="500px" :close-on-click-modal="false">
      <div class="action-content">
        <el-form :model="actionForm">
          <el-form-item label="处理备注" prop="remark">
            <el-input v-model="actionForm.remark" type="textarea" :rows="4" placeholder="请填写处理备注..." />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button size="large" @click="actionVisible = false">取消</el-button>
        <el-button type="primary" size="large" :loading="actionLoading" @click="submitAction">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning, Bell, Clock, User, Phone, Document } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const stats = ref({
  healthWarningPending: 0,
  healthWarningHandled: 0,
  sosPending: 0,
  sosProcessing: 0,
  sosCompleted: 0
})

const events = ref([])
const filterEventType = ref('')
const filterStatus = ref(null)

const detailVisible = ref(false)
const currentEvent = ref(null)
const handleLogs = ref([])

const actionVisible = ref(false)
const actionLoading = ref(false)
const actionTitle = ref('')
const actionForm = reactive({ remark: '' })
const currentAction = ref('')

const getStatusTagType = (event) => {
  if (!event) return 'info'
  if (event.status === 0 || event.status === 5) return 'danger'
  if (event.status === 1) return 'warning'
  if (event.status === 2) return 'primary'
  if (event.status === 3) return 'success'
  return 'info'
}

const getEventCardClass = (event) => {
  if (event.status === 0 || event.status === 5) return 'event-card-urgent'
  return ''
}

const formatDateTime = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const getActionText = (action) => {
  const map = {
    'CREATE': '事件创建',
    'ACKNOWLEDGE': '确认知晓/接单',
    'CONTACT_FAMILY': '联系家属',
    'HANDLE': '处理中',
    'RESOLVE': '已解决',
    'CLOSE': '闭环完结'
  }
  return map[action] || action
}

const loadStats = async () => {
  try {
    const res = await adminApi.getWarningStats()
    stats.value = res?.data || {}
  } catch (e) {
    console.error(e)
  }
}

const loadEvents = async () => {
  try {
    const params = {
      pageNum: 1,
      pageSize: 50
    }
    if (filterEventType.value) params.eventType = filterEventType.value
    if (filterStatus.value !== null) params.status = filterStatus.value

    const res = await adminApi.getWarningEvents(params)
    events.value = res?.data?.list || []
  } catch (e) {
    console.error(e)
    ElMessage.error('加载事件失败')
  }
}

const resetFilter = () => {
  filterEventType.value = ''
  filterStatus.value = null
  loadEvents()
}

const viewDetail = async (event) => {
  currentEvent.value = event
  try {
    const res = await adminApi.getWarningHandleLogs(event.eventType, event.id)
    handleLogs.value = res.data || []
  } catch (e) {
    handleLogs.value = []
  }
  detailVisible.value = true
}

const handleAcknowledge = (event) => {
  actionTitle.value = event.eventType === 'SOS' ? '确认接单' : '确认知晓'
  currentAction.value = 'acknowledge'
  actionForm.remark = ''
  actionVisible.value = true
  currentEvent.value = event
}

const handleContactFamily = (event) => {
  actionTitle.value = '联系家属'
  currentAction.value = 'contact-family'
  actionForm.remark = ''
  actionVisible.value = true
  currentEvent.value = event
}

const handleProcess = (event) => {
  actionTitle.value = event.eventType === 'SOS' ? '开始处理' : '处理预警'
  currentAction.value = 'handle'
  actionForm.remark = ''
  actionVisible.value = true
  currentEvent.value = event
}

const handleClose = (event) => {
  actionTitle.value = '闭环完结'
  currentAction.value = 'close'
  actionForm.remark = ''
  actionVisible.value = true
  currentEvent.value = event
}

const submitAction = async () => {
  actionLoading.value = true
  try {
    const event = currentEvent.value
    await adminApi.handleWarningEvent(event.eventType, event.id, currentAction.value, { remark: actionForm.remark })
    ElMessage.success('操作成功')
    actionVisible.value = false
    loadEvents()
    loadStats()
  } catch (e) {
    ElMessage.error('操作失败')
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  loadStats()
  loadEvents()
})
</script>

<style scoped>
.warning-center-page { padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-title { font-size: 24px; font-weight: 700; margin-bottom: 8px; color: #1e293b; }
.page-desc { color: #64748b; font-size: 16px; }

.stats-row { display: flex; gap: 16px; margin-bottom: 24px; flex-wrap: wrap; }
.stat-card { flex: 1; min-width: 160px; display: flex; align-items: center; gap: 16px; padding: 20px; background: #fff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; }
.warning-icon { background: #fef3c7; color: #d97706; }
.success-icon { background: #dcfce7; color: #16a34a; }
.danger-icon { background: #fee2e2; color: #dc2626; }
.primary-icon { background: #dbeafe; color: #2563eb; }
.stat-value { font-size: 32px; font-weight: 700; color: #1e293b; }
.stat-label { color: #64748b; margin-top: 4px; }

.filter-bar { display: flex; gap: 12px; margin-bottom: 24px; flex-wrap: wrap; }

.event-list { display: flex; flex-direction: column; gap: 16px; }
.event-card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.event-card-urgent { border-left: 4px solid #dc2626; }
.event-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.event-title { font-size: 18px; font-weight: 600; color: #1e293b; margin-bottom: 8px; }
.event-content { color: #64748b; margin-bottom: 12px; }
.event-meta { display: flex; gap: 20px; flex-wrap: wrap; }
.meta-item { display: flex; align-items: center; gap: 4px; color: #94a3b8; font-size: 14px; }
.event-actions { display: flex; gap: 10px; margin-top: 16px; flex-wrap: wrap; }

.empty-state { padding: 60px 20px; }

.detail-content { padding: 8px; }
.detail-header { display: flex; gap: 12px; }
.logs-section { margin-top: 24px; }
.logs-title { font-size: 18px; font-weight: 600; margin-bottom: 16px; color: #1e293b; }
.logs-empty { padding: 30px 0; }
.logs-list { display: flex; flex-direction: column; gap: 12px; }
.log-item { display: flex; gap: 16px; padding: 12px; background: #f8fafc; border-radius: 8px; }
.log-step { width: 28px; height: 28px; background: #3b82f6; color: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: 600; flex-shrink: 0; }
.log-content { flex: 1; }
.log-header { display: flex; justify-content: space-between; margin-bottom: 4px; }
.log-action { font-weight: 600; color: #1e293b; }
.log-time { color: #94a3b8; font-size: 14px; }
.log-operator { color: #64748b; font-size: 14px; margin-bottom: 4px; }
.log-remark { color: #334155; padding: 8px; background: #fff; border-radius: 6px; font-size: 14px; }

.action-content { padding: 8px; }
</style>
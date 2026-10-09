<template>
  <div class="alerts-page">
    <div class="page-header">
      <h2 class="page-title">预警中心</h2>
      <div class="filter-bar">
        <el-select v-model="filterStatus" placeholder="筛选状态" size="large">
          <el-option label="全部" value="" />
          <el-option label="待处理" value="PENDING" />
          <el-option label="已处理" value="HANDLED" />
          <el-option label="已关闭" value="CLOSED" />
        </el-select>
        <el-select v-model="filterType" placeholder="预警类型" size="large">
          <el-option label="全部" value="" />
          <el-option label="血压异常" value="BLOOD_PRESSURE" />
          <el-option label="血糖异常" value="BLOOD_GLUCOSE" />
          <el-option label="心率异常" value="HEART_RATE" />
          <el-option label="紧急求助" value="EMERGENCY" />
          <el-option label="设备离线" value="DEVICE_OFFLINE" />
        </el-select>
      </div>
    </div>

    <div class="alerts-list">
      <div 
        v-for="alert in alerts" 
        :key="alert.id" 
        class="alert-card"
        :class="getAlertClass(alert)"
      >
        <div class="alert-icon-wrapper">
          <component :is="getAlertIcon(alert.type)" class="alert-icon" />
        </div>
        <div class="alert-content">
          <div class="alert-header">
            <span class="alert-type">{{ getAlertTypeName(alert.type) }}</span>
            <span class="alert-status" :class="alert.status.toLowerCase()">
              {{ getAlertStatusName(alert.status) }}
            </span>
          </div>
          <h3 class="alert-title">{{ alert.title }}</h3>
          <p class="alert-desc">{{ alert.description }}</p>
          <div class="alert-meta">
            <span class="alert-time">{{ alert.createTime }}</span>
            <span class="alert-elder">{{ alert.elderName }}</span>
          </div>
        </div>
        <div class="alert-actions">
          <el-button 
            v-if="alert.status === 'PENDING'" 
            size="small" 
            type="primary" 
            @click="handleAlert(alert)"
          >
            处理
          </el-button>
          <el-button size="small" @click="viewDetail(alert)">详情</el-button>
        </div>
      </div>

      <div v-if="alerts.length === 0" class="empty-state">
        <el-icon class="empty-icon"><Bell /></el-icon>
        <p>暂无预警信息</p>
      </div>
    </div>

    <el-dialog v-model="showDetailModal" title="预警详情" width="600px">
      <div v-if="selectedAlert" class="alert-detail">
        <div class="detail-row">
          <span class="detail-label">预警类型</span>
          <span class="detail-value">{{ getAlertTypeName(selectedAlert.type) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">预警标题</span>
          <span class="detail-value">{{ selectedAlert.title }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">预警描述</span>
          <span class="detail-value">{{ selectedAlert.description }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">关联老人</span>
          <span class="detail-value">{{ selectedAlert.elderName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">预警时间</span>
          <span class="detail-value">{{ selectedAlert.createTime }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">当前状态</span>
          <span class="detail-value" :class="selectedAlert.status.toLowerCase()">
            {{ getAlertStatusName(selectedAlert.status) }}
          </span>
        </div>
        <div v-if="selectedAlert.data" class="detail-row">
          <span class="detail-label">检测数据</span>
          <pre class="detail-data">{{ JSON.stringify(selectedAlert.data, null, 2) }}</pre>
        </div>
      </div>
      <template #footer>
        <el-button size="large" @click="showDetailModal = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Bell, Warning, FirstAidKit, Odometer, Phone, CircleClose } from '@element-plus/icons-vue'
import { useToast } from '@/composables/useToast'
import { healthWarningApi } from '@/api/index'

const toast = useToast()
const alerts = ref([])
const filterStatus = ref('')
const filterType = ref('')
const showDetailModal = ref(false)
const selectedAlert = ref(null)

const getAlertIcon = (type) => {
  const icons = {
    BLOOD_PRESSURE: FirstAidKit,
    BLOOD_GLUCOSE: Odometer,
    HEART_RATE: FirstAidKit,
    EMERGENCY: Phone,
    DEVICE_OFFLINE: CircleClose
  }
  return icons[type] || Warning
}

const getAlertTypeName = (type) => {
  const names = {
    BLOOD_PRESSURE: '血压异常预警',
    BLOOD_GLUCOSE: '血糖异常预警',
    HEART_RATE: '心率异常预警',
    EMERGENCY: '紧急求助通知',
    DEVICE_OFFLINE: '设备离线提醒'
  }
  return names[type] || '未知预警'
}

const getAlertStatusName = (status) => {
  const names = {
    PENDING: '待处理',
    HANDLED: '已处理',
    CLOSED: '已关闭'
  }
  return names[status] || '未知状态'
}

const getAlertClass = (alert) => {
  const classes = {
    PENDING: 'alert-warning',
    HANDLED: 'alert-info',
    CLOSED: 'alert-success'
  }
  return classes[alert.status] || ''
}

const loadAlerts = async () => {
  try {
    const res = await healthWarningApi.list({
      status: filterStatus.value || undefined,
      type: filterType.value || undefined
    })
    const list = res.data || []
    // 后端返回 HealthWarning(warningType/warningContent/status整数)，映射成页面字段
    alerts.value = list.map(w => ({
      id: w.id,
      type: w.warningType,
      title: getAlertTypeName(w.warningType),
      description: w.warningContent,
      elderName: w.elderName || '未关联老人',
      status: w.status === 0 ? 'PENDING' : w.status === 1 ? 'HANDLED' : 'CLOSED',
      createTime: w.createTime
    }))
  } catch (error) {
    toast.error('加载预警列表失败')
  }
}

const handleAlert = async (alert) => {
  try {
    // 后端 updateStatus 接收整数：1=已知晓, 2=已处理
    await healthWarningApi.updateStatus(alert.id, 2)
    toast.success('预警处理成功')
    loadAlerts()
  } catch (error) {
    toast.error('处理失败')
  }
}

const viewDetail = (alert) => {
  selectedAlert.value = alert
  showDetailModal.value = true
}

onMounted(() => {
  loadAlerts()
})
</script>

<style scoped>
.alerts-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.filter-bar {
  display: flex;
  gap: 12px;
}

.alerts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.alert-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  display: flex;
  gap: 16px;
  border-left: 4px solid transparent;
}

.alert-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.alert-warning {
  border-left-color: #f59e0b;
}

.alert-info {
  border-left-color: #3b82f6;
}

.alert-success {
  border-left-color: #22c55e;
}

.alert-icon-wrapper {
  width: 48px;
  height: 48px;
  background: #f8fafc;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.alert-icon {
  width: 24px;
  height: 24px;
  color: #f59e0b;
}

.alert-warning .alert-icon {
  color: #f59e0b;
}

.alert-info .alert-icon {
  color: #3b82f6;
}

.alert-success .alert-icon {
  color: #22c55e;
}

.alert-content {
  flex: 1;
  min-width: 0;
}

.alert-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.alert-type {
  font-size: 14px;
  font-weight: 600;
  color: #f59e0b;
  background: #fffbeb;
  padding: 4px 12px;
  border-radius: 20px;
}

.alert-status {
  font-size: 13px;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 20px;
}

.alert-status.pending {
  background: #fffbeb;
  color: #d97706;
}

.alert-status.handled {
  background: #eff6ff;
  color: #2563eb;
}

.alert-status.closed {
  background: #ecfdf5;
  color: #16a34a;
}

.alert-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px;
}

.alert-desc {
  font-size: 15px;
  color: #64748b;
  margin: 0 0 12px;
  line-height: 1.6;
}

.alert-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #94a3b8;
}

.alert-actions {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.empty-state {
  background: #f8fafc;
  border-radius: 16px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.empty-icon {
  width: 64px;
  height: 64px;
  color: #94a3b8;
  margin-bottom: 16px;
}

.empty-state p {
  font-size: 16px;
  color: #64748b;
  margin: 0;
}

.alert-detail {
  padding: 16px 0;
}

.detail-row {
  display: flex;
  margin-bottom: 16px;
}

.detail-label {
  width: 120px;
  font-size: 15px;
  font-weight: 600;
  color: #64748b;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  font-size: 15px;
  color: #1e293b;
}

.detail-value.pending {
  color: #d97706;
}

.detail-value.handled {
  color: #2563eb;
}

.detail-value.closed {
  color: #16a34a;
}

.detail-data {
  flex: 1;
  background: #f8fafc;
  padding: 12px;
  border-radius: 8px;
  font-size: 13px;
  color: #64748b;
  max-height: 200px;
  overflow-y: auto;
}
</style>
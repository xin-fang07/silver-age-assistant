<template>
  <div class="device-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">设备管理</h2>
        <div class="stat-row">
          <span class="stat-pill">设备 {{ devices.length }}</span>
          <span class="stat-pill online">在线 {{ onlineCount }}</span>
          <span class="stat-pill offline">离线 {{ offlineCount }}</span>
        </div>
      </div>
      <div>
        <el-button size="large" @click="loadDevices">刷新</el-button>
        <el-button type="primary" size="large" @click="showBindModal = true">
          <el-icon><Plus /></el-icon>
          绑定设备
        </el-button>
      </div>
    </div>

    <div class="device-grid">
      <div 
        v-for="device in devices" 
        :key="device.deviceId" 
        class="device-card"
        :class="{ 'offline': device.status === 0 }"
      >
        <div class="device-icon-wrapper">
          <component :is="getDeviceIcon(device.deviceType)" class="device-icon" />
        </div>
        <div class="device-info">
          <h3 class="device-name">{{ device.deviceName }}</h3>
          <p class="device-type">{{ getDeviceTypeName(device.deviceType) }}</p>
          <p class="device-elder" v-if="getElderName(device.elderId)">关联老人：{{ getElderName(device.elderId) }}</p>
          <div class="device-status">
            <span class="status-dot" :class="device.status === 1 ? 'online' : 'offline'"></span>
            <span>{{ device.status === 1 ? '在线' : '离线' }}</span>
          </div>
          <p class="device-sync">最后同步：{{ device.lastSyncTime || '从未同步' }}</p>
        </div>
        <div class="device-actions">
          <el-button size="small" @click="viewDeviceData(device)">查看数据</el-button>
          <el-button size="small" type="warning" :loading="mockLoadingId === device.deviceId" @click="submitMockData(device)">模拟上报异常</el-button>
          <el-button size="small" type="danger" @click="unbindDevice(device)">解绑</el-button>
        </div>
      </div>

      <div v-if="devices.length === 0" class="empty-device-card">
        <el-icon class="empty-icon"><Monitor /></el-icon>
        <p>暂无绑定设备</p>
        <el-button type="primary" size="small" @click="showBindModal = true">绑定设备</el-button>
      </div>
    </div>

    <el-dialog v-model="showBindModal" title="绑定设备" width="500px" :close-on-click-modal="false">
      <el-form :model="bindForm" :rules="bindRules" ref="bindFormRef" label-width="120px">
        <el-form-item label="设备ID" prop="deviceId">
          <el-input v-model="bindForm.deviceId" placeholder="请输入设备ID" size="large" />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="bindForm.deviceName" placeholder="请输入设备名称" size="large" />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="bindForm.deviceType" placeholder="请选择设备类型" size="large">
            <el-option label="智能手表" value="WATCH" />
            <el-option label="电子血压计" value="BLOOD_PRESSURE" />
            <el-option label="血糖仪" value="GLUCOSE" />
            <el-option label="心率监测仪" value="HEART_RATE" />
            <el-option label="其他设备" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联老人" prop="elderId">
          <el-select v-model="bindForm.elderId" placeholder="请选择关联老人（可选）" size="large" clearable>
            <el-option
              v-for="e in elders"
              :key="e.elderInfoId"
              :label="e.realName || e.username"
              :value="e.elderInfoId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button size="large" @click="showBindModal = false">取消</el-button>
        <el-button type="primary" size="large" @click="submitBind">确认绑定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDataModal" title="设备数据" width="600px">
      <div v-if="selectedDevice" class="device-data-info">
        <h4>{{ selectedDevice.deviceName }} - 最近数据</h4>
        <el-table :data="deviceDataList" style="width: 100%;">
          <el-table-column prop="time" label="时间" />
          <el-table-column prop="heartRate" label="心率" />
          <el-table-column prop="steps" label="步数" />
          <el-table-column prop="sleepHours" label="睡眠(小时)" />
          <el-table-column prop="systolic" label="收缩压" />
          <el-table-column prop="diastolic" label="舒张压" />
          <el-table-column prop="bloodGlucose" label="血糖" />
        </el-table>
      </div>
      <template #footer>
        <el-button size="large" @click="showDataModal = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Monitor, Watch, FirstAidKit, Odometer } from '@element-plus/icons-vue'
import { useToast } from '@/composables/useToast'
import { deviceApi, familyApi, healthApi } from '@/api/index.js'

const toast = useToast()
const showBindModal = ref(false)
const showDataModal = ref(false)
const bindFormRef = ref(null)
const devices = ref([])
const selectedDevice = ref(null)
const deviceDataList = ref([])
const elders = ref([])
const mockLoadingId = ref('')
const onlineCount = computed(() => devices.value.filter(d => d.status === 1).length)
const offlineCount = computed(() => devices.value.filter(d => d.status === 0).length)

const loadElders = async () => {
  try {
    const res = await familyApi.myElders()
    elders.value = res.data || []
  } catch (error) {
    elders.value = []
  }
}

const bindForm = ref({
  deviceId: '',
  deviceName: '',
  deviceType: '',
  elderId: ''
})

const bindRules = {
  deviceId: [{ required: true, message: '请输入设备ID', trigger: 'blur' }],
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }]
}

const getDeviceIcon = (type) => {
  const icons = {
    WATCH: Watch,
    BLOOD_PRESSURE: FirstAidKit,
    GLUCOSE: Odometer,
    HEART_RATE: FirstAidKit,
    OTHER: Monitor
  }
  return icons[type] || Monitor
}

const getDeviceTypeName = (type) => {
  const names = {
    WATCH: '智能手表',
    BLOOD_PRESSURE: '电子血压计',
    GLUCOSE: '血糖仪',
    HEART_RATE: '心率监测仪',
    OTHER: '其他设备'
  }
  return names[type] || '未知设备'
}

const getElderName = (elderId) => {
  if (!elderId) return ''
  const e = elders.value.find((x) => x.elderInfoId === elderId)
  return e ? (e.realName || e.username) : ''
}

const loadDevices = async () => {
  try {
    const res = await deviceApi.getElderDevices()
    devices.value = res.data || []
  } catch (error) {
    toast.error('加载设备列表失败')
  }
}

const submitBind = async () => {
  if (!bindFormRef.value) return
  await bindFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await deviceApi.bindDevice(bindForm.value)
        toast.success('设备绑定成功')
        showBindModal.value = false
        bindForm.value = { deviceId: '', deviceName: '', deviceType: '', elderId: '' }
        loadDevices()
      } catch (error) {
        toast.error('设备绑定失败')
      }
    }
  })
}

const unbindDevice = async (device) => {
  try {
    await deviceApi.unbindDevice(device.deviceId)
    toast.success('设备解绑成功')
    loadDevices()
  } catch (error) {
    toast.error('设备解绑失败')
  }
}

const viewDeviceData = async (device) => {
  selectedDevice.value = device
  showDataModal.value = true
  try {
    const res = await deviceApi.getDeviceData(device.deviceId)
    deviceDataList.value = res.data || []
  } catch (error) {
    deviceDataList.value = []
    toast.error('加载设备数据失败')
  }
}

const submitMockData = async (device) => {
  mockLoadingId.value = device.deviceId
  try {
    const now = new Date()
    const data = {
      deviceType: device.deviceType,
      deviceId: device.deviceId,
      externalRecordId: 'MOCK-' + now.getTime(),
      measuredAt: now.toISOString(),
      metrics: {
        bloodPressureHigh: 185,
        bloodPressureLow: 95,
        recordDate: now.toISOString().slice(0, 10)
      }
    }
    await healthApi.submitDeviceData(data)
    toast.success('已模拟上报异常血压数据，请前往「预警中心」查看')
  } catch (error) {
    toast.error('模拟上报失败：' + (error?.message || '未知错误'))
  } finally {
    mockLoadingId.value = ''
  }
}

onMounted(() => {
  loadDevices()
  loadElders()
})
</script>

<style scoped>
.device-page {
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
.stat-row { display: flex; gap: 10px; margin-top: 8px; }
.stat-pill { font-size: 13px; color: #64748b; background: #f1f5f9; border-radius: 999px; padding: 2px 12px; }
.stat-pill.online { color: #16a34a; background: #dcfce7; }
.stat-pill.offline { color: #64748b; background: #f1f5f9; }

.device-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.device-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.device-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.device-card.offline {
  opacity: 0.6;
}

.device-icon-wrapper {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.device-icon {
  width: 32px;
  height: 32px;
  color: #fff;
}

.device-info {
  flex: 1;
}

.device-name {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px;
}

.device-type {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 12px;
}

.device-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.status-dot.online {
  background: #22c55e;
}

.status-dot.offline {
  background: #94a3b8;
}

.device-sync {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
}

.device-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

.empty-device-card {
  background: #f8fafc;
  border-radius: 16px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 2px dashed #cbd5e1;
}

.empty-icon {
  width: 64px;
  height: 64px;
  color: #94a3b8;
  margin-bottom: 16px;
}

.empty-device-card p {
  font-size: 16px;
  color: #64748b;
  margin: 0 0 16px;
}

.device-data-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 16px;
}
</style>
<template>
  <div class="devices-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">设备管理</h2>
        <p class="page-subtitle">查看平台全部智能设备，并将设备绑定到老人档案</p>
      </div>
      <el-button @click="loadDevices">刷新</el-button>
    </div>
    <div class="stat-row">
      <div class="stat-card"><div class="stat-num">{{ deviceList.length }}</div><div class="stat-label">设备总数</div></div>
      <div class="stat-card"><div class="stat-num">{{ onlineCount }}</div><div class="stat-label">在线</div></div>
      <div class="stat-card"><div class="stat-num">{{ boundCount }}</div><div class="stat-label">已绑定老人</div></div>
      <div class="stat-card"><div class="stat-num">{{ unboundCount }}</div><div class="stat-label">未绑定</div></div>
    </div>

    <el-tabs v-model="activeTab" class="device-tabs">
      <el-tab-pane label="设备列表" name="list">
        <div class="section-card">
          <div v-loading="loading">
        <el-empty v-if="!loading && deviceList.length === 0" description="暂无任何已绑定设备" :image-size="80" />
        <el-table v-else :data="deviceList" style="width: 100%" stripe border>
          <el-table-column prop="deviceId" label="设备编号" min-width="140" show-overflow-tooltip />
          <el-table-column prop="deviceName" label="设备名称" min-width="120" />
          <el-table-column prop="deviceType" label="类型" width="120">
            <template #default="{ row }">{{ typeText(row.deviceType) }}</template>
          </el-table-column>
          <el-table-column prop="vendor" label="厂商" min-width="110" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '在线' : '离线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="绑定老人" min-width="120">
            <template #default="{ row }">
              <span v-if="row.elderName">{{ row.elderName }}</span>
              <el-tag v-else type="warning" size="small">未绑定</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="familyUsername" label="所属家属" min-width="120" />
          <el-table-column label="最后同步" min-width="160">
            <template #default="{ row }">{{ row.lastSyncTime || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openBind(row)">绑定老人</el-button>
              <el-button v-if="row.elderId" type="warning" link size="small" @click="unbindElder(row)">解绑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
        </el-tab-pane>
        <el-tab-pane label="数据同步记录" name="sync">
          <div class="section-card">
            <div v-loading="syncLoading">
              <el-empty v-if="!syncLoading && syncLogs.length === 0" description="暂无设备同步记录" :image-size="80" />
              <el-table v-else :data="syncLogs" style="width:100%" stripe border>
                <el-table-column prop="deviceName" label="设备" min-width="120" />
                <el-table-column prop="elderName" label="老人" min-width="100" />
                <el-table-column prop="metricsSummary" label="指标摘要" min-width="220" show-overflow-tooltip />
                <el-table-column label="状态" width="90" align="center">
                  <template #default="{row}">
                    <el-tag :type="row.syncStatus==='SUCCESS' ? 'success' : 'danger'" size="small">{{ row.syncStatus==='SUCCESS' ? '成功' : '失败' }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="syncTime" label="同步时间" min-width="170" />
              </el-table>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

    <!-- 绑定老人弹窗 -->
    <el-dialog v-model="dialogVisible" title="绑定设备到老人档案" width="420px">
      <div v-if="currentDevice" class="bind-dialog">
        <p class="dialog-device">设备：{{ currentDevice.deviceName }}（{{ currentDevice.deviceId }}）</p>
        <el-select v-model="selectedElder" placeholder="请选择老人档案" style="width: 100%">
          <el-option
            v-for="e in elderOptions"
            :key="e.elderInfoId"
            :label="e.realName"
            :value="e.elderInfoId"
          />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="confirmBind">确认绑定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api'

const deviceList = ref([])
const loading = ref(false)
const elderOptions = ref([])
const dialogVisible = ref(false)
const currentDevice = ref(null)
const selectedElder = ref(null)
const saving = ref(false)

const onlineCount = computed(() => deviceList.value.filter(d => d.status === 1).length)
const boundCount = computed(() => deviceList.value.filter(d => d.elderId).length)
const unboundCount = computed(() => deviceList.value.filter(d => !d.elderId).length)

const typeText = (t) => {
  const map = { WATCH: '智能手表', BLOOD_PRESSURE: '血压计', GLUCOSE: '血糖仪', HEART_RATE: '心率带', OTHER: '其他' }
  return map[t] || t || '其他'
}

const loadDevices = async () => {
  loading.value = true
  try {
    const res = await adminApi.deviceList()
    deviceList.value = res.data || []
  } catch (e) {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}

const loadElders = async () => {
  try {
    const res = await adminApi.elderOptions()
    elderOptions.value = res.data || []
  } catch (e) {
    // 拦截器已处理
  }
}

const openBind = (row) => {
  currentDevice.value = row
  selectedElder.value = row.elderId || null
  dialogVisible.value = true
}

const confirmBind = async () => {
  if (!selectedElder.value) {
    ElMessage.warning('请选择老人档案')
    return
  }
  saving.value = true
  try {
    await adminApi.deviceBindElder(currentDevice.value.id, selectedElder.value)
    ElMessage.success('绑定成功')
    dialogVisible.value = false
    await loadDevices()
  } catch (e) {
    // 拦截器已处理
  } finally {
    saving.value = false
  }
}

const unbindElder = async (row) => {
  try {
    await adminApi.deviceUnbindElder(row.id)
    ElMessage.success('已解除绑定')
    await loadDevices()
  } catch (e) {
    // 拦截器已处理
  }
}

const activeTab = ref('list')
const syncLogs = ref([])
const syncLoading = ref(false)
const loadSyncLogs = async () => {
  syncLoading.value = true
  try {
    const res = await adminApi.deviceSyncLogs()
    syncLogs.value = res.data || []
  } catch (e) {
    // 拦截器已处理
  } finally {
    syncLoading.value = false
  }
}
watch(activeTab, (v) => { if (v === 'sync') loadSyncLogs() })

onMounted(() => {
  loadDevices()
  loadElders()
})
</script>

<style scoped>
.devices-page { padding: 4px; }
.page-header { margin-bottom: 18px; }
.page-title { font-size: 22px; font-weight: 700; color: #303133; margin: 0; }
.page-subtitle { font-size: 14px; color: #909399; margin: 6px 0 0; }
.stat-row { display: flex; gap: 14px; margin: 16px 0 4px; flex-wrap: wrap; }
.stat-card { flex: 1; min-width: 130px; background: #f7f9fc; border: 1px solid #eef1f6; border-radius: 12px; padding: 14px 18px; }
.stat-num { font-size: 24px; font-weight: 700; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.section-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}
.bind-dialog .dialog-device { margin: 0 0 12px; color: #606266; }
</style>

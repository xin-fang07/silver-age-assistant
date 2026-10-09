<template>
  <div class="device-binding-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">设备绑定管理</h2>
        <p class="page-subtitle">管控「硬件 ↔ 老人」的绑定关系，谁的设备、能否解绑换绑都在这里操作</p>
      </div>
      <el-button type="primary" @click="openBindDialog">手动绑定</el-button>
    </div>

    <div class="stat-row">
      <div class="stat-card"><div class="stat-num">{{ bindRecords.length }}</div><div class="stat-label">绑定记录总数</div></div>
      <div class="stat-card"><div class="stat-num">{{ boundCount }}</div><div class="stat-label">当前已绑定</div></div>
      <div class="stat-card"><div class="stat-num">{{ unboundCount }}</div><div class="stat-label">未绑定设备</div></div>
    </div>

    <div class="section-card">
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索设备编号/老人姓名" clearable style="width: 250px" @keyup.enter="loadBindRecords" />
        <el-select v-model="filterStatus" placeholder="绑定状态" style="width: 120px" clearable>
          <el-option label="已绑定" :value="1" />
          <el-option label="未绑定" :value="0" />
        </el-select>
        <el-button @click="loadBindRecords">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>

      <div v-loading="loading">
        <el-empty v-if="!loading && bindRecords.length === 0" description="暂无绑定记录" :image-size="80" />
        <el-table v-else :data="bindRecords" style="width: 100%" stripe border>
          <el-table-column prop="deviceId" label="设备SN" min-width="150" show-overflow-tooltip />
          <el-table-column prop="deviceType" label="设备类型" width="130">
            <template #default="{ row }">{{ typeText(row.deviceType) }}</template>
          </el-table-column>
          <el-table-column label="绑定老人" min-width="120">
            <template #default="{ row }">
              <span v-if="row.elderName">{{ row.elderName }}</span>
              <el-tag v-else type="warning" size="small">未绑定</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="familyUsername" label="绑定家属" min-width="100" />
          <el-table-column prop="bindTime" label="绑定时间" min-width="160" />
          <el-table-column label="绑定状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isBound ? 'success' : 'info'" size="small">
                {{ row.isBound ? '已绑定' : '未绑定' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openRebindDialog(row)">换绑</el-button>
              <el-button v-if="row.isBound" type="danger" link size="small" @click="handleUnbind(row)">解绑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="section-card mt-16">
      <h3 class="section-title">历史绑定记录</h3>
      <div v-loading="historyLoading">
        <el-table :data="historyRecords" style="width: 100%" stripe border>
          <el-table-column prop="deviceId" label="设备SN" min-width="150" />
          <el-table-column prop="deviceType" label="设备类型" width="130">
            <template #default="{ row }">{{ typeText(row.deviceType) }}</template>
          </el-table-column>
          <el-table-column label="绑定老人" min-width="120">
            <template #default="{ row }">{{ row.elderName || '未绑定' }}</template>
          </el-table-column>
          <el-table-column prop="familyUsername" label="绑定家属" min-width="100" />
          <el-table-column prop="bindTime" label="绑定时间" min-width="160" />
          <el-table-column label="当前状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.isBound ? 'success' : 'info'" size="small">
                {{ row.isBound ? '仍绑定' : '已解绑' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="bindDialogVisible" title="手动绑定设备" width="450px">
      <div class="bind-dialog-content">
        <el-form-item label="选择设备">
          <el-select v-model="bindForm.deviceId" placeholder="请选择设备" style="width: 100%">
            <el-option
              v-for="d in availableDevices"
              :key="d.id"
              :label="`${d.deviceName || d.deviceId} (${typeText(d.deviceType)})`"
              :value="d.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择老人">
          <el-select v-model="bindForm.elderId" placeholder="请选择老人档案" style="width: 100%">
            <el-option
              v-for="e in elderOptions"
              :key="e.elderInfoId"
              :label="e.realName"
              :value="e.elderInfoId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="绑定家属（可选）">
          <el-select v-model="bindForm.userId" placeholder="请选择家属账号" style="width: 100%" clearable>
            <el-option
              v-for="f in familyOptions"
              :key="f.userId"
              :label="f.nickname"
              :value="f.userId"
            />
          </el-select>
        </el-form-item>
      </div>
      <template #footer>
        <el-button @click="bindDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="confirmBind">确认绑定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rebindDialogVisible" title="设备换绑" width="450px">
      <div class="bind-dialog-content" v-if="currentRebindDevice">
        <p class="dialog-device-info">当前设备：{{ currentRebindDevice.deviceName || currentRebindDevice.deviceId }}（{{ typeText(currentRebindDevice.deviceType) }}）</p>
        <el-form-item label="重新绑定老人">
          <el-select v-model="rebindForm.elderId" placeholder="请选择老人档案" style="width: 100%" clearable>
            <el-option
              v-for="e in elderOptions"
              :key="e.elderInfoId"
              :label="e.realName"
              :value="e.elderInfoId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="重新绑定家属（可选）">
          <el-select v-model="rebindForm.userId" placeholder="请选择家属账号" style="width: 100%" clearable>
            <el-option
              v-for="f in familyOptions"
              :key="f.userId"
              :label="f.nickname"
              :value="f.userId"
            />
          </el-select>
        </el-form-item>
      </div>
      <template #footer>
        <el-button @click="rebindDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="confirmRebind">确认换绑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'

const bindRecords = ref([])
const historyRecords = ref([])
const loading = ref(false)
const historyLoading = ref(false)
const saving = ref(false)
const keyword = ref('')
const filterStatus = ref(null)

const bindDialogVisible = ref(false)
const rebindDialogVisible = ref(false)
const currentRebindDevice = ref(null)

const elderOptions = ref([])
const familyOptions = ref([])
const availableDevices = ref([])

const bindForm = ref({
  deviceId: null,
  elderId: null,
  userId: null
})

const rebindForm = ref({
  elderId: null,
  userId: null
})

const boundCount = computed(() => bindRecords.value.filter(r => r.isBound).length)
const unboundCount = computed(() => bindRecords.value.filter(r => !r.isBound).length)

const typeText = (t) => {
  const map = { WATCH: '智能手表', BLOOD_PRESSURE: '血压计', SLEEP_MONITOR: '睡眠监测仪', GLUCOSE: '血糖仪', HEART_RATE: '心率带', OTHER: '其他' }
  return map[t] || t || '其他'
}

const loadBindRecords = async () => {
  loading.value = true
  try {
    const res = await adminApi.deviceBindRecords()
    let records = res.data || []
    if (keyword.value) {
      const kw = keyword.value.toLowerCase()
      records = records.filter(r => 
        (r.deviceId && r.deviceId.toLowerCase().includes(kw)) ||
        (r.elderName && r.elderName.toLowerCase().includes(kw)) ||
        (r.familyUsername && r.familyUsername.toLowerCase().includes(kw))
      )
    }
    if (filterStatus.value !== null) {
      records = records.filter(r => r.isBound === (filterStatus.value === 1))
    }
    bindRecords.value = records
    historyRecords.value = records.slice(0, 50)
  } catch (e) {
    // 拦截器已处理
  } finally {
    loading.value = false
    historyLoading.value = false
  }
}

const loadElders = async () => {
  try {
    const res = await adminApi.elderOptions()
    elderOptions.value = res.data || []
  } catch (e) {}
}

const loadFamilies = async () => {
  try {
    const res = await adminApi.familyOptions()
    familyOptions.value = res.data || []
  } catch (e) {}
}

const loadAvailableDevices = async () => {
  try {
    const res = await adminApi.deviceList()
    availableDevices.value = (res.data || []).filter(d => d.enabled === 1)
  } catch (e) {}
}

const resetFilters = () => {
  keyword.value = ''
  filterStatus.value = null
  loadBindRecords()
}

const openBindDialog = () => {
  bindForm.value = { deviceId: null, elderId: null, userId: null }
  loadAvailableDevices()
  bindDialogVisible.value = true
}

const confirmBind = async () => {
  if (!bindForm.value.deviceId) {
    ElMessage.warning('请选择设备')
    return
  }
  if (!bindForm.value.elderId) {
    ElMessage.warning('请选择老人档案')
    return
  }
  saving.value = true
  try {
    await adminApi.deviceBindElder(bindForm.value.deviceId, bindForm.value.elderId)
    if (bindForm.value.userId) {
      await adminApi.deviceRebind(bindForm.value.deviceId, { userId: bindForm.value.userId })
    }
    ElMessage.success('绑定成功')
    bindDialogVisible.value = false
    await loadBindRecords()
  } catch (e) {
    // 拦截器已处理
  } finally {
    saving.value = false
  }
}

const openRebindDialog = (row) => {
  currentRebindDevice.value = row
  rebindForm.value = { elderId: row.elderId, userId: row.userId }
  rebindDialogVisible.value = true
}

const confirmRebind = async () => {
  saving.value = true
  try {
    await adminApi.deviceRebind(currentRebindDevice.value.id, rebindForm.value)
    ElMessage.success('换绑成功')
    rebindDialogVisible.value = false
    await loadBindRecords()
  } catch (e) {
    // 拦截器已处理
  } finally {
    saving.value = false
  }
}

const handleUnbind = async (row) => {
  try {
    await ElMessageBox.confirm('确定要解除该设备的绑定吗？', '解绑确认', { type: 'warning' })
    await adminApi.deviceUnbindElder(row.id)
    ElMessage.success('已解绑')
    await loadBindRecords()
  } catch (e) {
    // 拦截器已处理
  }
}

onMounted(() => {
  loadBindRecords()
  loadElders()
  loadFamilies()
})
</script>

<style scoped>
.device-binding-page { padding: 4px; }
.page-header { margin-bottom: 18px; display: flex; justify-content: space-between; align-items: flex-start; }
.page-title { font-size: 22px; font-weight: 700; color: #303133; margin: 0; }
.page-subtitle { font-size: 14px; color: #909399; margin: 6px 0 0; }
.stat-row { display: flex; gap: 14px; margin: 16px 0 4px; flex-wrap: wrap; }
.stat-card { flex: 1; min-width: 130px; background: #f7f9fc; border: 1px solid #eef1f6; border-radius: 12px; padding: 14px 18px; }
.stat-num { font-size: 24px; font-weight: 700; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.section-card { background: #fff; border-radius: 12px; padding: 18px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04); }
.section-card.mt-16 { margin-top: 16px; }
.section-title { font-size: 18px; font-weight: 600; color: #303133; margin: 0 0 16px; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; align-items: center; }
.bind-dialog-content { padding: 10px 0; }
.dialog-device-info { margin: 0 0 16px; padding: 10px; background: #f5f7fa; border-radius: 8px; color: #606266; }
</style>

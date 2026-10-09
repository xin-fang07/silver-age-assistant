<template>
  <div class="device-list-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">设备列表</h2>
        <p class="page-subtitle">全平台所有智能硬件总台账，统一管理硬件本身信息</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="openImportDialog">批量导入</el-button>
        <el-button type="primary" @click="openCreateDialog">新增设备</el-button>
      </div>
    </div>

    <div class="stat-row">
      <div class="stat-card"><div class="stat-num">{{ deviceList.length }}</div><div class="stat-label">设备总数</div></div>
      <div class="stat-card"><div class="stat-num">{{ onlineCount }}</div><div class="stat-label">在线</div></div>
      <div class="stat-card"><div class="stat-num">{{ boundCount }}</div><div class="stat-label">已绑定老人</div></div>
      <div class="stat-card"><div class="stat-num">{{ disabledCount }}</div><div class="stat-label">已禁用</div></div>
    </div>

    <div class="section-card">
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索设备编号/名称/型号" clearable style="width: 250px" @keyup.enter="loadDevices" />
        <el-select v-model="filterType" placeholder="设备类型" style="width: 150px" clearable>
          <el-option label="智能手表" value="WATCH" />
          <el-option label="血压计" value="BLOOD_PRESSURE" />
          <el-option label="睡眠监测仪" value="SLEEP_MONITOR" />
          <el-option label="血糖仪" value="GLUCOSE" />
          <el-option label="心率带" value="HEART_RATE" />
          <el-option label="其他" value="OTHER" />
        </el-select>
        <el-select v-model="filterOnline" placeholder="在线状态" style="width: 120px" clearable>
          <el-option label="在线" :value="1" />
          <el-option label="离线" :value="0" />
        </el-select>
        <el-button @click="loadDevices">搜索</el-button>
        <el-button @click="resetFilters">重置</el-button>
        <el-button @click="exportDevices">导出表格</el-button>
      </div>

      <div v-loading="loading">
        <el-empty v-if="!loading && deviceList.length === 0" description="暂无设备" :image-size="80" />
        <el-table v-else :data="deviceList" style="width: 100%" stripe border>
          <el-table-column prop="deviceId" label="设备编号" min-width="150" show-overflow-tooltip />
          <el-table-column prop="deviceName" label="设备名称" min-width="120" />
          <el-table-column prop="deviceType" label="设备类型" width="130">
            <template #default="{ row }">{{ typeText(row.deviceType) }}</template>
          </el-table-column>
          <el-table-column prop="model" label="设备型号" min-width="120" />
          <el-table-column prop="factoryCode" label="出厂编号" min-width="120" />
          <el-table-column prop="firmwareVersion" label="固件版本" width="100" />
          <el-table-column prop="productionBatch" label="生产批次" width="100" />
          <el-table-column prop="vendor" label="厂商" min-width="100" />
          <el-table-column label="在线状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '在线' : '离线' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="启用状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.enabled === 1 ? 'success' : 'danger'" size="small">
                {{ row.enabled === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="绑定老人" min-width="120">
            <template #default="{ row }">
              <span v-if="row.elderName">{{ row.elderName }}</span>
              <el-tag v-else type="warning" size="small">未绑定</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="familyUsername" label="绑定家属" min-width="100" />
          <el-table-column prop="bindTime" label="绑定时间" min-width="160" />
          <el-table-column prop="lastSyncTime" label="最后同步" min-width="160" />
          <el-table-column label="操作" width="180" fixed="right" align="center">
            <template #default="{ row }">
              <div class="row-actions">
                <el-button type="primary" link size="small" @click="openEditDialog(row)">编辑</el-button>
                <el-button v-if="row.enabled === 1" type="danger" link size="small" @click="handleDisable(row)">禁用</el-button>
                <el-button v-else type="success" link size="small" @click="handleEnable(row)">启用</el-button>
                <el-button type="warning" link size="small" @click="handleDelete(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="createDialogVisible" title="新增设备" width="500px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="设备编号" required>
          <el-input v-model="createForm.deviceId" placeholder="请输入设备唯一SN码" />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="createForm.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" required>
          <el-select v-model="createForm.deviceType" placeholder="请选择设备类型">
            <el-option label="智能手表" value="WATCH" />
            <el-option label="血压计" value="BLOOD_PRESSURE" />
            <el-option label="睡眠监测仪" value="SLEEP_MONITOR" />
            <el-option label="血糖仪" value="GLUCOSE" />
            <el-option label="心率带" value="HEART_RATE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备型号">
          <el-input v-model="createForm.model" placeholder="请输入设备型号" />
        </el-form-item>
        <el-form-item label="出厂编号">
          <el-input v-model="createForm.factoryCode" placeholder="请输入出厂编号" />
        </el-form-item>
        <el-form-item label="固件版本">
          <el-input v-model="createForm.firmwareVersion" placeholder="请输入固件版本" />
        </el-form-item>
        <el-form-item label="生产批次">
          <el-input v-model="createForm.productionBatch" placeholder="请输入生产批次" />
        </el-form-item>
        <el-form-item label="厂商">
          <el-input v-model="createForm.vendor" placeholder="请输入厂商名称" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="confirmCreate">确认添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑设备" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="设备编号">
          <el-input v-model="editForm.deviceId" disabled />
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input v-model="editForm.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备型号">
          <el-input v-model="editForm.model" placeholder="请输入设备型号" />
        </el-form-item>
        <el-form-item label="出厂编号">
          <el-input v-model="editForm.factoryCode" placeholder="请输入出厂编号" />
        </el-form-item>
        <el-form-item label="固件版本">
          <el-input v-model="editForm.firmwareVersion" placeholder="请输入固件版本" />
        </el-form-item>
        <el-form-item label="生产批次">
          <el-input v-model="editForm.productionBatch" placeholder="请输入生产批次" />
        </el-form-item>
        <el-form-item label="厂商">
          <el-input v-model="editForm.vendor" placeholder="请输入厂商名称" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="confirmEdit">确认修改</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="批量导入设备" width="500px">
      <div class="import-area">
        <el-upload
          class="upload-demo"
          drag
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          accept=".json,.xlsx,.csv"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <template #tip>
            <div class="el-upload__tip">支持 JSON、XLSX、CSV 格式文件</div>
          </template>
        </el-upload>
        <div v-if="importFile" class="import-file-info">
          <el-icon><Document /></el-icon>
          <span>{{ importFile.name }}</span>
          <el-button type="text" @click="importFile = null">移除</el-button>
        </div>
        <div class="import-hint">
          <p>导入数据格式示例（JSON）：</p>
          <pre>[
  {
    "deviceId": "SN2024001",
    "deviceName": "智能手表A1",
    "deviceType": "WATCH",
    "model": "A1-2024",
    "factoryCode": "FC001",
    "firmwareVersion": "1.0.0",
    "productionBatch": "2024Q1",
    "vendor": "XX科技"
  }
]</pre>
        </div>
      </div>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="confirmImport">确认导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, Document } from '@element-plus/icons-vue'
import { adminApi } from '@/api'

const deviceList = ref([])
const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const filterType = ref('')
const filterOnline = ref(null)

const createDialogVisible = ref(false)
const editDialogVisible = ref(false)
const importDialogVisible = ref(false)
const importFile = ref(null)
const currentEditDevice = ref(null)

const createForm = ref({
  deviceId: '',
  deviceName: '',
  deviceType: '',
  model: '',
  factoryCode: '',
  firmwareVersion: '',
  productionBatch: '',
  vendor: '',
  remark: ''
})

const editForm = ref({
  deviceId: '',
  deviceName: '',
  model: '',
  factoryCode: '',
  firmwareVersion: '',
  productionBatch: '',
  vendor: '',
  remark: ''
})

const onlineCount = computed(() => deviceList.value.filter(d => d.status === 1).length)
const boundCount = computed(() => deviceList.value.filter(d => d.elderId).length)
const disabledCount = computed(() => deviceList.value.filter(d => d.enabled === 0).length)

const typeText = (t) => {
  const map = { WATCH: '智能手表', BLOOD_PRESSURE: '血压计', SLEEP_MONITOR: '睡眠监测仪', GLUCOSE: '血糖仪', HEART_RATE: '心率带', OTHER: '其他' }
  return map[t] || t || '其他'
}

const loadDevices = async () => {
  loading.value = true
  try {
    const res = await adminApi.deviceList({
      keyword: keyword.value,
      deviceType: filterType.value,
      onlineStatus: filterOnline.value
    })
    deviceList.value = res.data || []
  } catch (e) {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  keyword.value = ''
  filterType.value = ''
  filterOnline.value = null
  loadDevices()
}

const exportDevices = () => {
  const headers = ['设备编号', '设备名称', '设备类型', '型号', '出厂编号', '固件版本', '生产批次', '厂商', '在线状态', '启用状态', '绑定老人', '绑定家属', '绑定时间', '最后同步']
  let csv = headers.join(',') + '\n'
  deviceList.value.forEach(d => {
    const row = [
      `"${d.deviceId || ''}"`,
      `"${d.deviceName || ''}"`,
      `"${typeText(d.deviceType)}"`,
      `"${d.model || ''}"`,
      `"${d.factoryCode || ''}"`,
      `"${d.firmwareVersion || ''}"`,
      `"${d.productionBatch || ''}"`,
      `"${d.vendor || ''}"`,
      `"${d.status === 1 ? '在线' : '离线'}"`,
      `"${d.enabled === 1 ? '启用' : '禁用'}"`,
      `"${d.elderName || ''}"`,
      `"${d.familyUsername || ''}"`,
      `"${d.bindTime || ''}"`,
      `"${d.lastSyncTime || ''}"`
    ]
    csv += row.join(',') + '\n'
  })
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `设备列表_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

const openCreateDialog = () => {
  createForm.value = {
    deviceId: '',
    deviceName: '',
    deviceType: '',
    model: '',
    factoryCode: '',
    firmwareVersion: '',
    productionBatch: '',
    vendor: '',
    remark: ''
  }
  createDialogVisible.value = true
}

const confirmCreate = async () => {
  if (!createForm.value.deviceId) {
    ElMessage.warning('请输入设备编号')
    return
  }
  if (!createForm.value.deviceType) {
    ElMessage.warning('请选择设备类型')
    return
  }
  saving.value = true
  try {
    await adminApi.deviceCreate(createForm.value)
    ElMessage.success('设备添加成功')
    createDialogVisible.value = false
    await loadDevices()
  } catch (e) {
    // 拦截器已处理
  } finally {
    saving.value = false
  }
}

const openEditDialog = (row) => {
  currentEditDevice.value = row
  editForm.value = {
    deviceId: row.deviceId,
    deviceName: row.deviceName,
    model: row.model,
    factoryCode: row.factoryCode,
    firmwareVersion: row.firmwareVersion,
    productionBatch: row.productionBatch,
    vendor: row.vendor,
    remark: row.remark
  }
  editDialogVisible.value = true
}

const confirmEdit = async () => {
  saving.value = true
  try {
    await adminApi.deviceUpdate(currentEditDevice.value.id, editForm.value)
    ElMessage.success('设备信息更新成功')
    editDialogVisible.value = false
    await loadDevices()
  } catch (e) {
    // 拦截器已处理
  } finally {
    saving.value = false
  }
}

const handleEnable = async (row) => {
  try {
    await adminApi.deviceEnable(row.id, 1)
    ElMessage.success('设备已启用')
    await loadDevices()
  } catch (e) {
    // 拦截器已处理
  }
}

const handleDisable = async (row) => {
  try {
    await adminApi.deviceEnable(row.id, 0)
    ElMessage.success('设备已禁用')
    await loadDevices()
  } catch (e) {
    // 拦截器已处理
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该设备吗？', '删除确认', { type: 'warning' })
    await adminApi.deviceDelete(row.id)
    ElMessage.success('设备已删除')
    await loadDevices()
  } catch (e) {
    // 拦截器已处理
  }
}

const openImportDialog = () => {
  importFile.value = null
  importDialogVisible.value = true
}

const handleFileChange = (file) => {
  importFile.value = file.raw
}

const confirmImport = async () => {
  if (!importFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  saving.value = true
  try {
    const reader = new FileReader()
    reader.onload = async (e) => {
      let data
      try {
        data = JSON.parse(e.target.result)
      } catch {
        ElMessage.error('文件格式错误，请上传JSON格式文件')
        saving.value = false
        return
      }
      if (!Array.isArray(data)) {
        ElMessage.error('导入数据必须是数组格式')
        saving.value = false
        return
      }
      const res = await adminApi.deviceBatchImport(data)
      const result = res.data
      ElMessage.success(`导入完成：成功 ${result.success} 条，失败 ${result.failed} 条`)
      if (result.failed > 0 && result.failedMessages) {
        console.log('导入失败详情:', result.failedMessages)
      }
      importDialogVisible.value = false
      await loadDevices()
      saving.value = false
    }
    reader.readAsText(importFile.value)
  } catch (e) {
    saving.value = false
    // 拦截器已处理
  }
}

onMounted(() => {
  loadDevices()
})
</script>

<style scoped>
.device-list-page { padding: 4px; }
.page-header { margin-bottom: 18px; display: flex; justify-content: space-between; align-items: flex-start; }
.page-title { font-size: 22px; font-weight: 700; color: #303133; margin: 0; }
.page-subtitle { font-size: 14px; color: #909399; margin: 6px 0 0; }
.header-actions { display: flex; gap: 10px; }
.stat-row { display: flex; gap: 14px; margin: 16px 0 4px; flex-wrap: wrap; }
.stat-card { flex: 1; min-width: 130px; background: #f7f9fc; border: 1px solid #eef1f6; border-radius: 12px; padding: 14px 18px; }
.stat-num { font-size: 24px; font-weight: 700; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
.section-card { background: #fff; border-radius: 12px; padding: 18px; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04); }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; align-items: center; }
.import-area { padding: 10px 0; }
.import-file-info { display: flex; align-items: center; gap: 8px; margin-top: 12px; padding: 10px; background: #f5f7fa; border-radius: 8px; }
.import-hint { margin-top: 16px; }
.import-hint p { margin: 0 0 8px; font-size: 13px; color: #606266; }
.import-hint pre { background: #f5f7fa; padding: 12px; border-radius: 8px; font-size: 12px; color: #606266; overflow-x: auto; }

/* 操作列：横向均匀排列，不换行，垂直居中 */
.row-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  flex-wrap: nowrap;
}
/* 还原文字链接按钮外观（覆盖全局 style.css 对 el-button 的实心样式） */
.row-actions :deep(.el-button.is-link) {
  min-height: auto !important;
  padding: 4px 6px !important;
  margin: 0 !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  letter-spacing: normal;
  transition: opacity 0.2s ease;
}
.row-actions :deep(.el-button.is-link + .el-button.is-link) {
  margin-left: 0 !important;
}
.row-actions :deep(.el-button.is-link.el-button--primary) { color: #2563eb !important; }
.row-actions :deep(.el-button.is-link.el-button--danger) { color: #dc2626 !important; }
.row-actions :deep(.el-button.is-link.el-button--success) { color: #16a34a !important; }
.row-actions :deep(.el-button.is-link.el-button--warning) { color: #d97706 !important; }
.row-actions :deep(.el-button.is-link:hover) { opacity: 0.7; }
</style>

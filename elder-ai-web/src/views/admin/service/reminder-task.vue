<template>
  <div class="admin-reminder-task">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="filter-group">
          <el-input v-model="keyword" placeholder="搜索标题、老人姓名" clearable style="width:260px" @keyup.enter="loadList" @clear="loadList" />
          <el-select v-model="filterType" clearable placeholder="提醒类型" style="width:150px" @change="loadList">
            <el-option label="用药提醒" value="MEDICINE" />
            <el-option label="复诊提醒" value="CHECKUP" />
            <el-option label="活动提醒" value="ACTIVITY" />
          </el-select>
          <el-select v-model="filterStatus" clearable placeholder="状态" style="width:120px" @change="loadList">
            <el-option label="待提醒" :value="0" />
            <el-option label="已完成" :value="1" />
            <el-option label="已过期" :value="2" />
          </el-select>
          <el-select v-model="filterPushStatus" clearable placeholder="推送状态" style="width:140px" @change="loadList">
            <el-option label="待推送" value="PENDING" />
            <el-option label="已推送" value="PUSHED" />
            <el-option label="推送失败" value="FAILED" />
          </el-select>
        </div>
        <div class="action-group">
          <el-button type="primary" @click="openAddDialog">新建提醒</el-button>
          <el-button @click="loadList">刷新</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="elderName" label="老人" width="120" />
        <el-table-column prop="title" label="提醒标题" min-width="160" show-overflow-tooltip />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.remindType)">{{ typeLabel(row.remindType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remindTime" label="提醒时间" width="170" />
        <el-table-column label="重复规则" width="100">
          <template #default="{ row }">{{ repeatLabel(row.repeatType) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推送状态" width="120">
          <template #default="{ row }">
            <el-tag :type="pushStatusTag(row.pushStatus)">{{ pushStatusLabel(row.pushStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="completedCount" label="完成次数" width="100" align="center" />
        <el-table-column prop="missedCount" label="错过次数" width="100" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button size="small" type="warning" v-if="row.status === 0" @click="togglePause(row)">停用</el-button>
            <el-button size="small" type="success" v-else @click="toggleResume(row)">启用</el-button>
            <el-button size="small" type="danger" @click="removeRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pager" v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10,20,50]"
        layout="total, sizes, prev, pager, next" @current-change="loadList" @size-change="loadList" />
    </el-card>

    <el-dialog v-model="addVisible" title="新建提醒" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="老人" required>
          <el-select v-model="form.elderInfoId" placeholder="请选择老人" filterable>
            <el-option v-for="e in elders" :key="e.elderInfoId" :label="e.realName" :value="e.elderInfoId" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入提醒标题" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.remindType">
            <el-option label="用药提醒" value="MEDICINE" />
            <el-option label="复诊提醒" value="CHECKUP" />
            <el-option label="活动提醒" value="ACTIVITY" />
            <el-option label="其他提醒" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒时间" required>
          <el-date-picker v-model="form.remindTime" type="datetime" placeholder="选择提醒时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="重复规则">
          <el-select v-model="form.repeatType" placeholder="单次提醒">
            <el-option label="单次" value="ONCE" />
            <el-option label="每日" value="DAILY" />
            <el-option label="每周" value="WEEKLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细内容">
          <el-input v-model="form.content" type="textarea" placeholder="请输入详细内容" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="saveReminder">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="提醒详情" width="650px">
      <template v-if="current">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="老人">{{ current.elderName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="提醒标题">{{ current.title || '—' }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ typeLabel(current.remindType) }}</el-descriptions-item>
          <el-descriptions-item label="提醒时间">{{ current.remindTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="重复规则">{{ repeatLabel(current.repeatType) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusLabel(current.status) }}</el-descriptions-item>
          <el-descriptions-item label="推送状态">{{ pushStatusLabel(current.pushStatus) }}</el-descriptions-item>
          <el-descriptions-item label="确认状态">{{ confirmStatusLabel(current.confirmStatus) }}</el-descriptions-item>
          <el-descriptions-item label="完成次数">{{ current.completedCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="错过次数">{{ current.missedCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="推送时间">{{ current.pushedAt || '—' }}</el-descriptions-item>
          <el-descriptions-item label="确认时间">{{ current.confirmedAt || '—' }}</el-descriptions-item>
          <el-descriptions-item label="详细内容" :span="2">{{ current.content || '—' }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi, familyApi } from '@/api'

const keyword = ref('')
const filterType = ref('')
const filterStatus = ref(null)
const filterPushStatus = ref('')
const elders = ref([])
const list = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const addVisible = ref(false)
const detailVisible = ref(false)
const current = ref(null)

const form = reactive({
  elderInfoId: null,
  title: '',
  content: '',
  remindType: 'MEDICINE',
  remindTime: null,
  repeatType: 'ONCE'
})

const typeMap = { MEDICINE: '用药提醒', CHECKUP: '复诊提醒', ACTIVITY: '活动提醒', OTHER: '其他提醒' }
const repeatMap = { ONCE: '单次', DAILY: '每日', WEEKLY: '每周' }
const statusMap = { 0: '待提醒', 1: '已完成', 2: '已过期' }
const pushStatusMap = { PENDING: '待推送', PUSHED: '已推送', FAILED: '推送失败' }
const confirmStatusMap = { UNCONFIRMED: '待确认', CONFIRMED: '已确认' }

function typeLabel(t) { return typeMap[t] || t || '—' }
function repeatLabel(t) { return repeatMap[t] || '单次' }
function statusLabel(s) { return statusMap[s] || s || '—' }
function pushStatusLabel(s) { return pushStatusMap[s] || s || '—' }
function confirmStatusLabel(s) { return confirmStatusMap[s] || s || '—' }

function typeTag(t) {
  const map = { MEDICINE: 'warning', CHECKUP: 'primary', ACTIVITY: 'success', OTHER: 'info' }
  return map[t] || 'info'
}
function statusTag(s) {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[s] || 'info'
}
function pushStatusTag(s) {
  const map = { PENDING: 'warning', PUSHED: 'success', FAILED: 'danger' }
  return map[s] || 'info'
}

async function loadElders() {
  const r = await familyApi.myElders()
  elders.value = r.data || r || []
}

async function loadList() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      remindType: filterType.value || undefined,
      status: filterType.value !== '' ? filterStatus.value : undefined,
      pushStatus: filterPushStatus.value || undefined
    }
    const r = await adminApi.listReminders(params)
    list.value = r.data || r.list || []
    total.value = r.total || 0
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  Object.assign(form, {
    elderInfoId: null,
    title: '',
    content: '',
    remindType: 'MEDICINE',
    remindTime: null,
    repeatType: 'ONCE'
  })
  addVisible.value = true
}

async function saveReminder() {
  if (!form.elderInfoId || !form.title || !form.remindTime) {
    ElMessage.warning('请填写必填项')
    return
  }
  await adminApi.createReminder(form)
  ElMessage.success('提醒已创建')
  addVisible.value = false
  loadList()
}

function openDetail(row) {
  current.value = row
  detailVisible.value = true
}

async function togglePause(row) {
  await adminApi.updateReminder(row.id, { status: 2 })
  ElMessage.success('已停用')
  loadList()
}

async function toggleResume(row) {
  await adminApi.updateReminder(row.id, { status: 0 })
  ElMessage.success('已启用')
  loadList()
}

async function removeRow(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.title}」提醒吗？`, '提示', { type: 'warning' })
  } catch { return }
  await adminApi.deleteReminder(row.id)
  ElMessage.success('已删除')
  loadList()
}

onMounted(() => { loadElders(); loadList() })
</script>

<style scoped>
.toolbar { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px; flex-wrap:wrap; gap:12px; }
.filter-group { display:flex; gap:12px; flex-wrap:wrap; }
.action-group { display:flex; gap:8px; }
.pager { margin-top:16px; justify-content:flex-end; }
</style>
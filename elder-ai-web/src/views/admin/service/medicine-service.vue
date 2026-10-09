<template>
  <div class="admin-medicine-service">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="filter-group">
          <el-input v-model="keyword" placeholder="搜索药品名称、老人姓名" clearable style="width:260px" @keyup.enter="loadList" @clear="loadList" />
          <el-select v-model="elderInfoId" placeholder="按老人筛选" clearable filterable style="width:180px" @change="loadList">
            <el-option v-for="e in elders" :key="e.elderInfoId" :label="e.realName" :value="e.elderInfoId" />
          </el-select>
          <el-select v-model="filterStatus" clearable placeholder="状态" style="width:120px" @change="loadList">
            <el-option label="待提醒" :value="0" />
            <el-option label="已完成" :value="1" />
            <el-option label="已过期" :value="2" />
          </el-select>
        </div>
        <div class="action-group">
          <el-button type="primary" @click="openAddDialog">新增用药计划</el-button>
          <el-button @click="loadList">刷新</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="elderName" label="老人" width="120" />
        <el-table-column prop="title" label="药品名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="服用频次" width="130">
          <template #default="{ row }">{{ repeatLabel(row.repeatType) }}</template>
        </el-table-column>
        <el-table-column prop="content" label="用量说明" min-width="180" show-overflow-tooltip />
        <el-table-column prop="remindTime" label="服药时间" width="170" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="completedCount" label="已服次数" width="100" align="center" />
        <el-table-column prop="missedCount" label="漏服次数" width="100" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button size="small" type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="removeRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pager" v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10,20,50]"
        layout="total, sizes, prev, pager, next" @current-change="loadList" @size-change="loadList" />
    </el-card>

    <el-dialog v-model="addVisible" :title="isEdit ? '编辑用药计划' : '新增用药计划'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="老人" required>
          <el-select v-model="form.elderInfoId" placeholder="请选择老人" filterable>
            <el-option v-for="e in elders" :key="e.elderInfoId" :label="e.realName" :value="e.elderInfoId" />
          </el-select>
        </el-form-item>
        <el-form-item label="药品名称" required>
          <el-input v-model="form.title" placeholder="请输入药品名称" />
        </el-form-item>
        <el-form-item label="服用频次" required>
          <el-select v-model="form.repeatType">
            <el-option label="每日一次" value="DAILY" />
            <el-option label="每日两次" value="DAILY_TWICE" />
            <el-option label="每日三次" value="DAILY_THREE" />
            <el-option label="每周一次" value="WEEKLY" />
            <el-option label="每月一次" value="MONTHLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="服药时间" required>
          <el-date-picker v-model="form.remindTime" type="datetime" placeholder="选择服药时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="用量说明" required>
          <el-input v-model="form.content" type="textarea" placeholder="例如：每次1片，饭后服用" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="saveMedicine">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="用药计划详情" width="650px">
      <template v-if="current">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="老人">{{ current.elderName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="药品名称">{{ current.title || '—' }}</el-descriptions-item>
          <el-descriptions-item label="服用频次">{{ repeatLabel(current.repeatType) }}</el-descriptions-item>
          <el-descriptions-item label="服药时间">{{ current.remindTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="用量说明">{{ current.content || '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusLabel(current.status) }}</el-descriptions-item>
          <el-descriptions-item label="已服次数">{{ current.completedCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="漏服次数">{{ current.missedCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="连续未确认">{{ current.consecutiveMissedCount || 0 }}次</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ current.createTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="最近完成">{{ current.lastCompletedAt || '—' }}</el-descriptions-item>
          <el-descriptions-item label="推送状态">{{ pushStatusLabel(current.pushStatus) }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="executions.length" class="executions-section">
          <h4 style="margin:16px 0 12px">服药执行记录</h4>
          <el-table :data="executions" size="small" border>
            <el-table-column prop="scheduledTime" label="计划时间" width="170" />
            <el-table-column label="执行状态" width="120">
              <template #default="{ row }">
                <el-tag :type="actionTag(row.action)">{{ actionLabel(row.action) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="actionTime" label="操作时间" width="170" />
            <el-table-column prop="operatorRole" label="操作角色" width="100" />
          </el-table>
        </div>
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
const elderInfoId = ref(null)
const filterStatus = ref(null)
const elders = ref([])
const list = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const addVisible = ref(false)
const detailVisible = ref(false)
const current = ref(null)
const isEdit = ref(false)
const executions = ref([])

const form = reactive({
  id: null,
  elderInfoId: null,
  title: '',
  content: '',
  repeatType: 'DAILY',
  remindTime: null
})

const repeatMap = {
  ONCE: '单次',
  DAILY: '每日一次',
  DAILY_TWICE: '每日两次',
  DAILY_THREE: '每日三次',
  WEEKLY: '每周一次',
  MONTHLY: '每月一次'
}
const statusMap = { 0: '待提醒', 1: '已完成', 2: '已过期' }
const pushStatusMap = { PENDING: '待推送', PUSHED: '已推送', FAILED: '推送失败' }
const actionMap = { COMPLETED: '已完成', SNOOZED: '已延迟', SKIPPED: '已跳过', MISSED: '已错过' }

function repeatLabel(t) { return repeatMap[t] || '每日一次' }
function statusLabel(s) { return statusMap[s] || s || '—' }
function pushStatusLabel(s) { return pushStatusMap[s] || s || '—' }
function actionLabel(a) { return actionMap[a] || a || '—' }

function statusTag(s) {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[s] || 'info'
}
function actionTag(a) {
  const map = { COMPLETED: 'success', SNOOZED: 'warning', SKIPPED: 'info', MISSED: 'danger' }
  return map[a] || 'info'
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
      elderInfoId: elderInfoId.value || undefined,
      remindType: 'MEDICINE',
      status: filterStatus.value !== null ? filterStatus.value : undefined
    }
    const r = await adminApi.listReminders(params)
    list.value = r.data || r.list || []
    total.value = r.total || 0
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    elderInfoId: null,
    title: '',
    content: '',
    repeatType: 'DAILY',
    remindTime: null
  })
  addVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    elderInfoId: row.elderInfoId,
    title: row.title,
    content: row.content,
    repeatType: row.repeatType || 'DAILY',
    remindTime: row.remindTime
  })
  addVisible.value = true
}

function closeDialog() {
  addVisible.value = false
}

async function saveMedicine() {
  if (!form.elderInfoId || !form.title || !form.remindTime) {
    ElMessage.warning('请填写必填项')
    return
  }
  if (isEdit.value) {
    await adminApi.updateReminder(form.id, form)
    ElMessage.success('用药计划已更新')
  } else {
    await adminApi.createReminder({ ...form, remindType: 'MEDICINE' })
    ElMessage.success('用药计划已添加')
  }
  addVisible.value = false
  loadList()
}

async function openDetail(row) {
  current.value = row
  detailVisible.value = true
  await loadExecutions(row.id)
}

async function loadExecutions(reminderId) {
  try {
    const r = await adminApi.getReminderExecutions(reminderId)
    executions.value = r.data || r || []
  } catch {
    executions.value = []
  }
}

async function removeRow(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.title}」用药计划吗？`, '提示', { type: 'warning' })
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
.executions-section { border-top:1px solid #ebeef5; }
</style>
<template>
  <div class="admin-permission">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="filter-group">
          <el-input v-model="keyword" placeholder="搜索用户名、昵称" clearable style="width:260px" @keyup.enter="loadList" @clear="loadList" />
          <el-select v-model="filterRole" clearable placeholder="角色" style="width:140px" @change="loadList">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="运营管理员" value="OPERATOR" />
            <el-option label="普通管理员" value="ADMIN" />
          </el-select>
          <el-select v-model="filterStatus" clearable placeholder="状态" style="width:120px" @change="loadList">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </div>
        <div class="action-group">
          <el-button type="primary" @click="openAddDialog">新增管理员</el-button>
          <el-button @click="loadList">刷新</el-button>
        </div>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="角色" width="130">
          <template #default="{ row }">
            <el-tag :type="roleTag(row.role)">{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="managedElderCount" label="管辖老人" width="120" align="center">
          <template #default="{ row }">{{ row.managedElderCount || 0 }}人</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.activeStatus" :active-text="'启用'" :inactive-text="'禁用'"
              active-color="#67c23a" inactive-color="#f56c6c" @change="toggleStatus(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">详情</el-button>
            <el-button size="small" type="warning" @click="openResetPassword(row)">重置密码</el-button>
            <el-button size="small" type="primary" @click="openRoleConfig(row)">权限配置</el-button>
            <el-button size="small" type="danger" @click="removeRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pager" v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next"
        @current-change="loadList" @size-change="loadList" />
    </el-card>

    <el-dialog v-model="addVisible" title="新增管理员" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称" required>
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.role">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="运营管理员" value="OPERATOR" />
            <el-option label="普通管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="初始密码" required>
          <el-input v-model="form.password" type="password" placeholder="请设置初始密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAdmin">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="管理员详情" width="600px">
      <template v-if="current">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ current.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ current.nickname }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ current.phone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ roleLabel(current.role) }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ current.status === 1 ? '启用' : '禁用' }}</el-descriptions-item>
          <el-descriptions-item label="管辖老人">{{ current.managedElderCount || 0 }}人</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ current.createTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="最近登录">{{ current.lastLoginTime || '—' }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleConfigVisible" title="权限配置" width="700px">
      <template v-if="current">
        <div class="role-config">
          <div class="config-section">
            <h4>基本信息</h4>
            <el-form :model="roleForm" label-width="80px">
              <el-form-item label="角色">
                <el-select v-model="roleForm.role">
                  <el-option label="超级管理员" value="SUPER_ADMIN" />
                  <el-option label="运营管理员" value="OPERATOR" />
                  <el-option label="普通管理员" value="ADMIN" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>

          <div class="config-section">
            <h4>菜单权限</h4>
            <el-checkbox-group v-model="roleForm.permissions">
              <el-checkbox label="device" border>设备管理</el-checkbox>
              <el-checkbox label="health" border>健康数据</el-checkbox>
              <el-checkbox label="warning" border>预警中心</el-checkbox>
              <el-checkbox label="service" border>服务管理</el-checkbox>
              <el-checkbox label="content" border>内容运营</el-checkbox>
              <el-checkbox label="system" border>系统管理</el-checkbox>
            </el-checkbox-group>
          </div>

          <div class="config-section">
            <h4>可管辖老人</h4>
            <el-select v-model="roleForm.managedElders" multiple filterable placeholder="选择可管辖的老人" style="width:100%">
              <el-option v-for="e in elders" :key="e.id" :label="e.realName" :value="e.id" />
            </el-select>
            <el-button type="text" size="small" @click="selectAllElders">全选</el-button>
            <el-button type="text" size="small" @click="clearElders">清空</el-button>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="roleConfigVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRoleConfig">保存配置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi, familyApi } from '@/api'

const keyword = ref('')
const filterRole = ref('')
const filterStatus = ref(null)
const list = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const elders = ref([])

const addVisible = ref(false)
const detailVisible = ref(false)
const roleConfigVisible = ref(false)
const current = ref(null)

const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  role: 'ADMIN',
  password: ''
})

const roleForm = reactive({
  role: 'ADMIN',
  permissions: [],
  managedElders: []
})

const roleMap = { SUPER_ADMIN: '超级管理员', OPERATOR: '运营管理员', ADMIN: '普通管理员' }

function roleLabel(r) { return roleMap[r] || r || '—' }
function roleTag(r) {
  const map = { SUPER_ADMIN: 'danger', OPERATOR: 'warning', ADMIN: 'primary' }
  return map[r] || 'info'
}

async function loadElders() {
  const r = await adminApi.getElders()
  elders.value = r.data || r || []
}

async function loadList() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      role: filterRole.value || undefined,
      status: filterStatus.value !== null ? filterStatus.value : undefined
    }
    const r = await adminApi.listAdminUsers(params)
    // 后端返回结构：{ code, data:{ list:[...], total } }
    list.value = (r.data && r.data.list) || r.list || (Array.isArray(r.data) ? r.data : []) || []
    total.value = (r.data && r.data.total) || r.total || 0
    list.value.forEach(u => { u.activeStatus = u.status === 1 })
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  Object.assign(form, { username: '', nickname: '', phone: '', role: 'ADMIN', password: '' })
  addVisible.value = true
}

async function saveAdmin() {
  if (!form.username || !form.nickname || !form.password) {
    ElMessage.warning('请填写必填项')
    return
  }
  await adminApi.createAdminUser(form)
  ElMessage.success('管理员已创建')
  addVisible.value = false
  loadList()
}

function openDetail(row) {
  current.value = row
  detailVisible.value = true
}

async function toggleStatus(row) {
  const status = row.activeStatus ? 1 : 0
  await adminApi.updateAdminStatus(row.id, status)
  ElMessage.success(`管理员"${row.username}"已${status === 1 ? '启用' : '禁用'}`)
  loadList()
}

async function openResetPassword(row) {
  try {
    const result = await ElMessageBox.prompt('请输入新密码', '重置密码', {
      confirmButtonText: '确认', cancelButtonText: '取消', inputType: 'password'
    })
    await adminApi.resetAdminPassword(row.id, result.value)
    ElMessage.success('密码已重置')
  } catch {}
}

function openRoleConfig(row) {
  current.value = row
  Object.assign(roleForm, {
    role: row.role || 'ADMIN',
    permissions: row.permissions ? row.permissions.split(',') : [],
    managedElders: row.managedElders || []
  })
  roleConfigVisible.value = true
}

function selectAllElders() {
  roleForm.managedElders = elders.value.map(e => e.elderInfoId)
}
function clearElders() {
  roleForm.managedElders = []
}

async function saveRoleConfig() {
  await adminApi.updateAdminRole(current.value.id, roleForm)
  ElMessage.success('权限配置已更新')
  roleConfigVisible.value = false
  loadList()
}

async function removeRow(row) {
  try {
    await ElMessageBox.confirm(`确定删除管理员「${row.username}」吗？`, '提示', { type: 'warning' })
  } catch { return }
  await adminApi.deleteAdminUser(row.id)
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
.role-config { padding:8px; }
.config-section { margin-bottom:20px; }
.config-section h4 { margin:0 0 12px; font-size:15px; color:#303133; }
</style>
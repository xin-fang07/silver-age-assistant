<template>
  <div class="elder-admin">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="姓名/昵称/电话" style="width:220px" clearable @keyup.enter="load" />
        <el-select v-model="query.status" placeholder="状态" clearable style="width:140px">
          <el-option label="正常" :value="1" />
          <el-option label="已注销" :value="0" />
        </el-select>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
      <el-table :data="rows" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="gender" label="性别" width="70">
  <template #default="{ row }">
    {{ row.gender === 0 || row.gender === '女' ? '女' : '男' }}
  </template>
</el-table-column>
        <el-table-column prop="age" label="年龄" width="70" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="healthStatus" label="健康状态" />
        <el-table-column prop="boundFamilyCount" label="绑定家属" width="90" />
        <el-table-column prop="deviceCount" label="设备" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '已注销' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="老人档案详情" width="780px">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ detail.realName }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detail.nickname }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ detail.gender }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ detail.age }}</el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ detail.birthDate }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ detail.phone }}</el-descriptions-item>
          <el-descriptions-item label="健康状态">{{ detail.healthStatus }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ detail.address }}</el-descriptions-item>
        </el-descriptions>
        <h4>已绑定家属</h4>
        <el-table :data="detail.families" size="small" border>
          <el-table-column prop="familyUsername" label="家属账号" />
          <el-table-column prop="familyRealName" label="姓名" />
          <el-table-column prop="relation" label="关系" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '生效' : '已解绑' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button link type="danger" @click="removeBinding(row)">解绑</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top:10px">
          <el-button type="primary" size="small" @click="openBind(detail)">添加家属绑定</el-button>
        </div>
        <h4>关联设备</h4>
        <el-table :data="detail.devices" size="small" border>
          <el-table-column prop="deviceName" label="设备名" />
          <el-table-column prop="deviceType" label="类型" />
          <el-table-column prop="vendor" label="厂商" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '在线' : '离线' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑老人档案" width="640px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="姓名"><el-input v-model="editForm.realName" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="editForm.nickname" /></el-form-item>
        <el-form-item label="性别">
          <el-select v-model="editForm.gender">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄"><el-input-number v-model="editForm.age" :min="0" :max="130" /></el-form-item>
        <el-form-item label="出生日期"><el-date-picker v-model="editForm.birthDate" value-format="YYYY-MM-DD" type="date" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="editForm.phone" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="editForm.address" /></el-form-item>
        <el-form-item label="健康状态"><el-input v-model="editForm.healthStatus" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status"><el-option label="正常" :value="1" /><el-option label="已注销" :value="0" /></el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bindVisible" title="添加家属绑定" width="480px">
      <el-form :model="bindForm" label-width="90px">
        <el-form-item label="家属账号">
          <el-select v-model="bindForm.familyUserId" filterable placeholder="选择家属账号">
            <el-option v-for="f in familyOptions" :key="f.familyUserId" :label="(f.username || '') + (f.realName ? ('(' + f.realName + ')') : '')" :value="f.familyUserId" />
          </el-select>
        </el-form-item>
        <el-form-item label="关系"><el-input v-model="bindForm.relation" placeholder="如 子女/配偶" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBind">绑定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { elderAdminApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const rows = ref([])
const loading = ref(false)
const query = reactive({ keyword: '', status: null })

const detailVisible = ref(false)
const detail = ref(null)
const editVisible = ref(false)
const editForm = reactive({})
const bindVisible = ref(false)
const bindForm = reactive({ elderInfoId: null, familyUserId: null, relation: '子女' })
const familyOptions = ref([])

async function load() {
  loading.value = true
  try {
    const r = await elderAdminApi.list({ keyword: query.keyword || undefined, status: query.status })
    rows.value = r.data || []
  } finally {
    loading.value = false
  }
}
function reset() {
  query.keyword = ''
  query.status = null
  load()
}

async function openDetail(row) {
  const r = await elderAdminApi.detail(row.id)
  detail.value = r.data
  detailVisible.value = true
}
function openEdit(row) {
  Object.assign(editForm, JSON.parse(JSON.stringify(row)))
  editForm.gender = row.gender === 0 || row.gender === '女' ? '女' : '男'
  editVisible.value = true
}
async function submitEdit() {
  const data = {
    id: editForm.id,
    realName: editForm.realName,
    nickname: editForm.nickname,
    gender: editForm.gender === '女' ? 0 : 1,
    age: editForm.age,
    birthDate: editForm.birthDate,
    emergencyPhone: editForm.phone,
    address: editForm.address,
    healthStatus: editForm.healthStatus,
    status: editForm.status
  }
  await elderAdminApi.update(data.id, data)
  ElMessage.success('已保存')
  editVisible.value = false
  load()
}
async function remove(row) {
  await ElMessageBox.confirm('确认删除该老人档案？将同时解除绑定与设备关联。', '提示', { type: 'warning' })
  await elderAdminApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

function openBind(d) {
  bindForm.elderInfoId = d.id
  bindForm.familyUserId = null
  bindForm.relation = '子女'
  bindVisible.value = true
  loadFamilyOptions()
}
async function loadFamilyOptions() {
  const r = await elderAdminApi.familyOptions()
  familyOptions.value = r.data || []
}
async function submitBind() {
  if (!bindForm.familyUserId) {
    ElMessage.warning('请选择家属账号')
    return
  }
  await elderAdminApi.addBinding({
    elderInfoId: bindForm.elderInfoId,
    familyUserId: bindForm.familyUserId,
    relation: bindForm.relation
  })
  ElMessage.success('已绑定')
  bindVisible.value = false
  const r = await elderAdminApi.detail(bindForm.elderInfoId)
  detail.value = r.data
}
async function removeBinding(b) {
  await ElMessageBox.confirm('确认解除该绑定？', '提示', { type: 'warning' })
  await elderAdminApi.removeBinding(b.bindingId)
  ElMessage.success('已解除')
  const r = await elderAdminApi.detail(detail.value.id)
  detail.value = r.data
}

onMounted(load)
</script>

<style scoped>
.elder-admin { padding: 0; }
.toolbar { display: flex; gap: 10px; margin-bottom: 14px; flex-wrap: wrap; }
h4 { margin: 16px 0 8px; }
</style>

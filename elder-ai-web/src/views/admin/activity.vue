<template>
  <div class="admin-activity">
    <div class="toolbar">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="类型">
          <el-select v-model="query.type" clearable placeholder="全部" style="width:130px" @change="load">
            <el-option label="线上资讯" value="ONLINE" />
            <el-option label="线下活动" value="OFFLINE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width:130px" @change="load">
            <el-option label="已发布" :value="1" />
            <el-option label="草稿" :value="0" />
            <el-option label="已下架" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="标题关键词" clearable @keyup.enter="load" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load">查询</el-button>
          <el-button type="success" @click="openCreate">新增活动</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column label="类型" width="100">
        <template #default="{row}">
          <el-tag :type="row.type === 'OFFLINE' ? 'warning' : 'success'">
            {{ row.type === 'OFFLINE' ? '线下活动' : '线上资讯' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{row}">
          <el-tag :type="row.status === 1 ? 'success' : (row.status === 0 ? 'info' : 'danger')">
            {{ row.status === 1 ? '已发布' : (row.status === 0 ? '草稿' : '已下架') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="signupCount" label="报名数" width="80" />
      <el-table-column label="创建时间" width="170">
        <template #default="{row}">{{ fmt(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{row}">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="warning" plain v-if="row.type === 'OFFLINE'" @click="openSignups(row)">报名管理</el-button>
          <el-button size="small" type="danger" plain @click="removeRow(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
      layout="total, prev, pager, next" @current-change="load" style="margin-top:16px" />

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑活动' : '新增活动'" width="640px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="活动标题" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-radio-group v-model="form.type">
            <el-radio label="ONLINE">线上资讯</el-radio>
            <el-radio label="OFFLINE">线下活动</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="封面图">
          <el-upload :show-file-list="false" :before-upload="beforeImg" :http-request="uploadCover" accept="image/*">
            <el-button size="small">上传封面</el-button>
          </el-upload>
          <img v-if="form.coverImage" :src="assetUrl(form.coverImage)" class="cover-prev" alt="封面预览" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="活动介绍 / 资讯正文" />
        </el-form-item>
        <template v-if="form.type === 'OFFLINE'">
          <el-form-item label="活动地点">
            <el-input v-model="form.location" placeholder="如：社区活动中心 3 楼" />
          </el-form-item>
          <el-form-item label="开始时间">
            <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择开始时间" />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择结束时间" />
          </el-form-item>
          <el-form-item label="报名截止">
            <el-date-picker v-model="form.signupDeadline" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择报名截止时间" />
          </el-form-item>
          <el-form-item label="名额上限">
            <el-input-number v-model="form.capacity" :min="0" />
            <span class="hint">0 表示不限</span>
          </el-form-item>
        </template>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">已发布</el-radio>
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="2">已下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="signupDialog" title="报名管理" width="720px">
      <el-table :data="signupList" v-loading="signupLoading" border>
        <el-table-column prop="elderName" label="老人" width="100" />
        <el-table-column prop="contactPhone" label="联系手机" width="140" />
        <el-table-column prop="remark" label="备注" min-width="120" />
        <el-table-column label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status === 0 ? 'success' : 'info'">{{ row.status === 0 ? '已报名' : '已取消' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名时间" width="160">
          <template #default="{row}">{{ fmt(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{row}">
            <el-button v-if="row.status === 0" size="small" type="danger" plain @click="setSignupStatus(row, 1)">取消</el-button>
            <el-button v-else size="small" type="success" plain @click="setSignupStatus(row, 0)">恢复</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { activityApi, activityAdminApi } from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ type: '', status: null, keyword: '', pageNum: 1, pageSize: 10 })

const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = ref(emptyForm())

const signupDialog = ref(false)
const signupLoading = ref(false)
const signupList = ref([])
const currentActivityId = ref(null)

import { assetUrl } from '@/utils/asset'

function fmt(s) {
  if (!s) return ''
  return s.replace('T', ' ').slice(0, 16)
}
function emptyForm() {
  return { title: '', type: 'ONLINE', coverImage: '', content: '', location: '',
    startTime: null, endTime: null, signupDeadline: null, capacity: 0, status: 1 }
}

async function load() {
  loading.value = true
  try {
    const params = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.type) params.type = query.type
    if (query.status != null) params.status = query.status
    if (query.keyword) params.keyword = query.keyword
    const r = await activityAdminApi.list(params)
    list.value = r.data || []
    total.value = r.total || 0
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  editingId.value = null
  form.value = emptyForm()
  dialogVisible.value = true
}
function openEdit(row) {
  isEdit.value = true
  editingId.value = row.id
  form.value = { ...row }
  dialogVisible.value = true
}
function beforeImg(file) {
  if (!file.type.startsWith('image/')) { ElMessage.error('只能上传图片文件'); return false }
  return true
}
async function uploadCover({ file }) {
  const r = await activityApi.uploadImage(file)
  const url = r.data
  if (!url) { ElMessage.error('上传失败'); return }
  form.value.coverImage = url
  ElMessage.success('已上传')
}
async function save() {
  if (!form.value.title) { ElMessage.warning('请填写标题'); return }
  if (!form.value.type) { ElMessage.warning('请选择类型'); return }
  if (!form.value.content) { ElMessage.warning('请填写内容'); return }
  saving.value = true
  try {
    const data = { ...form.value }
    if (isEdit.value) {
      await activityAdminApi.update({ ...data, id: editingId.value })
    } else {
      await activityAdminApi.create(data)
    }
    ElMessage.success('已保存')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}
async function removeRow(row) {
  try {
    await ElMessageBox.confirm('确定删除该活动吗？删除后不可恢复。', '提示', { type: 'warning' })
  } catch { return }
  await activityAdminApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}
async function openSignups(row) {
  currentActivityId.value = row.id
  signupDialog.value = true
  await loadSignups()
}
async function loadSignups() {
  signupLoading.value = true
  try {
    const r = await activityAdminApi.signups(currentActivityId.value, { pageNum: 1, pageSize: 200 })
    signupList.value = r.data || []
  } finally {
    signupLoading.value = false
  }
}
async function setSignupStatus(row, status) {
  await activityAdminApi.updateSignup(row.id, status)
  ElMessage.success('已更新')
  loadSignups()
  load()
}
onMounted(load)
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
.cover-prev { width: 120px; height: 80px; object-fit: cover; border-radius: 6px; margin-left: 12px; vertical-align: middle; }
.hint { color: #94a3b8; font-size: 12px; margin-left: 10px; }
</style>

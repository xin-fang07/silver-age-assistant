<template>
  <div class="medical-record-page">
    <div class="page-title">
      <el-icon><Document /></el-icon>
      <span>医疗档案</span>
    </div>

    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-select v-model="elderInfoId" placeholder="选择老人" clearable filterable style="width:220px" @change="loadList">
          <el-option v-for="e in elders" :key="e.elderInfoId" :label="e.realName" :value="e.elderInfoId" />
        </el-select>
        <el-button type="primary" @click="openCreate">新增就诊记录</el-button>
        <el-button @click="loadList">刷新</el-button>
      </div>
    </el-card>

    <div v-loading="loading" class="record-list">
      <el-empty v-if="!records.length" description="暂无就诊记录，点击右上角新增" />
      <el-card v-for="rec in records" :key="rec.id" shadow="hover" class="record-card">
        <div class="record-head">
          <div>
            <div class="hospital">{{ rec.hospitalName || '未填写医院' }}</div>
            <div class="sub">
              <template v-if="rec.department">{{ rec.department }}</template>
              <template v-if="rec.doctorName"> · {{ rec.doctorName }}</template>
              <template v-if="rec.elderName"> · {{ rec.elderName }}</template>
            </div>
          </div>
          <div class="date">{{ rec.visitDate || '—' }}</div>
        </div>

        <el-descriptions :column="1" border class="rec-desc">
          <el-descriptions-item label="诊断">{{ rec.diagnosis || '—' }}</el-descriptions-item>
          <el-descriptions-item label="医嘱/处方">{{ rec.prescription || '—' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ rec.remark || '—' }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="rec.attachments && rec.attachments.length" class="thumbs">
          <el-image v-for="(u,i) in rec.attachments" :key="i"
            :src="assetUrl(u)"
            :preview-src-list="rec.attachments.map(assetUrl)"
            :initial-index="i" fit="cover" class="thumb" />
        </div>

        <div class="rec-actions">
          <el-button size="small" @click="openEdit(rec)">编辑</el-button>
          <el-button size="small" type="danger" @click="removeRec(rec)">删除</el-button>
        </div>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑就诊记录' : '新增就诊记录'" width="640px">
      <el-form :model="form" label-width="92px">
        <el-form-item label="关联老人">
          <el-select v-model="form.elderInfoId" placeholder="请选择老人" :disabled="isEdit" style="width:100%">
            <el-option v-for="e in elders" :key="e.elderInfoId" :label="e.realName" :value="e.elderInfoId" />
          </el-select>
        </el-form-item>
        <el-form-item label="就诊医院"><el-input v-model="form.hospitalName" placeholder="如：市第一人民医院" /></el-form-item>
        <el-form-item label="就诊科室"><el-input v-model="form.department" placeholder="如：心内科" /></el-form-item>
        <el-form-item label="主治医生"><el-input v-model="form.doctorName" /></el-form-item>
        <el-form-item label="就诊日期">
          <el-date-picker v-model="form.visitDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="诊断结论"><el-input v-model="form.diagnosis" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="医嘱/处方"><el-input v-model="form.prescription" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
        <el-form-item label="病历/报告">
          <div>
            <el-upload :show-file-list="false" :http-request="uploadImage" accept="image/*" :before-upload="beforeImg">
              <el-button type="primary" size="small">+ 上传图片（病历 / 检查报告）</el-button>
            </el-upload>
            <div v-if="form.attachments.length" class="thumbs" style="margin-top:10px">
              <div v-for="(u,i) in form.attachments" :key="i" class="thumb-wrap">
                <el-image :src="assetUrl(u)" :preview-src-list="form.attachments.map(assetUrl)" :initial-index="i" fit="cover" class="thumb" />
                <el-button class="del-btn" size="small" type="danger" plain @click="removeImg(i)">删除</el-button>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { familyApi, medicalRecordApi } from '@/api/index'

const elders = ref([])
const elderInfoId = ref(null)
const records = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const editingId = ref(null)
const form = ref(emptyForm())
const fileList = ref([])

function emptyForm() {
  return { elderInfoId: null, hospitalName: '', department: '', doctorName: '', diagnosis: '', visitDate: '', prescription: '', remark: '', attachments: [] }
}

import { assetUrl } from '@/utils/asset'

async function loadElders() {
  const r = await familyApi.myElders()
  elders.value = r.data || r || []
}
async function loadList() {
  loading.value = true
  try {
    const r = await medicalRecordApi.list({ elderInfoId: elderInfoId.value || undefined, pageNum: 1, pageSize: 100 })
    records.value = r.data || []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  editingId.value = null
  form.value = emptyForm()
  form.value.elderInfoId = elderInfoId.value || null
  fileList.value = []
  dialogVisible.value = true
}
function openEdit(rec) {
  isEdit.value = true
  editingId.value = rec.id
  form.value = { ...rec, attachments: rec.attachments ? [...rec.attachments] : [] }
  dialogVisible.value = true
}

function beforeImg(file) {
  if (!file.type.startsWith('image/')) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  return true
}
async function uploadImage({ file }) {
  const r = await medicalRecordApi.uploadImage(file)
  const url = r.data
  if (!url) { ElMessage.error('上传失败'); return }
  form.value.attachments.push(url)
  ElMessage.success('已上传')
}
function removeImg(i) {
  form.value.attachments.splice(i, 1)
}

async function save() {
  if (!form.value.elderInfoId) { ElMessage.warning('请选择关联老人'); return }
  saving.value = true
  try {
    if (isEdit.value) {
      await medicalRecordApi.update({ ...form.value, id: editingId.value })
      ElMessage.success('已更新')
    } else {
      await medicalRecordApi.create(form.value)
      ElMessage.success('已添加')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    saving.value = false
  }
}

async function removeRec(rec) {
  try {
    await ElMessageBox.confirm('确定删除该就诊记录吗？删除后不可恢复。', '提示', { type: 'warning' })
  } catch { return }
  await medicalRecordApi.remove(rec.id)
  ElMessage.success('已删除')
  loadList()
}

onMounted(() => { loadElders(); loadList() })
</script>

<style scoped>
.page-title { display:flex; align-items:center; gap:8px; font-size:20px; font-weight:600; color:#1f2d3d; margin-bottom:16px; }
.filter-row { display:flex; gap:12px; align-items:center; flex-wrap:wrap; }
.record-list { margin-top:16px; display:grid; grid-template-columns:repeat(auto-fill, minmax(340px, 1fr)); gap:16px; }
.record-card { border-radius:12px; }
.record-head { display:flex; justify-content:space-between; align-items:flex-start; gap:8px; }
.hospital { font-size:16px; font-weight:600; color:#1f2d3d; }
.sub { color:#909399; font-size:13px; margin-top:2px; }
.date { color:#409eff; font-weight:600; white-space:nowrap; }
.rec-desc { margin:12px 0; }
.thumbs { display:flex; gap:8px; flex-wrap:wrap; }
.thumb { width:92px; height:92px; border-radius:8px; }
.thumb-wrap { position:relative; }
.del-btn { margin-top:4px; display:block; }
.rec-actions { display:flex; gap:8px; justify-content:flex-end; margin-top:8px; }
</style>

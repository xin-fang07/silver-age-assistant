<template>
  <div class="activity-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="活动广场" name="square">
        <div class="filter-bar">
          <el-radio-group v-model="typeFilter" @change="loadList">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="ONLINE">线上资讯</el-radio-button>
            <el-radio-button label="OFFLINE">线下活动</el-radio-button>
          </el-radio-group>
        </div>
        <div v-loading="loading" class="card-grid">
          <el-empty v-if="!loading && list.length === 0" description="暂无活动" />
          <el-card v-for="item in list" :key="item.id" class="act-card" shadow="hover" @click="openDetail(item)">
            <img v-if="item.coverImage" :src="assetUrl(item.coverImage)" class="cover" alt="封面" />
            <div class="act-body">
              <div class="act-title">
                <span class="tt">{{ item.title }}</span>
                <el-tag size="small" :type="item.type === 'OFFLINE' ? 'warning' : 'success'">
                  {{ item.type === 'OFFLINE' ? '线下活动' : '线上资讯' }}
                </el-tag>
              </div>
              <p class="summary">{{ summary(item.content) }}</p>
              <div v-if="item.type === 'OFFLINE'" class="meta">
                <div v-if="item.location">地点：{{ item.location }}</div>
                <div v-if="item.startTime">开始：{{ fmt(item.startTime) }}</div>
                <div>报名：{{ item.capacity > 0 ? item.signupCount + '/' + item.capacity : item.signupCount + ' 人' }}</div>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="我的报名" name="mine">
        <div v-loading="loadingMine">
          <el-empty v-if="!loadingMine && myList.length === 0" description="还没有报名任何活动" />
          <el-card v-for="s in myList" :key="s.id" class="signup-card" shadow="hover">
            <div class="signup-row">
              <div>
                <div class="tt">{{ s.activityTitle }}</div>
                <div class="sub">老人：{{ s.elderName || '—' }} ｜ 手机：{{ s.contactPhone || '—' }}</div>
                <el-tag size="small" :type="s.status === 0 ? 'success' : 'info'">
                  {{ s.status === 0 ? '已报名' : '已取消' }}
                </el-tag>
              </div>
              <el-button v-if="s.status === 0" type="danger" plain size="small" @click="cancelSignup(s)">取消报名</el-button>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="活动详情" width="640px">
      <div v-if="detailData">
        <img v-if="detailData.coverImage" :src="assetUrl(detailData.coverImage)" class="detail-cover" alt="封面" />
        <div class="detail-head">
          <h3>{{ detailData.title }}</h3>
          <el-tag :type="detailData.type === 'OFFLINE' ? 'warning' : 'success'">
            {{ detailData.type === 'OFFLINE' ? '线下活动' : '线上资讯' }}
          </el-tag>
        </div>
        <div v-if="detailData.type === 'OFFLINE'" class="detail-meta">
          <div v-if="detailData.location">地点：{{ detailData.location }}</div>
          <div v-if="detailData.startTime">开始：{{ fmt(detailData.startTime) }}</div>
          <div v-if="detailData.endTime">结束：{{ fmt(detailData.endTime) }}</div>
          <div v-if="detailData.signupDeadline">报名截止：{{ fmt(detailData.signupDeadline) }}</div>
          <div>名额：{{ detailData.capacity > 0 ? detailData.signupCount + '/' + detailData.capacity : '不限' }}</div>
        </div>
        <div class="detail-content">{{ detailData.content }}</div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="detailData && detailData.type === 'OFFLINE'" type="primary" @click="openSignup(detailData)">我要报名</el-button>
      </template>
    </el-dialog>

    <!-- 报名 -->
    <el-dialog v-model="signupVisible" title="活动报名" width="520px">
      <el-form :model="signupForm" label-width="80px">
        <el-form-item label="活动">
          <span>{{ signupForm.activityTitle }}</span>
        </el-form-item>
        <el-form-item label="为老人" required>
          <el-select v-model="signupForm.elderInfoId" placeholder="选择老人" style="width: 100%">
            <el-option v-for="e in elders" :key="e.elderInfoId" :label="e.realName" :value="e.elderInfoId" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系手机" required>
          <el-input v-model="signupForm.contactPhone" maxlength="20" placeholder="请输入联系手机" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="signupForm.remark" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="signupVisible = false">取消</el-button>
        <el-button type="primary" :loading="signing" @click="submitSignup">提交报名</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { activityApi, familyApi } from '@/api'

const activeTab = ref('square')
const typeFilter = ref('')
const loading = ref(false)
const list = ref([])
const loadingMine = ref(false)
const myList = ref([])
const elders = ref([])

const detailVisible = ref(false)
const detailData = ref(null)
const signupVisible = ref(false)
const signing = ref(false)
const signupForm = ref({ activityId: null, activityTitle: '', elderInfoId: null, contactPhone: '', remark: '' })

import { assetUrl } from '@/utils/asset'

function fmt(s) {
  if (!s) return ''
  return s.replace('T', ' ').slice(0, 16)
}
function summary(text) {
  if (!text) return ''
  const t = String(text).replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
  return t.length > 60 ? t.slice(0, 60) + '…' : t
}

async function loadList() {
  loading.value = true
  try {
    const r = await activityApi.list({ type: typeFilter.value || undefined, pageNum: 1, pageSize: 100 })
    list.value = r.data || []
  } finally {
    loading.value = false
  }
}

async function loadMySignups() {
  loadingMine.value = true
  try {
    const r = await activityApi.mySignups({ pageNum: 1, pageSize: 100 })
    myList.value = r.data || []
  } finally {
    loadingMine.value = false
  }
}

async function loadElders() {
  const r = await familyApi.myElders()
  elders.value = r.data || r || []
}

function openDetail(item) {
  detailData.value = item
  detailVisible.value = true
}

function openSignup(item) {
  if (elders.value.length === 0) {
    ElMessage.warning('请先在「老人绑定管理」中绑定老人')
    return
  }
  signupForm.value = { activityId: item.id, activityTitle: item.title, elderInfoId: null, contactPhone: '', remark: '' }
  signupVisible.value = true
}

async function submitSignup() {
  if (!signupForm.value.elderInfoId) { ElMessage.warning('请选择老人'); return }
  if (!signupForm.value.contactPhone) { ElMessage.warning('请填写联系手机'); return }
  signing.value = true
  try {
    await activityApi.signup({ ...signupForm.value })
    ElMessage.success('报名成功')
    signupVisible.value = false
    detailVisible.value = false
    loadList()
    loadMySignups()
  } finally {
    signing.value = false
  }
}

async function cancelSignup(row) {
  try {
    await ElMessageBox.confirm('确定取消该报名吗？', '提示', { type: 'warning' })
  } catch { return }
  await activityApi.cancelSignup(row.id)
  ElMessage.success('已取消')
  loadMySignups()
}

onMounted(() => { loadList(); loadElders(); loadMySignups() })
</script>

<style scoped>
.filter-bar { margin-bottom: 16px; }
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.act-card { cursor: pointer; }
.act-card .cover { width: 100%; height: 150px; object-fit: cover; border-radius: 8px; margin-bottom: 10px; }
.act-title { display: flex; justify-content: space-between; align-items: center; gap: 8px; }
.act-title .tt { font-weight: 600; font-size: 16px; }
.summary { color: #64748b; font-size: 13px; margin: 8px 0; line-height: 1.5;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.meta { color: #475569; font-size: 13px; display: flex; flex-direction: column; gap: 4px; }
.detail-cover { width: 100%; max-height: 240px; object-fit: cover; border-radius: 8px; margin-bottom: 12px; }
.detail-head { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.detail-head h3 { margin: 0; }
.detail-meta { background: #f8fafc; padding: 12px; border-radius: 8px; font-size: 14px;
  color: #475569; display: flex; flex-direction: column; gap: 6px; margin-bottom: 12px; }
.detail-content { white-space: pre-wrap; line-height: 1.7; color: #1f2937; }
.signup-card { margin-bottom: 12px; }
.signup-row { display: flex; justify-content: space-between; align-items: center; }
.signup-row .tt { font-weight: 600; font-size: 15px; }
.signup-row .sub { color: #64748b; font-size: 13px; margin: 4px 0; }
</style>
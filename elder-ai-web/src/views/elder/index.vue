<template>
  <div class="elder-page">
    <div class="page-head">
      <h2 class="page-title">我的老人</h2>
      <p class="page-sub">已绑定的老人档案，仅供查看，不可修改。</p>
    </div>

    <div v-loading="loading" class="elder-list">
      <el-empty v-if="!loading && elders.length === 0" description="暂无绑定的老人" />
      <el-card
        v-for="e in elders"
        :key="e.elderInfoId"
        class="elder-card"
        shadow="hover"
        @click="openDetail(e.elderInfoId)"
      >
        <div class="elder-card-body">
          <el-avatar :size="48" class="elder-avatar">{{ (e.realName || '老').slice(0, 1) }}</el-avatar>
          <div class="elder-card-info">
            <div class="elder-name">{{ e.realName || '未命名' }}</div>
            <div class="elder-rel">关系：{{ e.relation || '—' }}</div>
            <div class="elder-metrics" v-if="e.latestHealth">
              <span>血压 <b>{{ e.latestHealth.bloodPressureHigh != null ? e.latestHealth.bloodPressureHigh + '/' + e.latestHealth.bloodPressureLow : '—' }}</b></span>
              <span>心率 <b>{{ e.latestHealth.heartRate != null ? e.latestHealth.heartRate : '—' }}</b></span>
              <span v-if="e.latestHealth.bloodSugar != null">血糖 <b>{{ e.latestHealth.bloodSugar }}</b></span>
            </div>
            <div class="elder-metrics muted" v-else>暂无健康数据</div>
          </div>
          <div class="elder-badges">
            <el-badge :value="e.unhandledWarningCount" :hidden="!e.unhandledWarningCount" type="danger">
              <el-tag size="small" effect="plain">预警</el-tag>
            </el-badge>
            <el-tag size="small" :type="e.deviceOnlineCount > 0 ? 'success' : 'info'" effect="plain">
              设备 {{ e.deviceCount || 0 }}·在线 {{ e.deviceOnlineCount || 0 }}
            </el-tag>
          </div>
          <el-icon class="elder-arrow"><ArrowRight /></el-icon>
        </div>
        <div class="elder-foot" v-if="e.lastActiveTime">最近活跃：{{ formatTime(e.lastActiveTime) }}</div>
      </el-card>
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="老人档案详情"
      width="680px"
      :destroy-on-close="true"
    >
      <div v-loading="detailLoading">
        <template v-if="detail">
          <!-- 基本信息 -->
          <h4 class="block-title">基本信息</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="姓名">{{ detail.realName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="关系">{{ detail.relation || '—' }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ genderText(detail.gender) }}</el-descriptions-item>
            <el-descriptions-item label="年龄">{{ detail.age != null ? detail.age + ' 岁' : '—' }}</el-descriptions-item>
            <el-descriptions-item label="生日">{{ detail.birthDate || '—' }}</el-descriptions-item>
            <el-descriptions-item label="血型">{{ detail.bloodType || '—' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ detail.phone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="身份证号">{{ detail.idCard || '—' }}</el-descriptions-item>
            <el-descriptions-item label="紧急联系人">{{ detail.emergencyContact || '—' }}</el-descriptions-item>
            <el-descriptions-item label="紧急联系电话">{{ detail.emergencyPhone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="身高">{{ detail.height != null ? detail.height + ' cm' : '—' }}</el-descriptions-item>
            <el-descriptions-item label="体重">{{ detail.weight != null ? detail.weight + ' kg' : '—' }}</el-descriptions-item>
            <el-descriptions-item label="住址" :span="2">{{ detail.address || '—' }}</el-descriptions-item>
            <el-descriptions-item label="既往病史" :span="2">{{ detail.medicalHistory || '无' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 健康状态 -->
          <h4 class="block-title">健康状态</h4>
          <div class="health-status-row">
            <el-tag :type="healthStatusTag(detail.healthStatus)" effect="dark" size="large">
              {{ healthStatusText(detail.healthStatus) }}
            </el-tag>
          </div>

          <!-- 最新健康数据 -->
          <h4 class="block-title">最新健康数据</h4>
          <el-empty v-if="!detail.healthRecord" description="暂无健康数据" :image-size="60" />
          <el-descriptions v-else :column="2" border size="small">
            <el-descriptions-item label="血压">{{ detail.healthRecord.bloodPressureHigh != null ? detail.healthRecord.bloodPressureHigh + '/' + detail.healthRecord.bloodPressureLow + ' mmHg' : '—' }}</el-descriptions-item>
            <el-descriptions-item label="心率">{{ detail.healthRecord.heartRate != null ? detail.healthRecord.heartRate + ' 次/分' : '—' }}</el-descriptions-item>
            <el-descriptions-item label="血糖">{{ detail.healthRecord.bloodSugar != null ? detail.healthRecord.bloodSugar + ' mmol/L' : '—' }}</el-descriptions-item>
            <el-descriptions-item label="体重">{{ detail.healthRecord.weight != null ? detail.healthRecord.weight + ' kg' : '—' }}</el-descriptions-item>
            <el-descriptions-item label="数据来源">{{ sourceText(detail.healthRecord.sourceType) }}</el-descriptions-item>
            <el-descriptions-item label="测量时间">{{ formatTime(detail.healthRecord.measuredAt) }}</el-descriptions-item>
          </el-descriptions>

          <!-- 设备绑定 -->
          <h4 class="block-title">设备绑定信息</h4>
          <el-empty v-if="!detail.devices || detail.devices.length === 0" description="未绑定设备" :image-size="60" />
          <el-table v-else :data="detail.devices" size="small" border>
            <el-table-column prop="deviceName" label="设备名称" />
            <el-table-column prop="deviceType" label="类型" />
            <el-table-column prop="vendor" label="厂商" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                  {{ row.status === 1 ? '已绑定' : '未绑定' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="最近同步" width="170">
              <template #default="{ row }">{{ formatTime(row.lastSyncTime) }}</template>
            </el-table-column>
          </el-table>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { familyApi } from '@/api'

const elders = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const detail = ref(null)
const detailLoading = ref(false)

import { assetUrl } from '@/utils/asset'

function genderText(g) {
  if (g === 1) return '男'
  if (g === 0) return '女'
  return '未填'
}
function healthStatusText(s) {
  return ['健康', '慢性病', '需照护', '其他疾病'][s] || '未知'
}
function healthStatusTag(s) {
  return ['success', 'warning', 'danger', 'info'][s] || 'info'
}
function sourceText(t) {
  if (!t) return '—'
  const map = { DEVICE: '智能设备', MANUAL: '手动录入', IMPORT: '批量导入' }
  return map[t] || t
}
function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 19)
}

async function loadElders() {
  loading.value = true
  try {
    const r = await familyApi.overview()
    const list = r.data || r
    elders.value = Array.isArray(list) ? list : []
  } catch (e) {
    ElMessage.error('加载老人列表失败')
  } finally {
    loading.value = false
  }
}

async function openDetail(elderInfoId) {
  dialogVisible.value = true
  detail.value = null
  detailLoading.value = true
  try {
    const r = await familyApi.elderDetail(elderInfoId)
    detail.value = r.data || r
  } catch (e) {
    ElMessage.error('加载老人详情失败')
  } finally {
    detailLoading.value = false
  }
}

onMounted(loadElders)
</script>

<style scoped>
.elder-page { padding: 8px 4px; }
.page-head { margin-bottom: 16px; }
.page-title { font-size: 22px; font-weight: 600; margin: 0 0 4px; }
.page-sub { color: #909399; font-size: 13px; margin: 0; }
.elder-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; }
.elder-card { cursor: pointer; transition: transform .15s; }
.elder-card:hover { transform: translateY(-2px); }
.elder-card-body { display: flex; align-items: center; gap: 12px; }
.elder-avatar { background: #409eff; color: #fff; font-size: 18px; }
.elder-card-info { flex: 1; }
.elder-name { font-size: 16px; font-weight: 600; }
.elder-rel { font-size: 13px; color: #909399; margin-top: 2px; }
.elder-arrow { color: #c0c4cc; }
.elder-metrics { font-size: 12px; color: #606266; margin-top: 4px; display: flex; gap: 10px; flex-wrap: wrap; }
.elder-metrics.muted { color: #c0c4cc; }
.elder-metrics b { color: #303133; font-weight: 600; }
.elder-badges { display: flex; flex-direction: column; gap: 6px; align-items: flex-end; margin-left: 4px; }
.elder-foot { font-size: 12px; color: #c0c4cc; margin-top: 10px; padding-top: 8px; border-top: 1px solid #f0f0f0; }
.block-title { margin: 18px 0 10px; font-size: 15px; font-weight: 600; color: #303133; border-left: 3px solid #409eff; padding-left: 8px; }
.block-title:first-child { margin-top: 0; }
.health-status-row { margin-bottom: 4px; }
</style>

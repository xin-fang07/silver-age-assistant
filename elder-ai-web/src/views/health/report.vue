
<template>
  <div class="report-page">
    <div class="page-head">
      <h2 class="page-title">LLM 智能健康报告</h2>
      <p class="page-sub">基于老人近期健康数据，由大模型生成通俗易懂的健康分析与建议。</p>
    </div>

    <el-card class="filter-card" shadow="never">
      <div class="filter-row">
        <el-select v-model="selectedElderId" placeholder="选择老人" style="width: 220px" :disabled="generating">
          <el-option v-for="e in elders" :key="e.elderInfoId" :label="e.realName || '未命名'" :value="e.elderInfoId" />
        </el-select>
        <el-button type="primary" :loading="generating" :disabled="!selectedElderId" @click="generate">
          <el-icon v-if="!generating"><MagicStick /></el-icon>
          {{ generating ? '生成中…' : '生成健康报告' }}
        </el-button>
        <el-button :disabled="generating || !report" @click="copyReport">复制</el-button>
      </div>
    </el-card>

    <el-empty v-if="!generating && !report" description="选择老人后点击「生成健康报告」" />

    <el-card v-if="report" class="report-card" shadow="never" v-loading="generating">
      <div class="report-meta">
        <span><b>{{ currentElderName }}</b> 的健康报告</span>
        <span class="muted">{{ generatedAt }}</span>
      </div>
      <div class="report-body">{{ report }}</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { MagicStick } from '@element-plus/icons-vue'
import { familyApi } from '@/api'

const elders = ref([])
const selectedElderId = ref(null)
const generating = ref(false)
const report = ref('')
const generatedAt = ref('')
const currentElderName = ref('')

onMounted(async () => {
  try {
    const res = await familyApi.myElders()
    elders.value = Array.isArray(res) ? res : (res && res.data) || []
    if (elders.value.length) selectedElderId.value = elders.value[0].elderInfoId
  } catch (e) {
    console.error('加载老人列表失败：', e)
  }
})

async function generate() {
  if (!selectedElderId.value) return
  generating.value = true
  report.value = ''
  try {
    const cur = elders.value.find((e) => e.elderInfoId === selectedElderId.value)
    currentElderName.value = cur ? (cur.realName || '未命名') : ''
    const token = localStorage.getItem('elder_ai_token')
    const resp = await fetch('/api/family/health/advice?elderId=' + encodeURIComponent(selectedElderId.value), {
      headers: { Authorization: 'Bearer ' + token },
    })
    const body = await resp.json()
    report.value = (body && (body.data || body.message)) || ''
    generatedAt.value = new Date().toLocaleString()
  } catch (e) {
    console.error('生成报告失败：', e)
    report.value = '报告生成失败，请稍后重试。'
  } finally {
    generating.value = false
  }
}

function copyReport() {
  if (navigator.clipboard) navigator.clipboard.writeText(report.value)
}
</script>

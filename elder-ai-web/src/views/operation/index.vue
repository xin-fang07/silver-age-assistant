<template>
  <div class="op-page">
    <div class="page-head">
      <h2 class="page-title">操作记录</h2>
      <p class="page-sub">汇总您在照护过程中的近期动态：健康预警、SOS 求助与用药提醒。</p>
    </div>

    <div v-loading="loading" class="op-body">
      <el-empty v-if="!loading && records.length === 0" description="暂无操作记录" />

      <el-timeline v-else>
        <el-timeline-item
          v-for="(it, idx) in records"
          :key="idx"
          :timestamp="formatTime(it.time)"
          placement="top"
          :type="it.color"
        >
          <el-card class="op-card" shadow="never">
            <div class="op-row">
              <el-tag :type="it.tag" size="small" effect="light">{{ it.typeText }}</el-tag>
              <span class="op-elder">老人：{{ it.elderName }}</span>
            </div>
            <div class="op-title">{{ it.title }}</div>
            <div class="op-extra muted" v-if="it.extra">{{ it.extra }}</div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { familyApi } from '@/api'

const loading = ref(false)
const records = ref([])

function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 19)
}
function toArr(r) {
  const d = r && r.data !== undefined ? r.data : r
  if (Array.isArray(d)) return d
  if (d && Array.isArray(d.records)) return d.records
  if (d && Array.isArray(d.list)) return d.list
  return []
}

async function load() {
  loading.value = true
  try {
    const r = await familyApi.overview()
    const list = r.data || r
    const elders = Array.isArray(list) ? list : []
    const all = []
    for (const e of elders) {
      const name = e.realName || '未命名'
      // 预警
      try {
        const warns = toArr(await familyApi.warningList(e.elderInfoId))
        warns.forEach(w => all.push({
          typeText: '健康预警', tag: 'danger', color: 'danger', time: w.createTime,
          elderName: name, title: `${w.warningType || '预警'}（${w.warningLevel != null ? '等级' + w.warningLevel : '—'}）`,
          extra: w.warningContent || ''
        }))
      } catch (_) {}
      // SOS
      try {
        const ems = toArr(await familyApi.emergencyList(e.elderInfoId))
        ems.forEach(m => all.push({
          typeText: 'SOS 求助', tag: 'warning', color: 'warning', time: m.createTime,
          elderName: name, title: m.helpContent || '紧急求助',
          extra: m.status != null ? ('处理状态：' + (['待处理', '处理中', '已完成'][m.status] || m.status)) : ''
        }))
      } catch (_) {}
      // 提醒
      try {
        const rems = toArr(await familyApi.reminderList(e.elderInfoId))
        rems.forEach(rm => all.push({
          typeText: '用药提醒', tag: '', color: 'primary', time: rm.createTime || rm.remindTime,
          elderName: name, title: rm.title || '提醒',
          extra: rm.content || ''
        }))
      } catch (_) {}
    }
    all.sort((a, b) => String(b.time).localeCompare(String(a.time)))
    records.value = all.slice(0, 100)
  } catch (e) {
    ElMessage.error('加载操作记录失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.op-page { padding: 8px 4px; }
.page-head { margin-bottom: 16px; }
.page-title { font-size: 22px; font-weight: 600; margin: 0 0 4px; }
.page-sub { color: #909399; font-size: 13px; margin: 0; }
.op-body { padding: 4px 4px 8px; }
.op-card { border-radius: 10px; }
.op-row { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.op-elder { font-size: 13px; color: #606266; }
.op-title { font-size: 14px; color: #303133; }
.op-extra { font-size: 12px; margin-top: 4px; }
.muted { color: #c0c4cc; }
</style>

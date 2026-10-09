<template>
  <div class="location-page">
    <div class="page-head">
      <h2 class="page-title">位置信息查看</h2>
      <p class="page-sub">展示老人最近一次 SOS 报警上报的位置。若无记录，说明尚未触发过紧急求助。</p>
    </div>

    <div v-loading="loading" class="loc-list">
      <el-empty v-if="!loading && items.length === 0" description="暂无位置信息（需老人触发 SOS 后上报）" />

      <el-card v-for="it in items" :key="it.elderInfoId" class="loc-card" shadow="hover">
        <div class="loc-head">
          <el-avatar :size="44" class="loc-avatar">{{ (it.realName || '老').slice(0, 1) }}</el-avatar>
          <div>
            <div class="loc-name">{{ it.realName || '未命名' }}</div>
            <div class="loc-rel">关系：{{ it.relation || '—' }}</div>
          </div>
          <el-tag v-if="it.hasLocation" type="success" size="small" effect="plain">已定位</el-tag>
          <el-tag v-else type="info" size="small" effect="plain">暂无位置</el-tag>
        </div>

        <template v-if="it.hasLocation">
          <el-descriptions :column="1" border size="small" class="loc-desc">
            <el-descriptions-item label="位置描述">{{ it.locationText || '—' }}</el-descriptions-item>
            <el-descriptions-item label="经纬度">
              {{ it.latitude != null && it.longitude != null ? it.latitude + ', ' + it.longitude : '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="上报时间">{{ formatTime(it.createTime) }}</el-descriptions-item>
          </el-descriptions>
          <div class="loc-actions">
            <el-button size="small" type="primary" @click="openMap(it)">在地图中查看</el-button>
          </div>
        </template>
        <div v-else class="loc-empty muted">该老人尚无 SOS 位置上报记录。</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { familyApi } from '@/api'

const loading = ref(false)
const items = ref([])

function formatTime(t) {
  if (!t) return '—'
  return String(t).replace('T', ' ').slice(0, 19)
}

async function loadLocations() {
  loading.value = true
  try {
    const r = await familyApi.overview()
    const list = r.data || r
    const elders = Array.isArray(list) ? list : []
    const result = []
    for (const e of elders) {
      const row = { elderInfoId: e.elderInfoId, realName: e.realName, relation: e.relation, hasLocation: false }
      try {
        const er = await familyApi.emergencyList(e.elderInfoId)
        const ems = (er.data || er || [])
        const withLoc = ems.filter(x => x.latitude != null && x.longitude != null)
        if (withLoc.length) {
          const latest = withLoc[0]
          row.hasLocation = true
          row.latitude = latest.latitude
          row.longitude = latest.longitude
          row.locationText = latest.locationText
          row.createTime = latest.createTime
        }
      } catch (_) { /* 单个老人失败不影响其他 */ }
      result.push(row)
    }
    items.value = result
  } catch (e) {
    ElMessage.error('加载位置信息失败')
  } finally {
    loading.value = false
  }
}

function openMap(it) {
  if (it.latitude == null || it.longitude == null) return
  const url = `https://uri.amap.com/marker?position=${it.longitude},${it.latitude}&name=${encodeURIComponent(it.realName || '老人位置')}&src=elder-ai&coordinate=wgs84`
  window.open(url, '_blank')
}

onMounted(loadLocations)
</script>

<style scoped>
.location-page { padding: 8px 4px; }
.page-head { margin-bottom: 16px; }
.page-title { font-size: 22px; font-weight: 600; margin: 0 0 4px; }
.page-sub { color: #909399; font-size: 13px; margin: 0; }
.loc-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px; }
.loc-card { border-radius: 12px; }
.loc-head { display: flex; align-items: center; gap: 12px; }
.loc-avatar { background: #409eff; color: #fff; font-size: 16px; }
.loc-name { font-size: 16px; font-weight: 600; }
.loc-rel { font-size: 13px; color: #909399; margin-top: 2px; }
.loc-desc { margin-top: 14px; }
.loc-actions { margin-top: 12px; text-align: right; }
.loc-empty { margin-top: 12px; font-size: 13px; }
.muted { color: #c0c4cc; }
</style>

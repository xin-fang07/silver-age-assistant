<template>
  <div class="push-log-page">
    <el-card class="page-card" shadow="never">
      <div class="page-header">
        <div class="title-block">
          <h2 class="page-title">推送日志</h2>
          <p class="page-sub">记录提醒与预警推送到智能设备的明细，便于排查触达情况。</p>
        </div>
        <div class="header-actions">
          <el-select v-model="filters.pushType" placeholder="推送类型" clearable style="width:140px" @change="load">
            <el-option label="全部类型" value="" />
            <el-option label="提醒推送" value="REMINDER" />
            <el-option label="预警推送" value="WARNING" />
          </el-select>
          <el-select v-model="filters.status" placeholder="推送状态" clearable style="width:140px" @change="load">
            <el-option label="全部状态" value="" />
            <el-option label="已发送" value="SENT" />
            <el-option label="失败" value="FAILED" />
          </el-select>
          <el-button type="primary" :icon="Refresh" @click="load">刷新</el-button>
        </div>
      </div>

      <el-table :data="logs" v-loading="loading" stripe border class="log-table">
        <el-table-column prop="pushType" label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="row.pushType === 'WARNING' ? 'danger' : 'primary'" effect="light">
              {{ row.pushType === 'WARNING' ? '预警推送' : '提醒推送' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="140" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="deviceId" label="设备ID" min-width="150" show-overflow-tooltip />
        <el-table-column prop="elderId" label="老人ID" width="90" />
        <el-table-column prop="familyUserId" label="家属ID" width="90" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SENT' ? 'success' : 'danger'" effect="light">
              {{ row.status === 'SENT' ? '已发送' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failReason" label="失败原因" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.failReason">{{ row.failReason }}</span>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="推送时间" width="180" />
      </el-table>

      <el-empty v-if="!loading && logs.length === 0" description="暂无推送记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { pushLogApi } from '@/api'

const loading = ref(false)
const logs = ref([])
const filters = reactive({ pushType: '', status: '' })

async function load() {
  loading.value = true
  try {
    const params = {}
    if (filters.pushType) params.pushType = filters.pushType
    if (filters.status) params.status = filters.status
    const res = await pushLogApi.list(params)
    logs.value = res.data || []
  } catch (e) {
    logs.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.push-log-page { padding: 16px; }
.page-card { border-radius: 12px; }
.page-header { display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 12px; margin-bottom: 16px; }
.page-title { margin: 0; font-size: 22px; font-weight: 600; }
.page-sub { margin: 4px 0 0; color: #8a8f99; font-size: 13px; }
.header-actions { display: flex; gap: 8px; align-items: center; }
.log-table { width: 100%; }
.muted { color: #c0c4cc; }
</style>

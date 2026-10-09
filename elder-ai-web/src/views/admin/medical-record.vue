<template>
  <div class="admin-medical-record">
    <el-card shadow="never">
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索医院 / 科室 / 诊断" clearable style="width:260px" @keyup.enter="loadList" @clear="loadList" />
        <el-select v-model="elderInfoId" placeholder="按老人筛选" clearable filterable style="width:200px" @change="loadList">
          <el-option v-for="e in elders" :key="e.elderInfoId" :label="e.realName" :value="e.elderInfoId" />
        </el-select>
        <el-button type="primary" @click="loadList">查询</el-button>
        <el-button @click="resetFilter">重置</el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="elderName" label="老人" width="120" />
        <el-table-column prop="hospitalName" label="医院" min-width="160" show-overflow-tooltip />
        <el-table-column prop="department" label="科室" width="110" />
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="visitDate" label="就诊日期" width="120" />
        <el-table-column prop="diagnosis" label="诊断" min-width="160" show-overflow-tooltip />
        <el-table-column label="图片" width="80" align="center">
          <template #default="{ row }">{{ (row.attachments || []).length }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDetail(row)">查看</el-button>
            <el-button size="small" type="danger" @click="removeRow(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pager" v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10,20,50]"
        layout="total, sizes, prev, pager, next" @current-change="loadList" @size-change="loadList" />
    </el-card>

    <el-dialog v-model="detailVisible" title="就诊记录详情" width="700px">
      <template v-if="current">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="老人">{{ current.elderName }}</el-descriptions-item>
          <el-descriptions-item label="就诊日期">{{ current.visitDate }}</el-descriptions-item>
          <el-descriptions-item label="医院">{{ current.hospitalName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ current.department || '—' }}</el-descriptions-item>
          <el-descriptions-item label="主治医生">{{ current.doctorName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="诊断">{{ current.diagnosis || '—' }}</el-descriptions-item>
          <el-descriptions-item label="医嘱/处方" :span="2">{{ current.prescription || '—' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ current.remark || '—' }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="current.attachments && current.attachments.length" class="detail-imgs">
          <el-image v-for="(u,i) in current.attachments" :key="i"
            :src="assetUrl(u)"
            :preview-src-list="current.attachments.map(assetUrl)"
            :initial-index="i" fit="cover" class="detail-img" />
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { familyApi, medicalRecordAdminApi } from '@/api/index'

const keyword = ref('')
const elderInfoId = ref(null)
const elders = ref([])
const list = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const current = ref(null)

import { assetUrl } from '@/utils/asset'

async function loadElders() {
  const r = await familyApi.myElders()
  elders.value = r.data || r || []
}
async function loadList() {
  loading.value = true
  try {
    const r = await medicalRecordAdminApi.list({
      keyword: keyword.value || undefined,
      elderInfoId: elderInfoId.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    list.value = r.data || []
    total.value = r.total || 0
  } finally {
    loading.value = false
  }
}
function resetFilter() {
  keyword.value = ''
  elderInfoId.value = null
  pageNum.value = 1
  loadList()
}
function openDetail(row) {
  current.value = row
  detailVisible.value = true
}
async function removeRow(row) {
  try {
    await ElMessageBox.confirm(`确定删除「${row.elderName || ''}」的该条就诊记录吗？`, '提示', { type: 'warning' })
  } catch { return }
  await medicalRecordAdminApi.remove(row.id)
  ElMessage.success('已删除')
  loadList()
}
onMounted(() => { loadElders(); loadList() })
</script>

<style scoped>
.toolbar { display:flex; gap:12px; margin-bottom:16px; flex-wrap:wrap; }
.pager { margin-top:16px; justify-content:flex-end; }
.detail-imgs { display:flex; gap:10px; flex-wrap:wrap; margin-top:14px; }
.detail-img { width:120px; height:120px; border-radius:8px; }
</style>

<template>
  <div class="llm-analysis-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">LLM分析结果</h2>
        <p class="page-subtitle">留存大模型每一次的分析原文记录，方便溯源和审计</p>
      </div>
      <div class="header-actions">
        <el-button @click="exportData">导出记录</el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-select v-model="filterElder" placeholder="选择老人" clearable style="width:180px" @change="loadList">
        <el-option v-for="e in elders" :key="e.id" :label="e.realName" :value="e.id" />
      </el-select>
      <el-select v-model="filterRiskLevel" placeholder="风险等级" clearable style="width:140px" @change="loadList">
        <el-option label="正常" value="NORMAL" />
        <el-option label="偏低" value="LOW" />
        <el-option label="偏高" value="HIGH" />
        <el-option label="高危" value="DANGER" />
      </el-select>
      <el-button type="primary" @click="loadList">搜索</el-button>
      <el-button @click="resetFilter">重置</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="记录ID" width="80" />
      <el-table-column prop="elderName" label="老人姓名" width="120" />
      <el-table-column prop="riskLevel" label="风险等级" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.riskLevel === 'NORMAL'" type="success">正常</el-tag>
          <el-tag v-else-if="row.riskLevel === 'LOW'" type="info">偏低</el-tag>
          <el-tag v-else-if="row.riskLevel === 'HIGH'" type="warning">偏高</el-tag>
          <el-tag v-else-if="row.riskLevel === 'DANGER'" type="danger">高危</el-tag>
          <span v-else>{{ row.riskLevelText }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="分析开始日期" width="130" />
      <el-table-column prop="endDate" label="分析结束日期" width="130" />
      <el-table-column prop="batchNo" label="生成批次" width="100" />
      <el-table-column prop="pushed" label="推送状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.pushed === 1" type="success">已推送</el-tag>
          <el-tag v-else type="info">未推送</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="生成时间" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">查看详情</el-button>
          <el-button size="small" type="primary" @click="retriggerAnalysis(row)">重新分析</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10,20,50]"
        layout="total, sizes, prev, pager, next" @current-change="loadList" @size-change="loadList" />
    </div>

    <el-dialog v-model="detailVisible" title="LLM分析详情" width="800px">
      <div v-if="currentDetail" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="老人">{{ currentDetail.elderName }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">
            <el-tag v-if="currentDetail.riskLevel === 'NORMAL'" type="success">正常</el-tag>
            <el-tag v-else-if="currentDetail.riskLevel === 'LOW'" type="info">偏低</el-tag>
            <el-tag v-else-if="currentDetail.riskLevel === 'HIGH'" type="warning">偏高</el-tag>
            <el-tag v-else-if="currentDetail.riskLevel === 'DANGER'" type="danger">高危</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="分析周期">{{ currentDetail.startDate }} ~ {{ currentDetail.endDate }}</el-descriptions-item>
          <el-descriptions-item label="生成批次">{{ currentDetail.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="生成时间">{{ currentDetail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="推送状态">
            <el-tag v-if="currentDetail.pushed === 1" type="success">已推送</el-tag>
            <el-tag v-else type="info">未推送</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div class="analysis-text">
          <h4>AI分析原文</h4>
          <div class="text-content">{{ currentDetail.analysisText }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="retriggerCurrentAnalysis">重新分析</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="retriggerVisible" title="重新触发LLM分析" width="500px">
      <el-form :model="retriggerForm" label-width="100px">
        <el-form-item label="选择老人">
          <el-select v-model="retriggerForm.elderInfoId" placeholder="选择老人" style="width:100%">
            <el-option v-for="e in elders" :key="e.id" :label="e.realName" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分析开始日期">
          <el-date-picker v-model="retriggerForm.startDate" type="date" style="width:100%" />
        </el-form-item>
        <el-form-item label="分析结束日期">
          <el-date-picker v-model="retriggerForm.endDate" type="date" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="retriggerVisible = false">取消</el-button>
        <el-button type="primary" @click="doRetrigger">确认触发</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminHealthApi } from '@/api/index'

const list = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const elders = ref([])
const filterElder = ref(null)
const filterRiskLevel = ref(null)
const detailVisible = ref(false)
const retriggerVisible = ref(false)
const currentDetail = ref(null)

const retriggerForm = reactive({
  elderInfoId: null,
  startDate: null,
  endDate: null
})

async function loadElders() {
  const r = await adminHealthApi.getElders()
  elders.value = r.data || r || []
}

async function loadList() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    if (filterElder.value) params.elderInfoId = filterElder.value
    if (filterRiskLevel.value) params.riskLevel = filterRiskLevel.value
    const r = await adminHealthApi.getLlmAnalysis(params)
    list.value = r.data.list || []
    total.value = r.data.total || 0
  } catch (e) {
    console.error(e)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function viewDetail(row) {
  try {
    const r = await adminHealthApi.getLlmAnalysisDetail(row.id)
    currentDetail.value = r.data
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

function retriggerAnalysis(row) {
  retriggerForm.elderInfoId = row.elderInfoId
  retriggerForm.startDate = row.startDate
  retriggerForm.endDate = row.endDate
  retriggerVisible.value = true
}

function retriggerCurrentAnalysis() {
  if (currentDetail.value) {
    retriggerForm.elderInfoId = currentDetail.value.elderInfoId
    retriggerForm.startDate = currentDetail.value.startDate
    retriggerForm.endDate = currentDetail.value.endDate
    detailVisible.value = false
    retriggerVisible.value = true
  }
}

async function doRetrigger() {
  try {
    await adminHealthApi.retriggerLlmAnalysis(retriggerForm)
    ElMessage.success('LLM分析已重新触发')
    retriggerVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error('触发失败')
  }
}

function resetFilter() {
  filterElder.value = null
  filterRiskLevel.value = null
  pageNum.value = 1
  loadList()
}

async function exportData() {
  try {
    const params = {}
    if (filterElder.value) params.elderInfoId = filterElder.value
    if (filterRiskLevel.value) params.riskLevel = filterRiskLevel.value
    const r = await adminHealthApi.exportLlmAnalysis(params)
    const blob = new Blob([r.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'LLM分析记录.xlsx'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  loadElders()
  loadList()
})
</script>

<style scoped>
.llm-analysis-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  margin: 0;
}

.page-subtitle {
  color: #94a3b8;
  margin: 5px 0 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.detail-content {
  margin-top: 10px;
}

.analysis-text {
  margin-top: 20px;
}

.analysis-text h4 {
  margin: 0 0 10px;
  font-size: 14px;
  font-weight: 600;
}

.text-content {
  padding: 15px;
  background: #f8fafc;
  border-radius: 8px;
  white-space: pre-wrap;
  line-height: 1.8;
  max-height: 400px;
  overflow-y: auto;
}
</style>
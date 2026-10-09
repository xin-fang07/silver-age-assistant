<template>
  <div class="health-report-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">健康报告管理</h2>
        <p class="page-subtitle">汇总健康记录和LLM分析，生成定期健康报告（周报告、月报告）</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="openRegenerateDialog">重新生成报告</el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-select v-model="filterElder" placeholder="选择老人" clearable style="width:180px" @change="loadList">
        <el-option v-for="e in elders" :key="e.id" :label="e.realName" :value="e.id" />
      </el-select>
      <el-select v-model="filterReportType" placeholder="报告类型" clearable style="width:140px" @change="loadList">
        <el-option label="周报告" value="WEEKLY" />
        <el-option label="月报告" value="MONTHLY" />
      </el-select>
      <el-button type="primary" @click="loadList">搜索</el-button>
      <el-button @click="resetFilter">重置</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="报告ID" width="80" />
      <el-table-column prop="reportNo" label="报告编号" width="140" />
      <el-table-column prop="elderName" label="老人姓名" width="120" />
      <el-table-column prop="reportType" label="报告类型" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.reportType === 'WEEKLY'" type="info">周报告</el-tag>
          <el-tag v-else-if="row.reportType === 'MONTHLY'" type="warning">月报告</el-tag>
          <span v-else>{{ row.reportTypeText }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="报告周期开始" width="130" />
      <el-table-column prop="endDate" label="报告周期结束" width="130" />
      <el-table-column prop="pushed" label="推送状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.pushed === 1" type="success">已推送</el-tag>
          <el-tag v-else type="info">未推送</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="生成时间" width="180" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row)">预览报告</el-button>
          <el-button size="small" @click="downloadReport(row)">下载PDF</el-button>
          <el-button size="small" type="primary" @click="resendReport(row)">重发通知</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10,20,50]"
        layout="total, sizes, prev, pager, next" @current-change="loadList" @size-change="loadList" />
    </div>

    <el-dialog v-model="detailVisible" title="健康报告预览" width="800px">
      <div v-if="currentDetail" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="报告编号">{{ currentDetail.reportNo }}</el-descriptions-item>
          <el-descriptions-item label="老人">{{ currentDetail.elderName }}</el-descriptions-item>
          <el-descriptions-item label="报告类型">
            <el-tag v-if="currentDetail.reportType === 'WEEKLY'" type="info">周报告</el-tag>
            <el-tag v-else-if="currentDetail.reportType === 'MONTHLY'" type="warning">月报告</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="报告周期">{{ currentDetail.startDate }} ~ {{ currentDetail.endDate }}</el-descriptions-item>
          <el-descriptions-item label="生成时间">{{ currentDetail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="推送状态">
            <el-tag v-if="currentDetail.pushed === 1" type="success">已推送</el-tag>
            <el-tag v-else type="info">未推送</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <div class="report-content">
          <h4>报告内容</h4>
          <div class="content-html" v-html="currentDetail.reportContent"></div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="downloadCurrentReport">下载PDF</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="regenerateVisible" title="重新生成健康报告" width="500px">
      <el-form :model="regenerateForm" label-width="100px">
        <el-form-item label="选择老人">
          <el-select v-model="regenerateForm.elderInfoId" placeholder="选择老人" style="width:100%">
            <el-option v-for="e in elders" :key="e.id" :label="e.realName" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="报告类型">
          <el-select v-model="regenerateForm.reportType" style="width:100%">
            <el-option label="周报告" value="WEEKLY" />
            <el-option label="月报告" value="MONTHLY" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="regenerateVisible = false">取消</el-button>
        <el-button type="primary" @click="doRegenerate">确认生成</el-button>
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
const filterReportType = ref(null)
const detailVisible = ref(false)
const regenerateVisible = ref(false)
const currentDetail = ref(null)

const regenerateForm = reactive({
  elderInfoId: null,
  reportType: 'MONTHLY'
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
    if (filterReportType.value) params.reportType = filterReportType.value
    const r = await adminHealthApi.getHealthReports(params)
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
    const r = await adminHealthApi.getHealthReportDetail(row.id)
    currentDetail.value = r.data
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('获取详情失败')
  }
}

function downloadReport(row) {
  if (row.pdfUrl) {
    window.open(row.pdfUrl)
  } else {
    ElMessage.info('PDF文件生成中，请稍后')
  }
}

function downloadCurrentReport() {
  downloadReport(currentDetail.value)
}

async function resendReport(row) {
  try {
    await adminHealthApi.resendReport(row.id)
    ElMessage.success('已重新发送报告通知')
    loadList()
  } catch (e) {
    ElMessage.error('发送失败')
  }
}

function openRegenerateDialog() {
  regenerateForm.elderInfoId = null
  regenerateForm.reportType = 'MONTHLY'
  regenerateVisible.value = true
}

async function doRegenerate() {
  try {
    await adminHealthApi.regenerateHealthReport(regenerateForm)
    ElMessage.success('健康报告已重新生成')
    regenerateVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error('生成失败')
  }
}

function resetFilter() {
  filterElder.value = null
  filterReportType.value = null
  pageNum.value = 1
  loadList()
}

onMounted(() => {
  loadElders()
  loadList()
})
</script>

<style scoped>
.health-report-page {
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

.report-content {
  margin-top: 20px;
}

.report-content h4 {
  margin: 0 0 10px;
  font-size: 14px;
  font-weight: 600;
}

.content-html {
  padding: 15px;
  background: #f8fafc;
  border-radius: 8px;
  line-height: 1.8;
  max-height: 400px;
  overflow-y: auto;
}

.content-html h3 {
  margin: 0 0 10px;
  font-size: 16px;
}

.content-html p {
  margin: 5px 0;
}
</style>
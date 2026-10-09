<template>
  <div class="health-record-page">
    <div class="page-header">
      <div>
        <h2 class="page-title">健康记录</h2>
        <p class="page-subtitle">存放全量原始健康明细数据，是所有分析、报告的数据源头</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="openCreateDialog">手动录入</el-button>
        <el-button @click="exportData">导出数据</el-button>
      </div>
    </div>

    <div class="search-bar">
      <el-select v-model="filterElder" placeholder="选择老人" clearable style="width:180px" @change="loadList">
        <el-option v-for="e in elders" :key="e.id" :label="e.realName" :value="e.id" />
      </el-select>
      <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width:280px" @change="loadList" />
      <el-select v-model="filterSource" placeholder="数据来源" clearable style="width:140px" @change="loadList">
        <el-option label="智能设备" value="DEVICE" />
        <el-option label="手动录入" value="MANUAL" />
        <el-option label="文件导入" value="FILE_IMPORT" />
      </el-select>
      <el-button type="primary" @click="loadList">搜索</el-button>
      <el-button @click="resetFilter">重置</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="记录ID" width="80" />
      <el-table-column prop="elderName" label="老人姓名" width="120" />
      <el-table-column prop="recordDate" label="采集日期" width="120" />
      <el-table-column prop="bloodPressureHigh" label="血压(高)" width="100" />
      <el-table-column prop="bloodPressureLow" label="血压(低)" width="100" />
      <el-table-column prop="heartRate" label="心率" width="80" />
      <el-table-column prop="bloodOxygen" label="血氧(%)" width="90" />
      <el-table-column prop="steps" label="步数" width="90" />
      <el-table-column prop="bloodSugar" label="血糖" width="80" />
      <el-table-column prop="weight" label="体重(kg)" width="100" />
      <el-table-column prop="sourceType" label="数据来源" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.sourceType === 'DEVICE'" type="info">智能设备</el-tag>
          <el-tag v-else-if="row.sourceType === 'MANUAL'" type="success">手动录入</el-tag>
          <el-tag v-else-if="row.sourceType === 'FILE_IMPORT'" type="warning">文件导入</el-tag>
          <span v-else>{{ row.sourceType }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="录入时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteRow(row)">删除</el-button>
          <el-button size="small" type="warning" @click="markInvalid(row)">标记无效</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
        :total="total" :page-sizes="[10,20,50]"
        layout="total, sizes, prev, pager, next" @current-change="loadList" @size-change="loadList" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑健康记录' : '新增健康记录'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="老人">
          <el-select v-model="form.elderInfoId" placeholder="选择老人" style="width:100%">
            <el-option v-for="e in elders" :key="e.id" :label="e.realName" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="记录日期">
          <el-date-picker v-model="form.recordDate" type="date" style="width:100%" />
        </el-form-item>
        <el-form-item label="收缩压(高压)">
          <el-input v-model.number="form.bloodPressureHigh" placeholder="收缩压" />
        </el-form-item>
        <el-form-item label="舒张压(低压)">
          <el-input v-model.number="form.bloodPressureLow" placeholder="舒张压" />
        </el-form-item>
        <el-form-item label="心率">
          <el-input v-model.number="form.heartRate" placeholder="心率" />
        </el-form-item>
        <el-form-item label="血氧饱和度">
          <el-input v-model.number="form.bloodOxygen" placeholder="血氧(%)" />
        </el-form-item>
        <el-form-item label="步数">
          <el-input v-model.number="form.steps" placeholder="步数" />
        </el-form-item>
        <el-form-item label="血糖">
          <el-input v-model.number="form.bloodSugar" placeholder="血糖" />
        </el-form-item>
        <el-form-item label="体重">
          <el-input v-model.number="form.weight" placeholder="体重(kg)" />
        </el-form-item>
        <el-form-item label="数据来源">
          <el-select v-model="form.sourceType" style="width:100%">
            <el-option label="手动录入" value="MANUAL" />
            <el-option label="智能设备" value="DEVICE" />
            <el-option label="文件导入" value="FILE_IMPORT" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRecord">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminHealthApi } from '@/api/index'

const list = ref([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const elders = ref([])
const filterElder = ref(null)
const dateRange = ref([])
const filterSource = ref(null)
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  elderInfoId: null,
  recordDate: null,
  bloodPressureHigh: null,
  bloodPressureLow: null,
  heartRate: null,
  bloodOxygen: null,
  steps: null,
  bloodSugar: null,
  weight: null,
  sourceType: 'MANUAL',
  remark: ''
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
    if (dateRange.value && dateRange.value[0]) params.startDate = dateRange.value[0]
    if (dateRange.value && dateRange.value[1]) params.endDate = dateRange.value[1]
    if (filterSource.value) params.sourceType = filterSource.value
    const r = await adminHealthApi.getHealthRecords(params)
    list.value = r.data.list || []
    total.value = r.data.total || 0
  } catch (e) {
    console.error(e)
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    elderInfoId: null,
    recordDate: new Date(),
    bloodPressureHigh: null,
    bloodPressureLow: null,
    heartRate: null,
    bloodOxygen: null,
    steps: null,
    bloodSugar: null,
    weight: null,
    sourceType: 'MANUAL',
    remark: ''
  })
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    elderInfoId: row.elderInfoId,
    recordDate: row.recordDate,
    bloodPressureHigh: row.bloodPressureHigh,
    bloodPressureLow: row.bloodPressureLow,
    heartRate: row.heartRate,
    bloodOxygen: row.bloodOxygen,
    steps: row.steps,
    bloodSugar: row.bloodSugar,
    weight: row.weight,
    sourceType: row.sourceType,
    remark: row.remark
  })
  dialogVisible.value = true
}

async function saveRecord() {
  try {
    if (isEdit.value) {
      await adminHealthApi.updateHealthRecord(form.id, form)
      ElMessage.success('修改成功')
    } else {
      await adminHealthApi.addHealthRecord(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function deleteRow(row) {
  try {
    await ElMessageBox.confirm('确定删除这条记录吗？', '提示', { type: 'warning' })
    await adminHealthApi.deleteHealthRecord(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {}
}

async function markInvalid(row) {
  try {
    await adminHealthApi.markInvalid(row.id)
    ElMessage.success('已标记为无效数据')
    loadList()
  } catch (e) {
    ElMessage.error('标记失败')
  }
}

function resetFilter() {
  filterElder.value = null
  dateRange.value = []
  filterSource.value = null
  pageNum.value = 1
  loadList()
}

async function exportData() {
  try {
    const params = {}
    if (filterElder.value) params.elderInfoId = filterElder.value
    if (dateRange.value && dateRange.value[0]) params.startDate = dateRange.value[0]
    if (dateRange.value && dateRange.value[1]) params.endDate = dateRange.value[1]
    const r = await adminHealthApi.exportHealthRecords(params)
    const blob = new Blob([r.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '健康记录.xlsx'
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
.health-record-page {
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
</style>
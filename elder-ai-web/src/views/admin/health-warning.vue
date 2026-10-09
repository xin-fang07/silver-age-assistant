<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title"><el-icon><Warning /></el-icon>健康预警管理</h2>
      <p class="page-desc">管理系统中的健康预警信息，及时处理异常数据</p>
    </div>
    
    <div class="warning-stats">
      <div class="stat-card">
        <div class="stat-value">{{ stats.total }}</div>
        <div class="stat-label">总预警数</div>
      </div>
      <div class="stat-card warning">
        <div class="stat-value">{{ stats.warning }}</div>
        <div class="stat-label">待处理</div>
      </div>
      <div class="stat-card success">
        <div class="stat-value">{{ stats.resolved }}</div>
        <div class="stat-label">已处理</div>
      </div>
    </div>

    <div class="page-body">
      <el-table :data="warnings" border stripe class="data-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="elderName" label="老人姓名" />
        <el-table-column prop="type" label="预警类型">
          <template #default="{ row }">
            <el-tag :type="getTagType(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="value" label="异常值" />
        <el-table-column prop="threshold" label="阈值" />
        <el-table-column prop="createdAt" label="预警时间" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PENDING' ? 'warning' : 'success'">
              {{ row.status === 'PENDING' ? '待处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="primary" size="small" @click="handleWarning(row)">处理</el-button>
            <el-button v-else type="info" size="small" @click="viewDetail(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadWarnings"
          @current-change="loadWarnings"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'

const stats = ref({ total: 0, warning: 0, resolved: 0 })
const warnings = ref([])
const pagination = ref({ page: 1, size: 10, total: 0 })

const getTagType = (type) => {
  const types = { 'BLOOD_PRESSURE': 'danger', 'BLOOD_SUGAR': 'warning', 'HEART_RATE': 'danger', 'WEIGHT': 'info' }
  return types[type] || 'info'
}

const getTypeName = (type) => {
  const types = { 'BLOOD_PRESSURE': '血压异常', 'BLOOD_SUGAR': '血糖异常', 'HEART_RATE': '心率异常', 'WEIGHT': '体重异常' }
  return types[type] || type
}

const loadWarnings = async () => {
  warnings.value = [
    { id: 1, elderName: '张大爷', type: 'BLOOD_PRESSURE', value: '165/95 mmHg', threshold: '140/90 mmHg', createdAt: '2024-01-15 10:30:00', status: 'PENDING' },
    { id: 2, elderName: '李奶奶', type: 'BLOOD_SUGAR', value: '12.5 mmol/L', threshold: '7.0 mmol/L', createdAt: '2024-01-15 09:15:00', status: 'PENDING' },
    { id: 3, elderName: '王爷爷', type: 'HEART_RATE', value: '115 次/分', threshold: '100 次/分', createdAt: '2024-01-14 22:45:00', status: 'RESOLVED' },
    { id: 4, elderName: '赵奶奶', type: 'WEIGHT', value: '45 kg', threshold: '50 kg', createdAt: '2024-01-14 18:20:00', status: 'RESOLVED' },
    { id: 5, elderName: '刘大爷', type: 'BLOOD_PRESSURE', value: '170/100 mmHg', threshold: '140/90 mmHg', createdAt: '2024-01-13 11:00:00', status: 'RESOLVED' },
  ]
  stats.value = { total: 5, warning: 2, resolved: 3 }
  pagination.value.total = 5
}

const handleWarning = (row) => {
  row.status = 'RESOLVED'
  stats.value.warning--
  stats.value.resolved++
  ElMessage.success('预警已处理')
}

const viewDetail = (row) => {
  ElMessage.info(`查看预警详情：${row.elderName} - ${getTypeName(row.type)}`)
}

onMounted(loadWarnings)
</script>

<style scoped>
.page-container { padding: 24px; }
.page-header { margin-bottom: 24px; }
.page-title { font-size: 22px; font-weight: 700; margin-bottom: 8px; color: #1e293b; }
.page-desc { color: #64748b; }

.warning-stats { display: flex; gap: 20px; margin-bottom: 24px; }
.stat-card { flex: 1; padding: 20px; background: #fff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.stat-value { font-size: 32px; font-weight: 700; color: #1e293b; }
.stat-label { color: #64748b; margin-top: 8px; }
.stat-card.warning .stat-value { color: #f59e0b; }
.stat-card.success .stat-value { color: #10b981; }

.page-body { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.data-table { width: 100%; }

.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
</style>
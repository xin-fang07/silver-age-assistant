<!--
  ============================================================
  admin/emergency.vue - 求助管理页（需管理员权限）
  银发智能生活助手 - 管理员查看和处理紧急求助
  适老化设计：大字体表格、高亮待处理行、大按钮操作
  功能：查看求助列表、处理求助（填写备注）、分页
  ============================================================
-->
<template>
  <div class="emergency-page">
    <!-- ========== 页面标题 ========== -->
    <div class="page-header">
      <h2 class="page-title">🆘 紧急求助管理</h2>
      <p class="page-subtitle">查看和处理用户发起的紧急求助</p>
    </div>

    <!-- ========== 状态筛选 ========== -->
    <div class="section-card">
    <div class="filter-bar">
      <span class="filter-label">筛选状态：</span>
      <el-radio-group
        v-model="filterStatus"
        size="large"
        @change="onStatusFilterChange"
      >
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button :label="0">待接单</el-radio-button>
        <el-radio-button :label="5">已升级</el-radio-button>
        <el-radio-button :label="1">已接单</el-radio-button>
        <el-radio-button :label="2">处理中</el-radio-button>
        <el-radio-button :label="3">已完成</el-radio-button>
      </el-radio-group>
    </div>

    <!-- ========== 求助列表表格 ========== -->
    <div class="table-area">
      <!-- 加载中状态 -->
      <SkeletonLoader v-if="loading" type="table" :rows="6" />

      <!-- 空数据状态 -->
      <div v-else-if="emergencyList.length === 0" class="empty-area">
        <el-empty description="暂无求助记录" :image-size="80" />
      </div>

      <!-- 求助数据表格 -->
      <template v-else>
        <el-table
          :data="emergencyList"
          style="width: 100%"
          :header-cell-style="tableHeaderStyle"
          :cell-style="tableCellStyle"
          :row-class-name="tableRowClassName"
          stripe
          border
        >
          <!-- ID列 -->
          <el-table-column
            prop="id"
            label="ID"
            width="80"
            align="center"
          />

          <!-- 用户ID -->
          <el-table-column
            prop="userId"
            label="用户ID"
            width="100"
            align="center"
          />

          <!-- 紧急联系人姓名 -->
          <el-table-column
            prop="contactName"
            label="紧急联系人姓名"
            width="140"
            show-overflow-tooltip
          />

          <!-- 紧急联系人电话 -->
          <el-table-column
            prop="contactPhone"
            label="紧急联系人电话"
            width="160"
            align="center"
          >
            <template #default="{ row }">
              <el-button v-if="row.contactPhone && row.contactPhone !== '未设置'" type="primary" link @click="callPhone(row.elderPhone || row.contactPhone)">{{ row.contactPhone }}</el-button>
              <span v-else>-</span>
            </template>
          </el-table-column>

          <el-table-column label="求助位置" width="120" align="center">
            <template #default="{ row }"><template v-if="hasLocation(row)"><el-button type="success" link @click="openMap(row)">地图导航</el-button><div class="location-accuracy">{{ formatLocationAccuracy(row.locationAccuracy) }}</div></template><span v-else>未获取</span></template>
          </el-table-column>

          <!-- 求助内容 -->
          <el-table-column
            prop="helpContent"
            label="求助内容"
            min-width="180"
            show-overflow-tooltip
          >
            <template #default="{ row }">
              {{ row.helpContent || '-' }}
            </template>
          </el-table-column>

          <!-- 状态标签 -->
          <el-table-column
            prop="status"
            label="状态"
            width="110"
            align="center"
          >
            <template #default="{ row }">
              <!-- 待处理：红色标签 | 已处理：绿色标签 -->
              <el-tag
                :type="getStatusMeta(row.status).type"
                size="large"
                effect="dark"
              >
                {{ getStatusMeta(row.status).label }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="通知结果" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.notificationMessage || '平台已收到' }}
            </template>
          </el-table-column>

          <!-- 处理备注（已处理时显示） -->
          <el-table-column
            prop="handleRemark"
            label="处理备注"
            min-width="140"
            show-overflow-tooltip
          >
            <template #default="{ row }">
              {{ row.handleRemark || '-' }}
            </template>
          </el-table-column>

          <!-- 求助时间 -->
          <el-table-column
            prop="createTime"
            label="求助时间"
            width="180"
            align="center"
          >
            <template #default="{ row }">
              {{ formatDateTime(row.createTime || row.createdAt) }}
            </template>
          </el-table-column>

          <!-- 操作列 -->
          <el-table-column
            label="操作"
            width="250"
            align="center"
            fixed="right"
          >
            <template #default="{ row }">
              <el-button
                v-if="row.status === 0 || row.status === 5"
                type="danger"
                size="large"
                @click="advanceStatus(row, 1)"
              >接 单</el-button>
              <el-button
                v-if="row.status === 1"
                type="primary"
                size="large"
                @click="advanceStatus(row, 2)"
              >开始处理</el-button>
              <el-button
                v-if="row.status === 1 || row.status === 2"
                type="success"
                size="large"
                @click="openHandleDialog(row)"
              >完成</el-button>
              <span v-if="row.status === 3" class="handled-text">已完成</span>
              <span v-if="row.status === 4" class="handled-text">已取消</span>
            </template>
          </el-table-column>
        </el-table>

        <!-- ========== 分页组件 ========== -->
        <div class="pagination-area">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            background
            size="large"
            @current-change="fetchEmergency"
            @size-change="onPageSizeChange"
          />
        </div>
      </template>
    </div>
    </div><!-- /.section-card -->

    <!-- ========================================================== -->
    <!-- 处理求助弹窗 -->
    <!-- ========================================================== -->
    <el-dialog
      v-model="dialogVisible"
      title="完成紧急求助"
      width="550px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="handle-dialog-content">
        <!-- 求助基本信息展示 -->
        <el-descriptions :column="1" border size="large" class="info-desc">
          <el-descriptions-item label="求助ID">
            {{ currentEmergency.id }}
          </el-descriptions-item>
          <el-descriptions-item label="求助用户ID">
            {{ currentEmergency.userId }}
          </el-descriptions-item>
          <el-descriptions-item label="联系人">
            {{ currentEmergency.contactName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ currentEmergency.contactPhone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="求助内容">
            {{ currentEmergency.helpContent || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 处理备注输入 -->
        <div class="handle-remark-area">
          <label class="handle-remark-label">完成说明：</label>
          <el-input
            v-model="handleRemark"
            type="textarea"
            :rows="4"
            placeholder="请填写已采取的措施、联系结果等..."
            size="large"
          />
        </div>
      </div>

      <template #footer>
        <el-button size="large" @click="dialogVisible = false">取 消</el-button>
        <el-button
          type="primary"
          size="large"
          :loading="submitLoading"
          @click="submitHandle"
        >
          确认完成
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// ============================================================
// 求助管理页逻辑 - Composition API <script setup>
// ============================================================

import { ref, reactive, onMounted } from 'vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/index'
import { amapMarkerUrl, formatLocationAccuracy } from '@/utils/coordinate'
import { formatFriendlyTime } from '@/utils/friendlyTime'

// ========== 状态筛选 ==========
const filterStatus = ref('')  // 空字符串表示"全部"

// ========== 分页参数 ==========
const currentPage = ref(1)   // 当前页码
const pageSize = ref(10)     // 每页条数
const total = ref(0)         // 总记录数

// ========== 数据列表和加载状态 ==========
const emergencyList = ref([])  // 求助列表
const loading = ref(false)     // 加载状态

// ========== 处理弹窗状态 ==========
const dialogVisible = ref(false)     // 弹窗显示状态
const submitLoading = ref(false)     // 提交加载状态
const handleRemark = ref('')         // 处理备注内容
const currentEmergency = reactive({  // 当前处理的求助记录
  id: null,
  userId: '',
  contactName: '',
  contactPhone: '',
  helpContent: ''
})

const STATUS_META = {
  0: { label: '待接单', type: 'danger' },
  1: { label: '已接单', type: 'warning' },
  2: { label: '处理中', type: 'primary' },
  3: { label: '已完成', type: 'success' },
  4: { label: '已取消', type: 'info' },
  5: { label: '超时升级', type: 'danger' }
}
const getStatusMeta = (status) => STATUS_META[Number(status)] || STATUS_META[0]

// ========== 表格样式配置（适老化大字体） ==========
const tableHeaderStyle = {
  fontSize: '18px',
  fontWeight: '700',
  color: '#2c3e50',
  backgroundColor: '#f5f7fa',
  textAlign: 'center'
}

const tableCellStyle = {
  fontSize: '16px',
  color: '#2c3e50',
  padding: '14px 8px'
}

// ========== 为待处理行添加高亮样式 ==========
const tableRowClassName = ({ row }) => {
  // 待处理的行添加高亮类名
  if (row.status === 0 || row.status === 5) {
    return 'row-pending'
  }
  return ''
}

// ========== 页面挂载时加载求助列表 ==========
onMounted(() => {
  fetchEmergency()
})

// ========== 加载紧急求助列表 ==========
const fetchEmergency = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    // 如果有状态筛选，传递给后端
    if (filterStatus.value !== '') {
      params.status = filterStatus.value
    }

    const res = await adminApi.listEmergency(params)
    if (res.data) {
      emergencyList.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
      total.value = Number(res.total ?? res.data.total ?? emergencyList.value.length)
    }
  } catch (error) {
    console.error('加载求助列表失败：', error)
    ElMessage.error('加载求助列表失败，请稍后重试')
    emergencyList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// ========== 状态筛选变化 ==========
const onStatusFilterChange = () => {
  currentPage.value = 1
  fetchEmergency()
}

// ========== 每页条数变化 ==========
const onPageSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  fetchEmergency()
}

// ========== 打开处理弹窗 ==========
const openHandleDialog = (row) => {
  currentEmergency.id = row.id
  currentEmergency.userId = row.userId
  currentEmergency.contactName = row.contactName
  currentEmergency.contactPhone = row.contactPhone
  currentEmergency.helpContent = row.helpContent || ''
  handleRemark.value = ''
  dialogVisible.value = true
}

// ========== 提交处理求助 ==========
const submitHandle = async () => {
  if (!handleRemark.value.trim()) {
    ElMessage.warning('请填写处理备注信息')
    return
  }

  submitLoading.value = true
  try {
    await adminApi.transitionEmergency(currentEmergency.id, 3, handleRemark.value.trim())
    ElMessage.success('求助已完成')
    dialogVisible.value = false
    // 刷新列表
    await fetchEmergency()
  } catch (error) {
    console.error('处理求助失败：', error)
    ElMessage.error('处理失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

const advanceStatus = async (row, targetStatus) => {
  const action = targetStatus === 1 ? '接单' : '开始处理'
  try {
    await adminApi.transitionEmergency(row.id, targetStatus, action)
    ElMessage.success(`已${action}`)
    await fetchEmergency()
  } catch (error) {
    console.error(`${action}失败：`, error)
  }
}

const callPhone = async phone => {
  if (!phone) return
  if (/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)) {
    window.location.href = `tel:${phone}`
  } else {
    try {
      await navigator.clipboard.writeText(phone)
      ElMessage.success(`电话号码已复制：${phone}\n请在手机上拨打`)
    } catch {
      const textarea = document.createElement('textarea')
      textarea.value = phone
      document.body.appendChild(textarea)
      textarea.select()
      document.execCommand('copy')
      document.body.removeChild(textarea)
      ElMessage.success(`电话号码已复制：${phone}\n请在手机上拨打`)
    }
  }
}
const hasLocation = row => row.latitude != null && row.longitude != null
const openMap = row => window.open(amapMarkerUrl(row, '老人求助位置'), '_blank')

// ========== 格式化日期时间 ==========
const formatDateTime = formatFriendlyTime
</script>

<style scoped>
/*
 * ============================================================
 * 求助管理页样式 - 适老化设计
 * 原则：大字体≥18px、高亮待处理行、清晰操作
 * ============================================================
 */

/* ========== 整体页面容器 ========== */
.emergency-page {
  min-height: 100%;
}

/* ========== 页面标题区域 ========== */
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text-primary, #1e293b);
  margin: 0 0 6px 0;
}

.page-subtitle {
  font-size: 18px;
  color: var(--color-text-secondary, #64748b);
  margin: 0;
}

/* ========== 状态筛选栏 ========== */
.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  background-color: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-label {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
}

/* 单选按钮组大字体 */
.filter-bar :deep(.el-radio-button__inner) {
  font-size: 18px !important;
  padding: 12px 24px !important;
  min-height: 44px;
}

/* ========== 表格区域 ========== */
.table-area {
  background-color: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 加载和空状态 */
.loading-area {
  text-align: center;
  padding: 80px 20px;
  font-size: 18px;
  color: var(--color-text-secondary, #64748b);
}

.loading-area p {
  margin-top: 16px;
}

.empty-area {
  padding: 40px 20px;
}

.empty-area :deep(.el-empty__description) {
  font-size: 18px !important;
}

/* ========== 待处理行高亮 ========== */
:deep(.row-pending) {
  background-color: #fef0f0 !important;
}

:deep(.row-pending td) {
  background-color: #fef0f0 !important;
}

/* ========== 已处理文字 ========== */
.handled-text {
  font-size: 16px;
  color: #67c23a;
  font-weight: 600;
}

/* ========== 分页区域 ========== */
.pagination-area {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.pagination-area :deep(.el-pagination) {
  font-size: 16px !important;
}

.pagination-area :deep(.el-pager li) {
  font-size: 16px !important;
  min-width: 40px;
  height: 40px;
  line-height: 40px;
}

/* ========== 表格内标签和按钮样式 ========== */
:deep(.el-tag--large) {
  font-size: 16px !important;
  padding: 6px 16px !important;
  min-height: 34px;
}

:deep(.el-table .el-button) {
  min-height: 40px;
  font-size: 16px;
}

/* ========== 处理弹窗样式 ========== */
.handle-dialog-content {
  padding: 0 8px;
}

.info-desc {
  margin-bottom: 24px;
}

.info-desc :deep(.el-descriptions__title) {
  font-size: 20px !important;
}

.info-desc :deep(.el-descriptions__label) {
  font-size: 18px !important;
  font-weight: 600;
  color: var(--color-text-secondary, #64748b);
}

.info-desc :deep(.el-descriptions__content) {
  font-size: 18px !important;
  color: var(--color-text-primary, #1e293b);
}

/* 处理备注输入区域 */
.handle-remark-area {
  margin-top: 4px;
}

.handle-remark-label {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary, #1e293b);
  display: block;
  margin-bottom: 10px;
}

/* ========== 弹窗标题适老化 ========== */
:deep(.el-dialog) {
  border-radius: 16px;
}

:deep(.el-dialog__title) {
  font-size: 22px !important;
  font-weight: 700 !important;
}

:deep(.el-dialog__body) {
  padding: 20px 28px;
}

:deep(.el-textarea__inner) {
  font-size: 18px !important;
}
</style>

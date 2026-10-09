<!--
  ============================================================
  admin/users.vue - 用户管理页（需管理员权限）
  银发智能生活助手 - 管理员查看和管

理系统用户
  适老化设计：大字体表格、状态开关、清晰操作
  功能：查看用户列表、搜索用户、启用/禁用用户
  ============================================================
-->
<template>
  <div class="users-page">
    <!-- ========== 页面标题 ========== -->
    <div class="page-header">
      <h2 class="page-title"><svg viewBox="0 0 24 24" width="28" height="28" fill="#6db3f2" style="vertical-align:middle;margin-right:8px"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 4-7 8-7s8 3 8 7"/></svg>用户管理</h2>
      <p class="page-subtitle">查看和管理平台注册用户</p>
    </div>

    <!-- ========== 搜索栏 ========== -->
    <div class="section-card">
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        size="large"
        placeholder="请输入用户名搜索..."
        clearable
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button
        type="primary"
        size="large"
        :loading="loading"
        @click="handleSearch"
      >
        <el-icon><Search /></el-icon>
        搜 索
      </el-button>
    </div>

    <!-- ========== 用户表格 ========== -->
    <div class="table-area">
      <!-- 加载中状态 -->
      <SkeletonLoader v-if="loading" type="table" :rows="6" />

      <!-- 空数据状态 -->
      <div v-else-if="userList.length === 0" class="empty-area">
        <el-empty description="还没有用户数据" :image-size="80" />
      </div>

      <!-- 用户数据表格 -->
      <template v-else>
        <el-table
          :data="userList"
          style="width: 100%"
          :header-cell-style="tableHeaderStyle"
          :cell-style="tableCellStyle"
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

          <!-- 用户名 -->
          <el-table-column
            prop="username"
            label="用户名"
            min-width="140"
            show-overflow-tooltip
          />

          <!-- 昵称 -->
          <el-table-column
            prop="nickname"
            label="昵称"
            min-width="120"
            show-overflow-tooltip
          />

          <!-- 角色（标签展示） -->
          <el-table-column
            prop="role"
            label="角色"
            width="100"
            align="center"
          >
            <template #default="{ row }">
              <!-- FAMILY：绿色标签 | ADMIN：红色标签 -->
              <el-tag
                :type="row.role === 'FAMILY' ? 'success' : row.role === 'ADMIN' ? 'danger' : 'primary'"
                size="large"
                effect="dark"
              >
                {{ row.role === 'FAMILY' ? '家属' : row.role === 'ADMIN' ? '管理员' : '老年用户' }}
              </el-tag>
            </template>
          </el-table-column>

          <!-- 手机号 -->
          <el-table-column
            prop="phone"
            label="手机号"
            width="140"
            align="center"
          >
            <template #default="{ row }">
              {{ row.phone || '-' }}
            </template>
          </el-table-column>

          <!-- 状态（启用/禁用 开关） -->
          <el-table-column
            prop="status"
            label="状态"
            width="110"
            align="center"
          >
            <template #default="{ row }">
              <!-- el-switch 控制启用/禁用 -->
              <el-switch
                v-model="row.activeStatus"
                :active-text="row.activeStatus ? '启用' : '禁用'"
                active-color="#67c23a"
                inactive-color="#f56c6c"
                size="large"
                @change="(val) => handleStatusChange(row, val)" :loading="row._statusLoading"
              />
            </template>
          </el-table-column>

          <!-- 注册时间 -->
          <el-table-column
            prop="createTime"
            label="注册时间"
            width="180"
            align="center"
          >
            <template #default="{ row }">
              {{ formatDateTime(row.createTime || row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="详情" width="100" fixed="right"><template #default="{row}"><el-button type="primary" plain @click="showDetail(row)">查看</el-button></template></el-table-column>
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
            @current-change="fetchUsers"
            @size-change="onPageSizeChange"
          />
        </div>
      </template>
    </div>
    </div><!-- /.section-card -->
    <el-drawer v-model="detailVisible" title="用户详情" size="520px"><template v-if="detail.user"><el-descriptions :column="1" border><el-descriptions-item label="用户名">{{detail.user.username}}</el-descriptions-item><el-descriptions-item label="角色">{{detail.user.role}}</el-descriptions-item><el-descriptions-item label="手机/邮箱">{{detail.user.phone||'-'}} / {{detail.user.email||'-'}}</el-descriptions-item><el-descriptions-item label="最近登录">{{formatDateTime(detail.user.lastLoginTime)||'暂无'}}</el-descriptions-item><el-descriptions-item label="禁用原因">{{detail.user.disabledReason||'-'}}</el-descriptions-item></el-descriptions><el-divider>业务统计</el-divider><div class="detail-stats"><el-statistic title="健康记录" :value="detail.healthCount||0"/><el-statistic title="提醒" :value="detail.reminderCount||0"/><el-statistic title="求助" :value="detail.emergencyCount||0"/></div><el-divider>绑定关系</el-divider><el-table :data="detail.bindings||[]" size="small"><el-table-column prop="family_user_id" label="家属ID"/><el-table-column prop="elder_user_id" label="老人ID"/><el-table-column prop="relation" label="关系"/><el-table-column prop="status" label="状态"/></el-table><el-divider>注销申请</el-divider><p>{{detail.deletionTicket?`${detail.deletionTicket.status}：${detail.deletionTicket.content}`:'无注销申请'}}</p><el-divider>最近操作日志</el-divider><el-timeline><el-timeline-item v-for="l in detail.logs||[]" :key="l.id" :timestamp="formatDateTime(l.createTime)">{{l.operation}} · HTTP {{l.statusCode}}</el-timeline-item></el-timeline></template></el-drawer>
  </div>
</template>

<script setup>
// ============================================================
// 用户管理页逻辑 - Composition API <script setup>
// ============================================================

import { ref, onMounted } from 'vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/index'
import { formatFriendlyTime } from '@/utils/friendlyTime'

// ========== 搜索关键词 ==========
const searchKeyword = ref('')

// ========== 分页参数 ==========
const currentPage = ref(1)   // 当前页码
const pageSize = ref(10)     // 每页条数
const total = ref(0)         // 总记录数

// ========== 数据列表和加载状态 ==========
const userList = ref([])     // 用户列表
const loading = ref(false)   // 加载状态
const detailVisible = ref(false)
const detail = ref({})
const showDetail = async row => { detail.value=(await adminApi.userDetail(row.id)).data||{}; detailVisible.value=true }

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

// ========== 页面挂载时加载用户列表 ==========
onMounted(() => {
  fetchUsers()
})

// ========== 加载用户列表 ==========
const fetchUsers = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    // 如果有搜索关键词，传递给后端
    if (searchKeyword.value.trim()) {
      params.keyword = searchKeyword.value.trim()
    }

    const res = await adminApi.listUsers(params)
    if (res.data) {
      const records = Array.isArray(res.data) ? res.data : (res.data.records || [])
      // 为每条记录设置 activeStatus（用于 el-switch 绑定）
      userList.value = records.map(user => ({
        ...user,
        activeStatus: Number(user.status) === 1 || user.status === 'ACTIVE' || user.status === 'ENABLED' || user.enabled
      }))
      total.value = Number(res.total ?? res.data.total ?? userList.value.length)
    }
  } catch (error) {
    console.error('加载用户列表失败：', error)
    ElMessage.error('加载用户列表失败，请稍后重试')
    userList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// ========== 搜索用户 ==========
const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

// ========== 每页条数变化 ==========
const onPageSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  fetchUsers()
}

// ========== 切换用户启用/禁用状态 ==========
const handleStatusChange = async (row, newStatus) => {
    if (row._statusLoading) return
    row._statusLoading = true
  // 确定目标状态字符串
  const targetStatus = newStatus ? 'ACTIVE' : 'DISABLED'

  try {
    let reason = ''
    if (!newStatus) {
      const result = await ElMessageBox.prompt('请填写禁用原因，该原因会通知用户','禁用用户',{confirmButtonText:'确认禁用',cancelButtonText:'取消',inputType:'textarea',inputValidator:v=>!!v?.trim()||'禁用原因不能为空'})
      reason = result.value.trim()
    }
    await adminApi.updateUserStatus(row.id, targetStatus, reason)
    ElMessage.success(`用户"${row.username}"已${newStatus ? '启用' : '禁用'}`)
  } catch (error) {
    console.error('更新用户状态失败：', error)
    ElMessage.error('状态更新失败，请稍后重试')
    // 状态回滚
    row.activeStatus = !newStatus
  } finally {
    row._statusLoading = false
  }
}

// ========== 格式化日期时间 ==========
const formatDateTime = formatFriendlyTime
</script>

<style scoped>
/*
 * ============================================================
 * 用户管理页样式 - 适老化设计
 * 原则：大字体≥18px、大按钮≥48px、高对比度、简洁布局
 * ============================================================
 */

/* ========== 整体页面容器 ========== */
.users-page {
  min-height: 100%;
}
.detail-stats{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;text-align:center}

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

/* ========== 搜索栏 ========== */
.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 240px;
}

.search-input :deep(.el-input__inner) {
  font-size: 18px !important;
  min-height: 48px;
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

/* ========== 表格内标签样式 ========== */
:deep(.el-tag--large) {
  font-size: 16px !important;
  padding: 6px 16px !important;
  min-height: 34px;
}

/* ========== 开关样式（适老化） ========== */
:deep(.el-switch__label) {
  font-size: 16px !important;
  font-weight: 600 !important;
}

:deep(.el-switch__core) {
  min-width: 52px !important;
  height: 28px !important;
}
</style>

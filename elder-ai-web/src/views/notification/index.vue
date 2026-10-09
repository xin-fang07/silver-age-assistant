<template>
  <div class="notification-page">
    <div class="page-header">
      <h2 class="page-title">通知中心</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleReadAll" :loading="loading">
          全部标记为已读
        </el-button>
      </div>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="filterType" size="large">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="SOS">紧急求助</el-radio-button>
        <el-radio-button value="SYSTEM">系统通知</el-radio-button>
      </el-radio-group>
      <div class="filter-info">
        共 {{ notifications.length }} 条，未读 {{ unreadCount }} 条
      </div>
    </div>

    <AsyncState :state="pageState" :error-message="loadError" empty-title="暂无通知" @retry="loadNotifications"><div class="notification-list">
      <div
        v-for="item in notifications"
        :key="item.id"
        class="notification-item"
        :class="{ unread: item.isRead === 0 }"
        @click="handleClick(item)"
      >
        <div class="item-icon">
          <el-icon v-if="item.type === 'SOS'" class="icon-sos"><Bell /></el-icon>
          <el-icon v-else class="icon-system"><InfoFilled /></el-icon>
        </div>
        <div class="item-content">
          <div class="item-title">
            {{ item.title }}
            <span v-if="item.type === 'SOS'" class="type-tag sos">紧急</span>
            <span v-else class="type-tag system">系统</span>
          </div>
          <div class="item-desc">{{ item.content }}</div>
          <div class="item-time">{{ formatTime(item.createTime) }}</div>
        </div>
        <div class="item-status">
          <span v-if="item.isRead === 0" class="unread-dot"></span>
        </div>
      </div>
    </div></AsyncState>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell, InfoFilled } from '@element-plus/icons-vue'
import { notificationApi } from '@/api/index'
import { formatFriendlyTime } from '@/utils/friendlyTime'
import AsyncState from '@/components/AsyncState.vue'

const router = useRouter()

const notifications = ref([])
const loading = ref(false)
const pageState = ref('loading')
const loadError = ref('')
const filterType = ref('')

const unreadCount = computed(() => {
  return notifications.value.filter(n => n.isRead === 0).length
})

const loadNotifications = async () => {
  loading.value = true
  pageState.value = 'loading'; loadError.value = ''
  try {
    const res = await notificationApi.list(filterType.value)
    notifications.value = res.data || []
    pageState.value = notifications.value.length ? 'success' : 'empty'
  } catch (error) {
    pageState.value = 'error'; loadError.value = error?.message || '通知加载失败'
  } finally {
    loading.value = false
  }
}

const handleReadAll = async () => {
  try {
    await notificationApi.readAll()
    ElMessage.success('已全部标记为已读')
    loadNotifications()
  } catch (error) {
    console.error('标记已读失败：', error)
  }
}

const handleClick = async (item) => {
  if (item.isRead === 0) {
    try {
      await notificationApi.read(item.id)
      item.isRead = 1
    } catch (error) {
      console.error('标记已读失败：', error)
    }
  }
  if (item.refId && item.type === 'SOS') {
    router.push(`/emergency`)
  }
}

const formatTime = formatFriendlyTime

onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.notification-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #f8fafc;
  border-radius: 12px;
}

.filter-info {
  font-size: 16px;
  color: #64748b;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 20px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s ease;
}

.notification-item:hover {
  border-color: #cbd5e1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.notification-item.unread {
  background: #fef3c7;
  border-color: #fbbf24;
}

.item-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  margin-right: 16px;
  flex-shrink: 0;
}

.icon-sos {
  font-size: 24px;
  color: #dc2626;
}

.icon-system {
  font-size: 24px;
  color: #3b82f6;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.type-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.type-tag.sos {
  background: #fee2e2;
  color: #dc2626;
}

.type-tag.system {
  background: #dbeafe;
  color: #3b82f6;
}

.item-desc {
  font-size: 16px;
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 8px;
}

.item-time {
  font-size: 14px;
  color: #94a3b8;
}

.item-status {
  margin-left: 16px;
}

.unread-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  background: #dc2626;
  border-radius: 50%;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  color: #cbd5e1;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 18px;
  color: #94a3b8;
}
</style>

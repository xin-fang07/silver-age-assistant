<!--
  ============================================================
  news/detail.vue - 养老资讯详情页
  银发智能生活助手 - 查看资讯的完整内容
  适老化设计：大字体标题（24px）、大行距（line-height:2）、
  清晰的元信息展示、返回按钮
  功能：加载资讯详情、HTML 内容渲染、返回列表
  ============================================================
-->
<template>
  <div class="news-detail-page">
    <!-- ========== 返回按钮 ========== -->
    <div class="back-area">
      <el-button
        type="default"
        size="large"
        @click="goBack"
        round
      >
        <el-icon><ArrowLeft /></el-icon>
        返回资讯列表
      </el-button>
    </div>

    <!-- ========== 加载中状态 ========== -->
    <SkeletonLoader v-if="loading" type="card" :rows="4" />

    <!-- ========== 加载失败状态 ========== -->
    <div v-else-if="loadError" class="error-area">
      <el-result
        icon="error"
        title="加载失败"
        :sub-title="loadError"
      >
        <template #extra>
          <el-button type="primary" size="large" @click="fetchDetail">
            重新加载
          </el-button>
          <el-button size="large" @click="goBack">
            返回列表
          </el-button>
        </template>
      </el-result>
    </div>

    <!-- ========== 资讯内容 ========== -->
    <div v-else-if="detail" class="news-content">
      <!-- 标题行：标题 + 朗读全文按钮 -->
      <div class="detail-title-row">
        <h1 class="detail-title">{{ detail.title }}</h1>
        <button class="read-aloud-btn" @click="readDetail">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z"/>
          </svg>
          <span>朗读全文</span>
        </button>
      </div>

      <!-- 元信息行：发布时间 | 类型标签 | 浏览量 -->
      <div class="detail-meta">
        <span class="meta-time">
          <el-icon><Clock /></el-icon>
          {{ formatDateTime(detail.publishTime || detail.createTime || detail.createdAt) }}
        </span>
        <el-tag
          v-if="detail.category"
          :type="getCategoryTagType(detail.category)"
          size="large"
          effect="dark"
        >
          {{ detail.category }}
        </el-tag>
        <span v-if="detail.viewCount != null" class="meta-views">
          <el-icon><View /></el-icon>
          {{ detail.viewCount }} 次阅读
        </span>
      </div>

      <img v-if="detailCover" :src="detailCover" :alt="`${detail.title}的资讯配图`" class="detail-cover" />

      <!-- 分割线 -->
      <el-divider />

      <!-- 资讯正文（v-html 渲染 HTML 内容，大行距，已做XSS过滤） -->
      <div class="detail-body" v-html="sanitizedContent"></div>
    </div>
  </div>
</template>

<script setup>
// ============================================================
// 资讯详情页逻辑 - Composition API <script setup>
// ============================================================

import { ref, computed, onMounted } from 'vue'
import SkeletonLoader from '@/components/SkeletonLoader.vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { newsApi } from '@/api/index'
import DOMPurify from 'dompurify'
import { useVoiceKing } from '@/composables/useVoiceKing'
import { formatFriendlyTime } from '@/utils/friendlyTime'

// ========== 语音朗读 ==========
const { speak } = useVoiceKing()

// 富文本 XSS 清洗：DOMPurify 白名单（纵深防御；后端入库前已用 Jsoup 清洗一遍）
const sanitizeHtml = (html) => {
  if (!html) return ''
  return DOMPurify.sanitize(html)
}

// ========== 路由实例 ==========
const router = useRouter()
const route = useRoute()

// ========== 状态变量 ==========
const detail = ref(null)
const loading = ref(false)
const loadError = ref('')

// XSS过滤后的安全内容
const sanitizedContent = computed(() => sanitizeHtml(detail.value?.content || detail.value?.body || ''))
const DETAIL_COVERS = ['/images/news/elderly-exercise.jpg','/images/news/elderly-park-walk.jpg','/images/news/elderly-community-fitness.jpg']
const detailCover = computed(() => detail.value
  ? (detail.value.coverImage || detail.value.coverUrl || DETAIL_COVERS[Math.abs(Number(detail.value.id) || 0) % DETAIL_COVERS.length])
  : '')

// ========== 页面挂载时根据路由参数加载详情 ==========
onMounted(() => {
  const id = route.params.id
  if (id) {
    fetchDetail(id)
  } else {
    loadError.value = '资讯 ID 不存在'
  }
})

// ========== 加载资讯详情 ==========
const fetchDetail = async (idOverride) => {
  const id = idOverride || route.params.id
  if (!id) {
    loadError.value = '资讯 ID 不存在'
    return
  }

  loading.value = true
  loadError.value = ''

  try {
    const res = await newsApi.getDetail(id)
    if (res.data) {
      detail.value = res.data
    } else {
      loadError.value = '未找到该资讯'
    }
  } catch (error) {
    console.error('加载资讯详情失败：', error)
    loadError.value = '加载资讯详情失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

// ========== 返回资讯列表 ==========
const goBack = () => {
  // 优先使用路由回退
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/care/news')
  }
}

// ========== 朗读资讯全文 ==========
const readDetail = () => {
  if (!detail.value) return
  // 提取纯文本（去掉 HTML 标签）
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = sanitizedContent.value
  const bodyText = tempDiv.textContent || tempDiv.innerText || ''
  const fullText = `${detail.value.title}。${bodyText}`
  speak(fullText, '资讯详情')
}

// ========== 获取分类标签颜色 ==========
const getCategoryTagType = (category) => {
  const map = {
    '健康养生': 'success',
    '政策解读': 'warning',
    '社区活动': '',
  }
  return map[category] || 'info'
}

// ========== 格式化日期时间 ==========
const formatDateTime = formatFriendlyTime
</script>

<style scoped>
/*
 * ============================================================
 * 资讯详情页样式 - 适老化设计
 * 原则：大字体（标题24px、正文18px）、大行距 line-height:2、
 *       舒适的阅读宽度、清晰的返回按钮
 * ============================================================
 */

/* ========== 整体页面容器 ========== */
.news-detail-page {
  min-height: 100%;
  max-width: 860px;
  margin: 0 auto;
}

/* ========== 返回按钮区域 ========== */
.back-area {
  margin-bottom: 24px;
}

.back-area .el-button {
  min-height: 48px;
  font-size: 18px;
  padding: 12px 28px;
}

/* ========== 加载和错误状态 ========== */
.loading-area {
  text-align: center;
  padding: 80px 20px;
  font-size: 18px;
  color: #909399;
}

.loading-area p {
  margin-top: 16px;
}

.error-area {
  padding: 60px 20px;
}

/* ========== 资讯内容区域 ========== */
.news-content {
  background-color: #fff;
  border-radius: 16px;
  padding: 32px 36px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

/* 资讯标题（24px 大字体加粗） */
.detail-title {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 20px 0;
  line-height: 1.5;
  word-break: break-word;
  flex: 1;
}

/* 标题行：标题 + 朗读按钮并排 */
.detail-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

/* 朗读全文按钮 */
.read-aloud-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, #409eff, #1d4ed8);
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 8px 18px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  flex-shrink: 0;
}

.read-aloud-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

/* 元信息行 */
.detail-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}
.detail-cover{display:block;width:100%;max-height:380px;margin:18px 0 8px;border-radius:14px;object-fit:cover;background:#eef2f7}

/* 发布时间 */
.meta-time {
  font-size: 17px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-time .el-icon {
  font-size: 19px;
}

/* 浏览量 */
.meta-views {
  font-size: 17px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-views .el-icon {
  font-size: 19px;
}

/* 标签样式 */
.detail-meta :deep(.el-tag) {
  font-size: 16px !important;
  padding: 6px 16px !important;
  min-height: 34px;
}

/* 分割线 */
:deep(.el-divider) {
  margin: 20px 0;
}

/* 资讯正文（v-html 内容，大行距） */
.detail-body {
  font-size: 18px;
  color: #2c3e50;
  line-height: 2;
  word-break: break-word;
}

/* ========== 正文内各种HTML元素的大字体适配 ========== */
.detail-body :deep(h1) {
  font-size: 26px;
  line-height: 2;
  margin: 20px 0 16px;
}

.detail-body :deep(h2) {
  font-size: 24px;
  line-height: 2;
  margin: 18px 0 14px;
}

.detail-body :deep(h3) {
  font-size: 22px;
  line-height: 2;
  margin: 16px 0 12px;
}

.detail-body :deep(h4),
.detail-body :deep(h5),
.detail-body :deep(h6) {
  font-size: 20px;
  line-height: 2;
  margin: 14px 0 10px;
}

.detail-body :deep(p) {
  margin: 12px 0;
  font-size: 18px;
  line-height: 2;
}

.detail-body :deep(ul),
.detail-body :deep(ol) {
  padding-left: 28px;
  margin: 12px 0;
}

.detail-body :deep(li) {
  font-size: 18px;
  line-height: 2;
  margin: 6px 0;
}

.detail-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 12px 0;
}

.detail-body :deep(table) {
  font-size: 16px;
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

.detail-body :deep(th),
.detail-body :deep(td) {
  border: 1px solid #dcdfe6;
  padding: 10px 14px;
  text-align: left;
}

.detail-body :deep(th) {
  background-color: #f5f7fa;
  font-weight: 700;
}

.detail-body :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding: 10px 20px;
  margin: 16px 0;
  background-color: #ecf5ff;
  border-radius: 0 8px 8px 0;
}

.detail-body :deep(code) {
  background-color: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 16px;
}

.detail-body :deep(pre) {
  background-color: #f5f5f5;
  padding: 16px 20px;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 16px;
  line-height: 1.6;
}

/* ========== Element Plus 结果组件字体适配 ========== */
.error-area :deep(.el-result__title) {
  font-size: 22px !important;
}

.error-area :deep(.el-result__subtitle) {
  font-size: 18px !important;
  margin-top: 8px;
}
</style>

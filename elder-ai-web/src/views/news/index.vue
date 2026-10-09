<!--
  ============================================================
  news/index.vue - 养老资讯列表页
  银发智能生活助手 - 查看养老、健康、政策等相关资讯
  适老化设计：大字体卡片列表、分类筛选、大分页
  功能：按分类查看资讯、分页加载、点击查看详情
  ============================================================
-->
<template>
  <div class="news-page">
    <!-- ========== 页面标题 ========== -->
    <div class="page-header">
      <h2 class="page-title"><svg viewBox="0 0 24 24" width="28" height="28" fill="#4a90d9" style="vertical-align:middle;margin-right:8px"><path d="M22 3H2v18h20V3zM4 5h16v2H4V5zm0 4h16v2H4V9zm0 4h10v2H4v-2zm0 4h16v2H4v-2z"/></svg>养老资讯</h2>
      <p class="page-subtitle">关注健康养生、政策解读和社区活动资讯</p>
    </div>

    <!-- ========== 资讯分类 Tabs ========== -->
    <el-tabs
      v-model="activeCategory"
      class="news-tabs"
      @tab-change="onCategoryChange"
    >
      <el-tab-pane label="全部" name="" />
      <el-tab-pane label="健康养生" name="健康养生" />
      <el-tab-pane label="政策解读" name="政策解读" />
      <el-tab-pane label="社区活动" name="社区活动" />
    </el-tabs>

    <!-- ========== 加载中状态 ========== -->
    <AsyncState :state="pageState" :error-message="loadError" type="card" empty-title="暂时还没有养老资讯" empty-description="敬请期待新的健康、政策和社区资讯" @retry="fetchNews"><div class="news-list">
      <el-card
        v-for="item in newsList"
        :key="item.id"
        shadow="hover"
        class="news-card"
        @click="goToDetail(item.id)"
      >
        <div class="card-body">
          <!-- 封面图（若后端返回封面则展示，否则展示渐变占位） -->
          <div
            class="news-cover"
            :style="coverStyle(item)"
            @click.stop="goToDetail(item.id)"
          >
            <span v-if="!item.cover && !item.coverUrl && !item.imageUrl" class="news-cover-ph">
              {{ (item.category || '资讯').slice(0, 2) }}
            </span>
          </div>

          <!-- 右侧内容区 -->
          <div class="card-main">
            <!-- 资讯标题 + 朗读按钮 -->
            <div class="news-title-row">
              <h3 class="news-title" @click.stop="goToDetail(item.id)">{{ item.title }}</h3>
              <!-- 朗读按钮：点击朗读标题+摘要，阻止冒泡避免跳转详情 -->
              <button
                class="read-aloud-btn"
                @click.stop="readNews(item)"
                title="点击朗读"
              >
                <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
                  <path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z"/>
                </svg>
                <span>朗读</span>
              </button>
            </div>
            <!-- 资讯摘要（灰色，16px） -->
            <p v-if="item.summary" class="news-summary">{{ item.summary }}</p>
            <!-- 元信息行：类型标签 + 发布时间 + 浏览量 -->
            <div class="news-meta">
              <el-tag
                :type="getCategoryTagType(item.category)"
                size="large"
                effect="dark"
              >
                {{ item.category || '综合资讯' }}
              </el-tag>
              <span class="news-time">
                <el-icon><Clock /></el-icon>
                {{ formatDateTime(item.publishTime || item.createTime || item.createdAt) }}
              </span>
              <span v-if="item.viewCount != null" class="news-views">
                <el-icon><View /></el-icon>
                {{ item.viewCount }} 次阅读
              </span>
            </div>
            <!-- 操作行：阅读全文 / 收藏 / 点赞 -->
            <div class="news-actions">
              <button class="news-action-btn read-full-btn" @click.stop="goToDetail(item.id)">
                <el-icon><Document /></el-icon>
                阅读全文
              </button>
              <button
                class="news-action-btn fav-btn"
                :class="{ active: isFav(item.id) }"
                @click.stop="toggleFav(item)"
              >
                <el-icon><Star :filled="isFav(item.id)" /></el-icon>
                {{ isFav(item.id) ? '已收藏' : '收藏' }}
              </button>
              <button
                class="news-action-btn like-btn"
                :class="{ active: isLiked(item.id) }"
                @click.stop="toggleLike(item)"
              >
                <el-icon><Pointer /></el-icon>
                {{ isLiked(item.id) ? '已赞' : '点赞' }}
              </button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- ========== 分页组件 ========== -->
      <div class="pagination-area">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 30]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          size="large"
          @current-change="fetchNews"
          @size-change="onPageSizeChange"
        />
      </div>
    </div></AsyncState>
  </div>
</template>

<script setup>
// ============================================================
// 养老资讯列表页逻辑 - Composition API <script setup>
// ============================================================

import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { newsApi } from '@/api/index'
import { useVoiceKing } from '@/composables/useVoiceKing'
import AsyncState from '@/components/AsyncState.vue'
import { formatFriendlyTime } from '@/utils/friendlyTime'

// ========== 语音王 ==========
const { speak, announceTab } = useVoiceKing()

// ========== 路由实例 ==========
const router = useRouter()

// ========== 分类筛选 ==========
const activeCategory = ref('')  // 当前选中的分类，空字符串表示"全部"

// ========== 分页参数 ==========
const currentPage = ref(1)   // 当前页码
const pageSize = ref(10)     // 每页条数
const total = ref(0)         // 总记录数

// ========== 数据列表和加载状态 ==========
const newsList = ref([])     // 资讯列表
const loading = ref(false)   // 加载状态
const pageState = ref('loading')
const loadError = ref('')

// ========== 页面挂载时加载资讯 ==========
onMounted(() => {
  fetchNews()
  // 初始化收藏/点赞集合（本地存储，不改动后端与数据库）
  favSet.value = loadSet(FAV_KEY)
  likeSet.value = loadSet(LIKE_KEY)
})

// ========== 收藏 / 点赞（localStorage 本地存储） ==========
const FAV_KEY = 'elder_ai_news_fav'
const LIKE_KEY = 'elder_ai_news_like'

const favSet = ref(new Set())
const likeSet = ref(new Set())

function loadSet(key) {
  try {
    const arr = JSON.parse(localStorage.getItem(key) || '[]')
    return new Set(Array.isArray(arr) ? arr : [])
  } catch {
    return new Set()
  }
}

function saveSet(key, set) {
  try {
    localStorage.setItem(key, JSON.stringify([...set]))
  } catch (e) {
    /* 忽略写入异常（如隐私模式） */
  }
}

const isFav = (id) => favSet.value.has(id)
const isLiked = (id) => likeSet.value.has(id)

function toggleFav(item) {
  const id = item.id
  if (favSet.value.has(id)) {
    favSet.value.delete(id)
    ElMessage.success('已取消收藏')
  } else {
    favSet.value.add(id)
    ElMessage.success('已加入收藏')
  }
  // 触发响应式更新
  favSet.value = new Set(favSet.value)
  saveSet(FAV_KEY, favSet.value)
}

function toggleLike(item) {
  const id = item.id
  if (likeSet.value.has(id)) {
    likeSet.value.delete(id)
    ElMessage.success('已取消点赞')
  } else {
    likeSet.value.add(id)
    ElMessage.success('感谢点赞！')
  }
  likeSet.value = new Set(likeSet.value)
  saveSet(LIKE_KEY, likeSet.value)
}

// ========== 加载资讯列表 ==========
const fetchNews = async () => {
  loading.value = true
  pageState.value = 'loading'; loadError.value = ''
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    // 如果选择了分类，传递分类参数
    if (activeCategory.value) {
      params.category = activeCategory.value
    }

    const res = await newsApi.list(params)
    if (res.data) {
      const records = Array.isArray(res.data) ? res.data : (res.data.records || [])
      newsList.value = records.map(normalizeNews)
      total.value = Number(res.total ?? res.data.total ?? newsList.value.length)
      pageState.value = newsList.value.length ? 'success' : 'empty'
    }
  } catch (error) {
    console.error('加载资讯列表失败：', error)
    ElMessage.error('加载资讯列表失败，请稍后重试')
    pageState.value = 'error'; loadError.value = error?.message || '资讯加载失败'
  } finally {
    loading.value = false
  }
}

// ========== 切换分类 ==========
const onCategoryChange = () => {
  // 切换分类时重置到第1页
  currentPage.value = 1
  fetchNews()

  // 语音播报当前分类
  const category = activeCategory.value
  if (!category) {
    announceTab('全部资讯。查看所有养老资讯')
  } else {
    announceTab(`${category}。查看${category}类资讯`)
  }
}

// ========== 每页条数变化 ==========
const onPageSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  fetchNews()
}

// ========== 跳转到资讯详情页 ==========
const goToDetail = (id) => {
  router.push(`/news/${id}`)
}

// ========== 朗读资讯 ==========
const readNews = (item) => {
  const text = `${item.title}。${item.summary || ''}`
  speak(text, '养老资讯')
}

// ========== 获取分类标签颜色 ==========
const getCategoryTagType = (category) => {
  const map = {
    '健康养生': 'success',   // 绿色
    '政策解读': 'warning',   // 橙色
    '社区活动': '',           // 蓝色（primary）
  }
  return map[category] || 'info'
}

// ========== 格式化日期时间 ==========
const formatDateTime = formatFriendlyTime

// ========== 封面图样式（优先后端返回封面，否则渐变占位） ==========
const CATEGORY_GRADIENT = {
  '健康养生': 'linear-gradient(135deg, #52b788, #2f9e6e)',
  '政策解读': 'linear-gradient(135deg, #f0a04b, #e8590c)',
  '社区活动': 'linear-gradient(135deg, #4a90d9, #1d4ed8)',
}
const TYPE_CATEGORY = { HEALTH: '健康养生', POLICY: '政策解读', LIFE: '生活百科', ACTIVITY: '社区活动' }
const LOCAL_COVERS = ['/images/news/elderly-exercise.jpg','/images/news/elderly-park-walk.jpg','/images/news/elderly-community-fitness.jpg']
const normalizeNews = (item) => ({
  ...item,
  category: item.category || TYPE_CATEGORY[item.newsType] || item.newsType || '综合资讯',
  coverUrl: item.coverUrl || item.coverImage || item.cover || item.imageUrl || LOCAL_COVERS[Math.abs(Number(item.id) || 0) % LOCAL_COVERS.length]
})
const coverStyle = (item) => {
  const url = item.coverUrl || item.coverImage || item.cover || item.imageUrl
  if (url) {
    return { backgroundImage: `url(${url})` }
  }
  return { backgroundImage: CATEGORY_GRADIENT[item.category] || 'linear-gradient(135deg, #94a3b8, #64748b)' }
}
</script>

<style scoped>
/*
 * ============================================================
 * 养老资讯列表页样式 - 适老化设计
 * 原则：大字体≥18px、大卡片、清晰分类、舒适间距
 * ============================================================
 */

/* ========== 整体页面容器 ========== */
.news-page {
  min-height: 100%;
  max-width: 900px;
  margin: 0 auto;
}

/* ========== 页面标题区域 ========== */
.page-header {
  margin-bottom: 16px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary, #1e293b);
  margin: 0 0 6px 0;
}

.page-subtitle {
  font-size: 18px;
  color: var(--color-text-secondary, #64748b);
  margin: 0;
}

/* ========== 分类 Tabs 样式 ========== */
.news-tabs :deep(.el-tabs__item) {
  font-size: 20px !important;
  font-weight: 600 !important;
  min-height: 48px;
  line-height: 48px;
  padding: 0 24px !important;
}

/* ========== 加载和空状态 ========== */
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
  padding: 60px 20px;
}

.empty-area :deep(.el-empty__description) {
  font-size: 18px !important;
}

/* ========== 资讯卡片列表 ========== */
.news-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 18px;
}

/* 单条资讯卡片 */
.news-card {
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #ebeef5;
}

.news-card:hover {
  border-color: var(--color-primary, #4a90d9);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.card-body {
  padding: 12px 14px 14px;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 12px;
}

/* 封面图 */
.news-cover {
  width: 100%;
  min-height: 168px;
  border-radius: 14px;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  flex-shrink: 0;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  transition: transform 0.25s ease;
}

.news-cover:hover {
  transform: scale(1.02);
}

/* 封面占位文字（无图时显示分类简称） */
.news-cover-ph {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 2px;
  text-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

/* 右侧内容列 */
.card-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.news-title {
  cursor: pointer;
}

.news-title:hover {
  color: var(--color-primary, #4a90d9);
  text-decoration: underline;
}

/* 资讯标题（20px 加粗） */
.news-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text-primary, #1e293b);
  margin: 0 0 10px 0;
  line-height: 1.5;
  /* 支持换行 */
  word-break: break-word;
  flex: 1;
}

/* 标题行：标题 + 朗读按钮并排 */
.news-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

/* 朗读按钮 */
.read-aloud-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #409eff, #1d4ed8);
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 6px 14px;
  font-size: 15px;
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

.read-aloud-btn:active {
  transform: scale(0.98);
}

/* 资讯摘要（16px 灰色） */
.news-summary {
  font-size: 16px;
  color: var(--color-text-secondary, #64748b);
  margin: 0 0 14px 0;
  line-height: 1.7;
  /* 超过两行显示省略号 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 元信息行 */
.news-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

/* 发布时间 */
.news-time {
  font-size: 16px;
  color: var(--color-text-secondary, #64748b);
  display: flex;
  align-items: center;
  gap: 4px;
}

.news-time .el-icon {
  font-size: 18px;
}

/* 浏览量 */
.news-views {
  font-size: 16px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.news-views .el-icon {
  font-size: 18px;
}

/* ========== 操作行：阅读全文 / 收藏 / 点赞 ========== */
.news-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 14px;
  flex-wrap: wrap;
}

.news-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  border: 1.5px solid #e2e8f0;
  background: #fff;
  color: #475569;
  border-radius: 20px;
  padding: 7px 16px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.news-action-btn .el-icon {
  font-size: 18px;
}

.news-action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
}

.news-action-btn:active {
  transform: scale(0.97);
}

/* 阅读全文：主色描边 */
.read-full-btn {
  border-color: var(--color-primary, #4a90d9);
  color: var(--color-primary, #4a90d9);
}

.read-full-btn:hover {
  background: var(--color-primary, #4a90d9);
  color: #fff;
}

/* 收藏：激活变金色 */
.fav-btn.active {
  border-color: #f0a04b;
  background: #fff7ed;
  color: #e8590c;
}

/* 点赞：激活变红 */
.like-btn.active {
  border-color: #ef4444;
  background: #fef2f2;
  color: #ef4444;
}

/* ========== 标签样式（适老化） ========== */
.news-meta :deep(.el-tag) {
  font-size: 16px !important;
  padding: 6px 16px !important;
  min-height: 34px;
  line-height: 34px;
}

/* ========== 分页区域 ========== */
.pagination-area {
  margin-top: 32px;
  display: flex;
  justify-content: center;
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

/* ========== 卡片统一样式覆盖 ========== */
:deep(.el-card__body) {
  padding: 20px 24px;
}

/* ========== 响应式：窄屏封面图换行到顶部 ========== */
@media (max-width: 560px) {
  .card-body {
    flex-direction: column;
  }

  .news-cover {
    width: 100%;
    min-height: 150px;
  }
}
</style>

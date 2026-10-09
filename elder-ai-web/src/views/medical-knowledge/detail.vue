<template>
  <div class="kd-page" v-loading="loading">
    <el-button text :icon="ArrowLeft" @click="goBack" class="kd-back">返回列表</el-button>

    <h1 class="kd-title">{{ article.title }}</h1>
    <div class="kd-meta">
      <el-tag size="small">{{ article.categoryLabel || article.category }}</el-tag>
      <span v-if="article.author">{{ article.author }}</span>
      <span>{{ formatTime(article.createTime) }}</span>
      <span>👁 {{ article.viewCount || 0 }}</span>
    </div>

    <div class="kd-content" v-html="safeContent"></div>

    <el-divider />

    <div class="kd-ask">
      <h3>AI 健康问答</h3>
      <p class="kd-ask-tip">基于大模型的健康答疑（知识库智能问答能力持续完善中）</p>
      <div class="kd-ask-input">
        <el-input v-model="question" placeholder="例如：高血压老人饮食要注意什么？" @keyup.enter="ask" />
        <el-button type="primary" :loading="asking" @click="ask">提问</el-button>
      </div>
      <div class="kd-ask-answer" v-if="answer">
        <strong>答：</strong>{{ answer }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import DOMPurify from 'dompurify'
import { knowledgeApi } from '@/api/index'
import { formatFriendlyTime } from '@/utils/friendlyTime'

const route = useRoute()
const router = useRouter()
const article = ref({})
const loading = ref(false)
const question = ref('')
const answer = ref('')
const asking = ref(false)
const formatTime = (t) => (t ? formatFriendlyTime(t) : '-')
const safeContent = computed(() => DOMPurify.sanitize(article.value.content || ''))

const load = async () => {
  loading.value = true
  try {
    const r = await knowledgeApi.detail(route.params.id)
    article.value = r.data || {}
  } finally {
    loading.value = false
  }
}
const ask = async () => {
  if (!question.value.trim()) return ElMessage.warning('请输入问题')
  asking.value = true
  try {
    const r = await knowledgeApi.ask({ question: question.value.trim() })
    answer.value = (r.data && r.data.answer) || '暂无回答'
  } catch (e) {
    ElMessage.error('问答失败，请稍后再试')
  } finally {
    asking.value = false
  }
}
const goBack = () => router.push('/care/medical-knowledge')

onMounted(load)
</script>

<style scoped>
.kd-page { padding: 20px; max-width: 820px; margin: 0 auto; }
.kd-back { margin-bottom: 8px; }
.kd-title { font-size: 24px; margin: 4px 0 12px; }
.kd-meta { display: flex; align-items: center; gap: 12px; color: #999; font-size: 13px; margin-bottom: 18px; }
.kd-content { line-height: 1.8; font-size: 15px; color: #333; }
.kd-content :deep(img) { max-width: 100%; }
.kd-ask { background: #f7f9ff; border-radius: 10px; padding: 16px 18px; }
.kd-ask h3 { margin: 0 0 4px; }
.kd-ask-tip { color: #999; font-size: 12px; margin: 0 0 12px; }
.kd-ask-input { display: flex; gap: 10px; }
.kd-ask-answer { margin-top: 14px; line-height: 1.7; color: #333; white-space: pre-wrap; }
</style>

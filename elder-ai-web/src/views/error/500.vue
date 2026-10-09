<!--
  ============================================================
  500.vue - 服务器错误页
  银发智能生活助手 - 全局异常兜底页面
  ============================================================
-->
<template>
  <div class="error-page">
    <div class="error-card">
      <div class="error-code">500</div>
      <div class="error-emoji">🛠️</div>
      <h1>服务器开小差了</h1>
      <p>系统出现了一点小问题，请稍后重试</p>
      <div class="error-actions">
        <el-button type="primary" size="large" round @click="goHome">返回首页</el-button>
        <el-button size="large" round @click="reload">刷新重试</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
const goHome = () => router.push('/')
const reload = () => location.reload()
</script>

<style scoped>
.error-page { display: flex; justify-content: center; align-items: center; min-height: 70vh; }
.error-card { text-align: center; padding: 48px; }
.error-code { font-size: 110px; font-weight: 800; color: #e85d75; line-height: 1.1; letter-spacing: 4px; }
.error-emoji { font-size: 48px; margin: 4px 0 12px; }
.error-card h1 { font-size: 28px; color: #1e293b; margin: 8px 0 12px; }
.error-card p { font-size: 18px; color: #909399; margin-bottom: 32px; }
.error-actions { display: flex; gap: 14px; justify-content: center; }
</style>

<template>
  <div class="not-found-page">
    <div class="not-found-card">
      <div class="error-code">404</div>
      <h1>页面未找到</h1>
      <p>您访问的页面不存在或已被移除</p>
      <el-button type="primary" size="large" @click="$router.push('/')">
        返回首页
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found-page { display: flex; justify-content: center; align-items: center; min-height: 60vh; }
.not-found-card { text-align: center; padding: 48px; }
.error-code { font-size: 96px; font-weight: bold; color: #409eff; line-height: 1.2; }
h1 { font-size: 28px; color: #2c3e50; margin: 24px 0 12px; }
p { font-size: 18px; color: #909399; margin-bottom: 32px; }
</style>

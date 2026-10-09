<!--
  ============================================================
  SkeletonLoader.vue - 通用骨架屏加载组件
  银发智能生活助手 - 代替转圈 loading，提升 App 质感
  支持：表格、卡片列表、详情三种模式
  ============================================================
-->
<template>
  <div class="skeleton-wrapper">
    <!-- 表格骨架 -->
    <div v-if="type === 'table'" class="skeleton-table">
      <div
        v-for="row in rows"
        :key="row"
        class="skeleton-row"
        :style="{ animationDelay: (row * 0.1) + 's' }"
      >
        <div class="skeleton-cell" v-for="col in cols" :key="col"
          :style="{ width: getCellWidth(col) }" />
      </div>
    </div>

    <!-- 卡片列表骨架 -->
    <div v-else-if="type === 'card'" class="skeleton-cards">
      <div
        v-for="card in rows"
        :key="card"
        class="skeleton-card"
        :style="{ animationDelay: (card * 0.1) + 's' }"
      >
        <div class="sk-line sk-line-title" />
        <div class="sk-line sk-line-desc" />
        <div class="sk-line sk-line-short" />
      </div>
    </div>

    <!-- 详情骨架 -->
    <div v-else-if="type === 'detail'" class="skeleton-detail">
      <div class="sk-line sk-line-title" />
      <div class="sk-line sk-line-desc" />
      <div class="sk-line sk-line-desc" />
      <div class="sk-line sk-line-short" />
      <div style="height: 12px" />
      <div class="sk-line sk-line-title" />
      <div class="sk-line sk-line-desc" />
      <div class="sk-line sk-line-desc" />
      <div class="sk-line sk-line-short" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'table',
    validator: (v) => ['table', 'card', 'detail'].includes(v)
  },
  rows: { type: Number, default: 5 },
  cols: { type: Number, default: 4 }
})

const getCellWidth = (col) => {
  const widths = ['15%', '30%', '25%', '20%', '10%']
  return widths[(col - 1) % widths.length]
}
</script>

<style scoped>
.skeleton-wrapper {
  width: 100%;
}

/* ========== 骨架动画 ========== */
.skeleton-table,
.skeleton-cards,
.skeleton-detail {
  background: #fff;
  border-radius: var(--radius-xl, 20px);
  padding: 24px;
  box-shadow: var(--shadow-sm, 0 1px 3px rgba(0,0,0,0.06));
}

.skeleton-row,
.skeleton-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid var(--color-border-light, #f1f5f9);
  animation: sk-fade 1.6s ease-in-out infinite;
}

.skeleton-row:last-child,
.skeleton-card:last-child {
  border-bottom: none;
}

.skeleton-cell,
.sk-line {
  height: 18px;
  border-radius: 6px;
  background: linear-gradient(90deg, #e8ecf1 25%, #f0f3f7 50%, #e8ecf1 75%);
  background-size: 200% 100%;
  animation: sk-shimmer 1.8s ease-in-out infinite;
}

.skeleton-cell {
  height: 16px;
}

.skeleton-card {
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  padding: 20px 0;
}

.sk-line { width: 100%; }
.sk-line-title { width: 55%; height: 22px; }
.sk-line-desc { width: 85%; }
.sk-line-short { width: 35%; }

@keyframes sk-fade {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

@keyframes sk-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>

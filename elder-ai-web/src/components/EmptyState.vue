<!--
  ============================================================
  EmptyState.vue - 通用空状态组件
  银发智能生活助手 - 统一“暂无数据”视觉，支持图标/文案/引导操作
  ============================================================
-->
<template>
  <div class="empty-state">
    <el-empty :image-size="imageSize" :description="''">
      <template #default>
        <div class="es-emoji" v-if="emoji">{{ emoji }}</div>
        <div class="es-illust" v-else>
          <svg viewBox="0 0 120 120" width="96" height="96">
            <circle cx="60" cy="60" r="54" fill="#f1f5f9"/>
            <circle cx="60" cy="60" r="38" fill="#e3f0ff"/>
            <path d="M44 62 Q60 78 76 62" stroke="#9bbcf0" stroke-width="5" fill="none" stroke-linecap="round"/>
            <circle cx="48" cy="50" r="5" fill="#9bbcf0"/>
            <circle cx="72" cy="50" r="5" fill="#9bbcf0"/>
            <path d="M40 40 L30 30 M80 40 L90 30" stroke="#c7d8ef" stroke-width="4" stroke-linecap="round"/>
          </svg>
        </div>
        <p class="es-title" v-if="title">{{ title }}</p>
        <p class="es-text">{{ description }}</p>
        <div class="es-action">
          <slot name="action">
            <el-button v-if="actionText" type="primary" size="large" round @click="$emit('action')">
              {{ actionText }}
            </el-button>
          </slot>
        </div>
      </template>
    </el-empty>
  </div>
</template>

<script setup>
defineProps({
  description: { type: String, default: '暂无数据' },
  title: { type: String, default: '' },
  emoji: { type: String, default: '' },
  imageSize: { type: Number, default: 90 },
  actionText: { type: String, default: '' }
})
defineEmits(['action'])
</script>

<style scoped>
.empty-state { padding: 40px 16px; text-align: center; }
.es-emoji { font-size: 56px; margin-bottom: 8px; }
.es-title { font-size: 19px; font-weight: 700; color: var(--color-text-primary, #1e293b); margin: 4px 0 6px; }
.es-text { font-size: 16px; color: var(--color-text-muted, #94a3b8); margin: 0 0 18px; }
.es-action { display: flex; justify-content: center; }
</style>

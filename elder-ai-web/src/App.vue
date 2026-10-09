<!--
  App.vue - 应用根组件
  银发智能生活助手 - 全宽响应式布局，适老化 App 风格
-->
<template>
  <el-config-provider :locale="zhCn">
    <AccessibilityStatus />
    <!-- 路由出口 + 顶层页面切换淡入淡出 -->
    <router-view v-slot="{ Component }">
      <transition name="app-fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </el-config-provider>
</template>

<script setup>
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import AccessibilityStatus from '@/components/AccessibilityStatus.vue'
</script>

<style>
/* 全局盒模型重置（适老化基础） */
*,
*::before,
*::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

/* 页面主体基础字体（具体背景与颜色在 style.css 中定义） */
body {
  font-size: 18px;
  font-family: var(--font-family);
  line-height: 1.8;
  color: var(--color-text-primary);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 顶层路由切换动画 */
.app-fade-enter-active,
.app-fade-leave-active {
  transition: opacity 0.25s ease;
}
.app-fade-enter-from,
.app-fade-leave-to {
  opacity: 0;
}
</style>

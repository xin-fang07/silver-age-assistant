<!--
  ============================================================
  VoiceReader.vue - 语音王悬浮朗读控制器
  银发智能生活助手 - 全局悬浮朗读控件

  功能：
  1. 悬浮在页面右下角，不遮挡内容
  2. 朗读时显示控制面板（暂停/继续、停止、语速切换）
  3. 显示当前朗读的来源和文本预览
  4. 适老化设计：大按钮、大字体、高对比度
  ============================================================
-->
<template>
  <!-- 悬浮朗读控制器 -->
  <div v-if="isSpeaking" class="voice-reader">
    <!-- 控制面板 -->
    <div class="reader-panel">
      <!-- 顶部：来源标签 + 关闭 -->
      <div class="reader-header">
        <span class="reader-badge">🔊 语音王</span>
        <span v-if="currentLabel" class="reader-label">{{ currentLabel }}</span>
      </div>

      <!-- 文本预览 -->
      <div class="reader-text">
        {{ currentText.length > 50 ? currentText.substring(0, 50) + '...' : currentText }}
      </div>

      <!-- 控制按钮区 -->
      <div class="reader-controls">
        <!-- 暂停/继续按钮 -->
        <button
          class="reader-btn reader-btn-main"
          @click="togglePause"
        >
          <span v-if="isPaused" class="btn-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
              <path d="M8 5v14l11-7z" />
            </svg>
          </span>
          <span v-else class="btn-icon">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
              <path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z" />
            </svg>
          </span>
          <span class="btn-text">{{ isPaused ? '继续' : '暂停' }}</span>
        </button>

        <!-- 停止按钮 -->
        <button class="reader-btn reader-btn-stop" @click="stop">
          <span class="btn-icon">
            <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
              <path d="M6 6h12v12H6z" />
            </svg>
          </span>
          <span class="btn-text">停止</span>
        </button>

        <!-- 语速切换按钮 -->
        <button class="reader-btn reader-btn-rate" @click="switchRate">
          <span class="btn-text">语速：{{ rateOptions[currentRateIndex].label }}</span>
        </button>
      </div>
    </div>

    <!-- 朗读进度条动画 -->
    <div class="reader-progress" :class="{ 'progress-paused': isPaused }">
      <div class="progress-bar"></div>
    </div>
  </div>
</template>

<script setup>
// ============================================================
// VoiceReader 逻辑 - 使用 useSpeech composable
// ============================================================
import { useSpeech } from '@/composables/useSpeech'

const {
  isSpeaking,
  isPaused,
  currentText,
  currentLabel,
  currentRateIndex,
  rateOptions,
  togglePause,
  stop,
  switchRate
} = useSpeech()
</script>

<style scoped>
/*
 * ============================================================
 * 语音王悬浮控制器样式 - 适老化设计
 * ============================================================
 */

/* ========== 悬浮容器 ========== */
.voice-reader {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  width: 360px;
  /* 渐入动画 */
  animation: slideUp 0.3s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========== 控制面板 ========== */
.reader-panel {
  background: linear-gradient(135deg, #ffffff, #f8faff);
  border: 2px solid #409eff;
  border-radius: 16px;
  padding: 16px 20px;
  box-shadow: 0 8px 32px rgba(64, 158, 255, 0.2);
}

/* ========== 顶部标签行 ========== */
.reader-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

/* "语音王"徽章 */
.reader-badge {
  background: linear-gradient(135deg, #409eff, #1d4ed8);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  padding: 4px 14px;
  border-radius: 20px;
  white-space: nowrap;
}

/* 来源标签 */
.reader-label {
  font-size: 16px;
  color: #606266;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ========== 文本预览 ========== */
.reader-text {
  font-size: 16px;
  color: #2c3e50;
  line-height: 1.6;
  margin-bottom: 14px;
  padding: 10px 14px;
  background-color: #f0f5ff;
  border-radius: 10px;
  border-left: 4px solid #409eff;
  max-height: 60px;
  overflow: hidden;
}

/* ========== 控制按钮区 ========== */
.reader-controls {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* 通用按钮样式 */
.reader-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: none;
  border-radius: 10px;
  padding: 10px 16px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.reader-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.reader-btn:active {
  transform: translateY(0);
}

/* 主按钮（暂停/继续）- 蓝色 */
.reader-btn-main {
  background: linear-gradient(135deg, #409eff, #1d4ed8);
  color: #fff;
  flex: 1;
}

/* 停止按钮 - 红色 */
.reader-btn-stop {
  background: linear-gradient(135deg, #f56c6c, #e74c3c);
  color: #fff;
}

/* 语速按钮 - 橙色 */
.reader-btn-rate {
  background: linear-gradient(135deg, #f5dab1, #e6a23c);
  color: #fff;
}

/* 按钮图标 */
.btn-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 按钮文字 */
.btn-text {
  font-size: 16px;
}

/* ========== 进度条动画 ========== */
.reader-progress {
  height: 4px;
  margin-top: 8px;
  background-color: #dcdfe6;
  border-radius: 2px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  width: 40%;
  background: linear-gradient(90deg, #409eff, #67c23a);
  border-radius: 2px;
  animation: progressMove 2s ease-in-out infinite;
}

/* 暂停时停止动画 */
.progress-paused .progress-bar {
  animation-play-state: paused;
}

@keyframes progressMove {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(350%);
  }
}
</style>

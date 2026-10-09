// ============================================================
// useElderMode.js - 全局"银发模式（适老化）"状态管理
// 银发智能生活助手 - 模块级单例，所有组件共享同一状态
// 不依赖后端：状态持久化到 localStorage，并在 <html> 上切换
//             class（elder-mode / high-contrast / reduce-motion）
//             以及内联 zoom（银发模式基准 1.1 倍叠加字号档位）
// ============================================================

import { ref } from 'vue'

const STORAGE_KEY = 'elder_ai_elder_mode'
const CONTRAST_KEY = 'elder_ai_high_contrast'
const MOTION_KEY = 'elder_ai_reduce_motion'
const FONT_SIZE_KEY = 'elder_ai_font_size'

// 字号档位 -> 页面缩放系数（"标准"= 1.0 即常规网页字号，参照 Hugging Face 等标准站点，不额外放大）
const FONT_SCALE = { small: 0.9, medium: 1.0, large: 1.12, xlarge: 1.25 }
// 字号档位展示文案
export const FONT_LABELS = [
  { value: 'small', label: '小' },
  { value: 'medium', label: '标准' },
  { value: 'large', label: '大' },
  { value: 'xlarge', label: '特大' }
]

// 模块级单例：整个应用共享同一个 ref（真正"全局"）
const elderSaved = localStorage.getItem(STORAGE_KEY)
const isElderMode = ref(elderSaved === null ? true : elderSaved === '1')
const isHighContrast = ref(localStorage.getItem(CONTRAST_KEY) === '1')
const reduceMotionSaved = localStorage.getItem(MOTION_KEY)
const systemPrefersReducedMotion = typeof window !== 'undefined'
  && window.matchMedia?.('(prefers-reduced-motion: reduce)').matches
const isReduceMotion = ref(reduceMotionSaved === null
  ? systemPrefersReducedMotion
  : reduceMotionSaved === '1')

const savedFont = localStorage.getItem(FONT_SIZE_KEY)
const fontSize = ref(FONT_SCALE[savedFont] ? savedFont : 'medium')

// 将状态同步到 <html> 的 class 与内联 zoom，供全局 CSS 选择器使用
function applyElderClass() {
  if (typeof document === 'undefined') return
  document.documentElement.classList.toggle('elder-mode', isElderMode.value)
  document.documentElement.classList.toggle('high-contrast', isHighContrast.value)
  document.documentElement.classList.toggle('reduce-motion', isReduceMotion.value)
  // 页面缩放完全由字号档位决定："标准"= 1.0 常规网页字号（参照 Hugging Face）。
  // 银发模式只负责按钮放大、文字加粗等可读性增强（通过 class），不再额外抬高整体字号。
  const scale = FONT_SCALE[fontSize.value] || 1
  document.documentElement.style.zoom = String(scale)
}

// 首次加载时即时应用（避免刷新后闪烁）
applyElderClass()

// 设置模式并持久化
function setElderMode(value) {
  isElderMode.value = !!value
  try {
    localStorage.setItem(STORAGE_KEY, isElderMode.value ? '1' : '0')
  } catch (e) { /* 忽略隐私模式下的写入失败 */ }
  applyElderClass()
}

// 切换模式
function toggleElderMode() {
  setElderMode(!isElderMode.value)
}

function setHighContrast(value) {
  isHighContrast.value = !!value
  localStorage.setItem(CONTRAST_KEY, isHighContrast.value ? '1' : '0')
  applyElderClass()
}

function toggleHighContrast() {
  setHighContrast(!isHighContrast.value)
}

function setReduceMotion(value) {
  isReduceMotion.value = !!value
  localStorage.setItem(MOTION_KEY, isReduceMotion.value ? '1' : '0')
  applyElderClass()
}

function toggleReduceMotion() {
  setReduceMotion(!isReduceMotion.value)
}

// 设置字号档位并持久化（触发统一缩放重算）
function setFontSize(size) {
  if (!FONT_SCALE[size]) return
  fontSize.value = size
  try {
    localStorage.setItem(FONT_SIZE_KEY, size)
  } catch (e) { /* 忽略 */ }
  applyElderClass()
}

/**
 * 在任意组件中获取/控制银发模式与字号
 */
export function useElderMode() {
  return {
    isElderMode, setElderMode, toggleElderMode,
    isHighContrast, setHighContrast, toggleHighContrast,
    isReduceMotion, setReduceMotion, toggleReduceMotion,
    fontSize, setFontSize
  }
}

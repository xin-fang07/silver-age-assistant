/**
 * ============================================================
 * useSpeech.js - 语音朗读 composable（语音王核心）
 * 银发智能生活助手 - 全文朗读功能
 *
 * 基于 Web Speech API 的 SpeechSynthesis 接口封装
 * 功能：文本朗读、暂停/继续/停止、语速控制、长文本分块
 * 适老化：默认慢速 0.9、中文语音、大音量
 * ============================================================
 */

import { ref, readonly } from 'vue'

// ========== 全局单例状态（跨组件共享） ==========
const synth = window.speechSynthesis

// 是否正在朗读
const isSpeaking = ref(false)
// 是否暂停
const isPaused = ref(false)
// 当前朗读的文本
const currentText = ref('')
// 当前朗读的来源标签（如"资讯"、"健康建议"）
const currentLabel = ref('')
// 语速选项：慢速 0.8、正常 1.0、快速 1.2
const rateOptions = [
  { label: '慢速', value: 0.8 },
  { label: '正常', value: 1.0 },
  { label: '快速', value: 1.2 }
]
const currentRateIndex = ref(0) // 默认慢速（适老化）

// ========== 语音提示开关（两个独立场景，持久化，跨组件共享） ==========
// scope='login'  → 登录页开关A：仅控制登录页的欢迎语 + 语音引导
// scope='family' → 家属端开关B：控制其余全部播报（模块简介、聊天、资讯、语音王等）
// 两个开关分别持久化、互不影响
const VOICE_ENABLED_KEY = 'voice_enabled'               // 旧总开关，仅作迁移默认值
const VOICE_ENABLED_LOGIN_KEY = 'voice_enabled_login'   // 开关A
const VOICE_ENABLED_FAMILY_KEY = 'voice_enabled_family' // 开关B

// 读布尔：未设置过返回 fallback
const readBool = (key, fallback) => {
  try {
    const v = localStorage.getItem(key)
    return v === null ? fallback : v === 'true'
  } catch {
    return fallback
  }
}
// 旧总开关作为迁移默认值：老用户曾关过总开关的选择不丢失
const legacyDefault = readBool(VOICE_ENABLED_KEY, true)
const readLoginEnabled = () => readBool(VOICE_ENABLED_LOGIN_KEY, legacyDefault)
const readFamilyEnabled = () => readBool(VOICE_ENABLED_FAMILY_KEY, legacyDefault)

const loginVoiceEnabled = ref(readLoginEnabled())
const familyVoiceEnabled = ref(readFamilyEnabled())

// 切换开关A（登录页）：关闭时立即停止正在进行的朗读，并记住选择
const toggleLoginVoice = () => {
  loginVoiceEnabled.value = !loginVoiceEnabled.value
  try {
    localStorage.setItem(VOICE_ENABLED_LOGIN_KEY, String(loginVoiceEnabled.value))
  } catch { /* 忽略隐私模式下的写入失败 */ }
  if (!loginVoiceEnabled.value) {
    stop()
  }
}

// 切换开关B（家属端）：关闭时立即停止正在进行的朗读，并记住选择
const toggleFamilyVoice = () => {
  familyVoiceEnabled.value = !familyVoiceEnabled.value
  try {
    localStorage.setItem(VOICE_ENABLED_FAMILY_KEY, String(familyVoiceEnabled.value))
  } catch { /* 忽略隐私模式下的写入失败 */ }
  if (!familyVoiceEnabled.value) {
    stop()
  }
}

// 朗读队列（长文本分块后按序朗读）
let chunkQueue = []
let currentUtterance = null

// 朗读版本号：每次 speak/stop 递增，onend 回调检查版本号
// 彻底防止旧朗读的 onend 被异步触发后继续读队列中的下一块
let speakVersion = 0

// ========== 选择中文语音 ==========
let zhVoice = null
const loadVoices = () => {
  if (!synth) return
  const voices = synth.getVoices()
  // 优先选择中文语音
  zhVoice = voices.find(v => v.lang === 'zh-CN')
    || voices.find(v => v.lang.startsWith('zh'))
    || voices.find(v => v.name.includes('Chinese'))
    || null
}

// 浏览器异步加载语音列表
if (synth) {
  loadVoices()
  synth.onvoiceschanged = loadVoices
}

// ========== 文本清洗：去掉 emoji 和多余空白 ==========
const cleanText = (text) => {
  if (!text) return ''
  // 去掉 emoji 和特殊符号
  return text
    .replace(/[\u{1F000}-\u{1FFFF}\u{2600}-\u{27BF}]/gu, '')
    .replace(/\s+/g, ' ')
    .trim()
}

// ========== 长文本分块（按句号/换行拆分，每块不超过 200 字） ==========
const splitIntoChunks = (text) => {
  const cleaned = cleanText(text)
  if (!cleaned) return []

  // 按句号、问号、感叹号、换行拆分
  const sentences = cleaned.split(/[。！？\n.!?]+/).filter(s => s.trim())
  const chunks = []
  let buffer = ''

  for (const sentence of sentences) {
    const trimmed = sentence.trim()
    if (!trimmed) continue

    // 如果当前缓冲区 + 这句不超过 200 字，拼到一起
    if ((buffer + trimmed).length <= 200) {
      buffer += trimmed + '。'
    } else {
      // 缓冲区满了，推入队列
      if (buffer) chunks.push(buffer)
      // 单句超过 200 字，硬切
      if (trimmed.length > 200) {
        for (let i = 0; i < trimmed.length; i += 200) {
          chunks.push(trimmed.slice(i, i + 200))
        }
        buffer = ''
      } else {
        buffer = trimmed + '。'
      }
    }
  }
  // 最后一块
  if (buffer) chunks.push(buffer)

  return chunks
}

// ========== 朗读一块文本 ==========
// version 参数：当前朗读的版本号，用于在 onend 中判断是否已被取代
const speakChunk = (chunk, version) => {
  if (!synth || !chunk) return
  // 版本不匹配，说明已被新的 speak/stop 取代，不继续
  if (version !== speakVersion) return

  currentUtterance = new SpeechSynthesisUtterance(chunk)
  // 设置语言
  currentUtterance.lang = 'zh-CN'
  // 设置语音（如果找到中文语音）
  if (zhVoice) {
    currentUtterance.voice = zhVoice
  }
  // 语速
  currentUtterance.rate = rateOptions[currentRateIndex.value].value
  // 音调正常
  currentUtterance.pitch = 1.0
  // 最大音量
  currentUtterance.volume = 1.0

  // 当前块朗读完毕
  currentUtterance.onend = () => {
    // 版本不匹配 → 已被 stop() 或新的 speak() 取代，直接退出不继续
    if (version !== speakVersion) return

    // 还有剩余块，继续朗读
    if (chunkQueue.length > 0 && isSpeaking.value) {
      const next = chunkQueue.shift()
      speakChunk(next, version)
    } else {
      // 全部朗读完毕，重置状态
      isSpeaking.value = false
      isPaused.value = false
      currentText.value = ''
      currentLabel.value = ''
      currentUtterance = null
    }
  }

  // 朗读出错
  currentUtterance.onerror = (event) => {
    // 版本不匹配 → 已被取代，忽略错误
    if (version !== speakVersion) return
    console.error('语音朗读错误：', event.error)
    // 出错也重置状态
    isSpeaking.value = false
    isPaused.value = false
    currentText.value = ''
    currentLabel.value = ''
    currentUtterance = null
    chunkQueue = []
  }

  synth.speak(currentUtterance)
}

// ========== 按场景判断对应开关是否开启 ==========
// 同时实时校验 localStorage，防止多标签页 / HMR / 状态不同步时
// 内存中的开关值与用户实际选择不一致而"关了还播报"
const isScopeEnabled = (scope) => {
  if (scope === 'login') {
    return loginVoiceEnabled.value && readLoginEnabled()
  }
  // family / 默认
  return familyVoiceEnabled.value && readFamilyEnabled()
}

// ========== 开始朗读 ==========
// scope='login' → 受登录页开关A控制；scope='family'（默认）→ 受家属端开关B控制
const speak = (text, label = '', scope = 'family') => {
  if (!synth) {
    console.warn('浏览器不支持语音合成')
    return false
  }

  // 对应场景的语音开关关闭 → 不朗读（记住用户选择）
  if (!isScopeEnabled(scope)) {
    return false
  }

  // 如果正在朗读，先停止
  if (isSpeaking.value) {
    stop()
  }

  const chunks = splitIntoChunks(text)
  if (chunks.length === 0) {
    return false
  }

  // 递增版本号，使之前所有 onend/onerror 回调失效
  speakVersion++
  const myVersion = speakVersion

  // 设置状态
  currentText.value = cleanText(text)
  currentLabel.value = label
  isSpeaking.value = true
  isPaused.value = false

  // 第一块立即朗读，其余放入队列
  const first = chunks.shift()
  chunkQueue = chunks
  speakChunk(first, myVersion)

  return true
}

// ========== 暂停朗读 ==========
const pause = () => {
  if (!synth || !isSpeaking.value || isPaused.value) return
  synth.pause()
  isPaused.value = true
}

// ========== 继续朗读 ==========
const resume = () => {
  if (!synth || !isSpeaking.value || !isPaused.value) return
  synth.resume()
  isPaused.value = false
}

// ========== 停止朗读 ==========
const stop = () => {
  if (!synth) return

  // 递增版本号，使所有正在进行的 onend/onerror 回调失效
  // 这样即使 synth.cancel() 异步触发了旧回调，也不会继续读队列
  speakVersion++

  // 移除当前 utterance 的回调（双保险）
  if (currentUtterance) {
    currentUtterance.onend = null
    currentUtterance.onerror = null
  }

  // Chrome bug: 如果处于暂停状态，cancel() 可能无效，先 resume 再 cancel
  if (synth.paused) {
    synth.resume()
  }
  synth.cancel()

  isSpeaking.value = false
  isPaused.value = false
  currentText.value = ''
  currentLabel.value = ''
  currentUtterance = null
  chunkQueue = []
}

// ========== 切换语速 ==========
const switchRate = () => {
  currentRateIndex.value = (currentRateIndex.value + 1) % rateOptions.length
  // 如果正在朗读，用新语速重新朗读
  if (isSpeaking.value && currentText.value) {
    const text = currentText.value
    const label = currentLabel.value
    stop()
    // 延迟一点再重新开始，避免 cancel 还没完成
    setTimeout(() => speak(text, label), 100)
  }
}

// ========== 暂停/继续切换 ==========
const togglePause = () => {
  if (isPaused.value) {
    resume()
  } else {
    pause()
  }
}

// ========== 导出 composable ==========
export function useSpeech() {
  return {
    // 状态（只读）
    isSpeaking: readonly(isSpeaking),
    isPaused: readonly(isPaused),
    currentText: readonly(currentText),
    currentLabel: readonly(currentLabel),
    currentRateIndex: readonly(currentRateIndex),
    rateOptions,
    // 两个独立语音开关：登录页开关A / 家属端开关B
    loginVoiceEnabled: readonly(loginVoiceEnabled),
    familyVoiceEnabled: readonly(familyVoiceEnabled),
    toggleLoginVoice,
    toggleFamilyVoice,
    // 兼容别名（历史引用「总开关」的语义等同家属端开关B，防止旧代码报错）
    voiceEnabled: readonly(familyVoiceEnabled),
    toggleVoiceEnabled: toggleFamilyVoice,
    // 操作方法
    speak,
    pause,
    resume,
    stop,
    togglePause,
    switchRate
  }
}

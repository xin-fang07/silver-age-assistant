<!--
  chat/index.vue - 智能问答页（需登录） — 增强版
  ChatGPT 风格聊天气泡 + Markdown + 打字机动画 + 操作栏
-->
<template>
  <div class="chat-page">
    <!-- ========== 页面标题 + 操作 ========== -->
    <div class="chat-header">
      <h2 class="chat-title">
        <el-icon :size="26" style="vertical-align:middle;margin-right:8px;color:#2563eb;"><ChatDotRound /></el-icon>
        家庭智能养老助手
      </h2>
      <div class="chat-header-actions">
        <el-button v-if="messages.length" text type="info" size="small" @click="clearChat">
          <el-icon><Delete /></el-icon> 清空聊天
        </el-button>
      </div>
    </div>

    <!-- ========== AI 身份明示（合规标识） ========== -->
    <div class="ai-disclaimer" style="display:flex;align-items:flex-start;gap:8px;margin:0 16px 10px;padding:9px 12px;background:#eef4ff;border:1px solid #d3e0fb;border-radius:8px;color:#3a5bbf;font-size:13px;line-height:1.6;">
      <svg viewBox="0 0 24 24" width="16" height="16" style="flex-shrink:0;margin-top:2px;">
        <circle cx="12" cy="12" r="10" fill="#3a5bbf"/>
        <text x="12" y="16" text-anchor="middle" fill="#fff" font-size="13" font-weight="bold">!</text>
      </svg>
      <span>您正在与 <b>家庭智能养老助手</b> 对话，回复由人工智能生成，<b>仅供参考、不构成医疗或专业建议，并非真人</b>。如有紧急健康问题或身体不适，请使用首页「紧急求助」或及时联系家人、就医。</span>
    </div>

    <!-- ========== 聊天消息区域 ========== -->
    <el-scrollbar ref="chatScrollbar" class="chat-messages">
      <div class="messages-container">
        <!-- 欢迎提示 -->
        <div v-if="messages.length===0 && !aiThinking" class="welcome-tip">
          <div class="welcome-icon">
            <svg viewBox="0 0 80 80" width="56" height="56">
              <defs><linearGradient id="wG" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#5b9eff"/><stop offset="100%" style="stop-color:#2563eb"/></linearGradient></defs>
              <circle cx="40" cy="40" r="36" fill="url(#wG)" opacity="0.15"/><circle cx="40" cy="40" r="28" fill="url(#wG)"/>
              <path d="M28 36 Q28 30 34 30 L46 30 Q52 30 52 36 L52 42 Q52 48 46 48 L38 48 L32 54 L32 48 Q28 48 28 42 Z" fill="#fff"/><circle cx="36" cy="38" r="2.5" fill="#5b9eff"/><circle cx="44" cy="38" r="2.5" fill="#5b9eff"/><path d="M35 43 Q40 46 45 43" stroke="#5b9eff" stroke-width="2" fill="none" stroke-linecap="round"/>
            </svg>
          </div>
          <p class="welcome-text">您好！我是家庭智能养老助手</p>
          <p class="welcome-sub">您可以查询老人健康状态、获取照护建议、了解医疗知识</p>
          <div class="quick-questions">
            <p class="quick-title">您可以试着问我：</p>
            <button v-for="q in quickQuestions" :key="q.text" type="button" class="quick-question-btn" @click="handleQuickAction(q)">
              <span aria-hidden="true">{{ q.icon }}</span>{{ q.text }}
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-for="(msg, index) in messages" :key="msg.id" class="message-wrapper" :class="msg.role==='user'?'message-user':'message-ai'">
          <!-- 时间戳 -->
          <div v-if="index===0 || msg.time!==messages[index-1].time" class="msg-time">{{ msg.time }}</div>
          <!-- 用户消息 -->
          <div v-if="msg.role==='user'" class="message-bubble message-bubble-user">
            <div class="message-content">{{ msg.content }}</div>
          </div>
          <!-- AI 消息 -->
          <div v-else class="message-row">
            <div class="ai-avatar">
              <svg viewBox="0 0 40 40" width="34" height="34"><defs><linearGradient id="aA" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#67c23a"/><stop offset="100%" style="stop-color:#3e8e3e"/></linearGradient></defs><circle cx="20" cy="20" r="18" fill="url(#aA)"/><path d="M10 18 Q10 12 16 12 L24 12 Q30 12 30 18 L30 22 Q30 28 24 28 L18 28 L14 32 L14 28 Q10 28 10 22 Z" fill="#fff"/><circle cx="16" cy="19" r="2" fill="#67c23a"/><circle cx="24" cy="19" r="2" fill="#67c23a"/><path d="M15 23 Q20 26 25 23" stroke="#67c23a" stroke-width="1.5" fill="none" stroke-linecap="round"/></svg>
            </div>
            <div class="ai-content">
              <div class="message-bubble message-bubble-ai">
                <!-- Markdown 渲染内容 -->
                <div class="message-content md-content" v-html="renderMarkdown(msg.displayContent || msg.content)"></div>
                <!-- 打字中光标 -->
                <span v-if="msg.typing" class="typing-cursor">|</span>
              </div>
              <!-- 操作栏 -->
              <div class="ai-actions">
                <button class="action-btn" @click="copyMessage(msg)" title="复制" aria-label="复制这条回复"><el-icon :size="16"><CopyDocument /></el-icon></button>
                <button class="action-btn" @click="regenerateMessage(index)" title="重新生成" aria-label="重新生成这条回复" :disabled="aiThinking"><el-icon :size="16"><Refresh /></el-icon></button>
                <button class="action-btn" @click="readMessage(msg.content)" title="朗读" aria-label="朗读这条回复"><el-icon :size="16"><Headset /></el-icon></button>
                <button class="action-btn" :class="{liked:msg.liked}" @click="toggleLike(index)" title="有用" aria-label="标记这条回复有用"><el-icon :size="16"><Opportunity /></el-icon></button>
                <button class="action-btn" :class="{disliked:msg.disliked}" @click="toggleDislike(index)" title="没用" aria-label="标记这条回复没用"><el-icon :size="16"><Remove /></el-icon></button>
                <span v-if="msg.isFallback" class="fallback-tip">（本地智能回复）</span>
              </div>
            </div>
          </div>
        </div>

        <!-- AI 思考中 -->
        <div v-if="aiThinking" class="message-wrapper message-ai">
          <div class="message-row">
            <div class="ai-avatar">
              <svg viewBox="0 0 40 40" width="34" height="34"><defs><linearGradient id="aT" x1="0%" y1="0%" x2="100%" y2="100%"><stop offset="0%" style="stop-color:#67c23a"/><stop offset="100%" style="stop-color:#3e8e3e"/></linearGradient></defs><circle cx="20" cy="20" r="18" fill="url(#aT)"/><path d="M10 18 Q10 12 16 12 L24 12 Q30 12 30 18 L30 22 Q30 28 24 28 L18 28 L14 32 L14 28 Q10 28 10 22 Z" fill="#fff"/><circle cx="16" cy="19" r="2" fill="#67c23a"/><circle cx="24" cy="19" r="2" fill="#67c23a"/><path d="M15 23 Q20 26 25 23" stroke="#67c23a" stroke-width="1.5" fill="none" stroke-linecap="round"/></svg>
            </div>
            <div class="ai-content">
              <div class="message-bubble message-bubble-ai thinking-bubble">
                <div class="thinking-dots">
                  <span>AI 思考中</span>
                  <span class="dot-pulse">.</span><span class="dot-pulse" style="animation-delay:.2s">.</span><span class="dot-pulse" style="animation-delay:.4s">.</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-scrollbar>

    <!-- ========== 底部输入区域 ========== -->
    <div class="chat-input-area">
      <div class="input-wrapper">
        <el-button class="voice-input-btn" :class="{'voice-recording':isVoiceRecording}" :disabled="aiThinking" @click="toggleVoiceInput" circle size="large" :title="isVoiceRecording?'点击停止':'语音输入'" :aria-label="isVoiceRecording?'停止语音识别':'开始语音输入'">
          <el-icon :size="22"><Microphone /></el-icon>
        </el-button>
        <el-input v-model="inputText" size="large" :placeholder="isVoiceRecording?'正在聆听...':'输入您想了解的养老问题...'" class="chat-input" :class="{'input-recording':isVoiceRecording}" :disabled="aiThinking" @keyup.enter="sendMessage" clearable />
        <el-button type="primary" size="large" class="send-btn" :disabled="!inputText.trim()||aiThinking" @click="sendMessage">发 送</el-button>
      </div>
      <div v-if="voiceError" class="voice-error-tip">
        <el-alert :title="voiceError" type="warning" :closable="true" show-icon @close="voiceError=''" />
      </div>
      <div v-if="voiceAwaitingConfirm" class="voice-confirm-panel" role="group" aria-label="确认语音识别结果">
        <strong>请确认我听到的内容：</strong>
        <el-input v-model="inputText" size="large" aria-label="可修改的语音识别文字" />
        <div class="voice-confirm-actions">
          <el-button type="primary" size="large" @click="confirmVoiceSend">确认发送</el-button>
          <el-button size="large" @click="retryVoiceInput">重新说一遍</el-button>
          <el-button size="large" @click="cancelVoiceInput">取消</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { chatApi, chatRecordApi } from '@/api/index'
import { useSpeech } from '@/composables/useSpeech'

const speech = useSpeech()
const router = useRouter()

// ========== 危机词识别 + 安全干预（复用 SOS 紧急求助能力） ==========
// 命中以下高危表达时，主动弹出安全干预引导，呼应「生命安全干预 / 老年人保护」
const CRISIS_KEYWORDS = [
  '不想活', '不想活了', '活不下去', '活着没意思', '活着没意义', '没意思活',
  '轻生', '自杀', '想死', '去死', '结束生命', '结束自己', '了结自己',
  '一了百了', '离开这个世界', '跳楼', '跳河', '上吊', '割腕', '安眠药',
  '没人管我', '生无可恋', '解脱'
]
const crisisTriggered = ref(false)
const detectCrisis = (text) => {
  if (!text) return false
  return CRISIS_KEYWORDS.some(k => text.includes(k))
}
const showCrisisIntervention = () => {
  const tip = '如果您正遇到困难或身体、情绪上的不适，请不要独自承受。您可以立即使用「紧急求助」通知家人，或拨打全国心理援助热线 400-161-9995。'
  try { speech.speak(tip) } catch (e) {}
  ElMessageBox.confirm(
    tip,
    '我们很关心您 ❤',
    {
      confirmButtonText: '立即紧急求助',
      cancelButtonText: '我知道了',
      type: 'warning',
      distinguishCancelAndClose: true,
      customClass: 'crisis-messagebox'
    }
  ).then(() => {
    router.push('/care/emergency')
  }).catch(() => {})
}

// ========== 语音输入 ==========
const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
const voiceSupported = !!SpeechRecognition
let voiceRecognition = null
const isVoiceRecording = ref(false)
const voiceError = ref('')
const voiceAwaitingConfirm = ref(false)

const initVoiceRecognition = () => {
  if (!voiceSupported) return
  voiceRecognition = new SpeechRecognition()
  voiceRecognition.lang = 'zh-CN'; voiceRecognition.continuous = false
  voiceRecognition.interimResults = true; voiceRecognition.maxAlternatives = 1
  voiceRecognition.onresult = (e) => { let t=''; for(let i=e.resultIndex;i<e.results.length;i++) if(e.results[i]&&e.results[i][0]) t+=e.results[i][0].transcript; inputText.value=t }
  voiceRecognition.onend = () => {
    isVoiceRecording.value=false
    if (inputText.value.trim()) {
      voiceAwaitingConfirm.value = true
      speech.speak(`我听到的是：${inputText.value}。请确认后再发送。`)
    }
  }
  voiceRecognition.onerror = (e) => { isVoiceRecording.value=false; const m={'no-speech':'没有听到说话','network':'语音识别失败，请打字提问','not-allowed':'请允许麦克风权限','audio-capture':'麦克风不可用'}; voiceError.value=m[e.error]||'语音识别出错，请打字提问' }
}

const toggleVoiceInput = () => {
  if (!voiceSupported) { voiceError.value='您的浏览器不支持语音输入，请使用Chrome浏览器'; return }
  if (aiThinking.value) return
  if (isVoiceRecording.value) { if(voiceRecognition) voiceRecognition.stop(); isVoiceRecording.value=false }
  else { voiceError.value=''; inputText.value=''; voiceAwaitingConfirm.value=false; try { initVoiceRecognition(); voiceRecognition.start(); isVoiceRecording.value=true } catch(e) { voiceError.value='启动语音识别失败' } }
}

const confirmVoiceSend = () => { voiceAwaitingConfirm.value = false; sendMessage() }
const retryVoiceInput = () => { voiceAwaitingConfirm.value = false; toggleVoiceInput() }
const cancelVoiceInput = () => { voiceAwaitingConfirm.value = false; inputText.value = '' }

// ========== 聊天数据 ==========
const CHAT_HISTORY_KEY = 'elder_ai_chat_history2'
let msgId = 1
const messages = reactive([])
const inputText = ref('')
const aiThinking = ref(false)
const chatScrollbar = ref(null)

const quickQuestions = [
  { text: '张奶奶最近健康怎么样？', icon: '👵', route: '' },
  { text: '血压偏高需要注意什么？', icon: '❤️', route: '' },
  { text: '老人饮食有什么建议？', icon: '🍚', route: '' },
  { text: '今天天气怎么样？', icon: '🌤️', route: '' }
]

const WEATHER_KEYWORDS = ['天气','气温','温度','下雨','下雪','刮风','风大','雾霾','weather','rain','snow','sunny','cloudy']
const isWeatherQuestion = (text) => WEATHER_KEYWORDS.some(k => text.toLowerCase().includes(k.toLowerCase()))
const buildChatRequest = (text) => {
  const payload = { question: text }
  if (isWeatherQuestion(text)) {
    payload.city = localStorage.getItem('elder_ai_weather_city') || '北京'
  }
  return payload
}

// ========== 时间戳 ==========
const getTimestamp = () => { const d=new Date(); return `${d.getMonth()+1}/${d.getDate()} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}` }

// ========== 本地存储 ==========
const loadHistory = () => {
  try { const s=localStorage.getItem(CHAT_HISTORY_KEY); if(s) { const h=JSON.parse(s); if(Array.isArray(h)) { messages.splice(0,messages.length,...h.slice(-30)); msgId=messages.length+1 } } } catch(e) {}
}
const saveHistory = () => { try { localStorage.setItem(CHAT_HISTORY_KEY, JSON.stringify(messages.slice(-30))) } catch(e) {} }

// ========== 简单 Markdown 渲染 ==========
const renderMarkdown = (text) => {
  if (!text) return ''
  let html = String(text)
  // 转义 HTML（保留已渲染部分）
  html = html.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;')
  // 代码块 ```...```
  html = html.replace(/```(\w*)\n([\s\S]*?)```/g, '<pre class="md-code-block"><code>$2</code></pre>')
  // 行内代码 `...`
  html = html.replace(/`([^`]+)`/g, '<code class="md-inline-code">$1</code>')
  // 粗体 **...**
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  // 斜体 *...*
  html = html.replace(/\*(.+?)\*/g, '<em>$1</em>')
  // 标题 ### ...
  html = html.replace(/^### (.+)$/gm, '<h4 class="md-h4">$1</h4>')
  html = html.replace(/^## (.+)$/gm, '<h3 class="md-h3">$1</h3>')
  // 无序列表
  html = html.replace(/^[-*] (.+)$/gm, '<li class="md-li">$1</li>')
  // 有序列表
  html = html.replace(/^\d+\. (.+)$/gm, '<li class="md-li">$1</li>')
  // 换行 → <br>
  html = html.replace(/\n/g, '<br>')
  return html
}

// ========== 发送消息 + 打字机效果 ==========
let typingTimer = null
const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || aiThinking.value) return
  messages.push({ id: msgId++, role: 'user', content: text, time: getTimestamp() })
  inputText.value = ''
  await scrollToBottom()
  if (detectCrisis(text)) {
    showCrisisIntervention()
  }
  aiThinking.value = true
  try {
    const res = await chatApi.ask(buildChatRequest(text))
    let aiContent = ''; let isFallback = false
    if (res.data) {
      aiContent = res.data.answer || '抱歉，我没有理解您的问题。'
      isFallback = res.data.isFallback
    } else { aiContent = '抱歉，暂时无法为您解答。' }
    const aiMsg = reactive({ id: msgId++, role: 'ai', content: aiContent, displayContent: '', isFallback, time: getTimestamp(), liked: false, disliked: false, typing: true })
    messages.push(aiMsg)
    await scrollToBottom()
    let idx = 0
    const speed = 15
    speech.speak(aiContent)
    clearInterval(typingTimer)
    typingTimer = setInterval(() => {
      if (idx < aiContent.length) {
        aiMsg.displayContent = aiContent.slice(0, idx + 1)
        idx++
        scrollToBottom()
      } else {
        aiMsg.typing = false; clearInterval(typingTimer)
      }
    }, speed)
  } catch (e) {
    messages.push({ id: msgId++, role: 'ai', content: '抱歉，网络连接出现问题，请稍后再试。', displayContent: '抱歉，网络连接出现问题，请稍后再试。', time: getTimestamp(), liked: false, disliked: false, typing: false })
  } finally {
    aiThinking.value = false
    saveHistory()
    await scrollToBottom()
  }
}

const handleQuickAction = (q) => { if (q.route) router.push(q.route); else { inputText.value = q.text; sendMessage() } }
const readMessage = (c) => speak(c, 'AI 回复')
const copyMessage = (msg) => { navigator.clipboard.writeText(msg.content).then(() => ElMessage.success('已复制')).catch(() => ElMessage.error('复制失败')) }
const toggleLike = (i) => { messages[i].liked = !messages[i].liked; if (messages[i].liked) messages[i].disliked = false; saveHistory() }
const toggleDislike = (i) => { messages[i].disliked = !messages[i].disliked; if (messages[i].disliked) messages[i].liked = false; saveHistory() }
const regenerateMessage = async (index) => {
  if (aiThinking.value) return
  const userMsg = index > 0 ? messages[index - 1] : null
  if (!userMsg || userMsg.role !== 'user') return
  messages.splice(index, 1)
  saveHistory()
  aiThinking.value = true
  try {
    const res = await chatApi.ask(buildChatRequest(userMsg.content))
    let content = ''; let fallback = false
    if (res.data) { content = res.data.answer || '抱歉'; fallback = res.data.isFallback }
    else { content = '抱歉' }
    const aiMsg = reactive({ id: msgId++, role: 'ai', content, displayContent: '', isFallback:fallback, time:getTimestamp(), liked:false, disliked:false, typing:true })
    messages.push(aiMsg); let idx = 0
    speech.speak(content)
    clearInterval(typingTimer)
    typingTimer = setInterval(() => {
      if (idx < content.length) {
        aiMsg.displayContent = content.slice(0, idx + 1)
        idx++
        scrollToBottom()
      } else {
        aiMsg.typing = false
        clearInterval(typingTimer)
      }
    }, 15)
  } catch(e) {
    messages.push({ id: msgId++, role:'ai', content:'重新生成失败，请稍后再试。', displayContent:'重新生成失败，请稍后再试。', time:getTimestamp(), liked:false, disliked:false, typing:false })
  } finally { aiThinking.value = false; saveHistory(); await scrollToBottom() }
}
const clearChat = async () => {
  try {
    await ElMessageBox.confirm('确认清空全部聊天记录吗？数据库中的记录也会删除，且无法恢复。', '清空聊天记录', {
      confirmButtonText: '确认永久清空', cancelButtonText: '取消', type: 'error',
      confirmButtonClass: 'el-button--danger', buttonSize: 'large', autofocus: false
    })
    await chatRecordApi.clearAll()
    messages.splice(0)
    localStorage.removeItem(CHAT_HISTORY_KEY)
    msgId = 1
    ElMessage.success('聊天记录已全部清空')
  } catch (e) { /* 用户取消 */ }
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatScrollbar.value) { const w = chatScrollbar.value.$el?.querySelector('.el-scrollbar__wrap'); if (w) w.scrollTop = w.scrollHeight }
}

onMounted(() => { loadHistory() })
onUnmounted(() => { if (voiceRecognition) try { voiceRecognition.stop() } catch(e) {}; if (typingTimer) clearInterval(typingTimer) })
</script>

<style scoped>
.chat-page { display:flex; flex-direction:column; height:calc(100vh - 60px); margin:-24px; background:#fff; overflow:hidden; }
.chat-header { display:flex; align-items:center; justify-content:space-between; padding:16px 24px; border-bottom:1px solid #e2e8f0; background:#f8faff; flex-shrink:0; }
.chat-title { font-size:22px; font-weight:700; color:#1e293b; margin:0; }
.chat-messages { flex:1; padding:16px 24px; background:linear-gradient(180deg,#f8fafc 0%,#f0f4f8 100%); overflow:hidden; }
.messages-container { max-width:860px; margin:0 auto; }
.welcome-tip { text-align:center; padding:28px 20px 20px; }
.welcome-tip .welcome-text { font-size:20px; color:#64748b; margin:6px 0; }
.welcome-tip .welcome-sub { font-size:16px; color:#94a3b8; }
.quick-questions { margin-top:18px; }
.quick-title { font-size:15px; color:#94a3b8; margin-bottom:10px; }
.quick-question-btn{display:inline-flex;align-items:center;gap:8px;min-height:48px;margin:6px;padding:10px 18px;border:2px solid #7db8f2;border-radius:14px;background:#eaf4ff;color:#173f73;font-size:16px;font-weight:700;cursor:pointer;transition:.2s}
.quick-question-btn:hover{background:#cfe7ff;transform:translateY(-2px)}
.voice-confirm-panel{margin-top:12px;padding:16px;border:2px solid #4a90d9;border-radius:14px;background:#f3f8ff;font-size:18px}
.voice-confirm-panel strong{display:block;margin-bottom:10px}.voice-confirm-actions{display:flex;flex-wrap:wrap;gap:10px;margin-top:12px}
.message-wrapper { margin-bottom:18px; animation:cfi .3s ease-out; }
@keyframes cfi { from{opacity:0;transform:translateY(8px)} to{opacity:1;transform:translateY(0)} }
.msg-time { text-align:center; font-size:13px; color:#94a3b8; margin-bottom:10px; }
.message-user { display:flex; justify-content:flex-end; }
.message-user .msg-time { width:100%; }
.message-ai { display:flex; flex-direction:column; }
.message-row { display:flex; gap:10px; align-items:flex-start; }
.ai-avatar { flex-shrink:0; width:34px; height:34px; margin-top:4px; }
.ai-content { flex:1; min-width:0; }
.message-bubble { padding:14px 18px; word-break:break-word; max-width:100%; }
.message-bubble-user { background:linear-gradient(135deg,#4a90d9,#357abd); color:#fff; border-radius:18px 18px 6px 18px; box-shadow:0 4px 14px rgba(74,144,217,.25); display:inline-block; }
.message-bubble-ai { background:#fff; color:#1e293b; border-radius:6px 18px 18px 18px; box-shadow:0 1px 4px rgba(0,0,0,.06); }
.message-content { font-size:17px; line-height:1.75; white-space:pre-wrap; }
.typing-cursor { display:inline; animation:blink .8s infinite; color:#2563eb; font-weight:700; }
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:0} }
.ai-actions { display:flex; align-items:center; gap:4px; margin-top:8px; padding-left:4px; }
.action-btn { display:flex; align-items:center; justify-content:center; width:32px; height:32px; border:none; border-radius:8px; background:transparent; color:#94a3b8; cursor:pointer; transition:all .2s; }
.action-btn:hover { background:#f1f5f9; color:#475569; }
.action-btn.liked { color:#10b981; }
.action-btn.disliked { color:#ef4444; }
.fallback-tip { font-size:13px; color:#f0a04b; font-style:italic; }
.thinking-bubble { opacity:.8; }
.thinking-dots { font-size:17px; color:#94a3b8; display:flex; align-items:center; gap:0; }
.dot-pulse { animation:dp 1.5s infinite; }
@keyframes dp { 0%,20%{opacity:0} 50%{opacity:1} 80%,100%{opacity:0} }
.chat-input-area { background:#fff; border-top:1px solid #e2e8f0; padding:14px 24px; }
.input-wrapper { max-width:860px; margin:0 auto; display:flex; gap:10px; align-items:center; }
.chat-input { flex:1; }
.chat-input :deep(.el-input__inner) { font-size:17px!important; min-height:46px; }
.input-recording :deep(.el-input__wrapper) { box-shadow:0 0 0 2px #e85d75!important; }
.voice-input-btn { width:46px!important; height:46px!important; flex-shrink:0; background:#f1f5f9!important; border:2px solid #e2e8f0!important; border-radius:50%!important; transition:all .3s; }
.voice-input-btn:hover { background:#e3f0ff!important; border-color:#4a90d9!important; transform:scale(1.05); }
.voice-recording { background:#e85d75!important; border-color:#e85d75!important; animation:vp 1.2s infinite!important; }
.voice-recording :deep(.el-icon) { color:#fff; }
@keyframes vp { 0%{box-shadow:0 0 0 0 rgba(232,93,117,.5);transform:scale(1)} 50%{box-shadow:0 0 0 12px rgba(232,93,117,0);transform:scale(1.06)} 100%{box-shadow:0 0 0 0 rgba(232,93,117,0);transform:scale(1)} }
.voice-error-tip { max-width:860px; margin:8px auto 0; }
.send-btn { min-height:46px; font-size:17px!important; font-weight:600!important; padding:10px 28px!important; }
.chat-messages :deep(.el-scrollbar__wrap) { height:100%; }
.chat-messages :deep(.el-scrollbar__view) { height:auto; }
.chat-messages :deep(.el-scrollbar__thumb) { width:10px!important; background:#a8d4ff; border-radius:5px; }
.chat-messages :deep(.el-scrollbar__thumb:hover) { background:#6db3f2; }
/* Markdown styles */
.md-content :deep(strong) { font-weight:700; color:#1e293b; }
.md-content :deep(em) { font-style:italic; color:#475569; }
.md-content :deep(.md-code-block) { background:#1e293b; color:#e2e8f0; padding:14px 16px; border-radius:10px; overflow-x:auto; font-size:14px; line-height:1.6; margin:8px 0; }
.md-content :deep(.md-inline-code) { background:#f1f5f9; color:#e03131; padding:1px 6px; border-radius:4px; font-size:15px; }
.md-content :deep(.md-h3) { font-size:18px; font-weight:700; margin:10px 0 4px; }
.md-content :deep(.md-h4) { font-size:17px; font-weight:600; margin:8px 0 4px; }
.md-content :deep(.md-li) { margin:2px 0 2px 16px; }
</style>

/**
 * ============================================================
 * useVoiceKing.js - 语音朗读 composable（兼容别名封装）
 * 银发智能生活助手 - 全文朗读功能
 *
 * 历史代码以 useVoiceKing 为名引入语音朗读能力，
 * 底层统一复用 useSpeech（基于 Web Speech API）。
 * 对外提供：
 *   - speak(text, label)   朗读文本
 *   - announceTab(label)   朗读标签页/提示文案（无障碍播报）
 * 其余状态与方法（isSpeaking / pause / resume / stop 等）透传自 useSpeech。
 * ============================================================
 */

import { useSpeech } from './useSpeech'

export function useVoiceKing() {
  const speech = useSpeech()

  // 播报标签/提示文案（无障碍场景）
  const announceTab = (label) => {
    if (label) speech.speak(label)
  }

  return {
    ...speech,
    announceTab
  }
}

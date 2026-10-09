import { ref } from 'vue'

export function useAsyncState(initial = 'loading') {
  const state = ref(initial)
  const errorMessage = ref('')
  const run = async (task, isEmpty = value => Array.isArray(value) && value.length === 0) => {
    state.value = 'loading'; errorMessage.value = ''
    try {
      const value = await task()
      state.value = isEmpty(value) ? 'empty' : 'success'
      return value
    } catch (error) {
      state.value = 'error'
      errorMessage.value = error?.message || '网络或服务暂时不可用'
      throw error
    }
  }
  return { state, errorMessage, run }
}

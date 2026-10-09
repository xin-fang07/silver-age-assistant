<template>
  <section class="async-state" :aria-busy="state === 'loading'">
    <SkeletonLoader v-if="state === 'loading'" :type="type" :rows="rows" />
    <div v-else-if="state === 'error'" class="state-panel error" role="alert">
      <span class="state-icon" aria-hidden="true">!</span>
      <h3>内容加载失败</h3><p>{{ errorMessage || '网络或服务暂时不可用，请稍后重试。' }}</p>
      <el-button type="primary" size="large" @click="$emit('retry')">重新加载</el-button>
    </div>
    <div v-else-if="state === 'empty'" class="state-panel">
      <span class="state-icon empty" aria-hidden="true">○</span>
      <h3>{{ emptyTitle }}</h3><p v-if="emptyDescription">{{ emptyDescription }}</p>
      <slot name="empty-action" />
    </div>
    <slot v-else />
  </section>
</template>
<script setup>
import SkeletonLoader from '@/components/SkeletonLoader.vue'
defineProps({state:{type:String,default:'loading'},errorMessage:{type:String,default:''},emptyTitle:{type:String,default:'暂无数据'},emptyDescription:{type:String,default:''},type:{type:String,default:'table'},rows:{type:Number,default:6}})
defineEmits(['retry'])
</script>
<style scoped>
.state-panel{min-height:260px;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center;padding:32px;color:var(--color-text-secondary)}.state-panel h3{font-size:22px;color:var(--color-text-primary);margin:10px 0 4px}.state-panel p{font-size:17px;margin:0 0 18px}.state-icon{display:grid;place-items:center;width:64px;height:64px;border-radius:50%;background:#fee2e2;color:#b91c1c;font-size:36px;font-weight:800}.state-icon.empty{background:#eaf3ff;color:var(--color-primary)}
</style>

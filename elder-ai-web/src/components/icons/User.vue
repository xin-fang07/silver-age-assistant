<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none">
    <circle cx="12" cy="8" r="4" :fill="fill"/>
    <path d="M4 20c0-4.42 3.58-8 8-8s8 3.58 8 8" :stroke="stroke" stroke-width="2" stroke-linecap="round" fill="none"/>
  </svg>
</template>

<script setup>
defineProps({
  size: { type: Number, default: 24 },
  fill: { type: String, default: '#10b981' },
  stroke: { type: String, default: '#10b981' }
})
</script>
<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none">
    <path d="M12 2L2 7l10 5 10-5-10-5z" :fill="fill"/>
    <path d="M2 17l10 5 10-5" :stroke="stroke" stroke-width="2" fill="none"/>
    <path d="M2 12l10 5 10-5" :stroke="stroke" stroke-width="2" fill="none"/>
  </svg>
</template>

<script setup>
defineProps({
  size: { type: Number, default: 24 },
  fill: { type: String, default: '#8b5cf6' },
  stroke: { type: String, default: '#8b5cf6' }
})
</script>
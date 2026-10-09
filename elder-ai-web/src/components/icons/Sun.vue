<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none">
    <circle cx="12" cy="12" r="5" :fill="fill"/>
    <path d="M12 1v2m0 18v2M4.22 4.22l1.42 1.42m12.72 12.72l1.42 1.42M1 12h2m18 0h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" :stroke="stroke" stroke-width="2" fill="none"/>
  </svg>
</template>

<script setup>
defineProps({
  size: { type: Number, default: 24 },
  fill: { type: String, default: '#fbbf24' },
  stroke: { type: String, default: '#fbbf24' }
})
</script>
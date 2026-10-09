<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none">
    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" :stroke="color" stroke-width="2"/>
    <polyline points="22,6 12,13 2,6" :stroke="color" stroke-width="2"/>
  </svg>
</template>

<script setup>
defineProps({
  size: { type: Number, default: 24 },
  color: { type: String, default: '#4f46e5' }
})
</script>
<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none">
    <path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" :stroke="color" stroke-width="2" fill="none"/>
  </svg>
</template>

<script setup>
defineProps({
  size: { type: Number, default: 24 },
  color: { type: String, default: '#f59e0b' }
})
</script>
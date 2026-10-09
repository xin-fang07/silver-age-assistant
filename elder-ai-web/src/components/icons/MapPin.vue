<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none">
    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" :stroke="color" stroke-width="2" stroke-linecap="round"/>
    <circle cx="12" cy="7" r="4" :stroke="color" stroke-width="2"/>
  </svg>
</template>

<script setup>
defineProps({
  size: { type: Number, default: 24 },
  color: { type: String, default: '#4f46e5' }
})
</script>
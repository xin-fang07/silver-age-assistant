<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none">
    <path d="M6 19h12" :stroke="color" stroke-width="2" stroke-linecap="round"/>
    <path d="M8 7h8v12H8z" :stroke="color" stroke-width="2" fill="none"/>
    <path d="M8 7l4 4 4-4" :stroke="color" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
  </svg>
</template>

<script setup>
defineProps({
  size: { type: Number, default: 24 },
  color: { type: String, default: '#ef4444' }
})
</script>
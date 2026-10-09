<template><div class="chart-summary" role="status" aria-label="图表文字摘要"><strong>数据摘要：</strong><slot>{{ text || '暂无可用统计数据。' }}</slot></div></template>
<script setup>defineProps({text:{type:String,default:''}})</script>
<style scoped>.chart-summary{margin-top:14px;padding:14px 18px;border-left:5px solid var(--color-primary);border-radius:10px;background:#eef6ff;color:#263e59;font-size:16px;line-height:1.7}.chart-summary strong{color:#173f73}</style>

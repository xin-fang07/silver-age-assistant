<template>
  <div class="diet-page">
    <div class="page-header">
      <div class="title-wrap">
        <h2 class="page-title"><el-icon><Food /></el-icon> 智能餐饮推荐</h2>
        <p class="page-desc">根据老人健康状态智能推荐的适老化营养食谱</p>
      </div>
      <div class="elder-info" v-if="elderName">
        <el-tag type="info">老人：{{ elderName }}</el-tag>
        <el-tag :type="conditionTagType">{{ conditionLabel }}</el-tag>
      </div>
    </div>

    <div v-if="loading" class="loading-wrap">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <span>推荐生成中...</span>
    </div>
    <div v-else class="recipe-grid">
      <el-card
        v-for="item in recipes"
        :key="item.name"
        class="recipe-card"
        :class="item.matchLevel"
        shadow="hover"
      >
        <div class="recipe-head">
          <span class="recipe-name">{{ item.name }}</span>
          <el-tag size="small" :type="item.matchLevel === 'match' ? 'success' : 'info'">
            {{ item.matchLevel === 'match' ? '适合病情' : '通用推荐' }}
          </el-tag>
        </div>
        <el-tag size="small" effect="plain" class="meal-tag">{{ item.mealType }}</el-tag>
        <div class="recipe-field"><span class="label">食材：</span>{{ item.ingredients }}</div>
        <div class="recipe-field"><span class="label">营养：</span>{{ item.nutrition }}</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { dietApi, familyApi } from '@/api/index'

const loading = ref(false)
const elderName = ref('')
const conditionLabel = ref('健康')
const recipes = ref([])

const conditionTagType = computed(() => {
  if (conditionLabel.value === '健康') return 'success'
  if (conditionLabel.value === '慢性病' || conditionLabel.value === '需照护') return 'warning'
  return 'info'
})

// 取当前家属绑定的第一个老人档案ID（用于智能标注）
async function loadElderId() {
  try {
    const res = await familyApi.myElders()
    const list = res.data?.list || res.data || []
    if (Array.isArray(list) && list.length) {
      const e = list[0]
      return e.elderInfoId || e.elderId || e.id || null
    }
  } catch (e) {
    console.warn('获取绑定老人失败，使用通用推荐', e)
  }
  return null
}

async function loadRecommend() {
  loading.value = true
  try {
    const elderInfoId = await loadElderId()
    const res = await dietApi.recommend(elderInfoId)
    const d = res.data || {}
    elderName.value = d.elderName || ''
    conditionLabel.value = d.conditionLabel || '健康'
    recipes.value = d.list || []
  } catch (e) {
    console.error('加载餐饮推荐失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(loadRecommend)
</script>

<style scoped>
.diet-page {
  padding: 24px;
  max-width: 1100px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}
.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 22px;
  margin: 0;
}
.page-desc {
  color: #909399;
  margin: 4px 0 0;
  font-size: 13px;
}
.elder-info {
  display: flex;
  gap: 8px;
}
.recipe-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}
.recipe-card {
  border-radius: 12px;
}
.recipe-card.match {
  border-left: 4px solid #67c23a;
}
.recipe-card.normal {
  border-left: 4px solid #dcdfe6;
}
.recipe-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.recipe-name {
  font-size: 16px;
  font-weight: 600;
}
.meal-tag {
  margin-bottom: 10px;
}
.recipe-field {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}
.recipe-field .label {
  color: #909399;
}
.loading-wrap {
  text-align: center;
  padding: 60px;
  color: #909399;
}
</style>

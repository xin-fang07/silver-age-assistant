<template>
  <div class="kn-page">
    <div class="kn-header">
      <h2>健康知识</h2>
      <p class="sub">面向老人与家属的健康科普，由管理员维护，支持 AI 健康问答</p>
    </div>

    <div class="kn-filters">
      <el-input v-model="keyword" placeholder="搜索文章" clearable style="width:240px" @keyup.enter="load" @clear="load" />
      <el-select v-model="category" placeholder="全部分类" clearable style="width:180px" @change="load">
        <el-option label="全部分类" value="" />
        <el-option v-for="c in categories" :key="c.value" :label="c.label" :value="c.value" />
      </el-select>
    </div>

    <div v-loading="loading" class="kn-grid">
      <el-empty v-if="!loading && list.length === 0" description="暂无知识文章" />

      <div
        v-for="item in list"
        :key="item.id"
        class="kn-card"
        @click="goDetail(item.id)"
      >
        <div class="kn-card-cover" v-if="item.coverImage">
          <img :src="item.coverImage" alt="cover" />
        </div>
        <div class="kn-card-body">
          <div class="kn-card-top">
            <el-tag size="small" type="success">{{ item.categoryLabel || item.category }}</el-tag>
            <span class="kn-card-views">👁 {{ item.viewCount || 0 }}</span>
          </div>
          <h3 class="kn-card-title">{{ item.title }}</h3>
          <p class="kn-card-summary">{{ item.summary || '点击查看详情' }}</p>
          <div class="kn-card-foot">
            <span v-if="item.author">{{ item.author }}</span>
            <span>{{ formatTime(item.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>

    <el-pagination
      v-if="total > pageSize"
      v-model:current-page="page"
      :page-size="pageSize"
      :total="total"
      layout="prev, pager, next"
      @current-change="load"
      style="margin-top:18px; justify-content:center"
    />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { knowledgeApi } from '@/api/index'
import { formatFriendlyTime } from '@/utils/friendlyTime'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(9)
const total = ref(0)
const keyword = ref('')
const category = ref('')
const categories = ref([])

const formatTime = (t) => (t ? formatFriendlyTime(t) : '-')

const load = async () => {
  loading.value = true
  try {
    const r = await knowledgeApi.list({
      pageNum: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      category: category.value || undefined
    })
    list.value = r.data || []
    total.value = r.total || 0
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  const r = await knowledgeApi.categories()
  categories.value = r.data || []
}

const goDetail = (id) => router.push(`/care/medical-knowledge/${id}`)

onMounted(() => {
  loadCategories()
  load()
})
</script>

<style scoped>
.kn-page { padding: 20px; max-width: 1100px; margin: 0 auto; }
.kn-header { margin-bottom: 16px; }
.kn-header h2 { margin: 0; }
.sub { color: #888; font-size: 13px; margin: 4px 0 0; }
.kn-filters { display: flex; gap: 12px; margin-bottom: 18px; }
.kn-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 16px; min-height: 120px; }
.kn-card { background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,.06); cursor: pointer; transition: transform .15s, box-shadow .15s; display: flex; flex-direction: column; }
.kn-card:hover { transform: translateY(-3px); box-shadow: 0 6px 18px rgba(0,0,0,.1); }
.kn-card-cover { height: 140px; background: #eef2ff; }
.kn-card-cover img { width: 100%; height: 100%; object-fit: cover; }
.kn-card-body { padding: 14px 16px 16px; display: flex; flex-direction: column; flex: 1; }
.kn-card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.kn-card-views { color: #999; font-size: 12px; }
.kn-card-title { font-size: 16px; margin: 0 0 8px; line-height: 1.4; }
.kn-card-summary { color: #777; font-size: 13px; line-height: 1.6; margin: 0 0 12px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.kn-card-foot { margin-top: auto; display: flex; gap: 12px; color: #aaa; font-size: 12px; }
</style>

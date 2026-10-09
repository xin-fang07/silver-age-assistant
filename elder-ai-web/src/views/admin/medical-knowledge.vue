<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>医疗知识库管理</h2>
        <p class="sub">管理面向老人与家属的健康科普文章（支持分类、草稿/发布、富文本）</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增文章</el-button>
    </div>

    <div class="filters">
      <el-input v-model="keyword" placeholder="搜索标题" clearable style="width:240px" @keyup.enter="load" @clear="load" />
      <el-select v-model="category" placeholder="全部分类" clearable style="width:170px" @change="load">
        <el-option label="全部分类" value="" />
        <el-option v-for="c in categories" :key="c.value" :label="c.label" :value="c.value" />
      </el-select>
      <el-button @click="load">查询</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
      <el-table-column label="分类" width="120">
        <template #default="{ row }">
          <el-tag>{{ row.categoryLabel || row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览量" width="90" />
      <el-table-column label="更新时间" width="180">
        <template #default="{ row }">{{ formatTime(row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      :page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="load"
      style="margin-top:16px"
    />

    <el-dialog v-model="visible" :title="form.id ? '编辑文章' : '新增文章'" width="840px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select v-model="form.category" placeholder="请选择" style="width:100%">
            <el-option v-for="c in categories" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" maxlength="500" show-word-limit placeholder="列表展示用，可选" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.coverImage" placeholder="封面图 URL（可选）" />
        </el-form-item>
        <el-form-item label="正文" required>
          <el-input v-model="form.content" type="textarea" :rows="12" placeholder="支持 HTML 富文本，入库前自动清洗 XSS" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">已发布</el-radio>
            <el-radio :label="0">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { knowledgeAdminApi } from '@/api/index'
import { formatFriendlyTime } from '@/utils/friendlyTime'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const category = ref('')
const categories = ref([])
const visible = ref(false)
const saving = ref(false)
const form = reactive({ id: null, title: '', category: '', summary: '', coverImage: '', content: '', status: 1 })

const formatTime = (t) => (t ? formatFriendlyTime(t) : '-')

const load = async () => {
  loading.value = true
  try {
    const r = await knowledgeAdminApi.list({
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
  const r = await knowledgeAdminApi.categories()
  categories.value = r.data || []
}

const openCreate = () => {
  Object.assign(form, { id: null, title: '', category: '', summary: '', coverImage: '', content: '', status: 1 })
  visible.value = true
}
const openEdit = (row) => {
  Object.assign(form, {
    id: row.id,
    title: row.title,
    category: row.category,
    summary: row.summary || '',
    coverImage: row.coverImage || '',
    content: row.content || '',
    status: row.status ?? 1
  })
  visible.value = true
}
const save = async () => {
  if (!form.title.trim()) return ElMessage.warning('请输入标题')
  if (!form.category) return ElMessage.warning('请选择分类')
  if (!form.content || !form.content.trim()) return ElMessage.warning('请输入正文')
  saving.value = true
  try {
    if (form.id) {
      await knowledgeAdminApi.update(form.id, { ...form })
      ElMessage.success('已更新')
    } else {
      await knowledgeAdminApi.create({ ...form })
      ElMessage.success('已新增')
    }
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}
const remove = async (row) => {
  await ElMessageBox.confirm(`确认删除《${row.title}》？`, '提示', { type: 'warning' })
  await knowledgeAdminApi.remove(row.id)
  ElMessage.success('已删除')
  load()
}

onMounted(() => {
  loadCategories()
  load()
})
</script>

<style scoped>
.page-container { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.sub { color: #888; font-size: 13px; margin: 4px 0 0; }
.filters { display: flex; gap: 12px; margin-bottom: 16px; }
</style>

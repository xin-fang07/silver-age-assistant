<template>
  <div class="family-bind-page">
    <!-- 可绑定老人 -->
    <el-card class="bind-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">可绑定老人</span>
          <el-button text type="primary" :icon="Refresh" @click="loadAvailable">刷新</el-button>
        </div>
      </template>

      <div v-loading="availableLoading">
        <el-empty v-if="!availableLoading && availableList.length === 0" description="暂无可绑定的老人档案" />
        <el-row v-else :gutter="16">
          <el-col v-for="e in availableList" :key="e.elderInfoId" :xs="24" :sm="12" :md="8" :lg="8">
            <el-card class="elder-item" shadow="hover">
              <div class="elder-info">
                <el-avatar :size="48" :src="e.avatar || ''">
                  {{ (e.realName || '老').slice(0, 1) }}
                </el-avatar>
                <div class="elder-meta">
                  <div class="elder-name">{{ e.realName }}</div>
                  <div class="elder-sub">
                    {{ e.gender === 1 ? '男' : '女' }}
                    <span v-if="e.age"> · {{ e.age }}岁</span>
                    <el-tag v-if="e.healthStatus !== undefined && e.healthStatus !== null" size="small" class="health-tag">{{ healthStatusText(e.healthStatus) }}</el-tag>
                  </div>
                </div>
              </div>
              <div class="elder-actions">
                <el-select v-model="bindForm[e.elderInfoId]" placeholder="选择关系" size="small" style="width: 120px">
                  <el-option label="子女" value="子女" />
                  <el-option label="配偶" value="配偶" />
                  <el-option label="护工" value="护工" />
                  <el-option label="其他" value="其他" />
                </el-select>
                <el-button type="primary" size="small" :icon="Link" @click="doBind(e)">绑定</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 已绑定老人 -->
    <el-card class="bind-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">我的已绑定老人</span>
          <el-button text type="primary" :icon="Refresh" @click="loadBound">刷新</el-button>
        </div>
      </template>

      <div v-loading="boundLoading">
        <el-empty v-if="!boundLoading && boundList.length === 0" description="尚未绑定任何老人" />
        <el-row v-else :gutter="16">
          <el-col v-for="e in boundList" :key="e.elderInfoId" :xs="24" :sm="12" :md="8" :lg="8">
            <el-card class="elder-item bound-item" shadow="hover">
              <div class="elder-info">
                <el-avatar :size="52" :src="e.avatar || ''">
                  {{ (e.realName || '老').slice(0, 1) }}
                </el-avatar>
                <div class="elder-meta">
                  <div class="elder-name">{{ e.realName }}</div>
                  <div class="elder-sub">
                    <el-tag size="small" type="success" class="relation-tag">{{ e.relation || '家属' }}</el-tag>
                  </div>
                </div>
              </div>
              <div class="bound-detail">
                <span class="detail-icon">&#128222;</span>
                <span class="detail-text">{{ e.phone || '未填写紧急联系电话' }}</span>
              </div>
              <div class="elder-actions bound-actions">
                <el-popconfirm title="确认解除与该老人的绑定？" @confirm="doUnbind(e)">
                  <template #reference>
                    <el-button type="danger" plain size="small" :icon="Remove">解除绑定</el-button>
                  </template>
                </el-popconfirm>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { familyApi } from '@/api'
import { Refresh, Link, Remove } from '@element-plus/icons-vue'

const availableList = ref([])
const availableLoading = ref(false)
const boundList = ref([])
const boundLoading = ref(false)
const bindForm = reactive({})

const healthStatusText = (s) => {
  return ['健康', '慢性病', '需照护', '其他疾病'][s] || '未知'
}

const loadAvailable = async () => {
  availableLoading.value = true
  try {
    const res = await familyApi.availableElders()
    availableList.value = res.data || []
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    availableLoading.value = false
  }
}

const loadBound = async () => {
  boundLoading.value = true
  try {
    const res = await familyApi.myElders()
    boundList.value = res.data || []
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    boundLoading.value = false
  }
}

const doBind = async (e) => {
  const relation = bindForm[e.elderInfoId] || '子女'
  try {
    await familyApi.bind({ elderInfoId: e.elderInfoId, relation })
    ElMessage.success('绑定成功')
    await Promise.all([loadAvailable(), loadBound()])
  } catch (e) {
    // 错误已由拦截器处理
  }
}

const doUnbind = async (row) => {
  try {
    await familyApi.unbind({ elderInfoId: row.elderInfoId })
    ElMessage.success('已解除绑定')
    await loadBound()
  } catch (e) {
    // 错误已由拦截器处理
  }
}

onMounted(() => {
  loadAvailable()
  loadBound()
})
</script>

<style scoped>
.family-bind-page {
  padding: 4px;
}
.bind-card {
  margin-bottom: 20px;
  border-radius: 12px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 17px;
  font-weight: 600;
  color: #303133;
}
.elder-item {
  margin-bottom: 16px;
  border-radius: 10px;
}
.elder-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.elder-meta {
  flex: 1;
  min-width: 0;
}
.elder-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.elder-sub {
  font-size: 13px;
  color: #909399;
  margin-top: 2px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.health-tag {
  margin-left: 4px;
}
.elder-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

/* 已绑定老人卡片专属样式 */
.relation-tag {
  font-weight: 600;
}
.bound-detail {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  margin-bottom: 12px;
  background: #f7f9fc;
  border-radius: 8px;
  font-size: 14px;
  color: #475569;
}
.bound-detail .detail-icon {
  font-size: 15px;
}
.bound-detail .detail-text {
  flex: 1;
  min-width: 0;
}
.bound-actions {
  justify-content: flex-end;
}
</style>

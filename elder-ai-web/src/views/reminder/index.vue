<!--
  ============================================================
  reminder/index.vue - 家属用药提醒页（时间轴卡片布局）
  银发智能生活助手 - 家属用药提醒管理（吃药、运动、体检、缴费等）
  特性：时间轴分组(今天/明天/未来)、类型色彩标识、语音朗读、
        适老化大字体大按钮、卡片入场/完成/删除动画
  ============================================================
-->
<template>
  <div class="reminder-page">
    <!-- ============================================================
         页面顶部：标题 + 新增按钮
         ============================================================ -->
    <div class="page-header">
      <h2 class="page-title">
        <svg viewBox="0 0 24 24" width="28" height="28" fill="#f0a04b" class="title-icon">
          <path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"/>
        </svg>
        家属用药提醒
      </h2>
      <el-button type="primary" size="large" @click="openAddDialog" class="add-btn">
        <el-icon style="margin-right: 6px"><Plus /></el-icon>
        新增用药提醒
      </el-button>
    </div>

    <!-- 家属用药提醒定位说明 -->
    <div class="reminder-banner">
      <el-icon><Bell /></el-icon>
      <span>家属设置用药提醒，系统到点推送通知，由家属提醒老人服药。</span>
    </div>

    <!-- ============================================================
         加载骨架屏
         ============================================================ -->
    <SkeletonLoader v-if="loading" type="card" :rows="4" />

    <!-- ============================================================
         空状态提示
         ============================================================ -->
    <div v-else-if="reminderList.length === 0" class="empty-state">
      <el-empty description="暂无用药提醒，点击上方按钮为老人添加吧~" :image-size="80">
        <el-button type="primary" size="large" @click="openAddDialog">添加第一个提醒</el-button>
      </el-empty>
    </div>

    <!-- ============================================================
         时间轴分组渲染
         ============================================================ -->
    <div v-else class="timeline-container">
      <!-- ============================================================
           今日完成率进度条卡片（不弹窗、置顶展示）
           ============================================================ -->
      <section class="completion-card">
        <div class="completion-head">
          <div class="completion-label">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="#409eff">
              <path d="M9 16.17 4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/>
            </svg>
            今日已提醒
          </div>
          <div class="completion-percent" :class="{ full: todayStats.percent === 100 }">
            {{ todayStats.percent }}%
          </div>
        </div>
        <el-progress
          :percentage="todayStats.percent"
          :stroke-width="16"
          :color="todayStats.percent === 100 ? '#52b788' : '#409eff'"
          class="completion-progress"
          :show-text="false"
        />
        <div class="completion-sub">
          已提醒 <b>{{ todayStats.done }}</b> / {{ todayStats.total }} 次用药
          <span v-if="todayStats.total === 0" class="completion-empty">（今日暂无提醒）</span>
        </div>
        <div class="care-stats" v-if="careStats">
          近30天：完成 {{ careStats.completed }} 次，跳过 {{ careStats.skipped }} 次，
          未确认 {{ careStats.missed }} 次，综合完成率 <b>{{ careStats.completionRate }}%</b>
        </div>
      </section>

      <TransitionGroup name="group-enter" tag="div">
        <div
          v-for="group in timelineGroups"
          :key="group.key"
          class="timeline-group"
        >
          <!-- 分组标题 -->
          <div class="group-header">
            <div class="group-icon" :style="{ background: group.iconColor }">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="#fff" v-if="group.key === 'today'">
                <path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z"/>
              </svg>
              <svg viewBox="0 0 24 24" width="14" height="14" fill="#fff" v-else-if="group.key === 'tomorrow'">
                <path d="M9 11H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2zm2-7h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11z"/>
              </svg>
              <svg viewBox="0 0 24 24" width="14" height="14" fill="#fff" v-else>
                <path d="M19 4h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V10h14v10zm0-12H5V6h14v2zm-7 5h5v5h-5v-5z"/>
              </svg>
            </div>
            <h3 class="group-label">{{ group.label }}</h3>
            <span class="group-count">{{ group.items.length }} 项</span>
          </div>

          <!-- 时间轴体：每行 = 圆点竖线轨道 + 卡片 -->
          <div class="timeline-body">
            <TransitionGroup name="card-item" tag="div" class="timeline-list">
              <div
                v-for="(item, idx) in group.items"
                :key="item.id"
                class="timeline-row"
              >
                <!-- 左侧：时间轴轨道（圆点 + 竖线） -->
                <div class="timeline-track">
                  <div
                    class="timeline-dot"
                    :style="{
                      background: getTypeColor(item.type),
                      boxShadow: `0 0 0 5px ${getTypeColor(item.type)}22`
                    }"
                  ></div>
                  <div
                    v-if="idx < group.items.length - 1"
                    class="timeline-line"
                  ></div>
                </div>

                <!-- 右侧：提醒卡片 -->
                <div
                  class="reminder-card"
                  :class="{ 'is-done': isItemDone(item), 'is-expired': isItemExpired(item) }"
                  :style="cardStyle(item)"
                >
                  <!-- 勾选框 -->
                  <div class="card-check">
                    <el-checkbox
                      :model-value="isItemDone(item)"
                      :disabled="isItemExpired(item)"
                      @change="(val) => handleToggleDone(item, val)"
                      class="done-check"
                    />
                  </div>

                  <!-- 内容区 -->
                  <div class="card-content">
                    <div class="card-title-row">
                      <span
                        class="card-title"
                        :class="{ 'title-done': isItemDone(item) }"
                      >
                        {{ item.title }}
                      </span>
                      <span class="card-time">
                        <el-icon :size="15"><Clock /></el-icon>
                        {{ formatRemindTime(item.remindTime) }}
                      </span>
                    </div>
                    <p v-if="item.description" class="card-desc">
                      {{ item.description }}
                    </p>
                    <span v-if="elderNameOf(item.elderInfoId)" class="elder-name-tag">
                      <el-icon :size="13"><User /></el-icon>{{ elderNameOf(item.elderInfoId) }}
                    </span>
                    <span
                      class="card-type-tag"
                      :style="{
                        background: getTypeColor(item.type) + '18',
                        color: getTypeColor(item.type),
                        borderColor: getTypeColor(item.type) + '40'
                      }"
                    >
                      {{ getTypeLabel(item.type) }}
                    </span>
                    <span class="repeat-tag">{{ getRepeatLabel(item.repeatType) }}</span>
                    <span v-if="isDue(item)" class="due-tag">到点待处理</span>
                    <span v-if="isItemExpired(item)" class="expired-tag">已错过</span>
                    <span v-if="item.pushStatus && item.pushStatus !== 'PENDING'" class="push-tag" :class="item.pushStatus === 'PUSHED' ? 'pushed' : 'failed'">
                      {{ item.pushStatus === 'PUSHED' ? '已推送到设备' : '推送失败' }}
                    </span>
                    <span v-if="item.confirmStatus === 'CONFIRMED'" class="confirm-tag">老人已确认</span>
                  </div>

                  <!-- 右侧操作按钮 -->
                  <div class="card-actions">
                    <template v-if="isDue(item)">
                      <el-button type="success" size="default" @click="handleComplete(item)">确认已提醒老人</el-button>

                      <el-button type="info" size="default" @click="handleConfirm(item)">模拟老人确认</el-button>
                      <el-button type="warning" size="default" @click="handleSnooze(item)">稍后提醒</el-button>
                      <el-button type="danger" plain size="default" @click="handleSkip(item)">跳过</el-button>
                    </template>
                    <el-button type="primary" size="default" circle title="朗读提醒" @click="handleSpeak(item)" class="action-btn">
                      <el-icon :size="18"><Microphone /></el-icon>
                    </el-button>
                    <el-button type="success" size="default" circle title="分享给家人" @click="handleShare(item)" class="action-btn">
                      <el-icon :size="18"><Share /></el-icon>
                    </el-button>
                    <el-button type="primary" size="default" circle plain title="编辑" @click="openEditDialog(item)" class="action-btn">
                      <el-icon :size="18"><Edit /></el-icon>
                    </el-button>
                    <el-button type="danger" size="default" circle title="删除" @click="handleDelete(item)" class="action-btn">
                      <el-icon :size="18"><Delete /></el-icon>
                    </el-button>                  </div>
                </div>
              </div>
            </TransitionGroup>
          </div>
        </div>
      </TransitionGroup>
    </div>

    <!-- ============================================================
         新增 / 编辑弹窗
         ============================================================ -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑用药提醒' : '新增用药提醒'"
      width="600px"
      :close-on-click-modal="false"
      destroy-on-close
      class="reminder-dialog"
      :append-to-body="true"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
        size="large"
      >
        <!-- 标题 -->
        <el-form-item label="提醒标题" prop="title">
          <el-input
            v-model="formData.title"
            placeholder="例如：提醒张奶奶服用降压药"
          />
        </el-form-item>

        <!-- 关联老人 -->
        <el-form-item label="关联老人" prop="elderInfoId">
          <el-select
            v-model="formData.elderInfoId"
            placeholder="请选择要提醒的老人"
            style="width: 100%"
          >
            <el-option
              v-for="e in myElders"
              :key="e.elderInfoId"
              :label="e.realName"
              :value="e.elderInfoId"
            />
          </el-select>
        </el-form-item>

        <!-- 类型 -->
        <el-form-item label="提醒类型" prop="type">
          <el-select
            v-model="formData.type"
            placeholder="请选择提醒类型"
            style="width: 100%"
          >
            <el-option
              v-for="opt in typeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            >
              <div class="type-option">
                <span class="type-option-dot" :style="{ background: opt.color }"></span>
                <span :style="{ color: opt.color }">{{ opt.label }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 日期时间选择器 - 拆分为日期和时间两个选择器 -->
        <el-form-item label="提醒时间" prop="remindTime">
          <div style="display: flex; gap: 12px;">
            <el-date-picker
              v-model="formData.remindDate"
              type="date"
              placeholder="选择日期"
              format="YYYY年MM月DD日"
              value-format="YYYY-MM-DD"
              :disabled-date="disablePastDates"
              style="flex: 1"
              :teleported="true"
              :popper-props="{ placement: 'bottom-start' }"
            />
            <el-time-picker
              v-model="formData.remindTimeValue"
              placeholder="选择时间"
              format="HH:mm"
              value-format="HH:mm"
              :teleported="true"
              :popper-props="{ placement: 'bottom-start' }"
            />
          </div>
        </el-form-item>

        <el-form-item label="重复提醒" prop="repeatType">
          <el-radio-group v-model="formData.repeatType" size="large">
            <el-radio-button value="ONCE">仅一次</el-radio-button>
            <el-radio-button value="DAILY">每天</el-radio-button>
            <el-radio-button value="WEEKLY">每周</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 描述 -->
        <el-form-item label="提醒描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="描述一下提醒内容（选填）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button size="large" @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            size="large"
            :loading="submitLoading"
            @click="handleSubmit"
          >
            {{ isEditing ? '保存修改' : '确认添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<!-- ============================================================
     Script Setup - 逻辑脚本
     ============================================================ -->
<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reminderApi, familyApi, deviceApi } from '@/api/index'
import { useVoiceKing } from '@/composables/useVoiceKing'
import SkeletonLoader from '@/components/SkeletonLoader.vue'

// ==================== 语音朗读 ====================
const { speak } = useVoiceKing()

// ==================== 提醒类型选项（含配色） ====================
const typeOptions = [
  { value: 'MEDICINE', label: '吃药',  color: '#409eff' },
  { value: 'EXERCISE', label: '运动',  color: '#52b788' },
  { value: 'CHECKUP',  label: '体检',  color: '#f0a04b' },
  { value: 'PAYMENT',  label: '缴费',  color: '#8b5cf6' },
  { value: 'OTHER',    label: '其他',  color: '#94a3b8' }
]

// ==================== 分组图标颜色 ====================
const groupIconColors = {
  today:    '#f0a04b',
  tomorrow: '#6366f1',
  future:   '#64748b',
  history:  '#94a3b8'
}

// ==================== 响应式状态 ====================
const reminderList   = ref([])          // 原始提醒列表
const loading        = ref(false)       // 列表加载状态
const dialogVisible  = ref(false)       // 弹窗显隐
const isEditing      = ref(false)       // 编辑模式标识
const submitLoading  = ref(false)       // 提交按钮加载
const formRef        = ref(null)        // 表单组件引用
const careStats      = ref(null)
const myElders       = ref([])       // 已绑定老人列表（创建提醒时选择关联老人）

// ==================== 表单数据 ====================
const formData = reactive({
  id: null,
  title: '',
  description: '',
  type: '',
  elderInfoId: null,
  remindDate: '',
  remindTimeValue: '',
  repeatType: 'ONCE'
})

// ==================== 表单验证规则 ====================
const formRules = {
  title: [
    { required: true, message: '请输入提醒标题', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择提醒类型', trigger: 'change' }
  ],
  elderInfoId: [
    { required: true, message: '请选择关联老人', trigger: 'change' }
  ],
  remindDate: [
    { required: true, message: '请选择提醒日期', trigger: 'change' }
  ],
  remindTimeValue: [
    { required: true, message: '请选择提醒时间', trigger: 'change' }
  ],
  repeatType: [
    { required: true, message: '请选择重复规则', trigger: 'change' }
  ]
}

// ==================== 计算属性：时间轴分组 ====================
const timelineGroups = computed(() => {
  const now = new Date()
  const todayStr = formatDateKey(now)
  const tomorrowDate = new Date(now)
  tomorrowDate.setDate(tomorrowDate.getDate() + 1)
  const tomorrowStr = formatDateKey(tomorrowDate)

  const todayItems = []
  const tomorrowItems = []
  const futureItems = []
  const historyItems = []

  reminderList.value.forEach((item) => {
    const timeStr = item.remindTime || ''
    // 无时间或仅时间格式(HH:mm) → 视为今天
    if (!timeStr || /^\d{2}:\d{2}(:\d{2})?$/.test(timeStr.trim())) {
      todayItems.push(item)
      return
    }
    // 完整日期时间 → 按日期分组
    const d = new Date(timeStr)
    if (isNaN(d.getTime())) {
      todayItems.push(item)
      return
    }
    const key = formatDateKey(d)
    if (key < todayStr || isItemExpired(item)) {
      historyItems.push(item)
      return
    }
    if (key === todayStr) {
      todayItems.push(item)
    } else if (key === tomorrowStr) {
      tomorrowItems.push(item)
    } else {
      futureItems.push(item)
    }
  })

  // 只返回非空分组
  const groups = []
  if (todayItems.length) {
    groups.push({
      key: 'today',
      label: '今天',
      items: todayItems,
      iconColor: groupIconColors.today
    })
  }
  if (tomorrowItems.length) {
    groups.push({
      key: 'tomorrow',
      label: '明天',
      items: tomorrowItems,
      iconColor: groupIconColors.tomorrow
    })
  }
  if (futureItems.length) {
    groups.push({
      key: 'future',
      label: '未来',
      items: futureItems,
      iconColor: groupIconColors.future
    })
  }
  if (historyItems.length) {
    groups.push({
      key: 'history',
      label: '历史记录',
      items: historyItems,
      iconColor: groupIconColors.history
    })
  }
  return groups
})

// ==================== 计算属性：今日完成率 ====================
const todayStats = computed(() => {
  const todayKey = formatDateKey(new Date())
  const items = reminderList.value.filter(item => {
    const remindDate = new Date(item.remindTime || '')
    const completedDate = new Date(item.lastCompletedAt || '')
    return (!isNaN(remindDate.getTime()) && formatDateKey(remindDate) === todayKey)
      || (!isNaN(completedDate.getTime()) && formatDateKey(completedDate) === todayKey)
  })
  const total = items.length
  const done = items.filter(i => isItemDone(i) || completedToday(i)).length
  const percent = total === 0 ? 0 : Math.round((done / total) * 100)
  return { total, done, percent }
})

// ==================== 自动推送（到点自动推送到设备） ====================
const autoPushTimer = ref(null)
const autoPushingIds = new Set()  // 防止同一提醒并发重复推送

const autoPushDueReminders = async () => {
  const list = reminderList.value
  if (!list || list.length === 0) return
  const due = list.filter(
    (item) => isDue(item) && item.pushStatus !== 'PUSHED' && isItemPending(item)
  )
  if (due.length === 0) return
  let pushedCount = 0
  for (const item of due) {
    if (autoPushingIds.has(item.id)) continue
    autoPushingIds.add(item.id)
    try {
      await deviceApi.pushReminderToDevice(item.id)
      // 直接更新本地状态，避免整列表 loading 闪烁
      reminderList.value = list.map(x =>
        x.id === item.id ? { ...x, pushStatus: 'PUSHED' } : x
      )
      pushedCount++
    } catch (e) {
      console.error('自动推送失败：', e)
    } finally {
      autoPushingIds.delete(item.id)
    }
  }
  // 合并提示，避免多条提醒连续弹出多个 Toast 造成打扰
  if (pushedCount > 0) {
    ElMessage.success(`已自动推送 ${pushedCount} 条提醒到老人设备`)
  }
}

// ==================== 生命周期 ====================
onMounted(() => {
  fetchReminderList()
  fetchMyElders()
  autoPushTimer.value = setInterval(autoPushDueReminders, 30000)
})

onUnmounted(() => {
  if (autoPushTimer.value) {
    clearInterval(autoPushTimer.value)
    autoPushTimer.value = null
  }
})

// ==================== API 调用 ====================

/** 加载已绑定老人列表，供创建提醒时选择关联老人 */
const fetchMyElders = async () => {
  try {
    const res = await familyApi.myElders()
    myElders.value = Array.isArray(res.data) ? res.data : []
  } catch (error) {
    console.error('加载已绑定老人失败：', error)
  }
}

/** 加载提醒列表 */
const fetchReminderList = async () => {
  loading.value = true
  try {
    const res = await reminderApi.list()
    let list = []
    if (res.data) {
      list = Array.isArray(res.data)
        ? res.data
        : (res.data.records || [])
    }
    // 后端字段 remindType/content 映射为前端内部字段 type/description
    reminderList.value = list.map(item => ({
      ...item,
      type: item.remindType || item.type || '',
      description: item.content || item.description || '',
      repeatType: item.repeatType || 'ONCE'
    }))
    const statRes = await reminderApi.statistics(30)
    careStats.value = statRes.data || null
  } catch (error) {
    console.error('加载提醒列表失败：', error)
    ElMessage.error('加载提醒列表失败')
  } finally {
    loading.value = false
  }
}

// ==================== 弹窗操作 ====================

/** 根据老人档案ID获取姓名（用于卡片展示关联老人） */
const elderNameOf = (id) => {
  if (!id) return ''
  const e = myElders.value.find(x => x.elderInfoId === id)
  return e ? e.realName : ''
}

/** 打开新增弹窗 */
const openAddDialog = () => {
  isEditing.value = false
  formData.id = null
  formData.title = ''
  formData.description = ''
  formData.type = ''
  formData.elderInfoId = null
  const defaultTime = defaultReminderTime()
  formData.remindDate = defaultTime.split(' ')[0]
  formData.remindTimeValue = defaultTime.split(' ')[1].substring(0, 5)
  formData.repeatType = 'ONCE'
  dialogVisible.value = true
  setTimeout(() => {
    formRef.value?.resetFields()
  }, 0)
}

/** 打开编辑弹窗 */
const openEditDialog = (item) => {
  isEditing.value = true
  formData.id = item.id
  formData.title = item.title || ''
  formData.description = item.description || ''
  formData.type = item.type || ''
  formData.elderInfoId = item.elderInfoId || null
  const timeStr = normalizeDateTime(item.remindTime)
  formData.remindDate = timeStr.split(' ')[0]
  formData.remindTimeValue = timeStr.split(' ')[1].substring(0, 5)
  formData.repeatType = item.repeatType || 'ONCE'
  dialogVisible.value = true
  setTimeout(() => {
    formRef.value?.clearValidate()
  }, 0)
}

/** 提交表单（新增/编辑） */
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  submitLoading.value = true
  try {
    const remindTime = `${formData.remindDate} ${formData.remindTimeValue}:00`
    const payload = {
      title: formData.title,
      content: formData.description,
      remindType: formData.type,
      remindTime: remindTime,
      repeatType: formData.repeatType,
      elderInfoId: formData.elderInfoId
    }

    if (isEditing.value) {
      await reminderApi.update(formData.id, payload)
      ElMessage.success('提醒修改成功！')
    } else {
      await reminderApi.add(payload)
      ElMessage.success('提醒添加成功！')
    }
    dialogVisible.value = false
    await fetchReminderList()
  } catch (error) {
    ElMessage.error('提交失败，请稍后重试')
    console.error('提交提醒失败：', error)
  } finally {
    submitLoading.value = false
  }
}

// ==================== 卡片操作 ====================

/** 切换完成状态 */
const handleToggleDone = async (item, checked) => {
  try {
    if (checked) {
      await reminderApi.complete(item.id)
      ElMessage.success('已标记为完成')
    } else {
      // 取消完成：调用专用接口恢复为待提醒
      await reminderApi.uncomplete(item.id)
      ElMessage.success('已取消完成')
    }
    await fetchReminderList()
  } catch (error) {
    ElMessage.error('操作失败，请稍后重试')
    console.error('切换完成状态失败：', error)
  }
}

const handlePush = async (item) => {
  await deviceApi.pushReminderToDevice(item.id)
  ElMessage.success('已推送到老人设备')
  await fetchReminderList()
}
const handleConfirm = async (item) => {
  await deviceApi.confirmReminderByElder(item.id)
  ElMessage.success('已模拟老人确认收到')
  await fetchReminderList()
}

const handleComplete = async (item) => {
  await reminderApi.complete(item.id)
  ElMessage.success('已记录提醒时间')
  await fetchReminderList()
}

const handleSnooze = async (item) => {
  try {
    const { value } = await ElMessageBox.prompt('多少分钟后再次提醒？（5-240分钟）', '稍后提醒', {
      inputValue: '10', inputPattern: /^(?:[5-9]|[1-9]\d|1\d\d|2[0-3]\d|240)$/,
      inputErrorMessage: '请输入5到240之间的整数'
    })
    await reminderApi.snooze(item.id, Number(value))
    ElMessage.success(`将在${value}分钟后再次提醒`)
    await fetchReminderList()
  } catch (e) { /* 用户取消或请求层已提示 */ }
}

const handleSkip = async (item) => {
  try {
    await ElMessageBox.confirm(`确定跳过本次“${item.title}”吗？本次将计为未完成。`, '跳过提醒', {
      type: 'warning', confirmButtonText: '确认跳过', cancelButtonText: '返回'
    })
    await reminderApi.skip(item.id)
    ElMessage.success('已记录为跳过')
    await fetchReminderList()
  } catch (e) { /* 用户取消 */ }
}

/** 删除提醒 */
const handleDelete = (item) => {
  ElMessageBox.confirm(
    `确定要删除提醒"${item.title}"吗？删除后无法恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    }
  ).then(async () => {
    try {
      await reminderApi.delete(item.id)
      // 从列表移除（触发 TransitionGroup 离开动画）
      reminderList.value = reminderList.value.filter(r => r.id !== item.id)
      ElMessage.success('已删除')
    } catch (error) {
      console.error('删除提醒失败：', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

/** 朗读提醒 */
const handleSpeak = (item) => {
  const typeLabel = getTypeLabel(item.type)
  const time = formatRemindTime(item.remindTime)
  const text = `提醒：${item.title}。${item.description || ''}。类型：${typeLabel}。时间：${time}。`
  speak(text, '家属用药提醒')
}

/** 分享提醒给家人 */
const handleShare = (item) => {
  const typeLabel = getTypeLabel(item.type)
  const time = formatRemindTime(item.remindTime)
  const shareText = `【银发智能生活助手】提醒事项分享\n\n标题：${item.title}\n类型：${typeLabel}\n时间：${time}\n描述：${item.description || '无'}\n重复：${getRepeatLabel(item.repeatType)}`

  if (navigator.share) {
    navigator.share({
      title: '家属用药提醒分享',
      text: shareText,
    }).then(() => {
      ElMessage.success('分享成功')
    }).catch(() => {
      copyShareText(shareText)
      ElMessage.info('已复制提醒内容，请手动分享')
    })
  } else {
    copyShareText(shareText)
    ElMessage.info('已复制提醒内容，请手动分享')
  }
}

const copyShareText = (text) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('内容已复制到剪贴板')
  }).catch(() => {
    const textarea = document.createElement('textarea')
    textarea.value = text
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('内容已复制到剪贴板')
  })
}

// ==================== 工具函数 ====================

/** 获取类型对应的颜色 */
const getTypeColor = (type) => {
  if (!type) return '#94a3b8'
  // 生日类型特殊处理
  if (type.includes('生日')) return '#f472b6'
  const map = {
    'MEDICINE': '#409eff',
    'EXERCISE': '#52b788',
    'CHECKUP':  '#f0a04b',
    'PAYMENT':  '#8b5cf6',
    'OTHER':    '#94a3b8'
  }
  return map[type] || '#94a3b8'
}

/** 彩色卡片样式：按类型色生成浅色渐变背景 + 左侧色条，让卡片整体彩色化 */
const cardStyle = (item) => {
  const c = getTypeColor(item.type)
  return {
    borderLeftColor: c,
    background: `linear-gradient(135deg, ${c}14, ${c}07)`,
    '--rc': c
  }
}

/** 获取类型中文标签 */
const getTypeLabel = (type) => {
  if (!type) return '其他'
  if (type.includes('生日')) return '生日'
  const map = {
    'MEDICINE': '吃药',
    'EXERCISE': '运动',
    'CHECKUP':  '体检',
    'PAYMENT':  '缴费',
    'OTHER':    '其他'
  }
  return map[type] || type || '其他'
}

/** 判断提醒是否已完成（后端 status：0-待提醒, 1-已完成, 2-已过期） */
const isItemDone = (item) => {
  return item.status === 1 || item.status === '1'
}

const isItemPending = (item) => item.status === 0 || item.status === '0'
const isItemExpired = (item) => item.status === 2 || item.status === '2'
const isDue = (item) => {
  if (!isItemPending(item)) return false
  const date = new Date(String(item.remindTime || '').replace(' ', 'T'))
  return !isNaN(date.getTime()) && date <= new Date()
}

const completedToday = (item) => {
  if (!item.lastCompletedAt) return false
  const date = new Date(item.lastCompletedAt)
  return !isNaN(date.getTime()) && formatDateKey(date) === formatDateKey(new Date())
}

const getRepeatLabel = (repeatType) => ({
  ONCE: '仅一次',
  DAILY: '每天',
  WEEKLY: '每周'
}[repeatType] || '仅一次')

/** 格式化 remindTime 显示 */
const formatRemindTime = (timeStr) => {
  if (!timeStr) return ''
  // 纯时间格式 HH:mm 或 HH:mm:ss
  if (/^\d{2}:\d{2}(:\d{2})?$/.test(String(timeStr).trim())) {
    return String(timeStr).trim()
  }
  // 完整日期时间字符串 → 提取 MM-DD HH:mm
  const d = new Date(timeStr)
  if (isNaN(d.getTime())) return timeStr
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day   = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const mins  = String(d.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${mins}`
}

/** 将后端时间转换为日期时间选择器需要的格式 */
const normalizeDateTime = (timeStr) => {
  if (!timeStr) return ''
  const d = new Date(String(timeStr).replace(' ', 'T'))
  if (isNaN(d.getTime())) return ''
  return formatDateTime(d)
}

const formatDateTime = (date) => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${hh}:${mm}:00`
}

const defaultReminderTime = () => {
  const date = new Date()
  date.setMinutes(0, 0, 0)
  date.setHours(date.getHours() + 1)
  return formatDateTime(date)
}

const disablePastDates = (date) => date.getTime() < new Date().setHours(0, 0, 0, 0)

/** 将 Date 转为 YYYY-MM-DD 字符串用于日期比较 */
const formatDateKey = (date) => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}
</script>



<!-- ============================================================
     Scoped Styles - 时间轴卡片布局样式
     ============================================================ -->
<style scoped>
/*
 * 整体页面容器
 */
.reminder-page {
  min-height: 100%;
  max-width: 900px;
  margin: 0 auto;
}

.care-stats { margin-top: 12px; color: #475569; font-size: 15px; line-height: 1.6; }

/* ============================================================
   页面顶部
   ============================================================ */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0 28px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  flex-shrink: 0;
}

.reminder-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 4px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #fff7ed, #fef9f3);
  border: 1px solid #fed7aa;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.6;
  color: #9a5a1a;
  max-width: 760px;
}
.reminder-banner .el-icon { color: #f0a04b; font-size: 20px; flex-shrink: 0; }

.add-btn {
  height: 48px;
  min-height: 48px;
  font-size: 17px;
  font-weight: 600;
  border-radius: 14px;
  padding: 0 24px;
}

/* ============================================================
   今日完成率进度条卡片
   ============================================================ */
.completion-card {
  background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 100%);
  border: 1px solid #dbeafe;
  border-radius: 18px;
  padding: 22px 26px;
  margin-bottom: 32px;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.08);
}

.completion-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.completion-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 19px;
  font-weight: 700;
  color: #1e3a5f;
}

.completion-percent {
  font-size: 32px;
  font-weight: 800;
  color: #409eff;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.completion-percent.full {
  color: #52b788;
}

.completion-progress {
  margin: 4px 0 12px;
}

/* 进度条圆角 + 高度（覆盖 Element Plus 默认） */
.completion-progress :deep(.el-progress-bar__outer) {
  border-radius: 10px;
  background-color: #dbeafe;
}

.completion-progress :deep(.el-progress-bar__inner) {
  border-radius: 10px;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.completion-sub {
  font-size: 16px;
  color: #475569;
}

.completion-sub b {
  color: #1e3a5f;
  font-size: 18px;
}

.completion-empty {
  color: #94a3b8;
}

/* ============================================================
   空状态
   ============================================================ */
.empty-state {
  padding: 60px 20px;
}

.empty-state :deep(.el-empty__description) {
  font-size: 18px !important;
}

/* ============================================================
   时间轴容器
   ============================================================ */
.timeline-container {
  position: relative;
}

/* ============================================================
   分组区域
   ============================================================ */
.timeline-group {
  margin-bottom: 36px;
}

/* 分组标题 */
.group-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.group-icon {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.group-label {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  flex: 1;
}

.group-count {
  font-size: 15px;
  color: #94a3b8;
  font-weight: 500;
  background: #f8fafc;
  padding: 4px 14px;
  border-radius: 20px;
}

/* ============================================================
   时间轴体 - 每行 = 轨道(圆点+竖线) + 卡片
   ============================================================ */
.timeline-body {
  position: relative;
}

.timeline-list {
  position: relative;
}

/* 每一行 */
.timeline-row {
  display: flex;
  align-items: flex-start;
  position: relative;
}

/* 左侧轨道列 */
.timeline-track {
  width: 40px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  align-self: stretch;
  padding-top: 18px;
}

/* 类型彩色圆点 */
.timeline-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  flex-shrink: 0;
  position: relative;
  z-index: 2;
  border: 3px solid #fff;
  transition: transform 0.3s ease;
}

/* 竖线（连接相邻圆点） */
.timeline-line {
  width: 2px;
  flex: 1;
  background: #e2e8f0;
  margin-top: 8px;
  border-radius: 1px;
}

/* ============================================================
   提醒卡片
   ============================================================ */
.reminder-card {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin-bottom: 18px;
  padding: 18px 22px;
  background: #ffffff;
  border: 1px solid #f1f5f9;
  border-left: 6px solid #94a3b8;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

/* 卡片悬停效果 */
.reminder-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border-color: #e2e8f0;
  transform: translateY(-1px);
}

/* 已完成卡片 */
.reminder-card.is-done {
  opacity: 0.5;
  background: #fafbfc;
}

.reminder-card.is-done:hover {
  opacity: 0.7;
}

.reminder-card.is-expired {
  opacity: 0.7;
  filter: grayscale(0.35);
}

/* ============================================================
   勾选框
   ============================================================ */
.card-check {
  padding-top: 2px;
  flex-shrink: 0;
}

.done-check {
  --el-checkbox-checked-bg-color: #52b788;
  --el-checkbox-checked-input-border-color: #52b788;
}

.done-check :deep(.el-checkbox__inner) {
  width: 22px !important;
  height: 22px !important;
  border-radius: 6px !important;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.done-check :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  transform: scale(1.15);
}

/* 完成勾选动画 */
.done-check :deep(.el-checkbox__input.is-checked .el-checkbox__inner::after) {
  animation: check-bounce 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes check-bounce {
  0%   { transform: rotate(45deg) scale(0); }
  50%  { transform: rotate(45deg) scale(1.3); }
  100% { transform: rotate(45deg) scale(1); }
}

/* ============================================================
   卡片内容区
   ============================================================ */
.card-content {
  flex: 1;
  min-width: 0;
}

/* 标题行：标题 + 时间 */
.card-title-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.5;
  transition: color 0.3s ease, text-decoration 0.3s ease;
}

/* 已完成标题 - 删除线 */
.title-done {
  color: #94a3b8;
  text-decoration: line-through;
}

/* 时间 */
.card-time {
  font-size: 15px;
  color: #64748b;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
  flex-shrink: 0;
}

/* 描述 */
.card-desc {
  font-size: 16px;
  color: #64748b;
  margin: 6px 0 10px;
  line-height: 1.6;
}

/* 类型标签 */
.elder-name-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 13px;
  color: #764ba2;
  background: #f3eefe;
  border: 1px solid #e0d4f7;
  border-radius: 10px;
  padding: 2px 8px;
  margin-right: 6px;
}

.card-type-tag {
  display: inline-block;
  font-size: 13px;
  font-weight: 600;
  padding: 3px 12px;
  border-radius: 20px;
  border: 1px solid;
  letter-spacing: 0.3px;
}

.repeat-tag,
.due-tag,
.expired-tag {
  display: inline-block;
  margin-left: 8px;
  padding: 3px 10px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 700;
}

.repeat-tag { background: #eef2ff; color: #4338ca; }
.due-tag { background: #fff7ed; color: #c2410c; }
.expired-tag { background: #f1f5f9; color: #475569; }

/* ============================================================
   右侧操作按钮
   ============================================================ */
.card-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
  align-items: center;
  flex-shrink: 0;
  margin-left: auto;
  max-width: 60%;
}

.action-btn {
  width: 44px !important;
  height: 44px !important;
  min-height: 44px;
  border-radius: 12px !important;
  transition: all 0.2s ease;
}

.action-btn:hover {
  transform: scale(1.08);
}

.action-btn:active {
  transform: scale(0.95);
}

/* ============================================================
   卡片入场/离开动画（TransitionGroup）
   ============================================================ */

/* 进入动画：从下方滑入 + 淡入 */
.card-item-enter-active {
  transition: all 0.45s cubic-bezier(0.4, 0, 0.2, 1);
}

.card-item-enter-from {
  opacity: 0;
  transform: translateY(24px);
}

/* 离开动画：向右滑出 + 淡出 */
.card-item-leave-active {
  transition: all 0.35s ease-in;
}

.card-item-leave-to {
  opacity: 0;
  transform: translateX(50px);
}

/* 列表项移动动画（填补空隙） */
.card-item-move {
  transition: transform 0.35s ease;
}

/* 分组淡入 */
.group-enter-enter-active {
  transition: all 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.group-enter-enter-from {
  opacity: 0;
  transform: translateY(16px);
}

/* ============================================================
   弹窗样式
   ============================================================ */
.reminder-dialog :deep(.el-dialog) {
  border-radius: 20px;
  overflow: visible;
  max-height: 80vh;
}

.reminder-dialog :deep(.el-dialog__header) {
  padding: 24px 28px 0;
  margin: 0;
}

.reminder-dialog :deep(.el-dialog__title) {
  font-size: 22px !important;
  font-weight: 700 !important;
  color: #1e293b;
}

.reminder-dialog :deep(.el-dialog__body) {
  padding: 20px 28px 8px;
}

.reminder-dialog :deep(.el-dialog__footer) {
  padding: 0 28px 24px;
}

/* 弹窗表单项 */
.reminder-dialog :deep(.el-form-item__label) {
  font-size: 16px !important;
  font-weight: 600;
  color: #334155;
}

/* 类型选项 */
.type-option {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}

.type-option-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* 下拉选项行高 */
.reminder-dialog :deep(.el-select-dropdown__item) {
  font-size: 16px !important;
  min-height: 44px;
  line-height: 44px;
}

/* 弹窗底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  height: 48px;
  min-height: 48px;
  font-size: 17px;
  border-radius: 14px;
  padding: 0 24px;
}

/* 日期时间选择器弹出层样式 */
.reminder-dialog :deep(.el-date-picker__popper) {
  z-index: 9999 !important;
  position: fixed !important;
  left: auto !important;
  right: auto !important;
  top: auto !important;
}

/* 确保弹窗内选择器弹出层不被遮挡 */
.reminder-dialog :deep(.el-dialog) {
  overflow: visible;
}

/* 弹窗容器允许溢出 */
.reminder-dialog :deep(.el-dialog__wrapper) {
  overflow: visible;
}

/* ============================================================
   响应式适配
   ============================================================ */
@media (max-width: 768px) {
  .page-header {
    padding: 4px 0 20px;
  }

  .page-title {
    font-size: 24px;
  }

  .add-btn {
    height: 44px;
    min-height: 44px;
    font-size: 16px;
    border-radius: 12px;
  }

  .group-label {
    font-size: 18px;
  }

  .reminder-card {
    padding: 16px 16px;
    border-radius: 14px;
    gap: 10px;
  }

  .card-title {
    font-size: 17px;
  }

  .card-time {
    font-size: 14px;
  }

  .card-desc {
    font-size: 15px;
  }

  .card-actions {
    gap: 6px;
  }

  .action-btn {
    width: 40px !important;
    height: 40px !important;
    min-height: 40px;
  }

  .timeline-track {
    width: 32px;
    padding-top: 16px;
  }

  .timeline-dot {
    width: 12px;
    height: 12px;
  }
}

@media (max-width: 480px) {
  .reminder-card {
    flex-wrap: wrap;
  }

  .card-check {
    order: 1;
  }

  .card-content {
    order: 2;
    flex-basis: 100%;
  }

  .card-actions {
    order: 3;
    margin-left: 0;
    width: 100%;
    justify-content: flex-end;
    padding-top: 8px;
    border-top: 1px solid #f1f5f9;
  }

  .card-title-row {
    flex-direction: column;
    gap: 4px;
  }
}
.push-tag { display:inline-flex; align-items:center; margin-left:6px; padding:2px 8px; border-radius:10px; font-size:12px; }
.push-tag.pushed { background:#e8f5e9; color:#2e7d32; }
.push-tag.failed { background:#ffebee; color:#c62828; }
.confirm-tag { display:inline-flex; align-items:center; margin-left:6px; padding:2px 8px; border-radius:10px; font-size:12px; background:#e3f2fd; color:#1565c0; }

</style>

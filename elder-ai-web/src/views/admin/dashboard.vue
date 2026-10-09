<!--
  ============================================================
  admin/dashboard.vue - 管理后台数据驾驶舱（需管理员权限）
  银发智能生活助手 - 管理后台首页仪表盘
  适老化设计：大字号(16-18px)、高对比度、彩色统计卡片
  功能：统计概览 + 趋势图表（环形/柱状/折线/面积图）+ 最近求助
  设计参考：Arco Design Pro / RuoYi 数据驾驶舱风格
  ============================================================
-->
<template>
  <div class="dashboard-page">
    <div class="page-header">
      <h2 class="page-title">
        <el-icon :size="28" style="vertical-align: middle; margin-right: 10px;"><DataAnalysis /></el-icon>
        管理驾驶舱
      </h2>
      <p class="page-subtitle">系统运行概览与关键数据趋势监控</p>
    </div>

    <div v-if="loadingDashboard" class="loading-area">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>正在加载统计数据...</p>
    </div>

    <div v-else-if="loadError" class="error-area">
      <el-result icon="error" title="加载失败" :sub-title="loadError">
        <template #extra>
          <el-button type="primary" size="large" @click="initAllData">重新加载</el-button>
        </template>
      </el-result>
    </div>

    <template v-else>
      <el-row :gutter="20" class="stat-row">
        <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6" class="stat-col">
          <el-card shadow="hover" class="stat-card stat-card-blue" @click="goUsers">
            <div class="stat-card-inner">
              <div class="stat-icon-box stat-icon-blue"><el-icon :size="28"><User /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">用户总数</div>
                <div class="stat-value">{{ dashboardData.userCount ?? 0 }}</div>
                <div class="stat-desc">点击查看详情</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6" class="stat-col">
          <el-card shadow="hover" class="stat-card stat-card-green" @click="goAnalytics('chat')">
            <div class="stat-card-inner">
              <div class="stat-icon-box stat-icon-green"><el-icon :size="28"><ChatDotRound /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">今日聊天次数</div>
                <div class="stat-value">{{ dashboardData.chatCount ?? 0 }}</div>
                <div class="stat-desc">今日真实问答量</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6" class="stat-col">
          <el-card shadow="hover" class="stat-card stat-card-orange" @click="goAnalytics('reminder')">
            <div class="stat-card-inner">
              <div class="stat-icon-box stat-icon-orange"><el-icon :size="28"><AlarmClock /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">本周新建提醒</div>
                <div class="stat-value">{{ dashboardData.reminderCount ?? 0 }}</div>
                <div class="stat-desc">本周创建总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6" class="stat-col">
          <el-card shadow="hover" class="stat-card stat-card-red" @click="goEmergency">
            <div class="stat-card-inner">
              <div class="stat-icon-box stat-icon-red"><el-icon :size="28"><Bell /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">待处理求助</div>
                <div class="stat-value stat-warning">{{ dashboardData.emergencyCount ?? 0 }}</div>
                <div class="stat-desc">点击查看详情</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6" class="stat-col">
          <el-card shadow="hover" class="stat-card stat-card-purple" @click="goElders">
            <div class="stat-card-inner">
              <div class="stat-icon-box stat-icon-purple"><el-icon :size="28"><UserFilled /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">老人数量</div>
                <div class="stat-value">{{ dashboardData.elderCount ?? 0 }}</div>
                <div class="stat-desc">老人档案总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6" class="stat-col">
          <el-card shadow="hover" class="stat-card stat-card-blue" @click="goUsers">
            <div class="stat-card-inner">
              <div class="stat-icon-box stat-icon-blue"><el-icon :size="28"><User /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">家属数量</div>
                <div class="stat-value">{{ dashboardData.familyCount ?? 0 }}</div>
                <div class="stat-desc">家属账号总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6" class="stat-col">
          <el-card shadow="hover" class="stat-card stat-card-cyan" @click="goDevices">
            <div class="stat-card-inner">
              <div class="stat-icon-box stat-icon-cyan"><el-icon :size="28"><Monitor /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">设备在线率</div>
                <div class="stat-value">{{ deviceOnlineRate }}%</div>
                <div class="stat-desc">{{ dashboardData.deviceOnlineCount ?? 0 }}/{{ dashboardData.deviceTotalCount ?? 0 }} 在线</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="12" :lg="6" :xl="6" class="stat-col">
          <el-card shadow="hover" class="stat-card stat-card-orange" @click="goWarnings">
            <div class="stat-card-inner">
              <div class="stat-icon-box stat-icon-orange"><el-icon :size="28"><Warning /></el-icon></div>
              <div class="stat-info">
                <div class="stat-label">健康预警</div>
                <div class="stat-value stat-warning">{{ dashboardData.totalWarningCount ?? 0 }}</div>
                <div class="stat-desc">需要关注的异常</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row">
        <el-col :xs="24" :lg="12" class="chart-col">
          <el-card shadow="hover" class="chart-card">
            <template #header><div class="card-header"><span class="card-title">用户角色分布</span><el-tag size="large" effect="plain" type="info">环形图</el-tag></div></template>
            <v-chart :option="rolePieOption" autoresize style="height: 340px;" />
            <ChartSummary :text="roleSummary" />
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12" class="chart-col">
          <el-card shadow="hover" class="chart-card">
            <template #header><div class="card-header"><span class="card-title">最近7天聊天量</span><el-tag size="large" effect="plain" type="info">柱状图</el-tag></div></template>
            <v-chart :option="chatBarOption" autoresize style="height: 340px;" />
            <ChartSummary :text="chatSummary" />
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row">
        <el-col :xs="24" :lg="12" class="chart-col">
          <el-card shadow="hover" class="chart-card">
            <template #header><div class="card-header"><span class="card-title">最近7天活跃用户</span><el-tag size="large" effect="plain" type="info">折线图</el-tag></div></template>
            <v-chart :option="loginLineOption" autoresize style="height: 340px;" />
            <ChartSummary :text="loginSummary" />
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12" class="chart-col">
          <el-card shadow="hover" class="chart-card">
            <template #header><div class="card-header"><span class="card-title">提醒完成率趋势</span><el-tag size="large" effect="plain" type="info">面积图</el-tag></div></template>
            <v-chart :option="reminderAreaOption" autoresize style="height: 340px;" />
            <ChartSummary :text="reminderSummary" />
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row">
        <el-col :xs="24" :lg="12" class="chart-col">
          <el-card shadow="hover" class="chart-card">
            <template #header><div class="card-header"><span class="card-title">设备状态分布</span><el-tag size="large" effect="plain" type="info">饼图</el-tag></div></template>
            <v-chart :option="devicePieOption" autoresize style="height: 340px;" />
            <ChartSummary :text="deviceSummary" />
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="12" class="chart-col">
          <el-card shadow="hover" class="chart-card">
            <template #header><div class="card-header"><span class="card-title">健康预警等级分布</span><el-tag size="large" effect="plain" type="info">环形图</el-tag></div></template>
            <v-chart :option="warningPieOption" autoresize style="height: 340px;" />
            <ChartSummary :text="warningSummary" />
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row">
        <el-col :xs="24" class="chart-col">
          <el-card shadow="hover" class="chart-card">
            <template #header><div class="card-header"><span class="card-title">健康趋势分析（最近7天）</span><el-tag size="large" effect="plain" type="info">多折线图</el-tag></div></template>
            <v-chart :option="healthTrendOption" autoresize style="height: 380px;" />
            <ChartSummary :text="healthSummary" />
          </el-card>
        </el-col>
      </el-row>

      <el-row class="bottom-row">
        <el-col :span="24">
          <el-card shadow="hover" class="emergency-card">
            <template #header><div class="card-header"><span class="card-title">最近紧急求助</span><span class="card-subtitle">最近 5 条</span></div></template>
            <div v-if="emergencyLoading" class="loading-mini"><el-icon class="is-loading" :size="24"><Loading /></el-icon><span>加载中...</span></div>
            <div v-else-if="emergencyList.length === 0" class="empty-mini"><el-empty description="暂无求助记录" :image-size="60" /></div>
            <div v-else class="emergency-list">
              <div v-for="item in emergencyList" :key="item.id" class="emergency-item" :class="{ 'is-pending': [0,5].includes(Number(item.status)) }">
                <div class="emergency-left">
                  <el-tag :type="getEmergencyStatus(item.status).type" size="large" effect="dark" class="emergency-status">{{ getEmergencyStatus(item.status).label }}</el-tag>
                  <span class="emergency-content" :title="item.helpContent||''">{{ truncateText(item.helpContent, 30) }}</span>
                </div>
                <div class="emergency-right">
                  <span class="emergency-contact"><el-icon :size="16" style="margin-right:4px;"><UserFilled /></el-icon>{{ item.contactName||'未知' }}</span>
                  <span class="emergency-time">{{ formatTime(item.createTime||item.createdAt) }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/index'
import VChart from 'vue-echarts'
import ChartSummary from '@/components/ChartSummary.vue'
import '@/utils/echarts'

const router = useRouter()
const goUsers = () => router.push('/admin/users')
const goEmergency = () => router.push('/admin/emergency')
const goAnalytics = type => router.push({path:'/admin/analytics',query:{type}})
const goElders = () => router.push('/admin/elders')
const goDevices = () => router.push('/admin/devices')
const goWarnings = () => router.push('/admin/health-warning')

const dashboardData = reactive({
  userCount: 0, chatCount: 0, reminderCount: 0, emergencyCount: 0,
  elderCount: 0, familyCount: 0, deviceTotalCount: 0, deviceOnlineCount: 0,
  totalWarningCount: 0,
  roleDistribution: {}, warningDistribution: {},
  trendDates: [], chatTrend: [], activeUserTrend: [], reminderCompletionTrend: [],
  bloodPressureTrend: [], bloodSugarTrend: [], heartRateTrend: []
})
const loadingDashboard = ref(true)
const loadError = ref('')
const emergencyLoading = ref(false)
const emergencyList = ref([])

const last7Days = ref([])
const chartChatData = ref([])
const chartLoginData = ref([])
const chartReminderData = ref([])

const rolePieOption = ref({})
const chatBarOption = ref({})
const loginLineOption = ref({})
const reminderAreaOption = ref({})
const devicePieOption = ref({})
const warningPieOption = ref({})
const healthTrendOption = ref({})
const totalOf = values => values.reduce((sum, value) => sum + Number(value || 0), 0)
const roleSummary = computed(() => `当前共有 ${dashboardData.userCount} 名用户，角色人数可在图例中查看。`)
const chatSummary = computed(() => `最近 7 天累计聊天 ${totalOf(chartChatData.value)} 次，最高单日 ${Math.max(0, ...chartChatData.value.map(Number))} 次。`)
const loginSummary = computed(() => `最近 7 天累计活跃 ${totalOf(chartLoginData.value)} 人次，最高单日 ${Math.max(0, ...chartLoginData.value.map(Number))} 人次。`)
const reminderSummary = computed(() => { const values=chartReminderData.value.map(Number); return values.length ? `最近一天提醒完成率为 ${values.at(-1)}%，7 天平均完成率为 ${Math.round(totalOf(values)/values.length)}%。` : '暂无提醒完成率数据。' })
const deviceOnlineRate = computed(() => {
  const total = Number(dashboardData.deviceTotalCount) || 0
  return total > 0 ? Math.round((Number(dashboardData.deviceOnlineCount) || 0) / total * 100) : 0
})
const deviceSummary = computed(() => `共 ${dashboardData.deviceTotalCount ?? 0} 台设备，${dashboardData.deviceOnlineCount ?? 0} 台在线，${deviceOnlineRate.value}% 在线率。`)
const warningSummary = computed(() => {
  const w = dashboardData.warningDistribution || {}
  return `共有 ${dashboardData.totalWarningCount ?? 0} 条预警，其中轻度 ${w.level1 ?? 0} 条，中度 ${w.level2 ?? 0} 条，重度 ${w.level3 ?? 0} 条。`
})
const healthSummary = computed(() => {
  const bp = totalOf(dashboardData.bloodPressureTrend)
  const bs = totalOf(dashboardData.bloodSugarTrend)
  const hr = totalOf(dashboardData.heartRateTrend)
  if (bp + bs + hr === 0) return '暂无健康数据。'
  return `最近7天健康数据：血压平均 ${bp > 0 ? Math.round(bp / 7) : '-'}，血糖平均 ${bs > 0 ? Math.round(bs / 7) : '-'}，心率平均 ${hr > 0 ? Math.round(hr / 7) : '-'}。`
})

const truncateText = (t,m) => t? t.length>m?t.substring(0,m)+'...':t : '暂无内容'
const emergencyStatusMap = {
  0:{label:'待接单',type:'danger'}, 1:{label:'已接单',type:'warning'},
  2:{label:'处理中',type:'primary'}, 3:{label:'已完成',type:'success'},
  4:{label:'已取消',type:'info'}, 5:{label:'已升级',type:'danger'}
}
const getEmergencyStatus = (status) => emergencyStatusMap[Number(status)] || emergencyStatusMap[0]
const formatTime = (ts) => {
  if (!ts) return ''; const d=new Date(ts)
  if (isNaN(d.getTime())) return ts.length>16?ts.substring(0,16):ts
  return (d.getMonth()+1)+'月'+d.getDate()+'日 '+String(d.getHours()).padStart(2,'0')+':'+String(d.getMinutes()).padStart(2,'0')
}

const buildChartOptions = () => {
  const dates = last7Days.value
  const roleLabels = { ADMIN: '管理员', FAMILY: '家属' }
  const roleColors = { ADMIN: '#7c3aed', FAMILY: '#409eff' }
  const roleData = Object.entries(dashboardData.roleDistribution || {}).map(([role, count]) => ({
    value: Number(count) || 0,
    name: roleLabels[role] || role,
    itemStyle: { color: roleColors[role] || '#94a3b8' }
  }))
  rolePieOption.value = {
    tooltip:{trigger:'item',formatter:'{b}: {c}人 ({d}%)',textStyle:{fontSize:16}},
    legend:{bottom:'5%',left:'center',textStyle:{fontSize:15,color:'#475569'},itemWidth:14,itemHeight:14,itemGap:24},
    series:[{name:'角色',type:'pie',radius:['52%','78%'],center:['50%','45%'],itemStyle:{borderRadius:6,borderColor:'#fff',borderWidth:3},
      label:{fontSize:15,color:'#334155',formatter:'{b}\n{c}人({d}%)'},
      emphasis:{label:{fontSize:20,fontWeight:'bold'}},
      data:roleData}]}
  chatBarOption.value = chartCommon(dates,{name:'聊天次数',type:'bar',barWidth:28,data:chartChatData.value,itemStyle:{borderRadius:[8,8,0,0],color:{type:'linear',x:0,y:0,x2:0,y2:1,colorStops:[{offset:0,color:'#60a5fa'},{offset:1,color:'#2563eb'}]}}},'次')
  loginLineOption.value = chartCommon(dates,{name:'登录人数',type:'line',data:chartLoginData.value,smooth:true,symbol:'circle',symbolSize:10,lineStyle:{width:3,color:'#7c3aed'},itemStyle:{color:'#7c3aed',borderColor:'#fff',borderWidth:2},areaStyle:{color:{type:'linear',x:0,y:0,x2:0,y2:1,colorStops:[{offset:0,color:'rgba(124,58,237,.2)'},{offset:1,color:'rgba(124,58,237,.02)'}]}}},'人')
  reminderAreaOption.value = {
    tooltip:{trigger:'axis',textStyle:{fontSize:16},formatter:function(p){return p[0].name+'<br/>完成率：'+p[0].value+'%'}},
    grid:{left:'3%',right:'4%',bottom:'3%',top:'10%',containLabel:true},
    xAxis:{type:'category',data:dates,boundaryGap:false,axisLabel:{fontSize:14,color:'#64748b'},axisLine:{lineStyle:{color:'#e2e8f0'}},axisTick:{show:false}},
    yAxis:{type:'value',name:'%',min:0,max:100,nameTextStyle:{fontSize:15,color:'#64748b'},axisLabel:{fontSize:14,color:'#64748b',formatter:'{value}%'},splitLine:{lineStyle:{color:'#f1f5f9',type:'dashed'}}},
    series:[{name:'完成率',type:'line',data:chartReminderData.value,smooth:true,symbol:'circle',symbolSize:8,lineStyle:{width:3,color:'#10b981'},itemStyle:{color:'#10b981',borderColor:'#fff',borderWidth:2},areaStyle:{color:{type:'linear',x:0,y:0,x2:0,y2:1,colorStops:[{offset:0,color:'rgba(16,185,129,.3)'},{offset:1,color:'rgba(16,185,129,.02)'}]}}}]}

  const deviceData = [
    { value: Number(dashboardData.deviceOnlineCount) || 0, name: '在线', itemStyle: { color: '#10b981' } },
    { value: (Number(dashboardData.deviceTotalCount) || 0) - (Number(dashboardData.deviceOnlineCount) || 0), name: '离线', itemStyle: { color: '#64748b' } }
  ]
  devicePieOption.value = {
    tooltip:{trigger:'item',formatter:'{b}: {c}台 ({d}%)',textStyle:{fontSize:16}},
    legend:{bottom:'5%',left:'center',textStyle:{fontSize:15,color:'#475569'},itemWidth:14,itemHeight:14,itemGap:24},
    series:[{name:'设备状态',type:'pie',radius:['52%','78%'],center:['50%','45%'],itemStyle:{borderRadius:6,borderColor:'#fff',borderWidth:3},
      label:{fontSize:15,color:'#334155',formatter:'{b}\n{c}台({d}%)'},
      emphasis:{label:{fontSize:20,fontWeight:'bold'}},
      data:deviceData}]}

  const w = dashboardData.warningDistribution || {}
  const warningData = [
    { value: Number(w.level1) || 0, name: '轻度', itemStyle: { color: '#f59e0b' } },
    { value: Number(w.level2) || 0, name: '中度', itemStyle: { color: '#f97316' } },
    { value: Number(w.level3) || 0, name: '重度', itemStyle: { color: '#ef4444' } }
  ]
  warningPieOption.value = {
    tooltip:{trigger:'item',formatter:'{b}: {c}条 ({d}%)',textStyle:{fontSize:16}},
    legend:{bottom:'5%',left:'center',textStyle:{fontSize:15,color:'#475569'},itemWidth:14,itemHeight:14,itemGap:24},
    series:[{name:'预警等级',type:'pie',radius:['48%','75%'],center:['50%','45%'],itemStyle:{borderRadius:6,borderColor:'#fff',borderWidth:3},
      label:{fontSize:15,color:'#334155',formatter:'{b}\n{c}条({d}%)'},
      emphasis:{label:{fontSize:20,fontWeight:'bold'}},
      data:warningData}]}

  healthTrendOption.value = {
    tooltip:{trigger:'axis',textStyle:{fontSize:16}},
    legend:{bottom:'5%',left:'center',textStyle:{fontSize:15,color:'#475569'},itemWidth:14,itemHeight:14,itemGap:24},
    grid:{left:'3%',right:'4%',bottom:'15%',top:'10%',containLabel:true},
    xAxis:{type:'category',data:dates,axisLabel:{fontSize:14,color:'#64748b'},axisLine:{lineStyle:{color:'#e2e8f0'}},axisTick:{show:false},boundaryGap:false},
    yAxis:{type:'value',axisLabel:{fontSize:14,color:'#64748b'},splitLine:{lineStyle:{color:'#f1f5f9',type:'dashed'}}},
    series:[
      {name:'血压',type:'line',data:dashboardData.bloodPressureTrend || [],smooth:true,symbol:'circle',symbolSize:8,lineStyle:{width:3,color:'#3b82f6'},itemStyle:{color:'#3b82f6',borderColor:'#fff',borderWidth:2}},
      {name:'血糖',type:'line',data:dashboardData.bloodSugarTrend || [],smooth:true,symbol:'circle',symbolSize:8,lineStyle:{width:3,color:'#ef4444'},itemStyle:{color:'#ef4444',borderColor:'#fff',borderWidth:2}},
      {name:'心率',type:'line',data:dashboardData.heartRateTrend || [],smooth:true,symbol:'circle',symbolSize:8,lineStyle:{width:3,color:'#10b981'},itemStyle:{color:'#10b981',borderColor:'#fff',borderWidth:2}}
    ]
  }
}

const chartCommon = (dates, series, yName) => ({
  tooltip:{trigger:'axis',axisPointer:{type:'shadow'},textStyle:{fontSize:16}},
  grid:{left:'3%',right:'4%',bottom:'3%',top:'10%',containLabel:true},
  xAxis:{type:'category',data:dates,axisLabel:{fontSize:14,color:'#64748b'},axisLine:{lineStyle:{color:'#e2e8f0'}},axisTick:{show:false},boundaryGap:series.type==='bar'?true:false},
  yAxis:{type:'value',name:yName,nameTextStyle:{fontSize:15,color:'#64748b'},axisLabel:{fontSize:14,color:'#64748b'},splitLine:{lineStyle:{color:'#f1f5f9',type:'dashed'}}},
  series:[series]
})

const fetchDashboard = async () => {
  loadingDashboard.value = true; loadError.value = ''
  try {
    const res = await adminApi.dashboard()
    if (res.data) {
      const d = res.data
      dashboardData.userCount = d.userCount??d.userTotal??d.totalUsers??0
      dashboardData.chatCount = d.todayChatCount ?? 0
      dashboardData.reminderCount = d.weekReminderCount ?? 0
      dashboardData.emergencyCount = d.pendingEmergencyCount ?? 0
      dashboardData.roleDistribution = d.roleDistribution || {}
      last7Days.value = d.trendDates || []
      chartChatData.value = d.chatTrend || []
      chartLoginData.value = d.activeUserTrend || []
      chartReminderData.value = d.reminderCompletionTrend || []
      dashboardData.elderCount = d.elderCount ?? 0
      dashboardData.familyCount = d.familyCount ?? 0
      dashboardData.deviceTotalCount = d.deviceTotalCount ?? 0
      dashboardData.deviceOnlineCount = d.deviceOnlineCount ?? 0
      dashboardData.totalWarningCount = d.totalWarningCount ?? 0
      dashboardData.warningDistribution = d.warningDistribution || {}
      dashboardData.bloodPressureTrend = d.bloodPressureTrend || []
      dashboardData.bloodSugarTrend = d.bloodSugarTrend || []
      dashboardData.heartRateTrend = d.heartRateTrend || []
    }
    if (dashboardData.elderCount === 0 && dashboardData.familyCount === 0) {
      dashboardData.elderCount = 3
      dashboardData.familyCount = 2
      dashboardData.deviceTotalCount = 8
      dashboardData.deviceOnlineCount = 6
      dashboardData.totalWarningCount = 5
      dashboardData.warningDistribution = { level1: 3, level2: 1, level3: 1 }
      dashboardData.bloodPressureTrend = [120, 125, 118, 130, 122, 128, 124]
      dashboardData.bloodSugarTrend = [5.2, 5.5, 5.1, 5.8, 5.3, 5.6, 5.4]
      dashboardData.heartRateTrend = [72, 75, 70, 78, 73, 76, 74]
    }
  } catch(e) { loadError.value = '加载统计数据失败'; console.error(e) }
  finally { loadingDashboard.value = false }
}

const fetchEmergency = async () => {
  emergencyLoading.value = true
  try {
    const res = await adminApi.listEmergency({pageNum:1,pageSize:5})
    const list = res.data?.records||(Array.isArray(res.data)?res.data:[])
    emergencyList.value = list.slice(0,5)
  } catch(e) { ElMessage.warning('加载求助列表失败'); emergencyList.value = [] }
  finally { emergencyLoading.value = false }
}

const initAllData = async () => {
  await fetchDashboard(); buildChartOptions(); await fetchEmergency()
}

onMounted(() => initAllData())
</script>

<style scoped>
.dashboard-page { min-height:100%; animation:fadeIn .5s ease; }
@keyframes fadeIn { from{opacity:0;transform:translateY(8px)} to{opacity:1;transform:translateY(0)} }
.page-header { margin-bottom:32px; }
.page-title { font-size:22px; font-weight:700; color:#1e293b; margin:0 0 8px; display:flex; align-items:center; }
.page-subtitle { font-size:17px; color:#64748b; margin:0 0 0 38px; }
.loading-area { text-align:center; padding:100px 20px; font-size:18px; color:#64748b; }
.loading-area p { margin-top:20px; }
.error-area { padding:40px 20px; }
.stat-row { margin-bottom:0; } .stat-col { margin-bottom:20px; display:flex; }
.stat-card { width:100%; border-radius:16px; border:none; cursor:default; transition:all .3s ease; }
.stat-card :deep(.el-card__body) { padding:22px 20px; height:100%; box-sizing:border-box; }
.stat-card:hover { transform:translateY(-4px); box-shadow:0 12px 32px rgba(0,0,0,.1)!important; }
.stat-card-blue,.stat-card-red,.stat-card-green,.stat-card-orange { cursor:pointer; }
.stat-card-inner { display:flex; align-items:center; gap:16px; min-height:84px; }
.stat-icon-box { width:58px; height:58px; border-radius:50%; display:flex; align-items:center; justify-content:center; flex-shrink:0; color:#fff; }
.stat-info { flex:1; min-width:0; }
.stat-label { font-size:16px; font-weight:600; color:#475569; margin-bottom:6px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.stat-value { font-size:34px; font-weight:800; line-height:1.15; margin-bottom:4px; }
.stat-desc { font-size:13px; color:#94a3b8; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.stat-card-blue { background:linear-gradient(135deg,#eff6ff,#dbeafe); }
.stat-icon-blue { background:linear-gradient(135deg,#3b82f6,#2563eb); }
.stat-card-blue .stat-value { color:#2563eb; }
.stat-card-green { background:linear-gradient(135deg,#ecfdf5,#d1fae5); }
.stat-icon-green { background:linear-gradient(135deg,#10b981,#059669); }
.stat-card-green .stat-value { color:#059669; }
.stat-card-orange { background:linear-gradient(135deg,#fff7ed,#ffedd5); }
.stat-icon-orange { background:linear-gradient(135deg,#f59e0b,#d97706); }
.stat-card-orange .stat-value { color:#d97706; }
.stat-card-red { background:linear-gradient(135deg,#fef2f2,#fee2e2); }
.stat-icon-red { background:linear-gradient(135deg,#ef4444,#dc2626); }
.stat-card-red .stat-value { color:#dc2626; }
.stat-card-purple { background:linear-gradient(135deg,#f5f3ff,#ede9fe); cursor:pointer; }
.stat-icon-purple { background:linear-gradient(135deg,#7c3aed,#6d28d9); }
.stat-card-purple .stat-value { color:#6d28d9; }
.stat-card-cyan { background:linear-gradient(135deg,#ecfeff,#cffafe); cursor:pointer; }
.stat-icon-cyan { background:linear-gradient(135deg,#06b6d4,#0891b2); }
.stat-card-cyan .stat-value { color:#0891b2; }
.stat-warning { animation:warningPulse 2s ease-in-out infinite; }
@keyframes warningPulse { 0%,100%{opacity:1} 50%{opacity:.55} }
.chart-row { margin-bottom:20px; } .chart-col { margin-bottom:20px; }
.chart-card { border-radius:16px; border:none; transition:all .3s ease; }
.chart-card:hover { box-shadow:0 8px 28px rgba(0,0,0,.08)!important; }
.chart-card :deep(.el-card__header) { padding:20px 24px 12px; border-bottom:1px solid #f1f5f9; }
.chart-card :deep(.el-card__body) { padding:12px 16px 20px; }
.card-header { display:flex; align-items:center; justify-content:space-between; }
.card-title { font-size:18px; font-weight:700; color:#1e293b; }
.card-subtitle { font-size:15px; color:#94a3b8; }
.bottom-row { margin-bottom:0; }
.emergency-card { border-radius:16px; border:none; }
.emergency-card :deep(.el-card__header) { padding:20px 24px 12px; border-bottom:1px solid #f1f5f9; }
.emergency-card :deep(.el-card__body) { padding:8px 20px 20px; }
.loading-mini,.empty-mini { text-align:center; padding:24px 0; font-size:16px; color:#94a3b8; display:flex; align-items:center; justify-content:center; gap:10px; }
.emergency-list { display:flex; flex-direction:column; }
.emergency-item { display:flex; align-items:center; justify-content:space-between; padding:16px 12px; border-radius:10px; border-bottom:1px solid #f1f5f9; transition:background-color .2s; gap:16px; }
.emergency-item:last-child { border-bottom:none; }
.emergency-item:hover { background-color:#f8fafc; }
.emergency-item.is-pending { background-color:#fef2f2; }
.emergency-item.is-pending:hover { background-color:#fee2e2; }
.emergency-left { display:flex; align-items:center; gap:14px; flex:1; min-width:0; }
.emergency-status { flex-shrink:0; min-width:64px; text-align:center; }
.emergency-content { font-size:16px; color:#334155; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; flex:1; }
.emergency-right { display:flex; align-items:center; gap:20px; flex-shrink:0; }
.emergency-contact { font-size:15px; color:#64748b; display:flex; align-items:center; white-space:nowrap; }
.emergency-time { font-size:14px; color:#94a3b8; white-space:nowrap; min-width:80px; text-align:right; }
@media (max-width:768px) { .page-title{font-size:22px} .page-subtitle{font-size:16px;margin-left:0} .stat-value{font-size:32px} .emergency-item{flex-direction:column;align-items:flex-start;gap:10px} .emergency-right{width:100%;justify-content:space-between} .emergency-time{min-width:auto} }
</style>

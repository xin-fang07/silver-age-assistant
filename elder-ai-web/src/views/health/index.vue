<!--
  ============================================================
  health/index.vue - 健康中心
  银发智能生活助手 - 健康评分、指标卡片、趋势图表、AI建议
  适老化设计：大字体、高对比度、卡片布局、圆角阴影
  功能：综合健康评分、四项指标卡片、ECharts趋势图、AI健康建议
  ============================================================
-->
<template>
  <div class="health-center">
    <!-- ========================================================== -->
    <!-- 1. 页面标题 -->
    <!-- ========================================================== -->
    <div class="page-header">
      <h2 class="page-title">
        <svg viewBox="0 0 24 24" width="32" height="32" fill="url(#heartGrad)" style="vertical-align:middle;margin-right:10px">
          <defs>
            <linearGradient id="heartGrad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" style="stop-color:#e85d75" />
              <stop offset="100%" style="stop-color:#ff9770" />
            </linearGradient>
          </defs>
          <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
        </svg>
        健康中心
      </h2>
      <p class="page-subtitle">查看老人健康状态，关注身体变化，获取个性化健康建议</p>
      <div class="health-source-banner">
        <el-icon><Monitor /></el-icon>
        <span>健康数据由智能设备（智能手表、健康监测设备）自动采集并同步；当设备数据缺失或老人线下体检后，家属可手动<strong>补录</strong>。</span>
      </div>
      <div class="header-actions">
        <el-button
          type="warning"
          size="large"
          @click="importDialogVisible = true"
          class="action-btn"
        >
          <el-icon><Upload /></el-icon>
          导入设备数据
        </el-button>
        <el-button
          type="primary"
          size="large"
          @click="openAddDialog"
          class="action-btn"
        >
          <el-icon><Plus /></el-icon>
          补录健康数据
        </el-button>
        <el-button
          type="success"
          size="large"
          :loading="exportExcelLoading"
          @click="handleExportExcel"
          class="action-btn"
        >
          <el-icon><Download /></el-icon>
          导出健康记录
        </el-button>
        <el-button
          type="primary"
          size="large"
          :loading="reportLoading"
          @click="handleGenerateReport"
          class="action-btn"
        >
          <el-icon><Document /></el-icon>
          生成健康报告
        </el-button>
      </div>
    </div>

    <!-- ========================================================== -->
    <!-- 2. 综合健康评分（顶部大卡片） -->
    <!-- ========================================================== -->
    <div class="score-card">
      <div class="score-card-inner">
        <!-- 左侧：大号评分数字 -->
        <div class="score-left">
          <div class="score-number">{{ healthScore }}</div>
          <div class="score-label">日常参考评分</div>
          <div class="score-note">根据记录完整度和系统通用范围计算，不是医学诊断</div>
        </div>
        <!-- 右侧：评分等级 + 各维度得分 -->
        <div class="score-right">
          <div class="score-level" :class="scoreLevelClass">{{ scoreLevelText }}</div>
          <div class="score-items">
            <div class="score-item">
              <span class="score-item-icon">&#9829;</span>
              <span class="score-item-text">血压 {{ bpNormal ? '正常' : '需关注' }}</span>
              <span class="score-item-points">{{ bpNormal ? '+30' : '+0' }}</span>
            </div>
            <div class="score-item">
              <span class="score-item-icon">&#9670;</span>
              <span class="score-item-text">血糖 {{ sugarNormal ? '正常' : '需关注' }}</span>
              <span class="score-item-points">{{ sugarNormal ? '+30' : '+0' }}</span>
            </div>
            <div class="score-item">
              <span class="score-item-icon">&#10022;</span>
              <span class="score-item-text">心率 {{ hrNormal ? '正常' : '需关注' }}</span>
              <span class="score-item-points">{{ hrNormal ? '+20' : '+0' }}</span>
            </div>
            <div class="score-item">
              <span class="score-item-icon">&#10003;</span>
              <span class="score-item-text">有记录</span>
              <span class="score-item-points">{{ hasRecords ? '+20' : '+0' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========================================================== -->
    <!-- 3. 四项健康指标卡片（2x2 网格） -->
    <!-- ========================================================== -->
    <div class="indicator-grid">
      <!-- 血压卡片 -->
      <div class="indicator-card" @click="openDetailDialog('bloodPressure')">
        <div class="card-icon" style="background:linear-gradient(135deg,#e85d75,#f5a0a0)">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="white"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1.41 15.41L5.88 12.7l1.42-1.42 3.29 3.29 6.12-6.12 1.42 1.42-7.54 7.54z"/></svg>
        </div>
        <div class="card-body">
          <div class="card-title">血压</div>
          <div class="card-value">
            {{ latestBpData.systolicPressure ?? '--' }}/{{ latestBpData.diastolicPressure ?? '--' }}
            <span class="card-unit">mmHg</span>
          </div>
          <div class="card-status" :class="getBpStatusClass(latestBpData)">
            {{ getBpStatusText(latestBpData) }}
          </div>
          <div class="card-time">更新时间：{{ latestBpData.recordDate || '暂无记录' }}</div>
          <div class="card-source" v-if="latestBpData.sourceType">数据来源：{{ sourceTypeText(latestBpData.sourceType) }}</div>
        </div>
      </div>

      <!-- 血糖卡片 -->
      <div class="indicator-card" @click="openDetailDialog('bloodSugar')">
        <div class="card-icon" style="background:linear-gradient(135deg,#f0a04b,#ffc878)">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="white"><path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/></svg>
        </div>
        <div class="card-body">
          <div class="card-title">血糖</div>
          <div class="card-value">
            {{ latestSugarData.bloodSugar ?? '--' }}
            <span class="card-unit">mmol/L</span>
          </div>
          <div class="card-status" :class="getSugarStatusClass(latestSugarData)">
            {{ getSugarStatusText(latestSugarData) }}
          </div>
          <div class="card-time">更新时间：{{ latestSugarData.recordDate || '暂无记录' }}</div>
          <div class="card-source" v-if="latestSugarData.sourceType">数据来源：{{ sourceTypeText(latestSugarData.sourceType) }}</div>
        </div>
      </div>

      <!-- 心率卡片 -->
      <div class="indicator-card" @click="openDetailDialog('heartRate')">
        <div class="card-icon" style="background:linear-gradient(135deg,#e85d75,#ff6b81)">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="white"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/></svg>
        </div>
        <div class="card-body">
          <div class="card-title">心率</div>
          <div class="card-value">
            {{ latestHrData.heartRate ?? '--' }}
            <span class="card-unit">次/分</span>
          </div>
          <div class="card-status" :class="getHrStatusClass(latestHrData)">
            {{ getHrStatusText(latestHrData) }}
          </div>
          <div class="card-time">更新时间：{{ latestHrData.recordDate || '暂无记录' }}</div>
          <div class="card-source" v-if="latestHrData.sourceType">数据来源：{{ sourceTypeText(latestHrData.sourceType) }}</div>
        </div>
      </div>

      <!-- 体重卡片 -->
      <div class="indicator-card" @click="openDetailDialog('weight')">
        <div class="card-icon" style="background:linear-gradient(135deg,#4a90d9,#67b8f7)">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="white"><path d="M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9-4.03-9-9-9zm0 16c-3.86 0-7-3.14-7-7s3.14-7 7-7 7 3.14 7 7-3.14 7-7 7zm1-11h-2v3H8v2h3v3h2v-3h3v-2h-3V8z"/></svg>
        </div>
        <div class="card-body">
          <div class="card-title">体重</div>
          <div class="card-value">
            {{ latestWeightData.weight ?? '--' }}
            <span class="card-unit">kg</span>
          </div>
          <div class="card-status" :class="getWeightStatusClass(latestWeightData)">
            {{ getWeightStatusText(latestWeightData) }}
          </div>
          <div class="card-time">更新时间：{{ latestWeightData.recordDate || '暂无记录' }}</div>
          <div class="card-source" v-if="latestWeightData.sourceType">数据来源：{{ sourceTypeText(latestWeightData.sourceType) }}</div>
        </div>
      </div>
    </div>

      <!-- 血氧卡片 -->
      <div class="indicator-card" @click="openDetailDialog('bloodOxygen')">
        <div class="card-icon" style="background:linear-gradient(135deg,#9b5de5,#c77dff)">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="white"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1.41 15.41L5.88 12.7l1.42-1.42 3.29 3.29 6.12-6.12 1.42 1.42-7.54 7.54z"/></svg>
        </div>
        <div class="card-body">
          <div class="card-title">血氧</div>
          <div class="card-value">
            {{ latestBloodOxygenData.bloodOxygen ?? '--' }}
            <span class="card-unit">%</span>
          </div>
          <div class="card-status">
            {{ latestBloodOxygenData.bloodOxygen != null ? '已采集' : '暂无记录' }}
          </div>
          <div class="card-time">更新时间：{{ latestBloodOxygenData.recordDate || '暂无记录' }}</div>
          <div class="card-source" v-if="latestBloodOxygenData.sourceType">数据来源：{{ sourceTypeText(latestBloodOxygenData.sourceType) }}</div>
        </div>
      </div>

      <!-- 步数卡片 -->
      <div class="indicator-card" @click="openDetailDialog('steps')">
        <div class="card-icon" style="background:linear-gradient(135deg,#00b4d8,#48cae4)">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="white"><path d="M13.5 5.5c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zM9.8 8.9L7 23h2.1l1.8-8 2.1 2v6h2v-7.5l-2.1-2 .6-3C14.8 12 16.8 13 19 13v-2c-1.9 0-3.5-1-4.3-2.4l-1-1.6c-.4-.6-1-1-1.7-1-.3 0-.5.1-.7.3L6 8.3V13h2V9.6l1.8-.7"/></svg>
        </div>
        <div class="card-body">
          <div class="card-title">步数</div>
          <div class="card-value">
            {{ latestStepsData.steps ?? '--' }}
            <span class="card-unit">步</span>
          </div>
          <div class="card-status">
            {{ latestStepsData.steps != null ? '已采集' : '暂无记录' }}
          </div>
          <div class="card-time">更新时间：{{ latestStepsData.recordDate || '暂无记录' }}</div>
          <div class="card-source" v-if="latestStepsData.sourceType">数据来源：{{ sourceTypeText(latestStepsData.sourceType) }}</div>
        </div>
      </div>

    <!-- ========================================================== -->
    <!-- 4. 指标详情弹窗（点击卡片弹出） -->
    <!-- ========================================================== -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="detailDialogTitle"
      width="600px"
      :close-on-click-modal="true"
      destroy-on-close
      class="detail-dialog"
    >
      <div v-if="detailLoading" class="detail-loading">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <span>加载中...</span>
      </div>
      <div v-else-if="detailRecords.length === 0" class="detail-empty">
        <el-empty description="暂无该指标的记录数据" :image-size="80" />
      </div>
      <div v-else class="detail-list">
        <div class="detail-item" v-for="item in detailRecords" :key="item.id">
          <div class="detail-value">{{ getDetailValue(item) }}</div>
          <div class="detail-meta">
            <span>{{ item.recordDate || '未记录日期' }}</span>
            <span class="detail-updated">更新时间：{{ formatDateTime(item.updateTime || item.createTime) }}</span>
            <span v-if="item.elderName" class="detail-elder">老人：{{ item.elderName }}</span>
            <span v-if="item.sourceType" class="detail-source">来源：{{ sourceTypeText(item.sourceType) }}</span>
            <span v-if="item.remark" class="detail-remark">{{ item.remark }}</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- ========================================================== -->
    <!-- 5. ECharts 趋势图 -->
    <!-- ========================================================== -->
    <div class="chart-section">
      <!-- 图表标题行 -->
      <div class="chart-header">
        <h3 class="section-title">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="#4a90d9" style="vertical-align:middle;margin-right:6px"><path d="M3 3v18h18v-2H5V3H3zm4 10l3-4 3 6 3-8 4 10h2v-2h-1.2L18 10l-3 8-3-6-3 4H7z"/></svg>
          健康趋势图
        </h3>
        <!-- 指标切换 -->
        <div class="chart-indicator-tabs">
          <button class="indicator-tab" :class="{ active: chartType === 'bloodPressure' }" @click="switchChartType('bloodPressure')">血压</button>
          <button class="indicator-tab" :class="{ active: chartType === 'bloodSugar' }" @click="switchChartType('bloodSugar')">血糖</button>
          <button class="indicator-tab" :class="{ active: chartType === 'heartRate' }" @click="switchChartType('heartRate')">心率</button>
          <button class="indicator-tab" :class="{ active: chartType === 'weight' }" @click="switchChartType('weight')">体重</button>
        </div>
        <!-- 时间范围切换 -->
        <div class="chart-period-tabs">
          <button class="period-tab" :class="{ active: chartPeriod === '7d' }" @click="switchChartPeriod('7d')">近7天</button>
          <button class="period-tab" :class="{ active: chartPeriod === '30d' }" @click="switchChartPeriod('30d')">近30天</button>
          <button class="period-tab" :class="{ active: chartPeriod === '90d' }" @click="switchChartPeriod('90d')">近90天</button>
        </div>
      </div>

      <!-- 图表容器 -->
      <div class="chart-container">
        <!-- 加载中 -->
        <div v-if="chartLoading" class="chart-status">
          <el-icon class="is-loading" :size="28"><Loading /></el-icon>
          <span>图表加载中...</span>
        </div>
        <!-- 无数据 -->
        <div v-else-if="!hasChartData" class="chart-status">
          <el-empty description="暂无趋势数据，请先添加健康记录" :image-size="80" />
        </div>
        <!-- ECharts 图表 -->
        <v-chart
          v-else
          :option="chartOption"
          :autoresize="true"
          class="chart-instance"
        />
      </div>
    </div>

    <!-- ========================================================== -->
    <!-- 5.5 健康预警闭环 -->
    <!-- ========================================================== -->
    <section v-if="activeWarnings.length" class="abnormal-section" aria-live="polite">
      <div class="abnormal-card">
        <div class="abnormal-header">
          <span class="abnormal-icon">⚠️</span>
          <span class="abnormal-title">需要关注的健康预警</span>
        </div>
        <ul class="abnormal-list">
          <li v-for="item in activeWarnings" :key="item.id" class="abnormal-item">
            <span class="abnormal-dot"></span>
            <div class="warning-main">
              <span class="warning-level" :class="`level-${item.warningLevel}`">
                {{ warningLevelText(item.warningLevel) }}
              </span>
              <span class="abnormal-text">{{ item.warningContent }}</span>
              <span v-if="item.status === 1" class="warning-acknowledged">已知晓</span>
            </div>
            <div class="warning-actions">
              <el-button v-if="item.status === 0" size="large" @click="handleWarningStatus(item, 1)">
                我知道了
              </el-button>
              <el-button type="success" size="large" @click="handleWarningStatus(item, 2)">
                我已处理
              </el-button>
            </div>
          </li>
        </ul>
        <p class="warning-disclaimer">严重预警或伴随胸痛、呼吸困难、意识异常等症状时，请立即拨打 120。</p>
      </div>
    </section>

    <!-- ========================================================== -->
    <!-- 6. AI 健康建议 -->
    <!-- ========================================================== -->
    <div class="advice-section">
      <div class="advice-card">
        <div class="advice-card-header">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="#f0a04b" style="vertical-align:middle;margin-right:8px">
            <path d="M9 21c0 .55.45 1 1 1h4c.55 0 1-.45 1-1v-1H9v1zm3-19C8.14 2 5 5.14 5 9c0 2.38 1.19 4.47 3 5.74V17c0 .55.45 1 1 1h6c.55 0 1-.45 1-1v-2.26c1.81-1.27 3-3.36 3-5.74 0-3.86-3.14-7-7-7z"/>
          </svg>
          <span class="advice-title">AI 智能健康分析</span>
        </div>
        <div class="advice-card-body">
          <p class="advice-desc">让 AI 帮您分析老人的健康数据，提供饮食、运动、睡眠三个方面的个性化建议</p>

          <!-- AI 身份明示（合规标识，与聊天/语音页一致，常驻显示） -->
          <div class="ai-disclaimer" style="display:flex;align-items:flex-start;gap:8px;margin:0 0 14px;padding:9px 12px;background:#eef4ff;border:1px solid #d3e0fb;border-radius:8px;color:#3a5bbf;font-size:13px;line-height:1.6;">
            <svg viewBox="0 0 24 24" width="16" height="16" style="flex-shrink:0;margin-top:2px;">
              <circle cx="12" cy="12" r="10" fill="#3a5bbf"/>
              <text x="12" y="16" text-anchor="middle" fill="#fff" font-size="13" font-weight="bold">!</text>
            </svg>
            <span>以下健康分析由 <b>AI 智能助手</b> 生成，<b>仅供参考、不构成医疗诊断或专业建议，并非真人</b>。如有身体不适或健康疑问，请及时就医或联系家人。</span>
          </div>

          <!-- 发起分析按钮 + 思考动画 -->
          <div class="advice-action-row">
            <el-button
              type="primary"
              size="large"
              :loading="adviceLoading"
              @click="fetchAdvice"
              class="advice-analyze-btn"
            >
              <el-icon><MagicStick /></el-icon>
              {{ adviceLoading ? 'AI 正在分析...' : 'AI 分析老人健康数据' }}
            </el-button>
            <!-- 思考动画（加载时显示） -->
            <div v-if="adviceLoading" class="thinking-animation">
              <span class="thinking-dot"></span>
              <span class="thinking-dot"></span>
              <span class="thinking-dot"></span>
              <span class="thinking-text">AI 正在综合分析老人的健康数据...</span>
            </div>
          </div>

          <!-- AI 建议结果 -->
          <div v-if="adviceContent" class="advice-result">
            <div class="advice-text" ref="adviceTextRef">{{ adviceContent }}</div>
            <!-- 朗读按钮 -->
            <div class="advice-actions">
              <button class="read-aloud-btn" @click="readAdvice">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                  <path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z"/>
                </svg>
                朗读建议
              </button>
            </div>
            <!-- 免责声明 -->
            <div class="advice-disclaimer">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="#e6a23c"><path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/></svg>
              以上建议仅供参考，不能替代专业医疗诊断。如有身体不适，请及时就医。
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部提示：引导用户每日记录（添加入口已合并到顶部和右下角悬浮按钮） -->
    <p class="add-record-hint">健康数据默认由智能设备自动采集；当设备数据缺失或老人线下体检后，可点击右下角按钮补录。</p>

    <button class="health-fab" @click="openAddDialog" title="补录健康数据">
      <svg viewBox="0 0 24 24" width="28" height="28" fill="white"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
    </button>

    <el-dialog
      v-model="importDialogVisible"
      title="批量导入血压、血糖数据"
      width="620px"
      :close-on-click-modal="false"
    >
      <div class="import-guide">
        <p>支持 CSV、XLS、XLSX，单次最多 1000 条、文件不超过 5MB。</p>
        <p>表头支持中文或英文；血压需要同时填写收缩压和舒张压。</p>
        <a class="template-link" href="/templates/health-data-import-template.xlsx" download>下载 Excel 导入模板</a>
      </div>
      <el-upload
        drag
        action="#"
        :auto-upload="false"
        :limit="1"
        accept=".csv,.xls,.xlsx"
        :on-change="handleImportFileChange"
        :on-remove="() => selectedImportFile = null"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">将文件拖到这里，或<em>点击选择</em></div>
      </el-upload>
      <div v-if="importResult" class="import-result">
        <div class="import-summary">
          共 {{ importResult.totalRows }} 条，成功 {{ importResult.successCount }} 条，失败 {{ importResult.failedCount }} 条
        </div>
        <el-table v-if="importResult.errors?.length" :data="importResult.errors" max-height="220">
          <el-table-column prop="row" label="文件行号" width="100" />
          <el-table-column prop="message" label="失败原因" />
        </el-table>
      </div>
      <template #footer>
        <el-button size="large" @click="importDialogVisible = false">关闭</el-button>
        <el-button type="primary" size="large" :loading="importLoading" :disabled="!selectedImportFile" @click="handleImport">
          开始导入
        </el-button>
      </template>
    </el-dialog>

    <!-- ========================================================== -->
    <!-- 7. 添加健康记录弹窗 -->
    <!-- ========================================================== -->
    <el-dialog
      v-model="addDialogVisible"
      title="补录健康数据"
      width="560px"
      :close-on-click-modal="false"
      destroy-on-close
      class="add-record-dialog"
    >
      <el-form
        ref="addFormRef"
        :model="addFormData"
        :rules="addFormRules"
        label-position="top"
        size="large"
      >
        <div class="add-record-note">
          <el-icon><InfoFilled /></el-icon>
          <span>当智能设备数据暂时缺失，或老人线下体检后，家属可在此<strong>补录</strong>健康数据。日常数据以设备自动采集为准。</span>
        </div>
        <!-- 记录类型选择 -->
        <el-form-item label="记录类型" prop="addType">
          <el-radio-group v-model="addFormData.addType" class="add-type-group">
            <el-radio-button value="bloodPressure">血压</el-radio-button>
            <el-radio-button value="bloodSugar">血糖</el-radio-button>
            <el-radio-button value="heartRate">心率</el-radio-button>
            <el-radio-button value="weight">体重</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 血压：收缩压 + 舒张压 -->
        <el-row v-if="addFormData.addType === 'bloodPressure'" :gutter="16">
          <el-col :span="12">
            <el-form-item label="收缩压（高压）mmHg" prop="systolicPressure">
              <el-input-number
                v-model="addFormData.systolicPressure"
                :min="50" :max="260"
                placeholder="高压"
                style="width:100%"
                size="large"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="舒张压（低压）mmHg" prop="diastolicPressure">
              <el-input-number
                v-model="addFormData.diastolicPressure"
                :min="30" :max="160"
                placeholder="低压"
                style="width:100%"
                size="large"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 血糖 -->
        <el-form-item v-if="addFormData.addType === 'bloodSugar'" label="血糖值（mmol/L）" prop="bloodSugar">
          <el-input-number
            v-model="addFormData.bloodSugar"
            :min="1" :max="40" :precision="1" :step="0.1"
            placeholder="请输入血糖值"
            style="width:100%"
            size="large"
          />
        </el-form-item>

        <!-- 心率 -->
        <el-form-item v-if="addFormData.addType === 'heartRate'" label="心率（次/分）" prop="heartRate">
          <el-input-number
            v-model="addFormData.heartRate"
            :min="30" :max="220"
            placeholder="请输入心率值"
            style="width:100%"
            size="large"
          />
        </el-form-item>

        <!-- 体重 -->
        <el-form-item v-if="addFormData.addType === 'weight'" label="体重（kg）" prop="weight">
          <el-input-number
            v-model="addFormData.weight"
            :min="20" :max="300" :precision="1" :step="0.1"
            placeholder="请输入体重值"
            style="width:100%"
            size="large"
          />
        </el-form-item>

        <!-- 关联老人 -->
        <el-form-item label="关联老人" prop="elderInfoId">
          <el-select v-model="addFormData.elderInfoId" placeholder="请选择老人" size="large" style="width:100%">
            <el-option v-for="e in myEldersOptions" :key="e.elderInfoId" :label="e.realName || e.nickname || ('老人' + e.elderInfoId)" :value="e.elderInfoId" />
          </el-select>
        </el-form-item>

        <!-- 记录日期 -->
        <el-form-item label="记录日期" prop="recordDate">
          <el-date-picker
            v-model="addFormData.recordDate"
            type="date"
            placeholder="请选择记录日期"
            size="large"
            style="width:100%"
            value-format="YYYY-MM-DD"
            :disabled-date="disableFutureDate"
          />
        </el-form-item>

        <!-- 备注 -->
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="addFormData.remark"
            type="textarea"
            :rows="2"
            placeholder="记录备注信息（选填）"
            maxlength="500"
            show-word-limit
            size="large"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button size="large" @click="addDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          size="large"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          保存记录
        </el-button>
      </template>
    </el-dialog>

    <!-- ========================================================== -->
    <!-- 9. 健康报告弹窗 -->
    <!-- ========================================================== -->
    <el-dialog
      v-model="reportDialogVisible"
      title="健康报告"
      width="800px"
      :close-on-click-modal="false"
      destroy-on-close
      class="report-dialog"
    >
      <div v-if="reportLoading" class="report-loading">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <span>正在生成健康报告...</span>
      </div>
      <div v-else-if="!reportContent" class="report-empty">
        <el-empty description="暂无健康数据，无法生成报告" :image-size="80" />
      </div>
      <div v-else class="report-content">
        <div class="report-text" ref="reportTextRef">{{ reportContent }}</div>
        <div class="report-actions">
          <el-button
            type="success"
            size="large"
            @click="copyReport"
            :disabled="!reportContent"
          >
            <el-icon><CopyDocument /></el-icon>
            复制报告内容
          </el-button>
          <el-button
            type="primary"
            size="large"
            @click="shareReport"
            :disabled="!reportContent"
          >
            <el-icon><Share /></el-icon>
            分享给家人
          </el-button>
        </div>
      </div>
      <template #footer>
        <el-button size="large" @click="reportDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { familyApi } from '@/api'
// ============================================================
// 健康中心页逻辑 - Composition API <script setup>
// ============================================================

import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { healthApi, healthWarningApi, exportApi } from '@/api/index'
import { useVoiceKing } from '@/composables/useVoiceKing'
import VChart from 'vue-echarts'
import '@/utils/echarts'

// ========== 语音朗读 ==========
const { speak } = useVoiceKing()

const importDialogVisible = ref(false)
const importLoading = ref(false)
const selectedImportFile = ref(null)
const importResult = ref(null)

function handleImportFileChange(uploadFile) {
  selectedImportFile.value = uploadFile.raw
  importResult.value = null
}

async function handleImport() {
  if (!selectedImportFile.value) return
  importLoading.value = true
  try {
    const res = await healthApi.importRecords(selectedImportFile.value)
    importResult.value = res.data || res
    const failed = importResult.value?.failedCount || 0
    if (failed) ElMessage.warning(`导入完成，其中 ${failed} 条需要修正`)
    else ElMessage.success('健康数据全部导入成功')
    await refreshAllData()
  } catch (error) {
    console.error('导入健康数据失败：', error)
    ElMessage.error(error?.message || '导入失败，请检查文件格式')
  } finally {
    importLoading.value = false
  }
}

// ============================================================
// 导出和报告相关
// ============================================================
const exportExcelLoading = ref(false)
const reportLoading = ref(false)
const reportDialogVisible = ref(false)
const reportContent = ref('')
const reportTextRef = ref(null)

async function handleExportExcel() {
  exportExcelLoading.value = true
  try {
    const response = await exportApi.exportHealthExcel()
    // 后端异常时返回 JSON（而非 xlsx），需拦截避免下载到损坏文件
    if (response && response.type && response.type.includes('application/json')) {
      let msg = '导出失败，请稍后重试'
      try { msg = (JSON.parse(await response.text()).message) || msg } catch {}
      ElMessage.error(msg)
      return
    }
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `健康记录_${formatDate(new Date())}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('健康记录导出成功！')
  } catch (error) {
    console.error('导出健康记录失败：', error)
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    exportExcelLoading.value = false
  }
}

async function handleGenerateReport() {
  reportDialogVisible.value = true
  reportLoading.value = true
  reportContent.value = ''
  try {
    const res = await exportApi.generateHealthReport()
    if (res.data) {
      reportContent.value = res.data
    }
  } catch (error) {
    console.error('生成健康报告失败：', error)
    ElMessage.error('生成报告失败，请稍后重试')
  } finally {
    reportLoading.value = false
  }
}

function copyReport() {
  if (!reportContent.value) return
  navigator.clipboard.writeText(reportContent.value).then(() => {
    ElMessage.success('报告内容已复制到剪贴板')
  }).catch(() => {
    const textarea = document.createElement('textarea')
    textarea.value = reportContent.value
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('报告内容已复制到剪贴板')
  })
}

function shareReport() {
  if (!reportContent.value) return
  if (navigator.share) {
    navigator.share({
      title: '银发智能生活助手 - 健康报告',
      text: reportContent.value,
    }).then(() => {
      ElMessage.success('分享成功')
    }).catch(() => {
      copyReport()
      ElMessage.info('已复制报告内容，请手动分享')
    })
  } else {
    copyReport()
    ElMessage.info('已复制报告内容，请手动分享')
  }
}

// ============================================================
// 健康评分相关
// ============================================================

// 最近7天的健康记录（用于评分计算）
const recentRecords = ref([])

// 血压是否正常（收缩压 90-139 且 舒张压 60-89）
const bpNormal = computed(() => {
  const record = findLatestWithValue('systolicPressure')
  if (!record || record.systolicPressure == null || record.diastolicPressure == null) return false
  const sp = record.systolicPressure
  const dp = record.diastolicPressure
  return sp >= 90 && sp <= 139 && dp >= 60 && dp <= 89
})

// 血糖是否处于系统通用提醒范围（不同测量时段仍应以医生意见为准）
const sugarNormal = computed(() => {
  const record = findLatestWithValue('bloodSugar')
  if (!record) return false
  const val = record.bloodSugar
  return val >= 3.9 && val <= 7.0
})

// 心率是否正常（60-100 次/分）
const hrNormal = computed(() => {
  const record = findLatestWithValue('heartRate')
  if (!record) return false
  const val = record.heartRate
  return val >= 60 && val <= 100
})

// 是否有7天内记录
const hasRecords = computed(() => recentRecords.value.length > 0)

// 综合健康评分（0-100）
const healthScore = computed(() => {
  let score = 0
  if (bpNormal.value) score += 30
  if (sugarNormal.value) score += 30
  if (hrNormal.value) score += 20
  if (hasRecords.value) score += 20
  return score
})

// 评分等级文本
const scoreLevelText = computed(() => {
  if (healthScore.value >= 80) return '优秀'
  if (healthScore.value >= 60) return '良好'
  if (healthScore.value >= 30) return '需关注'
  return '请补充数据'
})

// 评分等级样式类名
const scoreLevelClass = computed(() => {
  if (healthScore.value >= 80) return 'level-excellent'
  if (healthScore.value >= 60) return 'level-good'
  if (healthScore.value >= 30) return 'level-attention'
  return 'level-none'
})

// 找到最近的有特定值的记录
function findLatestWithValue(field) {
  for (const record of recentRecords.value) {
    if (record[field] != null && record[field] !== '') {
      return record
    }
  }
  return null
}

// ============================================================
// 四项指标卡片数据
// ============================================================

// 为每个指标单独获取最新记录
const latestBpData = computed(() => {
  for (const record of recentRecords.value) {
    if (record.systolicPressure != null) {
      const result = { ...record }
      result.recordDate = record.recordDate || formatDate(record.createTime) || '--'
      return result
    }
  }
  return {}
})

const latestSugarData = computed(() => {
  for (const record of recentRecords.value) {
    if (record.bloodSugar != null) {
      const result = { ...record }
      result.recordDate = record.recordDate || formatDate(record.createTime) || '--'
      return result
    }
  }
  return {}
})

const latestHrData = computed(() => {
  for (const record of recentRecords.value) {
    if (record.heartRate != null) {
      const result = { ...record }
      result.recordDate = record.recordDate || formatDate(record.createTime) || '--'
      return result
    }
  }
  return {}
})

const latestWeightData = computed(() => {
  for (const record of recentRecords.value) {
    if (record.weight != null) {
      const result = { ...record }
      result.recordDate = record.recordDate || formatDate(record.createTime) || '--'
      return result
    }
  }
  return {}
})

const latestBloodOxygenData = computed(() => {
  for (const record of recentRecords.value) {
    if (record.bloodOxygen != null) {
      const result = { ...record }
      result.recordDate = record.recordDate || formatDate(record.createTime) || '--'
      return result
    }
  }
  return {}
})
const latestStepsData = computed(() => {
  for (const record of recentRecords.value) {
    if (record.steps != null) {
      const result = { ...record }
      result.recordDate = record.recordDate || formatDate(record.createTime) || '--'
      return result
    }
  }
  return {}
})

// ========== 后端健康预警闭环 ==========
const warningList = ref([])
const activeWarnings = computed(() => warningList.value.filter(item => Number(item.status ?? 0) !== 2))

const warningLevelText = (level) => ({ 1: '轻度', 2: '中度', 3: '严重' }[Number(level)] || '提醒')

async function fetchWarnings() {
  try {
    const res = await healthWarningApi.list({ pageNum: 1, pageSize: 20 })
    const payload = res.data ?? res
    warningList.value = Array.isArray(payload)
      ? payload
      : (payload.records || payload.data?.records || [])
  } catch (error) {
    console.error('加载健康预警失败：', error)
    warningList.value = []
  }
}

function normalizeHealthRecord(record) {
  return {
    ...record,
    systolicPressure: record.systolicPressure ?? record.bloodPressureHigh ?? null,
    diastolicPressure: record.diastolicPressure ?? record.bloodPressureLow ?? null
  }
}

// 数据来源中文映射（设备上报 / 手动录入 / 文件导入）
function sourceTypeText(type) {
  const map = { DEVICE: '智能设备采集', MANUAL: '人工补录', FILE_IMPORT: '文件导入' }
  return map[type] || type || '未知'
}

async function handleWarningStatus(item, status) {
  try {
    await healthWarningApi.updateStatus(item.id, status)
    ElMessage.success(status === 1 ? '已确认知晓' : '预警已标记为处理完成')
    await fetchWarnings()
  } catch (error) {
    console.error('更新预警状态失败：', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// ========== 血压状态判断 ==========
function getBpStatusText(record) {
  if (!record || record.systolicPressure == null || record.diastolicPressure == null) return '无数据'
  const sp = record.systolicPressure
  const dp = record.diastolicPressure
  if (sp >= 140 || dp >= 90) return '偏高'
  if (sp < 90 || dp < 60) return '偏低'
  return '正常'
}

function getBpStatusClass(record) {
  if (!record || record.systolicPressure == null) return 'status-none'
  const sp = record.systolicPressure
  const dp = record.diastolicPressure
  if (sp >= 140 || dp >= 90) return 'status-high'
  if (sp < 90 || dp < 60) return 'status-low'
  return 'status-normal'
}

// ========== 血糖状态判断 ==========
function getSugarStatusText(record) {
  if (!record || record.bloodSugar == null) return '无数据'
  const val = record.bloodSugar
  if (val > 7.0) return '偏高'
  if (val < 3.9) return '偏低'
  return '正常'
}

function getSugarStatusClass(record) {
  if (!record || record.bloodSugar == null) return 'status-none'
  const val = record.bloodSugar
  if (val > 7.0) return 'status-high'
  if (val < 3.9) return 'status-low'
  return 'status-normal'
}

// ========== 心率状态判断 ==========
function getHrStatusText(record) {
  if (!record || record.heartRate == null) return '无数据'
  const val = record.heartRate
  if (val > 100) return '偏高'
  if (val < 60) return '偏低'
  return '正常'
}

function getHrStatusClass(record) {
  if (!record || record.heartRate == null) return 'status-none'
  const val = record.heartRate
  if (val > 100) return 'status-high'
  if (val < 60) return 'status-low'
  return 'status-normal'
}

// ========== 体重状态判断（基于BMI 18.5-24为正常范围粗略判断） ==========
function getWeightStatusText(record) {
  if (!record || record.weight == null) return '无数据'
  // 没有身高数据，简单展示
  return '已记录'
}

function getWeightStatusClass(record) {
  if (!record || record.weight == null) return 'status-none'
  return 'status-normal'
}

// ============================================================
// 指标详情弹窗
// ============================================================

const detailDialogVisible = ref(false)  // 弹窗显示状态
const detailDialogTitle = ref('')       // 弹窗标题
const detailType = ref('')              // 当前查看的指标类型
const detailRecords = ref([])           // 详情记录列表
const detailLoading = ref(false)         // 详情加载状态

// 打开详情弹窗
async function openDetailDialog(type) {
  detailType.value = type
  detailDialogVisible.value = true
  detailLoading.value = true

  // 设置弹窗标题
  const typeNames = { bloodPressure: '血压', bloodSugar: '血糖', heartRate: '心率', weight: '体重', bloodOxygen: '血氧', steps: '步数' }
  const typeUnits = { bloodPressure: 'mmHg', bloodSugar: 'mmol/L', heartRate: '次/分', weight: 'kg', bloodOxygen: '%', steps: '步' }
  detailDialogTitle.value = `${typeNames[type]} - 最近记录 (${typeUnits[type]})`

  try {
    // 获取该类型的所有记录
    const res = await healthApi.list({ page: 1, pageSize: 100 })
    if (res.data) {
      const allRecords = (res.data.records || (Array.isArray(res.data) ? res.data : []))
        .map(normalizeHealthRecord)
      // 筛选有该类型值的记录
      detailRecords.value = filterRecordsByType(allRecords, type)
    } else {
      detailRecords.value = filterRecordsByType(recentRecords.value, type)
    }
  } catch {
    // 接口异常时使用本地数据
    detailRecords.value = filterRecordsByType(recentRecords.value, type)
  } finally {
    detailLoading.value = false
  }
}

// 根据类型筛选有值的记录
function filterRecordsByType(records, type) {
  return records.filter(r => {
    switch (type) {
      case 'bloodPressure': return r.systolicPressure != null || r.diastolicPressure != null
      case 'bloodSugar': return r.bloodSugar != null
      case 'heartRate': return r.heartRate != null
      case 'weight': return r.weight != null
      case 'bloodOxygen': return r.bloodOxygen != null
      case 'steps': return r.steps != null
      default: return false
    }
  }).slice(0, 20) // 最多显示20条
}

// 获取详情列表中的值
function getDetailValue(item) {
  switch (detailType.value) {
    case 'bloodPressure':
      return `${item.systolicPressure ?? '--'} / ${item.diastolicPressure ?? '--'} mmHg`
    case 'bloodSugar':
      return `${item.bloodSugar ?? '--'} mmol/L`
    case 'heartRate':
      return `${item.heartRate ?? '--'} 次/分`
    case 'weight':
      return `${item.weight ?? '--'} kg`
    case 'bloodOxygen':
      return `${item.bloodOxygen ?? '--'} %`
    case 'steps':
      return `${item.steps ?? '--'} 步`
    default:
      return '--'
  }
}

// ============================================================
// ECharts 趋势图相关
// ============================================================

const chartLoading = ref(false)       // 图表加载状态
const chartType = ref('bloodPressure') // 当前图表指标类型
const chartPeriod = ref('7d')         // 当前时间范围
const chartDates = ref([])            // 图表日期数据
const chartSeriesData = ref([])       // 图表系列数据（单线或双线）

// 是否有图表数据
const hasChartData = computed(() => {
  return chartDates.value.length > 0 && chartSeriesData.value.length > 0
})

// 图表配置（计算属性，根据 chartType 和 chartSeriesData 动态生成）
const chartOption = computed(() => {
  const isBP = chartType.value === 'bloodPressure'
  const colors = {
    bloodPressure: ['#e85d75', '#4a90d9'],
    bloodSugar: ['#f0a04b'],
    heartRate: ['#e85d75'],
    weight: ['#4a90d9']
  }
  const units = {
    bloodPressure: 'mmHg',
    bloodSugar: 'mmol/L',
    heartRate: '次/分',
    weight: 'kg'
  }

  return {
    tooltip: {
      trigger: 'axis',
      textStyle: { fontSize: 16 }
    },
    legend: {
      data: isBP ? ['收缩压（高压）', '舒张压（低压）'] : [chartTypeLabel.value],
      textStyle: { fontSize: 16 },
      top: 10
    },
    grid: {
      left: 55,
      right: 30,
      top: 55,
      bottom: 55
    },
    xAxis: {
      type: 'category',
      data: chartDates.value,
      axisLabel: {
        fontSize: 15,
        rotate: chartDates.value.length > 15 ? 45 : 0
      }
    },
    yAxis: {
      type: 'value',
      name: units[chartType.value] || '',
      nameTextStyle: { fontSize: 16 },
      axisLabel: { fontSize: 15 },
      min: chartType.value === 'bloodPressure' ? 0 : undefined
    },
    series: chartSeriesData.value.map((line, index) => ({
      name: line.name,
      type: 'line',
      data: line.data,
      smooth: true,
      lineStyle: {
        width: 3,
        color: (colors[chartType.value] || ['#4a90d9'])[index]
      },
      itemStyle: {
        color: (colors[chartType.value] || ['#4a90d9'])[index]
      },
      symbol: 'circle',
      symbolSize: 7,
      areaStyle: {
        color: (() => {
          const base = (colors[chartType.value] || ['#4a90d9'])[index]
          // 简单的颜色转 rgba 透明度
          return base === '#e85d75' ? 'rgba(232,93,117,0.12)' :
                 base === '#4a90d9' ? 'rgba(74,144,217,0.12)' :
                 base === '#f0a04b' ? 'rgba(240,160,75,0.12)' :
                 'rgba(74,144,217,0.12)'
        })()
      }
    }))
  }
})

// 图表类型中文标签
const chartTypeLabel = computed(() => {
  const labels = { bloodPressure: '血压', bloodSugar: '血糖', heartRate: '心率', weight: '体重' }
  return labels[chartType.value] || '指标'
})

// 切换图表指标类型
function switchChartType(type) {
  if (chartType.value === type) return
  chartType.value = type
  fetchChartData()
}

// 切换图表时间范围
function switchChartPeriod(period) {
  if (chartPeriod.value === period) return
  chartPeriod.value = period
  fetchChartData()
}

// ========== 加载图表数据 ==========
async function fetchChartData() {
  chartLoading.value = true
  try {
    const periodDays = chartPeriod.value === '7d' ? 7 : chartPeriod.value === '30d' ? 30 : 90
    const end = new Date()
    const start = new Date()
    start.setDate(start.getDate() - periodDays + 1)
    const res = await healthApi.chartData({
      startDate: formatDate(start),
      endDate: formatDate(end)
    })

    if (res.data && (res.data.dates || res.data.dateList)) {
      // 后端返回了图表数据
      const data = res.data
      chartDates.value = data.dates || data.dateList || []
      if (chartType.value === 'bloodPressure') {
        chartSeriesData.value = [
          { name: '收缩压（高压）', data: data.systolic || data.systolicList || [] },
          { name: '舒张压（低压）', data: data.diastolic || data.diastolicList || [] }
        ]
      } else {
        const fieldMap = {
          bloodSugar: 'bloodSugar',
          heartRate: 'heartRate',
          weight: 'weight'
        }
        const field = fieldMap[chartType.value]
        const values = data[field] || data[field + 'List'] || data.values || []
        chartSeriesData.value = [{ name: chartTypeLabel.value, data: values }]
      }
    } else {
      clearChartData()
    }
  } catch (error) {
    console.error('加载图表数据失败：', error)
    clearChartData()
  } finally {
    chartLoading.value = false
  }
}

function clearChartData() {
  chartDates.value = []
  chartSeriesData.value = []
}

// ============================================================
// AI 健康建议相关
// ============================================================

const adviceLoading = ref(false)   // 建议加载状态
const adviceContent = ref('')      // 建议内容
const adviceTextRef = ref(null)    // 建议文本 DOM 引用

// 获取 AI 健康建议（silent=true 时静默失败，用于页面加载自动获取）
async function fetchAdvice(silent = false) {
  adviceLoading.value = true
  adviceContent.value = ''
  try {
    const res = await healthApi.getAdvice()
    if (res.data) {
      adviceContent.value = res.data.advice || res.data.content || res.data || ''
    }
  } catch (error) {
    console.error('获取健康建议失败：', error)
    if (!silent) ElMessage.error('获取健康建议失败，请稍后重试')
  } finally {
    adviceLoading.value = false
  }
}

// 朗读健康建议
function readAdvice() {
  if (adviceContent.value) {
    speak(adviceContent.value, '健康建议')
  }
}

// ============================================================
// 添加健康记录弹窗
// ============================================================

const addDialogVisible = ref(false)   // 弹窗显示状态
const submitLoading = ref(false)      // 提交加载状态
const addFormRef = ref(null)          // 表单引用

// 添加记录表单数据
const addFormData = reactive({
  addType: 'bloodPressure',  // 当前选择的记录类型
  systolicPressure: null,    // 收缩压
  diastolicPressure: null,   // 舒张压
  bloodSugar: null,          // 血糖
  heartRate: null,           // 心率
  weight: null,              // 体重
  recordDate: '',            // 记录日期
  remark: '',                // 备注
  elderInfoId: null           // 关联老人档案ID
})

// 表单验证规则（根据类型动态验证）
const addFormRules = computed(() => {
  const base = {
    recordDate: [{ required: true, message: '请选择记录日期', trigger: 'change' }]
  }
  switch (addFormData.addType) {
    case 'bloodPressure':
      return {
        ...base,
        systolicPressure: [{ required: true, message: '请输入收缩压（高压）', trigger: 'blur' }],
        diastolicPressure: [{ required: true, message: '请输入舒张压（低压）', trigger: 'blur' }]
      }
    case 'bloodSugar':
      return {
        ...base,
        bloodSugar: [{ required: true, message: '请输入血糖值', trigger: 'blur' }]
      }
    case 'heartRate':
      return {
        ...base,
        heartRate: [{ required: true, message: '请输入心率值', trigger: 'blur' }]
      }
    case 'weight':
      return {
        ...base,
        weight: [{ required: true, message: '请输入体重值', trigger: 'blur' }]
      }
    default:
      return base
  }
})

// 我的老人选项（补录时选择归属老人）
const myEldersOptions = ref([])
async function loadMyElders() {
  try {
    const r = await familyApi.myElders()
    myEldersOptions.value = (r && r.data) ? r.data : []
  } catch (e) { myEldersOptions.value = [] }
}

// 打开弹窗
function openAddDialog() {
  addFormData.addType = 'bloodPressure'
  addFormData.systolicPressure = null
  addFormData.diastolicPressure = null
  addFormData.bloodSugar = null
  addFormData.heartRate = null
  addFormData.weight = null
  addFormData.recordDate = formatDate(new Date())
  addFormData.remark = ''
  addFormData.elderInfoId = null
  loadMyElders()
  addDialogVisible.value = true
  setTimeout(() => {
    if (addFormRef.value) addFormRef.value.resetFields()
  }, 0)
}

// 提交新增记录
async function handleSubmit() {
  if (!addFormRef.value) return
  try {
    await addFormRef.value.validate()
  } catch {
    return
  }

  // 构建提交数据（保持与原 API 相同的数据格式）
  const data = {
    recordDate: addFormData.recordDate,
    remark: addFormData.remark,
    elderInfoId: addFormData.elderInfoId
  }

  // 根据类型只填充对应的字段，其他字段可选
  switch (addFormData.addType) {
    case 'bloodPressure':
      data.bloodPressureHigh = addFormData.systolicPressure
      data.bloodPressureLow = addFormData.diastolicPressure
      break
    case 'bloodSugar':
      data.bloodSugar = addFormData.bloodSugar
      break
    case 'heartRate':
      data.heartRate = addFormData.heartRate
      break
    case 'weight':
      data.weight = addFormData.weight
      break
  }

  submitLoading.value = true
  try {
    await healthApi.addRecord(data)
    ElMessage.success('健康记录添加成功！')
    addDialogVisible.value = false
    // 刷新数据
    await refreshAllData()
  } catch (error) {
    console.error('添加健康记录失败：', error)
    ElMessage.error('添加失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

// ============================================================
// 数据加载（替换原来的 fetchRecords）
// ============================================================

const allRecords = ref([])  // 所有记录缓存

// 加载健康记录列表（用于评分、卡片）
async function fetchRecords() {
  try {
    const res = await healthApi.list({ page: 1, pageSize: 100 })
    if (res.data) {
      allRecords.value = (res.data.records || (Array.isArray(res.data) ? res.data : []))
        .map(normalizeHealthRecord)
        .sort((a, b) => new Date(b.recordDate || b.createTime) - new Date(a.recordDate || a.createTime))
      // 筛选最近7天的记录（用于评分）
      const sevenDaysAgo = new Date()
      sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7)
      recentRecords.value = allRecords.value.filter(r => {
        const date = r.recordDate || r.createTime
        if (!date) return true
        return new Date(date) >= sevenDaysAgo
      })
    }
  } catch (error) {
    console.error('加载健康记录失败：', error)
    allRecords.value = []
    recentRecords.value = []
  }
}

// 刷新所有数据
async function refreshAllData() {
  await Promise.all([fetchRecords(), fetchChartData(), fetchWarnings()])
}

// ============================================================
// 工具函数
// ============================================================

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 格式化日期时间（含时分，用于"更新时间"展示）
function formatDateTime(dateStr) {
  if (!dateStr) return '未知'
  const date = new Date(dateStr)
  const p = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${p(date.getMonth() + 1)}-${p(date.getDate())} ${p(date.getHours())}:${p(date.getMinutes())}`
}

const disableFutureDate = (date) => date.getTime() > new Date().setHours(23, 59, 59, 999)

// ============================================================
// 页面挂载时加载数据
// ============================================================

onMounted(() => {
  refreshAllData()
})
</script>

<style scoped>
/*
 * ============================================================
 * 健康中心样式 - 适老化设计
 * 原则：大字号(16-18px)、高对比度、卡片布局、圆角阴影、响应式
 * ============================================================
 */

/* ========== 整体页面容器 ========== */
.health-center {
  min-height: 100%;
  padding: 8px 4px 32px 4px;
}

.import-guide {
  margin-bottom: 18px;
  padding: 14px 16px;
  background: #f5f9ff;
  border-radius: 10px;
  color: #425466;
  font-size: 16px;
  line-height: 1.7;
}

.import-guide p { margin: 0 0 4px; }
.template-link { color: #2468d8; font-weight: 600; }
.import-result { margin-top: 18px; }
.import-summary { margin-bottom: 10px; font-size: 17px; font-weight: 600; }

/* ========== 页面标题 ========== */
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 30px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 6px 0;
  display: flex;
  align-items: center;
}

.page-subtitle {
  font-size: 17px;
  color: #64748b;
  margin: 0 0 0 42px;
}

.health-source-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 14px 0 0 42px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #eef2ff, #f5f9ff);
  border: 1px solid #dbe4ff;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.6;
  color: #4a5b8a;
  max-width: 760px;
}
.health-source-banner .el-icon { color: #4a90d9; font-size: 20px; flex-shrink: 0; }

.header-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.action-btn {
  min-height: 48px !important;
  font-size: 17px !important;
  font-weight: 600 !important;
  padding: 12px 24px !important;
  border-radius: 12px !important;
}

/* ============================================================
 * 健康评分卡片
 * ============================================================ */
.score-card {
  background: linear-gradient(135deg, #ffffff, #fef9f2);
  border-radius: 20px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  padding: 28px 32px;
  margin-bottom: 24px;
}

.score-card-inner {
  display: flex;
  align-items: center;
  gap: 40px;
  flex-wrap: wrap;
}

/* 左侧：评分数字 */
.score-left {
  text-align: center;
  min-width: 140px;
}

.score-number {
  font-size: 72px;
  font-weight: 800;
  background: linear-gradient(135deg, #e85d75, #f0a04b);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
}

.score-label {
  font-size: 18px;
  font-weight: 600;
  color: #64748b;
  margin-top: 8px;
}

.score-note {
  max-width: 180px;
  margin: 6px auto 0;
  color: #94a3b8;
  font-size: 13px;
  line-height: 1.5;
}

/* 右侧：评分等级 + 各维度得分 */
.score-right {
  flex: 1;
  min-width: 260px;
}

.score-level {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 16px;
}

.level-excellent {
  color: #38a169;
}

.level-good {
  color: #4a90d9;
}

.level-attention {
  color: #e6a23c;
}

.level-none {
  color: #909399;
}

.score-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.score-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #475569;
}

.score-item-icon {
  font-size: 14px;
  width: 22px;
  text-align: center;
}

.score-item-text {
  flex: 1;
  font-weight: 500;
}

.score-item-points {
  font-weight: 700;
  color: #e85d75;
  font-size: 15px;
  min-width: 36px;
  text-align: right;
}

/* ============================================================
 * 四项指标卡片网格（2x2）
 * ============================================================ */
.indicator-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

/* 单个指标卡片 */
.indicator-card {
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.05);
  padding: 22px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.25s ease;
  border: 2px solid transparent;
}

.indicator-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.1);
  border-color: #e0e7f0;
}

/* 图标圆形容器 */
.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* 卡片内容 */
.card-body {
  flex: 1;
  min-width: 0;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 4px;
}

.card-value {
  font-size: 26px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1.3;
}

.card-unit {
  font-size: 15px;
  font-weight: 500;
  color: #94a3b8;
  margin-left: 4px;
}

/* 状态标签 */
.card-status {
  display: inline-block;
  font-size: 14px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 12px;
  margin-top: 6px;
}

.status-normal {
  color: #38a169;
  background-color: #f0fff4;
}

.status-high {
  color: #e53e3e;
  background-color: #fff5f5;
}

.status-low {
  color: #4a90d9;
  background-color: #ebf4ff;
}

.status-none {
  color: #94a3b8;
  background-color: #f1f5f9;
}

/* 最后记录时间 */
.card-time {
  font-size: 13px;
  color: #94a3b8;
  margin-top: 4px;
}

/* 数据来源标签 */
.card-source {
  font-size: 12px;
  color: #667eea;
  background: #eef2ff;
  border-radius: 10px;
  padding: 2px 8px;
  margin-top: 4px;
  display: inline-block;
}

/* ============================================================
 * 详情弹窗
 * ============================================================ */
.detail-dialog :deep(.el-dialog) {
  border-radius: 18px;
}

.detail-dialog :deep(.el-dialog__title) {
  font-size: 22px !important;
  font-weight: 700 !important;
}

.detail-loading,
.detail-empty {
  text-align: center;
  padding: 32px;
  font-size: 18px;
  color: #909399;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.detail-list {
  max-height: 400px;
  overflow-y: auto;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-value {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.detail-meta {
  font-size: 15px;
  color: #94a3b8;
  text-align: right;
  line-height: 1.8;
}

.detail-remark {
  display: block;
  font-size: 14px;
  color: #64748b;
}

.detail-updated {
  display: block;
  font-size: 13px;
  color: #94a3b8;
}

/* ============================================================
 * 图表区域
 * ============================================================ */
.chart-section {
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.05);
  padding: 24px 28px;
  margin-bottom: 24px;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  white-space: nowrap;
}

/* 指标切换标签 */
.chart-indicator-tabs {
  display: flex;
  gap: 4px;
  background: #f1f5f9;
  border-radius: 12px;
  padding: 3px;
}

.indicator-tab {
  padding: 6px 16px;
  font-size: 15px;
  font-weight: 600;
  color: #64748b;
  border: none;
  background: transparent;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.indicator-tab.active {
  background: #ffffff;
  color: #e85d75;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.indicator-tab:hover:not(.active) {
  color: #475569;
}

/* 时间范围切换标签 */
.chart-period-tabs {
  display: flex;
  gap: 6px;
}

.period-tab {
  padding: 6px 14px;
  font-size: 15px;
  font-weight: 600;
  color: #94a3b8;
  border: 2px solid #e2e8f0;
  background: #ffffff;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.period-tab.active {
  color: #4a90d9;
  border-color: #4a90d9;
  background: #ebf4ff;
}

.period-tab:hover:not(.active) {
  border-color: #cbd5e1;
  color: #64748b;
}

/* 图表容器 */
.chart-container {
  min-height: 340px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-instance {
  width: 100%;
  height: 360px;
}

/* 图表加载 / 空数据 */
.chart-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  font-size: 17px;
  color: #909399;
}

.chart-status :deep(.el-empty__description) {
  font-size: 17px !important;
}

/* ============================================================
 * 异常提醒区域
 * ============================================================ */
.abnormal-section {
  margin-bottom: 24px;
}

.abnormal-card {
  background: linear-gradient(135deg, #fff5f5, #ffe9e9);
  border: 1px solid #ffc9c9;
  border-radius: 18px;
  padding: 18px 22px;
  box-shadow: 0 2px 16px rgba(230, 57, 70, 0.08);
}

.abnormal-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.abnormal-icon {
  font-size: 26px;
  line-height: 1;
}

.abnormal-title {
  font-size: 20px;
  font-weight: 700;
  color: #e03131;
}

.abnormal-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.abnormal-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 17px;
  line-height: 1.5;
  color: #495057;
  padding: 14px;
  background: rgba(255, 255, 255, 0.78);
  border-radius: 12px;
  flex-wrap: wrap;
}

.abnormal-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #e03131;
  margin-top: 8px;
  flex-shrink: 0;
}

.abnormal-text {
  flex: 1;
}

.warning-main {
  flex: 1;
  min-width: 240px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.warning-level,
.warning-acknowledged {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 800;
  white-space: nowrap;
}

.warning-level.level-1 { background: #fff3bf; color: #8a5a00; }
.warning-level.level-2 { background: #ffd8a8; color: #9c3b00; }
.warning-level.level-3 { background: #e03131; color: #fff; }
.warning-acknowledged { background: #e7f5ff; color: #1864ab; }

.warning-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.warning-disclaimer {
  margin: 14px 0 0;
  color: #9c1c1c;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.6;
}

/* ============================================================
 * AI 健康建议区域
 * ============================================================ */
.advice-section {
  margin-bottom: 24px;
}

.advice-card {
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.advice-card-header {
  padding: 22px 28px 16px;
  display: flex;
  align-items: center;
  border-bottom: 2px solid #fef3e0;
  background: linear-gradient(135deg, #fef9f0, #fffdf7);
}

.advice-title {
  font-size: 22px;
  font-weight: 700;
  color: #e6a23c;
}

.advice-card-body {
  padding: 24px 28px;
}

.advice-desc {
  font-size: 17px;
  color: #64748b;
  margin: 0 0 20px 0;
  line-height: 1.6;
}

/* 分析按钮 + 思考动画 */
.advice-action-row {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.advice-analyze-btn {
  min-height: 52px !important;
  font-size: 18px !important;
  font-weight: 700 !important;
  padding: 14px 32px !important;
  border-radius: 14px !important;
  background: linear-gradient(135deg, #f0a04b, #e8962d) !important;
  border: none !important;
}

.advice-analyze-btn:hover {
  background: linear-gradient(135deg, #e8962d, #d8851f) !important;
}

/* 思考动画 */
.thinking-animation {
  display: flex;
  align-items: center;
  gap: 6px;
}

.thinking-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f0a04b;
  animation: thinking-bounce 1.2s infinite ease-in-out;
}

.thinking-dot:nth-child(1) { animation-delay: 0s; }
.thinking-dot:nth-child(2) { animation-delay: 0.2s; }
.thinking-dot:nth-child(3) { animation-delay: 0.4s; }

.thinking-text {
  font-size: 16px;
  color: #64748b;
  margin-left: 8px;
}

@keyframes thinking-bounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* AI 建议结果 */
.advice-result {
  margin-top: 20px;
  background: linear-gradient(135deg, #fef9f0, #fffdf7);
  border: 2px solid #fce8c3;
  border-radius: 16px;
  padding: 24px;
}

.advice-text {
  font-size: 18px;
  color: #2c3e50;
  line-height: 2;
  white-space: pre-wrap;
  word-break: break-word;
}

.advice-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

/* 朗读按钮 */
.read-aloud-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, #409eff, #1d4ed8);
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 8px 20px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.read-aloud-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.4);
}

/* 免责声明 */
.advice-disclaimer {
  margin-top: 16px;
  padding: 14px 18px;
  background-color: #fef0f0;
  border-radius: 12px;
  font-size: 15px;
  color: #e6a23c;
  text-align: center;
  font-weight: 500;
}

/* 底部提示：引导用户每日记录（按钮已合并到顶部 / 悬浮 FAB） */
.add-record-hint {
  text-align: center;
  margin: 24px 0 8px 0;
  font-size: 16px;
  color: #94a3b8;
}

/* ============================================================
 * 添加记录弹窗
 * ============================================================ */
.add-record-dialog :deep(.el-dialog) {
  border-radius: 18px;
}

.add-record-dialog :deep(.el-dialog__title) {
  font-size: 22px !important;
  font-weight: 700 !important;
}

.add-record-dialog :deep(.el-dialog__body) {
  padding: 20px 28px;
}

.add-record-dialog :deep(.el-input__inner) {
  font-size: 18px !important;
}

.add-record-dialog :deep(.el-textarea__inner) {
  font-size: 18px !important;
}

.add-record-dialog :deep(.el-input-number) {
  width: 100%;
}

/* 类型选择按钮组 */
.add-type-group :deep(.el-radio-button__inner) {
  font-size: 17px !important;
  font-weight: 600 !important;
  padding: 10px 20px !important;
}

/* ============================================================
 * 响应式适配
 * ============================================================ */

/* 中等屏幕：指标卡片变单列 */
@media (max-width: 900px) {
  .indicator-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .score-card-inner {
    flex-direction: column;
    gap: 20px;
  }

  .score-left {
    min-width: auto;
  }

  .chart-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .chart-indicator-tabs,
  .chart-period-tabs {
    width: 100%;
  }

  .indicator-tab {
    flex: 1;
    text-align: center;
  }
}

/* 小屏幕：进一步缩小 */
@media (max-width: 600px) {
  .score-number {
    font-size: 56px;
  }

  .card-value {
    font-size: 22px;
  }

  .page-title {
    font-size: 24px;
  }

  .chart-instance {
    height: 280px;
  }
}
.add-record-note {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin: 0 0 20px 0;
  padding: 12px 16px;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.6;
  color: #9a5a1a;
}
.add-record-note .el-icon { color: #f0a04b; font-size: 18px; flex-shrink: 0; margin-top: 2px; }

.health-fab {
  position: fixed;
  right: 24px;
  bottom: 100px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #e85d75, #f0a04b);
  box-shadow: 0 6px 20px rgba(232, 93, 117, 0.45);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9990;
  transition: transform 0.2s, box-shadow 0.2s;
}

.health-fab:hover {
  transform: scale(1.08);
  box-shadow: 0 8px 26px rgba(232, 93, 117, 0.6);
}

.health-fab:active {
  transform: scale(0.94);
}

/* ============================================================
 * 健康报告弹窗样式
 * ============================================================ */
.report-dialog :deep(.el-dialog) {
  border-radius: 18px;
}

.report-dialog :deep(.el-dialog__title) {
  font-size: 24px !important;
  font-weight: 700 !important;
}

.report-loading,
.report-empty {
  text-align: center;
  padding: 40px;
  font-size: 18px;
  color: #909399;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.report-content {
  padding: 8px;
}

.report-text {
  font-size: 17px;
  color: #2c3e50;
  line-height: 2;
  white-space: pre-wrap;
  word-break: break-word;
  background: linear-gradient(135deg, #fef9f0, #fffdf7);
  border: 2px solid #fce8c3;
  border-radius: 16px;
  padding: 24px;
  max-height: 500px;
  overflow-y: auto;
}

.report-actions {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 16px;
}

</style>

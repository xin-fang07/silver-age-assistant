<template>
  <div class="admin-config">
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="健康阈值" name="health">
        <el-card shadow="never">
          <div class="config-section">
            <h4>血压阈值（mmHg）</h4>
            <el-form :model="configForm.health" label-width="120px">
              <el-form-item label="收缩压正常范围">
                <el-input-number v-model="configForm.health.systolicMin" :min="60" :max="200" class="threshold-num" style="width:140px" />
                <span style="margin:0 8px">~</span>
                <el-input-number v-model="configForm.health.systolicMax" :min="60" :max="200" class="threshold-num" style="width:140px" />
              </el-form-item>
              <el-form-item label="舒张压正常范围">
                <el-input-number v-model="configForm.health.diastolicMin" :min="40" :max="120" class="threshold-num" style="width:140px" />
                <span style="margin:0 8px">~</span>
                <el-input-number v-model="configForm.health.diastolicMax" :min="40" :max="120" class="threshold-num" style="width:140px" />
              </el-form-item>
            </el-form>
          </div>

          <div class="config-section">
            <h4>心率阈值（次/分钟）</h4>
            <el-form :model="configForm.health" label-width="120px">
              <el-form-item label="正常范围">
                <el-input-number v-model="configForm.health.heartRateMin" :min="30" :max="200" class="threshold-num" style="width:140px" />
                <span style="margin:0 8px">~</span>
                <el-input-number v-model="configForm.health.heartRateMax" :min="30" :max="200" class="threshold-num" style="width:140px" />
              </el-form-item>
            </el-form>
          </div>

          <div class="config-section">
            <h4>血氧阈值（%）</h4>
            <el-form :model="configForm.health" label-width="120px">
              <el-form-item label="正常范围">
                <el-input-number v-model="configForm.health.bloodOxygenMin" :min="60" :max="100" class="threshold-num" style="width:140px" />
                <span style="margin:0 8px">~</span>
                <el-input-number v-model="configForm.health.bloodOxygenMax" :min="60" :max="100" class="threshold-num" style="width:140px" />
              </el-form-item>
            </el-form>
          </div>

          <div class="config-section">
            <h4>血糖阈值（mmol/L）</h4>
            <el-form :model="configForm.health" label-width="120px">
              <el-form-item label="空腹正常范围">
                <el-input-number v-model="configForm.health.fastingGlucoseMin" :min="2" :max="20" step="0.1" class="threshold-num" style="width:140px" />
                <span style="margin:0 8px">~</span>
                <el-input-number v-model="configForm.health.fastingGlucoseMax" :min="2" :max="20" step="0.1" class="threshold-num" style="width:140px" />
              </el-form-item>
              <el-form-item label="餐后正常范围">
                <el-input-number v-model="configForm.health.afterMealGlucoseMin" :min="2" :max="25" step="0.1" class="threshold-num" style="width:140px" />
                <span style="margin:0 8px">~</span>
                <el-input-number v-model="configForm.health.afterMealGlucoseMax" :min="2" :max="25" step="0.1" class="threshold-num" style="width:140px" />
              </el-form-item>
            </el-form>
          </div>

          <div class="config-section">
            <h4>睡眠阈值（小时）</h4>
            <el-form :model="configForm.health" label-width="120px">
              <el-form-item label="正常范围">
                <el-input-number v-model="configForm.health.sleepMin" :min="3" :max="12" step="0.5" class="threshold-num" style="width:140px" />
                <span style="margin:0 8px">~</span>
                <el-input-number v-model="configForm.health.sleepMax" :min="3" :max="12" step="0.5" class="threshold-num" style="width:140px" />
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="消息推送" name="push">
        <el-card shadow="never">
          <div class="config-section">
            <h4>推送开关</h4>
            <el-form :model="configForm.push" label-width="140px">
              <el-form-item label="短信推送">
                <el-switch v-model="configForm.push.smsEnabled" active-text="开启" inactive-text="关闭" />
              </el-form-item>
              <el-form-item label="APP推送">
                <el-switch v-model="configForm.push.appEnabled" active-text="开启" inactive-text="关闭" />
              </el-form-item>
              <el-form-item label="预警推送">
                <el-switch v-model="configForm.push.warningEnabled" active-text="开启" inactive-text="关闭" />
              </el-form-item>
              <el-form-item label="提醒推送">
                <el-switch v-model="configForm.push.reminderEnabled" active-text="开启" inactive-text="关闭" />
              </el-form-item>
            </el-form>
          </div>

          <div class="config-section">
            <h4>推送模板</h4>
            <el-form :model="configForm.push" label-width="140px">
              <el-form-item label="预警模板">
                <el-input v-model="configForm.push.warningTemplate" type="textarea" :rows="3" placeholder="例如：【银发智能助手】{{elderName}}的{{warningType}}异常，请及时关注" />
              </el-form-item>
              <el-form-item label="提醒模板">
                <el-input v-model="configForm.push.reminderTemplate" type="textarea" :rows="3" placeholder="例如：【银发智能助手】{{elderName}}的{{reminderTitle}}提醒时间到了" />
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="LLM配置" name="llm">
        <el-card shadow="never">
          <div class="config-section">
            <h4>接口配置</h4>
            <el-form :model="configForm.llm" label-width="120px">
              <el-form-item label="API地址">
                <el-input v-model="configForm.llm.apiUrl" placeholder="https://api.example.com/v1/chat/completions" />
              </el-form-item>
              <el-form-item label="API密钥">
                <el-input v-model="configForm.llm.apiKey" type="password" placeholder="sk-xxxxxxxxxxxxxxxxxxxx" show-password />
              </el-form-item>
              <el-form-item label="模型名称">
                <el-input v-model="configForm.llm.modelName" placeholder="deepseek-chat" />
              </el-form-item>
            </el-form>
          </div>

          <div class="config-section">
            <h4>参数配置</h4>
            <el-form :model="configForm.llm" label-width="120px">
              <el-form-item label="最大token数">
                <el-input-number v-model="configForm.llm.maxTokens" :min="256" :max="8192" style="width:150px" />
              </el-form-item>
              <el-form-item label="温度">
                <el-input-number v-model="configForm.llm.temperature" :min="0" :max="1" step="0.1" style="width:150px" />
              </el-form-item>
            </el-form>
          </div>

          <div class="config-section">
            <h4>系统提示词</h4>
            <el-form :model="configForm.llm" label-width="120px">
              <el-form-item label="健康分析提示词">
                <el-input v-model="configForm.llm.healthPrompt" type="textarea" :rows="6" placeholder="你是一位专业的老年健康分析师，请根据以下健康数据进行分析..." />
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="系统参数" name="system">
        <el-card shadow="never">
          <div class="config-section">
            <h4>基本信息</h4>
            <el-form :model="configForm.system" label-width="120px">
              <el-form-item label="系统名称">
                <el-input v-model="configForm.system.systemName" placeholder="银发智能生活助手" />
              </el-form-item>
              <el-form-item label="系统版本">
                <el-input v-model="configForm.system.version" placeholder="1.0.0" disabled />
              </el-form-item>
            </el-form>
          </div>

          <div class="config-section">
            <h4>文件配置</h4>
            <el-form :model="configForm.system" label-width="120px">
              <el-form-item label="文件上传大小限制（MB）">
                <el-input-number v-model="configForm.system.maxFileSize" :min="1" :max="100" style="width:150px" />
              </el-form-item>
            </el-form>
          </div>

          <div class="config-section">
            <h4>数据归档</h4>
            <el-form :model="configForm.system" label-width="120px">
              <el-form-item label="自动归档周期（天）">
                <el-input-number v-model="configForm.system.archiveDays" :min="7" :max="365" style="width:150px" />
              </el-form-item>
              <el-form-item label="定时备份">
                <el-switch v-model="configForm.system.backupEnabled" active-text="开启" inactive-text="关闭" />
              </el-form-item>
              <el-form-item label="备份时间">
                <el-time-picker v-model="configForm.system.backupTime" format="HH:mm" placeholder="选择备份时间" />
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <div class="save-bar">
      <el-button type="primary" size="large" :loading="saving" @click="saveConfig">保存配置</el-button>
      <el-button size="large" @click="loadConfig">重置</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api'

const activeTab = ref('health')
const saving = ref(false)

const configForm = reactive({
  health: {
    systolicMin: 90,
    systolicMax: 140,
    diastolicMin: 60,
    diastolicMax: 90,
    heartRateMin: 60,
    heartRateMax: 100,
    bloodOxygenMin: 95,
    bloodOxygenMax: 100,
    fastingGlucoseMin: 3.9,
    fastingGlucoseMax: 6.1,
    afterMealGlucoseMin: 4.4,
    afterMealGlucoseMax: 7.8,
    sleepMin: 7,
    sleepMax: 9
  },
  push: {
    smsEnabled: true,
    appEnabled: true,
    warningEnabled: true,
    reminderEnabled: true,
    warningTemplate: '',
    reminderTemplate: ''
  },
  llm: {
    apiUrl: '',
    apiKey: '',
    modelName: '',
    maxTokens: 2048,
    temperature: 0.7,
    healthPrompt: ''
  },
  system: {
    systemName: '银发智能生活助手',
    version: '1.0.0',
    maxFileSize: 10,
    archiveDays: 30,
    backupEnabled: false,
    backupTime: '02:00'
  }
})

async function loadConfig() {
  try {
    const r = await adminApi.getSystemConfig()
    const data = r.data || r || {}
    Object.assign(configForm.health, data.health || configForm.health)
    Object.assign(configForm.push, data.push || configForm.push)
    Object.assign(configForm.llm, data.llm || configForm.llm)
    Object.assign(configForm.system, data.system || configForm.system)
  } catch {
    ElMessage.warning('加载配置失败，使用默认配置')
  }
}

async function saveConfig() {
  saving.value = true
  try {
    await adminApi.saveSystemConfig(configForm)
    ElMessage.success('配置已保存')
  } catch {
    ElMessage.error('保存配置失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadConfig)
</script>

<style scoped>
.admin-config { padding: 8px; }
.config-section { margin-bottom: 24px; }
.config-section h4 { margin: 0 0 16px; font-size: 16px; font-weight: 600; color: #303133; }
.save-bar { margin-top: 20px; display: flex; justify-content: flex-end; gap: 12px; }

/* 健康阈值数字输入框：确保数值完整、居中、清晰可辨，不被加减按钮裁切 */
.threshold-num :deep(.el-input__inner) {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  text-align: center;
  /* 覆盖 Element 默认按需省略，保证多位数/小数完整显示 */
  text-overflow: clip;
  padding: 0 6px;
  -webkit-font-smoothing: antialiased;
}
/* 加减按钮固定宽度，避免挤占中间数字显示区 */
.threshold-num :deep(.el-input-number__decrease),
.threshold-num :deep(.el-input-number__increase) {
  width: 34px;
  font-size: 16px;
  color: #475569;
}
.threshold-num :deep(.el-input__wrapper) {
  padding-left: 34px;
  padding-right: 34px;
}
</style>
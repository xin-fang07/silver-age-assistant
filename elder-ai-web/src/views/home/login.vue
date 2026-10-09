<!--
  ============================================================
  home/login.vue - 登录/注册页
  银发智能生活助手 - 用户登录与管理员登录（导航栏切换）
  适老化设计：大字体、大按钮、大输入框、高对比度、简洁布局
  ============================================================
-->
<template>
  <div class="login-page">
    <!-- 登录语音开关A：仅控制登录页的欢迎语播报，记住选择 -->
    <div
      class="voice-switch"
      :class="{ 'voice-switch-off': !loginVoiceEnabled }"
      @click="toggleLoginVoice"
      :title="loginVoiceEnabled ? '登录语音已开启，点击关闭' : '登录语音已关闭，点击开启'"
    >
      <svg v-if="loginVoiceEnabled" width="18" height="18" viewBox="0 0 24 24" fill="none">
        <path d="M3 10v4a1 1 0 0 0 1 1h3l3.29 3.29c.63.63 1.71.18 1.71-.71V6.41c0-.89-1.08-1.34-1.71-.71L7 9H4a1 1 0 0 0-1 1z" fill="currentColor"/>
        <path d="M16 9a3 3 0 0 1 0 6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none">
        <path d="M3 10v4a1 1 0 0 0 1 1h3l3.29 3.29c.63.63 1.71.18 1.71-.71V6.41c0-.89-1.08-1.34-1.71-.71L7 9H4a1 1 0 0 0-1 1z" fill="currentColor"/>
        <path d="M16 9l5 5M21 9l-5 5" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      </svg>
      <span>登录语音{{ loginVoiceEnabled ? '开' : '关' }}</span>
    </div>


    <!-- 左侧品牌展示区（大屏显示，填充左侧空白、强化品牌识别） -->
    <div class="brand-panel">
      <div class="brand-inner">
        <div class="brand-logo">
          <svg width="72" height="72" viewBox="0 0 24 24" fill="none">
            <path d="M12 21s-7.5-4.35-10-9.5C.5 8 2.5 4.5 6 4.5c2 0 3.3 1 4 2.2C10.7 5.5 12 4.5 14 4.5c3.5 0 5.5 3.5 4 7-2.5 5.15-10 9.5-10 9.5z" fill="#fff" opacity="0.95"/>
          </svg>
        </div>
        <h2 class="brand-name">银发智能生活助手</h2>
        <p class="brand-slogan">用科技守护长辈的每一天</p>

        <ul class="brand-features">
          <li class="brand-feature">
            <span class="bf-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
                <path d="M3 12h4l2 5 4-12 2 7h6" stroke="#fff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
            <div class="bf-text">
              <strong>智能健康监测</strong>
              <span>实时掌握老人血压、心率等健康数据</span>
            </div>
          </li>
          <li class="brand-feature">
            <span class="bf-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
                <path d="M4 5h16v11H8l-4 4V5z" stroke="#fff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
            <div class="bf-text">
              <strong>AI 智能助手</strong>
              <span>语音问答，随时获取照护与生活建议</span>
            </div>
          </li>
          <li class="brand-feature">
            <span class="bf-icon">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
                <path d="M12 3l7 3v6c0 4.5-3 7.5-7 9-4-1.5-7-4.5-7-9V6l7-3z" stroke="#fff" stroke-width="2.2" stroke-linejoin="round"/>
                <path d="M9 12l2 2 4-4" stroke="#fff" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
            <div class="bf-text">
              <strong>安全守护</strong>
              <span>一键紧急求助，异常预警及时通知家属</span>
            </div>
          </li>
        </ul>
      </div>
    </div>

    <div class="login-card">
      <h1 class="login-title">欢迎使用银发智能生活助手</h1>

      <div class="role-tabs">
        <div
          class="role-tab"
          :class="{ 'role-tab-active': loginMode === 'user' }"
          @click="switchMode('user')"
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="8" r="4" fill="currentColor"/>
            <path d="M4 20c0-4.42 3.58-8 8-8s8 3.58 8 8" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" fill="none"/>
          </svg>
          <span>家属登录</span>
        </div>
        <div
          class="role-tab"
          :class="{ 'role-tab-active': loginMode === 'admin' }"
          @click="switchMode('admin')"
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L4 6v6c0 5 3.4 9.5 8 10 4.6-.5 8-5 8-10V6l-8-4z" fill="currentColor" opacity="0.2"/>
            <path d="M12 2L4 6v6c0 5 3.4 9.5 8 10 4.6-.5 8-5 8-10V6l-8-4z" stroke="currentColor" stroke-width="2" stroke-linejoin="round" fill="none"/>
            <path d="M9 12l2 2 4-4" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
          </svg>
          <span>管理员登录</span>
        </div>
      </div>

      <p class="login-subtitle" v-if="loginMode === 'user' && !isRegister">请登录您的账号</p>
      <p class="login-subtitle" v-else-if="loginMode === 'user' && isRegister">创建一个新账号</p>
      <p class="login-subtitle" v-else>管理员请输入管理账号和密码</p>

      <el-form
        v-if="!isRegister"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        size="large"
        class="form-container"
      >
        <el-form-item label="账号 / 手机号" prop="username">
          <el-input
            v-model="loginForm.username"
            :placeholder="'请输入账号或手机号'"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            :prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item v-if="loginMode === 'user'">
          <el-checkbox v-model="loginForm.rememberMe" size="large">
            记住我
          </el-checkbox>
        </el-form-item>

        <el-form-item v-if="loginMode === 'user'" class="forgot-password">
          <a class="forgot-link" @click="showForgotPassword = true">忘记密码？</a>
        </el-form-item>

        <div v-if="loginMode === 'admin'" class="admin-security-tip">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
            <rect x="4" y="11" width="16" height="10" rx="2" stroke="#8b5cf6" stroke-width="2" fill="none"/>
            <path d="M8 11V7a4 4 0 0 1 8 0v4" stroke="#8b5cf6" stroke-width="2" stroke-linecap="round" fill="none"/>
          </svg>
          <span>管理员账号仅供工作人员使用，请妥善保管账号密码</span>
        </div>

        <el-form-item>
          <el-button
            :type="loginMode === 'admin' ? 'primary' : 'primary'"
            size="large"
            class="submit-btn"
            :class="{ 'submit-btn-admin': loginMode === 'admin' }"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loginMode === 'admin' ? '管理员登录' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <el-dialog
        v-if="showForgotPassword"
        title="忘记密码"
        :visible.sync="showForgotPassword"
        width="420px"
        class="forgot-password-dialog"
      >
        <el-form
          v-if="forgotStep === 1"
          ref="forgotFormRef"
          :model="forgotForm"
          :rules="forgotRules"
          label-position="top"
          size="large"
        >
          <el-form-item label="邮箱地址" prop="email">
            <el-input
              v-model="forgotForm.email"
              type="email"
              placeholder="请输入注册时的邮箱"
              :prefix-icon="Message"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="forgotLoading"
              @click="handleForgotPassword"
            >
              发送验证码
            </el-button>
          </el-form-item>
        </el-form>

        <el-form
          v-if="forgotStep === 2"
          ref="resetFormRef"
          :model="resetForm"
          :rules="resetRules"
          label-position="top"
          size="large"
        >
          <el-form-item label="邮箱地址">
            <el-input
              :model-value="resetForm.email"
              disabled
              type="email"
              :prefix-icon="Message"
            />
          </el-form-item>
          <el-form-item label="验证码" prop="code">
            <div class="code-input-group">
              <el-input
                v-model="resetForm.code"
                placeholder="请输入验证码"
                :prefix-icon="CopyDocument"
              />
              <el-button
                type="default"
                :loading="sendCodeLoading"
                :disabled="codeCountdown > 0"
                @click="resendCode"
                class="send-code-btn"
              >
                {{ codeCountdown > 0 ? codeCountdown + '秒' : '重新发送' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="resetForm.newPassword"
              type="password"
              placeholder="请输入新密码（至少6位）"
              show-password
              :prefix-icon="Lock"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="resetLoading"
              @click="handleResetPassword"
            >
              重置密码
            </el-button>
          </el-form-item>
        </el-form>
      </el-dialog>

      <el-form
        v-if="isRegister"
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
        size="large"
        class="form-container"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号（选填）"
            size="large"
            :prefix-icon="Phone"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            type="email"
            placeholder="请输入邮箱（用于找回密码）"
            size="large"
            :prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item label="注册身份">
          <el-radio-group v-model="registerForm.role" size="large">
            <el-radio value="FAMILY">家属</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item class="agree-item">
          <el-checkbox v-model="registerForm.agree" size="large">
            我已阅读并同意
          </el-checkbox>
          <a class="agree-link" @click="goPrivacy">《隐私政策与用户协议》</a>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="loading"
            @click="handleRegister"
          >
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="form-toggle" v-if="loginMode === 'user'">
        <span v-if="!isRegister">
          还没有账号？
          <a class="toggle-link" @click="switchToRegister">去注册</a>
        </span>
        <span v-else>
          已有账号？
          <a class="toggle-link" @click="switchToLogin">去登录</a>
        </span>
      </div>

      <div class="back-home">
        <a @click="goHome">← 返回首页</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone, Message, CopyDocument } from '@element-plus/icons-vue'
import { authApi } from '@/api/index'
import { setToken, setUser } from '@/utils/auth'
import { useSpeech } from '@/composables/useSpeech'

const speech = useSpeech()
// 登录语音开关A（仅控制登录页的欢迎语与引导语，记住选择）
const { loginVoiceEnabled, toggleLoginVoice } = speech

// 进入登录页自动播报一句本页用途（受登录语音开关A控制；
// 若浏览器自动播放策略拦截了首次朗读，用户点击"语音引导"仍可手动听取）
onMounted(() => {
  setTimeout(() => {
    speech.speak('欢迎来到银发智能生活助手登录页面。您可以在这里登录家属或管理员账号，如果还没有账号，可以点击去注册。', '', 'login')
  }, 400)
})

const router = useRouter()
const route = useRoute()

const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginMode = ref('user')
const isRegister = ref(false)
const loading = ref(false)

const showForgotPassword = ref(false)
const forgotStep = ref(1)
const forgotLoading = ref(false)
const resetLoading = ref(false)
const sendCodeLoading = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null

const forgotFormRef = ref(null)
const resetFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const loginRules = {
  username: [
    { required: true, message: '请输入账号或手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  role: 'FAMILY',
  agree: false
})

const forgotForm = reactive({
  email: ''
})

const forgotRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const resetForm = reactive({
  email: '',
  code: '',
  newPassword: ''
})

const resetRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  phone: [
    {
      validator: (rule, value, callback) => {
        if (value && !/^1[3-9]\d{9}$/.test(value)) {
          callback(new Error('请输入正确的手机号'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const switchMode = (mode) => {
  if (loginMode.value === mode) return
  speech.stop()
  loginMode.value = mode
  isRegister.value = false
  loginForm.username = ''
  loginForm.password = ''
  loginForm.rememberMe = false
  if (loginFormRef.value) {
    loginFormRef.value.resetFields()
  }
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    const res = await authApi.login({
      username: loginForm.username,
      password: loginForm.password
    })

    if (res.data) {
      setToken(res.data.token)
      setUser(res.data.userInfo || res.data.user || {})
      const role = res.data.userInfo?.role || res.data.user?.role

      if (loginMode.value === 'admin' && role !== 'ADMIN') {
        ElMessage.warning('该账号不是管理员账号，已为您跳转到用户端')
        router.push('/care')
      } else {
        let targetPath
        if (role === 'ADMIN') targetPath = '/admin'
        else targetPath = route.query.redirect || '/care'
        const safePath = (targetPath.startsWith('/') && !targetPath.startsWith('//')) ? targetPath : '/'
        router.push(safePath)
      }
      ElMessage.success('登录成功')
    }
  } catch (error) {
    console.error('登录失败：', error)
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  try {
    await registerFormRef.value.validate()
  } catch {
    return
  }

  if (!registerForm.agree) {
    ElMessage.warning('请先阅读并同意《隐私政策与用户协议》')
    return
  }

  loading.value = true
  try {
    const res = await authApi.register({
      username: registerForm.username,
      password: registerForm.password,
      phone: registerForm.phone,
      email: registerForm.email,
      role: registerForm.role
    })

    ElMessage.success('注册成功！请登录')
    switchToLogin()
    loginForm.username = registerForm.username
  } catch (error) {
    console.error('注册失败：', error)
  } finally {
    loading.value = false
  }
}

const switchToRegister = () => {
  isRegister.value = true
  registerForm.username = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerForm.phone = ''
  registerForm.email = ''
  registerForm.agree = false
  registerForm.role = 'FAMILY'
  if (registerFormRef.value) {
    registerFormRef.value.resetFields()
  }
}

const switchToLogin = () => {
  isRegister.value = false
  loginForm.username = ''
  loginForm.password = ''
  loginForm.rememberMe = false
  if (loginFormRef.value) {
    loginFormRef.value.resetFields()
  }
}

const goPrivacy = () => {
  router.push('/privacy')
}

const goHome = () => {
  router.push('/landing')
}

const handleForgotPassword = async () => {
  if (!forgotFormRef.value) return
  try {
    await forgotFormRef.value.validate()
  } catch {
    return
  }

  forgotLoading.value = true
  try {
    await authApi.forgotPassword({ email: forgotForm.email })
    ElMessage.success('验证码已发送到您的邮箱，请查收')
    forgotStep.value = 2
    resetForm.email = forgotForm.email
    startCountdown()
  } catch (error) {
    console.error('发送验证码失败：', error)
  } finally {
    forgotLoading.value = false
  }
}

const resendCode = async () => {
  sendCodeLoading.value = true
  try {
    await authApi.forgotPassword({ email: resetForm.email })
    ElMessage.success('验证码已重新发送')
    startCountdown()
  } catch (error) {
    console.error('重新发送验证码失败：', error)
  } finally {
    sendCodeLoading.value = false
  }
}

const startCountdown = () => {
  codeCountdown.value = 60
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
  countdownTimer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

const handleResetPassword = async () => {
  if (!resetFormRef.value) return
  try {
    await resetFormRef.value.validate()
  } catch {
    return
  }

  resetLoading.value = true
  try {
    await authApi.resetPassword({
      email: resetForm.email,
      code: resetForm.code,
      newPassword: resetForm.newPassword
    })
    ElMessage.success('密码重置成功，请登录')
    showForgotPassword.value = false
    forgotStep.value = 1
    forgotForm.email = ''
    resetForm.code = ''
    resetForm.newPassword = ''
    if (countdownTimer) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
    codeCountdown.value = 0
  } catch (error) {
    console.error('重置密码失败：', error)
  } finally {
    resetLoading.value = false
  }
}
</script>

<style scoped>
/* 语音提示总开关：小巧低调，放在语音引导按钮下方 */
.voice-switch {
  position: fixed;
  top: 24px;
  right: 24px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  background: rgba(255, 255, 255, 0.9);
  color: #357abd;
  border: 1.5px solid #7db3e6;
  border-radius: 20px;
  cursor: pointer;
  z-index: 999;
  box-shadow: 0 2px 10px rgba(53, 122, 189, 0.18);
  transition: all 0.25s ease;
}

.voice-switch span {
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.voice-switch:hover {
  box-shadow: 0 4px 14px rgba(53, 122, 189, 0.3);
  transform: translateY(-1px);
}

/* 关闭状态：灰色低调 */
.voice-switch-off {
  color: #9aa7b5;
  border-color: #cfd8e0;
  background: rgba(245, 247, 250, 0.92);
}

.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 56px;
  background: linear-gradient(160deg, #e3f0ff 0%, #c5dfff 30%, #a8d4ff 60%, #6db3f2 100%);
  padding: 20px;
  position: relative;
  overflow-x: hidden;
  overflow-y: auto;
}

/* ===== 左侧品牌展示区 ===== */
.brand-panel {
  position: relative;
  z-index: 1;
  width: 420px;
  max-width: 42vw;
  color: #fff;
  animation: brand-in 0.7s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes brand-in {
  from { opacity: 0; transform: translateX(-28px); }
  to { opacity: 1; transform: translateX(0); }
}

.brand-inner {
  padding: 8px 12px;
}

.brand-logo {
  width: 96px;
  height: 96px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 28px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(6px);
  box-shadow: 0 8px 28px rgba(30, 100, 200, 0.25);
  margin-bottom: 24px;
  animation: float-circle 5s ease-in-out infinite;
}

.brand-name {
  font-size: 38px;
  font-weight: 800;
  margin: 0 0 12px;
  letter-spacing: 2px;
  text-shadow: 0 2px 12px rgba(30, 80, 160, 0.25);
}

.brand-slogan {
  font-size: 19px;
  font-weight: 500;
  margin: 0 0 36px;
  color: rgba(255, 255, 255, 0.92);
  letter-spacing: 1px;
}

.brand-features {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.brand-feature {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 18px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 16px;
  backdrop-filter: blur(6px);
  transition: transform 0.25s ease, background 0.25s ease, box-shadow 0.25s ease;
  animation: feature-in 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
}

/* 三条功能卡片错落入场，增强层次与活力 */
.brand-feature:nth-child(1) { animation-delay: 0.25s; }
.brand-feature:nth-child(2) { animation-delay: 0.4s; }
.brand-feature:nth-child(3) { animation-delay: 0.55s; }

@keyframes feature-in {
  from { opacity: 0; transform: translateX(-18px); }
  to { opacity: 1; transform: translateX(0); }
}

.brand-feature:hover {
  transform: translateX(6px);
  background: rgba(255, 255, 255, 0.22);
  box-shadow: 0 6px 20px rgba(20, 70, 150, 0.18);
}

.bf-icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.22);
}

.bf-text {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.bf-text strong {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.bf-text span {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  line-height: 1.4;
}

/* 窄屏隐藏品牌区，回退为单卡片居中 */
@media (max-width: 900px) {
  .login-page { gap: 0; }
  .brand-panel { display: none; }
}

/* 尊重"减少动效"无障碍设置：关闭入场/浮动动画，仅保留静态布局 */
:global(html.reduce-motion) .brand-panel,
:global(html.reduce-motion) .brand-feature,
:global(html.reduce-motion) .brand-logo,
:global(html.reduce-motion) .login-card,
:global(html.reduce-motion) .login-page::before,
:global(html.reduce-motion) .login-page::after {
  animation: none !important;
}

@media (prefers-reduced-motion: reduce) {
  .brand-panel,
  .brand-feature,
  .brand-logo,
  .login-card,
  .login-page::before,
  .login-page::after {
    animation: none !important;
  }
}
.login-page::before {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  top: -180px;
  right: -120px;
  animation: float-circle 8s ease-in-out infinite;
}

.login-page::after {
  content: '';
  position: absolute;
  width: 350px;
  height: 350px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  bottom: -100px;
  left: -80px;
  animation: float-circle 6s ease-in-out infinite reverse;
}

@keyframes float-circle {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-20px) scale(1.05); }
}

.login-card {
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 30px 40px 24px;
  width: 100%;
  max-width: 480px;
  box-shadow: 0 12px 48px rgba(30, 100, 200, 0.18), 0 2px 8px rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 1;
  border: 1px solid rgba(255, 255, 255, 0.6);
  animation: card-in 0.7s cubic-bezier(0.22, 1, 0.36, 1) 0.1s both;
}

/* 登录卡右侧入场，与左侧品牌区一左一右形成层次动感 */
@keyframes card-in {
  from { opacity: 0; transform: translateY(24px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.login-title {
  font-size: 26px;
  font-weight: 700;
  color: #1a3a6b;
  text-align: center;
  margin: 0 0 16px 0;
  line-height: 1.4;
  letter-spacing: 1px;
}

.role-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 14px;
  border-radius: 14px;
  overflow: hidden;
  background: #eef4fc;
  padding: 5px;
}

.role-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 11px 8px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 17px;
  font-weight: 600;
  color: #7b9ec7;
  transition: all 0.3s ease;
  position: relative;
}

.role-tab:hover {
  color: #4a90d9;
  background: rgba(74, 144, 217, 0.08);
}

.role-tab-active {
  color: #fff !important;
  background: linear-gradient(135deg, #4a90d9, #357abd);
  box-shadow: 0 4px 14px rgba(53, 122, 189, 0.35);
}

.login-subtitle {
  font-size: 16px;
  color: #7b9ec7;
  text-align: center;
  margin: 0 0 16px 0;
}

.form-container {
  margin-bottom: 4px;
}

.admin-security-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f5f0ff;
  border: 1px solid #ddd6fe;
  border-radius: 10px;
  margin-bottom: 20px;
}

.admin-security-tip span {
  font-size: 15px;
  color: #7c3aed;
  line-height: 1.5;
}

.submit-btn {
  width: 100%;
  min-height: 50px;
  font-size: 19px !important;
  font-weight: 700 !important;
  border-radius: 14px !important;
  letter-spacing: 6px;
  background: linear-gradient(135deg, #4a90d9, #357abd) !important;
  border: none !important;
  box-shadow: 0 6px 20px rgba(53, 122, 189, 0.35) !important;
  transition: all 0.3s ease !important;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #5da0e8, #4a90d9) !important;
  box-shadow: 0 8px 28px rgba(53, 122, 189, 0.5) !important;
  transform: translateY(-2px);
}

.submit-btn-admin {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed) !important;
  box-shadow: 0 6px 20px rgba(124, 58, 237, 0.35) !important;
  letter-spacing: 3px;
}

.submit-btn-admin:hover {
  background: linear-gradient(135deg, #a78bfa, #8b5cf6) !important;
  box-shadow: 0 8px 28px rgba(124, 58, 237, 0.5) !important;
}

.form-toggle {
  text-align: center;
  padding: 10px 0 6px;
  font-size: 17px;
  color: #5a7da8;
}

.toggle-link {
  color: #357abd;
  cursor: pointer;
  font-weight: 600;
  font-size: 18px;
  padding: 4px 8px;
  transition: color 0.2s;
}

.toggle-link:hover {
  color: #1a5ca8;
  text-decoration: underline;
}

.back-home {
  text-align: center;
  padding-top: 8px;
  border-top: 1px solid #e3edf7;
}

.back-home a {
  font-size: 18px;
  color: #8faac7;
  cursor: pointer;
  transition: color 0.2s;
}

.back-home a:hover {
  color: #357abd;
}

:deep(.el-form-item__label) {
  font-size: 18px !important;
  font-weight: 600 !important;
  color: #2c5282 !important;
  padding-bottom: 4px !important;
  line-height: 1.4 !important;
}

/* 收紧表单项垂直间距，使整卡内容在单屏内完整可见 */
.form-container :deep(.el-form-item) {
  margin-bottom: 14px;
}
/* “记住我”“忘记密码”两行更紧凑 */
.form-container :deep(.el-form-item:has(.el-checkbox)) {
  margin-bottom: 4px;
}
/* 表单最后一项（提交按钮）去除多余下边距 */
.form-container :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

:deep(.el-input__inner::placeholder) {
  font-size: 17px !important;
  color: #b0c8e0;
}

:deep(.el-checkbox__label) {
  font-size: 18px !important;
  color: #4a6d8c !important;
}

:deep(.el-input__wrapper) {
  border-radius: 10px !important;
  box-shadow: 0 0 0 1px #d0dff0 inset !important;
  transition: box-shadow 0.3s !important;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #a0c4e8 inset !important;
}

:deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 2px #4a90d9 inset !important;
}

:deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: #4a90d9 !important;
  border-color: #4a90d9 !important;
}

.agree-item {
  margin-bottom: 12px !important;
}
.agree-item :deep(.el-form-item__content) {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  line-height: 1.5;
}
.agree-link {
  color: #3a5bbf;
  cursor: pointer;
  font-size: 15px;
  margin-left: 2px;
}
.agree-link:hover {
  text-decoration: underline;
}

.forgot-password {
  text-align: right;
  margin-bottom: 8px !important;
}
.forgot-link {
  color: #4a90d9;
  cursor: pointer;
  font-size: 18px;
  font-weight: 500;
  transition: color 0.2s;
}
.forgot-link:hover {
  color: #2c6bb3;
  text-decoration: underline;
}

.forgot-password-dialog :deep(.el-dialog__header) {
  padding: 24px 24px 16px;
}
.forgot-password-dialog :deep(.el-dialog__title) {
  font-size: 22px;
  font-weight: 700;
  color: #1a3a6b;
}
.forgot-password-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
}
.forgot-password-dialog .code-input-group {
  display: flex;
  gap: 12px;
}
.forgot-password-dialog .code-input-group :deep(.el-input) {
  flex: 1;
}
.forgot-password-dialog .send-code-btn {
  min-width: 120px;
  font-size: 16px;
}
</style>
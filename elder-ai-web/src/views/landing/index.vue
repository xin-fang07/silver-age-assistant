<template>
  <div class="landing-page">
    <!-- ========== Header ========== -->
    <header class="landing-header" :class="{ 'scrolled': isScrolled }">
      <div class="header-container">
        <div class="logo">
          <div class="logo-icon">
            <svg width="36" height="36" viewBox="0 0 48 48" fill="none">
              <circle cx="24" cy="24" r="22" fill="url(#logoBg)"/>
              <path d="M14 20 Q14 12 24 12 Q34 12 34 20" fill="#fff" opacity="0.9"/>
              <ellipse cx="24" cy="28" rx="14" ry="10" fill="#fff" opacity="0.95"/>
              <circle cx="18" cy="26" r="2.5" fill="#4f46e5"/>
              <circle cx="30" cy="26" r="2.5" fill="#4f46e5"/>
              <circle cx="19" cy="27" r="0.8" fill="#fff"/>
              <circle cx="31" cy="27" r="0.8" fill="#fff"/>
              <path d="M18 33 Q24 37 30 33" stroke="#4f46e5" stroke-width="2" fill="none" stroke-linecap="round"/>
              <circle cx="24" cy="38" r="5" fill="url(#brainGrad)" opacity="0.6"/>
              <path d="M22 36 L26 36 M24 34 L24 38" stroke="#fff" stroke-width="1.5" stroke-linecap="round"/>
              <defs>
                <linearGradient id="logoBg" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#6366f1"/>
                  <stop offset="100%" stop-color="#a855f7"/>
                </linearGradient>
                <linearGradient id="brainGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#8b5cf6"/>
                  <stop offset="100%" stop-color="#6366f1"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <span class="logo-text">银发智能助手</span>
        </div>
        <nav class="header-nav">
          <a href="#" class="nav-link" :class="{ active: activeNav === 'features' }" @click.prevent="scrollToSection('features')">功能特色</a>
          <a href="#" class="nav-link" :class="{ active: activeNav === 'users' }" @click.prevent="scrollToSection('users')">适用人群</a>
          <a href="#" class="nav-link" :class="{ active: activeNav === 'scenarios' }" @click.prevent="scrollToSection('scenarios')">使用场景</a>
          <a href="#" class="nav-link" :class="{ active: activeNav === 'about' }" @click.prevent="scrollToSection('about')">关于我们</a>
        </nav>
        <div class="header-actions">
          <button class="btn btn-outline" @click="goLogin">登录</button>
          <button class="btn btn-primary" @click="goRegister">免费注册</button>
        </div>
      </div>
    </header>

    <!-- ========== Hero Section ========== -->
    <section class="hero-section">
      <div class="hero-bg-shapes">
        <div class="hero-bg-shape shape-1"></div>
        <div class="hero-bg-shape shape-2"></div>
        <div class="hero-bg-shape shape-3"></div>
        <div class="hero-bg-circle circle-1"></div>
        <div class="hero-bg-circle circle-2"></div>
      </div>
      <div class="hero-container">
        <div class="hero-content">
          <div class="hero-badge">
            <span>AI驱动 · 智慧养老</span>
          </div>
          <h1 class="hero-title">让科技温暖晚年生活</h1>
          <p class="hero-subtitle">银发智能生活助手，面向家属与照护管理者的智慧养老平台。外接智能设备自动同步健康数据，AI 语音助手贴心陪伴，异常预警实时守护，让家人远程照护更安心。</p>
          <div class="hero-actions">
            <button class="btn btn-primary btn-lg" @click="goRegister">立即体验</button>
            <button class="btn btn-outline btn-lg" @click="scrollToFeatures">了解更多</button>
          </div>
          <div class="hero-stats">
            <div class="stat-item">
              <span class="stat-value">10万+</span>
              <span class="stat-label">服务老人</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value">100万+</span>
              <span class="stat-label">AI问答</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-value">99.8%</span>
              <span class="stat-label">预警处理</span>
            </div>
          </div>
        </div>
        <div class="hero-image">
          <div class="image-card">
            <div class="card-icon ai-icon">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none">
                <path d="M12 2L2 7l10 5 10-5-10-5z" fill="#8b5cf6"/>
                <path d="M2 17l10 5 10-5" stroke="#8b5cf6" stroke-width="2" fill="none"/>
                <path d="M2 12l10 5 10-5" stroke="#8b5cf6" stroke-width="2" fill="none"/>
              </svg>
            </div>
            <div class="card-title">AI 智能助手</div>
            <div class="card-desc">语音对话贴心陪伴，健康建议随时播报</div>
          </div>
          <div class="image-card card-2">
            <div class="card-icon health-icon">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none">
                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="#ef4444"/>
              </svg>
            </div>
            <div class="card-title">智能健康监测</div>
            <div class="card-desc">设备同步血压心率，异常预警</div>
          </div>
          <div class="image-card card-3">
            <div class="card-icon medical-icon">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="6" width="18" height="14" rx="2" stroke="#06b6d4" stroke-width="2" fill="none"/>
                <path d="M9 6V4h6v2" stroke="#06b6d4" stroke-width="2" fill="none"/>
                <path d="M12 9v6m-3-3h6" stroke="#06b6d4" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </div>
            <div class="card-title">医疗服务</div>
            <div class="card-desc">用药提醒与就医管理，健康生活更省心</div>
          </div>
          <div class="floating-dots">
            <div class="dot dot-1"></div>
            <div class="dot dot-2"></div>
            <div class="dot dot-3"></div>
            <div class="dot dot-4"></div>
          </div>
        </div>
      </div>
    </section>

    <!-- ========== Features Section ========== -->
    <section id="features" class="features-section">
      <div class="section-container">
        <div class="section-header">
          <div class="section-badge">核心功能</div>
          <h2 class="section-title">专为老年人设计的智能服务</h2>
          <p class="section-desc">我们精心打造每一个功能，让老人轻松上手，享受科技带来的便利</p>
        </div>
        <div class="features-grid">
          <div class="feature-card" v-for="(feature, index) in features" :key="index">
            <div class="feature-icon" :class="feature.iconClass">
              <div v-html="feature.icon"></div>
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-desc">{{ feature.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ========== Users Section ========== -->
    <section id="users" class="users-section">
      <div class="section-container">
        <div class="section-header">
          <div class="section-badge">适用人群</div>
          <h2 class="section-title">两大角色，协同关爱</h2>
          <p class="section-desc">我们为不同角色提供专属服务，构建完整的养老关爱体系</p>
        </div>
        <div class="users-grid">
          <div class="user-card family-card">
            <div class="user-icon-wrapper">
              <div class="user-icon">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
                  <circle cx="6" cy="8" r="4" fill="#10b981"/>
                  <circle cx="18" cy="8" r="4" fill="#10b981"/>
                  <path d="M4 20c0-4.42 3.58-8 8-8s8 3.58 8 8" stroke="#10b981" stroke-width="2" stroke-linecap="round" fill="none"/>
                  <path d="M6 12c0 0 4-4 6-4s6 4 6 4" stroke="#10b981" stroke-width="2" fill="none"/>
                </svg>
              </div>
            </div>
            <h3 class="user-title">家属 / 照护者</h3>
            <p class="user-desc">绑定并管理老人档案，远程查看智能设备同步的健康数据，接收异常预警，安排用药提醒与就医信息。</p>
            <ul class="user-features">
              <li>老人绑定与档案管理</li>
              <li>智能设备健康数据查看</li>
              <li>异常预警实时接收</li>
              <li>用药提醒与就医信息</li>
              <li>娱乐活动报名参与</li>
            </ul>
          </div>
          <div class="user-card admin-card">
            <div class="user-icon-wrapper">
              <div class="user-icon">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
                  <path d="M12 2L4 6v6c0 5 3.4 9.5 8 10 4.6-.5 8-5 8-10V6l-8-4z" stroke="#f59e0b" stroke-width="2" stroke-linejoin="round" fill="none"/>
                  <path d="M9 12l2 2 4-4" stroke="#f59e0b" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
                </svg>
              </div>
            </div>
            <h3 class="user-title">管理员（管理端）</h3>
            <p class="user-desc">统筹平台运营：维护老人档案、管理智能设备、全局健康监控、配置预警规则、维护医疗知识库与发布活动。</p>
            <ul class="user-features">
              <li>老人档案管理</li>
              <li>智能设备管理</li>
              <li>全局健康监控</li>
              <li>异常预警管理</li>
              <li>医疗知识库维护</li>
              <li>娱乐活动管理</li>
            </ul>
          </div>
        </div>
      </div>
    </section>

    <!-- ========== Scenarios Section ========== -->
    <section id="scenarios" class="scenarios-section">
      <div class="section-container">
        <div class="section-header">
          <div class="section-badge">使用场景</div>
          <h2 class="section-title">一天的智能生活</h2>
          <p class="section-desc">从清晨到夜晚，银发智能助手陪伴老人的每一刻</p>
        </div>
        <div class="timeline">
          <div class="timeline-item" v-for="(item, index) in scenarios" :key="index">
            <div class="timeline-dot"></div>
            <div class="timeline-content">
              <div class="timeline-time">{{ item.time }}</div>
              <h3 class="timeline-title">{{ item.title }}</h3>
              <p class="timeline-desc">{{ item.desc }}</p>
            </div>
            <div class="timeline-icon">
              <div v-html="item.icon"></div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ========== Stats Section ========== -->
    <section class="stats-section" ref="statsSection">
      <div class="section-container">
        <div class="stats-grid">
          <div class="stat-card" v-for="(stat, index) in stats" :key="index">
            <div class="stat-icon">
              <div v-html="stat.icon"></div>
            </div>
            <div class="stat-value-wrapper">
              <span class="stat-value">{{ animatedNumbers[index] !== undefined ? animatedNumbers[index] : stat.value }}</span>
              <span class="stat-unit">{{ stat.unit }}</span>
            </div>
            <span class="stat-label">{{ stat.label }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- ========== Testimonials Section ========== -->
    <section id="testimonials" class="testimonials-section">
      <div class="section-container">
        <div class="section-header">
          <div class="section-badge">用户评价</div>
          <h2 class="section-title">真实用户的使用体验</h2>
          <p class="section-desc">听听他们怎么说，银发智能助手已服务超过10万+老人</p>
        </div>
        <div class="testimonials-grid">
          <div class="testimonial-card" v-for="(testimonial, index) in testimonials" :key="index">
            <div class="testimonial-stars">
              <svg v-for="n in 5" :key="n" width="16" height="16" viewBox="0 0 24 24" fill="#fbbf24">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
              </svg>
            </div>
            <p class="testimonial-content">{{ testimonial.content }}</p>
            <div class="testimonial-author">
              <div class="author-avatar" :style="{ background: testimonial.avatarColor }">
                {{ testimonial.name.charAt(0) }}
              </div>
              <div class="author-info">
                <span class="author-name">{{ testimonial.name }}</span>
                <span class="author-role">{{ testimonial.role }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ========== Security Section ========== -->
    <section id="security" class="security-section">
      <div class="section-container">
        <div class="section-header">
          <div class="section-badge">安全保障</div>
          <h2 class="section-title">您的数据安全是我们的首要任务</h2>
          <p class="section-desc">采用银行级加密技术，全方位保护您的隐私和数据安全</p>
        </div>
        <div class="security-grid">
          <div class="security-card" v-for="(security, index) in securityFeatures" :key="index">
            <div class="security-icon" :class="security.iconClass">
              <div v-html="security.icon"></div>
            </div>
            <h3 class="security-title">{{ security.title }}</h3>
            <p class="security-desc">{{ security.desc }}</p>
          </div>
        </div>
      </div>
    </section>



    <!-- ========== CTA Section ========== -->
    <section class="cta-section">
      <div class="section-container">
        <div class="cta-content">
          <h2 class="cta-title">开启智能养老新生活</h2>
          <p class="cta-desc">立即注册，让科技温暖您的晚年生活</p>
          <div class="cta-actions">
            <button class="btn btn-primary btn-lg" @click="goRegister">免费注册</button>
            <button class="btn btn-outline btn-lg" @click="goLogin">已有账号？登录</button>
          </div>
        </div>
      </div>
    </section>

    <!-- ========== Back to Top Button ========== -->
    <button 
      class="back-to-top" 
      :class="{ show: showBackTop }"
      @click="scrollToTop"
      aria-label="返回顶部"
    >
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
        <path d="M5 15l7-7 7 7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </button>

    <!-- ========== Footer ========== -->
    <footer id="about" class="landing-footer">
      <div class="section-container">
        <div class="footer-content">
          <div class="footer-brand">
            <div class="logo">
              <div class="logo-icon">
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                  <rect x="4" y="4" width="16" height="16" rx="4" fill="url(#footerLogoBg)"/>
                  <path d="M8 16V8l4 4 4-4v8" stroke="#fff" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                  <circle cx="12" cy="10" r="1.5" fill="#fff"/>
                  <circle cx="14" cy="12" r="1" fill="rgba(255,255,255,0.8)"/>
                  <circle cx="10" cy="14" r="1" fill="rgba(255,255,255,0.6)"/>
                  <circle cx="16" cy="14" r="0.8" fill="rgba(255,255,255,0.4)"/>
                  <circle cx="8" cy="12" r="0.8" fill="rgba(255,255,255,0.4)"/>
                  <defs>
                    <linearGradient id="footerLogoBg" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" stop-color="#4f46e5"/>
                      <stop offset="100%" stop-color="#8b5cf6"/>
                    </linearGradient>
                  </defs>
                </svg>
              </div>
              <span class="logo-text">银发智能助手</span>
            </div>
            <p class="footer-desc">专为老年人打造的智能生活助手，让科技温暖晚年。</p>
          </div>
          <div class="footer-links">
            <div class="footer-link-group">
              <h4 class="link-title">产品功能</h4>
              <a href="#features">AI语音助手</a>
              <a href="#features">智能健康监测</a>
              <a href="#features">异常预警</a>
              <a href="#features">紧急求助</a>
            </div>
            <div class="footer-link-group">
              <h4 class="link-title">关于我们</h4>
              <a href="#about">公司介绍</a>
              <a href="/contact">联系我们</a>
              <a href="#privacy">隐私政策</a>
              <a href="#privacy">用户协议</a>
            </div>
            <div class="footer-link-group">
              <h4 class="link-title">帮助支持</h4>
              <a href="#">使用指南</a>
              <router-link to="/faq">常见问题</router-link>
              <a href="#">在线客服</a>
              <a href="#">反馈建议</a>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <p class="copyright">© 2026 银发智能生活助手. All rights reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

const isScrolled = ref(false)
const statsSection = ref<HTMLElement | null>(null)
const animatedNumbers = ref<string[]>([])
const activeNav = ref('')
const showBackTop = ref(false)

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50
  showBackTop.value = window.scrollY > 500
  
  const sections = ['features', 'users', 'scenarios', 'testimonials', 'security', 'about']
  let currentSection = ''
  
  for (const sectionId of sections) {
    const element = document.getElementById(sectionId)
    if (element) {
      const rect = element.getBoundingClientRect()
      if (rect.top <= 150 && rect.bottom > 150) {
        currentSection = sectionId
        break
      }
    }
  }
  
  activeNav.value = currentSection
}

const scrollToSection = (sectionId: string) => {
  const element = document.getElementById(sectionId)
  if (element) {
    const headerHeight = 70
    const elementPosition = element.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.pageYOffset - headerHeight
    
    window.scrollTo({
      top: offsetPosition,
      behavior: 'smooth'
    })
  }
}

const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

const animateNumbers = () => {
  const targetNumbers = ['100,000', '1,000,000', '99.8', '10,000']
  const durations = [2000, 2500, 1500, 1800]
  
  targetNumbers.forEach((target, index) => {
    const isDecimal = target.includes('.')
    const targetNum = parseFloat(target.replace(/,/g, ''))
    const duration = durations[index]
    const startTime = Date.now()
    
    const animate = () => {
      const elapsed = Date.now() - startTime
      const progress = Math.min(elapsed / duration, 1)
      const easeOut = 1 - Math.pow(1 - progress, 3)
      const current = targetNum * easeOut
      
      if (isDecimal) {
        animatedNumbers.value[index] = current.toFixed(1)
      } else {
        animatedNumbers.value[index] = Math.floor(current).toLocaleString()
      }
      
      if (progress < 1) {
        requestAnimationFrame(animate)
      }
    }
    
    requestAnimationFrame(animate)
  })
}

let observer: IntersectionObserver | null = null

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  
  if (statsSection.value) {
    observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting && animatedNumbers.value.length === 0) {
          animateNumbers()
        }
      })
    }, { threshold: 0.3 })
    
    observer.observe(statsSection.value)
  }
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
  if (observer) {
    observer.disconnect()
  }
})

const features = ref([
  {
    icon: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><path d="M12 2L2 7l10 5 10-5-10-5z" fill="#8b5cf6"/><path d="M2 17l10 5 10-5" stroke="#8b5cf6" stroke-width="2" fill="none"/><path d="M2 12l10 5 10-5" stroke="#8b5cf6" stroke-width="2" fill="none"/></svg>',
    iconClass: 'ai-icon',
    title: 'AI 智能助手',
    desc: '基于大模型的智能对话，支持语音交互与健康咨询，家属可代为使用或协助老人操作'
  },
  {
    icon: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="#ef4444"/></svg>',
    iconClass: 'health-icon',
    title: '智能健康监测',
    desc: '外接智能手表与健康设备自动同步血压、血糖、心率、体重，数据实时上传'
  },
  {
    icon: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><rect x="3" y="6" width="18" height="14" rx="2" stroke="#06b6d4" stroke-width="2" fill="none"/><path d="M9 6V4h6v2" stroke="#06b6d4" stroke-width="2" fill="none"/><path d="M12 9v6m-3-3h6" stroke="#06b6d4" stroke-width="2" stroke-linecap="round"/></svg>',
    iconClass: 'medical-icon',
    title: '医疗服务',
    desc: '用药提醒、就医信息（病历照片/检查报告）与饮食健康管理，一站式健康服务'
  },
  {
    icon: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6A19.79 19.79 0 012.12 4.18 2 2 0 014.11 2h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z" stroke="#ef4444" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
    iconClass: 'emergency-icon',
    title: '紧急事件管理',
    desc: '接收老人 SOS 报警通知与位置信息，查看报警记录与处理状态，第一时间响应紧急情况'
  },
  {
    icon: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="8" r="4" fill="#10b981"/><path d="M4 20c0-4.42 3.58-8 8-8s8 3.58 8 8" stroke="#10b981" stroke-width="2" stroke-linecap="round" fill="none"/></svg>',
    iconClass: 'family-icon',
    title: '家属协同照护',
    desc: '绑定并管理老人档案，远程查看设备健康数据，接收异常预警与求助通知'
  },
  {
    icon: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><path d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" stroke="#4f46e5" stroke-width="2" fill="none"/></svg>',
    iconClass: 'voice-icon',
    title: '资讯与娱乐',
    desc: '每日养老资讯与线上科普，线下活动报名（棋牌、座谈等），丰富精神生活'
  }
])

const scenarios = ref([
  {
    time: '07:00',
    title: '晨间问候',
    desc: '语音助手播报天气与健康建议，开启一天的智能守护',
    icon: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="5" fill="#fbbf24"/><path d="M12 1v2m0 18v2M4.22 4.22l1.42 1.42m12.72 12.72l1.42 1.42M1 12h2m18 0h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" stroke="#fbbf24" stroke-width="2" fill="none"/></svg>'
  },
  {
    time: '08:00',
    title: '设备健康同步',
    desc: '智能手表自动上传晨起血压与心率，异常指标即时预警',
    icon: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="#ef4444"/></svg>'
  },
  {
    time: '10:00',
    title: 'AI 健康咨询',
    desc: '问诊用药、饮食宜忌，AI 语音助手随时解答生活疑问',
    icon: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><path d="M12 2L2 7l10 5 10-5-10-5z" fill="#8b5cf6"/><path d="M2 17l10 5 10-5" stroke="#8b5cf6" stroke-width="2" fill="none"/><path d="M2 12l10 5 10-5" stroke="#8b5cf6" stroke-width="2" fill="none"/></svg>'
  },
  {
    time: '12:00',
    title: '用药提醒',
    desc: '按设定准时提醒服药，自动记录用药历史',
    icon: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" stroke="#10b981" stroke-width="2" fill="none"/></svg>'
  },
  {
    time: '15:00',
    title: '就医记录',
    desc: '上传病历与检查报告图片，完善老人健康档案',
    icon: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" stroke="#f59e0b" stroke-width="2" fill="none"/></svg>'
  },
  {
    time: '19:00',
    title: '娱乐活动',
    desc: '报名线下棋牌或收看线上科普，丰富晚间生活',
    icon: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="#4f46e5" stroke-width="2" fill="none"/><circle cx="12" cy="7" r="4" stroke="#4f46e5" stroke-width="2" fill="none"/></svg>'
  }
])

const stats = ref([
  {
    icon: '<svg width="48" height="48" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="8" r="4" fill="#4f46e5"/><path d="M4 20c0-4.42 3.58-8 8-8s8 3.58 8 8" stroke="#4f46e5" stroke-width="2" stroke-linecap="round" fill="none"/></svg>',
    value: '100,000',
    unit: '+',
    label: '守护老人'
  },
  {
    icon: '<svg width="48" height="48" viewBox="0 0 24 24" fill="none"><path d="M12 2L2 7l10 5 10-5-10-5z" fill="#8b5cf6"/></svg>',
    value: '1,000,000',
    unit: '+',
    label: 'AI对话次数'
  },
  {
    icon: '<svg width="48" height="48" viewBox="0 0 24 24" fill="none"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="#ef4444"/></svg>',
    value: '99.8',
    unit: '%',
    label: '异常预警准确率'
  },
  {
    icon: '<svg width="48" height="48" viewBox="0 0 24 24" fill="none"><path d="M6 19h12" stroke="#ef4444" stroke-width="2" stroke-linecap="round"/><path d="M8 7h8v12H8z" stroke="#ef4444" stroke-width="2" fill="none"/></svg>',
    value: '10,000',
    unit: '+',
    label: '紧急求助响应'
  }
])

const testimonials = ref([
  {
    content: '自从用了银发智能助手，早上语音播报天气和健康建议，智能手表数据自动同步，吃药提醒也很贴心，最重要的是随时能问 AI，就像有个小助手在身边。',
    name: '王阿姨',
    role: '老年用户 · 使用半年',
    avatarColor: 'linear-gradient(135deg, #4f46e5, #8b5cf6)'
  },
  {
    content: '作为女儿，最担心的就是父母健康。现在爸妈的智能设备数据我随时能看到，指标异常还会实时通知我，心里踏实多了。',
    name: '李女士',
    role: '家属 · 使用一年',
    avatarColor: 'linear-gradient(135deg, #10b981, #34d399)'
  },
  {
    content: '系统操作很简单，界面清晰，字也大，我这个70多岁的老人轻松就能上手。特别是紧急求助功能，按一下就能通知家人，很安心。',
    name: '张大爷',
    role: '老年用户 · 使用三个月',
    avatarColor: 'linear-gradient(135deg, #f59e0b, #fbbf24)'
  },
  {
    content: '养老院引入系统后，老人健康预警、紧急求助都能及时处理，家属随时了解状况，还能发布线下活动，管理效率明显提升。',
    name: '陈院长',
    role: '养老院管理者',
    avatarColor: 'linear-gradient(135deg, #ef4444, #f87171)'
  }
])

const securityFeatures = ref([
  {
    icon: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" fill="#4f46e5"/></svg>',
    iconClass: 'encryption-icon',
    title: '银行级加密',
    desc: '采用256位SSL加密传输，数据存储使用AES加密，确保您的信息安全'
  },
  {
    icon: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z" fill="#10b981"/></svg>',
    iconClass: 'shield-icon',
    title: '隐私保护',
    desc: '严格遵守《个人信息保护法》，您的数据仅用于服务提供，绝不对外泄露'
  },
  {
    icon: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><path d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" fill="#f59e0b"/></svg>',
    iconClass: 'access-icon',
    title: '权限控制',
    desc: '精细的权限管理，家属经绑定授权后查看健康数据，全程加密保护隐私'
  },
  {
    icon: '<svg width="40" height="40" viewBox="0 0 24 24" fill="none"><path d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" fill="#ef4444"/></svg>',
    iconClass: 'backup-icon',
    title: '数据备份',
    desc: '多重数据备份机制，异地容灾存储，确保您的数据永不丢失'
  }
])

const goLogin = () => {
  window.location.href = '/login'
}

const goRegister = () => {
  window.location.href = '/login'
}

const scrollToFeatures = () => {
  document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' })
}
</script>

<style scoped>
.landing-page {
  min-height: 100vh;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* ========== Header ========== */
.landing-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: transparent;
  backdrop-filter: blur(0px);
  border-bottom: none;
  transition: all 0.3s ease;
}

.landing-header.scrolled {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(15px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
}

.header-nav {
  display: flex;
  gap: 30px;
}

.nav-link {
  text-decoration: none;
  color: #64748b;
  font-size: 15px;
  font-weight: 500;
  transition: color 0.3s;
  position: relative;
}

.nav-link:hover {
  color: #4f46e5;
}

.nav-link.active {
  color: #4f46e5;
  font-weight: 600;
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #4f46e5, #8b5cf6);
  border-radius: 1px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn {
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.3s;
}

.btn-outline {
  background: transparent;
  color: #4f46e5;
  border: 1px solid #4f46e5;
}

.btn-outline:hover {
  background: #4f46e5;
  color: white;
}

.btn-primary {
  background: linear-gradient(135deg, #4f46e5, #8b5cf6);
  color: white;
  box-shadow: 0 4px 15px rgba(79, 70, 229, 0.3);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.4);
}

.btn-lg {
  padding: 14px 32px;
  font-size: 16px;
}

/* ========== Hero Section ========== */
.hero-section {
  padding: 150px 20px 100px;
  background: linear-gradient(135deg, #eef2ff 0%, #f0f0ff 30%, #fef3ff 60%, #fff5f5 100%);
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    linear-gradient(rgba(79, 70, 229, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(79, 70, 229, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
}

.hero-section::after {
  content: '';
  position: absolute;
  top: -100px;
  right: -100px;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}

.hero-bg-shapes {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.hero-bg-shape {
  position: absolute;
  border-radius: 50%;
}

.hero-bg-shape.shape-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  left: -5%;
  background: radial-gradient(circle, rgba(79, 70, 229, 0.08) 0%, transparent 70%);
  animation: float 8s ease-in-out infinite;
}

.hero-bg-shape.shape-2 {
  width: 200px;
  height: 200px;
  bottom: 20%;
  right: 5%;
  background: radial-gradient(circle, rgba(239, 68, 68, 0.06) 0%, transparent 70%);
  animation: float 6s ease-in-out infinite reverse;
}

.hero-bg-shape.shape-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: radial-gradient(circle, rgba(139, 92, 246, 0.05) 0%, transparent 70%);
  animation: float 10s ease-in-out infinite;
  animation-delay: 2s;
}

.hero-bg-circle {
  position: absolute;
  border: 1px solid rgba(79, 70, 229, 0.08);
  border-radius: 50%;
}

.hero-bg-circle.circle-1 {
  width: 500px;
  height: 500px;
  top: -200px;
  right: -100px;
}

.hero-bg-circle.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -100px;
  left: -50px;
}

.hero-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.hero-badge {
  display: inline-block;
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.1), rgba(139, 92, 246, 0.1));
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  color: #4f46e5;
  font-weight: 500;
  margin-bottom: 20px;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  color: #1e293b;
  line-height: 1.2;
  margin-bottom: 20px;
  text-align: center;
}

.hero-subtitle {
  font-size: 18px;
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 30px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 40px;
}

.hero-stats {
  display: flex;
  align-items: center;
  gap: 30px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #4f46e5;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #e2e8f0;
}

.hero-image {
  position: relative;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-card {
  position: absolute;
  background: white;
  padding: 24px;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  transition: transform 0.3s, box-shadow 0.3s;
}

.image-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.12);
}

.card-3:hover {
  transform: translateX(-50%) translateY(-5px);
}

.card-icon {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-icon {
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.1), rgba(139, 92, 246, 0.05));
}

.health-icon {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1), rgba(239, 68, 68, 0.05));
}

.emergency-icon {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.1), rgba(245, 158, 11, 0.05));
}

.medical-icon {
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.1), rgba(6, 182, 212, 0.05));
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.card-desc {
  font-size: 13px;
  color: #64748b;
}

.image-card:first-child {
  top: 20px;
  left: 20px;
}

.card-2 {
  top: 10px;
  right: 10px;
}

.card-3 {
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
}

.floating-dots {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.dot {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.1), rgba(139, 92, 246, 0.1));
  animation: float 6s ease-in-out infinite;
}

.dot-1 {
  width: 60px;
  height: 60px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.dot-2 {
  width: 40px;
  height: 40px;
  top: 60%;
  left: 80%;
  animation-delay: 2s;
}

.dot-3 {
  width: 80px;
  height: 80px;
  bottom: 10%;
  left: 30%;
  animation-delay: 4s;
}

.dot-4 {
  width: 30px;
  height: 30px;
  top: 40%;
  right: 5%;
  animation-delay: 1s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

/* ========== Section Common ========== */
.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px 20px;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-badge {
  display: inline-block;
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.1), rgba(139, 92, 246, 0.1));
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  color: #4f46e5;
  font-weight: 500;
  margin-bottom: 16px;
}

.section-title {
  font-size: 36px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 16px;
  text-align: center;
  display: block;
}

.section-desc {
  font-size: 16px;
  color: #64748b;
  max-width: 600px;
  margin: 0 auto;
}

/* ========== Features Section ========== */
.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
}

.feature-card {
  background: white;
  padding: 30px;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  border: 2px solid transparent;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.12);
  border-color: rgba(79, 70, 229, 0.2);
}

.feature-icon {
  width: 70px;
  height: 70px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.feature-icon.ai-icon { background: linear-gradient(135deg, rgba(139, 92, 246, 0.15), rgba(139, 92, 246, 0.05)); }
.feature-icon.health-icon { background: linear-gradient(135deg, rgba(239, 68, 68, 0.15), rgba(239, 68, 68, 0.05)); }
.feature-icon.reminder-icon { background: linear-gradient(135deg, rgba(245, 158, 11, 0.15), rgba(245, 158, 11, 0.05)); }
.feature-icon.emergency-icon { background: linear-gradient(135deg, rgba(239, 68, 68, 0.15), rgba(239, 68, 68, 0.05)); }
.feature-icon.family-icon { background: linear-gradient(135deg, rgba(16, 185, 129, 0.15), rgba(16, 185, 129, 0.05)); }
.feature-icon.voice-icon { background: linear-gradient(135deg, rgba(79, 70, 229, 0.15), rgba(79, 70, 229, 0.05)); }
.feature-icon.medical-icon { background: linear-gradient(135deg, rgba(6, 182, 212, 0.15), rgba(6, 182, 212, 0.05)); }

.feature-card:hover .feature-icon.ai-icon { 
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.3), rgba(139, 92, 246, 0.1)); 
  box-shadow: 0 8px 25px rgba(139, 92, 246, 0.3);
}
.feature-card:hover .feature-icon.health-icon { 
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.3), rgba(239, 68, 68, 0.1)); 
  box-shadow: 0 8px 25px rgba(239, 68, 68, 0.3);
}
.feature-card:hover .feature-icon.reminder-icon { 
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.3), rgba(245, 158, 11, 0.1)); 
  box-shadow: 0 8px 25px rgba(245, 158, 11, 0.3);
}
.feature-card:hover .feature-icon.emergency-icon { 
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.3), rgba(239, 68, 68, 0.1)); 
  box-shadow: 0 8px 25px rgba(239, 68, 68, 0.3);
}
.feature-card:hover .feature-icon.family-icon { 
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.3), rgba(16, 185, 129, 0.1)); 
  box-shadow: 0 8px 25px rgba(16, 185, 129, 0.3);
}
.feature-card:hover .feature-icon.voice-icon { 
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.3), rgba(79, 70, 229, 0.1)); 
  box-shadow: 0 8px 25px rgba(79, 70, 229, 0.3);
}
.feature-card:hover .feature-icon.medical-icon { 
  background: linear-gradient(135deg, rgba(6, 182, 212, 0.3), rgba(6, 182, 212, 0.1)); 
  box-shadow: 0 8px 25px rgba(6, 182, 212, 0.3);
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 10px;
}

.feature-desc {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
}

/* ========== Users Section ========== */
.users-section {
  background: #f8fafc;
}

.users-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30px;
  max-width: 800px;
  margin: 0 auto;
}

.user-card {
  background: white;
  padding: 35px;
  border-radius: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  border-top: 4px solid transparent;
}

.user-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
}

.elder-card { border-top-color: #4f46e5; }
.family-card { border-top-color: #10b981; }
.admin-card { border-top-color: #f59e0b; }

.user-icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.elder-card .user-icon-wrapper { background: linear-gradient(135deg, rgba(79, 70, 229, 0.1), rgba(79, 70, 229, 0.05)); }
.family-card .user-icon-wrapper { background: linear-gradient(135deg, rgba(16, 185, 129, 0.1), rgba(16, 185, 129, 0.05)); }
.admin-card .user-icon-wrapper { background: linear-gradient(135deg, rgba(245, 158, 11, 0.1), rgba(245, 158, 11, 0.05)); }

.user-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 10px;
}

.user-desc {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 20px;
}

.user-features {
  list-style: none;
  padding: 0;
  margin: 0;
}

.user-features li {
  font-size: 13px;
  color: #64748b;
  padding: 6px 0;
  padding-left: 20px;
  position: relative;
}

.user-features li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #4f46e5;
}

/* ========== Scenarios Section ========== */
.timeline {
  position: relative;
  max-width: 800px;
  margin: 0 auto;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #e2e8f0;
  transform: translateX(-50%);
}

.timeline-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 40px;
}

.timeline-item:nth-child(even) {
  flex-direction: row-reverse;
}

.timeline-dot {
  position: absolute;
  left: 50%;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: white;
  border: 3px solid #4f46e5;
  transform: translateX(-50%);
  z-index: 1;
}

.timeline-content {
  width: 45%;
}

.timeline-time {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
  margin-bottom: 8px;
}

.timeline-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
}

.timeline-desc {
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
}

.timeline-icon {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ========== Stats Section ========== */
.stats-section {
  background: linear-gradient(135deg, #4f46e5, #8b5cf6);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 40px;
}

.stat-card {
  text-align: center;
  color: white;
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.stat-value-wrapper {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
}

.stat-unit {
  font-size: 20px;
  font-weight: 500;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
}

/* ========== Testimonials Section ========== */
.testimonials-section {
  background: #f8fafc;
}

.testimonials-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.testimonial-card {
  background: white;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.testimonial-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.testimonial-stars {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
}

.testimonial-content {
  font-size: 15px;
  color: #475569;
  line-height: 1.8;
  margin-bottom: 24px;
}

.testimonial-author {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.author-name {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.author-role {
  display: block;
  font-size: 13px;
  color: #94a3b8;
}

/* ========== Security Section ========== */
.security-section {
  background: linear-gradient(135deg, #4f46e5 0%, #8b5cf6 100%);
  color: white;
}

.security-section .section-title {
  color: white;
}

.security-section .section-desc {
  color: rgba(255, 255, 255, 0.8);
}

.security-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
}

.security-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  padding: 32px;
  border-radius: 16px;
  text-align: center;
  transition: all 0.3s ease;
}

.security-card:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-4px);
}

.security-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
}

.security-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
}

.security-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
}

/* ========== Contact Section ========== */
.contact-section {
  background: #f8fafc;
}

.contact-form-wrapper {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 40px;
  max-width: 900px;
  margin: 0 auto;
}

.contact-form {
  background: white;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  font-size: 15px;
  font-weight: 500;
  color: #334155;
  margin-bottom: 8px;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 15px;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.form-group textarea {
  resize: vertical;
  min-height: 120px;
}

.submit-btn {
  width: 100%;
  margin-top: 8px;
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.info-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.1), rgba(139, 92, 246, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.info-title {
  display: block;
  font-size: 14px;
  color: #94a3b8;
  margin-bottom: 4px;
}

.info-text {
  display: block;
  font-size: 15px;
  font-weight: 500;
  color: #334155;
}

/* ========== CTA Section ========== */
.cta-section {
  padding: 80px 20px;
  text-align: center;
}

.cta-title {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 16px;
  text-align: center;
}

.cta-desc {
  font-size: 16px;
  color: #64748b;
  margin-bottom: 30px;
}

.cta-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

/* ========== Footer ========== */
.landing-footer {
  background: #1e293b;
  color: white;
}

.footer-content {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 60px;
  padding-bottom: 40px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.footer-brand .logo-text {
  color: white;
}

.footer-desc {
  font-size: 14px;
  color: #94a3b8;
  margin-top: 16px;
  line-height: 1.6;
}

.footer-links {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
}

.link-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.footer-link-group a {
  display: block;
  text-decoration: none;
  color: #94a3b8;
  font-size: 14px;
  padding: 6px 0;
  transition: color 0.3s;
}

.footer-link-group a:hover {
  color: white;
}

.footer-bottom {
  padding-top: 20px;
}

.copyright {
  font-size: 13px;
  color: #64748b;
  text-align: center;
}

/* ========== Back to Top Button ========== */
.back-to-top {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4f46e5, #8b5cf6);
  color: white;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transform: translateY(20px);
  transition: all 0.3s ease;
  z-index: 1000;
  box-shadow: 0 4px 16px rgba(79, 70, 229, 0.4);
}

.back-to-top:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(79, 70, 229, 0.5);
}

.back-to-top.show {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

/* ========== Responsive ========== */
@media (max-width: 768px) {
  .header-nav { display: none; }
  
  .header-container { height: 60px; }
  .logo-text { font-size: 17px; }
  
  .hero-section { padding: 100px 16px 60px; }
  
  .hero-container {
    grid-template-columns: 1fr;
    gap: 30px;
  }
  
  .hero-badge {
    padding: 8px 20px;
    font-size: 14px;
  }
  
  .hero-title { 
    font-size: 36px; 
    line-height: 1.3;
    letter-spacing: -1px;
  }
  
  .hero-subtitle { 
    font-size: 17px; 
    line-height: 1.7;
  }
  
  .hero-actions {
    flex-direction: column;
    gap: 12px;
  }
  
  .hero-stats { 
    flex-direction: column; 
    gap: 16px; 
  }
  .stat-divider { display: none; }
  .stat-value { font-size: 32px; }
  .stat-label { font-size: 15px; }
  
  .hero-image {
    height: auto;
    padding: 20px 0;
  }
  
  .image-card {
    position: relative;
    top: auto;
    left: auto;
    right: auto;
    bottom: auto;
    margin-bottom: 16px;
    padding: 20px;
    flex-direction: row;
    align-items: center;
    gap: 16px;
  }
  
  .card-icon {
    width: 60px;
    height: 60px;
    flex-shrink: 0;
  }
  
  .card-title { font-size: 17px; }
  .card-desc { font-size: 14px; }
  
  .card-2, .card-3 {
    position: relative;
    top: auto;
    right: auto;
    bottom: auto;
    transform: none;
  }
  
  .image-card:first-child {
    position: relative;
    top: auto;
    left: auto;
  }
  
  .floating-dots { display: none; }
  
  .section-header { padding-bottom: 24px; }
  .section-title { 
    font-size: 28px; 
    letter-spacing: -0.5px;
  }
  .section-desc { font-size: 16px; }
  
  .features-grid, .users-grid { grid-template-columns: 1fr; }
  
  .feature-card {
    padding: 28px 24px;
    border-radius: 16px;
  }
  
  .feature-title { font-size: 18px; }
  .feature-desc { font-size: 15px; }
  
  .user-card { padding: 24px; }
  .user-name { font-size: 18px; }
  .user-role { font-size: 14px; }
  .user-desc { font-size: 15px; }
  
  .timeline::before { left: 30px; }
  .timeline-dot { left: 30px; }
  .timeline-item { flex-direction: column !important; align-items: flex-start; }
  .timeline-content { width: 100%; padding-left: 60px; }
  .timeline-icon { display: none; }
  
  .timeline-time { font-size: 16px; }
  .timeline-title { font-size: 18px; }
  .timeline-desc { font-size: 15px; }
  
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  
  .stat-card { padding: 24px; }
  .stat-card-value { font-size: 36px; }
  .stat-card-label { font-size: 14px; }
  
  .testimonials-grid { grid-template-columns: 1fr; }
  .testimonial-card { padding: 24px; }
  
  .testimonial-content { font-size: 16px; }
  .author-name { font-size: 17px; }
  
  .security-grid { grid-template-columns: 1fr; }
  .security-card { padding: 24px; }
  
  .security-title { font-size: 17px; }
  .security-desc { font-size: 15px; }
  
  .cta-title { font-size: 26px; }
  .cta-desc { font-size: 16px; }
  
  .footer-content { grid-template-columns: 1fr; }
  .footer-links { grid-template-columns: 1fr; }
  
  .link-title { font-size: 17px; }
  .footer-link-group a { font-size: 15px; }
  
  .btn {
    padding: 14px 28px;
    font-size: 16px;
    min-height: 52px;
    min-width: 140px;
    border-radius: 12px;
  }
  
  .btn-lg {
    padding: 18px 40px;
    font-size: 18px;
    min-height: 60px;
    min-width: 180px;
  }
  
  .back-to-top {
    width: 56px;
    height: 56px;
    bottom: 24px;
    right: 24px;
  }
  
  .copyright { font-size: 14px; }
}
</style>

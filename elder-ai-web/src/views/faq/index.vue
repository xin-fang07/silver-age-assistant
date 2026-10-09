<template>
  <div class="faq-page">
    <div class="faq-header">
      <div class="faq-container">
        <div class="faq-title-section">
          <h1 class="faq-title">常见问题</h1>
          <p class="faq-desc">解答您最关心的问题，让您安心使用银发智能生活助手</p>
        </div>
        <button class="faq-back-btn" @click="goBack">返回首页</button>
      </div>
    </div>

    <div class="faq-content">
      <div class="faq-container">
        <div class="faq-section">
          <h2 class="faq-section-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L2 7l10 5 10-5-10-5z" fill="#8b5cf6"/>
              <path d="M2 17l10 5 10-5" stroke="#8b5cf6" stroke-width="2" fill="none"/>
              <path d="M2 12l10 5 10-5" stroke="#8b5cf6" stroke-width="2" fill="none"/>
            </svg>
            <span>关于产品</span>
          </h2>
          <div class="faq-item" v-for="(item, index) in productFaqs" :key="'product-' + index">
            <div class="faq-question" @click="toggleFaq('product-' + index)">
              <span class="faq-q-text">{{ item.question }}</span>
              <span class="faq-arrow" :class="{ open: openedFaqs.includes('product-' + index) }">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <path d="M7 10l5 5 5-5" stroke="#4f46e5" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
            </div>
            <div class="faq-answer" v-show="openedFaqs.includes('product-' + index)">
              {{ item.answer }}
            </div>
          </div>
        </div>

        <div class="faq-section">
          <h2 class="faq-section-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" stroke="#f59e0b" stroke-width="2" fill="none"/>
            </svg>
            <span>使用方式</span>
          </h2>
          <div class="faq-item" v-for="(item, index) in usageFaqs" :key="'usage-' + index">
            <div class="faq-question" @click="toggleFaq('usage-' + index)">
              <span class="faq-q-text">{{ item.question }}</span>
              <span class="faq-arrow" :class="{ open: openedFaqs.includes('usage-' + index) }">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <path d="M7 10l5 5 5-5" stroke="#4f46e5" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
            </div>
            <div class="faq-answer" v-show="openedFaqs.includes('usage-' + index)">
              {{ item.answer }}
            </div>
          </div>
        </div>

        <div class="faq-section">
          <h2 class="faq-section-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z" fill="#10b981"/>
            </svg>
            <span>安全隐私</span>
          </h2>
          <div class="faq-item" v-for="(item, index) in securityFaqs" :key="'security-' + index">
            <div class="faq-question" @click="toggleFaq('security-' + index)">
              <span class="faq-q-text">{{ item.question }}</span>
              <span class="faq-arrow" :class="{ open: openedFaqs.includes('security-' + index) }">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <path d="M7 10l5 5 5-5" stroke="#4f46e5" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
            </div>
            <div class="faq-answer" v-show="openedFaqs.includes('security-' + index)">
              {{ item.answer }}
            </div>
          </div>
        </div>

        <div class="faq-section">
          <h2 class="faq-section-title">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="#ef4444"/>
            </svg>
            <span>费用说明</span>
          </h2>
          <div class="faq-item" v-for="(item, index) in pricingFaqs" :key="'pricing-' + index">
            <div class="faq-question" @click="toggleFaq('pricing-' + index)">
              <span class="faq-q-text">{{ item.question }}</span>
              <span class="faq-arrow" :class="{ open: openedFaqs.includes('pricing-' + index) }">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
                  <path d="M7 10l5 5 5-5" stroke="#4f46e5" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </span>
            </div>
            <div class="faq-answer" v-show="openedFaqs.includes('pricing-' + index)">
              {{ item.answer }}
            </div>
          </div>
        </div>

        <div class="faq-contact">
          <div class="faq-contact-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" stroke="#4f46e5" stroke-width="2" stroke-linecap="round"/>
              <circle cx="12" cy="7" r="4" stroke="#4f46e5" stroke-width="2"/>
            </svg>
          </div>
          <h3 class="faq-contact-title">还有其他问题？</h3>
          <p class="faq-contact-desc">我们的客服团队随时为您解答</p>
          <div class="faq-contact-actions">
            <a href="/contact" class="faq-contact-btn">联系我们</a>
            <span class="faq-contact-phone">服务热线：400-888-9999</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const openedFaqs = ref([])

const toggleFaq = (id) => {
  const index = openedFaqs.value.indexOf(id)
  if (index > -1) {
    openedFaqs.value.splice(index, 1)
  } else {
    openedFaqs.value.push(id)
  }
}

const goBack = () => {
  router.push('/landing')
}

const productFaqs = [
  {
    question: '银发智能生活助手是什么？',
    answer: '银发智能生活助手是专为老年人打造的智能陪伴平台，集成AI问答、健康管理、生活提醒、紧急求助等功能，帮助老人更安心、便捷地生活。'
  },
  {
    question: '需要智能手机才能使用吗？',
    answer: '是的，目前需要使用智能手机安装浏览器访问。我们的界面采用大字体、大按钮设计，非常适合老年人操作。未来我们也计划推出适老化智能终端设备。'
  },
  {
    question: '老人不会使用智能手机怎么办？',
    answer: '我们的产品设计充分考虑了老年人的使用习惯，操作简单直观。子女可以协助老人完成首次注册和基本设置，之后老人只需简单点击就能使用各项功能。我们还提供详细的使用指南和在线客服支持。'
  },
  {
    question: '可以同时为多个老人使用吗？',
    answer: '可以的。每个老人需要单独注册账号，子女可以通过"家属管理"功能绑定多个老人账号，方便统一查看和管理。'
  }
]

const usageFaqs = [
  {
    question: '如何注册和登录？',
    answer: '打开网站首页，点击"立即体验"进入注册页面，填写手机号、密码等信息即可完成注册。注册成功后使用手机号和密码登录。子女可以帮助老人完成注册。'
  },
  {
    question: 'AI智能问答可以问哪些问题？',
    answer: 'AI助手可以回答各种生活问题，包括健康咨询、天气查询、新闻资讯、生活常识、烹饪食谱、用药提醒等。支持语音输入，老人可以直接说话提问。'
  },
  {
    question: '如何记录健康数据？',
    answer: '进入"健康管理"页面，点击"添加健康记录"，选择要记录的项目（血压、血糖、体重等），输入数值即可。系统会自动生成健康趋势图表。'
  },
  {
    question: '紧急求助功能如何使用？',
    answer: '在首页点击"紧急求助"按钮，系统会立即向预设的紧急联系人发送求助通知，包含老人的位置信息。建议老人提前设置好紧急联系人。'
  }
]

const securityFaqs = [
  {
    question: '老人的数据安全吗？',
    answer: '非常安全。我们采用银行级加密技术保护用户数据，所有数据存储在安全的服务器上，严格遵守数据隐私保护法规。只有用户本人和绑定的家属可以查看数据。'
  },
  {
    question: '定位功能会泄露隐私吗？',
    answer: '定位功能仅在紧急求助时触发，或者用户主动查看位置时使用。平时不会持续定位，也不会向第三方分享位置信息。定位权限需要用户手动授权。'
  },
  {
    question: '如何保护账号安全？',
    answer: '建议设置强密码，并定期更换。开启"忘记密码"功能可以通过邮箱找回密码。绑定家属账号后，家属可以协助管理账号安全。'
  },
  {
    question: '客服能看到我的聊天记录吗？',
    answer: '为了提供更好的服务，客服在处理问题时可能会查看相关的聊天记录，但严格遵守隐私保护规定，不会泄露给第三方。'
  }
]

const pricingFaqs = [
  {
    question: '使用这个软件需要付费吗？',
    answer: '基础功能完全免费！包括AI智能问答、健康记录、生活提醒、紧急求助等核心功能都是免费使用的。'
  },
  {
    question: '有没有付费功能？',
    answer: '目前所有功能都是免费的。未来我们可能会推出一些增值服务，届时会提前通知用户。'
  },
  {
    question: '需要购买额外的设备吗？',
    answer: '不需要。只要有智能手机就能使用所有功能。如果老人没有智能手机，可以由子女协助在子女的手机上注册使用。'
  },
  {
    question: '数据导出需要付费吗？',
    answer: '不需要。健康记录和聊天记录导出功能都是免费的，可以导出为Excel或PDF格式。'
  }
]
</script>

<style scoped>
.faq-page {
  min-height: 100vh;
  background: #f8fafc;
}

.faq-header {
  background: linear-gradient(135deg, #4f46e5 0%, #8b5cf6 100%);
  padding: 48px 20px;
  color: white;
}

.faq-container {
  max-width: 800px;
  margin: 0 auto;
}

.faq-title-section {
  text-align: center;
}

.faq-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 12px;
}

.faq-desc {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.faq-back-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 10px;
  padding: 10px 24px;
  font-size: 15px;
  cursor: pointer;
  margin-top: 20px;
  transition: all 0.3s;
}

.faq-back-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.faq-content {
  padding: 48px 20px;
}

.faq-section {
  margin-bottom: 32px;
}

.faq-section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 20px;
}

.faq-item {
  background: white;
  border-radius: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.faq-question {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  cursor: pointer;
  transition: background 0.3s;
}

.faq-question:hover {
  background: #f8fafc;
}

.faq-q-text {
  font-size: 16px;
  font-weight: 500;
  color: #334155;
  flex: 1;
}

.faq-arrow {
  transition: transform 0.3s;
  flex-shrink: 0;
}

.faq-arrow.open {
  transform: rotate(180deg);
}

.faq-answer {
  padding: 0 24px 20px;
  color: #64748b;
  font-size: 15px;
  line-height: 1.7;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.faq-contact {
  background: linear-gradient(135deg, #f0f0ff 0%, #fef3ff 100%);
  border-radius: 16px;
  padding: 40px;
  text-align: center;
  margin-top: 40px;
}

.faq-contact-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  box-shadow: 0 4px 16px rgba(79, 70, 229, 0.1);
}

.faq-contact-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px;
}

.faq-contact-desc {
  font-size: 15px;
  color: #64748b;
  margin: 0 0 24px;
}

.faq-contact-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  flex-wrap: wrap;
}

.faq-contact-btn {
  background: linear-gradient(135deg, #4f46e5, #8b5cf6);
  color: white;
  text-decoration: none;
  padding: 12px 32px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s;
}

.faq-contact-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.4);
}

.faq-contact-phone {
  font-size: 15px;
  color: #4f46e5;
  font-weight: 500;
}

@media (max-width: 768px) {
  .faq-header { padding: 32px 16px; }
  .faq-title { font-size: 28px; }
  .faq-content { padding: 32px 16px; }
  .faq-section-title { font-size: 18px; }
  .faq-q-text { font-size: 15px; }
  .faq-answer { font-size: 14px; }
  .faq-contact { padding: 24px; }
}
</style>
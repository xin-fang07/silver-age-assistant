<template>
  <div class="contact-page">
    <div class="contact-container">
      <div class="contact-header">
        <h1 class="contact-title">联系我们</h1>
        <p class="contact-desc">有问题？我们随时为您解答</p>
      </div>

      <div class="contact-form-wrapper">
        <form class="contact-form" @submit.prevent="handleContactSubmit">
          <div class="form-row">
            <div class="form-group">
              <label for="name">您的姓名</label>
              <input type="text" id="name" v-model="contactForm.name" placeholder="请输入姓名" required>
            </div>
            <div class="form-group">
              <label for="phone">联系电话</label>
              <input type="tel" id="phone" v-model="contactForm.phone" placeholder="请输入电话" required>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="email">电子邮箱</label>
              <input type="email" id="email" v-model="contactForm.email" placeholder="请输入邮箱">
            </div>
            <div class="form-group">
              <label for="subject">咨询类型</label>
              <select id="subject" v-model="contactForm.subject" required>
                <option value="">请选择咨询类型</option>
                <option value="product">产品咨询</option>
                <option value="technical">技术支持</option>
                <option value="cooperation">商务合作</option>
                <option value="other">其他问题</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label for="message">留言内容</label>
            <textarea id="message" v-model="contactForm.message" placeholder="请描述您的问题或需求..." rows="5" required></textarea>
          </div>
          <button type="submit" class="submit-btn" :disabled="isSubmitting">
            <span v-if="!isSubmitting">提交留言</span>
            <span v-else>提交中...</span>
          </button>
        </form>

        <div class="contact-info">
          <div class="info-item">
            <div class="info-icon"><MapPin :size="24" /></div>
            <div class="info-content">
              <span class="info-title">公司地址</span>
              <span class="info-text">杭州市西湖区科技大道88号银发大厦</span>
            </div>
          </div>
          <div class="info-item">
            <div class="info-icon"><Phone :size="24" /></div>
            <div class="info-content">
              <span class="info-title">服务热线</span>
              <span class="info-text">400-888-9999（7x24小时）</span>
            </div>
          </div>
          <div class="info-item">
            <div class="info-icon"><Mail :size="24" /></div>
            <div class="info-content">
              <span class="info-title">电子邮箱</span>
              <span class="info-text">support@elderai.com</span>
            </div>
          </div>
        </div>
      </div>

      <div class="contact-actions">
        <button class="contact-back-btn" @click="goBack">返回首页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import MapPin from '@/components/icons/MapPin.vue'
import Phone from '@/components/icons/Phone.vue'
import Mail from '@/components/icons/Mail.vue'
import { contactApi } from '@/api/index'

const router = useRouter()

const contactForm = ref({
  name: '',
  phone: '',
  email: '',
  subject: '',
  message: ''
})

const isSubmitting = ref(false)

const handleContactSubmit = async () => {
  if (isSubmitting.value) return
  isSubmitting.value = true
  try {
    const res = await contactApi.submit(contactForm.value)
    alert(`${res.message || '留言提交成功'}！留言编号：${res.data?.id || '-'}，我们会尽快处理。`)
    contactForm.value = { name: '', phone: '', email: '', subject: '', message: '' }
  } catch (error) {
    console.error('留言提交失败：', error)
  } finally {
    isSubmitting.value = false
  }
}

const goBack = () => {
  router.push('/landing')
}
</script>

<style scoped>
.contact-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 32px 16px;
  box-sizing: border-box;
}

.contact-container {
  max-width: 900px;
  margin: 0 auto;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(79, 70, 229, 0.08);
  padding: 48px;
  box-sizing: border-box;
}

.contact-header {
  text-align: center;
  margin-bottom: 40px;
}

.contact-title {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 12px;
}

.contact-desc {
  font-size: 16px;
  color: #64748b;
  margin: 0;
}

.contact-form-wrapper {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 40px;
}

.contact-form {
  padding: 0;
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
  padding: 14px 24px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  background: linear-gradient(135deg, #4f46e5, #8b5cf6);
  color: white;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(79, 70, 229, 0.4);
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
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

.contact-actions {
  text-align: center;
  margin-top: 32px;
}

.contact-back-btn {
  background: transparent;
  color: #4f46e5;
  border: 1px solid #4f46e5;
  border-radius: 10px;
  padding: 12px 48px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.contact-back-btn:hover {
  background: #4f46e5;
  color: white;
}

@media (max-width: 768px) {
  .contact-container { padding: 24px; }
  .contact-title { font-size: 24px; }
  .contact-form-wrapper { grid-template-columns: 1fr; }
  .form-row { grid-template-columns: 1fr; }
}
</style>

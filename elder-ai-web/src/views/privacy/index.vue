<template>
  <!-- ============================================================ -->
  <!-- privacy/index.vue - 隐私政策与用户协议页                     -->
  <!-- 合规基线：收集个人信息（手机号/健康数据/紧急联系人）前公示   -->
  <!-- 对应《个人信息保护法》《人工智能拟人化互动服务管理暂行办法》 -->
  <!-- ============================================================ -->
  <div class="privacy-page">
    <div class="privacy-container">
      <!-- 顶部标题 -->
      <div class="privacy-header">
        <h1 class="privacy-title">隐私政策与用户协议</h1>
        <p class="privacy-update">更新日期：2026 年 7 月 17 日 · 生效日期：2026 年 7 月 17 日</p>
      </div>

      <div class="privacy-body">
        <p class="privacy-intro">
          欢迎使用<b>银发智能生活助手</b>（以下简称"本系统"）。我们非常重视您的个人信息与隐私保护。
          本政策将帮助您了解我们如何收集、使用、存储和保护您的个人信息，以及您享有的权利。
          请您在使用本系统前仔细阅读并充分理解本政策。<b>勾选"同意"即表示您已阅读并接受本政策全部内容。</b>
        </p>

        <h2 class="privacy-h2">一、我们收集的信息</h2>
        <p>为向您提供健康管理、生活提醒、紧急求助等服务，我们会在您使用时收集以下信息：</p>
        <ul class="privacy-ul">
          <li><b>账户信息：</b>用户名、密码（加密存储）、手机号、昵称、头像。</li>
          <li><b>健康数据：</b>您主动录入的血压、血糖、心率等健康指标及既往病史。</li>
          <li><b>紧急联系人：</b>您设置的紧急联系人姓名、电话、邮箱，仅用于紧急求助通知。</li>
          <li><b>交互记录：</b>与 AI 助手的问答记录、提醒事项、资讯浏览记录。</li>
          <li><b>位置信息：</b>仅在您使用天气功能并授权时获取，用于提供本地天气，不会留存。</li>
        </ul>

        <h2 class="privacy-h2">二、我们如何使用信息</h2>
        <ul class="privacy-ul">
          <li>提供健康记录管理、健康评分与 AI 健康建议参考。</li>
          <li>在您发起紧急求助（SOS）时，向您设置的紧急联系人发送通知邮件。</li>
          <li>提供智能问答、语音交互、生活提醒、天气与资讯服务。</li>
          <li>保障账户与系统安全，改进产品体验。</li>
        </ul>

        <h2 class="privacy-h2">三、AI 服务特别说明</h2>
        <p>
          本系统的智能问答、语音助手、健康建议均由<b>人工智能生成</b>，属于工具性辅助服务，
          <b>并非真人，也不构成医疗、法律等专业建议</b>。AI 生成内容仅供参考，
          如涉及健康、身体不适等重要事项，请务必咨询专业医师或及时就医。
          本系统不会将 AI 塑造为虚拟亲属或情感陪伴对象。
        </p>

        <h2 class="privacy-h2">四、信息的存储与安全</h2>
        <ul class="privacy-ul">
          <li>您的密码采用 BCrypt 加密存储，任何人（包括管理员）都无法查看明文。</li>
          <li>数据存储于受保护的数据库中，我们采取合理的技术与管理措施防止信息泄露。</li>
          <li>我们仅在为您提供服务所必需的期限内保存您的个人信息。</li>
        </ul>

        <h2 class="privacy-h2">五、您的权利</h2>
        <ul class="privacy-ul">
          <li><b>查询与更正：</b>您可在"我的"页面查看并修改个人资料。</li>
          <li><b>导出：</b>您可导出与 AI 的对话记录。</li>
          <li><b>删除与注销：</b>您有权删除个人数据或注销账户；注销后我们将删除或匿名化您的个人信息。</li>
        </ul>

        <h2 class="privacy-h2">六、老年人与未成年人保护</h2>
        <p>
          本系统主要面向老年人群体，我们在界面设计（大字体、语音交互）与内容上充分考虑适老化需求，
          并在健康、紧急场景提供安全提示与风险引导。本系统不面向未成年人提供服务。
        </p>

        <h2 class="privacy-h2">七、信息共享</h2>
        <p>
          除以下情形外，我们不会向第三方共享您的个人信息：
          （1）为实现紧急求助，向<b>您本人设置的紧急联系人</b>发送必要的求助信息；
          （2）获得您的明确同意；（3）法律法规要求。
        </p>

        <h2 class="privacy-h2">八、政策更新与联系</h2>
        <p>
          我们可能适时更新本政策，重大变更将以显著方式通知您。
          如您对本政策有任何疑问、意见或投诉，可通过系统内"我的"页面反馈。
        </p>

        <p class="privacy-footer-note">
          本系统为毕业设计演示项目，仅用于学习与研究目的，不用于商业运营。
        </p>
      </div>

      <!-- 返回按钮 -->
      <div class="privacy-actions">
        <button class="privacy-back-btn" @click="goBack">返回</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/login')
  }
}
</script>

<style scoped>
.privacy-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #eef4ff 0%, #f7faff 100%);
  padding: 32px 16px;
  box-sizing: border-box;
}

.privacy-container {
  max-width: 800px;
  margin: 0 auto;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(58, 91, 191, 0.08);
  padding: 40px 44px;
  box-sizing: border-box;
}

.privacy-header {
  text-align: center;
  border-bottom: 2px solid #eef2f9;
  padding-bottom: 20px;
  margin-bottom: 24px;
}

.privacy-title {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 10px;
}

.privacy-update {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.privacy-body {
  font-size: 16px;
  line-height: 1.9;
  color: #3a3f47;
}

.privacy-intro {
  background: #f7faff;
  border-left: 4px solid #3a5bbf;
  padding: 14px 18px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.privacy-h2 {
  font-size: 19px;
  font-weight: 700;
  color: #3a5bbf;
  margin: 28px 0 12px;
}

.privacy-ul {
  padding-left: 22px;
  margin: 10px 0;
}

.privacy-ul li {
  margin-bottom: 8px;
}

.privacy-footer-note {
  margin-top: 28px;
  padding-top: 16px;
  border-top: 1px dashed #e0e4ec;
  color: #909399;
  font-size: 14px;
}

.privacy-actions {
  text-align: center;
  margin-top: 32px;
}

.privacy-back-btn {
  background: #3a5bbf;
  color: #fff;
  border: none;
  border-radius: 10px;
  padding: 12px 48px;
  font-size: 17px;
  cursor: pointer;
  transition: background 0.2s;
}

.privacy-back-btn:hover {
  background: #2f4aa0;
}

@media (max-width: 640px) {
  .privacy-container { padding: 24px 20px; }
  .privacy-title { font-size: 23px; }
  .privacy-body { font-size: 15px; }
}
</style>

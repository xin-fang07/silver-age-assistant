<!--
  mine/index.vue - 我的页面
  银发智能生活助手 - 家属端个人中心
-->
<template>
  <div class="mine-page">
    <!-- 用户信息卡片 -->
    <div class="mine-card">
      <div class="mine-avatar-wrapper" @click="showEditProfile = true">
        <img v-if="avatarUrl" :src="avatarUrl" class="mine-avatar-img" loading="lazy" />
        <div v-else class="mine-avatar">{{ avatarText }}</div>
        <div class="avatar-edit-icon"><el-icon><Camera /></el-icon></div>
      </div>
      <div class="mine-name">{{ userName }}</div>
      <div class="mine-info-row">
        <span v-if="phone" class="mine-tag"><el-icon :size="14"><Phone /></el-icon> {{ phone }}</span>
      </div>
      <div class="edit-profile-btn" @click="showEditProfile = true">
        <el-icon><Edit /></el-icon> 编辑资料
      </div>
    </div>

    <!-- 以下卡片（BMI/紧急联系人）属于老人档案，已从家属个人资料页移除 -->

    <div v-if="familyLoadError" class="section-card data-error" role="alert">
      <strong>家属绑定信息加载失败</strong><span>{{ familyLoadError }}</span>
      <el-button type="primary" size="large" @click="retryFamilyData">重新加载</el-button>
    </div>

    <!-- 老人管理卡片 -->
    <div class="section-card" v-if="elderList.length">
      <div class="sc-title"><el-icon><UserFilled /></el-icon> 绑定的老人（{{ elderList.length }}）</div>
      <div class="sc-body family-list">
        <div class="family-item" v-for="e in elderList" :key="e.elderInfoId">
          <div class="fi-info">
            <span class="fi-name">{{ e.username }}</span>
            <span class="fi-relation">{{ e.relation }}</span>
          </div>
          <el-button size="small" @click="go('/care/elder/' + e.elderInfoId)">详情</el-button>
          <el-button class="unbind-btn" type="danger" size="small" @click="removeElder(e)">解绑</el-button>
        </div>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-area">
      <el-button class="logout-btn" @click="handleLogout" size="large">
        <el-icon><SwitchButton /></el-icon> 退出登录
      </el-button>
    </div>

    <!-- 个人资料编辑弹窗 -->
    <el-dialog v-model="showEditProfile" title="编辑个人资料" width="500px" :close-on-click-modal="false" top="10vh">
      <el-form :model="profileForm" label-width="100px" label-position="top">
        <div class="avatar-upload-area">
          <div class="avatar-preview" @click="triggerAvatarUpload">
            <img v-if="profileForm.avatar" :src="profileForm.avatar" class="preview-img" loading="lazy" />
            <div v-else class="preview-placeholder">
              <el-icon :size="32"><User /></el-icon>
              <span>点击上传头像</span>
            </div>
          </div>
          <input type="file" ref="avatarInput" class="avatar-input" accept="image/*" @change="handleAvatarChange" />
          <p class="avatar-tip">支持 jpg、png、gif 格式，大小不超过 5MB</p>
        </div>

        <el-form-item label="昵称">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" size="large" />
        </el-form-item>

        <el-form-item label="手机号码">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号码" size="large" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditProfile = false" size="large">取消</el-button>
        <el-button type="primary" @click="handleProfileSubmit" size="large">保存修改</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUser, removeToken, removeUser } from '@/utils/auth'
import { userApi, familyApi } from '@/api/index'

const router = useRouter()
const user = getUser()
const userName = ref(user?.nickname || user?.username || '家属用户')
const phone = ref(user?.phone || '')
const avatarUrl = ref(user?.avatar || '')

const avatarText = computed(() => userName.value?.charAt(0) || '老')

const showEditProfile = ref(false)
const elderList = ref([])
const familyLoadError = ref('')

const profileForm = reactive({
  avatar: '',
  nickname: '',
  phone: ''
})

const avatarInput = ref(null)

const go = (p) => router.push(p)

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
    confirmButtonText: '确定退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    removeToken()
    removeUser()
    router.push('/login')
  }).catch(() => {})
}

const triggerAvatarUpload = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = async (e) => {
  const file = e.target.files?.[0]
  if (!file) return

  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('文件大小不能超过 5MB')
    return
  }

  try {
    const res = await userApi.uploadAvatar(file)
    if (res.data?.url) {
      profileForm.avatar = res.data.url
      ElMessage.success('头像上传成功')
    }
  } catch (e) {
    ElMessage.error('头像上传失败')
  }
}

const handleProfileSubmit = async () => {
  try {
    if (profileForm.avatar || profileForm.nickname || profileForm.phone) {
      await userApi.updateProfile({
        avatar: profileForm.avatar || undefined,
        nickname: profileForm.nickname || undefined,
        phone: profileForm.phone || undefined
      })
    }

    ElMessage.success('个人资料更新成功')
    showEditProfile.value = false
    loadUserInfo()
  } catch (e) {
    ElMessage.error('更新失败，请重试')
  }
}

const loadUserInfo = async () => {
  try {
    const res = await userApi.getUserInfo()
    if (res.data) {
      const u = res.data.user || {}

      if (u.avatar) avatarUrl.value = u.avatar
      if (u.nickname) userName.value = u.nickname
      if (u.phone) phone.value = u.phone
    }
  } catch (e) { /* ignore */ }
}

const initProfileForm = () => {
  profileForm.avatar = avatarUrl.value
  profileForm.nickname = userName.value
  profileForm.phone = phone.value
}

const loadElders = async () => {
  try {
    const res = await familyApi.myElders()
    elderList.value = res.data || []
  } catch (e) { familyLoadError.value = e?.message || '无法获取已绑定老人' }
}

const retryFamilyData = async () => {
  familyLoadError.value = ''
  await loadElders()
}

const removeElder = async (e) => {
  try {
    await ElMessageBox.confirm(
      `确定解绑老人「${e.username}」吗？解绑后将无法查看该老人的健康数据。`,
      '解绑老人',
      { confirmButtonText: '解绑', cancelButtonText: '取消', type: 'warning', confirmButtonClass: 'el-button--danger' }
    )
    await familyApi.unbind({ elderInfoId: e.elderInfoId })
    ElMessage.success('已解绑')
    await loadElders()
  } catch (e) { }
}

onMounted(async () => {
  await loadUserInfo()
  await loadElders()
})

watch(showEditProfile, (val) => {
  if (val) {
    nextTick(() => {
      initProfileForm()
    })
  }
})
</script>

<style scoped>
.mine-page { padding: 0 0 24px; animation: fadeIn .4s ease; }
@keyframes fadeIn { from{opacity:0;transform:translateY(8px)} to{opacity:1;transform:translateY(0)} }

.mine-card { text-align: center; padding: 36px 0 20px; }

.mine-avatar-wrapper { position: relative; display: inline-block; cursor: pointer; }
.mine-avatar { width: 80px; height: 80px; border-radius: 50%; background: linear-gradient(135deg, #4dabf7, #1c7ed6); color: #fff; font-size: 34px; font-weight: 700; display: flex; align-items: center; justify-content: center; margin: 0 auto 14px; box-shadow: 0 8px 24px rgba(28,126,214,.35); }
.mine-avatar-img { width: 80px; height: 80px; border-radius: 50%; object-fit: cover; box-shadow: 0 8px 24px rgba(28,126,214,.35); }
.avatar-edit-icon { position: absolute; bottom: 0; right: 0; width: 28px; height: 28px; border-radius: 50%; background: #fff; border: 3px solid #4dabf7; display: flex; align-items: center; justify-content: center; color: #4dabf7; font-size: 14px; box-shadow: 0 2px 8px rgba(0,0,0,.15); }

.mine-name { font-size: 24px; font-weight: 700; color: #1e293b; margin-bottom: 8px; }
.mine-info-row { display: flex; justify-content: center; gap: 12px; flex-wrap: wrap; }
.mine-tag { font-size: 14px; color: #64748b; display: flex; align-items: center; gap: 4px; background: #f1f5f9; padding: 4px 12px; border-radius: 20px; }

.edit-profile-btn { display: inline-flex; align-items: center; gap: 6px; margin-top: 12px; padding: 8px 20px; background: #e3f0ff; color: #2563eb; border-radius: 20px; font-size: 14px; cursor: pointer; transition: all .2s; }
.edit-profile-btn:hover { background: #dbeafe; }

.section-card { background: #fff; border-radius: 16px; padding: 16px 18px; margin: 0 auto 16px; max-width: 520px; box-shadow: 0 2px 10px rgba(0,0,0,.05); }
.sc-title { font-size: 16px; font-weight: 600; color: #1e293b; display: flex; align-items: center; gap: 6px; margin-bottom: 10px; }
.sc-body p { font-size: 16px; color: #334155; margin: 4px 0; }

.menu-item { display: flex; align-items: center; gap: 12px; padding: 16px 18px; font-size: 17px; color: #1e293b; cursor: pointer; transition: background .15s; border-bottom: 1px solid #f1f5f9; }
.menu-item:last-child { border-bottom: none; }
.menu-item:hover, .menu-item:active { background: #f8fafc; }
.menu-item span:nth-child(2) { flex: 1; }
.mi-icon { width: 34px; height: 34px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.mi-blue { background: #e3f0ff; color: #2563eb; }
.mi-green { background: #d1fae5; color: #059669; }
.mi-orange { background: #fff7ed; color: #d97706; }
.mi-purple { background: #f3e8ff; color: #7c3aed; }
.mi-teal { background: #ccfbf1; color: #0d9488; }
.mi-arrow { color: #94a3b8; flex-shrink: 0; }

.logout-area { text-align: center; padding: 8px 0 32px; }
.logout-btn { min-width: 200px; color: #64748b; background: #f8fafc; border-color: #e2e8f0; }

.avatar-upload-area { text-align: center; margin-bottom: 20px; }
.avatar-preview { width: 120px; height: 120px; border-radius: 50%; border: 2px dashed #e2e8f0; display: flex; align-items: center; justify-content: center; margin: 0 auto; cursor: pointer; transition: all .2s; }
.avatar-preview:hover { border-color: #4dabf7; background: #f8fafc; }
.preview-img { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; }
.preview-placeholder { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #94a3b8; }
.avatar-input { display: none; }
.avatar-tip { margin-top: 10px; font-size: 12px; color: #94a3b8; }

.family-list { display: flex; flex-direction: column; gap: 10px; }
.unbind-btn { color: #fff !important; background-color: #f56c6c !important; border-color: #f56c6c !important; }
.unbind-btn:hover { color: #fff !important; background-color: #f78989 !important; border-color: #f78989 !important; }
.family-item { display: flex; align-items: center; justify-content: space-between; padding: 10px 12px; background: #f8fafc; border-radius: 10px; }
.fi-info { display: flex; align-items: center; gap: 10px; }
.fi-name { font-size: 16px; color: #334155; font-weight: 600; }
.fi-relation { font-size: 12px; color: #fff; background: #4a90d9; padding: 2px 10px; border-radius: 20px; }
</style>

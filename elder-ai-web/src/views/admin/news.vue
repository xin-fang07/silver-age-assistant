<template>
  <div class="news-page">
    <header><h2>资讯发布管理</h2><p>支持草稿、预览、定时发布、下架和版本追踪</p></header>
    <div class="toolbar">
      <el-input v-model="keyword" clearable placeholder="搜索标题或正文" size="large" @keyup.enter="search" />
      <el-button type="primary" size="large" @click="openCreate">新增资讯</el-button>
    </div>

    <div class="card">
      <AsyncState :state="pageState" :error-message="loadError" empty-title="暂无资讯" @retry="load">
      <template #default>
        <el-table :data="list" stripe border>
          <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
          <el-table-column label="分类" width="120"><template #default="{row}">{{ typeText(row.newsType) }}</template></el-table-column>
          <el-table-column label="状态" width="110" align="center">
            <template #default="{row}"><el-tag :type="statusTag(row)">{{ statusText(row) }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="viewCount" label="浏览" width="80" align="center" />
          <el-table-column label="发布时间" width="175"><template #default="{row}">{{ formatTime(row.publishedAt || row.scheduledPublishTime) }}</template></el-table-column>
          <el-table-column label="操作" width="330" fixed="right">
            <template #default="{row}">
              <el-button @click="preview(row)">预览</el-button>
              <el-button type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button @click="showRevisions(row)">记录</el-button>
              <el-button v-if="row.status === 1" type="warning" @click="changeStatus(row,2)">下架</el-button>
              <el-button v-else type="success" @click="changeStatus(row,1)">发布</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination"><el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total"
          :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next" @current-change="load" @size-change="load" /></div>
      </template>
      </AsyncState>
    </div>

    <el-dialog v-model="editorVisible" :title="form.id ? '编辑资讯' : '新增资讯'" width="820px" :close-on-click-modal="false">
      <el-alert v-if="autoSaveText" :title="autoSaveText" type="success" :closable="false" show-icon class="save-tip" />
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large">
        <el-form-item label="标题" prop="title"><el-input v-model="form.title" maxlength="100" show-word-limit /></el-form-item>
        <div class="two-col">
          <el-form-item label="分类" prop="newsType"><el-select v-model="form.newsType" style="width:100%">
            <el-option label="健康养生" value="HEALTH"/><el-option label="政策解读" value="POLICY"/><el-option label="社区活动" value="ACTIVITY"/>
          </el-select></el-form-item>
          <el-form-item label="发布方式"><el-select v-model="form.publishMode" style="width:100%">
            <el-option label="保存草稿" value="draft"/><el-option label="立即发布" value="now"/><el-option label="定时发布" value="scheduled"/>
          </el-select></el-form-item>
        </div>
        <el-form-item v-if="form.publishMode==='scheduled'" label="预约发布时间" prop="scheduledPublishTime">
          <el-date-picker v-model="form.scheduledPublishTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择未来时间" />
        </el-form-item>
        <el-form-item label="摘要"><el-input v-model="form.summary" type="textarea" :rows="2" maxlength="300" show-word-limit /></el-form-item>
        <el-form-item label="来源链接"><el-input v-model="form.sourceUrl" placeholder="https://..." /></el-form-item>
        <el-form-item label="封面图片（上传后自动居中裁剪为 16:9）">
          <el-upload :show-file-list="false" accept="image/*" :http-request="uploadCover">
            <el-button :loading="uploading">选择并裁剪图片</el-button>
          </el-upload>
          <img v-if="form.coverImage" :src="assetUrl(form.coverImage)" class="cover" alt="资讯封面预览" />
        </el-form-item>
        <el-form-item label="正文（支持 HTML）" prop="content"><el-input v-model="form.content" type="textarea" :rows="12" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button size="large" @click="openFormPreview">预览</el-button>
        <el-button size="large" @click="editorVisible=false">取消</el-button>
        <el-button type="primary" size="large" :loading="saving" @click="save(false)">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="资讯预览" width="760px"><article class="preview">
      <h1>{{ previewData.title }}</h1><p class="summary">{{ previewData.summary }}</p>
      <img v-if="previewData.coverImage" :src="assetUrl(previewData.coverImage)" alt="封面" />
      <iframe sandbox="" :srcdoc="previewData.content || '<p>暂无正文</p>'" title="资讯正文预览" />
      <a v-if="previewData.sourceUrl" :href="previewData.sourceUrl" target="_blank" rel="noopener">查看来源</a>
    </article></el-dialog>

    <el-drawer v-model="revisionVisible" title="最近 20 次修改记录" size="520px">
      <el-timeline><el-timeline-item v-for="item in revisions" :key="item.id" :timestamp="formatTime(item.createTime)">
        修改人 ID：{{ item.editorId }}<el-button link type="primary" @click="previewSnapshot(item)">查看当时内容</el-button>
      </el-timeline-item></el-timeline><el-empty v-if="!revisions.length" description="暂无修改记录" />
    </el-drawer>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AsyncState from '@/components/AsyncState.vue'
import { adminApi } from '@/api/index'
import { formatFriendlyTime } from '@/utils/friendlyTime'

const list=ref([]), loading=ref(false), page=ref(1), pageSize=ref(10), total=ref(0), keyword=ref('')
const pageState=ref('loading'),loadError=ref('')
const editorVisible=ref(false), previewVisible=ref(false), revisionVisible=ref(false), saving=ref(false), uploading=ref(false)
const formRef=ref(), revisions=ref([]), previewData=ref({}), autoSaveText=ref('')
const form=reactive({id:null,title:'',summary:'',newsType:'HEALTH',publishMode:'draft',scheduledPublishTime:null,sourceUrl:'',coverImage:'',content:''})
const rules={title:[{required:true,message:'请输入标题'}],newsType:[{required:true,message:'请选择分类'}],content:[{required:true,message:'请输入正文'}],scheduledPublishTime:[{validator:(_,v,cb)=>form.publishMode!=='scheduled'||v?cb():cb(new Error('请选择预约发布时间'))}]}
let autoSaveTimer

const load=async()=>{loading.value=true;pageState.value='loading';loadError.value='';try{const r=await adminApi.listNews({pageNum:page.value,pageSize:pageSize.value,keyword:keyword.value||undefined});list.value=r.data||[];total.value=Number(r.total||0);pageState.value=list.value.length?'success':'empty'}catch(e){pageState.value='error';loadError.value=e?.message||'加载资讯失败'}finally{loading.value=false}}
const search=()=>{page.value=1;load()}
const resetForm=()=>Object.assign(form,{id:null,title:'',summary:'',newsType:'HEALTH',publishMode:'draft',scheduledPublishTime:null,sourceUrl:'',coverImage:'',content:''})
const openCreate=()=>{resetForm();autoSaveText.value='';editorVisible.value=true}
const openEdit=row=>{Object.assign(form,row,{publishMode:row.scheduledPublishTime?'scheduled':row.status===1?'now':'draft'});autoSaveText.value='';editorVisible.value=true}
const payload=forcedDraft=>({title:form.title,summary:form.summary,newsType:form.newsType,content:form.content,coverImage:form.coverImage,sourceUrl:form.sourceUrl,status:forcedDraft?0:form.publishMode==='now'?1:0,scheduledPublishTime:forcedDraft?null:form.publishMode==='scheduled'?form.scheduledPublishTime:null})
const save=async auto=>{if(!auto){try{await formRef.value.validate()}catch{return}}else if(!form.title.trim()||!form.content.trim())return;saving.value=!auto;try{const r=form.id?await adminApi.updateNews(form.id,payload(auto)):await adminApi.createNews(payload(auto));form.id=r.data?.id||form.id;if(auto)autoSaveText.value=`草稿已自动保存：${new Date().toLocaleTimeString()}`;else{ElMessage.success('资讯已保存');editorVisible.value=false}await load()}catch(e){if(!auto)ElMessage.error('保存失败')}finally{saving.value=false}}
watch(form,()=>{if(!editorVisible.value)return;clearTimeout(autoSaveTimer);autoSaveTimer=setTimeout(()=>save(true),30000)},{deep:true})
onBeforeUnmount(()=>clearTimeout(autoSaveTimer))

const preview=d=>{previewData.value={...d};previewVisible.value=true}, openFormPreview=()=>preview(form)
const changeStatus=async(row,status)=>{await ElMessageBox.confirm(status===2?'下架后用户端将无法看到该资讯，确定继续吗？':'确定立即发布该资讯吗？','确认操作',{confirmButtonText:'确定',cancelButtonText:'取消',type:'warning'});await adminApi.changeNewsStatus(row.id,status);ElMessage.success(status===2?'已下架':'已发布');load()}
const showRevisions=async row=>{const r=await adminApi.newsRevisions(row.id);revisions.value=r.data||[];revisionVisible.value=true}
const previewSnapshot=item=>{try{preview(JSON.parse(item.snapshot))}catch{ElMessage.error('历史内容无法解析')}}

const cropImage=async file=>{const bitmap=await createImageBitmap(file);const ratio=16/9;let sx=0,sy=0,sw=bitmap.width,sh=bitmap.height;if(sw/sh>ratio){sw=sh*ratio;sx=(bitmap.width-sw)/2}else{sh=sw/ratio;sy=(bitmap.height-sh)/2}const canvas=document.createElement('canvas');canvas.width=1200;canvas.height=675;canvas.getContext('2d').drawImage(bitmap,sx,sy,sw,sh,0,0,1200,675);return new Promise(resolve=>canvas.toBlob(b=>resolve(new File([b],`cover-${Date.now()}.jpg`,{type:'image/jpeg'})),'image/jpeg',.88))}
const uploadCover=async({file})=>{uploading.value=true;try{const cropped=await cropImage(file);const r=await adminApi.uploadNewsImage(cropped);form.coverImage=r.data?.url||'';ElMessage.success('封面已上传并裁剪')}catch(e){ElMessage.error('图片上传失败')}finally{uploading.value=false}}
import { assetUrl } from '@/utils/asset'
const typeText=t=>({HEALTH:'健康养生',POLICY:'政策解读',ACTIVITY:'社区活动'}[t]||t||'-')
const statusText=r=>r.scheduledPublishTime?'待定时发布':r.status===1?'已发布':r.status===2?'已下架':'草稿'
const statusTag=r=>r.scheduledPublishTime?'warning':r.status===1?'success':r.status===2?'danger':'info'
const formatTime=formatFriendlyTime
onMounted(load)
</script>

<style scoped>
.news-page header{margin-bottom:22px}.news-page h2{font-size:22px;margin:0 0 6px;color:#1e293b}.news-page header p{font-size:17px;color:#64748b;margin:0}.toolbar,.card{background:#fff;border-radius:14px;padding:20px;margin-bottom:18px}.toolbar{display:flex;gap:14px}.toolbar .el-input{max-width:520px}.pagination{display:flex;justify-content:flex-end;margin-top:20px}.two-col{display:grid;grid-template-columns:1fr 1fr;gap:18px}.cover{display:block;width:320px;aspect-ratio:16/9;object-fit:cover;margin-top:12px;border-radius:10px}.save-tip{margin-bottom:15px}.preview h1{font-size:30px}.preview .summary{color:#64748b;font-size:18px}.preview img{width:100%;max-height:360px;object-fit:cover;border-radius:12px}.preview iframe{width:100%;min-height:360px;border:0;margin:16px 0;background:#fff}@media(max-width:760px){.two-col{grid-template-columns:1fr}.toolbar{flex-direction:column}}
</style>

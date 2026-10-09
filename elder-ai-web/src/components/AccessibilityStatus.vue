<template>
  <div>
    <transition name="network-slide">
      <div v-if="!online" class="network-bar" role="alert" aria-live="assertive">
        <span aria-hidden="true">⚠</span><span>网络已断开，暂时无法提交；已填写的内容不会被清空。</span>
        <button type="button" @click="retry">重新检查</button>
      </div>
    </transition>
    <p class="sr-only" aria-live="assertive" aria-atomic="true">{{ announcement }}</p>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
const online=ref(navigator.onLine),announcement=ref(''),router=useRouter();let observer,errorTimer
const announce=text=>{announcement.value='';nextTick(()=>announcement.value=text)}
const updateOnline=()=>{online.value=navigator.onLine;announce(online.value?'网络已恢复，可以继续使用。':'网络已断开，暂时无法提交。')}
const onFailure=()=>{online.value=false;announce('网络连接失败，请检查网络后重试。')}
const onRestored=()=>{if(!online.value){online.value=true;announce('网络已恢复，可以继续使用。')}}
const retry=()=>{online.value=navigator.onLine;announce(online.value?'网络已恢复，可以继续使用。':'仍未连接网络，请检查网络设置。')}
const clickables='.clickable,.kpi-card,.stat-card,.ai-card,.block-more,.recent-chat-item,.health-tile,.reminder-row,.news-card,.indicator-card,.fl-menu-item,.fl-collapse-btn,.weather,.weather-icon'

function enhanceTables(){
  document.querySelectorAll('.el-table').forEach(table=>{
    const labels=[...table.querySelectorAll('.el-table__header-wrapper th')].map(th=>th.innerText.trim())
    table.querySelectorAll('.el-table__body-wrapper tr').forEach(row=>[...row.querySelectorAll('td')].forEach((cell,index)=>cell.dataset.label=labels[index]||'内容'))
  })
}
function enhanceInteractive(){
  document.querySelectorAll(clickables).forEach(node=>{if(!['A','BUTTON','INPUT','SELECT','TEXTAREA'].includes(node.tagName)){node.setAttribute('tabindex',node.getAttribute('tabindex')||'0');node.setAttribute('role',node.getAttribute('role')||'button')}})
  document.querySelectorAll('button:not([aria-label])').forEach(button=>{const text=button.innerText.trim()||button.title;if(text)button.setAttribute('aria-label',text)})
  enhanceTables()
}
function observeChanges(){enhanceInteractive();clearTimeout(errorTimer);errorTimer=setTimeout(()=>{const errors=[...document.querySelectorAll('.el-form-item__error')].filter(n=>n.offsetParent!==null).map(n=>n.textContent?.trim()).filter(Boolean);if(!errors.length)return;const unique=[...new Set(errors)];announce(`表单有${unique.length}处需要修改：${unique.join('；')}`);document.querySelector('.el-form-item.is-error input,.el-form-item.is-error textarea,.el-form-item.is-error button')?.focus()},180)}
function keyActivate(event){if(!['Enter',' '].includes(event.key))return;const target=event.target.closest(clickables);if(!target||['A','BUTTON','INPUT','SELECT','TEXTAREA'].includes(target.tagName))return;event.preventDefault();target.click()}
function focusHeading(){nextTick(()=>{const target=document.querySelector('main h1,main h2,.el-main h1,.el-main h2,#main-content,#admin-main-content');if(target){target.setAttribute('tabindex','-1');target.focus({preventScroll:true})}})}
onMounted(()=>{window.addEventListener('online',updateOnline);window.addEventListener('offline',updateOnline);window.addEventListener('app-network-error',onFailure);window.addEventListener('app-network-restored',onRestored);observer=new MutationObserver(observeChanges);observer.observe(document.body,{subtree:true,childList:true,attributes:true,attributeFilter:['class']});document.addEventListener('keydown',keyActivate);enhanceInteractive()})
router.afterEach(focusHeading)
onUnmounted(()=>{window.removeEventListener('online',updateOnline);window.removeEventListener('offline',updateOnline);window.removeEventListener('app-network-error',onFailure);window.removeEventListener('app-network-restored',onRestored);observer?.disconnect();document.removeEventListener('keydown',keyActivate);clearTimeout(errorTimer)})
</script>

<style scoped>
.network-bar{position:fixed;left:50%;bottom:18px;transform:translateX(-50%);z-index:10001;display:flex;align-items:center;gap:12px;width:min(760px,calc(100% - 24px));padding:12px 18px;background:#7f1d1d;color:#fff;border-radius:14px;font-size:17px;font-weight:700;box-shadow:0 8px 24px rgba(0,0,0,.24)}.network-bar button{margin-left:auto;min-height:42px;padding:7px 16px;border:2px solid #fff;border-radius:10px;background:#fff;color:#7f1d1d;font-size:16px;font-weight:700}.network-slide-enter-active,.network-slide-leave-active{transition:transform .2s,opacity .2s}.network-slide-enter-from,.network-slide-leave-to{transform:translate(-50%,100%);opacity:0}@media(max-width:560px){.network-bar{align-items:flex-start;flex-wrap:wrap}.network-bar button{margin-left:28px}}
</style>

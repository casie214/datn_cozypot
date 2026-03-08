<script setup lang="ts">
import Sidebar from './components/sidebar.vue'
import Header from './components/adminHeader.vue'
import CommonNav from './components/commonNav.vue'
import { useRoute } from 'vue-router'
import { computed, watch, onMounted } from 'vue'

const route = useRoute()

const isAdminRoute = computed(() => {
  const path = route.path || ''
  return path.startsWith('/admin') || path.startsWith('/manage')
})

let botLoaded = false

// 1. Nạp Botpress (Giữ nguyên logic của bạn)
function loadBotpress() {
  if (botLoaded) return
  botLoaded = true

  const injectScript = document.createElement('script')
  injectScript.src = "https://cdn.botpress.cloud/webchat/v3.6/inject.js"
  injectScript.onload = () => {
    const configScript = document.createElement('script')
    configScript.src = "https://files.bpcontent.cloud/2026/03/08/05/20260308055825-NE1GWMIY.js"
    document.body.appendChild(configScript)
  }
  document.body.appendChild(injectScript)
}

// 2. Hàm điều khiển: Vừa gọi API, vừa can thiệp trực tiếp vào Container bao ngoài Shadow Root
function toggleBot(isAdmin: boolean) {
  const bot = (window as any).botpressWebChat

  if (isAdmin) {
    document.body.classList.add('hide-botpress-final')
    if (bot) {
      bot.sendEvent({ type: 'hide' })
      bot.sendConfig({ hideWidget: true })
    }
  } else {
    document.body.classList.remove('hide-botpress-final')
    if (bot) {
      bot.sendEvent({ type: 'show' })
      bot.sendConfig({ hideWidget: false })
    }
  }
}

watch(
  () => route.path,
  (path) => {
    const isAdmin = path.startsWith('/admin') || path.startsWith('/manage')
    if (!isAdmin) {
      loadBotpress()
      setTimeout(() => toggleBot(false), 500)
    } else {
      toggleBot(isAdmin)
    }
  },
  { immediate: true }
)

// 3. Chốt chặn cuối: Quét sạch các Container bạn thấy trong Inspector
onMounted(() => {
  setInterval(() => {
    if (isAdminRoute.value) {
      // Nhắm trực tiếp vào các class/id bao ngoài Shadow Root bạn vừa chụp
      const targets = [
        '.bpChatContainer',
        '#fab-root',
        'iframe[title="Web Chat"]',
        'bp-web-widget'
      ];

      targets.forEach(selector => {
        const el = document.querySelector(selector) as HTMLElement
        if (el) {
          el.style.display = 'none'
          el.style.visibility = 'hidden'
          el.style.opacity = '0'
        }
      })
    }
  }, 500)
})
</script>

<template>
  <div class="app-container">
    <Sidebar class="app-sidebar" v-if="isAdminRoute" />
    <div class="main-layout">
      <Header v-if="isAdminRoute"/>
      <CommonNav v-else/>

      <div class="app-content" :class="{ 'admin-content': isAdminRoute }">
        <router-view />
      </div>
    </div>
  </div>
</template>

<style>
/* CSS Nuke: Đè bẹp các container bao quanh Shadow Root */
body.hide-botpress-final .bpChatContainer,
body.hide-botpress-final #fab-root,
body.hide-botpress-final iframe[title="Web Chat"],
body.hide-botpress-final bp-web-widget {
  display: none !important;
  opacity: 0 !important;
  visibility: hidden !important;
  z-index: -9999 !important;
}

/* Style chung của bạn */
* {
  box-sizing: border-box;
}

body,
html {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.app-container {
  display: flex; 
  height: 100dvh;
  width: 100%;
  overflow: hidden;
}

.app-sidebar {
  width: 250px;
  flex-shrink: 0; 
  height: 100%;
}

.app-container {
  display: flex;
  height: 100dvh;
  width: 100%;
  overflow: hidden;
}

.app-sidebar {
  width: 250px;
  flex-shrink: 0;
}

.main-layout {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.app-content {
  flex-grow: 1;
  overflow-y: auto;
  background-color: #fdfbfa;
}

.app-content.admin-content table thead tr {
  background-color: #7d161a !important;
}

.app-content.admin-content table thead th {
  background-color: #7d161a !important;
  color: #ffffff !important;
  border-color: #7d161a !important;
}
</style>

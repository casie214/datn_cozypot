<script setup lang="ts">
import Sidebar from './components/sidebar.vue'
import Header from './components/adminHeader.vue'
import CommonNav from './components/commonNav.vue'

// 🔥 CẬP NHẬT: Đổi tên biến import thành chữ Hoa (ChatWidget) để khớp với thẻ template
// Lưu ý: Đảm bảo tên file của bạn trong thư mục components là ChatWidget.vue (chữ C hoa) hoặc chatWidget.vue tùy hệ điều hành.
import ChatWidget from './components/chatWidget.vue'

import { useRoute } from 'vue-router'
import { computed } from 'vue'

const route = useRoute()

const isAdminRoute = computed(() => {
  const path = route.path || ''
  return path.startsWith('/admin') || path.startsWith('/manage')
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

    <ChatWidget v-if="!isAdminRoute" />
  </div>
</template>

<style>
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
  position: relative; /* Thêm relative để fixed element canh tọa độ chuẩn */
}

.app-sidebar {
  width: 250px;
  flex-shrink: 0; 
  height: 100%;
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
<script setup>
// Đảm bảo tên file sidebar.vue viết đúng hoa thường (case-sensitive)
import Sidebar from './components/sidebar.vue';
</script>

<template>
  <div class="app-container">
    <Sidebar class="app-sidebar" />

    <div class="app-content">
      <router-view />
    </div>
  </div>
</template>

<style>
/* Reset padding/margin toàn trang */
* {
  box-sizing: border-box;
}

body, html { 
  margin: 0; 
  padding: 0; 
  height: 100%; 
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.app-container {
  display: flex; /* Sidebar bên trái, Content bên phải */
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.app-sidebar {
  width: 250px;
  flex-shrink: 0; /* Không cho phép sidebar bị co lại */
  height: 100%;
}

.app-content {
  flex-grow: 1; /* Chiếm toàn bộ không gian còn lại */
  background-color: #fdfbfa; /* Màu nền nhẹ đặc trưng của Admin Layout */
  overflow-y: auto; /* Cho phép cuộn nội dung trang con */
  padding: 20px 40px;
  transition: all 0.3s ease;
}

/* Tùy chỉnh thanh cuộn cho App Content cho đẹp hơn */
.app-content::-webkit-scrollbar {
  width: 6px;
}
.app-content::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 10px;
}
</style>
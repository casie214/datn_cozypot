<script setup>
import Sidebar from './components/sidebar.vue';
import Header from './components/adminHeader.vue'; // 1. Import Header
</script>

<template>
  <div class="app-container">
    <Sidebar class="app-sidebar" />

    <div class="main-layout">
      <Header/>

      <div class="app-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<style>
/* ... (Giữ nguyên style cũ) ... */

.app-container {
  display: flex; 
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.app-sidebar {
  width: 250px;
  flex-shrink: 0; 
  height: 100%;
}

/* STYLE MỚI CHO LAYOUT CHÍNH */
.main-layout {
  flex-grow: 1; /* Chiếm toàn bộ không gian còn lại bên phải Sidebar */
  display: flex;
  flex-direction: column; /* Sắp xếp Header và Content theo chiều dọc */
  height: 100%; /* Chiều cao full màn hình */
  overflow: hidden; /* Ẩn thanh cuộn của layout cha */
}

.app-content {
  /* flex-grow: 1; -> Xóa dòng này ở style cũ */
  background-color: #fdfbfa; 
  
  /* Header đã chiếm 60px, phần này sẽ chiếm không gian còn lại và có thanh cuộn riêng */
  flex: 1; 
  overflow-y: auto; 
  
  padding: 20px 40px;
  transition: all 0.3s ease;
}

/* ... (Giữ nguyên style scrollbar) ... */
</style>
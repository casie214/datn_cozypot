<script setup lang="ts">
import Sidebar from './components/sidebar.vue';
import Header from './components/adminHeader.vue'; // 1. Import Header
import { useRoute } from 'vue-router';
import { computed } from 'vue';

const route = useRoute();

const isAdminRoute = computed(() =>{
  return route.path.startsWith('/admin') || route.path.startsWith('/manage');
})
</script>

<template>
  <div class="app-container">
    <Sidebar class="app-sidebar" v-if="isAdminRoute" />

    <div class="main-layout">
      <Header/>

      <div class="app-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<style>
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
<script setup>
import { useRouter } from 'vue-router';
const router = useRouter();
const goBack = () => router.back();
</script>

<template>
  <header class="app-header">
    <div class="header-left">
      <button class="btn-back" @click="goBack">←</button>  
    </div>
    <div class="header-right">
      <i class="fa-regular fa-bell icon-bell"></i>
      <div class="user-info">
        <span>Placeholder</span>
        <i class="fa-solid fa-chevron-down icon-arrow"></i>
      </div>
    </div>
  </header>
</template>

<style scoped>
/* Import Font Awesome nếu chưa có trong dự án */
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

.app-header {
  height: 60px; /* Chiều cao của Header */
  background-color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  border-bottom: 1px solid #e0e0e0; /* Đường kẻ dưới header */
  /* Sticky header (tuỳ chọn) */
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 25px;
  color: #555;
}

.icon-bell {
  font-size: 1.2rem;
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  font-weight: 500;
}

.icon-arrow {
  font-size: 0.8rem;
}
</style>
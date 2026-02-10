<template>
  <div class="client-layout-container">
    <router-view v-slot="{ Component }">
      <transition name="fade-transform" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<script setup>
// File này đóng vai trò là cửa ngõ cho các route con của Khách hàng
</script>

<style scoped>
/* Hiệu ứng chuyển trang nhẹ nhàng cho chuyên nghiệp */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.client-layout-container {
  width: 100%;
  min-height: 100vh;
  background-color: #ffffff; /* Đảm bảo nền luôn trắng sạch sẽ như bên nhân viên */
}
</style>
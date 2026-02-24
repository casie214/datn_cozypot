<template>
  <div class="client-layout-container">
    <router-view v-slot="{ Component, route }">
      <transition name="fade-transform" mode="out-in">
        <component :is="Component" :key="route.fullPath" />
      </transition>
    </router-view>
  </div>
</template>

<style scoped>
/* Hiệu ứng trượt từ TRÁI sang PHẢI */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Trang mới: Xuất hiện từ bên trái (-30px) trượt vào vị trí chính giữa */
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

/* Trang cũ: Từ chính giữa trượt thoát ra bên phải (30px) */
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.client-layout-container {
  width: 100%;
  min-height: 100vh;
  background-color: #ffffff;
  /* Quan trọng: Tránh hiện thanh cuộn ngang khi trang đang trượt */
  overflow-x: hidden;
  position: relative;
}
</style>
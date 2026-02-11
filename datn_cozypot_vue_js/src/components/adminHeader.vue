<script setup>
import { useAuthStore } from '@/pages/guest/authentication/authenticationServices/authenticationService';
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';

const showDropdown = ref(false);
const dropdownRef = ref(null);

const authStore = useAuthStore();
const router = useRouter();
const goBack = () => router.back();

const isLoggedIn = computed(() => !!authStore.token);

const isAdmin = computed(() => {
    const role = authStore.role;
    return role === 'ADMIN' || role === 'Quản lý' || role === 'EMPLOYEE';
});

const toggleDropdown = () => {
    showDropdown.value = !showDropdown.value;
};

const handleLogout = () => {
    authStore.logout();
    showDropdown.value = false;
    window.location.reload();
};

const closeDropdown = (e) => {
    if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
        showDropdown.value = false;
    }
};

const userAvatar = computed(() => {
    return authStore.user?.avatar || 'https://ui-avatars.com/api/?name=' + (authStore.user?.username || 'User') + '&background=random';
});

onMounted(() => {
    document.addEventListener('click', closeDropdown);
});

onUnmounted(() => {
    document.removeEventListener('click', closeDropdown);
});
</script>

<template>
  <header class="app-header">
    <div class="header-left">
      <button class="btn-back" @click="goBack">←</button>  
    </div>
    <div class="header-right">
      <i class="fa-regular fa-bell icon-bell"></i>
      <div class="nav-auth">
                <div v-if="!isLoggedIn" class="guest-actions">
                    <button class="btn-login" @click="handleLogin">
                        <i class="fa-regular fa-user"></i> Đăng nhập
                    </button>
                    <button class="btn-register" @click="handleRegister">Đăng ký</button>
                </div>

                <div v-else class="user-actions">
                    <div class="user-profile" ref="dropdownRef">
                        <div class="user-info" @click="toggleDropdown">
                            <img :src="userAvatar" alt="User" class="avatar-img" />
                            <span class="user-name">{{ authStore.user?.username || 'Đã đăng nhập' }}</span>
                            <i class="fa-solid fa-caret-down"></i>
                        </div>

                        <transition name="dropdown-fade">
                            <div v-if="showDropdown" class="dropdown-menu">
                                <div class="dropdown-item" @click="goToProfile">
                                    <i class="fa-solid fa-id-card"></i> Hồ sơ cá nhân
                                </div>
                                <div class="dropdown-divider"></div>
                                <div class="dropdown-item text-danger" @click="handleLogout">
                                    <i class="fa-solid fa-right-from-bracket"></i> Đăng xuất
                                </div>
                            </div>
                        </transition>
                    </div>
                </div>
            </div>
    </div>
  </header>
</template>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

.btn-back{
  border: none;
  background-color: white;
  transition: 0.2s;
  scale: 1.3;
}

.btn-back:hover{
  transform: scale(1.1);
}

.app-header {
  height: 60px; /* Chiều cao của Header */
  background-color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  border-bottom: 1px solid #e0e0e0; /* Đường kẻ dưới header */
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

.guest-actions {
    display: flex;
    gap: 15px;
    align-items: center;
}

.btn-login {
    background: transparent;
    border: none;
    color: #333;
    font-weight: 600;
    cursor: pointer;
}

.btn-login:hover {
    color: #7d161a;
}

.btn-register {
    background: #7d161a;
    color: white;
    border: none;
    padding: 8px 20px;
    border-radius: 20px;
    font-weight: 600;
    cursor: pointer;
    transition: 0.3s;
}

.btn-register:hover {
    background: #5c0a16;
}

/* USER ACTIONS */
.user-actions {
    display: flex;
    align-items: center;
    gap: 20px;
}

.btn-admin {
    background: #2c3e50;
    color: white;
    border: 1px solid #2c3e50;
    padding: 6px 15px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 0.9rem;
    font-weight: 600;
}

.btn-admin:hover {
    background: #1a252f;
}

/* PROFILE & DROPDOWN */
.user-profile {
    position: relative;
    display: inline-block;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 5px 10px;
    border-radius: 30px;
    transition: 0.2s;
    user-select: none;
}

.user-info:hover {
    background-color: #f5f5f5;
}

.avatar-img {
    width: 35px;
    height: 35px;
    border-radius: 50%;
    object-fit: cover;
    border: 1px solid #ddd;
}

.user-name {
    font-weight: 600;
    font-size: 0.9rem;
    max-width: 100px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.dropdown-menu {
    position: absolute;
    top: 100%;
    right: 0;
    margin-top: 10px;
    width: 220px;
    background-color: #ffffff;
    border: 1px solid #ccc;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

    z-index: 10000;

    display: flex;
    flex-direction: column;
    transform-origin: top right;
}

.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
    transition: all 0.2s ease-out;
}

.dropdown-fade-enter-from,
.dropdown-fade-leave-to {
    opacity: 0;
    transform: translateY(-10px) scale(0.95);
}

.dropdown-fade-enter-to,
.dropdown-fade-leave-from {
    opacity: 1;
    transform: translateY(0) scale(1);
}

.dropdown-item {
    padding: 12px;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 10px;
    color: #333;
}

.dropdown-item:hover {
    background-color: #f5f5f5;
}

.dropdown-divider {
    height: 1px;
    background: #eee;
    margin: 5px 0;
}

.text-danger {
    color: #d32f2f;
}

.text-danger:hover {
    background-color: #fff5f5;
}

/* TRANSITION: FADE (Chuẩn Vue) */
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
    transform: translateY(-10px);
}

/* RESPONSIVE */
@media (max-width: 768px) {
    .nav-links {
        display: none;
    }

    .user-name {
        display: none;
    }
}
</style>
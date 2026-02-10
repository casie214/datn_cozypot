<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/pages/guest/authentication/authenticationServices/authenticationService';

const router = useRouter();
const authStore = useAuthStore();

// --- STATE ---
const showDropdown = ref(false);
const dropdownRef = ref(null);

const isLoggedIn = computed(() => !!authStore.token);

const isAdmin = computed(() => {
    const role = authStore.role;
    return role === 'ADMIN' || role === 'Quản lý' || role === 'EMPLOYEE';
});

const userAvatar = computed(() => {
    return authStore.user?.avatar || 'https://ui-avatars.com/api/?name=' + (authStore.user?.username || 'User') + '&background=random';
});

const handleLogin = () => router.push({ name: 'login' });
const handleRegister = () => router.push({ name: 'register' });

const goToAdminDashboard = () => {
    router.push('/admin/dashboard');
};

const toggleDropdown = () => {
    showDropdown.value = !showDropdown.value;
};

const handleLogout = () => {
    authStore.logout();
    showDropdown.value = false;
    window.location.reload();
};

const goToProfile = () => {
    alert("Chức năng đang phát triển");
}

const closeDropdown = (e) => {
    if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
        showDropdown.value = false;
    }
};

onMounted(() => {
    document.addEventListener('click', closeDropdown);
});

onUnmounted(() => {
    document.removeEventListener('click', closeDropdown);
});
</script>

<template>
    <nav class="navbar">
        <div class="container nav-content">
            <div class="logo" @click="router.push('/')">
                <img src="../assets/images/logo_upscaled.jpg" alt="CozyPot" />
                <span>CozyPot</span>
            </div>

            <ul class="nav-links">
                <li><router-link to="/" active-class="active">Trang chủ</router-link></li>
                <li><router-link to="/menu" active-class="active">Thực đơn</router-link></li>
                <li><router-link to="/offers" active-class="active">Ưu đãi</router-link></li>
            </ul>

            <div class="nav-auth">
                <div v-if="!isLoggedIn" class="guest-actions">
                    <button class="btn-login" @click="handleLogin">
                        <i class="fa-regular fa-user"></i> Đăng nhập
                    </button>
                    <button class="btn-register" @click="handleRegister">Đăng ký</button>
                </div>

                <div v-else class="user-actions">
                    <button v-if="isAdmin" class="btn-admin" @click="goToAdminDashboard">
                        <i class="fa-solid fa-gauge"></i> Quản lý
                    </button>

                    <div class="user-profile" ref="dropdownRef">
                        <div class="user-info" @click="toggleDropdown">
                            <img :src="userAvatar" alt="User" class="avatar-img" />
                            <span class="user-name">{{ authStore.user?.username || 'Khách' }}</span>
                            <i class="fa-solid fa-caret-down"></i>
                        </div>

                        <transition name="dropdown-fade">
                            <div v-if="showDropdown" class="dropdown-menu">
                                <div class="dropdown-item" @click="goToProfile">
                                    <i class="fa-solid fa-id-card"></i> Hồ sơ cá nhân
                                </div>
                                <div class="dropdown-item" @click="router.push('/history')">
                                    <i class="fa-solid fa-clock-rotate-left"></i> Lịch sử đặt bàn
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
    </nav>
</template>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* --- VARIABLES --- */
:root {
    --primary: #7d161a;
    --text-dark: #333;
}

/* NAVBAR LAYOUT */
.navbar {
    background: white;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
    position: sticky;
    top: 0;
    z-index: 1000;
    height: 70px;
    display: flex;
    align-items: center;

    /* 👇 QUAN TRỌNG: Cho phép menu thò ra ngoài mà không bị cắt */
    overflow: visible !important;
}

.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
    width: 100%;
}

.nav-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

/* LOGO */
.logo {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 1.5rem;
    font-weight: 800;
    color: #7d161a;
    cursor: pointer;
}

.logo img {
    height: 40px;
}

/* LINKS */
.nav-links {
    display: flex;
    list-style: none;
    gap: 30px;
    margin: 0;
}

.nav-links a {
    text-decoration: none;
    color: #333;
    font-weight: 600;
    transition: color 0.3s;
}

.nav-links a:hover,
.nav-links a.active {
    color: #7d161a;
}

/* AUTH BUTTONS */
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
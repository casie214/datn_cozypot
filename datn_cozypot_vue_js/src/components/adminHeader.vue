<script setup>
import { useAuthStore } from '@/pages/guest/authentication/authenticationServices/authenticationService';
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import axiosClient from "@/services/axiosClient";
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import Swal from 'sweetalert2';

const notifications = ref([]);
const authStore = useAuthStore();
const router = useRouter();

// --- STATE QUẢN LÝ DROPDOWN ---
const showDropdown = ref(false); // Menu User
const showNotifyDropdown = ref(false); // Menu Thông báo
const dropdownRef = ref(null);
const notifyRef = ref(null);

// --- STATE THÔNG BÁO ---
const saveToLocal = () => {
    localStorage.setItem("user_notifications", JSON.stringify(notifications.value));
};

// 2. Hàm load từ LocalStorage khi mở web
const loadFromLocal = () => {
    const saved = localStorage.getItem("user_notifications");
    if (saved) {
        notifications.value = JSON.parse(saved);
    }
};

const handleNotifyClick = (item) => {

};

const isLoadingNotify = ref(false);

// --- LOGIC WEBSOCKET ---
let stompClient = null;
const connectWebSocket = () => {
    const socket = new SockJS('http://localhost:8080/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, (frame) => {
        stompClient.subscribe('/topic/notifications', (message) => {
            const newNotify = JSON.parse(message.body);

            // Thêm thông báo mới vào đầu mảng
            notifications.value.unshift({
                id: newNotify.id,
                targetId: newNotify.targetId,
                title: newNotify.title,
                content: newNotify.content,
                time: 'Vừa xong',
                isRead: false
            });

            // LƯU VÀO LOCALSTORAGE NGAY LẬP TỨC
            saveToLocal();

            // Tùy chọn: Hiện thông báo Toast của SweetAlert2
            Swal.fire({
                title: newNotify.title,
                text: newNotify.content,
                icon: 'info',
                toast: true,
                position: 'top-end',
                timer: 4000,
                showConfirmButton: false
            });
        });
    });
};

const removeNotify = (id) => {
    notifications.value = notifications.value.filter(n => n.id !== id);
    saveToLocal();
};

const showAllNotifyModal = ref(false);

const openAllNotifyModal = () => {
    showNotifyDropdown.value = false;
    showAllNotifyModal.value = true;
};

// Xóa toàn bộ thông báo (có hỏi xác nhận)
const clearAllNotify = () => {
    Swal.fire({
        title: 'Xóa tất cả?',
        text: "Hành động này sẽ làm sạch nhật ký thông báo của bạn!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#7d161a',
        confirmButtonText: 'Đồng ý xóa',
        cancelButtonText: 'Hủy'
    }).then((result) => {
        if (result.isConfirmed) {
            notifications.value = [];
            saveToLocal();
            showAllNotifyModal.value = false;
        }
    });
};

const goToAllNotifications = () => {
    showNotifyDropdown.value = false;
    router.push('/admin/notifications'); // Bạn cần tạo route này
};


// --- LOGIC ĐIỀU KHIỂN ---
const toggleNotify = () => {
    showNotifyDropdown.value = !showNotifyDropdown.value;
    if (showNotifyDropdown.value) showDropdown.value = false;
};

const toggleDropdown = () => {
    showDropdown.value = !showDropdown.value;
    if (showDropdown.value) showNotifyDropdown.value = false;
};

const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length);

const markAllAsRead = () => {
    notifications.value.forEach(n => n.isRead = true);
    saveToLocal();
};

// Hàm đóng menu khi click ra ngoài (GỘP CHUNG)
const handleClickOutside = (e) => {
    // Đóng dropdown User
    if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
        showDropdown.value = false;
    }
    // Đóng dropdown Thông báo
    if (notifyRef.value && !notifyRef.value.contains(e.target)) {
        showNotifyDropdown.value = false;
    }
};

// --- AUTH & NAVIGATION ---
const isLoggedIn = computed(() => !!authStore.token);
const userAvatar = computed(() => authStore.user?.avatar || `https://ui-avatars.com/api/?name=${authStore.user?.username || 'User'}&background=random`);

const handleLogout = () => { authStore.logout(); window.location.reload(); };
const goBack = () => router.back();
const handleLogin = () => router.push({ name: 'login' });
const handleRegister = () => router.push({ name: 'register' });
const goToChangePassword = () => { showDropdown.value = false; router.push('/doi-mat-khau'); };
const goToProfile = () => {
    showDropdown.value = false;
    const role = authStore.role;
    if (role === 'USER') router.push('/ho-so');
    else if (['ADMIN', 'Quản lý', 'EMPLOYEE'].includes(role)) router.push('/admin/profile');
    else router.push('/login');
};

// --- LIFECYCLE (CHỈ GIỮ LẠI 1 CẶP DUY NHẤT) ---
onMounted(() => {
    loadFromLocal(); // Phải load dữ liệu cũ lên trước
    document.addEventListener('click', handleClickOutside);
    if (isLoggedIn.value) connectWebSocket();
});
onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside);
    if (stompClient) stompClient.disconnect();
});
</script>

<template>
    <header class="app-header">
        <div class="header-left">
            <button class="btn-back" @click="goBack">←</button>
        </div>
        <div class="header-right">
            <div class="notification-wrapper" ref="notifyRef">
                <div class="bell-container" @click.stop="toggleNotify">
                    <i class="fa-regular fa-bell icon-bell"></i>
                    <span v-if="unreadCount > 0" class="notify-badge">{{ unreadCount }}</span>
                </div>

                <transition name="dropdown-fade">
                    <div v-if="showNotifyDropdown" class="dropdown-menu notify-menu">
                        <div class="notify-header">
                            <span>Thông báo</span>
                            <div class="d-flex gap-2">
                                <button @click="markAllAsRead" class="btn-icon-head" title="Đánh dấu đã đọc">
                                    <i class="fa-solid fa-check-double"></i>
                                </button>
                            </div>
                        </div>

                        <div class="notify-list custom-scrollbar">
                            <div v-if="notifications.length === 0" class="notify-empty py-4 text-center text-muted">
                                Không có thông báo mới
                            </div>

                            <div v-for="item in notifications" :key="item.id" class="notify-item"
                                :class="{ 'unread': !item.isRead }" @click="handleNotifyClick(item)">
                                <div class="notify-dot" v-if="!item.isRead"></div>
                                <div class="notify-info flex-grow-1">
                                    <div class="notify-title">{{ item.title }}</div>
                                    <div class="notify-content">{{ item.content }}</div>
                                    <div class="notify-time">{{ item.time }}</div>
                                </div>
                                <button class="btn-delete-item" @click.stop="removeNotify(item.id)">
                                    <i class="fa-solid fa-xmark"></i>
                                </button>
                            </div>
                        </div>

                        <div class="notify-footer" @click="openAllNotifyModal">
                            Xem tất cả thông báo
                        </div>
                    </div>
                </transition>
            </div>
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
                        </div>

                        <transition name="dropdown-fade">
                            <div v-if="showDropdown" class="dropdown-menu">
                                <div class="dropdown-item" @click="goToProfile">
                                    <i class="fa-solid fa-id-card"></i> Hồ sơ cá nhân
                                </div>
                                <div class="dropdown-item" @click="goToChangePassword">
                                    <i class="fa-solid fa-key"></i> Đổi mật khẩu
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

    <transition name="modal-fade">
    <div v-if="showAllNotifyModal" class="modal-overlay" style="z-index: 99999;">
        <div class="modal-box" style="width: 850px; max-width: 95vw;">
            <div class="modal-header border-bottom pb-3 mb-3">
                <h4 class="m-0 text-danger fw-bold">
                    <i class="fa-solid fa-bell me-2"></i>Lịch sử thông báo
                </h4>
                <button class="close-btn" @click="showAllNotifyModal = false">✕</button>
            </div>

            <div class="modal-body p-0 custom-scrollbar" style="max-height: 60vh; overflow-y: auto;">
                <div v-if="notifications.length === 0" class="text-center py-5 text-muted">
                    <i class="fa-solid fa-folder-open fa-3x mb-3 opacity-25"></i>
                    <p>Không có dữ liệu thông báo</p>
                </div>

                <div v-else class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="bg-light sticky-top" style="top: 0; z-index: 10;">
                            <tr>
                                <th class="ps-4" style="width: 60%;">Nội dung</th>
                                <th style="width: 20%;">Thời gian</th>
                                <th class="text-end pe-4" style="width: 20%;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="item in notifications" :key="item.id" 
                                :class="{'bg-unread-table': !item.isRead}" 
                                style="cursor: pointer;" @click="handleNotifyClick(item)">
                                <td class="ps-4">
                                    <div class="fw-bold text-dark">{{ item.title }}</div>
                                    <div class="text-muted small notify-content">{{ item.content }}</div>
                                </td>
                                <td class="text-muted small">{{ item.time }}</td>
                                <td class="text-end pe-4">
                                    <button class="btn btn-sm btn-outline-danger border-0" 
                                            @click.stop="removeNotify(item.id)">
                                        <i class="fa-solid fa-trash-can"></i>
                                    </button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>

            <div class="d-flex justify-content-between mt-3 pt-3 border-top">
                <button class="btn btn-outline-secondary px-4 py-2" 
                        style="background: #f8f9fa !important; color: #333 !important; border: 1px solid #ddd !important;"
                        @click="clearAllNotify" :disabled="notifications.length === 0">
                    <i class="fa-solid fa-eraser me-2"></i>Xóa tất cả
                </button>
                <button class="btn px-5 py-2" @click="showAllNotifyModal = false">Đóng</button>
            </div>
        </div>
    </div>
</transition>
</template>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

.btn-back {
    border: none;
    background-color: white;
    transition: 0.2s;
    scale: 1.3;
}

.btn-back:hover {
    transform: scale(1.1);
}

.app-header {
    height: 60px;
    /* Chiều cao của Header */
    background-color: #ffffff;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 30px;
    border-bottom: 1px solid #e0e0e0;
    /* Đường kẻ dưới header */
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

.notification-wrapper {
    position: relative;
}

.bell-container {
    position: relative;
    cursor: pointer;
    padding: 5px;
}

.notify-badge {
    position: absolute;
    top: -2px;
    right: -2px;
    background-color: #7d161a;
    /* Màu đỏ đô thương hiệu */
    color: white;
    font-size: 10px;
    font-weight: bold;
    padding: 2px 5px;
    border-radius: 10px;
    border: 2px solid white;
    min-width: 18px;
    text-align: center;
}

/* Dropdown Thông báo */
.notify-menu {
    width: 320px !important;
    padding: 0;
    overflow: hidden;
}

.notify-header {
    padding: 12px 15px;
    font-weight: bold;
    border-bottom: 1px solid #eee;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fdf2f2;
}

.btn-mark-read {
    background: none;
    border: none;
    color: #7d161a;
    font-size: 12px;
    cursor: pointer;
}

.notify-list {
    max-height: 350px;
    overflow-y: auto;
}

.notify-item {
    padding: 12px 15px;
    border-bottom: 1px solid #f9f9f9;
    display: flex;
    gap: 10px;
    transition: 0.2s;
    cursor: pointer;
    position: relative;
}

.notify-item:hover {
    background-color: #fcfcfc;
}

.notify-item.unread {
    background-color: #fff8f8;
}

.notify-dot {
    width: 8px;
    height: 8px;
    background-color: #7d161a;
    border-radius: 50%;
    margin-top: 5px;
    flex-shrink: 0;
}

.notify-title {
    font-weight: 700;
    font-size: 14px;
    margin-bottom: 3px;
    color: #333;
}

.notify-content {
    font-size: 13px;
    color: #666;
    line-height: 1.4;
}

.notify-time {
    font-size: 11px;
    color: #999;
    margin-top: 5px;
}

.notify-footer {
    padding: 10px;
    text-align: center;
    font-size: 13px;
    font-weight: 600;
    color: #7d161a;
    background: #f9f9f9;
    cursor: pointer;
}

.custom-scrollbar::-webkit-scrollbar {
    width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background: #ddd;
    border-radius: 4px;
}

.notify-content {
    white-space: pre-line;
    /* Quan trọng nhất để hiển thị xuống dòng */
}

.btn-delete-single {
    background: none;
    border: none;
    color: #ccc;
    padding: 5px;
    transition: 0.2s;
    opacity: 0;
    /* Mặc định ẩn */
}

.notify-item:hover .btn-delete-single {
    opacity: 1;
    /* Di chuột vào mới hiện nút xóa */
}

.btn-delete-single:hover {
    color: #7d161a;
    transform: scale(1.2);
}

.btn-mark-read:hover {
    transform: scale(1.1);
}

.btn-icon-head {
    background: none;
    border: none;
    color: #7d161a;
    cursor: pointer;
    padding: 2px 5px;
    transition: 0.2s;
}

/* Nút xóa từng item trong dropdown */
.btn-delete-item {
    background: none;
    border: none;
    color: #ccc;
    opacity: 0;
    transition: 0.2s;
    padding: 5px;
}

.notify-item:hover .btn-delete-item {
    opacity: 1;
}

.btn-delete-item:hover {
    color: #7d161a;
    transform: scale(1.2);
}

/* Màu nền cho dòng chưa đọc trong bảng */
.bg-unread-table {
    background-color: #fff8f8;
}

.bg-unread-table:hover {
    background-color: #fdf2f2 !important;
}

/* Tận dụng lại CSS Modal có sẵn của bạn */
.modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    backdrop-filter: blur(2px);
}

.modal-box {
    background: #fff;
    border-radius: 15px;
    padding: 25px;
    animation: fadeIn 0.3s ease-out;
    box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}

.close-btn {
    border: none;
    background: none;
    font-size: 20px;
    color: #999;
    cursor: pointer;
}

.close-btn:hover {
    color: #333;
}

/* Định dạng nội dung thông báo trong bảng (giữ \n) */
.notify-content {
    white-space: pre-line !important;
}
.modal-header{
    display: flex;
    flex-direction: row;
    justify-content: space-between;
}
</style>

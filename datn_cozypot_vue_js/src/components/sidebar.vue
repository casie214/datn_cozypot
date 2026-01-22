<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const menuItems = [
    { name: 'Tổng quan', icon: "fa-solid fa-house", path: '/admin/dashboard' },
    { name: 'Đặt bàn', icon: "fa-solid fa-calendar-days", path: '/admin/booking' },
    { name: 'Hóa đơn', icon: "fa-solid fa-cart-shopping", path: '/admin/orders' },
    { name: 'Check-in bàn', icon: "fa-solid fa-circle-check", path: '/admin/checkin' },
    { name: 'Quản lý bàn', icon: "fa-solid fa-chair", path: '/admin/tables' },
    { name: 'Nhân viên', icon: "fa-solid fa-user", path: '/admin/staff' },
    // Sửa path ở đây cho khớp với Router của bạn
    { name: 'Khách hàng', icon: "fa-solid fa-users", path: '/admin/client' }, 
    { name: 'Thực đơn', icon: "fa-solid fa-bell-concierge", path: '/admin/menu' },
    { name: 'Danh mục', icon: "fa-solid fa-table-list", path: '/admin/categories' },
    { name: 'Nhắn tin', icon: "fa-solid fa-comments", path: '/admin/messages' },
    { name: 'Khuyến mãi', icon: "fa-solid fa-tags", path: '/admin/promotions' },
    { name: 'Thống kê', icon: "fa-solid fa-chart-area", path: '/admin/statistics'}
];

// Hàm kiểm tra thông minh hơn: Nếu path hiện tại bắt đầu bằng path menu thì active
const isActive = (path) => {
    return route.path.startsWith(path);
};
</script>

<template>
    <aside class="sidebar">
        <div class="logo-container">
            <div class="logo-circle">
                <img src="../assets/images/logo_upscaled.jpg" alt="Logo" class="logo">
            </div>
        </div>
    
        <nav class="menu-list">
            <hr class="divider">
            <router-link 
                v-for="(item, index) in menuItems" 
                :key="index" 
                :to="item.path"
                class="menu-item" 
                :class="{ 'active': isActive(item.path) }"
            >
                <i :class="item.icon" class="icon"></i>
                <span class="label">{{ item.name }}</span>
                <span v-if="isActive(item.path)" class="arrow">
                    <i class="fa-solid fa-chevron-right"></i>
                </span>
            </router-link>
        </nav>
    </aside>
</template>

<style scoped>
/* Giữ nguyên phần CSS cũ của bạn vì nó đã rất đẹp rồi */
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

.sidebar {
    width: 250px;
    background-color: #fff;
    height: 100vh;
    box-shadow: 4px 0 10px rgba(0, 0, 0, 0.05); 
    display: flex;
    flex-direction: column;
    padding-top: 20px;
    position: sticky;
    top: 0;
    left: 0;
    z-index: 100;
}

.divider {
    border: 0;
    border-top: 2px solid #7d1619;
    margin: 10px auto;
    width: 90%;
}

.logo-container {
    width: 100%;
    height: 10em;
    display: flex;
    align-items: center;
    justify-content: center;
}

.logo-circle {
    width: 7em;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
}

.logo {
    width: 100%;      
    height: 100%;      
    object-fit: cover; 
}

.menu-list {
    overflow-y: auto;
    padding: 0 10px;
}

.menu-item {
    display: flex;
    align-items: center;
    padding: 12px 20px;
    cursor: pointer;
    color: #555;
    font-weight: 500;
    transition: all 0.3s;
    text-decoration: none;
    margin-bottom: 4px;
}

.menu-item:hover {
    background-color: #f5f5f5;
    border-radius: 10px;
}

.menu-item.active {
    background-color: #7B121C; /* Màu đỏ đô */
    color: white;
    border-radius: 10px;
}

.icon {
    margin-right: 15px;
    width: 25px; 
    text-align: center;
    font-size: 18px; 
}

.arrow {
    margin-left: auto;
    font-size: 12px;
}
</style>
<script setup>
import { ref } from 'vue';

// Cập nhật thêm thuộc tính 'path' để map với router
const menuItems = [
    { name: 'Tổng quan', icon: "fa-solid fa-house", path: "/dashboard" },
    { name: 'Đặt bàn', icon: "fa-solid fa-calendar-days", path: "/booking" },
    
    // Link tới trang Quản lý đơn hàng vừa tạo
    { name: 'Đơn hàng', icon: "fa-solid fa-cart-shopping", path: "/manage/orders" },
    
    { name: 'Check-in bàn', icon: "fa-solid fa-circle-check", path: "/checkin" },
    { name: 'Quản lý bàn', icon: "fa-solid fa-chair", path: "/tables" },
    { name: 'Nhân viên', icon: "fa-solid fa-user", path: "/staff" },
    { name: 'Khách hàng', icon: "fa-solid fa-users", path: "/customers" }, 
    
    // Link tới trang Quản lý thực đơn
    { name: 'Thực đơn', icon: "fa-solid fa-bell-concierge", path: "/manage/food" },
    
    { name: 'Danh mục', icon: "fa-solid fa-table-list", path: "/categories" },
    { name: 'Nhắn tin', icon: "fa-solid fa-comments", path: "/messages" },
    { name: 'Khuyến mãi', icon: "fa-solid fa-tags", path: "/promotions" },
    { name: 'Thống kê', icon: "fa-solid fa-chart-area", path: "/reports"}
];
</script>

<template>
    <aside class="sidebar">
        <div class="logo-container">
            <div class="logo-circle">
                <span><img src="../assets/images/logo_upscaled.jpg" alt="CozyPot Logo" class="logo"></span>
            </div>
        </div>

        <nav class="menu-list">
            <router-link 
                v-for="(item, index) in menuItems" 
                :key="index" 
                :to="item.path"
                class="menu-item"
                active-class="active"
            >
                
                <i :class="item.icon" class="icon"></i>
                
                <span class="label">{{ item.name }}</span>
                
                <span class="arrow" v-if="$route.path === item.path">▼</span>
            </router-link>
        </nav>
    </aside>
</template>

<style scoped>
/* Import thư viện icon */
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

.sidebar {
    width: 250px;
    background-color: #fff;
    height: 100vh;
    border-right: 1px solid #eee;
    display: flex;
    flex-direction: column;
    padding-top: 20px;
    position: fixed; /* Cố định sidebar để scroll nội dung bên phải */
    left: 0;
    top: 0;
}

.logo-container {
    width: 100%;
    height: 10em;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 30px;
}

.logo-circle {
    width: 10em;
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

/* Style cho router-link (thẻ a) */
.menu-item {
    display: flex;
    align-items: center;
    padding: 12px 20px;
    cursor: pointer;
    color: #555;
    font-weight: 500;
    transition: all 0.3s;
    text-decoration: none; /* Bỏ gạch chân mặc định của thẻ a */
}

.menu-item:hover {
    background-color: #f5f5f5;
}

/* Class active này sẽ được Vue Router tự động thêm vào */
.menu-item.active {
    background-color: #8B0000;
    color: white;
}

.icon {
    margin-right: 15px;
    width: 25px; 
    text-align: center;
    font-size: 18px; 
}

.arrow {
    margin-left: auto;
    font-size: 10px;
}
</style>
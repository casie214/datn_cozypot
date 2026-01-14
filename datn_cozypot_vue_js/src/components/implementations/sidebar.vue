<script setup>
import { ref } from 'vue';

const menuItems = [
    { name: 'Tổng quan', icon: '' },
    { name: 'Đặt bàn', icon: '' },
    { name: 'Đơn hàng', icon: '' },
    { name: 'Check-in bàn', icon: '' },
    { name: 'Quản lý bàn', icon: '' },
    { name: 'Nhân viên', icon: '' },
    { name: 'Khách hàng', icon: '' }, 
    { name: 'Thực đơn', icon: '', active: true },
    { name: 'Danh mục', icon: '' },
    { name: 'Nhắn tin', icon: '' },
    { name: 'Khuyến mãi', icon: '' },
];
</script>

<template>
    <aside class="sidebar">
        <div class="logo-container">
            <div class="logo-circle">
                <span><img src="../../assets/images/logo_upscaled.jpg" alt="" class="logo"></span>
            </div>
        </div>

        <nav class="menu-list">
            <div v-for="(item, index) in menuItems" :key="index" class="menu-item" :class="{ 'active': item.active }">
                <span class="icon">{{ item.icon }}</span>
                <span class="label">{{ item.name }}</span>
                <span v-if="item.active" class="arrow">▼</span>
            </div>
        </nav>
    </aside>
</template>

<style scoped>
.sidebar {
    width: 250px;
    background-color: #fff;
    height: 100vh;
    border-right: 1px solid #eee;
    display: flex;
    flex-direction: column;
    padding-top: 20px;
}

.logo-container {
    display: flex;
    justify-content: center;
    margin-bottom: 30px;
}

.logo-circle {
    width: 80px;
    height: 80px;
    border: 2px solid #8B0000;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #8B0000;
}

.menu-item {
    display: flex;
    align-items: center;
    padding: 12px 20px;
    cursor: pointer;
    color: #555;
    font-weight: 500;
    transition: all 0.3s;
}

.menu-item:hover {
    background-color: #f5f5f5;
}

.menu-item.active {
    background-color: #8B0000;
    color: white;
}

.icon {
    margin-right: 15px;
    width: 20px;
    text-align: center;
}

.arrow {
    margin-left: auto;
    font-size: 10px;
}

.logo-container {
    width: 100%;
    height: 10em;
    display: flex;
    align-items: center;
    justify-content: center;
}

.logo {
    width: 10em;
    height: 10em;
}
</style>
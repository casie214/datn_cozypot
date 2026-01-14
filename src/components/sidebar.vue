<script setup>
import { ref } from 'vue';

const menuItems = [
    { name: 'Tổng quan', icon: "fa-solid fa-house" },
    { name: 'Đặt bàn', icon: "fa-solid fa-calendar-days", active: true },
    { name: 'Đơn hàng', icon: "fa-solid fa-cart-shopping" },
    { name: 'Check-in bàn', icon: "fa-solid fa-circle-check" },
    { name: 'Quản lý bàn', icon: "fa-solid fa-chair" },
    { name: 'Nhân viên', icon: "fa-solid fa-user" },
    { name: 'Khách hàng', icon: "fa-solid fa-users" }, 
    { name: 'Thực đơn', icon: "fa-solid fa-bell-concierge", },
    { name: 'Danh mục', icon: "fa-solid fa-table-list" },
    { name: 'Nhắn tin', icon: "fa-solid fa-comments" },
    { name: 'Khuyến mãi', icon: "fa-solid fa-tags" },
    { name: 'Thống kê', icon: "fa-solid fa-chart-area"}
];
</script>

<template>
    <aside class="sidebar">
        <div class="logo-container">
            <div class="logo-circle">
                <span><img src="../assets/images/logo_upscaled.jpg" alt="" class="logo"></span>
            </div>
        </div>
    
        <nav class="container menu-list">
            <hr>
            <div v-for="(item, index) in menuItems" :key="index" class="menu-item" :class="{ 'active': item.active }">
                
                <i :class="item.icon" class="icon"></i>
                
                <span class="label">{{ item.name }}</span>
                <span v-if="item.active" class="arrow"></span>
            </div>
        </nav>
    </aside>
</template>

<style scoped src="../assets/base.css">
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
</style>
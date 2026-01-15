<script setup>
import { ref } from 'vue';
import { useRoute } from "vue-router";
import { useRouter } from "vue-router";

const menuItems = [
    { name: 'Tổng quan', icon: "fa-solid fa-house", routeName: 'dashboard' },
    { name: 'Đặt bàn', icon: "fa-solid fa-calendar-days", routeName: 'booking' },
    { name: 'Đơn hàng', icon: "fa-solid fa-cart-shopping", routeName: 'orders' },
    { name: 'Check-in bàn', icon: "fa-solid fa-circle-check", routeName: 'checkin' },
    { name: 'Quản lý bàn', icon: "fa-solid fa-chair", routeName: 'tableManager' },
    { name: 'Nhân viên', icon: "fa-solid fa-user", routeName: 'staff' },
    { name: 'Khách hàng', icon: "fa-solid fa-users", routeName: 'customers' },
    { name: 'Thực đơn', icon: "fa-solid fa-bell-concierge", routeName: 'foodManager' },
    { name: 'Danh mục', icon: "fa-solid fa-table-list", routeName: 'categoryManager' }, 
    { name: 'Nhắn tin', icon: "fa-solid fa-comments", routeName: 'chat' },
    { name: 'Khuyến mãi', icon: "fa-solid fa-tags", routeName: 'promotion' },
    { name: 'Thống kê', icon: "fa-solid fa-chart-area", routeName: 'statistics'}
];


const router = useRouter();
const route = useRoute();

function navigate(name) {
  if (name) {
    router.push({ name: name });
  }
}

const isActive = (itemName) => {
    return route.name === itemName;
}
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
            <div 
                v-for="(item, index) in menuItems" 
                :key="index" 
                class="menu-item" 
                :class="{ 'active': isActive(item.routeName) }"
                @click="navigate(item.routeName)"
            >
                
                <i :class="item.icon" class="icon"></i>
                
                <span class="label">{{ item.name }}</span>
                
                <span v-if="isActive(item.routeName)" class="arrow"></span>
            </div>
        </nav>
    </aside>
</template>

<style scoped src="../assets/base.css">
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
</style>
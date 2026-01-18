<script setup>
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from "vue-router";

const menuItems = ref([
    { name: 'Tổng quan', icon: "fa-solid fa-house", path: '/admin/dashboard' },
    { name: 'Đặt bàn', icon: "fa-solid fa-calendar-days", path: '/admin/booking' },
    { name: 'Đơn hàng', icon: "fa-solid fa-cart-shopping", path: '/admin/orders' },
    { name: 'Check-in bàn', icon: "fa-solid fa-circle-check", path: '/admin/checkin' },
    { name: 'Quản lý bàn', icon: "fa-solid fa-chair", path: '/admin/tables' },
    { name: 'Nhân viên', icon: "fa-solid fa-user", path: '/admin/staff' },
    { name: 'Khách hàng', icon: "fa-solid fa-users", path: '/admin/client' }, 
    
    { 
        name: 'Thực đơn', 
        icon: 'fa-solid fa-bell-concierge', 
        routeName: 'foodManager',
        isOpen: false, 
        children: [
            { name: 'Danh sách món', tab: 'thucdon' },
            { name: 'Thực đơn chi tiết', tab: 'chitietTD' },
            { name: 'Set lẩu', tab: 'setlau' }
        ]
    },
    // -----------------------------------mm

    { 
        name: 'Danh mục', 
        icon: "fa-solid fa-table-list", 
        routeName: 'categoryManager', 
        isOpen: false,
        children: [
            { name: 'Danh mục gốc', tab: 'danhmuc' },
            { name: 'Danh mục chi tiết', tab: 'chitietDM' },
            { name: 'Loại Set Lẩu', tab: 'loaiset' }
        ]
    },
    { name: 'Nhắn tin', icon: "fa-solid fa-comments", path: '/admin/messages' },
    { name: 'Khuyến mãi', icon: "fa-solid fa-tags", path: '/admin/promotions' },
    { name: 'Thống kê', icon: "fa-solid fa-chart-area", path: '/admin/statistics'}
]);

const router = useRouter();
const route = useRoute();

function handleItemClick(item) {
    if (item.children) {
        item.isOpen = !item.isOpen;
    } else {
        if (item.path) router.push(item.path);
        else if (item.routeName) router.push({ name: item.routeName });
    }
}

function navigate(name) {
    if (name) router.push({ name: name });
}

function navigateToTab(routeName, tabName) {
    router.push({ name: routeName, query: { tab: tabName } });
}

const isActive = (item) => {
    if (item.children) {
        if (item.routeName) {
            return route.name === item.routeName || route.meta?.parentMenu === item.routeName;
        }
        return false;
    }
    if (item.path) {
        return route.path === item.path;
    }

    return false;
}

const isSubActive = (tabName) => {
    return route.query.tab === tabName || 
           (tabName === 'thucdon' && !route.query.tab && route.name === 'foodManager') ||
           route.meta?.activeTab === tabName;
}
const checkAndOpenMenu = () => {
    menuItems.value.forEach(item => {
        if (item.children && isActive(item)) {
            item.isOpen = true;
        }
    });
};

onMounted(() => {
    checkAndOpenMenu();
});

watch(() => route.name, () => {
    checkAndOpenMenu();
});
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
            <div v-for="(item, index) in menuItems" :key="index">
                
                <div 
                    class="menu-item" 
                    :class="{ 'active': isActive(item) }"
                    @click="handleItemClick(item)"
                >
                    <i :class="item.icon" class="icon"></i>
                    <span class="label">{{ item.name }}</span>
                    
                    <span v-if="item.children" class="arrow-dropdown">
                        <i :class="item.isOpen ? 'fa-solid fa-chevron-up' : 'fa-solid fa-chevron-down'"></i>
                    </span>
                    <span v-else-if="isActive(item)" class="arrow"></span>
                </div>

                <div v-if="item.children && item.isOpen" class="submenu">
                    <div 
                        v-for="(child, cIndex) in item.children" 
                        :key="cIndex"
                        class="submenu-item"
                        :class="{ 'sub-active': isSubActive(child.tab) }"
                        @click="navigateToTab(item.routeName, child.tab)"
                    >
                        • {{ child.name }}
                    </div>
                </div>

            </div>
        </nav>
    </aside>
</template>

<style scoped src="../assets/base.css">
    @import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
</style>
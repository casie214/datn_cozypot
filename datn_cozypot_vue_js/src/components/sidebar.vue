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

.arrow-dropdown {
    margin-left: auto;
    font-size: 12px;
    transition: transform 0.3s;
}

/* Khối chứa menu con */
.submenu {
    padding-left: 20px; /* Thụt lề để phân cấp */
    margin-bottom: 5px;
    animation: fadeIn 0.3s ease-in-out;
}

/* Từng item con */
.submenu-item {
    padding: 10px 15px 10px 45px; /* Padding trái lớn để thụt vào so với icon cha */
    cursor: pointer;
    color: #666;
    font-size: 14px;
    transition: all 0.2s;
    border-radius: 8px;
    position: relative;
}

.submenu-item:hover {
    background-color: #f9f9f9;
    color: #7B121C; /* Đổi màu chữ khi hover */
}

/* Trạng thái Active của item con */
.submenu-item.sub-active {
    color: #7B121C; /* Chữ đỏ */
    font-weight: bold;
    background-color: #fff0f0; /* Nền hồng nhạt */
}

/* Hiệu ứng hiện ra mượt mà */
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(-5px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>

<script setup>
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from "vue-router";

const menuItems = ref([
    { name: 'Tổng quan', icon: "fa-solid fa-house", path: '/admin/dashboard' },
    { name: 'Thống kê', icon: "fa-solid fa-chart-area", path: '/admin/statistics' },
    { name: 'Đặt bàn', icon: "fa-solid fa-calendar-days", path: '/admin/tables' },
    { name: 'Check-in bàn', icon: "fa-solid fa-circle-check", path: '/admin/checkin' },
    { name: 'Quản lý bàn', icon: "fa-solid fa-chair", path: '/manage/all' },
    { name: 'Hóa đơn', icon: "fa-solid fa-cart-shopping", path: '/admin/orders' },
    { name: 'Nhân viên', icon: "fa-solid fa-user", path: '/admin/staff' },
    { name: 'Khách hàng', icon: "fa-solid fa-users", path: '/admin/client' },

    {
        name: 'Thực đơn',
        icon: 'fa-solid fa-bell-concierge',
        routeName: 'foodManager',
        isOpen: false,
        children: [
            { name: 'Danh mục', routeName: 'categoryManager', tab: 'danhmuc' },
            { name: 'Món ăn', routeName: 'foodManager', tab: 'thucdon' }
        ]
    },


    {
        name: 'Set lẩu',
        icon: "fa-solid fa-fire-burner",
        isOpen: false,
        children: [
            { name: 'Loại lẩu', routeName: 'categoryManager', tab: 'loaiset' },
            { name: 'Set lẩu', routeName: 'foodManager', tab: 'setlau' }
        ]
    },

    {
        name: 'Định lượng',
        icon: "fa-solid fa-scale-balanced",
        routeName: 'unitManager',
        query: { tab: 'dinhluong' },
        isOpen: false
    },
    
    { name: 'Giảm giá', icon: "fa-solid fa-tags", path: '/admin/voucher' },
    { name: 'Nhắn tin', icon: "fa-solid fa-comments", path: '/admin/messages' },


]);

const router = useRouter();
const route = useRoute();

function handleItemClick(item) {
    if (item.children) {
        item.isOpen = !item.isOpen;
    } else {
        if (item.path) {
            router.push(item.path);
        } else if (item.routeName) {
            // Nếu item cấp 1 có truyền query (như Định lượng)
            const queryParams = item.query ? item.query : {};
            router.push({ name: item.routeName, query: queryParams });
        }
    }
}

// Xử lý click Menu cấp 2
const handleSubClick = (parent, child) => {
    // Nếu có path tĩnh
    if (child.path) {
        router.push(child.path);
        return;
    }

    // Ưu tiên routeName của child, nếu không có mới lấy của parent
    const targetRouteName = child.routeName || parent.routeName;

    if (targetRouteName && child.tab) {
        const queryParams = { tab: child.tab };
        if (child.query) Object.assign(queryParams, child.query);
        router.push({ name: targetRouteName, query: queryParams });
    }
};

const isActive = (item) => {
    // 1. Khớp theo đường dẫn (path) - Cho phép các trang con cũng sáng menu cha
    if (item.path && (route.path === item.path || route.path.startsWith(item.path + '/'))) {
        return true;
    }

    // 2. Khớp theo tên Route (Phải đảm bảo item CÓ khai báo routeName mới đem đi so sánh)
    if (!item.children && item.routeName) {
        const isMatchRoute = route.name === item.routeName || route.meta?.parentMenu === item.routeName;

        if (isMatchRoute) {
            // Nếu menu item có trỏ đích danh một tab nào đó (VD: Định lượng)
            if (item.query && item.query.tab) {
                return item.query.tab === route.query.tab || item.query.tab === route.meta?.activeTab;
            }
            return true;
        }
    }

    if (item.children) {
        return item.children.some(child => isSubActive(item, child));
    }
    return false;
}

const isSubActive = (parent, child) => {
    if (child.path) {
        return route.path === child.path || route.path.startsWith(child.path + '/');
    }

    if (child.tab) {
        const targetRouteName = child.routeName || parent.routeName;

        // Khớp route name hiện tại hoặc thẻ meta parentMenu (dùng cho các trang Add/Update)
        const isMatchRoute = route.name === targetRouteName || route.meta?.parentMenu === targetRouteName;

        if (isMatchRoute) {
            // Ưu tiên khớp tab trên URL trước
            if (route.query.tab === child.tab) return true;

            // Nếu URL không có tab (như trang Add/Update), khớp fallback qua thẻ meta
            if (route.meta?.activeTab === child.tab) return true;
        }
    }
    return false;
};
const checkAndOpenMenu = () => {
    menuItems.value.forEach(item => {
        if (item.children && isActive(item)) {
            item.isOpen = true;
        }
    });
};

onMounted(() => {
    // Xóa gọi API fetchCategoriesAndBuildMenu() vì không cần thiết nữa
    checkAndOpenMenu();
});

watch(() => route.query, () => {
    checkAndOpenMenu();
}, { deep: true });

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

                <div class="menu-item" :class="{ 'active': isActive(item) }" @click="handleItemClick(item)">
                    <i :class="item.icon" class="icon"></i>
                    <span class="label">{{ item.name }}</span>

                    <span v-if="item.children" class="arrow-dropdown">
                        <i :class="item.isOpen ? 'fa-solid fa-chevron-up' : 'fa-solid fa-chevron-down'"></i>
                    </span>
                </div>

                <div v-if="item.children && item.isOpen" class="submenu">
                    <div v-for="(child, cIndex) in item.children" :key="cIndex">
                        <div class="submenu-item" :class="{ 'sub-active': isSubActive(item, child) }"
                            @click="handleSubClick(item, child)">
                            • {{ child.name }}
                        </div>

                    </div>
                </div>

            </div>
        </nav>
    </aside>
</template>
<style scoped>
/* GIỮ NGUYÊN TOÀN BỘ CSS CŨ CỦA BẠN TẠI ĐÂY */
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
    height: 7em;
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
    padding: 10px 20px;
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
    background-color: #7B121C;
    /* Màu đỏ đô */
    color: white;
    border-radius: 10px;
}

.icon {
    margin-right: 15px;
    width: 25px;
    text-align: center;
    font-size: 18px;
}

.arrow-dropdown {
    margin-left: auto;
    font-size: 12px;
    transition: transform 0.3s;
}

.submenu {
    padding-left: 20px;
    margin-bottom: 5px;
    animation: fadeIn 0.3s ease-in-out;
    position: relative;
    padding-top: 5px;
}

.submenu-item {
    padding: 10px 15px 10px 50px;
    cursor: pointer;
    color: #666;
    font-size: 14px;
    font-weight: 500;
    transition: all 0.2s;
    position: relative;
    display: flex;
    align-items: center;
}

.submenu-item:hover {
    color: #7B121C;
    background-color: transparent;
}

.submenu-item.sub-active {
    color: #7B121C;
    font-weight: bold;
    background-color: #fff5f5;
    border-radius: 0 20px 20px 0;
    margin-right: 10px;
    border-left: 4px solid #7B121C;
    padding-left: 46px;
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(-5px); }
    to { opacity: 1; transform: translateY(0); }
}
</style>

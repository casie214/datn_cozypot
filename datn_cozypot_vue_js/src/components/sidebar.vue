<script setup>
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from "vue-router";
import { getAllCategory } from '../services/foodFunction';

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
        children: []
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
            { name: 'Loại set lẩu', tab: 'loaiset' }
        ]
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
    if (item.routeName) {
        if (route.name === item.routeName) return true;
        if (route.meta?.parentMenu === item.routeName) return true;
    }

    if (item.path) {
        return route.path === item.path;
    }

    if (item.children) {
        return item.children.some(child => isSubActive(item, child));
    }
    return false;
}

const isSubActive = (parent, child) => {
    if (child.path) {
        return route.path === child.path;
    }

    if (child.tab) {
        if (route.meta?.activeTab === child.tab) return true;

        if (route.query.preRoot && child.query?.preRoot) {
            return String(route.query.preRoot) === String(child.query.preRoot);
        }

        if (route.query.tab === child.tab) {
            if (!route.query.preRoot && !child.query) return true;
            if (route.query.preRoot) return false;
            return true;
        }

        if (!route.query.tab && !route.meta?.activeTab && route.name === parent.routeName) {

            if (parent.children && parent.children[0].tab === child.tab) return true;
        }
    }
    return false;
};
const checkAndOpenMenu = () => {
    menuItems.value.forEach(item => {
        if (item.children && isActive(item)) {
            item.isOpen = true;

            item.children.forEach(subItem => {
                if (subItem.isGroup && subItem.children) {
                    const hasActiveChild = subItem.children.some(grandChild => isSubActive(item, grandChild));
                    if (hasActiveChild) subItem.isOpen = true;
                }
            });
        }
    });
};

const handleSubClick = (parent, child) => {
    if (child.isGroup && child.children) {
        child.isOpen = !child.isOpen;
        return;
    }

    if (child.path) {
        router.push(child.path);
    } else if (parent.routeName && child.tab) {
        const queryParams = { tab: child.tab };
        if (child.query) Object.assign(queryParams, child.query);
        router.push({ name: parent.routeName, query: queryParams });
    }
};



onMounted(async () => {
    await fetchCategoriesAndBuildMenu();
    checkAndOpenMenu();
});

watch(() => route.name, () => {
    checkAndOpenMenu();
});

const fetchCategoriesAndBuildMenu = async () => {
    try {
        const res = await getAllCategory();
        const categories = res.data || [];

        const foodMenu = menuItems.value.find(m => m.name === 'Thực đơn');

        if (foodMenu) {
            const dynamicChildren = [
                {
                    name: '• Danh sách món',
                    isGroup: true,
                    isOpen: false,
                    children: [
                        { name: 'Tất cả món', tab: 'thucdon' },

                        ...categories.map(cat => ({
                            name: `${cat.tenDanhMuc}`,
                            tab: 'thucdon',
                            query: { preRoot: cat.id, locked: 'true' }
                        }))
                    ]
                },

                { name: 'Món ăn chi tiết', tab: 'chitietTD' },
                { name: 'Set lẩu', tab: 'setlau' }
            ];

            foodMenu.children = dynamicChildren;

            checkAndOpenMenu();
        }
    } catch (error) {
        console.error("Lỗi build menu:", error);
    }
};
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

                        <div v-if="child.isGroup">
                            <div class="submenu-item group-title" @click="handleSubClick(item, child)">
                                <span style="flex: 1; font-weight: 600;">{{ child.name }}</span>
                                <i :class="child.isOpen ? 'fa-solid fa-caret-up' : 'fa-solid fa-caret-down'"
                                    style="font-size: 12px;"></i>
                            </div>

                            <div v-if="child.isOpen" class="submenu-level-3">
                                <div v-for="(grandChild, gIndex) in child.children" :key="gIndex"
                                    class="submenu-item level-3-item"
                                    :class="{ 'sub-active': isSubActive(item, grandChild) }"
                                    @click="handleSubClick(item, grandChild)">
                                    {{ grandChild.name }}
                                </div>
                            </div>
                        </div>

                        <div v-else class="submenu-item" :class="{ 'sub-active': isSubActive(item, child) }"
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
    padding-left: 20px;
    /* Thụt lề để phân cấp */
    margin-bottom: 5px;
    animation: fadeIn 0.3s ease-in-out;
}

/* Từng item con */
.submenu-item {
    padding: 10px 15px 10px 45px;
    /* Padding trái lớn để thụt vào so với icon cha */
    cursor: pointer;
    color: #666;
    font-size: 14px;
    transition: all 0.2s;
    border-radius: 8px;
    position: relative;
}

.submenu-item:hover {
    background-color: #f9f9f9;
    color: #7B121C;
    /* Đổi màu chữ khi hover */
}

/* Trạng thái Active của item con */
.submenu-item.sub-active {
    color: #7B121C;
    /* Chữ đỏ */
    font-weight: bold;
    background-color: #fff0f0;
    /* Nền hồng nhạt */
}

/* Hiệu ứng hiện ra mượt mà */
@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(-5px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.submenu {
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

.group-title {
    color: #888;
    letter-spacing: 0.5px;
    padding: 10px 15px 5px 50px;
    pointer-events: auto;
}

.group-title:hover {
    color: #333;
}

.submenu-level-3 {
    margin-left: 60px;
    border-left: 2px solid #eee;
    margin-bottom: 10px;
    padding-left: 5px;
    animation: slideDown 0.3s ease-out;
}

.level-3-item {
    padding: 8px 10px 8px 20px !important;
    /* Padding gọn hơn */
    font-size: 13.5px;
    color: #777;
    border-radius: 6px;
    margin-bottom: 2px;
    position: relative;
    transition: all 0.2s;
}

.level-3-item::before {
    content: "";
    position: absolute;
    left: 0;
    top: 50%;
    width: 15px;
    height: 2px;
    background-color: #eee;
    transform: translateY(-50%);
}

.level-3-item:hover {
    color: #7B121C;
    background-color: #fff;
    padding-left: 25px !important;
}

.level-3-item.sub-active {
    color: #7B121C;
    font-weight: 700;
    background-color: #ffebeb;
}

.level-3-item.sub-active::before {
    background-color: #7B121C;
}

@keyframes slideDown {
    from {
        opacity: 0;
        transform: translateY(-5px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>

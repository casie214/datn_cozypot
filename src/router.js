import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "./pages/guest/authentication/authenticationServices/authenticationService";
import ListReserve from "./pages/admin/table/modal/listReserve.vue";
import TableCalendar from "./pages/admin/table/modal/tableCalendar.vue";
import CardTable from "./pages/admin/table/modal/cardTable.vue";
import ListTable from "./pages/admin/table/modal/listTable.vue";
import Swal from 'sweetalert2';

const routes = [
    {
        path: "/admin/client",
        component: () => import("@/pages/admin/client/screens/clientManager.vue"),
        meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] },
        children: [
            {
                path: "", 
                name: "clientList",
                component: () => import("@/pages/admin/client/modal/clientListContent.vue"),
                meta: { index: 1 } 
            },
            {
                path: "view/:id",
                name: "clientView",
                component: () => import("@/pages/admin/client/modal/clientDetailPage.vue"),
                meta: { index: 2 } 
            },
            {
                path: "form/:id?", 
                name: "clientForm",
                component: () => import("@/pages/admin/client/modal/clientFormPage.vue"),
                meta: { index: 2 } 
            }
        ]
    },
    {
        path: "/login",
        name: "login",
        component: () => import("@/pages/guest/authentication/loginPage.vue")
    },
    {
        path: "/register",
        name: "register",
        component: () => import("@/pages/guest/authentication/registerPage.vue")
    },
    {
        path: "/admin/dashboard",
        name: "adminDashboard",
        component: () => import("@/pages/admin/adminDashboard.vue"),
        meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] }
    },

    // ================== QUẢN LÝ BÀN ==================
    {
        path: "/admin/tables",
        name: "tableManager",
        component: () => import("@/pages/admin/table/screen/tableReserveManager.vue"),
        children: [
            { path: "", component: ListReserve },
            { path: "calendar", component: TableCalendar },
        ],
        meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] }
    },
    {
        path: "/admin/checkin",
        name: "tableCheckIn",
        component: () => import("@/pages/admin/table/screen/tableCheckIn.vue"),
        meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] }
    },
    {
        path: "/manage/all",
        name: "tableManaAll",
        component: () => import("@/pages/admin/table/screen/tableManaAll.vue"),
        children: [
            {
                path: "",
                component: CardTable,
                meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] }
            },
            {
                path: "danh-sach",
                component: ListTable,
                meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] }
            },
        ],
    },

    // ================== NHÂN VIÊN ==================
    {
        path: "/admin/staff",
        component: () => import("@/pages/admin/staff/screens/staffManager.vue"),
        children: [
            {
                path: "",
                name: "staffManager",
                component: () => import("@/pages/admin/staff/modal/staffListContent.vue"),
            },
            {
                path: "form/:id?",
                name: "staffForm",
                component: () => import("@/pages/admin/staff/modal/staffFormPage.vue"),
            },
            {
                path: "view/:id",
                name: "staffView",
                component: () => import("@/pages/admin/staff/modal/staffDetailPage.vue"),
            }
        ]
    },

    // ================== THỐNG KÊ ==================
    {
        path: "/admin/statistics",
        name: "statisticsManager",
        component: () => import("@/pages/admin/statistics/screens/statisticsManager.vue"),
        meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] }
    },

    // ================== MENU & FOOD (ĐÃ LÀM PHẲNG) ==================
    {
        path: "/manage/food",
        name: "foodManager",
        component: () => import("./pages/admin/food/screens/foodManager.vue"),
        meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] }
    },
    {
        path: "/manage/unit",
        name: "unitManager",
        component: () => import("./pages/admin/unit/screens/UnitManagerAll.vue"),
        meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] }
    },
    {
        path: "/manage/category",
        name: "categoryManager",
        component: () => import("./pages/admin/category/screens/categoryManager.vue"),
        meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] }
    },
    
    // --- SET LẨU ---
    {
        path: '/manage/food/hotpot/add',
        name: 'addHotpotSet',
        component: () => import("./pages/admin/food/modal/addModal/FoodHotpotAddModal.vue"),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'setlau',
            requiresAuth: true,
            requiredRole: ['ADMIN']
        }
    },
    {
        path: '/manage/food/hotpot/update/:id',
        name: 'updateHotpotSet',
        component: () => import('./pages/admin/food/modal/updateModal/foodHotpotModal.vue'),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'setlau',
            requiresAuth: true,
            requiredRole: 'ADMIN'
        }
    },
    {
        path: '/manage/food/hotpot/view/:id',
        name: 'viewHotpotSet',
        component: () => import('./pages/admin/food/modal/updateModal/foodHotpotModal.vue'),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'setlau',
            requiresAuth: true,
            requiredRole: ['ADMIN', 'EMPLOYEE']
        }
    },

    // --- MÓN ĂN (Thay thế cho cả Món ăn và Chi tiết cũ) ---
    {
        path: '/manage/food/add',
        name: 'addFood',
        component: () => import('./pages/admin/food/modal/addModal/FoodDetailAddModal.vue'),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'thucdon',
            requiresAuth: true,
            requiredRole: 'ADMIN'
        }
    },
    
    {
        path: '/manage/food/update/:id',
        name: 'updateFood',
        component: () => import('./pages/admin/food/modal/updateModal/foodDetailModal.vue'),
        meta: {
            title: 'Cập nhật Món Ăn',
            parentMenu: 'foodManager',
            activeTab: 'thucdon',
            requiresAuth: true,
            requiredRole: 'ADMIN'
        }
    },
    {
        path: '/manage/food/view/:id',
        name: 'viewFood',
        component: () => import('./pages/admin/food/modal/updateModal/foodDetailModal.vue'),
        meta: {
            title: 'Chi tiết Món Ăn',
            parentMenu: 'foodManager',
            activeTab: 'thucdon',
            requiresAuth: true,
            requiredRole: ['ADMIN', 'EMPLOYEE']
        }
    },
    // ĐÃ XÓA: CÁC ROUTE DETAIL (addFoodDetail, updateFoodDetail, viewFoodDetail) VÌ KHÔNG CÒN CẦN THIẾT

    // ================== HÓA ĐƠN & GUEST ==================
    { path: "/", redirect: "/home" },
    {
        path: "/home",
        name: "homePage",
        component: () => import("./pages/guest/viewPages/home.vue"),
        meta: { requiresAuth: false }
    },
    {
        path: "/menu",
        name: "menu",
        component: () => import("./pages/guest/viewPages/food-menu.vue"),
        meta: { requiresAuth: false }
    },
    {
        path: "/admin/orders",
        name: "orderManager",
        component: () => import("@/pages/admin/order/screens/OrderManager.vue"),
        meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] }
    },
    {
        path: "/admin/orders/detail/:id",
        name: "OrderDetail",
        component: () => import("@/pages/admin/order/screens/OrderDetailPage.vue"),
        meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] }
    },
    {
        path: "/admin/payment/:id",
        name: "paymentScreen",
        component: () => import("@/pages/admin/order/screens/PaymentScreen.vue"),
        meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] }
    },
    {
        path: "/admin/add-food/:id",
        name: "addFoodScreen",
        component: () => import("@/pages/admin/order/screens/AddFoodScreen.vue"),
        meta: { requiresAuth: true, requiredRole: 'ADMIN' }
    },

    // ================== KHUYẾN MÃI & KHÁC ==================
    {
        path: '/admin/voucher',
        name: 'AdminVoucher',
        component: () => import('@/pages/admin/voucher/screens/voucherManager.vue'),
        meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] }
    },
    {
        path: "/admin/promotions",
        name: "voucherManager",
        component: () => import("@/pages/admin/promotion/screens/KhuyenMaiThongKe.vue"),
        meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] }
    },
    {
        path: "/admin/promotion",
        name: "promotionManager",
        component: () => import("@/pages/admin/promotion/screens/promotionManager.vue"),
        meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] }
    },
    {
        path: '/auth/google/callback',
        name: 'GoogleCallback',
        component: () => import('@/pages/guest/authentication/googleLoginCallback.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: "/admin/checkin/food",
        name: "foodCheckIn",
        component: () => import("@/pages/admin/table/modal/innerComponents/foodList.vue"),
        meta: { requiresAuth: true, requiredRole: ['ADMIN', 'EMPLOYEE'] }
    },
    {
        path: "/test",
        name: "test",
        component: () => import("./components/testConnection.vue")
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior() {
        return { top: 0 };
    }
});

// Navigation Guards (Giữ nguyên toàn bộ logic Auth của bạn)
router.beforeEach((to, from, next) => {
    const authStore = useAuthStore();
    const isLoggedIn = !!authStore.token;
    const userRole = authStore.role;

    if (to.meta.requiresAuth) {
        if (!isLoggedIn) {
            return next('/login');
        }

        if (to.meta.requiredRole) {
            const routeRoles = to.meta.requiredRole;
            let hasPermission = false;

            if (Array.isArray(routeRoles)) {
                hasPermission = routeRoles.includes(userRole);
            } else {
                hasPermission = routeRoles === userRole;
            }

            if (!hasPermission) {
                Swal.fire({
                    icon: 'error',
                    title: 'Không có quyền truy cập!',
                    text: 'Bạn không có quyền hạn để vào trang này.',
                    confirmButtonText: 'Đã hiểu',
                    confirmButtonColor: '#7d161a',
                    timer: 3000,
                    timerProgressBar: true
                });

                if (userRole === 'EMPLOYEE' || userRole === 'Nhân viên') {
                    return next(false);
                }
                return next('/');
            }
        }
    }

    if (to.path === '/login' && isLoggedIn) {
        if (userRole === 'ADMIN' || userRole === 'Quản lý') {
            return next('/admin/dashboard');
        }
        return next('/');
    }

    next();
});

export default router;
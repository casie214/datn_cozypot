import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "./pages/guest/authentication/authenticationServices/authenticationService";

const routes = [
    {
        path: "/manage/food",
        name: "foodManager",
        component: () =>

            import ("./pages/admin/food/screens/foodManager.vue"),
        meta: { requiresAuth: true, requiredRole: 'ADMIN' }
    },
    {
        path: "/admin/staff",
        name: "staffManager",
        component: () => import("@/pages/admin/staff/screens/staffManager.vue"),
        meta: { requiresAuth: true, requiredRole: 'ADMIN' }
    },
    {
        path: "/manage/category",
        name: "categoryManager",
        component: () =>

            import ("./pages/admin/category/screens/categoryManager.vue"),
        meta: { requiresAuth: true, requiredRole: 'ADMIN' }
    },
    {
        path: '/manage/food/hotpot/add',
        name: 'addHotpotSet',
        component: () =>
            import("./pages/admin/food/modal/addModal/FoodHotpotAddModal.vue"),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'setlau',
            requiresAuth: true, 
            requiredRole: 'ADMIN'
        }
    },
    {
        path: "/admin/client",
        name: "clientManager",
        component: () => import("@/pages/admin/client/screens/clientManager.vue"),
        meta: { requiresAuth: true, 
            requiredRole: 'ADMIN' }
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
        path: "/admin/promotion",
        name: "promotionManager",
        component: () => import("@/pages/admin/promotion/screens/promotionManager.vue"),
        meta: 
        { 
            requiresAuth: true, 
            requiredRole: 'ADMIN' 
        }
    },
    {
        path: "/test",
        name: "test",
        component: () =>
            import("./components/testConnection.vue")
    },
    {
        path: '/manage/food/hotpot/update/:id',
        name: 'updateHotpotSet',
        component: () =>
            import('./pages/admin/food/modal/updateModal/foodHotpotModal.vue'),
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
        component: () =>
            import('./pages/admin/food/modal/updateModal/foodHotpotModal.vue'),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'setlau',
            requiresAuth: true, 
            requiredRole: 'ADMIN'
        }
    },
    {
        path: '/manage/food/detail/add',
        name: 'addFoodDetail',
        component: () =>
            import('./pages/admin/food/modal/addModal/FoodDetailAddModal.vue'),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'chitietTD',
            requiresAuth: true, 
            requiredRole: 'ADMIN'
        }
    },
    {
        path: '/manage/food/add',
        name: 'addFood',
        component: () =>
            import('./pages/admin/food/modal/addModal/FoodAddModal.vue'),
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
        component: () =>
            import('./pages/admin/food/modal/updateModal/foodModal.vue'),
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
        component: () =>
            import('./pages/admin/food/modal/updateModal/foodModal.vue'),
        meta: {
            title: 'Chi tiết Món Ăn',
            parentMenu: 'foodManager',
            activeTab: 'thucdon',
            requiresAuth: true, 
            requiredRole: 'ADMIN'
        }
    },
    {
        path: '/manage/food/detail/update/:id',
        name: 'updateFoodDetail',
        component: () =>
            import('./pages/admin/food/modal/updateModal/foodDetailModal.vue'),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'chitietTD',
            requiresAuth: true, 
            requiredRole: 'ADMIN'
        }
    },
    {
        path: '/manage/food/detail/view/:id',
        name: 'viewFoodDetail',
        component: () =>
            import('./pages/admin/food/modal/updateModal/foodDetailModal.vue'),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'chitietTD',
            requiresAuth: true, 
            requiredRole: 'ADMIN'
        }
    },
    
    {
        path: "/",
        redirect: "/home"
    },

    {
        path: "/admin/orders",
        name: "orderManager",
        component: () => import("@/pages/admin/order/screens/OrderManager.vue"),
        meta: { requiresAuth: true, 
            requiredRole: 'ADMIN' }
    },

    {
        path: "/admin/orders/detail/:id",
        name: "OrderDetail",
        component: () => import("@/pages/admin/order/screens/OrderDetailPage.vue"),
        meta: { requiresAuth: true, 
            requiredRole: 'ADMIN' }
    },

    {
        path: "/admin/payment/:id",
        name: "paymentScreen",
        component: () => import("@/pages/admin/order/screens/PaymentScreen.vue"),
        meta: { requiresAuth: true, 
            requiredRole: 'ADMIN' }
    },

    {
        path: "/admin/add-food/:id",
        name: "addFoodScreen",
        component: () => import("@/pages/admin/order/screens/AddFoodScreen.vue"),
        meta: { requiresAuth: true, 
            requiredRole: 'ADMIN' }
    },
    {
        path: '/admin/voucher',
        name: 'AdminVoucher',

        component: () => import('@/pages/admin/voucher/screens/voucherManager.vue'),
        meta: { requiresAuth: true, 
            requiredRole: 'ADMIN' }
    },
    {
        path: "/admin/promotions",
        name: "voucherManager",
        component: () => import("@/pages/admin/promotion/screens/KhuyenMaiThongKe.vue"),
        meta: { requiresAuth: true, 
            requiredRole: 'ADMIN' }
    },

];

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior() {
        return { top: 0 };
    }
});

router.beforeEach((to, from, next) => {
    const authStore = useAuthStore();
    
    const isLoggedIn = !!authStore.token;
    const userRole = authStore.role;    


    if (to.meta.requiresAuth) {
        
        if (!isLoggedIn) {
            alert("Vui lòng đăng nhập để tiếp tục!");
            return next('/login');
        }

        if (to.meta.requiredRole) {
            if (userRole !== to.meta.requiredRole) {
                alert("Bạn không có quyền truy cập trang này!");
                return next(false); 
            }
        }
    }


    if (to.path === '/login' && isLoggedIn) {
        if (userRole === 'ADMIN') return next('/admin/dashboard');
        return next('/');
    }

    next();
});

export default router;
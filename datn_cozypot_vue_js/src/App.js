import { createRouter, createWebHistory } from "vue-router";

const routes = [
    {
        path: "/",
        redirect: "/manage/food"
    },
    {
        path: "/manage/food",
        name: "foodManager",
<<<<<<< HEAD
        component: () => import("@/pages/employee/screens/foodManager.vue")
    },
    {
        path: "/admin/staff",
        name: "staffManager",
        component: () => import("@/pages/admin/staff/screens/staffManager.vue")
    },
    // SỬA TẠI ĐÂY: Đổi path thành /admin/client cho đồng bộ với thư mục
    {
        path: "/admin/client", 
        name: "clientManager",
        component: () => import("@/pages/admin/client/screens/clientManager.vue")
    },
    {
        path: "/admin/promotion", 
        name: "promotionManager",
        component: () => import("@/pages/admin/promotion/screens/promotionManager.vue")
=======
        component: () =>
<<<<<<< HEAD
            import ("../src/pages/employee/screens/foodManager.vue")
=======
            import("../src/pages/employee/screens/foodManager.vue")
    },
    {
        path: "/manage/promotion",
        name: "promotionManager",
        component: () => 
            import("../src/pages/employee/screens/promotionManager.vue")
>>>>>>> @{-1}
>>>>>>> 1b190272e5d68a107cc64dc772e59a052d39ace8
    },
    {
        path: "/test",
        name: "test",
<<<<<<< HEAD
        component: () => import("@/components/testConnection.vue")
=======
        component: () =>
<<<<<<< HEAD
            import ("./components/testConnection.vue")
=======
            import("./components/testConnection.vue")
>>>>>>> @{-1}
>>>>>>> 1b190272e5d68a107cc64dc772e59a052d39ace8
    },
    {
        path: "/admin/voucher", 
        name: "voucherManager",
        component: () => import("@/pages/admin/voucher/screens/voucherManager.vue")
    },
    
];

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior() {
        return { top: 0 };
    }
});

export default router;
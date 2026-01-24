import { createRouter, createWebHistory } from "vue-router";

const routes = [
    {
        path: "/",
        redirect: "/manage/food"
    },
    {
        path: "/manage/food",
        name: "foodManager",
        component: () => import("@/pages/employee/screens/foodManager.vue")
    },
    {
        path: "/admin/staff",
        name: "staffManager",
        component: () => import("@/pages/admin/staff/screens/staffManager.vue")
    },
    {
        path: "/admin/client", 
        name: "clientManager",
        component: () => import("@/pages/admin/client/screens/clientManager.vue")
    },
    {
        path: "/test",
        name: "test",
        component: () => import("@/components/testConnection.vue")
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
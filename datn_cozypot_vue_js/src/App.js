import { createRouter, createWebHistory } from "vue-router";

const routes = [
    {
        path: "/manage/food",
        name: "foodManager",
        component: () =>
            import("../src/pages/employee/screens/foodManager.vue")
    },
    {
        path: "/manage/promotion",
        name: "promotionManager",
        component: () => 
            import("../src/pages/employee/screens/promotionManager.vue")
    },
    {
        path: "/test",
        name: "test",
        component: () =>
            import("./components/testConnection.vue")
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
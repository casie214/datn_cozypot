import { createRouter, createWebHistory } from "vue-router";

const routes = [
    {
        path: "/manage/food",
        name: "foodManager",
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
    },
    {
        path: "/test",
        name: "test",
        component: () =>
<<<<<<< HEAD
            import ("./components/testConnection.vue")
=======
            import("./components/testConnection.vue")
>>>>>>> @{-1}
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
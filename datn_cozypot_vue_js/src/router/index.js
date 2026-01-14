

import { createRouter, createWebHistory } from "vue-router";

const routes = [
    {
        path: "/manage/food",
        name: "foodManager",
        component: () =>
            import ("../components/screens/FoodManager.vue")
    },
    {
        path: "/test",
        name: "test",
        component: () =>
            import ("../components/WelcomeItem.vue")
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
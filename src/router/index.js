// src/router/index.js

import { createRouter, createWebHistory } from "vue-router";

const routes = [{
        path: "/manage/food",
        name: "foodManager",
        component: () =>
            import ("../components/screens/FoodManager.vue")
    },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior() {
        return { top: 0 }; // luôn scroll lên top khi đổi route
    }
});

export default router;
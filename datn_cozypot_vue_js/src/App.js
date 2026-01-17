import { createRouter, createWebHistory } from "vue-router";

const routes = [
    {
        path: "/manage/orders",
        name: "orderManager",
        component: () => 
            import("../src/pages/employee/screens/OrderManager.vue") 
    },
    {
        path: "/payment/:id", 
        name: "paymentScreen",
        component: () => import("../src/pages/employee/screens/PaymentScreen.vue")
    },
    {
    path: "/add-food/:id", 
    name: "addFoodScreen",
    component: () => import("../src/pages/employee/screens/AddFoodScreen.vue")
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
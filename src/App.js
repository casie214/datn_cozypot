import { createRouter, createWebHistory } from "vue-router";

const routes = [
    {
        path: "/manage/food",
        name: "foodManager",
        component: () =>
            import ("./pages/admin/food/screens/foodManager.vue")
    },
    {
        path: "/manage/category",
        name: "categoryManager",
        component: () =>
            import ("./pages/admin/category/screens/categoryManager.vue")
    },
    {
        path: '/manage/food/hotpot/add',
        name: 'addHotpotSet',
        component: () =>
            import ("./pages/admin/food/modal/addModal/FoodHotpotAddModal.vue")
    },
    {
        path: "/test",
        name: "test",
        component: () =>
            import ("./components/testConnection.vue")
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
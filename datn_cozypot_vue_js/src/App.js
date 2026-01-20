import { createRouter, createWebHistory } from "vue-router";

const routes = [{
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
            import ("./pages/admin/food/modal/addModal/FoodHotpotAddModal.vue"),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'setlau'
        }
    },
    {
        path: "/test",
        name: "test",
        component: () =>
            import ("./components/testConnection.vue")
    },
    {
        path: '/manage/food/hotpot/update/:id',
        name: 'updateHotpotSet',
        component: () =>
            import ('./pages/admin/food/modal/updateModal/foodHotpotModal.vue'),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'setlau'
        }
    },
    {
        path: '/manage/food/hotpot/view/:id',
        name: 'viewHotpotSet',
        component: () =>
            import ('./pages/admin/food/modal/updateModal/foodHotpotModal.vue'),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'setlau'
        }
    },
    {
        path: '/admin/food/detail/add',
        name: 'addFoodDetail',
        component: () =>
            import ('./pages/admin/food/modal/addModal/FoodDetailAddModal.vue'),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'chitietTD'
        }
    },
    {
        path: '/admin/food/add',
        name: 'addFood',
        component: () =>
            import ('./pages/admin/food/modal/addModal/FoodAddModal.vue'),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'thucdon'
        }
    },
    {
        path: '/admin/food/detail/update/:id',
        name: 'updateFoodDetail',
        component: () =>
            import ('./pages/admin/food/modal/updateModal/foodDetailModal.vue'),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'chitietTD'
        }
    }
];

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior() {
        return { top: 0 };
    }
});

export default router;
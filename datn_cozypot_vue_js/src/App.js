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
        path: '/manage/food/detail/add',
        name: 'addFoodDetail',
        component: () =>
            import ('./pages/admin/food/modal/addModal/FoodDetailAddModal.vue'),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'chitietTD'
        }
    },
    {
        path: '/manage/food/add',
        name: 'addFood',
        component: () =>
            import ('./pages/admin/food/modal/addModal/FoodAddModal.vue'),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'thucdon'
        }
    },
    {
        path: '/manage/food/update/:id',
        name: 'updateFood',
        component: () =>
            import ('./pages/admin/food/modal/updateModal/foodModal.vue'),
        meta: {
                title: 'Cập nhật Món Ăn',
                parentMenu: 'foodManager',
                activeTab: 'thucdon'
            }
    },
    {
        path: '/manage/food/view/:id',
        name: 'viewFood',
        component: () =>
            import ('./pages/admin/food/modal/updateModal/foodModal.vue'),
        meta: { 
            title: 'Chi tiết Món Ăn', 
            parentMenu: 'foodManager',
            activeTab: 'thucdon' 
        }
    },
    {
        path: '/manage/food/detail/update/:id',
        name: 'updateFoodDetail',
        component: () =>
            import ('./pages/admin/food/modal/updateModal/foodDetailModal.vue'),
        meta: {
            parentMenu: 'foodManager',
            activeTab: 'chitietTD'
        }
    },
    {
        path: '/manage/food/detail/view/:id',
        name: 'viewFoodDetail',
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
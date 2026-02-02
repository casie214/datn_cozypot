import { createRouter, createWebHistory } from "vue-router";
import ListReserve from "./pages/admin/table/modal/listReserve.vue";
import TableCalendar from "./pages/admin/table/modal/tableCalendar.vue";
import CardTable from "./pages/admin/table/modal/cardTable.vue";
import ListTable from "./pages/admin/table/modal/listTable.vue";

const routes = [
    {
        path: "/manage/food",
        name: "foodManager",
        component: () =>
            import ("./pages/admin/food/screens/foodManager.vue")
    },
    {
        path: "/admin/staff",
        name: "staffManager",
        component: () => import("@/pages/admin/staff/screens/staffManager.vue")
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
        path: "/admin/client", 
        name: "clientManager",
        component: () => import("@/pages/admin/client/screens/clientManager.vue")
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
    },
    // Table
    {
        path: "/manage/table",
        name: "tableManager",
        component: () =>
          import("@/pages/admin/table/screen/tableReserveManager.vue"),
        children: [
          {
            path: "",
            component: ListReserve,
          },
          {
            path: "calendar",
            component: TableCalendar,
          },
        ],
      },
      {
        path: "/manage/tableCheckIn",
        name: "tableCheckIn",
        component: () =>
          import("@/pages/admin/table/screen/tableCheckIn.vue"),
      },
      {
        path: "/manage/all",
        name: "tableManaAll",
        component: () =>
          import("@/pages/admin/table/screen/tableManaAll.vue"),
      },
      {
        path: "/tableManage",
        component: () =>
          import("@/pages/admin/table/screen/tableManaAll.vue"),
        children: [
          {
            path: "trang-thai",
            component: CardTable,
          },
          {
            path: "danh-sach",
            component: ListTable,
          },
        ],
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
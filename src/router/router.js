import CardTable from "../pages/employee/screens/tableMana/cardTable.vue";
import ListTable from "../pages/employee/screens/tableMana/listTable.vue";
import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: "/manage/food",
    name: "foodManager",
    component: () => import("../pages/employee/screens/foodManager.vue"),
  },
  {
    path: "/manage/table",
    name: "tableManager",
    component: () =>
      import("../pages/employee/screens/tableMana/tableManager.vue"),
  },
  {
    path: "/manage/tableCheckIn",
    name: "tableCheckIn",
    component: () =>
      import("../pages/employee/screens/tableMana/tableCheckIn.vue"),
  },
  {
    path: "/manage/all",
    name: "tableManaAll",
    component: () =>
      import("../pages/employee/screens/tableMana/tableManaAll.vue"),
  },
  {
    path: "/tableManage",
    component: () =>
      import("../pages/employee/screens/tableMana/tableManaAll.vue"),
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
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 };
  },
});

export default router;

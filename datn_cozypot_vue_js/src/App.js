import { createRouter, createWebHistory } from "vue-router";

const routes = [
  {
    path: "/",
    redirect: "/admin/orders" 
  },

  {
    path: "/admin/orders",
    name: "orderManager",
    component: () => import("@/pages/employee/screens/OrderManager.vue"),
  },

  {
    path: "/admin/orders/detail/:id", 
    name: "OrderDetail",
    component: () => import("@/pages/employee/screens/OrderDetailPage.vue"),
  },

  {
    path: "/admin/payment/:id",
    name: "paymentScreen",
    component: () => import("@/pages/employee/screens/PaymentScreen.vue"),
  },

  {
    path: "/admin/add-food/:id",
    name: "addFoodScreen",
    component: () => import("@/pages/employee/screens/AddFoodScreen.vue"),
  },

  {
    path: "/test",
    name: "test",
    component: () => import("@/components/testConnection.vue"), 
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
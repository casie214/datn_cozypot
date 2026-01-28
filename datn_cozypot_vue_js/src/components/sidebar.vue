<script setup>
import { ref } from "vue";

const menuItems = [
  { name: "Trang chủ", icon: "fa-solid fa-house", path: "/" },
  { name: "Thống kê", icon: "fa-solid fa-chart-area" , path: "/" },
  {
    name: "Phiếu đặt bàn",
    icon: "fa-solid fa-calendar-days",
    path: "/manage/table",
    active: true,
  },{
    name: "Check-in bàn",
    icon: "fa-solid fa-circle-check",
    path: "/manage/tableCheckIn",
  },
  { name: "Quản lý hóa đơn", icon: "fa-solid fa-cart-shopping", path: "/" },
  { name: "Quản lý bàn", icon: "fa-solid fa-chair", path: "/tableManage/trang-thai" },
  {
    name: "Thực đơn",
    icon: "fa-solid fa-bell-concierge",
    path: "/manage/food",
  },
  { name: "Quản lý nhân viên", icon: "fa-solid fa-user", path: "/" },
  { name: "Quản lý khách hàng", icon: "fa-solid fa-users", path: "/" },
  { name: "Khuyến mãi", icon: "fa-solid fa-tags", path: "/" },
  { name: "Nhắn tin", icon: "fa-solid fa-comments", path: "/" },
  
  
];
</script>

<template>
  <aside class="sidebar">
    <div class="logo-container">
      <div class="logo-circle">
        <span
          ><img src="../assets/images/logo_upscaled.jpg" alt="" class="logo"
        /></span>
      </div>
    </div>

    <nav class="container menu-list">
      <hr />
      <router-link
        v-for="(item, index) in menuItems"
        :key="index"
        :to="item.path"
        class="menu-item"
        active-class="active"
      >
        <i :class="item.icon" class="icon"></i>
        <span class="label">{{ item.name }}</span>
        <span class="arrow"></span>
      </router-link>
    </nav>
  </aside>
</template>

<style scoped>
/* Import thư viện icon */
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

hr {
  border: 0;
  border-top: 2px solid #7d1619; /* Chỉnh độ dày ở đây */
}
.sidebar {
  width: 250px;
  background-color: #fff;
  height: 100vh;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
  padding-top: 20px;

}

.logo-container {
  width: 100%;
  height: 7em;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-circle {
  width: 7em;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.logo {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  cursor: pointer;
  color: #555;
  font-weight: 500;
  transition: all 0.3s;
  text-decoration: none;
}

.menu-item:hover {
  background-color: #f5f5f5;
  border-radius: 15px;
}

.menu-item.active {
  background-color: #8b0000;
  color: white;
  border-radius: 15px;
}

.icon {
  margin-right: 15px;
  width: 25px;
  text-align: center;
  font-size: 18px;
}



.arrow {
  margin-left: auto;
  font-size: 10px;
}
</style>

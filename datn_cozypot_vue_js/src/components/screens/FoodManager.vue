<script setup>
import { ref, shallowRef } from 'vue';
import Sidebar from '../../components/implementations/sidebar.vue';
import MenuFood from '../../components/implementations/FoodManageGeneral.vue';   // Import file 1
import MenuHotpot from '../../components/implementations/FoodHotPotSetGeneral.vue'; // Import file 2
import FoodDetailManageGeneral from '../implementations/FoodDetailManageGeneral.vue';

// Khai báo các tab
const tabs = {
  'thucdon': MenuFood,
  'setlau': MenuHotpot,
  'chitiet': FoodDetailManageGeneral
};

// Biến lưu tab hiện tại (Mặc định là Set lẩu)
const currentTabName = ref('thucdon');
const currentComponent = shallowRef(MenuFood); 

// Hàm đổi tab
const changeTab = (tabName) => {
  currentTabName.value = tabName;
  currentComponent.value = tabs[tabName];
};
</script>

<template>
  <div class="app-layout">
    <Sidebar />

    <main class="main-content">
      <h1 class="page-title">Quản lý thực đơn</h1>

      <div class="tabs-header">
        <button 
          :class="{ active: currentTabName === 'thucdon' }" 
          @click="changeTab('thucdon')"
        >
          ☰ Thực đơn
        </button>
        <button 
          :class="{ active: currentTabName === 'setlau' }" 
          @click="changeTab('setlau')"
        >
          ☰ Set lẩu
        </button>
        <button 
          :class="{ active: currentTabName === 'chitiet' }" 
          @click="changeTab('chitiet')"
        >
          Thực đơn chi tiết
        </button>
      </div>

      <div class="dynamic-content">
        <component :is="currentComponent" />
      </div>

    </main>
  </div>
</template>

<style>
body { 
  margin: 0; 
  font-family: Arial, sans-serif; 
  background-color: #f9f9f9; 
}

.app-layout { 
  display: flex; 
  height: 100vh; 
  width: 100vw; 
  overflow: hidden; 
}

.main-content {
  flex: 1;
  padding: 20px 40px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.page-title { 
  color: #8B0000; 
  margin-bottom: 20px; 
  font-size: 24px; 
}

.tabs-header {
  border-bottom: 2px solid #ddd;
  margin-bottom: 20px;
  display: flex;
  gap: 15px;
}
.tabs-header button {
  background: none; border: none; padding: 10px 15px;
  font-size: 16px; color: #666; cursor: pointer;
  position: relative;
}
.tabs-header button:hover { color: #8B0000; }
.tabs-header button.active { color: #8B0000; font-weight: bold; }
.tabs-header button.active::after {
  content: ''; 
  position: absolute; 
  bottom: -2px; 
  left: 0;
  width: 100%; 
  height: 3px; 
  background-color: #8B0000;
}
</style>
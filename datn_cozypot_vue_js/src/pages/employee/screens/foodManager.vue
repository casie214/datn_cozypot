<script setup>
import Sidebar from '../../../components/sidebar.vue';
import { useTabManager } from './foodFunction';

const { currentTabName, currentComponent, changeTab } = useTabManager();
</script>

<template>
  

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
          :class="{ active: currentTabName === 'chitiet' }" 
          @click="changeTab('chitiet')"
        >
          ☰ Thực đơn chi tiết
        </button>
        <button 
          :class="{ active: currentTabName === 'setlau' }" 
          @click="changeTab('setlau')"
        >
          ☰ Set lẩu
        </button>
      </div>

      <div class="dynamic-content">
        <component :is="currentComponent" />
      </div>

    </main>
</template>

<style src="../screens/foodFragment/foodManager.css">
</style>
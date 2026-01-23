<script setup>
import Sidebar from '../../../components/sidebar.vue';
import { usePromotionManager } from './promotionFunction';

const { currentTabName, currentComponent, changeTab } = usePromotionManager();
</script>

<template>
  <div class="app-layout">
    <Sidebar />
    <main class="main-content">
      <h1 class="page-title">Quản lý khuyến mãi</h1>

      <div class="tabs-container">
        <div class="tabs-header">
          <button :class="{ active: currentTabName === 'thongke' }" @click="changeTab('thongke')">
            <i class="fa-solid fa-list-ul"></i> Thống kê
          </button>
          <button :class="{ active: currentTabName === 'dotkhuyenmai' }" @click="changeTab('dotkhuyenmai')">
            <i class="fa-solid fa-list-ul"></i> Quản lý đợt khuyến mãi
          </button>
          <button :class="{ active: currentTabName === 'phieugiamgia' }" @click="changeTab('phieugiamgia')">
            <i class="fa-solid fa-list-ul"></i> Quản lý phiếu giảm giá
          </button>
          <button :class="{ active: currentTabName === 'canhan' }" @click="changeTab('canhan')">
            <i class="fa-solid fa-list-ul"></i> Quản lý phiếu giảm giá cá nhân
          </button>
        </div>
      </div>

      <div class="dynamic-content">
        <component :is="currentComponent" />
      </div>
    </main>
  </div>
</template>

<style src="./promotionFragment/promotionManager.css" scoped>
</style>
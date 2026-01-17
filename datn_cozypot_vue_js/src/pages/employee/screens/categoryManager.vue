<script setup>
import { useCategoryTabManager } from '../../../services/foodFunction';

const { currentTabName, currentComponent, changeTab } = useCategoryTabManager();
</script>

<template>
    <main class="main-content">
      <h1 class="page-title">Quản lý danh mục</h1>

      <div class="tabs-header">
        <button 
          :class="{ active: currentTabName === 'danhmuc' }" 
          @click="changeTab('danhmuc')"
        >
          ☰ Danh mục
        </button>
        <button 
          :class="{ active: currentTabName === 'chitiet' }" 
          @click="changeTab('chitiet')"
        >
          ☰ Danh mục chi tiết
        </button>
        <button 
          :class="{ active: currentTabName === 'loaiset' }" 
          @click="changeTab('loaiset')"
        >
          ☰ Loại Set Lẩu
        </button>
      </div>

      <div class="dynamic-content">
        <component :is="currentComponent" />
      </div>

    </main>
</template>

<style scoped src="../screens/foodFragment/foodManager.css"></style>
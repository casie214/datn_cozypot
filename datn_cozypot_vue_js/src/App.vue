<script setup lang="ts">
import Sidebar from './components/sidebar.vue';
import Header from './components/adminHeader.vue'; // 1. Import Header
import { useRoute } from 'vue-router';
import { computed } from 'vue';
import CommonNav from './components/commonNav.vue';

const route = useRoute();

const isAdminRoute = computed(() =>{
  return route.path.startsWith('/admin') || route.path.startsWith('/manage');
})

</script>

<template>
  <div class="app-container">
    <Sidebar class="app-sidebar" v-if="isAdminRoute" />

    <div class="main-layout">
      <Header v-if="isAdminRoute"/>
      <CommonNav v-else/>

      <div class="app-content" :class="{ 'admin-content': isAdminRoute }">
        <router-view />
      </div>
    </div>
  </div>
</template>

<style>

* {
  box-sizing: border-box;
}
.navbar {
  height: 70px;
    overflow: visible !important; 
}
body, html {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.app-container {
  display: flex; 
  height: 100dvh;
  width: 100%;
  overflow: hidden;
}

.app-sidebar {
  width: 250px;
  flex-shrink: 0; 
  height: 100%;
}

.app-container {
  display: flex;
  height: 100dvh;
  width: 100%;
  overflow: hidden;
}

.app-sidebar {
  width: 250px;
  flex-shrink: 0;
  height: 100%;
}

.main-layout {
  flex-grow: 1; 
  display: flex;
  flex-direction: column;
  height: 100%; 
  overflow: hidden; 
}

.app-content {
  background-color: #fdfbfa;

  flex: 1;
  overflow-y: auto;

  transition: all 0.3s ease;
}

.app-content {
  flex-grow: 1;
  background-color: #fdfbfa;
  overflow-y: auto; 
  
  transition: all 0.3s ease;
}

.app-content::-webkit-scrollbar {
  width: 6px;
}
.app-content::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 10px;
}

.app-content.admin-content table thead tr {
  background-color: #7d161a !important;
}

.app-content.admin-content table thead th {
  background-color: #7d161a !important;
  color: #ffffff !important;
  border-color: #7d161a !important;
}
</style>

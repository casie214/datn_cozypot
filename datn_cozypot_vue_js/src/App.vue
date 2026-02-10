<script setup lang="ts">
import Sidebar from './components/sidebar.vue';
import Header from './components/adminHeader.vue'; // 1. Import Header
import { useRoute } from 'vue-router';
import { computed } from 'vue';
<<<<<<< HEAD
=======
import CommonNav from './components/commonNav.vue';
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77

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
<<<<<<< HEAD
=======
      <CommonNav v-else/>
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77

      <div class="app-content">
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
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.app-sidebar {
  width: 250px;
  flex-shrink: 0; 
  height: 100%;
}

.app-container {
  display: flex;
  height: 100vh;
  width: 100vw;
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
</style>
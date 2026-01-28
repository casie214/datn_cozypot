<script setup lang="ts">
import Sidebar from './components/sidebar.vue';

</script>

<template>
  <div class="app-container">
    <aside class="sidebar-wrapper">
      <sidebar />
    </aside>
    
    <main class="content-wrapper">
      <router-view />
    </main>
  </div>
</template>

<style>
.app-container {
  display: flex;
  height: 100vh;
}
.sidebar-wrapper {
  width: 250px;
  background: #fff;
  border-right: 1px solid #eee;
    box-shadow: 4px 0 12px rgba(0, 0, 0, 0.12);
  z-index: 10;
}
.content-wrapper {
  flex: 1;
  overflow-y: auto;
  background-color: #f4f7f6;
}
</style>
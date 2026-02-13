<script setup>
import { onMounted, ref, watch, shallowRef } from 'vue';
import { useRoute } from 'vue-router';

import UnitManager from './UnitManager.vue';

const route = useRoute();

const tabConfig = {
    'dinhluong': { 
        title: 'Quản lý định lượng', 
        component: UnitManager
    },
};

const currentComponent = shallowRef(UnitManager);
const pageTitle = ref('Quản lý món ăn');

const syncTabWithRoute = () => {
    const tabName = route.query.tab || 'dinhluong';
    const activeConfig = tabConfig[tabName] || tabConfig['dinhluong'];
    
    currentComponent.value = activeConfig.component;
    pageTitle.value = activeConfig.title;
};

watch(() => route.query.tab, () => syncTabWithRoute());
onMounted(() => syncTabWithRoute());
</script>

<template>
    <main class="main-content">
      <h1 class="page-title">{{ pageTitle }}</h1>
      <div class="dynamic-content">
        <component :is="currentComponent" />
      </div>
    </main>
</template>

<style scoped src="/src/assets/foodManager.css"></style>
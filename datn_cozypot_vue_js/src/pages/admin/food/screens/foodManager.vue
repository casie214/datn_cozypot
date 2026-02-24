<script setup>
import { onMounted, ref, watch, shallowRef } from 'vue';
import { useRoute } from 'vue-router';

import FoodHotPotSetGeneral from './FoodHotPotSetGeneral.vue';
import FoodDetailManageGeneral from './FoodDetailManageGeneral.vue';

const route = useRoute();

const tabConfig = {
    'thucdon': { 
        title: 'Quản lý món ăn', 
        component: FoodDetailManageGeneral
    },
    'setlau': { 
        title: 'Quản lý set lẩu', 
        component: FoodHotPotSetGeneral 
    }
};

const currentComponent = shallowRef(FoodDetailManageGeneral);
const pageTitle = ref('Quản lý món ăn');

const syncTabWithRoute = () => {
    const tabName = route.query.tab || 'thucdon';
    const activeConfig = tabConfig[tabName] || tabConfig['thucdon'];
    
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
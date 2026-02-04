<script setup>
import { onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useCategoryTabManager } from '../../../../services/foodFunction'; 

const route = useRoute();
const { currentTabName, currentComponent, changeTab } = useCategoryTabManager();

const syncTabWithRoute = () => {
    const tabName = route.query.tab;
    if (tabName) {
        changeTab(tabName);
    } else {
        changeTab('danhmuc');
    }
};

watch(() => route.query.tab, () => {
    syncTabWithRoute();
});

onMounted(() => {
    syncTabWithRoute();
});
</script>

<template>
    <main class="main-content">
      <h1 class="page-title">Quản lý danh mục</h1>

      <div class="dynamic-content">
        <component :is="currentComponent" />
      </div>

    </main>
</template>

<style scoped src="/src/assets/foodManager.css"></style>
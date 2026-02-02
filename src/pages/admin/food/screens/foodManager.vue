<script setup>
import { watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { useTabManager } from '../../../../services/foodFunction';


const route = useRoute();
const { currentTabName, currentComponent, changeTab } = useTabManager();

const syncTabWithRoute = () => {
    const tabName = route.query.tab;
    if (tabName) {
        changeTab(tabName);
    } else {
        changeTab('thucdon');
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

      <div class="dynamic-content">
        <component :is="currentComponent" />
      </div>

    </main>
</template>

<style src="/src/assets/foodManager.css">
</style>
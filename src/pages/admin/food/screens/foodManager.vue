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
      <h1 class="page-title" style="padding-left: 0;">Quản lý thực đơn</h1>
      <h5 style="margin-bottom: 1em;" v-if="currentTabName == 'thucdon'">Quản lý thực đơn / Thực đơn</h5>
      <h5 style="margin-bottom: 1em;" v-else-if="currentTabName == 'chitietTD'">Quản lý thực đơn / Thực đơn chi tiết</h5>
      <h5 style="margin-bottom: 1em;" v-if="currentTabName == 'setlau'">Quản lý thực đơn / Set lẩu</h5>

      <div class="dynamic-content">
        <component :is="currentComponent" />
      </div>

    </main>
</template>

<style src="/src/assets/foodManager.css">
</style>
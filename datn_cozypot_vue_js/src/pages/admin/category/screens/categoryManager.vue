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
      <h5 style="margin-bottom: 1em;" v-if="currentTabName == 'danhmuc'">Quản lý danh mục / Danh mục</h5>
      <h5 style="margin-bottom: 1em;" v-else-if="currentTabName == 'chitietDM'">Quản lý danh mục / Danh mục chi tiết</h5>
      <h5 style="margin-bottom: 1em;" v-if="currentTabName == 'loaiset'">Quản lý danh mục / Loại set lẩu</h5>

      <div class="dynamic-content">
        <component :is="currentComponent" />
      </div>

    </main>
</template>

<style scoped src="/src/assets/foodManager.css"></style>
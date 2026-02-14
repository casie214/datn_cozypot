<script setup>
import { onMounted, ref, watch, shallowRef } from 'vue';
import { useRoute } from 'vue-router';

// 1. Import các màn hình con tương ứng
import CategoryGeneral from './CategoryGeneral.vue';
import CategoryHotpotGeneral from './CategoryHotpotGeneral.vue';
// import UnitGeneral from './UnitGeneral.vue'; // (Nếu bạn có làm riêng 1 file cho Định lượng)

const route = useRoute();

// 2. Cấu hình danh sách các Tab (Khớp với query.tab trên Sidebar)
const tabConfig = {
    'danhmuc': { 
        title: 'Quản lý danh mục', 
        component: CategoryGeneral 
    },
    'loaiset': { 
        title: 'Quản lý loại set lẩu', 
        component: CategoryHotpotGeneral 
    },
    // 'dinhluong': {
    //     title: 'Quản lý Định lượng',
    //     component: UnitGeneral
    // }
};

// 3. State quản lý hiển thị
const currentComponent = shallowRef(CategoryGeneral);
const pageTitle = ref('Quản lý danh mục');

// 4. Hàm đồng bộ URL với Giao diện
const syncTabWithRoute = () => {
    const tabName = route.query.tab || 'danhmuc'; // Mặc định là danhmuc
    const activeConfig = tabConfig[tabName] || tabConfig['danhmuc'];
    
    currentComponent.value = activeConfig.component;
    pageTitle.value = activeConfig.title;
};

// Lắng nghe sự thay đổi của URL (khi bấm trên Sidebar)
watch(() => route.query.tab, () => {
    syncTabWithRoute();
});

onMounted(() => {
    syncTabWithRoute();
});
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
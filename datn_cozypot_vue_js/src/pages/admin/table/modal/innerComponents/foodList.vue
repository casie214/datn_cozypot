<script setup>
import { getAllCategoryDetailActive, getAllCategoryGeneralActive, getAllFoodGeneralActive, getAllHotpotGeneralActive } from '@/services/tableManageService';
import Multiselect from '@vueform/multiselect';
import { watch } from 'vue';
import { ref, computed, onMounted } from 'vue';

const isLoading = ref(false);

const props = defineProps({
    initialItems: {
        type: Array,
        default: () => []
    }
});

const initSelectedItems = () => {
    // Reset trước khi init
    selectedItems.value = {};

    if (props.initialItems && props.initialItems.length > 0) {
        props.initialItems.forEach(item => {
            let key = item.uniqueId;

            if (!key && item.originalId && item.type) {
                key = item.type === 'FOOD' 
                      ? `food_${item.originalId}` 
                      : `set_${item.originalId}`;
                
                item.uniqueId = key;
            }

            if (key) {
                selectedItems.value[key] = { ...item };
            }
        });
    }
};

const fullMenuList = ref([]);
const categories = ref([]);
const subCategories = ref([]);

const searchQuery = ref('');
const filterType = ref(null);
const filterCategory = ref(null);
const filterSubCategory = ref(null);
const selectedItems = ref({});

const emit = defineEmits(['close', 'save']);

const getImg = (url) => {
    if (url && (url.startsWith('http') || url.startsWith('data:image'))) {
        return url;
    }
    return 'https://placehold.co/150x150?text=No+Img';
}

const typeOptions = [
    { value: 'ALL', label: 'Tất cả loại' },
    { value: 'FOOD', label: 'Món ăn lẻ' },
    { value: 'SET', label: 'Set Lẩu' }
];

const categoryOptions = computed(() => {
    return categories.value.map(c => ({
        value: c.id,
        label: c.tenDanhMuc
    }));
});

const subCategoryOptions = computed(() => {
    if (!filterCategory.value) return [];

    const filtered = subCategories.value.filter(sub => {
        const parentId = sub.idDanhMuc || (sub.danhMuc ? sub.danhMuc.id : null);

        return String(parentId) === String(filterCategory.value);
    });

    return filtered.map(s => ({
        value: s.id,
        label: s.tenDanhMucChiTiet
    }));
});

const handleCategoryChange = () => {
    filterSubCategory.value = null;
};

const fetchAllData = async () => {
    isLoading.value = true;
    try {
        const [resFood, resSet, resCat, resSubCat] = await Promise.all([
            getAllFoodGeneralActive(),
            getAllHotpotGeneralActive(),
            getAllCategoryGeneralActive(),
            getAllCategoryDetailActive()
        ]);

        const foods = (resFood.data || []).map(item => ({
            ...item,
            type: 'FOOD',
            uniqueId: `food_${item.id}`,
            catId: item.danhMuc?.id || item.idDanhMuc,
            subCatId: item.danhMucChiTiet?.id || item.idDanhMucChiTiet
        }));

        const sets = (resSet.data || []).map(item => ({
            ...item,
            tenMonAn: item.tenSetLau,
            type: 'SET',
            uniqueId: `set_${item.id}`,
            catId: item.loaiSet?.id || item.idLoaiSet,
            subCatId: null
        }));

        fullMenuList.value = [...sets, ...foods];

        categories.value = resCat.data || [];
        subCategories.value = resSubCat.data || [];

    } catch (e) {
        console.error("Lỗi tải dữ liệu:", e);
    } finally {
        isLoading.value = false;
    }
};

onMounted(() => {
    fetchAllData();
    initSelectedItems();
})

watch(() => props.initialItems, () => {
    initSelectedItems();
}, { deep: true });

const filteredMenu = computed(() => {
    return fullMenuList.value.filter(item => {
        const name = (item.tenMonAn || item.tenChiTietMonAn || '').toLowerCase();
        const matchesSearch = name.includes(searchQuery.value.toLowerCase().trim());

        let matchesType = true;
        if (filterType.value && filterType.value !== 'ALL') {
            matchesType = item.type === filterType.value;
        }

        let matchesCat = true;
        if (filterCategory.value) {
            matchesCat = item.catId == filterCategory.value;
        }

        let matchesSubCat = true;
        if (filterSubCategory.value) {
            matchesSubCat = item.subCatId == filterSubCategory.value;
        }

        return matchesSearch && matchesType && matchesCat && matchesSubCat;
    });
});

const addItem = (item) => {
    const id = item.uniqueId;
    if (!selectedItems.value[id]) {
        selectedItems.value[id] = {
            uniqueId: id,
            originalId: item.id,
            type: item.type,
            name: item.tenMonAn || item.tenChiTietMonAn,
            price: item.giaBan,
            img: item.hinhAnh,
            unit: item.donVi || 'Suất',
            quantity: 1
        };
    } else {
        selectedItems.value[id].quantity++;
    }
};

const decreaseItem = (uniqueId) => {
    if (selectedItems.value[uniqueId]) {
        selectedItems.value[uniqueId].quantity--;
        if (selectedItems.value[uniqueId].quantity <= 0) {
            delete selectedItems.value[uniqueId];
        }
    }
};

const totalAmount = computed(() => {
    return Object.values(selectedItems.value).reduce((sum, item) => sum + (item.price * item.quantity), 0);
});

const handleSave = () => {
    const itemsArray = Object.values(selectedItems.value);
    emit('save', itemsArray);

};


</script>

<template>
    <div class="add-food-container">

        <div class="toolbar">
            <div class="search-box">
                <input v-model="searchQuery" type="text" placeholder="Tìm tên món..." />
                <i class="fas fa-search search-icon"></i>
            </div>

            <div class="filters-row">

                <div class="filter-item">
                    <Multiselect v-model="filterType" :options="typeOptions" placeholder="Loại món" :searchable="true"
                        class="custom-multiselect" />
                </div>

                <div class="filter-item">
                    <Multiselect v-model="filterCategory" :options="categoryOptions" placeholder="Danh mục"
                        :searchable="true" class="custom-multiselect" @change="handleCategoryChange" />
                </div>

                <div class="filter-item">
                    <Multiselect v-model="filterSubCategory" :options="subCategoryOptions" placeholder="Chi tiết"
                        :searchable="true" class="custom-multiselect" :disabled="!filterCategory" />
                </div>
            </div>
        </div>

        <div class="menu-grid-container">
            <div v-if="isLoading" class="loading-state">
                <div class="spinner"></div>
                <p>Đang tải thực đơn...</p>
            </div>

            <div v-else-if="filteredMenu.length === 0" class="empty-state">
                <i class="fas fa-utensils"></i>
                <p>Không tìm thấy món ăn nào.</p>
            </div>

            <div class="menu-grid" v-else>
                <div v-for="item in filteredMenu" :key="item.uniqueId" class="grid-card"
                    :class="{ 'selected': selectedItems[item.uniqueId] }">

                    <span class="badge-type" :class="item.type">{{ item.type === 'SET' ? 'Set' : 'Món' }}</span>

                    <div class="card-img">
                        <img :src="getImg(item.hinhAnh)" loading="lazy">
                        <div v-if="selectedItems[item.uniqueId]" class="qty-overlay">
                            <span class="qty-large">{{ selectedItems[item.uniqueId].quantity }}</span>
                        </div>
                    </div>

                    <div class="card-body">
                        <div class="card-title" :title="item.tenMonAn">{{ item.tenMonAn }}</div>
                        <div class="card-meta">
                            <span class="card-unit">{{ item.donVi || 'Phần' }}</span>
                            <span class="card-price">{{ item.giaBan?.toLocaleString() }}đ</span>
                        </div>
                    </div>

                    <div class="card-actions">
                        <div v-if="selectedItems[item.uniqueId]" class="qty-control-grid">
                            <button @click.stop="decreaseItem(item.uniqueId)">-</button>
                            <button @click.stop="addItem(item)">+</button>
                        </div>
                        <button v-else class="btn-add-grid" @click.stop="addItem(item)">
                            Thêm
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div class="footer-actions">
            <div class="total-info">
                <span>Đã chọn: <b>{{ Object.keys(selectedItems).length }}</b> món</span>
                <div class="price-group">
                    <span>Tổng:</span>
                    <strong class="price">{{ totalAmount.toLocaleString() }}đ</strong>
                </div>
            </div>
            <div class="buttons">
                <button class="btn-cancel" @click="$emit('close')">Đóng</button>
                <button class="btn-save" @click="handleSave" :disabled="totalAmount === 0">
                    Xác nhận
                </button>
            </div>
        </div>
    </div>
</template>

<style scoped>
.add-food-container {
    display: flex;
    flex-direction: column;
    height: 100%;
    background-color: #f8f9fa;
}

/* --- TOOLBAR --- */
.toolbar {
    padding: 10px 0px 20px 0;
    background: white;
    border-bottom: 1px solid #eee;
}

.search-box {
    position: relative;
    margin-bottom: 10px;
}

.search-box input {
    width: 100%;
    padding: 10px 35px 10px 15px;
    border: 1px solid #ddd;
    border-radius: 8px;
    outline: none;
    transition: border 0.2s;
}

.search-box input:focus {
    border-color: #8B0000;
}

.search-icon {
    position: absolute;
    right: 15px;
    top: 50%;
    transform: translateY(-50%);
    color: #999;
}

.filters-row {
    display: flex;
    gap: 10px;
}

.filter-select {
    flex: 1;
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 13px;
    cursor: pointer;
    background: white;
    min-width: 0;
    /* Tránh tràn layout flex */
}

/* --- GRID LAYOUT --- */
.menu-grid-container {
    flex: 1;
    overflow-y: auto;
    padding: 1.5rem;
}

.menu-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 15px;
}

.grid-card {
    background: white;
    border: 1px solid #eee;
    border-radius: 8px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    position: relative;
    transition: transform 0.2s, box-shadow 0.2s;
}

.grid-card:hover {
    transform: translateY(-3px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    border-color: #f0f0f0;
}

.grid-card.selected {
    border: 1px solid #8B0000;
}

/* Badge Loại */
.badge-type {
    position: absolute;
    top: 5px;
    left: 5px;
    font-size: 10px;
    padding: 2px 6px;
    border-radius: 4px;
    color: white;
    font-weight: bold;
    z-index: 10;
}

.badge-type.FOOD {
    background-color: #28a745;
}

.badge-type.SET {
    background-color: #ff9800;
}

.card-img {
    height: 100px;
    width: 100%;
    position: relative;
    background: #fafafa;
}

.card-img img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.qty-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(139, 0, 0, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
}

.qty-large {
    color: white;
    font-size: 24px;
    font-weight: bold;
    border: 2px solid white;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.card-body {
    padding: 10px;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.card-title {
    font-size: 13px;
    font-weight: 600;
    color: #333;
    margin-bottom: 5px;
    line-height: 1.3;
    /* Cắt dòng nếu dài quá */
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.card-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.card-unit {
    font-size: 11px;
    color: #888;
}

.card-price {
    font-size: 13px;
    font-weight: bold;
    color: #d32f2f;
}

.card-actions {
    padding: 8px;
    border-top: 1px solid #f5f5f5;
}

.btn-add-grid {
    width: 100%;
    padding: 6px;
    background: white;
    border: 1px solid #8B0000;
    color: #8B0000;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: 0.2s;
}

.btn-add-grid:hover {
    background: #8B0000;
    color: white;
}

.qty-control-grid {
    display: flex;
    gap: 5px;
}

.qty-control-grid button {
    flex: 1;
    padding: 6px;
    border: 1px solid #ddd;
    background: #f9f9f9;
    border-radius: 4px;
    font-weight: bold;
    cursor: pointer;
}

.qty-control-grid button:hover {
    background: #e0e0e0;
}

/* Footer */
.footer-actions {
    padding: 5px;
    background: white;
    border-top: 1px solid #ddd;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.total-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    font-size: 14px;
}

.price-group {
    display: flex;
    align-items: center;
    gap: 5px;
}

.price {
    font-size: 18px;
    color: #d32f2f;
}

.buttons {
    display: flex;
    gap: 10px;
}

.buttons button {
    flex: 1;
    padding: 12px;
    border-radius: 6px;
    font-weight: bold;
    border: none;
    cursor: pointer;
}

.btn-cancel {
    background: #eee;
    color: #555;
}

.btn-save {
    background: #8B0000;
    color: white;
}

.btn-save:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.empty-state {
    text-align: center;
    padding: 40px;
    color: #999;
}

.empty-state i {
    font-size: 40px;
    margin-bottom: 10px;
    color: #ddd;
}

.filters-row {
    display: flex;
    gap: 10px;
    align-items: center;
}

.filter-item {
    flex: 1;
    min-width: 150px;
    max-width: 200px;
}

:deep(.multiselect.is-disabled) {
    background-color: #e9ecef;
    cursor: not-allowed;
    opacity: 0.7;
}

:deep(.multiselect) {
    min-height: 38px;
    border: 1px solid #ced4da;
    border-radius: 0.375rem;
}

:deep(.multiselect.is-active) {
    box-shadow: 0 0 0 3px rgba(211, 47, 47, 0.25);
    border-color: #d32f2f;
}

:deep(.multiselect-option.is-selected) {
    background: #d32f2f;
}

:deep(.multiselect-option.is-selected.is-pointed) {
    background: #b71c1c;
}

:deep(.multiselect-placeholder) {
    color: #495057;
}

.loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    /* Căn giữa màn hình */
    color: #666;
    padding-top: 50px;
}

.spinner {
    width: 40px;
    height: 40px;
    border: 4px solid #f3f3f3;
    /* Màu xám nhạt */
    border-top: 4px solid #7d161a;
    /* Màu đỏ chủ đạo */
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 15px;
}

@keyframes spin {
    0% {
        transform: rotate(0deg);
    }

    100% {
        transform: rotate(360deg);
    }
}
</style>
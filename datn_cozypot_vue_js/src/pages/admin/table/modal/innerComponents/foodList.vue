<script setup>
import { 
    getAllCategoryDetailActive, 
    getAllCategoryGeneralActive, 
    getAllHotpotGeneralActive,
    getAllLoaiSetLauActive // Đảm bảo service này trả về danh sách loại set
} from '@/services/tableManageService';
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';
import { watch, ref, computed, onMounted } from 'vue';
import Swal from 'sweetalert2';
import axiosClient from "@/services/axiosClient";

const isLoading = ref(false);
const props = defineProps({
    initialItems: { type: Array, default: () => [] }
});

const emit = defineEmits(['close', 'save']);

// --- STATE DỮ LIỆU ---
const fullMenuList = ref([]);
const categories = ref([]);     // Danh mục cho Món lẻ (Thịt, Nước, Rau...)
const setCategories = ref([]);  // Danh mục cho Set lẩu (Lẩu 2 người, Lẩu Thái...)
const selectedItems = ref({});

// --- BỘ LỌC ---
const searchQuery = ref('');
const filterType = ref('ALL');
const filterCategory = ref(null);
const filterSetCategory = ref(null);

const initSelectedItems = () => {
    selectedItems.value = {};
    if (props.initialItems && props.initialItems.length > 0) {
        props.initialItems.forEach(item => {
            const key = item.uniqueId;
            if (key) {
                selectedItems.value[key] = { 
                    ...item,
                    tenMonAn: item.tenMonAn || item.name || item.tenMon 
                };
            }
        });
    }
};

const fetchAllData = async () => {
    isLoading.value = true;
    try {
        // GỌI 4 API SONG SONG ĐỂ LẤY ĐỦ DỮ LIỆU BỘ LỌC VÀ MENU
        const [resSet, resCat, resDetail, resLoaiSet] = await Promise.all([
            getAllHotpotGeneralActive(),
            getAllCategoryGeneralActive(),
            getAllCategoryDetailActive(),
            getAllLoaiSetLauActive() // Gọi trực tiếp API lấy Loại Set
        ]);

        // 1. Gán dữ liệu cho các bộ lọc
        categories.value = resCat.data || [];
        setCategories.value = (resLoaiSet.data || []).map(ls => ({
            value: ls.id,
            label: ls.tenLoaiSet
        }));

        // 2. Map dữ liệu MÓN LẺ
        const foods = (resDetail.data || []).map(item => ({
            ...item,
            id: item.id || item.idDanhMucChiTiet,
            type: 'FOOD',
            uniqueId: `food_${item.id || item.idDanhMucChiTiet}`,
            tenMonAn: item.tenDanhMucChiTiet, // Tên hiển thị chính
            catId: item.idDanhMuc || (item.danhMuc ? item.danhMuc.id : null),
            donVi: item.dinhLuong?.tenDinhLuong || 'Phần'
        }));

        // 3. Map dữ liệu SET LẨU
        const sets = (resSet.data || []).map(item => ({
            ...item,
            id: item.id || item.idSetLau,
            type: 'SET',
            uniqueId: `set_${item.id || item.idSetLau}`,
            tenMonAn: item.tenSetLau, // Tên hiển thị chính
            setCatId: item.loaiSet?.id || item.idLoaiSet,
            donVi: 'Set' 
        }));

        fullMenuList.value = [...sets, ...foods];
    } catch (e) {
        console.error("Lỗi tải dữ liệu Menu:", e);
    } finally {
        isLoading.value = false;
    }
};

onMounted(() => {
    fetchAllData();
    initSelectedItems();
});

watch(() => props.initialItems, () => initSelectedItems(), { deep: true });

// --- LOGIC LỌC MENU ---
const filteredMenu = computed(() => {
    return fullMenuList.value.filter(item => {
        const name = (item.tenMonAn || '').toLowerCase();
        const matchesSearch = name.includes(searchQuery.value.toLowerCase().trim());

        // Lọc theo Loại (Food/Set)
        const matchesType = (filterType.value === 'ALL') || (item.type === filterType.value);

        // Lọc theo Danh mục món lẻ
        let matchesCat = true;
        if (filterCategory.value) {
            matchesCat = item.type === 'FOOD' && String(item.catId) === String(filterCategory.value);
        }

        // Lọc theo Loại Set lẩu
        let matchesSetCat = true;
        if (filterSetCategory.value) {
            matchesSetCat = item.type === 'SET' && String(item.setCatId) === String(filterSetCategory.value);
        }

        return matchesSearch && matchesType && matchesCat && matchesSetCat;
    });
});

const addItem = (item) => {
    const id = item.uniqueId;
    if (!selectedItems.value[id]) {
        selectedItems.value[id] = {
            uniqueId: id,
            originalId: item.id,
            type: item.type,
            name: item.tenMonAn, // Đồng bộ key cho modal cha
            tenMonAn: item.tenMonAn,
            price: item.giaBan,
            img: item.hinhAnh,
            unit: item.donVi,
            quantity: 1
        };
    } else {
        selectedItems.value[id].quantity++;
    }
};

const decreaseItem = (uniqueId) => {
    if (selectedItems.value[uniqueId]) {
        selectedItems.value[uniqueId].quantity--;
        
        // Khi số lượng chạm mốc 0, chỉ xóa khỏi giỏ hàng trên màn hình
        if (selectedItems.value[uniqueId].quantity <= 0) {
            delete selectedItems.value[uniqueId];
            
            // 🚨 KHÔNG GỌI API XÓA Ở ĐÂY NỮA
            // Khi bấm "Xác nhận", Backend sẽ tự so sánh và Xóa món này giúp bạn.
        }
    }
};

const totalAmount = computed(() => {
    return Object.values(selectedItems.value).reduce((sum, item) => sum + (item.price * item.quantity), 0);
});

const handleSave = () => {
    emit('save', Object.values(selectedItems.value));
};

const getImg = (url) => {
    if (url && (url.startsWith('http') || url.startsWith('data:image'))) return url;
    return 'https://placehold.co/150x150?text=No+Img';
};

const typeOptions = [
    { value: 'ALL', label: 'Tất cả loại' },
    { value: 'FOOD', label: 'Món ăn lẻ' },
    { value: 'SET', label: 'Set Lẩu' }
];

const categoryOptions = computed(() => categories.value.map(c => ({ value: c.id, label: c.tenDanhMuc })));
const setCategoryOptions = computed(() => setCategories.value); // Sử dụng ref trực tiếp từ API
</script>

<template>
    <div class="add-food-container">

        <div class="toolbar">
            <div class="search-box">
                <input v-model="searchQuery" type="text" placeholder="Tìm tên món/set lẩu..." />
                <i class="fas fa-search search-icon"></i>
            </div>

            <div class="filters-row">
                <div class="filter-item">
                    <Multiselect v-model="filterType" :options="typeOptions" placeholder="Loại món" />
                </div>

                <div class="filter-item" v-if="filterType !== 'SET'">
                    <Multiselect v-model="filterCategory" :options="categoryOptions" placeholder="Danh mục món lẻ" :searchable="true" />
                </div>

                <div class="filter-item" v-if="filterType !== 'FOOD'">
                    <Multiselect v-model="filterSetCategory" :options="setCategoryOptions" placeholder="Loại Set lẩu" :searchable="true" />
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
                        <div class="card-title" :title="item.tenMon">{{ item.tenMonAn || item.tenMon }}</div>
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
    align-items: center;
}

.filter-item {
    flex: 1;
    min-width: 150px;
}

/* --- GRID LAYOUT --- */
.menu-grid-container {
    flex: 1;
    overflow-y: auto;
    padding: 1.5rem;
}

.menu-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); /* Chỉnh nhỏ lại để list trông vừa vặn hơn trên Modal */
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
    height: 120px;
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
    font-size: 14px;
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
    padding: 10px 15px;
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
    color: #666;
    padding-top: 50px;
}

.spinner {
    width: 40px;
    height: 40px;
    border: 4px solid #f3f3f3;
    border-top: 4px solid #7d161a;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 15px;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}
</style>
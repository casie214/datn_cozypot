<script setup>
import foodModal from '../../food/modal/updateModal/foodModal.vue';
import { useFoodManager, usePriceFilter } from '../../../../services/foodFunction';
import FoodAddModal from '../../food/modal/addModal/FoodAddModal.vue';
import Slider from '@vueform/slider';
import "@vueform/slider/themes/default.css";
import { useRouter } from 'vue-router';
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';
import CommonPagination from '@/components/commonPagination.vue';

const router = useRouter();

const {
    mockData,
    paginatedData,
    activeTab,
    isModalOpen,
    selectedItem,
    isAddFoodModalOpen,
    itemsPerPage,
    currentPage,
    totalPages,
    totalElements,
    visiblePages,
    goToPage,
    handleViewDetails,
    goToDetailTable,
    getAllFood,
    handleToggleStatus,
    handleEditFood,

    // Lọc & Sort cũ
    searchQuery,
    sortOption,
    statusFilter,
    clearFilters,

    // 2. LẤY BIẾN CHO THANH KÉO GIÁ (MỚI)
    selectedPriceRange,
    globalMinPrice,
    globalMaxPrice,
    isCategoryFilterOpen,
    listRootCategories,
    selectedRootCate,
    selectedSubCate,
    availableSubCategories,
    exportToExcel,
    isCategoryLocked
} = useFoodManager();

const goToAddScreen = () => {
    router.push({ name: 'addFood' });
};

const handleRefreshList = () => {
    setTimeout(() => {
        getAllFood();
    }, 500);
};

const formatPriceRange = (item) => {
    const min = item.giaThapNhat || 0;
    const max = item.giaCaoNhat || 0;
    if (min === 0 && max === 0) return 'Chưa cập nhật';
    if (min === max) return min.toLocaleString() + ' đ';
    return `${min.toLocaleString()} - ${max.toLocaleString()} đ`;
};

const getImg = (url) => {
    if (url && (url.startsWith('http') || url.startsWith('data:image'))) {
        return url;
    }
    return 'https://placehold.co/100x100?text=No+Img';
}
</script>

<template>
    <div class="flex-row">
        <h1 class="page-title" style="padding-left: 0;">Quản lý thực đơn</h1>
        <div class="action-row">
            <button class="btn-add" @click="goToAddScreen">+ Thêm món ăn</button>
            <button class="btn-excel" @click="exportToExcel" title="Xuất danh sách hiện tại ra Excel">
                <i class="fas fa-file-excel"></i> Xuất Excel
            </button>
        </div>
    </div>
    <div class="filter-box">
        <div class="filter-row">
            <div class="filter-item search">
                <label>Tìm kiếm</label>
                <div class="input-group">
                    <input v-model="searchQuery" type="text" class="form-search form-control"
                        placeholder="Tìm kiếm món (mã, tên)" />
                    <button class="search-btn">🔍</button>
                </div>
            </div>

            <div class="filter-item">
                <label>Trạng thái</label>
                <select v-model="statusFilter" class="form-control">
                    <option value="all">Tất cả</option>
                    <option value="1">Đang kinh doanh</option>
                    <option value="0">Ngưng kinh doanh</option>
                </select>
            </div>

            <div class="filter-item">
                <label>Sắp xếp theo</label>
                <select v-model="sortOption" class="form-control">
                    <option value="newest">Mới nhất</option>
                    <option value="name_asc">Tên (A-Z)</option>
                    <option value="price_asc">Giá tăng dần</option>
                    <option value="price_desc">Giá giảm dần</option>
                </select>
            </div>

            <div class="filter-item">
                <label>Danh mục gốc</label>
                <div class="multiselect-wrapper">
                    <Multiselect v-model="selectedRootCate" :options="listRootCategories" valueProp="id"
                        label="tenDanhMuc" placeholder="-- Tất cả --" :searchable="true" :canClear="true" :disabled="isCategoryLocked"
                        @change="selectedSubCate = null" noOptionsText="Không có dữ liệu"
                        noResultsText="Không tìm thấy" />
                </div>
            </div>

            <div class="filter-item">
                <label>Danh mục chi tiết</label>
                <div class="multiselect-wrapper">
                    <Multiselect v-model="selectedSubCate" :options="availableSubCategories" valueProp="id"
                        label="tenDanhMucChiTiet" placeholder="-- Tất cả --" :searchable="true" :canClear="true"
                        :disabled="!selectedRootCate" noOptionsText="Vui lòng chọn danh mục gốc trước"
                        noResultsText="Không tìm thấy" />
                </div>
            </div>

            <button class="btn-clear" @click="clearFilters">Xóa bộ lọc</button>
        </div>
    </div>


    <div class="table-container" style="min-height: 278px;">
        <table>
            <thead>
                <tr>
                    <th>STT</th>
                    <th>MÃ</th>
                    <th>MÓN ĂN</th>
                    <th>GIÁ BÁN</th>
                    <th>DANH MỤC</th>
                    <th>CHI TIẾT</th>
                    <th>TRẠNG THÁI</th>
                    <th>CHỨC NĂNG</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(item, index) in paginatedData" :key="item.id">

                    <td>{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>

                    <td>{{ item.maMonAn }}</td>
                    <td>{{ item.tenMonAn }}</td>
                    <td>{{ formatPriceRange(item) }}</td>
                    <td>{{ item.tenDanhMuc }}</td>
                    <td>{{ item.tenDanhMucChiTiet }}</td>

                    <td :class="item.trangThaiKinhDoanh === 1 ? 'status-active' : 'status-inactive'">
                        {{ item.trangThaiKinhDoanh === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
                    </td>
                    <td class="actions">
                        <div class="action-group">
                            <i style="cursor:pointer" class="fa-solid fa-list" title="Xem chi tiết"
                                @click="goToDetailTable(item.id)"></i>

                            <i style="cursor:pointer" class="fas fa-eye view-icon me-2" title="Xem chi tiết"
                                @click="handleViewDetails(item)"></i>

                            <i style="cursor:pointer" class="fas fa-pen edit-icon me-2" title="Xem chi tiết"
                                @click="handleEditFood(item)"></i>

                            <i v-if="item.trangThaiKinhDoanh === 1" class="fas  fa-unlock-alt unlock-icon"
                                title="Khóa tài khoản" @click="handleToggleStatus(item)"></i>
                            <i v-else class="fas fa-lock lock-icon" title="Mở khóa tài khoản"
                                @click="handleToggleStatus(item)"></i>
                        </div>
                    </td>
                </tr>
                <tr v-if="paginatedData.length === 0">
                    <td colspan="10" class="empty-state-cell">
                        <div class="empty-state-content">
                            <div class="empty-icon">🍜</div>
                            <h3>Không tìm thấy món nào!</h3>
                            <p>Thử thay đổi bộ lọc hoặc tìm kiếm từ khóa khác xem sao nhé.</p>
                            <button class="btn-reset-empty" @click="clearFilters">
                                Xóa bộ lọc
                            </button>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>

        <div style="padding-bottom: 30px;" class="pagination">
           <CommonPagination
                v-model:currentPage="currentPage"
                v-model:pageSize="itemsPerPage"
                :total-pages="totalPages"
                :total-elements="totalElements"
                :current-count="paginatedData.length"
                @change="() => {}" 
            />
        </div>
    </div>


</template>

<style scoped src="/src/assets/foodManager.css"></style>
<style scoped>
.slider-wrapper {
    width: 200px;
    padding: 0 10px;
    margin-top: 5px;
    --slider-connect-bg: #d32f2f;
    --slider-tooltip-bg: #d32f2f;
    --slider-handle-ring-color: rgba(211, 47, 47, 0.3);
    --slider-height: 6px;
}

:deep(.slider-connect) {
    background: #d32f2f !important;
}

:deep(.slider-base) {
    background-color: #e5e7eb !important;
    height: 6px !important;
}

:deep(.slider-handle) {
    background: #d32f2f !important;
    box-shadow: none !important;
    border: 2px solid white;
}

:deep(.slider-handle:focus) {
    box-shadow: 0 0 0 3px rgba(211, 47, 47, 0.3) !important;
}

.price-filter-item {
    display: flex;
    flex-direction: column;
    justify-content: center;
    margin-right: 20px;
}

.price-range-text {
    font-weight: bold;
    color: #d32f2f;
    margin-left: 5px;
}

.btn-primary {
    background: #8B0000;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
}

.btn-primary:hover {
    background: #b71c1c;
}

.btn-secondary {
    background: #fff;
    border: 1px solid #ccc;
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
}

.btn-secondary:hover {
    background: #f0f0f0;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.btn-filter-category {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    padding: 0 12px;
    background-color: #fff;
    border: 1px solid #ddd;
    border-radius: 4px;
    cursor: pointer;
    min-width: 140px;
    height: 43px;
}

.close-btn {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    color: #666;
}

.actions {
    height: 100%;
    display: table-cell;
}

.action-group {
    display: flex;
    align-items: center;
    justify-content: start;
    gap: 15px;
}

/* 4. Style icon */
.action-group i {
    font-size: 1.1rem;
    cursor: pointer;
    transition: transform 0.2s;
}

.action-group i:hover {
    transform: scale(1.2);
}
</style>
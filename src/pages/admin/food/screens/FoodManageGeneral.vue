<script setup>
import foodModal from '../../food/modal/updateModal/foodModal.vue';
import { useFoodManager, usePriceFilter } from '../../../../services/foodFunction';
import FoodAddModal from '../../food/modal/addModal/FoodAddModal.vue';
import Slider from '@vueform/slider';
import "@vueform/slider/themes/default.css";
import { useRouter } from 'vue-router';

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
    visiblePages,
    goToPage,
    handleViewDetails,
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
    availableSubCategories
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
                <label>Danh mục</label>
                <button class="btn-filter-category" @click="isCategoryFilterOpen = true">
                    <span v-if="!selectedRootCate">Chọn danh mục</span>
                    <span v-else class="active-filter-text">Đang lọc...</span>

                </button>
            </div>

            <button class="btn-clear" @click="clearFilters">Xóa bộ lọc</button>
        </div>
    </div>

    <div v-if="isCategoryFilterOpen" class="category-modal-overlay" @click.self="isCategoryFilterOpen = false">
        <div class="category-modal">
            <div class="modal-header">
                <h3>Lọc theo Danh Mục</h3>
                <button class="close-btn" @click="isCategoryFilterOpen = false">×</button>
            </div>

            <div class="modal-body">
                <div class="form-group">
                    <label>Danh mục gốc</label>
                    <select v-model="selectedRootCate" class="form-control" @change="selectedSubCate = ''">
                        <option value="">-- Tất cả --</option>
                        <option v-for="cat in listRootCategories" :key="cat.id" :value="cat.id">
                            {{ cat.tenDanhMuc }}
                        </option>
                    </select>
                </div>

                <div class="form-group">
                    <label>Danh mục chi tiết</label>
                    <select v-model="selectedSubCate" class="form-control" :disabled="!selectedRootCate">
                        <option value="">-- Tất cả --</option>
                        <option v-for="sub in availableSubCategories" :key="sub.id" :value="sub.id">
                            {{ sub.tenDanhMucChiTiet }}
                        </option>
                    </select>
                </div>
            </div>

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
        <div style="padding-bottom: 30px;" class="pagination" v-if="totalPages > 1">
            <button @click="goToPage(currentPage - 1)" :disabled="currentPage === 1"
                :class="{ 'disabled': currentPage === 1 }">
                &lt;
            </button>

            <button v-for="(page, index) in visiblePages" :key="index"
                :class="{ 'active': page === currentPage, 'dots': page === '...' }"
                @click="page !== '...' ? goToPage(page) : null" :disabled="page === '...'">
                {{ page }}
            </button>

            <button @click="goToPage(currentPage + 1)" :disabled="currentPage === totalPages"
                :class="{ 'disabled': currentPage === totalPages }">
                &gt;
            </button>
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
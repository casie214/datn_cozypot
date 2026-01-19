<script setup>
import { useRouter } from 'vue-router'; 
import { useHotpotManager } from '../../../../services/foodFunction';
import FoodHotpotModal from '../../food/modal/updateModal/foodHotpotModal.vue';
import Slider from '@vueform/slider';
import "@vueform/slider/themes/default.css";

const router = useRouter();

const {
  isModalOpen, selectedHotpot, handleViewDetails, handleToggleStatus, getAllHotpot,
  paginatedData, searchQuery, sortOption, currentPage, totalPages, visiblePages, itemsPerPage, goToPage,
  statusFilter, typeFilter, uniqueTypes, clearFilters,
  
  // 2. LẤY BIẾN SLIDER
  selectedPriceRange,
  globalMinPrice,
  globalMaxPrice
} = useHotpotManager();

const handleRefreshList = () => {
    setTimeout(() => { getAllHotpot(); }, 500);
};

const goToAddScreen = () => {
    router.push({ name: 'addHotpotSet' });
};

const handleViewDetailUpdate = (item) => {
    router.push({ 
        name: 'updateHotpotSet', 
        params: { id: item.id } 
    });
};
</script>

<template>
  <div class="tab-content">
    <div class="filter-box">
      <div class="filter-row">
        <div class="filter-item search">
          <label>Tìm kiếm</label>
          <div class="input-group">
            <input v-model="searchQuery" type="text" class="form-search form-control" placeholder="Tìm kiếm set lẩu (mã, tên)" />
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
            <label>Loại set lẩu</label>
            <select v-model="typeFilter" class="form-control">
                <option value="all">Tất cả</option>
                <option v-for="type in uniqueTypes" :key="type.id" :value="type.id">{{ type.name }}</option>
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

        <div class="filter-item price-filter-item" style="margin: 0 15px 15px 15px;">
            <label>
                Khoảng giá:
                <span class="price-range-text">
                    {{ selectedPriceRange[0].toLocaleString() }} - {{ selectedPriceRange[1].toLocaleString() }}
                </span>
            </label>
            <div class="slider-wrapper" v-if="globalMaxPrice > 0">
                <Slider 
                    v-model="selectedPriceRange" 
                    :min="globalMinPrice" 
                    :max="globalMaxPrice" 
                    :step="10000"
                    :tooltips="false" 
                />
            </div>
             <div v-else class="loading-text">Đang tải...</div>
        </div>
        
        <button class="btn-clear" @click="clearFilters">Xóa bộ lọc</button>
      </div>
    </div>

    <div class="action-row">
      <button class="btn-add" @click="goToAddScreen">Thêm set lẩu</button>
    </div>

    <div class="table-container" style="min-height: 305px;">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>MÃ</th>
            <th>SET LẨU</th>
            <th>GIÁ BÁN</th>
            <th>LOẠI LẨU</th>
            <th>TRẠNG THÁI</th>
            <th>CHỨC NĂNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in paginatedData" :key="item.id || index">
            <td align="center">{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
            
            <td>{{ item.maSetLau }}</td>
            <td><b>{{ item.tenSetLau }}</b></td>
            <td style="color:#d32f2f"><b>{{ item.giaBan?.toLocaleString() }} đ</b></td>
            <td>{{ item.tenLoaiSet }}</td>
            
            <td :class="item.trangThai === 1 ? 'status-active' : 'status-inactive'">
              {{ item.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
            </td>
            
            <td class="actions">
              <button class="btn-icon" @click="handleViewDetailUpdate(item)">👁️</button>
              <div class="toggle-switch" :class="{ 'on': item.trangThai === 1 }"
                   @click.stop="handleToggleStatus(item)">
                   <div class="toggle-knob"></div>
              </div>
            </td>
          </tr>
          
          <tr v-if="paginatedData.length === 0">
              <td colspan="7" style="text-align: center; padding: 20px; color: #888;">
                  Không tìm thấy dữ liệu phù hợp
              </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <div class="pagination" v-if="totalPages > 1">
    <button 
        @click="goToPage(currentPage - 1)" 
        :disabled="currentPage === 1"
        :class="{ 'disabled': currentPage === 1 }"
    >
        &lt;
    </button>
    
    <button 
        v-for="(page, index) in visiblePages" 
        :key="index"
        :class="{ 'active': page === currentPage, 'dots': page === '...' }"
        @click="page !== '...' ? goToPage(page) : null"
        :disabled="page === '...'"
    >
        {{ page }}
    </button>
    
    <button 
        @click="goToPage(currentPage + 1)" 
        :disabled="currentPage === totalPages"
        :class="{ 'disabled': currentPage === totalPages }"
    >
        &gt;
    </button>
  </div>

</template>

<style scoped src="/src/assets/foodManager.css"></style>

<style scoped>
/* Container bao ngoài slider */
.slider-wrapper {
    width: 200px;
    padding: 0 10px;
    margin-top: 5px;

    /* --- CÁCH 1: Dùng biến CSS (Khuyên dùng - Chuẩn nhất) --- */
    --slider-connect-bg: #d32f2f;  /* Màu thanh nối */
    --slider-tooltip-bg: #d32f2f;  /* Màu tooltip */
    --slider-handle-ring-color: rgba(211, 47, 47, 0.3); /* Màu vòng focus */
    --slider-height: 6px;          /* Độ dày thanh */
}

/* Nếu Cách 1 không chạy (do phiên bản cũ), dùng Cách 2 dưới đây: */

/* --- CÁCH 2: Dùng :deep (Ghi đè cưỡng bức) --- */
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

/* Chỉnh lại layout ô lọc giá */
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
</style>
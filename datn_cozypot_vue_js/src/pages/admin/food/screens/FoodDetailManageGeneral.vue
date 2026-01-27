<script setup>
import { useFoodDetailManager } from '../../../../services/foodFunction'; // Sửa lại đường dẫn
import { useRouter } from 'vue-router';
// --- IMPORT THƯ VIỆN ---
import Slider from '@vueform/slider';
import "@vueform/slider/themes/default.css";
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';

const {
  paginatedData, searchQuery, currentPage, totalPages, visiblePages, itemsPerPage, changePage,
  getAllFoodDetails, handleToggleStatus, sortOption, statusFilter,
  selectedPriceRange, globalMinPrice, globalMaxPrice, clearFilters,
  handleMinChange, handleMaxChange,

  // Các biến mới
  categoryFilter,
  foodFilter,
  listCategories,
  availableFoods 
} = useFoodDetailManager();

const router = useRouter();
const goToAddScreen = () => router.push({ name: 'addFoodDetail' });

const handleView = (item) => {
  router.push({ name: 'viewFoodDetail', params: { id: item.id } });
};

const handleEdit = (item) => {
  router.push({ name: 'updateFoodDetail', params: { id: item.id } });
};
</script>

<template>
  <div class="flex-row">
    <h1 class="page-title" style="padding-left: 0;">Quản lý thực đơn</h1>
    <div class="action-row">
      <button class="btn-add" @click="goToAddScreen">+ Thêm chi tiết món</button>
    </div>
  </div>

  <div class="tab-content">
    <div class="filter-box">
      <div class="filter-row">
        <div class="filter-item search">
          <label>Tìm kiếm</label>
          <div class="input-group">
            <input v-model="searchQuery" type="text" class="form-search form-control"
              placeholder="Tìm chi tiết (mã, tên)" />
            <button class="search-btn">🔍</button>
          </div>
        </div>

        <div class="filter-item">
          <label>Món ăn gốc</label>
          <div class="multiselect-wrapper">
            <Multiselect
              v-model="foodFilter"
              :options="availableFoods"
              valueProp="id"
              label="tenMonAn"
              placeholder="-- Tất cả --"
              :searchable="true"
              :canClear="true"
              noOptionsText="Chọn danh mục trước"
              noResultsText="Không tìm thấy món"
            />
          </div>
        </div>

        <div class="filter-item">
          <label>Trạng thái</label>
          <select v-model="statusFilter" class="form-control">
            <option value="all">Tất cả</option>
            <option value="1">Đang hoạt động</option>
            <option value="0">Ngưng hoạt động</option>
          </select>
        </div>

        <div class="filter-item">
          <label>Sắp xếp theo</label>
          <select v-model="sortOption" class="form-control">
            <option value="id_asc">Số thứ tự tăng dần</option>
            <option value="id_desc">Mới thêm gần đây</option>
            <option value="price_asc">Giá thấp -> Cao</option>
            <option value="price_desc">Giá cao -> Thấp</option>
          </select>
        </div>

        <div class="filter-item price-filter-item">
          <div style="display: flex; flex-direction: row; justify-content: space-between;">
             <label>Khoảng giá: <span class="price-range-text">{{ selectedPriceRange[0].toLocaleString() }} - {{ selectedPriceRange[1].toLocaleString() }}</span></label>
             <div class="slider-wrapper" v-if="globalMaxPrice > 0">
               <Slider v-model="selectedPriceRange" :min="globalMinPrice" :max="globalMaxPrice" :step="5000" :tooltips="false" />
             </div>
          </div>
          <div class="price-inputs">
             <input type="number" v-model="selectedPriceRange[0]" @change="handleMinChange" class="price-input-small" placeholder="Từ">
             <span class="separator">-</span>
             <input type="number" v-model="selectedPriceRange[1]" @change="handleMaxChange" class="price-input-small" placeholder="Đến">
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
            <th>MÃ CHI TIẾT</th>
            <th>TÊN CHI TIẾT</th>
            <th>MÓN ĂN GỐC</th>
            <th>GIÁ BÁN</th>
            <th>KÍCH CỠ</th>
            <th>ĐƠN VỊ</th>
            <th>TRẠNG THÁI</th>
            <th>CHỨC NĂNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="paginatedData.length === 0">
            <td colspan="10" class="empty-state-cell">
              <div class="empty-state-content">
                <div class="empty-icon">🍜</div>
                <h3>Không tìm thấy món nào!</h3>
                <button class="btn-reset-empty" @click="clearFilters">Xóa bộ lọc</button>
              </div>
            </td>
          </tr>
          <tr v-for="(item, index) in paginatedData" :key="item.id">
            <td>{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
            <td>{{ item.maChiTietMonAn }}</td>
            <td><b>{{ item.tenChiTietMonAn }}</b></td>
            <td>{{ item.monAnDiKem ? item.monAnDiKem.tenMonAn : (item.tenMonAnDiKem || '---') }}</td>
            <td style="color:#d32f2f; font-weight:bold">{{ item.giaBan?.toLocaleString() }}</td>
            <td>{{ item.kichCo }}</td>
            <td>{{ item.donVi }}</td>
            <td :class="item.trangThai ? 'status-active' : 'status-inactive'">
              {{ item.trangThai ? 'Đang hoạt động' : 'Ngưng bán' }}
            </td>
            <td class="actions">
              <div class="action-group">
                <i class="fas fa-eye view-icon" @click="handleView(item)"></i>
                <i class="fas fa-pen edit-icon" @click="handleEdit(item)"></i>
                <i :class="item.trangThai === 1 ? 'fas fa-unlock-alt' : 'fas fa-lock'" @click="handleToggleStatus(item)"></i>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div style="padding-bottom: 30px;" class="pagination" v-if="totalPages > 1">
        <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1">&lt;</button>
        <template v-for="(page, index) in visiblePages" :key="index">
          <button v-if="page === '...'" disabled>...</button>
          <button v-else @click="changePage(page)" :class="{ 'active': currentPage === page }">{{ page }}</button>
        </template>
        <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages">&gt;</button>
      </div>
    </div>
  </div>
</template>

<style scoped src="/src/assets/foodManager.css"></style>
<style src="@vueform/multiselect/themes/default.css"></style>

<style scoped>
/* CSS cho Multiselect */
.multiselect-wrapper {
  width: 200px;
}
:deep(.multiselect) {
  min-height: 38px;
  border: 1px solid #ced4da;
  border-radius: 0.375rem;
  --ms-tag-bg: #d32f2f;
  --ms-ring-color: rgba(211, 47, 47, 0.3);
  --ms-option-bg-selected: #d32f2f;
  --ms-option-bg-selected-pointed: #b71c1c;
}
/* CSS Slider & Actions (Giữ nguyên của bạn) */
.slider-wrapper { width: 200px; padding: 0 10px; margin-top: 5px; }
:deep(.slider-connect) { background: #d32f2f !important; }
:deep(.slider-base) { background-color: #e5e7eb !important; height: 6px !important; }
:deep(.slider-handle) { background: #d32f2f !important; border: 2px solid white; }
.price-filter-item { display: flex; flex-direction: column; justify-content: center; margin-right: 20px; }
.price-range-text { font-weight: bold; color: #d32f2f; margin-left: 5px; }
.actions { height: 100%; display: table-cell; }
.action-group { display: flex; gap: 15px; }
.action-group i { font-size: 1.1rem; cursor: pointer; transition: transform 0.2s; }
.action-group i:hover { transform: scale(1.2); }
.form-control { min-width: 140px; }
</style>
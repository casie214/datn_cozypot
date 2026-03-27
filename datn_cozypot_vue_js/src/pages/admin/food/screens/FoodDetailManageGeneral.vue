<script setup>
import { useFoodManager } from '../../../../services/foodFunction';
import { useRouter } from 'vue-router';
// --- IMPORT THƯ VIỆN ---
import Slider from '@vueform/slider';
import "@vueform/slider/themes/default.css";
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';
import CommonPagination from '@/components/commonPagination.vue';
import Swal from 'sweetalert2';
import { usePermission } from "@/components/permissionHelper";
const { handleActionWithAuth } = usePermission();

const statusOptions = [
  { value: 'all', label: 'Tất cả' },
  { value: '1', label: 'Đang kinh doanh' },
  { value: '0', label: 'Ngưng kinh doanh' }
];

const sortOptions = [
  { value: 'newest', label: 'Mới nhất' },
  { value: 'name_asc', label: 'Tên (A-Z)' },
  { value: 'price_asc', label: 'Giá (Thấp -> Cao)' },
  { value: 'price_desc', label: 'Giá (Cao -> Thấp)' }
];


const {
  mockData, paginatedData, currentPage, itemsPerPage, totalPages, totalElements,
  searchQuery, sortOption, statusFilter, categoryFilter, listCategories, isCategoryLocked,
  globalMinPrice, globalMaxPrice, selectedPriceRange, getAllFoods,
  handleToggleStatus, exportToExcel, goToAdd, goToEdit, handleViewDetails, clearFilters
} = useFoodManager();

const handleRefreshListBtn = () => {
  Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công!', timer: 1500, showConfirmButton: false });
  emit('refresh');
  setTimeout(() => emit('close'), 1000);
  getAllFoods();
}


const router = useRouter();

</script>

<template>

  <div class="tab-content">
    <div class="filter-box">
      <div class="filter-row" style="align-items: flex-end;">

        <div class="filter-item search">
          <label>Tìm kiếm</label>
          <div class="input-group">
            <input v-model="searchQuery" type="text" class="form-search form-control"
              placeholder="Nhập mã, tên món ăn" />
          </div>
        </div>

        <div class="filter-item">
          <label>Danh mục gốc</label>
          <div class="multiselect-wrapper">
            <Multiselect v-model="categoryFilter" :options="listCategories" valueProp="id" label="tenDanhMuc"
              placeholder="-- Tất cả danh mục --" :searchable="true" :canClear="!isCategoryLocked"
              :disabled="isCategoryLocked" noOptionsText="Không có danh mục nào"
              noResultsText="Không tìm thấy kết quả" />
          </div>
        </div>

        <div class="filter-item">
          <label>Trạng thái</label>
          <div class="multiselect-wrapper-sm">
            <Multiselect v-model="statusFilter" :options="statusOptions" :searchable="true" :canClear="false"
              :no-results-text="'Không tìm thấy kết quả nào'" class="custom-filter-multiselect" />
          </div>
        </div>

        <div class="filter-item">
          <label>Sắp xếp theo</label>
          <div class="multiselect-wrapper-sm">
            <Multiselect v-model="sortOption" :options="sortOptions" :searchable="true" :canClear="false"
              :no-results-text="'Không tìm thấy kết quả nào'" class="custom-filter-multiselect" />
          </div>
        </div>

        <div class="filter-item price-filter-item" style="width: 250px;">
          <div style="display: flex; flex-direction: row; justify-content: space-between; margin-bottom: 5px;">
            <label>Khoảng giá (VNĐ):</label>
            <span class="price-range-text" v-if="selectedPriceRange?.length === 2">
              {{ (selectedPriceRange[0] || 0).toLocaleString() }} - {{ (selectedPriceRange[1] || 0).toLocaleString() }}
            </span>
          </div>
          <div class="slider-wrapper" v-if="globalMaxPrice > 0">
            <Slider v-model="selectedPriceRange" :min="globalMinPrice" :max="globalMaxPrice" :step="1000"
              :tooltips="false" />
          </div>
        </div>

        <button class="btn-clear" style="margin-bottom: 5px;" @click="clearFilters">Xóa bộ lọc</button>
      </div>
    </div>

    <div class="flex-row">
      <div class="action-row" style="margin-left: auto;">
        <button class="btn-action-icon btn-add-only" @click="handleActionWithAuth(() => goToAdd(), 'ADMIN')">
          <i class="fas fa-plus"></i>
        </button>

        <button class="btn-action-icon" @click="handleActionWithAuth(() => exportToExcel(), 'ADMIN')"
          title="Xuất Excel danh sách hiện tại">
          <i class="fas fa-file-excel"></i>
        </button>

        <button class="btn-action-icon btn-refresh-only" @click="handleRefreshListBtn" title="Tải lại">
          <i class="fas fa-sync-alt"></i>
        </button>
      </div>
    </div>

    <div class="table-container" style="min-height: 278px;">
      <table class="table table-hover align-middle mb-0" style="font-family: var(--bs-body-font-family); font-size: var(--bs-body-font-size);">
        <thead class="table-light text-muted">
          <tr>
            <th class="py-3 ps-3">STT</th>
            <th class="py-3">MÃ DANH MỤC</th>
            <th class="py-3">MÃ DMCT</th>
            <th class="py-3">TÊN DMCT</th>
            <th class="py-3">ĐỊNH LƯỢNG</th>
            <th class="py-3">ĐƠN VỊ</th>
            <th class="py-3">GIÁ</th>
            <th class="py-3">TRẠNG THÁI</th>
            <th class="py-3 text-center" style="width: 120px;">CHỨC NĂNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="paginatedData.length === 0">
            <td colspan="9" class="empty-state-cell">
              <div class="empty-state-content" style="padding: 30px; text-align: center;">
                <div class="empty-icon" style="font-size: 3rem; color: #ccc;">🍲</div>
                <h3 style="color: #666; margin: 10px 0;">Không tìm thấy món ăn nào!</h3>
                <button class="btn btn-outline-danger" @click="clearFilters">Xóa bộ lọc</button>
              </div>
            </td>
          </tr>

          <tr v-for="(item, index) in paginatedData" :key="item.id">
            <td>{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>

            <td>{{ item.maDanhMuc || 'DM001' }}</td>
            <td>{{ item.maMon }}</td>
            <td>
              <div style="display: flex; align-items: center; gap: 10px;">
                <span>{{ item.tenMon }}</span>
              </div>
            </td>



            <td>
              <span class="badge bg-secondary">{{ item.giaTriDinhLuong || '---' }}</span>
            </td>

            <td>{{ item.kichCo || '---' }}</td>



            <td class="fw-bold">{{ (item.giaGoc || item.giaBan || 0).toLocaleString() }} ₫</td>

            <td>
              <span :class="['status-badge', item.trangThai === 1 ? 'active' : 'inactive']">
                {{ item.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
              </span>
            </td>

            <td class="">
              <div class="action-group d-flex justify-content-center">
                <div class="icon-tooltip">
                  <i class="fas fa-pen edit-icon" title="Cập nhật" @click="goToEdit(item)"></i>
                  <span class="tooltip-text">Cập nhật món ăn</span>                
                </div>
                <label class="custom-toggle" :title="item.trangThai === 1 ? 'Ngưng kinh doanh' : 'Kích hoạt kinh doanh'">
                  <input 
                    type="checkbox" 
                    :checked="item.trangThai === 1"
                    @click.prevent="handleActionWithAuth(() => handleToggleStatus(item), 'ADMIN')"
                  >
                  <span class="toggle-slider"></span>
                </label>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div style="padding-bottom: 30px;" class="pagination">
        <CommonPagination v-model:currentPage="currentPage" v-model:pageSize="itemsPerPage" :total-pages="totalPages"
          :total-elements="totalElements" :current-count="paginatedData.length" @change="() => { }" />
      </div>
    </div>
  </div>
</template>

<style scoped src="/src/assets/foodManager.css"></style>
<style src="@vueform/multiselect/themes/default.css"></style>

<style scoped>
/* CSS cho Multiselect */
.multiselect-wrapper {
  width: 230px;
}

.multiselect-wrapper-sm {
  width: 220px;
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

/* CSS Slider & Actions */
.slider-wrapper {
  width: 100%;
  padding: 0 5px;
  margin-top: 10px;
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
  border: 2px solid white;
}

.price-range-text {
  font-weight: bold;
  color: #d32f2f;
  font-size: 0.9rem;
}

.action-group {
  gap: 12px;
}

.action-group i {
  font-size: 1.1rem;
  cursor: pointer;
  transition: transform 0.2s;
}

.action-group i:hover {
  transform: scale(1.2);
  opacity: 0.8;
}

/* Badge Trạng thái */
.status-badge {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.status-badge.active {
  background-color: white;
  color: black;
  border: 1px solid lightgray;
}

.status-badge.inactive {
  background-color: whitesmoke;
  color: black;
  border: 1px solid lightgray;
}

.price-filter-item {
  min-height: 65px;
  /* Rộng hơn một chút để thanh trượt thoải mái */
  margin-right: 15px;
  /* Đẩy nút Xóa bộ lọc ra xa */
  display: flex;
  flex-direction: column;
}

.btn-add-only {
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%) !important;
  color: white !important;
  border: none !important;
}


.btn-action-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  border: 1px solid #eee !important;
  color: var(--primary-red);
  transition: all 0.2s ease;
}

.custom-toggle {
  position: relative;
  display: inline-block;
  width: 46px;
  height: 24px;
  margin: 0;
}

.custom-toggle input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #333333; /* MÀU ĐEN: Trạng thái Ngưng kinh doanh */
  transition: .4s cubic-bezier(0.25, 0.8, 0.25, 1);
  border-radius: 34px;
  box-shadow: inset 0 1px 3px rgba(0,0,0,0.3);
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .4s cubic-bezier(0.25, 0.8, 0.25, 1);
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

/* KHI ĐƯỢC CHECK (TRẠNG THÁI 1) -> CHUYỂN SANG MÀU ĐỎ */
.custom-toggle input:checked + .toggle-slider {
  background-color: #7D161A; /* MÀU ĐỎ: Đang kinh doanh */
}

/* HIỆU ỨNG TRƯỢT SANG PHẢI */
.custom-toggle input:checked + .toggle-slider:before {
  transform: translateX(22px);
}

/* Hiệu ứng khi hover vào toggle */
.custom-toggle:hover .toggle-slider {
  filter: brightness(1.2);
}

.icon-tooltip {
    position: relative;
    display: inline-flex;
    align-items: center;
    justify-content: center;
}

/* Tooltip text */
.icon-tooltip .tooltip-text {
    position: absolute;
    bottom: 130%; /* hiện phía trên icon */
    left: 50%;
    transform: translateX(-50%);
    background-color: #333;
    color: #fff;
    padding: 6px 10px;
    font-size: 12px;
    border-radius: 6px;
    white-space: nowrap;
    opacity: 0;
    visibility: hidden;
    transition: all 0.2s ease;
    pointer-events: none;
    z-index: 999;
}

/* Mũi tên nhỏ */
.icon-tooltip .tooltip-text::after {
    content: "";
    position: absolute;
    top: 100%;
    left: 50%;
    transform: translateX(-50%);
    border-width: 6px;
    border-style: solid;
    border-color: #333 transparent transparent transparent;
}

/* Hover hiện tooltip */
.icon-tooltip:hover .tooltip-text {
    opacity: 1;
    visibility: visible;
    transform: translateX(-50%) translateY(-4px);
}
</style>
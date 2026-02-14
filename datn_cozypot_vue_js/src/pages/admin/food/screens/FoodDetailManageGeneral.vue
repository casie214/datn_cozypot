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

const {
  mockData, paginatedData, currentPage, itemsPerPage, totalPages, totalElements,
  searchQuery, sortOption, statusFilter, categoryFilter, listCategories, isCategoryLocked,
  globalMinPrice, globalMaxPrice, selectedPriceRange, getAllFoods,
  handleToggleStatus, exportToExcel, goToAdd, goToEdit, handleViewDetails, clearFilters
} = useFoodManager();

const handleRefreshListBtn = () => {
  Swal.fire({ icon: 'success', title: 'Thành công!', timer: 1500, showConfirmButton: false });
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
            <button class="search-btn"><i class="fas fa-search me-1"></i></button>
          </div>
        </div>

        <div class="filter-item">
          <label>Danh mục gốc</label>
          <div class="multiselect-wrapper">
            <Multiselect v-model="categoryFilter" :options="listCategories" valueProp="id" label="tenDanhMuc"
              placeholder="-- Tất cả danh mục --" :searchable="true" :canClear="!isCategoryLocked"
              :disabled="isCategoryLocked" noOptionsText="Không có danh mục nào" noResultsText="Không tìm thấy" />
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
            <option value="price_asc">Giá (Thấp -> Cao)</option>
            <option value="price_desc">Giá (Cao -> Thấp)</option>
          </select>
        </div>

        <div class="filter-item price-filter-item" style="width: 250px;">
          <div style="display: flex; flex-direction: row; justify-content: space-between; margin-bottom: 5px;">
            <label>Khoảng giá (VNĐ):</label>
            <span class="price-range-text">
              {{ selectedPriceRange[0].toLocaleString() }} - {{ selectedPriceRange[1].toLocaleString() }}
            </span>
          </div>
          <div class="slider-wrapper" v-if="globalMaxPrice > 0">
            <Slider v-model="selectedPriceRange" :min="globalMinPrice" :max="globalMaxPrice" :step="5000"
              :tooltips="false" />
          </div>
        </div>

        <button class="btn-clear" style="margin-bottom: 5px;" @click="clearFilters">Xóa bộ lọc</button>
      </div>
    </div>

    <div class="flex-row">
      <div class="action-row" style="margin-left: auto;">
        <button class="btn-action-icon btn-add-only" @click="goToAdd">
          <i class="fas fa-plus"></i>
        </button>
        <button class="btn-action-icon" @click="exportToExcel" title="Xuất Excel danh sách hiện tại">
          <i class="fas fa-file-excel"></i>
        </button>
        <button class="btn-action-icon btn-refresh-only" @click="handleRefreshListBtn" title="Tải lại"><i
            class="fas fa-sync-alt"></i></button>
      </div>
    </div>

    <div class="table-container" style="min-height: 278px;">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>MÃ DANH MỤC</th>
            <th>MÃ DMCT</th>
            <th>TÊN DMCT</th>
            <th>ĐỊNH LƯỢNG</th>
            <th>ĐƠN VỊ</th>
            <th>GIÁ</th>
            <th>TRẠNG THÁI</th>
            <th style="width: 120px;">HÀNH ĐỘNG</th>
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

            

            <td class="fw-bold">{{ item.giaBan?.toLocaleString() }} ₫</td>

            <td>
              <span :class="['status-badge', item.trangThai === 1 ? 'active' : 'inactive']">
                {{ item.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
              </span>
            </td>

            <td class="">
              <div class="action-group d-flex justify-content-center">
                <i class="fas fa-pen edit-icon" title="Cập nhật" @click="goToEdit(item)"></i>

                <i v-if="item.trangThai === 1" class="fas fa-unlock-alt unlock-icon" title="Tạm ngưng bán"
                  @click="handleToggleStatus(item)"></i>
                <i v-else class="fas fa-lock lock-icon" title="Kích hoạt bán" @click="handleToggleStatus(item)"></i>
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
  background-color: #8B0000 !important;
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
</style>
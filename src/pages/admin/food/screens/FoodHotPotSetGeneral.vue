<script setup>
import { useRouter } from 'vue-router';
import { useHotpotManager } from '../../../../services/foodFunction';
import Slider from '@vueform/slider';
import "@vueform/slider/themes/default.css";
import CommonPagination from '@/components/commonPagination.vue';
import '@vueform/multiselect/themes/default.css';
import Multiselect from '@vueform/multiselect';
import Swal from 'sweetalert2';

const router = useRouter();

const {
  getAllHotpot, paginatedData, searchQuery, sortOption, currentPage, totalPages,
  visiblePages, itemsPerPage, goToPage, statusFilter, typeFilter, uniqueTypes,
  goToDetailTable, clearFilters, selectedPriceRange, globalMinPrice, globalMaxPrice, isTypeLocked, handleToggleStatus, totalElements, exportToExcel,
} = useHotpotManager();

const goToAddScreen = () => {
  router.push({ name: 'addHotpotSet' });
};

// --- HÀM MỚI: Xem chi tiết ---
const handleViewDetail = (item) => {
  router.push({
    name: 'viewHotpotSet',
    params: { id: item.id }
  });
};

const handleEdit = (item) => {
  router.push({
    name: 'updateHotpotSet',
    params: { id: item.id }
  });
};

const handleRefreshListBtn = () => {
  Swal.fire({ icon: 'success', title: 'Thành công!', timer: 1500, showConfirmButton: false });
  setTimeout(() => emit('close'), 1000);
  getAllHotpot();
}

const getImg = (url) => {
  if (url && (url.startsWith('http') || url.startsWith('data:image'))) {
    return url;
  }
  return 'https://placehold.co/100x100?text=No+Img';
}
</script>

<template>



  <div class="tab-content">
    <div class="filter-box">
      <div class="filter-row">
        <div class="filter-item search">
          <label>Tìm kiếm</label>
          <div class="input-group">
            <input v-model="searchQuery" type="text" class="form-search form-control"
              placeholder="Tìm kiếm set lẩu (mã, tên)" />
            <button class="search-btn"><i class="fas fa-search me-1"></i></button>
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
          <div class="multiselect-wrapper">
            <Multiselect v-model="typeFilter" :options="uniqueTypes" :key="uniqueTypes.length" valueProp="id"
              label="name" placeholder="Tất cả loại lẩu" :searchable="true" :canClear="!isTypeLocked"
              :disabled="isTypeLocked" noOptionsText="Đang tải dữ liệu..." noResultsText="Không tìm thấy kết quả" />
          </div>
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

        <div class="filter-item price-filter-item">
          <div class="" style="display: flex; flex-direction: row; justify-content: space-between;">
            <label>
              Khoảng giá:
              <span class="price-range-text">
                {{ selectedPriceRange[0].toLocaleString() }} - {{ selectedPriceRange[1].toLocaleString() }}
              </span>
            </label>
            <div class="slider-wrapper" v-if="globalMaxPrice > 0">
              <Slider v-model="selectedPriceRange" :min="globalMinPrice" :max="globalMaxPrice" :step="10000"
                :tooltips="false" />
            </div>
            <div v-else class="loading-text">Đang tải...</div>
          </div>
          <div class="price-inputs">
            <input type="number" v-model="selectedPriceRange[0]" @change="handleMinChange" class="price-input-small"
              placeholder="Từ">
            <span class="separator">-</span>
            <input type="number" v-model="selectedPriceRange[1]" @change="handleMaxChange" class="price-input-small"
              placeholder="Đến">
          </div>
        </div>

        <button class="btn-clear" @click="clearFilters">Xóa bộ lọc</button>
      </div>
    </div>

    <div class="action-row" style="margin-left: auto;">
      <button class="btn-action-icon btn-add-only" @click="goToAddScreen">
        <i class="fas fa-plus"></i>
      </button>
      <button class="btn-action-icon" @click="exportToExcel" title="Xuất Excel danh sách hiện tại">
        <i class="fas fa-file-excel"></i>
      </button>
      <button class="btn-action-icon btn-refresh-only" @click="handleRefreshListBtn" title="Tải lại"><i
          class="fas fa-sync-alt"></i></button>
    </div>

    <div class="table-container" style="min-height: 278px;">
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

            <td>{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
            <td>{{ item.maSetLau }}</td>
            <td>{{ item.tenSetLau }}</td>
            <td>{{ item.giaBan?.toLocaleString() }} VNĐ</td>
            <td>{{ item.tenLoaiSet }}</td>

            <td :class="item.trangThai === 1 ? '' : ''">
              {{ item.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
            </td>

            <td class="actions">
              <div class="action-group">

                <i style="cursor:pointer" class="fas fa-pen edit-icon me-2" title="Chỉnh sửa set lẩu"
                  @click="handleEdit(item)"></i>

                <i v-if="item.trangThai === 1" class="fas  fa-unlock-alt unlock-icon" title="Khóa set lẩu"
                  @click="handleToggleStatus(item)"></i>
                <i v-else class="fas fa-lock lock-icon" title="Mở khóa set lẩu" @click="handleToggleStatus(item)"></i>
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
        <CommonPagination v-model:currentPage="currentPage" v-model:pageSize="itemsPerPage" :total-pages="totalPages"
          :total-elements="totalElements" :current-count="paginatedData.length" @change="() => { }" />
      </div>
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

.action-group i {
  font-size: 1.1rem;
  cursor: pointer;
  transition: transform 0.2s;
}

.action-group i:hover {
  transform: scale(1.2);
}

.status-badge {
  padding: 4px 10px;
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
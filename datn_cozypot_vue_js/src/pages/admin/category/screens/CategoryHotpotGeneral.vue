<script setup>
import CommonPagination from '@/components/commonPagination.vue';
import { useHotpotSetTypeManager } from '../../../../services/foodFunction';
import CategoryHotpotAddModal from '../modal/addModal/CategoryHotpotAddModal.vue';
import CategoryHotpotPutModal from '../modal/updateModal/CategoryHotpotPutModal.vue';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import Multiselect from '@vueform/multiselect';
import { usePermission } from "@/components/permissionHelper";
const { handleActionWithAuth } = usePermission();
const {
  hotpotTypeData, isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, getAllHotpotType,
  paginatedData, searchQuery, statusFilter, sortOption, totalElements, exportToExcel,
  currentPage, totalPages, visiblePages, itemsPerPage, changePage
} = useHotpotSetTypeManager();
const router = useRouter();
const statusOptions = [
  { value: 'all', label: 'Tất cả' },
  { value: '1', label: 'Đang kinh doanh' },
  { value: '0', label: 'Ngưng kinh doanh' }
];

const sortOptions = [
  { value: 'id_desc', label: 'Mới nhất' },
  { value: 'id_asc', label: 'Cũ nhất' },
  { value: 'name_asc', label: 'Tên (A-Z)' },
  { value: 'status_active', label: 'Đang hoạt động trước' },
  { value: 'status_inactive', label: 'Ngưng hoạt động trước' }
];

const handleRefreshListBtn = async () => {
  try {
    // Gọi API lấy lại dữ liệu mới nhất
    await getAllHotpotType(); 
    
    // Hiển thị thông báo nhỏ ở góc (Toast) cho chuyên nghiệp, không che màn hình
    Swal.fire({
      toast: true,
      position: 'top-end',
      icon: 'success',
      iconColor: '#7D161A',
      title: 'Đã làm mới dữ liệu!',
      showConfirmButton: false,
      timer: 1500
    });
  } catch (error) {
    Swal.fire('Lỗi', 'Không thể tải lại dữ liệu', 'error');
  }
};

const addFormData = ref({});

const goToHotpotList = (item) => {
  router.push({
    name: 'foodManager',
    query: {
      tab: 'setlau',
      preType: item.id,
      locked: 'true'
    }
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
            <input v-model="searchQuery" class="form-control form-search" type="text"
              placeholder="Tìm kiếm loại lẩu (mã, tên)" />
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
          <label>Lọc theo</label>
          <div class="multiselect-wrapper-sm">
            <Multiselect v-model="sortOption" :options="sortOptions" :searchable="true" :canClear="false"
              :no-results-text="'Không tìm thấy kết quả nào'" class="custom-filter-multiselect" />
          </div>
        </div>

        <button class="btn-clear" @click="searchQuery = ''; statusFilter = 'all'; sortOption = 'id_desc'">
          Xóa bộ lọc
        </button>
      </div>
    </div>

    <div class="action-row" style="margin-left: auto;">
      <button class="btn-action-icon btn-add-only" @click="handleActionWithAuth(() => { isModalOpen = true }, 'ADMIN')">
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

    <div class="table-container" style="min-height: 278px;">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>MÃ LOẠI</th>
            <th>TÊN LOẠI SET</th>
            <th>MÔ TẢ</th>
            <th>TRẠNG THÁI</th>
            <th>HÀNH ĐỘNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="paginatedData.length === 0">
            <td colspan="6" style="text-align: center; padding: 20px; color: #888;">
              Không tìm thấy dữ liệu.
            </td>
          </tr>

          <tr v-for="(item, index) in paginatedData" :key="item.id">
            <td>{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>

            <td>{{ item.maLoaiSet }}</td>
            <td style="max-width: 200px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis;"
              :title="item.moTa">{{ item.tenLoaiSet }}</td>
            <td style="max-width: 200px; overflow: hidden; white-space: nowrap; text-overflow: ellipsis;"
              :title="item.moTa">{{ item.moTa }}</td>

            <td :class="item.trangThai === 1 ? '' : ''">
              {{ item.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
            </td>

            <td>
              <div class="action-group">
                <div class="icon-tooltip">
                  <i class="fas fa-pen edit-icon" title="Sửa danh mục"
                    @click="handleActionWithAuth(() => openModal(item), 'ADMIN')">
                  </i>
                  <span class="tooltip-text">Cập nhật loại lẩu</span>
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

    <CategoryHotpotAddModal :isOpen="isModalOpen" :formData="addFormData" @close="isModalOpen = false" @save="handleAdd"
      @refresh="handleRefreshListBtn" />
    <CategoryHotpotPutModal :isOpen="isModalUpdateOpen" :formData="addFormData" :itemList="selectedItem"
      @close="isModalUpdateOpen = false" @save="handleAdd" @refresh="handleRefreshListBtn" />
  </div>
</template>

<style scoped>
@import url("/src/assets/foodManager.css");

.actions {
  height: 100%;
  display: table-cell;
}

.action-group {
  display: flex;
  align-items: start;
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

.multiselect-wrapper-sm {
  width: 220px;
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
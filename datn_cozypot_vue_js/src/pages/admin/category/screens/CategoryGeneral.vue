<script setup>
import CategoryAddModal from '../modal/addModal/CategoryAddModal.vue';
import CategoryPutModal from '../modal/updateModal/CategoryPutModal.vue';
import { useCategoryManager } from '../../../../services/foodFunction';
import { ref } from 'vue';
import CommonPagination from '@/components/commonPagination.vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';

const {
  categoryData, isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, getAllCategories,
  paginatedData, searchQuery, totalElements,
  sortOption, statusSort,
  currentPage, totalPages, visiblePages, itemsPerPage, changePage, exportToExcel
} = useCategoryManager();

const handleRefreshList = () => {
  getAllCategories();
};

const handleRefreshListBtn = () => {
  Swal.fire({ icon: 'success', title: 'Thành công!', timer: 1500, showConfirmButton: false });
                    emit('refresh');
                    setTimeout(() => emit('close'), 1000);
  getAllCategories();
};

const router = useRouter();

const addFormData = ref({});

const goToFoodList = (category) => {
  router.push({
    name: 'foodManager',
    query: {
      preRoot: category.id,
      locked: 'true'
    }
  });
};
</script>

<template>
  <div class="tab-content" style="margin-top: 0 !important;">
    <div class="filter-box">
      <div class="filter-row">
        <div class="filter-item search">
          <label>Tìm kiếm</label>
          <div class="input-group">
            <input v-model="searchQuery" class="form-control form-search" type="text"
              placeholder="Tìm kiếm danh mục (mã, tên)" />
            <button class="search-btn"><i class="fas fa-search me-1"></i></button>
          </div>
        </div>

        <div class="filter-item">
          <label>Trạng thái</label>
          <select v-model="statusSort" class="form-control">
            <option value="default">Mặc định (Hỗn hợp)</option>
            <option value="active_first">Đang kinh doanh lên đầu</option>
            <option value="inactive_first">Ngưng kinh doanh lên đầu</option>
          </select>
        </div>

        <div class="filter-item">
          <label>Sắp xếp theo</label>
          <select v-model="sortOption" class="form-control">
            <option value="id_desc">Mới nhất (ID giảm)</option>
            <option value="id_asc">Cũ nhất (ID tăng)</option>
            <option value="name_asc">Tên (A-Z)</option>
          </select>
        </div>

        <button class="btn-clear" @click="searchQuery = ''; sortOption = 'id_desc'; statusSort = 'default'">
          Xóa bộ lọc
        </button>
      </div>
    </div>

    <div class="action-row" style="margin-left: auto;">
      <button class="btn-action-icon btn-add-only" @click="isModalOpen = true">
        <i class="fas fa-plus"></i>
      </button>
      <button class="btn-action-icon" @click="exportToExcel" title="Xuất Excel danh sách hiện tại">
          <i class="fas fa-file-excel"></i>
      </button>
      <button class="btn-action-icon btn-refresh-only" @click="handleRefreshListBtn" title="Tải lại"><i class="fas fa-sync-alt"></i></button>
    </div>

    <div class="table-container" style="min-height: 278px;">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>MÃ DANH MỤC</th>
            <th>TÊN DANH MỤC</th>
            <th>SỐ MÓN ĂN</th>
            <th>MÔ TẢ</th>
            <th>TRẠNG THÁI</th>
            <th>CHỨC NĂNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="paginatedData.length === 0">
            <td colspan="6" style="text-align: center; padding: 20px; color: #888;">
              Không tìm thấy dữ liệu phù hợp.
            </td>
          </tr>

          <tr v-for="(item, index) in paginatedData" :key="item.id">
            <td>{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>

            <td>{{ item.maDanhMuc }}</td>
            <td>{{ item.tenDanhMuc }}</td>
            <td>
              <span>
                {{ item.soLuongMon || 0 }} món
              </span>
            </td>
            <td style="max-width: 200px;">{{ item.moTa || '---' }}</td>

            <td>
              <span :class="['status-badge', item.trangThai === 1 ? 'active' : 'inactive']">
                {{ item.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
              </span>
            </td>

            <td>
              <div class="action-group">
                <i class="fas fa-pen edit-icon" title="Sửa danh mục" @click="openModal(item)">
                </i>

                <i v-if="item.trangThai === 1" class="fas fa-unlock-alt unlock-icon" title="Ngưng kinh doanh"
                  @click="handleToggleStatus(item)">
                </i>
                <i v-else class="fas fa-lock lock-icon" title="Kích hoạt kinh doanh" @click="handleToggleStatus(item)">
                </i>
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

    <CategoryAddModal :isOpen="isModalOpen" @close="isModalOpen = false" @refresh="handleRefreshList" />

    <CategoryPutModal :isOpen="isModalUpdateOpen" :itemList="selectedItem" @close="isModalUpdateOpen = false"
      @refresh="handleRefreshList" />
  </div>
</template>

<style scoped>
@import url("/src/assets/foodManager.css");

.actions {
  vertical-align: middle;
}

.action-group {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 15px;
}

.action-group i {
  font-size: 1.1rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-group i:hover {
  transform: scale(1.2);
  opacity: 0.8;
}

/* Badge trạng thái mới */
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
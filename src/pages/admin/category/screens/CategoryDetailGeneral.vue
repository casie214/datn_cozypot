<script setup>
import { useCategoryDetailManager } from '../../../../services/foodFunction';
import CategoryDetailAddModal from '../modal/addModal/CategoryDetailAddModal.vue';
import CategoryDetailPutModal from '../modal/updateModal/CategoryDetailPutModal.vue';

// Import thêm statusSort
const {
  detailData, parentCategories, isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, getAllCategoriesDetail,
  paginatedData, searchQuery, categoryFilter, 
  sortOption, statusSort, // Import 2 biến sort
  currentPage, totalPages, visiblePages, itemsPerPage, changePage
} = useCategoryDetailManager();

const handleRefreshList = () => {
  setTimeout(() => { getAllCategoriesDetail(); }, 500);
};

import { ref } from 'vue';
const addFormData = ref({});
</script>

<template>
  <div class="tab-content">
    <div class="filter-box">
       <div class="filter-row">
            <div class="filter-item search">
                <label>Tìm kiếm</label>
                <div class="input-group">
                    <input v-model="searchQuery" class="form-control form-search" type="text" placeholder="Tìm kiếm (mã, tên)" />
                    <button class="search-btn">🔍</button>
                </div>
            </div>

            <div class="filter-item">
                <label>Danh mục</label>
                <select v-model="categoryFilter" class="form-control">
                    <option value="all">Tất cả</option>
                    <option v-for="cat in parentCategories" :key="cat.id" :value="cat.tenDanhMuc">
                        {{ cat.tenDanhMuc }}
                    </option>
                </select>
            </div>

            <div class="filter-item">
                <label>Trạng thái</label>
                <select v-model="statusSort" class="form-control">
                    <option value="default">Mặc định</option>
                    <option value="active_first">Đang hoạt động lên đầu</option>
                    <option value="inactive_first">Ngưng hoạt động lên đầu</option>
                </select>
            </div>

            <div class="filter-item">
                <label>Sắp xếp chính</label>
                <select v-model="sortOption" class="form-control">
                    <option value="id_desc">Mới nhất (ID giảm)</option>
                    <option value="id_asc">Cũ nhất (ID tăng)</option>
                    <option value="name_asc">Tên chi tiết (A-Z)</option>
                    <option value="parent_asc">Tên danh mục gốc (A-Z)</option>
                </select>
            </div>

            <button class="btn-clear" 
                @click="searchQuery=''; categoryFilter='all'; statusSort='default'; sortOption='id_desc'">
                Xóa bộ lọc
            </button>
        </div>
    </div>

    <div class="action-row" style="margin-left: auto;">
       <button class="btn-add" @click="isModalOpen = true">+ Thêm chi tiết</button>
    </div>

    <div class="table-container" style="min-height: 278px;">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>MÃ CHI TIẾT</th>
            <th>TÊN CHI TIẾT</th>
            <th>DANH MỤC GỐC (FK)</th>
            <th>MÔ TẢ</th>
            <th>TRẠNG THÁI</th>
            <th>CHỨC NĂNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="paginatedData.length === 0">
             <td colspan="7" style="text-align: center; padding: 20px; color: #888;">Không tìm thấy dữ liệu.</td>
          </tr>
          <tr v-for="(item, index) in paginatedData" :key="item.id">
            <td>{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
            <td>{{ item.maDanhMucChiTiet }}</td>
            <td><b>{{ item.tenDanhMucChiTiet }}</b></td>
            <td style="color: #8B0000; font-weight: 500;">{{ item.tenDanhMuc }}</td>
            <td>{{ item.moTa || '---' }}</td>
            <td :class="item.trangThai === 1 ? 'status-active' : 'status-inactive'">
              {{ item.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
            </td>
            <td class="actions">
              <div class="action-group">
                <i style="cursor:pointer" class="fas fa-pen edit-icon me-2" title="Xem chi tiết"
                  @click="openModal(item)"></i>

                <i v-if="item.trangThai === 1" class="fas  fa-unlock-alt unlock-icon" title="Khóa tài khoản"
                  @click="handleToggleStatus(item)"></i>
                <i v-else class="fas fa-lock lock-icon" title="Mở khóa tài khoản" @click="handleToggleStatus(item)"></i>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="totalPages > 1">
      <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1" :class="{ 'disabled': currentPage === 1 }">&lt;</button>
      <template v-for="(page, index) in visiblePages" :key="index">
        <button v-if="page === '...'" class="dots" disabled>...</button>
        <button v-else @click="changePage(page)" :class="{ 'active': currentPage === page }">{{ page }}</button>
      </template>
      <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages" :class="{ 'disabled': currentPage === totalPages }">&gt;</button>
    </div>

    <CategoryDetailAddModal :isOpen="isModalOpen" :formData="addFormData" @close="isModalOpen = false" @save="handleAdd" @refresh="handleRefreshList" />
    <CategoryDetailPutModal :isOpen="isModalUpdateOpen" :formData="addFormData" :itemList="selectedItem" @close="isModalUpdateOpen = false" @save="handleAdd" @refresh="handleRefreshList" />
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
</style>
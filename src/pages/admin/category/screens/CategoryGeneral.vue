<script setup>
import CategoryAddModal from '../modal/addModal/CategoryAddModal.vue';
import CategoryPutModal from '../modal/updateModal/CategoryPutModal.vue';
import { useCategoryManager } from '../../../../services/foodFunction';

// Import các biến mới
const { 
    categoryData, isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, getAllCategories,
    paginatedData, searchQuery, 
    sortOption, statusSort, // Import 2 biến sort
    currentPage, totalPages, visiblePages, itemsPerPage, changePage
} = useCategoryManager();

const handleRefreshList = () => {
    setTimeout(() => { getAllCategories(); }, 500);
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
            <input v-model="searchQuery" class="form-control form-search" type="text" placeholder="Tìm kiếm danh mục (mã, tên)" />
            <button class="search-btn">🔍</button>
          </div>
        </div>
        
        <div class="filter-item">
          <label>Trạng thái</label>
          <select v-model="statusSort" class="form-control">
            <option value="default">Mặc định (Hỗn hợp)</option>
            <option value="active_first">Đang hoạt động lên đầu</option>
            <option value="inactive_first">Ngưng hoạt động lên đầu</option>
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
      <button class="btn-add" @click="isModalOpen = true">+ Thêm danh mục</button>
    </div>

    <div class="table-container" style="min-height: 305px;">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>MÃ DANH MỤC</th>
            <th>TÊN DANH MỤC</th>
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
            <td><b>{{ item.tenDanhMuc }}</b></td>
            <td>{{ item.moTa || '---' }}</td>
            
            <td :class="item.trangThai === 1 ? 'status-active' : 'status-inactive'">
              {{ item.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
            </td>
            
            <td class="actions">
              <button class="btn-icon" @click="openModal(item)">✏️</button>
              <div class="toggle-switch" :class="{ 'on': item.trangThai === 1 }"
                   @click.stop="handleToggleStatus(item)">
                <div class="toggle-knob"></div>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="totalPages > 1">
      <button @click="changePage(currentPage - 1)" :disabled="currentPage === 1"
        :class="{ 'disabled': currentPage === 1 }">&lt;</button>
        
      <template v-for="(page, index) in visiblePages" :key="index">
        <button v-if="page === '...'" class="dots" disabled>...</button>
        <button v-else @click="changePage(page)" :class="{ 'active': currentPage === page }">{{ page }}</button>
      </template>
      
      <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages"
        :class="{ 'disabled': currentPage === totalPages }">&gt;</button>
    </div>

    <CategoryAddModal :isOpen="isModalOpen" :formData="addFormData" @close="isModalOpen = false" @save="handleRefreshList" @refresh="handleRefreshList"/>
    <CategoryPutModal :isOpen="isModalUpdateOpen" :formData="addFormData" :itemList="selectedItem" @close="isModalUpdateOpen = false" @save="handleRefreshList" @refresh="handleRefreshList"/>
  </div>
</template>

<style scoped>
@import url("/src/assets/foodManager.css");
</style>
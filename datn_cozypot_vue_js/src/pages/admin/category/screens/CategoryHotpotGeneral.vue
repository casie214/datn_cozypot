<script setup>
import { useHotpotSetTypeManager } from '../../../../services/foodFunction';
import CategoryHotpotAddModal from '../modal/addModal/CategoryHotpotAddModal.vue';
import CategoryHotpotPutModal from '../modal/updateModal/CategoryHotpotPutModal.vue';

// Import thêm các biến mới
const { 
    hotpotTypeData, isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, getAllHotpotType,
    // Biến mới
    paginatedData, searchQuery, statusFilter, sortOption, 
    currentPage, totalPages, visiblePages, itemsPerPage, changePage
} = useHotpotSetTypeManager();

const handleRefreshList = () => {
    setTimeout(() => { getAllHotpotType(); }, 500);
};

// Dummy data cho formData modal (nếu cần)
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
            <input v-model="searchQuery" class="form-control form-search" type="text" placeholder="Tìm kiếm loại lẩu (mã, tên)" />
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
          <label>Lọc theo</label>
          <select v-model="sortOption" class="form-control">
            <option value="id_desc">Mới nhất</option>
            <option value="id_asc">Cũ nhất</option>
            <option value="name_asc">Tên (A-Z)</option>
            <option value="status_active">Trạng thái: Đang hoạt động</option>
            <option value="status_inactive">Trạng thái: Ngưng hoạt động</option>
          </select>
        </div>

        <button class="btn-clear" @click="searchQuery = ''; statusFilter = 'all'; sortOption = 'id_desc'">
            Xóa bộ lọc
        </button>
      </div>
    </div>

    <div class="action-row" style="margin-left: auto;">
      <button class="btn-add" @click="isModalOpen = true">+ Thêm loại set</button>
    </div>

    <div class="table-container" style="min-height: 305px;">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>MÃ LOẠI</th>
            <th>TÊN LOẠI SET</th>
            <th>MÔ TẢ</th>
            <th>TRẠNG THÁI</th>
            <th>CHỨC NĂNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="paginatedData.length === 0">
             <td colspan="6" style="text-align: center; padding: 20px; color: #888;">
                 Không tìm thấy dữ liệu.
             </td>
          </tr>

          <tr v-for="(item, index) in paginatedData" :key="item.id">
            <td align="center">{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
            
            <td>{{ item.maLoaiSet }}</td>
            <td><b>{{ item.tenLoaiSet }}</b></td>
            <td>{{ item.moTa }}</td>
            
            <td :class="item.trangThai === 1 ? 'status-active' : 'status-inactive'">
              {{ item.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
            </td>
            
            <td class="actions">
              <button class="btn-icon" @click="openModal(item)">✏️</button>
              <div class="toggle-switch" :class="{ 'on': item.trangThai === 1 }" @click.stop="handleToggleStatus(item)">
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

    <CategoryHotpotAddModal :isOpen="isModalOpen" :formData="addFormData" @close="isModalOpen = false" @save="handleAdd" @refresh="handleRefreshList"/>
    <CategoryHotpotPutModal :isOpen="isModalUpdateOpen" :formData="addFormData" :itemList="selectedItem" @close="isModalUpdateOpen = false" @save="handleAdd" @refresh="handleRefreshList"/>
  </div>
</template>

<style scoped>
@import url("/src/assets/foodManager.css");
</style>
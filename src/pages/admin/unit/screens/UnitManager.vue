<script setup>
import { ref } from 'vue';
import CommonPagination from '@/components/commonPagination.vue';
import { useUnitManager } from '../../../../services/foodFunction';
import Swal from 'sweetalert2';
import UnitAddScreen from './UnitAddScreen.vue';
import UnitUpdateScreen from './UnitUpdateScreen.vue';

const {
    isLoading, paginatedData, searchQuery, sortOption, totalElements, listCategories,
    currentPage, totalPages, itemsPerPage, fetchAllData, exportToExcel
} = useUnitManager();

const isModalOpen = ref(false);
const isModalUpdateOpen = ref(false);
const selectedItem = ref(null);

const openUpdateModal = (item) => {
    selectedItem.value = item;
    isModalUpdateOpen.value = true;
};

const handleRefreshListBtn = () => {
    fetchAllData();
    Swal.fire({ 
        icon: 'success', 
        title: 'Đã cập nhật dữ liệu mới nhất', 
        timer: 1000, 
        showConfirmButton: false,
        toast: true,
        position: 'top-end'
    });
};

const handleRefresh = () => {
    fetchAllData();
};
</script>

<template>
  <div class="tab-content">
    <div class="filter-box">
      <div class="filter-row">
        <div class="filter-item search">
          <label>Tìm kiếm định lượng</label>
          <div class="input-group">
            <input v-model="searchQuery" class="form-control form-search" type="text" placeholder="Nhập tên hiển thị, kích cỡ..." />
            <button class="search-btn"><i class="fas fa-search"></i></button>
          </div>
        </div>

        <div class="filter-item">
          <label>Sắp xếp</label>
          <select v-model="sortOption" class="form-control">
            <option value="id_desc">Mới nhất</option>
            <option value="id_asc">Cũ nhất</option>
            <option value="name_asc">Tên hiển thị (A-Z)</option>
          </select>
        </div>

        <button class="btn-clear" @click="searchQuery = ''; sortOption = 'id_desc'">
            Xóa bộ lọc
        </button>
      </div>
    </div>

    <div class="action-row" style="margin-left: auto; display: flex; gap: 10px;">
      <button class="btn-action-icon btn-add-only" @click="isModalOpen = true" title="Thêm định lượng mới">
        <i class="fas fa-plus"></i>
      </button>
      <button class="btn-action-icon" @click="exportToExcel" title="Xuất Excel">
          <i class="fas fa-file-excel"></i>
      </button>
      <button class="btn-action-icon btn-refresh-only" @click="handleRefreshListBtn" title="Tải lại">
          <i class="fas fa-sync-alt"></i>
      </button>
    </div>

    <div class="table-container" style="min-height: 278px;">
      <div v-if="isLoading" class="text-center p-5">
          <i class="fas fa-spinner fa-spin fa-2x text-danger"></i>
          <p>Đang tải định lượng...</p>
      </div>
      
      <table v-else>
        <thead>
          <tr>
            <th style="width: 60px;">STT</th>
            <th>KÍCH CỠ / ĐƠN VỊ</th>
            <th>GIÁ TRỊ ĐỊNH LƯỢNG</th>
            <th>TÊN HIỂN THỊ</th>
            <th style="width: 100px;">HÀNH ĐỘNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="paginatedData.length === 0">
             <td colspan="5" style="text-align: center; padding: 40px; color: #888;">
                 <i class="fas fa-box-open fa-2x mb-2"></i><br>
                 Không tìm thấy dữ liệu định lượng nào.
             </td>
          </tr>

          <tr v-for="(item, index) in paginatedData" :key="item.id">
            <td class="text-center">{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
            
            <td class="fw-bold">{{ item.kichCo || '---' }}</td>
            <td>
                <span class="badge bg-secondary">{{ item.dinhLuong || '---' }}</span>
            </td>
            <td>{{ item.tenHienThi }}</td>
            
            <td class="actions text-center">
              <div class="action-group">
                <i class="fas fa-pen" title="Chỉnh sửa" @click="openUpdateModal(item)"></i>
                </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div style="padding-bottom: 30px;" class="pagination">
           <CommonPagination
                v-model:currentPage="currentPage"
                v-model:pageSize="itemsPerPage"
                :total-pages="totalPages"
                :total-elements="totalElements"
                :current-count="paginatedData.length"
                @change="handleRefresh" 
            />
      </div>
    </div>

    </div>

<UnitAddScreen 
    :isOpen="isModalOpen",
    :categories="listCategories"
    @close="isModalOpen = false" 
    @refresh="handleRefresh" 
/>
<UnitUpdateScreen
    :isOpen="isModalUpdateOpen" 
    :itemList="selectedItem" 
    @close="isModalUpdateOpen = false" 
    @refresh="handleRefresh" 
/>
</template>

<style scoped>
@import url("/src/assets/foodManager.css");

.action-group {
    display: flex;
    justify-content: center;
    gap: 15px;
}

.edit-icon { 
    color: #FF9800; 
    cursor: pointer;
    font-size: 1.1rem;
    transition: transform 0.2s;
}

.edit-icon:hover { transform: scale(1.2); }

.btn-add-only {
    background-color: #8B0000 !important;
    color: white !important;
}

.btn-action-icon {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    border: 1px solid #eee !important;
    transition: all 0.2s ease;
}

.btn-action-icon:hover {
    background-color: #f8f9fa;
    box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}


</style>
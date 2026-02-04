<script setup>
import CommonPagination from '@/components/commonPagination.vue';
import { useHotpotSetTypeManager } from '../../../../services/foodFunction';
import CategoryHotpotAddModal from '../modal/addModal/CategoryHotpotAddModal.vue';
import CategoryHotpotPutModal from '../modal/updateModal/CategoryHotpotPutModal.vue';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
const { 
    hotpotTypeData, isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, getAllHotpotType,
    paginatedData, searchQuery, statusFilter, sortOption, totalElements, 
    currentPage, totalPages, visiblePages, itemsPerPage, changePage
} = useHotpotSetTypeManager();
const router = useRouter();

const handleRefreshList = () => {
    setTimeout(() => { getAllHotpotType(); }, 500);
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
            <input v-model="searchQuery" class="form-control form-search" type="text" placeholder="Tìm kiếm loại lẩu (mã, tên)" />
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

    <div class="table-container" style="min-height: 278px;">
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
            <td>{{ item.tenLoaiSet }}</td>
            <td>{{ item.moTa }}</td>
            
            <td :class="item.trangThai === 1 ? '' : ''">
              {{ item.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
            </td>
            
            <td class="actions">
              <div class="action-group">
                <i style="cursor:pointer" class="fa-solid fa-list" title="Xem chi tiết"
                  @click="goToHotpotList(item)"></i>

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
      <div style="padding-bottom: 30px;" class="pagination">
           <CommonPagination
                v-model:currentPage="currentPage"
                v-model:pageSize="itemsPerPage"
                :total-pages="totalPages"
                :total-elements="totalElements"
                :current-count="paginatedData.length"
                @change="() => {}" 
            />
      </div>
    </div>

    <CategoryHotpotAddModal :isOpen="isModalOpen" :formData="addFormData" @close="isModalOpen = false" @save="handleAdd" @refresh="handleRefreshList"/>
    <CategoryHotpotPutModal :isOpen="isModalUpdateOpen" :formData="addFormData" :itemList="selectedItem" @close="isModalUpdateOpen = false" @save="handleAdd" @refresh="handleRefreshList"/>
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
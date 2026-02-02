<script setup>
import CategoryAddModal from '../modal/addModal/CategoryAddModal.vue';
import CategoryPutModal from '../modal/updateModal/CategoryPutModal.vue';
import { useCategoryManager } from '../../../../services/foodFunction';
import { ref } from 'vue';
import CommonPagination from '@/components/commonPagination.vue';
import { useRouter } from 'vue-router';

const { 
    categoryData, isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, getAllCategories,
    paginatedData, searchQuery, totalElements,
    sortOption, statusSort, 
    currentPage, totalPages, visiblePages, itemsPerPage, changePage
} = useCategoryManager();

const handleRefreshList = () => {
    setTimeout(() => { getAllCategories(); }, 500);
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

    <div class="table-container" style="min-height: 278px;">
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
              <div class="action-group">
                <i style="cursor:pointer;" 
                   class="fas fa-list-ul view-icon me-2" 
                   title="Xem danh sách món thuộc danh mục này"
                   @click="goToFoodList(item)">
                </i>

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

    <CategoryAddModal :isOpen="isModalOpen" :formData="addFormData" @close="isModalOpen = false" @save="handleRefreshList" @refresh="handleRefreshList"/>
    <CategoryPutModal :isOpen="isModalUpdateOpen" :formData="addFormData" :itemList="selectedItem" @close="isModalUpdateOpen = false" @save="handleRefreshList" @refresh="handleRefreshList"/>
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
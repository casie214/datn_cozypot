<script setup>
import { useRouter } from 'vue-router'; 
import { useHotpotManager } from '../../../../services/foodFunction';
import FoodHotpotModal from '../../food/modal/updateModal/foodHotpotModal.vue';

const router = useRouter();

const {
  isModalOpen,
  selectedHotpot,
  handleViewDetails,
  handleToggleStatus,
  getAllHotpot,
  
  paginatedData,    
  searchQuery,      
  sortOption,       
  currentPage,
  totalPages,
  visiblePages,
  itemsPerPage,
  goToPage
} = useHotpotManager();

const handleRefreshList = () => {
    setTimeout(() => { getAllHotpot(); }, 500);
};

const goToAddScreen = () => {
    router.push({ name: 'addHotpotSet' });
};

const handleViewDetailUpdate = (item) => {
    router.push({ 
        name: 'updateHotpotSet', 
        params: { id: item.id } 
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
            <input 
                v-model="searchQuery" 
                type="text" 
                class="form-search form-control" 
                placeholder="Tìm kiếm set lẩu (mã, tên)" 
            />
            <button class="search-btn">🔍</button>
          </div>
        </div>

        <div class="filter-item"><label>Trạng thái</label><select><option>Tất cả</option></select></div>
        <div class="filter-item"><label>Loại set lẩu</label><select><option>Tất cả</option></select></div>
        
        <div class="filter-item">
            <label>Sắp xếp theo</label>
            <select v-model="sortOption">
                <option value="newest">Mặc định</option>
                <option value="name_asc">Tên (A-Z)</option>
                <option value="price_asc">Giá tăng dần</option>
                <option value="price_desc">Giá giảm dần</option>
            </select>
        </div>
        
        <button class="btn-clear" @click="searchQuery = ''; sortOption = 'newest'">Xóa bộ lọc</button>
      </div>
    </div>

    <div class="action-row">
      <button class="btn-add" @click="goToAddScreen">Thêm set lẩu</button>
    </div>

    <div class="table-container" style="min-height: 305px;">
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
            <td align="center">{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
            
            <td>{{ item.maSetLau }}</td>
            <td><b>{{ item.tenSetLau }}</b></td>
            <td style="color:#d32f2f"><b>{{ item.giaBan?.toLocaleString() }} đ</b></td>
            <td>{{ item.tenLoaiSet }}</td>
            
            <td :class="item.trangThai === 1 ? 'status-active' : 'status-inactive'">
              {{ item.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
            </td>
            
            <td class="actions">
              <button class="btn-icon" @click="handleViewDetailUpdate(item)">👁️</button>
              <div class="toggle-switch" :class="{ 'on': item.trangThai === 1 }"
                   @click.stop="handleToggleStatus(item)">
                   <div class="toggle-knob"></div>
              </div>
            </td>
          </tr>
          
          <tr v-if="paginatedData.length === 0">
              <td colspan="7" style="text-align: center; padding: 20px; color: #888;">
                  Không tìm thấy dữ liệu phù hợp
              </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <div class="pagination" v-if="totalPages > 1">
    <button 
        @click="goToPage(currentPage - 1)" 
        :disabled="currentPage === 1"
        :class="{ 'disabled': currentPage === 1 }"
    >
        &lt;
    </button>
    
    <button 
        v-for="(page, index) in visiblePages" 
        :key="index"
        :class="{ 'active': page === currentPage, 'dots': page === '...' }"
        @click="page !== '...' ? goToPage(page) : null"
        :disabled="page === '...'"
    >
        {{ page }}
    </button>
    
    <button 
        @click="goToPage(currentPage + 1)" 
        :disabled="currentPage === totalPages"
        :class="{ 'disabled': currentPage === totalPages }"
    >
        &gt;
    </button>
  </div>

</template>

<style scoped src="/src/assets/foodManager.css"></style>
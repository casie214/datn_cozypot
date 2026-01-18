<script setup>
// ... Imports giữ nguyên
import { useFoodDetailManager } from '../../../../services/foodFunction'; // Sửa lại đường dẫn import file JS nếu cần
import { useRouter } from 'vue-router';

const {
  // Lấy các biến mới ra
  paginatedData, // Dùng cái này thay cho detailData ở v-for
  searchQuery,
  currentPage,
  totalPages,
  visiblePages,
  itemsPerPage,
  changePage,
  // Các biến cũ
  getAllFoodDetails,
  handleViewDetails: _handleViewDetails, // Nếu trong composable không return thì giữ nguyên logic cũ ở đây
  handleToggleStatus
} = useFoodDetailManager();

const router = useRouter();

// ... Giữ nguyên các hàm routing goToAddScreen, handleViewDetails ...
const goToAddScreen = () => router.push({ name: 'addFoodDetail' });
const handleViewDetails = (item) => router.push({ name: 'updateFoodDetail', params: { id: item.id } });

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
                placeholder="Tìm chi tiết (mã, tên)" 
                @input="currentPage = 1" 
            /> 
            <button class="search-btn">🔍</button>
          </div>
        </div>
        </div>
    </div>

    <div class="action-row">
      <button class="btn-add" @click="goToAddScreen">+ Thêm chi tiết món</button>
    </div>

    <div class="table-container" style="min-height: 305px;">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>MÃ CHI TIẾT</th>
            <th>TÊN CHI TIẾT</th>
            <th>MÓN ĂN GỐC</th>
            <th>GIÁ BÁN</th>
            <th>KÍCH CỠ</th>
            <th>ĐƠN VỊ</th>
            <th>TRẠNG THÁI</th>
            <th>CHỨC NĂNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in paginatedData" :key="item.id">
            
            <td align="center">{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
            
            <td>{{ item.maChiTietMonAn }}</td>
            <td><b>{{ item.tenChiTietMonAn }}</b></td>
            <td>{{ item.monAnDiKem ? item.monAnDiKem.tenMonAn : (item.tenMonAnDiKem || '---') }}</td>
            <td style="color:#d32f2f; font-weight:bold">{{ item.giaBan?.toLocaleString() }}</td>
            <td>{{ item.kichCo }}</td>
            <td>{{ item.donVi }}</td>
            
            <td :class="item.trangThai ? 'status-active' : 'status-inactive'">
              {{ item.trangThai ? 'Đang hoạt động' : 'Ngưng bán' }}
            </td>
            
            <td class="actions">
              <button class="btn-icon" @click="handleViewDetails(item)">✏️</button>
              <div class="toggle-switch" :class="{ 'on': item.trangThai === 1 }" @click.stop="handleToggleStatus(item)">
                <div class="toggle-knob"></div>
              </div>
            </td>
          </tr>
          
          <tr v-if="paginatedData.length === 0">
              <td colspan="9" style="text-align: center; padding: 20px; color: #888;">
                  Không tìm thấy dữ liệu phù hợp
              </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="totalPages > 1">
      <button 
        @click="changePage(currentPage - 1)" 
        :disabled="currentPage === 1"
        :class="{ 'disabled': currentPage === 1 }"
      >
        &lt;
      </button>

      <template v-for="(page, index) in visiblePages" :key="index">
          <button 
            v-if="page === '...'" 
            class="dots" 
            disabled
          >...</button>
          
          <button 
            v-else 
            @click="changePage(page)" 
            :class="{ 'active': currentPage === page }"
          >
            {{ page }}
          </button>
      </template>

      <button 
        @click="changePage(currentPage + 1)" 
        :disabled="currentPage === totalPages"
        :class="{ 'disabled': currentPage === totalPages }"
      >
        &gt;
      </button>
    </div>

  </div>
</template>

<style scoped src="/src/assets/foodManager.css"></style>
<style scoped>

</style>
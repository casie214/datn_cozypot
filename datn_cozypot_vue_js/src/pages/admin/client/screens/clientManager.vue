<template>
  <div class="flex-grow-1 client-manager-wrapper">
    <div class="d-flex justify-content-between align-items-center mb-3">
  <h2 class="title-page">Quản lý khách hàng</h2>
  <div class="d-flex gap-2"> <button class="btn-export-excel" @click="handleExportExcel" :disabled="exportLoading">
      <span v-if="exportLoading" class="spinner-border spinner-border-sm me-1"></span>
      <i v-else class="fas fa-file-excel me-1"></i> Xuất Excel
    </button>
    
    <button class="btn-red-dark" @click="openModalAdd">
      <i class="fas fa-plus me-2"></i> Thêm khách hàng
    </button>
  </div>
</div>

    <div class="filter-card">
      <div class="row g-3 align-items-end">
        <div class="col-md-4">
          <label class="filter-label">Tìm kiếm</label>
          <input v-model="filters.keyword" class="form-control custom-input" placeholder="Mã, tên, SĐT, email..." @input="onSearchInput">
        </div>
        <div class="col-md-3">
          <label class="filter-label">Trạng thái</label>
          <select v-model="filters.trangThai" class="form-select custom-input" @change="handleSearch">
            <option :value="null">Tất cả trạng thái</option>
            <option :value="1">Đang hoạt động</option>
            <option :value="0">Ngừng hoạt động</option>
          </select>
        </div>
        <div class="col-md-3">
          <label class="filter-label">Ngày tạo từ</label>
          <input type="date" v-model="filters.tuNgay" class="form-control custom-input" @change="handleSearch">
        </div>
        <div class="col-md-2">
          <button class="btn-red-dark w-100" @click="handleSearch">
            <i class="fas fa-search me-1"></i> Tìm kiếm
          </button>
        </div>
      </div>
    </div>

    <div class="table-container shadow-sm mt-3">
      <table class="table mb-0 custom-table">
        <thead>
          <tr>
            <th>STT</th>
            <th>MÃ</th>
            <th>HỌ TÊN</th>
            <th>SĐT</th>
            <th>EMAIL</th>
            <th>TÍCH ĐIỂM</th>
            <th>GIỚI TÍNH</th>
            <th>TRẠNG THÁI</th>
            <th class="text-center">HÀNH ĐỘNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(kh, index) in listKhachHang" :key="kh.id">
            <td>{{ (pagination.currentPage - 1) * pagination.pageSize + index + 1 }}</td>
            <td class="fw-bold text-dark">{{ kh.maKhachHang }}</td>
            <td>{{ kh.tenKhachHang }}</td>
            <td>{{ kh.soDienThoai }}</td>
            <td class="text-dark">{{ kh.email }}</td>
            <td class="points-highlight">{{ kh.diemTichLuy || 0 }}</td>
            <td>{{ kh.gioiTinh ? 'Nam' : 'Nữ' }}</td>
            <td>
              <span :class="getStatusDisplay(kh.trangThai).class">
                {{ getStatusDisplay(kh.trangThai).text }}
              </span>
            </td>
            <td class="text-center">
              <div class="action-group">
                <i class="fas fa-eye view-icon me-2" @click="openModalView(kh.id)"></i>
                <i class="fas fa-pen edit-icon me-2" :class="{ 'disabled-icon': kh.trangThai === 0 }" @click="kh.trangThai === 1 ? openModalEdit(kh.id) : warnLocked()"></i>
                <i v-if="kh.trangThai === 1" class="fas fa-unlock-alt unlock-icon" @click="handleToggleStatus(kh)"></i>
                <i v-else class="fas fa-lock lock-icon" @click="handleToggleStatus(kh)"></i>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="listKhachHang.length === 0" class="p-5 text-center text-muted">Không tìm thấy dữ liệu khách hàng</div>
    </div>

    <div class="pagination-wrapper d-flex justify-content-between align-items-center mt-4 px-2">
      <div class="d-flex align-items-center">
        <span class="me-2 text-muted">Hiển thị</span>
        <select v-model="pagination.pageSize" class="form-select form-select-sm select-per-page" @change="handleSearch">
          <option :value="5">5 dòng</option>
          <option :value="8">8 dòng</option>
          <option :value="10">10 dòng</option>
          <option :value="20">20 dòng</option>
        </select>
      </div>

      <div class="pagination-controls d-flex align-items-center">
        <button class="btn-page" :disabled="pagination.currentPage === 1" @click="goToPage(1)">
          <i class="fas fa-step-backward"></i>
        </button>
        <button class="btn-page" :disabled="pagination.currentPage === 1" @click="changePage(-1)">
          <i class="fas fa-chevron-left"></i>
        </button>

        <div class="mx-3 d-flex align-items-center">
          <template v-if="pagination.totalPages <= 5">
            <span v-for="p in pagination.totalPages" :key="p" 
                  class="page-number" :class="{ active: pagination.currentPage === p }" 
                  @click="goToPage(p)">{{ p }}</span>
          </template>
          <template v-else>
            <div class="input-page-wrapper d-flex align-items-center">
              <input type="number" v-model.number="inputPage" class="input-go-to" @keyup.enter="jumpToPage">
              <span class="ms-2 text-muted">/ {{ pagination.totalPages }}</span>
            </div>
          </template>
        </div>

        <button class="btn-page" :disabled="pagination.currentPage === pagination.totalPages" @click="changePage(1)">
          <i class="fas fa-chevron-right"></i>
        </button>
        <button class="btn-page" :disabled="pagination.currentPage === pagination.totalPages" @click="goToPage(pagination.totalPages)">
          <i class="fas fa-step-forward"></i>
        </button>
      </div>

      <div class="total-info text-muted">
        Hiển thị {{ listKhachHang.length }} trong tổng số {{ pagination.totalElements }} khách hàng
      </div>
    </div>

    <CustomerModal v-if="isModalOpen" :client-id="selectedId" @close="closeModal" @refresh="handleSearch" />
    <CustomerDetailModal v-if="isDetailModalOpen" :client-id="selectedId" @close="closeDetailModal" />
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue'; // Thêm watch
import { useClientLogic } from './clientFunction.js'; 
import CustomerModal from '../modal/clientModal.vue';
import CustomerDetailModal from '../modal/clientDetailModal.vue';
import '../clientStyle.css'; 
import clientService from '@/services/clientService';

const { getStatusDisplay, fetchData } = useClientLogic();

// --- State ---
const listKhachHang = ref([]);
const filters = reactive({ 
  keyword: '', 
  trangThai: null, 
  tuNgay: '' 
});

const pagination = reactive({ 
  currentPage: 1, 
  pageSize: 8, 
  totalPages: 0,
  totalElements: 0 // Cần tổng số bản ghi để hiển thị text bên phải
});

const inputPage = ref(1); // Biến cho ô nhập số
const isModalOpen = ref(false);
const isDetailModalOpen = ref(false);
const selectedId = ref(null);

// --- Logic ---
const handleSearch = async () => {
  try {
    const data = await fetchData(filters, pagination);
    listKhachHang.value = data.content || [];
    pagination.totalPages = data.totalPages || 0;
    pagination.totalElements = data.totalElements || 0;
  } catch (error) {
    console.error("Lỗi khi load danh sách khách hàng:", error);
  }
};

// Đồng bộ ô nhập số khi trang thay đổi
watch(() => pagination.currentPage, (val) => {
  inputPage.value = val;
});

const jumpToPage = () => {
  let p = parseInt(inputPage.value);
  if (p >= 1 && p <= pagination.totalPages) {
    goToPage(p);
  } else {
    inputPage.value = pagination.currentPage;
  }
};

const goToPage = (page) => {
  pagination.currentPage = page;
  handleSearch();
};

const onSearchInput = () => {
  clearTimeout(window.debounceTimer);
  window.debounceTimer = setTimeout(() => {
    pagination.currentPage = 1;
    handleSearch();
  }, 500);
};

const changePage = (step) => {
  const newPage = pagination.currentPage + step;
  if (newPage >= 1 && newPage <= pagination.totalPages) {
    pagination.currentPage = newPage;
    handleSearch();
  }
};

const handleToggleStatus = async (kh) => {
  const isLocking = kh.trangThai === 1;
  const message = isLocking 
    ? `Bạn có chắc chắn muốn khóa tài khoản "${kh.maKhachHang}"?` 
    : `Bạn có chắc chắn muốn mở khóa tài khoản "${kh.maKhachHang}"?`;

  if (confirm(message)) {
    try {
      await clientService.toggleStatus(kh.id);
      handleSearch();
    } catch (error) {
      alert("Lỗi: " + error.message);
    }
  }
};

const exportLoading = ref(false); // Biến trạng thái khi đang tải file

const handleExportExcel = async () => {
  try {
    exportLoading.value = true;
    
    // Gọi API từ service đã bổ sung hàm exportExcel trước đó
    const response = await clientService.exportExcel({
      keyword: filters.keyword,
      trangThai: filters.trangThai,
      tuNgay: filters.tuNgay
    });

    // Xử lý tạo file để trình duyệt tự tải về
    const blob = new Blob([response.data], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    
    // Đặt tên file theo ngày hiện tại
    const dateStr = new Date().toISOString().slice(0, 10);
    link.setAttribute('download', `Danh_Sach_Khach_Hang_${dateStr}.xlsx`);
    
    document.body.appendChild(link);
    link.click();
    
    // Dọn dẹp bộ nhớ
    link.remove();
    window.URL.revokeObjectURL(url);
    
  } catch (error) {
    console.error("Lỗi xuất file:", error);
    alert("Không thể xuất file Excel lúc này!");
  } finally {
    exportLoading.value = false;
  }
};

// Các hàm modal giữ nguyên
const openModalAdd = () => { selectedId.value = null; isModalOpen.value = true; };
const openModalEdit = (id) => { selectedId.value = id; isModalOpen.value = true; };
const openModalView = (id) => { selectedId.value = id; isDetailModalOpen.value = true; };
const closeModal = () => isModalOpen.value = false;
const closeDetailModal = () => isDetailModalOpen.value = false;
const warnLocked = () => alert("Vui lòng mở khóa trước khi chỉnh sửa!");

onMounted(handleSearch);
</script>
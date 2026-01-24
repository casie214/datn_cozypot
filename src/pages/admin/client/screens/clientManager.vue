<template>
  <div class="flex-grow-1 client-manager-wrapper">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h2 class="title-page">Quản lý khách hàng</h2>
      <button class="btn-red-dark" @click="openModalAdd">
        <i class="fas fa-plus me-2"></i> Thêm khách hàng
      </button>
    </div>

    <div class="filter-card">
      <div class="row g-3 align-items-end">
        <div class="col-md-4">
          <label class="filter-label">Tìm kiếm</label>
          <input 
            v-model="filters.keyword" 
            class="form-control custom-input" 
            placeholder="Mã, tên, SĐT, email..."
            @input="onSearchInput"
          >
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

    <div class="table-container shadow-sm">
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
            <td class="points-highlight">
              {{ kh.diemTichLuy || 0 }}
            </td>            
            <td>{{ kh.gioiTinh ? 'Nam' : 'Nữ' }}</td>
            <td>
              <span :class="getStatusDisplay(kh.trangThai).class">
                {{ getStatusDisplay(kh.trangThai).text }}
              </span>
            </td>
            <td class="text-center">
  <div class="action-group">
    <i 
      class="fas fa-eye view-icon me-2" 
      title="Xem chi tiết" 
      @click="openModalView(kh.id)"
    ></i>

    <i 
      class="fas fa-pen edit-icon me-2" 
      :class="{ 'disabled-icon': kh.trangThai === 0 }"
      :title="kh.trangThai === 0 ? 'Tài khoản đang bị khóa, không thể sửa' : 'Chỉnh sửa'" 
      @click="kh.trangThai === 1 ? openModalEdit(kh.id) : warnLocked()"
    ></i>

    <i 
      v-if="kh.trangThai === 1" 
      class="fas  fa-unlock-alt unlock-icon" 
      title="Khóa tài khoản" 
      @click="handleToggleStatus(kh)"
    ></i>
    <i 
      v-else 
      class="fas fa-lock lock-icon" 
      title="Mở khóa tài khoản" 
      @click="handleToggleStatus(kh)"
    ></i>
  </div>
</td>
          </tr>
        </tbody>
      </table>
      <div v-if="listKhachHang.length === 0" class="p-5 text-center text-muted">
        Không tìm thấy dữ liệu khách hàng
      </div>
    </div>

    <nav v-if="pagination.totalPages > 1" class="mt-4 d-flex justify-content-center">
      <ul class="pagination pagination-custom">
        <li class="page-item" :class="{ disabled: pagination.currentPage === 1 }">
          <button class="page-link" @click="changePage(-1)">&lt;</button>
        </li>
        <li v-for="p in pagination.totalPages" :key="p" class="page-item" :class="{active: pagination.currentPage === p}">
          <button class="page-link" @click="pagination.currentPage = p; handleSearch()">{{p}}</button>
        </li>
        <li class="page-item" :class="{ disabled: pagination.currentPage === pagination.totalPages }">
          <button class="page-link" @click="changePage(1)">&gt;</button>
        </li>
      </ul>
    </nav>

    <CustomerModal 
      v-if="isModalOpen" 
      :client-id="selectedId" 
      @close="closeModal" 
      @refresh="handleSearch"
    />

    <CustomerDetailModal 
      v-if="isDetailModalOpen" 
      :client-id="selectedId" 
      @close="closeDetailModal"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { useClientLogic } from './clientFunction.js'; 
import CustomerModal from '../modal/clientModal.vue';
import CustomerDetailModal from '../modal/clientDetailModal.vue';
import '../clientStyle.css'; 
import clientService from '@/services/clientService'; // Đảm bảo đã import service

const { getStatusDisplay, fetchData } = useClientLogic();

// --- State ---
const listKhachHang = ref([]);
const filters = reactive({ 
  keyword: '', 
  trangThai: null, 
  tuNgay: '' 
});
const pagination = reactive({ currentPage: 1, pageSize: 8, totalPages: 0 });

const isModalOpen = ref(false);
const isDetailModalOpen = ref(false);
const selectedId = ref(null);

// --- Logic ---
const handleSearch = async () => {
  try {
    const data = await fetchData(filters, pagination);
    listKhachHang.value = data.content || [];
    pagination.totalPages = data.totalPages || 0;
  } catch (error) {
    console.error("Lỗi khi load danh sách khách hàng:", error);
  }
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

onMounted(handleSearch);

// --- Modal Controls ---
const openModalAdd = () => {
  selectedId.value = null;
  isModalOpen.value = true;
};

const openModalEdit = (id) => {
  selectedId.value = id;
  isModalOpen.value = true;
};

const openModalView = (id) => {
  selectedId.value = id;
  isDetailModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
};

const closeDetailModal = () => {
  isDetailModalOpen.value = false;
};

// Thêm hàm cảnh báo khi ấn vào nút sửa của tài khoản bị khóa
const warnLocked = () => {
  alert("Tài khoản này đang ngừng hoạt động. Vui lòng mở khóa trước khi chỉnh sửa thông tin!");
};

// Hàm xử lý Khóa / Mở khóa
const handleToggleStatus = async (kh) => {
  const isLocking = kh.trangThai === 1;
  const message = isLocking 
    ? `Bạn có chắc chắn muốn khóa tài khoản của khách hàng "${kh.maKhachHang}"?)` 
    : `Bạn có chắc chắn muốn mở khóa tài khoản của khách hàng "${kh.maKhachHang}"?`;

  if (confirm(message)) {
    try {
      await clientService.toggleStatus(kh.id);
      alert(`${isLocking ? 'Khóa' : 'Mở khóa'} thành công!`);
      handleSearch(); // Load lại danh sách, Backend sẽ tự động đẩy tài khoản này xuống cuối (do sort ở Service)
    } catch (error) {
      console.error(error);
      alert("Có lỗi xảy ra khi thực hiện thao tác này.");
    }
  }
};
</script>
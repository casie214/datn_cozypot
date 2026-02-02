<template>
  <div class="d-flex">
    <sidebar />

    <div class="flex-grow-1 staff-manager-wrapper">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h2 class="title-page">Quản lý nhân viên</h2>
        <div class="d-flex gap-2"> <button class="btn-export-excel" @click="exportToExcel">
            <i class="fas fa-file-export me-2"></i> Xuất Excel
          </button>
          <button class="btn-red-dark" @click="openModalAdd">
            <i class="fas fa-plus me-2"></i> Thêm nhân viên
          </button>
        </div>
      </div>

      <div class="filter-card">
        <div class="row g-3 align-items-end">
          <div class="col-md-4">
            <label class="filter-label">Tìm kiếm</label>
            <input 
              v-model="filters.keyword" 
              class="form-control custom-input" 
              placeholder="Tên, mã, email, SĐT..."
              @input="onSearchInput"
            >
          </div>
          <div class="col-md-3">
            <label class="filter-label">Trạng thái</label>
            <select v-model="filters.trangThai" class="form-select custom-input" @change="handleSearch">
              <option :value="null">Tất cả</option>
              <option :value="1">Đang làm việc</option>
              <option :value="2">Ngừng hoạt động</option>
            </select>
          </div>
          <div class="col-md-3">
            <label class="filter-label">Từ ngày</label>
            <input type="date" v-model="filters.tuNgay" class="form-control custom-input" @change="handleSearch">
          </div>
          <div class="col-md-2">
            <button class="btn-red-dark w-100" @click="handleSearch">
              <i class="fas fa-search"></i> Tìm kiếm
            </button>
          </div>
        </div>
      </div>

      <div class="table-container">
        <table class="table mb-0 custom-table">
          <thead>
            <tr>
              <th>STT</th>
              <th>MÃ</th>
              <th>HỌ TÊN</th>
              <th>SĐT</th>
              <th>EMAIL</th>
              <th>NGÀY VÀO LÀM</th>
              <th>GIỚI TÍNH</th>
              <th>TRẠNG THÁI</th>
              <th class="text-center">HÀNH ĐỘNG</th>
            </tr>
          </thead>
          <tbody>
            <tr 
              v-for="(nv, index) in listNhanVien" 
              :key="nv.id"
              :class="{ 'row-staff-locked': nv.trangThaiLamViec === 2 }"
            >
              <td>{{ (pagination.currentPage - 1) * pagination.pageSize + index + 1 }}</td>
              <td class="fw-bold">{{ nv.maNhanVien }}</td>
              <td>{{ nv.hoTenNhanVien }}</td>
              <td>{{ nv.sdtNhanVien }}</td>
              <td class="text-muted">{{ nv.email }}</td>
              <td>{{ formatDate(nv.ngayVaoLam) }}</td>              
              <td>{{ nv.gioiTinh ? 'Nam' : 'Nữ' }}</td>
              
              <td :class="{ 'cell-locked': nv.trangThaiLamViec === 2 }">
                <span :class="getStatusDisplay(nv.trangThaiLamViec).class">
                  {{ getStatusDisplay(nv.trangThaiLamViec).text }}
                </span>
              </td>

              <td class="text-center" :class="{ 'cell-locked': nv.trangThaiLamViec === 2 }">
                <div class="action-group">
                  <i 
                    class="fas fa-eye detail-icon text-dark me-2" 
                    title="Xem chi tiết"
                    style="cursor: pointer;"
                    @click="openModalView(nv.id)"
                  ></i>

                  <i 
                    class="fas fa-pen edit-icon me-2" 
                    :class="{ 'disabled-icon': nv.trangThaiLamViec === 2 }"
                    title="Sửa"
                    @click="nv.trangThaiLamViec === 1 ? openModalEdit(nv.id) : null"
                  ></i>

                  <i 
                    v-if="nv.trangThaiLamViec === 1" 
                    class="fas fa-unlock-alt unlock-icon text-danger" 
                    title="Khóa"
                    @click="onToggleStatus(nv)"
                  ></i>
                  <i 
                    v-else 
                    class="fas fa-lock lock-icon text-success" 
                    title="Mở khóa"
                    @click="onToggleStatus(nv)"
                  ></i>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="listNhanVien.length === 0" class="p-5 text-center text-muted">
          Không tìm thấy dữ liệu
        </div>
      </div>

      <div class="pagination-wrapper d-flex justify-content-between align-items-center mt-4">
  <div class="d-flex align-items-center">
    <span class="me-2">Hiển thị</span>
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
      <span 
        v-for="p in pagination.totalPages" 
        :key="p" 
        class="page-number" 
        :class="{ active: pagination.currentPage === p }"
        @click="goToPage(p)"
      >
        {{ p }}
      </span>
    </template>

    <template v-else>
      <div class="input-page-wrapper d-flex align-items-center">
        <input 
          type="number" 
          v-model.number="inputPage" 
          class="input-go-to" 
          @keyup.enter="jumpToPage"
        >
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
    Hiển thị {{ listNhanVien.length }} trong tổng số {{ pagination.totalElements }} nhân viên
  </div>
</div>
    </div>

    <StaffModal 
      v-if="isModalOpen" 
      :staff-id="selectedStaffId" 
      @close="closeModal" 
      @refresh="handleSearch"
    />

    <StaffDetailModal
      v-if="isDetailModalOpen"
      :staff-id="detailStaffId"
      @close="closeDetailModal"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue'; // Đã thêm watch
import { useStaffLogic } from './staffFunction.js';
import StaffModal from '../modal/staffModal.vue';
import StaffDetailModal from '../modal/staffDetailModal.vue';
import dayjs from 'dayjs';
import '../staffStyle.css';
import staffService from '@/services/staffService.js';

const { getStatusDisplay, fetchData, toggleStaffStatus } = useStaffLogic();

// --- STATE QUẢN LÝ ---
const listNhanVien = ref([]);
const filters = reactive({ 
  keyword: '', 
  trangThai: null, 
  tuNgay: '' 
});

const pagination = reactive({ 
  currentPage: 1, 
  pageSize: 8, 
  totalPages: 0,
  totalElements: 0 
});

const inputPage = ref(1); // Biến hỗ trợ ô nhập số trang
const isModalOpen = ref(false);
const selectedStaffId = ref(null);
const isDetailModalOpen = ref(false);
const detailStaffId = ref(null);

// --- LOGIC XỬ LÝ DỮ LIỆU ---

// Đồng bộ ô nhập số mỗi khi trang hiện tại thay đổi (do bấm nút mũi tên)
watch(() => pagination.currentPage, (newVal) => {
  inputPage.value = newVal;
});

const formatDate = (date) => {
  if (!date) return '---';
  return dayjs(date).format('DD/MM/YYYY');
};

const handleSearch = async () => {
  try {
    const data = await fetchData(filters, pagination);
    listNhanVien.value = data.content || [];
    pagination.totalPages = data.totalPages || 0;
    pagination.totalElements = data.totalElements || 0;
  } catch (error) {
    console.error("Lỗi khi load danh sách:", error);
  }
};

const onSearchInput = () => {
  clearTimeout(window.debounceTimer);
  window.debounceTimer = setTimeout(() => {
    pagination.currentPage = 1;
    handleSearch();
  }, 500);
};

// Hàm nhảy trang khi nhập số và nhấn Enter
const jumpToPage = () => {
  let p = parseInt(inputPage.value);
  if (p >= 1 && p <= pagination.totalPages) {
    goToPage(p);
  } else {
    // Nếu nhập sai, trả về số trang hiện tại của hệ thống
    inputPage.value = pagination.currentPage;
  }
};

const changePage = (step) => {
  const newPage = pagination.currentPage + step;
  if (newPage >= 1 && newPage <= pagination.totalPages) {
    pagination.currentPage = newPage;
    handleSearch();
  }
};

const goToPage = (page) => {
  pagination.currentPage = page;
  handleSearch();
};

const onToggleStatus = async (nv) => {
  await toggleStaffStatus(nv, handleSearch);
};

// --- LOGIC MODAL ---

const openModalAdd = () => {
  selectedStaffId.value = null;
  isModalOpen.value = true;
};

const openModalEdit = (id) => {
  selectedStaffId.value = id;
  isModalOpen.value = true;
};

const closeModal = () => {
  isModalOpen.value = false;
};

const openModalView = (id) => {
  detailStaffId.value = id;
  isDetailModalOpen.value = true;
};

const closeDetailModal = () => {
  isDetailModalOpen.value = false;
};




const exportToExcel = async () => {
  try {
    console.log("Đang xuất file với bộ lọc:", filters);
    
    // SỬA TẠI ĐÂY: Gọi thông qua staffService
    const response = await staffService.exportStaffExcel(filters);

    // Xử lý dữ liệu nhị phân (Blob)
    const blob = new Blob([response.data], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    });
    
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    
    const fileName = `DS_NhanVien_${dayjs().format('DD_MM_YYYY')}.xlsx`;
    link.setAttribute('download', fileName);
    
    document.body.appendChild(link);
    link.click();

    link.remove();
    window.URL.revokeObjectURL(url);

  } catch (error) {
    console.error("Lỗi khi xuất file:", error);
    alert("Không thể xuất file. Vui lòng kiểm tra lại phía Server!");
  }
};
onMounted(handleSearch);
</script>
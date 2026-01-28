<template>
  <div class="d-flex">
    <sidebar />

    <div class="flex-grow-1 staff-manager-wrapper">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h2 class="title-page">Quản lý nhân viên</h2>
        <button class="btn-red-dark" @click="openModalAdd">
          <i class="fas fa-plus me-2"></i> Thêm nhân viên
        </button>
      </div>

      <div class="filter-card">
        <div class="row g-3 align-items-end">
          <div class="col-md-4">
            <label class="filter-label">Tìm kiếm</label>
            <input v-model="filters.keyword" class="form-control custom-input" placeholder="Tên, mã, email, SĐT..."
              @input="onSearchInput">
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
            <tr v-for="(nv, index) in listNhanVien" :key="nv.id"
              :class="{ 'row-staff-locked': nv.trangThaiLamViec === 2 }">

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
                  <i class="fas fa-eye detail-icon text-dark me-2" title="Xem chi tiết" style="cursor: pointer;"
                    @click="openModalView(nv.id)"></i>

                  <i class="fas fa-pen edit-icon me-2" :class="{ 'disabled-icon': nv.trangThaiLamViec === 2 }"
                    title="Sửa" @click="nv.trangThaiLamViec === 1 ? openModalEdit(nv.id) : null"></i>

                  <i v-if="nv.trangThaiLamViec === 1" class="fas fa-unlock-alt unlock-icon text-danger" title="Khóa"
                    @click="onToggleStatus(nv)"></i>
                  <i v-else class="fas fa-lock lock-icon text-success" title="Mở khóa" @click="onToggleStatus(nv)"></i>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="listNhanVien.length === 0" class="p-5 text-center text-muted">
          Không tìm thấy dữ liệu
        </div>
      </div>

      <CommonPagination
        v-model:currentPage="pagination.currentPage"
        v-model:pageSize="pagination.pageSize"
        :total-pages="pagination.totalPages"
        :total-elements="pagination.totalElements"
        :current-count="listNhanVien.length"
        @change="handleSearch"
      />
    </div>
    

    <StaffModal v-if="isModalOpen" :staff-id="selectedStaffId" @close="closeModal" @refresh="handleSearch" />

    <StaffDetailModal v-if="isDetailModalOpen" :staff-id="detailStaffId" @close="closeDetailModal" />
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import { useStaffLogic } from './staffFunction.js';
import StaffModal from '../modal/staffModal.vue';
import StaffDetailModal from '../modal/staffDetailModal.vue'; // Import file mới
import dayjs from 'dayjs';
import '../staffStyle.css';
import CommonPagination from '@/components/commonPagination.vue';

const getImg = (imgName) => {
  if (!imgName) return 'https://placehold.co/100x100?text=No+Img';
  
  // Nếu đã là link đầy đủ (ví dụ link ảnh mạng) thì giữ nguyên
  if (imgName.startsWith('http') || imgName.startsWith('data:image')) {
    return imgName;
  }

  return `http://localhost:8080/uploads/images/${imgName}`;
}

const { getStatusDisplay, fetchData, toggleStaffStatus } = useStaffLogic();

// State quản lý danh sách & filters
const listNhanVien = ref([]);
const filters = reactive({
  keyword: '',
  trangThai: null,
  tuNgay: ''
});
const pagination = reactive({ currentPage: 1, pageSize: 5, totalPages: 0 });

// State quản lý Modal Thêm/Sửa
const isModalOpen = ref(false);
const selectedStaffId = ref(null);

// State quản lý Modal Xem chi tiết
const isDetailModalOpen = ref(false);
const detailStaffId = ref(null);

// --- Logic xử lý ---

const formatDate = (date) => {
  if (!date) return '---';
  return dayjs(date).format('DD/MM/YYYY');
};

const handleSearch = async () => {
  try {
    const data = await fetchData(filters, pagination);
    listNhanVien.value = data.content || [];
    pagination.totalPages = data.totalPages || 0;
  } catch (error) {
    console.error("Lỗi khi load danh sách:", error);
  }
};

const onToggleStatus = async (nv) => {
  await toggleStaffStatus(nv, handleSearch);
};

const onSearchInput = () => {
  clearTimeout(window.debounceTimer);
  window.debounceTimer = setTimeout(() => {
    pagination.currentPage = 1;
    handleSearch();
  }, 500);
};

const changePage = (step) => {
  pagination.currentPage += step;
  handleSearch();
};

// --- Logic đóng mở Modal ---

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

// Mở Modal xem chi tiết
const openModalView = (id) => {
  detailStaffId.value = id;
  isDetailModalOpen.value = true;
};

const closeDetailModal = () => {
  isDetailModalOpen.value = false;
};

onMounted(handleSearch);
</script>
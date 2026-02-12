<template>
  <div class="flex-grow-1 staff-manager-wrapper" style="padding: 25px; background: #ffffff;min-height: 100vh;">
    <div class="mb-3">
      <h2 class="title-page-cozy">Quản lý nhân viên</h2>
    </div>

    <div class="filter-card-premium mb-4">
      <div class="filter-header-simple">
        <i class="fas fa-filter me-2"></i>Bộ lọc tìm kiếm
      </div>
      <div class="row g-3 p-3 align-items-end">
        <div class="col-md-5">
          <label class="filter-label">Từ khóa</label>
          <input v-model="filters.keyword" class="form-control custom-input" placeholder="Tên, mã, email..."
            @input="onSearchInput">
        </div>
        <div class="col-md-5">
          <label class="filter-label">Trạng thái</label>
          <select v-model="filters.trangThai" class="form-select custom-input" @change="handleSearch">
            <option :value="null">Tất cả</option>
            <option :value="1">Đang làm việc</option>
            <option :value="2">Ngừng hoạt động</option>
          </select>
        </div>
        <!-- <div class="col-md-3">
          <label class="filter-label">Từ ngày</label>
          <input type="date" v-model="filters.tuNgay" class="form-control custom-input" @change="handleSearch">
        </div> -->
        <div class="col-md-2">
          <button class="btn-red-dark w-100 py-2" @click="handleSearch">Tìm kiếm</button>
        </div>
      </div>
    </div>

    <div class="d-flex justify-content-end gap-2 mb-3">
      <button class="btn-action-icon btn-add-only" @click="openModalAdd" title="Thêm nhân viên">
        <i class="fas fa-plus"></i>
      </button>
      <button class="btn-action-icon btn-excel-only" @click="exportToExcel" title="Xuất Excel">
        <i class="fas fa-file-excel"></i>
      </button>
      <button class="btn-action-icon btn-refresh-only" @click="handleSearch(true)" title="Tải lại">
        <i class="fas fa-sync-alt"></i>
      </button>

    </div>

    <div class="table-container-premium">
      <table class="table mb-0 custom-table">
        <thead>
          <tr>
            <th class="text-center">STT</th>
            <th class="text-center">MÃ NV</th>
            <th class="text-center">HỌ TÊN</th>
            <th class="text-center">SĐT</th>
            <th class="text-center">EMAIL</th>
            <th class="text-center">NGÀY VÀO</th>
            <th class="text-center">TRẠNG THÁI</th>
            <th class="text-center">HÀNH ĐỘNG</th>
          </tr>
        </thead>
        <tbody>
          <!-- Có dữ liệu -->
          <tr v-if="listNhanVien.length > 0" v-for="(nv, index) in listNhanVien" :key="nv.id"
            :class="{ 'row-locked': nv.trangThaiLamViec === 2 }">

            <td class="text-center text-muted small">
              {{ (pagination.currentPage - 1) * pagination.pageSize + index + 1 }}
            </td>

            <td class="text-center fw-bold text-wine">{{ nv.maNhanVien }}</td>
            <td class="text-center text-dark">{{ nv.hoTenNhanVien }}</td>
            <td class="text-center">{{ nv.sdtNhanVien }}</td>
            <td class="text-center text-dark">{{ nv.email }}</td>
            <td class="text-center">{{ formatDate(nv.ngayVaoLam) }}</td>

            <td class="text-center">
              <span :class="['status-text', getStatusDisplay(nv.trangThaiLamViec).class]">
                {{ getStatusDisplay(nv.trangThaiLamViec).text }}
              </span>
            </td>

            <td class="text-center">
              <div class="d-flex justify-content-center gap-3 action-icons">
                <i class="fas fa-eye" @click="openModalView(nv.id)" title="Xem"></i>
                <i class="fas fa-pen" @click="openModalEdit(nv.id)" title="Sửa"></i>
                <i :class="['fas', nv.trangThaiLamViec === 1 ? 'fa-unlock' : 'fa-lock']" @click="onToggleStatus(nv)"
                  title="Khóa/Mở"></i>
              </div>
            </td>
          </tr>

          <!-- Không có dữ liệu -->
          <tr v-else>
            <td colspan="8" class="text-center text-muted py-4">
              Không có dữ liệu
            </td>
          </tr>
        </tbody>

      </table>
    </div>

    <CommonPagination v-model:currentPage="pagination.currentPage" v-model:pageSize="pagination.pageSize"
      :totalPages="pagination.totalPages" :totalElements="pagination.totalElements" :currentCount="listNhanVien.length"
      @change="handleSearch" />

  </div>
</template>

<script setup>

import { ref, onMounted, reactive, watch, nextTick } from 'vue'; // Đã thêm watch
import { useStaffLogic } from '../screens/staffFunction.js';

import dayjs from 'dayjs';
import { useRouter } from 'vue-router';
import '../staffStyle.css';
import Swal from 'sweetalert2';
import staffService from '@/services/staffService.js';
import CommonPagination from '@/components/commonPagination.vue';
const { getStatusDisplay, fetchData, toggleStaffStatus } = useStaffLogic();
const router = useRouter();
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
const handleSearch = async (showToast = false) => {
  try {
    const data = await fetchData(filters, pagination);

    listNhanVien.value = data.content || [];
    pagination.totalPages = data.totalPages || 0;
    pagination.totalElements = data.totalElements || 0;

    if (showToast) {
      Swal.fire({
        icon: 'success',
        title: 'Đã tải lại dữ liệu',
        timer: 1500,
        showConfirmButton: false
      });
    }

  } catch (error) {
    console.error("Lỗi khi load danh sách:", error);
    listNhanVien.value = [];

    Swal.fire('Lỗi!', 'Không thể tải dữ liệu.', 'error');
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
  router.push("/admin/staff/form");
};

const openModalEdit = (id) => {
  router.push(`/admin/staff/form/${id}`);
};


const openModalView = (id) => {
  // detailStaffId.value = id;
  // isDetailModalOpen.value = true;
  router.push(`/admin/staff/view/${id}`);
};

const closeDetailModal = () => {
  isDetailModalOpen.value = false;
};

const exportToExcel = async () => {
  // 1. Hỏi xác nhận
  const result = await Swal.fire({
    title: 'Xác nhận xuất file?',
    text: "Hệ thống sẽ trích xuất danh sách nhân viên ra file Excel.",
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#28a745',
    cancelButtonColor: '#6c757d',
    confirmButtonText: 'Đồng ý xuất',
    cancelButtonText: 'Hủy'
  });

  if (result.isConfirmed) {
    try {
      // Hiện loading (tùy chọn nhưng nên có vì xuất file có thể lâu)
      Swal.showLoading();

      const response = await staffService.exportStaffExcel(filters);

      const blob = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `DS_NhanVien_${dayjs().format('DD_MM_YYYY')}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      // 2. Thông báo thành công
      Swal.fire({
        title: 'Thành công!',
        text: 'File của bạn đã được tải xuống.',
        icon: 'success',
        timer: 2000,
        showConfirmButton: false
      });
    } catch (error) {
      console.error("Lỗi:", error);
      // 3. Thông báo thất bại
      Swal.fire('Lỗi!', 'Không thể xuất file. Vui lòng thử lại sau.', 'error');
    }
  }
};
onMounted(handleSearch);
</script>
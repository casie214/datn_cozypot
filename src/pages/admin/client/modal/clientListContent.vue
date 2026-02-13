<template>
  <div class="flex-grow-1 staff-manager-wrapper" style="padding: 25px; background: #ffffff; min-height: 100vh;">
    <div class="mb-3">
      <h2 class="title-page-cozy" style="    color: var(--primary-red);
    font-weight: 700;
    font-size: 24px;">Quản lý khách hàng</h2>
    </div>

    <div class="filter-card mb-4">
      <div class="filter-header-simple">
        <i class="fas fa-filter me-2"></i>Bộ lọc tìm kiếm
      </div>
      <div class="row g-3 p-3 align-items-end">
        <div class="col-md-5">
          <label class="filter-label">Từ khóa</label>
          <input v-model="filters.keyword" class="form-control custom-input" placeholder="Tên, mã, SĐT, email..."
            @input="onSearchInput">
        </div>
        <div class="col-md-5">
          <label class="filter-label">Trạng thái</label>
          <select v-model="filters.trangThai" class="form-select custom-input" @change="handleSearch">
            <option :value="null">Tất cả</option>
            <option :value="1">Đang hoạt động</option>
            <option :value="0">Ngừng hoạt động</option>
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
      <button class="btn-action-icon btn-add-only" @click="openModalAdd" title="Thêm khách hàng">
        <i class="fas fa-plus"></i>
      </button>

      <button class="btn-action-icon btn-excel-only" @click="handleExportExcel" title="Xuất Excel">
        <i class="fas fa-file-excel"></i>
      </button>

      <button class="btn-action-icon btn-refresh-only" @click="handleRefresh" title="Tải lại">
        <i class="fas fa-sync-alt"></i>
      </button>
    </div>

    <div class="table-container-premium">
      <table class="table mb-0 custom-table">
        <thead>
          <tr>
            <th class="text-center">STT</th>
            <th class="text-center">MÃ KH</th>
            <th class="text-center">HỌ TÊN</th>
            <th class="text-center">SĐT</th>
            <th class="text-center">EMAIL</th>
            <th class="text-center">GIỚI TÍNH</th>
            <th class="text-center">TRẠNG THÁI</th>
            <th class="text-center">HÀNH ĐỘNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="listKhachHang.length > 0" v-for="(kh, index) in listKhachHang" :key="kh.id"
            :class="{ 'row-locked': kh.trangThai === 0 }">
            <td class="text-center text-muted small">
              {{ (pagination.currentPage - 1) * pagination.pageSize + index + 1 }}
            </td>
            <td class="text-center fw-bold text-wine">{{ kh.maKhachHang }}</td>
            <td class="text-center text-dark">{{ kh.tenKhachHang }}</td>
            <td class="text-center">{{ kh.soDienThoai }}</td>
            <td class="text-center text-dark">{{ kh.email }}</td>
            <td class="text-center">{{ kh.gioiTinh ? 'Nam' : 'Nữ' }}</td>
            <td class="text-center">
              <span :class="['status-text', getStatusDisplay(kh.trangThai).class]">
                {{ getStatusDisplay(kh.trangThai).text }}
              </span>
            </td>
            <td class="text-center">
              <div class="d-flex justify-content-center gap-3 action-icons">
                <i class="fas fa-eye text-secondary" @click="openModalView(kh.id)" title="Xem"></i>

                <i class="fas fa-pen" :class="kh.trangThai === 1 ? 'text-secondary' : 'disabled-icon'"
                  @click="kh.trangThai === 1 ? openModalEdit(kh.id) : warnLocked()" title="Sửa"></i>

                <i :class="['fas', kh.trangThai === 1 ? 'fa-unlock-alt text-secondary' : 'fa-lock text-danger']"
                  @click="handleToggleStatus(kh)"
                  :title="kh.trangThai === 1 ? 'Khóa khách hàng' : 'Mở khóa khách hàng'"></i>
              </div>
            </td>
          </tr>
          <tr v-else>
            <td colspan="8" class="text-center text-muted py-4">Không có dữ liệu khách hàng</td>
          </tr>
        </tbody>
      </table>
    </div>

    <CommonPagination v-model:currentPage="pagination.currentPage" v-model:pageSize="pagination.pageSize"
      :totalPages="pagination.totalPages" :totalElements="pagination.totalElements" :currentCount="listKhachHang.length"
      @change="handleSearch" />
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue';
import { useClientLogic } from '../screens/clientFunction.js'; // Sửa lại đường dẫn khớp ảnh
import CommonPagination from '@/components/commonPagination.vue';
import clientService from '@/services/clientService';
import Swal from 'sweetalert2';
import { useRouter } from 'vue-router';

const { getStatusDisplay, fetchData } = useClientLogic();
const router = useRouter();
const listKhachHang = ref([]);
const filters = reactive({ keyword: '', trangThai: null, tuNgay: '' });
const pagination = reactive({ currentPage: 1, pageSize: 8, totalPages: 0, totalElements: 0 });

const handleSearch = async () => {
  const data = await fetchData(filters, pagination);
  listKhachHang.value = data.content || [];
  pagination.totalPages = data.totalPages || 0;
  pagination.totalElements = data.totalElements || 0;
};

const onSearchInput = () => {
  clearTimeout(window.debounceTimer);
  window.debounceTimer = setTimeout(() => {
    pagination.currentPage = 1;
    handleSearch();
  }, 500);
};



import dayjs from 'dayjs'; // Đảm bảo đã cài đặt và import dayjs

// --- 1. Hàm Xuất Excel ---
const handleExportExcel = async () => {
  const result = await Swal.fire({
    title: 'Xác nhận xuất file?',
    text: "Hệ thống sẽ trích xuất danh sách khách hàng ra file Excel.",
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#28a745',
    cancelButtonColor: '#6c757d',
    confirmButtonText: 'Đồng ý xuất',
    cancelButtonText: 'Hủy'
  });

  if (result.isConfirmed) {
    try {
      Swal.showLoading();

      // Gọi API xuất excel khách hàng
const response = await clientService.exportExcel({
  keyword: null,
  trangThai: null,
  tuNgay: null
});
      const rawData = response.data ? response.data : response;

      const blob = new Blob([rawData], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });

      // Kiểm tra file có rỗng không (Dưới 1KB là nghi vấn)
      if (blob.size < 500) {
        console.warn("Dữ liệu nhận được quá nhỏ, có thể file rỗng.");
      }

      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `DS_KhachHang_${dayjs().format('DD_MM_YYYY')}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      Swal.fire({
        title: 'Thành công!',
        text: 'File khách hàng đã được tải xuống.',
        icon: 'success',
        timer: 2000,
        showConfirmButton: false
      });
    } catch (error) {
      console.error("Lỗi xuất file khách hàng:", error);
      Swal.fire('Lỗi!', 'Không thể xuất file khách hàng.', 'error');
    }
  }
};

// --- 2. Hàm Tải lại (Refresh) ---
const handleRefresh = async () => {
  try {
    // Gọi lại hàm tìm kiếm dữ liệu
    await handleSearch();

    // Hiện thông báo "Đã tải lại dữ liệu" màu xanh lá giống ảnh bạn gửi
    Swal.fire({
      title: 'Đã tải lại dữ liệu',
      icon: 'success',
      iconColor: '#a5dc86', // Màu xanh lá nhạt của dấu tích
      showConfirmButton: false,
      timer: 1500,
      toast: false, // Để hiện giữa màn hình giống ảnh
    });
  } catch (error) {
    console.error("Lỗi tải lại:", error);
  }
};

// Tìm và XÓA phiên bản handleToggleStatus cũ đi
// CHỈ GIỮ LẠI đoạn này:

const handleToggleStatus = async (kh) => {
  const isLocking = kh.trangThai === 1;

  const result = await Swal.fire({
    title: isLocking ? 'Xác nhận khóa?' : 'Xác nhận mở khóa?',
    text: `Bạn có chắc chắn muốn ${isLocking ? 'khóa' : 'mở khóa'} khách hàng này không?`,
    icon: isLocking ? 'warning' : 'question',
    showCancelButton: true,
    confirmButtonColor: isLocking ? '#d33' : '#28a745',
    cancelButtonColor: '#6c757d',
    confirmButtonText: 'Đồng ý',
    cancelButtonText: 'Hủy',
    reverseButtons: true
  });

  if (result.isConfirmed) {
    try {
      Swal.fire({
        title: 'Đang xử lý...',
        allowOutsideClick: false,
        didOpen: () => { Swal.showLoading(); }
      });

      await clientService.toggleStatus(kh.id);

      await Swal.fire({
        title: 'Thành công!',
        text: `Đã ${isLocking ? 'khóa' : 'mở khóa'} khách hàng thành công.`,
        icon: 'success',
        timer: 1500,
        showConfirmButton: false
      });

      handleSearch(); // Tải lại danh sách
    } catch (error) {
      console.error("Lỗi:", error);
      Swal.fire('Lỗi!', 'Không thể thực hiện thao tác này.', 'error');
    }
  }
};

const openModalAdd = () => {
  // Chuyển hướng sang trang thêm mới khách hàng
  router.push("/admin/client/form");
};

const openModalEdit = (id) => {
  // Chuyển hướng sang trang sửa với ID tương ứng
  router.push(`/admin/client/form/${id}`);
};

const openModalView = (id) => {
  // Chuyển hướng sang trang xem chi tiết
  router.push(`/admin/client/view/${id}`);
};
const warnLocked = () => Swal.fire({ icon: 'info', title: 'Thông báo', text: 'Vui lòng mở khóa để chỉnh sửa thông tin này!' });

onMounted(handleSearch);
</script>

<style scoped>
.btn-add-only {
  background-color: var(--primary-red) !important;
  color: white !important;
  border: none !important;
}

.filter-card {
  background: var(--white);
  border-radius: 15px;
  padding: 25px;
  margin-bottom: 30px;
  /* Giữ viền xám nhẹ của bạn */
  border: 1px solid #d0cece; 
 
box-shadow: 0 3px 6px rgba(131, 131, 131, 0.2);
}

.filter-label {
  font-size: 13px;
  color: #888;
  margin-bottom: 5px;
  display: block;
}

.custom-input {
  border-radius: 8px;
  border: 1px solid #ddd;
  padding: 8px 12px;
  color: gray;
}
/* --- TÙY CHỈNH NÚT THÊM MỚI (Btn Red Dark) --- */
.btn-red-dark {
    color: var(--white) !important;
    border-radius: 8px;
    padding: 10px 24px;
    border: 1px solid var(--dark-red);
    font-size: 14px;
    font-weight: 600;
    transition: all 0.3s ease;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 6px rgba(123, 18, 28, 0.2);
    background-color: #8b0000;
}

.btn-red-dark:hover {
    background-color: #8b0000;
    border-color: #8b0000;
    transform: translateY(-2px);
    box-shadow: 0 6px 12px rgba(123, 18, 28, 0.3);
    color: white !important;
}

.btn-red-dark:active {
    transform: translateY(0);
}

/* --- TÙY CHỈNH NÚT BỎ LỌC (Btn Reset/Refresh) --- */
/* Class này áp dụng cho nút có icon xoay trong bộ lọc */
.btn-reset-filter {
    background-color: var(--white);
    color: #6c757d; /* Màu xám trung tính */
    border: 1px solid #d0cece;
    border-radius: 8px;
    padding: 10px;
    transition: all 0.3s ease;
    width: 100%; /* Đảm bảo khớp với chiều cao input */
    display: flex;
    align-items: center;
    justify-content: center;
}

.btn-reset-filter:hover {
    background-color: #f8f9fa;
    color: #8b0000;
    border-color: #8b0000;
    box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.btn-reset-filter i {
    font-size: 16px;
}

/* Hiệu ứng xoay icon khi hover vào nút bỏ lọc */
.btn-reset-filter:hover i {
    transform: rotate(180deg);
    transition: transform 0.5s ease;
}

.btn-red-dark:hover {
  background-color: #8b0000;
  
font-weight: bold;
}

/* Nút bấm */
.btn-red-btn-outline-secondary {
  background-color: var(--dark-red);
  color: white !important;
  border-radius: 8px;
  padding: 8px 20px;
  border: none;
  font-size: 14px;
}
/* Tùy chỉnh màu sắc cho Toggle Switch */
.custom-switch {
    width: 40px !important;
    height: 20px !important;
    cursor: pointer;
    border-color: #ccc;
    background-color: #ccc;
}

/* Khi công tắc ở trạng thái ON (Đang hoạt động) */
.custom-switch:checked {
    background-color: #8b0000!important; /* Màu đỏ đô của bạn */
    border-color: #8b0000 !important;
}

/* Loại bỏ viền xanh mặc định của Bootstrap khi click */
.custom-switch:focus {
    box-shadow: none;
    border-color: #ccc;
}

.custom-switch:checked:focus {
    border-color: #8b0000;
    box-shadow: 0 0 0 0.25rem rgba(123, 18, 28, 0.25);
}

.status-badge {
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.status-badge.active {
  background-color: white;
  color: black;
  border: 1px solid lightgray;
}

.status-badge.inactive {
  background-color: whitesmoke;
  color: black;
  border: 1px solid lightgray;
}

.btn-add-only {
    background-color: #8B0000 !important;
    color: white !important;
    border: none !important;
}


.btn-action-icon {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    border: 1px solid #eee !important;
    color: var(--primary-red);
    transition: all 0.2s ease;
}
</style>
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
        <div class="col-md-2">
          <button class="btn-red-dark w-100 py-2" style="color: white !important;" @click="handleSearch">Tìm kiếm</button>
        </div>
      </div>
    </div>

    <div class="shadow-sm mt-4"
      style="background: #ffffff; border-radius: 12px; border: 1px solid #eef2f6; overflow: hidden;">

      <div
        style="padding: 16px 24px; border-bottom: 1px solid #f1f1f1; background-color: #ffffff; display: flex; justify-content: space-between; align-items: center;">
        <h4 style="font-size: 1.25rem; font-weight: 700; color: #800000; margin: 0; font-family: sans-serif;">
          Danh sách khách hàng
        </h4>

        <div style="display: flex; gap: 8px;">
          <button @click="openModalAdd" title="Thêm khách hàng"
            style="width: 38px; height: 38px; background: #ffffff; border: 1.5px solid #800000; border-radius: 8px; color: #800000; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: 0.3s;">
            <i class="fas fa-plus"></i>
          </button>

          <button @click="handleExportExcel" title="Xuất Excel"
            style="width: 38px; height: 38px; background: #ffffff; border: 1.5px solid #800000; border-radius: 8px; color: #800000; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: 0.3s;">
            <i class="fas fa-file-excel"></i>
          </button>

          <button @click="handleRefresh" title="Làm mới dữ liệu"
            style="width: 38px; height: 38px; background: #ffffff; border: 1.5px solid #800000; border-radius: 8px; color: #800000; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: 0.3s;">
            <i class="fas fa-sync-alt"></i>
          </button>
        </div>
      </div>

      <div class="table-responsive p-3">
        <table class="table mb-0 custom-table table-hover align-middle">
          <thead>
            <tr style="background-color: #800000;">
              <th class="text-center" style="width: 40px; border: none;">
                <input type="checkbox" class="form-check-input" @change="toggleSelectAll"
                  :checked="listKhachHang?.length > 0 && selectedIds.length === listKhachHang.length">
              </th>
              <th class="text-center" style="color: white; border: none;">STT</th>
              <th style="color: white; border: none;">Khách hàng</th>
              <th class="text-center" style="color: white; border: none;">Giới tính</th>
              <th class="text-center" style="color: white; border: none;">Ngày sinh</th>
              <th style="color: white; border: none;">Liên hệ</th>
              <th style="color: white; border: none;">Địa chỉ nhận hàng</th>
              <th class="text-center" style="color: white; border: none;">Trạng thái</th>
              <th class="text-center" style="color: white; border: none;">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(kh, index) in listKhachHang" :key="kh.id">
              <td class="text-center">
                <input type="checkbox" class="form-check-input custom-checkbox" :value="kh.id" v-model="selectedIds">
              </td>

              <td class="text-center text-muted font-monospace">
                {{ (pagination.currentPage - 1) * pagination.pageSize + index + 1 }}
              </td>

              <td>
                <div class="d-flex align-items-center">
                  <!-- <div class="avatar-placeholder me-2"
          style="width: 32px; height: 32px; background: #f8f9fa; border: 1px solid #eee; border-radius: 50%; display: flex; align-items: center; justify-content: center;">
          <i class="fas fa-user text-muted" style="font-size: 13px;"></i>
        </div> -->
                  <div>
                    <div class="fw-bold text-dark mb-0">{{ kh.tenKhachHang }}</div>
                    <!-- <small class="text-muted" style="font-size: 11px;">{{ kh.maKhachHang }}</small> -->
                  </div>
                </div>
              </td>

              <td class="text-center">{{ kh.gioiTinh ? 'Nam' : 'Nữ' }}</td>
              <td class="text-center text-muted small">{{ formatBirthDate(kh.ngaySinh) }}</td>

              <td>
                <div class="contact-box">
                  <div class="small mb-1">
                    <i class="far fa-envelope me-1" style="color: #800000;"></i>{{ kh.email }}
                  </div>
                  <div class="text-danger small">
                    <i class="fas fa-phone-alt me-1"></i>{{ kh.soDienThoai }}
                  </div>
                </div>
              </td>

              <td>
                <div class="text-muted small text-wrap" style="max-width: 220px; line-height: 1.4;">
                  {{ kh.diaChi }}
                </div>
              </td>

              <td class="text-center">
                <span :class="['badge-status', kh.trangThai === 1 ? 'status-active' : 'status-locked']">
                  {{ kh.trangThai === 1 ? 'Đang hoạt động' : 'Ngừng hoạt động' }}
                </span>
              </td>

              <td class="text-center" style="vertical-align: middle;">
                <div class="d-flex justify-content-center align-items-center gap-3">
                  <button class="btn btn-link p-0 action-edit-v2" :class="{ 'is-locked': kh.trangThai !== 1 }"
                    @click="handleEdit(kh)" :title="kh.trangThai !== 1 ? 'Tài khoản bị khóa' : 'Chỉnh sửa'"
                    style="text-decoration: none;">
                    <i class="fas fa-pen" :style="{ color: kh.trangThai !== 1 ? '#ccc' : '#444' }"></i>
                  </button>

                  <div class="form-check form-switch" style="margin-bottom: 0; min-height: auto;">
                    <input class="form-check-input cz-switch shadow-none" type="checkbox" role="switch"
                      style="cursor: pointer; width: 2.5em; height: 1.25em;" :checked="kh.trangThai === 1"
                      @click.prevent="onToggleStatus(kh)">
                  </div>
                </div>
              </td>
            </tr>

            <tr v-if="listKhachHang.length === 0">
              <td colspan="9" class="text-center py-5 text-muted">
                <i class="fas fa-box-open d-block mb-2" style="font-size: 2rem; opacity: 0.3;"></i>
                Không tìm thấy khách hàng nào trong danh sách.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
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
// 1. Thêm biến selectedIds để quản lý việc chọn nhiều dòng
const selectedIds = ref([]);

// 2. Thêm hàm định dạng ngày sinh (vì trong table bạn gọi formatBirthDate)
const formatBirthDate = (date) => {
  if (!date) return '---';
  return dayjs(date).format('DD/MM/YYYY');
};

// 3. Thêm hàm toggleSelectAll (chọn tất cả checkbox)
// Hàm xử lý Chọn tất cả / Bỏ chọn tất cả
const toggleSelectAll = (event) => {
  if (event.target.checked) {
    // Lấy tất cả ID của trang hiện tại thêm vào mảng selectedIds
    selectedIds.value = listKhachHang.value.map(kh => kh.id);
  } else {
    // Xóa sạch mảng
    selectedIds.value = [];
  }
};

// 4. Đồng bộ tên hàm onToggleStatus với handleToggleStatus
// Trong HTML bạn để @click.prevent="onToggleStatus(kh)" 
// nhưng trong Script bạn đặt là handleToggleStatus.
// Hãy đổi tên trong script hoặc template cho khớp nhau.
const onToggleStatus = (kh) => handleToggleStatus(kh);

// 5. Thêm hàm handleEdit
const handleEdit = (kh) => {
  // Kiểm tra trạng thái bằng kh.trangThai (camelCase) thay vì kh.trang_thai
  if (kh.trangThai !== 1) {
    warnLocked();
  } else {
    // Truyền kh.id thay vì kh.id_khach_hang
    openModalEdit(kh.id);
  }
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
    text: selectedIds.value.length > 0
      ? `Hệ thống sẽ xuất ${selectedIds.value.length} khách hàng đã chọn.`
      : "Hệ thống sẽ trích xuất danh sách khách hàng theo bộ lọc hiện tại.",
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#28a745',
    cancelButtonColor: '#6c757d',
    confirmButtonText: 'Đồng ý xuất',
    cancelButtonText: 'Hủy'
  });

  if (result.isConfirmed) {
    try {
      // Đảm bảo filters có giá trị, nếu không thì dùng object rỗng
      // Bạn hãy kiểm tra xem trong code của bạn biến filter tên là gì nhé (ví dụ: filterParams)
      const currentFilters = filters?.value || {}; 

      const params = {
        keyword: currentFilters.keyword || null,
        trangThai: currentFilters.trangThai !== undefined ? currentFilters.trangThai : null,
        tuNgay: currentFilters.tuNgay || null
      };

      const ids = selectedIds.value || [];

      // Gọi service
      const response = await clientService.exportExcel(params, ids);

      // axiosClient thường trả về data trực tiếp, nếu không dùng response.data
      const rawData = response.data ? response.data : response;

      const blob = new Blob([rawData], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });

      if (blob.size < 500) {
        throw new Error("Dữ liệu nhận được không hợp lệ.");
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

      // (Tùy chọn) Bỏ chọn checkbox sau khi xuất thành công
      // selectedIds.value = [];

    } catch (error) {
      console.error("Lỗi xuất file khách hàng:", error);
      Swal.fire('Lỗi!', 'Không thể xuất file. Vui lòng thử lại sau.', 'error');
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
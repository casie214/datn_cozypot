<template>
  <div class="flex-grow-1 staff-manager-wrapper" style="padding: 25px; background: #ffffff; min-height: 100vh;">
    <div class="mb-3">
      <h2 class="title-page-cozy" style="    color: var(--primary-red);
    font-weight: 700;
    font-size: 24px;">Quản lý khách hàng</h2>
    </div>

    <div class="filter-card-client mb-4">
      <div class="filter-header-simple">
        <i class="fas fa-filter me-2"></i>Bộ lọc tìm kiếm
      </div>
      <div class="row g-3 p-3 align-items-end">
        <div class="col-md-4">
          <label class="filter-label">Từ khóa</label>
          <input v-model="filters.keyword" class="form-control custom-input" placeholder="Tên, mã, SĐT, email..."
            @input="onSearchInput">
        </div>

        <div class="col-md-3">
          <label class="filter-label">Trạng thái</label>
          <select v-model="filters.trangThai" class="form-select custom-input" @change="handleSearch">
            <option :value="null">Tất cả</option>
            <option :value="1">Đang hoạt động</option>
            <option :value="0">Ngừng hoạt động</option>
          </select>
        </div>

        <div class="col-md-3">
          <div class="d-flex flex-column h-100">
            <label class="filter-label ">Giới tính</label>

            <div class="gender-filter-container">
              <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" v-model="filters.gioiTinh" :value="null" id="genderAll"
                  name="gioiTinh" @change="handleSearch">
                <label class="form-check-label small" for="genderAll">Tất cả</label>
              </div>

              <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" v-model="filters.gioiTinh" :value="true" id="genderMale"
                  name="gioiTinh" @change="handleSearch">
                <label class="form-check-label small" for="genderMale">Nam</label>
              </div>

              <div class="form-check form-check-inline">
                <input class="form-check-input" type="radio" v-model="filters.gioiTinh" :value="false" id="genderFemale"
                  name="gioiTinh" @change="handleSearch">
                <label class="form-check-label small" for="genderFemale">Nữ</label>
              </div>
            </div>
          </div>
        </div>

        <div class="col-md-2">
          <button class="btn-red-dark w-100 py-2" style="color: white !important;" @click="handleReset">Xóa bộ
            lọc</button>
        </div>
      </div>
    </div>

    <div class="shadow-sm mt-4"
      style="background: #ffffff; border-radius: 12px; border: 1px solid #eef2f6; overflow: hidden;">

      <div
        style="padding: 16px 24px; border-bottom: 1px solid #f1f1f1; background-color: #ffffff; display: flex; justify-content: space-between; align-items: center;">
        <h4 style="font-size: 1.25rem; font-weight: 700; color: #800000; margin: 0; font-family: sans-serif;">
          <!-- Danh sách khách hàng -->
        </h4>

        <div style="display: flex; gap: 8px;">
          <button @click="openModalAdd" title="Thêm khách hàng"
            style="width: 38px; height: 38px; background: #ffffff; border: 1.5px solid #800000; border-radius: 8px; color: #800000; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: 0.3s;">
            <i class="fas fa-plus"></i>
          </button>

          <button @click="handleActionWithAuth(handleExportExcel, 'ADMIN')" title="Xuất Excel"
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
              <th class="text-center">
                <input type="checkbox" class="form-check-input" @change="toggleSelectAll" :checked="isSelectAllPages">
              </th>
              <th class="text-center" style="color: white; border: none;">STT</th>
              <th style="color: white; border: none;">Khách hàng</th>
              <th class="text-center" style="color: white; border: none;">Giới tính</th>
              <th class="text-center" style="color: white; border: none;">Ngày sinh</th>
              <th style="color: white; border: none;">Liên hệ</th>
              <th style="color: white; border: none;">Địa chỉ</th>
              <th class="text-center" style="color: white; border: none;">Trạng thái</th>
              <th class="text-center" style="color: white; border: none;">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(kh, index) in listKhachHang" :key="kh.id">
              <td class="text-center">
                <input type="checkbox" class="form-check-input" :value="kh.id" v-model="selectedIds"
                  :checked="isSelectAllPages || selectedIds.includes(kh.id)">
              </td>
              <td class="text-center text-muted font-monospace">
                {{ (pagination.currentPage - 1) * pagination.pageSize + index + 1 }}
              </td>
              <td>
                <div class="d-flex align-items-center" style="justify-content: center;">

                  <div>
                    <div style="text-align: center !important;" class="fw-bold text-dark mb-0">
                      <span>{{ kh.tenKhachHang }}</span>
                    </div>
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
                  <button class="btn btn-link p-0 action-edit-v2 shadow-none"
                    :class="{ 'is-locked': kh.trangThai !== 1 }" @click="handleEdit(kh)"
                    :title="kh.trangThai !== 1 ? 'Tài khoản bị khóa' : 'Chỉnh sửa'"
                    style="text-decoration: none; outline: none; border: none; background: none;">
                    <i class="fas fa-pen"
                      :style="{ color: kh.trangThai !== 1 ? '#ccc' : '#444', transition: '0.3s' }"></i>
                  </button>

                  <div class="form-check form-switch" style="margin-bottom: 0; min-height: auto;">
                    <input class="form-check-input cz-switch shadow-none" type="checkbox" role="switch"
                      style="cursor: pointer; width: 2.5em; height: 1.25em;" :checked="kh.trangThai === 1"
                      @click.prevent="handleActionWithAuth(() => handleToggleStatus(kh), 'ADMIN')">
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
      @change="(page) => handleSearch(page)" />


  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue';
import { useClientLogic } from '../screens/clientFunction.js'; // Sửa lại đường dẫn khớp ảnh
import CommonPagination from '@/components/commonPagination.vue';
import clientService from '@/services/clientService';
import Swal from 'sweetalert2';
import { useRouter } from 'vue-router';
import '../clientStyle.css';
import { useAuthStore } from "@/pages/guest/authentication/authenticationServices/authenticationService";
const authStore = useAuthStore();
const handleActionWithAuth = (callbackAction, requiredRole) => {
  const userRole = authStore.role;

  // Nếu role của người dùng không khớp với role yêu cầu (ADMIN)
  if (userRole !== requiredRole) {
    Swal.fire({
      icon: 'error',
      title: 'Không có quyền thực hiện!',
      text: 'Bạn không có quyền hạn thao tác chức năng này. Vui lòng liên hệ Quản trị viên.',
      confirmButtonText: 'Đã hiểu',
      confirmButtonColor: '#7d161a',
      timer: 3000,
      timerProgressBar: true
    });
    return; // Dừng lại, không chạy hàm callbackAction
  }

  // Nếu đúng quyền thì thực hiện hành động chính
  callbackAction();
};
const { getStatusDisplay, fetchData } = useClientLogic();
const router = useRouter();
const listKhachHang = ref([]);
const filters = reactive({
  keyword: '',
  trangThai: null,
  gioiTinh: null, // Thêm dòng này để mặc định là "Tất cả"
  tuNgay: ''
});
const pagination = reactive({ currentPage: 1, pageSize: 8, totalPages: 0, totalElements: 0 });

// Sửa lại hàm handleSearch để nhận tham số page (mặc định là null)
const handleSearch = async (page = null) => {
  console.log("Hàm handleSearch đã chạy!");

  // Nếu có truyền page từ pagination thì cập nhật
  if (pagination.currentPage > pagination.totalPages && pagination.totalPages > 0) {
    pagination.currentPage = 1;
  }
  try {
    const data = await fetchData(filters, pagination);

    listKhachHang.value = data.content || [];
    pagination.totalPages = data.totalPages || 0;
    pagination.totalElements = data.totalElements || 0;

  } catch (error) {
    console.error("Lỗi khi fetch dữ liệu:", error);
  }
};
// 1. Thêm biến selectedIds để quản lý việc chọn nhiều dòng
const selectedIds = ref([]);
const handleReset = async () => {
  // 1. Reset các ô nhập liệu trên giao diện (v-model)
  // Vì filters là reactive, ta gán trực tiếp các thuộc tính
  filters.keyword = '';
  filters.trangThai = null;
  filters.gioiTinh = null;
  filters.tuNgay = '';

  // 2. Tham số mặc định để gọi lại API
  const defaultFilters = {
    keyword: '',
    trangThai: null,
    gioiTinh: null,
    tuNgay: ''
  };

  const defaultPagination = {
    currentPage: 1,
    pageSize: 10
  };

  try {
    // 3. Gọi hàm fetchData (Đảm bảo hàm này đã được import hoặc định nghĩa)
    const data = await fetchData(defaultFilters, defaultPagination);

    // 4. Cập nhật lại danh sách khách hàng
    // Nhớ dùng .value vì listKhachHang là ref
    listKhachHang.value = data.content;

    console.log("Đã reset bộ lọc khách hàng thành công!");
  } catch (error) {
    console.error("Lỗi khi reset khách hàng:", error);
  }
};
// 2. Thêm hàm định dạng ngày sinh (vì trong table bạn gọi formatBirthDate)
const formatBirthDate = (date) => {
  if (!date) return '---';
  return dayjs(date).format('DD/MM/YYYY');
};

// 3. Thêm hàm toggleSelectAll (chọn tất cả checkbox)
// Hàm xử lý Chọn tất cả / Bỏ chọn tất cả
const isAllSelectedGlobal = ref(false); // Trạng thái để biết có đang chọn "Tất cả mọi trang" không

const isSelectAllPages = ref(false); // Biến quan trọng nhất

const toggleSelectAll = (event) => {
  const checked = event.target.checked;
  isSelectAllPages.value = checked; // Đánh dấu là chọn tất cả các trang

  if (checked) {
    // Chỉ đổ ID trang hiện tại vào để hiển thị ngay lập tức
    selectedIds.value = listKhachHang.value.map(kh => kh.id);
  } else {
    selectedIds.value = [];
  }
};

// Khi người dùng tích lẻ tẻ vào 1 dòng
const toggleSelectItem = (id) => {
  // Nếu đang ở chế độ chọn tất cả mà lại bỏ tích 1 dòng lẻ
  // thì phải tắt chế độ chọn tất cả các trang đi
  if (isSelectAllPages.value) {
    isSelectAllPages.value = false;
  }
}

// 4. Đồng bộ tên hàm onToggleStatus với handleToggleStatus
// Trong HTML bạn để @click.prevent="onToggleStatus(kh)" 
// nhưng trong Script bạn đặt là handleToggleStatus.
// Hãy đổi tên trong script hoặc template cho khớp nhau.

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
  // ✅ KIỂM TRA: Nếu không chọn tất cả hệ thống VÀ cũng không chọn lẻ dòng nào
  if (!isSelectAllPages.value && selectedIds.value.length === 0) {
    Swal.fire({
      title: 'Thông báo',
      text: 'Vui lòng chọn ít nhất một khách hàng để xuất file!',
      icon: 'warning',
      confirmButtonText: 'Đồng ý'
    });
    return;
  }

  // Thông báo hiển thị số lượng tương ứng
  const textConfirm = isSelectAllPages.value
    ? "Hệ thống sẽ xuất TOÀN BỘ danh sách khách hàng dựa theo bộ lọc hiện tại."
    : `Hệ thống sẽ xuất ${selectedIds.value.length} khách hàng đã chọn.`;

  const result = await Swal.fire({
    title: 'Xác nhận xuất file?',
    text: textConfirm,
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#28a745',
    cancelButtonColor: '#6c757d',
    confirmButtonText: 'Đồng ý xuất',
    cancelButtonText: 'Hủy'
  });

  if (result.isConfirmed) {
    try {
      Swal.fire({
        title: 'Đang xử lý...',
        allowOutsideClick: false,
        didOpen: () => { Swal.showLoading(); }
      });

      const params = {
        keyword: filters.keyword || null,
        trangThai: filters.trangThai !== null ? filters.trangThai : null,
        gioiTinh: filters.gioiTinh,
      };

      // ✅ LOGIC QUAN TRỌNG: 
      // Nếu chọn tất cả hệ thống -> gửi mảng rỗng để Backend tự lấy hết theo params
      // Nếu không -> gửi danh sách ID đã tích
      const ids = isSelectAllPages.value ? [] : selectedIds.value;

      const response = await clientService.exportExcel(params, ids);
      const rawData = response.data ? response.data : response;

      const blob = new Blob([rawData], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });

      if (blob.size < 500) throw new Error("Dữ liệu không hợp lệ.");

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
        text: 'File đã được tải xuống.',
        icon: 'success',
        timer: 2000,
        showConfirmButton: false
      });

    } catch (error) {
      console.error("Lỗi xuất file:", error);
      Swal.fire('Lỗi!', 'Không thể xuất file. Vui lòng kiểm tra lại phía Server.', 'error');
    }
  }
};
const getFullAddress = (dc) => {
  const tinh = listTinhThanh.find(t => t.id === dc.idTinhThanh)?.name
  const huyen = listQuanHuyen.find(q => q.id === dc.idQuanHuyen)?.name
  const xa = listPhuongXa.find(x => x.id === dc.idPhuongXa)?.name

  return [dc.diaChiChiTiet, xa, huyen, tinh]
    .filter(Boolean)
    .join(', ')
}
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

const onToggleStatus = (kh) => {
  if (authStore.role !== 'ADMIN') {
    Swal.fire({
      icon: 'error',
      title: 'Hành động bị chặn!',
      text: 'Bạn không có quyền thay đổi trạng thái khách hàng.',
      confirmButtonText: 'Đã hiểu',
      confirmButtonColor: '#7d161a',
      timer: 2500,
      timerProgressBar: true
    });
    return;
  }
  //a dmin thì đươc dùng
  handleToggleStatus(kh);
};
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

.filter-card-client {
  background: var(--white);
  border-radius: 15px;
  margin-bottom: 30px;
  /* Giữ viền xám nhẹ của bạn */

  box-shadow: 0 3px 6px rgba(131, 131, 131, 0.2);
}

.filter-label {
  font-size: 13px;
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
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%) !important;
}

.btn-red-dark:hover {
  transform: scale(1.04);
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%) !important;
  border-color: #8b0000;
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
  color: #6c757d;
  /* Màu xám trung tính */
  border: 1px solid #d0cece;
  border-radius: 8px;
  padding: 10px;
  transition: all 0.3s ease;
  width: 100%;
  /* Đảm bảo khớp với chiều cao input */
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-reset-filter:hover {
  background-color: #f8f9fa;
  color: #8b0000;
  border-color: #8b0000;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
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
  background-color: #8b0000 !important;
  /* Màu đỏ đô của bạn */
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

/* Container bọc giới tính */
.gender-filter-container {
  display: flex;
  align-items: center;
  /* Căn giữa theo chiều dọc với Label */
  height: 38px;
  /* Khớp với chiều cao chuẩn của ô input/select */
}

/* Tùy chỉnh màu sắc Radio */
.form-check-input:checked {
  background-color: #8b1c1c !important;
  border-color: #8b1c1c !important;
}

.form-check-input:focus {
  /* box-shadow: 0 0 0 0.25rem rgba(139, 28, 28, 0.25) !important; */
  border-color: #8b1c1c !important;
}

/* Đảm bảo khoảng cách giữa các lựa chọn */
.form-check-inline {
  margin-right: 1.5rem !important;
  display: flex;
  align-items: center;
}

.form-check-label {
  cursor: pointer;
  margin-left: 5px;
  color: #333;
}
</style>
<template>
  <div class="flex-grow-1 staff-manager-wrapper" style="padding: 25px; background: #ffffff; min-height: 100vh;">
    <div class="mb-3">
      <h2 class="title-page-cozy">Quản lý khách hàng</h2>

    </div>
    <div class="filter-card-premium mb-4">
      <div class="row  p-3 align-items-end">
        <div class="col-md-4">
          <label class="filter-label">Tìm kiếm</label>
          <input v-model="filters.keyword" class="form-control custom-input" placeholder="Tên, mã, SĐT, email..."
            @input="onSearchInput">
        </div>

        <div class="col-md-3">
          <label class="filter-label">Trạng thái</label>
          <Multiselect v-model="filters.trangThai" :options="trangThaiOptions" label="label" valueProp="value"
            placeholder="Chọn trạng thái" :searchable="false" :createOption="false" :canClear="true"
            :closeOnSelect="true" class="custom-multiselect-theme" @update:modelValue="handleSearch" />
        </div>

        <div class="col-md-3">
          <label class="filter-label">Giới tính</label>
          <div class="gender-filter-group">
            <div class="form-check form-check-inline">
              <input class="form-check-input" type="radio" v-model="filters.gioiTinh" :value="null" id="genderAll"
                name="gioiTinh" @change="handleSearch">
              <label class="form-check-label" for="genderAll">Tất cả</label>
            </div>

            <div class="form-check form-check-inline">
              <input class="form-check-input" type="radio" v-model="filters.gioiTinh" :value="true" id="genderMale"
                name="gioiTinh" @change="handleSearch">
              <label class="form-check-label" for="genderMale">Nam</label>
            </div>

            <div class="form-check form-check-inline">
              <input class="form-check-input" type="radio" v-model="filters.gioiTinh" :value="false" id="genderFemale"
                name="gioiTinh" @change="handleSearch">
              <label class="form-check-label" for="genderFemale">Nữ</label>
            </div>
          </div>
        </div>

        <div class="col-md-2">
          <button class="btn-red-dark w-100 py-2" @click="handleReset">Xóa bộ lọc</button>
        </div>
      </div>
    </div>

    <div class="mt-4">
      <div style="  background-color: #ffffff; display: flex; justify-content: space-between; align-items: center;">
        <h4 style="font-size: 1.25rem; font-weight: 700; color: #800000; margin: 0; font-family: sans-serif;">
          <!-- Danh sách khách hàng -->
        </h4>
        <div class="d-flex justify-content-end mb-3 gap-2">
          <div class="icon-tooltip">
            <button class="btn-red-dark d-flex align-items-center justify-content-center" @click="openModalAdd"
              type="button">
              <i class="fas fa-plus"></i>
              <span class="tooltip-text">Thêm khách hàng</span>
            </button>
          </div>

          <div class="icon-tooltip">
            <button class="btn-red-dark d-flex align-items-center justify-content-center"
              @click="handleActionWithAuth(handleExportExcel, 'ADMIN')" type="button">
              <i class="fas fa-file-excel"></i>
              <span class="tooltip-text">Xuất Excel</span>
            </button>
          </div>

          <div class="icon-tooltip">
            <button class="btn-red-dark d-flex align-items-center justify-content-center" @click="handleRefresh"
              type="button">
              <i class="fas fa-sync-alt"></i>
              <span class="tooltip-text">Làm mới</span>
            </button>
          </div>
        </div>
      </div>
      <div class="table-pagination-wrapper">
        <div class="table-responsive">
          <div class="table-responsive">
            <table class="table mb-0 custom-table table-hover align-middle">
              <thead>
                <tr style="background-color: #800000;">
                  <th class="text-center">
                    <input type="checkbox" class="form-check-input" @change="toggleSelectAll"
                      :checked="isSelectAllPages">
                  </th>
                  <th class="" style="color: white; border: none;">STT</th>
                  <th style="color: white; border: none;">Khách hàng</th>
                  <th class="" style="color: white; border: none;">Giới tính</th>
                  <th class="" style="color: white; border: none;">Ngày sinh</th>
                  <th style="color: white; border: none;">Liên hệ</th>
                  <th style="color: white; border: none;">Địa chỉ</th>
                  <th class="" style="color: white; border: none;">Trạng thái</th>
                  <th class="" style="color: white; border: none;">Thao tác</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(kh, index) in listKhachHang" :key="kh.id">
                  <td class="text-center">
                    <input type="checkbox" class="form-check-input" :value="kh.id" v-model="selectedIds"
                      :checked="isSelectAllPages || selectedIds.includes(kh.id)">
                  </td>
                  <td class="text-muted font-monospace">
                    {{ (pagination.currentPage - 1) * pagination.pageSize + index + 1 }}
                  </td>
                  <td>
                    <div class="d-flex align-items-center" style="justify-content: left;">

                      <div>
                        <div style="text-align: left !important;" class="text-dark mb-0">
                          <span>{{ kh.tenKhachHang }}</span>
                        </div>
                        <!-- <small class="text-muted" style="font-size: 11px;">{{ kh.maKhachHang }}</small> -->
                      </div>
                    </div>
                  </td>

                  <td class="">{{ kh.gioiTinh ? 'Nam' : 'Nữ' }}</td>
                  <td class="text-muted small">{{ formatBirthDate(kh.ngaySinh) }}</td>

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
                      {{ getFullAddress(kh) }}
                    </div>
                  </td>

                  <td class="">
                    <span :class="['badge-status', kh.trangThai === 1 ? 'status-active' : 'status-locked']">
                      {{ kh.trangThai === 1 ? 'Đang hoạt động' : 'Ngừng hoạt động' }}
                    </span>
                  </td>

                  <td class="" style="vertical-align: middle;">
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
                  <td colspan="9" class=" py-5 text-muted">
                    <i class="fas fa-box-open d-block mb-2" style="font-size: 2rem; opacity: 0.3;"></i>
                    Không tìm thấy khách hàng nào trong danh sách.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <CommonPagination v-model:currentPage="pagination.currentPage" v-model:pageSize="pagination.pageSize"
          :totalPages="pagination.totalPages" :totalElements="pagination.totalElements"
          :currentCount="listKhachHang.length" @change="(page) => handleSearch(page)" />
        <br>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue';
import { useClientLogic } from '../screens/clientFunction.js'; // Sửa lại đường dẫn khớp ảnh
import CommonPagination from '@/components/commonPagination.vue';
import clientService from '@/services/clientService';
import Swal from 'sweetalert2';
import { useRouter } from 'vue-router';
import Multiselect from '@vueform/multiselect'
import '@vueform/multiselect/themes/default.css'
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
    return;
  }
  callbackAction();
};
const { getStatusDisplay, fetchData } = useClientLogic();
const router = useRouter();
const listKhachHang = ref([]);
const filters = reactive({
  keyword: '',
  trangThai: null,
  gioiTinh: null,
  tuNgay: ''
});
const pagination = reactive({ currentPage: 1, pageSize: 8, totalPages: 0, totalElements: 0 });
const trangThaiOptions = [
  { label: "Đang làm việc", value: 1 },
  { label: "Ngừng hoạt động", value: 0 }
];
const handleSearch = async (page = null) => {
  console.log("Hàm handleSearch đã chạy!");
  if (pagination.currentPage > pagination.totalPages && pagination.totalPages > 0) {
    pagination.currentPage = 1;
  }
  try {
    const params = {
      ...filters,
      trangThai: filters.trangThai?.value ?? null
    };

    const data = await fetchData(filters, pagination);

    listKhachHang.value = data.content || [];
    pagination.totalPages = data.totalPages || 0;
    pagination.totalElements = data.totalElements || 0;

  } catch (error) {
    console.error("Lỗi khi fetch dữ liệu:", error);
  }
};
const selectedIds = ref([]);
const handleReset = async () => {
  filters.keyword = '';
  filters.trangThai = null;
  filters.gioiTinh = null;
  filters.tuNgay = '';
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
    const data = await fetchData(defaultFilters, defaultPagination);
    listKhachHang.value = data.content;
    console.log("Đã reset bộ lọc khách hàng thành công!");
  } catch (error) {
    console.error("Lỗi khi reset khách hàng:", error);
  }
};

const formatBirthDate = (date) => {
  if (!date) return '---';
  return dayjs(date).format('DD/MM/YYYY');
};
const isSelectAllPages = ref(false);
const toggleSelectAll = (event) => {
  const checked = event.target.checked;
  isSelectAllPages.value = checked;
  if (checked) {
    selectedIds.value = listKhachHang.value.map(kh => kh.id);
  } else {
    selectedIds.value = [];
  }
};
const toggleSelectItem = (id) => {
  if (isSelectAllPages.value) {
    isSelectAllPages.value = false;
  }
}
const handleEdit = (kh) => {
  if (kh.trangThai !== 1) {
    warnLocked();
  } else {
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
import dayjs from 'dayjs';

const handleExportExcel = async () => {
  if (!isSelectAllPages.value && selectedIds.value.length === 0) {
    Swal.fire({
      title: 'Thông báo',
      text: 'Vui lòng chọn ít nhất một khách hàng để xuất file!',
      icon: 'warning',
      confirmButtonText: 'Đồng ý'
    });
    return;
  }
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
        iconColor: '#7D161A',
        showConfirmButton: false
      });

    } catch (error) {
      console.error("Lỗi xuất file:", error);
      Swal.fire('Lỗi!', 'Không thể xuất file. Vui lòng kiểm tra lại phía Server.', 'error');
    }
  }
};
const getFullAddress = (kh) => {
  return kh.diaChi || '---'
}
const handleRefresh = async () => {
  try {
    await handleSearch();
    Swal.fire({
      title: 'Đã tải lại dữ liệu',
      icon: 'success',
      iconColor: '#7D161A',
      showConfirmButton: false,
      timer: 1500,
      toast: false,
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
        iconColor: '#7D161A',
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
  router.push(`/admin/client/form/${id}`);
};
const openModalView = (id) => {
  router.push(`/admin/client/view/${id}`);
};
const warnLocked = () => Swal.fire({ icon: 'info', title: 'Thông báo', text: 'Vui lòng mở khóa để chỉnh sửa thông tin này!' });

onMounted(handleSearch);
</script>

<style scoped>
@import '../clientStyle.css';
</style>
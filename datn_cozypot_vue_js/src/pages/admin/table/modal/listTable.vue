<script setup>
import {
  fetchAllBanAn,
  fetchAllKhuVuc,
  fetchBanAnById,
  updateBanAn,
} from "@/services/tableManageService";
import { computed, inject, onMounted, onUnmounted, ref, watch } from "vue";
import dayjs from "dayjs";
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';

import { usePermission } from "@/components/permissionHelper";
import CommonPagination from "@/components/commonPagination.vue";
import Swal from "sweetalert2";

// Cấu hình Toast mặc định của SweetAlert2 
const refreshTableData = inject("refreshTableData");
const Toast = Swal.mixin({
  toast: true,
  position: 'top-end',
  showConfirmButton: false,
  timer: 3000,
  timerProgressBar: true,
  background: '#ffffff',
  color: '#333',
  didOpen: (toast) => {
    toast.addEventListener('mouseenter', Swal.stopTimer)
    toast.addEventListener('mouseleave', Swal.resumeTimer)
  }
});

const { handleActionWithAuth } = usePermission();
const danhSachBan = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const pageSizeOptions = [5, 10, 20];

/* ===== CÁC HÀM TIỆN ÍCH ===== */
const getStatusText = (trangThai) => {
  const status = String(trangThai).trim();
  if (status === "0") return "Trống";
  if (status === "1") return "Có khách";
  return "Đã đặt";
};

/* ===== FETCH DỮ LIỆU TỪ BACKEND ===== */
const fetchAllBan = async () => {
  try {
    const data = await fetchAllBanAn();

    danhSachBan.value = data.map((ban, index) => {
      let loaiDatBanNormalized = 0;
      if (typeof ban.loaiDatBan === "object" && ban.loaiDatBan !== null) {
        loaiDatBanNormalized = Number(ban.loaiDatBan.value ?? 0);
      } else {
        loaiDatBanNormalized = Number(ban.loaiDatBan ?? 0);
      }

      const defaultCol = (index % 3) * 4 + 1;
      const defaultRow = Math.floor(index / 3) * 2 + 1;

      return {
        ...ban,
        loaiDatBan: loaiDatBanNormalized,
        column: ban.column || defaultCol,
        row: ban.row || defaultRow,
      };
    });
  } catch (e) {
    console.error("Lỗi load danh sách bàn", e);
  }
};

const listKhuVuc = ref([]);

const handleFetchAllKhuVuc = async () => {
  try {
    listKhuVuc.value = await fetchAllKhuVuc();
  } catch (error) {
    console.log(error);
  }
};

const initialTang = ref(null);
const fetchDetailBanAn = async (id) => {
  const data = await fetchBanAnById(id);
  form.value = {
    id: data.id,
    maBan: data.maBan,
    soNguoiToiDa: data.soCho,
    idKhuVuc: data.idKhuVuc,
    tang: data.soTang,
    trangThai: data.trangThai,
    loaiDatBan: data.loaiDatBan,
  };
  initialTang.value = data.soTang;
};

const khuVucTheoTang = computed(() => {
  if (!form.value.tang) return [];
  return listKhuVuc.value.filter((kv) => kv.tang === Number(form.value.tang));
});

/* Dữ liệu Options cho Multiselect trong Modal Update */
const tangOptionsModal = computed(() => {
  const tangs = [...new Set(listKhuVuc.value.map((kv) => kv.tang))];
  return tangs.map(t => ({ value: t, label: `Tầng ${t}` }));
});

const khuVucOptionsModal = computed(() => {
  return khuVucTheoTang.value.map(kv => ({ value: kv.id, label: kv.tenKhuVuc }));
});

const currentTime = ref("");
let timer = null;

const updateTime = () => {
  const now = new Date();
  currentTime.value = now.toLocaleTimeString("vi-VN");
};

const showUpdateModal = ref(false);

const openUpdateModal = async (id) => {
  try {
    await handleFetchAllKhuVuc();
    await fetchDetailBanAn(id);
    showUpdateModal.value = true;
  } catch (error) {
    console.error("Lỗi lấy chi tiết bàn:", error);
  }
};

const closeAddModal = () => {
  showUpdateModal.value = false;
  errors.value = { maBan: false, soNguoiToiDa: false, tang: false, idKhuVuc: false };
};

const form = ref({
  id: null,
  maBan: "",
  soNguoiToiDa: null,
  idKhuVuc: null,
  trangThai: 0,
  loaiDatBan: 0,
  tang: null,
});

/* ===== TOAST & VALIDATION ===== */
const errors = ref({
  maBan: false,
  soNguoiToiDa: false,
  tang: false,
  idKhuVuc: false
});

const validateForm = () => {
  let isValid = true;
  errors.value = { maBan: false, soNguoiToiDa: false, tang: false, idKhuVuc: false };

  if (!form.value.maBan || String(form.value.maBan).trim() === "") {
    errors.value.maBan = true; isValid = false;
  }
  if (!form.value.soNguoiToiDa || form.value.soNguoiToiDa <= 0) {
    errors.value.soNguoiToiDa = true; isValid = false;
  }
  if (!form.value.tang) {
    errors.value.tang = true; isValid = false;
  }
  if (!form.value.idKhuVuc) {
    errors.value.idKhuVuc = true; isValid = false;
  }

  if (!isValid) {
    Toast.fire({ icon: 'error', title: 'Dữ liệu không hợp lệ', text: 'Vui lòng kiểm tra lại các trường báo đỏ' });
  }
  return isValid;
};

const submitUpdateBan = async () => {
  if (!validateForm()) return; 

  const confirmResult = await Swal.fire({
    title: 'Xác nhận lưu?',
    text: "Bạn có chắc chắn muốn cập nhật thông tin bàn này không?",
    icon: 'question', 
    iconColor: '#7d161a', 
    showCancelButton: true,
    confirmButtonColor: '#7d161a', 
    cancelButtonColor: '#6c757d', 
    confirmButtonText: '<i class="fas fa-save me-1"></i> Đồng ý lưu',
    cancelButtonText: 'Hủy bỏ',
    reverseButtons: true 
  });

  if (confirmResult.isConfirmed) {
    try {
      await updateBanAn(form.value);
      closeAddModal(); 
      
      // 🚀 GỌI HÀM CẬP NHẬT CỦA CHA ĐỂ ĐỒNG BỘ LẠI TẤT CẢ DỮ LIỆU
      if (refreshTableData) {
        await refreshTableData();
      } else {
        await fetchAllBan(); // Sơ cua nếu chạy độc lập
      }
      
      Swal.fire({
        icon: 'success', title: 'Thành công!', text: 'Cập nhật thông tin bàn thành công!',
        iconColor: '#7d161a', confirmButtonText: 'Đóng', confirmButtonColor: '#7d161a', 
      });
    } catch (e) {
      Swal.fire({
        icon: 'error', title: 'Thất bại!', text: 'Có lỗi xảy ra khi cập nhật!', confirmButtonColor: '#dc3545',
      });
    }
  }
};

const formatDate = (time) => {
  if (!time) return "";
  return dayjs(time).format("DD/MM/YYYY HH:mm");
};

/* ===== FILTER 2 LỚP & SỐ GHẾ ===== */
const filterTang = ref([]);
const filterKhuVuc = ref([]); 
const filterSoGhe = ref(""); // THÊM MỚI: Biến lọc theo số ghế
const searchKeyword = ref("");

const listTangOptions = computed(() => {
  return [...new Set(danhSachBan.value.map((b) => b.soTang))].map((t) => ({
    label: `Tầng ${t}`,
    value: String(t),
  }));
});

const listKhuVucOptions = computed(() => {
  let filteredKV = listKhuVuc.value;
  if (filterTang.value && filterTang.value.length > 0) {
    filteredKV = filteredKV.filter(kv => filterTang.value.includes(String(kv.tang)));
  }
  return filteredKV.map(kv => ({
    label: kv.tenKhuVuc,
    value: String(kv.id),
  }));
});

const danhSachBanFiltered = computed(() => {
  let filtered = danhSachBan.value.filter((ban) => {
    const keyword = searchKeyword.value.toLowerCase().trim();
    const matchSearch =
      !keyword ||
      ban.maBan?.toLowerCase().includes(keyword) ||
      ban.tenKhuVuc?.toLowerCase().includes(keyword) ||
      String(ban.soCho).includes(keyword) ||
      String(ban.soTang).includes(keyword);

    const matchTang =
      !filterTang.value ||
      filterTang.value.length === 0 ||
      filterTang.value.includes(String(ban.soTang));

    const matchKhuVuc = 
      !filterKhuVuc.value ||
      filterKhuVuc.value.length === 0 ||
      filterKhuVuc.value.includes(String(ban.idKhuVuc));

    // THÊM MỚI: Lọc theo số ghế (Lớn hơn hoặc bằng)
    const matchSoGhe = 
      !filterSoGhe.value || 
      Number(ban.soCho) >= Number(filterSoGhe.value);

    return matchSearch && matchTang && matchKhuVuc && matchSoGhe;
  });

  return filtered.sort((a, b) => b.id - a.id);
});

const totalPages = computed(() => {
  const total = Math.ceil(danhSachBanFiltered.value.length / pageSize.value);
  return total > 0 ? total : 1;
});

const danhSachBanPaginated = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return danhSachBanFiltered.value.slice(start, end);
});

const resetFilter = () => {
  filterTang.value = [];
  filterKhuVuc.value = []; 
  filterSoGhe.value = ""; // THÊM MỚI: Reset ô số ghế
  searchKeyword.value = "";
};

// Khi chọn tầng khác -> Xóa khu vực cũ đi để không bị lệch logic
watch(filterTang, () => {
  filterKhuVuc.value = [];
});

watch(
  () => form.value.tang,
  (newTang) => {
    if (newTang !== initialTang.value) {
      form.value.idKhuVuc = null;
    }
  },
);

// THÊM MỚI: Watch cả filterSoGhe để chuyển về page 1 khi gõ
watch([searchKeyword, filterTang, filterKhuVuc, filterSoGhe], () => {
  currentPage.value = 1;
}, { deep: true });

watch(pageSize, () => {
  currentPage.value = 1;
});

watch(danhSachBanFiltered, () => {
  if (currentPage.value > totalPages.value) {
    currentPage.value = totalPages.value;
  }
});
watch(showUpdateModal, async (val) => {
  if (val) {
    await handleFetchAllKhuVuc();
  }
});

onMounted(() => {
  updateTime();
  timer = setInterval(updateTime, 1000);
  handleFetchAllKhuVuc();
  fetchAllBan();
});

onUnmounted(() => {
  clearInterval(timer);
});
</script>

<template>
  <div class="">

    <div>
      <div class="filter-box">
        <div>
          <label>Tìm kiếm</label>
          <br />
          <input type="text" v-model="searchKeyword" class="dropdown-display"
            placeholder="Tìm theo mã bàn, khu vực, tầng..." />
        </div>
        <div class="filter-item">
          <label>Lọc theo tầng</label>
          <div class="multiselect-wrapper-sm">
            <Multiselect v-model="filterTang" :options="listTangOptions" mode="tags" :searchable="true" :canClear="true"
              placeholder="Chọn tầng..." no-results-text="Không tìm thấy tầng"
              class="custom-filter-multiselect" />
          </div>
        </div>

        <div class="filter-item">
          <label>Lọc theo khu vực</label>
          <div class="multiselect-wrapper-sm">
            <Multiselect v-model="filterKhuVuc" :options="listKhuVucOptions" mode="tags" :searchable="true" :canClear="true"
              placeholder="Chọn khu vực..." no-results-text="Không có khu vực nào"
              class="custom-filter-multiselect" />
          </div>
        </div>

        <div class="filter-item">
          <label>Số ghế (&ge;)</label>
          <input 
            type="number" 
            v-model="filterSoGhe" 
            class="form-input" 
            style="width: 120px; height: 38px; margin-top: 4px;" 
            placeholder="VD: 4" 
            min="1" 
          />
        </div>

        
        <div>
          <br />
          <button class="btn" @click="resetFilter">
            <i class="fas fa-undo"></i>
            Hủy bộ lọc
          </button>
        </div>
      </div>
    </div>

    <div class="list-card">
      <table class="table shadow-sm">
        <thead>
          <tr>
            <th>STT</th>
            <th>Bàn</th>
            <th>Số ghế</th>
            <th>Tầng</th>
            <th>Khu vực</th>
            <th class="text-center" style="width: 120px;">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(ban, index) in danhSachBanPaginated" :key="ban.id">
            <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
            <td><strong>{{ ban.maBan }}</strong></td>
            <td>{{ ban.soCho }} chỗ</td>
            <td>{{ ban.soTang }}</td>
            <td>{{ ban.tenKhuVuc }}</td>
            <td class="text-center align-middle">
              <button class="action-list" 
                      style="border: none; background-color: transparent; padding: 0;"
                      @click="handleActionWithAuth(() => openUpdateModal(ban.id), 'ADMIN')">
                <div class="icon-tooltip">
                  <i class="fas fa-pen action-icon" style=" font-size: 1.1rem; cursor: pointer; transition: 0.2s;" onmouseover="this.style.transform='scale(1.1)'" onmouseout="this.style.transform='scale(1)'"></i>
                  <span class="tooltip-text">Xem chi tiết</span>
                </div>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="custom-pagination-wrapper mt-4 pt-3 border-top">
      <CommonPagination v-model:currentPage="currentPage" v-model:pageSize="pageSize" :total-pages="totalPages"
        :total-elements="danhSachBanFiltered.length" :current-count="danhSachBanPaginated.length" @change="() => { }" />
    </div>
  </div>

  <div v-if="showUpdateModal" class="modal-overlay">
    <div class="modal-box">
      <div class="modal-header">
        <h4><i class="fas fa-edit me-2" style="color: #7d161a;"></i> Sửa thông tin bàn</h4>
        <button class="close-btn" @click="closeAddModal">&times;</button>
      </div>

      <div class="modal-body">
        <div class="form-group">
          <label class="form-label">Mã bàn <span class="required-star">*</span></label>
          <input style="opacity: 0.5;" disabled type="text" v-model="form.maBan" class="form-input" :class="{ 'is-invalid': errors.maBan }"
            placeholder="Nhập mã bàn..." />
          <span v-if="errors.maBan" class="error-text">Mã bàn không được để trống</span>
        </div>

        <div class="form-group">
          <label class="form-label">Số người tối đa <span class="required-star">*</span></label>
          <input type="number" v-model="form.soNguoiToiDa" class="form-input"
            :class="{ 'is-invalid': errors.soNguoiToiDa }" placeholder="Nhập số người tối đa..." />
          <span v-if="errors.soNguoiToiDa" class="error-text">Số người tối đa phải lớn hơn 0</span>
        </div>

        <div class="form-group">
          <label class="form-label">Tầng <span class="required-star">*</span></label>
          <Multiselect v-model="form.tang" :options="tangOptionsModal" :searchable="true" mode="single"
            placeholder="-- Tìm hoặc chọn tầng --"
            :class="['custom-modal-multiselect', { 'is-invalid-multiselect': errors.tang }]"
            no-results-text="Không tìm thấy tầng" />
          <span v-if="errors.tang" class="error-text">Vui lòng chọn tầng</span>
        </div>

        <div class="form-group">
          <label class="form-label">Khu vực <span class="required-star">*</span></label>
          <Multiselect v-model="form.idKhuVuc" :options="khuVucOptionsModal" :searchable="true" mode="single"
            :disabled="!form.tang" placeholder="-- Tìm hoặc chọn khu vực --"
            :class="['custom-modal-multiselect', { 'is-invalid-multiselect': errors.idKhuVuc }]"
            no-results-text="Không tìm thấy khu vực" />
          <span v-if="errors.idKhuVuc" class="error-text">Vui lòng chọn khu vực</span>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-outline" @click="closeAddModal">Hủy bỏ</button>
        <button class="btn btn-active" @click="submitUpdateBan">
          <i class="fas fa-save me-1"></i> Lưu thay đổi
        </button>
      </div>
    </div>
  </div>
</template>

<style src="@/pages/admin/table/tableManagementStyle.css"></style>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* ==================================
   TOAST NOTIFICATION (Báo lỗi ở góc)
===================================== */
.toast-container {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 10000;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.custom-toast {
  min-width: 280px;
  padding: 14px 20px;
  border-radius: 8px;
  color: white;
  font-weight: 500;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  animation: slideInRight 0.3s ease, fadeOut 0.3s ease 2.7s forwards;
}

.custom-toast.error {
  background: #dc3545;
}

.custom-toast.success {
  background: #28a745;
}

@keyframes slideInRight {
  from {
    transform: translateX(100%);
    opacity: 0;
  }

  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes fadeOut {
  from {
    opacity: 1;
  }

  to {
    opacity: 0;
  }
}

/* ==================================
   MODAL & FORM UPDATE (Làm đẹp UI)
===================================== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(2px);
}

.modal-box {
  background: #fff;
  width: 450px;
  border-radius: 12px;
  animation: fadeIn 0.25s ease;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h4 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #888;
  cursor: pointer;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #dc3545;
}

.modal-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-weight: 600;
  font-size: 14px;
  color: #444;
}

.required-star {
  color: #dc3545;
  margin-left: 2px;
}

.form-input {
  width: 100%;
  border-radius: 8px;
  border: 1px solid #ced4da;
  padding: 10px 12px;
  margin: 0;
  font-size: 14px;
  transition: 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #7d161a;
  box-shadow: 0 0 0 3px rgba(125, 22, 26, 0.1);
}

/* KHUNG ĐỎ VALIDATE */
.is-invalid {
  border-color: #dc3545 !important;
  background-color: #fff8f8;
}

.is-invalid:focus {
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.15) !important;
}

.is-invalid-multiselect {
  --ms-border-color: #dc3545;
  --ms-bg: #fff8f8;
}

.error-text {
  color: #dc3545;
  font-size: 12px;
  margin-top: 2px;
}

.modal-footer {
  padding: 16px 20px;
  background: #f8f9fa;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
}

/* ==================================
   CUSTOM MULTISELECT TRONG MODAL
===================================== */
.custom-modal-multiselect {
  --ms-border-color: #ced4da;
  --ms-radius: 8px;
  --ms-py: 8px;
  --ms-ring-color: rgba(125, 22, 26, 0.1);
  --ms-border-color-active: #7d161a;
  --ms-option-bg-pointed: #fcf4f4;
  --ms-option-color-pointed: #7d161a;
  --ms-option-bg-selected: #7d161a;
  --ms-option-color-selected: #ffffff;
}

/* ==================================
   CÁC STYLE CŨ (GIỮ NGUYÊN)
===================================== */
.dropdown-wrapper {
  position: relative;
  width: 300px;
  padding-bottom: 8px;
}

.dropdown-display {
  min-width: 400px;
  border: 1px solid #ddd;
  padding: 8px 10px;
  border-radius: 8px;
  background: #fff;
}

.btn:focus,
.btn:active,
.btn:focus-visible {
  color: white !important;
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%) !important;
  box-shadow: none !important;
  outline: none !important;
  border-color: transparent !important;
}

.btn:hover {
  transform: scale(1.04);
  color: white !important;
}

.dropdown-search {
  width: 100%;
  padding: 6px;
  margin-bottom: 6px;
  border-radius: 6px;
  border: 1px solid #ddd;
}

.dropdown-item {
  padding: 6px 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-status {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.badge {
  background: #7d161a;
  color: white;
  padding: 4px 8px;
  border-radius: 20px;
  font-size: 12px;
  margin-right: 6px;
}

.multiselect-wrapper {
  width: 200px;
}

:deep(.multiselect) {
  min-height: 38px;
  border: 1px solid #ced4da;
  border-radius: 0.375rem;
}

:deep(.multiselect.is-active) {
  box-shadow: 0 0 0 3px rgba(211, 47, 47, 0.25);
  border-color: #d32f2f;
}

:deep(.multiselect-option.is-selected) {
  background: #d32f2f;
}

:deep(.multiselect-option.is-selected.is-pointed) {
  background: #b71c1c;
}

:deep(.multiselect-placeholder) {
  color: #495057;
}

.table {
  margin-bottom: 0 !important;
}

thead {
  border-radius: 10px;
  background-color: #8B0000 !important;
}

thead tr {
  border-radius: 15px;
  background-color: transparent !important;
}

thead tr th {
  padding-left: 20px;
  background-color: transparent !important;
  color: white;
}

tr td {
  padding-left: 20px;
}

thead tr:hover {
  background-color: #8B0000 !important;
}

tr:hover {
  background-color: lightgray;
}

.list-card {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border-light) !important;
  box-shadow: var(--shadow-premium) !important;
}

.pagination-footer-custom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  color: #666;
}

.page-size-select-v2 {
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 4px 8px;
  outline: none;
  background-color: #fff;
  cursor: pointer;
}

.footer-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.pagination-v2 {
  display: flex;
  list-style: none;
  padding: 0;
  margin: 0;
  gap: 15px;
  align-items: center;
}

.page-item-v2 a {
  text-decoration: none;
  color: #ccc;
  font-weight: 600;
  transition: all 0.2s;
}

.page-item-v2 i {
  font-size: 12px;
}

.page-item-v2.active a {
  background-color: #7D161A;
  color: white !important;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
}

.page-item-v2:not(.disabled):not(.active) a:hover {
  color: #7D161A;
}

.page-item-v2.disabled {
  opacity: 0.3;
  pointer-events: none;
}

.table tbody tr:last-child td {
  border-bottom: none;
}

.filter-box {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.filter-box select {
  width: 200px;
}

.form-check {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

.form-check-input {
  cursor: pointer;
}

.form-check-label {
  font-size: 14px;
}

.btn {
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
  border-radius: 8px;
  font-size: 14px;
  padding: 8px 16px;
  color: white;
  cursor: pointer;
  transition: 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.btn-outline {
  border: 1px solid #ccc;
  background: #fff;
  color: #333;
}

.btn-outline:hover {
  background: #f1f1f1;
  color: #333 !important;
}

.btn-active {
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
  color: #fff;
  border: none;
}

@keyframes fadeIn {
  from {
    transform: scale(0.92);
    opacity: 0;
  }

  to {
    transform: scale(1);
    opacity: 1;
  }
}

.filter-item {
  display: flex;
  flex-direction: column;
  align-self: center;
}

.multiselect-wrapper-sm {
  width: 250px;
}

.custom-filter-multiselect {
  --ms-border-color: #ddd;
  --ms-radius: 8px;
  --ms-py: 6px;
  --ms-ring-color: rgba(125, 22, 26, 0.15);
  --ms-border-color-active: #7d161a;
  --ms-option-bg-pointed: #fcf4f4;
  --ms-option-color-pointed: #7d161a;
  --ms-option-bg-selected: #7d161a;
  --ms-option-color-selected: #ffffff;
  --ms-option-bg-selected-pointed: #5f0f12;
  --ms-tag-bg: #7d161a;
  --ms-tag-color: #ffffff;
  --ms-tag-radius: 4px;
  --ms-tag-font-weight: 500;
  --ms-clear-color: #999;
  --ms-clear-color-hover: #dc3545;
}

:deep(.multiselect-single-labelText),
:deep(.multiselect-placeholder) {
  font-size: 14px;
  color: #495057;
}

:deep(.multiselect-caret) {
  background-color: #666;
}

:deep(.multiselect.is-active .multiselect-caret) {
  background-color: #7d161a;
}

.icon-tooltip {
    position: relative;
    display: inline-flex;
    align-items: center;
    justify-content: center;
}

/* Tooltip text */
.icon-tooltip .tooltip-text {
    position: absolute;
    bottom: 130%; /* hiện phía trên icon */
    left: 50%;
    transform: translateX(-50%);
    background-color: #333;
    color: #fff;
    padding: 6px 10px;
    font-size: 12px;
    border-radius: 6px;
    white-space: nowrap;
    opacity: 0;
    visibility: hidden;
    transition: all 0.2s ease;
    pointer-events: none;
    z-index: 999;
}

/* Mũi tên nhỏ */
.icon-tooltip .tooltip-text::after {
    content: "";
    position: absolute;
    top: 100%;
    left: 50%;
    transform: translateX(-50%);
    border-width: 6px;
    border-style: solid;
    border-color: #333 transparent transparent transparent;
}

/* Hover hiện tooltip */
.icon-tooltip:hover .tooltip-text {
    opacity: 1;
    visibility: visible;
    transform: translateX(-50%) translateY(-4px);
}
</style>
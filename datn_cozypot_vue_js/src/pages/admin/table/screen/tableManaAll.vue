<script setup>
import {
  addBanAn,
  createBanFull,
  createKhuVuc,
  fetchAllBanAn,
  fetchAllKhuVuc,
  fetchTableStatusByDate,
} from "@/services/tableManageService";
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { provide } from "vue";
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';
import { usePermission } from "@/components/permissionHelper";
import Swal from "sweetalert2";

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
/* 1. KHỞI TẠO TRẠNG THÁI */
const danhSachBan = ref([]);
const listKhuVuc = ref([]);
const refreshKey = ref(0);

const listLoaiDatBanOptions = [
  { label: "Cho phép đặt online", value: 1 },
  { label: "Không cho phép", value: 0 },
];

/* 3. FETCH DỮ LIỆU TỪ BACKEND */
const fetchAllBan = async () => {
  try {
    const data = await fetchAllBanAn();
    danhSachBan.value = data.map((ban, index) => {
      const defaultCol = (index % 3) * 4 + 1;
      const defaultRow = Math.floor(index / 3) * 2 + 1;
      return {
        ...ban,
        column: ban.column || defaultCol,
        row: ban.row || defaultRow,
      };
    });
  } catch (e) {
    console.error("Lỗi load danh sách bàn", e);
  }
};

const handleFetchAllKhuVuc = async () => {
  try {
    listKhuVuc.value = await fetchAllKhuVuc();
  } catch (error) {
    console.log(error);
  }
};

const currentTime = ref("");
let timer = null;
const updateTime = () => {
  const now = new Date();
  currentTime.value = now.toLocaleTimeString("vi-VN");
};

// Pop up
const listTang = computed(() => {
  return [...new Set(listKhuVuc.value.map((kv) => kv.tang))].map(t => ({ value: t, label: `Tầng ${t}` }));
});

const isTangMoi = computed(() => {
  if (!selectedTang.value) return false;
  return !listKhuVuc.value.map(kv => kv.tang).includes(Number(selectedTang.value));
});

const selectedTang = ref(null);
const newKhuVucName = ref("");
const showAddModal = ref(false);

const openAddModal = () => {
  showAddModal.value = true;
};

// ==========================================
// VALIDATION & SUBMIT: THÊM BÀN
// ==========================================
const addBanErrors = ref({ soNguoiToiDa: false, tang: false, idKhuVuc: false, newKhuVucName: false });

const closeAddModal = () => {
  showAddModal.value = false;
  // Reset form và lỗi
  addBanErrors.value = { soNguoiToiDa: false, tang: false, idKhuVuc: false, newKhuVucName: false };
  form.value = { soNguoiToiDa: null, idKhuVuc: "", loaiDatBan: 0, tang: "" };
  selectedTang.value = null;
  newKhuVucName.value = "";
};

const form = ref({
  soNguoiToiDa: null,
  idKhuVuc: "",
  loaiDatBan: 0,
  tang: "",
});

const khuVucTheoTang = computed(() => {
  if (!form.value.tang) return [];
  return listKhuVuc.value.filter((kv) => kv.tang === Number(form.value.tang));
});

const validateAddBan = () => {
  let isValid = true;
  addBanErrors.value = { soNguoiToiDa: false, tang: false, idKhuVuc: false, newKhuVucName: false };

  if (!form.value.soNguoiToiDa || form.value.soNguoiToiDa <= 0 || form.value.soNguoiToiDa > 50) { addBanErrors.value.soNguoiToiDa = true; isValid = false; }
  if (!selectedTang.value) { addBanErrors.value.tang = true; isValid = false; }
  
  if (isTangMoi.value) {
    if (!newKhuVucName.value || newKhuVucName.value.trim() === "" || newKhuVucName.value.length > 100) { addBanErrors.value.newKhuVucName = true; isValid = false; }
  } else {
    if (!form.value.idKhuVuc) { addBanErrors.value.idKhuVuc = true; isValid = false; }
  }

  if (!isValid) {
    // 🚀 DÙNG TOAST Ở GÓC MÀN HÌNH 🚀
    Toast.fire({
      icon: 'error',
      title: 'Dữ liệu không hợp lệ',
      text: 'Vui lòng kiểm tra lại các trường báo đỏ'
    });
  }
  return isValid;
};

const submitAddBan = async () => {
  if (!validateAddBan()) return;

  const confirmResult = await Swal.fire({
    title: 'Xác nhận lưu?',
    text: "Bạn có chắc chắn muốn thêm bàn này không?",
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
      let tenKhuVucFinal = null;
      if (form.value.idKhuVuc) {
        const kv = listKhuVuc.value.find((k) => k.id === form.value.idKhuVuc);
        tenKhuVucFinal = kv?.tenKhuVuc;
      }
      if (!tenKhuVucFinal) {
        tenKhuVucFinal = newKhuVucName.value.trim();
      }

      await createBanFull({
        soNguoiToiDa: Number(form.value.soNguoiToiDa),
        loaiDatBan: form.value.loaiDatBan,
        tang: Number(form.value.tang),
        tenKhuVuc: tenKhuVucFinal,
      });

      closeAddModal();
      
      // 🚀 GỌI LẠI API ĐỂ CẬP NHẬT CẢ BẢNG VÀ SƠ ĐỒ
      await refreshData(); 
      refreshKey.value += 1;

      Swal.fire({ icon: 'success', title: 'Thành công!', text: 'Thêm bàn mới thành công!', iconColor: '#7d161a', confirmButtonColor: '#7d161a' });
    } catch (error) {
      Swal.fire({ icon: 'error', title: 'Thất bại!', text: 'Có lỗi xảy ra khi thêm bàn!', confirmButtonColor: '#dc3545' });
    }
  }
};

// ==========================================
// VALIDATION & SUBMIT: THÊM TẦNG/KHU VỰC
// ==========================================
const showAddTangModal = ref(false);
const addTangErrors = ref({ tang: false, tenKhuVuc: false });

const tangForm = ref({ tang: "", tenKhuVuc: "", moTa: "" });

const openAddTangModal = () => showAddTangModal.value = true;

const closeAddTangModal = () => {
  showAddTangModal.value = false;
  addTangErrors.value = { tang: false, tenKhuVuc: false };
  tangForm.value = { tang: "", tenKhuVuc: "", moTa: "" };
};

const validateAddTang = () => {
  let isValid = true;
  addTangErrors.value = { tang: false, tenKhuVuc: false };
  const parsedTang = Number(tangForm.value.tang);

  if (!tangForm.value.tang || isNaN(parsedTang) || parsedTang <= 0) { addTangErrors.value.tang = true; isValid = false; }
  if (!tangForm.value.tenKhuVuc || tangForm.value.tenKhuVuc.trim() === "") { addTangErrors.value.tenKhuVuc = true; isValid = false; }

  if (!isValid) {
    // 🚀 DÙNG TOAST Ở GÓC MÀN HÌNH 🚀
    Toast.fire({
      icon: 'error',
      title: 'Dữ liệu không hợp lệ',
      text: 'Vui lòng kiểm tra lại các trường báo đỏ'
    });
  }
  return isValid;
};

const filterNumberOnly = (query, selectRef) => {
  if (!query) return;
  const numericVal = query.replace(/[^0-9]/g, '');
  if (query !== numericVal && selectRef) {
    selectRef.search = numericVal; // Ép lại thành số
  }
};

// 2. Khai báo ref để trỏ tới Multiselect
const tangMultiselectRef = ref(null);
const addKhuVucTangRef = ref(null);

const submitAddTang = async () => {
  if (!validateAddTang()) return;
  
  const confirmResult = await Swal.fire({
    title: 'Xác nhận lưu?',
    text: "Bạn có chắc chắn muốn thêm khu vực/tầng này không?",
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
      await createKhuVuc({
        tang: Number(tangForm.value.tang),
        tenKhuVuc: tangForm.value.tenKhuVuc.trim(),
        moTa: tangForm.value.moTa,
      });

      // 🚀 GỌI LẠI HÀM CẬP NHẬT
      await handleFetchAllKhuVuc();
      refreshKey.value += 1;
      closeAddTangModal();

      Swal.fire({ icon: 'success', title: 'Thành công!', text: 'Thêm tầng và khu vực thành công!', iconColor: '#7d161a', confirmButtonColor: '#7d161a' });
    } catch (error) {
      Swal.fire({ icon: 'error', title: 'Lỗi', text: error.response?.data || error.message || "Có lỗi xảy ra", confirmButtonColor: '#dc3545' });
    }
  }
};





watch(selectedTang, (val) => {
  form.value.tang = Number(val);
  if (isTangMoi.value) {
    form.value.idKhuVuc = "";
  }
});

watch(
  () => form.value.tang,
  () => {
    form.value.idKhuVuc = "";
  },
);

const refreshData = async () => {
  await Promise.all([fetchAllBan(), handleFetchAllKhuVuc()]);
};

provide("refreshTableData", refreshData);
// Wrap trong computed để đảm bảo tính reactive khi data thay đổi
provide("danhSachBan", computed(() => danhSachBan.value)); 
provide("listKhuVuc", computed(() => listKhuVuc.value));

onMounted(() => {
  updateTime();
  timer = setInterval(updateTime, 1000);
  fetchAllBan();
  handleFetchAllKhuVuc();
});

onUnmounted(() => {
  clearInterval(timer);
});
</script>

<template>
  <div class="layout-table">
    <div class="navbar-search m-4">
      <div class="header-manager">
        <div>
          <h3 style="color: #7d161a; font-weight: bold; font-size: 1.5rem">
            Quản lý bàn
          </h3>
        </div>

        <div>
          <button class="btn" @click="handleActionWithAuth(() => openAddModal(), 'ADMIN')">
            <i class="fas fa-plus me-1"></i> Thêm bàn
          </button>
          <button class="btn ms-2" @click="handleActionWithAuth(() => openAddTangModal(), 'ADMIN')">
            <i class="fas fa-layer-group me-1"></i> Thêm tầng
          </button>
        </div>
      </div>

      <ul class="nav nav-tabs">
        <li class="nav-item">
          <router-link class="nav-link" exact-active-class="active" to="/manage/all">
            <i class="fa-solid fa-list"></i> Lịch đặt bàn
          </router-link>
        </li>
        <li class="nav-item">
          <router-link class="nav-link" active-class="active" to="/manage/all/danh-sach">
            <i class="fa-regular fa-calendar"></i> Danh sách bàn
          </router-link>
        </li>
      </ul>

      <hr />

      <div class="contain-frame mt-3">
        <router-view :key="refreshKey" :selectedDate="selectedDate" :tableStatusMap="tableStatusMap" />
      </div>
    </div>
  </div>

  <div v-if="showAddModal" class="modal-overlay">
    <div class="modal-box">
      <div class="modal-header">
        <h4><i class="fas fa-plus-circle me-2" style="color: #7d161a;"></i> Thêm bàn ăn</h4>
        <button class="close-btn" @click="closeAddModal">&times;</button>
      </div>

      <div class="modal-body">
        <div class="form-group">
          <label class="form-label">Số người tối đa <span class="required-star">*</span></label>
          <input 
            type="number" 
            v-model="form.soNguoiToiDa" 
            class="form-input"
            :class="{'is-invalid': addBanErrors.soNguoiToiDa}"
            placeholder="Nhập số người tối đa" 
          />
          <span v-if="addBanErrors.soNguoiToiDa" class="error-text">Số người phải lớn hơn 0 và nhỏ hơn 50</span>
        </div>

        <div class="form-group">
          <label class="form-label">Tầng <span class="required-star">*</span></label>
          <Multiselect 
            ref="tangMultiselectRef"
            v-model="selectedTang" 
            :options="listTang" 
            :searchable="true" 
            :create-option="true" 
            :canClear="true" 
            placeholder="-- Chọn hoặc nhập số tầng mới --"
            no-results-text="Nhập số để tạo tầng mới" 
            :class="['custom-modal-multiselect', {'is-invalid-multiselect': addBanErrors.tang}]"
            @search-change="(query) => filterNumberOnly(query, tangMultiselectRef)"
          /> 
          <span v-if="addBanErrors.tang" class="error-text">Vui lòng chọn hoặc nhập tầng</span>
        </div>
        
        <div v-if="isTangMoi" class="form-group">
          <label class="form-label">Tên khu vực mới <span class="required-star">*</span></label>
          <input
            type="text"
            v-model="newKhuVucName"
            class="form-input"
            :class="{'is-invalid': addBanErrors.newKhuVucName}"
            placeholder="Nhập tên khu vực mới"
          />
          <span v-if="addBanErrors.newKhuVucName" class="error-text">Vui lòng nhập tên khu vực</span>
        </div>

        <div class="form-group" v-if="!isTangMoi">
          <label class="form-label">Khu vực <span class="required-star">*</span></label>
          <Multiselect 
            v-model="form.idKhuVuc" 
            :options="khuVucTheoTang.map(kv => ({ value: kv.id, label: kv.tenKhuVuc }))" 
            :searchable="true" 
            :canClear="true" 
            :disabled="!form.tang"
            placeholder="-- Chọn khu vực --"
            no-results-text="Không tìm thấy khu vực nào" 
            :class="['custom-modal-multiselect', {'is-invalid-multiselect': addBanErrors.idKhuVuc}]"
          />
          <span v-if="addBanErrors.idKhuVuc" class="error-text">Vui lòng chọn khu vực</span>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-outline" @click="closeAddModal">Hủy bỏ</button>
        <button class="btn btn-active" @click="submitAddBan">
          <i class="fas fa-save me-1"></i> Lưu bàn
        </button>
      </div>
    </div>
  </div>

  <div v-if="showAddTangModal" class="modal-overlay">
    <div class="modal-box">
      <div class="modal-header">
        <h4><i class="fas fa-layer-group me-2" style="color: #7d161a;"></i> Thêm tầng & khu vực</h4>
        <button class="close-btn" @click="closeAddTangModal">&times;</button>
      </div>

      <div class="modal-body">
        <div class="form-group">
          <label class="form-label">Tầng <span class="required-star">*</span></label>
          <Multiselect
            ref="addKhuVucTangRef"
            v-model="tangForm.tang"
            :options="listTang"
            :searchable="true"
            :create-option="true"
            placeholder="-- Chọn hoặc nhập số tầng mới --"
            no-results-text="Nhập số để tạo tầng mới"
            :class="['custom-modal-multiselect', {'is-invalid-multiselect': addTangErrors.tang}]"
            @search-change="(query) => filterNumberOnly(query, addKhuVucTangRef)"
          />
          <span v-if="addTangErrors.tang" class="error-text">Vui lòng chọn hoặc nhập số tầng hợp lệ</span>
        </div>

        <div class="form-group">
          <label class="form-label">Tên khu vực <span class="required-star">*</span></label>
          <input 
            type="text" 
            v-model="tangForm.tenKhuVuc" 
            class="form-input"
            :class="{'is-invalid': addTangErrors.tenKhuVuc}"
            placeholder="Nhập tên khu vực (VD: Sảnh A, Ngoài trời...)"
          />
          <span v-if="addTangErrors.tenKhuVuc" class="error-text">Tên khu vực không được để trống</span>
        </div>

        <div class="form-group">
          <label class="form-label">Mô tả</label>
          <textarea
            v-model="tangForm.moTa"
            rows="3"
            class="form-input"
            placeholder="Nhập mô tả khu vực (không bắt buộc)"
          ></textarea>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-outline" @click="closeAddTangModal">Hủy bỏ</button>
        <button class="btn btn-active" @click="submitAddTang">
          <i class="fas fa-save me-1"></i> Lưu tầng
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

:global(.swal2-container) {
  z-index: 99999 !important;
}

.layout-table {
  display: flex;
  background-color: white;
  height: 100%;
}

.header-manager {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.navbar-search {
  width: 100%;
}

/* ==================================
   MODAL & FORM CHUNG
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
.close-btn:hover { color: #dc3545; }

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
  font-size: 14px;
  transition: 0.2s;
}

textarea.form-input {
  resize: none;
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

/* Fix text multiselect */
:deep(.multiselect-single-labelText),
:deep(.multiselect-placeholder) {
  font-size: 14px;
  color: #495057;
}
:deep(.multiselect-caret) { background-color: #666; }
:deep(.multiselect.is-active .multiselect-caret) { background-color: #7d161a; }

/* ==================================
   BUTTONS CHUNG
===================================== */
.btn {
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
  color: white;
  transition: 0.2s;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  padding: 8px 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.btn:focus,
.btn:active,
.btn:focus-visible {
  color: white !important;
  box-shadow: none !important;
  outline: none !important;
}

.btn:hover {
  transform: scale(1.04);
  color: white;
}

.btn-outline {
  background: white !important;
  color: #333 !important;
  border: 1px solid #ccc !important;
}

.btn-outline:hover {
  background: #f1f1f1 !important;
  color: #000 !important;
  transform: scale(1.04);
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

:deep(.custom-modal-multiselect) {
  /* Màu viền khi click (Focus) */
  --ms-border-color-active: #7d161a;
  --ms-ring-color: rgba(125, 22, 26, 0.2);

  /* Màu khi Hover (Rê chuột qua) */
  --ms-option-bg-pointed: #fdf2f2;
  --ms-option-color-pointed: #7d161a;

  /* Màu khi Đã chọn (Selected) */
  --ms-option-bg-selected: #7d161a;
  --ms-option-color-selected: #ffffff;

  /* Màu khi Hover vào cái Đã chọn */
  --ms-option-bg-selected-pointed: #5f0f12;
  --ms-option-color-selected-pointed: #ffffff;
  
  /* Căn chỉnh bo góc cho đẹp */
  --ms-radius: 8px;
  --ms-py: 8px;
}
</style>
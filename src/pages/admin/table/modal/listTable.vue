<script setup>
import {
  fetchAllBanAn,
  fetchAllKhuVuc,
  fetchBanAnById,
  updateBanAn,
} from "@/services/tableManageService";
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import dayjs from "dayjs";
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';

import { usePermission } from "@/components/permissionHelper";
const { handleActionWithAuth } = usePermission();
const danhSachBan = ref([]);

/* 2. CÁC HÀM TIỆN ÍCH (Sửa lỗi "is not a function") */
const getStatusText = (trangThai) => {
  const status = String(trangThai).trim();
  if (status === "0") return "Trống";
  if (status === "1") return "Có khách";
  return "Đã đặt";
};

/* 3. FETCH DỮ LIỆU TỪ BACKEND */
const fetchAllBan = async () => {
  try {
    const data = await fetchAllBanAn();

    danhSachBan.value = data.map((ban, index) => {
      // ✅ Chuẩn hóa loaiDatBan về number 0 hoặc 1
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
        loaiDatBan: loaiDatBanNormalized, // 👈 thêm dòng này
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
  console.log("===== DATA TỪ API =====");
  console.log(data);
  console.log("JSON:", JSON.stringify(data, null, 2));
  console.log("=======================");
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
  // Debug
  console.log("ID Khu vực:", form.value.idKhuVuc, typeof form.value.idKhuVuc);
  console.log(
    "List khu vực IDs:",
    listKhuVuc.value.map((k) => ({ id: k.id, type: typeof k.id })),
  );
};

const khuVucTheoTang = computed(() => {
  if (!form.value.tang) return [];
  return listKhuVuc.value.filter((kv) => kv.tang === Number(form.value.tang));
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
    await fetchDetailBanAn(id); //  load chi tiết bàn
    showUpdateModal.value = true; //  mở modal SAU khi có data
  } catch (error) {
    console.error("Lỗi lấy chi tiết bàn:", error);
  }
};

const closeAddModal = () => {
  showUpdateModal.value = false;
};

const form = ref({
  id: null,
  maBan: "",
  soNguoiToiDa: null,
  idKhuVuc: null,
  trangThai: 0,
  loaiDatBan: 0,
  tang: "",
});

const submitUpdateBan = async () => {
  try {
    await updateBanAn(form.value);
    closeAddModal();
    await fetchAllBan();
    alert("Update thành công!");
  } catch (e) {
    console.error("Lỗi sửa bàn", e);
  }
};

const formatDate = (time) => {
  if (!time) return "";
  return dayjs(time).format("DD/MM/YYYY HH:mm");
};

/* ===== FILTER ===== */
const filterTang = ref([]);
const filterLoaiDatBan = ref([]);

// const danhSachBanFiltered = computed(() => {
//   return danhSachBan.value.filter((ban) => {
//     const matchTang =
//       !filterTang.value || ban.soTang == filterTang.value;

//     const matchLoaiDatBan =
//       filterLoaiDatBan.value === "" ||
//       ban.loaiDatBan === filterLoaiDatBan.value;
// console.log(typeof ban.loaiDatBan)
// console.log(typeof filterLoaiDatBan.value)

//     return matchTang && matchLoaiDatBan;
//   });
// });

const listTangOptions = computed(() => {
  return [...new Set(danhSachBan.value.map((b) => b.soTang))].map((t) => ({
    label: `Tầng ${t}`,
    value: String(t),
  }));
});
const listLoaiDatBanOptions = [
  { label: "Cho phép đặt online", value: "1" },
  { label: "Không cho phép", value: "0" },
];

const tangSearch = ref("");
const loaiSearch = ref("");

const tangFilteredOptions = computed(() => {
  return listTang.value.filter((t) => String(t).includes(tangSearch.value));
});
const loaiFilteredOptions = computed(() => {
  return listLoaiDatBan.filter((loai) =>
    loai.label.toLowerCase().includes(loaiSearch.value.toLowerCase()),
  );
});

const danhSachBanFiltered = computed(() => {
  return danhSachBan.value.filter((ban) => {
    /* ===== SEARCH ===== */
    const keyword = searchKeyword.value.toLowerCase().trim();
    const matchSearch =
      !keyword ||
      ban.maBan?.toLowerCase().includes(keyword) ||
      ban.tenKhuVuc?.toLowerCase().includes(keyword) ||
      String(ban.soCho).includes(keyword) ||
      String(ban.soTang).includes(keyword);

    /* ===== FILTER TẦNG ===== */
    // Vì mode="tags", filterTang.value là 1 mảng các string (VD: ["1", "2", "3"])
    const matchTang =
      !filterTang.value || 
      filterTang.value.length === 0 ||
      filterTang.value.includes(String(ban.soTang));

    /* ===== FILTER LOẠI ĐẶT BÀN ===== */
    // filterLoaiDatBan.value cũng là mảng các string (VD: ["0", "1"])
    const matchLoaiDatBan =
      !filterLoaiDatBan.value || 
      filterLoaiDatBan.value.length === 0 ||
      filterLoaiDatBan.value.includes(String(ban.loaiDatBan));

    return matchSearch && matchTang && matchLoaiDatBan;
  });
});

const searchKeyword = ref("");
const resetFilter = () => {
  filterTang.value = [];
  filterLoaiDatBan.value = [];
  searchKeyword.value = "";
};

watch(
  () => form.value.tang,
  (newTang) => {
    // ❗ chỉ reset khi người dùng đổi tầng
    if (newTang !== initialTang.value) {
      form.value.idKhuVuc = "";
    }
  },
);
watch(showUpdateModal, async (val) => {
  if (val) {
    await handleFetchAllKhuVuc();
  }
});

onMounted(() => {
  updateTime();
  timer = setInterval(updateTime, 1000);
  handleFetchAllKhuVuc();
});

onUnmounted(() => {
  clearInterval(timer);
});

onMounted(() => {
  fetchAllBan();
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
        <!-- Lọc tầng -->
        <!-- ===== FILTER TẦNG ===== -->
        <div class="filter-item">
          <label>Lọc theo tầng</label>
          <div class="multiselect-wrapper-sm">
            <Multiselect 
              v-model="filterTang" 
              :options="listTangOptions" 
              mode="tags" 
              :searchable="true" 
              :canClear="true" 
              placeholder="Chọn tầng..."
              no-results-text="Không tìm thấy kết quả nào" 
              class="custom-filter-multiselect" 
            /> 
          </div>
        </div>

        <div class="filter-item">
          <label>Lọc theo loại đặt bàn</label>
          <div class="multiselect-wrapper-sm">
            <Multiselect 
              v-model="filterLoaiDatBan" 
              :options="listLoaiDatBanOptions" 
              mode="tags" 
              :searchable="true" 
              :canClear="true" 
              placeholder="Chọn loại..."
              no-results-text="Không tìm thấy kết quả nào" 
              class="custom-filter-multiselect" 
            /> 
          </div>
        </div>
        <div>
          <br />
          <!-- Reset -->
          <button class="btn" @click="resetFilter">
            <i data-v-caf8f241="" class="fas fa-undo"></i>
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
            <!-- <th>Trạng thái</th> -->
            <!-- <th>Ngày tạo</th> -->
            <!-- <th>Người tạo</th> -->
            <!-- <th>Đặt online</th> -->
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(ban, index) in danhSachBanFiltered" :key="ban.id">
            <td>{{ index + 1 }}</td>
            <td>
              <strong>{{ ban.maBan }}</strong>
            </td>
            <td>{{ ban.soCho }} chỗ</td>
            <td>{{ ban.soTang }}</td>
            <td>{{ ban.tenKhuVuc }}</td>
            <!-- <td>
              <span
                :class="[
                  'badge',
                  String(ban.trangThai) === '0'
                    ? 'badge-empty'
                    : String(ban.trangThai) === '1'
                      ? 'badge-occupied'
                      : 'badge-booked',
                ]"
              >
                {{ getStatusText(ban.trangThai) }}
              </span>
            </td> -->
            <!-- <td>{{ formatDate(ban.ngayTao) }}</td> -->
            <!-- <td>{{ ban.nguoiTao }}</td> -->
            <!-- <td>
              {{ ban.loaiDatBan == 0 ? "Không cho phép" : "Cho phép" }}
            </td> -->

            <td>
              <button class="action-list" style="border: none; background-color: white"
                @click="handleActionWithAuth(() => openUpdateModal(ban.id), 'ADMIN')">
                <i class="fa-regular fa-pen-to-square action-icon"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
  <!-- POPUP SỬA BÀN -->
  <div v-if="showUpdateModal" class="modal-overlay">
    <div class="modal-box">
      <div class="modal-header">
        <h4>Sửa bàn ăn</h4>
        <button class="close-btn" @click="closeAddModal">&times;</button>
      </div>

      <div class="modal-body">
        <div class="form-group">
          <label>Mã bàn</label><br />
          <input type="text" v-model="form.maBan" />
        </div>

        <div class="form-group">
          <label>Số người tối đa</label><br />
          <input type="number" v-model="form.soNguoiToiDa" />
        </div>

        <!-- <div class="form-group">
          <label>Trạng thái</label><br />
          <input type="number" v-model="form.trangThai" />
        </div> -->
        <br />
        <!-- <select class="form-select" v-model.number="form.idKhuVuc">
          <option value="null" disabled>-- Chọn khu vực --</option>
          <option
            v-for="khuVuc in listKhuVuc"
            :key="khuVuc.id"
            :value="khuVuc.id"
          >
            {{ khuVuc.tenKhuVuc }}
          </option>
        </select> -->

        <div class="form-group">
          <label>Tầng</label>
          <select class="form-select" v-model="form.tang">
            <option value="" disabled>-- Chọn tầng --</option>
            <option v-for="tang in [...new Set(listKhuVuc.map((kv) => kv.tang))]" :key="tang" :value="tang">
              Tầng {{ tang }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>Khu vực</label>
          <select class="form-select" v-model="form.idKhuVuc" :disabled="!form.tang">
            <option value="" disabled>-- Chọn khu vực --</option>
            <option v-for="khuVuc in khuVucTheoTang" :key="khuVuc.id" :value="khuVuc.id">
              {{ khuVuc.tenKhuVuc }}
            </option>
          </select>
        </div>


        <!-- <div class="form-group">
          <label>Ngày tạo</label><br>
          <input type="text" v-model="form.ngayTao" />
        </div>

        <div class="form-group">
          <label>Người tạo</label><br>
          <input type="text" v-model="form.nguoiTao" />
        </div> -->
      </div>

      <div class="modal-footer">
        <button class="btn btn-outline" @click="closeAddModal">Hủy</button>
        <button class="btn btn-active" @click="submitUpdateBan">Lưu</button>
      </div>
    </div>
  </div>
</template>

<style src="@/pages/admin/table/tableManagementStyle.css"></style>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

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
    border-color: transparent !important; /* Xóa viền thừa */
}

/* Đảm bảo chữ luôn trắng kể cả khi trỏ chuột (hover) */
.btn:hover {
  transform: scale(1.04);
    color: white !important;
}

.dropdown-menu-custom {
  top: 100%;
  left: 0;
  position: absolute;
  width: 100%;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 8px;
  z-index: 999;
  max-height: 200px;
  overflow-y: auto;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);

  opacity: 0;
  transform: translateY(-6px);
  visibility: hidden;
  pointer-events: none;
  transition: all 0.18s ease;
}

.dropdown-wrapper:hover .dropdown-menu-custom {
  opacity: 1;
  transform: translateY(0);
  visibility: visible;
  pointer-events: auto;
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

/* ===== CUSTOM MULTISELECT ===== */

.multiselect-wrapper {
    width: 200px;
    /* Điều chỉnh độ rộng cho vừa */
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


/* Màu chữ khi placeholder hiển thị */

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

thead tr{
    border-radius: 15px;
    background-color: transparent !important;
}

thead tr th{
    padding-left: 20px;
    background-color: transparent !important;
    color: white;
}

tr td{
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

/* Body */
.table tbody tr:last-child td {
  border-bottom: none;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-box {
  background: #fff;
  width: 420px;
  border-radius: 12px;
  animation: fadeIn 0.25s ease;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header,
.modal-footer {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
}

.modal-footer {
  border-top: 1px solid #eee;
  border-bottom: none;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  border-bottom-left-radius: 12px;
  border-bottom-right-radius: 12px;
}

.modal-body {
  padding: 16px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
}

/* INPUT / SELECT */
.modal-body input,
.modal-body select {
  border-radius: 8px;
  border: 1px solid #ddd;
  padding: 8px 10px;
  font-size: 14px;
  transition: 0.2s;
}

.form-group input {
  width: 100%;
}

.modal-body input:focus,
.modal-body select:focus {
  outline: none;
  border-color: #7d161a;
  box-shadow: 0 0 0 2px rgba(125, 22, 26, 0.15);
}

.filter-box {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.filter-box select {
  width: 200px;
}

/* ===== CHECKBOX ===== */
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

/* ===== FOOTER ===== */
.modal-footer {
  background: #fafafa;
}

/* BUTTONS */
.btn {
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
  border-radius: 8px;
  font-size: 14px;
  color: white;
  cursor: pointer;
  transition: 0.2s;
}

.btn:hover {
  color:white;
}

.btn-outline {
  border: 1px solid #ccc;
  background: #fff;
}

.btn-outline:hover {
  background: #f1f1f1;
}

.btn-active {
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
  color: #fff;
  border: none;
}

/* ===== ANIMATION ===== */
@keyframes overlayFade {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
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
}

.multiselect-wrapper-sm {
    width: 250px;
}

/* Định dạng tổng thể */
.custom-filter-multiselect {
    --ms-border-color: #ddd;
    --ms-radius: 8px; 
    --ms-py: 6px; 
    
    /* Màu khi focus */
    --ms-ring-color: rgba(125, 22, 26, 0.15);
    --ms-border-color-active: #7d161a;
    
    /* Hover vào Option */
    --ms-option-bg-pointed: #fcf4f4;
    --ms-option-color-pointed: #7d161a;
    
    /* Option đã chọn (Màu đỏ giống ảnh 5) */
    --ms-option-bg-selected: #7d161a;
    --ms-option-color-selected: #ffffff;
    --ms-option-bg-selected-pointed: #5f0f12;
    
    /* TAG ĐÃ CHỌN (MÀU XANH LÁ GIỐNG ẢNH 6) */
    --ms-tag-bg: #7d161a; /* Đổi từ #38c172 thành #7d161a */
    --ms-tag-color: #ffffff;
    --ms-tag-radius: 4px;
    --ms-tag-font-weight: 500;
    
    /* Nút Xóa (Clear all) */
    --ms-clear-color: #999;
    --ms-clear-color-hover: #dc3545;
}

/* Sửa text bị lệch */
:deep(.multiselect-single-labelText),
:deep(.multiselect-placeholder) {
    font-size: 14px;
    color: #495057;
}

/* Icon mũi tên thả xuống */
:deep(.multiselect-caret) {
    background-color: #666;
}
:deep(.multiselect.is-active .multiselect-caret) {
    background-color: #7d161a;
}

</style>

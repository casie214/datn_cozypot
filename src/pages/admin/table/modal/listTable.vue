<script setup>
import {
  fetchAllBanAn,
  fetchAllKhuVuc,
  fetchBanAnById,
  updateBanAn,
} from "@/services/tableManageService";
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import dayjs from "dayjs";
/* 1. KHỞI TẠO TRẠNG THÁI */
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
const filterTang = ref("");
const filterLoaiDatBan = ref("");

const danhSachBanFiltered = computed(() => {
  return danhSachBan.value.filter((ban) => {
    const matchTang =
      !filterTang.value || ban.soTang == filterTang.value;

    const matchLoaiDatBan =
      filterLoaiDatBan.value === "" ||
      ban.loaiDatBan === filterLoaiDatBan.value;
console.log(typeof ban.loaiDatBan)
console.log(typeof filterLoaiDatBan.value)

    return matchTang && matchLoaiDatBan;
  });
});



watch(
  () => form.value.tang,
  (newTang) => {
    // ❗ chỉ reset khi người dùng đổi tầng
    if (newTang !== initialTang.value) {
      form.value.idKhuVuc = "";
    }
  },
);


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
  <!-- Lọc tầng -->
  <select v-model="filterTang" class="form-select">
    <option value="">-- Tất cả tầng --</option>
    <option
      v-for="tang in [...new Set(danhSachBan.map(b => b.soTang))]"
      :key="tang"
      :value="tang"
    >
      Tầng {{ tang }}
    </option>
  </select>

  <!-- Lọc đặt online -->
  <select v-model.number="filterLoaiDatBan" class="form-select">
    <option value="">-- Tất cả --</option>
    <option value="1">Cho phép đặt online</option>
    <option value="0">Không cho phép</option>
  </select>

  <!-- Reset -->
  <button class="btn" @click="() => {
    filterTang = '';
    filterLoaiDatBan = '';
  }">
    Reset
  </button>
</div>

    </div>
    <div class="list-card">
      <table class="table">
        <thead>
          <tr class="table-dark">
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
              <button
                class="action-list"
                style="border: none; background-color: white"
                @click="openUpdateModal(ban.id)"
              >
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
            <option
              v-for="tang in [...new Set(listKhuVuc.map((kv) => kv.tang))]"
              :key="tang"
              :value="tang"
            >
              Tầng {{ tang }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>Khu vực</label>
          <select
            class="form-select"
            v-model="form.idKhuVuc"
            :disabled="!form.tang"
          >
            <option value="" disabled>-- Chọn khu vực --</option>
            <option
              v-for="khuVuc in khuVucTheoTang"
              :key="khuVuc.id"
              :value="khuVuc.id"
            >
              {{ khuVuc.tenKhuVuc }}
            </option>
          </select>
        </div>
        <div class="form-check">
          <input
            class="form-check-input"
            type="checkbox"
            value=""
            v-model="form.loaiDatBan"
            :true-value="1"
            :false-value="0"
          />
          <label class="form-check-label" for="checkIndeterminate">
            Có thể đặt online
          </label>
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

.table {
  margin-bottom: 0 !important;
}

.table thead tr th {
  background-color: #7d161a;
}
.list-card {
  border-radius: 12px;
  overflow: hidden; /* QUAN TRỌNG */
  border: 1px solid #ddd;
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
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: 0.2s;
  border: 1px solid #5f0f12;
}

.btn:hover{
    border: 1px solid #5f0f12 !important;
}

.btn-outline {
  border: 1px solid #ccc;
  background: #fff;
}

.btn-outline:hover {
  background: #f1f1f1;
}

.btn-active {
  background: #7d161a;
  color: #fff;
  border: none;
}

.btn-active:hover {
  background: #5f0f12;
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
</style>

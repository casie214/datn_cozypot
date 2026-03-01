<script setup>
import "@vuepic/vue-datepicker/dist/main.css";
import { onMounted, ref, watch } from "vue";
// import utc from "dayjs/plugin/utc";
// import timezone from "dayjs/plugin/timezone";
import {
  createPhieuDatBanFullService,
  fetchAllBanAn,
  fetchAllCustomers,
  fetchTableStatusByDate,
  searchCustomerByPhone,
  searchDatBanService,
} from "@/services/tableManageService";
import Multiselect from "@vueform/multiselect";
import "@vueform/multiselect/themes/default.css";

// dayjs.extend(utc);
// dayjs.extend(timezone);

//---- --- Khai báo State ---
const listPhieuDatBan = ref([]);
const totalPages = ref(0);
const currentPage = ref(1); // Vue hiển thị từ trang 1
const pageSize = ref(6);

const searchForm = ref({
  soDienThoai: "",
  trangThai: "",
  ngayDat: "",
});
// Form tạo mới
const createForm = ref({
  idBanAn: "",
  idKhachHang: "",
  tenKhachHang: "",
  soDienThoai: "",
  email: "",
  ngaySinh: "",
  gioiTinh: true,
  thoiGianDat: "",
  soLuongKhach: 1,
  hinhThucDat: 1,
});
const showCreateModal = ref(false);
const listBanAn = ref([]);
const customerOptions = ref([]);
const selectedCustomer = ref(null);
const isOldCustomer = ref(false);

const allTables = ref([]);
const tableOptions = ref([]);
const selectedTable = ref(null);

const showCreateTableModal = ref(false);

const createFormBan = ref({
  idBanAn: null,
  soLuongKhach: 2,
});

const newTableForm = ref({
  tenBan: "",
  soNguoiToiDa: 4,
  tang: "",
  khuVuc: "",
});

const createBanAnService = async (data) => {
  return {
    ...data,
    idBanAn: Date.now(),
  };
};

const tableStatusMap = ref({}); // map: id → trạng thái theo ngày

const checkTablesByDate = async (thoiGian) => {
  if (!thoiGian) {
    tableStatusMap.value = {};
    return;
  }

  try {
    const data = await fetchTableStatusByDate(thoiGian);
    console.log("Data trả về:", data); // ← xem cấu trúc data
    console.log("allTables:", allTables.value); // ← xem value của từng bàn

    tableStatusMap.value = Object.fromEntries(data.map((b) => [b.banId, b]));

    console.log("tableStatusMap:", tableStatusMap.value); // ← xem map sau khi build
  } catch (err) {
    console.error("Lỗi check bàn theo ngày:", err);
  }
};

// ===== LOAD DATA =====
const loadTables = async () => {
  const data = await fetchAllBanAn();

  allTables.value = data.map((b) => ({
    value: b.id, // idBanAn → id
    label: `${b.maBan} - ${b.tenKhuVuc} (Tầng ${b.soTang})`,
    raw: b,
  }));

  tableOptions.value = allTables.value;
};

loadTables();

// ===== SEARCH =====
const searchTable = (query) => {
  if (!query) {
    tableOptions.value = allTables.value;
    return;
  }

  const q = query.toLowerCase();
  const filtered = allTables.value.filter(
    (t) =>
      t.raw.maBan?.toLowerCase().includes(q) ||
      t.raw.tenKhuVuc?.toLowerCase().includes(q) ||
      String(t.raw.soTang).includes(q),
  );

  if (filtered.length === 0) {
    tableOptions.value = [
      {
        value: "create-new",
        label: `+ Thêm bàn mới: ${query}`,
        isNew: true,
        name: query,
      },
    ];
  } else {
    tableOptions.value = filtered;
  }
};

// ===== SELECT =====
const handleSelectTable = (value) => {
  const option =
    allTables.value.find((o) => o.value === value) ||
    tableOptions.value.find((o) => o.value === value);

  if (!option) return;

  if (option.isNew) {
    newTableForm.value = {
      tenBan: option.name,
      soNguoiToiDa: createForm.value.soLuongKhach || 4,
      tang: "",
      khuVuc: "",
    };
    showCreateTableModal.value = true;
  } else {
    createForm.value.idBanAn = option.value; // lưu b.id vào form
  }
};

// ===== SAVE NEW TABLE =====
const saveNewTable = async () => {
  try {
    const created = await createBanAnService(newTableForm.value);

    const newOption = {
      value: created.idBanAn,
      label: `${created.tenBan} (Tầng ${created.tang} - KV ${created.khuVuc})`,
      raw: created,
    };

    allTables.value.push(newOption);
    tableOptions.value = allTables.value;

    selectedTable.value = created.idBanAn;
    createForm.value.idBanAn = created.idBanAn;

    showCreateTableModal.value = false;
  } catch (err) {
    alert("Tạo bàn thất bại");
  }
};

// --- Các hàm xử lý Logic ---

// ================= SEARCH =================
const buildSearchPayload = () => {
  const payload = {
    soDienThoai: searchForm.value.soDienThoai?.trim() || "",
    trangThai:
      searchForm.value.trangThai === "" ? null : searchForm.value.trangThai,
    ngayDat: null,
  };

  if (searchForm.value.ngayDat) {
    const d = new Date(searchForm.value.ngayDat);
    const offset = d.getTimezoneOffset();
    const localDate = new Date(d.getTime() - offset * 60 * 1000);
    payload.ngayDat = localDate.toISOString().split("T")[0];
  }

  return payload;
};

const searchDatBan = async () => {
  try {
    const data = await searchDatBanService({
      payload: buildSearchPayload(),
      page: currentPage.value - 1,
      size: pageSize.value,
    });

    listPhieuDatBan.value = data.content;
    totalPages.value = data.totalPages;
  } catch (error) {
    console.error("Lỗi searchDatBan:", error);
  }
};

// ================= OPEN MODAL =================
const openCreateModal = async () => {
  selectedCustomer.value = null;
  customerOptions.value = [];
  isOldCustomer.value = false;

  createForm.value = {
    idBanAn: "",
    idKhachHang: "",
    tenKhachHang: "",
    soDienThoai: "",
    email: "",
    ngaySinh: "",
    gioiTinh: true,
    thoiGianDat: "",
    soLuongKhach: 1,
    hinhThucDat: 1,
  };

  showCreateModal.value = true;

  // Load song song cho nhanh
  const [ban, _] = await Promise.all([fetchAllBanAn(), loadAllCustomers()]);
  listBanAn.value = ban;
};

// Thêm vào state
const allCustomers = ref([]);

// Thêm hàm load all
const loadAllCustomers = async () => {
  try {
    const data = await fetchAllCustomers(); // hoặc API của bạn
    allCustomers.value = data.map((c) => ({
      value: c.idKhachHang,
      label: `${c.tenKhachHang} - ${c.soDienThoai}`,
      raw: c,
    }));
    customerOptions.value = allCustomers.value;
  } catch (err) {
    console.error("Load customers error:", err);
  }
};

// ================= SEARCH CUSTOMER =================
const searchCustomer = async (query) => {
  if (!query || query.trim() === "") {
    customerOptions.value = allCustomers.value;
    return;
  }

  const q = query.toLowerCase();
  const filtered = allCustomers.value.filter(
    (c) =>
      c.raw.tenKhachHang?.toLowerCase().includes(q) ||
      c.raw.soDienThoai?.includes(q),
  );

  if (filtered.length === 0) {
    customerOptions.value = [
      {
        value: "create-new",
        label: `➕ Thêm khách mới: ${query}`,
        isNew: true,
        phone: query,
      },
    ];
  } else {
    customerOptions.value = filtered;
  }
};

const handleSelectCustomer = (value) => {
  // Multiselect trả về value (idKhachHang) hoặc object tùy cấu hình
  // Tìm lại option đầy đủ từ customerOptions
  const option = customerOptions.value.find((o) => o.value === value);
  if (!option) return;

  if (option.isNew) {
    isOldCustomer.value = false;
    createForm.value.idKhachHang = null;
    createForm.value.soDienThoai = option.phone;
    createForm.value.tenKhachHang = "";
    createForm.value.email = "";
    createForm.value.ngaySinh = "";
    createForm.value.gioiTinh = true;
    return;
  }

  const c = option.raw;
  isOldCustomer.value = true;
  createForm.value.idKhachHang = c.idKhachHang;
  createForm.value.tenKhachHang = c.tenKhachHang;
  createForm.value.soDienThoai = c.soDienThoai;
  createForm.value.email = c.email;
  createForm.value.ngaySinh = c.ngaySinh;
  createForm.value.gioiTinh = c.gioiTinh;
};

const clearCustomer = () => {
  isOldCustomer.value = false;
  selectedCustomer.value = null;
  createForm.value.idKhachHang = null;
};

// ================= SUBMIT =================
const submitCreate = async () => {
  try {
    const thoiGianDat =
      createForm.value.thoiGianDat.length === 16
        ? createForm.value.thoiGianDat + ":00"
        : createForm.value.thoiGianDat;

    const payload = { ...createForm.value, thoiGianDat };

    const res = await createPhieuDatBanFullService(payload);

    showCreateModal.value = false;
    await searchDatBan();

    // Hiện toast tuỳ theo có gửi mail không
    if (res.daGuiMail) {
      showToast(
        "success",
        "✅ Tạo phiếu thành công!",
        `Mail xác nhận đã gửi tới: ${res.emailGuiToi}`
      );
    } else {
      showToast(
        "warning",
        "✅ Tạo phiếu thành công!",
        "Khách không có email nên không gửi được xác nhận."
      );
    }
  } catch (error) {
    showToast(
      "error",
      "❌ Tạo phiếu thất bại",
      error.response?.data?.message || "Có lỗi xảy ra, vui lòng thử lại."
    );
  }
};
const closeCreateModal = () => {
  showCreateModal.value = false;
  selectedCustomer.value = null;
  customerOptions.value = [];
};

// ===== TOAST =====
const toast = ref({
  show: false,
  type: "success",   // "success" | "warning" | "error"
  title: "",
  message: "",
});

const showToast = (type, title, message, duration = 4000) => {
  toast.value = { show: true, type, title, message };
  setTimeout(() => {
    toast.value.show = false;
  }, duration);
};

watch(
  () => createForm.value.thoiGianDat,
  (val) => {
    if (val) checkTablesByDate(val);
  },
);

// --- Lifecycle ---
onMounted(() => {
  searchDatBan();
});
</script>

<template>
  <div class="layout-table">
    <div class="navbar-search m-4">
      <div class="header-manager">
        <div>
          <h3 style="color: #7d161a; font-weight: bold; font-size: 1.5rem">
            Quản lý phiếu đặt bàn
          </h3>
        </div>
        <div>
          <button class="btn" @click="openCreateModal">
            + Thêm phiếu đặt bàn
          </button>
        </div>
      </div>
      <!-- TAB -->
      <ul class="nav nav-tabs">
        <li class="nav-item">
          <router-link
            class="nav-link"
            exact-active-class="active"
            to="/admin/tables"
          >
            <i class="fa-solid fa-list"></i>
            Danh sách phiếu
          </router-link>
        </li>
        <li class="nav-item">
          <router-link
            class="nav-link"
            active-class="active"
            to="/admin/tables/calendar"
          >
            <i class="fa-regular fa-calendar"></i> Lịch đặt bàn
          </router-link>
        </li>
      </ul>
      <hr />
      <div class="contain-frame mt-3">
        <router-view />
      </div>
    </div>
  </div>
  <!-- MODAL -->
  <div v-if="showCreateModal" class="modal-overlay">
    <div class="modal-box">
      <h4>Thêm phiếu đặt bàn</h4>

      <!-- Multiselect tìm khách -->
      <label>Tìm khách theo SĐT</label>
      <Multiselect
        v-model="selectedCustomer"
        :options="customerOptions"
        :searchable="true"
        :filter-results="false"
        value-prop="value"
        label="label"
        track-by="value"
        placeholder="Tìm theo tên hoặc SĐT..."
        no-options-text="Không có khách hàng"
        no-results-text="Không tìm thấy"
        @open="
          () => {
            customerOptions = allCustomers;
          }
        "
        @search-change="searchCustomer"
        @select="handleSelectCustomer"
        @clear="clearCustomer"
      >
        <template #option="{ option }">
          <div v-if="option.isNew" style="color: #7d161a; font-weight: bold">
            {{ option.label }}
          </div>
          <div v-else>
            <span style="font-weight: 600">{{ option.raw.tenKhachHang }}</span>
            <span style="color: #888; margin-left: 8px">{{
              option.raw.soDienThoai
            }}</span>
          </div>
        </template>
      </Multiselect>
      <div class="info-grid">
        <div>
          <label>Tên khách</label>
          <input
            v-model="createForm.tenKhachHang"
            :disabled="isOldCustomer"
            placeholder="Tên khách"
          />
        </div>

        <div>
          <label>Số điện thoại</label>
          <input
            v-model="createForm.soDienThoai"
            :disabled="isOldCustomer"
            placeholder="SĐT"
          />
        </div>

        <div>
          <label>Email</label>
          <input
            v-model="createForm.email"
            :disabled="isOldCustomer"
            placeholder="Email"
          />
        </div>

        <div>
          <label>Ngày sinh</label>
          <input
            type="date"
            v-model="createForm.ngaySinh"
            :disabled="isOldCustomer"
          />
        </div>

        <div>
          <label>Giới tính</label>
          <select v-model="createForm.gioiTinh" :disabled="isOldCustomer">
            <option :value="true">Nam</option>
            <option :value="false">Nữ</option>
          </select>
        </div>
        <div>
          <label>Chọn bàn</label>
          <Multiselect
            v-model="selectedTable"
            :options="tableOptions"
            :searchable="true"
            :filter-results="false"
            value-prop="value"
            label="label"
            track-by="value"
            placeholder="Tìm hoặc thêm bàn..."
            no-options-text="Không có bàn"
            no-results-text="Không tìm thấy"
            @open="
              () => {
                tableOptions = allTables;
              }
            "
            @search-change="searchTable"
            @select="handleSelectTable"
          >
            <template #option="{ option }">
              <div
                v-if="option.isNew"
                style="color: #7d161a; font-weight: bold"
              >
                {{ option.label }}
              </div>
              <div v-else-if="option.raw">
                <div style="font-weight: 600">{{ option.raw.maBan }}</div>
                <div style="font-size: 12px; color: #777">
                  {{ option.raw.tenKhuVuc }} · Tầng {{ option.raw.soTang }} ·
                  {{ option.raw.soCho }} chỗ
                  <span
                    :style="{
                      color: tableStatusMap[String(option.value)]
                        ? tableStatusMap[String(option.value)].trangThai === 2
                          ? '#e74c3c'
                          : '#27ae60'
                        : '#aaa',
                      marginLeft: '6px',
                    }"
                  >
                    ●
                    {{
                      tableStatusMap[String(option.value)]
                        ? tableStatusMap[String(option.value)].trangThai === 2
                          ? "Đã đặt"
                          : "Còn trống"
                        : "Còn trống"
                    }}
                  </span>
                </div>
              </div>
            </template>
          </Multiselect>
        </div>

        <div>
          <label>Thời gian đặt</label>
          <input type="datetime-local" v-model="createForm.thoiGianDat" />
        </div>

        <div>
          <label>Số lượng khách</label>
          <input type="number" min="1" v-model="createForm.soLuongKhach" />
        </div>
      </div>

      <div style="text-align: right; margin-top: 20px">
        <button class="btn btn-custom-outline" @click="closeCreateModal">
          Hủy
        </button>
        <button class="btn ms-2" @click="submitCreate">Lưu</button>
      </div>
    </div>
  </div>
  <!-- POPUP THÊM BÀN -->
  <div v-if="showCreateTableModal" class="modal-overlay">
    <div class="modal-box" style="width: 500px">
      <h4>Thêm bàn mới</h4>

      <div class="info-grid">
        <input v-model="newTableForm.tenBan" placeholder="Tên bàn" />
        <input
          type="number"
          v-model.number="newTableForm.soNguoiToiDa"
          placeholder="Sức chứa"
        />
        <input v-model="newTableForm.tang" placeholder="Tầng" />
        <input v-model="newTableForm.khuVuc" placeholder="Khu vực" />
      </div>

      <div style="text-align: right; margin-top: 20px">
        <button
          class="btn btn-custom-outline"
          @click="
            () => {
              showCreateTableModal = false;
              selectedTable = null;
            }
          "
        >
          Hủy
        </button>
        <button class="btn ms-2" @click="saveNewTable">Lưu</button>
      </div>
    </div>
  </div>
  <!-- ===== TOAST NOTIFICATION ===== -->
<Transition name="toast">
  <div v-if="toast.show" :class="['toast-noti', `toast-${toast.type}`]">
    <div class="toast-icon">
      <span v-if="toast.type === 'success'">✅</span>
      <span v-else-if="toast.type === 'warning'">⚠️</span>
      <span v-else>❌</span>
    </div>
    <div class="toast-content">
      <div class="toast-title">{{ toast.title }}</div>
      <div class="toast-msg">{{ toast.message }}</div>
    </div>
    <button class="toast-close" @click="toast.show = false">×</button>
  </div>
</Transition>
</template>

<style>
/* ================= LAYOUT ================= */
.layout-table {
  display: flex;
  background: #fff;
}

.navbar-search {
  width: 100%;
}

.header-manager {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

hr {
  border: 0;
  border-top: 2px solid #7d161a;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-box {
  width: 700px;
  background: #fff;
  border-radius: 15px;
  padding: 25px;
  animation: fadeIn 0.2s ease-in-out;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.close-btn {
  border: none;
  background: none;
  font-size: 22px;
  cursor: pointer;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* ================= NAV TABS ================= */
.nav-tabs .nav-link {
  color: #7d161a !important;
  font-weight: 600;
  border-radius: 10px 10px 0 0;
  transition: 0.2s;
}

.nav-tabs .nav-link:hover {
  background: #fcf4f4;
  color: #5a0f12 !important;
}

.nav-tabs .nav-link.active {
  font-weight: bold;
  background: #fff;
  border-color: #dee2e6 #dee2e6 #fff;
}

/* ================= FORM & TABLE ================= */
.search-form {
  border: 1px solid #cacaca;
  border-radius: 15px;
  padding: 2%;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
}

.table-container {
  border: 1px solid #dee2e6;
  border-radius: 15px;
  overflow: hidden;
}

.table thead th {
  background: #7d161a !important;
  color: #fff;
  font-weight: 600;
}

/* ================= BUTTON ================= */
.btn {
  background: #7d161a;
  color: #fff;
  border: none;
  transition: 0.2s;
}

.btn:hover {
  background: #5a1013;
}

/* Outline Button */
.btn-custom-outline {
  background: transparent !important;
  color: #7d161a !important;
  border: 1px solid #7d161a !important;
  transition: 0.2s;
}

.btn-custom-outline:hover {
  background: #7d161a !important;
  color: #fff !important;
}

/* ================= PAGINATION ================= */
.pagination .page-link {
  color: #7d161a;
  transition: 0.2s;
}

.pagination .page-link:hover {
  background: #f8ecec;
  color: #5a1013;
}

.pagination .page-item.active .page-link {
  background: #7d161a !important;
  border-color: #7d161a !important;
  color: #fff !important;
}

.pagination .page-link:focus {
  box-shadow: 0 0 0 0.2rem rgba(125, 22, 26, 0.25);
}

/* ================= GRID FORM ================= */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin: 18px 0;
}

.info-grid label {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
}

.info-grid input,
.info-grid select {
  width: 100%;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid #ddd;
  transition: 0.2s;
}

.info-grid input:focus,
.info-grid select:focus {
  border-color: #7d161a;
  box-shadow: 0 0 0 2px rgba(125, 22, 26, 0.15);
  outline: none;
}

/* ================= MULTISELECT FIX ================= */
.info-grid .multiselect {
  border-radius: 8px;
  border: 1px solid #ddd;
}

.info-grid .multiselect.is-active {
  border-color: #7d161a;
  box-shadow: 0 0 0 2px rgba(125, 22, 26, 0.15);
}

.info-grid .multiselect-wrapper {
  border: none !important;
  box-shadow: none !important;
}

.info-grid .multiselect-input {
  border: none !important;
  box-shadow: none !important;
}

/* ================= BÀN CARD ================= */
.ban-grid {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin: 20px 0;
}

.ban-card {
  width: 150px;
  height: 90px;
  border: 2px solid #ccc;
  border-radius: 10px;
  text-align: center;
  padding-top: 18px;
  cursor: pointer;
  transition: 0.25s;
}

.ban-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.ban-card.selected {
  border-color: #7d161a;
  background: #f4eaea;
}

.ban-card.disabled {
  opacity: 0.4;
  pointer-events: none;
}

.ban-name {
  font-weight: bold;
  font-size: 17px;
}

.ban-size {
  margin-top: 4px;
}

.suggest {
  position: absolute;
  top: 6px;
  right: 10px;
  color: #7d161a;
}

/* ================= CHECKBOX TOGGLE ================= */
.checkbox-wrapper-5 .check {
  --size: 24px;
  position: relative;
  font-size: var(--size);
}

.checkbox-wrapper-5 input[type="checkbox"] {
  appearance: none;
  width: calc(2.2 * var(--size));
  height: var(--size);
  background: #d7d7d7;
  border-radius: var(--size);
  position: relative;
  cursor: pointer;
  transition: 0.3s;
}

.checkbox-wrapper-5 input[type="checkbox"]:checked {
  background: linear-gradient(90deg, #7d161a, #dc2f0b);
}

/* ================= TOAST ================= */
.toast-noti {
  position: fixed;
  bottom: 28px;
  right: 28px;
  z-index: 9999;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-width: 320px;
  max-width: 420px;
  padding: 16px 18px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  background: #fff;
  border-left: 5px solid #ccc;
}

.toast-success { border-left-color: #27ae60; }
.toast-warning { border-left-color: #f39c12; }
.toast-error   { border-left-color: #e74c3c; }

.toast-icon {
  font-size: 22px;
  line-height: 1;
  margin-top: 2px;
}

.toast-content {
  flex: 1;
}

.toast-title {
  font-weight: 700;
  font-size: 14px;
  color: #222;
  margin-bottom: 4px;
}

.toast-msg {
  font-size: 13px;
  color: #555;
  line-height: 1.5;
}

.toast-close {
  background: none;
  border: none;
  font-size: 20px;
  color: #aaa;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  margin-top: -2px;
}

.toast-close:hover { color: #555; }

/* Animation */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(40px);
}

</style>

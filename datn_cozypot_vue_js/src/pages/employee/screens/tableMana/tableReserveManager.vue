<script setup>
import "@vuepic/vue-datepicker/dist/main.css";
import { onMounted, ref } from "vue";
// import utc from "dayjs/plugin/utc";
// import timezone from "dayjs/plugin/timezone";
import {
  searchDatBanService,
} from "@/services/tableManageService";

// dayjs.extend(utc);
// dayjs.extend(timezone);

// --- Khai báo State ---
const listPhieuDatBan = ref([]);
const totalPages = ref(0);
const currentPage = ref(1); // Vue hiển thị từ trang 1
const pageSize = ref(6);

const searchForm = ref({
  soDienThoai: "",
  trangThai: "",
  ngayDat: "",
});



// --- Các hàm xử lý Logic ---

// 1. Hàm tìm kiếm và phân trang (POST)
const buildSearchPayload = () => {
  const payload = {
    soDienThoai: searchForm.value.soDienThoai || null,
    trangThai: searchForm.value.trangThai || null,
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
    const payload = buildSearchPayload();

    const data = await searchDatBanService({
      payload,
      page: currentPage.value - 1,
      size: pageSize.value,
    });

    listPhieuDatBan.value = data.content;
    totalPages.value = data.totalPages;
  } catch (error) {
    console.error("Lỗi searchDatBan:", error);
  }
};




// --- Lifecycle ---
onMounted(() => {
  searchDatBan();
});
</script>

<template>
  <div class="layout-table">
    <div class="navbar-search m-4">
      <h3 style="color: #7d161a; font-weight: bold; font-size: 1.5rem">
        Quản lý phiếu đặt bàn
      </h3>
      <!-- TAB -->
      <ul class="nav nav-tabs">
        <li class="nav-item">
          <router-link
            class="nav-link"
            exact-active-class="active"
            to="/manage/table"
          >
            <i class="fa-solid fa-list"></i>
            Danh sách phiếu
          </router-link>
        </li>
        <li class="nav-item">
          <router-link
            class="nav-link"
            active-class="active"
            to="/manage/table/calendar"
          >
            <i class="fa-regular fa-calendar"></i>
            Lịch đặt bàn
          </router-link>
        </li>
      </ul>
      <hr />

      <div class="contain-frame mt-3">
        <router-view />
        <!-- nội dung đổi ở đây -->
      </div>
    </div>
  </div>
</template>

<style>
.layout-table {
  display: flex;
  background-color: white;
}

.search-form {
  border: solid 1px #cacaca;
  border-radius: 15px;
  padding: 2%;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.navbar-search {
  width: 100%;
}

hr {
  border: 0;
  border-top: 2px solid #7d161a;
  /* Chỉnh độ dày ở đây */
}

.btn {
  background-color: #7d161a;
  color: white;
}

.table-container {
  border: 1px solid #dee2e6;
  /* Viền bao ngoài */
  border-radius: 15px;
  /* Độ bo góc bạn muốn */
  overflow: hidden;
  /* Quan trọng: Cắt các góc nhọn của header/footer bên trong */
}

.table thead tr th {
  background-color: #7d1619 !important;
  color: white;
  font-weight: 600;
}

/* From Uiverse.io by Shoh2008 */
.checkbox-wrapper-5 .check {
  --size: 25px;
  position: relative;
  background: linear-gradient(90deg, #7d161a, #dc2f0b);
  line-height: 0;
  perspective: 400px;
  font-size: var(--size);
}

.checkbox-wrapper-5 .check input[type="checkbox"],
.checkbox-wrapper-5 .check label,
.checkbox-wrapper-5 .check label::before,
.checkbox-wrapper-5 .check label::after,
.checkbox-wrapper-5 .check {
  appearance: none;
  display: inline-block;
  border-radius: var(--size);
  border: 0;
  transition: 0.35s ease-in-out;
  box-sizing: border-box;
  cursor: pointer;
}

.checkbox-wrapper-5 .check label {
  width: calc(2.2 * var(--size));
  height: var(--size);
  background: #d7d7d7;
  overflow: hidden;
}

.checkbox-wrapper-5 .check input[type="checkbox"] {
  position: absolute;
  z-index: 1;
  width: calc(0.8 * var(--size));
  height: calc(0.8 * var(--size));
  top: calc(0.1 * var(--size));
  left: calc(0.1 * var(--size));
  background: linear-gradient(45deg, #dedede, #ffffff);
  box-shadow: 0 6px 7px rgba(0, 0, 0, 0.3);
  outline: none;
  margin: 0;
}

.checkbox-wrapper-5 .check input[type="checkbox"]:checked {
  left: calc(1.3 * var(--size));
}

.checkbox-wrapper-5 .check input[type="checkbox"]:checked + label {
  background: transparent;
}

.checkbox-wrapper-5 .check label::before,
.checkbox-wrapper-5 .check label::after {
  content: "· ·";
  position: absolute;
  overflow: hidden;
  left: calc(0.15 * var(--size));
  top: calc(0.5 * var(--size));
  height: var(--size);
  letter-spacing: calc(-0.04 * var(--size));
  color: #9b9b9b;
  font-family: "Times New Roman", serif;
  z-index: 2;
  font-size: calc(0.6 * var(--size));
  border-radius: 0;
  transform-origin: 0 0 calc(-0.5 * var(--size));
  backface-visibility: hidden;
}

.checkbox-wrapper-5 .check label::after {
  content: "●";
  top: calc(0.65 * var(--size));
  left: calc(0.2 * var(--size));
  height: calc(0.1 * var(--size));
  width: calc(0.35 * var(--size));
  font-size: calc(0.2 * var(--size));
  transform-origin: 0 0 calc(-0.4 * var(--size));
}

.checkbox-wrapper-5 .check input[type="checkbox"]:checked + label::before,
.checkbox-wrapper-5 .check input[type="checkbox"]:checked + label::after {
  left: calc(1.55 * var(--size));
  top: calc(0.4 * var(--size));
  line-height: calc(0.1 * var(--size));
  transform: rotateY(360deg);
}

.checkbox-wrapper-5 .check input[type="checkbox"]:checked + label::after {
  height: calc(0.16 * var(--size));
  top: calc(0.55 * var(--size));
  left: calc(1.6 * var(--size));
  font-size: calc(0.6 * var(--size));
  line-height: 0;
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
  width: 720px;
  background: #fff;
  border-radius: 10px;
  padding: 20px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  color: #8b1d2c;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin: 16px 0;
}

.info-grid label {
  color: #777;
  display: block;
}

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
  cursor: pointer;
  position: relative;
  padding-top: 18px;

  /* 👇 hiệu ứng mượt */
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease,
    background-color 0.25s ease,
    border-color 0.25s ease;
}

/* Hover nhẹ */
.ban-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.ban-card.selected {
  border-color: #8b1d2c;
  background: #f4eaea;
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease,
    background-color 0.25s ease,
    border-color 0.25s ease;
}

.ban-card.disabled {
  opacity: 0.4;
  pointer-events: none;
}

.ban-name {
  font-weight: bold;
  font-size: 18px;
}

.ban-size {
  margin-top: 6px;
}

.suggest {
  position: absolute;
  top: 6px;
  right: 10px;
  color: #8b1d2c;
}

.modal-footer {
  display: flex;
  justify-content: space-between;
}

.btn-cancel {
  background: #eee;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
}

.btn-confirm {
  background: #8b1d2c;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
}

.btn-confirm:disabled {
  opacity: 0.6;
}

.btn-confirm:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease,
    background-color 0.25s ease,
    border-color 0.25s ease;
}

.btn-custom-outline {
  background-color: transparent !important; /* Ép không có màu nền mặc định */
  color: #7d161a !important;
  border: 1px solid #7d161a !important;
}

.btn-custom-outline:hover {
  background-color: #7d161a !important; /* Chỉ đỏ khi hover */
  color: #ffffff !important;
}

/* Màu cho nút đang chọn (Active) */
.pagination .page-item.active .page-link {
  background-color: #7d161a !important;
  border-color: #7d161a !important;
  color: #ffffff !important;
}

/* Màu chữ cho các nút bình thường */
.pagination .page-link {
  color: #7d161a;
}

/* Hiệu ứng khi di chuột qua các nút */
.pagination .page-link:hover {
  background-color: #f8ecec;
  color: #5a1013;
  border-color: #dee2e6;
}

/* Xóa viền xanh khi click (box-shadow) */
.pagination .page-link:focus {
  box-shadow: 0 0 0 0.2rem rgba(125, 22, 26, 0.25);
}
</style>

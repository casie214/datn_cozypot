<script setup>
import { VueDatePicker } from "@vuepic/vue-datepicker";
import "@vuepic/vue-datepicker/dist/main.css";
import { onMounted, ref } from "vue";
import dayjs from "dayjs";
import utc from "dayjs/plugin/utc";
import timezone from "dayjs/plugin/timezone";
import { fetchAllBanAn } from "@/services/tableManageService";

dayjs.extend(utc);
dayjs.extend(timezone);


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

const trangThaiList = [
  { value: "0", label: "Chờ xác nhận" },
  { value: "1", label: "Đã xác nhận" },
  { value: "2", label: "Đã hủy" },
];

const selectedPhieu = ref(null);
const selectedBan = ref(null);
const showModal = ref(false);
const danhSachBan = ref([]);

// --- Các hàm xử lý Logic ---

// 1. Hàm tìm kiếm và phân trang (POST)
const searchDatBan = async () => {
  try {
    const payload = {
      soDienThoai: searchForm.value.soDienThoai || null,
      trangThai: searchForm.value.trangThai || null,
      ngayDat: null
    };

    // Xử lý ngày tháng về định dạng yyyy-MM-dd để tránh lỗi 403/500 do sai format
    if (searchForm.value.ngayDat) {
      const d = new Date(searchForm.value.ngayDat);
      const offset = d.getTimezoneOffset();
      const localDate = new Date(d.getTime() - (offset * 60 * 1000));
      payload.ngayDat = localDate.toISOString().split('T')[0];
    }

    const response = await fetch(
      `http://localhost:8080/dat-ban/search?page=${currentPage.value - 1}&size=${pageSize.value}`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      }
    );

    if (!response.ok) throw new Error("Lỗi khi tìm kiếm");

    const data = await response.json();
    listPhieuDatBan.value = data.content;
    totalPages.value = data.totalPages;
  } catch (error) {
    console.error("Lỗi searchDatBan:", error);
  }
};


// 2. Hàm chuyển trang
const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  searchDatBan();
};

// 3. Xử lý modal và xếp bàn
const openChonBanModal = async (phieu) => {
  selectedPhieu.value = phieu;
  try {
    danhSachBan.value = await fetchAllBanAn();
    showModal.value = true;
  } catch (error) {
    console.error("Lỗi lấy danh sách bàn:", error);
  }
};

const closeModal = () => {
  showModal.value = false;
  selectedBan.value = null;
};

const chonBan = (ban) => {
  selectedBan.value = ban;
};

const updatePhieuDatBan = async () => {
  if (!selectedBan.value || !selectedPhieu.value) return;

  const payload = {
    id: selectedPhieu.value.idDatBan,
    idBanAn: selectedBan.value.id,
  };

  try {
    const res = await fetch("http://localhost:8080/dat-ban/update", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    if (!res.ok) throw new Error("Cập nhật thất bại");

    alert("Xếp bàn thành công!");
    closeModal();
    await searchDatBan(); // Load lại danh sách tại trang hiện tại
  } catch (err) {
    console.error(err);
    alert("Có lỗi xảy ra khi xếp bàn");
  }
};

const formatDate = (time) =>
  dayjs.utc(time).format("DD/MM/YYYY HH:mm");



// --- Lifecycle ---
onMounted(() => {
  searchDatBan();
});
</script>

<template>
  <div class="layout-table">
 

    <div class="navbar-search m-4">
      <h3 style="color: #7d161a; font-weight: bold; font-size: 1.5rem;">Quản lý phiếu đặt bàn</h3>
      <ul class="nav nav-tabs">
        <li class="nav-item">
          <a
            class="nav-link active"
            style="color: #7d161a"
            aria-current="page"
            href="#"
          >
            <i class="fa-solid fa-list"></i>
            Danh sách</a
          >
        </li>
        <li class="nav-item">
          <a class="nav-link" style="color: black" aria-current="page" href="">
            <i class="fa-regular fa-calendar"></i>
            Lịch đặt bàn</a
          >
        </li>
      </ul>
      <hr />
      <div class="container search-form">
        Tìm kiếm
        <form class="d-flex gap-2 mt-2" role="search">
          <input
            class="form-control"
            type="search"
            placeholder="Số điện thoại khách hàng"
            style="width: 30%"
            v-model="searchForm.soDienThoai"
          />
          <select
            v-model="searchForm.trangThai"
            class="form-select"
            style="width: 25%"
          >
            <option value="" disabled>Trạng thái</option>
            <option
              v-for="item in trangThaiList"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>

          <div style="width: 30%">
            <VueDatePicker
              v-model="searchForm.ngayDat"
              placeholder="Chọn ngày"
              auto-apply
              :enable-time-picker="false"
            />
          </div>

          <button class="btn text-nowrap" type="button" @click="searchDatBan()">
            <i class="fa-solid fa-magnifying-glass"></i> Tìm kiếm
          </button>
        </form>
      </div>
      <div class="mt-3">
        <div class="table-container">
          <table class="table">
            <thead>
              <tr>
                <th scope="col">STT</th>
                <th scope="col">Mã</th>
                <th scope="col">Khách hàng</th>
                <th scope="col">SDT</th>
                <th scope="col">Bàn</th>
                <th scope="col">Tầng</th>
                <th scope="col">Ngày giờ</th>
                <th scope="col">Số người</th>
                <th scope="col">Trạng thái</th>
                <th scope="col">Hành động</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(phieuDatBan,index) in listPhieuDatBan" :key="phieuDatBan.id">
                
                <th>{{ (currentPage - 1) * pageSize + index + 1 }}</th>
                <td>{{ phieuDatBan.maDatBan }}</td>
                <td>{{ phieuDatBan.tenKhachHang }}</td>
                <td>{{ phieuDatBan.soDienThoai }}</td>
                <td>
                  <!-- Nếu đã có bàn -->
                  <span v-if="phieuDatBan.maBan">
                    {{ phieuDatBan.maBan }}
                  </span>

                  <!-- Nếu chưa có bàn -->
                  <button
                    v-else
                    class="btn btn-sm btn-custom-outline"
                    @click="openChonBanModal(phieuDatBan)"
                  >
                    Chọn bàn
                  </button>
                </td>

                <td>
                  {{
                    phieuDatBan.soTang == null ? "Trống" : phieuDatBan.soTang
                  }}
                </td>
                <td>{{ formatDate(phieuDatBan.thoiGianDat) }}</td>
                <td>{{ phieuDatBan.soLuongKhach }}</td>
                <td>{{ phieuDatBan.tenTrangThai }}</td>
                <td>
                  <div class="action-groups d-flex align-items-center gap-3">
                    <i class="fa-solid fa-eye"></i>

                    <div class="checkbox-wrapper-5">
                      <div class="check">
                        <input
                          :id="'check-' + phieuDatBan.id"
                          type="checkbox"
                          checked
                        />
                        <label :for="'check-' + phieuDatBan.id"></label>
                      </div>
                    </div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="pagination mt-3 d-flex justify-content-center">
  <nav>
    <ul class="pagination">
      <li
        class="page-item"
        :class="{ disabled: currentPage === 1 }"
      >
        <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)">
          &laquo;
        </a>
      </li>

      <li
        v-for="page in totalPages"
        :key="page"
        class="page-item"
        :class="{ active: page === currentPage }"
      >
        <a class="page-link" href="#" @click.prevent="changePage(page)">
          {{ page }}
        </a>
      </li>

      <li
        class="page-item"
        :class="{ disabled: currentPage === totalPages }"
      >
        <a class="page-link" href="#" @click.prevent="changePage(currentPage + 1)">
          &raquo;
        </a>
      </li>
    </ul>
  </nav>
</div>

    </div>
  </div>

  <!--POPUP XẾP BÀN-->
  <div v-if="showModal" class="modal-overlay">
    <div class="modal-box">
      <!-- Header -->
      <div class="modal-header">
        <h3>Xếp bàn</h3>
        <button class="close-btn" @click="closeModal">✕</button>
      </div>
      <hr />
      <!-- Thông tin -->
      <div class="info-grid">
        <div>
          <label>Khách hàng:</label>
          <strong>{{ selectedPhieu?.tenKhachHang }}</strong>
        </div>
        <div>
          <label>Số điện thoại:</label>
          <strong>{{ selectedPhieu?.soDienThoai }}</strong>
        </div>
        <div>
          <label>Số người:</label>
          <strong>{{ selectedPhieu?.soLuongKhach }} người</strong>
        </div>
        <div>
          <label>Ngày và giờ:</label>
          <strong>{{ selectedPhieu?.thoiGianDat }}</strong>
        </div>
      </div>

      <hr />

      <!-- Danh sách bàn (demo) -->
      <!-- Danh sách bàn (DYNAMIC) -->
      <div class="ban-grid">
        <div
          v-for="ban in danhSachBan"
          :key="ban.id"
          class="ban-card"
          :class="{
            selected: selectedBan?.id === ban.id,
            disabled: ban.soCho < selectedPhieu.soLuongKhach,
          }"
          @click="chonBan(ban)"
        >
          <!-- ⭐ icon khi được chọn -->
          <i
            v-if="selectedBan?.id === ban.id"
            class="fa-solid fa-star suggest"
          ></i>

          <div class="ban-name">{{ ban.maBan }}</div>
          <div class="ban-size">({{ ban.soCho }} chỗ)</div>
        </div>
      </div>

      <hr />

      <!-- Footer -->
      <div class="modal-footer">
        <button class="btn-cancel" @click="closeModal">Hủy</button>
        <button
          class="btn-confirm"
          :disabled="!selectedBan"
          @click="updatePhieuDatBan"
        >
          Xác nhận xếp bàn
        </button>
      </div>
    </div>
  </div>
  <!-- 🔥 HẾT POPUP 🔥 -->
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
  transition: transform 0.25s ease, box-shadow 0.25s ease,
    background-color 0.25s ease, border-color 0.25s ease;
}

/* Hover nhẹ */
.ban-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.ban-card.selected {
  border-color: #8b1d2c;
  background: #f4eaea;
  transition: transform 0.25s ease, box-shadow 0.25s ease,
    background-color 0.25s ease, border-color 0.25s ease;
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
  transition: transform 0.25s ease, box-shadow 0.25s ease,
    background-color 0.25s ease, border-color 0.25s ease;
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

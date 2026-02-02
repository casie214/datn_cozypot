<script setup>
import "@vuepic/vue-datepicker/dist/main.css";
import { computed, onMounted, ref } from "vue";
import dayjs from "dayjs";
// import utc from "dayjs/plugin/utc";
// import timezone from "dayjs/plugin/timezone";
import {
  fetchAllBanAn,
  searchDatBanService,
  updatePhieuDatBanService,
  updateTTPhieuDatBan,
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
const buildSearchPayload = () => {
  let ngayDat = null;

  if (searchForm.value.ngayDat) {
    // logic giống format("YYYY-MM-DD")
    ngayDat = dayjs(searchForm.value.ngayDat).format("YYYY-MM-DD");
  }

  return {
    soDienThoai: searchForm.value.soDienThoai || null,
    trangThai: searchForm.value.trangThai || null,
    ngayDat, // yyyy-MM-dd
  };
};

const searchDatBan = async () => {
  try {
    const payload = buildSearchPayload();
    console.log(buildSearchPayload());

    const data = await searchDatBanService({
      payload,
      page: currentPage.value - 1,
      size: pageSize.value,
    });

    listPhieuDatBan.value = data.content;
    listPhieuDatBanFiltered.value = data.content; 
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
    await updatePhieuDatBanService(payload);

    alert("Xếp bàn thành công!");
    closeModal();
    await searchDatBan(); // Load lại danh sách tại trang hiện tại
  } catch (err) {
    console.error(err);
    alert("Có lỗi xảy ra khi xếp bàn");
  }
};

const formatDate = (time) => {
  if (!time) return "";
  return dayjs(time).format("DD/MM/YYYY HH:mm");
};

const onToggleCheckIn = async (phieu) => {
  // chỉ xử lý khi đang = 1
  if (phieu.trangThai !== 1) return;

  try {
    await updateTTPhieuDatBan(phieu.idDatBan, 2);
    phieu.trangThai = 2; // sync UI
  } catch (err) {
    console.error("Check-in thất bại", err);
  }
};
const listPhieuDatBanFiltered = ref([]); // data sau khi bấm tìm kiếm



const onSearchClick = () => {
  listPhieuDatBanFiltered.value = listPhieuDatBan.value.filter((phieu) => {
    // SDT
    const matchPhone =
      !searchForm.value.soDienThoai ||
      phieu.soDienThoai?.includes(searchForm.value.soDienThoai);

    // Trạng thái
    const matchTrangThai =
      !searchForm.value.trangThai ||
      String(phieu.trangThai) === String(searchForm.value.trangThai);

    // Ngày (logic GIỐNG cái mày đưa)
    let matchDate = true;
    if (searchForm.value.ngayDat) {
      matchDate =
        dayjs(phieu.thoiGianDat).format("YYYY-MM-DD") ===
        searchForm.value.ngayDat;
    }

    return matchPhone && matchTrangThai && matchDate;
  });
};




// --- Lifecycle ---
onMounted(() => {
  searchDatBan();
});
</script>

<template>
  <div class="layout-table">
    <div class="navbar-search m-4">
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
            <input
              type="date"
              v-model="searchForm.ngayDat"
              class="form-control"
            />
          </div>

          <button class="btn text-nowrap" type="button" @click="onSearchClick()">
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
              <tr
                v-for="(phieuDatBan, index) in listPhieuDatBanFiltered"
                :key="phieuDatBan.id"
              >
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
                      <!-- From Uiverse.io by JaydipPrajapati1910 -->
                      <label class="switch">
                        <input
                          type="checkbox"
                          :checked="phieuDatBan.trangThai === 1"
                          :disabled="phieuDatBan.trangThai !== 1"
                          @click.prevent="onToggleCheckIn(phieuDatBan)"
                        />

                        <div class="slider">
                          <div class="circle">
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              version="1.1"
                              xmlns:xlink="http://www.w3.org/1999/xlink"
                              width="6"
                              height="6"
                              x="0"
                              y="0"
                              viewBox="0 0 365.696 365.696"
                              style="enable-background: new 0 0 512 512"
                              xml:space="preserve"
                              class="cross"
                            >
                              <g>
                                <path
                                  d="M243.188 182.86 356.32 69.726c12.5-12.5 12.5-32.766 0-45.247L341.238 9.398c-12.504-12.503-32.77-12.503-45.25 0L182.86 122.528 69.727 9.374c-12.5-12.5-32.766-12.5-45.247 0L9.375 24.457c-12.5 12.504-12.5 32.77 0 45.25l113.152 113.152L9.398 295.99c-12.503 12.503-12.503 32.769 0 45.25L24.48 356.32c12.5 12.5 32.766 12.5 45.247 0l113.132-113.132L295.99 356.32c12.503 12.5 32.769 12.5 45.25 0l15.081-15.082c12.5-12.504 12.5-32.77 0-45.25zm0 0"
                                  fill="currentColor"
                                  data-original="#000000"
                                ></path>
                              </g>
                            </svg>
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              version="1.1"
                              xmlns:xlink="http://www.w3.org/1999/xlink"
                              width="10"
                              height="10"
                              x="0"
                              y="0"
                              viewBox="0 0 24 24"
                              style="enable-background: new 0 0 512 512"
                              xml:space="preserve"
                              class="checkmark"
                            >
                              <g>
                                <path
                                  d="M9.707 19.121a.997.997 0 0 1-1.414 0l-5.646-5.647a1.5 1.5 0 0 1 0-2.121l.707-.707a1.5 1.5 0 0 1 2.121 0L9 14.171l9.525-9.525a1.5 1.5 0 0 1 2.121 0l.707.707a1.5 1.5 0 0 1 0 2.121z"
                                  fill="currentColor"
                                  data-original="#000000"
                                  class=""
                                ></path>
                              </g>
                            </svg>
                          </div>
                        </div>
                      </label>
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
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <a
                class="page-link"
                href="#"
                @click.prevent="changePage(currentPage - 1)"
              >
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
              <a
                class="page-link"
                href="#"
                @click.prevent="changePage(currentPage + 1)"
              >
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

/* From Uiverse.io by JaydipPrajapati1910 */
.switch {
  /* switch */
  --switch-width: 46px;
  --switch-height: 24px;
  --switch-bg: rgb(131, 131, 131);
  --switch-checked-bg: #222;
  --switch-offset: calc((var(--switch-height) - var(--circle-diameter)) / 2);
  --switch-transition: all 0.2s cubic-bezier(0.27, 0.2, 0.25, 1.51);
  /* circle */
  --circle-diameter: 18px;
  --circle-bg: #fff;
  --circle-shadow: 1px 1px 2px rgba(146, 146, 146, 0.45);
  --circle-checked-shadow: -1px 1px 2px rgba(163, 163, 163, 0.45);
  --circle-transition: var(--switch-transition);
  /* icon */
  --icon-transition: all 0.2s cubic-bezier(0.27, 0.2, 0.25, 1.51);
  --icon-cross-color: var(--switch-bg);
  --icon-cross-size: 6px;
  --icon-checkmark-color: var(--switch-checked-bg);
  --icon-checkmark-size: 10px;
  /* effect line */
  --effect-width: calc(var(--circle-diameter) / 2);
  --effect-height: calc(var(--effect-width) / 2 - 1px);
  --effect-bg: var(--circle-bg);
  --effect-border-radius: 1px;
  --effect-transition: all 0.2s ease-in-out;
}

.switch input {
  display: none;
}

.switch {
  display: inline-block;
}

.switch svg {
  -webkit-transition: var(--icon-transition);
  -o-transition: var(--icon-transition);
  transition: var(--icon-transition);
  position: absolute;
  height: auto;
}

.switch .checkmark {
  width: var(--icon-checkmark-size);
  color: var(--icon-checkmark-color);
  -webkit-transform: scale(0);
  -ms-transform: scale(0);
  transform: scale(0);
}

.switch .cross {
  width: var(--icon-cross-size);
  color: var(--icon-cross-color);
}

.slider {
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
  width: var(--switch-width);
  height: var(--switch-height);
  background: var(--switch-bg);
  border-radius: 999px;
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
  position: relative;
  -webkit-transition: var(--switch-transition);
  -o-transition: var(--switch-transition);
  transition: var(--switch-transition);
  cursor: pointer;
}

.circle {
  width: var(--circle-diameter);
  height: var(--circle-diameter);
  background: var(--circle-bg);
  border-radius: inherit;
  -webkit-box-shadow: var(--circle-shadow);
  box-shadow: var(--circle-shadow);
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
  -webkit-box-pack: center;
  -ms-flex-pack: center;
  justify-content: center;
  -webkit-transition: var(--circle-transition);
  -o-transition: var(--circle-transition);
  transition: var(--circle-transition);
  z-index: 1;
  position: absolute;
  left: var(--switch-offset);
}

.slider::before {
  content: "";
  position: absolute;
  width: var(--effect-width);
  height: var(--effect-height);
  left: calc(var(--switch-offset) + (var(--effect-width) / 2));
  background: var(--effect-bg);
  border-radius: var(--effect-border-radius);
  -webkit-transition: var(--effect-transition);
  -o-transition: var(--effect-transition);
  transition: var(--effect-transition);
}

/* actions */

.switch input:checked + .slider {
  background: var(--switch-checked-bg);
}

.switch input:checked + .slider .checkmark {
  -webkit-transform: scale(1);
  -ms-transform: scale(1);
  transform: scale(1);
}

.switch input:checked + .slider .cross {
  -webkit-transform: scale(0);
  -ms-transform: scale(0);
  transform: scale(0);
}

.switch input:checked + .slider::before {
  left: calc(
    100% - var(--effect-width) - (var(--effect-width) / 2) -
      var(--switch-offset)
  );
}

.switch input:checked + .slider .circle {
  left: calc(100% - var(--circle-diameter) - var(--switch-offset));
  -webkit-box-shadow: var(--circle-checked-shadow);
  box-shadow: var(--circle-checked-shadow);
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

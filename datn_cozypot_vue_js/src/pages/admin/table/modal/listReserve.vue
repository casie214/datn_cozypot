<script setup>
import { onMounted, onUnmounted, ref } from "vue";
import dayjs from "dayjs";
import {
  searchDatBanService,
  updateTTPhieuDatBan,
} from "@/services/tableManageService";
import CommonPagination from "@/components/commonPagination.vue";

// --- Khai báo State ---
const listPhieuDatBan = ref([]);
const totalPages = ref(0);
const totalElements = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const searchForm = ref({
  soDienThoai: "",
  trangThai: "",
  thoiGianDat: "",
});

const trangThaiList = [
  { value: "0", label: "Chờ xác nhận" },
  { value: "1", label: "Đã xác nhận" },
  { value: "2", label: "Đã hủy" },
  { value: "3", label: "Đang sử dụng" },
  { value: "4", label: "Hoàn thành" },
  { value: "5", label: "Quá hạn" },
];

// --- Các hàm xử lý Logic ---

const buildSearchPayload = () => {
  let ngayDat = null;

  if (searchForm.value.thoiGianDat) {
    ngayDat = dayjs(searchForm.value.thoiGianDat).format("YYYY-MM-DD");
  }

  return {
    soDienThoai: searchForm.value.soDienThoai || null,
    trangThai: searchForm.value.trangThai || null,
    thoiGianDat: ngayDat, 
  };
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
    totalElements.value = data.totalElements ?? data.content?.length ?? 0;
  } catch (error) {
    console.error("Lỗi searchDatBan:", error);
  }
};

const onResetSearch = () => {
  searchForm.value = {
    soDienThoai: "",
    trangThai: "",
    thoiGianDat: "",
  };
  currentPage.value = 1;
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer);
  searchDatBan();
};

let searchDebounceTimer = null;

const triggerAutoSearch = () => {
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer);
  searchDebounceTimer = setTimeout(() => {
    currentPage.value = 1;
    searchDatBan();
  }, 250);
};

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  searchDatBan();
};

const formatDate = (time) => {
  if (!time) return "";
  return dayjs(time).format("DD/MM/YYYY HH:mm");
};

const getStatusText = (trangThai) => {
  const status = String(trangThai).trim();
  if (status === "0") return "Chờ xác nhận";
  if (status === "1") return "Đã xác nhận";
  if (status === "2") return "Đã hủy";
  if (status === "3") return "Đang phục vụ";
  if (status === "4") return "Hoàn tất";
  if (status === "5") return "Hết hạn";
  return "Không xác định";
};

const getTableList = (phieu) => {
  let list = [];
  if (phieu.danhSachBan && Array.isArray(phieu.danhSachBan)) {
    list = phieu.danhSachBan.map(b => b.maBan);
  } else if (phieu.danhSachTenBan && Array.isArray(phieu.danhSachTenBan)) {
    list = phieu.danhSachTenBan;
  } else if (phieu.maBan) {
    // Nếu BE gộp thành chuỗi "B01, B02" thì tự động cắt ra
    list = String(phieu.maBan).split(',').map(s => s.trim()).filter(s => s);
  }
  return list;
};

const getStatusClass = (trangThai) => {
  const status = String(trangThai).trim();
  if (status === "0") return "bg-danger text-light";
  if (status === "1") return "bg-danger text-light";
  if (status === "2") return "bg-danger text-light";
  if (status === "3") return "bg-danger text-light";
  if (status === "4") return "bg-danger text-light";
  if (status === "5") return "bg-danger text-light";
  return "bg-secondary";
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

// --- Lifecycle ---
onMounted(() => {
  searchDatBan();
});

onUnmounted(() => {
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer);
});
</script>

<template>
  <div class="layout-table">
    <div class="navbar-search">
      <div class="search-form">
        Tìm kiếm
        <form class="d-flex gap-2 mt-2" role="search">
          <input
            class="form-control reserve-filter-phone"
            type="search"
            placeholder="Số điện thoại khách hàng"
            v-model="searchForm.soDienThoai"
            @input="triggerAutoSearch"
          />
          <select
            v-model="searchForm.trangThai"
            @change="triggerAutoSearch"
            class="form-select reserve-filter-status"
          >
            <option value="" disabled>Trạng thái</option>
            <option v-for="item in trangThaiList" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>

          <div class="reserve-filter-date">
            <input
              type="date"
              v-model="searchForm.thoiGianDat"
              @input="triggerAutoSearch"
              class="form-control"
            />
          </div>

          <button class="btn text-nowrap" type="button" @click="onResetSearch">
             Xóa bộ lọc
          </button>
        </form>
      </div>
      <div class="mt-3">
        <div class="table-container">
          <table class="table">
            <thead>
              <tr>
                <th scope="col">STT</th>
                <th scope="col">MÃ</th>
                <th scope="col">KHÁCH HÀNG</th>
                <th scope="col">SĐT</th>
                <th scope="col">BÀN</th>
                <th scope="col">TẦNG</th>
                <th scope="col">NGÀY GIỜ</th>
                <th scope="col">SỐ NGƯỜI</th>
                <th scope="col">TRẠNG THÁI</th>
                <th scope="col">HÀNH ĐỘNG</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(phieuDatBan, index) in listPhieuDatBan" :key="phieuDatBan.idDatBan">
                <th>{{ (currentPage - 1) * pageSize + index + 1 }}</th>
                <td>{{ phieuDatBan.maDatBan }}</td>
                <td>{{ phieuDatBan.tenKhachHang }}</td>
                <td>{{ phieuDatBan.soDienThoai }}</td>

                <td>
                  <template v-if="getTableList(phieuDatBan).length > 0">
                    <div class="d-flex gap-1 flex-wrap align-items-center">
                      <span v-for="(ban, i) in getTableList(phieuDatBan).slice(0, 2)" :key="i" class="badge bg-danger" style="font-size: 12px;">
                        {{ ban }}
                      </span>
                      
                      <span v-if="getTableList(phieuDatBan).length > 2" class="badge bg-secondary text-white shadow-sm" style="font-size: 11px;">
                        +{{ getTableList(phieuDatBan).length - 2 }}
                      </span>
                    </div>
                  </template>

                  <span v-else class="text-danger fst-italic small fw-bold">
                    Chưa xếp bàn
                  </span>
                </td>

                <td>
                  {{ phieuDatBan.soTang == null ? "Trống" : phieuDatBan.soTang }}
                </td>
                <td>{{ formatDate(phieuDatBan.thoiGianDat) }}</td>
                <td>{{ phieuDatBan.soLuongKhach }}</td>
                <td>
                  <span class="badge" :class="getStatusClass(phieuDatBan.trangThai)">
                    {{ getStatusText(phieuDatBan.trangThai) }}
                  </span>
                </td>
                <td>
                  <div class="action-groups d-flex align-items-center gap-3">
                    
                    
                    <i class="fa-solid fa-eye" style="cursor: pointer;" @click="$emit('open-detail', phieuDatBan)"></i>
                   

                    <div class="checkbox-wrapper-5">
                      <label class="switch">
                        <input
                          type="checkbox"
                          :checked="phieuDatBan.trangThai === 1"
                          :disabled="phieuDatBan.trangThai !== 1"
                          @click.prevent="onToggleCheckIn(phieuDatBan)"
                        />
                        <div class="slider">
                          <div class="circle">
                            <svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="6" height="6" viewBox="0 0 365.696 365.696" class="cross">
                              <g><path d="M243.188 182.86 356.32 69.726c12.5-12.5 12.5-32.766 0-45.247L341.238 9.398c-12.504-12.503-32.77-12.503-45.25 0L182.86 122.528 69.727 9.374c-12.5-12.5-32.766-12.5-45.247 0L9.375 24.457c-12.5 12.504-12.5 32.77 0 45.25l113.152 113.152L9.398 295.99c-12.503 12.503-12.503 32.769 0 45.25L24.48 356.32c12.5 12.5 32.766 12.5 45.247 0l113.132-113.132L295.99 356.32c12.503 12.5 32.769 12.5 45.25 0l15.081-15.082c12.5-12.504 12.5-32.77 0-45.25zm0 0" fill="currentColor"></path></g>
                            </svg>
                            <svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="10" height="10" viewBox="0 0 24 24" class="checkmark">
                              <g><path d="M9.707 19.121a.997.997 0 0 1-1.414 0l-5.646-5.647a1.5 1.5 0 0 1 0-2.121l.707-.707a1.5 1.5 0 0 1 2.121 0L9 14.171l9.525-9.525a1.5 1.5 0 0 1 2.121 0l.707.707a1.5 1.5 0 0 1 0 2.121z" fill="currentColor"></path></g>
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
      <div class="custom-pagination-wrapper mt-4 pt-3 border-top">
        <CommonPagination
          v-model:currentPage="currentPage"
          v-model:pageSize="pageSize"
          :total-pages="totalPages"
          :total-elements="totalElements"
          :current-count="listPhieuDatBan.length"
          @change="searchDatBan"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.layout-table {
  display: flex;
  background-color: white;
}

.table tr th{
  padding: 14px 15px;
}

.table tr th {
  font-family: "Segoe UI", sans-serif;
  font-size: 14px;
  font-weight: 500;
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
}

.btn {
  background-color: #7d161a;
  color: white;
}

.switch {
  --switch-width: 46px;
  --switch-height: 24px;
  --switch-bg: rgb(131, 131, 131);
  --switch-checked-bg: #222;
  --switch-offset: calc((var(--switch-height) - var(--circle-diameter)) / 2);
  --switch-transition: all 0.2s cubic-bezier(0.27, 0.2, 0.25, 1.51);
  --circle-diameter: 18px;
  --circle-bg: #fff;
  --circle-shadow: 1px 1px 2px rgba(146, 146, 146, 0.45);
  --circle-checked-shadow: -1px 1px 2px rgba(163, 163, 163, 0.45);
  --circle-transition: var(--switch-transition);
  --icon-transition: all 0.2s cubic-bezier(0.27, 0.2, 0.25, 1.51);
  --icon-cross-color: var(--switch-bg);
  --icon-cross-size: 6px;
  --icon-checkmark-color: var(--switch-checked-bg);
  --icon-checkmark-size: 10px;
  --effect-width: calc(var(--circle-diameter) / 2);
  --effect-height: calc(var(--effect-width) / 2 - 1px);
  --effect-bg: var(--circle-bg);
  --effect-border-radius: 1px;
  --effect-transition: all 0.2s ease-in-out;
}

.switch input { display: none; }
.switch { display: inline-block; }
.switch svg {
  transition: var(--icon-transition);
  position: absolute;
  height: auto;
}
.switch .checkmark {
  width: var(--icon-checkmark-size);
  color: var(--icon-checkmark-color);
  transform: scale(0);
}
.switch .cross {
  width: var(--icon-cross-size);
  color: var(--icon-cross-color);
}
.slider {
  box-sizing: border-box;
  width: var(--switch-width);
  height: var(--switch-height);
  background: var(--switch-bg);
  border-radius: 999px;
  display: flex;
  align-items: center;
  position: relative;
  transition: var(--switch-transition);
  cursor: pointer;
}
.circle {
  width: var(--circle-diameter);
  height: var(--circle-diameter);
  background: var(--circle-bg);
  border-radius: inherit;
  box-shadow: var(--circle-shadow);
  display: flex;
  align-items: center;
  justify-content: center;
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
  transition: var(--effect-transition);
}
.switch input:checked + .slider { background: var(--switch-checked-bg); }
.switch input:checked + .slider .checkmark { transform: scale(1); }
.switch input:checked + .slider .cross { transform: scale(0); }
.switch input:checked + .slider::before { left: calc(100% - var(--effect-width) - (var(--effect-width) / 2) - var(--switch-offset)); }
.switch input:checked + .slider .circle {
  left: calc(100% - var(--circle-diameter) - var(--switch-offset));
  box-shadow: var(--circle-checked-shadow);
}

.btn-custom-outline {
  background-color: transparent !important; 
  color: #7d161a !important;
  border: 1px solid #7d161a !important;
}

.btn-custom-outline:hover {
  background-color: #7d161a !important; 
  color: #ffffff !important;
}

.pagination .page-item.active .page-link {
  background-color: #7d161a !important;
  border-color: #7d161a !important;
  color: #ffffff !important;
}

.pagination .page-link { color: #7d161a; }
.pagination .page-link:hover {
  background-color: #f8ecec;
  color: #5a1013;
  border-color: #dee2e6;
}
.pagination .page-link:focus { box-shadow: 0 0 0 0.2rem rgba(125, 22, 26, 0.25); }

.btn{
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
  color: white !important;
  transition: 0.2s;
}

.btn:hover {
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
  color: white !important;
  transform: scale(1.04);
}

.table-container{
  box-shadow: var(--bs-box-shadow-sm) !important;
  border-radius: 15px;
}

.reserve-filter-phone { width: 30%; }
.reserve-filter-status { width: 25%; }
.reserve-filter-date { width: 30%; }

@media (max-width: 992px) {
  .reserve-filter-phone,
  .reserve-filter-status,
  .reserve-filter-date {
    width: 100%;
  }
}

/* 🚨 BẢO ĐẢM THÔNG BÁO SWAL LUÔN NỔI LÊN TRÊN MỌI THỨ */
:global(.swal2-container) {
  z-index: 100000 !important; 
}
</style>
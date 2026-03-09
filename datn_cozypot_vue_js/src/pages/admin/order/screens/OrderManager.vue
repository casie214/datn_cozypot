<script setup>
import { computed } from "vue";
import { useOrderManager } from "./orderFunction";
import { useRouter } from "vue-router";
import Multiselect from "@vueform/multiselect";
import "@vueform/multiselect/themes/default.css";
import CommonPagination from "@/components/commonPagination.vue";

const router = useRouter();

const {
  filters,
  orderList,
  handleSearch,
  handleReset,
  handlePrintOrder,
  currentPage,
  totalPages,
  handlePageChange,
  pageSize,
  totalElements,
  formatDateTime,
} = useOrderManager();

const statusOptions = [
  "Tất cả",
  "Vừa tạo",
  "Chờ cọc",
  "Đã cọc",
  "Đã xác nhận",
  "Khách đã đến",
  "Chờ thanh toán",
  "Đã thanh toán",
  "Hoàn thành",
  "Đã hủy",
  "Đã hoàn tiền",
];

const uiCurrentPage = computed({
  get: () => currentPage.value + 1,
  set: (val) => {
    currentPage.value = Math.max(0, (Number(val) || 1) - 1);
  },
});

const onPaginationChange = () => {
  handlePageChange(currentPage.value);
};
</script>

<template>
  <div class="d-flex" style="min-height: 100vh">
    <main class="flex-grow-1 p-4 main-offset">
      <h1 class="page-title mb-4">Quản lý hóa đơn</h1>

      <div
        class="card shadow-sm mb-4"
        style="border: 1px solid #dee2e6 !important; border-radius: 8px"
      >
        <div class="card-body">
          <div class="row g-2 align-items-end filter-bar">
            <div class="col-md">
              <label class="form-label text-muted small fw-bold"
                >Tìm kiếm</label
              >
              <input
                type="text"
                v-model="filters.search"
                @input="handleSearch"
                class="form-control"
                placeholder="Mã ĐH, tên KH, SĐT"
              />
            </div>

            <div class="col-md">
              <label class="form-label text-muted small fw-bold"
                >Trạng thái hóa đơn</label
              >
              <Multiselect
                v-model="filters.status"
                :options="statusOptions"
                :searchable="true"
                mode="single"
                placeholder="Chọn trạng thái"
                class="custom-multiselect-theme"
                @change="
                  (val) => {
                    filters.status = val;
                    handleSearch();
                  }
                "
              />
            </div>

            <div class="col-md">
              <label class="form-label text-muted small fw-bold"
                >Loại thời gian</label
              >
              <select
                v-model="filters.dateType"
                @change="handleSearch"
                class="form-select"
              >
                <option value="booking">Ngày khách đến</option>
                <option value="created">Ngày tạo hóa đơn</option>
              </select>
            </div>

            <div class="col-md">
              <label class="form-label text-muted small fw-bold">Từ ngày</label>
              <input
                type="date"
                v-model="filters.fromDate"
                @input="handleSearch"
                class="form-control"
              />
            </div>

            <div class="col-md">
              <label class="form-label text-muted small fw-bold"
                >Đến ngày</label
              >
              <input
                type="date"
                v-model="filters.toDate"
                @input="handleSearch"
                class="form-control"
              />
            </div>

            <div class="col-md-auto d-flex">
              <button
                class="btn btn-outline-custom btn-reset-filter"
                @click="handleReset"
                title="Làm mới"
              >
                Xóa bộ lọc
              </button>
            </div>
          </div>
        </div>
      </div>

      <div
        class="card shadow-sm"
        style="border: 1px solid #dee2e6 !important; border-radius: 15px"
      >
        <div class="card-body p-0">
          <div
            class="table-responsive"
            style="border-radius: 8px 8px 0 0; overflow: hidden"
          >
            <table class="table table-hover align-middle mb-0">
              <thead class="table-header-red">
                <tr>
                  <th class="py-3 ps-3">STT</th>
                  <th class="py-3">MÃ ĐƠN</th>
                  <th class="py-3">KHÁCH HÀNG</th>
                  <th class="py-3">SĐT</th>
                  <th class="py-3">BÀN</th>
                  <!-- <th class="py-3">SL KHÁCH</th> -->
                  <th class="py-3">LOẠI</th>
                  <th class="py-3">THỜI GIAN</th>
                  <th class="py-3">TỔNG TIỀN</th>
                  <th class="py-3">TIỀN CỌC</th>
                  <th class="py-3">TRẠNG THÁI</th>
                  <th class="py-3 text-center">THAO TÁC</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="orderList.length === 0">
                  <td colspan="11" class="text-center py-4 text-muted">
                    Không tìm thấy dữ liệu
                  </td>
                </tr>
                <tr v-for="(order, index) in orderList" :key="order.id">
                  <td class="ps-4 fw-bold">
                    {{ index + 1 + currentPage * pageSize }}
                  </td>
                  <td>{{ order.id }}</td>
                  <td>{{ order.khachHang || "Khách vãng lai" }}</td>
                  <td>{{ order.sdt || "---" }}</td>
                  <td>{{ order.ban }}</td>
                  <td>{{ order.loai }}</td>
                  <td>
                    <div class="d-flex flex-column gap-1">
                      <span
                        class="fw-bold"
                        style="color: #8b0000; font-size: 0.9rem"
                      >
                        <i class="far fa-clock"></i> Đến:
                        {{ formatDateTime(order.thoiGianDat) }}
                      </span>
                      <span class="text-muted" style="font-size: 0.8rem">
                        <i class="fas fa-file-invoice"></i> Tạo:
                        {{ formatDateTime(order.ngayTaoRaw) }}
                      </span>
                    </div>
                  </td>
                  <td class="fw-bold">{{ order.tongTien }}</td>
                  <td class="fw-bold">{{ order.tienCoc }}</td>
                  <td>{{ order.trangThai }}</td>
                  <td class="text-center">
                    <div class="icon-tooltip">
                      <i
                        style="cursor: pointer"
                        class="fas fa-eye view-icon"
                        @click="
                          router.push({
                            name: 'OrderDetail',
                            params: { id: order.dbId },
                          })
                        "
                      ></i>
                      <span class="tooltip-text">Xem chi tiết</span>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div
          class="card-footer bg-white py-3"
          v-if="totalPages > 0"
          style="
            border-top: 1px solid #dee2e6 !important;
            border-radius: 0 0 8px 8px;
          "
        >
          
        </div>
      </div>
      <div class="custom-pagination-wrapper mt-4 pt-3 border-top">
            <CommonPagination
              v-model:currentPage="uiCurrentPage"
              v-model:pageSize="pageSize"
              :total-pages="totalPages"
              :total-elements="totalElements"
              :current-count="orderList.length"
              @change="onPaginationChange"
            />
          </div>
    </main>
  </div>
</template>

<style scoped>


.page-title {
  color: #8b0000;
  font-size: 24px;
  font-weight: bold;
}
.form-control,
.form-select {
  border-radius: 4px;
  border: 1px solid #ddd;
}
.filter-bar .form-control,
.filter-bar .form-select {
  height: 40px;
}
.form-control:focus,
.form-select:focus {
  border-color: #8b0000;
  box-shadow: 0 0 0 0.2rem rgba(139, 0, 0, 0.1);
}
.btn-custom-red {
  background-color: #720e1e;
  border: none;
}
.btn-custom-red:hover {
  background-color: #5c0b18;
  color: white;
}
.btn-outline-custom {
  background: linear-gradient(135deg, #7d161a 0%, #d32f2f 100%);
  color: white !important;
  border: none;
  transition: all 0.2s ease;
}
.btn-outline-custom:hover {
  opacity: 0.9;
  color: white;
}
.btn-reset-filter {
  height: 40px;
  width: auto;
  white-space: nowrap;
  padding: 0 14px;
}

.table-header-red {
  background-color: #8b0000 !important;
}

.table-header-red th {
  background-color: transparent !important;
  color: white;
  font-size: 14px;
  text-transform: uppercase;
  font-weight: 500;
  border-bottom: none;
  font-family: "Segoe UI", sans-serif;
}
.btn-icon {
  background: transparent;
  border: none;
  font-size: 16px;
  padding: 4px 8px;
  transition: background 0.2s;
}
.btn-icon:hover {
  background-color: #eee;
  border-radius: 4px;
}
.bg-custom-red {
  background-color: #8b0000 !important;
  border-color: #8b0000 !important;
  color: white !important;
}

.page-link {
  color: #333;
  border: 1px solid #ddd;
  margin: 0 2px;
  border-radius: 4px;
  cursor: pointer;
}
.page-link:hover {
  background-color: #f0f0f0;
}
.page-item.disabled .page-link {
  color: #ccc;
  pointer-events: none;
  background-color: #fff;
}
.custom-multiselect-theme {
  --ms-border-color: #ddd;
  --ms-border-color-active: #8b0000;
  --ms-ring-color: rgba(139, 0, 0, 0.1);
  --ms-option-bg-pointed: #fdf2f2;
  --ms-option-color-pointed: #8b0000;
  --ms-option-bg-selected: #8b0000;
  --ms-option-color-selected: #ffffff;
  --ms-option-bg-selected-pointed: #720e1e;
  border-radius: 4px;
  --ms-max-height: 200px;
}
:deep(.filter-bar .multiselect),
:deep(.filter-bar .multiselect-wrapper) {
  min-height: 40px;
}
.custom-multiselect-theme :global(.multiselect-is-active) {
  box-shadow: 0 0 0 0.2rem rgba(139, 0, 0, 0.1) !important;
  border-color: #8b0000 !important;
}

.icon-tooltip {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.icon-tooltip .tooltip-text {
  position: absolute;
  bottom: 135%;
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
  z-index: 1000;
}
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

/* Hover show */
.icon-tooltip:hover .tooltip-text {
  opacity: 1;
  visibility: visible;
  transform: translateX(-50%) translateY(-4px);
}
</style>

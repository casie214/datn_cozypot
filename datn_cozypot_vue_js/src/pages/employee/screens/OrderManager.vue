<script setup>
import { useOrderManager } from "./orderFunction";
import { useRouter } from "vue-router";

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
} = useOrderManager();
</script>

<template>
  <div class="d-flex bg-light" style="min-height: 100vh">
    <main class="flex-grow-1 p-4 main-offset">
      <h1 class="page-title mb-4">Quản lý hóa đơn</h1>

      <div class="card border-0 shadow-sm mb-4">
        <div class="card-body">
          <div class="row g-2 align-items-end">
            <div class="col-md-3">
              <label class="form-label text-muted small fw-bold"
                >Tìm kiếm</label
              >
              <input
                type="text"
                v-model="filters.search"
                class="form-control"
                placeholder="Mã ĐH, tên KH, SĐT"
              />
            </div>

            <div class="col-md-2">
              <label class="form-label text-muted small fw-bold"
                >Trạng thái hóa đơn</label
              >
              <select v-model="filters.status" class="form-select">
                <option>Tất cả</option>
                <option>Chờ nhận bàn</option>
                <option>Đang phục vụ</option>
                <option>Hoàn thành</option>
                <option>Đã hủy</option>
              </select>
            </div>

            <div class="col-md-2">
              <label class="form-label text-muted small fw-bold"
                >Trạng thái Hoàn tiền</label
              >
              <select v-model="filters.refundStatus" class="form-select">
                <option>Tất cả</option>
                <option>Không cần hoàn</option>
                <option>Chờ hoàn</option>
                <option>Đã hoàn</option>
                <option>Không hoàn tiền</option>
              </select>
            </div>

            <div class="col-md-2">
              <label class="form-label text-muted small fw-bold">Từ ngày</label>
              <input
                type="date"
                v-model="filters.fromDate"
                class="form-control"
              />
            </div>

            <div class="col-md-2">
              <label class="form-label text-muted small fw-bold"
                >Đến ngày</label
              >
              <input
                type="date"
                v-model="filters.toDate"
                class="form-control"
              />
            </div>

            <div class="col-md-1">
              <div class="d-flex flex-column gap-1">
                <button
                  class="btn btn-custom-red btn-sm text-white w-100"
                  @click="handleSearch"
                  title="Tìm kiếm"
                >
                  <i class="fas fa-search"></i> Tìm
                </button>
                <button
                  class="btn btn-outline-custom btn-sm w-100"
                  @click="handleReset"
                  title="Làm mới"
                >
                  <i class="fas fa-undo"></i> Hủy
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="card border-0 shadow-sm">
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-header-red">
                <tr>
                  <th class="py-3 ps-4">STT</th>
                  <th class="py-3">MÃ ĐƠN</th>
                  <th class="py-3">KHÁCH HÀNG</th>
                  <th class="py-3">SĐT</th>
                  <th class="py-3">BÀN</th>
                  <th class="py-3">SL KHÁCH</th>
                  <th class="py-3">LOẠI</th>
                  <th class="py-3">TỔNG TIỀN</th>
                  <th class="py-3">TIỀN CỌC</th>
                  <th class="py-3">TRẠNG THÁI</th>
                  <th class="py-3 text-center">THAO TÁC</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="orderList.length === 0">
                  <td colspan="9" class="text-center py-4 text-muted">
                    Không tìm thấy dữ liệu
                  </td>
                </tr>
                <tr v-for="(order, index) in orderList" :key="order.id">
                  <td class="ps-4 fw-bold">
                    {{ index + 1 + currentPage * 5 }}
                  </td>
                  <td>{{ order.id }}</td>
                  <td>{{ order.khachHang }}</td>
                  <td>{{ order.sdt }}</td>
                  <td>{{ order.ban }}</td>
                  <td class="ps-4">{{ order.soLuongKhach }}</td>
                  <td>{{ order.loai }}</td>
                  <td class="fw-bold">{{ order.tongTien }}</td>
                  <td class="fw-bold">{{ order.tienCoc }}</td>
                  <td>{{ order.trangThai }}</td>
                  <td class="text-center">
                    <div class="d-flex justify-content-center gap-2">
                      <button
                        class="btn btn-icon"
                        title="Xem chi tiết"
                        @click="
                          router.push({
                            name: 'OrderDetail',
                            params: { id: order.dbId },
                          })
                        "
                      >
                        👁️
                      </button>

                      <button
                        class="btn btn-icon"
                        title="In hóa đơn"
                        @click="handlePrintOrder(order.id)"
                      >
                        🖨️
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="card-footer bg-white border-0 py-3" v-if="totalPages > 0">
          <ul class="pagination justify-content-center mb-0">
            <li class="page-item" :class="{ disabled: currentPage === 0 }">
              <button
                class="page-link text-dark"
                @click="handlePageChange(currentPage - 1)"
                :disabled="currentPage === 0"
              >
                &lt;
              </button>
            </li>

            <li class="page-item" v-for="page in totalPages" :key="page">
              <button
                class="page-link"
                :class="
                  currentPage === page - 1
                    ? 'bg-custom-red border-custom-red text-white'
                    : 'text-dark'
                "
                @click="handlePageChange(page - 1)"
              >
                {{ page }}
              </button>
            </li>

            <li
              class="page-item"
              :class="{ disabled: currentPage === totalPages - 1 }"
            >
              <button
                class="page-link text-dark"
                @click="handlePageChange(currentPage + 1)"
                :disabled="currentPage === totalPages - 1"
              >
                &gt;
              </button>
            </li>
          </ul>
        </div>
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
  background-color: #8b0000;
  color: white;
  border: none;
}
.btn-outline-custom:hover {
  opacity: 0.9;
  color: white;
}
.table-header-red th {
  background-color: #8b0000;
  color: white;
  font-size: 13px;
  text-transform: uppercase;
  font-weight: 600;
  border-bottom: none;
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
</style>

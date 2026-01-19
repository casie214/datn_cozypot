<script setup>
import { onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import Sidebar from "../../../components/sidebar.vue";
import { useOrderManager } from "./orderFunction";

const route = useRoute();
const router = useRouter();

const {
  selectedOrder,
  orderDetails,
  currentVAT,
  handleViewDetail,
  handleHuyDon,
  handlePrintOrder,
  historyEvents,
  paymentHistory,
} = useOrderManager();

onMounted(async () => {
  const orderDbId = route.params.id;
  if (orderDbId) {
    await handleViewDetail(orderDbId);
  }
});

const formatMoney = (value) => {
  if (value === undefined || value === null) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
};

// Tính toán tiền
const subTotal = computed(() => {
  return (
    orderDetails.value?.reduce((sum, item) => {
      if (item.trangThaiCode === 0) return sum; 
      return sum + (item.thanhTien || 0);
    }, 0) || 0
  );
});

const taxAmount = computed(() => subTotal.value * (currentVAT.value / 100));
const discount = computed(() => selectedOrder.value?.soTienDaGiam || 0);
const finalTotal = computed(
  () => subTotal.value + taxAmount.value - discount.value,
);

const isReadOnly = computed(
  () =>
    selectedOrder.value?.trangThai === "Đã hủy" ||
    selectedOrder.value?.trangThai === "Hoàn thành",
);
const hasServedDish = computed(() =>
  orderDetails.value?.some((item) => item.trangThaiCode === 2),
);

const onBack = () => router.push({ name: "orderManager" });

const getEventColor = (type) => {
  if (type === "create") return "bg-warning";
  if (type === "delete") return "bg-danger";
  if (type === "payment") return "bg-warning";
  return "bg-secondary";
};

const formatDateTime = (dateString) => {
  if (!dateString) return "";
  return new Date(dateString).toLocaleString("vi-VN");
};
</script>

<template>
  <div class="d-flex bg-light min-vh-100">
    <Sidebar />

    <main class="flex-grow-1 p-4 main-offset">
      <h1 class="page-title mb-4">Quản lý hóa đơn chi tiết</h1>

      <div class="card border-0 shadow-sm mb-4">
        <div class="card-header bg-white border-bottom py-3">
          <span class="fw-bold">Thông tin hóa đơn:</span>
          <span class="text-custom-red fw-bold ms-2">{{
            selectedOrder?.id
          }}</span>
        </div>
        <div class="card-body">
          <div class="row g-4">
            <div class="col-md">
              <label class="d-block text-muted small mb-1">Khách hàng</label>
              <p class="mb-0 fw-medium">
                {{ selectedOrder?.khachHang || "Khách vãng lai" }}
              </p>
            </div>
            <div class="col-md">
              <label class="d-block text-muted small mb-1">Số điện thoại</label>
              <p class="mb-0 fw-medium">{{ selectedOrder?.sdt || "---" }}</p>
            </div>
            <div class="col-md">
              <label class="d-block text-muted small mb-1">Bàn</label>
              <p class="mb-0 fw-bold">{{ selectedOrder?.ban }}</p>
            </div>
            <div class="col-md">
              <label class="d-block text-muted small mb-1">Trạng thái</label>
              <p
                class="mb-0 fw-bold"
                :class="{
                  'text-success': selectedOrder?.trangThai === 'Hoàn thành',
                  'text-danger': selectedOrder?.trangThai === 'Đã hủy',
                  'text-warning': selectedOrder?.trangThai === 'Đã xác nhận',
                }"
              >
                {{ selectedOrder?.trangThai }}
              </p>
            </div>
            <div class="col-md">
              <label class="d-block text-muted small mb-1">Thời gian tạo</label>
              <p class="mb-0 fw-medium">{{ selectedOrder?.ngayTao }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="card border-0 shadow-sm mb-4">
        <div class="card-header bg-white border-bottom py-3 fw-bold">
          🍴 Thông tin món đã đặt
        </div>
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table align-middle mb-0">
              <thead class="bg-light">
                <tr>
                  <th
                    class="text-center py-3 text-custom-red border-bottom-red"
                    width="50"
                  >
                    STT
                  </th>
                  <th class="py-3 text-custom-red border-bottom-red">
                    TÊN MÓN ĂN
                  </th>
                  <th
                    class="text-center py-3 text-custom-red border-bottom-red"
                  >
                    SỐ LƯỢNG
                  </th>
                  <th class="text-end py-3 text-custom-red border-bottom-red">
                    ĐƠN GIÁ
                  </th>
                  <th class="text-end py-3 text-custom-red border-bottom-red">
                    THÀNH TIỀN
                  </th>
                  <th
                    class="text-end py-3 text-custom-red border-bottom-red"
                  >
                    TRẠNG THÁI
                  </th>
                  <th
                    class="text-center py-3 text-custom-red border-bottom-red"
                  >
                    CHI TIẾT MÓN
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in orderDetails" :key="index">
                  <td class="text-center">{{ index + 1 }}</td>
                  <td>
                    <div class="d-flex flex-column">
                      <span class="fw-medium">{{ item.tenMon }}</span>
                      <small
                        v-if="item.ghiChu"
                        class="text-danger"
                        style="font-size: 0.8rem"
                        >Ghi chú: {{ item.ghiChu }}</small
                      >
                    </div>
                  </td>
                  <td class="text-center">{{ item.soLuong }}</td>
                  <td class="text-end">{{ formatMoney(item.donGia) }}</td>
                  <td class="text-end fw-bold">
                    {{ formatMoney(item.thanhTien) }}
                  </td>
                  <td class="text-end">
                    <span
                      class="badge px-2 py-1"
                      :class="
                        item.trangThaiCode === 2
                          ? 'bg-success-subtle text-success'
                          : item.trangThaiCode === 1
                          ? 'bg-warning-subtle text-warning'
                          :'bg-danger-subtle text-danger'
                      "
                    >
                      {{ item.trangThaiText }}
                    </span>
                  </td>
                  <td class="text-center">
                    <button
                      class="btn btn-icon"
                      title="Xem chi tiết"
                    >
                      👁️
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="row g-4 mb-4 align-items-stretch">
        
        <div class="col-md-6 d-flex flex-column gap-4">
            
            <div class="card border-0 shadow-sm flex-grow-1">
                <div class="card-header bg-white border-bottom py-3 fw-bold">
                    🕒 Lịch sử hóa đơn
                </div>
                <div class="card-body p-3 h-100" style="overflow-y: auto">
                    <div v-if="!historyEvents || historyEvents.length === 0" class="text-center text-muted py-4">
                        <small>Chưa có lịch sử ghi nhận</small>
                    </div>
                    <div v-else>
                        <div v-for="(event, index) in historyEvents" :key="index" class="d-flex mb-3 position-relative">
                            <div class="flex-shrink-0 mt-1">
                                <div class="rounded-circle" :class="getEventColor(event.type)" style="width: 12px; height: 12px"></div>
                            </div>
                            <div class="ms-3">
                                <div class="fw-bold small">{{ event.title }}</div>
                                <div class="text-muted" style="font-size: 0.8rem">
                                    {{ event.time }} - <span class="fw-medium">{{ event.user }}</span>
                                </div>
                                <div v-if="event.detail" class="text-secondary fst-italic mt-1" style="font-size: 0.85rem">
                                    "{{ event.detail }}"
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div v-if="paymentHistory && paymentHistory.length > 0" class="card border-0 shadow-sm">
                <div class="card-header bg-white border-bottom py-3 fw-bold">
                    💳 Lịch sử thanh toán
                </div>
                <div class="card-body p-3">
                    <div v-for="(pay, index) in paymentHistory" :key="index" class="d-flex align-items-start mb-3 pb-3 border-bottom-dashed last-no-border">
                        <div class="me-3">
                            <div class="bg-warning-subtle text-warning rounded-circle d-flex align-items-center justify-content-center" style="width: 32px; height: 32px;">
                                <i class="fa-solid fa-check small"></i>
                            </div>
                        </div>
                        <div class="flex-grow-1">
                            <div class="d-flex justify-content-between">
                                <span class="fw-bold text-warning">
                                    {{ formatMoney(pay.soTienThanhToan) }}
                                </span>
                                <span class="text-muted small">{{ formatDateTime(pay.ngayThanhToan) }}</span>
                            </div>
                            <div class="d-flex justify-content-between align-items-center mt-1">
                                <span class="small text-dark">
                                    {{ pay.tenPhuongThuc }} 
                                    <span class="badge bg-warning ms-1">Thanh toán</span>
                                </span>
                            </div>
                            <div class="small text-muted fst-italic mt-1">
                                Mã GD: {{ pay.maGiaoDich || '---' }}
                            </div>
                        </div>
                    </div>
                </div>
            </div>

        </div>

        <div class="col-md-6">
          <div class="card border-0 shadow-sm h-100">
            <div class="card-header bg-white border-bottom py-3 fw-bold">
              💰 Tổng kết đơn hàng
            </div>
            <div class="card-body p-4 d-flex flex-column">
              <div class="d-flex justify-content-between mb-1">
                <span class="text-muted fw-medium">Tổng tiền hàng:</span>
                <span class="fw-bold">{{ formatMoney(subTotal) }}</span>
              </div>
              <div class="text-end text-muted small mb-3 fst-italic">
                Giá trị sản phẩm
              </div>

              <hr
                class="border-secondary border-opacity-25 border-dashed my-3"
              />

              <div class="d-flex justify-content-between mb-3">
                <span class="text-muted fw-medium"
                  >Thuế VAT ({{ currentVAT }}%):</span
                >
                <span class="fw-bold">{{ formatMoney(taxAmount) }}</span>
              </div>

              <div v-if="discount > 0">
                <div class="d-flex justify-content-between mb-1">
                  <span class="text-muted fw-medium">Giảm giá:</span>
                  <span class="fw-bold text-danger"
                    >({{ formatMoney(discount) }})</span
                  >
                </div>
                <div class="text-end text-muted small mb-3 fst-italic">
                  Khuyến mãi áp dụng
                </div>
              </div>

              <hr class="border-secondary border-opacity-25 my-3" />

              <div
                class="d-flex justify-content-between align-items-center mb-1"
              >
                <span class="fs-5 fw-bold text-dark">Thành tiền:</span>
                <span class="fs-4 fw-bold text-custom-red">{{
                  formatMoney(finalTotal)
                }}</span>
              </div>
              <div class="text-end text-muted small fst-italic">
                Số tiền phải thanh toán
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="d-flex justify-content-between mt-4 pb-5">
        <div>
          <button
            v-if="!isReadOnly"
            class="btn btn-outline-danger px-4 py-2 fw-medium"
            @click="handleHuyDon(selectedOrder)"
            :disabled="hasServedDish"
          >
            Hủy hóa đơn
          </button>
        </div>

        <div class="d-flex gap-2">
          <button
            class="btn btn-white border px-4 py-2 fw-medium"
            @click="onBack"
          >
            Quay lại
          </button>

          <button
            class="btn btn-print px-4 py-2 fw-medium text-white"
            @click="handlePrintOrder(selectedOrder?.id)"
          >
            In hóa đơn
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.main-offset {
  margin-left: 250px;
}

.page-title {
  color: #8b0000;
  font-size: 24px;
  font-weight: bold;
}

.text-custom-red {
  color: #8b0000 !important;
}

.border-bottom-red {
  border-bottom: 2px solid #8b0000 !important;
}

.btn-print {
  background-color: #8b0000;
  border: none;
}
.btn-print:hover {
  background-color: #b84747;
}

.bg-success-subtle {
  background-color: #d4edda !important;
}
.bg-warning-subtle {
  background-color: #fff3cd !important;
}
.text-warning {
  color: #856404 !important;
}

.cursor-pointer {
  cursor: pointer;
}
.border-dashed {
  border-style: dashed !important;
}

.last-no-border:last-child {
  border-bottom: none !important;
  padding-bottom: 0 !important;
  margin-bottom: 0 !important;
}
.border-bottom-dashed {
  border-bottom: 1px dashed #eee;
}
</style>

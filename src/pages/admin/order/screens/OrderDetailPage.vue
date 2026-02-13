<script setup>
import { onMounted, computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useOrderManager } from "./orderFunction";
import dayjs from "dayjs";
import "dayjs/locale/vi";
import relativeTime from "dayjs/plugin/relativeTime";
import logoUrl from "@/assets/images/logo_upscaled.jpg";

dayjs.locale("vi");
dayjs.extend(relativeTime);
const route = useRoute();
const router = useRouter();

const {
  selectedOrder,
  orderDetails,
  currentVAT,
  configHoldTime,
  configCancelLimit,
  handleViewDetail,
  openCancelModal,
  confirmCancelOrder,
  closeCancelModal,
  cancelModalState,
  handlePrintOrder,
  historyEvents,
  paymentHistory,
  invoiceDate,
  formatDate,
} = useOrderManager();

const formatMoney = (value) => {
  if (value === undefined || value === null) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
};

const formatDateTime = (dateString) => {
  if (!dateString) return "---";
  return dayjs(dateString).format("HH:mm - DD/MM/YYYY");
};

const getEventColor = (type) => {
  if (type === "create") return "bg-warning";
  if (type === "delete") return "bg-danger";
  if (type === "payment") return "bg-warning";
  return "bg-secondary";
};

// Tính tiền
const subTotal = computed(() => selectedOrder.value?.tongTienHangRaw || 0);
const discount = computed(() => selectedOrder.value?.soTienDaGiam || 0);
const deposit = computed(() => selectedOrder.value?.tienCocRaw || 0);
const finalTotal = computed(() => selectedOrder.value?.tongTienRaw || 0);

const appliedVAT = computed(() => {
  if (
    selectedOrder.value?.vatApDung !== undefined &&
    selectedOrder.value?.vatApDung !== null
  ) {
    return Number(selectedOrder.value.vatApDung);
  }
  return Number(currentVAT.value || 0);
});

const taxAmount = computed(() => {
  if (subTotal.value === 0) return 0;
  return finalTotal.value + deposit.value + discount.value - subTotal.value;
});


const isReadOnly = computed(
  () =>
    selectedOrder.value?.trangThai === "Đã hủy" ||
    selectedOrder.value?.trangThai === "Hoàn thành",
);

const hasServedDish = computed(() =>
  orderDetails.value?.some((item) => item.trangThaiCode === 2),
);

//Logic hien thi trang thai
const standardSteps = [
  { key: "Chờ nhận bàn", label: "Chờ nhận bàn", icon: "fa-hourglass-start" },
  { key: "Đang phục vụ", label: "Đang phục vụ", icon: "fa-utensils" },
  { key: "Hoàn thành", label: "Hoàn thành", icon: "fa-check" },
];

const currentStepIndex = computed(() => {
  const status = selectedOrder.value?.trangThai;
  if (status === "Đã hủy") return -1;
  return standardSteps.findIndex((s) => s.key === status);
});

const isCancelled = computed(() => selectedOrder.value?.trangThai === "Đã hủy");


const onBack = () => router.push({ name: "orderManager" });

onMounted(async () => {
  const orderDbId = route.params.id;
  if (orderDbId) {
    await handleViewDetail(orderDbId);
  }
});
</script>

<template>
  <div class="d-flex bg-light min-vh-100">
    <main class="flex-grow-1 p-4 main-offset">
      <h1 class="page-title mb-4">Quản lý hóa đơn chi tiết</h1>

      <!-- Thong tin chung -->

      <div class="card border-0 shadow-sm mb-4">
        <div class="card border-0 shadow-sm mb-4">
          <div class="card-header bg-white border-bottom py-3">
            <span class="fw-bold"
              ><i class="fa-solid fa-clock-rotate-left me-2"></i>Trạng thái hóa
              đơn:
            </span>
            <span class="text-custom-red fw-bold ms-2">{{
              selectedOrder?.id
            }}</span>
          </div>
          <div class="card-body py-4">
            <div v-if="isCancelled" class="text-center">
              <div
                class="d-inline-flex align-items-center justify-content-center bg-danger text-white rounded-circle mb-2"
                style="width: 50px; height: 50px"
              >
                <i class="fa-solid fa-xmark fs-4"></i>
              </div>
              <h5 class="fw-bold text-danger">Đã Hủy</h5>
            </div>

            <div
              v-else
              class="stepper-wrapper d-flex justify-content-between position-relative w-75 mx-auto"
            >
              <div
                class="position-absolute top-50 start-0 translate-middle-y bg-secondary bg-opacity-25 w-100"
                style="height: 3px; z-index: 0"
              ></div>

              <div
                class="position-absolute top-50 start-0 translate-middle-y bg-success"
                style="height: 3px; z-index: 0; transition: width 0.5s"
                :style="{
                  width:
                    (currentStepIndex / (standardSteps.length - 1)) * 100 + '%',
                }"
              ></div>

              <div
                v-for="(step, index) in standardSteps"
                :key="index"
                class="step-item text-center position-relative"
                style="z-index: 1"
              >
                <div
                  class="step-circle d-flex align-items-center justify-content-center rounded-circle border border-3 mb-2 mx-auto"
                  :class="
                    index <= currentStepIndex
                      ? 'bg-success border-success text-white'
                      : 'bg-white border-secondary border-opacity-25 text-muted'
                  "
                  style="width: 40px; height: 40px; transition: all 0.3s"
                >
                  <i :class="['fa-solid', step.icon]"></i>
                </div>
                <div
                  class="step-label fw-bold small"
                  :class="
                    index <= currentStepIndex ? 'text-success' : 'text-muted'
                  "
                >
                  {{ step.label }}
                </div>
              </div>
            </div>
          </div>
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
              <label class="d-block text-muted small mb-1">Tiền cọc</label>
              <p class="mb-0 fw-bold">{{ selectedOrder?.tienCoc }}</p>
            </div>
            <div class="col-md">
              <label class="d-block text-muted small mb-1"
                >Trạng thái hoàn tiền</label
              >
              <p class="mb-0 fw-bold">
                {{ selectedOrder?.trangThaiHoanTien }}
              </p>
            </div>
            <div class="col-md">
              <label class="d-block text-muted small mb-1">Thời gian tạo</label>
              <p class="mb-0 fw-medium">{{ selectedOrder?.ngayTao }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Bảng món -->

      <div class="card border-0 shadow-sm mb-4">
        <div class="card-header bg-white border-bottom py-3 fw-bold">
          🍴 Thông tin món đã đặt
        </div>
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table align-middle mb-0">
              <thead class="bg-custom-red text-white">
                <tr>
                  <th class="text-center py-3" style="width: 5%">STT</th>
                  <th class="py-3" style="width: 40%">TÊN MÓN ĂN</th>
                  <th class="text-center py-3" style="width: 10%">SỐ LƯỢNG</th>
                  <th class="text-end py-3" style="width: 15%">ĐƠN GIÁ</th>
                  <th class="text-end py-3" style="width: 15%">THÀNH TIỀN</th>
                  <th class="text-center py-3" style="width: 15%">
                    TRẠNG THÁI
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in orderDetails" :key="index">
                  <td class="fw-bold text-center">{{ index + 1 }}</td>
                  <td>
                    <div class="d-flex flex-column">
                      <span class="fw-medium">{{ item.tenMon }}</span>
                      <small
                        v-if="item.ghiChu"
                        class="text-custom-red"
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
                  <td class="text-center">
                    <span
                      class="badge px-2 py-1"
                      :class="
                        item.trangThaiCode === 2
                          ? 'bg-success-subtle text-success'
                          : item.trangThaiCode === 1
                            ? 'bg-warning-subtle text-warning'
                            : 'bg-custom-red-subtle text-custom-red'
                      "
                    >
                      {{ item.trangThaiText }}
                    </span>
                  </td>
                </tr>

                <tr v-if="!orderDetails || orderDetails.length === 0">
                  <td colspan="6" class="text-center py-5 text-muted">
                    <div class="d-flex flex-column align-items-center">
                      <i class="fa-solid fa-utensils fs-3 mb-2 opacity-25"></i>
                      <span>Chưa có món ăn nào được gọi</span>
                    </div>
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
            <div
              class="card-body p-3 h-100"
              style="max-height: 310px; overflow-y: auto"
            >
              <div
                v-if="!historyEvents || historyEvents.length === 0"
                class="text-center text-muted py-4"
              >
                <small>Chưa có lịch sử ghi nhận</small>
              </div>
              <div v-else>
                <div
                  v-for="(event, index) in historyEvents"
                  :key="index"
                  class="d-flex mb-3 position-relative"
                >
                  <div class="flex-shrink-0 mt-1">
                    <div
                      class="rounded-circle"
                      :class="getEventColor(event.type)"
                      style="width: 12px; height: 12px"
                    ></div>
                  </div>
                  <div class="ms-3">
                    <div class="fw-bold small">{{ event.title }}</div>
                    <div class="text-muted" style="font-size: 0.8rem">
                      {{ event.time }} -
                      <span class="fw-medium">{{ event.user }}</span>
                    </div>
                    <div
                      v-if="event.detail"
                      class="text-secondary fst-italic mt-1"
                      style="font-size: 0.85rem"
                    >
                      "{{ event.detail }}"
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          
          <div
            v-if="paymentHistory && paymentHistory.length > 0"
            class="card border-0 shadow-sm"
          >
            <div class="card-header bg-white border-bottom py-3 fw-bold">
              💳 Lịch sử thanh toán
            </div>
            <div class="card-body p-3">
              <div
                v-for="(pay, index) in paymentHistory"
                :key="index"
                class="d-flex align-items-start mb-3 pb-3 border-bottom-dashed last-no-border"
              >
                <div class="me-3">
                  <div
                    class="bg-warning-subtle text-warning rounded-circle d-flex align-items-center justify-content-center"
                    style="width: 32px; height: 32px"
                  >
                    <i class="fa-solid fa-check small"></i>
                  </div>
                </div>
                <div class="flex-grow-1">
                  <div class="d-flex justify-content-between">
                    <span class="fw-bold text-warning">
                      {{ formatMoney(pay.soTienThanhToan) }}
                    </span>
                    <span class="text-muted small">{{
                      formatDateTime(pay.ngayThanhToan)
                    }}</span>
                  </div>
                  <div
                    class="d-flex justify-content-between align-items-center mt-1"
                  >
                    <span class="small text-dark">
                      {{ pay.tenPhuongThuc }}
                      <span class="badge bg-warning ms-1">Thanh toán</span>
                    </span>
                  </div>
                  <div class="small text-muted fst-italic mt-1">
                    Mã GD: {{ pay.maGiaoDich || "---" }}
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
                  >Thuế VAT ({{ appliedVAT }}%):</span
                >
                <span class="fw-bold">{{ formatMoney(taxAmount) }}</span>
              </div>

              <div v-if="discount > 0">
                <div class="d-flex justify-content-between mb-1">
                  <span class="text-muted fw-medium">Giảm giá:</span>
                  <span class="fw-bold text-custom-red"
                    >({{ formatMoney(discount) }})</span
                  >
                </div>
                <div class="text-end text-muted small mb-3 fst-italic">
                  Khuyến mãi áp dụng
                </div>
              </div>

              <hr class="border-secondary border-opacity-25 my-3" />

              <div
                v-if="deposit > 0 && subTotal > 0"
                class="d-flex justify-content-between mb-3"
              >
                <span class="text-muted fw-medium">Đã đặt cọc:</span>
                <span class="fw-bold text-custom-red"
                  >- {{ formatMoney(deposit) }}</span
                >
              </div>

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
            class="btn btn-outline-custom px-4 py-2 fw-medium"
            @click="openCancelModal(selectedOrder)"
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
            :disabled="selectedOrder?.trangThai === 'Đã hủy'"
          >
            In hóa đơn
          </button>
        </div>
      </div>

      <!-- Thông báo hủy hóa đơn -->

      <div
        v-if="cancelModalState.isOpen"
        class="modal-backdrop fade show"
        style="z-index: 1050"
      ></div>
      <div
        v-if="cancelModalState.isOpen"
        class="modal fade show d-block"
        tabindex="-1"
        style="z-index: 1055"
      >
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content">
            
            <!-- Không cọc -->

            <div class="modal-header bg-custom-red text-white">
              <h5 class="modal-title">
                Xác nhận hủy hóa đơn: {{ cancelModalState.orderData?.id }}
              </h5>
              <button
                type="button"
                class="btn-close btn-close-white"
                @click="closeCancelModal"
              ></button>
            </div>

            <!-- Có cọc -->

            <div class="modal-body">
              <div
                v-if="cancelModalState.isDeposit"
                class="alert alert-brand d-flex align-items-center mb-3 py-2"
              >
                <i class="fas fa-info-circle me-2 fs-5"></i>
                <div class="small">
                  <strong>Quy định hiện tại:</strong><br />
                  - Thời gian giữ bàn: <b>{{ configHoldTime }} phút</b>.<br />
                  - Hủy trước <b>{{ configCancelLimit }} giờ</b>: Được hoàn 100%
                  cọc.
                </div>
              </div>
              <div
                v-if="cancelModalState.isWarning"
                class="alert alert-warning d-flex align-items-start border-warning mb-3"
              >
                <i
                  class="fas fa-exclamation-triangle mt-1 me-2 text-warning"
                ></i>
                <div>
                  <strong>Cảnh báo hủy đơn:</strong><br />
                  {{ cancelModalState.warningMessage }}
                </div>
              </div>

              <div
                v-if="cancelModalState.isDeposit"
                class="mb-3 p-2 bg-light rounded border"
              >
                <span
                  >Khách đã cọc:
                  <strong class="text-custom-red">{{
                    cancelModalState.orderData?.tienCoc
                  }}</strong></span
                >
              </div>

              <div class="mb-3">
                <label class="form-label fw-bold"
                  >Lý do hủy đơn <span class="text-custom-red">*</span></label
                >
                <textarea
                  v-model="cancelModalState.reason"
                  class="form-control"
                  rows="3"
                  placeholder="Nhập chi tiết lý do (VD: Khách đổi ý, Hết bàn, ...)"
                ></textarea>
              </div>
            </div>

            <div class="modal-footer d-flex justify-content-between">
              <button
                type="button"
                class="btn btn-secondary"
                @click="closeCancelModal"
              >
                Đóng
              </button>

              <div class="d-flex gap-2">
                <template v-if="!cancelModalState.isSafe">
                  <button
                    type="button"
                    class="btn btn-outline-custom"
                    @click="confirmCancelOrder('khach')"
                    title="Khách vi phạm -> Mất cọc"
                  >
                    Lỗi do Khách <br />
                    <small style="font-size: 0.7rem">(Không hoàn cọc)</small>
                  </button>

                  <button
                    type="button"
                    class="btn btn-custom"
                    @click="confirmCancelOrder('quan')"
                    title="Lỗi quán -> Hoàn cọc"
                  >
                    Lỗi do Quán <br />
                    <small style="font-size: 0.7rem">(Hoàn cọc 100%)</small>
                  </button>
                </template>

                <template v-else>
                  <div
                    class="d-flex align-items-center me-2 text-warning small fw-bold"
                  >
                    <i class="fa-solid fa-check-circle me-1"></i> Hủy đúng hạn
                  </div>
                  <button
                    type="button"
                    class="btn btn-custom px-4"
                    @click="confirmCancelOrder('quan')"
                    title="Hủy đúng quy định -> Hoàn tiền"
                  >
                    Xác nhận Hủy đơn <br />
                    <small style="font-size: 0.7rem">(Hoàn cọc 100%)</small>
                  </button>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>

  <!-- Hóa đơn in -->
  <div style="position: absolute; left: -9999px; top: -9999px">
    <div id="invoice-template" class="invoice-wrapper">
      <div class="main-frame">
        <div class="d-flex justify-content-between align-items-start mb-4">
          <div
            class="d-flex flex-column align-items-center"
            style="width: 150px"
          >
            <img :src="logoUrl" alt="Logo" class="invoice-logo mb-2" />
            <span class="fw-bold text-custom-red text-uppercase small"
              >CozyPot</span
            >
          </div>

          <div class="text-center flex-grow-1 pt-2">
            <h1
              class="fw-bold text-uppercase mb-1"
              style="font-size: 26px; letter-spacing: 2px"
            >
              HÓA ĐƠN
            </h1>
            <p class="mb-0 small fst-italic">Mã HĐ: #{{ selectedOrder?.id }}</p>
          </div>

          <div class="text-end" style="width: 150px">
            <img
              src="https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=CozyPot-Invoice"
              alt="QR"
              style="
                width: 80px;
                height: 80px;
                border: 1px solid #000;
                padding: 2px;
              "
            />
          </div>
        </div>

        <div class="info-section mb-4">
          <div class="row g-0">
            <div class="col-6 pe-3">
              <div class="border-bottom border-dark fw-bold mb-2 pb-1">
                Hóa đơn cho:
              </div>
              <p class="mb-1 fw-bold">
                {{ selectedOrder?.khachHang || "Khách lẻ" }}
              </p>
              <p class="mb-0">SĐT: {{ selectedOrder?.sdt || "---" }}</p>
            </div>
            <div class="col-6 ps-3">
              <div class="border-bottom border-dark fw-bold mb-2 pb-1 text-end">
                Chi tiết:
              </div>
              <p class="mb-1 text-end">Ngày in: {{ invoiceDate }}</p>

              <p
                v-if="selectedOrder?.tienCocRaw > 0"
                class="mb-1 text-end text-muted fst-italic small"
              >
                (Ngày đặt: {{ formatDate(selectedOrder?.thoiGianDat) }})
              </p>
              <p class="mb-0 text-end">Bàn: {{ selectedOrder?.ban }}</p>
            </div>
          </div>
        </div>

        <h6 class="fw-bold mb-2 text-uppercase">Thông tin chi tiết</h6>

        <table class="table table-bordered border-dark invoice-table mb-4">
          <thead>
            <tr class="bg-light">
              <th class="text-center" style="width: 50px">STT</th>
              <th class="text-start">Tên dịch vụ / Món ăn</th>
              <th class="text-center" style="width: 70px">SL</th>
              <th class="text-end" style="width: 120px">Đơn giá</th>
              <th class="text-end" style="width: 130px">Tổng tiền</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in orderDetails" :key="index">
              <td class="text-center fw-bold">{{ index + 1 }}</td>
              <td>
                <span class="fw-medium">{{ item.tenMon }}</span>
                <div v-if="item.idSetLau" class="small text-muted fst-italic">
                  (Set lẩu)
                </div>
              </td>
              <td class="text-center fw-bold">{{ item.soLuong }}</td>
              <td class="text-end">
                {{ formatMoney(item.donGia).replace(" ₫", "") }}
              </td>
              <td class="text-end fw-bold">
                {{ formatMoney(item.thanhTien).replace(" ₫", "") }}
              </td>
            </tr>
            <tr v-if="orderDetails.length < 5">
              <td colspan="5" style="height: 30px"></td>
            </tr>
          </tbody>
        </table>

        <div class="row">
          <div class="col-12">
            <h6 class="fw-bold mb-2 text-uppercase">Chi phí khác & Tổng kết</h6>
            <table class="table table-bordered border-dark invoice-table">
              <tbody>
                <tr>
                  <td class="fw-medium" style="width: 70%">Tổng tiền hàng:</td>
                  <td class="text-end fw-bold">{{ formatMoney(subTotal) }}</td>
                </tr>
                <tr>
                  <td class="fw-medium" style="width: 70%">
                    Thuế VAT ({{ appliedVAT }}%):
                  </td>
                  <td class="text-end fw-bold">{{ formatMoney(taxAmount) }}</td>
                </tr>
                <tr v-if="discount > 0">
                  <td class="fw-medium">Giảm giá / Voucher:</td>
                  <td class="text-end fw-bold text-danger">
                    - {{ formatMoney(discount) }}
                  </td>
                </tr>
                <tr v-if="deposit > 0">
                  <td class="fw-medium">Đã đặt cọc:</td>
                  <td class="text-end fw-bold text-success">
                    - {{ formatMoney(deposit) }}
                  </td>
                </tr>

                <tr style="background-color: #f8f9fa">
                  <td class="align-middle">
                    <span class="fw-bold fs-6 text-uppercase"
                      >Tổng thanh toán:</span
                    >
                    <div class="small fst-italic text-muted mt-1">
                      (Đã bao gồm VAT và các khoản giảm trừ)
                    </div>
                  </td>
                  <td class="text-end align-middle">
                    <span class="fw-bold fs-4 text-custom-red">{{
                      formatMoney(finalTotal)
                    }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="text-center mt-5 pt-4">
          <p class="mb-1 fw-bold fst-italic">
            Cảm ơn quý khách và hẹn gặp lại!
          </p>
          <div class="small text-muted">
            <p class="mb-0">COZYPOT RESTAURANT | 123 Đường FPT, Hà Nội</p>
            <p class="mb-0">
              Hotline: 1900 888 888 | Email: contact@cozypot.vn
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.text-custom-red { color: #8b0000 !important; }
.bg-custom-red { background-color: #8b0000 !important; }
.border-bottom-red { border-bottom: 2px solid #8b0000 !important; }
.page-title { color: #8b0000; font-size: 24px; font-weight: bold; }

.btn-custom { background-color: #8b0000; border-color: #8b0000; color: white; }
.btn-custom:hover { background-color: #a00000; border-color: #a00000; color: white; }
.btn-outline-custom { color: #8b0000; border-color: #8b0000; background-color: transparent; }
.btn-outline-custom:hover { background-color: #8b0000; color: white; }
.btn-print { background-color: #8b0000; border: none; }
.btn-print:hover { background-color: #b84747; }
.btn-close-white { filter: invert(1) grayscale(100%) brightness(200%); }

.bg-success-subtle { background-color: #d4edda !important; }
.bg-warning-subtle { background-color: #fff3cd !important; }
.text-warning { color: #856404 !important; }
.alert-brand { background-color: rgba(139, 0, 0, 0.1); border: 1px solid rgba(139, 0, 0, 0.3); color: #8b0000; }
.alert-brand i { color: #8b0000 !important; }

.border-dashed { border-style: dashed !important; }
.last-no-border:last-child { border-bottom: none !important; padding-bottom: 0 !important; margin-bottom: 0 !important; }
.border-bottom-dashed { border-bottom: 1px dashed #eee; }
.modal { background-color: rgba(0, 0, 0, 0.5); }

/* Style bảng chính  */
.table-responsive { overflow-x: auto; }
.table thead th { vertical-align: middle; }

/* Thanh trang thái */
.stepper-wrapper { margin-top: 10px; margin-bottom: 10px; }
.step-item { min-width: 100px; }
.step-circle { background-color: #fff; transition: all 0.4s ease-in-out; }
.position-absolute { transition: all 0.4s ease-in-out; }

/* Hóa đơn in */
.invoice-wrapper { width: 760px; background: white; padding: 20px; box-sizing: border-box; }
.main-frame { border: 2px solid #000; border-radius: 15px; padding: 30px; min-height: 900px; position: relative; font-family: "Times New Roman", serif; color: #000; }
.invoice-logo { width: 80px; height: 80px; object-fit: contain; }
.invoice-table { width: 100%; margin-bottom: 20px; }
.invoice-table thead th { background-color: #f0f0f0; border-top: 2px solid #000 !important; border-bottom: 2px solid #000 !important; border-left: 1px solid #000; border-right: 1px solid #000; text-transform: uppercase; font-size: 13px; font-weight: bold; vertical-align: middle; }
.invoice-table tbody td { border: 1px solid #000; padding: 8px 10px; vertical-align: middle; font-size: 14px; }
.table-bordered > :not(caption) > * { border-width: 1px; }
</style>
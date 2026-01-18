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

// Tinh tien
const subTotal = computed(() => {
  return (
    orderDetails.value?.reduce((sum, item) => sum + (item.thanhTien || 0), 0) ||
    0
  );
});
const taxAmount = computed(() => subTotal.value * (currentVAT.value / 100));
const discount = computed(() => selectedOrder.value?.soTienDaGiam || 0);
const finalTotal = computed(
  () => subTotal.value + taxAmount.value - discount.value,
);

// Trạng thái
const isReadOnly = computed(
  () =>
    selectedOrder.value?.trangThai === "Đã hủy" ||
    selectedOrder.value?.trangThai === "Hoàn thành",
);
const hasServedDish = computed(() =>
  orderDetails.value?.some((item) => item.trangThaiCode === 2),
);

const onBack = () => router.push({ name: "orderManager" });
</script>

<template>
  <div class="app-layout">
    <Sidebar />
    <main class="main-content">
      <h1 class="page-title">Quản lý hóa đơn chi tiết</h1>

      <section class="info-card">
        <div class="card-header">
          Thông tin hóa đơn:
          <span class="highlight">{{ selectedOrder?.id }}</span>
        </div>
        <div class="card-body grid-container">
          <div class="info-group">
            <label>Khách hàng</label>
            <p>{{ selectedOrder?.khachHang || "Khách vãng lai" }}</p>
          </div>
          <div class="info-group">
            <label>Số điện thoại</label>
            <p>{{ selectedOrder?.sdt || "---" }}</p>
          </div>
          <div class="info-group">
            <label>Bàn</label>
            <p class="font-bold">{{ selectedOrder?.ban }}</p>
          </div>
          <div class="info-group">
            <label>Trạng thái</label>
            <p :class="['status-text', selectedOrder?.trangThai]">
              {{ selectedOrder?.trangThai }}
            </p>
          </div>
          <div class="info-group">
            <label>Thời gian tạo</label>
            <p>{{ selectedOrder?.ngayTao }}</p>
          </div>
        </div>
      </section>

      <section class="info-card mb-4">
        <div class="card-header">
          <span>🍴 Thông tin món đã đặt</span>
        </div>
        <div class="card-body p-0">
          <table class="detail-table">
            <thead>
              <tr>
                <th width="50" class="text-center">STT</th>
                <th>Tên món ăn</th>
                <th class="text-center">Số lượng</th>
                <th class="text-right">Đơn giá</th>
                <th class="text-right">Thành tiền</th>
                <th class="text-center">Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in orderDetails" :key="index">
                <td class="text-center">{{ index + 1 }}</td>
                <td>
                  <div class="food-info">
                    <span class="food-name">{{ item.tenMon }}</span>
                    <small v-if="item.ghiChu" class="food-note"
                      >Ghi chú: {{ item.ghiChu }}</small
                    >
                  </div>
                </td>
                <td class="text-center">{{ item.soLuong }}</td>
                <td class="text-right">{{ formatMoney(item.donGia) }}</td>
                <td class="text-right font-bold">
                  {{ formatMoney(item.thanhTien) }}
                </td>
                <td class="text-center">
                  <span
                    :class="[
                      'badge',
                      item.trangThaiCode === 2 ? 'bg-green' : 'bg-yellow',
                    ]"
                  >
                    {{ item.trangThaiText }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <div class="bottom-section">
        <section class="info-card history-card">
          <div class="card-header">
            <span>🕒 Lịch sử hóa đơn</span>
          </div>
          <div class="card-body">
            <div
              class="empty-history text-center text-muted"
              style="padding: 20px; color: #999"
            >
              <p>... (Khu vực hiển thị Timeline) ...</p>
            </div>
          </div>
        </section>

        <section class="info-card summary-card">
          <div class="card-header">
            <span>💰 Tổng kết đơn hàng</span>
          </div>
          <div class="card-body">
            <div class="summary-line">
              <span class="label">Tổng tiền hàng:</span>
              <span class="value">{{ formatMoney(subTotal) }}</span>
            </div>
            <div class="summary-note">Giá trị sản phẩm</div>

            <hr class="dashed-line" />

            <div class="summary-line">
              <span class="label">Thuế VAT ({{ currentVAT }}%):</span>
              <span class="value">{{ formatMoney(taxAmount) }}</span>
            </div>

            <div class="summary-line" v-if="discount > 0">
              <span class="label">Giảm giá:</span>
              <span class="value text-red">({{ formatMoney(discount) }})</span>
            </div>
            <div class="summary-note" v-if="discount > 0">
              Khuyến mãi áp dụng
            </div>

            <hr class="solid-line" />

            <div class="summary-line total-group">
              <span class="label">Thành tiền:</span>
              <span class="value final-price">{{
                formatMoney(finalTotal)
              }}</span>
            </div>
            <div class="summary-note text-right">Số tiền phải thanh toán</div>
          </div>
        </section>
      </div>

      <div class="page-footer">
        <div class="footer-left">
          <button
            v-if="!isReadOnly"
            class="btn-danger-outline"
            @click="handleHuyDon(selectedOrder)"
            :disabled="hasServedDish"
          >
            Hủy hóa đơn
          </button>
        </div>
        <div class="footer-right">
          <button class="btn-outline" @click="onBack">Quay lại</button>
          <button
            class="btn-primary"
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
.app-layout {
  min-height: 100vh;
  background-color: #f8f9fa;
}
.main-content {
  margin-left: 250px;
  width: calc(100% - 250px);
  padding: 20px 30px;
  background-color: #fff;
}

.page-title {
  color: #8b0000;
  font-size: 24px;
  margin-bottom: 20px;
  font-weight: bold;
}

.info-card {
  background: white;
  border-radius: 8px;
  border: 1px solid #eee;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.card-header {
  background: #fdfdfd;
  padding: 15px 25px;
  border-bottom: 1px solid #eee;
  font-weight: bold;
  font-size: 16px;
  display: flex;
  align-items: center;
}

.highlight {
  color: #8b0000;
  margin-left: 5px;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  padding: 25px;
}
.info-group label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 5px;
}
.info-group p {
  margin: 0;
  font-size: 15px;
  color: #333;
}
.font-bold {
  font-weight: bold;
}

.detail-table {
  width: 100%;
  border-collapse: collapse;
}
.detail-table th {
  background: #fcfcfc;
  padding: 12px;
  text-align: left;
  border-bottom: 2px solid #8b0000;
  color: #8b0000;
  font-size: 14px;
  text-transform: uppercase;
  font-weight: 600;
}
.detail-table td {
  padding: 15px 12px;
  border-bottom: 1px solid #eee;
  font-size: 14px;
  color: #333;
}
.food-info {
  display: flex;
  flex-direction: column;
}
.food-note {
  color: #dc3545;
  font-size: 12px;
}

.payment-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 25px;
  background-color: #fff;
}
.payment-summary {
  width: 320px;
}
.summary-line {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #555;
  font-size: 14px;
}
.total-line {
  border-top: 1px solid #eee;
  padding-top: 15px;
  margin-top: 15px;
  font-weight: bold;
}
.final-price {
  color: #8b0000;
  font-size: 22px;
}

.page-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
  padding-bottom: 50px;
}
.btn-primary {
  background: #28a745;
  color: white;
  border: none;
  padding: 10px 25px;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
}
.btn-primary:hover {
  opacity: 0.9;
}

.btn-outline {
  background: white;
  border: 1px solid #ccc;
  color: #333;
  padding: 10px 25px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
  font-weight: 500;
}
.btn-outline:hover {
  background-color: #f0f0f0;
}

.btn-danger-outline {
  background: white;
  border: 1px solid #dc3545;
  color: #dc3545;
  padding: 10px 25px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}
.btn-danger-outline:hover {
  background-color: #fff5f5;
}
.btn-danger-outline:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background-color: #eee;
  border-color: #ccc;
  color: #999;
}

.badge {
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: bold;
}
.bg-yellow {
  background: #fff3cd;
  color: #856404;
}
.bg-green {
  background: #d4edda;
  color: #155724;
}
.status-text.Hoàn\ thành {
  color: #28a745;
  font-weight: bold;
}
.status-text.Đã\ hủy {
  color: #dc3545;
  font-weight: bold;
}
.status-text.Đã\ xác\ nhận {
  color: #ff9800;
  font-weight: bold;
}

.text-center {
  text-align: center;
}
.text-right {
  text-align: right;
}
.mt-4 {
  margin-top: 20px;
}
.bottom-section {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}
.history-card {
  flex: 1;
}
.summary-card {
  flex: 1;
}

.mb-4 {
  margin-bottom: 20px;
}
.p-0 {
  padding: 0 !important;
}

.summary-card .card-body {
  padding: 20px 25px;
}

.summary-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
  font-size: 14px;
}

.summary-line .label {
  color: #555;
  font-weight: 500;
}

.summary-line .value {
  font-weight: 600;
  color: #333;
}

.summary-note {
  font-size: 12px;
  color: #999;
  text-align: right;
  margin-bottom: 10px;
  font-style: italic;
}

.dashed-line {
  border: 0;
  border-top: 1px dashed #eee;
  margin: 10px 0;
}
.solid-line {
  border: 0;
  border-top: 1px solid #eee;
  margin: 15px 0;
}

.total-group .label {
  font-size: 16px;
  font-weight: 700;
}
.total-group .final-price {
  font-size: 24px;
  color: #dc3545;
  font-weight: 800;
}

.text-red {
  color: #dc3545 !important;
}
.text-right {
  text-align: right;
}
.btn-print {
    background: #17a2b8;
    color: white;
    border: none;
    padding: 10px 25px;
    border-radius: 4px;
    font-weight: 500;
    cursor: pointer;
}
</style>

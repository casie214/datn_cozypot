<script setup>
import { defineProps, defineEmits, computed } from "vue";
import { useRouter } from "vue-router";

const props = defineProps({
  isOpen: Boolean,
  orderData: Object,
  orderDetailList: Array,
  vatRate: { type: Number, default: 0 },
});

const emit = defineEmits([
  "close",
  "update-served",
  "update-all-served",
  "cancel-order",
  "remove-item",
]);
const router = useRouter();

const formatMoney = (value) => {
  if (value === undefined || value === null) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
};

const subTotal = computed(() => {
  if (!props.orderDetailList || props.orderDetailList.length === 0) return 0;
  return props.orderDetailList.reduce(
    (sum, item) => sum + (item.thanhTien || 0),
    0
  );
});

const taxAmount = computed(() => subTotal.value * (props.vatRate / 100));
const discount = computed(() => props.orderData?.soTienDaGiam || 0);
const total = computed(() => subTotal.value + taxAmount.value - discount.value);

const isCancelled = computed(() => props.orderData?.trangThai === "Đã hủy");
const isCompleted = computed(() => props.orderData?.trangThai === "Hoàn thành");
const isReadOnly = computed(() => isCancelled.value || isCompleted.value);

const hasServedDish = computed(() => {
  return props.orderDetailList?.some((item) => item.trangThaiCode === 2);
});

const isAllServed = computed(() => {
  if (!props.orderDetailList || props.orderDetailList.length === 0) return false;
  return props.orderDetailList.every((item) => item.trangThaiCode === 2);
});

const hasUnservedDish = computed(() => {
  if (!props.orderDetailList) return false;
  return props.orderDetailList.some((item) => item.trangThaiCode === 1);
});

const goToPayment = () => {
  if (props.orderData?.dbId) {
    emit("close");
    router.push({
      name: "paymentScreen",
      params: { id: props.orderData.dbId },
    });
  } else {
    alert("Lỗi: Không lấy được ID Database!");
  }
};

const goToAddFood = () => {
  if (props.orderData?.id) {
    emit("close");
    router.push({ name: "addFoodScreen", params: { id: props.orderData.id } });
  }
};

const handleCancelOrder = () => {
  if (hasServedDish.value) {
    alert("Không thể hủy hóa đơn vì đã có món ăn được phục vụ lên bàn!");
    return;
  }
  emit("cancel-order", props.orderData);
};
</script>

<template>
  <div v-if="isOpen" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);" @click.self="emit('close')">
    <div class="modal-dialog modal-dialog-centered modal-lg"> <div class="modal-content shadow">
        
        <div class="modal-header text-white" style="background-color: #8b0000;">
          <div class="d-flex flex-column">
            <h5 class="modal-title fw-bold mb-0">Chi tiết đơn hàng</h5>
            <small class="opacity-75">{{ orderData?.id || "---" }}</small>
          </div>
          <button type="button" class="btn-close btn-close-white" @click="emit('close')"></button>
        </div>

        <div class="modal-body custom-scroll">
          
          <div class="card bg-light border-0 mb-3">
            <div class="card-body py-2">
              <div class="row g-2">
                <div class="col-6">
                  <small class="text-muted d-block">Khách hàng</small>
                  <span class="fw-bold">{{ orderData?.khachHang || "Khách vãng lai" }}</span>
                </div>
                <div class="col-6">
                  <small class="text-muted d-block">Bàn</small>
                  <span class="fw-bold">{{ orderData?.ban || "---" }}</span>
                </div>
                <div class="col-6">
                  <small class="text-muted d-block">Trạng thái</small>
                  <span class="fw-bold" 
                    :class="{
                      'text-danger': isCancelled,
                      'text-success': isCompleted,
                      'text-warning': !isReadOnly
                    }">
                    {{ orderData?.trangThai || "---" }}
                  </span>
                </div>
                <div class="col-6">
                  <small class="text-muted d-block">Ngày tạo</small>
                  <span class="fw-bold">{{ orderData?.ngayTao || "---" }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="d-flex justify-content-between align-items-center mb-2">
            <div class="h6 mb-0 d-flex align-items-center gap-2">
              <span>🍴</span> <b>Danh sách món</b>
            </div>
            <div v-if="!isReadOnly" class="d-flex gap-2">
              <button class="btn btn-sm btn-outline-danger fw-bold" @click="goToAddFood">
                ➕ Thêm món
              </button>
              <button 
                v-if="!isAllServed"
                class="btn btn-sm text-white" 
                style="background-color: #8b0000;"
                @click="emit('update-all-served')"
              >
                ✔ Đã lên tất cả
              </button>
            </div>
          </div>

          <div v-if="!isCancelled" class="list-group mb-3">
            <div 
              v-for="(item, index) in orderDetailList" 
              :key="item.id || index"
              class="list-group-item d-flex align-items-center gap-3 p-2 position-relative"
            >
              <input 
                v-if="!isReadOnly"
                type="checkbox" 
                class="form-check-input mt-0" 
                style="transform: scale(1.2); cursor: pointer;"
                :checked="item.trangThaiCode === 2"
                :disabled="item.trangThaiCode === 2"
                @change="emit('update-served', item.id)"
              />

              <div class="food-img-wrapper rounded bg-secondary-subtle d-flex align-items-center justify-content-center" style="width: 50px; height: 50px; overflow: hidden;">
                <img v-if="item.hinhAnh" :src="item.hinhAnh" class="w-100 h-100 object-fit-cover" alt="img" />
                <span v-else class="text-muted fs-5">🍴</span>
              </div>

              <div class="flex-grow-1">
                <div class="d-flex align-items-center gap-2 mb-1">
                  <span class="fw-bold text-dark">{{ item.tenMon }}</span>
                  <span class="badge" 
                    :class="item.trangThaiCode === 2 ? 'bg-success' : 'bg-warning text-dark'">
                    {{ item.trangThaiText }}
                  </span>
                </div>
                
                <div class="small text-muted d-flex gap-3 flex-wrap">
                  <span v-if="item.ghiChu" class="text-danger w-100 mb-1">Note: {{ item.ghiChu }}</span>
                  <span>SL: <b>{{ item.soLuong }}</b></span>
                  <span>Đơn giá: {{ formatMoney(item.donGia) }}</span>
                </div>
              </div>

              <div class="text-end">
                <div class="fw-bold">{{ formatMoney(item.thanhTien) }}</div>
              </div>
              
              <button 
                v-if="!isReadOnly && item.trangThaiCode === 1"
                class="btn btn-sm text-muted position-absolute top-0 end-0 p-1 lh-1"
                title="Xóa món"
                @click="emit('remove-item', item.id)"
              >
                ✖
              </button>
            </div>
            
            <div v-if="!orderDetailList || orderDetailList.length === 0" class="text-center text-muted py-4">
              Chưa có món ăn nào.
            </div>
          </div>
          
          <div v-else class="alert alert-danger text-center fw-bold">
            ⚠️ Đơn hàng này đã bị hủy.
          </div>

          <div v-if="!isCancelled" class="card bg-light border">
            <div class="card-body p-3">
              <h6 class="card-title border-bottom pb-2 mb-2">Tổng kết</h6>
              <div class="d-flex justify-content-between mb-1">
                <span class="text-muted">Tạm tính:</span>
                <span class="fw-bold">{{ formatMoney(subTotal) }}</span>
              </div>
              <div class="d-flex justify-content-between mb-1">
                <span class="text-muted">Thuế ({{ vatRate }}%):</span>
                <span class="fw-bold">{{ formatMoney(taxAmount) }}</span>
              </div>
              <div class="d-flex justify-content-between mb-1" v-if="discount !== 0">
                <span class="text-muted">Giảm giá:</span>
                <span class="fw-bold text-success">-{{ formatMoney(discount) }}</span>
              </div>
              <hr class="my-2">
              <div class="d-flex justify-content-between fs-5">
                <span class="fw-bold">Tổng cộng:</span>
                <span class="fw-bold text-danger">{{ formatMoney(total) }}</span>
              </div>
            </div>
          </div>

        </div>

        <div class="modal-footer bg-light">
          <template v-if="isCompleted">
            <span class="text-success fw-bold me-auto">✅ Đơn hàng đã thanh toán thành công</span>
            <button class="btn btn-secondary" @click="emit('close')">Đóng</button>
          </template>

          <template v-else-if="isCancelled">
            <button class="btn btn-secondary" @click="emit('close')">Đóng</button>
          </template>

          <template v-else>
            <button 
              class="btn text-white fw-bold" 
              style="background-color: #8b0000;"
              :class="{ 'opacity-50': hasServedDish }"
              :disabled="hasServedDish"
              :title="hasServedDish ? 'Món đã lên bàn, không thể hủy đơn' : ''"
              @click="handleCancelOrder"
            >
              ✖ Hủy đơn
            </button>
            <button 
              class="btn btn-danger fw-bold"
              :disabled="hasUnservedDish"
              :title="hasUnservedDish ? 'Vui lòng xác nhận tất cả món đã lên bàn' : ''"
              @click="goToPayment"
            >
              💳 Tiến hành thanh toán
            </button>
          </template>
        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-body.custom-scroll {
  max-height: 60vh;
  overflow-y: auto;
}

.modal.show .modal-dialog {
  animation: slideIn 0.2s ease-out;
}
@keyframes slideIn {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.object-fit-cover {
  object-fit: cover;
}
</style>
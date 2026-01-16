<script setup>
import { defineProps, defineEmits, computed } from 'vue';
import { useRouter } from 'vue-router'; 

const props = defineProps({
  isOpen: Boolean,
  orderData: Object,
  orderDetailList: Array,
  vatRate: { type: Number, default: 0 } 
});

const emit = defineEmits(['close']);
const router = useRouter(); 

const formatMoney = (value) => {
    if (value === undefined || value === null) return '0 ₫';
    return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
};

// 1. Tạm tính
const subTotal = computed(() => {
    if (!props.orderDetailList || props.orderDetailList.length === 0) return 0;
    return props.orderDetailList.reduce((sum, item) => sum + (item.thanhTien || 0), 0);
});

// 2. Tiền thuế
const taxAmount = computed(() => {
    return subTotal.value * (props.vatRate / 100);
});

// 3. Giảm giá
const discount = computed(() => {
    return props.orderData?.soTienDaGiam || 0;
});

// 4. Tổng cộng
const total = computed(() => {
    return subTotal.value + taxAmount.value - discount.value;
});

// --- LOGIC TRẠNG THÁI ---
const isCancelled = computed(() => props.orderData?.trangThai === 'Đã hủy');
const isCompleted = computed(() => props.orderData?.trangThai === 'Hoàn thành');
const isReadOnly = computed(() => isCancelled.value || isCompleted.value);

const goToPayment = () => {
    if (props.orderData?.id) {
        emit('close');
        router.push({ name: 'paymentScreen', params: { id: props.orderData.id } });
    }
};

const goToAddFood = () => {
    if (props.orderData?.id) {
        emit('close'); 
        router.push({ name: 'addFoodScreen', params: { id: props.orderData.id } });
    }
};
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-content">
      
      <div class="modal-header">
        <div class="header-text">
            <h2 style="color: white;">Chi tiết đơn hàng</h2>
            <span class="order-id">{{ orderData?.id || '---' }}</span>
        </div>
        <button class="btn-close" @click="emit('close')">X</button>
      </div>

      <div class="modal-body">
        <div class="info-box">
            <div class="info-row">
                <div class="info-col">
                    <label>Khách hàng</label>
                    <b>{{ orderData?.khachHang || 'Khách vãng lai' }}</b>
                </div>
                <div class="info-col">
                    <label>Bàn</label>
                    <b>{{ orderData?.ban || '---' }}</b>
                </div>
            </div>
            <div class="info-row mt-2">
                <div class="info-col">
                    <label>Trạng thái</label>
                    <b :class="{'text-red': isCancelled, 'text-green': isCompleted, 'text-yellow': !isReadOnly}">
                        {{ orderData?.trangThai || '---' }}
                    </b>
                </div>
                <div class="info-col">
                    <label>Ngày tạo</label>
                    <b>{{ orderData?.ngayTao || '---' }}</b>
                </div>
            </div>
        </div>

        <div class="section-title">
            <div class="title-left">
                <span>🍴</span> <b>Danh sách món</b>
            </div>
            <div class="title-actions" v-if="!isReadOnly">
                <button class="btn-add-food" @click="goToAddFood">➕ Thêm món</button>
                <button class="btn-check-all">✔ Đã lên tất cả</button>
            </div>
        </div>

        <div class="food-list" v-if="!isCancelled">
            <div v-for="(item, index) in orderDetailList" :key="item.id || index" class="food-item">
                <input v-if="!isReadOnly" type="checkbox" class="checkbox" />
                
                <div class="food-img-placeholder">
                    <img v-if="item.hinhAnh" :src="item.hinhAnh" alt="img" class="food-img">
                    <span v-else>🍴</span>
                </div>
                <div class="food-info">
                    <div class="food-name-row">
                        <b>{{ item.tenMon }}</b>
                        <span class="badge-status" :class="{'bg-yellow': item.trangThaiCode === 1, 'bg-green': item.trangThaiCode === 2}">
                              {{ item.trangThaiText }}
                        </span>
                    </div>
                    <div class="food-meta">
                        <div v-if="item.ghiChu" class="food-note">Note: {{ item.ghiChu }}</div>
                        <span>SL: <b>{{ item.soLuong }}</b></span>
                        <span class="food-price-orig">Đơn giá: {{ formatMoney(item.donGia) }}</span>
                    </div>
                </div>
                <div class="food-total"><b>{{ formatMoney(item.thanhTien) }}</b></div>
                
                <button v-if="!isReadOnly" class="btn-remove-item">✖</button>
            </div>
            <div v-if="!orderDetailList || orderDetailList.length === 0" class="empty-food">Chưa có món ăn nào.</div>
        </div>
        <div v-else class="cancelled-message">⚠️ Đơn hàng này đã bị hủy.</div>

        <div class="summary-box" v-if="!isCancelled">
            <div class="summary-row"><span>Tổng kết</span></div>
            <div class="summary-row">
                <span class="label-light">Tạm tính:</span>
                <b>{{ formatMoney(subTotal) }}</b>
            </div>
            <div class="summary-row">
                <span class="label-light">Thuế ({{ vatRate }}%):</span>
                <b>{{ formatMoney(taxAmount) }}</b>
            </div>
            <div class="summary-row" v-if="discount !== 0">
                <span class="label-light">Giảm giá / Voucher:</span>
                <b class="text-green">-{{ formatMoney(discount) }}</b>
            </div>
            <div class="divider"></div>
            <div class="summary-row total-row">
                <b>Tổng cộng:</b>
                <b class="text-red">{{ formatMoney(total) }}</b>
            </div>
        </div>
      </div>

      <div class="modal-footer" v-if="!isReadOnly">
        <button class="btn-cancel-order">✖ Hủy đơn</button>
        <button class="btn-payment" @click="goToPayment">💳 Tiến hành thanh toán</button>
      </div>
      <div class="modal-footer completed-footer" v-else-if="isCompleted">
         <span class="success-text">✅ Đơn hàng đã thanh toán thành công</span>
         <button class="btn-close-modal" @click="emit('close')">Đóng</button>
      </div>
      <div class="modal-footer cancel-footer" v-else>
         <button class="btn-close-modal" @click="emit('close')">Đóng</button>
      </div>

    </div>
  </div>
</template>

<style scoped>

.text-green { color: #2e7d32 !important; }
.text-yellow { color: #fbc02d; }
.food-img { width:100%; height:100%; object-fit:cover; border-radius:4px; }
.food-note { color: #d32f2f; font-size: 11px; margin-bottom: 2px; }
.empty-food { text-align: center; color: #999; padding: 20px; }
.completed-footer { justify-content: space-between; background: #e8f5e9; }
.cancel-footer { justify-content: flex-end; }
.success-text { color: #2e7d32; font-weight: bold; align-self: center; display: flex; align-items: center; gap: 5px; }

.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); display: flex; justify-content: center; align-items: center; z-index: 9999; }
.modal-content { background: white; width: 700px; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.2); animation: slideIn 0.2s ease-out; }
.modal-header { background-color: #8B0000; color: white; padding: 15px 20px; display: flex; justify-content: space-between; align-items: flex-start; }
.header-text h2 { margin: 0; font-size: 18px; font-weight: bold; }
.order-id { font-size: 12px; opacity: 0.9; color: white; }
.btn-close { background: none; border: none; color: white; font-size: 20px; cursor: pointer; }
.modal-body { padding: 20px; background-color: white; max-height: 60vh; overflow-y: auto; }
.info-box { background: #f5f5f5; padding: 15px; border-radius: 6px; margin-bottom: 20px; }
.info-row { display: flex; justify-content: space-between; }
.info-col { width: 48%; display: flex; flex-direction: column; font-size: 13px; }
.info-col label { color: #666; margin-bottom: 2px; font-size: 12px; }
.mt-2 { margin-top: 10px; }
.section-title { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.title-left { font-size: 16px; display: flex; gap: 5px; align-items: center; }
.title-actions { display: flex; gap: 10px; }
.btn-check-all { background: #8B0000; color: white; border: none; padding: 5px 10px; border-radius: 4px; font-size: 12px; cursor: pointer; }
.btn-add-food { background: white; border: 1px solid #8B0000; color: #8B0000; padding: 5px 10px; border-radius: 4px; font-size: 12px; cursor: pointer; font-weight: bold; transition: 0.2s; }
.btn-add-food:hover { background: #ffebee; }
.food-item { display: flex; align-items: center; gap: 15px; border: 1px solid #eee; padding: 10px; border-radius: 6px; margin-bottom: 10px; position: relative; }
.food-img-placeholder { width: 50px; height: 50px; background: #eee; display: flex; align-items: center; justify-content: center; border-radius: 4px; font-size: 20px; color: #999; overflow: hidden;}
.food-info { flex: 1; display: flex; flex-direction: column; gap: 5px; }
.food-name-row { display: flex; align-items: center; gap: 10px; }
.badge-status { background: #eee; color: #555; padding: 2px 6px; border-radius: 4px; font-size: 10px; font-weight: bold; }
.bg-yellow { background-color: #fff3cd; color: #856404; }
.bg-green { background-color: #d4edda; color: #155724; }
.food-meta { font-size: 13px; color: #666; }
.food-total { font-size: 14px; font-weight: bold; margin-right: 20px; }
.btn-remove-item { background: none; border: none; color: #999; cursor: pointer; position: absolute; top: 5px; right: 5px; font-size: 16px; }
.btn-remove-item:hover { color: red; }
.cancelled-message { text-align: center; color: #dc3545; background-color: #fff5f5; padding: 20px; border: 1px dashed #dc3545; border-radius: 6px; margin-bottom: 20px; font-weight: bold; }
.summary-box { background: #f9f9f9; padding: 15px; border-radius: 6px; border: 1px solid #eee; }
.summary-row { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px; }
.label-light { color: #666; }
.divider { height: 1px; background: #ddd; margin: 10px 0; }
.total-row { font-size: 16px; margin-bottom: 0; }
.text-red { color: #D32F2F; font-size: 18px; }
.modal-footer { padding: 15px 20px; background: #f5f5f5; border-top: 1px solid #ddd; display: flex; justify-content: space-between; }
.btn-cancel-order { background: #8B0000; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; }
.btn-payment { background: #D32F2F; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; }
.btn-close-modal { background: #666; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; }
@keyframes slideIn { from { transform: scale(0.95); opacity: 0; } to { transform: scale(1); opacity: 1; } }
</style>
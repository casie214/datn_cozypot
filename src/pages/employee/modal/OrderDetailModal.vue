<script setup>
import { defineProps, defineEmits } from 'vue';
import { useRouter } from 'vue-router'; 

const props = defineProps({
  isOpen: Boolean,
  orderData: Object
});

const emit = defineEmits(['close']);
const router = useRouter(); 

// Dữ liệu mẫu danh sách món
const mockFoodItems = [
    { id: 1, name: 'Lẩu nấm', status: 'Chưa lên', quantity: 1, price: '280.000 đ' },
    { id: 2, name: 'Lẩu nấm', status: 'Chưa lên', quantity: 1, price: '280.000 đ' },
    { id: 3, name: 'Lẩu nấm', status: 'Chưa lên', quantity: 1, price: '280.000 đ' }
];

// Hàm chuyển sang trang Thanh toán
const goToPayment = () => {
    if (props.orderData && props.orderData.id) {
        emit('close');
        router.push({ name: 'paymentScreen', params: { id: props.orderData.id } });
    }
};

// --- MỚI: Hàm chuyển sang trang Thêm món ---
const goToAddFood = () => {
    if (props.orderData && props.orderData.id) {
        emit('close'); // Đóng popup
        // Chuyển sang màn hình thêm món
        router.push({ 
            name: 'addFoodScreen', 
            params: { id: props.orderData.id } 
        });
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
        <button class="btn-close" @click="emit('close')" style="color: white;">X</button>
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
                    <b>{{ orderData?.trangThai || '---' }}</b>
                </div>
                <div class="info-col">
                    <label>Ngày tạo</label>
                    <b>17:36 01/01/2026</b>
                </div>
            </div>
        </div>

        <div class="section-title">
            <div class="title-left">
                <span>🍴</span> <b>Danh sách món</b>
            </div>
            <div class="title-actions">
                <button class="btn-add-food" @click="goToAddFood">➕ Thêm món</button>
                <button class="btn-check-all">✔ Đã lên tất cả</button>
            </div>
        </div>

        <div class="food-list">
            <div v-for="item in mockFoodItems" :key="item.id" class="food-item">
                <input type="checkbox" class="checkbox" />
                <div class="food-img-placeholder">🍴</div>
                <div class="food-info">
                    <div class="food-name-row">
                        <b>{{ item.name }}</b>
                        <span class="badge-status">{{ item.status }}</span>
                    </div>
                    <div class="food-meta">
                        <span>SL:<b>{{ item.quantity }}</b></span>
                        <span class="food-price-orig">Giá: {{ item.price }}</span>
                    </div>
                </div>
                <div class="food-total">
                    <b>Giá: {{ item.price }}</b>
                </div>
                <button class="btn-remove-item">✖</button>
            </div>
        </div>

        <div class="summary-box">
            <div class="summary-row">
                <span>Tổng kết</span>
            </div>
            <div class="summary-row">
                <span class="label-light">Tạm tính:</span>
                <b>280.000 ₫</b>
            </div>
            <div class="summary-row">
                <span class="label-light">Thuế(10%):</span>
                <b>28.000 ₫</b>
            </div>
            <div class="divider"></div>
            <div class="summary-row total-row">
                <b>Tổng cộng:</b>
                <b class="text-red">308.000 ₫</b>
            </div>
        </div>

      </div>

      <div class="modal-footer">
        <button class="btn-cancel-order">✖ Hủy đơn</button>
        <button class="btn-payment" @click="goToPayment">💳 Tiến hành thanh toán</button>
      </div>

    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex; justify-content: center; align-items: center;
  z-index: 9999;
}

.modal-content {
  background: white;
  width: 700px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
  animation: slideIn 0.2s ease-out;
}

/* Header */
.modal-header {
  background-color: #8B0000;
  color: white;
  padding: 15px 20px;
  display: flex; justify-content: space-between; align-items: flex-start;
}
.header-text h2 { margin: 0; font-size: 18px; font-weight: bold; }
.order-id { font-size: 12px; opacity: 0.9; color: white; }
.btn-close { background: none; border: none; color: white; font-size: 20px; cursor: pointer; }

/* Body */
.modal-body { padding: 20px; background-color: white; max-height: 60vh; overflow-y: auto; }

/* Info Box */
.info-box { background: #f5f5f5; padding: 15px; border-radius: 6px; margin-bottom: 20px; }
.info-row { display: flex; justify-content: space-between; }
.info-col { width: 48%; display: flex; flex-direction: column; font-size: 13px; }
.info-col label { color: #666; margin-bottom: 2px; font-size: 12px; }
.mt-2 { margin-top: 10px; }

/* Section Title & Buttons */
.section-title { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.title-left { font-size: 16px; display: flex; gap: 5px; align-items: center; }
.title-actions { display: flex; gap: 10px; } /* Wrapper cho 2 nút */

.btn-check-all { background: #8B0000; color: white; border: none; padding: 5px 10px; border-radius: 4px; font-size: 12px; cursor: pointer; }

/* Nút Thêm món mới (Style outline đỏ) */
.btn-add-food { 
    background: white; 
    border: 1px solid #8B0000; 
    color: #8B0000; 
    padding: 5px 10px; 
    border-radius: 4px; 
    font-size: 12px; 
    cursor: pointer; 
    font-weight: bold;
    transition: 0.2s;
}
.btn-add-food:hover { background: #ffebee; }

/* Food List */
.food-item { 
    display: flex; align-items: center; gap: 15px; 
    border: 1px solid #eee; padding: 10px; border-radius: 6px; margin-bottom: 10px; position: relative;
}
.food-img-placeholder { width: 50px; height: 50px; background: #eee; display: flex; align-items: center; justify-content: center; border-radius: 4px; font-size: 20px; color: #999; }
.food-info { flex: 1; display: flex; flex-direction: column; gap: 5px; }
.food-name-row { display: flex; align-items: center; gap: 10px; }
.badge-status { background: #eee; color: #555; padding: 2px 6px; border-radius: 4px; font-size: 10px; font-weight: bold; }
.food-meta { font-size: 13px; color: #666; }
.food-total { font-size: 14px; font-weight: bold; margin-right: 20px; }
.btn-remove-item { background: none; border: none; color: #999; cursor: pointer; position: absolute; top: 5px; right: 5px; font-size: 16px; }
.btn-remove-item:hover { color: red; }

/* Summary */
.summary-box { background: #f9f9f9; padding: 15px; border-radius: 6px; border: 1px solid #eee; }
.summary-row { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px; }
.label-light { color: #666; }
.divider { height: 1px; background: #ddd; margin: 10px 0; }
.total-row { font-size: 16px; margin-bottom: 0; }
.text-red { color: red; }

/* Footer */
.modal-footer { 
    padding: 15px 20px; background: #f5f5f5; border-top: 1px solid #ddd;
    display: flex; justify-content: space-between; 
}
.btn-cancel-order { background: #8B0000; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; }
.btn-payment { background: #D32F2F; color: white; border: none; padding: 10px 20px; border-radius: 6px; cursor: pointer; font-weight: bold; }

@keyframes slideIn {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
</style>
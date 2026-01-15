<script setup>
import Sidebar from '../../../components/sidebar.vue';
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();
const orderId = route.params.id; 

const paymentMethod = ref('cash'); 

const orderDetails = {
    customerName: 'Nguyễn Văn A',
    table: 'T-01',
    items: [
        { name: 'Món ăn 1', quantity: 1, unitPrice: '350.000 ₫', total: '350.000 ₫' },
        { name: 'Lẩu Tokbokki', quantity: 1, unitPrice: '320.000 ₫', total: '320.000 ₫' }
    ],
    subTotal: '670.000 ₫',
    tax: '67.000 ₫',
    total: '737.000 ₫'
};

const handleConfirmPayment = () => {
    alert(`Đã thanh toán đơn ${orderId} thành công!`);
    router.push('/manage/orders'); 
};

// Hàm quay lại trang trước
const handleBack = () => {
    router.back(); 
};
</script>

<template>
  <div class="app-layout">
    <Sidebar />

    <main class="main-content">
      <div class="header-section">
          <div class="header-text">
            <h1 class="page-title">Thanh toán đơn hàng</h1>
            <p class="sub-title">Xác nhận và thanh toán đơn hàng</p>
          </div>
          <button class="btn-back" @click="handleBack">
            ⬅ Quay lại
          </button>
      </div>

      <div class="payment-container">
        <div class="left-column">
            
            <div class="card info-card">
                <div class="card-header">
                    <h3>👤 Thông tin khách hàng</h3>
                </div>
                <div class="card-body row-info">
                    <div class="info-group">
                        <label>Họ tên</label>
                        <b>{{ orderDetails.customerName }}</b>
                    </div>
                    <div class="info-group">
                        <label>Bàn</label>
                        <b>{{ orderDetails.table }}</b>
                    </div>
                </div>
            </div>

            <div class="card items-card">
                <div class="card-header">
                    <h3>Danh sách món</h3>
                </div>
                <div class="card-body list-body">
                    <div v-for="(item, index) in orderDetails.items" :key="index" class="item-row">
                        <div class="item-info">
                            <b>{{ item.name }}</b>
                            <span>Số lượng: {{ item.quantity }} × {{ item.unitPrice }}</span>
                        </div>
                        <div class="item-price">
                            <b>{{ item.total }}</b>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="right-column">
            <div class="card summary-card">
                
                <div class="summary-section">
                    <h3>Thanh toán</h3>
                    <div class="summary-row">
                        <span>Tạm tính:</span>
                        <b>{{ orderDetails.subTotal }}</b>
                    </div>
                    <div class="summary-row">
                        <span>Thuế(10%):</span>
                        <b>{{ orderDetails.tax }}</b>
                    </div>
                    <div class="divider"></div>
                    <div class="summary-row total-row">
                        <span>Tổng cộng:</span>
                        <b class="text-red">{{ orderDetails.total }}</b>
                    </div>
                </div>

                <div class="divider"></div>

                <div class="promo-section">
                    <h3>Khuyến mãi</h3>
                    <button class="btn-promo">🏷️ Chọn khuyến mãi</button>
                </div>

                <div class="divider"></div>

                <div class="method-section">
                    <h3>Phương thức thanh toán</h3>
                    
                    <label class="method-option" :class="{ active: paymentMethod === 'cash' }">
                        <input type="radio" value="cash" v-model="paymentMethod" hidden>
                        <div class="radio-circle"></div>
                        <div class="method-text">
                            <b>Tiền mặt</b>
                            <span>Thanh toán bằng tiền mặt</span>
                        </div>
                    </label>

                    <label class="method-option" :class="{ active: paymentMethod === 'banking' }">
                        <input type="radio" value="banking" v-model="paymentMethod" hidden>
                        <div class="radio-circle"></div>
                        <div class="method-text">
                            <b>Chuyển khoản</b>
                            <span>Thanh toán qua ngân hàng</span>
                        </div>
                    </label>
                </div>

                <div class="action-section">
                    <button class="btn-confirm" @click="handleConfirmPayment">
                        ✔ Xác nhận thanh toán
                    </button>
                </div>

            </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>

.app-layout { min-height: 100vh; background-color: #f8f9fa; }
.main-content { margin-left: 250px; width: calc(100% - 250px); padding: 30px 40px; }

/* Header mới với Flexbox */
.header-section { 
    margin-bottom: 25px; 
    display: flex; 
    justify-content: space-between; /* Đẩy 2 phần sang 2 bên */
    align-items: center; 
}
.page-title { color: #8B0000; font-size: 24px; font-weight: bold; margin: 0; }
.sub-title { color: #666; font-size: 14px; margin-top: 5px; }

/* Style nút Quay lại */
.btn-back {
    background-color: #e0e0e0;
    color: #333;
    border: none;
    padding: 10px 20px;
    border-radius: 6px;
    font-weight: 600;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 8px;
    transition: background 0.2s;
}
.btn-back:hover {
    background-color: #d0d0d0;
}


.payment-container { display: flex; gap: 30px; }
.left-column { flex: 2; display: flex; flex-direction: column; gap: 20px; }
.right-column { flex: 1; }


.card { background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); border: 1px solid #eee; overflow: hidden; }
.card-header { padding: 15px 20px; background: #fff; border-bottom: 1px solid #f0f0f0; }
.card-header h3 { margin: 0; font-size: 16px; font-weight: 700; color: #333; }
.card-body { padding: 20px; }


.row-info { display: flex; justify-content: space-between; }
.info-group { display: flex; flex-direction: column; }
.info-group label { font-size: 12px; color: #888; margin-bottom: 5px; }
.info-group b { font-size: 15px; color: #333; }

.item-row { display: flex; justify-content: space-between; align-items: center; padding: 15px; background: #f9f9f9; border-radius: 6px; border: 1px solid #eee; margin-bottom: 15px; }
.item-info { display: flex; flex-direction: column; }
.item-info b { font-size: 15px; margin-bottom: 4px; }
.item-info span { font-size: 13px; color: #666; }
.item-price b { font-size: 16px; color: #333; }


.summary-card { padding: 20px; }
.summary-section h3, .promo-section h3, .method-section h3 { font-size: 14px; margin: 0 0 15px 0; font-weight: 700; color: #333; }
.summary-row { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px; }
.total-row { font-size: 18px; margin-top: 5px; }
.text-red { color: #d32f2f; }

.divider { height: 1px; background: #eee; margin: 20px 0; }


.btn-promo { width: 100%; background: #a92323; color: white; border: none; padding: 12px; border-radius: 6px; font-weight: 600; cursor: pointer; transition: 0.2s; }
.btn-promo:hover { background: #8B0000; }

.btn-confirm { width: 100%; background: #d32f2f;  color: white; border: none; padding: 15px; border-radius: 6px; font-weight: bold; font-size: 16px; cursor: pointer; margin-top: 10px; box-shadow: 0 4px 10px rgba(211, 47, 47, 0.2); }
.btn-confirm:hover { background: #b71c1c; }

.method-option { display: flex; align-items: center; padding: 15px; border: 1px solid #ddd; border-radius: 8px; margin-bottom: 10px; cursor: pointer; transition: all 0.2s; position: relative; }
.radio-circle { width: 18px; height: 18px; border: 2px solid #ccc; border-radius: 50%; margin-right: 15px; position: relative; }
.method-text { display: flex; flex-direction: column; }
.method-text b { font-size: 14px; }
.method-text span { font-size: 12px; color: #888; }


.method-option.active { border-color: #d32f2f; background-color: #ffebee; }
.method-option.active .radio-circle { border-color: #d32f2f; }
.method-option.active .radio-circle::after { content: ''; width: 10px; height: 10px; background: #d32f2f; border-radius: 50%; position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); }
</style>
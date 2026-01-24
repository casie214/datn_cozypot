<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { BeGetHoaDonById, BeGetChiTietHoaDon, BeXacNhanThanhToan } from "./orderService"; 

const route = useRoute();
const router = useRouter();
const orderId = route.params.id; 

const paymentMethod = ref('cash'); 
const orderDetails = ref(null); 
const loading = ref(true);

const formatMoney = (value) => {
    if (!value) return "0 ₫";
    return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
    }).format(value);
};

onMounted(async () => {
    try {
        loading.value = true;
        const data = await BeGetHoaDonById(orderId);
        const items = await BeGetChiTietHoaDon(orderId);
        orderDetails.value = { ...data, items: items }; 
    } catch (error) {
        console.error("Lỗi lấy dữ liệu:", error);
        alert("Không thể tải thông tin đơn hàng!");
    } finally {
        loading.value = false;
    }
});

const handleConfirmPayment = async () => {
    try {
        const payload = {
            idHoaDon: orderId,
            idNhanVien: 1, // Sửa khi có đăng nhập
            hanhDong: "Thanh toán",
            thoiGianThucHien: new Date().toISOString()
        };

        const success = await BeXacNhanThanhToan(payload);
        if (success) {
            alert(`Thanh toán đơn hàng thành công!`);
            router.push('/manage/orders');
        }
    } catch (error) {
        alert("Thanh toán thất bại: " + error.message);
    }
};

const handleBack = () => {
    router.back(); 
};
</script>

<template>
  <div class="d-flex" style="background-color: #f8f9fa; min-height: 100vh;">
    <main class="flex-grow-1 p-4">
      
      <div class="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h2 class="fw-bold mb-0 text-custom-red">Thanh toán đơn hàng</h2>
            <small class="text-muted">Xác nhận thông tin và tiến hành thanh toán</small>
          </div>
          <button class="btn btn-secondary fw-bold shadow-sm" @click="handleBack">
            <i class="fa-solid fa-arrow-left me-2"></i>Quay lại
          </button>
      </div>

      <div v-if="!loading && orderDetails" class="row g-4">
        
        <div class="col-lg-8">
            <div class="card border-0 shadow-sm mb-4">
                <div class="card-header bg-white py-3 border-bottom">
                    <h5 class="mb-0 fw-bold text-dark"><i class="fa-solid fa-user me-2 text-custom-red"></i>Thông tin khách hàng</h5>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6 mb-3 mb-md-0">
                            <small class="text-muted d-block mb-1">Họ tên khách hàng</small>
                            <span class="fw-bold fs-5">{{ orderDetails.tenKhachHang || 'Khách vãng lai' }}</span>
                        </div>
                        <div class="col-md-6 border-start-md ps-md-4">
                            <small class="text-muted d-block mb-1">Vị trí bàn</small>
                            <span class="fw-bold fs-5 text-custom-red">{{ orderDetails.tenBan }}</span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="card border-0 shadow-sm">
                <div class="card-header bg-white py-3 border-bottom">
                    <h5 class="mb-0 fw-bold text-dark"><i class="fa-solid fa-list-check me-2 text-custom-red"></i>Danh sách món ăn</h5>
                </div>
                <div class="card-body p-0">
                    <ul class="list-group list-group-flush">
                        <li v-for="(item, index) in orderDetails.items" :key="index" class="list-group-item p-3 border-bottom-0 border-top">
                            <div class="d-flex justify-content-between align-items-center">
                                <div>
                                    <h6 class="fw-bold mb-1">{{ item.tenMon }}</h6>
                                    <small class="text-muted">
                                        {{ item.soLuong }} x {{ formatMoney(item.donGia) }}
                                    </small>
                                </div>
                                <span class="fw-bold text-dark">{{ formatMoney(item.thanhTien) }}</span>
                            </div>
                        </li>
                    </ul>
                    <div v-if="!orderDetails.items || orderDetails.items.length === 0" class="text-center py-4 text-muted">
                        Không có món ăn nào.
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-4">
            <div class="card border-0 shadow-sm h-100">
                <div class="card-body p-4">
                    
                    <h5 class="fw-bold mb-3">Tóm tắt thanh toán</h5>
                    
                    <div class="d-flex justify-content-between mb-2 text-muted">
                        <span>Tạm tính:</span>
                        <span class="fw-bold text-dark">{{ formatMoney(orderDetails.tongTienThanhToan + (orderDetails.soTienDaGiam || 0)) }}</span>
                    </div>
                    <div class="d-flex justify-content-between mb-2 text-muted">
                        <span>Thuế (10%):</span>
                        <span class="fw-bold text-dark">{{ formatMoney(orderDetails.tongTienThanhToan * 0.1) }}</span>
                    </div>
                    
                    <div v-if="orderDetails.soTienDaGiam > 0" class="d-flex justify-content-between mb-2 text-success">
                        <span><i class="fa-solid fa-tag me-1"></i>Giảm giá:</span>
                        <span class="fw-bold">-{{ formatMoney(orderDetails.soTienDaGiam) }}</span>
                    </div>

                    <hr class="my-3">

                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <span class="fw-bold fs-5">Tổng cộng:</span>
                        <span class="fw-bold fs-3 text-custom-red">{{ formatMoney(orderDetails.tongTienThanhToan) }}</span>
                    </div>

                    <button class="btn btn-outline-custom w-100 mb-4 py-2 fw-bold border-dashed">
                        <i class="fa-solid fa-ticket me-2"></i>Chọn mã khuyến mãi
                    </button>

                    <h6 class="fw-bold mb-3">Phương thức thanh toán</h6>
                    
                    <div class="payment-methods mb-4">
                        <label class="payment-option d-flex align-items-center p-3 border rounded mb-2 cursor-pointer" :class="{ 'active': paymentMethod === 'cash' }">
                            <input type="radio" value="cash" v-model="paymentMethod" class="form-check-input me-3 mt-0" style="width: 1.2em; height: 1.2em;">
                            <div class="flex-grow-1">
                                <div class="fw-bold text-dark">Tiền mặt</div>
                                <small class="text-muted">Thanh toán trực tiếp tại quầy</small>
                            </div>
                            <i class="fa-solid fa-money-bill-wave text-success fs-4"></i>
                        </label>

                        <label class="payment-option d-flex align-items-center p-3 border rounded cursor-pointer" :class="{ 'active': paymentMethod === 'banking' }">
                            <input type="radio" value="banking" v-model="paymentMethod" class="form-check-input me-3 mt-0" style="width: 1.2em; height: 1.2em;">
                            <div class="flex-grow-1">
                                <div class="fw-bold text-dark">Chuyển khoản</div>
                                <small class="text-muted">QR Code / Internet Banking</small>
                            </div>
                            <i class="fa-solid fa-qrcode text-primary fs-4"></i>
                        </label>
                    </div>

                    <button class="btn btn-custom-red w-100 py-3 fw-bold fs-5 shadow-sm" @click="handleConfirmPayment">
                        <i class="fa-solid fa-check-circle me-2"></i>Xác nhận thanh toán
                    </button>

                </div>
            </div>
        </div>

      </div>

      <div v-else-if="loading" class="text-center py-5">
        <div class="spinner-border text-custom-red" role="status">
            <span class="visually-hidden">Loading...</span>
        </div>
        <p class="mt-2 text-muted fw-bold">Đang tải thông tin đơn hàng...</p>
      </div>

    </main>
  </div>
</template>

<style scoped>
.text-custom-red { color: #8b0000 !important; }
.bg-custom-red { background-color: #8b0000 !important; }

.btn-custom-red {
    background-color: #8b0000;
    border-color: #8b0000;
    color: white;
    transition: all 0.2s;
}
.btn-custom-red:hover {
    background-color: #5e0d15;
    border-color: #5e0d15;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(123, 18, 28, 0.3);
}

.btn-outline-custom {
    color: #8b0000;
    border: 1px dashed #8b0000;
    background: transparent;
}
.btn-outline-custom:hover {
    background-color: #fff5f5;
}

.cursor-pointer { cursor: pointer; }
.payment-option {
    transition: all 0.2s;
    border: 1px solid #dee2e6;
}
.payment-option:hover {
    background-color: #f8f9fa;
}
.payment-option.active {
    border-color: #8b0000;
    background-color: #fff5f5;
    box-shadow: 0 0 0 1px #8b0000 inset;
}
.payment-option.active .form-check-input {
    background-color: #8b0000;
    border-color: #8b0000;
}

@media (min-width: 768px) {
    .border-start-md {
        border-left: 1px solid #dee2e6;
    }
}
</style>
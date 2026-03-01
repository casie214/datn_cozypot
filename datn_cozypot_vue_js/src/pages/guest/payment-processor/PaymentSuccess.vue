<template>
    <div class="payment-result success-bg">
        <div class="result-card">
            <div class="icon-circle success">
                <i class="fa-solid fa-check"></i>
            </div>

            <div v-if="paymentType === 'deposit'">
                <h2>Đặt Bàn Thành Công!</h2>
                <p>Tiền cọc đã được thanh toán. Nhà hàng đã ghi nhận lịch hẹn và chuẩn bị món ăn. Xin cảm ơn!</p>

                <button class="btn-return" @click="goHome">
                    Trở về Trang chủ
                </button>
            </div>

            <div v-else>
                <h2>Thanh Toán Thành Công!</h2>
                <p>Hóa đơn đã được ghi nhận. Bàn đã được dọn trống.</p>

                <button class="btn-return" @click="goHome">
                    Quay lại Quản lý bàn
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

// 1. Đọc query params trên URL (Ví dụ: ?type=deposit)
// Nếu không có thì mặc định là 'full'
const paymentType = computed(() => route.query.type || 'full');

onMounted(() => {
    // 2. NẾU LÀ KHÁCH HÀNG: Xóa sạch giỏ hàng và dữ liệu nháp
    if (paymentType.value === 'deposit') {
        localStorage.removeItem("cart");
        sessionStorage.removeItem("pendingBooking");
    }
});

// 3. Xử lý nút bấm dựa theo loại thanh toán
const goHome = () => {
    if (paymentType.value === 'deposit') {
        // Khách hàng -> Về trang chủ
        router.push('/'); 
    } else {
        // Nhân viên -> Về quản lý bàn
        router.push('/admin/checkin'); 
    }
};
</script>

<style scoped>
.payment-result {
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f0fdf4;
}

.result-card {
    background: white;
    padding: 40px;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    text-align: center;
    max-width: 400px;
}

.icon-circle.success {
    width: 80px;
    height: 80px;
    background: #22c55e;
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40px;
    margin: 0 auto 20px;
}

h2 {
    color: #166534;
    font-weight: bold;
    margin-bottom: 10px;
}

p {
    color: #4b5563;
    margin-bottom: 25px;
}

.btn-return {
    background: #7d161a;
    color: white;
    border: none;
    padding: 12px 24px;
    border-radius: 8px;
    font-weight: bold;
    cursor: pointer;
    transition: 0.3s;
}

.btn-return:hover {
    background: #5c0a16;
}
</style>
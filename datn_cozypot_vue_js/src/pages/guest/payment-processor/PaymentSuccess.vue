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
    /* Giữ nền xanh nhạt nhẹ nhàng để làm nổi bật thẻ trắng và nút đỏ */
    background-color: #f0fdf4; 
}

.result-card {
    background: white;
    padding: 40px;
    border-radius: 16px; /* Bo góc mềm mại hơn một chút */
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05); /* Đổ bóng nhẹ nhàng, sang trọng */
    text-align: center;
    max-width: 420px;
    width: 90%; /* Đảm bảo hiển thị tốt trên điện thoại */
    border: 1px solid #e0e0e0;
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
    margin: 0 auto 25px;
    /* Thêm một chút hiệu ứng glow cho icon */
    box-shadow: 0 4px 15px rgba(34, 197, 94, 0.3); 
}

h2 {
    color: #166534;
    font-weight: 800;
    margin-bottom: 12px;
    font-size: 26px;
}

p {
    color: #4b5563;
    margin-bottom: 35px;
    font-size: 16px;
    line-height: 1.6;
}

.btn-return {
    /* --- ĐÂY LÀ MÀU GRADIENT BẠN YÊU CẦU --- */
    background: linear-gradient(135deg, #7d161a 0%, #d32f2f 100%) !important;
    
    color: white;
    border: none;
    padding: 14px 30px; /* Tăng padding một chút cho nút nhìn đầy đặn */
    border-radius: 10px; /* Bo góc nút đồng bộ với thẻ */
    font-weight: bold;
    font-size: 16px;
    cursor: pointer;
    transition: all 0.3s ease; /* Hiệu ứng mượt mà khi hover */
    
    /* Thêm đổ bóng cho nút để tạo chiều sâu */
    box-shadow: 0 4px 12px rgba(125, 22, 26, 0.2);
    width: 100%; /* Cho nút full chiều ngang để dễ bấm hơn */
}

/* Hiệu ứng khi di chuột vào nút */
.btn-return:hover {
    /* Nút hơi nổi lên một chút và đổ bóng đậm hơn */
    transform: translateY(-2px);
    box-shadow: 0 6px 18px rgba(125, 22, 26, 0.3);
}

/* Hiệu ứng khi bấm vào nút */
.btn-return:active {
    /* Nút lún xuống tạo cảm giác thật */
    transform: translateY(1px);
    box-shadow: 0 2px 6px rgba(125, 22, 26, 0.2);
}
</style>
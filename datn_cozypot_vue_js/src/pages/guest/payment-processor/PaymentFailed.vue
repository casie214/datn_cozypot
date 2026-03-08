<template>
    <div class="payment-result fail-bg">
        <div class="result-card">
            <div class="icon-circle fail">
                <i class="fa-solid fa-xmark"></i>
            </div>
            <h2>Thanh Toán Thất Bại</h2>

            <template v-if="isDeposit">
                <p>Giao dịch đặt cọc thất bại. Vui lòng thanh toán tiền cọc trong 15p, sau 15p nếu chưa thanh toán hệ thống sẽ tự động hủy phiếu đặt bàn</p>
                <button class="btn-return" @click="goToHistory">
                    Xem Lịch sử đặt bàn
                </button>
            </template>

            <template v-else>
                <p>Giao dịch đã bị hủy hoặc có lỗi xảy ra. Hóa đơn vẫn chưa được thanh toán.</p>
                <button class="btn-return" @click="goHome">
                    Quay lại Sơ đồ bàn
                </button>
            </template>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute(); // Thêm useRoute để lấy tham số trên thanh địa chỉ

// Kiểm tra xem URL có chứa "?type=deposit" hay không
const isDeposit = computed(() => route.query.type === 'deposit');

// Hàm chuyển hướng cho Nhân viên
const goHome = () => {
    router.push('/admin/checkin'); 
};

// Hàm chuyển hướng cho Khách hàng
const goToHistory = () => {
    router.push('/tra-cuu'); 
};
</script>

<style scoped>
.payment-result {
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    /* Dùng nền xám nhạt đồng bộ với trang Thành công và toàn dự án */
    background-color: #fafafa; 
}

.result-card {
    background: white;
    padding: 40px;
    border-radius: 16px; /* Bo góc mềm mại */
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05); /* Đổ bóng nhẹ nhàng, sang trọng */
    text-align: center;
    max-width: 420px;
    width: 90%;
    border: 1px solid #e0e0e0;
}

.icon-circle.fail {
    width: 80px;
    height: 80px;
    background: #dc3545; /* Đỏ tươi báo lỗi (chuẩn UI/UX) */
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40px;
    margin: 0 auto 25px;
    /* Hiệu ứng glow màu đỏ */
    box-shadow: 0 4px 15px rgba(220, 53, 69, 0.3); 
}

h2 {
    color: #333; /* Chữ xám đen sang trọng thay vì đỏ bầm */
    font-weight: 800;
    margin-bottom: 12px;
    font-size: 26px;
}

p {
    color: #666;
    margin-bottom: 35px;
    font-size: 15px;
    line-height: 1.6;
}

.btn-return {
    /* --- MÀU GRADIENT THƯƠNG HIỆU --- */
    background: linear-gradient(135deg, #7d161a 0%, #d32f2f 100%) !important;
    
    color: white;
    border: none;
    padding: 14px 30px;
    border-radius: 10px;
    font-weight: bold;
    font-size: 16px;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(125, 22, 26, 0.2);
    width: 100%;
}

.btn-return:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 18px rgba(125, 22, 26, 0.3);
}

.btn-return:active {
    transform: translateY(1px);
    box-shadow: 0 2px 6px rgba(125, 22, 26, 0.2);
}
</style>
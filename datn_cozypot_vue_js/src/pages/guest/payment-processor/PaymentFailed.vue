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
    router.push('/history'); 
};
</script>

<style scoped>
/* Toàn bộ CSS của bạn được giữ nguyên không thay đổi */
.payment-result {
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #fef2f2;
}

.result-card {
    background: white;
    padding: 40px;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    text-align: center;
    max-width: 400px;
}

.icon-circle.fail {
    width: 80px;
    height: 80px;
    background: #ef4444;
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 40px;
    margin: 0 auto 20px;
}

h2 {
    color: #991b1b;
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
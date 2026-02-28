<template>
    <div class="payment-result fail-bg">
        <div class="result-card">
            <div class="icon-circle fail">
                <i class="fa-solid fa-xmark"></i>
            </div>
            <h2>Thanh Toán Thất Bại</h2>
            <p>Giao dịch đã bị hủy hoặc có lỗi xảy ra. Hóa đơn vẫn chưa được thanh toán.</p>

            <button class="btn-return" @click="goHome">
                Quay lại Sơ đồ bàn
            </button>
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
const router = useRouter();

const goHome = () => {
    router.push('/admin/checkin'); // Đổi thành route màn hình sơ đồ bàn của bạn
};
</script>

<style scoped>
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
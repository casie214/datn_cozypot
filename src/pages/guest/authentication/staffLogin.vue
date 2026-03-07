<script setup>
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import axiosClient from '@/services/axiosClient';
import { useAuthStore } from './authenticationServices/authenticationService';

const authStore = useAuthStore();
const router = useRouter();

// Mặc định là false cho luồng quản lý
const isClientLogin = ref(false);

const email = ref('');
const password = ref('');
const errors = ref({});
const wasSubmitted = ref(false); 

// Logic Quên mật khẩu
const showForgotModal = ref(false);
const forgotEmail = ref('');
const isSending = ref(false);

watch([email, password], () => {
    if (wasSubmitted.value) {
        validate();
    }
});

const handleForgotPassword = async () => {
    if (!forgotEmail.value) {
        Swal.fire({
            icon: 'warning',
            iconColor: '#7D161A',
            title: 'Chú ý',
            text: 'Vui lòng nhập email công việc của bạn!',
            confirmButtonColor: '#8B1A1A'
        });
        return;
    }
    isSending.value = true;
    try {
        // Gửi cả email và username vì dữ liệu của bạn đang bị lệch giữa 2 cột này
        // Thêm isClient: false để Backend lọc bảng nhân viên (nếu có logic này)
        await axiosClient.post('/auth/forgot-password', { 
            email: forgotEmail.value,
            username: forgotEmail.value,
            isClient: false 
        });

        Swal.fire({
            icon: 'success',
            iconColor: '#7D161A',
            title: 'Thành công!',
            text: 'Mật khẩu mới đã được gửi vào Email của bạn. Vui lòng kiểm tra!',
            confirmButtonColor: '#8B1A1A'
        });
        showForgotModal.value = false;
        forgotEmail.value = '';
    } catch (error) {
        console.error("Lỗi:", error.response);
        let errorMsg = "Email không tồn tại trong hệ thống quản lý!";
        if (error.response && error.response.data) {
            errorMsg = typeof error.response.data === 'string' ? error.response.data : (error.response.data.message || errorMsg);
        }
        Swal.fire({
            icon: 'error',
            title: 'Thất bại',
            text: errorMsg,
            confirmButtonColor: '#8B1A1A'
        });
    } finally {
        isSending.value = false;
    }
}

const validate = () => {
    errors.value = {};
    let isValid = true;

    if (!email.value.trim()) {
        errors.value.email = "Vui lòng nhập Email quản lý";
        isValid = false;
    }
    if (!password.value.trim()) {
        errors.value.password = "Mật khẩu không được để trống";
        isValid = false;
    }
    return isValid;
};

const handleLogin = async () => {
    wasSubmitted.value = true;
    if (!validate()) return;

    Swal.fire({
        title: 'Đang xác thực...',
        text: 'Hệ thống quản lý đang kiểm tra quyền truy cập',
        allowOutsideClick: false,
        didOpen: () => { Swal.showLoading(); }
    });

    try {
        // Tham số thứ 3 là false cho Quản lý
        await authStore.login(email.value, password.value, false);
        
        await Swal.fire({
            icon: 'success',
            iconColor: '#7D161A',
            title: 'Đăng nhập thành công!',
            text: 'Chào mừng bạn đến với trang quản lý!',
            timer: 1500,
            showConfirmButton: false,
            timerProgressBar: true
        });

        router.push('/admin/dashboard');
    } catch (error) {
        let errorMsg = "Sai tài khoản hoặc mật khẩu quản lý"; 
        if (error.response && error.response.status === 403) {
            errorMsg = "Tài khoản của bạn không có quyền truy cập khu vực này.";
        }
        Swal.fire({
            icon: 'error',
            title: 'Đăng nhập thất bại',
            text: errorMsg,
            confirmButtonColor: '#8B1A1A'
        });
    }
};
</script>

<template>
    <div class="main-content">
        <div class="login-card">
            <h2 class="login-title">Cổng Quản Lý</h2>
            <p class="login-subtitle">Hệ thống quản trị nội bộ CozyPot</p>

            <form @submit.prevent="handleLogin" class="mt-4">
                <div class="mb-3">
                    <input v-model="email" type="text" class="custom-input"
                        placeholder="Email nhân viên"
                        :class="{ 'is-invalid': errors.email }">
                    <div class="invalid-feedback text-start">{{ errors.email }}</div>
                </div>

                <div class="mb-3">
                    <input v-model="password" type="password" class="custom-input" placeholder="Mật khẩu"
                        :class="{ 'is-invalid': errors.password }">
                    <div class="invalid-feedback text-start">{{ errors.password }}</div>
                </div>

                <div class="d-flex justify-content-between align-items-center mb-4 px-1">
                    <div class="form-check" style="display: flex; align-items: center; gap: 8px;">
                        <input class="form-check-input custom-checkbox" type="checkbox" id="remember_me">
                        <label class="form-check-label remember-text" for="remember_me">Nhớ đăng nhập</label>
                    </div>
                    <a href="#!" class="forgot-password" @click.prevent="showForgotModal = true">Quên mật khẩu?</a>
                </div>

                <button type="submit" class="btn-login">Đăng nhập hệ thống</button>
            </form>

            <div class="mt-5 pt-3 border-top">
                <p class="text-muted small">Vui lòng không cung cấp tài khoản quản trị cho bất kỳ ai.</p>
            </div>
        </div>

        <div v-if="showForgotModal" class="modal-overlay" @click.self="showForgotModal = false">
            <div class="forgot-modal">
                <div class="text-end">
                    <button @click="showForgotModal = false" class="btn-close-modal">&times;</button>
                </div>
                <h3 class="modal-title">Quên mật khẩu?</h3>
                <p class="modal-desc">Nhập email nhân viên để nhận lại mật khẩu mới từ hệ thống.</p>

                <div class="mb-4 text-start"> 
                    <input v-model="forgotEmail" type="email" class="custom-input shadow-sm" placeholder="Nhập email của bạn">
                </div>

                <button @click="handleForgotPassword" class="btn-login w-100" :disabled="isSending">
                    <span v-if="isSending" class="spinner-border spinner-border-sm me-2"></span>
                    {{ isSending ? 'Đang xử lý...' : 'Gửi mật khẩu mới' }}
                </button>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* GIỮ NGUYÊN HOÀN TOÀN STYLE CŨ CỦA BẠN */
:root {
    --cozy-red: #6d1313;
}

.main-content {
    background-color: #f8f9fa;
    position: fixed;
    inset: 0;
    width: 100vw;
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    overflow: hidden;
}

.login-card {
    background: #ffffff;
    width: 100%;
    max-width: 850px;
    padding: 60px 100px;
    border-radius: 40px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.05);
    text-align: center;
}

.login-title {
    font-weight: 800;
    font-size: 32px;
    margin-bottom: 5px;
    color: #6d1414;
}

.login-subtitle {
    color: #888;
    font-size: 14px;
}

.custom-input {
    width: 100%;
    background: #f8f9fa;
    border: 1px solid #eee;
    padding: 15px 20px;
    border-radius: 15px;
    outline: none;
    transition: 0.2s;
}

.custom-input:focus {
    background: #fff;
    border-color: #6d1313;
    box-shadow: 0 0 0 3px rgba(139, 26, 26, 0.1);
}

.remember-text, .forgot-password {
    color: #e19e72;
    text-decoration: none;
    font-weight: 500;
}
.forgot-password:hover {
    color: #c0520e;
}
.custom-checkbox {
    width: 18px;
    height: 18px;
    accent-color: #6d1313;
    cursor: pointer;
}

.btn-login {
    width: 100%;
    background: #7d1f1f;
    color: white;
    border: none;
    padding: 14px;
    border-radius: 15px;
    font-weight: 600;
    font-size: 16px;
    transition: 0.3s;
    box-shadow: 0 4px 15px rgba(139, 26, 26, 0.2);
}

.btn-login:hover {
    background: #6d1414;
    transform: translateY(-1px);
}

.modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.3);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
    backdrop-filter: blur(4px);
}

.forgot-modal {
    background: white;
    padding: 35px;
    border-radius: 24px;
    width: 90%;
    max-width: 400px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
    text-align: center;
    animation: popIn 0.3s ease-out;
}

.modal-title {
    font-size: 1.5rem;
    color: #8B1A1A;
    font-weight: 700;
}

.btn-close-modal {
    background: none;
    border: none;
    font-size: 24px;
    color: #444;
    cursor: pointer;
}

@keyframes popIn {
    from { transform: scale(0.9); opacity: 0; }
    to { transform: scale(1); opacity: 1; }
}

.btn-login:disabled {
    background: #ccc;
    cursor: not-allowed;
}
</style>
<script setup>
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import axiosClient from '@/services/axiosClient';
import { useAuthStore } from './authenticationServices/authenticationService';

const authStore = useAuthStore();
const router = useRouter();

// Luôn mặc định là true vì đây là trang dành riêng cho khách
const isClientLogin = ref(true);

const email = ref('');
const password = ref('');
const errorMessage = ref('');
const errors = ref({});

const showForgotModal = ref(false);
const forgotEmail = ref('');
const isSending = ref(false);
const wasSubmitted = ref(false); 

// Theo dõi thay đổi để validate realtime sau khi đã nhấn submit lần đầu
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
            text: 'Vui lòng nhập email của bạn!',
            confirmButtonColor: '#8B1A1A'
        });
        return;
    }
    isSending.value = true;
    try {
        await axiosClient.post('/auth/forgot-password', { email: forgotEmail.value });
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
        let errorMsg = "Có lỗi xảy ra, vui lòng thử lại sau!";
        if (error.response && error.response.data) {
            errorMsg = typeof error.response.data === 'string' ? error.response.data : error.response.data.message;
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

const navigateToRegister = () => {
    router.push('/register');
}

const validate = () => {
    errors.value = {};
    let isValid = true;

    const emailVal = email.value ? email.value.trim() : "";
    const passwordVal = password.value ? password.value.trim() : "";

    if (!emailVal) {
        errors.value.email = "Vui lòng nhập Email";
        isValid = false;
    } else {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(emailVal)) {
            errors.value.email = "Định dạng Email không hợp lệ";
            isValid = false;
        }
    }
    if (!passwordVal) {
        errors.value.password = "Mật khẩu không được để trống";
        isValid = false;
    } else if (passwordVal.length < 6) {
        errors.value.password = "Mật khẩu phải có ít nhất 6 ký tự";
        isValid = false;
    }

    return isValid;
};

const handleLogin = async () => {
    wasSubmitted.value = true;
    if (!validate()) return;

    Swal.fire({
        title: 'Đang xác thực...',
        text: 'Vui lòng chờ trong giây lát',
        allowOutsideClick: false,
        didOpen: () => {
            Swal.showLoading();
        }
    });

    try {
        // Truyền tham số thứ 3 là true cho Client
        await authStore.login(email.value, password.value, true);
        
        await Swal.fire({
            icon: 'success',
            iconColor: '#7D161A',
            title: 'Đăng nhập thành công!',
            text: 'Chào mừng bạn đến với CozyPot!',
            timer: 1500,
            showConfirmButton: false,
            timerProgressBar: true
        });

        router.push('/');

    } catch (error) {
        let errorMsg = "Sai tài khoản hoặc mật khẩu"; 
        let errorTitle = "Đăng nhập thất bại";

        if (error.response) {
            const status = error.response.status;
            const data = error.response.data;
            if (status === 403) {
                errorTitle = "Tài khoản bị khóa";
                errorMsg = data.message || data || "Tài khoản của bạn tạm thời không thể truy cập.";
            } else if (status === 401) {
                errorMsg = "Tên đăng nhập hoặc mật khẩu không chính xác.";
            }
        } else if (error.request) {
            errorMsg = "Không thể kết nối đến máy chủ. Vui lòng kiểm tra lại mạng!";
        }

        Swal.fire({
            icon: 'error',
            title: errorTitle,
            text: errorMsg,
            confirmButtonColor: '#8B1A1A',
            confirmButtonText: 'Thử lại'
        });
    }
};

const loginWithGoogle = () => {
    const clientId = "20518372642-a98sqlousqg989t519jf8fbse68q5iqk.apps.googleusercontent.com";
    const redirectUri = "http://localhost:5173/auth/google/callback";
    const scope = "email profile";
    const responseType = "code";

    const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${redirectUri}&scope=${scope}&response_type=${responseType}`;

    window.location.href = googleAuthUrl;
};

</script>

<template>
    <div class="main-content">
        <div class="login-card">
            <h2 class="login-title">Đăng nhập</h2>
            <p class="login-subtitle">Trải nghiệm không gian ẩm thực tinh hoa tại CozyPot</p>

            <form @submit.prevent="handleLogin" class="mt-4">
                <div class="mb-3">
                    <input v-model="email" type="text" class="custom-input"
                        placeholder="Email khách hàng"
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
                        <input class="form-check-input custom-checkbox" type="checkbox" id="remember_me"
                            style="cursor: pointer; accent-color: var(--primary);">
                        <label class="form-check-label remember-text" for="remember_me"
                            style="cursor: pointer; user-select: none;">Nhớ đăng nhập</label>
                    </div>
                    <a href="#!" class="forgot-password" @click.prevent="showForgotModal = true">Quên mật khẩu?</a>
                </div>

                <button type="submit" class="btn-login">Đăng nhập ngay</button>
            </form>

            <div class="register-hint mt-4">
                Chưa có tài khoản? <a @click="navigateToRegister" class="register-link">Đăng ký ngay</a>
            </div>

            <div class="divider">
                <span>HOẶC TIẾP TỤC VỚI</span>
            </div>

            <div class="social-login">
                <button @click="loginWithGoogle" class="social-btn">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 48 48">
                        <path fill="#FFC107" d="M43.611,20.083H42V20H24v8h11.303c-1.649,4.657-6.08,8-11.303,8c-6.627,0-12-5.373-12-12c0-6.627,5.373-12,12-12c3.059,0,5.842,1.154,7.961,3.039l5.657-5.657C34.046,6.053,29.268,4,24,4C12.955,4,4,12.955,4,24c0,11.045,8.955,20,20,20c11.045,0,20-8.955,20-20C44,22.659,43.862,21.35,43.611,20.083z" />
                        <path fill="#FF3D00" d="M6.306,14.691l6.571,4.819C14.655,15.108,18.961,12,24,12c3.059,0,5.842,1.154,7.961,3.039l5.657-5.657C34.046,6.053,29.268,4,24,4C16.318,4,9.656,8.337,6.306,14.691z" />
                        <path fill="#4CAF50" d="M24,44c5.166,0,9.86-1.977,13.409-5.192l-6.19-5.238C29.211,35.091,26.715,36,24,36c-5.202,0-9.619-3.317-11.283-7.946l-6.522,5.025C9.505,39.556,16.227,44,24,44z" />
                        <path fill="#1976D2" d="M43.611,20.083H42V20H24v8h11.303c-0.792,2.237-2.231,4.166-4.087,5.571l6.19,5.238C36.971,39.205,44,34,44,24C44,22.659,43.862,21.35,43.611,20.083z" />
                    </svg>
                    <span>Google</span>
                </button>
            </div>
        </div>

        <div v-if="showForgotModal" class="modal-overlay" @click.self="showForgotModal = false">
            <div class="forgot-modal">
                <div class="text-end">
                    <button @click="showForgotModal = false" class="btn-close-modal">&times;</button>
                </div>
                <h3 class="modal-title">Quên mật khẩu?</h3>
                <p class="modal-desc">Đừng lo lắng, nhập email của bạn để nhận lại mật khẩu mới từ CozyPot.</p>
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
:root { --cozy-red: #6d1313; --cozy-gold: #D4A017; }
.main-content { background-color: #f8f9fa; position: fixed; inset: 0; width: 100vw; height: 100vh; display: flex; align-items: center; justify-content: center; padding: 20px; overflow: hidden; }
.login-card { background: #ffffff; width: 100%; max-width: 850px; padding: 40px 100px; border-radius: 40px; box-shadow: 0 10px 40px rgba(0, 0, 0, 0.05); text-align: center; max-height: 90vh; overflow-y: auto; margin: 0; }
.login-title { font-weight: 800; font-size: 32px; margin-bottom: 5px; color: #6d1414; }
.login-subtitle { color: #888; font-size: 14px; }
.custom-input { width: 100%; background: #f8f9fa; border: 1px solid #eee; padding: 15px 20px; border-radius: 15px; outline: none; transition: 0.2s; }
.custom-input:focus { background: #fff; border-color: #6d1313; box-shadow: 0 0 0 3px rgba(139, 26, 26, 0.1); }
.remember-text, .forgot-password { 
    color: #e19e72; text-decoration: none; font-weight: 500; }
.forgot-password:hover {
    color: #c0520e;
}
.custom-checkbox { width: 18px; height: 18px; border-radius: 4px; cursor: pointer; }
.btn-login { width: 100%; color: rgb(255, 255, 255); border: none; padding: 14px; border-radius: 15px; font-weight: 600; font-size: 16px; transition: 0.3s; background: #7d1f1f; box-shadow: 0 4px 15px rgba(139, 26, 26, 0.2); }
.btn-login:hover { background: #6d1414; transform: translateY(-1px); }
.register-hint { font-size: 14px; color: #666; }
.register-link { color: #e19e72; font-weight: 700; text-decoration: underline; cursor: pointer; }
.register-link:hover {
    color: #c0520e;
}
.divider { margin: 30px 0 20px; border-bottom: 1px solid #eee; position: relative; }
.divider span { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); background: #fff; padding: 0 15px; color: #aaa; font-size: 11px; letter-spacing: 1px; }
form { max-width: 500px; margin: 0 auto; }
.social-login { max-width: 500px; margin: 0 auto; display: flex; gap: 15px; }
.social-btn { flex: 1; display: flex; align-items: center; justify-content: center; gap: 10px; background: #fff; border: 1px solid #eee; padding: 10px; border-radius: 12px; font-weight: 500; font-size: 14px; transition: 0.2s; }
.social-btn:hover { background: #f8f9fa; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.3); display: flex; justify-content: center; align-items: center; z-index: 9999; backdrop-filter: blur(4px); }
.forgot-modal { background: white; padding: 35px; border-radius: 24px; width: 90%; max-width: 400px; box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1); text-align: center; }
.btn-close-modal { background: none; border: none; font-size: 24px; color: #444; cursor: pointer; }
.modal-title { font-size: 1.5rem; color: #8B1A1A; font-weight: 700; }
.modal-desc { font-size: 0.95rem; color: #555; margin-bottom: 20px; }
</style>
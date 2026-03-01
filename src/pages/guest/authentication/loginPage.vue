<script setup>
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import axiosClient from '@/services/axiosClient';
import { useAuthStore } from './authenticationServices/authenticationService';
import CommonNav from '@/components/commonNav.vue';
import GoogleLoginCallback from './googleLoginCallback.vue';

const authStore = useAuthStore();
const router = useRouter();
const isClientLogin = ref(true);

const email = ref('');
const password = ref('');
const errorMessage = ref('');
const errors = ref({});

const showForgotModal = ref(false);
const forgotEmail = ref('');
const isSending = ref(false);
const wasSubmitted = ref(false); 

watch([email, password, isClientLogin], () => {
    if (wasSubmitted.value) {
        validate();
    }
});
const handleForgotPassword = async () => {
    if (!forgotEmail.value) {
        Swal.fire({
            icon: 'warning',
            title: 'Chú ý',
            text: 'Vui lòng nhập email của bạn!',
            confirmButtonColor: '#8B1A1A'
        });
        return;
    }
    isSending.value = true;
    try {
        // 2. Gọi API Backend đã viết
        await axiosClient.post('/auth/forgot-password', { email: forgotEmail.value });

        // 3. Thông báo thành công bằng SweetAlert
        Swal.fire({
            icon: 'success',
            title: 'Thành công!',
            text: 'Mật khẩu mới đã được gửi vào Email của bạn. Vui lòng kiểm tra!',
            confirmButtonColor: '#8B1A1A'
        });

        // 4. Đóng modal và reset input
        showForgotModal.value = false;
        forgotEmail.value = '';
    } catch (error) {
        // 5. Thông báo lỗi
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

    const accountVal = email.value ? email.value.trim() : "";
    const passwordVal = password.value ? password.value.trim() : "";

    if (!accountVal) {
        errors.value.email = isClientLogin.value 
            ? "Vui lòng nhập Email hoặc Tên đăng nhập" 
            : "Tên đăng nhập không được để trống";
        isValid = false;
    } 
    else if (!isClientLogin.value && accountVal.length < 4) {
        errors.value.email = "Tên đăng nhập phải có ít nhất 4 ký tự";
        isValid = false;
    }
    else if (isClientLogin.value && accountVal.includes('@')) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(accountVal)) {
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

    errorMessage.value = '';
    
    // 1. Hiển thị loading chuyên nghiệp
    Swal.fire({
        title: 'Đang xác thực...',
        text: 'Vui lòng chờ trong giây lát',
        allowOutsideClick: false,
        didOpen: () => {
            Swal.showLoading();
        }
    });

    try {
        console.log("Đang gọi API đăng nhập...");
        // Lưu ý: Đảm bảo biến email.value và password.value khớp với logic Store
        await authStore.login(email.value, password.value, isClientLogin.value);
        
        console.log("Đăng nhập thành công, chuẩn bị chuyển trang");

        // 2. Thông báo thành công (Dùng timer để tự đóng)
        await Swal.fire({
            icon: 'success',
            title: 'Đăng nhập thành công!',
            text: isClientLogin.value ? 'Chào mừng bạn đến với CozyPot!' : 'Chào mừng bạn đến với trang quản lý!',
            timer: 1500,
            showConfirmButton: false,
            timerProgressBar: true
        });

        // 3. Thực hiện chuyển trang
        if (isClientLogin.value) {
            router.push('/');
        } else {
            router.push('/admin/dashboard');
        }

    } catch (error) {
        console.error("Lỗi đăng nhập:", error);
        
        // Mặc định là thông báo sai thông tin
        let errorMsg = "Sai tài khoản hoặc mật khẩu"; 
        let errorTitle = "Đăng nhập thất bại";

        // Xử lý bóc tách lỗi từ Backend
        if (error.response) {
            const status = error.response.status;
            const data = error.response.data;

            if (status === 403) {
                errorTitle = "Tài khoản bị khóa";
                errorMsg = data.message || data || "Tài khoản của bạn tạm thời không thể truy cập.";
            } else if (status === 401) {
                errorMsg = "Tên đăng nhập hoặc mật khẩu không chính xác.";
            } else if (data) {
                // Lấy message nếu Backend trả về Object { message: "..." }
                errorMsg = data.message || (typeof data === 'string' ? data : errorMsg);
            }
        } else if (error.request) {
            // Request đã gửi nhưng không nhận được phản hồi (Lỗi Server chết hoặc Network)
            errorMsg = "Không thể kết nối đến máy chủ. Vui lòng kiểm tra lại mạng!";
        } else {
            errorMsg = error.message;
        }

        // 4. Hiển thị thông báo lỗi
        Swal.fire({
            icon: 'error',
            title: errorTitle,
            text: errorMsg,
            confirmButtonColor: '#8B1A1A',
            confirmButtonText: 'Thử lại'
        });
    }
};

const switchTab = (isClient) => {
    isClientLogin.value = isClient;
    email.value = '';
    password.value = '';
    errors.value = {}; //reset lỗi khi chuyển tab
    errorMessage.value = '';
    wasSubmitted.value = false; 
}

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
            <div class="tab-switcher mb-4">
                <button :class="['tab-btn', { active: isClientLogin }]" @click="switchTab(true)">Khách hàng</button>
                <button :class="['tab-btn', { active: !isClientLogin }]" @click="switchTab(false)">Quản lý</button>
            </div>

            <h2 class="login-title">Đăng nhập</h2>
            <p class="login-subtitle">Trải nghiệm không gian ẩm thực tinh hoa</p>

            <form @submit.prevent="handleLogin" class="mt-4">
                <div class="mb-3">
                    <input v-model="email" type="text" class="custom-input"
                        :placeholder="isClientLogin ? 'Email' : 'Tên đăng nhập'"
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

            <!-- <div class="register-hint mt-4">
                Chưa có tài khoản? <a @click="navigateToRegister" class="register-link">Đăng ký ngay</a>
            </div> -->
            <div v-if="isClientLogin" class="register-hint mt-4">
                Chưa có tài khoản? <a @click="navigateToRegister" class="register-link">Đăng ký ngay</a>
            </div>

            <template v-if="isClientLogin">
                <div class="divider">
                    <span>HOẶC TIẾP TỤC VỚI</span>
                </div>

                <div class="social-login">
                    <button @click="loginWithGoogle" class="social-btn">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 48 48">
                            <path fill="#FFC107"
                                d="M43.611,20.083H42V20H24v8h11.303c-1.649,4.657-6.08,8-11.303,8c-6.627,0-12-5.373-12-12c0-6.627,5.373-12,12-12c3.059,0,5.842,1.154,7.961,3.039l5.657-5.657C34.046,6.053,29.268,4,24,4C12.955,4,4,12.955,4,24c0,11.045,8.955,20,20,20c11.045,0,20-8.955,20-20C44,22.659,43.862,21.35,43.611,20.083z" />
                            <path fill="#FF3D00"
                                d="M6.306,14.691l6.571,4.819C14.655,15.108,18.961,12,24,12c3.059,0,5.842,1.154,7.961,3.039l5.657-5.657C34.046,6.053,29.268,4,24,4C16.318,4,9.656,8.337,6.306,14.691z" />
                            <path fill="#4CAF50"
                                d="M24,44c5.166,0,9.86-1.977,13.409-5.192l-6.19-5.238C29.211,35.091,26.715,36,24,36c-5.202,0-9.619-3.317-11.283-7.946l-6.522,5.025C9.505,39.556,16.227,44,24,44z" />
                            <path fill="#1976D2"
                                d="M43.611,20.083H42V20H24v8h11.303c-0.792,2.237-2.231,4.166-4.087,5.571l6.19,5.238C36.971,39.205,44,34,44,24C44,22.659,43.862,21.35,43.611,20.083z" />
                        </svg>
                        <span>Google</span>
                    </button>

                    <!-- <button class="social-btn">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="#1877F2"
                            viewBox="0 0 24 24">
                            <path
                                d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z" />
                        </svg>
                        <span>Facebook</span>
                    </button> -->
                </div>
            </template>
        </div>
        <div v-if="showForgotModal" class="modal-overlay" @click.self="showForgotModal = false">
            <div class="forgot-modal">
                <div class="text-end">
                    <button @click="showForgotModal = false" class="btn-close-modal">&times;</button>
                </div>
                <h3 class="modal-title">Quên mật khẩu?</h3>
                <p class="modal-desc">Đừng lo lắng, nhập email của bạn để nhận lại mật khẩu mới từ CozyPot.</p>

                <div class="mb-4 text-start"> <input v-model="forgotEmail" type="email" class="custom-input shadow-sm"
                        placeholder="Nhập email của bạn">
                    <small v-if="forgotError" class="text-danger mt-1 d-block">{{ forgotError }}</small>
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
:root {
    --cozy-red: #6d1313;
    /* Đỏ thẫm từ chữ CozyPot */
    --cozy-gold: #D4A017;
    /* Vàng đồng từ icon nồi lẩu */
}

.main-content {
    background-color: #f8f9fa;
    /* Khóa chặt vị trí vào khung nhìn, không cho cuộn trang */
    position: fixed;
    inset: 0;
    /* Tương đương top:0, left:0, right:0, bottom:0 */
    width: 100vw;
    height: 100vh;

    /* Căn giữa tuyệt đối nội dung bên trong */
    display: flex;
    align-items: center;
    justify-content: center;

    padding: 20px;
    overflow: hidden;
    /* Triệt tiêu hoàn toàn thanh cuộn trình duyệt */
}

.login-card {
    background: #ffffff;
    width: 100%;
    max-width: 850px;

    /* Điều chỉnh padding dọc nhỏ lại một chút để không bị "kích" chiều cao */
    padding: 40px 100px;
    border-radius: 40px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.05);
    text-align: center;

    /* Đảm bảo card không bao giờ vượt quá chiều cao màn hình */
    max-height: 90vh;
    overflow-y: auto;
    /* Chỉ cho phép cuộn bên trong card nếu nội dung quá dài */

    /* Khử mọi margin thừa có thể gây xê dịch */
    margin: 0;
}

/* Tab Switcher Style */
.tab-switcher {
    background: #f1f1f1;
    display: inline-flex;
    padding: 5px;
    border-radius: 12px;
}

.tab-btn {
    border: none;
    padding: 6px 20px;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 500;
    transition: 0.3s;
    background: transparent;
    color: #666;
}

.tab-btn.active {
    background: #6d1414;
    color: white;
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

/* Input Style */
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
    border-color: var(--cozy-red);
    box-shadow: 0 0 0 3px rgba(139, 26, 26, 0.1);
}

.remember-text,
.forgot-password {
    color: #75231a;
    text-decoration: none;
    font-weight: 500;
}

.custom-checkbox {
    width: 18px;
    height: 18px;
    border-radius: 4px;
    cursor: pointer;
}

.custom-checkbox:hover {
    filter: brightness(0.9);
}

/* Button Login */
.btn-login {
    width: 100%;
    background: var(--cozy-red);
    color: rgb(255, 255, 255);
    border: none;
    padding: 14px;
    border-radius: 15px;
    font-weight: 600;
    font-size: 16px;
    transition: 0.3s;
    background: #7d1f1f;
    box-shadow: 0 4px 15px rgba(139, 26, 26, 0.2);
}

.btn-login:hover {
    background: #6d1414;
    /* Đỏ đậm hơn khi hover */
    transform: translateY(-1px);
}

.register-hint {
    font-size: 14px;
    color: #666;
}

.register-link {
    color: #75231a;
    font-weight: 700;
    text-decoration: underline;
    cursor: pointer;
}

/* Social Login */
.divider {
    margin: 30px 0 20px;
    border-bottom: 1px solid #eee;
    position: relative;
}

.divider span {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background: #fff;
    padding: 0 15px;
    color: #aaa;
    font-size: 11px;
    letter-spacing: 1px;
}

form {
    max-width: 500px;
    /* Giới hạn độ rộng của riêng form bên trong card */
    margin: 0 auto;
    /* Căn giữa form */
}

.social-login {
    max-width: 500px;
    margin: 0 auto;
    display: flex;
    gap: 15px;
}

.social-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    background: #fff;
    border: 1px solid #eee;
    padding: 10px;
    border-radius: 12px;
    font-weight: 500;
    font-size: 14px;
    transition: 0.2s;
}

.social-btn:hover {
    background: #f8f9fa;
}

/* Modal Quên mật khẩu */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.3);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
    backdrop-filter: blur(4px);
}

/* Khung Popup */
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

.btn-close-modal {
    background: none;
    border: none;
    font-size: 24px;
    line-height: 1;
    color: #444;
    cursor: pointer;
    padding: 0 5px;
    transition: all 0.2s;
    font-weight: 300;
}

.btn-close-modal:hover {
    color: #000;
    transform: scale(1.1);
}

@keyframes popIn {
    from {
        transform: scale(0.9);
        opacity: 0;
    }

    to {
        transform: scale(1);
        opacity: 1;
    }
}

.modal-header-custom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
}

.modal-title {
    font-size: 1.5rem;
    color: #8B1A1A;
    /* Màu đỏ CozyPot */
    font-weight: 700;
}

.close-btn {
    background: none;
    border: none;
    font-size: 1.8rem;
    color: #999;
    cursor: pointer;
}

.modal-desc {
    font-size: 0.95rem;
    color: #555;
    margin-bottom: 20px;
}

/* Biến nút login của bạn dùng chung cho nút Gửi */
.btn-login:disabled {
    background: #ccc;
    cursor: not-allowed;
}
</style>
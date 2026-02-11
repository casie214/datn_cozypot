<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from './authenticationServices/authenticationService';
import CommonNav from '@/components/commonNav.vue';
import GoogleLoginCallback from './googleLoginCallback.vue';

const authStore = useAuthStore();
const router = useRouter();
const isClientLogin = ref(true);

const navigateToRegister = () => {
    router.push('/register');
}

const email = ref('');
const password = ref('');
const errorMessage = ref('');

const errors = ref({});

const validate = () => {
    errors.value = {};
    let isValid = true;

    // validate email
    if (!email.value) {
        errors.value.email = isClientLogin.value ? "Email không được để trống" : "Tên đăng nhập không được để trống";
        isValid = false;
    } 
    else if (isClientLogin.value) { 
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email.value)) {
            errors.value.email = "Email không đúng định dạng";
            isValid = false;
        }
    }

    // validate mật khẩu
    if (!password.value) {
        errors.value.password = "Mật khẩu không được để trống";
        isValid = false;
    } else if (password.value.length < 6) {
        errors.value.password = "Mật khẩu phải có ít nhất 6 ký tự";
        isValid = false;
    }
    return isValid;
}

const handleLogin = async () => {
    if(!validate()){
        return;
    }
    errorMessage.value = '';

    try {
        await authStore.login(email.value, password.value, isClientLogin.value);

        if (isClientLogin.value) {
            alert("Đăng nhập thành công!");
            router.push('/');
        } else {
            router.push('/admin/dashboard');
        }

    } catch (error) {
        if (error.response && error.response.data) {
            errorMessage.value = error.response.data.message || "Sai tài khoản hoặc mật khẩu";
        } else {
            errorMessage.value = "Lỗi kết nối đến Server!";
        }
    }
}

const switchTab = (isClient) => {
    isClientLogin.value = isClient;
    errorMessage.value = '';
    email.value = '';
    password.value = '';
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
        
        <div class="etched-container">
            <section class="py-3 py-md-5 py-xl-8 etched-container-2">
                <div class="container">
                    <div class="container-fluid px-5">
                        <div class="row">
                            <div class="col-12">
                                <div class="mb-5">
                                    <h2 class="display-5 fw-bold text-center">
                                        {{ isClientLogin ? 'Đăng nhập' : 'Cổng nhân viên' }}
                                    </h2>

                                    <p v-if="isClientLogin" class="text-center m-0">
                                        Chưa có tài khoản?
                                        <a style="cursor: pointer;" class="register-link" @click="navigateToRegister">Đăng ký</a>
                                    </p>
                                    <p v-else class="text-center m-0 text-secondary small">
                                        Cho nhân viên và quản lý
                                    </p>
                                </div>
                            </div>
                        </div>

                        <div class="row justify-content-center">
                            <div class="col-12 col-lg-10 col-xl-8">
                                <div class="row gy-5 justify-content-center">
                                    <div class="col-12 col-lg-5">

                                        <div v-if="errorMessage" class="alert alert-danger p-2 text-center small mb-3">
                                            {{ errorMessage }}
                                        </div>

                                        <form @submit.prevent="handleLogin">
                                            <div class="row gy-3 overflow-hidden">
                                                <div class="col-12">
                                                    <div class="form-floating mb-3">
                                                        <input v-model="email" type="text"
                                                            class="form-control border-0 border-bottom rounded-0"
                                                            :class="{ 'is-invalid': errors.email }"
                                                            name="email" id="email" placeholder="name@example.com"
                                                            >

                                                        <label for="email" class="form-label"
                                                            :class="{ 'admin-label': !isClientLogin }">
                                                            {{ isClientLogin ? 'Email' : 'Tên đăng nhập' }}
                                                        </label>
                                                        <div class="invalid-feedback">{{ errors.email }}</div>
                                                    </div>
                                                </div>
                                                <div class="col-12">
                                                    <div class="form-floating mb-3">
                                                        <input v-model="password" type="password"
                                                            class="form-control border-0 border-bottom rounded-0"
                                                            :class="{ 'is-invalid': errors.password }"
                                                            name="password" id="password" value=""
                                                            placeholder="Password">
                                                        <label for="password" class="form-label">Mật khẩu</label>
                                                        <div class="invalid-feedback">{{ errors.password }}</div>
                                                    </div>
                                                </div>

                                                <div class="col-12">
                                                    <div class="row justify-content-between">
                                                        <div class="col-12">
                                                            <div class="text-start">
                                                                <a href="#!"
                                                                    class="link-secondary text-decoration-none">Quên mật khẩu?</a>
                                                            </div>
                                                        </div>
                                                        
                                                    </div>
                                                </div>

                                                <div class="col-12">
                                                    <div class="row justify-content-between">
                                                        <div class="col-12">
                                                            <div class="form-check">
                                                                <input class="form-check-input" type="checkbox" value=""
                                                                    name="remember_me" id="remember_me">
                                                                <label class="form-check-label text-secondary"
                                                                    for="remember_me">Nhớ đăng nhập</label>
                                                            </div>
                                                        </div>
                                                        
                                                    </div>
                                                </div>
                                                

                                                <div class="col-12">
                                                    <div class="d-grid">
                                                        <button class="btn btn-lg rounded-0 fs-6"
                                                            :class="isClientLogin ? 'btn-dark' : 'btn-danger'"
                                                            type="submit">
                                                            {{ isClientLogin ? 'Đăng nhập' : 'Truy cập' }}
                                                        </button>
                                                    </div>
                                                </div>
                                            </div>
                                        </form>
                                    </div>

                                    <template v-if="isClientLogin">
                                        <div
                                            class="col-12 col-lg-2 d-flex align-items-center justify-content-center gap-3 flex-lg-column">
                                            <div class="bg-dark h-100 d-none d-lg-block"
                                                style="width: 1px; --bs-bg-opacity: .1;"></div>
                                            <div class="bg-dark w-100 d-lg-none"
                                                style="height: 1px; --bs-bg-opacity: .1;"></div>
                                            <div>or</div>
                                            <div class="bg-dark h-100 d-none d-lg-block"
                                                style="width: 1px; --bs-bg-opacity: .1;"></div>
                                            <div class="bg-dark w-100 d-lg-none"
                                                style="height: 1px; --bs-bg-opacity: .1;"></div>
                                        </div>
                                        <div class="col-12 col-lg-5 d-flex align-items-center">
                                            <div class="d-flex gap-3 flex-column w-100 ">
                                                <a @click="loginWithGoogle"
                                                    class="btn bsb-btn-2xl btn-outline-dark rounded-0 d-flex align-items-center">
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16"
                                                        fill="currentColor" class="bi bi-google text-danger"
                                                        viewBox="0 0 16 16">
                                                        <path
                                                            d="M15.545 6.558a9.42 9.42 0 0 1 .139 1.626c0 2.434-.87 4.492-2.384 5.885h.002C11.978 15.292 10.158 16 8 16A8 8 0 1 1 8 0a7.689 7.689 0 0 1 5.352 2.082l-2.284 2.284A4.347 4.347 0 0 0 8 3.166c-2.087 0-3.86 1.408-4.492 3.304a4.792 4.792 0 0 0 0 3.063h.003c.635 1.893 2.405 3.301 4.492 3.301 1.078 0 2.004-.276 2.722-.764h-.003a3.702 3.702 0 0 0 1.599-2.431H8v-3.08h7.545z" />
                                                    </svg>
                                                    <span class="ms-2 fs-6 flex-grow-1">Tiếp tục với Google</span>
                                                </a>
                                                <a href="#!"
                                                    class="btn bsb-btn-2xl btn-outline-dark rounded-0 d-flex align-items-center">
                                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16"
                                                        fill="currentColor" class="bi bi-facebook text-primary"
                                                        viewBox="0 0 16 16">
                                                        <path
                                                            d="M16 8.049c0-4.446-3.582-8.05-8-8.05C3.58 0-.002 3.603-.002 8.05c0 4.017 2.926 7.347 6.75 7.951v-5.625h-2.03V8.05H6.75V6.275c0-2.017 1.195-3.131 3.022-3.131.876 0 1.791.157 1.791.157v1.98h-1.009c-.993 0-1.303.621-1.303 1.258v1.51h2.218l-.354 2.326H9.25V16c3.824-.604 6.75-3.934 6.75-7.951z" />
                                                    </svg>
                                                    <span class="ms-2 fs-6 flex-grow-1">Tiếp tục với Facebook</span>
                                                </a>
                                            </div>
                                        </div>
                                    </template>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="px-4 mb-2 d-flex pt-5">
                    <div class="btn-group shadow-sm" role="group" aria-label="Login Type">
                        <button type="button" class="btn btn-sm px-4"
                            :class="isClientLogin ? 'btn-dark' : 'btn-outline-dark'" @click="isClientLogin = true">
                            Khách hàng
                        </button>
                        <button type="button" class="btn btn-sm px-4"
                            :class="!isClientLogin ? 'btn-danger' : 'btn-outline-secondary'"
                            @click="isClientLogin = false">
                            Quản lý
                        </button>
                    </div>
                </div>

            </section>
        </div>
    </div>
</template>



<style scoped>
.main-content {
    display: flex;
    justify-content: center;
    height: 80vh;
}

.etched-container {
    margin: 0;
    padding: 0;
    display: flex;
    padding-top: 30px;
    justify-content: center;
    height: 100%;
    width: 80%;
}

.etched-container-2 {
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    height: 90%;
    width: 70%;
    border-radius: 10px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    background-color: white;
}

.container {
    width: 100% !important;
}

.login-link:hover {
    color: #0d6efd;
}

.half-sized {
    width: 50%;
}

.invalid-feedback {
    display: block;
    width: 100%;
    margin-top: 0.25rem;
    font-size: 0.875em;
    color: #dc3545;
    text-align: left;
}

.is-invalid {
    border-color: #dc3545 !important;
    padding-right: calc(1.5em + 0.75rem);
    background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12' width='12' height='12' fill='none' stroke='%23dc3545'%3e%3ccircle cx='6' cy='6' r='4.5'/%3e%3cpath stroke-linejoin='round' d='M5.8 3.6h.4L6 6.5z'/%3e%3ccircle cx='6' cy='8.2' r='.6' fill='%23dc3545' stroke='none'/%3e%3c/svg%3e");
    background-repeat: no-repeat;
    background-position: right calc(0.375em + 0.1875rem) center;
    background-size: calc(0.75em + 0.375rem) calc(0.75em + 0.375rem);
}
.form-control.is-invalid:focus {
    border-bottom: 1px solid #dc3545 !important;
    box-shadow: 0 0 0 0.25rem rgba(220, 53, 69, 0.25);
}
</style>
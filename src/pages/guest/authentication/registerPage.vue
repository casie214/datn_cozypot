<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from './authenticationServices/authenticationService';
import Swal from 'sweetalert2'

const router = useRouter();
const authStore = useAuthStore();

const tenDangNhap = ref('')
const tenKhachHang = ref('');
const email = ref('');
const matKhauDangNhap = ref('');
const soDienThoai = ref('');
const diaChi = ref('');
const ngaySinh = ref('');
const gioiTinh = ref(true);
const errorMessage = ref('');
const isSubmitting = ref(false);

const errors = ref({});

const validate = () => {
    errors.value = {};
    let isValid = true;

    // validate email
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email.value) {
        errors.value.email = "Email không được để trống";
        isValid = false;
    } else if (!emailRegex.test(email.value)) {
        errors.value.email = "Email không đúng định dạng";
        isValid = false;
    }

    // validate Mật khẩu
    if (!matKhauDangNhap.value) {
        errors.value.matKhau = "Mật khẩu không được để trống";
        isValid = false;
    } else if (matKhauDangNhap.value.length < 6) {
        errors.value.matKhau = "Mật khẩu phải có ít nhất 6 ký tự";
        isValid = false;
    }

    // validate họ tên
    if (!tenKhachHang.value) {
        errors.value.tenKhachHang = "Họ tên không được để trống";
        isValid = false;
    }

    if (!tenDangNhap.value) {
        errors.value.tenDangNhap = "Tên hiển thị không được để trống";
        isValid = false;
    }

    // validate sdt
    const phoneRegex = /(84|0[3|5|7|8|9])+([0-9]{8})\b/;
    if (!soDienThoai.value) {
        errors.value.soDienThoai = "Số điện thoại không được để trống";
        isValid = false;
    } else if (!phoneRegex.test(soDienThoai.value)) {
        errors.value.soDienThoai = "Số điện thoại không hợp lệ (VN)";
        isValid = false;
    }

    // validate ngày sinh
    if (!ngaySinh.value) {
        errors.value.ngaySinh = "Vui lòng chọn ngày sinh";
        isValid = false;
    }

    return isValid;
}

const handleRegister = async () => {
    if (!validate()) {
        return;
    }

    errorMessage.value = '';
    isSubmitting.value = true;

    try {
        const payload = {
            tenDangNhap: tenDangNhap.value,
            tenKhachHang: tenKhachHang.value,
            email: email.value,
            matKhauDangNhap: matKhauDangNhap.value,
            soDienThoai: soDienThoai.value,
            diaChi: diaChi.value,
            ngaySinh: ngaySinh.value,
            gioiTinh: gioiTinh.value
        };

        await authStore.register(payload);

        Swal.fire({
            icon: 'success',
            title: 'Đăng ký thành công!',
            text: 'Vui lòng đăng nhập để tiếp tục.',
            confirmButtonColor: '#212529',
            confirmButtonText: 'Đến trang đăng nhập'
        }).then((result) => {
            if (result.isConfirmed) {
                router.push('/login');
            }
        });

        router.push('/login');
    } catch (error) {
        let msg = "Lỗi kết nối Server.";
        
        if (error.response && error.response.data) {
            msg = error.response.data.message || "Đăng ký thất bại.";
            
            if (typeof error.response.data === 'object' && !error.response.data.message) {
                 msg = JSON.stringify(error.response.data); 
            }
        }
        
        Swal.fire({
            icon: 'error',
            title: 'Đăng ký thất bại',
            text: msg,
            confirmButtonColor: '#dc3545'
        });
        
    } finally {
        isSubmitting.value = false;
    }
}
const navigateToLogin = () => {
    router.push('/login');
}
</script>

<template>
    <div class="main-content">
        <div class="etched-container">
            <section class="py-3 py-md-5 py-xl-8 etched-container-2">

                <div class="container">
                    <div class="container-fluid px-5">

                        <div class="row mb-4">
                            <div class="col-12">
                                <div class="text-center">
                                    <h2 class="display-5 fw-bold">Đăng ký</h2>
                                    <p class="m-0">Bạn đã có tài khoản?
                                        <a class="login-link text-primary"
                                            style="cursor: pointer; text-decoration: underline;"
                                            @click="navigateToLogin">Đăng nhập</a>
                                    </p>
                                </div>
                            </div>
                        </div>

                        <div v-if="errorMessage" class="alert alert-danger p-2 text-center small mb-3">
                            {{ errorMessage }}
                        </div>

                        <div class="row justify-content-center">
                            <div class="col-12 half-sized">
                                <form @submit.prevent="handleRegister">
                                    <div class="row gy-3">

                                        <div class="col-12 col-lg-6">
                                            <div class="form-floating mb-3">
                                                <input v-model="email" type="email"
                                                    class="form-control border-0 border-bottom rounded-0"
                                                    :class="{ 'is-invalid': errors.email }" id="email"
                                                    placeholder="Email">
                                                <label for="email">Email</label>
                                                <div class="invalid-feedback">{{ errors.email }}</div>
                                            </div>

                                            <div class="form-floating mb-3">
                                                <input v-model="matKhauDangNhap" type="password"
                                                    class="form-control border-0 border-bottom rounded-0"
                                                    :class="{ 'is-invalid': errors.matKhau }" id="password"
                                                    placeholder="Password">
                                                <label for="password">Mật khẩu</label>
                                                <div class="invalid-feedback">{{ errors.matKhau }}</div>
                                            </div>

                                            <div class="form-floating mb-3">
                                                <input v-model="tenKhachHang" type="text"
                                                    class="form-control border-0 border-bottom rounded-0"
                                                    :class="{ 'is-invalid': errors.tenKhachHang }" id="fullname"
                                                    placeholder="Full Name">
                                                <label for="fullname">Họ và tên</label>
                                                <div class="invalid-feedback">{{ errors.tenKhachHang }}</div>
                                            </div>

                                            <div class="form-floating mb-3">
                                                <input v-model="tenDangNhap" type="text"
                                                    class="form-control border-0 border-bottom rounded-0" id="username"
                                                    :class="{ 'is-invalid': errors.tenDangNhap }"
                                                    placeholder="User Name">
                                                <label for="username">Tên hiển thị</label>
                                                <div class="invalid-feedback">{{ errors.tenDangNhap }}</div>
                                            </div>
                                        </div>

                                        <div class="col-12 col-lg-6">
                                            <div class="form-floating mb-3">
                                                <input v-model="soDienThoai" type="tel"
                                                    class="form-control border-0 border-bottom rounded-0"
                                                    :class="{ 'is-invalid': errors.soDienThoai }" id="phone"
                                                    placeholder="Phone">
                                                <label for="phone">Số điện thoại</label>
                                                <div class="invalid-feedback">{{ errors.soDienThoai }}</div>
                                            </div>

                                            <div class="form-floating mb-3">
                                                <input v-model="ngaySinh" type="date"
                                                    class="form-control border-0 border-bottom rounded-0"
                                                    :class="{ 'is-invalid': errors.ngaySinh }" id="dob">
                                                <label for="dob">Ngày sinh</label>
                                                <div class="invalid-feedback">{{ errors.ngaySinh }}</div>
                                            </div>

                                            <div class="form-floating mb-3">
                                                <input v-model="diaChi" type="text"
                                                    class="form-control border-0 border-bottom rounded-0" id="address"
                                                    placeholder="Address">
                                                <label for="address">Địa chỉ</label>
                                            </div>

                                            <div class="col-12 mb-2" style="margin-top: 50px;">
                                                <div class="form-check form-check-inline">
                                                    <input class="form-check-input" type="radio" :value="true"
                                                        v-model="gioiTinh" id="male">
                                                    <label class="form-check-label" for="male">Nam</label>
                                                </div>
                                                <div class="form-check form-check-inline">
                                                    <input class="form-check-input" type="radio" :value="false"
                                                        v-model="gioiTinh" id="female">
                                                    <label class="form-check-label" for="female">Nữ</label>
                                                </div>
                                            </div>
                                        </div>



                                        <div class="col-12">
                                            <div class="d-grid">
                                                <button class="btn btn-lg btn-dark rounded-0 fs-6" type="submit"
                                                    :disabled="isSubmitting">
                                                    {{ isSubmitting ? 'Đang xử lý...' : 'Đăng ký tài khoản' }}
                                                </button>
                                            </div>
                                        </div>

                                    </div>
                                </form>
                            </div>
                        </div>

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
    background: white;
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
</style>
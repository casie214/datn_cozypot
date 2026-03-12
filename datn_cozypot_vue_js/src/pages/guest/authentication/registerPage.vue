<script setup>
import { ref, onMounted, watch, onUnmounted, reactive } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { useAuthStore } from './authenticationServices/authenticationService';
import Swal from 'sweetalert2';
import OtpModal from './OtpModal.vue';
import dayjs from 'dayjs';
import Multiselect from '@vueform/multiselect'
import '@vueform/multiselect/themes/default.css'

const router = useRouter();
const authStore = useAuthStore();
const todayDate = dayjs().subtract(16, "year").format("YYYY-MM-DD");
// Form Data
const tenDangNhap = ref('');
const tenKhachHang = ref('');
const email = ref('');
const matKhauDangNhap = ref('');
const soDienThoai = ref('');
const ngaySinh = ref('');
const gioiTinh = ref(true);
const isSubmitting = ref(false);

// Address Data
const listTinh = ref([]);
const listQuan = ref([]);
const listPhuong = ref([]);
const selectedTinh = ref('');
const selectedQuan = ref('');
const selectedPhuong = ref('');
const diaChiChiTiet = ref('');

const errors = ref({});
const wasSubmitted = ref(false);

const isOtpModalOpen = ref(false);
const timeLeft = ref(900); // 15 phút = 900 giây
const resendTimer = ref(10); // 60 giây chờ để gửi lại mã
let timerInterval = null;

// Hàm bắt đầu đếm ngược
const startCountdown = () => {
    if (timerInterval) clearInterval(timerInterval);
    timerInterval = setInterval(() => {
        if (timeLeft.value > 0) {
            timeLeft.value--;
            if (resendTimer.value > 0) resendTimer.value--;
        } else {
            stopCountdown();
            handleCancelOtp(); // Hết hạn thì đóng modal, xóa cache
            Swal.fire('Hết hạn', 'Mã xác thực đã hết hạn, vui lòng đăng ký lại.', 'warning');
        }
    }, 1000);
};
const formData = reactive({
    ngaySinh: ""
});
const stopCountdown = () => {
    clearInterval(timerInterval);
};

// Đảm bảo dọn dẹp timer khi chuyển trang
onUnmounted(() => stopCountdown());

watch(
    [tenDangNhap, tenKhachHang, email, matKhauDangNhap, soDienThoai, ngaySinh, selectedTinh, selectedQuan, selectedPhuong, diaChiChiTiet],
    () => {
        if (wasSubmitted.value) {
            validate();
        }
    }
);

onMounted(async () => {
    // Tải tỉnh thành
    try {
        const res = await axios.get('https://provinces.open-api.vn/api/p/');
        listTinh.value = res.data;
    } catch (e) { console.error(e); }

    // Kiểm tra chống F5
    const savedData = localStorage.getItem('pending_registration');
    if (savedData) {
        const { email: savedEmail, expireAt, formData } = JSON.parse(savedData);
        const now = new Date().getTime();
        const expiry = new Date(expireAt).getTime();

        if (expiry > now) {
            // Gán lại từng biến ref từ formData
            tenDangNhap.value = formData.tenDangNhap;
            tenKhachHang.value = formData.tenKhachHang;
            email.value = formData.email;
            soDienThoai.value = formData.soDienThoai;
            ngaySinh.value = formData.ngaySinh;
            gioiTinh.value = formData.gioiTinh;
            selectedTinh.value = formData.idTinhThanh;
            diaChiChiTiet.value = formData.diaChiChiTiet;

            // Xử lý logic load Quận/Phường chờ một chút để API tỉnh thành kịp load
            setTimeout(async () => {
                selectedQuan.value = formData.idQuanHuyen;
                setTimeout(() => {
                    selectedPhuong.value = formData.idPhuongXa;
                }, 500);
            }, 500);

            timeLeft.value = Math.floor((expiry - now) / 1000);
            isOtpModalOpen.value = true;
            startCountdown();
        } else {
            localStorage.removeItem('pending_registration');
        }
    }
});

// Watch Tỉnh -> Load Quận
watch(selectedTinh, async (newVal) => {
    selectedQuan.value = '';
    selectedPhuong.value = '';
    listQuan.value = [];
    if (!newVal) return;
    try {
        const res = await axios.get(`https://provinces.open-api.vn/api/p/${newVal}?depth=2`);
        listQuan.value = res.data.districts;
    } catch (e) { console.error(e); }
});

// Watch Quận -> Load Phường
watch(selectedQuan, async (newVal) => {
    selectedPhuong.value = '';
    listPhuong.value = [];
    if (!newVal) return;
    try {
        const res = await axios.get(`https://provinces.open-api.vn/api/d/${newVal}?depth=2`);
        listPhuong.value = res.data.wards;
    } catch (e) { console.error(e); }
});

const validate = () => {
    errors.value = {};
    let isValid = true;
    if (!email.value) {
        errors.value.email = "Email không được để trống";
        isValid = false;
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) {
        errors.value.email = "Email không đúng định dạng";
        isValid = false;
    }
    if (!matKhauDangNhap.value) {
        errors.value.matKhau = "Mật khẩu không được để trống";
        isValid = false;
    } else if (matKhauDangNhap.value.length < 6) {
        errors.value.matKhau = "Mật khẩu ít nhất 6 ký tự";
        isValid = false;
    }
    if (!tenKhachHang.value?.trim()) { errors.value.tenKhachHang = "Họ tên không được để trống"; isValid = false; }
    if (!tenDangNhap.value?.trim()) { errors.value.tenDangNhap = "Tên đăng nhập không được để trống"; isValid = false; }

    if (!soDienThoai.value) {
        errors.value.soDienThoai = "Số điện thoại không được để trống";
        isValid = false;
    } else if (!/^(0[3|5|7|8|9])[0-9]{8}$/.test(soDienThoai.value)) {
        errors.value.soDienThoai = "SĐT phải đủ 10 số và đúng định dạng Việt Nam";
        isValid = false;
    }
    if (!formData.ngaySinh) {
        errors.value.ngaySinh = "Vui lòng chọn ngày sinh";
        isValid = false;
    } else {
        const selectedDate = new Date(formData.ngaySinh);
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        if (selectedDate > today) {
            errors.value.ngaySinh = "Ngày sinh không được là ngày ở tương lai";
            isValid = false;
        }
    } if (!selectedTinh.value || !selectedQuan.value || !selectedPhuong.value || !diaChiChiTiet.value?.trim()) {
        errors.value.diaChi = "Vui lòng điền đầy đủ địa chỉ";
        isValid = false;
    }
    return isValid;
};

const handleRegister = async () => {
    wasSubmitted.value = true;
    if (!validate()) return;

    isSubmitting.value = true;
    Swal.fire({
        title: 'Đang gửi mã OTP...',
        allowOutsideClick: false,
        didOpen: () => { Swal.showLoading(); }
    });

    // Gom dữ liệu vào một object để lưu tạm
    const payload = {
        tenDangNhap: tenDangNhap.value,
        tenKhachHang: tenKhachHang.value,
        email: email.value,
        matKhauDangNhap: matKhauDangNhap.value,
        soDienThoai: soDienThoai.value,
        ngaySinh: formData.ngaySinh,
        gioiTinh: gioiTinh.value,

        idTinhThanh: String(selectedTinh.value),
        idQuanHuyen: String(selectedQuan.value),
        idPhuongXa: String(selectedPhuong.value),

        tenTinhThanh: listTinh.value.find(t => t.code == selectedTinh.value)?.name,
        tenQuanHuyen: listQuan.value.find(q => q.code == selectedQuan.value)?.name,
        tenPhuongXa: listPhuong.value.find(p => p.code == selectedPhuong.value)?.name,

        diaChiChiTiet: diaChiChiTiet.value
    };

    try {
        // 1. Gọi API gửi mail và lưu bảng tạm
        await axios.post('/api/auth/register', payload);

        // 2. Lưu vào LocalStorage chống F5
        const expireAt = new Date(new Date().getTime() + 15 * 60 * 1000);
        localStorage.setItem('pending_registration', JSON.stringify({
            email: email.value,
            expireAt: expireAt,
            formData: payload
        }));

        // 3. Hiển thị Popup
        timeLeft.value = 900;
        resendTimer.value = 10;
        isOtpModalOpen.value = true;
        startCountdown();

        Swal.close();
        await Swal.fire({
            icon: 'success',
            iconColor: '#7D161A',
            title: 'Thành công',
            text: 'Vui lòng kiểm tra email để lấy mã OTP',
            showConfirmButton: false,
            timer: 2000
        });
    } catch (error) {
        const errorMsg = error.response?.data?.message || "Lỗi hệ thống";
        Swal.fire('Lỗi', errorMsg, 'error');
    } finally {
        isSubmitting.value = false;
    }
};
const handleVerifyOtp = async (otpCode) => {
    try {
        await axios.post(`/api/auth/verify-otp`, {
            email: email.value,
            otp: otpCode
        });

        localStorage.removeItem('pending_registration');
        isOtpModalOpen.value = false;
        stopCountdown();

        await Swal.fire({
            icon: 'success',
            iconColor: '#7D161A',
            title: 'Thành công',
            text: 'Đăng ký tài khoản thành công!',
            showConfirmButton: false,
            timer: 2000
        });

        router.push('/login');
    } catch (error) {
        Swal.fire('Lỗi', error.response?.data?.message || 'Mã OTP không chính xác', 'error');
    }
};

const handleCancelOtp = () => {
    isOtpModalOpen.value = false;
    stopCountdown();
    localStorage.removeItem('pending_registration');
};

const handleResendOtp = async () => {
    try {
        await axios.post('/api/auth/resend-otp', { email: email.value });
        resendTimer.value = 10;
        Swal.fire('Đã gửi lại', 'Mã OTP mới đã được gửi đến email của bạn', 'success');
    } catch (error) {
        Swal.fire('Lỗi', 'Không thể gửi lại mã lúc này', 'error');
    }
};
const navigateToLogin = () => router.push('/login');
</script>

<template>
    <div class="main-content">
        <div class="registration-card">
            <div class="card-header-box">
                <h2 class="login-title">Đăng ký tài khoản</h2>
                <p class="login-subtitle">Khởi tạo tài khoản để bắt đầu mua sắm cùng chúng tôi</p>
            </div>

            <form @submit.prevent="handleRegister" class="registration-form">
                <div class="row g-4">
                    <div class="col-md-6 border-column">
                        <h5 class="section-header"><i class="bi bi-person-circle"></i> Thông tin cá nhân</h5>

                        <div class="input-group-custom">
                            <input v-model="tenKhachHang" type="text" class="custom-input"
                                :class="{ 'is-invalid': errors.tenKhachHang }" placeholder="Họ và tên *">
                            <div class="invalid-feedback">{{ errors.tenKhachHang }}</div>
                        </div>

                        <div class="input-group-custom">
                            <input v-model="email" type="email" class="custom-input"
                                :class="{ 'is-invalid': errors.email }" placeholder="Email *">
                            <div class="invalid-feedback">{{ errors.email }}</div>
                        </div>

                        <div class="input-group-custom">
                            <input v-model="soDienThoai" type="tel" class="custom-input"
                                :class="{ 'is-invalid': errors.soDienThoai }" placeholder="Số điện thoại *">
                            <div class="invalid-feedback">{{ errors.soDienThoai }}</div>
                        </div>

                        <div class="input-group-custom">
                            <label class="date-label">Ngày sinh *</label>
                            <input v-model="formData.ngaySinh" type="date" class="custom-input"
                                :class="{ 'is-invalid': errors.ngaySinh }"
                                :max="dayjs().subtract(16, 'year').format('YYYY-MM-DD')">
                            <div class="error-text" v-if="errors.ngaySinh">{{ errors.ngaySinh }}</div>
                        </div>

                        <div class="gender-selection">
                            <span>Giới tính:</span>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" :value="true" v-model="gioiTinh" id="m">
                                <label class="form-check-label" for="m">Nam</label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" :value="false" v-model="gioiTinh" id="f">
                                <label class="form-check-label" for="f">Nữ</label>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6 ps-md-4">
                        <h5 class="section-header"><i class="bi bi-shield-lock"></i> Tài khoản & Địa chỉ</h5>

                        <div class="input-group-custom">
                            <input v-model="tenDangNhap" type="text" class="custom-input"
                                :class="{ 'is-invalid': errors.tenDangNhap }" placeholder="Tên đăng nhập *">
                            <div class="invalid-feedback">{{ errors.tenDangNhap }}</div>
                        </div>

                        <div class="input-group-custom">
                            <input v-model="matKhauDangNhap" type="password" class="custom-input"
                                :class="{ 'is-invalid': errors.matKhau }" placeholder="Mật khẩu *">
                            <div class="invalid-feedback">{{ errors.matKhau }}</div>
                        </div>

                        <div class="row g-3 mb-3">
                            <div class="col-12">
                                <Multiselect v-model="selectedTinh" :options="listTinh" valueProp="code" label="name"
                                    placeholder="Chọn Tỉnh / Thành phố" :searchable="true" :canClear="false" />
                            </div>

                            <div class="col-6">
                                <Multiselect v-model="selectedQuan" :options="listQuan" valueProp="code" label="name"
                                    placeholder="Chọn Quận / Huyện" :searchable="true" :canClear="false"
                                    :disabled="!selectedTinh" />
                            </div>

                            <div class="col-6">
                                <Multiselect v-model="selectedPhuong" :options="listPhuong" valueProp="code"
                                    label="name" placeholder="Chọn Phường / Xã" :searchable="true" :canClear="false"
                                    :disabled="!selectedQuan" />
                            </div>
                        </div>

                        <div class="input-group-custom">
                            <textarea v-model="diaChiChiTiet" class="custom-input" rows="2"
                                placeholder="Số nhà, tên đường..."></textarea>
                            <div v-if="errors.diaChi" class="error-text">{{ errors.diaChi }}</div>
                        </div>
                    </div>
                </div>

                <div class="form-footer">
                    <button type="submit" class="btn-submit-gold" :disabled="isSubmitting || isOtpModalOpen">
                        {{ isSubmitting ? 'Đang gửi mã...' : 'Hoàn tất đăng ký' }}
                    </button>
                    <p class="mt-3">
                        Đã có tài khoản? <span class="login-link-text" @click="navigateToLogin">Đăng nhập ngay</span>
                    </p>
                </div>
            </form>
        </div>
    </div>
    <OtpModal :is-open="isOtpModalOpen" :email="email" :time-left="timeLeft" :resend-timer="resendTimer"
        @verify="handleVerifyOtp" @resend="handleResendOtp" @cancel="handleCancelOtp" />
</template>

<style scoped>
.main-content {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f4f7f6;
    /* Màu nền nhẹ nhàng */
    padding: 40px 20px;
}

.registration-card {
    background: #ffffff;
    width: 100%;
    max-width: 950px;
    padding: 50px;
    border-radius: 10px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
}

.card-header-box {
    text-align: center;
    margin-bottom: 40px;
}

.login-title {
    font-weight: 800;
    font-size: 30px;
    color: #6d1414;
    letter-spacing: -0.5px;
}

.login-subtitle {
    color: #888;
    font-size: 15px;
}

.section-header {
    font-size: 17px;
    font-weight: 700;
    color: #8B1A1A;
    margin-bottom: 25px;
    display: flex;
    align-items: center;
    gap: 10px;
}

:deep(.multiselect-option.is-pointed) {
    background: #f7dcdc !important;
    color: #7D161A !important;
}

:deep(.multiselect-option.is-selected) {
    background: #f2c4c4 !important;
    color: #7D161A !important;
}

:deep(.multiselect-option.is-selected.is-pointed) {
    background: #eab3b3 !important;
}

.input-group-custom {
    margin-bottom: 18px;
    position: relative;
}

.custom-input {
    width: 100%;
    padding: 12px 18px;
    background: #f9f9f9;
    border: 1px solid #edebeb;
    border-radius: 8px;
    font-size: 14px;
    transition: all 0.3s ease;
}

.custom-input:focus {
    background: #fff;
    border-color: #8B1A1A;
    outline: none;
    box-shadow: 0 5px 15px rgba(139, 26, 26, 0.08);
}

.date-label {
    display: block;
    font-size: 12px;
    color: #999;
    margin-left: 10px;
    margin-bottom: 4px;
}

.gender-selection {
    display: flex;
    align-items: center;
    gap: 20px;
    padding-left: 10px;
    font-size: 14px;
    color: #555;
}

.form-check-input:checked {
    background-color: #6d1414;
    border-color: #6d1414;
}

/* Kẻ dọc giữa 2 cột */
@media (min-width: 768px) {
    .border-column {
        border-right: 1px solid #f0f0f0;
        padding-right: 35px;
    }
}

.form-footer {
    text-align: center;
    margin-top: 30px;
}

.btn-submit-gold {
    width: 100%;
    max-width: 300px;
    background: #7d1f1f;
    color: white;
    border: none;
    padding: 14px;
    border-radius: 50px;
    font-weight: 600;
    font-size: 16px;
    transition: 0.3s;
    box-shadow: 0 8px 20px rgba(125, 31, 31, 0.2);
}

.btn-submit-gold:hover {
    background: #6d1414;
    transform: translateY(-2px);
    box-shadow: 0 10px 25px rgba(125, 31, 31, 0.3);
}

.login-link-text {
    color: #e19e72;
    font-weight: 700;
    cursor: pointer;
    text-decoration: underline;
}

.login-link-text:hover {
    color: #c0520e;
}

/* Validation Style */
.invalid-feedback {
    font-size: 11px;
    margin-left: 10px;
    color: #dc3545;
    display: block;
    text-align: left;
}

.error-text {
    color: #dc3545;
    font-size: 11px;
    margin-top: 5px;
    margin-left: 10px;
    text-align: left;
}

.is-invalid {
    border-color: #dc3545 !important;
    background-color: #fff8f8;
}

.small-text {
    font-size: 12px;
}

/* ===== SELECT ADDRESS ===== */

.multiselect {
    width: 100%;
    min-height: 40px;
    border: 1px solid #ddd;
    font-size: 13px;
    display: flex;
    align-items: center;
    background: #f9f9f9;
}


.multiselect-wrapper {
    border: 1px solid #dcdcdc !important;
    
    background: #fff;
}



/* khi focus */
.multiselect.is-active {
    border-color: #7D161A !important;
    box-shadow: 0 0 0 2px rgba(125, 22, 26, 0.08);
}

/* placeholder */
.multiselect-placeholder {
    font-size: 13px;
    color: #888;
}

/* label sau khi chọn */
.multiselect-single-label {
    font-size: 13px;
}

/* dropdown */
.multiselect-dropdown {
    border: 1px solid #eee;
}

/* hover option */
.multiselect-option.is-pointed {
    background: #f7e4e5 !important;
    color: #7D161A !important;
}

/* option đã chọn */
.multiselect-option.is-selected {
    background: #7D161A !important;
    color: #fff !important;
}

/* bỏ màu xanh mặc định */
.multiselect-option.is-selected.is-pointed {
    background: #7D161A !important;
}

/* search input */
.multiselect-search {
    font-size: 13px;
}

/* đổi toàn bộ màu xanh của Multiselect */

:root {
    --ms-option-bg-selected: #f7dcdc;
    --ms-option-bg-selected-pointed: #f2c4c4;
    --ms-option-bg-pointed: #f7dcdc;

    --ms-option-color-selected: #7D161A;
}
</style>

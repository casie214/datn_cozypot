<template>
    <div class="client-form-page bg-light min-vh-100 p-4">
        <div class="container mb-4">
            <div class="d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center gap-3">
                    <div class="icon-header-box bg-wine text-white p-3 rounded-circle">
                        <i class="fas fa-user-circle fa-lg"></i>
                    </div>
                    <div>
                        <h4 class="title-page text-wine fw-bold m-0">
                            Thông tin hồ sơ cá nhân
                        </h4>
                        <p class="text-muted small m-0">Quản lý thông tin tài khoản cá nhân của bạn</p>
                    </div>
                </div>
            </div>
        </div>

        <div class="container">
            <div class="row g-4">
                <div class="col-lg-4">
                    <div class="card shadow-sm border-0 rounded-4 sticky-top" style="top: 20px;">
                        <div class="card-body p-4 text-center">
                            <div class="upload-container mb-4">
                                <div class="upload-zone mx-auto" @click="triggerFileInput"
                                    :class="{ 'has-image': previewUrl }">
                                    <template v-if="previewUrl">
                                        <img :src="previewUrl" alt="Avatar" class="img-preview rounded-circle"
                                            style=" object-fit: cover;" />
                                        <div class="upload-overlay">
                                            <i class="fas fa-camera mb-2"></i>
                                            <span>Đổi ảnh</span>
                                        </div>
                                    </template>
                                    <template v-else>
                                        <div class="upload-placeholder">
                                            <div class="icon-circle">
                                                <i class="fas fa-user fa-3x text-light-wine"></i>
                                            </div>
                                            <p class="mt-3 mb-1 fw-bold">Tải ảnh đại diện</p>
                                        </div>
                                    </template>
                                </div>
                                <input type="file" ref="fileInput" class="d-none" @change="onFileChange"
                                    accept="image/*" />
                            </div>

                            <h5 class="fw-bold mb-1">{{ formData.tenKhachHang || 'Chưa cập nhật tên' }}</h5>


                            <hr class="dashed">

                            <div class="client-stats mb-4 text-start">
                                <div class="d-flex justify-content-between mb-2">
                                    <span class="text-muted small">Hạng thành viên:</span>
                                    <span class="fw-bold text-wine">Thành viên Bạc</span>
                                </div>
                                <div class="d-flex justify-content-between mb-2">
                                    <span class="text-muted small">Điểm tích lũy:</span>
                                    <span class="badge bg-gold text-dark fw-bold">{{ formData.diemTichLuy || 0 }}
                                        điểm</span>
                                </div>
                            </div>

                            <div class="info-alert p-3 rounded-3 bg-light-wine text-start">
                                <p class="small text-muted mb-0">
                                    <i class="fas fa-shield-alt text-wine me-2"></i>
                                    Thông tin cá nhân của bạn được bảo mật theo chính sách của chúng tôi.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-8">
                    <div class="card shadow-sm border-0 rounded-4 overflow-hidden mb-4">
                        <div class="card-body p-4 p-md-5">
                            <form @submit.prevent>
                                <div class="form-section mb-5">
                                    <div class="d-flex align-items-center mb-4">
                                        <span class="badge-number">1</span>
                                        <h5 class="m-0 fw-bold">Thông tin định danh</h5>
                                    </div>

                                    <div class="row g-4">
                                        <div class="col-md-6">
                                            <label class="form-label-custom">Tên đăng nhập <span
                                                    class="star">*</span></label>
                                            <div class="input-group-custom">
                                                <i class="fas fa-at icon-input"></i>
                                                <input type="text" class="form-control"
                                                    :class="{ 'is-invalid': errors.tenDangNhap }"
                                                    v-model="formData.tenDangNhap" placeholder="username">
                                            </div>
                                            <div class="error-text">{{ errors.tenDangNhap }}</div>
                                        </div>

                                        <div class="col-md-6">
                                            <label class="form-label-custom">Họ và tên <span
                                                    class="star">*</span></label>
                                            <div class="input-group-custom">
                                                <i class="far fa-user icon-input"></i>
                                                <input type="text" class="form-control"
                                                    :class="{ 'is-invalid': errors.tenKhachHang }"
                                                    v-model="formData.tenKhachHang" placeholder="Nguyễn Văn A">
                                            </div>
                                            <div class="error-text">{{ errors.tenKhachHang }}</div>
                                        </div>

                                        <div class="col-md-6">
                                            <label class="form-label-custom">Số điện thoại <span
                                                    class="star">*</span></label>
                                            <div class="input-group-custom">
                                                <i class="fas fa-phone-alt icon-input"></i>
                                                <input type="text" class="form-control"
                                                    :class="{ 'is-invalid': errors.soDienThoai }"
                                                    v-model="formData.soDienThoai" placeholder="09xxxxxxxx">
                                            </div>
                                            <div class="error-text">{{ errors.soDienThoai }}</div>
                                        </div>

                                        <div class="col-md-6">
                                            <label class="form-label-custom">Giới tính <span
                                                    class="star">*</span></label>
                                            <div class="d-flex gap-4 align-items-center py-2">
                                                <div class="form-check">
                                                    <input class="form-check-input" type="radio" name="gender" id="male"
                                                        :value="true" v-model="formData.gioiTinh">
                                                    <label class="form-check-label" for="male">Nam</label>
                                                </div>
                                                <div class="form-check">
                                                    <input class="form-check-input" type="radio" name="gender"
                                                        id="female" :value="false" v-model="formData.gioiTinh">
                                                    <label class="form-check-label" for="female">Nữ</label>
                                                </div>
                                            </div>
                                        </div>

                                        <div class="col-md-6">
                                            <label class="form-label-custom">Ngày sinh <span
                                                    class="star">*</span></label>
                                            <div class="input-group-custom">
                                                <i class="far fa-calendar-alt icon-input"></i>
                                                <input type="date" class="form-control"
                                                    :class="{ 'is-invalid': errors.ngaySinh }"
                                                    v-model="formData.ngaySinh" :max="todayDate"
                                                    @change="validateNgaySinh" />
                                            </div>
                                            <div class="error-text">{{ errors.ngaySinh }}</div>
                                        </div>

                                        <div class="col-md-6">
                                            <label class="form-label-custom">Email <span class="star">*</span></label>
                                            <div class="input-group-custom">
                                                <i class="far fa-envelope icon-input"></i>
                                                <input type="email" class="form-control"
                                                    :class="{ 'is-invalid': errors.email }" v-model="formData.email"
                                                    placeholder="example@gmail.com">
                                            </div>
                                            <div class="error-text">{{ errors.email }}</div>
                                        </div>
                                    </div>
                                </div>

                                <div class="card border-0 shadow-sm rounded-4 overflow-hidden mb-4"
                                    style="background-color: #fdfafb;">
                                    <div
                                        class="card-header bg-transparent border-0 pt-4 px-4 d-flex justify-content-between">
                                        <h6 class="fw-bold m-0 text-wine">
                                            <i class="fas fa-map-marker-alt me-2"></i>Địa chỉ
                                        </h6>
                                        <button @click="addAddress" type="button"
                                            class="btn btn-sm btn-outline-wine rounded-pill">
                                            <i class="fas fa-plus"></i> Thêm mới
                                        </button>
                                    </div>

                                    <div class="card-body p-4">
                                        <div class="address-list">
                                            <div v-for="(item, index) in formData.danhSachDiaChi"
                                                :key="item.id ?? index"
                                                class="address-card border rounded-4 p-4 mb-3 position-relative"
                                                :style="index === defaultIndex ? 'background-color: #fdf2f2; border: 1.5px solid #800000 !important;' : 'background-color: #fff; border: 1px solid #eee;'">

                                                <span v-if="index === defaultIndex"
                                                    class="badge position-absolute top-0 end-0 m-3 bg-wine px-3 py-2">
                                                    Mặc định
                                                </span>

                                                <div class="row g-3">
                                                    <div class="col-md-4">
                                                        <label class="small fw-bold text-secondary">Tỉnh/Thành phố
                                                            *</label>
                                                        <Multiselect v-model="item.id_tinh_thanh"
                                                            :options="listTinhThanh" placeholder="Chọn Tỉnh"
                                                            :searchable="true" :canClear="false"
                                                            class="custom-filter-multiselect"
                                                            :class="{ 'is-invalid-multi': item.errors?.id_tinh_thanh }"
                                                            @change="onProvinceChange($event, index)" />
                                                    </div>

                                                    <div class="col-md-4">
                                                        <label class="small fw-bold text-secondary">Quận/Huyện *</label>
                                                        <Multiselect v-model="item.id_quan_huyen"
                                                            :options="item.listHuyen" placeholder="Chọn Huyện"
                                                            :searchable="true" :canClear="false"
                                                            :disabled="!item.id_tinh_thanh"
                                                            class="custom-filter-multiselect"
                                                            :class="{ 'is-invalid-multi': item.errors?.id_quan_huyen }"
                                                            @change="onDistrictChange($event, index)" />
                                                    </div>

                                                    <div class="col-md-4">
                                                        <label class="small fw-bold text-secondary">Phường/Xã *</label>
                                                        <Multiselect v-model="item.id_phuong_xa" :options="item.listXa"
                                                            placeholder="Chọn Xã" :searchable="true" :canClear="false"
                                                            :disabled="!item.id_quan_huyen"
                                                            class="custom-filter-multiselect"
                                                            :class="{ 'is-invalid-multi': item.errors?.id_phuong_xa }"
                                                            @change="onWardChange($event, index)" />
                                                    </div>

                                                    <div class="col-12">
                                                        <label class="small fw-bold text-secondary">Địa chỉ cụ thể
                                                            *</label>
                                                        <textarea v-model="item.dia_chi_chi_tiet"
                                                            class="form-control shadow-none"
                                                            :class="{ 'is-invalid': item.errors?.dia_chi_chi_tiet }"
                                                            rows="2" placeholder="Số nhà, tên đường..."></textarea>
                                                    </div>
                                                </div>

                                                <div
                                                    class="d-flex justify-content-between align-items-center mt-3 pt-3 border-top">
                                                    <div class="form-check form-switch d-flex align-items-center gap-2">
                                                        <input class="form-check-input switch-wine shadow-none"
                                                            type="checkbox" :checked="index === defaultIndex"
                                                            @change="setDefault(index)">
                                                        <label class="small text-muted mb-0">Đặt làm địa chỉ
                                                            chính</label>
                                                    </div>
                                                    <button @click="removeAddress(index)"
                                                        class="btn btn-link text-danger p-0 text-decoration-none small">
                                                        <i class="fas fa-trash-alt me-1"></i> Xóa
                                                    </button>
                                                </div>
                                            </div>

                                            <div v-if="!formData.danhSachDiaChi || formData.danhSachDiaChi.length === 0"
                                                class="text-center py-4">
                                                <p class="text-muted small">Bạn cần thêm ít nhất một địa chỉ để nhận
                                                    hàng.</p>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="d-flex justify-content-between align-items-center pt-4 border-top">

                                    <div class="d-flex gap-3">
                                        <button type="button" class="btn btn-light rounded-pill px-4"
                                            @click="handleBack">Hủy</button>
                                        <button type="button"
                                            class="btn btn-main-custom px-5 py-2 rounded-pill shadow-sm"
                                            @click="handleSave" :disabled="loading">
                                            <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                                            Lưu thay đổi
                                        </button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { reactive, ref, onMounted, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import clientService from '@/services/clientService';
import dayjs from 'dayjs';
import { useToast } from "vue-toastification";
import Swal from 'sweetalert2';
import axios from 'axios';
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';
import select2 from 'select2';
const route = useRoute();
const router = useRouter();
const toast = useToast();
const userSession = JSON.parse(localStorage.getItem('user') || '{}');
const clientId = ref(userSession.id || null);
const loading = ref(false);
const fileInput = ref(null);
const selectedFile = ref(null);
const previewUrl = ref(null);
const currentAddress = ref('');
const selectedIndex = ref(0); // Mặc định chọn ô đầu tiên
const editingIndex = ref(null);
const todayDate = dayjs().subtract(16, "year").format("YYYY-MM-DD");

const listTinhThanh = ref([]);
// 1. Dữ liệu chính để gửi lên Backend
const formData = reactive({
    tenKhachHang: '',
    soDienThoai: '',
    email: '',
    tenDangNhap: '',
    matKhauDangNhap: '',
    ngaySinh: '',
    gioiTinh: true,
    trangThai: 1,
    danhSachDiaChi: [] // Nên bắt đầu bằng mảng rỗng để dễ quản lý
});

// Hàm khởi tạo Select2 cho từng dòng cụ thể
const initSelect2ForIndex = (index) => {
    const $ = window.$;

    if (typeof $.fn.select2 !== 'function') {
        if (typeof select2 === 'function') select2();
    }

    setTimeout(() => {

        /* =======================
           1. TỈNH / THÀNH
        ========================*/
        const $tinh = $(`.select-tinh[data-index="${index}"]`);

        if ($tinh.length) {

            $tinh.select2({
                placeholder: "Chọn Tỉnh/Thành",
                width: '100%'
            })
                .val(formData.danhSachDiaChi[index].idTinhThanh)
                .trigger('change.select2');

            // lấy text ban đầu
            const dataTinh = $tinh.select2('data');
            if (dataTinh.length) {
                formData.danhSachDiaChi[index].tenTinhThanh = dataTinh[0].text;
            }

            $tinh.off('change').on('change', async function () {
                const val = $(this).val();
                const data = $(this).select2('data');
                const text = data.length ? data[0].text : '';

                // 🚨 ÉP GÁN GIÁ TRỊ VÀO VUE REACTIVE STATE
                formData.danhSachDiaChi[index].id_tinh_thanh = val;
                formData.danhSachDiaChi[index].tenTinhThanh = text;

                // reset huyện xã
                formData.danhSachDiaChi[index].id_quan_huyen = '';
                formData.danhSachDiaChi[index].tenQuanHuyen = '';
                formData.danhSachDiaChi[index].id_phuong_xa = '';
                formData.danhSachDiaChi[index].tenPhuongXa = '';

                // Cập nhật giao diện lỗi (xóa báo đỏ nếu có)
                if (formData.danhSachDiaChi[index].errors) {
                    formData.danhSachDiaChi[index].errors.id_tinh_thanh = '';
                }

                // Gọi hàm load Huyện
                await onProvinceChange(val, index);

                // Khởi tạo lại Select2 cho Huyện sau khi có option mới
                nextTick(() => {
                    $(`.select-huyen[data-index="${index}"]`).select2({
                        placeholder: "Chọn Quận/Huyện",
                        width: '100%'
                    });
                });
            });
        }


        /* =======================
           2. QUẬN / HUYỆN
        ========================*/
        const $huyen = $(`.select-huyen[data-index="${index}"]`);

        if ($huyen.length) {

            $huyen.select2({
                placeholder: "Chọn Quận/Huyện",
                width: '100%'
            })
                .val(formData.danhSachDiaChi[index].idQuanHuyen)
                .trigger('change.select2');

            const dataHuyen = $huyen.select2('data');
            if (dataHuyen.length) {
                formData.danhSachDiaChi[index].tenQuanHuyen = dataHuyen[0].text;
            }

            $huyen.off('change').on('change', async function () {

                const val = $(this).val();
                const data = $(this).select2('data');
                const text = data.length ? data[0].text : '';

                formData.danhSachDiaChi[index].idQuanHuyen = val;
                formData.danhSachDiaChi[index].tenQuanHuyen = text;

                // reset xã
                formData.danhSachDiaChi[index].idPhuongXa = '';
                formData.danhSachDiaChi[index].tenPhuongXa = '';

                await onDistrictChange(val, index);

                nextTick(() => {
                    $(`.select-xa[data-index="${index}"]`).select2({
                        placeholder: "Chọn Phường/Xã",
                        width: '100%'
                    });
                });

            });
        }


        /* =======================
           3. PHƯỜNG / XÃ
        ========================*/
        const $xa = $(`.select-xa[data-index="${index}"]`);

        if ($xa.length) {

            $xa.select2({
                placeholder: "Chọn Phường/Xã",
                width: '100%'
            })
                .val(formData.danhSachDiaChi[index].idPhuongXa)
                .trigger('change.select2');

            const dataXa = $xa.select2('data');
            if (dataXa.length) {
                formData.danhSachDiaChi[index].tenPhuongXa = dataXa[0].text;
            }

            $xa.off('change').on('change', function () {

                const val = $(this).val();
                const data = $(this).select2('data');
                const text = data.length ? data[0].text : '';

                formData.danhSachDiaChi[index].idPhuongXa = val;
                formData.danhSachDiaChi[index].tenPhuongXa = text;

            });
        }

    }, 300);
};

// Chỉ chạy lại khi dữ liệu options (Huyện/Xã) thực sự thay đổi từ API
watch(
    () => formData.danhSachDiaChi.map(item => ({ h: item.listHuyen, x: item.listXa })),
    () => {
        nextTick(() => {
            if (typeof $.fn.select2 === 'function') {
                formData.danhSachDiaChi.forEach((_, index) => {
                    // Chỉ re-init nếu nó đã là select2 rồi để tránh lỗi khởi tạo đè
                    const $huyen = $(`.select-huyen[data-index="${index}"]`);
                    const $xa = $(`.select-xa[data-index="${index}"]`);

                    if ($huyen.hasClass("select2-hidden-accessible")) {
                        $huyen.select2({ width: '100%' });
                    }
                    if ($xa.hasClass("select2-hidden-accessible")) {
                        $xa.select2({ width: '100%' });
                    }
                });
            }
        });
    },
    { deep: true }
);

// Sửa lại hàm addAddress của bạn một chút để kích hoạt Select2 cho card mới
const addAddress = () => {
    formData.danhSachDiaChi.push({
        id_dia_chi: null,
        ho_ten_nhan: formData.tenKhachHang || '',
        so_dien_thoai_nhan: formData.soDienThoai || '',
        id_tinh_thanh: '',
        id_quan_huyen: '',
        id_phuong_xa: '',
        tenTinhThanh: '',
        tenQuanHuyen: '',
        tenPhuongXa: '',
        dia_chi_chi_tiet: '',
        la_mac_dinh: formData.danhSachDiaChi.length === 0,
        listHuyen: [],
        listXa: [],
        errors: {}
    });
};

// Hàm lấy danh sách Tỉnh/Thành
const loadTinhThanh = async () => {
    try {
        const response = await axios.get('https://provinces.open-api.vn/api/p/');
        listTinhThanh.value = response.data.map(item => ({
            value: String(item.code), // Đổi id thành value cho Multiselect
            label: item.name          // Đổi text thành label cho Multiselect
        }));
    } catch (error) {
        console.error("Không thể lấy danh sách tỉnh:", error);
    }
};


const onProvinceChange = async (provinceId, index, isInitial = false) => {
    const item = formData.danhSachDiaChi[index];

    if (!isInitial) {
        item.id_quan_huyen = '';
        item.id_phuong_xa = '';
        item.listHuyen = [];
        item.listXa = [];

        // Lưu tên Tỉnh để gửi lên Backend
        const tinhObj = listTinhThanh.value.find(t => t.value === provinceId);
        if (tinhObj) item.tenTinhThanh = tinhObj.label;

        if (item.errors) item.errors.id_tinh_thanh = ''; // Xóa lỗi
    }

    if (provinceId) {
        try {
            const res = await axios.get(`https://provinces.open-api.vn/api/p/${provinceId}?depth=2`);
            item.listHuyen = res.data.districts.map(d => ({ value: String(d.code), label: d.name }));
        } catch (e) { console.error("Lỗi load huyện:", e); }
    }
};

const onDistrictChange = async (districtId, index, isInitial = false) => {
    const item = formData.danhSachDiaChi[index];

    if (!isInitial) {
        item.id_phuong_xa = '';
        item.listXa = [];

        // Lưu tên Huyện
        const huyenObj = item.listHuyen.find(h => h.value === districtId);
        if (huyenObj) item.tenQuanHuyen = huyenObj.label;

        if (item.errors) item.errors.id_quan_huyen = '';
    }

    if (districtId) {
        try {
            const res = await axios.get(`https://provinces.open-api.vn/api/d/${districtId}?depth=2`);
            item.listXa = res.data.wards.map(w => ({ value: String(w.code), label: w.name }));
        } catch (e) { console.error("Lỗi load xã:", e); }
    }
};

const onWardChange = (wardId, index) => {
    const item = formData.danhSachDiaChi[index];
    const xaObj = item.listXa.find(x => x.value === wardId);
    if (xaObj) item.tenPhuongXa = xaObj.label;

    if (item.errors) item.errors.id_phuong_xa = '';
};


// Gọi hàm này ngay khi component được khởi tạo

const errors = reactive({
    tenKhachHang: '', soDienThoai: '', email: '',
    tenDangNhap: '', matKhauDangNhap: '', ngaySinh: '', diaChi: ''
});
// Cấu hình Swal chung giống nhân viên
const swalConfig = {
    confirmButtonColor: '#800000',
    cancelButtonColor: '#6c757d',
};
// Theo dõi thay đổi để xóa báo đỏ
watch(() => ({ ...formData }), (val) => {
    Object.keys(val).forEach(key => {
        if (val[key] && errors[key]) errors[key] = '';
    });

}, { deep: true });
const triggerFileInput = () => fileInput.value.click();
const onFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
        selectedFile.value = file;
        previewUrl.value = URL.createObjectURL(file);
    }
};
const tempAddressText = ref('');
const defaultIndex = ref(0);   // Lưu vị trí mặc định (mặc định là cái đầu tiên)
const preparePayload = () => {
    const data = new FormData();
    if (clientId.value) {
        data.append('id', clientId.value);
    }
    // 1. Xử lý thông tin chung của khách hàng
    Object.keys(formData).forEach(key => {
        if (key === 'danhSachDiaChi' || key === 'diaChi') return;

        let val = formData[key];

        // Ép kiểu boolean thành 1/0 nếu Backend của bạn yêu cầu
        if (key === 'gioiTinh') {
            val = val ? 1 : 0;
        }

        if (val !== null && val !== undefined && val !== '') {
            data.append(key, val);
        }
    });

    // 2. Xử lý Ảnh đại diện (File)
    if (selectedFile.value) {
        data.append('hinhAnhFile', selectedFile.value);
    } else if (clientId.value && formData.anhDaiDien) {
        data.append('anhDaiDien', formData.anhDaiDien);
    }

    // 3. Xử lý Mảng Danh sách địa chỉ
    if (formData.danhSachDiaChi && formData.danhSachDiaChi.length > 0) {

        // Nếu chỉ có 1 địa chỉ, ép buộc nó là mặc định
        if (formData.danhSachDiaChi.length === 1) {
            defaultIndex.value = 0;
        }

        formData.danhSachDiaChi.forEach((addr, index) => {
            // Thông tin người nhận (Lấy từ địa chỉ, nếu rỗng thì mượn từ thông tin khách)
            data.append(`danhSachDiaChi[${index}].hoTenNhan`, addr.ho_ten_nhan || formData.tenKhachHang || '');
            data.append(`danhSachDiaChi[${index}].soDienThoaiNhan`, addr.so_dien_thoai_nhan || formData.soDienThoai || '');

            // Các ID Tỉnh/Huyện/Xã
            data.append(`danhSachDiaChi[${index}].idTinhThanh`, addr.id_tinh_thanh || '');
            data.append(`danhSachDiaChi[${index}].idQuanHuyen`, addr.id_quan_huyen || '');
            data.append(`danhSachDiaChi[${index}].idPhuongXa`, addr.id_phuong_xa || '');

            // 🚨 Text Tỉnh/Huyện/Xã: Lấy trực tiếp từ Object đã được lưu ở hàm onChange (KHÔNG DÙNG querySelector nữa)
            data.append(`danhSachDiaChi[${index}].tenTinhThanh`, addr.tenTinhThanh || '');
            data.append(`danhSachDiaChi[${index}].tenQuanHuyen`, addr.tenQuanHuyen || '');
            data.append(`danhSachDiaChi[${index}].tenPhuongXa`, addr.tenPhuongXa || '');

            // Địa chỉ cụ thể & Trạng thái mặc định
            data.append(`danhSachDiaChi[${index}].diaChiChiTiet`, addr.dia_chi_chi_tiet || '');
            data.append(`danhSachDiaChi[${index}].laMacDinh`, index === defaultIndex.value);
        });
    }

    tempAddressText.value = '';

    // Log ra để bạn dễ debug xem payload đã ghép chuẩn chưa
    console.log("Dữ liệu địa chỉ chuẩn bị gửi:", JSON.stringify(formData.danhSachDiaChi, null, 2));

    return data;
};
// Bắt đầu sửa
const startEdit = (index) => {
    editingIndex.value = index;
    tempAddressText.value =
        formData.danhSachDiaChi[index].thong_tin_dia_chi;
};
// Hủy sửa
const cancelEdit = () => {
    editingIndex.value = null;
    tempAddressText.value = '';
};
// Chọn địa chỉ mặc định
const setDefault = (index) => {
    // 1. Cập nhật vị trí index mặc định để đổi màu ngôi sao trên giao diện
    defaultIndex.value = index;
    // 2. Quan trọng: Cập nhật biến la_mac_dinh trong mảng để preparePayload gửi đi đúng
    formData.danhSachDiaChi.forEach((addr, i) => {
        addr.la_mac_dinh = (i === index);
    });
    // (Tùy chọn) Hiện thông báo nhỏ cho người dùng biết
    // toast.info("Đã đổi địa chỉ mặc định");
};

// Xóa địa chỉ
const removeAddress = (index) => {
    Swal.fire({
        title: 'Bạn chắc chắn chứ?',
        text: "Địa chỉ này sẽ bị xóa khỏi danh sách tạm!",
        icon: 'warning',
        iconColor: '#7D161A',
        showCancelButton: true,
        // Phối màu lại ở đây
        confirmButtonColor: '#eb445a', // Màu đỏ mận (soft red)
        cancelButtonColor: '#6c757d',  // Màu xám thanh lịch (muted gray)
        confirmButtonText: '<i class="fas fa-trash-alt"></i> Đồng ý xóa',
        cancelButtonText: 'Hủy',
        reverseButtons: true, // Đưa nút Hủy sang bên trái, Đồng ý sang bên phải cho thuận tay
        customClass: {
            confirmButton: 'btn btn-danger mx-2', // Bạn có thể dùng class Bootstrap nếu muốn
            cancelButton: 'btn btn-secondary mx-2'
        },
        buttonsStyling: true // Nếu dùng class Bootstrap thì để false, dùng màu manual thì để true
    }).then((result) => {
        if (result.isConfirmed) {
            // Logic xóa (giữ nguyên)
            if (index === defaultIndex.value) {
                defaultIndex.value = 0;
            } else if (index < defaultIndex.value) {
                defaultIndex.value--;
            }

            formData.danhSachDiaChi.splice(index, 1);

            // Thông báo thành công nhỏ gọn (Toast style)
            const Toast = Swal.mixin({
                toast: true,
                position: 'top-end',
                showConfirmButton: false,
                timer: 2000,
                timerProgressBar: true
            });

            Toast.fire({
                icon: 'success', iconColor: '#7D161A',
                title: 'Đã xóa địa chỉ thành công'
            });

            if (formData.danhSachDiaChi.length === 0) {
                defaultIndex.value = -1;
            }
        }
    });
};
const handleBack = () => {
    if (Object.values(formData).some(x => x !== '' && x !== true)) {
        Swal.fire({
            ...swalConfig,
            title: 'Xác nhận thoát?',
            text: "Các thay đổi chưa lưu sẽ bị mất!",
            icon: 'warning',
            iconColor: '#7D161A',
            showCancelButton: true,
            confirmButtonText: 'Đồng ý',
            cancelButtonText: 'Ở lại'
        }).then((result) => {
            if (result.isConfirmed) location.reload();
        });
    } else {
        location.reload();
    }
};

const validateForm = async () => {
    let ok = true;
    const today = dayjs();

    Object.keys(errors).forEach(k => errors[k] = '');

    const requiredFields = [
        { key: 'tenDangNhap', msg: 'Vui lòng nhập tên đăng nhập' },
        { key: 'tenKhachHang', msg: 'Vui lòng nhập họ và tên khách hàng' },
        { key: 'soDienThoai', msg: 'Vui lòng nhập số điện thoại liên lạc' },
        { key: 'email', msg: 'Vui lòng nhập địa chỉ email' },
        { key: 'ngaySinh', msg: 'Vui lòng chọn ngày tháng năm sinh' },
    ];

    requiredFields.forEach(field => {
        if (!formData[field.key] || formData[field.key].toString().trim() === '') {
            errors[field.key] = field.msg;
            ok = false;
        }
    });
    if (formData.tenDangNhap) {
        const len = formData.tenDangNhap.length;
        if (len < 5 || len > 50) {
            errors.tenDangNhap = 'Tên đăng nhập phải từ 5 đến 50 ký tự';
            ok = false;
        }
    }

    // --- VALIDATE DANH SÁCH ĐỊA CHỈ ---
    if (!formData.danhSachDiaChi || formData.danhSachDiaChi.length === 0) {
        errors.diaChi = 'Vui lòng thêm ít nhất một địa chỉ';
        ok = false;
    } else {
        formData.danhSachDiaChi.forEach((item, index) => {
            if (!item.errors) {
                item.errors = { id_tinh_thanh: '', id_quan_huyen: '', id_phuong_xa: '', dia_chi_chi_tiet: '' };
            }
            item.errors.id_tinh_thanh = !item.id_tinh_thanh ? 'Vui lòng chọn Tỉnh/Thành' : '';
            item.errors.id_quan_huyen = !item.id_quan_huyen ? 'Vui lòng chọn Quận/Huyện' : '';
            item.errors.id_phuong_xa = !item.id_phuong_xa ? 'Vui lòng chọn Phường/Xã' : '';
            item.errors.dia_chi_chi_tiet = (!item.dia_chi_chi_tiet || item.dia_chi_chi_tiet.trim() === '')
                ? 'Vui lòng nhập địa chỉ chi tiết' : '';

            if (item.errors.id_tinh_thanh || item.errors.id_quan_huyen ||
                item.errors.id_phuong_xa || item.errors.dia_chi_chi_tiet) {
                ok = false;
            }
        });
    }

    // 3. Kiểm tra định dạng (Format)
    if (formData.soDienThoai && !/^(0[3|5|7|8|9])([0-9]{8})$/.test(formData.soDienThoai)) {
        errors.soDienThoai = 'Số điện thoại không hợp lệ';
        ok = false;
    }

    if (formData.email && !/^\S+@\S+\.\S+$/.test(formData.email)) {
        errors.email = 'Địa chỉ email không đúng định dạng';
        ok = false;
    }

    if (formData.ngaySinh && dayjs(formData.ngaySinh).isAfter(today)) {
        errors.ngaySinh = 'Ngày sinh không thể lớn hơn ngày hiện tại';
        ok = false;
    }

    if (!ok) return false;

    // 4. KIỂM TRA TRÙNG LẶP QUA API (Bỏ điều kiện !clientId cho tenDangNhap)
    try {
        const checks = [
            { key: 'tenDangNhap', label: 'Tên đăng nhập' },
            { key: 'soDienThoai', label: 'Số điện thoại' },
            { key: 'email', label: 'Email' }
        ];

        for (const item of checks) {
            const val = formData[item.key];
            if (val) {
                // Truyền clientId.value vào để backend loại trừ chính nó khi check trùng (Update)
                const res = await clientService.checkDuplicate(item.key, val, clientId.value);
                if (res.data.exists) {
                    errors[item.key] = `${item.label} này đã tồn tại`;
                    ok = false;
                }
            }
        }
    } catch (e) {
        console.error("Lỗi validate trùng lặp:", e);
    }

    return ok;
};

const generateRandomPassword = (length = 8) => {
    const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    let retVal = "";
    for (let i = 0, n = charset.length; i < length; ++i) {
        retVal += charset.charAt(Math.floor(Math.random() * n));
    }
    return retVal;
};

const submitClient = async (payload) => {
    // Đối với hồ sơ cá nhân, chúng ta luôn xác định đây là hành động CẬP NHẬT
    // Backend sẽ tự biết bạn là ai qua Token trong axiosClient
    return await clientService.updateMyProfile(payload);
};

const handleSave = async () => {
    // --- BƯỚC 1: GÁN GIÁ TRỊ MẶC ĐỊNH TRƯỚC ---
    // Phải gán TRƯỚC khi validate để hàm validate kiểm tra được dữ liệu mới nhất
    if (!clientId.value) {
        // Nếu người dùng không nhập tên đăng nhập, lấy số điện thoại làm tên đăng nhập
        if (!formData.tenDangNhap) {
            formData.tenDangNhap = formData.soDienThoai;
        }
        if (!formData.matKhauDangNhap) {
            formData.matKhauDangNhap = generateRandomPassword(10);
        }
    }

    // --- BƯỚC 2: KIỂM TRA TÍNH HỢP LỆ (FE) ---
    const isValid = await validateForm();
    if (!isValid) {
        Swal.fire({
            ...swalConfig,
            title: 'Thông tin chưa hợp lệ',
            text: 'Vui lòng kiểm tra lại các trường bị báo đỏ!',
            icon: 'warning',
            iconColor: '#7D161A',
            confirmButtonText: 'Đã hiểu'
        });
        return;
    }

    // --- BƯỚC 3: XÁC NHẬN ---
    const result = await Swal.fire({
        ...swalConfig,
        title: clientId.value ? 'Cập nhật khách hàng?' : 'Xác nhận thêm mới?',
        text: clientId.value
            ? 'Bạn có chắc chắn muốn lưu các thay đổi này?'
            : 'Hệ thống sẽ tạo tài khoản khách hàng mới vào cơ sở dữ liệu.',
        icon: 'question',
        iconColor: '#7D161A',
        showCancelButton: true,
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy bỏ'
    });

    if (!result.isConfirmed) return;

    try {
    loading.value = true;
    const payload = preparePayload();

    // Kiểm tra: Nếu có clientId thì gọi UPDATE, ngược lại gọi CREATE
    if (clientId.value) {
        // Giả sử hàm update của bạn nhận vào (id, data) hoặc chỉ data tùy Service
        await clientService.update(clientId.value, payload); 
    } else {
        await clientService.create(payload);
    }

    await Swal.fire({
        ...swalConfig,
        title: 'Thành công!',
        text: clientId.value ? 'Cập nhật thành công.' : 'Thêm mới thành công.',
        icon: 'success',
        timer: 1500,
        iconColor: '#7D161A',
        showConfirmButton: false
    });
    
    // Tùy chọn: Thay vì reload, bạn có thể chỉ cập nhật lại state nếu cần
    location.reload(); 

    } catch (e) {
        // --- BƯỚC 5: XỬ LÝ LỖI VALIDATION TỪ SERVER (NẾU CÓ) ---
        const errorData = e.response?.data;
        if (errorData?.code === "VALIDATION_ERORR" && errorData.errors) {
            // Đổ lỗi từ Server vào object errors để hiện báo đỏ ngay lập tức
            Object.assign(errors, errorData.errors);
        } else {
            Swal.fire({
                ...swalConfig,
                title: 'Thao tác thất bại',
                text: errorData?.message || "Đã có lỗi xảy ra.",
                icon: 'error'
            });
        }
    } finally {
        loading.value = false;
    }
};
onMounted(async () => {
    // 1. Load danh mục Tỉnh/Thành trước
    await loadTinhThanh();

    // 2. Xác định clientId từ Session/LocalStorage (Thay vì route.params)
    const userSession = JSON.parse(localStorage.getItem('user') || '{}');
    clientId.value = userSession.id || null;

    if (clientId.value) {
        try {
            loading.value = true;

            // Gọi API lấy chi tiết hồ sơ cá nhân
            const res = await clientService.getDetail(clientId.value);
            const data = res.data;

            // 3. Map thông tin cơ bản
            const { danhSachDiaChi, ...rest } = data;
            Object.assign(formData, rest);

            // Xử lý ảnh đại diện
            if (formData.anhDaiDien) {
                // Thay đổi domain theo môi trường của bạn
                previewUrl.value = `http://localhost:8080/uploads/images/${formData.anhDaiDien}`;
            }

            // 4. Xử lý danh sách địa chỉ
            if (danhSachDiaChi && danhSachDiaChi.length > 0) {
                const mappedAddresses = [];

                // Sử dụng for...of để có thể dùng await bên trong
                for (const d of danhSachDiaChi) {
                    const addr = {
                        id: d.id, // Giữ ID để Backend biết là update địa chỉ cũ
                        ho_ten_nhan: d.hoTenNhan || '',
                        so_dien_thoai_nhan: d.soDienThoaiNhan || '',

                        id_tinh_thanh: d.idTinhThanh ? String(d.idTinhThanh) : '',
                        id_quan_huyen: d.idQuanHuyen ? String(d.idQuanHuyen) : '',
                        id_phuong_xa: d.idPhuongXa ? String(d.idPhuongXa) : '',

                        tenTinhThanh: d.tenTinhThanh || '',
                        tenQuanHuyen: d.tenQuanHuyen || '',
                        tenPhuongXa: d.tenPhuongXa || '',

                        dia_chi_chi_tiet: d.diaChiChiTiet || '',
                        la_mac_dinh: d.laMacDinh === true,

                        listHuyen: [],
                        listXa: [],
                        errors: {} // Khởi tạo object lỗi cho từng dòng
                    };

                    // Nạp dữ liệu Quận/Huyện/Xã để Select2 hiển thị được Text ban đầu
                    try {
                        if (addr.id_tinh_thanh) {
                            const resH = await axios.get(`https://provinces.open-api.vn/api/p/${addr.id_tinh_thanh}?depth=2`);
                            addr.listHuyen = resH.data.districts.map(i => ({ value: String(i.code), label: i.name }));
                        }
                        if (addr.id_quan_huyen) {
                            const resX = await axios.get(`https://provinces.open-api.vn/api/d/${addr.id_quan_huyen}?depth=2`);
                            addr.listXa = resX.data.wards.map(i => ({ value: String(i.code), label: i.name }));
                        }
                    } catch (err) {
                        console.warn("Lỗi nạp dữ liệu địa chỉ mồi:", err);
                    }

                    mappedAddresses.push(addr);
                }

                formData.danhSachDiaChi = mappedAddresses;

                // Cập nhật vị trí địa chỉ mặc định
                const idx = formData.danhSachDiaChi.findIndex(d => d.la_mac_dinh === true);
                defaultIndex.value = idx !== -1 ? idx : 0;

                // 5. Khởi tạo lại Select2 sau khi DOM đã render dữ liệu mồi
                nextTick(() => {
                    setTimeout(() => {
                        formData.danhSachDiaChi.forEach((_, index) => {
                            initSelect2ForIndex(index);
                        });
                    }, 300);
                });
            }
        } catch (e) {
            console.error("Lỗi khi tải hồ sơ cá nhân:", e);
            toast.error("Không thể tải thông tin hồ sơ.");
        } finally {
            loading.value = false;
        }
    } else {
        // Nếu không có clientId, chuyển hướng về trang login
        router.push('/login');
    }
});



</script>

<style scoped>
/* Màu chủ đạo */
:root {
    --primary-red: #7B121C;
    --dark-red: #800000;
    --bg-white: #ffffff;
    --text-main: #2c3e50;
    --text-muted: #666;
    --border-color: #eee;
    --shadow-sm: 0 3px 6px rgba(131, 131, 131, 0.2);
    --shadow-md: 0 6px 18px rgba(0, 0, 0, 0.08);
    --font-main: 14px !important;

}

.text-wine {
    color: #800000;
}

.cz-switch:checked,
.form-check-input:checked {
    background-color: #800000 !important;
    border-color: #800000 !important;
}

.title-page-cozy {
    color: #7B121C;
    font-weight: bold;
    font-size: 1.5rem;
}

.bg-light-wine {
    background-color: #fff8f8;
}

.border-start-wine {
    border-left: 4px solid #800000;
}

.bg-gold {
    background-color: #ffc107;
}

/* Icon Header */
.icon-header-box {
    width: 50px;
    height: 50px;
    background: #800000;
    color: white;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
}

.upload-zone {
    width: 300px;
    /* Tăng kích thước khung lên một chút cho đẹp */
    height: 300px;
    border-radius: 50%;
    /* Ép khung thành hình tròn */
    border: 2px dashed #ddd;
    position: relative;
    cursor: pointer;
    overflow: hidden;
    /* Quan trọng: Cắt ảnh thừa để luôn nằm trong hình tròn */
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f8f9fa;
    margin: 0 auto;
    /* Căn giữa trong card */
}

.upload-zone:hover {
    border-color: #7D161A;
    /* Màu đỏ mận khi hover */
    background-color: #f1f1f1;
}

.img-preview {
    width: 100% !important;
    /* Ép ảnh full chiều rộng khung */
    height: 100% !important;
    /* Ép ảnh full chiều cao khung */
    object-fit: cover;
    /* Quan trọng: Giữ tỉ lệ ảnh, không bị móp méo */
    display: block;
}

.upload-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    /* Nền tối mờ */
    color: white;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    opacity: 0;
    /* Mặc định ẩn */
    transition: opacity 0.3s ease;
}

.upload-zone:hover .upload-overlay {
    opacity: 1;
    /* Hiện khi di chuột vào */
}

.upload-placeholder {
    text-align: center;
    color: #6c757d;
}

.icon-circle {
    width: 60px;
    height: 60px;
    background: #fdf2f2;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 10px;
}

.text-light-wine {
    color: #7D161A;
}

/* Card stats & helper */
.bg-light-wine {
    background-color: #fdf2f2;
}

.text-wine {
    color: #7D161A;
}

/* Form Input Custom */
.form-label-custom {
    font-weight: 700;
    font-size: 13px;
    color: #444;
    text-transform: uppercase;
    margin-bottom: 8px;
    display: block;
}

.input-group-custom {
    position: relative;
}

.icon-input {
    position: absolute;
    left: 15px;
    top: 50%;
    transform: translateY(-50%);
    color: #999;
    z-index: 5;
}

.input-group-custom .form-control {
    padding-left: 45px;
    height: 48px;
    border-radius: 10px;
    border: 1px solid #e0e0e0;
}

.custom-input {
    border-radius: 10px;
    border: 1px solid #e0e0e0;
    padding: 10px 15px;
}

.form-control:focus {
    border-color: #800000;
    box-shadow: 0 0 0 4px rgba(128, 0, 0, 0.1);
}

/* Badge Number */
.badge-number {
    width: 32px;
    height: 32px;
    background: #800000;
    color: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    margin-right: 15px;
    flex-shrink: 0;
}

/* Nút giới tính kiểu hiện đại */
.btn-outline-wine {
    color: #800000;
    border-color: #800000;
    border-radius: 10px;
    padding: 10px;
}

.btn-check:checked+.btn-outline-wine {
    background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
    color: white;
}

/* Buttons */
.btn-main-custom {
    background: #800000;
    color: white;
    border-radius: 10px;
    font-weight: 600;
    transition: 0.3s;
}

.btn-check+.btn:hover {
    border: 1px solid black;
}

.btn-main-custom:hover:not(:disabled) {
    background: #600000;
    transform: translateY(-2px);
}

.text-wine {
    color: #800000;
}

.btn-wine {
    background: #800000;
    color: white;
    border: none;
}

.btn-wine:hover {
    background: #600000;
}

.input-group-custom {
    display: flex;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
    border-radius: 50px;
}

.address-item {
    background: #fdfdfd;
    border: 1px solid #f0f0f0;
    transition: all 0.3s ease;
}

.address-item:hover {
    background: #fff;
    border-color: #800000;
    transform: translateX(5px);
    box-shadow: 0 4px 12px rgba(128, 0, 0, 0.08);
}

.icon-map-pin {
    width: 32px;
    height: 32px;
    background: #fff0f0;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    flex-shrink: 0;
}

/* Hiệu ứng mượt mà khi thêm/xóa */
.list-enter-active,
.list-leave-active {
    transition: all 0.4s ease;
}

.list-enter-from,
.list-leave-to {
    opacity: 0;
    transform: translateY(20px);
}

.btn-light-custom {
    background: #f0f0f0;
    color: #555;
    border-radius: 10px;
    font-weight: 600;
}

.error-text {
    color: #dc3545;
    font-size: 12px;
    margin-top: 5px;
    min-height: 18px;
    font-weight: 500;
}

.star {
    color: #ff4d4f;
}

.dashed {
    border-top: 1px dashed #ddd;
    margin: 25px 0;
}

.tiny {
    font-size: 11px;
}

.border-wine {
    border: 2px solid #800000 !important;
}

.text-wine {
    color: #800000 !important;
}

.cursor-pointer {
    cursor: pointer;
}

.transition-all {
    transition: all 0.2s ease-in-out;
}

.hover-scale:hover {
    transform: scale(1.2);
}

.border-dashed {
    border: 2px dashed #dee2e6 !important;
}

/* Hiệu ứng làm mờ các ô không được chọn để nổi bật ô được chọn */
.opacity-75:hover {
    opacity: 1;
    border-color: #dee2e6 !important;
}

/* Hiệu ứng cho danh sách địa chỉ */
.text-wine {
    color: #800000;
}

.btn-wine {
    background: #800000;
    color: white;
}

.bg-wine-subtle {
    background-color: #fff5f5 !important;
}

.border-wine-light {
    border: 1px solid #ffcccc !important;
}

.text-light-gray {
    color: #dee2e6;
}

.cursor-pointer {
    cursor: pointer;
}

.tiny {
    font-size: 0.7rem;
}

.address-item {
    transition: all 0.3s ease;
    border: 1px solid #f0f0f0;
}

.address-item:hover {
    transform: translateX(8px);
    border-color: #800000;
    background-color: white;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.icon-star i {
    transition: transform 0.2s;
}

.icon-star:hover i {
    transform: scale(1.3);
}

.text-wine {
    color: #800000 !important;
}

.bg-wine {
    background-color: #800000 !important;
}

.address-card {
    transition: all 0.25s ease;
}

/* Hiệu ứng nẩy nhẹ khi hover vào card địa chỉ */
.address-card:hover {
    box-shadow: 0 10px 20px rgba(128, 0, 0, 0.05);
}

/* Tinh chỉnh Switch màu Wine */
.switch-wine:checked {
    background-color: #800000 !important;
    border-color: #800000 !important;
}

.border-light {
    border-color: #f0f0f0 !important;
}

/* Tùy chỉnh khung Select2 cho giống Bootstrap 5 và màu Wine */
:deep(.select2-container--default .select2-selection--single) {
    border: 1px solid #dee2e6 !important;
    height: 40px !important;
    border-radius: 8px !important;
    display: flex;
    align-items: center;
}

:deep(.select2-container--default .select2-selection--single .select2-selection__arrow) {
    height: 38px !important;
}

:deep(.select2-container--default .select2-selection--single:focus) {
    border-color: #800000 !important;
    box-shadow: 0 0 0 0.25rem rgba(128, 0, 0, 0.1);
}

/* Màu chữ của placeholder */
:deep(.select2-container--default .select2-selection--single .select2-selection__placeholder) {
    color: #6c757d;
    font-size: 0.875rem;
}

.custom-filter-multiselect {
    --ms-border-color: #dee2e6;
    --ms-radius: 8px;

    /* Màu vòng sáng khi focus */
    --ms-ring-color: rgba(128, 0, 0, 0.1);
    --ms-border-color-active: #800000;

    /* Hover vào Option chưa chọn */
    --ms-option-bg-pointed: #fdf2f2;
    --ms-option-color-pointed: #800000;

    /* Màu Option khi đã được chọn */
    --ms-option-bg-selected: #800000;
    --ms-option-color-selected: #ffffff;
    --ms-option-bg-selected-pointed: #600000;
}

/* Đổi màu text hiển thị ra ngoài */
.custom-filter-multiselect :deep(.multiselect-single-label) {
    color: #444;
    font-size: 14px;
}

.custom-filter-multiselect :deep(.multiselect-placeholder) {
    color: #888;
    font-size: 14px;
}

/* Đảm bảo Option đã chọn luôn có chữ màu trắng */
.custom-filter-multiselect :deep(.multiselect-option.is-selected) {
    background-color: #800000 !important;
    color: white !important;
}

/* 🚨 CSS KHI VALIDATE BỊ LỖI (Viền đỏ rực) */
.is-invalid-multi {
    --ms-border-color: #dc3545;
    --ms-ring-color: rgba(220, 53, 69, 0.25);
    box-shadow: 0 0 0 0.25rem rgba(220, 53, 69, 0.25);
}

.error-text {
    font-size: 12px;
    color: #dc3545;
    font-weight: 500;
}
</style>
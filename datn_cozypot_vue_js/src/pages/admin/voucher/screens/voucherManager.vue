<template>
    <div class="flex-grow-1 promotion-manager-wrapper ">
        <div class="toast-container">
            <div v-for="toast in toasts" :key="toast.id" class="custom-toast" :class="toast.type">
                <i :class="toast.type === 'success' ? 'fa-solid fa-circle-check' : 'fa-solid fa-circle-xmark'"></i>
                <div class="toast-content">
                    <h4>{{ toast.title }}</h4>
                    <p>{{ toast.message }}</p>
                </div>
                <span class="close" @click="removeToast(toast.id)">&times;</span>
            </div>
        </div>

        <div v-if="confirmModal.show" class="modal-overlay">
            <div class="confirm-modal animate__animated animate__zoomIn">
                <div class="confirm-icon">
                    <i class="fa-solid fa-circle-question"></i>
                </div>
                <h4 class="mt-3">{{ confirmModal.title }}</h4>
                <p class="text-muted">{{ confirmModal.message }}</p>
                <div class="d-flex justify-content-center gap-2 mt-4">
                    <button class="btn btn-light px-4" @click="confirmModal.show = false">Hủy bỏ</button>
                    <button class="btn-red-dark px-4" @click="executeConfirm">Xác nhận</button>
                </div>
            </div>
        </div>

        <div v-if="!isFormActive">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h2 class="title-page">Quản lý phiếu giảm giá</h2>
            </div>

            <div class="filter-card mb-4 shadow-sm p-3 bg-white rounded">
                <div class="row g-3 align-items-end">
                    <div class="col-md-3">
                        <label class="filter-label fw-bold">Tìm kiếm</label>
                        <input v-model="filters.keyword" class="form-control custom-input"
                            placeholder="Mã, code hoặc tên..." @input="onSearchInput">
                    </div>
                    <div class="col-md-2">
                        <label class="filter-label fw-bold">Đối tượng</label>
                        <select v-model="filters.doiTuong" class="form-select custom-input" @change="handleSearch">
                            <option :value="null">Tất cả đối tượng</option>
                            <option :value="0">Công khai</option>
                            <option :value="1">Cá nhân</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label class="filter-label fw-bold">Loại giảm giá</label>
                        <select v-model="filters.loaiGiamGia" class="form-select custom-input" @change="handleSearch">
                            <option :value="null">Tất cả loại</option>
                            <option :value="1">Giảm theo %</option>
                            <option :value="2">Giảm theo tiền</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label class="filter-label fw-bold">Trạng thái</label>
                        <select v-model.number="filters.trangThai" class="form-select custom-input"
                            @change="handleSearch">
                            <option :value="null">Tất cả trạng thái</option>
                            <option :value="1">Đang hoạt động</option>
                            <option :value="3">Sắp diễn ra</option>
                            <option :value="2">Hết hạn</option>
                            <option :value="0">Ngừng hoạt động</option>
                        </select>
                    </div>
                    <div class="col-md-3 d-flex align-items-end gap-2">
                        <button class="btn-reset-filter w-100" @click="resetFilters">
                            <i class="fas fa-sync-alt"></i> Làm mới
                        </button>
                    </div>
                    <div class="col-md-3">
                        <label class="filter-label fw-bold">Từ ngày</label>
                        <input type="date" v-model="filters.ngayBatDau" class="form-control custom-input"
                            @change="handleSearch">
                    </div>
                    <div class="col-md-3">
                        <label class="filter-label fw-bold">Đến ngày</label>
                        <input type="date" v-model="filters.ngayKetThuc" class="form-control custom-input"
                            @change="handleSearch">
                    </div>
                </div>
            </div>

            <div class="d-flex justify-content-end mb-3">
                <button class="btn-red-dark" @click="openFormAdd">
                    <i class="fas fa-plus me-2"></i> Thêm phiếu giảm giá
                </button>
            </div>

            <div class="table-container shadow-sm bg-white rounded overflow-hidden">
                <table class="table mb-0 custom-table">
                    <thead>
                        <tr>
                            <th>STT</th>
                            <th>THÔNG TIN PHIẾU</th>
                            <th>GIÁ TRỊ & ĐIỀU KIỆN</th>
                            <th>ĐỐI TƯỢNG</th>
                            <th>SỐ LƯỢNG</th>
                            <th>THỜI GIAN HẠN</th>
                            <th>TRẠNG THÁI</th>
                            <th class="text-center">HÀNH ĐỘNG</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(pg, index) in listPhieuGiamGia" :key="pg.id">
                            <td>{{ (pagination.currentPage - 1) * pagination.pageSize + index + 1 }}</td>
                            <td>
                                <div class="fw-bold text-dark">{{ pg.tenPhieuGiamGia }}</div>
                                <small class="text-muted">Mã: {{ pg.maPhieuGiamGia }} | Code: {{ pg.codeGiamGia
                                }}</small>
                            </td>
                            <td>
                                <div class="text-danger fw-bold">
                                    Giảm: {{ pg.loaiGiamGia === 1 ? pg.giaTriGiam + '%' : formatPrice(pg.giaTriGiam) }}
                                </div>
                                <div class="small text-muted" style="font-size: 0.8rem;">
                                    - Tối đa: {{ formatPrice(pg.giaTriGiamToiDa) }} <br>
                                    - Đơn tối thiểu: {{ formatPrice(pg.donHangToiThieu) }}
                                </div>
                            </td>
                            <td>
                                <span class="badge" :class="pg.doiTuong === 0 ? 'bg-info' : 'bg-warning text-dark'">
                                    {{ pg.doiTuong === 0 ? 'Công khai' : 'Cá nhân' }}
                                </span>
                            </td>
                            <td>{{ pg.soLuong }}</td>
                            <td>
                                <div class="small">
                                    <span class="text-success">Bắt đầu:</span> {{ formatDate(pg.ngayBatDau) }} <br>
                                    <span class="text-danger">Kết thúc:</span> {{ formatDate(pg.ngayKetThuc) }}
                                </div>
                            </td>
                            <td>
                                <span :class="getStatusDisplay(pg).class">
                                    {{ getStatusDisplay(pg).text }}
                                </span>
                            </td>
                            <td class="text-center">
                                <div class="action-group d-flex justify-content-center gap-2">
                                    <i class="fas fa-eye view-icon" title="Chi tiết" @click="openFormView(pg.id)"></i>
                                    <i class="fas fa-pen edit-icon"
                                        :class="{ 'disabled-icon': getStatusDisplay(pg).text === 'Hết hạn' }"
                                        title="Sửa"
                                        @click="getStatusDisplay(pg).text !== 'Hết hạn' && openFormEdit(pg.id)">
                                    </i>

                                    <div class="form-check form-switch d-inline-block mb-0">
                                        <input class="form-check-input custom-red-switch" type="checkbox"
                                            :checked="pg.trangThai === 1"
                                            :disabled="getStatusDisplay(pg).text === 'Hết hạn'"
                                            @click.prevent="getStatusDisplay(pg).text !== 'Hết hạn' && triggerToggleStatus(pg)">
                                    </div>
                                </div>
                            </td>
                        </tr>
                        <tr v-if="listPhieuGiamGia.length === 0">
                            <td colspan="8" class="text-center py-4 text-muted">Không tìm thấy dữ liệu phù hợp</td>
                        </tr>
                    </tbody>
                </table>

                <div class="d-flex justify-content-center mt-4 pb-3">
                    <div class="pagination-wrapper">
                        <button class="pg-btn" :disabled="pagination.currentPage === 1"
                            @click="changePage(pagination.currentPage - 1)">
                            <i class="fas fa-chevron-left"></i>
                        </button>
                        <div class="pg-numbers">
                            <button v-for="page in pagination.totalPages" :key="page" class="pg-num"
                                :class="{ 'active': page === pagination.currentPage }" @click="changePage(page)">
                                {{ page }}
                            </button>
                        </div>
                        <button class="pg-btn" :disabled="pagination.currentPage === pagination.totalPages"
                            @click="changePage(pagination.currentPage + 1)">
                            <i class="fas fa-chevron-right"></i>
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div v-else class="form-page-container animate__animated animate__fadeIn">
            <div class="d-flex align-items-center mb-4">
                <button class="btn btn-red-dark me-3" @click="closeForm">
                    <i class="fas fa-arrow-left"></i> Quay lại
                </button>
                <h2 class="title-page mb-0">
                    {{ isReadOnly ? 'Chi tiết phiếu' : (selectedId ? 'Cập nhật phiếu' : 'Thêm mới phiếu giảm giá') }}
                </h2>

            </div>


            <form @submit.prevent="triggerSubmit">
                <div class="row g-4">
                    <div :class="isCustomerListOpen ? 'col-md-5' : 'col-md-12'">
                        <div class="card border-0 shadow-sm p-4 h-100">
                            <h5 class="mb-4 text-maroon"><i class="fa-solid fa-circle-info me-2"></i>Thông tin phiếu
                                giảm giá</h5>

                            <div class="mb-3">
                                <label class="form-label fw-bold">TÊN PHIẾU GIẢM GIÁ <span
                                        class="text-danger">*</span></label>
                                <input v-model="formData.tenPhieuGiamGia" type="text" class="form-control custom-input"
                                    :class="{ 'is-invalid': errors.tenPhieuGiamGia }" :disabled="isReadOnly"
                                    placeholder="Ví dụ: Khuyến mãi Tết Nguyên Đán">
                                <div class="invalid-feedback">{{ errors.tenPhieuGiamGia }}</div>
                            </div>

                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold">MÃ CODE <span class="text-danger">*</span></label>
                                    <input v-model="formData.codeGiamGia" type="text"
                                        class="form-control custom-input text-uppercase"
                                        :class="{ 'is-invalid': errors.codeGiamGia }" :disabled="isReadOnly"
                                        placeholder="TET2026">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold">LOẠI GIẢM GIÁ</label>
                                    <select v-model="formData.loaiGiamGia" class="form-select custom-input"
                                        :disabled="isReadOnly">
                                        <option :value="1">Giảm theo %</option>
                                        <option :value="2">Giảm theo tiền mặt</option>
                                    </select>
                                </div>

                            </div>
                            <div class="mb-3">
                                <label class="form-label fw-bold">ĐỢT KHUYẾN MÃI</label>
                                <select v-model="formData.idDotKhuyenMai" class="form-select custom-input"
                                    :disabled="isReadOnly">
                                    <option :value="null">-- Chọn đợt giảm giá --</option>
                                    <option v-for="item in listDotKhuyenMai" :key="item.id" :value="item.id">
                                        {{ item.tenDotKhuyenMai }}
                                    </option>
                                </select>
                            </div>



                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold">GIÁ TRỊ GIẢM <span
                                            class="text-danger">*</span></label>
                                    <div class="input-group has-validation"> <input v-model.number="formData.giaTriGiam"
                                            type="number" class="form-control custom-input"
                                            :class="{ 'is-invalid': errors.giaTriGiam }" :disabled="isReadOnly">
                                        <span class="input-group-text">{{ formData.loaiGiamGia === 1 ? '%' : 'đ'
                                        }}</span>
                                        <div class="invalid-feedback">{{ errors.giaTriGiam }}</div>
                                    </div>
                                </div>

                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold">SỐ LƯỢNG <span
                                            class="text-danger">*</span></label>
                                    <input v-model.number="formData.soLuong" type="number"
                                        class="form-control custom-input" :class="{ 'is-invalid': errors.soLuong }"
                                        :disabled="isReadOnly">
                                    <div class="invalid-feedback">{{ errors.soLuong }}</div>
                                </div>

                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold">GIẢM TỐI ĐA <span
                                            class="text-danger">*</span></label>
                                    <input v-model.number="formData.giaTriGiamToiDa" type="number"
                                        class="form-control custom-input"
                                        :class="{ 'is-invalid': errors.giaTriGiamToiDa }" :disabled="isReadOnly">
                                    <div class="invalid-feedback">{{ errors.giaTriGiamToiDa }}</div>
                                </div>

                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold">ĐƠN TỐI THIỂU <span
                                            class="text-danger">*</span></label>
                                    <input v-model.number="formData.donHangToiThieu" type="number"
                                        class="form-control custom-input"
                                        :class="{ 'is-invalid': errors.donHangToiThieu }" :disabled="isReadOnly">
                                    <div class="invalid-feedback">{{ errors.donHangToiThieu }}</div>
                                </div>

                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold">NGÀY BẮT ĐẦU <span
                                            class="text-danger">*</span></label>
                                    <input v-model="formData.ngayBatDau" type="datetime-local"
                                        class="form-control custom-input" :class="{ 'is-invalid': errors.ngayBatDau }"
                                        :disabled="isReadOnly">
                                    <div class="invalid-feedback">{{ errors.ngayBatDau }}</div>
                                </div>

                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold">NGÀY KẾT THÚC <span
                                            class="text-danger">*</span></label>
                                    <input v-model="formData.ngayKetThuc" type="datetime-local"
                                        class="form-control custom-input" :class="{ 'is-invalid': errors.ngayKetThuc }"
                                        :disabled="isReadOnly">
                                    <div class="invalid-feedback">{{ errors.ngayKetThuc }}</div>
                                </div>
                            </div>
                            <div class="mb-3">
                                <label class="form-label fw-bold">ĐỐI TƯỢNG ÁP DỤNG</label>
                                <select v-model="formData.doiTuong" class="form-select custom-input"
                                    :disabled="isReadOnly" @change="isCustomerListOpen = (formData.doiTuong === 1)">
                                    <option :value="0">Công khai (Tất cả khách hàng)</option>
                                    <option :value="1">Cá nhân (Chọn khách hàng)</option>
                                </select>
                            </div>

                            <div class="mt-4 d-flex justify-content-end gap-2">
                                <button type="button" class="btn btn-light px-4" @click="closeForm">Hủy</button>
                                <button v-if="!isReadOnly" type="submit" class="btn-red-dark px-4">Lưu phiếu</button>
                            </div>
                        </div>
                    </div>

                    <div v-if="isCustomerListOpen" class="col-md-7">
                        <transition name="fade-slide">
                            <div class="card border-0 shadow-sm p-4 h-100" style="background-color: #f8f9fa;">

                                <div class="d-flex justify-content-between align-items-center mb-4">
                                    <h5 class="mb-0 text-maroon"><i class="fa-solid fa-users me-2"></i>Khách hàng áp
                                        dụng
                                    </h5>
                                    <div class="form-check" v-if="formData.doiTuong === 1">
                                        <input class="form-check-input" type="checkbox" id="selectAll"
                                            :checked="isAllCustomersSelected" @change="toggleAllCustomers">
                                        <label class="form-check-label fw-bold" for="selectAll">Chọn tất cả</label>
                                    </div>
                                </div>

                                <div class="mb-3">
                                    <div class="input-group bg-white rounded shadow-sm">
                                        <span class="input-group-text bg-transparent border-0"><i
                                                class="fas fa-search text-muted"></i></span>
                                        <input v-model="customerSearch" type="text" class="form-control border-0 ps-0"
                                            placeholder="Tìm tên, email hoặc số điện thoại...">
                                    </div>
                                </div>

                                <div class="table-responsive rounded bg-white shadow-sm"
                                    style="max-height: 500px; overflow-y: auto;">
                                    <table class="table table-hover align-middle mb-0">
                                        <thead class="table-light sticky-top">
                                            <tr>
                                                <th class="text-center" style="width: 50px;">Chọn</th>
                                                <th>Khách hàng</th>
                                                <th>Liên hệ</th>
                                            </tr>
                                        </thead>
                                        <tbody v-if="formData.doiTuong === 1">
                                            <tr v-for="kh in filteredCustomers" :key="kh.id">
                                                <td class="text-center">
                                                    <input type="checkbox" :value="kh.id"
                                                        v-model="formData.listIdKhachHang" class="form-check-input"
                                                        :disabled="isReadOnly" />
                                                </td>
                                                <td>
                                                    <div class="fw-bold text-dark">{{ kh.hoTen || kh.tenKhachHang }}
                                                    </div>
                                                    <small class="text-muted">ID: {{ kh.id }}</small>
                                                </td>
                                                <td>
                                                    <div class="small"><i class="fa-solid fa-envelope me-1"></i>{{
                                                        kh.email
                                                        }}</div>
                                                    <div class="small"><i class="fa-solid fa-phone me-1"></i>{{
                                                        kh.soDienThoai }}</div>
                                                </td>
                                            </tr>
                                        </tbody>
                                        <tbody v-else>
                                            <tr>
                                                <td colspan="3" class="text-center py-5 text-muted">
                                                    <i class="fa-solid fa-globe fa-2x mb-2 d-block"></i>
                                                    Phiếu đang ở chế độ <b>Công khai</b>.<br>Tất cả khách hàng đều có
                                                    thể sử
                                                    dụng.
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </transition>
                    </div>
                </div>
            </form>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import axios from 'axios';
import '../voucherStyle.css';
import voucherService from '@/services/voucherService';

// --- TRẠNG THÁI GIAO DIỆN ---
const isFormActive = ref(false);
const isReadOnly = ref(false);
const selectedId = ref(null);
const customerSearch = ref('');
const listPhieuGiamGia = ref([]);
const listKhachHang = ref([]);

// --- DỮ LIỆU PHIẾU GIẢM GIÁ ---
const formData = reactive({
    maPhieuGiamGia: '',
    codeGiamGia: '',
    tenPhieuGiamGia: '',
    loaiGiamGia: 1,
    giaTriGiam: 0,
    giaTriGiamToiDa: 0,
    donHangToiThieu: 0,
    doiTuong: 0,
    ngayBatDau: '',
    ngayKetThuc: '',
    soLuong: 1,
    idDotKhuyenMai: null,
    listIdKhachHang: [],
    listEmails: []
});
const listDotKhuyenMai = ref([]);


// --- PHÂN TRANG & BỘ LỌC ---
const filters = reactive({
    keyword: '',
    trangThai: null,
    doiTuong: null,
    loaiGiamGia: null,
    ngayBatDau: '',
    ngayKetThuc: ''
});

const pagination = reactive({
    currentPage: 1,
    pageSize: 5,
    totalPages: 0
});

// --- QUẢN LÝ LỖI (VALIDATE) ---
const errors = reactive({});

const clearErrors = () => {
    Object.keys(errors).forEach(key => delete errors[key]);
};

const validateForm = () => {
    let isValid = true;
    clearErrors();

    formData.tenPhieuGiamGia = formData.tenPhieuGiamGia?.trim() || '';
    formData.codeGiamGia = formData.codeGiamGia?.trim().toUpperCase() || '';

    // 1. Tên phiếu
    if (!formData.tenPhieuGiamGia) {
        errors.tenPhieuGiamGia = "Tên phiếu không được để trống";
        isValid = false;
    }

    // 2. Mã Code
    if (!formData.codeGiamGia) {
        errors.codeGiamGia = "Mã code không được để trống";
        isValid = false;
    }

    // 3. Giá trị giảm
    if (formData.loaiGiamGia === 2) { // Tiền mặt
        if (!formData.giaTriGiam || formData.giaTriGiam < 1000) {
            errors.giaTriGiam = "Giá trị giảm tối thiểu là 1,000đ";
            isValid = false;
        }
    } else { // Phần trăm
        if (!formData.giaTriGiam || formData.giaTriGiam < 1 || formData.giaTriGiam > 100) {
            errors.giaTriGiam = "Phần trăm giảm phải từ 1 đến 100";
            isValid = false;
        }
    }

    // 4. Giá trị giảm tối đa
    if (formData.giaTriGiamToiDa === null || formData.giaTriGiamToiDa === undefined || formData.giaTriGiamToiDa < 0) {
        errors.giaTriGiamToiDa = "Giá trị tối đa không được để trống";
        isValid = false;
    }

    // 5. Đơn hàng tối thiểu
    if (formData.donHangToiThieu === null || formData.donHangToiThieu === undefined || formData.donHangToiThieu < 0) {
        errors.donHangToiThieu = "Đơn tối thiểu không được để trống";
        isValid = false;
    }

    // 6. Số lượng
    if (!formData.soLuong || formData.soLuong < 1) {
        errors.soLuong = "Số lượng phải ít nhất là 1";
        isValid = false;
    }

    // 7. Ngày bắt đầu & Kết thúc
    const now = new Date();
    const start = formData.ngayBatDau ? new Date(formData.ngayBatDau) : null;
    const end = formData.ngayKetThuc ? new Date(formData.ngayKetThuc) : null;

    if (!formData.ngayBatDau) {
        errors.ngayBatDau = "Vui lòng chọn ngày bắt đầu";
        isValid = false;
    }

    if (!formData.ngayKetThuc) {
        errors.ngayKetThuc = "Vui lòng chọn ngày kết thúc";
        isValid = false;
    } else if (start && end <= start) {
        errors.ngayKetThuc = "Ngày kết thúc phải sau ngày bắt đầu";
        isValid = false;
    }

    // 8. Đối tượng cá nhân
    if (formData.doiTuong === 1 && formData.listIdKhachHang.length === 0) {
        showToast("Lỗi", "Vui lòng chọn ít nhất 1 khách hàng!", "error");
        isValid = false;
    }

    return isValid;
};

// --- HỆ THỐNG TOAST ---
const toasts = ref([]);
const showToast = (title, message, type = 'success') => {
    const id = Date.now();
    toasts.value.push({ id, title, message, type });
    setTimeout(() => removeToast(id), 4000);
};
const removeToast = (id) => toasts.value = toasts.value.filter(t => t.id !== id);

// --- MODAL XÁC NHẬN ---
const confirmModal = reactive({ show: false, title: '', message: '', action: null });
const triggerConfirm = (title, message, action) => {
    Object.assign(confirmModal, { show: true, title, message, action });
};
const executeConfirm = async () => {
    const action = confirmModal.action;
    confirmModal.show = false;
    if (action) await action();
};

const filteredCustomers = computed(() => {
    if (!listKhachHang.value) return [];
    if (!customerSearch.value) return listKhachHang.value;

    const k = customerSearch.value.toLowerCase().trim();
    return listKhachHang.value.filter(kh => {
        // Kiểm tra cả hoTen và tenKhachHang tùy theo API của bạn
        const ten = (kh.hoTen || kh.tenKhachHang || '').toLowerCase();
        const email = (kh.email || '').toLowerCase();
        const sdt = (kh.soDienThoai || '');

        return ten.includes(k) || email.includes(k) || sdt.includes(k);
    });
});

const isAllCustomersSelected = computed(() => {
    return filteredCustomers.value.length > 0 &&
        filteredCustomers.value.every(kh => formData.listIdKhachHang.includes(kh.id));
});

const toggleAllCustomers = (e) => {
    const visibleIds = filteredCustomers.value.map(kh => Number(kh.id));
    if (e.target.checked) {
        formData.listIdKhachHang = [...new Set([...formData.listIdKhachHang, ...visibleIds])];
    } else {
        formData.listIdKhachHang = formData.listIdKhachHang.filter(id => !visibleIds.includes(id));
    }
};

const handleSearch = async () => {
    try {
        // 1. Gửi request lên Server 
        // Lưu ý: Nếu lọc Hết hạn (2) hoặc Sắp diễn ra (3), 
        // ta nên gửi trangThai = 1 (Active) lên server để lấy các phiếu chưa bị "Ngừng hoạt động" tay.
        let paramsTrangThai = filters.trangThai;
        if (filters.trangThai === 2 || filters.trangThai === 3) {
            paramsTrangThai = 1;
        }

        const res = await voucherService.fetchData({ ...filters, trangThai: paramsTrangThai }, pagination);
        const now = new Date().getTime();

        // 2. Lọc dữ liệu dựa trên thời gian thực
        let rawData = res.content || [];

        if (filters.trangThai === 1) { // Đang hoạt động
            listPhieuGiamGia.value = rawData.filter(pg => {
                const start = new Date(pg.ngayBatDau).getTime();
                const end = new Date(pg.ngayKetThuc).getTime();
                return pg.trangThai === 1 && now >= start && now <= end;
            });
        }
        else if (filters.trangThai === 2) { // Hết hạn
            listPhieuGiamGia.value = rawData.filter(pg => {
                const end = new Date(pg.ngayKetThuc).getTime();
                return pg.trangThai === 1 && now > end;
            });
        }
        else if (filters.trangThai === 3) { // Sắp diễn ra
            listPhieuGiamGia.value = rawData.filter(pg => {
                const start = new Date(pg.ngayBatDau).getTime();
                return pg.trangThai === 1 && now < start;
            });
        }
        else {
            // Trường hợp Ngừng hoạt động (0) hoặc Tất cả (null)
            listPhieuGiamGia.value = rawData;
        }

        pagination.totalPages = res.totalPages || 0;
    } catch (e) {
        console.error("Lỗi tải danh sách:", e);
        showToast("Lỗi", "Không thể tải danh sách phiếu giảm giá", "error");
    }
};
import dayjs from 'dayjs';

// Hàm định dạng ngày tháng dd/mm/yyyy HH:mm
const formatDate = (dateString) => {
    if (!dateString) return '---';
    return dayjs(dateString).format('DD/MM/YYYY HH:mm');
};
// Thêm vào cùng nhóm với các ref trạng thái giao diện
const isCustomerListOpen = ref(false);
// Hàm chỉ lấy ngày dd/mm/yyyy
const formatDateOnly = (dateString) => {
    if (!dateString) return '---';
    return dayjs(dateString).format('DD/MM/YYYY');
};


const loadDetail = async (id) => {

    try {

        const res = await voucherService.getById(id);
        Object.assign(formData, res);
        formData.idDotKhuyenMai = res.idDotKhuyenMai?.id ?? res.idDotKhuyenMai ?? null;


        // ✅ ÉP KIỂU ĐỐI TƯỢNG (RẤT QUAN TRỌNG)
        formData.doiTuong = Number(res.doiTuong);
        isCustomerListOpen.value = formData.doiTuong === 1;
        // Format ngày tháng
        if (res.ngayBatDau) formData.ngayBatDau = res.ngayBatDau.substring(0, 16);
        if (res.ngayKetThuc) formData.ngayKetThuc = res.ngayKetThuc.substring(0, 16);

        if (res.danhSachKhachHang && res.danhSachKhachHang.length > 0) {
            formData.listIdKhachHang = res.danhSachKhachHang.map(item => {
                // ưu tiên lấy id khách hàng
                if (item.khachHang && item.khachHang.id) {
                    return Number(item.khachHang.id);
                }
                // fallback nếu BE trả thẳng id
                if (item.id) {
                    return Number(item.id);
                }
                return null;
            }).filter(id => id !== null);
        } else {
            formData.listIdKhachHang = [];
        }
        console.log("Danh sách ID khách hàng đã chọn:", formData.listIdKhachHang);
    } catch (err) {
        console.error(err);
        showToast("Lỗi", "Không thể tải dữ liệu chi tiết", "error");
    }
};

const triggerSubmit = () => {
    if (!validateForm()) return;

    triggerConfirm(
        "Xác nhận lưu dữ liệu",
        "Hệ thống sẽ lưu phiếu và gửi email thông báo tới các khách hàng được chọn.",
        async () => {
            try {
                // Lấy danh sách email từ các ID khách hàng đã chọn
                formData.listEmails = listKhachHang.value
                    .filter(kh => formData.listIdKhachHang.includes(kh.id))
                    .map(kh => kh.email);

                // Nếu đối tượng là cá nhân mà email rỗng thì chặn lại
                if (formData.doiTuong === 1 && formData.listEmails.length === 0) {
                    showToast("Lỗi", "Không tìm thấy email của khách hàng đã chọn", "error");
                    return;
                }

                if (selectedId.value) {
                    await voucherService.update(selectedId.value, formData);
                    showToast("Thành công", "Phiếu giảm giá đã được cập nhật.");
                } else {
                    await voucherService.create(formData);
                    showToast("Thành công", "Phiếu giảm giá mới đã được tạo.");
                }
                closeForm();
                handleSearch();
            } catch (err) {
                showToast("Thất bại", err.response?.data?.message || "Lỗi hệ thống", "error");
            }
        }
    );
};

const triggerToggleStatus = (pg) => {
    const actionText = pg.trangThai === 1 ? 'ngừng hoạt động' : 'kích hoạt';
    triggerConfirm(
        "Xác nhận thay đổi",
        `Bạn có chắc muốn ${actionText} phiếu ${pg.maPhieuGiamGia}?`,
        async () => {
            try {
                await voucherService.toggleStatus(pg.id, pg.trangThai);
                showToast("Thành công", "Trạng thái đã được cập nhật.");
                handleSearch();
            } catch (e) {
                showToast("Lỗi", "Không thể cập nhật trạng thái", "error");
            }
        }
    );
};

// --- ĐIỀU HƯỚNG FORM ---
const openFormAdd = async () => {
    resetFormData();
    clearErrors();
    await loadDotDangHoatDong();
    selectedId.value = null;
    isReadOnly.value = false;
    isFormActive.value = true;
    isCustomerListOpen.value = false; // Reset khi thêm mới
};

const openFormEdit = async (id) => {
    clearErrors();
    selectedId.value = id;
    isReadOnly.value = false;
    if (listKhachHang.value.length === 0) {
        await loadCustomers();
    }
    await loadDetail(id);
        // ⭐ TRUYỀN idDotKhuyenMai
    await loadDotDangHoatDong(formData.idDotKhuyenMai);
    isFormActive.value = true;
};

const closeForm = () => {
    isFormActive.value = false;
    isCustomerListOpen.value = false;
};

const loadDotDangHoatDong = async (selectedDotId = null) => {
    try {
        const res = await axios.get('http://localhost:8080/api/dot-khuyen-mai/active');
        let dots = res.data || [];

        // 🔥 Nếu đang sửa / xem & có đợt đã chọn
        if (selectedDotId) {
            const existed = dots.some(d => d.id === selectedDotId);

            if (!existed) {
                // gọi thêm API lấy đợt theo ID
                const detail = await axios.get(
                    `http://localhost:8080/api/dot-khuyen-mai/${selectedDotId}`
                );
                dots.unshift(detail.data); // cho lên đầu combobox
            }
        }

        listDotKhuyenMai.value = dots;
    } catch (e) {
        showToast("Lỗi", "Không tải được đợt khuyến mãi", "error");
    }
};


const openFormView = async (id) => {
    selectedId.value = id;
    isReadOnly.value = true;

    await loadDetail(id);
    await loadDotDangHoatDong(formData.idDotKhuyenMai);

    isFormActive.value = true; // Ẩn bảng, hiện form
};


const loadCustomers = async () => {
    try {
        const res = await axios.get('http://localhost:8080/api/khach-hang/active'); // Thay bằng service của bạn
        listKhachHang.value = res.data;
    } catch (err) {
        console.error("Lỗi tải khách hàng", err);
    }
};



const resetFormData = () => {
    Object.assign(formData, {
        maPhieuGiamGia: '', codeGiamGia: '', tenPhieuGiamGia: '', loaiGiamGia: 1,
        giaTriGiam: 0, giaTriGiamToiDa: 0, donHangToiThieu: 0, doiTuong: 0,
        ngayBatDau: '', ngayKetThuc: '', soLuong: 1, listIdKhachHang: [], listEmails: [],idDotKhuyenMai: null,

    });
};

const resetFilters = () => {
    Object.assign(filters, { keyword: '', trangThai: null, doiTuong: null, loaiGiamGia: null, ngayBatDau: '', ngayKetThuc: '' });
    pagination.currentPage = 1;
    handleSearch();
};



const changePage = (p) => {
    if (p >= 1 && p <= pagination.totalPages) {
        pagination.currentPage = p;
        handleSearch();
    }
};

const onSearchInput = () => {
    clearTimeout(window.debounceSearch);
    window.debounceSearch = setTimeout(() => {
        pagination.currentPage = 1;
        handleSearch();
    }, 500);
};

const onDoiTuongChange = () => {
    // Chuyển sang Cá nhân (1) thì mở bảng, ngược lại đóng
    isCustomerListOpen.value = (Number(formData.doiTuong) === 1);
};

// --- HELPERS HIỂN THỊ ---
const formatPrice = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0);
const getStatusDisplay = (pg) => {
    const now = new Date().getTime();
    const start = new Date(pg.ngayBatDau).getTime();
    const end = new Date(pg.ngayKetThuc).getTime();

    if (pg.trangThai === 0) return { text: 'Ngừng hoạt động', class: 'badge bg-secondary' };
    if (now < start) return { text: 'Sắp diễn ra', class: 'badge bg-info' };
    if (now > end) return { text: 'Hết hạn', class: 'badge bg-danger' };
    return { text: 'Hoạt động', class: 'badge bg-success' };
};

onMounted(async () => {
    // Tải danh sách khách hàng ngay khi trang web vừa load xong
    await loadCustomers();
    handleSearch();

});

</script>

<style scoped>
/* Hiệu ứng trượt và mờ dần cho bảng khách hàng */
.fade-slide-enter-active,
.fade-slide-leave-active {
    transition: all 0.4s ease-out;
}

.fade-slide-enter-from {
    opacity: 0;
    transform: translateX(30px);
}

.fade-slide-leave-to {
    opacity: 0;
    transform: translateX(-30px);
}

/* Hiệu ứng mờ dần cho hướng dẫn trống */
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

/* Style cho phần hướng dẫn khi chưa chọn khách hàng */
.empty-state-guide {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    border: 2px dashed #dee2e6;
    border-radius: 1rem;
    color: #6c757d;
    padding: 2rem;
    text-align: center;
    background: #fff;
}



/* Đảm bảo chiều cao card ổn định khi chuyển đổi */
.customer-card-container {
    overflow: hidden;
    background-color: #f8f9fa;
}

.text-maroon {
    color: #800000;
    /* Màu đỏ sẫm tiêu đề */
}

.custom-input {
    border-radius: 8px;
    padding: 10px 12px;
    border: 1px solid #dee2e6;
}

.custom-input:focus {
    border-color: #800000;
    box-shadow: 0 0 0 0.2rem rgba(128, 0, 0, 0.1);
}

.btn-red-dark {
    background-color: #800000;
    color: white;
    border: none;
    border-radius: 6px;
    transition: all 0.3s;
}

.btn-red-dark:hover {
    background-color: #600000;
    transform: translateY(-1px);
}

.table-responsive {
    border: 1px solid white;
    border-radius: 8px;
}

.table-responsive::-webkit-scrollbar {
    width: 6px;
}

.table-responsive::-webkit-scrollbar-thumb {
    background-color: #ccc;
    border-radius: 10px;
}

.input-group-text {
    border-color: #dee2e6;
}

/* Hiệu ứng khi focus vào ô tìm kiếm khách hàng */
.input-group:focus-within .input-group-text,
.input-group:focus-within input {
    border-color: #d32f2f;
}

/* Đồng bộ hóa các hiệu ứng và layout */
.promotion-manager-wrapper {
    background-color: #f8f9fa;
    min-height: 100vh;
}

.toast-container {
    position: fixed;
    top: 30px;
    right: 30px;
    z-index: 9999;
}

.custom-toast {
    display: flex;
    align-items: center;
    gap: 14px;
    background: #fff;
    border-left: 6px solid #2ecc71;
    padding: 16px 20px;
    border-radius: 12px;
    width: 380px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
    margin-bottom: 15px;
    animation: slideIn 0.4s ease forwards;
}

.custom-toast.error {
    border-left-color: #e74c3c;
}

.custom-toast.error i {
    color: #e74c3c;
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 10000;
}

.confirm-modal {
    background: #fff;
    padding: 30px;
    border-radius: 16px;
    width: 400px;
    text-align: center;
}

.custom-input:focus {
    border-color: #d32f2f;
    box-shadow: 0 0 0 0.2rem rgba(211, 47, 47, 0.25);
}

.is-invalid {
    border-color: #dc3545 !important;
}

.disabled-icon {
    opacity: 0.3;
    cursor: not-allowed;
}

.transition-all {
    transition: all 0.3s ease;
}

.sticky-top {
    position: sticky;
    top: 0;
    z-index: 10;
}

/* Trong thẻ <style> */
.customer-selection-box .table-responsive {
    height: 450px;
    /* Tăng chiều cao một chút */
    scrollbar-width: thin;
    /* Làm thanh cuộn nhỏ lại cho gọn */
}

/* Đảm bảo bảng không bị vỡ chữ khi màn hình nhỏ */
.customer-selection-box table td {
    white-space: nowrap;
    padding: 8px 4px;
}

@keyframes slideIn {
    from {
        transform: translateX(100%);
        opacity: 0;
    }

    to {
        transform: translateX(0);
        opacity: 1;
    }
}
</style>
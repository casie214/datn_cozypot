<template>
    <div class="flex-grow-1 promotion-manager-wrapper">
        <div class="toast-container">
            <div v-for="t in toasts" :key="t.id" class="custom-toast" :class="t.type">
                <i :class="t.type === 'error' ? 'fa-solid fa-circle-xmark' : 'fa-solid fa-circle-check'"></i>
                <div class="custom-toast-content">
                    <h4>{{ t.title }}</h4>
                    <p>{{ t.message }}</p>
                </div>
                <span class="close-btn" @click="removeToast(t.id)">&times;</span>
            </div>
        </div>

        <div v-if="!isFormActive">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h2 class="title-page">Quản lý đợt khuyến mãi</h2>
            </div>

            <div class="filter-card mb-4">
                <div class="row g-3 align-items-end">
                    <div class="col-md-3">
                        <label class="filter-label">Tìm kiếm</label>
                        <input v-model="filters.keyword" class="form-control custom-input"
                            placeholder="Nhập mã hoặc tên để tìm..." @input="onSearchInput">
                    </div>
                    <div class="col-md-2">
                        <label class="filter-label">Trạng thái</label>
                        <select v-model.number="filters.trangThai" class="form-select custom-input"
                            @change="handleSearch">
                            <option :value="null">Tất cả trạng thái</option>
                            <option :value="1">Đang hoạt động</option>
                            <option :value="0">Ngừng hoạt động</option>
                            <option :value="2">Đã hết hạn</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label class="filter-label">Từ ngày</label>
                        <input type="date" v-model="filters.ngayBatDau" class="form-control custom-input"
                            @change="handleSearch">
                    </div>
                    <div class="col-md-2">
                        <label class="filter-label">Đến ngày</label>
                        <input type="date" v-model="filters.ngayKetThuc" class="form-control custom-input"
                            @change="handleSearch">
                    </div>
                    <div class="col-md-2 d-flex align-items-end">
                        <button class="btn-reset-filter" @click="resetFilters">
                            <i class="fas fa-sync-alt"></i> Bỏ lọc
                        </button>
                    </div>
                </div>
            </div>

            <div class="d-flex justify-content-end mb-3">
                <button class="btn-red-dark" @click="openFormAdd">
                    <i class="fas fa-plus me-2"></i> Thêm đợt khuyến mãi
                </button>
            </div>

            <div class="table-container shadow-sm">
                <table class="table mb-0 custom-table">
                    <thead>
                        <tr>
                            <th>STT</th>
                            <th>MÃ KM</th>
                            <th>TÊN ĐỢT KM</th>
                            <th>GIẢM (%)</th>
                            <th>THỜI GIAN</th>
                            <th>TRẠNG THÁI</th>
                            <th class="text-center">HÀNH ĐỘNG</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(km, index) in listKhuyenMai" :key="km.id">
                            <td>{{ (pagination.currentPage - 1) * pagination.pageSize + index + 1 }}</td>
                            <td class="fw-bold text-dark">{{ km.maDotKhuyenMai }}</td>
                            <td>{{ km.tenDotKhuyenMai }}</td>
                            <td class="text-center text-danger fw-bold">{{ km.phanTramGiam }}%</td>
                            <td>
                                <small>{{ km.ngayBatDau }} <i class="fas fa-arrow-right mx-1"></i> {{ km.ngayKetThuc
                                }}</small>
                            </td>
                            <td>
                                <span :class="calculateStatus(km).class">
                                    {{ calculateStatus(km).text }}
                                </span>
                            </td>
                            <td class="text-center">
                                <div class="action-group">
                                    <i class="fas fa-eye view-icon" title="Xem chi tiết"
                                        @click="openFormView(km.id)"></i>
                                    <i class="fas fa-pen edit-icon" title="Chỉnh sửa" @click="openFormEdit(km.id)"></i>
                                    <div class="form-check form-switch d-inline-block mb-0">
                                        <input class="form-check-input custom-red-switch" type="checkbox"
                                            :checked="km.trangThai === 1" :disabled="isExpired(km.ngayKetThuc)"
                                            @click.prevent="!isExpired(km.ngayKetThuc) && handleToggleStatus(km)"
                                            :title="isExpired(km.ngayKetThuc) ? 'Khuyến mãi đã hết hạn, không thể đổi trạng thái' : ''">
                                    </div>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>

                <div class="pagination-wrapper mt-3">
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

        <div v-else class="form-page-container animate__animated animate__fadeIn">
            <div class="d-flex align-items-center mb-4">
                <button class="btn btn-red-dark me-3" @click="closeForm">
                    <i class="fas fa-arrow-left"></i> Quay lại
                </button>
                <h2 class="title-page mb-0">{{ formTitle }}</h2>
            </div>

            <div class="card border-0 shadow-sm p-4">
                <form @submit.prevent="submitForm">
                    <div class="row g-0">
                        <div class="col-md-7 p-4 border-end">
                            <h5 class="mb-4 text-primary-red d-flex align-items-center">
                                <i class="fas fa-info-circle me-2"></i> Thông tin đợt khuyến mãi
                            </h5>

                            <div class="mb-3">
                                <label class="form-label fw-bold small text-uppercase">Tên đợt khuyến mãi <span
                                        class="text-danger">*</span></label>
                                <input v-model="formData.tenDotKhuyenMai" type="text"
                                    class="form-control custom-input shadow-none"
                                    :class="{ 'is-invalid': errors.tenDotKhuyenMai }" :disabled="isReadOnly"
                                    placeholder="Ví dụ: Khuyến mãi Tết Nguyên Đán">
                                <div class="invalid-feedback">{{ errors.tenDotKhuyenMai }}</div>
                            </div>

                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold small text-uppercase">Phần trăm giảm (%) <span
                                            class="text-danger">*</span></label>
                                    <div class="input-group">
                                        <input v-model.number="formData.phanTramGiam" type="number"
                                            class="form-control custom-input shadow-none"
                                            :class="{ 'is-invalid': errors.phanTramGiam }" :disabled="isReadOnly">
                                        <span class="input-group-text bg-light text-muted fw-bold">%</span>
                                    </div>
                                    <div class="text-danger small mt-1" v-if="errors.phanTramGiam">{{
                                        errors.phanTramGiam }}</div>
                                </div>

                            </div>

                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold small text-uppercase">Ngày bắt đầu <span
                                            class="text-danger">*</span></label>
                                    <input v-model="formData.ngayBatDau" type="date"
                                        class="form-control custom-input shadow-none"
                                        :class="{ 'is-invalid': errors.ngayBatDau }" :disabled="isReadOnly">
                                    <div class="invalid-feedback">{{ errors.ngayBatDau }}</div>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold small text-uppercase">Ngày kết thúc <span
                                            class="text-danger">*</span></label>
                                    <input v-model="formData.ngayKetThuc" type="date"
                                        class="form-control custom-input shadow-none"
                                        :class="{ 'is-invalid': errors.ngayKetThuc }" :disabled="isReadOnly">
                                    <div class="invalid-feedback">{{ errors.ngayKetThuc }}</div>
                                </div>
                            </div>

                            <div class="mb-3">
                                <label class="form-label fw-bold small text-uppercase">Mô tả chương trình</label>
                                <textarea v-model="formData.moTa" class="form-control custom-input shadow-none" rows="5"
                                    :disabled="isReadOnly"
                                    placeholder="Mô tả ngắn gọn về chương trình khuyến mãi này..."></textarea>
                            </div>
                        </div>

                        <div class="col-md-5 p-4 bg-light-soft">
                            <h5 class="mb-4 text-primary-red d-flex align-items-center">
                                <i class="fas fa-utensils me-2"></i> Sản phẩm áp dụng
                            </h5>

                            <div class="product-selector-card mb-4">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <label class="fw-bold small text-secondary">HÀNG HÓA / SET LẨU</label>
                                    <div v-if="!isReadOnly" class="form-check small">
                                        <input class="form-check-input" type="checkbox" id="selectAll"
                                            :checked="isAllSelected" @change="toggleSelectAll">
                                        <label class="form-check-label text-primary" for="selectAll"
                                            style="cursor:pointer">Chọn tất cả</label>
                                    </div>
                                </div>
                                <div class="input-group input-group-sm mb-2 shadow-sm">
                                    <span class="input-group-text bg-white border-end-0"><i
                                            class="fas fa-search text-muted"></i></span>
                                    <input v-model="searchSetLau" type="text"
                                        class="form-control border-start-0 shadow-none" placeholder="Tìm set lẩu..."
                                        :disabled="isReadOnly">
                                </div>
                                <div class="selector-box custom-scrollbar border rounded-3 bg-white"
                                    style="height: 150px; overflow-y: auto;">
                                    <div v-for="set in filteredSetLau" :key="set.id"
                                        class="item-row px-3 py-2 border-bottom">
                                        <div class="form-check">
                                            <input class="form-check-input me-2" type="checkbox" :id="'set-' + set.id"
                                                :value="set.id" v-model="formData.idSetLauChiTiet"
                                                :disabled="isReadOnly">
                                            <label :for="'set-' + set.id"
                                                class="form-check-label d-flex justify-content-between w-100">
                                                <span class="text-dark">{{ set.tenSetLau }}</span>
                                                <span class="text-danger fw-bold">{{ formatPrice(set.giaBan) }}</span>
                                            </label>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="product-selector-card">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <label class="fw-bold small text-secondary">MÓN ĂN LẺ ĐI KÈM</label>
                                    <div v-if="!isReadOnly" class="form-check small">
                                        <input class="form-check-input" type="checkbox" id="selectAllMonAn"
                                            :checked="isAllMonAnSelected" @change="toggleSelectAllMonAn">
                                        <label class="form-check-label text-primary" for="selectAllMonAn"
                                            style="cursor:pointer">Chọn tất cả</label>
                                    </div>
                                </div>
                                <div class="input-group input-group-sm mb-2 shadow-sm">
                                    <span class="input-group-text bg-white border-end-0"><i
                                            class="fas fa-search text-muted"></i></span>
                                    <input v-model="searchMonAn" type="text"
                                        class="form-control border-start-0 shadow-none" placeholder="Tìm món lẻ..."
                                        :disabled="isReadOnly">
                                </div>
                                <div class="selector-box custom-scrollbar border rounded-3 bg-white"
                                    style="height: 150px; overflow-y: auto;">
                                    <div v-for="mon in filteredMonAn" :key="mon.id"
                                        class="item-row px-3 py-2 border-bottom">
                                        <div class="form-check">
                                            <input class="form-check-input me-2" type="checkbox" :id="'mon-' + mon.id"
                                                :value="mon.id" v-model="formData.idMonAnChiTiet"
                                                :disabled="isReadOnly">
                                            <label :for="'mon-' + mon.id"
                                                class="form-check-label d-flex justify-content-between w-100">
                                                <span class="text-dark">{{ mon.tenMonAn }}</span>
                                                <span class="text-danger fw-bold">{{ formatPrice(mon.giaBan) }}</span>
                                            </label>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="card-footer bg-white border-top p-4 d-flex justify-content-end gap-3">
                        <button type="button" class="btn btn-light px-4 py-2 border text-secondary fw-bold"
                            @click="closeForm">HỦY BỎ</button>
                        <button v-if="!isReadOnly" type="submit" class="btn btn-red-dark px-5 py-2 fw-bold shadow-sm">
                            <i class="fas fa-save me-2"></i> LƯU DỮ LIỆU
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';
import { usePromotionLogic } from './promotionFunction.js';
import promotionService from '@/services/promotionService';
import '../promotionStyle.css';

// --- STATE ---
const { getStatusDisplay, fetchData } = usePromotionLogic();
const listMonAnDiKem = ref([]);
const searchMonAn = ref('');
const isFormActive = ref(false);
const isReadOnly = ref(false);
const listKhuyenMai = ref([]);
const selectedId = ref(null);
const listSetLau = ref([]);
const searchSetLau = ref('');
const toasts = ref([]);

const errors = reactive({
    tenDotKhuyenMai: '',
    phanTramGiam: '',
    ngayBatDau: '',
    ngayKetThuc: '',
    idSetLauChiTiet: ''
});

const formData = reactive({
    tenDotKhuyenMai: '',
    phanTramGiam: 0,
    ngayBatDau: '',
    ngayKetThuc: '',
    moTa: '',
    idSetLauChiTiet: [],
    idMonAnChiTiet: []
});

const filters = reactive({ keyword: '', trangThai: null, ngayBatDau: '', ngayKetThuc: '' });
const pagination = reactive({ currentPage: 1, pageSize: 5, totalPages: 0 });

// --- COMPUTED ---
const formTitle = computed(() => {
    if (isReadOnly.value) return 'Chi tiết đợt khuyến mãi';
    return selectedId.value ? 'Chỉnh sửa đợt khuyến mãi' : 'Thêm mới đợt khuyến mãi';
});

// const filteredMonAn = computed(() => {
//     if (!searchMonAn.value) return listMonAnDiKem.value;
//     return listMonAnDiKem.value.filter(m =>
//         m.tenMonAn.toLowerCase().includes(searchMonAn.value.toLowerCase())
//     );
// });

const filteredSetLau = computed(() => {
    if (!searchSetLau.value) return listSetLau.value;
    return listSetLau.value.filter(s => s.tenSetLau.toLowerCase().includes(searchSetLau.value.toLowerCase()));
});

const isAllSelected = computed(() => {
    if (filteredSetLau.value.length === 0) return false;
    return filteredSetLau.value.every(s => formData.idSetLauChiTiet.includes(s.id));
});

// --- TOAST LOGIC ---
const showToast = (title, message, type = 'success') => {
    const id = Date.now();
    toasts.value.push({ id, title, message, type });
    setTimeout(() => removeToast(id), 4000);
};
const removeToast = (id) => { toasts.value = toasts.value.filter(t => t.id !== id); };
const monAnList = ref([])
const isAllMonAnSelected = computed(() => {
    if (filteredMonAn.value.length === 0) return false;
    return filteredMonAn.value.every(m =>
        formData.idMonAnChiTiet.includes(m.id)
    );
});

// --- ACTIONS ---
const toggleSelectAllMonAn = (e) => {
    const isChecked = e.target.checked;
    const currentIds = filteredMonAn.value.map(m => m.id);
    if (isChecked) {
        const newIds = currentIds.filter(id => !formData.idMonAnChiTiet.includes(id));
        formData.idMonAnChiTiet.push(...newIds);
    } else {
        formData.idMonAnChiTiet = formData.idMonAnChiTiet.filter(id => !currentIds.includes(id));
    }
};

const toggleSelectAll = (e) => {
    const isChecked = e.target.checked;
    const currentIds = filteredSetLau.value.map(s => s.id);
    if (isChecked) {
        const newIds = currentIds.filter(id => !formData.idSetLauChiTiet.includes(id));
        formData.idSetLauChiTiet.push(...newIds);
    } else {
        formData.idSetLauChiTiet = formData.idSetLauChiTiet.filter(id => !currentIds.includes(id));
    }
};

const isExpired = (ngayKetThuc) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const end = new Date(ngayKetThuc);
    end.setHours(0, 0, 0, 0);
    return end < today;
};

const validateForm = () => {
    let isValid = true;
    Object.keys(errors).forEach(k => errors[k] = '');
    formData.tenDotKhuyenMai = formData.tenDotKhuyenMai?.trim() || '';
    const start = formData.ngayBatDau ? new Date(formData.ngayBatDau) : null;
    const end = formData.ngayKetThuc ? new Date(formData.ngayKetThuc) : null;

    if (!formData.tenDotKhuyenMai) { errors.tenDotKhuyenMai = "Tên khuyến mãi không được bỏ trống!"; isValid = false; }
    if (formData.phanTramGiam <= 0 || formData.phanTramGiam > 100) { errors.phanTramGiam = "Phần trăm giảm phải từ 1 đến 100!"; isValid = false; }
    if (!formData.ngayBatDau) { errors.ngayBatDau = "Vui lòng chọn ngày bắt đầu!"; isValid = false; }
    if (!formData.ngayKetThuc) { errors.ngayKetThuc = "Vui lòng chọn ngày kết thúc!"; isValid = false; }
    else if (start && end < start) { errors.ngayKetThuc = "Ngày kết thúc không được nhỏ hơn ngày bắt đầu!"; isValid = false; }

    if (formData.idSetLauChiTiet.length === 0 && formData.idMonAnChiTiet.length === 0) {
        showToast("Lỗi nhập liệu", "Bạn chưa chọn sản phẩm nào cho đợt khuyến mãi này.", "error");
        isValid = false;
    }
    return isValid;
};

const handleSearch = async () => {
    let apiFilters = { ...filters };
    let currentPagination = { ...pagination };

    // Nếu lọc "Hết hạn", ta ép Server trả về số lượng lớn để dồn dữ liệu
    if (filters.trangThai === 2) {
        apiFilters.trangThai = null;
        currentPagination.pageSize = 1000; // Lấy số lượng lớn để không bị chia trang ở Server
        currentPagination.currentPage = 1;
    }

    const data = await fetchData(apiFilters, currentPagination);
    let results = data.content || [];
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // Bắt đầu lọc logic
    if (filters.trangThai === 1) { // Đang hoạt động
        results = results.filter(km => {
            const endDate = new Date(km.ngayKetThuc);
            endDate.setHours(0, 0, 0, 0);
            return km.trangThai === 1 && endDate >= today;
        });
    }
    else if (filters.trangThai === 2) { // Hết hạn
        results = results.filter(km => {
            const endDate = new Date(km.ngayKetThuc);
            endDate.setHours(0, 0, 0, 0);
            return endDate < today;
        });
        // Cập nhật lại tổng trang dựa trên số lượng sau khi lọc
        pagination.totalPages = Math.ceil(results.length / pagination.pageSize);
    } else {
        pagination.totalPages = data.totalPages || 0;
    }

    listKhuyenMai.value = results;
};

const submitForm = async () => {
    if (!validateForm()) return;
    Swal.fire({
        title: 'Xác nhận lưu?',
        text: `Bạn có chắc chắn muốn ${selectedId.value ? 'cập nhật' : 'tạo mới'} đợt khuyến mãi này không?`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#7B121C',
        confirmButtonText: 'Có, lưu ngay!',
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                if (selectedId.value) {
                    await promotionService.update(selectedId.value, formData);
                    showToast("Cập nhật thành công", "Thông tin khuyến mãi đã được cập nhật.");
                } else {
                    await promotionService.create(formData);
                    showToast("Thêm mới thành công", "Đợt khuyến mãi đã được tạo.");
                }
                closeForm();
                handleSearch();
            } catch (e) {
                showToast("Lỗi máy chủ", "Không thể thực hiện thao tác này.", "error");
            }
        }
    });
};

const handleToggleStatus = async (km) => {
    const originalStatus = km.trangThai;
    Swal.fire({
        title: 'Thay đổi trạng thái?',
        text: `Xác nhận ${originalStatus === 1 ? 'ngừng' : 'kích hoạt'} đợt khuyến mãi này?`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#7B121C',
    }).then(async (result) => {
        if (result.isConfirmed) {
            try {
                await promotionService.toggleStatus(km.id, originalStatus);
                showToast("Thành công", "Trạng thái đã được cập nhật.");
                handleSearch();
            } catch (e) {
                showToast("Lỗi", "Không thể thay đổi trạng thái!", "error");
                handleSearch();
            }
        } else {
            handleSearch();
        }
    });
};

const fetchListSetLau = async () => {
    try {
        const res = await axios.get('http://localhost:8080/api/set-lau/active');
        listSetLau.value = res.data;
    } catch (e) { console.error(e); }
};

const fetchListMonAn = async () => {
    try {
        const res = await axios.get('http://localhost:8080/api/mon-an-di-kem/active');
        listMonAnDiKem.value = res.data || [];
    } catch (e) { console.error(e); }
};

const loadDataToForm = async (id) => {
    try {
        const data = await promotionService.getById(id);

        // 1. Gán các thông tin cơ bản
        Object.assign(formData, data);

        // 2. Format lại ngày tháng (cắt lấy YYYY-MM-DD)
        if (data.ngayBatDau) formData.ngayBatDau = data.ngayBatDau.substring(0, 10);
        if (data.ngayKetThuc) formData.ngayKetThuc = data.ngayKetThuc.substring(0, 10);

        // 3. Map ID sản phẩm (Lưu ý: Kiểm tra lại tên field setLauChiTiets/monAnChiTiets từ API)
        // Nếu API trả về list object, ta phải lấy .id của từng object đó
        // Tìm trong hàm loadDataToForm và sửa 2 chỗ map:
        if (data.setLauChiTiets && Array.isArray(data.setLauChiTiets)) {
            formData.idSetLauChiTiet = data.setLauChiTiets.map(item => Number(item.id)); // Ép kiểu Number
        }

        if (data.monAnChiTiets && Array.isArray(data.monAnChiTiets)) {
            formData.idMonAnChiTiet = data.monAnChiTiets.map(item => Number(item.id)); // Ép kiểu Number
        }

        console.log("Dữ liệu đã map vào Form:", formData); // Debug để kiểm tra
    } catch (e) {
        showToast("Lỗi", "Không thể tải dữ liệu chi tiết!", "error");
        console.error("Chi tiết lỗi load form:", e);
    }
};
const filteredMonAn = computed(() => {
    if (!Array.isArray(listMonAnDiKem.value)) return [];

    let result = listMonAnDiKem.value.filter(m => m.trangThai === 1);

    if (searchMonAn.value) {
        result = result.filter(m =>
            m.tenMonAn?.toLowerCase().includes(searchMonAn.value.toLowerCase())
        );
    }

    return result;
});

const openFormAdd = () => {
    isReadOnly.value = false;
    selectedId.value = null;
    Object.assign(formData, {
        tenDotKhuyenMai: '', phanTramGiam: 0, ngayBatDau: '', ngayKetThuc: '', moTa: '', idSetLauChiTiet: [], idMonAnChiTiet: []
    });
    Object.keys(errors).forEach(k => errors[k] = '');
    isFormActive.value = true;
};

const openFormEdit = async (id) => { 
    isReadOnly.value = false; 
    selectedId.value = id; 
    await loadDataToForm(id); 
    
    // Thêm dòng này để xóa sạch các thông báo lỗi cũ từ lần trước hoặc do logic map gây ra
    Object.keys(errors).forEach(k => errors[k] = ''); 
    
    isFormActive.value = true; 
};
const openFormView = async (id) => { isReadOnly.value = true; selectedId.value = id; await loadDataToForm(id); isFormActive.value = true; };
const closeForm = () => { isFormActive.value = false; selectedId.value = null; };
const onSearchInput = () => { clearTimeout(window.dt_timer); window.dt_timer = setTimeout(() => { pagination.currentPage = 1; handleSearch(); }, 500); };
const changePage = (p) => { if (p >= 1 && p <= pagination.totalPages) { pagination.currentPage = p; handleSearch(); } };
const resetFilters = () => { Object.assign(filters, { keyword: '', trangThai: null, ngayBatDau: '', ngayKetThuc: '' }); handleSearch(); };
const formatPrice = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0);

const calculateStatus = (km) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const endDate = new Date(km.ngayKetThuc);
    endDate.setHours(0, 0, 0, 0);
    if (km.trangThai === 0) return { text: 'Ngừng hoạt động', class: 'badge bg-secondary' };
    if (endDate < today) return { text: 'Đã hết hạn', class: 'badge bg-danger' };
    return { text: 'Đang hoạt động', class: 'badge bg-success' };
};

onMounted(async () => { // Thêm async vào đây
    await handleSearch();
    await fetchListSetLau();
    await fetchListMonAn();
});
</script>
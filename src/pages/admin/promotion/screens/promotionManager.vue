<template>
    <div class=" promotion-manager-wrapper">
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

        <div v-if="confirmModal.show" class="modal-overlay" @click.self="confirmModal.show = false">
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
                <h2 class="title-page">Quản lý đợt giảm giá</h2>
            </div>


            <div class="filter-card mb-4">
                <div class="row g-3 align-items-end">
                    <div class="col-md-8">
                        <label class="filter-label">Tìm kiếm</label>
                        <input v-model="filters.keyword" class="form-control custom-input"
                            placeholder="Nhập mã hoặc tên để tìm..." @input="onSearchInput">
                    </div>
                    <div class="col-md-4">
                        <label class="filter-label">Trạng thái</label>
                        <Multiselect
    v-model="filters.trangThai"
    :options="statusOptions"
    label="label"
    track-by="value"
    placeholder="Chọn trạng thái"
    :multiple="false"
    :close-on-select="true"
    :clear-on-select="true"
    @select="handleSearch"
/>
                    </div>
                    <div class="col-md-3">
                        <label class="filter-label">Từ ngày</label>
                        <input type="date" v-model="filters.ngayBatDau" class="form-control custom-input"
                            @change="onFilterChange" />
                    </div>

                    <div class="col-md-3">
                        <label class="filter-label">Đến ngày</label>
                        <input type="date" v-model="filters.ngayKetThuc" class="form-control custom-input"
                            @change="onFilterChange" />
                    </div>

                    <!-- <div class="col-md-3">
                        <label class="filter-label">
                            Giảm giá:
                            <span class="percent-text">
                                {{ filters.minPercent }}% – {{ filters.maxPercent }}%
                            </span>
                        </label>

                        <div class="range-slider">
                            <input type="range" min="0" max="100" step="5" v-model="filters.minPercent"
                                @input="handlePercentChange" />

                            <input type="range" min="0" max="100" step="5" v-model="filters.maxPercent"
                                @input="handlePercentChange" />
                        </div>

                    </div> -->

                    <div class="col-md-3">
                        <label class="filter-label d-flex justify-content-between">
                            <span>Giảm giá:</span>
                            <span class="percent-text fw-bold">
                                {{ filters.minPercent }}% – {{ filters.maxPercent }}%
                            </span>
                        </label>

                        <div class="range-slider-wrapper">
                            <div class="slider-track-bg"></div>

                            <div class="slider-track-active" :style="sliderTrackStyle"></div>

                            <input type="range" min="0" max="100" step="5" v-model.number="filters.minPercent"
                                @input="handlePercentChange" class="slider-input" />

                            <input type="range" min="0" max="100" step="5" v-model.number="filters.maxPercent"
                                @input="handlePercentChange" class="slider-input" />
                        </div>
                    </div>

                    <div class="col-md-3 d-flex">
                        <button class="btn-reset-filter" @click="resetFilters">
                            Xóa bộ lọc
                        </button>
                    </div>
                </div>
            </div>

            <div class="d-flex justify-content-end mb-3 gap-2">
                <button class="btn-red-dark d-flex align-items-center justify-content-center" @click="exportExcel">
                    <i class="fas fa-file-excel"></i>
                </button>

                <button class="btn-red-dark d-flex align-items-center justify-content-center" @click="openFormAdd">
                    <i class="fas fa-plus"></i>
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
                        <!-- ✅ Có dữ liệu -->
                        <tr v-for="(km, index) in listKhuyenMai" :key="km.id">
                            <td>{{ (pagination.currentPage - 1) * pagination.pageSize + index + 1 }}</td>
                            <td class=" text-dark">{{ km.maDotKhuyenMai }}</td>
                            <td>{{ km.tenDotKhuyenMai }}</td>
                            <td class="text-center ">{{ km.phanTramGiam }}%</td>
                            <td>
                                <small>
                                    {{ km.ngayBatDau }}
                                    <i class="fas fa-arrow-right mx-1"></i>
                                    {{ km.ngayKetThuc }}
                                </small>
                            </td>
                            <td>
                                <span :class="calculateStatus(km).class">
                                    {{ calculateStatus(km).text }}
                                </span>
                            </td>
                            <td class="text-center">
                                <div class="action-group">
                                    <div class="icon-tooltip">
                                        <i class="fas fa-eye view-icon" @click="openFormView(km.id)"></i>
                                        <span class="tooltip-text">Xem chi tiết</span>
                                    </div>
                                    <div class="icon-tooltip">
                                        <i class="fas fa-pen edit-icon"
                                            :class="{ 'text-muted disabled-icon': isExpired(km.ngayKetThuc) }"
                                            @click="!isExpired(km.ngayKetThuc) && openFormEdit(km.id)"></i>
                                        <span class="tooltip-text">Chỉnh sửa</span>
                                    </div>
                                    <div class="icon-tooltip d-inline-block">
                                        <div class="form-check form-switch mb-0">
                                            <input class="form-check-input custom-red-checkbox custom-red-switch"
                                                type="checkbox" :checked="km.trangThai === 1"
                                                :disabled="isExpired(km.ngayKetThuc)"
                                                @click.prevent="!isExpired(km.ngayKetThuc) && handleToggleStatus(km)">
                                        </div>

                                        <span class="tooltip-text">
                                            {{ isExpired(km.ngayKetThuc)
                                                ? 'Giảm giá đã hết hạn'
                                                : 'Bật / Tắt giảm giá' }}
                                        </span>
                                    </div>
                                </div>
                            </td>
                        </tr>
                        <tr v-if="listKhuyenMai.length === 0">
                            <td colspan="7" class="text-center text-muted py-4">
                                <i class="fas fa-search me-2"></i>
                                Không tìm thấy dữ liệu phù hợp
                            </td>
                        </tr>
                    </tbody>
                </table>

                <div class="pagination-wrapper d-flex justify-content-between align-items-center mt-4 px-2">

                    <div class="d-flex align-items-center">
                        <span class="me-2 text-muted">Hiển thị</span>
                        <select v-model="pagination.pageSize"
                            @change="() => { pagination.currentPage = 1; handleSearch(); }">
                            <option :value="5">5 dòng</option>
                            <option :value="8">8 dòng</option>
                            <option :value="10">10 dòng</option>
                            <option :value="20">20 dòng</option>
                        </select>
                    </div>

                    <div class="pagination-controls d-flex align-items-center">
                        <button class="btn-page" :disabled="pagination.currentPage === 1" @click="goToPage(1)">
                            <i class="fas fa-step-backward"></i>
                        </button>

                        <button class="btn-page" :disabled="pagination.currentPage === 1" @click="changePage(-1)">
                            <i class="fas fa-chevron-left"></i>
                        </button>

                        <div class="mx-3 d-flex align-items-center">
                            <template v-if="pagination.totalPages <= 5">
                                <span v-for="p in pagination.totalPages" :key="p" class="page-number"
                                    :class="{ active: pagination.currentPage === p }" @click="goToPage(p)">
                                    {{ p }}
                                </span>
                            </template>

                            <template v-else>
                                <div class="input-page-wrapper d-flex align-items-center">
                                    <input type="number" v-model.number="inputPage" class="input-go-to"
                                        @keyup.enter="jumpToPage">
                                    <span class="ms-2 text-muted">
                                        / {{ pagination.totalPages }}
                                    </span>
                                </div>
                            </template>
                        </div>

                        <button class="btn-page" :disabled="pagination.currentPage === pagination.totalPages"
                            @click="changePage(1)">
                            <i class="fas fa-chevron-right"></i>
                        </button>

                        <button class="btn-page" :disabled="pagination.currentPage === pagination.totalPages"
                            @click="goToPage(pagination.totalPages)">
                            <i class="fas fa-step-forward"></i>
                        </button>
                    </div>

                    <div class="total-info text-muted">
                        Hiển thị {{ listKhuyenMai.length }} /
                        {{ pagination.totalElements }} đợt giảm giá
                    </div>

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
                                <i class="fas fa-info-circle me-2"></i> Thông tin đợt giảm giá
                            </h5>

                            <div class="mb-3">
                                <label class="form-label fw-bold small ">Tên đợt giảm giá <span
                                        class="text-danger">*</span></label>
                                <input v-model="formData.tenDotKhuyenMai" type="text"
                                    class="form-control custom-input shadow-none"
                                    :class="{ 'is-invalid': errors.tenDotKhuyenMai }" :disabled="isReadOnly"
                                    placeholder="Ví dụ: Khuyến mãi Tết Nguyên Đán">
                                <div class="invalid-feedback">{{ errors.tenDotKhuyenMai }}</div>
                            </div>

                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold small ">Phần trăm giảm (%) <span
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
                                    <label class="form-label fw-bold small ">Ngày bắt đầu <span
                                            class="text-danger">*</span></label>
                                    <input v-model="formData.ngayBatDau" type="date"
                                        class="form-control custom-input shadow-none"
                                        :class="{ 'is-invalid': errors.ngayBatDau }" :disabled="isReadOnly">
                                    <div class="invalid-feedback">{{ errors.ngayBatDau }}</div>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label class="form-label fw-bold small ">Ngày kết thúc <span
                                            class="text-danger">*</span></label>
                                    <input v-model="formData.ngayKetThuc" type="date"
                                        class="form-control custom-input shadow-none"
                                        :class="{ 'is-invalid': errors.ngayKetThuc }" :disabled="isReadOnly">
                                    <div class="invalid-feedback">{{ errors.ngayKetThuc }}</div>
                                </div>
                            </div>

                            <div class="mb-3">
                                <label class="form-label fw-bold small ">Mô tả chương trình</label>
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
                                    <label class="fw-bold small text-secondary">Hàng hóa / Set lẩu</label>
                                    <div v-if="!isReadOnly" class="form-check small">
                                        <input class="form-check-input custom-red-checkbox" type="checkbox"
                                            id="selectAll" :checked="isAllSelected" @change="toggleSelectAll">
                                        <label class="form-check-label custom-red-checkbox" for="selectAll"
                                            style="cursor:pointer">Chọn
                                            tất cả</label>
                                    </div>
                                </div>
                                <div class="d-flex gap-2 mb-2">

                                    <select v-model="filterGia" class="form-select form-select-sm">
                                        <option value="">Giá</option>
                                        <option value="1">Dưới 50k</option>
                                        <option value="2">50k - 100k</option>
                                        <option value="3">Trên 100k</option>
                                    </select>

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
                                        class="item-row px-3 py-2 border-bottom d-flex align-items-center gap-2">

                                        <!-- Ảnh -->
                                        <!-- <img :src="getImageUrl(set.hinhAnh)" class="product-thumb" /> -->

                                        <div class="flex-grow-1">
                                            <div class="form-check">
                                                <input class="form-check-input custom-red-checkbox me-2" type="checkbox"
                                                    :id="'set-' + set.id" :value="set.id"
                                                    v-model="formData.idSetLauChiTiet" :disabled="isReadOnly" />

                                                <label :for="'set-' + set.id"
                                                    class="form-check-label d-flex justify-content-between w-100">
                                                    <span>{{ set.tenSetLau }}</span>
                                                    <span class="text-danger fw-bold">
                                                        {{ formatPrice(set.giaBan) }}
                                                    </span>
                                                </label>
                                            </div>
                                        </div>
                                    </div>

                                </div>
                            </div>

                            <div class="product-selector-card">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <label class="fw-bold small text-secondary">MÓN ĂN LẺ ĐI KÈM</label>
                                    <div v-if="!isReadOnly" class="form-check small">
                                        <input class="form-check-input custom-red-checkbox" type="checkbox"
                                            id="selectAllMonAn" :checked="isAllMonAnSelected"
                                            @change="toggleSelectAllMonAn">
                                        <label class="form-check-label custom-red-checkbox" for="selectAllMonAn"
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
                                        class="item-row px-3 py-2 border-bottom d-flex align-items-center gap-2">

                                        <!-- Ảnh -->
                                        <!-- <img :src="getImageUrl(mon.hinhAnh)" class="product-thumb" /> -->

                                        <div class="flex-grow-1">
                                            <div class="form-check">
                                                <input class="form-check-input custom-red-checkbox me-2" type="checkbox"
                                                    :id="'mon-' + mon.id" :value="mon.id"
                                                    v-model="formData.idMonAnChiTiet" :disabled="isReadOnly" />

                                                <label :for="'mon-' + mon.id"
                                                    class="form-check-label d-flex justify-content-between w-100">
                                                    <span>{{ mon.tenMonAn }}</span>
                                                    <span class="text-danger fw-bold">
                                                        {{ formatPrice(mon.giaBan) }}
                                                    </span>
                                                </label>
                                            </div>
                                        </div>
                                    </div>

                                </div>
                            </div>
                        </div>
                    </div>

                    <div v-if="selectedProducts.length"
                        class="selected-product-card mt-4 animate__animated animate__fadeIn">

                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <h6 class="fw-bold text-primary-red mb-0">
                                <i class="fas fa-list-check me-2"></i> Sản phẩm đã chọn
                            </h6>
                            <button v-if="!isReadOnly" class="btn btn-sm btn-outline-danger" @click="clearAllSelected">
                                <i class="fas fa-trash me-1"></i> Clear
                            </button>
                        </div>

                        <div class="table-responsive">
                            <table class="table table-sm align-middle mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th>#</th>
                                        <th>Ảnh</th> <!-- ✅ THÊM -->
                                        <th>Tên</th>
                                        <th>Loại</th>
                                        <th class="text-end">Giá sau KM</th>
                                        <th class="text-end ">Giá sau KM</th>
                                        <th v-if="!isReadOnly" class="text-center">Xóa</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr v-for="(item, index) in selectedProducts" :key="item.type + item.id">
                                        <td>{{ index + 1 }}</td>
                                        <img :src="getImageUrl(item.hinhAnh)"
                                            style="width:50px;height:50px;object-fit:cover;border-radius:6px" />


                                        <td class="fw-semibold">{{ item.ten }}</td>
                                        <td>
                                            <span :class="item.type === 'SET'
                                                ? 'badge bg-warning text-dark'
                                                : 'badge bg-info'">
                                                {{ item.type }}
                                            </span>
                                        </td>
                                        <!-- Giá gốc -->
                                        <td class="text-end text-muted text-decoration-line-through">
                                            {{ formatPrice(item.gia) }}
                                        </td>

                                        <!-- Giá sau KM -->
                                        <td class="text-end text-success fw-bold">
                                            {{ formatPrice(getDiscountedPrice(item.gia)) }}
                                        </td>

                                        <td v-if="!isReadOnly" class="text-center">
                                            <button class="btn btn-sm btn-light" @click="removeSelectedItem(item)">
                                                ❌
                                            </button>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>


                    <div class="card-footer bg-white border-top p-4 d-flex justify-content-end gap-3">
                        <div class="card-footer under-nav bg-white border-top p-4 d-flex gap-3"
                            style="align-items: end;">
                            <button type="button"
                                class="btn btn-cancel btn-light px-4 border text-secondary fw-bold d-flex align-items-center justify-content-center"
                                style="height: 42px; color: white;background-color: #800000;" @click="closeForm">
                                HỦY BỎ
                            </button>

                            <button v-if="!isReadOnly" type="submit"
                                class="btn btn-red-dark px-5 fw-bold shadow-sm d-flex align-items-center justify-content-center">
                                <i class="fas fa-save me-2"></i> LƯU DỮ LIỆU
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';
import { usePromotionLogic } from './promotionFunction.js';
import promotionService from '@/services/promotionService';
import '../promotionStyle.css';
import Multiselect from 'vue-multiselect'
import 'vue-multiselect/dist/vue-multiselect.min.css'

const statusOptions = [
    { value: null, label: 'Tất cả trạng thái' },
    { value: 1, label: 'Đang hoạt động' },
    { value: 0, label: 'Ngừng hoạt động' },
    { value: 2, label: 'Đã hết hạn' }
]
const exportExcel = async () => {
    try {
        const response = await axiosClient.get(
            '/dot-khuyen-mai/export-excel',
            {
                params: {
                    keyword: filters.keyword,
                    trangThai: filters.trangThai,
                    ngayBatDau: filters.ngayBatDau,
                    ngayKetThuc: filters.ngayKetThuc
                },
                responseType: 'blob' // ⚠️ BẮT BUỘC
            }
        );

        const blob = new Blob([response.data], {
            type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        });

        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'danh_sach_dot_khuyen_mai.xlsx';
        document.body.appendChild(link);
        link.click();
        link.remove();

        showToast('Thành công', 'Xuất file Excel thành công!');
    } catch (error) {
        console.error(error);
        showToast('Lỗi', 'Xuất file Excel thất bại!', 'error');
    }
};

const onFilterChange = () => {
    pagination.currentPage = 1;
    handleSearch();
};
const onSearchInput = () => {
    clearTimeout(window.dt_timer);
    window.dt_timer = setTimeout(() => {
        pagination.currentPage = 1; // ✅ đã reset
        handleSearch();
    }, 500);
};

const inputPage = ref(1);
const pagination = reactive({
    currentPage: 1,
    pageSize: 8,
    totalPages: 0,
    totalElements: 0
});
import axiosClient from '@/services/axiosClient.js';

// --- STATE ---
const { getStatusDisplay, fetchData } = usePromotionLogic();
const listMonAnDiKem = ref([]);
const searchMonAn = ref('');
const isFormActive = ref(false);
const isReadOnly = ref(false);
const listKhuyenMai = ref([]);
const filteredKhuyenMai = ref([]);
const selectedId = ref(null);
const listSetLau = ref([]);
const searchSetLau = ref('');
const filterLoai = ref('');
const filterGia = ref('');

const toasts = ref([]);
const confirmModal = reactive({
    show: false,
    title: '',
    message: '',
    onConfirm: null
});
const openConfirm = (title, message, callback) => {
    confirmModal.title = title;
    confirmModal.message = message;
    confirmModal.onConfirm = callback;
    confirmModal.show = true;
};
const formatDateForApi = (dateStr) => {
    if (!dateStr) return null;
    // đảm bảo YYYY-MM-DD
    return new Date(dateStr).toISOString().slice(0, 10);
};
const getMaxDiscount = (productId) => {
    const today = new Date();

    const validPromos = listKhuyenMai.value.filter(km => {
        const start = new Date(km.ngayBatDau);
        const end = new Date(km.ngayKetThuc);

        if (today < start || today > end) return false;

        return (
            km.setLauIds?.includes(productId) ||
            km.monAnIds?.includes(productId)
        );
    });

    if (!validPromos.length) return 0;

    return Math.max(...validPromos.map(km => km.phanTramGiam));
};

const getDiscountedPrice = (price, productId) => {
    const percent = getMaxDiscount(productId);
    return Math.round(price * (100 - percent) / 100);
};


const executeConfirm = async () => {
    if (typeof confirmModal.onConfirm === 'function') {
        await confirmModal.onConfirm();
    }
    confirmModal.show = false;
    confirmModal.onConfirm = null;
};

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

const filters = reactive({
    keyword: '',
    trangThai: null,
    ngayBatDau: '',
    ngayKetThuc: '',
    minPercent: 0,
    maxPercent: 100
});
const handlePercentChange = () => {
    if (filters.minPercent > filters.maxPercent) {
        const temp = filters.minPercent;
        filters.minPercent = filters.maxPercent;
        filters.maxPercent = temp;
    }
    handleSearch();
};

// --- COMPUTED ---
const formTitle = computed(() => {
    if (isReadOnly.value) return 'Chi tiết khuyến mãi thực đơn';
    return selectedId.value ? 'Chỉnh sửa khuyến mãi thực đơn' : 'Thêm mới khuyến mãi thực đơn';
});

// const filteredMonAn = computed(() => {
//     if (!searchMonAn.value) return listMonAnDiKem.value;
//     return listMonAnDiKem.value.filter(m =>
//         m.tenMonAn.toLowerCase().includes(searchMonAn.value.toLowerCase())
//     );
// });

const filteredSetLau = computed(() => {
    let result = listSetLau.value;

    // Tìm tên
    if (searchSetLau.value) {
        result = result.filter(s =>
            s.tenSetLau
                .toLowerCase()
                .includes(searchSetLau.value.toLowerCase())
        );
    }

    // Lọc giá
    if (filterGia.value === '1') {
        result = result.filter(s => s.giaBan < 50000);
    }

    if (filterGia.value === '2') {
        result = result.filter(s =>
            s.giaBan >= 50000 && s.giaBan <= 100000
        );
    }

    if (filterGia.value === '3') {
        result = result.filter(s => s.giaBan > 100000);
    }

    return result;
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
const isDateOverlap = (start1, end1, start2, end2) => {
    return (
        new Date(start1) <= new Date(end2) &&
        new Date(end1) >= new Date(start2)
    );
};

const isExpired = (ngayKetThuc) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const end = new Date(ngayKetThuc);
    end.setHours(0, 0, 0, 0);
    return end < today;
};
const getImageUrl = (img) => {
    if (!img) return '/no-image.png';

    // Nếu là base64 → trả thẳng
    if (img.startsWith('data:image')) {
        return img;
    }

    // Nếu là file → ghép link
    return `http://localhost:8080/uploads/${img}`;
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
    const apiFilters = {
    keyword: filters.keyword,
    trangThai: filters.trangThai ? filters.trangThai.value : null,
    ngayBatDau: formatDateForApi(filters.ngayBatDau),
    ngayKetThuc: formatDateForApi(filters.ngayKetThuc)
};

    // 🔥 LẤY TOÀN BỘ DATA (KHÔNG PHÂN TRANG)
    const data = await fetchData(apiFilters, {
        currentPage: 1,
        pageSize: 10000 // đủ lớn
    });

    let results = data.content || [];

    // ===== LỌC % GIẢM =====
    results = results.filter(km =>
        km.phanTramGiam >= filters.minPercent &&
        km.phanTramGiam <= filters.maxPercent
    );

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // // ===== LỌC TRẠNG THÁI =====
    // if (filters.trangThai === 1) {
    //     results = results.filter(km => {
    //         const end = new Date(km.ngayKetThuc);
    //         end.setHours(0, 0, 0, 0);
    //         return km.trangThai === 1 && end >= today;
    //     });
    // }
    // else if (filters.trangThai === 0) {
    //     results = results.filter(km => km.trangThai === 0);
    // }
    // else if (filters.trangThai === 2) {
    //     results = results.filter(km => {
    //         const end = new Date(km.ngayKetThuc);
    //         end.setHours(0, 0, 0, 0);
    //         return end < today;
    //     });
    // }

    if (filters.trangThai && filters.trangThai.value !== null) {

    const today = new Date();
    today.setHours(0,0,0,0);

    results = results.filter(km => {

        const end = new Date(km.ngayKetThuc);
        end.setHours(0,0,0,0);

        // Đang hoạt động
        if (filters.trangThai.value === 1) {
            return km.trangThai === 1 && end >= today;
        }

        // Ngừng
        if (filters.trangThai.value === 0) {
            return km.trangThai === 0;
        }

        // Hết hạn
        if (filters.trangThai.value === 2) {
            return end < today;
        }

        return true;
    });


}

    // ✅ LƯU DATA ĐÃ LỌC
    filteredKhuyenMai.value = results;

    // ===== PHÂN TRANG FE =====
    pagination.totalElements = results.length;
    pagination.totalPages = Math.ceil(results.length / pagination.pageSize);

    const start = (pagination.currentPage - 1) * pagination.pageSize;
    const end = start + pagination.pageSize;

    listKhuyenMai.value = results.slice(start, end);
};

const sliderTrackStyle = computed(() => {
    const min = Math.min(filters.minPercent, filters.maxPercent);
    const max = Math.max(filters.minPercent, filters.maxPercent);

    return {
        left: `${min}%`,
        width: `${max - min}%`
    };
});


const submitForm = async () => {
    if (!validateForm()) return;
    // 🔥 CHECK TRÙNG KHUYẾN MÃI
    const conflict = filteredKhuyenMai.value.find(km => {

        // bỏ qua chính nó khi edit
        if (selectedId.value && km.id === selectedId.value) return false;

        // check trùng thời gian
        const overlap = isDateOverlap(
            formData.ngayBatDau,
            formData.ngayKetThuc,
            km.ngayBatDau,
            km.ngayKetThuc
        );

        if (!overlap) return false;

        // check trùng sản phẩm
        const setConflict = km.setLauIds?.some(id =>
            formData.idSetLauChiTiet.includes(id)
        );

        const monConflict = km.monAnIds?.some(id =>
            formData.idMonAnChiTiet.includes(id)
        );

        return setConflict || monConflict;
    });

    if (conflict) {
        showToast(
            "Trùng khuyến mãi",
            `Sản phẩm đã có khuyến mãi "${conflict.tenDotKhuyenMai}" trong thời gian này!`,
            "error"
        );
        return;
    }

    openConfirm(
        'Xác nhận lưu?',
        `Bạn có chắc chắn muốn ${selectedId.value ? 'cập nhật' : 'tạo mới'} đợt khuyến mãi này không?`,
        async () => {
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
                const msg =
                    e?.response?.data?.message ||
                    e?.response?.data ||
                    e?.message ||
                    "Có lỗi xảy ra!";

                showToast("Lỗi", msg, "error");
            }

        }
    );
};


const handleToggleStatus = async (km) => {
    const originalStatus = km.trangThai;

    openConfirm(
        'Thay đổi trạng thái?',
        `Xác nhận ${originalStatus === 1 ? 'ngừng' : 'kích hoạt'} đợt khuyến mãi này?`,
        async () => {
            try {
                await promotionService.toggleStatus(km.id);
                showToast("Thành công", "Trạng thái đã được cập nhật.");
                handleSearch();
            } catch (e) {
                showToast("Lỗi", "Không thể thay đổi trạng thái!", "error");
                handleSearch();
            }
        }
    );
};


const fetchListSetLau = async () => {
    try {
        const res = await axiosClient.get('http://localhost:8080/api/set-lau/active');
        listSetLau.value = res.data;
    } catch (e) { console.error(e); }
};

const fetchListMonAn = async () => {
    try {
        const res = await axiosClient.get('http://localhost:8080/api/mon-an-di-kem/active');
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
    let result = listMonAnDiKem.value.filter(m => m.trangThai === 1);

    // Tìm tên
    if (searchMonAn.value) {
        result = result.filter(m =>
            m.tenMonAn
                ?.toLowerCase()
                .includes(searchMonAn.value.toLowerCase())
        );
    }

    // Lọc giá
    if (filterGia.value === '1') {
        result = result.filter(m => m.giaBan < 50000);
    }

    if (filterGia.value === '2') {
        result = result.filter(m =>
            m.giaBan >= 50000 && m.giaBan <= 100000
        );
    }

    if (filterGia.value === '3') {
        result = result.filter(m => m.giaBan > 100000);
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
const resetFilters = () => {
    Object.assign(filters, {
        keyword: '',
        trangThai: null,
        ngayBatDau: '',
        ngayKetThuc: '',
        minPercent: 0,
        maxPercent: 100
    });
    pagination.currentPage = 1;
    handleSearch();
};
const formatPrice = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0);
const selectedProducts = computed(() => {
    const sets = listSetLau.value
        .filter(s => formData.idSetLauChiTiet.includes(s.id))
        .map(s => ({
            id: s.id,
            ten: s.tenSetLau,
            gia: s.giaBan,
            type: 'SET',
            hinhAnh: s.hinhAnh   // ✅ SỬA Ở ĐÂY
        }));

    const mons = listMonAnDiKem.value
        .filter(m => formData.idMonAnChiTiet.includes(m.id))
        .map(m => ({
            id: m.id,
            ten: m.tenMonAn,
            gia: m.giaBan,
            type: 'MÓN',
            hinhAnh: m.hinhAnh   // ✅ SỬA Ở ĐÂY
        }));

    return [...sets, ...mons];
});

const removeSelectedItem = (item) => {
    if (item.type === 'SET') {
        formData.idSetLauChiTiet =
            formData.idSetLauChiTiet.filter(id => id !== item.id);
    } else {
        formData.idMonAnChiTiet =
            formData.idMonAnChiTiet.filter(id => id !== item.id);
    }
};

const clearAllSelected = () => {
    formData.idSetLauChiTiet = [];
    formData.idMonAnChiTiet = [];
};

const calculateStatus = (km) => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const endDate = new Date(km.ngayKetThuc);
    endDate.setHours(0, 0, 0, 0);
    if (km.trangThai === 0) return { text: 'Ngừng hoạt động', class: 'badge bg-secondary' };
    if (endDate < today) return { text: 'Đã hết hạn', class: 'badge bg-danger' };
    return { text: 'Đang hoạt động', class: 'badge bg-success' };
};

const jumpToPage = () => {
    let p = parseInt(inputPage.value);
    if (p >= 1 && p <= pagination.totalPages) {
        goToPage(p);
    } else {
        inputPage.value = pagination.currentPage;
    }
};

const goToPage = (page) => {
    pagination.currentPage = page;

    const start = (page - 1) * pagination.pageSize;
    const end = start + pagination.pageSize;
    listKhuyenMai.value = filteredKhuyenMai.value.slice(start, end);
};


const changePage = (step) => {
    const newPage = pagination.currentPage + step;
    if (newPage >= 1 && newPage <= pagination.totalPages) {
        goToPage(newPage);
    }
};

onMounted(async () => { // Thêm async vào đây
    await handleSearch();
    await fetchListSetLau();
    await fetchListMonAn();
});

watch(() => pagination.currentPage, (val) => {
    inputPage.value = val;
});
watch(() => confirmModal.show, (val) => {
    document.body.style.overflow = val ? 'hidden' : '';
});

watch(
    () => [filters.ngayBatDau, filters.ngayKetThuc],
    () => {
        pagination.currentPage = 1;
        handleSearch();
    }
);

const getErrorMessage = (e) => {
    return (
        e?.response?.data?.message ||
        e?.response?.data ||
        e?.message ||
        "Lỗi không xác định"
    );
};

</script>

<style scoped>
.form-switch .form-check-input {
    background-color: #e9ecef;
    background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='-4 -4 8 8'%3e%3ccircle r='3' fill='rgba%280, 0, 0, 0.25%29'/%3e%3c/svg%3e");
}


.form-switch .form-check-input:checked {
    background-color: #c0392b !important;
    border-color: #c0392b !important;
    background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='-4 -4 8 8'%3e%3ccircle r='3' fill='%23fff'/%3e%3c/svg%3e");
}

.form-switch .form-check-input:focus {
    box-shadow: 0 0 0 0.25rem rgba(192, 57, 43, 0.25) !important;
    border-color: #c0392b;
}

.custom-red-checkbox:checked {
    background-color: #7d161a !important;
    /* Màu đỏ của bạn */
    border-color: #7d161a !important;
}

/* Màu viền khi click vào (Focus) để mất viền xanh mặc định */
.custom-red-checkbox:focus {
    border-color: #7d161a;
    box-shadow: 0 0 0 0.25rem rgba(125, 22, 26, 0.25);
    /* Hiệu ứng tỏa sáng đỏ nhạt */
}
</style>
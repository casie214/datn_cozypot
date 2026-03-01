<template>
    <div class="container-fluid mt-4">
        <div class="manager-wrapper">
            <!-- TITLE -->
            <h4 class="page-title mb-4">Quản lý tham số hệ thống</h4>

            <!-- FILTER CARD -->
            <div class="card filter-card mb-4">
                <div class="card-body">
                    <div class="row g-3 align-items-end">

                        <div class="col-md-6">
                            <label class="form-label">Tìm kiếm</label>
                            <input v-model="keyword" type="text" class="form-control custom-input"
                                placeholder="Mã hoặc tên tham số..." />
                        </div>

                        <div class="col-md-3">
                            <label class="form-label">Trạng thái</label>
                            <select v-model="status" class="form-select custom-input">
                                <option value="">Tất cả</option>
                                <option value="1">Hoạt động</option>
                                <option value="0">Ngưng hoạt động</option>
                            </select>
                        </div>

                        <div class="col-md-3 d-flex align-items-end gap-2">
                            <button class="btn-reset-filter w-100" @click="resetFilters">
                                <i class="fas fa-sync-alt"></i> Làm mới
                            </button>
                        </div>

                    </div>
                </div>
            </div>
            <!-- TABLE -->

            <!-- TABLE CARD -->

            <div class="table-responsive">
                <table class="table custom-table align-middle mb-0">

                    <thead>
                        <tr>
                            <th>STT</th>
                            <th>MÃ THAM SỐ</th>
                            <th>TÊN THAM SỐ</th>
                            <th>GIÁ TRỊ</th>
                            <th>KIỂU DỮ LIỆU</th>
                            <th>TRẠNG THÁI</th>
                            <th class="text-center">HÀNH ĐỘNG</th>
                        </tr>
                    </thead>

                    <tbody>
                        <tr v-for="(item, index) in paginatedList" :key="item.id">
                            <td>{{ index + 1 }}</td>
                            <td class="fw-bold">{{ item.maThamSo }}</td>
                            <td>{{ item.tenThamSo }}</td>
                            <td>{{ item.giaTri }}</td>
                            <td>{{ item.kieuDuLieu }}</td>

                            <td>
                                <span :class="item.trangThai === 1 ? 'status-active' : 'status-inactive'">
                                    {{ item.trangThai === 1 ? 'Hoạt động' : 'Ngưng hoạt động' }}
                                </span>
                            </td>

                            <td class="text-center">
                                <button class="btn-icon"><i class="fas fa-eye"></i></button>
                                <button class="btn-icon"><i class="fas fa-pen"></i></button>
                            </td>
                        </tr>
                    </tbody>

                </table>
                <div class="pagination-wrapper d-flex justify-content-between align-items-center mt-4 px-2">

                    <div class="d-flex align-items-center">
                        <span class="me-2 text-muted">Hiển thị</span>
                        <select v-model="pageSize" @change="currentPage = 1">

                            <option :value="5">5 dòng</option>
                            <option :value="8">8 dòng</option>
                            <option :value="10">10 dòng</option>
                            <option :value="20">20 dòng</option>
                        </select>
                    </div>

                    <div class="pagination-controls d-flex align-items-center">
                        <button class="btn-page" :disabled="currentPage === 1" @click="goToPage(1)">
                            <i class="fas fa-step-backward"></i>
                        </button>

                        <button class="btn-page" :disabled="currentPage === 1" @click="goToPage(1)">
                            <i class="fas fa-chevron-left"></i>
                        </button>

                        <div class="mx-3 d-flex align-items-center">
                            <template v-if="totalPages <= 5">
                                <span v-for="p in totalPages" :key="p" class="page-number"
                                    :class="{ active: currentPage === p }" @click="goToPage(p)">
                                    {{ p }}
                                </span>
                            </template>

                            <template v-else>
                                <div class="input-page-wrapper d-flex align-items-center">
                                    <input type="number" v-model.number="inputPage" class="input-go-to"
                                        @keyup.enter="jumpToPage">
                                    <span class="ms-2 text-muted">
                                        / {{ totalPages }}
                                    </span>
                                </div>
                            </template>
                        </div>

                        <button class="btn-page" :disabled="currentPage === totalPages" @click="changePage(1)">
                            <i class="fas fa-chevron-right"></i>
                        </button>

                        <button class="btn-page" :disabled="currentPage === totalPages" @click="goToPage(totalPages)">
                            <i class="fas fa-step-forward"></i>
                        </button>
                    </div>

                    <div class="total-info text-muted">
                        Hiển thị {{ filteredList.length }} /
                        {{ list.length }} tham số
                    </div>

                </div>
            </div>
        </div>

    </div>
</template>

<script setup>
import { ref, computed, watch ,onUnmounted} from 'vue'

const keyword = ref('')
const status = ref('')
const currentPage = ref(1)
const pageSize = ref(5)

const list = ref([
    { id: 1, maThamSo: 'VAT', tenThamSo: 'Thuế VAT', giaTri: '10', kieuDuLieu: 'Double', trangThai: 1 },
    { id: 2, maThamSo: 'MIN_RESERVE', tenThamSo: 'Thời gian đặt trước tối thiểu', giaTri: '30', kieuDuLieu: 'Integer', trangThai: 1 },
    { id: 3, maThamSo: 'MAX_HOLD_TIME', tenThamSo: 'Thời gian giữ bàn', giaTri: '15', kieuDuLieu: 'Integer', trangThai: 0 },
    { id: 4, maThamSo: 'TIME_OUT', tenThamSo: 'Timeout', giaTri: '60', kieuDuLieu: 'Integer', trangThai: 1 },
    { id: 5, maThamSo: 'POINT_RATE', tenThamSo: 'Tỷ lệ tích điểm', giaTri: '5', kieuDuLieu: 'Double', trangThai: 1 },
    { id: 6, maThamSo: 'MAX_ORDER', tenThamSo: 'Giới hạn đơn', giaTri: '20', kieuDuLieu: 'Integer', trangThai: 0 }
])

const changePage = (step) => {
    const newPage = currentPage.value + step
    if (newPage >= 1 && newPage <= totalPages.value) {
        currentPage.value = newPage
    }
}
const goToPage = (page) => {
    if (page < 1 || page > totalPages.value) return
    currentPage.value = page
}
const filteredList = computed(() => {
    return list.value.filter(item => {
        const matchKeyword =
            item.maThamSo.toLowerCase().includes(keyword.value.toLowerCase()) ||
            item.tenThamSo.toLowerCase().includes(keyword.value.toLowerCase())

        const matchStatus =
            status.value === '' ||
            item.trangThai.toString() === status.value

        return matchKeyword && matchStatus
    })
})

const totalPages = computed(() =>
    Math.ceil(filteredList.value.length / pageSize.value)
)

const paginatedList = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    return filteredList.value.slice(start, start + pageSize.value)
})

const toggleStatus = (item) => {
    item.trangThai = item.trangThai === 1 ? 0 : 1
}

const resetFilters = () => {
    keyword.value = ''
    status.value = ''
    currentPage.value = 1
}
watch([keyword, status], () => {
    currentPage.value = 1
})

</script>

<style scoped>
/* ================= WRAPPER ================= */
.manager-wrapper {
    background: #fff;
    padding-left: 10px;
    padding-right: 20px;

}

/* ================= TITLE ================= */
.page-title {
    color: #8B0000;
    font-weight: 700;
}

/* ===== TABLE CARD ===== */
.table-responsive {
    background: #ffffff;
    border-radius: 18px;
    overflow: hidden;
    border: 1px solid #e5e7eb;
}

/* ===== HEADER ===== */
.custom-table {
    margin-bottom: 0;
}

.custom-table thead {
    background: #8B0D0D;
    color: #fff;
}

.custom-table thead th {
    padding: 18px 20px;
    font-weight: 600;
    font-size: 14px;
    border: none;
}

/* ===== BODY ===== */
.custom-table tbody tr {
    background: #f9fafb;
    border-bottom: 1px solid #e5e7eb;
    height: 56px;
}

.custom-table td {
    padding: 16px 20px;
    font-size: 14px;
    vertical-align: middle;
}

/* Hover nhẹ */
.custom-table tbody tr:hover {
    background: #f3f4f6;
}

/* STT mảnh */
.custom-table td:first-child {
    width: 60px;
    text-align: center;
    font-weight: 400;
}

/* ===== STATUS BADGE ===== */
.status-active {
    background: #d1fae5;
    color: #065f46;
    padding: 8px 16px;
    border-radius: 999px;
    font-weight: 500;
    font-size: 13px;
}

.status-inactive {
    background: #fee2e2;
    color: #b91c1c;
    padding: 8px 16px;
    border-radius: 999px;
    font-weight: 500;
    font-size: 13px;
}

/* ===== ACTION ICON ===== */
.btn-icon {
    border: none;
    background: #e5e7eb;
    width: 38px;
    height: 38px;
    border-radius: 10px;
    margin: 0 4px;
    transition: 0.2s;
}

.btn-icon i {
    color: #374151;
}

.btn-icon:hover {
    background: #8B0D0D;
}

.btn-icon:hover i {
    color: #fff;
}

.pagination-wrapper {
    background: #ffffff;
    padding: 20px 25px;
    border-top: 1px solid #e5e7eb;
}

/* Dropdown */
.pagination-wrapper select {
    border-radius: 10px;
    padding: 6px 10px;
}

/* Nút page */
.btn-page {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    border: 1px solid #d1d5db;
    background: #fff;
    color: #8B0D0D;
    transition: 0.2s;
}

.btn-page:hover:not(:disabled) {
    background: #8B0D0D;
    color: #fff;
    border-color: #8B0D0D;
}

.btn-page:disabled {
    opacity: 0.4;
}

/* Page number */
.page-number {
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    margin: 0 6px;
    border: 1px solid #d1d5db;
    cursor: pointer;
}

.page-number.active {
    background: #8B0D0D;
    color: #fff;
    border-color: #8B0D0D;
}

/* Tooltip box */
.icon-tooltip .tooltip-text {
    position: absolute;
    bottom: 135%;
    left: 50%;
    transform: translateX(-50%);
    background-color: #333;
    color: #fff;
    padding: 6px 10px;
    font-size: 12px;
    border-radius: 6px;
    white-space: nowrap;
    opacity: 0;
    visibility: hidden;
    transition: all 0.2s ease;
    pointer-events: none;
    z-index: 1000;
}

/* Arrow */
.icon-tooltip .tooltip-text::after {
    content: "";
    position: absolute;
    top: 100%;
    left: 50%;
    transform: translateX(-50%);
    border-width: 6px;
    border-style: solid;
    border-color: #333 transparent transparent transparent;
}

/* Hover show */
.icon-tooltip:hover .tooltip-text {
    opacity: 1;
    visibility: visible;
    transform: translateX(-50%) translateY(-4px);
}

/* INPUT */
.custom-input {
    border-radius: 12px;
    padding: 10px;
}

/* MAIN BUTTON */
.btn-main {
    background: #8B0000;
    color: white;
    border-radius: 10px;
    padding: 8px 18px;
    font-weight: 600;
}

.btn-main:hover {
    background: #6f0000;
}

/* REFRESH BUTTON */
.btn-refresh {
    background: #f3f3f3;
    border-radius: 10px;
}

/* TABLE */
.custom-table thead {
    background: #8B0000;
    color: white;
}

.custom-table th {
    padding: 14px;
    font-weight: 600;
}

.custom-table td {
    padding: 14px;
}

/* ACTION */
.btn-action {
    border: none;
    background: transparent;
    cursor: pointer;
    font-size: 16px;
}

/* TOGGLE SWITCH */
.switch {
    position: relative;
    display: inline-block;
    width: 42px;
    height: 22px;
}

.switch input {
    display: none;
}

.slider {
    position: absolute;
    cursor: pointer;
    background-color: #ccc;
    border-radius: 22px;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    transition: 0.3s;
}

.slider:before {
    content: "";
    position: absolute;
    height: 18px;
    width: 18px;
    left: 2px;
    bottom: 2px;
    background-color: white;
    border-radius: 50%;
    transition: 0.3s;
}

input:checked+.slider {
    background-color: #8B0000;
}

input:checked+.slider:before {
    transform: translateX(20px);
}

/* ACTION ICON STYLE */
.btn-action {
    border: none;
    background: transparent;
    cursor: pointer;
    font-size: 16px;
    padding: 6px;
    border-radius: 8px;
    transition: 0.2s ease;
}

.btn-view i {
    color: #333;
}

.btn-edit i {
    color: #333;
}

.btn-action:hover {
    background-color: #f3f3f3;
    transform: scale(1.1);
}

.btn-page {
    border: 1px solid #8B0000;
    background: white;
    color: #8B0000;
    padding: 4px 10px;
    border-radius: 8px;
    transition: 0.2s;
}

.btn-page:hover:not(:disabled) {
    background: #8B0000;
    color: white;
}

.btn-page:disabled {
    opacity: 0.4;
    cursor: not-allowed;
}

.page-number {
    background: #8B0000;
    color: white;
    padding: 6px 12px;
    border-radius: 8px;
    font-weight: 600;
}
</style>
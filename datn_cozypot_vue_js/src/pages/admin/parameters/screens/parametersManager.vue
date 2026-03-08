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
                            <Multiselect v-model="status" :options="statusOptions" label="label" track-by="value"
                                placeholder="Chọn trạng thái" :allow-empty="true" :close-on-select="true"
                                :select-label="''" :deselect-label="''"
                                @select="() => { /* Logic xử lý ngay khi chọn nếu cần */ }" />
                        </div>
                        <div class="col-md-3 d-flex align-items-end gap-2">
                            <button class="btn-reset-filter " @click="resetFilters">
                                <!-- <i class="fas fa-sync-alt"></i> -->Xóa bộ lọc
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
                            <th>TRẠNG THÁI</th>
                            <th class="text-center">HÀNH ĐỘNG</th>
                        </tr>
                    </thead>

                    <tbody>
                        <tr v-for="(item, index) in paginatedList" :key="item.id">
                            <td>{{ index + 1 }}</td>
                            <td class="fw-bold">{{ item.maThamSo }}</td>
                            <td>{{ item.tenThamSo }}</td>
                            <td>{{ formatValue(item) }}</td>

                            <td>
                                <span :class="item.trangThai === 1 ? 'status-active' : 'status-inactive'">
                                    {{ item.trangThai === 1 ? 'Hoạt động' : 'Ngưng hoạt động' }}
                                </span>
                            </td>

                            <td class="text-center">
                                <div class="action-wrapper">

                                    <button type="button" class="btn-icon" @click="openEditModal(item)">
                                        <i class="fas fa-pen"></i>
                                    </button>

                                    <div class="form-check form-switch">
                                        <input class="form-check-input custom-red-switch" type="checkbox"
                                            :checked="item.trangThai === 1" @change="toggleStatus(item)" />
                                    </div>
                                </div>
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
        <!-- EDIT MODAL -->
        <div v-if="showModal" class="modal-overlay">
            <div class="custom-modal">

                <div class="modal-header">
                    <h5>Chỉnh sửa tham số</h5>
                    <button class="close-btn" @click="closeModal">
                        <i class="fas fa-times"></i>
                    </button>
                </div>

                <div class="modal-body">

                    <div class="mb-3">
                        <label class="form-label">Mã tham số</label>
                        <input type="text" class="form-control" v-model="editingItem.maThamSo" disabled>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Tên tham số</label>
                        <input type="text" class="form-control" :class="{ 'is-invalid': errors.tenThamSo }"
                            v-model="editingItem.tenThamSo">
                        <div class="invalid-feedback">
                            {{ errors.tenThamSo }}
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Giá trị</label>
                        <input type="text" class="form-control" :class="{ 'is-invalid': errors.giaTri }"
                            v-model="editingItem.giaTri">

                        <div class="invalid-feedback">
                            {{ errors.giaTri }}
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Mô tả</label>
                        <textarea type="text" class="form-control" v-model="editingItem.moTa"></textarea>
                    </div>


                </div>

                <div class="modal-footer">
                    <button class="btn-cancel" @click="closeModal">Hủy</button>
                    <button class="btn-save" @click="handleSave">Lưu</button>
                </div>

            </div>
        </div>
        <!-- VIEW MODAL -->
        <div v-if="showViewModal" class="modal-overlay">
            <div class="custom-modal">

                <div class="modal-header">
                    <h5>Chi tiết tham số</h5>
                    <button class="close-btn" @click="closeViewModal">
                        <i class="fas fa-times"></i>
                    </button>
                </div>

                <div class="modal-body">

                    <div class="mb-3">
                        <label class="form-label">Mã tham số</label>
                        <input type="text" class="form-control" v-model="editingItem.maThamSo" disabled>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Tên tham số</label>
                        <input type="text" class="form-control" v-model="editingItem.tenThamSo" disabled>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Giá trị</label>
                        <!-- INTEGER / DOUBLE / DECIMAL -->
                        <input v-if="['INTEGER', 'DOUBLE', 'DECIMAL'].includes(editingItem.kieuDuLieu?.value)"
                            type="number" class="form-control" :class="{ 'is-invalid': errors.giaTri }"
                            v-model="editingItem.giaTri">

                        <!-- DATE -->
                        <input v-else-if="editingItem.kieuDuLieu?.value === 'DATE'" type="date" class="form-control"
                            :class="{ 'is-invalid': errors.giaTri }" v-model="editingItem.giaTri">

                        <!-- BOOLEAN -->
                        <select v-else-if="editingItem.kieuDuLieu?.value === 'BOOLEAN'" class="form-control"
                            :class="{ 'is-invalid': errors.giaTri }" v-model="editingItem.giaTri">
                            <option value="true">true</option>
                            <option value="false">false</option>
                        </select>

                        <!-- STRING default -->
                        <input v-else type="text" class="form-control" :class="{ 'is-invalid': errors.giaTri }"
                            v-model="editingItem.giaTri">
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Kiểu dữ liệu</label>
                        <input type="text" class="form-control" v-model="editingItem.kieuDuLieu" disabled>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Mô tả</label>
                        <textarea class="form-control" v-model="editingItem.moTa" disabled></textarea>
                    </div>


                    <div class="mb-3">
                        <label class="form-label">Trạng thái</label>
                        <input type="text" class="form-control"
                            :value="editingItem.trangThai === 1 ? 'Hoạt động' : 'Ngưng hoạt động'" disabled>
                    </div>

                </div>

                <div class="modal-footer">
                    <button class="btn-cancel" @click="closeViewModal">
                        Đóng
                    </button>
                </div>

            </div>
        </div>

    </div>
</template>

<script setup>
import { ref, computed, watch, onUnmounted, onMounted } from 'vue'
import axios from 'axios'
import Multiselect from 'vue-multiselect'
import 'vue-multiselect/dist/vue-multiselect.css'

import Swal from "sweetalert2"
// Thêm dòng này vào cụm khai báo ref ở đầu script
const errors = ref({})
const dataTypeOptions = ref([
    { label: "String", value: "STRING" },
    { label: "Integer", value: "INTEGER" },
    { label: "Double", value: "DOUBLE" },
    { label: "Boolean", value: "BOOLEAN" },
    { label: "Date", value: "DATE" },
    { label: "Decimal", value: "DECIMAL" },

])

const handleSave = async () => {
    if (!validateEditForm()) return
        showModal.value = false   // đóng modal trước

    const result = await Swal.fire({
        title: "Xác nhận cập nhật?",
        text: "Bạn có chắc muốn thay đổi thông tin tham số?",
        icon: "question",
        showCancelButton: true,
        confirmButtonColor: "#8B0000",
        cancelButtonColor: "#6c757d",
        confirmButtonText: "Xác nhận",
        cancelButtonText: "Hủy"
    })

    if (result.isConfirmed) {
        confirmSave()
    }
}

const confirmSave = async () => {
    try {
        const payload = {}

        if (editingItem.value.tenThamSo !== originalItem.value.tenThamSo) {
            payload.tenThamSo = editingItem.value.tenThamSo
        }

        if (editingItem.value.giaTri !== originalItem.value.giaTri) {
            payload.giaTri = editingItem.value.giaTri
        }

        if (editingItem.value.moTa !== originalItem.value.moTa) {
            payload.moTa = editingItem.value.moTa
        }

        await axios.patch(
            `http://localhost:8080/api/tham-so-he-thong/${editingItem.value.id}`,
            payload,
            authConfig()
        )

        await fetchSystemParams()
        showModal.value = false

        Swal.fire({
            icon: "success",
            title: "Cập nhật thành công",
            text: `Tham số "${editingItem.value.tenThamSo}" đã được cập nhật`,
            timer: 2000,
            showConfirmButton: false
        })

    } catch (error) {

    Swal.fire({
        icon: "error",
        title: "Cập nhật thất bại",
        timer: 2000,
        showConfirmButton: false
    })

}
}
const keyword = ref('')
const status = ref(null)
const currentPage = ref(1)
const pageSize = ref(5)
const showToast = ref(false)
const toastTitle = ref('')
const toastMessage = ref('')
const pendingEditItem = ref(null)
const formatValue = (item) => {
    if (!item) return ""

    const value = item.giaTri
    const code = item.maThamSo

    switch (code) {

        case "VAT":
            return `${value} %`

        case "POINT_RATE":
            return `${value} %`

        case "MIN_RESERVE":
            return `${value} phút`

        case "CURRENCY":
            return value

        case "HOTLINE":
            return value

        case "THOI_GIAN_GIU_BAN":
            return `${value} phút`
        case "THOI_GIAN_HUY_HOAN_COC":
            return `${value} phút`

        case "THOI_GIAN_TOI_DA_DAT_TRUOC":
            return `${value} ngày`

        default:
            return value
    }
}
const validateEditForm = () => {
    errors.value = {}

    if (!editingItem.value.tenThamSo?.trim()) {
        errors.value.tenThamSo = "Tên tham số không được để trống"
    }

    if (!editingItem.value.giaTri?.toString().trim()) {
        errors.value.giaTri = "Giá trị không được để trống"
    }

    if (editingItem.value.giaTri && editingItem.value.kieuDuLieu) {
        const value = editingItem.value.giaTri
        const type = editingItem.value.kieuDuLieu?.value
        switch (type) {
            case "INTEGER":
                if (!/^-?\d+$/.test(value)) {
                    errors.value.giaTri = "Giá trị phải là số nguyên"
                }
                break

            case "DOUBLE":
            case "DECIMAL":
                if (!/^-?\d+(\.\d+)?$/.test(value)) {
                    errors.value.giaTri = "Giá trị phải là số"
                }
                break

            case "BOOLEAN":
                if (typeof value !== "string" ||
                    !["true", "false"].includes(value.toLowerCase())) {
                    errors.value.giaTri = "Chỉ được nhập true hoặc false"
                }
                break

            case "DATE":
                if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
                    errors.value.giaTri = "Định dạng ngày yyyy-MM-dd"
                }
                break

            case "STRING":
                if (value.length > 255) {
                    errors.value.giaTri = "Không vượt quá 255 ký tự"
                }
                break
        }
    }

    return Object.keys(errors.value).length === 0
}
const authConfig = () => {
    const token = localStorage.getItem("token")

    return {
        headers: {
            Authorization: token ? `Bearer ${token}` : ""
        }
    }
}

const userRole = ref(null)

onMounted(() => {
    fetchSystemParams()

    const token = localStorage.getItem("token")
    if (token) {
        try {
            const payload = JSON.parse(atob(token.split('.')[1]))
            userRole.value = payload.role || payload.roles
        } catch (e) {
            console.error("Lỗi decode token")
        }
    }
})

const isAdmin = computed(() => userRole.value === "ADMIN")
const toggleStatus = async (item) => {

    const result = await Swal.fire({
        title: "Xác nhận thay đổi trạng thái?",
        text: `Bạn muốn ${item.trangThai === 1 ? "ngưng hoạt động" : "kích hoạt"} tham số này?`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonColor: "#8B0000",
        cancelButtonText: "Hủy",
        confirmButtonText: "Xác nhận"
    })

    if (!result.isConfirmed) return

    const oldStatus = item.trangThai
    item.trangThai = item.trangThai === 1 ? 0 : 1

    try {

        await axios.put(
            `http://localhost:8080/api/tham-so-he-thong/update-status/${item.maThamSo}`,
            { trangThai: item.trangThai },
            authConfig()
        )

        Swal.fire({
            icon: "success",
            title: "Cập nhật trạng thái thành công",
timer: 1500,
    showConfirmButton: false        })

    } catch (error) {

        item.trangThai = oldStatus

        Swal.fire({
            icon: "error",
            title: "Lỗi cập nhật trạng thái",
timer: 1500,
    showConfirmButton: false        })

    }
}
const showSuccessToast = (title, message) => {
    toastTitle.value = title
    toastMessage.value = message
    showToast.value = true

    setTimeout(() => {
        showToast.value = false
    }, 4000)
}

const originalItem = ref(null)
const list = ref([]) // ❌ bỏ dữ liệu fake

const fetchSystemParams = async () => {
    try {

        const response = await axios.get(
            'http://localhost:8080/api/tham-so-he-thong',
            authConfig()
        )

        list.value = response.data

    } catch (error) {
        console.error('Lỗi khi load tham số hệ thống:', error)
    }
}
const showConfirmModal = ref(false)
const confirmLoading = ref(false)
const showModal = ref(false)
const editingItem = ref({
    id: null,
    maThamSo: '',
    tenThamSo: '',
    giaTri: '',
    kieuDuLieu: null,
    moTa: '',
    trangThai: 1
})

const filteredList = computed(() => {
    return list.value.filter(item => {
        if (!item) return false

        const ma = item.maThamSo?.toLowerCase() || ""
        const ten = item.tenThamSo?.toLowerCase() || ""

        const matchKeyword =
            ma.includes(keyword.value.toLowerCase()) ||
            ten.includes(keyword.value.toLowerCase())

        const matchStatus =
            !status.value ||
            status.value.value === '' ||
            item.trangThai?.toString() === status.value.value

        return matchKeyword && matchStatus
    })
})
const showViewModal = ref(false)

const openViewModal = (item) => {
    editingItem.value = { ...item }
    showViewModal.value = true
}

const closeViewModal = () => {
    showViewModal.value = false
}
const openEditModal = (item) => {
    originalItem.value = { ...item }   // lưu bản gốc

    editingItem.value = {
        ...item,
        moTa: item.moTa || '',
        kieuDuLieu: dataTypeOptions.value.find(
            opt => opt.value === item.kieuDuLieu
        )
    }

    showModal.value = true
}

const closeModal = () => {
    showModal.value = false
}
const saveEdit = async () => {
    try {
        await axios.put(
            `http://localhost:8080/api/tham-so-he-thong/${editingItem.value.id}`,
            editingItem.value,
            authConfig()
        )

        await fetchSystemParams()
        showModal.value = false

        showSuccessToast(
            "Cập nhật thành công",
            `Tham số "${editingItem.value.tenThamSo}" đã được cập nhật`
        )

    } catch (error) {
        console.error("Lỗi cập nhật", error)
    }
}

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

const totalPages = computed(() =>
    Math.ceil(filteredList.value.length / pageSize.value)
)

const paginatedList = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    return filteredList.value.slice(start, start + pageSize.value)
})

const resetFilters = () => {
    keyword.value = ''
    status.value = null
    currentPage.value = 1
}
watch([keyword, status], () => {
    currentPage.value = 1
})

const statusOptions = ref([
    { label: "Tất cả", value: "" },
    { label: "Hoạt động", value: "1" },
    { label: "Ngưng hoạt động", value: "0" }
])
</script>

<style scoped>
/* ================= WRAPPER ================= */
.manager-wrapper {
    background: #fdfbfa;
    padding-left: 10px;
    padding-right: 15px;

    border-radius: 18px;
}

/* ================= TITLE ================= */
.page-title {
    color: #8B0000;
    font-weight: 700;
}

/* ================= FILTER CARD ================= */
.filter-card {
    border-radius: 18px;
    border: 1px solid #e5e7eb;
}

.custom-input {
    border-radius: 12px;
    padding: 10px 14px;
    border: 1px solid #d1d5db;
}
.swal2-container {
    z-index: 20000 !important;
}
.custom-input:focus {
    border-color: #8B0000;
    box-shadow: 0 0 0 2px rgba(139, 0, 0, 0.1);
}

/* ================= TABLE ================= */
.table-responsive {
    background: #ffffff;
    border-radius: 18px;
    overflow: hidden;
    border: 1px solid #e5e7eb;
}

.custom-table {
    margin-bottom: 0;
}

.custom-table thead {
    background: #8B0000;
    color: #ffffff;
}

.custom-table thead th {
    padding: 16px;
    font-weight: 600;
    font-size: 14px;
    border: none;
}

.custom-table tbody tr {
    border-bottom: 1px solid #e5e7eb;
    transition: 0.2s;
}

.custom-table tbody tr:hover {
    background: #f9fafb;
}

.custom-table td {
    font-size: 14px;
    vertical-align: middle;

}

/* ================= STATUS BADGE ================= */
.status-active {
    background: #dcfce7;
    color: #166534;
    padding: 6px 14px;
    border-radius: 999px;
    font-weight: 500;
    font-size: 13px;
}

.status-inactive {
    background: #fee2e2;
    color: #991b1b;
    padding: 6px 14px;
    border-radius: 999px;
    font-weight: 500;
    font-size: 13px;
}

/* ================= ACTION BUTTON ================= */
.btn-icon {
    border: none;
    background: white;
    width: 36px;
    height: 36px;
    border-radius: 10px;
    margin: 0 4px;
    transition: 0.2s ease;
}

.btn-icon i {
    color: #374151;
}

.btn-icon:hover {
    background: #8B0000;
    transform: scale(1.1);
}

.btn-icon:hover i {
    color: #ffffff;
}

/* ================= PAGINATION ================= */
.pagination-wrapper {
    padding: 18px 24px;
    border-top: 1px solid #e5e7eb;
    background: #ffffff;
}

/* Dropdown */
.pagination-wrapper select {
    border-radius: 10px;
    padding: 6px 10px;
    border: 1px solid #d1d5db;
}

/* Page buttons */
.btn-page {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    border: 1px solid #d1d5db;
    background: #ffffff;
    color: #8B0000;
    transition: 0.2s;
}

.btn-page:hover:not(:disabled) {
    background: #8B0000;
    color: #ffffff;
    border-color: #8B0000;
}

.btn-page:disabled {
    opacity: 0.4;
    cursor: not-allowed;
}

/* Page number */
.page-number {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    margin: 0 4px;
    border: 1px solid #d1d5db;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: 0.2s;
}

.page-number:hover {
    background: #f3f4f6;
}

.page-number.active {
    background: #8B0000;
    color: #ffffff;
    border-color: #8B0000;
}

.btn-reset-filter {
    background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
    color: white;
    border: none;
    padding: 8px 15px;
    border-radius: 4px;
    cursor: pointer;
    height: inherit;
    text-align: center;
    transition: 0.2s;
}

/* ================= MODAL ================= */

.modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
}

.custom-modal {
    background: #ffffff;
    width: 500px;
    border-radius: 16px;
    padding: 20px;
    animation: fadeIn 0.2s ease;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
}

.modal-header h5 {
    color: #8B0000;
    font-weight: 600;
}

.close-btn {
    border: none;
    background: transparent;
    font-size: 18px;
    cursor: pointer;
}

.modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
}

.btn-cancel {
    background: #f3f4f6;
    border: none;
    padding: 8px 16px;
    border-radius: 10px;
}

.btn-save {
    background: #8B0000;
    color: #ffffff;
    border: none;
    padding: 8px 16px;
    border-radius: 10px;
}

.btn-save:hover {
    opacity: 0.9;
}

@keyframes fadeIn {
    from {
        transform: scale(0.95);
        opacity: 0;
    }

    to {
        transform: scale(1);
        opacity: 1;
    }
}

/* ================= CUSTOM RED SWITCH ================= */

.form-switch .form-check-input.custom-red-switch {
    width: 2.5em;
    height: 1.2em;
    cursor: pointer;
    transition: all 0.25s ease;
}

/* OFF */
.form-switch .form-check-input.custom-red-switch {
    background-color: #e9ecef;
}

/* ON - đỏ đô */
.form-switch .form-check-input.custom-red-switch:checked {
    background-color: #8B0000;
    border-color: #8B0000;
}

/* Focus */
.form-switch .form-check-input.custom-red-switch:focus {
    box-shadow: 0 0 0 0.2rem rgba(139, 0, 0, 0.25);
}

/* ================= SUCCESS TOAST ================= */

.toast-success {
    position: fixed;
    top: 30px;
    right: 30px;
    display: flex;
    align-items: center;
    gap: 14px;
    background: #ffffff;
    border-left: 6px solid #8B0000;
    padding: 16px 20px;
    border-radius: 14px;
    width: 380px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
    animation: slideIn 0.4s ease;
    z-index: 99999;
}

.toast-success i {
    font-size: 28px;
    color: #8B0000;
}

.toast-success h4 {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
}

.toast-success p {
    margin: 4px 0 0;
    font-size: 13px;
    color: #555;
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

/* ===== OVERLAY ===== */
.confirm-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;
}

/* ===== MODAL ===== */
.confirm-modal {
    width: 500px;
    background: #fff;
    border-radius: 20px;
    padding: 40px 30px;
    text-align: center;
    animation: scaleIn 0.25s ease;
}

/* ICON */
.confirm-icon {
    font-size: 28px;
    color: #1e2a38;
    margin-bottom: 15px;
}

.confirm-modal h2 {
    font-weight: 700;
    margin-bottom: 10px;
}

.confirm-modal p {
    color: #6c757d;
    font-size: 15px;
    margin-bottom: 30px;
}

/* BUTTONS */
.confirm-actions {
    display: flex;
    justify-content: center;
    gap: 15px;
}

.btn-cancel {
    background: #eee;
    border: none;
    padding: 10px 25px;
    border-radius: 10px;
    cursor: pointer;
}

.btn-confirm {
    background: #a50000;
    color: white;
    border: none;
    padding: 10px 25px;
    border-radius: 10px;
    cursor: pointer;
}

/* ANIMATION */
@keyframes scaleIn {
    from {
        transform: scale(0.9);
        opacity: 0;
    }

    to {
        transform: scale(1);
        opacity: 1;
    }
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

.multiselect {
    border-radius: 12px;
}

.multiselect__option--highlight {
    background: #8B0000 !important;
}

.multiselect__tag {
    background: #8B0000;
}

.action-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 5px;
}

/* Loại bỏ khoảng trống thừa và căn chỉnh độ cao đồng nhất với bootstrap input */
.multiselect {
    min-height: 38px !important;
    /* Độ cao chuẩn của form-control bootstrap */
    margin-bottom: 0 !important;
}

.multiselect__tags {
    min-height: 38px !important;
    padding: 6px 40px 0 8px !important;
    /* Căn chỉnh lại padding bên trong */
    border-radius: 12px !important;
    border: 1px solid #d1d5db !important;
}

.multiselect__placeholder {
    margin-bottom: 0 !important;
    padding-top: 2px !important;
}

.multiselect__select {
    height: 36px !important;
}

/* Đảm bảo Multiselect không làm hỏng dòng khi nằm trong d-flex */
.col-md-3 .multiselect {
    display: block;
    width: 100%;
}

.is-invalid {
    border-color: #dc3545 !important;
}

.invalid-feedback {
    display: block;
}

.custom-table {
    table-layout: fixed;
    width: 100%;
}

.custom-table td,
.custom-table th {
    word-wrap: break-word;
    word-break: break-word;
}

/* STT */
.custom-table th:nth-child(1),
.custom-table td:nth-child(1) {
    width: 60px;
    text-align: center;
}

/* Giới hạn từng cột */
.custom-table th:nth-child(2),
.custom-table td:nth-child(2) {
    width: 300px;
}

.custom-table th:nth-child(3),
.custom-table td:nth-child(3) {
    width: 400px;
}

.custom-table th:nth-child(4),
.custom-table td:nth-child(4) {
    width: 120px;
}
</style>
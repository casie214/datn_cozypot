<template>
    <div class="promotion-campaign-container">
        <div class="filter-box">
            <div class="filter-row">
                <div class="filter-item search-wrapper">
                    <label>Tìm kiếm</label>
                    <div class="input-group">
                        <input v-model="filters.keyword" type="text" placeholder="Tìm kiếm theo mã, tên..."
                            class="form-control" @keyup.enter="loadData">
                        <button class="search-btn" @click="loadData">
                            <i class="fa-solid fa-magnifying-glass"></i>
                        </button>
                    </div>
                </div>

                <div class="filter-item">
                    <label>Trạng thái</label>
                    <select v-model="filters.status" class="form-select" @change="loadData">
                        <option value="">Tất cả</option>
                        <option :value="0">Chưa sử dụng</option>
                        <option :value="1">Đã sử dụng</option>
                        <option :value="2">Hết hạn</option>
                    </select>
                </div>



                <div class="filter-item">
                    <button class="btn-clear" @click="resetFilters">Xóa bộ lọc</button>
                </div>
            </div>
        </div>

        <div class="action-row-right">
            <button class="btn-add-new" @click="showAddModal = true">
                <i class="fa-solid fa-plus"></i> Thêm phiếu giảm giá
            </button>
        </div>

        <div class="table-container" style="overflow-x: auto;">
            <table style="min-width: 1400px;">
                <thead>
                    <tr>
                        <th>STT</th>
                        <th>MÃ PHIẾU CÁ NHÂN</th>
                        <th>KHÁCH HÀNG</th>
                        <th>TÊN PHIẾU GỐC</th>
                        <th>NGÀY NHẬN</th>
                        <th>NGÀY HẾT HẠN</th>
                        <th>NGÀY SỬ DỤNG</th>
                        <th>TRẠNG THÁI</th>
                        <th>GHI CHÚ</th>
                        <th>CHỨC NĂNG</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(item, index) in vouchers" :key="item.id || index">
                        <td>{{ index + 1 }}</td>
                        <td><b class="voucher-code">{{ item.maGiamGiaCaNhan }}</b></td>

                        <td>
                            <strong>{{ item.tenKhachHang || 'Khách hàng ID: ' + item.idKhachHang }}</strong>
                        </td>

                        <td>{{ item.tenPhieuGiamGia || 'Phiếu gốc ID: ' + item.idPhieuGiamGia }}</td>

                        <td>{{ formatDate(item.ngayNhan) }}</td>
                        <td class="text-danger">{{ formatDate(item.ngayHetHan) }}</td>
                        <td>{{ item.ngaySuDung ? formatDate(item.ngaySuDung) : '---' }}</td>
                        <td>
                            <span :class="getStatusClass(item.trangThaiSuDung)">
                                {{ getStatusText(item.trangThaiSuDung) }}
                            </span>
                        </td>
                        <td>{{ item.ghiChu || 'Không có' }}</td>

                        <td>
                            <div class="actions">
                                <button class="btn-icon btn-view" @click="handleViewDetail(item)" title="Xem chi tiết">
                                    <i class="fa-regular fa-eye"></i>
                                </button>
                                <button class="btn-icon btn-edit" @click="handleEdit(item)" title="Sửa ghi chú">
                                    <i class="fa-regular fa-pen-to-square"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="pagination" v-if="totalPages > 0">
            <button @click="changePage(filters.page - 1)" :disabled="filters.page === 0">
                <i class="fa-solid fa-chevron-left"></i>
            </button>
            <button v-for="p in totalPages" :key="p" @click="changePage(p - 1)"
                :class="{ active: filters.page === p - 1 }">
                {{ p }}
            </button>
            <button @click="changePage(filters.page + 1)" :disabled="filters.page === totalPages - 1">
                <i class="fa-solid fa-chevron-right"></i>
            </button>
        </div>
        <PersonalVoucherAddModal :is-open="showAddModal" @close="showAddModal = false" @save="handleSaveAdd" />

        <PersonalVoucherDetailModal :is-open="showDetailModal" :data="selectedVoucher"
            @close="showDetailModal = false" />

        <PersonalVoucherEditModal :is-open="showEditModal" :data="editingVoucher" @close="showEditModal = false"
            @save="handleSaveEdit" />
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { PersonalVoucherService } from '@/services/PersonalVoucherService.';
import PersonalVoucherAddModal from '../../modal/PersonalVoucherAddModal.vue';
import PersonalVoucherDetailModal from '../../modal/PersonalVoucherDetailModal.vue';
import PersonalVoucherEditModal from '../../modal/PersonalVoucherEditModal.vue';

const vouchers = ref([]);
const totalPages = ref(0);
const showAddModal = ref(false);
const showEditModal = ref(false);
const showDetailModal = ref(false);
const editingVoucher = ref(null);
const selectedVoucher = ref(null);

const filters = reactive({
    keyword: '',
    status: '',
    page: 0,
    size: 10
});

const loadData = async () => {
    try {
        const response = await PersonalVoucherService.search(filters, filters.page, filters.size);
        console.log("Dữ liệu nhận được:", response);

        // Gán trực tiếp vì response là mảng [{}, {}, {}]
        vouchers.value = response || [];

        // Nếu không có phân trang từ Backend, tạm thời để totalPages = 1
        totalPages.value = 1;
    } catch (error) {
        console.error("Lỗi khi tải dữ liệu:", error);
    }
};

const changePage = (p) => {
    filters.page = p;
    loadData();
};

const resetFilters = () => {
    filters.keyword = '';
    filters.status = '';
    filters.page = 0;
    loadData();
};

const formatDate = (dateStr) => {
    if (!dateStr) return '---';
    return new Date(dateStr).toLocaleDateString('vi-VN');
};

const getStatusText = (status) => {
    const map = { 0: 'Chưa dùng', 1: 'Đã dùng', 2: 'Hết hạn' };
    return map[status] || 'Không xác định';
};

const getStatusClass = (status) => {
    if (status === 0) return 'status-active';
    if (status === 1) return 'status-used';
    return 'status-inactive';
};

const handleEdit = (item) => {
    editingVoucher.value = { ...item };
    showEditModal.value = true;
};

const handleViewDetail = (item) => {
    selectedVoucher.value = { ...item };
    showDetailModal.value = true;
};

onMounted(() => {
    loadData();
});
</script>

<style scoped>
/* Các style quan trọng nhất để bảng hiển thị đúng */
.promotion-campaign-container {
    padding: 20px;
}

/* Layout bộ lọc nằm ngang */
.filter-box {
    background: white;
    padding: 25px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    margin-bottom: 20px;
}

.filter-row {
    display: flex;
    align-items: flex-end;
    gap: 15px;
    flex-wrap: wrap;
}

.filter-item {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.filter-item label {
    font-weight: 600;
    font-size: 14px;
    color: #333;
}

/* Ô tìm kiếm */
.search-wrapper {
    flex: 1;
    /* Để ô tìm kiếm dài ra */
    min-width: 250px;
}

.input-group {
    display: flex;
    border: 1px solid #ddd;
    border-radius: 4px;
    overflow: hidden;
}

.form-control,
.form-select {
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    outline: none;
}

.form-control {
    border: none;
    /* Bỏ border để dùng border của input-group */
    flex: 1;
}

.search-btn {
    background: #8b0000;
    color: white;
    border: none;
    padding: 0 15px;
    cursor: pointer;
}

/* Nút xóa bộ lọc */
.btn-clear {
    color: white;
    border: none;
    padding: 9px 20px;
    border-radius: 4px;
    font-weight: 600;
    cursor: pointer;
}

/* Nút THÊM nằm bên phải */
.action-row-right {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 20px;
}

.btn-add-new {
    background: #8b0000;
    color: white;
    border: none;
    padding: 12px 25px;
    border-radius: 4px;
    font-weight: bold;
    cursor: pointer;
    box-shadow: 0 4px 6px rgba(139, 0, 0, 0.2);
    transition: 0.3s;
}

.btn-add-new:hover {
    background: #a00000;
    transform: translateY(-2px);
}

.btn-clear {
    background: #555;
    color: white;
    border: none;
    padding: 10px 15px;
    border-radius: 6px;
    cursor: pointer;
}

.table-container {
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

table {
    width: 100%;
    border-collapse: collapse;
}

th {
    background: #8B0000;
    color: white;
    padding: 15px;
    text-align: left;
    font-size: 13px;
    text-transform: uppercase;
}

td {
    padding: 15px;
    border-bottom: 1px solid #eee;
    font-size: 14px;
}

.voucher-code {
    padding: 3px 8px;
    border-radius: 4px;
    font-weight: bold;
    font-family: monospace;
}

.status-active {
    color: #28a745;
    font-weight: bold;
}

.status-used {
    color: #007bff;
    font-weight: bold;
}

.status-inactive {
    color: #dc3545;
    font-weight: bold;
}

.text-danger {
    color: #8B0000;
}

.small-text {
    font-size: 12px;
    color: #666;
}

.actions {
    display: flex;
    gap: 15px;
    align-items: center;
}

.btn-icon {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 18px;
    color: #555;
}

.pagination {
    display: flex;
    justify-content: center;
    gap: 8px;
    margin-top: 25px;
}

.pagination button {
    padding: 8px 16px;
    border: 1px solid #ddd;
    background: white;
    border-radius: 6px;
    cursor: pointer;
}

.pagination button.active {
    background: #8B0000;
    color: white;
    border-color: #8B0000;
}
</style>
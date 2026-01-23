<template>
    <div class="promotion-campaign-container">
        <div class="filter-box">
            <div class="filter-row">
                <div class="filter-item search-group">
                    <label>Tìm kiếm</label>
                    <div class="input-group">
                        <input v-model="filters.keyword" type="text" placeholder="Tìm kiếm theo mã, tên..."
                            class="form-search" @keyup.enter="loadData">
                        <button class="search-btn" @click="loadData">
                            <i class="fa-solid fa-magnifying-glass"></i>
                        </button>
                    </div>
                </div>

                <div class="filter-item">
                    <label>Trạng thái</label>
                    <select v-model="filters.status" @change="loadData">
                        <option value="">Tất cả</option>
                        <option :value="1">Đang hoạt động</option>
                        <option :value="0">Ngưng hoạt động</option>
                    </select>
                </div>
                </div>

                <div class="filter-item">
                    <label>Loại khuyến mãi</label>
                    <select v-model="filters.dotKhuyenMaiId" @change="loadData">
                        <option value="">Tất cả</option>
                        <option v-for="dot in dotKhuyenMaiList" :key="dot.id" :value="dot.id">
                            {{ dot.tenDotKhuyenMai }}
                        </option>
                    </select>
                </div>

                <button class="btn-clear" @click="resetFilters">Xóa bộ lọc</button>
            </div>

            <div class="action-row">
                <button class="btn-add" @click="showAddModal = true">
                    <i class="fa-solid fa-plus"></i> Thêm phiếu giảm giá
                </button>
            </div>

            <div class="table-container" style="overflow-x: auto;">
                <table style="min-width: 1600px;">
                    <thead>
                        <tr>
                            <th>STT</th>
                            <th>MÃ PHIẾU</th>
                            <th>TÊN PHIẾU</th>
                            <th>ĐỢT GIẢM</th>
                            <th>CODE GIẢM</th>
                            <th>LOẠI GIẢM</th>
                            <th>GIÁ TRỊ</th>
                            <th>GIẢM TỐI ĐA</th>
                            <th>ĐƠN TỐI THIỂU</th>
                            <th>SỐ LƯỢNG</th>
                            <th>THỜI GIAN</th>
                            <th>TRẠNG THÁI</th>
                            <th>CHỨC NĂNG</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(item, index) in vouchers" :key="item.id || index">
                            <td>{{ (filters.page * filters.size) + index + 1 }}</td>
                            <td><b>{{ item.maPhieuGiamGia }}</b></td>
                            <td>{{ item.tenPhieuGiamGia }}</td>
                            <td>{{ item.tenDotKhuyenMai || '---' }}</td>
                            <td><code class="voucher-code">{{ item.codeGiamGia }}</code></td>
                            <td>{{ item.loaiGiamGia === 1 ? 'Phần trăm' : 'Tiền mặt' }}</td>
                            <td class="text-danger">
                                <b>{{ item.loaiGiamGia === 1 ? item.giaTriGiam + '%' : formatCurrency(item.giaTriGiam)
                                    }}</b>
                            </td>
                            <td>{{ formatCurrency(item.giaTriGiamToiDa) }}</td>
                            <td>{{ formatCurrency(item.donHangToiThieu) }}</td>
                            <td>{{ item.soLuongConLai }} / {{ item.soLuongPhatHanh }}</td>
                            <td class="small-text">
                                <div class="time-display">
                                    <span class="time-start" title="Ngày bắt đầu">
                                        <i class="fa-regular fa-calendar-check"></i> {{ formatDate(item.ngayBatDau) }}
                                    </span>
                                    <div class="time-separator"></div>
                                    <span class="time-end" title="Ngày kết thúc"
                                        :class="{ 'text-danger': isExpired(item.ngayKetThuc) }">
                                        <i class="fa-regular fa-calendar-xmark"></i> {{ formatDate(item.ngayKetThuc) }}
                                    </span>
                                </div>
                            </td>
                            <td>
                                <span :class="item.trangThai === 1 ? 'status-active' : 'status-inactive'">
                                    {{ item.trangThai === 1 ? 'Đang hoạt động' : 'Ngưng hoạt động' }}
                                </span>
                            </td>
                            <td>
                                <div class="actions">
                                    <button class="btn-icon btn-view" @click="handleViewDetail(item)"
                                        title="Xem chi tiết">
                                        <i class="fa-regular fa-eye"></i>
                                    </button>

                                    <button class="btn-icon btn-edit" @click="handleEdit(item)" title="Cập nhật">
                                        <i class="fa-regular fa-pen-to-square"></i>
                                    </button>

                                    <div class="toggle-switch" :class="{ on: item.trangThai === 1 }"
                                        @click="toggleStatus(item)">
                                        <div class="toggle-knob"></div>
                                    </div>
                                </div>
                            </td>
                        </tr>
                        <tr v-if="vouchers.length === 0">
                            <td colspan="13" style="text-align: center; padding: 40px; color: #999;">
                                Không tìm thấy dữ liệu phiếu giảm giá
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



            <PromotionVoucherAddModal 
  v-if="showAddModal" 
  :is-open="showAddModal"
  :danh-sach-dot-k-m="dotKhuyenMaiList"  @close="showAddModal = false"
  @save="onSaved" 
/>
            <PromotionVoucherEditModal v-if="showEditModal" :is-open="showEditModal" :data="editingVoucher"
                @close="showEditModal = false" @save="onSaved" />
            <PromotionVoucherDetailModal v-if="showDetailModal" :is-open="showDetailModal" :data="selectedVoucher"
                @close="showDetailModal = false" />
        </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { PromotionVoucherService } from '../../../../services/PromotionVoucherService';
// Cần đảm bảo 3 file này tồn tại trong thư mục modal của bạn
import PromotionVoucherAddModal from '../../modal/PromotionVoucherAddModal.vue';
import PromotionVoucherEditModal from '../../modal/PromotionVoucherEditModal.vue';
import PromotionVoucherDetailModal from '../../modal/PromotionVoucherDetailModal.vue';

const vouchers = ref([]);
const dotKhuyenMaiList = ref([]); // Danh sách để đổ vào Select
const totalPages = ref(0);
const showAddModal = ref(false);
const showEditModal = ref(false);
const showDetailModal = ref(false);
const editingVoucher = ref(null);
const selectedVoucher = ref(null);

const filters = reactive({
    keyword: '',
    status: '',
    dotKhuyenMaiId: '',
    page: 0,
    size: 5
});

// 1. Hàm load danh sách Đợt khuyến mãi cho Select box
const loadDotKhuyenMai = async () => {
    try {
        // Gọi API lấy toàn bộ đợt khuyến mãi
        const res = await PromotionVoucherService.getAll();
        // Lưu ý: Kiểm tra nếu API trả về res.data hoặc res trực tiếp
        dotKhuyenMaiList.value = res.data || res;
    } catch (error) {
        console.error("Lỗi load đợt khuyến mãi:", error);
    }
};

// 2. Hàm load danh sách Phiếu giảm giá (có lọc theo đợt)
const loadData = async () => {
    try {
        const response = await PromotionVoucherService.search(filters);
        vouchers.value = response.content || response.data?.content || [];
        totalPages.value = response.totalPages || response.data?.totalPages || 1;
    } catch (error) {
        console.error("Lỗi API loadData:", error);
    }
};

const changePage = (p) => {
    filters.page = p;
    loadData();
};

const onSaved = () => {
    showAddModal.value = false;
    showEditModal.value = false;
    loadData();
};

const handleEdit = (item) => {
    editingVoucher.value = { ...item };
    showEditModal.value = true;
};

const handleViewDetail = (item) => {
    selectedVoucher.value = { ...item };
    showDetailModal.value = true;
};

const toggleStatus = async (item) => {
    const oldStatus = item.trangThai;
    item.trangThai = oldStatus === 1 ? 0 : 1;
    try {
        await PromotionVoucherService.update(item.id, item);
    } catch (error) {
        item.trangThai = oldStatus;
        alert("Lỗi khi cập nhật trạng thái!");
    }
};

const formatDate = (dateString) => {
    if (!dateString) return '---';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
};

const isExpired = (dateString) => {
    if (!dateString) return false;
    return new Date(dateString) < new Date();
};

const resetFilters = () => {
    Object.assign(filters, { keyword: '', status: '', dotKhuyenMaiId: '', page: 0 });
    loadData();
};

const formatCurrency = (val) => new Intl.NumberFormat('vi-VN').format(val || 0) + 'đ';

onMounted(() => {
    loadData();
    loadDotKhuyenMai(); // Gọi hàm này khi component vừa load
});
</script>

<style scoped>
.promotion-campaign-container {
    padding: 20px;
}

.promotion-campaign-container {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 20px;
}

.filter-box {
    background: white;
    padding: 20px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
/* Container chính của bộ lọc */
.filter-row {
    display: flex;
    align-items: flex-end;
    gap: 15px; /* Khoảng cách giữa các phần tử */
    flex-wrap: nowrap;
}

.filter-item {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

/* Tùy chỉnh riêng cho ô Tìm kiếm để nó dài hơn các ô khác */
.search-group {
    flex: 1.5; 
}

.filter-item:not(.search-group) {
    flex: 1;
}

/* Bọc ô input và nút search */
.input-group {
    display: flex;
    position: relative;
    border: 1px solid #ddd;
    border-radius: 8px; /* Bo góc nhẹ */
    overflow: hidden;
}

.form-search {
    border: none !important; /* Bỏ border mặc định */
    flex: 1;
    padding: 10px 12px;
    outline: none;
}

.search-btn {
    background: #8B0000; /* Màu đỏ đô */
    color: white;
    border: none;
    padding: 0 15px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
}

/* Tùy chỉnh Select box */
select {
    padding: 10px 12px;
    border: 1px solid #ddd;
    border-radius: 8px;
    outline: none;
    background-color: white;
}

/* Nút xóa bộ lọc màu đỏ đô bo góc */
.btn-clear {
    background: #8B0000;
    color: white;
    border: none;
    padding: 10px 20px;
    border-radius: 8px;
    font-weight: 500;
    cursor: pointer;
    white-space: nowrap;
    height: 42px; /* Khớp chiều cao với input/select */
}

/* Căn chỉnh nút Thêm mới ở hàng dưới */
.action-row {
    display: flex;
    justify-content: flex-end;
    margin-top: 15px;
}

.btn-add {
    background: #8B0000;
    color: white;
    border: none;
    padding: 12px 25px;
    border-radius: 8px;
    font-weight: bold;
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 8px;
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
}

td {
    padding: 15px;
    border-bottom: 1px solid #eee;
    font-size: 14px;
}

.voucher-code {
    background: #fdf2f2;
    color: #8B0000;
    padding: 3px 6px;
    border-radius: 4px;
    font-weight: bold;
}

.status-active {
    color: #28a745;
    font-weight: bold;
}

.status-inactive {
    color: #dc3545;
    font-weight: bold;
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
    transition: 0.2s;
}

.btn-icon:hover {
    color: #8B0000;
}

.btn-edit:hover {
    color: #007bff;
}

/* Màu xanh khi hover vào nút sửa */

/* Toggle Switch */
.toggle-switch {
    width: 42px;
    height: 22px;
    background: #ccc;
    border-radius: 20px;
    position: relative;
    cursor: pointer;
}

.toggle-switch.on {
    background: #28a745;
}

.toggle-knob {
    width: 18px;
    height: 18px;
    background: white;
    border-radius: 50%;
    position: absolute;
    top: 2px;
    left: 2px;
    transition: 0.3s;
}

.toggle-switch.on .toggle-knob {
    left: 22px;
}

/* Pagination */
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

/* Thừa hưởng CSS cũ của bạn */
/* Thêm các tùy chỉnh cho Voucher */
.voucher-code {
    background: #f8d7da;
    color: #842029;
    padding: 3px 8px;
    border-radius: 4px;
    font-weight: bold;
    font-family: monospace;
}

.text-danger {
    color: #8B0000;
}

.small-text {
    font-size: 11px;
    color: #666;
}

.promotion-campaign-container {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 20px;
}

.filter-box {
    background: white;
    padding: 20px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-row {
    display: flex;
    gap: 20px;
    align-items: flex-end;
    flex-wrap: wrap;
}

.filter-item {
    display: flex;
    flex-direction: column;
    gap: 5px;
}

.filter-item label {
    font-size: 13px;
    color: #888;
    font-weight: bold;
}

.form-search,
select {
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 6px;
    outline: none;
}

.input-group {
    display: flex;
}

.search-btn {
    background: #8B0000;
    color: white;
    border: none;
    padding: 0 15px;
    border-radius: 0 6px 6px 0;
    cursor: pointer;
}

.btn-clear {
    background: #8B0000;
    color: white;
    border: none;
    padding: 9px 20px;
    border-radius: 6px;
    cursor: pointer;
    font-weight: 500;
}

.action-row {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 10px;
}

.btn-add {
    background: #8B0000;
    color: white;
    border: none;
    padding: 10px 20px;
    border-radius: 6px;
    font-weight: bold;
    cursor: pointer;
}

.table-container {
    background: white;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

table {
    width: 100%;
    border-collapse: collapse;
}

th {
    background-color: #8B0000;
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

.status-active {
    color: #28a745;
    font-weight: bold;
}

.status-inactive {
    color: #dc3545;
    font-weight: bold;
}

.actions {
    display: flex;
    align-items: center;
    gap: 15px;
}

.btn-icon {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 18px;
    color: #666;
}

.toggle-switch {
    width: 42px;
    height: 22px;
    background: #ccc;
    border-radius: 20px;
    position: relative;
    cursor: pointer;
    transition: 0.3s;
}

.toggle-switch.on {
    background: #28a745;
}

.toggle-knob {
    width: 18px;
    height: 18px;
    background: white;
    border-radius: 50%;
    position: absolute;
    top: 2px;
    left: 2px;
    transition: 0.3s;
}

.toggle-switch.on .toggle-knob {
    left: 22px;
}

.pagination {
    display: flex;
    justify-content: center;
    gap: 8px;
    margin-top: 20px;
}

.pagination button {
    padding: 8px 12px;
    border: 1px solid #ddd;
    background: white;
    border-radius: 4px;
    cursor: pointer;
}

.pagination button.active {
    background: #8B0000;
    color: white;
    border-color: #8B0000;
}

.filter-row {
    display: grid !important;
    grid-template-columns: 2fr 1fr 1fr auto;
    gap: 20px;
    align-items: end;
}
</style>
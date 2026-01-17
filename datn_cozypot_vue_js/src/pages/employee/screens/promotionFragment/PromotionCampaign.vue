<template>
    <div class="promotion-campaign-container">
        <div class="filter-box">
            <div class="filter-row">
                <div class="filter-item">
                    <label>Tìm kiếm</label>
                    <div class="input-group">
                        <input v-model="filters.keyword" type="text" placeholder="Tìm kiếm theo mã, tên..."
                            class="form-search" @keyup.enter="loadData">
                        <button class="search-btn" @click="loadData(0)">
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

                <div class="filter-item">
                    <label>Loại khuyến mãi</label>
                    <select v-model="filters.type" @change="loadData">
                        <option value="">Tất cả</option>
                        <option :value="1">Giảm theo %</option>
                        <option :value="2">Giảm theo số tiền</option>
                    </select>
                </div>

                <button class="btn-clear" @click="resetFilters">Xóa bộ lọc</button>
            </div>
        </div>

        <div class="action-row">
            <button class="btn-add" @click="showAddModal = true">
                <i class="fa-solid fa-plus"></i> Thêm đợt khuyến mãi
            </button>
        </div>

        <PromotionAddModal :is-open="showAddModal" @close="showAddModal = false" @save="handleCreatePromotion" />

        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>STT</th>
                        <th>MÃ ĐỢT KM</th>
                        <th>TÊN ĐỢT KM</th>
                        <th>LOẠI KM</th>
                        <th>ĐỐI TƯỢNG ÁP DỤNG</th>
                        <th>KHUNG GIỜ</th>
                        <th>TRẠNG THÁI</th>
                        <th>CHỨC NĂNG</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(item, index) in dotKhuyenMais" :key="item.id || index">
                        <td>{{ ((currentPage || 0) * 5) + index + 1 }}</td>
                        <td><b>{{ item.maDotKhuyenMai }}</b></td>
                        <td>{{ item.tenDotKhuyenMai }}</td>
                        <td>{{ formatLoaiKM(item.loaiKhuyenMai) }}</td>
                        <td>{{ item.doiTuongApDung || 'Tất cả' }}</td>
                        <td>{{ item.khungGioApDung || 'Cả ngày' }}</td>
                        <td>
                            <span :class="item.trangThai === 1 ? 'status-active' : 'status-inactive'">
                                {{ item.trangThai === 1 ? 'Đang hoạt động' : 'Ngưng hoạt động' }}
                            </span>
                        </td>
                        <td>
                            <div class="actions">
                                <button class="btn-icon btn-view" title="Xem chi tiết" @click="handleViewDetail(item)">
                                    <i class="fa-regular fa-eye"></i>
                                </button>
                                <button class="btn-icon btn-edit" title="Chỉnh sửa" @click="handleEdit(item)">
                                    <i class="fa-regular fa-pen-to-square"></i>
                                </button>
                                <div class="toggle-switch" :class="{ on: item.trangThai === 1 }"
                                    @click="toggleStatus(item)">
                                    <div class="toggle-knob"></div>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <tr v-if="dotKhuyenMais.length === 0">
                        <td colspan="8" style="text-align: center; color: #888; padding: 20px;">
                            Không có dữ liệu khuyến mãi
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="pagination">
            <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0">
                <i class="fa-solid fa-chevron-left"></i>
            </button>

            <button v-for="p in totalPages" :key="p" :class="{ active: currentPage === p - 1 }"
                @click="changePage(p - 1)">
                {{ p }}
            </button>

            <button @click="changePage(currentPage + 1)" :disabled="currentPage === totalPages - 1">
                <i class="fa-solid fa-chevron-right"></i>
            </button>
        </div>
        <PromotionEditModal :is-open="showEditModal" :data="editingPromotion" @close="showEditModal = false"
            @save="handleUpdatePromotion" />

        <PromotionDetailModal :is-open="showDetailModal" :data="selectedPromotion" @close="showDetailModal = false" />
    </div>
</template>

<script setup>
import PromotionAddModal from '../../modal/PromotionAddModal.vue';
import PromotionEditModal from '../../modal/PromotionEditModal.vue';

const showEditModal = ref(false);
const editingPromotion = ref(null);

// 3. Hàm xử lý khi nhấn icon Bút chì (Edit)
const handleEdit = (item) => {
    editingPromotion.value = { ...item }; // Copy dữ liệu dòng cần sửa
    showEditModal.value = true;
};

// 4. Hàm lưu dữ liệu sau khi sửa (Gửi về Backend)
const handleUpdatePromotion = async (updatedData) => {
    try {
        // Gọi API update(id, dto) từ Spring Boot
        await PromotionService.update(updatedData.id, updatedData);
        showEditModal.value = false;
        alert("Cập nhật thành công!");
        loadData(); // Tải lại bảng
    } catch (error) {
        alert("Cập nhật thất bại!");
    }
};
// --- State ---
const showAddModal = ref(false);

// Trong script setup của file view chính (nơi chứa handleCreatePromotion)
import { toast } from 'vue3-toastify';
import 'vue3-toastify/dist/index.css';

const handleCreatePromotion = async (newData) => {
    try {
        await PromotionService.create(newData);

        showAddModal.value = false;

        // Thông báo thành công góc trên bên phải
        toast.success("Thêm đợt khuyến mãi thành công!", {
            autoClose: 3000,
            position: toast.POSITION.TOP_RIGHT,
        });

        loadData();
    } catch (error) {
        console.error("Lỗi:", error);

        // Thông báo thất bại
        toast.error("Thêm thất bại: " + (error.response?.data || "Lỗi hệ thống"), {
            autoClose: 3000,
            position: toast.POSITION.TOP_RIGHT,
        });
    }
};
import { ref, reactive, onMounted } from 'vue';
import { PromotionService } from '../../../../services/PromotionService';
// Import Modal đúng đường dẫn từ ảnh cấu trúc file của bạn
import PromotionDetailModal from '../../modal/PromotionDetailModal.vue';
// --- Khai báo State ---
const dotKhuyenMais = ref([]);
const showDetailModal = ref(false);
const selectedPromotion = ref(null);
const filters = reactive({
    keyword: '',
    status: '',
    type: ''
});

const totalPages = ref(0);
const currentPage = ref(0);

const loadData = async (page = 0) => {
    try {
        currentPage.value = page; // Quan trọng để hiển thị đúng số trang và STT

        const response = await PromotionService.search(filters, page, 5);

        if (response && response.content) {
            dotKhuyenMais.value = response.content;
            totalPages.value = response.totalPages;
        } else {
            dotKhuyenMais.value = [];
            totalPages.value = 0;
        }
    } catch (error) {
        console.error("Lỗi:", error);
        dotKhuyenMais.value = [];
        totalPages.value = 0;
    }
};

// Hàm chuyển trang
const changePage = (p) => {
    if (p >= 0 && p < totalPages.value) {
        loadData(p);
    }
};


const handleViewDetail = (item) => {
    // Gán dữ liệu của dòng vừa click vào selectedPromotion
    selectedPromotion.value = { ...item };
    showDetailModal.value = true;
};

const resetFilters = () => {
    filters.keyword = '';
    filters.status = '';
    filters.type = '';
    loadData(0); // Truyền 0 vào đây để reset trang và STT
};

const toggleStatus = async (item) => {
    const oldStatus = item.trangThai;
    const newStatus = oldStatus === 1 ? 0 : 1;
    try {
        item.trangThai = newStatus;
        const updatedDto = { ...item, trangThai: newStatus };
        await PromotionService.update(item.id, updatedDto);
    } catch (error) {
        item.trangThai = oldStatus;
        alert("Cập nhật trạng thái thất bại!");
    }
};

const formatLoaiKM = (type) => {
    if (type === 1) return 'Giảm %';
    if (type === 2) return 'Giảm tiền trực tiếp';
    return 'N/A';
};

onMounted(() => {
    loadData();
});
</script>

<style scoped>
/* Giữ nguyên phần CSS của bạn */
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
</style>
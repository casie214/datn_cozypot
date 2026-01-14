<script setup>
import { ref } from 'vue';
import foodModal from '../implementations/Modal/FoodDetailModal.vue';

const mockData = ref([
    { stt: 1, ma: 'BA01', ten: 'Viên thả lẩu phô mai', gia: '40.000 - 60.000 VNĐ', danhmuc: 'Đồ nhúng lẩu', chitiet: '-', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1', trangthai: true },
    { stt: 2, ma: 'BA02', ten: 'Coca-Cola', gia: '18.000 - 26.000 VNĐ', danhmuc: 'Đồ uống', chitiet: 'Đồ uống đóng chai', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-2', trangthai: false },
    { stt: 3, ma: 'BA03', ten: 'Fanta', gia: '0987654321', danhmuc: 'Đồ uống', chitiet: 'Đồ uống đóng chai', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-2', trangthai: true },
    { stt: 4, ma: 'BA04', ten: 'Nguyễn Văn E', gia: '0987654321', danhmuc: 'Đồ uống', chitiet: 'Đồ uống đóng chai', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-2', trangthai: true },
    { stt: 5, ma: 'BA05', ten: 'Nguyễn Văn F', gia: '0987654321', danhmuc: 'Đồ uống', chitiet: 'Đồ uống đóng chai', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-2', trangthai: true },
    { stt: 6, ma: 'BA06', ten: 'Nguyễn Văn G', gia: '0987654321', danhmuc: 'Đồ uống', chitiet: 'Đồ uống đóng chai', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-2', trangthai: true },
    { stt: 7, ma: 'BA07', ten: 'Nguyễn Văn H', gia: '0987654321', danhmuc: 'Đồ uống', chitiet: 'Đồ uống đóng chai', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-8', trangthai: false },
    { stt: 8, ma: 'BA08', ten: 'Nguyễn Văn Y', gia: '0987654321', danhmuc: 'Đồ uống', chitiet: 'Đồ uống đóng chai', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-010', trangthai: true },
]);

// Tabs
const activeTab = ref('thucdon');

const isModalOpen = ref(false);
const selectedItem = ref(null);

const handleViewDetails = (item) => {
    selectedItem.value = item;
    isModalOpen.value = true;
};
</script>

<template>


    <div class="filter-box">
        <div class="filter-row">
            <div class="filter-item search">
                <label>Tìm kiếm</label>
                <div class="input-group">
                    <input class="form-control form-search" type="text" placeholder="Tìm kiếm món ăn (mã, tên)" />
                    <button class="search-btn">🔍</button>
                </div>
            </div>
            <div class="filter-item">
                <label>Trạng thái</label>
                <select>
                    <option>Tất cả</option>
                </select>
            </div>
            <div class="filter-item">
                <label>Danh mục</label>
                <select>
                    <option>Tất cả</option>
                </select>
            </div>
            <div class="filter-item">
                <label>Chi tiết danh mục</label>
                <select>
                    <option>Tất cả</option>
                </select>
            </div>
            <div class="filter-item">
                <label>Người tạo</label>
                <select>
                    <option>Tất cả</option>
                </select>
            </div>
            <div class="filter-item">
                <label>Lọc theo</label>
                <select>
                    <option>Số thứ tự giảm dần</option>
                </select>
            </div>
            <button class="btn-clear">Xóa bộ lọc</button>
        </div>


    </div>
    <div class="action-row">
        <button class="btn-add">Thêm món ăn</button>
    </div>

    <div class="table-container">
        <table>
            <thead>
                <tr>
                    <th>STT</th>
                    <th>MÃ</th>
                    <th>MÓN ĂN</th>
                    <th>GIÁ BÁN</th>
                    <th>DANH MỤC</th>
                    <th>CHI TIẾT</th>
                    <th>NGÀY GIỜ TẠO</th>
                    <th>NGƯỜI TẠO</th>
                    <th>TRẠNG THÁI</th>
                    <th>CHỨC NĂNG</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="item in mockData" :key="item.ma">
                    <td style="text-align: center;">{{ item.stt }}</td>
                    <td>{{ item.ma }}</td>
                    <td>{{ item.ten }}</td>
                    <td>{{ item.gia }}</td>
                    <td>{{ item.danhmuc }}</td>
                    <td>{{ item.chitiet }}</td>
                    <td>{{ item.tao }}</td>
                    <td>{{ item.nguoi }}</td>
                    <td :class="item.trangthai ? 'status-active' : 'status-inactive'">
                        {{ item.trangthai ? 'Đang hoạt động' : 'Ngưng hoạt động' }}
                    </td>
                    <td class="actions">
                        <button class="btn-icon" @click="handleViewDetails(item)">👁️</button>
                        <div class="toggle-switch" :class="{ 'on': item.trangthai }">
                            <div class="toggle-knob"></div>
                        </div>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <div class="pagination">
        <button>&lt;</button>
        <button class="active">1</button>
        <button>2</button>
        <button>...</button>
        <button>7</button>
        <button>8</button>
        <button>&gt;</button>
    </div>

    <foodModal :isOpen="isModalOpen" :foodItem="selectedItem" @close="isModalOpen = false" />
</template>

<style scoped>
.main-content {
    flex: 1;
    padding: 20px 40px;
    background-color: #f9f9f9;
    overflow-y: auto;
}

.page-title {
    color: #8B0000;
    margin-bottom: 20px;
}

/* Tabs */
.tabs {
    border-bottom: 1px solid #ddd;
    margin-bottom: 20px;
}

.tabs button {
    background: none;
    border: none;
    padding: 10px 20px;
    font-size: 16px;
    cursor: pointer;
    color: #666;
}

.tabs button.active {
    color: #8B0000;
    border-bottom: 3px solid #8B0000;
    font-weight: bold;
}

/* Filter */
.filter-box {
    background: white;
    padding: 15px;
    border-radius: 8px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
    margin-bottom: 20px;
}

.filter-row {
    display: flex;
    gap: 15px;
    align-items: flex-end;
    flex-wrap: wrap;
}

.filter-item {
    display: flex;
    flex-direction: column;
}

.filter-item label {
    font-size: 12px;
    color: #999;
    margin-bottom: 5px;
}

.filter-item select,
.filter-item input {
    padding: 8px;
    border: 1px solid #ddd;
    border-radius: 4px;
    min-width: 120px;
}

.input-group {
    display: flex;
}

.search-btn {
    background: #8B0000;
    color: white;
    border: none;
    padding: 0 10px;
    border-radius: 0 4px 4px 0;
    cursor: pointer;
}

.form-search {
    width: 20em;
}

.btn-clear {
    background: #8B0000;
    color: white;
    border: none;
    padding: 8px 15px;
    border-radius: 4px;
    cursor: pointer;
    height: inherit;
    text-align: center;
}

.action-row {
    display: flex;
    justify-content: flex-end;
    margin-top: 15px;
    margin-bottom: 15px;
}

.btn-add {
    background-color: #8B0000;
    color: white;
    border: none;
    padding: 10px 20px;
    border-radius: 4px;
    font-weight: bold;
    cursor: pointer;
}

.table-container {
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
    overflow: hidden;
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
    font-size: 12px;
    text-transform: uppercase;
}

td {
    padding: 12px 15px;
    border-bottom: 1px solid #eee;
    font-size: 14px;
    color: #333;
}

.status-active {
    color: green;
    font-weight: 500;
}

.status-inactive {
    color: red;
    font-weight: 500;
}

.actions {
    display: flex;
    align-items: center;
    gap: 10px;
}

.btn-icon {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 16px;
}

.toggle-switch {
    width: 36px;
    height: 20px;
    background-color: #ccc;
    border-radius: 20px;
    position: relative;
    cursor: pointer;
}

.toggle-switch.on {
    background-color: #4CAF50;
}

.toggle-knob {
    width: 16px;
    height: 16px;
    background: white;
    border-radius: 50%;
    position: absolute;
    top: 2px;
    left: 2px;
    transition: 0.3s;
}

.toggle-switch.on .toggle-knob {
    left: 18px;
}

.pagination {
    display: flex;
    justify-content: center;
    margin-top: 20px;
    gap: 5px;
}

.pagination button {
    width: 35px;
    height: 35px;
    border: 1px solid #ddd;
    background: white;
    cursor: pointer;
}

.pagination button.active {
    background: #8B0000;
    color: white;
    border-color: #8B0000;
}
</style>
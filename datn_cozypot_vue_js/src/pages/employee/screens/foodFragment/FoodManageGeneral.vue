<script setup>
import { Alert } from 'bootstrap/dist/js/bootstrap.bundle.min';
import foodModal from '../../modal/FoodUpdateModals/foodModal.vue';

import { useFoodManager } from '../../../../services/foodFunction';
import FoodAddModal from '../../modal/FoodAddModals/FoodAddModal.vue';

const {
    mockData,
    activeTab,
    isModalOpen,
    selectedItem,
    isAddFoodModalOpen,
    handleViewDetails,
    getAllFood,
    handleToggleStatus
} = useFoodManager();

const handleRefreshList = () => {
    setTimeout(() => {
        getAllFood();
    }, 500);
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
        <button class="btn-add" @click="isAddFoodModalOpen = true">Thêm món ăn</button>
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
                <tr v-for="(item, index) in mockData" :key="item.id">
                    <td>{{ index + 1 }}</td>
                    <td>{{ item.maMonAn }}</td>
                    <td>{{ item.tenMonAn }}</td>
                    <td>{{ item.giaBan }}</td>
                    <td>{{ item.tenDanhMuc }}</td>
                    <td>{{ item.tenDanhMucChiTiet }}</td>
                    <td>{{ item.ngayTao }}</td>
                    <td>{{ item.nguoiTao }}</td>
                    <td :class="item.trangThaiKinhDoanh === 1 ? 'status-active' : 'status-inactive'">
                        {{ item.trangThaiKinhDoanh === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
                    </td>
                    <td class="actions">
                        <button class="btn-icon" @click="handleViewDetails(item)">👁️</button>

                        <div class="toggle-switch" :class="{ 'on': item.trangThaiKinhDoanh === 1 }"
                            @click.stop="handleToggleStatus(item)">
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

    <foodModal v-if="isModalOpen && selectedItem" :isOpen="isModalOpen" :foodItem="selectedItem"
        @close="isModalOpen = false" @refresh="handleRefreshList" />

    <FoodAddModal v-if="isAddFoodModalOpen" :isOpen="isAddFoodModalOpen" @close="isAddFoodModalOpen = false"
        @refresh="handleRefreshList" />
</template>

<style scoped src="../foodFragment/foodManager.css"></style>
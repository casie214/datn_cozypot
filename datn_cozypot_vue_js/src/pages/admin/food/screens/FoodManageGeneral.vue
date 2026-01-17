<script setup>
import foodModal from '../../food/modal/updateModal/foodModal.vue';

import { useFoodManager } from '../../../../services/foodFunction';
import FoodAddModal from '../../food/modal/addModal/FoodAddModal.vue';

const {
    mockData,
    paginatedData,
    activeTab,
    isModalOpen,
    selectedItem,
    isAddFoodModalOpen,
    itemsPerPage,
    currentPage,
    totalPages,
    visiblePages,
    goToPage,
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
            <input type="text" class="form-search form-control" placeholder="Tìm kiếm set lẩu (mã, tên)" />
            <button class="search-btn">🔍</button>
          </div>
        </div>
        <div class="filter-item"><label>Trạng thái</label><select>
            <option>Tất cả</option>
          </select></div>
        <div class="filter-item"><label>Loại set lẩu</label><select>
            <option>Tất cả</option>
          </select></div>
        <div class="filter-item"><label>Người tạo</label><select>
            <option>Tất cả</option>
          </select></div>
        <div class="filter-item"><label>Lọc theo</label><select>
            <option>Số thứ tự tăng dần</option>
          </select></div>
        <button class="btn-clear">Xóa bộ lọc</button>
      </div>

    </div>
    <div class="action-row">
        <button class="btn-add" @click="isAddFoodModalOpen = true">Thêm món ăn</button>
    </div>

    <div class="table-container" style="min-height: 305px;">
        <table>
            <thead>
                <tr>
                    <th>STT</th>
                    <th>MÃ</th>
                    <th>MÓN ĂN</th>
                    <th>GIÁ BÁN</th>
                    <th>DANH MỤC</th>
                    <th>CHI TIẾT</th>
                    <th>TRẠNG THÁI</th>
                    <th>CHỨC NĂNG</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="(item, index) in paginatedData" :key="item.id">
                    <td>{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>

                    <td>{{ item.maMonAn }}</td>
                    <td>{{ item.tenMonAn }}</td>
                    <td>{{ item.giaBan?.toLocaleString() }} đ</td>
                    <td>{{ item.tenDanhMuc }}</td>
                    <td>{{ item.tenDanhMucChiTiet }}</td>

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
                <tr v-if="paginatedData.length === 0">
                    <td colspan="8" style="text-align: center; padding: 20px;">Không tìm thấy dữ liệu</td>
                </tr>
            </tbody>
        </table>
    </div>

    <div class="pagination" v-if="totalPages > 1">
        <button @click="goToPage(currentPage - 1)" :disabled="currentPage === 1"
            :class="{ 'disabled': currentPage === 1 }">
            &lt;
        </button>

        <button v-for="(page, index) in visiblePages" :key="index"
            :class="{ 'active': page === currentPage, 'dots': page === '...' }"
            @click="page !== '...' ? goToPage(page) : null" :disabled="page === '...'">
            {{ page }}
        </button>

        <button @click="goToPage(currentPage + 1)" :disabled="currentPage === totalPages"
            :class="{ 'disabled': currentPage === totalPages }">
            &gt;
        </button>
    </div>

    <foodModal v-if="isModalOpen && selectedItem" :isOpen="isModalOpen" :foodItem="selectedItem"
        @close="isModalOpen = false" @refresh="handleRefreshList" />

    <FoodAddModal v-if="isAddFoodModalOpen" :isOpen="isAddFoodModalOpen" @close="isAddFoodModalOpen = false"
        @refresh="handleRefreshList" />
</template>

<style scoped src="/src/assets/foodManager.css"></style>
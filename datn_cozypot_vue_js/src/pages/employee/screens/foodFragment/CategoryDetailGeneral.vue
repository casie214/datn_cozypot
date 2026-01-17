<script setup>
import { useCategoryDetailManager } from '../../../../services/foodFunction';
import CategoryDetailAddModal from '../../modal/FoodAddModals/CategoryDetailAddModal.vue';
import CategoryDetailPutModal from '../../modal/FoodUpdateModals/CategoryDetailPutModal.vue';
import CategoryPutModal from '../../modal/FoodUpdateModals/CategoryPutModal.vue';

const { detailData, isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, getAllCategoriesDetail } = useCategoryDetailManager();

const handleRefreshList = () => {
    setTimeout(() => {
        getAllCategoriesDetail();
    }, 500);
};
</script>

<template>
  <div class="tab-content">
    <div class="filter-box">
       <div class="filter-row">
            <div class="filter-item search">
                <label>Tìm kiếm</label>
                <div class="input-group">
                    <input class="form-control form-search" type="text" placeholder="Tìm kiếm danh mục chi tiết (mã, tên)" />
                    <button class="search-btn">🔍</button>
                </div>
            </div>
            <div class="filter-item">
                <label>Danh mục</label>
                <select>
                    <option>Tất cả</option>
                </select>
            </div>
            <div class="filter-item">
                <label>Trạng thái</label>
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

    <div class="action-row" style="margin-left: auto;">
             <button class="btn-add" @click="isModalOpen = true">+ Thêm chi tiết</button>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>MÃ CHI TIẾT</th>
            <th>TÊN CHI TIẾT</th>
            <th>DANH MỤC GỐC (FK)</th>
            <th>MÔ TẢ</th>
            <th>TRẠNG THÁI</th>
            <th>CHỨC NĂNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in detailData" :key="item.id">
            <td>{{ index + 1 }}</td>
            <td>{{ item.maDanhMucChiTiet }}</td>
            <td><b>{{ item.tenDanhMucChiTiet }}</b></td>
            <td style="color: #8B0000; font-weight: 500;">{{ item.tenDanhMuc }}</td>
            <td>{{ item.moTa }}</td>
            <td :class="item.trangThai === 1 ? 'status-active' : 'status-inactive'">
              {{ item.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
            </td>
            <td class="actions">
              <button class="btn-icon" @click="openModal(item)">✏️</button>
              <div class="toggle-switch" :class="{ 'on': item.trangThai === 1 }"
                            @click.stop="handleToggleStatus(item)">
                            <div class="toggle-knob"></div>
                        </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <CategoryDetailAddModal 
            :isOpen="isModalOpen" 
            :formData="addFormData" 
            @close="isModalOpen = false" 
            @save="handleAdd" 
            @refresh="handleRefreshList"
        />

    <CategoryDetailPutModal 
            :isOpen="isModalUpdateOpen" 
            :formData="addFormData" 
            :itemList="selectedItem"
            @close="isModalUpdateOpen = false" 
            @save="handleAdd" 
            @refresh="handleRefreshList"
        />
    </div>
</template>

<style scoped src="../foodFragment/foodManager.css"></style>
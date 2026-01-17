<script setup>
import { useHotpotSetTypeManager } from '../../../../services/foodFunction';
import CategoryHotpotAddModal from '../../modal/FoodAddModals/CategoryHotpotAddModal.vue';
import CategoryHotpotPutModal from '../../modal/FoodUpdateModals/CategoryHotpotPutModal.vue';
import CategoryPutModal from '../../modal/FoodUpdateModals/CategoryPutModal.vue';

const { hotpotTypeData, isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, getAllHotpotType } = useHotpotSetTypeManager();
const handleRefreshList = () => {
    setTimeout(() => {
        getAllHotpotType();
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
            <input class="form-control form-search" type="text" placeholder="Tìm kiếm loại lẩu (mã, tên)" />
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
      <button class="btn-add" @click="isModalOpen = true">+ Thêm loại set</button>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>MÃ LOẠI</th>
            <th>TÊN LOẠI SET</th>
            <th>MÔ TẢ</th>
            <th>TRẠNG THÁI</th>
            <th>CHỨC NĂNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in hotpotTypeData" :key="item.id">
            <td align="center">{{ index + 1 }}</td>
            <td>{{ item.maLoaiSet }}</td>
            <td><b>{{ item.tenLoaiSet }}</b></td>
            <td>{{ item.moTa }}</td>
            <td :class="item.trangThai === 1 ? 'status-active' : 'status-inactive'">
              {{ item.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
            </td>
            <td class="actions">
              <button class="btn-icon" @click="openModal(item)">✏️</button>
              <div class="toggle-switch" :class="{ 'on': item.trangThai === 1 }" @click.stop="handleToggleStatus(item)">
                <div class="toggle-knob"></div>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <CategoryHotpotAddModal 
            :isOpen="isModalOpen" 
            :formData="addFormData" 
            @close="isModalOpen = false" 
            @save="handleAdd" 
            @refresh="handleRefreshList"
        />
    <CategoryHotpotPutModal 
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
<script setup>
import FoodDetailAddModal from '../../food/modal/addModal/FoodDetailAddModal.vue';
import CategoryDetailModal from '../../food/modal/updateModal/foodDetailModal.vue';

import { useFoodDetailManager } from '../../../../services/foodFunction';

const {
  detailData,
  isModalOpen,
  isAddModalOpen,
  selectedDetail,
  openAddModal,
  getAllFoodDetails,
  openEditModal,
  handleSaveData,
  handleToggleStatus
} = useFoodDetailManager();

const handleRefreshList = () => {
  setTimeout(() => {
    getAllFoodDetails();
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
            <input type="text" class="form-search form-control" placeholder="Tìm chi tiết (mã, tên)" />
            <button class="search-btn">🔍</button>
          </div>
        </div>
        <div class="filter-item"><label>Trạng thái</label><select>
            <option>Tất cả</option>
          </select></div>
        <div class="filter-item"><label>Thuộc món ăn</label><select>
            <option>Tất cả</option>
          </select></div>
      </div>

    </div>

    <div class="action-row">
      <button class="btn-add" @click="isAddModalOpen = true">+ Thêm chi tiết món</button>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>MÃ CHI TIẾT</th>
            <th>TÊN CHI TIẾT</th>
            <th>MÓN ĂN GỐC</th>
            <th>GIÁ BÁN</th>
            <th>KÍCH CỠ</th>
            <th>ĐƠN VỊ</th>
            <th>TRẠNG THÁI</th>
            <th>CHỨC NĂNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in detailData" :key="item.id">
            <td align="center">{{ index + 1 }}</td>
            <td>{{ item.maChiTietMonAn }}</td>
            <td><b>{{ item.tenChiTietMonAn }}</b></td>
            <td>{{ item.tenMonAnDiKem }}</td>
            <td style="color:#d32f2f; font-weight:bold">{{ item.giaBan }}</td>
            <td>{{ item.kichCo }}</td>
            <td>{{ item.donVi }}</td>
            <td :class="item.trangThai ? 'status-active' : 'status-inactive'">
              {{ item.trangThai ? 'Đang hoạt động' : 'Ngưng bán' }}
            </td>
            <td class="actions">
              <button class="btn-icon" @click="openEditModal(item)">✏️</button>
              <div class="toggle-switch" :class="{ 'on': item.trangThai === 1 }" @click.stop="handleToggleStatus(item)">
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


    <CategoryDetailModal :isOpen="isModalOpen" :detailItem="selectedDetail" @close="isModalOpen = false"
      @save="handleSaveData" @refresh="handleRefreshList" />

    <FoodDetailAddModal v-if="isAddModalOpen" :isOpen="isAddModalOpen" @close="isAddModalOpen = false"
      @refresh="handleRefreshList" />

  </div>
</template>

<style scoped src="/src/assets/foodManager.css"></style>
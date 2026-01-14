<script setup>
import CategoryDetailModal from '../../modal/foodDetailModal.vue';

import { useFoodDetailManager } from '../foodFunction';

const { 
  detailData, 
  isModalOpen, 
  selectedDetail, 
  openAddModal, 
  openEditModal, 
  handleSaveData 
} = useFoodDetailManager();
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
        <div class="filter-item"><label>Trạng thái</label><select><option>Tất cả</option></select></div>
        <div class="filter-item"><label>Thuộc món ăn</label><select><option>Tất cả</option></select></div>
      </div>
      
    </div>

    <div class="action-row">
        <button class="btn-add" @click="openAddModal">+ Thêm chi tiết món</button>
      </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>MÃ CHI TIẾT</th>
            <th>TÊN CHI TIẾT</th>
            <th>MÓN ĂN GỐC (FK)</th>
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
            <td>{{ item.ma }}</td>
            <td><b>{{ item.ten }}</b></td>
            <td>{{ item.monAnTen }}</td> <td style="color:#d32f2f; font-weight:bold">{{ item.gia }}</td>
            <td>{{ item.kichCo }}</td>
            <td>{{ item.donVi }}</td>
            <td :class="item.trangThai ? 'status-active' : 'status-inactive'">
              {{ item.trangThai ? 'Đang hoạt động' : 'Ngưng' }}
            </td>
            <td class="actions">
              <button class="btn-icon" @click="openEditModal(item)">✏️</button>
              <button class="btn-icon">🗑️</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <CategoryDetailModal 
      :isOpen="isModalOpen"
      :detailItem="selectedDetail"
      @close="isModalOpen = false"
      @save="handleSaveData"
    />

  </div>
</template>

<style scoped src="../foodFragment/foodManager.css">

</style>
<script setup>
import { ref } from 'vue';
import { useHotpotManager } from '../foodFunction';
import FoodHotpotModal from '../../modal/foodHotpotModal.vue';

const { 
  hotpotData, 
  isModalOpen, 
  selectedHotpot, 
  handleViewDetails 
} = useHotpotManager();

</script>

<template>
  <div class="tab-content">
    <div class="filter-box">
      <div class="filter-row">
        <div class="filter-item search">
          <label>Tìm kiếm</label>
          <div class="input-group">
            <input type="text" class="form-search form-control" placeholder="Tìm kiếm set lẩu (mã, tên)" />
            <button class="search-btn">🔍</button>
          </div>
        </div>
        <div class="filter-item"><label>Trạng thái</label><select><option>Tất cả</option></select></div>
        <div class="filter-item"><label>Loại set lẩu</label><select><option>Tất cả</option></select></div> <div class="filter-item"><label>Người tạo</label><select><option>Tất cả</option></select></div>
        <div class="filter-item"><label>Lọc theo</label><select><option>Số thứ tự giảm dần</option></select></div>
        <button class="btn-clear">Xóa bộ lọc</button>
      </div>
      
    </div>

    <div class="action-row">
        <button class="btn-add">Thêm set lẩu</button> </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>STT</th> <th>MÃ</th> <th>SET LẨU</th> <th>GIÁ BÁN</th> <th>LOẠI LẨU</th> <th>NGÀY TẠO</th> <th>NGƯỜI TẠO</th> <th>TRẠNG THÁI</th> <th>CHỨC NĂNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in hotpotData" :key="item.ma">
            <td align="center">{{ item.stt }}</td>
            <td>{{ item.ma }}</td>
            <td><b>{{ item.ten }}</b></td>
            <td style="color:#d32f2f"><b>{{ item.gia }}</b></td>
            <td>{{ item.loailau }}</td> <td>{{ item.tao }}</td>
            <td>{{ item.nguoi }}</td>
            <td :class="item.trangthai ? 'status-active' : 'status-inactive'">{{ item.trangthai ? 'Đang hoạt động' : 'Ngưng' }}</td>
            <td class="actions">
               <button class="btn-icon" @click="handleViewDetails(item)">👁️</button>
               <div class="toggle-switch" :class="{ 'on': item.trangthai }"><div class="toggle-knob"></div></div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <FoodHotpotModal
      :isOpen="isModalOpen" 
      :hotpotItem="selectedHotpot" 
      @close="isModalOpen = false" 
    />
</template>

<style scoped src="../foodFragment/foodManager.css">

</style>
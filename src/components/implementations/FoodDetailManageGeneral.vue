<script setup>
import { ref } from 'vue';
import CategoryDetailModal from '../../components/implementations/Modal/CFoodDetailModal.vue';

const detailData = ref([
  { id: 1, ma: 'CCLA-11-15', ten: 'Chai 1.5 L', monAnGoc: 1, monAnTen: 'Coca-Cola', gia: '18.000', kichCo: '1.5L', donVi: 'Chai', trangThai: true },
  { id: 2, ma: 'CCLA-11-18', ten: 'Chai 1.8 L', monAnGoc: 1, monAnTen: 'Coca-Cola', gia: '20.000', kichCo: '1.8L', donVi: 'Chai', trangThai: false },
]);

const isModalOpen = ref(false);
const selectedDetail = ref(null);

const openAddModal = () => {
  selectedDetail.value = null;
  isModalOpen.value = true;
};

const openEditModal = (item) => {
  selectedDetail.value = item; 
  isModalOpen.value = true;
};

const handleSaveData = (data) => {
  console.log("Dữ liệu đã lưu:", data);
  isModalOpen.value = false;
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

/* Table */
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
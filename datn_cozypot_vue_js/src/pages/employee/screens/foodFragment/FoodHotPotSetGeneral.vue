<script setup>
import { ref } from 'vue';
import { useHotpotManager } from '../../../../services/foodFunction';
import FoodHotpotModal from '../../modal/FoodUpdateModals/foodHotpotModal.vue';
import FoodHotpotAddModal from '../../modal/FoodAddModals/FoodHotpotAddModal.vue';

const {
  hotpotData,
  isModalOpen,
  selectedHotpot,
  isAddHotpotModalOpen,
  handleViewDetails,
  handleToggleStatus,
  getAllHotpot
} = useHotpotManager();

const handleRefreshList = () => {
    setTimeout(() => {
        getAllHotpot();
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
            <option>Số thứ tự giảm dần</option>
          </select></div>
        <button class="btn-clear">Xóa bộ lọc</button>
      </div>

    </div>

    <div class="action-row">
      <button class="btn-add" @click="isAddHotpotModalOpen = true">Thêm set lẩu</button>
    </div>

    <div class="table-container">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>MÃ</th>
            <th>SET LẨU</th>
            <th>GIÁ BÁN</th>
            <th>LOẠI LẨU</th>
            <th>NGÀY TẠO</th>
            <th>NGƯỜI TẠO</th>
            <th>TRẠNG THÁI</th>
            <th>CHỨC NĂNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in hotpotData" :key="item.ma">
            <td align="center">{{ index + 1 }}</td>
            <td>{{ item.maSetLau }}</td>
            <td><b>{{ item.tenSetLau }}</b></td>
            <td style="color:#d32f2f"><b>{{ item.giaBan }}</b></td>
            <td>{{ item.tenLoaiSet }}</td>
            <td>{{ item.ngayTao }}</td>
            <td>{{ item.nguoiTao }}</td>
            <td :class="item.trangThai === 1 ? 'status-active' : 'status-inactive'">
              {{ item.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
            </td>
            <td class="actions">
              <button class="btn-icon" @click="handleViewDetails(item)">👁️</button>

              <div class="toggle-switch" :class="{ 'on': item.trangThai === 1 }"
                            @click.stop="handleToggleStatus(item)">
                            <div class="toggle-knob"></div>
                        </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
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


  <FoodHotpotModal :isOpen="isModalOpen" :hotpotItem="selectedHotpot" @close="isModalOpen = false" @refresh="handleRefreshList" />

  <FoodHotpotAddModal v-if="isAddHotpotModalOpen" :isOpen="isAddHotpotModalOpen" @close="isAddHotpotModalOpen = false"
        @refresh="handleRefreshList" />
</template>

<style scoped src="../foodFragment/foodManager.css"></style>
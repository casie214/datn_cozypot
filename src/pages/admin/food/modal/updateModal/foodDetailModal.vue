<script setup>
import { defineProps, defineEmits } from 'vue';
import { useFoodDetailModal } from '../../../../../services/foodFunction'; 

const props = defineProps({
  isOpen: Boolean,
  detailItem: Object
});

const emit = defineEmits(['close', 'save', 'refresh']); // Thêm refresh

const { 
  formData, 
  listMonAn, 
  handleSave 
} = useFoodDetailModal(props, emit);


</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-content">
      
      <div class="modal-header">
        <h2>Cập nhật chi tiết món</h2>
        <button class="btn-close" @click="emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-grid">
          
          <div class="form-group">
            <label>Mã chi tiết</label>
            <input v-model="formData.maChiTietMonAn" type="text" disabled style="background: #eee; cursor: not-allowed;">
          </div>

          <div class="form-group">
            <label>Tên chi tiết <span class="required">*</span></label>
            <input v-model="formData.tenChiTietMonAn" type="text" placeholder="VD: Chai 1.5 L">
          </div>

          <div class="form-group">
            <label>Thuộc món ăn</label>
            <select v-model="formData.idMonAnDiKem" class="form-control">
              <option value="" >-- Chọn món ăn --</option>
              <option v-for="mon in listMonAn" :key="mon.id" :value="mon.id">
                  {{ mon.tenMonAn || mon.name }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>Giá bán</label>
            <input v-model.number="formData.giaBan" type="number" placeholder="VD: 18000">
          </div>

          <div class="form-group">
            <label>Giá vốn</label>
            <input v-model.number="formData.giaVon" type="number" placeholder="VD: 12000">
          </div>

          <div class="form-group">
            <label>Kích cỡ</label>
            <input v-model="formData.kichCo" type="text" placeholder="VD: Lớn, Nhỏ">
          </div>

          <div class="form-group">
            <label>Đơn vị tính</label>
            <input v-model="formData.donVi" type="text" placeholder="VD: Chai, Lon">
          </div>

          <div class="form-group full-width">
            <label>Trạng thái kinh doanh</label>
             <div class="toggle-wrapper">
                <span>{{ formData.trangThai === 1 ? 'Đang hoạt động' : 'Ngưng hoạt động' }}</span>
                
                <div class="toggle-switch" 
                     :class="{ 'on': formData.trangThai === 1 }" 
                     @click="formData.trangThai = (formData.trangThai === 1 ? 0 : 1)">
                  <div class="toggle-knob"></div>
                </div>
             </div>
          </div>

        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="emit('close')">Hủy</button>
        <button class="btn-confirm" @click="handleSave">
          Lưu thay đổi
        </button>
      </div>

    </div>
  </div>
</template>

<style scoped src="/src/assets/foodModalManager.css">
</style>
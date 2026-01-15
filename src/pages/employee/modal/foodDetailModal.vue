<script setup>
import { defineProps, defineEmits } from 'vue';
import { useCategoryDetailModal } from '../screens/foodFunction'; 

const props = defineProps({
  isOpen: Boolean,
  detailItem: Object
});

const emit = defineEmits(['close', 'save']);

const { 
  formData, 
  listMonAn, 
  handleSave 
} = useCategoryDetailModal(props, emit);
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-content">
      
      <div class="modal-header">
        <h2>{{ detailItem ? 'Cập nhật chi tiết món' : 'Thêm chi tiết món mới' }}</h2>
        <button class="btn-close" @click="emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-grid">
          
          <div class="form-group">
            <label>Mã chi tiết <span class="required">*</span></label>
            <input v-model="formData.ma" type="text" placeholder="VD: CCLA-11-15">
          </div>
          <div class="form-group">
            <label>Tên chi tiết <span class="required">*</span></label>
            <input v-model="formData.ten" type="text" placeholder="VD: Chai 1.5 L">
          </div>

          <div class="form-group">
            <label>Thuộc món ăn (FK)</label>
            <select v-model="formData.monAnGoc" class="form-control">
              <option value="">-- Chọn món ăn --</option>
              <option v-for="mon in listMonAn" :key="mon.id" :value="mon.id">{{ mon.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>Giá bán</label>
            <input v-model="formData.gia" type="text" placeholder="VD: 18.000">
          </div>

          <div class="form-group">
            <label>Kích cỡ</label>
            <input v-model="formData.kichCo" type="text" placeholder="VD: Lớn, Nhỏ, 1.5L">
          </div>
          <div class="form-group">
            <label>Đơn vị tính</label>
            <input v-model="formData.donVi" type="text" placeholder="VD: Chai, Lon, Đĩa">
          </div>

           <div class="form-group full-width">
            <label>Trạng thái kinh doanh</label>
             <div class="toggle-wrapper">
                <span>{{ formData.trangThai ? 'Đang hoạt động' : 'Ngưng hoạt động' }}</span>
                <div class="toggle-switch" :class="{ 'on': formData.trangThai }" @click="formData.trangThai = !formData.trangThai">
                  <div class="toggle-knob"></div>
                </div>
             </div>
          </div>

        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="emit('close')">Hủy</button>
        <button class="btn-confirm" @click="handleSave">
          {{ detailItem ? 'Lưu thay đổi' : 'Thêm mới' }}
        </button>
      </div>

    </div>
  </div>
</template>

<style scoped>
</style>